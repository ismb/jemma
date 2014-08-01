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

public class ZclApplianceEventsAndAlertsClient extends ZclServiceCluster implements ApplianceEventsAndAlertsClient,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2818;

	public ZclApplianceEventsAndAlertsClient() throws ApplianceException {
		super();
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		boolean handled;
		handled = super.notifyZclFrame(clusterId, zclFrame);
		if (handled) {
			return handled;
		}
		int commandId = zclFrame.getCommandId();
		if (zclFrame.isServerToClient()) {
			throw new ZclValidationException("invalid direction field");
		}
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		ApplianceEventsAndAlertsServer c = ((ApplianceEventsAndAlertsServer) getSinglePeerCluster((ApplianceEventsAndAlertsServer.class
				.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseGetEventsAndAlerts(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclApplianceEventsAndAlertsClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execAlertsNotification(int[] Events, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += (Events.length * ZclDataTypeUI24.zclSize(0));
		size += 1;
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		int i;
		for (i = 0; (i < Events.length); i++) {
			ZclDataTypeUI24.zclSerialize(zclFrame, Events[i]);
		}
		issueExec(zclFrame, 11, context);
	}
	
    public void execEventNotification(short EventHeader, short EventIdentification, IEndPointRequestContext context)
            throws ApplianceException, ServiceClusterException
        {
            int size = 0;
		size += ZclDataTypeUI8 .zclSize(EventHeader);
            size += ZclDataTypeUI8 .zclSize(EventIdentification);
            ZclFrame zclFrame = new ZclFrame(1, size);
            zclFrame.setCommandId(2);
            ZclDataTypeUI8 .zclSerialize(zclFrame, EventHeader);
            ZclDataTypeUI8 .zclSerialize(zclFrame, EventIdentification);
            issueExec(zclFrame, 11, context);
        }

	protected IZclFrame parseGetEventsAndAlerts(ApplianceEventsAndAlertsServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		//TODO: check merge, following line was different in 3.3.0
		//GetAlertsResponse r = o.execGetAlerts(null);
		GetAlertsResponse r = o.execGetAlerts(endPoint.getDefaultRequestContext());
		int size = ZclGetAlertsResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclGetAlertsResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

}
