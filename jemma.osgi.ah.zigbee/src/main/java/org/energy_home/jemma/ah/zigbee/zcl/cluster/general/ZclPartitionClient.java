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

import org.energy_home.jemma.ah.cluster.zigbee.general.PartitionClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.PartitionServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.ReadHandshakeParamResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclReadHandshakeParamResponse;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPartitionClient extends ZclServiceCluster implements ZigBeeDeviceListener, PartitionClient {

	public final static short CLUSTER_ID = 22;
	protected PartitionServer c;

	public ZclPartitionClient(PartitionServer s) throws ApplianceException {
		super();
		this.c = s;
	}

	public ZclPartitionClient() throws ApplianceException {
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
		switch (commandId) {
		case 0:
			responseZclFrame = parseTransferPartitionedFrame(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseReadHandshakeParam(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseWriteHandshakeParam(c, zclFrame);
			break;
		default:
			return false;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			} else
				return true;
		}
		if (responseZclFrame != null) {
			device.post(ZclPartitionClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return true;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor[] getPeerClusterAttributeDescriptors() {
		return ZclPartitionServer.attributeDescriptors;
	}

	public void execMultipleACK(short ACKOptions, short FirstFrameID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeBitmap8.zclSize(ACKOptions);
		size += ZclDataTypeUI8.zclSize(FirstFrameID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, ACKOptions);
		ZclDataTypeUI8.zclSerialize(zclFrame, FirstFrameID);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseTransferPartitionedFrame(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException, ZclException {
		short FragmentationOptions = ZclDataTypeBitmap8.zclParse(zclFrame);
		int PartitionIndicator = ZclDataTypeUI16.zclParse(zclFrame);
		short FrameType = ZclDataTypeUI8.zclParse(zclFrame);
		byte[] PartitionedFrame = ZclDataTypeOctets.zclParse(zclFrame);
		if (o == null) {
			return null;
		}
		o.execTransferPartitionedFrame(FragmentationOptions, PartitionIndicator, FrameType, PartitionedFrame,
				endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseReadHandshakeParam(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		ReadHandshakeParamResponse r = o.execReadHandshakeParam(endPoint.getDefaultRequestContext());
		int size = ZclReadHandshakeParamResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclReadHandshakeParamResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseWriteHandshakeParam(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		if (o == null) {
			return null;
		}
		o.execWriteHandshakeParam(endPoint.getDefaultRequestContext());
		return null;
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0:
			return ZclDataTypeUI16.zclSize(((int) 0));
		case 1:
			return ZclDataTypeUI16.zclSize(((int) 0));
		case 2:
			return ZclDataTypeUI8.zclSize(((short) 0));
		case 3:
			return ZclDataTypeUI16.zclSize(((int) 0));
		case 4:
			return ZclDataTypeUI8.zclSize(((short) 0));
		case 5:
			return ZclDataTypeUI16.zclSize(((int) 0));
		case 6:
			return ZclDataTypeUI8.zclSize(((short) 0));
		case 7:
			return ZclDataTypeUI8.zclSize(((short) 0));
		case 8:
			return ZclDataTypeUI16.zclSize(((int) 0));
		case 9:
			return ZclDataTypeUI16.zclSize(((int) 0));
		default:
			throw new UnsupportedClusterAttributeException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ApplianceException,
			ServiceClusterException {

		switch (attrId) {
		case 0: {
			int v;
			v = c.getMaximumIncomingTransferSize(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 1: {
			int v;
			v = c.getMaximumOutgoingTransferSize(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 2: {
			short v;
			v = c.getPartitionedFrameSize(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 3: {
			int v;
			v = c.getLargeFrameSize(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 4: {
			short v;
			v = c.getNumberOfACKFrame(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 5: {
			int v;
			v = c.getNACKTimeout(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 6: {
			short v;
			v = c.getInterframeDelay(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 7: {
			short v;
			v = c.getNumberOfSendRetries(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 8: {
			int v;
			v = c.getSenderTimeout(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 9: {
			int v;
			v = c.getReceiverTimeout(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}
		default:
			return false;
		}
		return true;
	}

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {
		switch (attrId) {
		case 0:
		case 1:
		case 5:
		case 7:
		case 8:
		case 9:
			return ZCL.READ_ONLY;
		case 2: {
			short v = ZclDataTypeUI8.zclParse(zclFrame);
			c.setPartitionedFrameSize(v, endPoint.getDefaultRequestContext());
			break;
		}
		case 3: {
			int v = ZclDataTypeUI16.zclParse(zclFrame);
			c.setLargeFrameSize(v, endPoint.getDefaultRequestContext());
			break;
		}
		case 4: {
			short v = ZclDataTypeUI8.zclParse(zclFrame);
			c.setNumberOfACKFrame(v, endPoint.getDefaultRequestContext());
			break;
		}
		case 6: {
			short v = ZclDataTypeUI8.zclParse(zclFrame);
			c.setInterframeDelay(v, endPoint.getDefaultRequestContext());
			break;
		}
		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
		return ZCL.SUCCESS;
	}

	public void execMultipleACK(short ACKOptions, int FirstFrameID, int[] ackList, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
	}
}
