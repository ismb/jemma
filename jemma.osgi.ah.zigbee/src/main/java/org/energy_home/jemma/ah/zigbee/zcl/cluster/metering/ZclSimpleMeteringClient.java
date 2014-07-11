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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.metering;

import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclSimpleMeteringClient extends ZclServiceCluster implements SimpleMeteringClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 1794;

	public ZclSimpleMeteringClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}
	
	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {

		case 0x0304:
			return ZclDataTypeBitmap8.zclSize((short) 0);

		case 0x0400:
			return ZclDataTypeI24.zclSize(0);

		default:
			throw new UnsupportedClusterOperationException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ServiceClusterException,
			ApplianceException {
		SimpleMeteringServer c = ((SimpleMeteringServer) getSinglePeerCluster((SimpleMeteringServer.class.getName())));

		switch (attrId) {
		case 0x0304: {
			short v = c.getDemandFormatting(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
			ZclDataTypeBitmap8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 0x0400: {
			int v = c.getIstantaneousDemand(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeI24.ZCL_DATA_TYPE);
			ZclDataTypeI24.zclSerialize(zclResponseFrame, v);
			break;
		}

		default:
			return false;
		}

		return true;
	}
}
