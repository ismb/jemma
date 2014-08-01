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

import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class IASZoneClientCluster extends ServiceCluster implements IASZoneClient {	
	public IASZoneClientCluster() throws ApplianceException {
		super();
	}
	
	// HA 1.1
	public void execZoneStatusChangeNotification(int zoneStatus, short extendedStatus, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
	}
	
	// HA 1.2
	public void execZoneStatusChangeNotification(int zoneStatus, short extendedStatus, short zoneID, int delay,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		execZoneStatusChangeNotification(zoneStatus, extendedStatus, context);	
	}

	// Some sensor needs to receive a response to execZoneEnrollRequest during initial configuration phase
	public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		ZoneEnrollResponse response = new ZoneEnrollResponse();
		return response;
	}

}
