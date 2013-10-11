/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

import org.energy_home.jemma.ah.cluster.zigbee.eh.DBFTR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.DEFTR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.DEFTWAR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.IndesitBridgeClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.IndesitBridgeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public class ZclIndesitBridgeClient extends ZclServiceCluster implements IndesitBridgeClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2566;

	public ZclIndesitBridgeClient() throws ApplianceException {
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
		IndesitBridgeServer c = ((IndesitBridgeServer) getSinglePeerCluster((IndesitBridgeServer.class.getName())));
		switch (commandId) {
		case 147:
			responseZclFrame = parseDBFT(c, zclFrame);
			break;
		case 149:
			responseZclFrame = parseDEFT(c, zclFrame);
			break;
		case 157:
			responseZclFrame = parseDEFTWA(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclIndesitBridgeClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseDBFT(IndesitBridgeServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short I2CBusAddress = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameHeader = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameType = ZclDataTypeUI8.zclParse(zclFrame);
		byte[] FramePayload = ZclDataTypeOctets.zclParse(zclFrame);
		DBFTR r = o.execDBFT(I2CBusAddress, FrameHeader, FrameType, FramePayload, null);
		int size = ZclDBFTR.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(147);
		ZclDBFTR.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseDEFT(IndesitBridgeServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short I2CBusAddress = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameHeader = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameType = ZclDataTypeUI8.zclParse(zclFrame);
		byte[] FramePayload = ZclDataTypeOctets.zclParse(zclFrame);
		DEFTR r = o.execDEFT(I2CBusAddress, FrameHeader, FrameType, FramePayload, null);
		int size = ZclDEFTR.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(149);
		ZclDEFTR.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseDEFTWA(IndesitBridgeServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short I2CBusAddress = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameHeader = ZclDataTypeUI8.zclParse(zclFrame);
		short FrameType = ZclDataTypeUI8.zclParse(zclFrame);
		byte[] FramePayload = ZclDataTypeOctets.zclParse(zclFrame);
		DEFTWAR r = o.execDEFTWA(I2CBusAddress, FrameHeader, FrameType, FramePayload, null);
		int size = ZclDEFTWAR.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(157);
		ZclDEFTWAR.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

}
