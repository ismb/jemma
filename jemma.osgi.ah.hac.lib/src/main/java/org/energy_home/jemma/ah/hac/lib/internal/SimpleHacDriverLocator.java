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
package org.energy_home.jemma.ah.hac.lib.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Vector;

import org.energy_home.jemma.ah.hac.IAppliance;
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
public class SimpleHacDriverLocator implements DriverLocator {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHacDriverLocator.class);


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
		String driverId = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);
		if (driverId != null) {
			// driver id is specified, so we have our virtual appliance type
			// that matches the properties
			return new String[] { driverId };
		}

		// the driver_id properties is not specified, we return null because
		// currently we don't provide any mechanism to find out a driver_id by
		// examining the passed properties.

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
		if (driverIds.size() > 0) {
			String[] drivers = new String[driverIds.size()];
			for (int i = 0; i < driverIds.size(); i++) {
				drivers[i] = (String) driverIds.get(i);
			}

			return drivers;
		} else {
			LOG.warn("unable to find a driver id for device " + props);
		}

		return null;
	}

	/**
	 * Download and install the bundle that implements the passed vaType
	 * parameter. This is called only if no suitable driver is currently
	 * installed in OSGi.
	 */

	public InputStream loadDriver(String vaType) throws IOException {
		// TODO: not yet implemented
		return null;
	}
}
