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

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.SignalStateResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclApplianceControlClient extends ZclServiceCluster implements ApplianceControlClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 27;

	public ZclApplianceControlClient() throws ApplianceException {
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
		ApplianceControlServer c = ((ApplianceControlServer) getSinglePeerCluster((ApplianceControlServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseCommandExecution(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseSignalState(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseWriteFunctions(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseOverloadPauseResume(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseOverloadPause(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseOverloadWarning(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclApplianceControlClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execSignalStateNotification(short ApplianceStatus, short RemoteEnableFlags, int ApplianceStatus2,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(ApplianceStatus);
		size += ZclDataTypeUI8.zclSize(RemoteEnableFlags);
		size += ZclDataTypeUI24.zclSize(ApplianceStatus2);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeEnum8.zclSerialize(zclFrame, ApplianceStatus);
		ZclDataTypeUI8.zclSerialize(zclFrame, RemoteEnableFlags);
		ZclDataTypeUI24.zclSerialize(zclFrame, ApplianceStatus2);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseCommandExecution(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short CommandId = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execCommandExecution(CommandId, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseSignalState(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		SignalStateResponse r = o.execSignalState(endPoint.getDefaultRequestContext());
		int size = ZclSignalStateResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclSignalStateResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseWriteFunctions(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		// TODO: implement this!
		return null;
	}

	protected IZclFrame parseOverloadPauseResume(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		o.execOverloadPauseResume(endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseOverloadPause(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		o.execOverloadPause(endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseOverloadWarning(ApplianceControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short WarningEvent = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execOverloadWarning(WarningEvent, endPoint.getDefaultRequestContext());
		return null;
	}

}
