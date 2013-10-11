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
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

import org.energy_home.jemma.ah.cluster.zigbee.eh.DBFTR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.DEFTR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.DEFTWAR;
import org.energy_home.jemma.ah.cluster.zigbee.eh.IndesitBridgeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public class ZclIndesitBridgeServer extends ZclServiceCluster implements IndesitBridgeServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2566;

	public ZclIndesitBridgeServer() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public DBFTR execDBFT(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(I2CBusAddress);
		size += ZclDataTypeUI8.zclSize(FrameHeader);
		size += ZclDataTypeUI8.zclSize(FrameType);
		size += ZclDataTypeOctets.zclSize(FramePayload);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(147);
		ZclDataTypeUI8.zclSerialize(zclFrame, I2CBusAddress);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameHeader);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameType);
		ZclDataTypeOctets.zclSerialize(zclFrame, FramePayload);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 147, context);
		return (ZclDBFTR.zclParse(zclResponseFrame));
	}

	public DEFTR execDEFT(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(I2CBusAddress);
		size += ZclDataTypeUI8.zclSize(FrameHeader);
		size += ZclDataTypeUI8.zclSize(FrameType);
		size += ZclDataTypeOctets.zclSize(FramePayload);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(149);
		ZclDataTypeUI8.zclSerialize(zclFrame, I2CBusAddress);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameHeader);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameType);
		ZclDataTypeOctets.zclSerialize(zclFrame, FramePayload);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 149, context);
		return (ZclDEFTR.zclParse(zclResponseFrame));
	}

	public DEFTWAR execDEFTWA(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(I2CBusAddress);
		size += ZclDataTypeUI8.zclSize(FrameHeader);
		size += ZclDataTypeUI8.zclSize(FrameType);
		size += ZclDataTypeOctets.zclSize(FramePayload);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(157);
		ZclDataTypeUI8.zclSerialize(zclFrame, I2CBusAddress);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameHeader);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameType);
		ZclDataTypeOctets.zclSerialize(zclFrame, FramePayload);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 157, context);
		return (ZclDEFTWAR.zclParse(zclResponseFrame));
	}

}
