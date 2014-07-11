/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2014 Istituto Superiore Mario Boella (http://www.ismb.it)
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
package org.energy_home.jemma.ah.zigbee.appliances;

import java.util.Dictionary;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.DriverApplianceFactory;
import org.osgi.service.device.Driver;

/**
 * 
 * @author ISMB-Pert
 *
 *  bitronhome 4-Buttons Remote Control  
 *   * the ZigbeeManger Match filter
 * Adding bitronhome Remote Control [deviceProperties Key Value pairs]
 * 		{
 * 			zigbee.device.eps.number=1,
 * 			zigbee.device.device.id=6,
 * 			zigbee.device.eps=[Ljava.lang.String;@f0f6ac,
 * 			service.pid=5149013002559736,
 * 			zigbee.device.ep.id=1,
 * 			DEVICE_SERIAL=5149013002559736, 
 * 			DEVICE_CATEGORY=ZigBee, 
 * 			zigbee.device.profile.id=260, 
 * 			zigbee.device.manufacturer.id=0
 * 		}
 */

public class ZclBitronhomeRemoteControlApplianceFactory extends DriverApplianceFactory implements Driver {
	public static final String APPLIANCE_TYPE = "org.energy_home.jemma.ah.zigbee.bitronhome.remotecontrol";
	public static final String APPLIANCE_FRIENDLY_NAME = "bitronhome remote control [which includes 4 buttons]";
	public static final String DEVICE_TYPE = "ZigBee";

	public static final IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
			APPLIANCE_FRIENDLY_NAME);

	public IApplianceDescriptor getDescriptor() {
		return APPLIANCE_DESCRIPTOR;
	}

	public Appliance getInstance(String pid, Dictionary config) throws ApplianceException {
		return new ZclBitronhomeRemoteControlAppliance(pid, config);
	}

	public String deviceMatchFilterString() {
		String result = "(&(DEVICE_CATEGORY=ZigBee)(zigbee.device.eps.number=1)(zigbee.device.profile.id=260)(zigbee.device.device.id=6)(zigbee.device.manufacturer.id=0))"; 	
		return result; 
		
	}
}