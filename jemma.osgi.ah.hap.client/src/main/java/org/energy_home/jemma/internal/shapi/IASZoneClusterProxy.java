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
package org.energy_home.jemma.internal.shapi;

import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hap.client.AHContainers;


public class IASZoneClusterProxy extends ServiceClusterProxy implements IASZoneClient {	
	public IASZoneClusterProxy(ApplianceProxyList applianceProxyList, AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
	}

	protected String getAttributeId(String attributeName) {
		if (attributeName.equals(IASZoneServer.ATTR_ZoneStatus_NAME))
			return AHContainers.attrId_ah_cluster_iascontact_open;
		return null;
	}

	protected Object decodeAttributeValue(String appliancePid, int endPointId, String attributeName, Object value) {
		if (attributeName.equals(IASZoneServer.ATTR_ZoneStatus_NAME)) {
			boolean booleanValue = (((Integer)value).intValue() & 0x01) > 0;
			return new Boolean(booleanValue);
		}
		return value;
	}
	
	// HA 1.1
	public void execZoneStatusChangeNotification(int zoneStatus, short extendedStatus, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		IEndPoint peerEndPoint = context.getPeerEndPoint();
		IAppliance peerAppliance = peerEndPoint.getAppliance();
		String appliancePid = peerEndPoint.getAppliance().getPid();
		int endPointId = peerEndPoint.getId();
		LOG.debug("execZoneStatusChangeNotification: " + zoneStatus + " from appliance " + appliancePid 
				+ " (" + peerAppliance.getDescriptor().getType() +  "), end point id " + endPointId
				+ " (" + peerEndPoint.getType() + ")");	
		Object objectValue = decodeAttributeValue(appliancePid, endPointId, IASZoneServer.ATTR_ZoneStatus_NAME, zoneStatus);
		try {
			sendAttributeValue(appliancePid, endPointId, AHContainers.attrId_ah_cluster_iascontact_open, System.currentTimeMillis(), objectValue, 
					true);
		} catch (Exception e) {
			LOG.error("Error while managing execZoneStatusChangeNotification", e);
		}	
	}
	
	// HA 1.2
	public void execZoneStatusChangeNotification(int zoneStatus, short extendedStatus, short zoneID, int delay,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		execZoneStatusChangeNotification(zoneStatus, extendedStatus, context);	
	}

	public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		IEndPoint peerEndPoint = context.getPeerEndPoint();
		IAppliance peerAppliance = peerEndPoint.getAppliance();
		String appliancePid = peerEndPoint.getAppliance().getPid();
		int endPointId = peerEndPoint.getId();
		LOG.debug("execZoneEnrollRequest: zoneType=" + ZoneType + ", manufacturerCode=" + ManufacturerCode + " from appliance " + appliancePid 
				+ " (" + peerAppliance.getDescriptor().getType() +  "), end point id " + endPointId
				+ " (" + peerEndPoint.getType() + ")");	
		ZoneEnrollResponse response = new ZoneEnrollResponse();
		return response;
	}

}
