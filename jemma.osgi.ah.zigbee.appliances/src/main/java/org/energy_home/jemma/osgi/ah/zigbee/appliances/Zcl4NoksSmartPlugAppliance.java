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
package org.energy_home.jemma.osgi.ah.zigbee.appliances;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.custom.ZclSimpleMetering4NoksServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffServer;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPoint;

import java.util.Dictionary;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Zcl4NoksSmartPlugAppliance extends ZclAppliance {
	private ZclEndPoint endPoint = null;

	private static final Log log = LogFactory.getLog(Zcl4NoksSmartPlugAppliance.class);

	public static final String ENDPOINT_TYPE = "EnergyMetering";

	public Zcl4NoksSmartPlugAppliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);

		endPoint = this.zclAddEndPoint(ENDPOINT_TYPE);

		// Server Clusters
		endPoint.addServiceCluster(new ZclOnOffServer());
		endPoint.addServiceCluster(new ZclBasicServer());
		endPoint.addServiceCluster(new ZclIdentifyServer());
		endPoint.addServiceCluster(new ZclSimpleMetering4NoksServer());
	}

	protected void attached() {
		log.debug("attached");
	}

	protected void detached() {
		log.debug("detached");
	}

}
