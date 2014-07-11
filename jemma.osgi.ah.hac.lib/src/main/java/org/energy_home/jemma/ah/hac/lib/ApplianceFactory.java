/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.ah.hac.lib;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplianceFactory;
import org.energy_home.jemma.ah.hac.IHacDevice;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.device.Driver;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApplianceRecord {
	public Appliance appliance;
	public List deviceList = new ArrayList(1);
	public ServiceRegistration applianceServiceRegistration;
	public String servicePid;
}

/**
 * The base class that all the appliance factories of the A@H framework needs to
 * extend.
 */
public abstract class ApplianceFactory implements IApplianceFactory, ManagedServiceFactory, ServiceTrackerCustomizer {

	static final Logger LOG = LoggerFactory.getLogger(ApplianceFactory.class);

	private ServiceRegistration sr;
	private ServiceRegistration driverRegistration;
	
	/** Dictionary { String service.pid, Appliance appliance} */
	Map pid2record = new HashMap();
	Map spid2record = new HashMap();

	BundleContext bc;


	ApplianceRecord getByAppliancePid(String appliancePid) {
		return (ApplianceRecord) pid2record.get(appliancePid);
	}

	ApplianceRecord getByServicePid(String servicePid) {
		return (ApplianceRecord) spid2record.get(servicePid);
	}
	
	protected final IManagedAppliance createAppliance(String pid, Dictionary config) throws ApplianceException {
		Appliance appliance;
		try {
			appliance = (Appliance) getInstance(pid, config);
			
			
			
		} catch (Exception e) {
			LOG.debug("Unable to instantiate appliance", e);
			throw new ApplianceException("Unable to instantiate appliance " + pid);
		}
	
		appliance.setApplianceFactory(this);
	
		LOG.debug("created " + appliance.getDescriptor().getType());
		return appliance;
	}
	
	/**
	 * Method invoked by the OSGi activator when the bundle starts
	 * 
	 * @param bc
	 *            The bundle context
	 * @throws Exception
	 */

	public final void start(BundleContext bc) throws Exception {
		this.bc = bc;
		
		// check if type has been correctly set.
		String type = this.getDescriptor().getType();

		if ((type == null) || (type != null) && (type.equals(""))) {
			LOG.debug("invalid type in factory");
			throw new Exception("invalid type in factory");
		}

		Hashtable props = new Hashtable();
		props.put(Constants.SERVICE_PID, type);
		this.sr = bc.registerService(new String[] { IApplianceFactory.class.getName(), ManagedServiceFactory.class.getName() },
				this, props);

		started(bc);
		LOG.debug("started appliance factory for type '" + getDescriptor().getType() + "'");
	
	}

	void started(BundleContext bc) {
		return;
	}
	
	/**
	 * Method invoked by the OSGi activator when the bundle stops
	 * 
	 * @param bc
	 *            The bundle context
	 */
	public synchronized final void stop(BundleContext bc) {
		if (driverRegistration != null) {
			driverRegistration.unregister();
			driverRegistration = null;
		}
		this.sr.unregister();
		this.sr = null;
		pid2record.keySet().iterator();
		for (Iterator iterator = pid2record.values().iterator(); iterator.hasNext();) {
			ApplianceRecord record = (ApplianceRecord) iterator.next();
			Appliance appliance = record.appliance;

			// Note: the following call has no effects if the appliance is not a
			// driver.

			if (appliance != null) {
				if (appliance.isDriver() && record.deviceList != null) {
					((DriverApplianceFactory)this).untrackAllDevices(record, false);
					try {
						// Multieps: in equinox detach is called by untrackDevice (closing service tracker trigger a service removed event)
						for (Iterator iterator2 = record.deviceList.iterator(); iterator2.hasNext();) {
							IHacDevice device = (IHacDevice) iterator2.next();
							((DriverAppliance)appliance).detach(device);	
						}
					} catch (ApplianceException e) {
						LOG.warn(e.getMessage(), e);
					}
					
				}

				appliance.stop();

				if (record.applianceServiceRegistration != null) {
					record.applianceServiceRegistration.unregister();
					record.applianceServiceRegistration = null;
				}

				appliance = null;
			} else {
				return;
			}
		}

		pid2record.clear();
		spid2record.clear();
	}

	
	
