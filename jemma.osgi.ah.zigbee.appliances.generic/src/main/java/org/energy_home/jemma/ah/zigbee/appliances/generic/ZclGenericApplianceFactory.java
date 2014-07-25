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
package org.energy_home.jemma.ah.zigbee.appliances.generic;


import java.util.Dictionary;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.DriverApplianceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.service.device.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZclGenericApplianceFactory extends DriverApplianceFactory implements Driver {
	private static final Logger LOG = LoggerFactory.getLogger( ZclGenericApplianceFactory.class );
	
	public static final String APPLIANCE_TYPE = "org.energy_home.jemma.ah.zigbee.generic";
	public static final String APPLIANCE_FRIENDLY_NAME = "Generic";
	public static final String DEVICE_TYPE = "ZigBee";
	
	public static final IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
			APPLIANCE_FRIENDLY_NAME);

	public IApplianceDescriptor getDescriptor() {
		return APPLIANCE_DESCRIPTOR;
	}

	public Appliance getInstance(String pid, Dictionary config) throws ApplianceException {
		return new ZclGenericAppliance(pid, config);
	}
	
	public boolean genericMatch(ServiceReference d) {
		String[] endPoints = (String[]) d.getProperty("zigbee.device.eps");
		if (endPoints == null || endPoints.length < 1) {
			LOG.error("Null or invalid zigbee.device.eps property value");
			return false;
		}
//		if (endPoints.length == 1) {
//			if (endPoints[0].startsWith("260.82"))
//				// Whitegood appliance
//				return false;
//			if (endPoints[0].startsWith("260.81"))
//				// Smartplug
//				return false;
//			if (endPoints[0].startsWith("260.83"))
//				// SmartInfo
//				return false;
//		}
		return true;
	}

}
