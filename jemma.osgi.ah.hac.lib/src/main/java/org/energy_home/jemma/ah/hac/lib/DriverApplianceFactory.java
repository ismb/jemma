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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IHacDevice;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.device.Device;
import org.osgi.util.tracker.ServiceTracker;

public abstract class DriverApplianceFactory extends ApplianceFactory {
	private Filter deviceFilter = null;
	private Filter classFilter = null;

	protected boolean enableAutoInstall = false;
	
	HashMap trackedDevices = new HashMap();
	HashMap appliance2tracker = new HashMap();	
	
	void started(BundleContext bc) {
		String enableAutoInstallStr = this.bc.getProperty("org.energy_home.jemma.ah.driver.autoinstall");
		if (!isNullOrEmpty(enableAutoInstallStr)) {
			try {
				enableAutoInstall = Boolean.parseBoolean(enableAutoInstallStr);
			} catch (Exception e) {
				LOG.warn(e.getMessage(), e);
			}
		}
	}
	
	private static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}
	
	/**
	 * Method used only by driver implementation (not used for {@code logical
	 * appliances} factories)
	 * 
	 * @parameter s
	 */
	public synchronized final String attach(ServiceReference s) throws Exception {

		String devicePid = (String) s.getProperty("service.pid");
		if (devicePid == null) {
			throw new Exception("The target driver must have a pid property set");
		}

		// force creating the Appliance if it doesn't exist, yet
		// no configuration available
		String appliancePid = "ah.app." + devicePid;
		ApplianceRecord record = this.getByAppliancePid(appliancePid);

		if (record == null) {
			// Appliance doesn't exist, attach it and starts it with an
			// installing status
			Hashtable config = new Hashtable();
			config.put(IAppliance.APPLIANCE_TYPE_PROPERTY, this.getDescriptor().getType());
			config.put(IAppliance.APPLIANCE_PID, appliancePid);
			Appliance appliance = (Appliance) createAppliance(appliancePid, config);

			if (appliance != null) {
				IHacDevice device = (IHacDevice) bc.getService(s);
				record = new ApplianceRecord();
				record.appliance = appliance;
				record.deviceList.add(device);

				appliance.start();
				((DriverAppliance)appliance).attach(device);

				trackDevice(device, s);
				if (appliance.isAvailable) {
					config.put(IAppliance.APPLIANCE_EPS_IDS_PROPERTY, appliance.getEndPointIds());
					config.put(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY, appliance.getEndPointTypes());
					
					Dictionary customConfig = ((DriverAppliance)record.appliance).getCustomConfiguration();
					if (customConfig != null) {				
						for (Enumeration e = customConfig.keys(); e.hasMoreElements();) {
							String key = (String)e.nextElement();
							// Custom properties that are invalid are filtered
							if (key.startsWith(IAppliance.APPLIANCE_CUSTOM_PROPERTIES_PREXIF));
								config.put(key, customConfig.get(key));
						}
					}
					
					if (!enableAutoInstall)
						config.put("ah.status", "installing");		
					ServiceRegistration sr = bc.registerService(IManagedAppliance.class.getName(), appliance, config);
					record.applianceServiceRegistration = sr;
				}
				pid2record.put(appliancePid, record);
			}
		} else {
			// The Appliance already exist, simply attach it, because it is
			// already started
			IHacDevice device = (IHacDevice) bc.getService(s);
			record.deviceList.add(device);
			((DriverAppliance)record.appliance).attach(device);
			trackDevice(device, s);
			// When the following condition could be true? 
			if (record.applianceServiceRegistration == null) {
				if (record.appliance.isAvailable()) {
					Hashtable config = new Hashtable();
					config.put(IAppliance.APPLIANCE_TYPE_PROPERTY, this.getDescriptor().getType());
					config.put(IAppliance.APPLIANCE_PID, appliancePid);
					config.put(IAppliance.APPLIANCE_EPS_IDS_PROPERTY, record.appliance.getEndPointIds());
					config.put(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY, record.appliance.getEndPointTypes());
					
					Dictionary customConfig = ((DriverAppliance)record.appliance).getCustomConfiguration();
					if (customConfig != null) {				
						for (Enumeration e = customConfig.keys(); e.hasMoreElements();) {
							String key = (String)e.nextElement();
							// Custom properties that are invalid are filtered
							if (key.startsWith(IAppliance.APPLIANCE_CUSTOM_PROPERTIES_PREXIF));
								config.put(key, customConfig.get(key));
						}
					}
					
					if (!enableAutoInstall)
						config.put("ah.status", "installing");					
					ServiceRegistration sr = bc.registerService(IManagedAppliance.class.getName(), record.appliance, config);
					record.applianceServiceRegistration = sr;					
				}					
			}
		}

		return null;
	}

	private void trackDevice(IHacDevice device, ServiceReference s) {
		ServiceTracker deviceTracker = new ServiceTracker(bc, s, this);
		trackedDevices.put(s, deviceTracker);
		this.appliance2tracker.put(device, deviceTracker);
		deviceTracker.open();
	}

	private void untrackDevice(IHacDevice device) {
		ServiceTracker deviceTracker = (ServiceTracker) appliance2tracker.remove(device);
		if (deviceTracker != null) {
			deviceTracker.close();
		}
	}
	
	void untrackAllDevices(ApplianceRecord record, boolean removeDevice) {
		IHacDevice[] devices = new IHacDevice[record.deviceList.size()];
		int i = 0;
		for (Iterator iterator = record.deviceList.iterator(); iterator.hasNext();) {
			IHacDevice device = (IHacDevice) iterator.next();
			devices[i++] = device;	
		}
		// Multieps: an array is needed to avoid concurrent modification in deviceList while using another iterator
		for (int j = 0; j < devices.length; j++) {
			untrackDevice(devices[j]);	
			if (removeDevice)
				devices[j].remove();
		}
	}

	public String deviceMatchFilterString() {
		return null;
	}

	public String classMatchFilterString() {
		return null;
	}

	public boolean genericMatch(ServiceReference d) {
		return false;
	}
	
	public int match(ServiceReference d) {	
		int matchValue;
		String filterString;

		if (deviceFilter == null) {
			try {
				filterString = deviceMatchFilterString();
				if (filterString != null)
					deviceFilter = bc.createFilter(filterString);
			} catch (InvalidSyntaxException e) {
				LOG.warn("Error in deviceMatchFilterString LDAP expression", e);
			}
		}

		if (classFilter == null) {
			try {
				filterString = classMatchFilterString();
				if (filterString != null)
					classFilter = bc.createFilter(filterString);
			} catch (InvalidSyntaxException e) {
				LOG.warn("Error in classMatchFilterString LDAP expression", e);
			}
		}
		
		if ((deviceFilter != null) && deviceFilter.match(d)) {
			matchValue = 10;
		} else if ((classFilter != null) && classFilter.match(d)) {
			matchValue = 5;
		}  else if (genericMatch(d)) {
			matchValue = 1;
		} else 
			matchValue = Device.MATCH_NONE;
		return matchValue;
	}
}