	/**
	 * Internal method used by the A@H framework
	 */
	public final void init() {
		Hashtable props = new Hashtable();
		if (this instanceof Driver) {
			props.put(org.osgi.service.device.Constants.DRIVER_ID, getDescriptor().getType());
			driverRegistration = bc.registerService(new String[] { Driver.class.getName() }, this, props);
		}
	}

	/**
	 * Internal method used by the appliances proxy service (servicePid = permanentId)
	 */
	public final boolean updateAppliance(String servicePid, Map config) {
		// TODO: not used yet, still needs to be tested
		try {
			updated(servicePid, new Hashtable(config));
		} catch (Exception e) {
			LOG.warn("updated error", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Internal method used by the A@H framework
	 */
	public synchronized final void updated(String servicePid, Dictionary config) throws ConfigurationException {
		ApplianceRecord record = (ApplianceRecord) spid2record.get(servicePid);

		if (record == null) {
			String appliancePid;
			appliancePid = (String) config.get("appliance.pid");
			if (appliancePid == null) {
				LOG.debug("props don't contain the appliance.pid property in factory " + this.getName());
				throw new ConfigurationException(null, "props don't contain the appliance.pid property in factory "
						+ this.getName());
			}

			// checks if this appliance.pid already belongs to an already
			// existing appliance

			record = (ApplianceRecord) this.getByAppliancePid(appliancePid);
			Appliance appliance;

			if (record == null) {
				// this is a brand new configuration on a brand new appliance
				try {
					appliance = (Appliance) createAppliance(appliancePid, config);
				} catch (ApplianceException e) {
					throw new ConfigurationException(null, e.getMessage());
				}

				record = new ApplianceRecord();

				try {
					appliance.updateConfig(config);

					record.appliance = appliance;
					record.servicePid = servicePid;
					spid2record.put(servicePid, record);
					pid2record.put(appliancePid, record);

					record.appliance.start();
					ServiceRegistration sReg = bc.registerService(IManagedAppliance.class.getName(), appliance, config);
					record.applianceServiceRegistration = sReg;
				} catch (ApplianceException e) {
					throw new ConfigurationException(null, e.getMessage());
				}
			} else {
				// this is a new configuration on a already existing appliance
				try {
					// bc.record.applianceServiceRegistration.
					record.appliance.updateConfig(config);
					record.applianceServiceRegistration.setProperties(config);
				} catch (ApplianceException e) {
					throw new ConfigurationException(null, e.getMessage());
				}
				spid2record.put(servicePid, record);
			}

		} else {
			try {
				record.appliance.updateConfig(config);
				record.applianceServiceRegistration.setProperties(config);
			} catch (ApplianceException e) {
				throw new ConfigurationException(null, e.getMessage());
			}
		}
	}
	
	/**
	 * Internal method used by the appliances proxy service (servicePid = permanentId)
	 */
	public final boolean deleteAppliance(String servicePid) {
		// TODO: not used yet, still needs to be tested
		try {
			deleted(servicePid);
		} catch (Exception e) {
			LOG.debug("updated error", e);
			return false;
		}
		return true;
	}
	
	public final void deleteAppliance(String appliancePid, boolean removeDevices) {
		ApplianceRecord record = this.getByAppliancePid(appliancePid);
		if (record == null) {
			LOG.warn("record==null should not happen here!!!");
			return;
		}
		Appliance appliance = record.appliance;
		if (appliance != null) {
			if (appliance.isDriver() && record.deviceList != null) {
				((DriverApplianceFactory)this).untrackAllDevices(record, removeDevices);
				try {
					// !!!Multieps: in equinox detach is called by untrackDevice (closing service tracker trigger a service removed event)
					for (Iterator iterator = record.deviceList.iterator(); iterator.hasNext();) {
						IHacDevice device = (IHacDevice) iterator.next();
						((DriverAppliance)appliance).detach(device);	
					}
				} catch (ApplianceException e) {
					LOG.warn(e.getMessage(), e);
				}

			}

			appliance.stop();

			if (record.applianceServiceRegistration != null) {
				record.applianceServiceRegistration.unregister();
				record.applianceServiceRegistration = null;
			}

			this.spid2record.remove(record.servicePid);
			this.pid2record.remove(record.appliance.getPid());

		} else {
			return;
		}
		
	}
	
	/**
	 * Internal method used by the A@H framework
	 */
	public synchronized final void deleted(String servicePid) {
		ApplianceRecord record = this.getByServicePid(servicePid);
		if (record == null) {
			LOG.warn("record==null should not happen here!!!");
			return;
		}
		Appliance appliance = record.appliance;
		if (appliance != null) {
			if (appliance.isDriver() && record.deviceList != null) {
				((DriverApplianceFactory)this).untrackAllDevices(record, true);
				try {
					// !!!Multieps: in equinox detach is called by untrackDevice (closing service tracker trigger a service removed event)
					for (Iterator iterator = record.deviceList.iterator(); iterator.hasNext();) {
						IHacDevice device = (IHacDevice) iterator.next();
						((DriverAppliance)appliance).detach(device);	
					}
				} catch (ApplianceException e) {
					LOG.warn(e.getMessage(), e);
				}

			}

			appliance.stop();

			if (record.applianceServiceRegistration != null) {
				record.applianceServiceRegistration.unregister();
				record.applianceServiceRegistration = null;
			}

			this.spid2record.remove(servicePid);
			this.pid2record.remove(record.appliance.getPid());

			appliance = null;
		} else {
			return;
		}			

	}

	/**
	 * Internal method used by the A@H framework
	 */
	public synchronized final Object addingService(ServiceReference s) {
		return s;
	}

	/**
	 * Internal method used by the A@H framework
	 */
	public synchronized final void modifiedService(ServiceReference s, Object service) {
	}

	/**
	 * Internal method used by the A@H framework
	 */
	public synchronized final void removedService(ServiceReference s, Object service) {
		String devicePid = (String) s.getProperty("service.pid");
		if (devicePid == null) {
			return;
		}

		String pid = "ah.app." + devicePid;
		ApplianceRecord record = this.getByAppliancePid(pid);

		if (record == null) {
			LOG.debug("unexisting pid");
			return;
		}

		Appliance appliance = record.appliance;
		if (appliance != null) {
			if (appliance.isDriver() && record.deviceList != null) {
				for (Iterator iterator = record.deviceList.iterator(); iterator.hasNext();) {
					IHacDevice device = (IHacDevice) iterator.next();
					if (bc.getService(s).equals(device))
						try {
							((DriverAppliance)appliance).detach(device);
							iterator.remove();
						} catch (ApplianceException e) {
							LOG.warn(e.getMessage(), e);
						}					
				}
			}
		}
		((DriverApplianceFactory)this).trackedDevices.remove(s);
	}

	/**
	 * Internal method used by the A@H framework
	 */
	public final synchronized String getName() {
		return this.getDescriptor().getType();
	}

	/**
	 * This method needs to be implemented in each factory class; it returns the
	 * specific descriptor associated to the appliance class managed by the
	 * factory
	 * 
	 */
	public abstract IApplianceDescriptor getDescriptor();
	
	/**
	 * This method needs to be implemented by each factory class and returns an
	 * instance of the managed appliance class 
	 * 
	 * @param pid
	 *            The PID of the appliance instance
	 * @param config
	 *            The initial configuration for the appliance instance
	 * @return The create appliance object
	 */
	protected abstract Appliance getInstance(String pid, Dictionary config) throws ApplianceException;
}