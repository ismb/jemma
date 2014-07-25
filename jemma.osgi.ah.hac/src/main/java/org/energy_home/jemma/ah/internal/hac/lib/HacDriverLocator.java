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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Vector;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Property;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Resource;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.device.DriverLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the OSGi Device Admin service {@link DriverLocator
 * interface}. This DriverLocator implementation is used by the HAC to locate
 * and, if necessary, install a missing Virtual Appliance driver. The current
 * implementation has the following limitations:
 * <ul>
 * <li>Device recognition is static</li>
 * <li>Installation of device bundles is not performed</li>
 * </ul>
 */
public class HacDriverLocator implements DriverLocator {

	private String repositoryPath = "D:\\A@H\\bundles\\";

	private RepositoryAdmin repositoryAdmin;
	private ConfigurationAdmin configAdmin;

	private Object properties;
	
	private static final Logger LOG = LoggerFactory.getLogger(HacDriverLocator.class);

	public void activate(BundleContext bc) {
		// the following property is used to specify the driver repository
		// location. The repository location may be local or remote. Examples of
		// valid locations are;
		// http://www.myrepository.it/repository
		// file://C:/repository

		String repositoryPath = bc.getProperty("org.energy_home.jemma.ah.hac.repository");
		if (repositoryPath != null) {
			this.repositoryPath = repositoryPath;
		}
	}

	public void setRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
		this.repositoryAdmin = repositoryAdmin;
	}

	public synchronized void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	public synchronized void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
		if (this.configAdmin == configAdmin) {
			this.configAdmin = null;
		}
	}

	public void unsetRepositoryAdmin(RepositoryAdmin repositoryAdmin) {
		if (this.repositoryAdmin == repositoryAdmin)
			this.repositoryAdmin = null;
	}

	/**
	 * Returns the DRIVER_IDs that matches the passed properties. This
	 * implementation of the DriverLocator interface behaves in this way: If the
	 * passed props Dictionary contains the "org.telecomitalia.hac.type"
	 * property, it returns just one DRIVER_IDs: the ID of the factory driver
	 * that is able to create instance of the requested driver. On the other
	 * hand it returns two DRIVER_IDs: the persistent ID of the requested
	 * driver, and the driver type.
	 * 
	 * @param props
	 *            Properties that have to be used to find the correct DRIVER_ID
	 * @return an array that contains the DRIVER_IDs.
	 */

	public String[] findDrivers(Dictionary props) {

		if (this.configAdmin == null) {
			return null;
		}

		try {
			String servicePid = (String) props.get("service.pid");
			Configuration[] configurations = this.configAdmin.listConfigurations("(device.serial=" + servicePid + ")");
			// if (configurations == null) {
			// // this device not in the DB, returns configurator pid
			// return new String[] { "configurator.pid" };
			// }
		} catch (IOException e) {
			LOG.warn(e.getMessage(), e);
		} catch (InvalidSyntaxException e) {
			LOG.warn(e.getMessage(), e);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}

		String driverId = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);
		if (driverId != null) {
			// driver id is specified, so we have our virtual appliance type
			// that matches the properties
			return new String[] { driverId };
		}

		// the driver_id properties is not specified, we return null because
		// currently we don't provide any mechanism to find out a driver_id by
		// examining the passed properties.

		// FIXME: Not yet implemented: find out an ApplicationFactory that
		// matches the
		// passed properties.

		// we suggest as DRIVER_ID the concatenation of the DEVICE_CATEGORY and
		// the DEVICE_SERIAL
		// properties. In A@H the DRIVER_ID matches the
		// {DEVICE_CATEGORY}.{DEVICE_SERIAL}

		Vector driverIds = new Vector();

		String deviceCategory = (String) props.get("DEVICE_CATEGORY");
		String deviceSerial = (String) props.get("DEVICE_SERIAL");

		if ((deviceCategory != null) && (deviceSerial != null)) {
			driverId = deviceCategory.toLowerCase() + "." + deviceSerial;
			driverIds.add(driverId);
		}

//!!!!!!Multieps: sembra che vengano comunque restituiti tutti i driver registrati (viene ancora usato device_serial per match su driver gia allocato?)		
		
		// select a set of DRIVER_IDs that may match the requested driver
		// the following code should be technology independent
