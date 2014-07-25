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

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogQueueResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;

public class ZclApplianceStatisticsClient extends ZclServiceCluster implements ApplianceStatisticsClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2819;

	public ZclApplianceStatisticsClient() throws ApplianceException {
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
		ApplianceStatisticsServer c = ((ApplianceStatisticsServer) getSinglePeerCluster((ApplianceStatisticsServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseLogRequest(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseLogQueueRequest(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclApplianceStatisticsClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execLogNotification(long Timestamp, long LogID, long LogLength, byte[] LogPayload, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUTCTime.zclSize(Timestamp);
		size += ZclDataTypeUI32.zclSize(LogID);
		size += ZclDataTypeUI32.zclSize(LogLength);
		size += ZclDataTypeOctets.zclSize(LogPayload);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUTCTime.zclSerialize(zclFrame, Timestamp);
		ZclDataTypeUI32.zclSerialize(zclFrame, LogID);
		ZclDataTypeUI32.zclSerialize(zclFrame, LogLength);
		ZclDataTypeOctets.zclSerialize(zclFrame, LogPayload);
		issueExec(zclFrame, 11, context);
	}

	public void execStatisticsAvailable(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(3);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseLogRequest(ApplianceStatisticsServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		long LogID = ZclDataTypeUI32.zclParse(zclFrame);
		LogResponse r = o.execLogRequest(LogID, endPoint.getDefaultRequestContext());
		int size = ZclLogResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclLogResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseLogQueueRequest(ApplianceStatisticsServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		LogQueueResponse r = o.execLogQueueRequest(endPoint.getDefaultRequestContext());
		int size = ZclLogQueueResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(2);
		ZclLogQueueResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

}
