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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.eh;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceEventsAndAlertsClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceEventsAndAlertsServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetAlertsResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclApplianceEventsAndAlertsServer extends ZclServiceCluster implements ApplianceEventsAndAlertsServer,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2818;

	public ZclApplianceEventsAndAlertsServer() throws ApplianceException {
		super();
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		boolean handled;
		handled = super.notifyZclFrame(clusterId, zclFrame);
		if (handled) {
			return handled;
		}
		int commandId = zclFrame.getCommandId();
		if (zclFrame.isClientToServer()) {
			throw new ZclValidationException("invalid direction field");
		}
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		ApplianceEventsAndAlertsClient c = ((ApplianceEventsAndAlertsClient) getSinglePeerClusterNoException((ApplianceEventsAndAlertsClient.class
				.getName())));
		switch (commandId) {
		case 1:
			responseZclFrame = parseAlertsNotification(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseEventNotification(c, zclFrame);
			break;
		
		default:
			return false;
		}
		
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		else {
			device.post(ZclApplianceEventsAndAlertsServer.CLUSTER_ID, responseZclFrame);
		}
		return true;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public GetAlertsResponse execGetAlerts(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(0);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
		return (ZclGetAlertsResponse.zclParse(zclResponseFrame));
	}

	protected IZclFrame parseAlertsNotification(ApplianceEventsAndAlertsClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		int size;
		size = ZclDataTypeUI8.zclParse(zclFrame);
		int[] Events;
		Events = new int[size];
		int i;
		for (i = 0; (i < size); i++) {
			Events[i] = ZclDataTypeUI24.zclParse(zclFrame);
		}
		if (o == null)
			return null;
		//TODO: check merge, following line was different in 3.3.0
		//o.execAlertsNotification(Events, null);
		o.execAlertsNotification(Events, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseEventNotification(ApplianceEventsAndAlertsClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short EventHeader = ZclDataTypeUI8.zclParse(zclFrame);
		short EventIdentification = ZclDataTypeUI8.zclParse(zclFrame);
		
		if (o == null)
			return null;
		
		o.execEventNotification(EventHeader, EventIdentification, null);
		return null;
	}
}
