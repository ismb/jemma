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

import org.energy_home.jemma.ah.cluster.zigbee.general.TimeClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.TimeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.InvalidValueException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;


public class ZclTimeClient extends ZclServiceCluster implements TimeClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 10;

	public ZclTimeClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor[] getPeerAttributeDescriptors() {
		return ZclTimeServer.attributeDescriptors;
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
		IEndPointRequestContext context = endPoint.getDefaultRequestContext();

		switch (attrId) {
		case 0x00: {
			long v = c.getTime(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
			ZclDataTypeUTCTime.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x01: {
			short v = c.getTimeStatus(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
			ZclDataTypeBitmap8.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x02: {
			long v = c.getTimeZone(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
			ZclDataTypeI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x03: {
			long v = c.getDstStart(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x04: {
			long v = c.getDstEnd(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x05: {
			long v = c.getDstShift(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
			ZclDataTypeI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x06: {
			long v = c.getStandardTime(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x07: {
			long v = c.getLocalTime(context);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
			ZclDataTypeUI32.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x08: {
			long v;
			try {
				v = c.getLastSetTime(context);
			} catch (InvalidValueException e) {
				v = 0xFFFFFFFF;
			}
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
			ZclDataTypeUTCTime.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x09: {
			long v;
			try {
				v = c.getValidUntilTime(context);
			} catch (InvalidValueException e) {
				v = 0xFFFFFFFF;
			}
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

		TimeServer c = ((TimeServer) getSinglePeerCluster((TimeServer.class.getName())));
		IEndPointRequestContext context = endPoint.getDefaultRequestContext();

		switch (attrId) {
		case 0x00:
		case 0x01:
		case 0x06:
		case 0x07:
		case 0x08:
			return ZCL.READ_ONLY;

		case 0x02: {
			long v = ZclDataTypeI32.zclParse(zclFrame);
			c.setTimeZone(v, context);
			break;
		}

		case 0x03: {
			long v = ZclDataTypeI32.zclParse(zclFrame);
			c.setDstStart(v, context);
			break;
		}

		case 0x04: {
			long v = ZclDataTypeI32.zclParse(zclFrame);
			c.setDstEnd(v, context);
			break;
		}

		case 0x05: {
			long v = ZclDataTypeI32.zclParse(zclFrame);
			c.setDstShift(v, context);
			break;
		}

		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
		return ZCL.SUCCESS;
	}
}
