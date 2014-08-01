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

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclAlarmsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPowerConfigurationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.security.ZclIASZoneServer;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPoint;

import java.util.Dictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZclIASZoneAppliance extends ZclAppliance {
	private ZclEndPoint endPoint = null;

	private ZclIASZoneServer iasZoneServer;

	private static final Logger LOG = LoggerFactory.getLogger( ZclIASZoneAppliance.class );

	public ZclIASZoneAppliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);

		endPoint = this.zclAddEndPoint(IEndPointTypes.ZIGBEE_IAS_ZONE);
		endPoint.addServiceCluster(new ZclBasicServer());
		endPoint.addServiceCluster(new ZclIdentifyServer());
		endPoint.addServiceCluster(new ZclPowerConfigurationServer());
		endPoint.addServiceCluster(new ZclAlarmsServer());
		iasZoneServer = (ZclIASZoneServer) endPoint.addServiceCluster(new ZclIASZoneServer());
	}

	protected synchronized void attached() {
		LOG.debug("appliance attached");

		Thread t = new Thread("setActualDriverJob") {
			public void run() {
				try {
					setActualDriver();
				} catch (ServiceClusterException e) {
					LOG.error("exception on attached for ZclASZoneAppliance", e);
				}
			}
		};
		t.start();
	}

	protected void setActualDriver() throws ServiceClusterException {
		if (iasZoneServer != null) {
			try {
				// TODO attention the following call may block the attached()
				// method. Therefore it should be asynchronous.
				int zoneType = iasZoneServer.getZoneType(null);
				switch (zoneType) { //FIXME magic numbers ?
				case 0x0015:
					LOG.debug("This is a Contact Switch IASZone Device");
					break;
					
				case 0x002a:
					LOG.debug("This is a Water Sensor IASZone Device");
					break;
				default:
					LOG.error("unknown Zone Type: " + zoneType);
					break;
				}
			} catch (ApplianceException e) {
				LOG.error("exception while reading ZoneType attribute. " + e.getMessage(),e);
			}
		}
	}

	protected void detached() {
		LOG.debug("ZclASZoneAppliance detached");
	}
}