/*
		Integer profile_id = null;
		Integer device_id = null;
		Integer manufacturer_code = null;
		String[] nodeEndPoints = null;

		try {
			profile_id = (Integer) props.get("zigbee.device.profile.id");
			device_id = (Integer) props.get("zigbee.device.device.id");
			manufacturer_code = (Integer) props.get("zigbee.device.manufacturer.id");
			nodeEndPoints = (String[]) props.get("zigbee.device.eps");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		driverIds.add("it.telecomitalia.ah.zigbee.generic");

		if ((nodeEndPoints != null) && (nodeEndPoints.length == 1) && (profile_id != null) && (device_id != null) && (manufacturer_code != null)) {
			int p_id = profile_id.intValue();
			int d_id = device_id.intValue();
			int m_code = manufacturer_code.intValue();

			if ((p_id == 0xC044) && (d_id == 0x0501)) {
				driverIds.add(new String("org.4noks.smartplug"));
			} else if (p_id == 0x0104) {
				switch (d_id) {

				case 0x0000:
					driverIds.add(new String("fr.cleode.ah.zigbee.switch"));
					break;

				case 0x0008:
					driverIds.add(new String("it.telecomitalia.ah.zigbee.rangeextender"));
					break;

				case 0x0009:
					driverIds.add(new String("org.zigbee.ha.mainspwoutlet"));
					break;

				case 0x0101:
					driverIds.add(new String("org.zigbee.ha.wcs10a"));
					break;

				case 0x0107:
					driverIds.add(new String("fr.cleode.ah.zigbee.zmove"));
					break;

				case 0x0301:
					if (m_code == 0x1071) {
						driverIds.add(new String("it.telecomitalia.zigbee.4noks.thermostat"));
					} else {
						driverIds.add(new String("it.telecomitalia.zigbee.thermostat"));
					}
					break;

				case 0x0302:
					driverIds.add(new String("fr.cleode.ah.zigbee.temperaturesensor"));
					break;

				case 0x0402:
					driverIds.add(new String("fr.cleode.ah.zigbee.zdoor"));
					break;

				case 0x0501:
				case 0x0053:
					driverIds.add(new String("it.telecomitalia.ah.zigbee.metering"));
					break;

				case 0x0504:
				case 0x0051:
					driverIds.add(new String("it.telecomitalia.zigbee.smartplug"));
					break;

				case 0x0A04:
				case 0x0A00:
				case 0x0052:
					switch (m_code) {
					case 0x10C8:
						// Electrolux Whitegood
						driverIds.add(new String("it.electrolux.ah.app.whitegood"));
						break;
					case 0x10CD:
						// Indesit Whitegood
						driverIds.add(new String("com.indesit.ah.app.whitegood"));
						break;

					default:
						// Defaults to Indesit Whitegood
						driverIds.add(new String("com.indesit.ah.app.whitegood"));
						break;
					}
					break;

				default:
					driverIds.add(new String("org.zigbee.ha"));
					break;
				}
			} else if (p_id == 0xC23C) {
				// Old Energy@Home profile
				switch (d_id) {
				case 0x0008:
					// this is a router device
					driverIds.add(new String("org.ti.router"));
					break;
				case 0x0501:
					// Energy@Home Metering Device
					driverIds.add(new String("it.telecomitalia.ah.zigbee.metering"));
					break;

				case 0x0A04:
				case 0x0A00:
					switch (m_code) {
					case 0x10C8:
					case 0x10CD:
						// Electrolux Whitegood
						driverIds.add(new String("it.electrolux.ah.app.whitegood"));
						break;

					default:
						// Indesit Whitegood
						driverIds.add(new String("com.indesit.ah.app.whitegood"));
						break;
					}
					break;

				default:
					break;
				}
			} else if (p_id == 0xA1E0) {
				if (d_id == 0x0001) {
					driverIds.add(new String("it.telecomitalia.ah.zigbee.testharness"));
				}
			} else if (p_id == 0x0107) {
				// Telecom ZigBee Profile
				switch (d_id) {
				case 0x0101: // ZIN device
					driverIds.add(new String("it.telecomitalia.ah.zigbee.zin"));
					break;

				default:
					break;
				}
			} else {
				// default driver for a ZCL appliance
				driverIds.add(new String("it.telecomitalia.ah.driver.zcl"));
			}
		} else if (deviceCategory.equals("Plugwise")) {
			// default driver for the device
			driverIds.add(new String("org.telecomitalia.plugwise.circle"));
		}
*/
		if (driverIds.size() > 0) {
			String[] drivers = new String[driverIds.size()];
			for (int i = 0; i < driverIds.size(); i++) {
				drivers[i] = (String) driverIds.get(i);
			}

			return drivers;
		} else {
			LOG.debug("unable to find a driver id for device " + props);
		}

		return null;
	}

	protected void resolveBundles(String filter) throws InvalidSyntaxException {
		if (this.repositoryAdmin == null) {
			return;
		}

		org.apache.felix.bundlerepository.Resolver resolver = this.repositoryAdmin.resolver();
		Repository[] repositories = this.repositoryAdmin.listRepositories();
		Resource[] resources = this.repositoryAdmin.discoverResources(filter);
		for (int i = 0; i < resources.length; i++) {
			LOG.debug(resources[i].getId() + ":");
			Capability[] capabilities = resources[i].getCapabilities();
			// log.debug(capabilities);
			for (int j = 0; j < capabilities.length; j++) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("\t" + capabilities[j].getName() + ":");
				}
				Property[] props = capabilities[j].getProperties();
				if (LOG.isDebugEnabled()) {
					for (int k = 0; k < props.length; k++) {
						LOG.debug("\t\tname=" + props[k].getName() + ", value=" + props[k].getValue());
					}
				}
			}
		}
	}

	/**
	 * Download and install the bundle that implements the passed vaType
	 * parameter. This is called only if no suitable driver is currently
	 * installed in OSGi.
	 */

	public InputStream loadDriver(String vaType) throws IOException {
		// Declare the file input stream.
		InputStream stream = null;

		try {
			if (vaType.equals("org.telecomitalia.hac.drivers.energyathome.sm")) {
				stream = new FileInputStream(this.repositoryPath + "org.telecomitalia.hac.energyathome_1.0.3.jar");
			} else if (vaType.equals("org.4noks.smartplug")) {
				return null;
			}
		} catch (Exception e) {
			LOG.debug("Exception: file not found" + e.getMessage());
		}

		return stream;
	}
}
