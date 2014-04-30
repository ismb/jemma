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
package org.energy_home.jemma.ah.zigbee.appliances;

import org.energy_home.jemma.ah.hac.lib.ApplianceFactory;

import java.util.Iterator;
import java.util.Vector;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private Vector applicationFactories = new Vector();

	public void start(BundleContext bc) throws Exception {
		/**
		 * @brief adding Appliance Factory here
		 * bitronhome SMART-PLUG and Remote COntrol  by ISMB-Pert   
		 */		
		applicationFactories.add(new ZclBitronhomeSmartPlugApplianceFactory());
		applicationFactories.add(new ZclBitronhomeRemoteControlApplianceFactory());
		applicationFactories.add(new ZclURMETTemeratureHumidityApplianceFactory());
		applicationFactories.add(new ZclURMETSmokeDetectorSirenApplianceFactory());
		/**
		 * Ends bitronhome 
		 */
		// applicationFactories.add(new ZigbeeWhiteGoodApplianceFactory());
		applicationFactories.add(new ZigbeeMeteringApplianceFactory());
		applicationFactories.add(new ZclZinApplianceFactory());
		// applicationFactories.add(new Zcl4NoksSmartPlugApplianceFactory());
		applicationFactories.add(new ZclSmartPlugApplianceFactory());
		applicationFactories.add(new ZclZoppasDisplayApplianceFactory());
		applicationFactories.add(new Zcl4NoksThermostatApplianceFactory());
		applicationFactories.add(new ZclRangeExtenderApplianceFactory());
		applicationFactories.add(new ZclDimmableLightApplianceFactory());
		applicationFactories.add(new ZclColorLightApplianceFactory());
		applicationFactories.add(new ZclIASZoneApplianceFactory());
		Iterator it = applicationFactories.iterator();

		while (it.hasNext()) {
			ApplianceFactory applianceFactory = (ApplianceFactory) it.next();
			applianceFactory.start(bc);
		}
	}

	public void stop(BundleContext bc) throws Exception {
		Iterator it = applicationFactories.iterator();

		while (it.hasNext()) {
			ApplianceFactory applicationFactory = (ApplianceFactory) it.next();
			applicationFactory.stop(bc);
		}
	}
}