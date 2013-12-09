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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.general;

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;

import org.energy_home.jemma.ah.cluster.zigbee.general.TimeClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.TimeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;

public class ZclTimeClient extends ZclServiceCluster implements TimeClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 10;

	public ZclTimeClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0x00:
			return ZclDataTypeUTCTime.zclSize(0);

		case 0x01:
			return ZclDataTypeBitmap8.zclSize((byte) 0);

		case 0x02:
			return ZclDataTypeI32.zclSize(0);

		case 0x03:
			return ZclDataTypeUI32.zclSize(0);

		case 0x04:
			return ZclDataTypeUI32.zclSize(0);

		case 0x05:
			return ZclDataTypeI32.zclSize(0);

		case 0x06:
			return ZclDataTypeUI32.zclSize(0);

		case 0x07:
			return ZclDataTypeUI32.zclSize(0);

		case 0x08:
			return ZclDataTypeUTCTime.zclSize(0);

		case 0x09:
			return ZclDataTypeUTCTime.zclSize(0);

		default:
			throw new UnsupportedClusterOperationException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ServiceClusterException,
			ApplianceException {
		TimeServer c = ((TimeServer) getSinglePeerCluster((TimeServer.class.getName())));

		switch (attrId) {
		case 0x00: {
			long v = c.getTime(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
			ZclDataTypeUTCTime.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x01: {
			short v = c.getTimeStatus(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
			ZclDataTypeBitmap8.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x02: {
			long v = c.getTimeZone(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
			ZclDataTypeI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x03: {
			long v = c.getDstStart(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x04: {
			long v = c.getDstEnd(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x05: {
			long v = c.getDstShift(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
			ZclDataTypeI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x06: {
			long v = c.getStandardTime(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x07: {
			long v = c.getLocalTime(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x08: {
			long v = c.getLastSetTime(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
			ZclDataTypeUTCTime.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x09: {
			long v = c.getValidUntilTime(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
			ZclDataTypeUTCTime.zclSerialize(zclResponseFrame, v);
			break;
		}

		default:
			return false;
		}

		return true;
	}

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {
		switch (attrId) {
		case 0x00:
		case 0x01:
		case 0x02:
		case 0x03:
		case 0x04:
		case 0x05:
		case 0x06:
		case 0x07:
			return ZCL.READ_ONLY;

		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
	}
}
