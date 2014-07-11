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

import java.util.Collection;
import java.util.Map;

import org.energy_home.jemma.ah.cluster.zigbee.general.PartitionClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.PartitionServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.ReadHandshakeParamResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclReadHandshakeParamResponse;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPartitionServer extends ZclServiceCluster implements ZigBeeDeviceListener, PartitionServer {

	public final static short CLUSTER_ID = 22;
	static Map attributesMapByName = null;
	static Map attributesMapById = null;
	static ZclAttributeDescriptor[] attributeDescriptors = null;

	static {
		attributeDescriptors = new ZclAttributeDescriptor[10];
		attributeDescriptors[0] = new ZclAttributeDescriptor(0, ZclPartitionServer.ATTR_MaximumIncomingTransferSize_NAME,
				new ZclDataTypeUI16(), null, true, 1);
		attributeDescriptors[1] = new ZclAttributeDescriptor(1, ZclPartitionServer.ATTR_MaximumOutgoingTransferSize_NAME,
				new ZclDataTypeUI16(), null, true, 1);
		attributeDescriptors[2] = new ZclAttributeDescriptor(2, ZclPartitionServer.ATTR_PartitionedFrameSize_NAME,
				new ZclDataTypeUI8(), null, true, 0);
		attributeDescriptors[3] = new ZclAttributeDescriptor(3, ZclPartitionServer.ATTR_LargeFrameSize_NAME, new ZclDataTypeUI16(),
				null, true, 0);
		attributeDescriptors[4] = new ZclAttributeDescriptor(4, ZclPartitionServer.ATTR_NumberOfACKFrame_NAME,
				new ZclDataTypeUI8(), null, true, 0);
		attributeDescriptors[5] = new ZclAttributeDescriptor(5, ZclPartitionServer.ATTR_NACKTimeout_NAME, new ZclDataTypeUI16(),
				null, true, 1);
		attributeDescriptors[6] = new ZclAttributeDescriptor(6, ZclPartitionServer.ATTR_InterframeDelay_NAME, new ZclDataTypeUI8(),
				null, true, 0);
		attributeDescriptors[7] = new ZclAttributeDescriptor(7, ZclPartitionServer.ATTR_NumberOfSendRetries_NAME,
				new ZclDataTypeUI8(), null, true, 1);
		attributeDescriptors[8] = new ZclAttributeDescriptor(8, ZclPartitionServer.ATTR_SenderTimeout_NAME, new ZclDataTypeUI16(),
				null, true, 1);
		attributeDescriptors[9] = new ZclAttributeDescriptor(9, ZclPartitionServer.ATTR_ReceiverTimeout_NAME,
				new ZclDataTypeUI16(), null, true, 1);
		attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
		attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
	}

	private PartitionClient c;

	public ZclPartitionServer(PartitionClient c) throws ApplianceException {
		super();
		this.c = c;
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
		switch (commandId) {
		case 0:
			responseZclFrame = parseMultipleACK(c, zclFrame);
			break;
		default:
			return false;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		} else {
			device.post(ZclPartitionServer.CLUSTER_ID, responseZclFrame);
		}
		return true;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		return ((IZclAttributeDescriptor) attributesMapByName.get(name));
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(int attrId) {
		return ((IZclAttributeDescriptor) attributesMapById.get(attrId));
	}

	protected Collection getAttributeDescriptors() {
		return (attributesMapByName.values());
	}

	public void execTransferPartitionedFrame(short FragmentationOptions, int PartitionIndicator, short FrameType,
			byte[] PartitionedFrame, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeBitmap8.zclSize(FragmentationOptions);
		size += ZclDataTypeUI16.zclSize(PartitionIndicator);
		size += ZclDataTypeUI8.zclSize(FrameType);
		size += ZclDataTypeOctets.zclSize(PartitionedFrame);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, FragmentationOptions);
		ZclDataTypeUI16.zclSerialize(zclFrame, PartitionIndicator);
		ZclDataTypeUI8.zclSerialize(zclFrame, FrameType);
		ZclDataTypeOctets.zclSerialize(zclFrame, PartitionedFrame);
		issueExec(zclFrame, 11, context);
	}

	public ReadHandshakeParamResponse execReadHandshakeParam(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclReadHandshakeParamResponse.zclParse(zclResponseFrame));
	}

	public void execWriteHandshakeParam(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(2);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseMultipleACK(PartitionClient o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short ACKOptions = ZclDataTypeBitmap8.zclParse(zclFrame);
		short FirstFrameID = ZclDataTypeUI8.zclParse(zclFrame);
		int ackList[] = null;
		if (o == null) {
			return null;
		}
		o.execMultipleACK(ACKOptions, FirstFrameID, ackList, endPoint.getDefaultRequestContext());
		return null;
	}

	public int getMaximumIncomingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(0, new Integer(v));
		return v;
	}

	public int getMaximumOutgoingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(1, new Integer(v));
		return v;
	}

	public void setPartitionedFrameSize(short PartitionedFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 2;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(PartitionedFrameSize);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, PartitionedFrameSize);
		issueSet(ZclPartitionServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getPartitionedFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(2, new Short(v));
		return v;
	}

	public void setLargeFrameSize(int LargeFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 3;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(LargeFrameSize);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, LargeFrameSize);
		issueSet(ZclPartitionServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getLargeFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(3, new Integer(v));
		return v;
	}

	public void setNumberOfACKFrame(short NumberOfACKFrame, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 4;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(NumberOfACKFrame);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, NumberOfACKFrame);
		issueSet(ZclPartitionServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getNumberOfACKFrame(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(4, new Short(v));
		return v;
	}

	public int getNACKTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(5, new Integer(v));
		return v;
	}

	public void setInterframeDelay(short InterframeDelay, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 6;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(InterframeDelay);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, InterframeDelay);
		issueSet(ZclPartitionServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getInterframeDelay(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(6, new Short(v));
		return v;
	}

	public short getNumberOfSendRetries(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(7, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(7, new Short(v));
		return v;
	}

	public int getSenderTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(8, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(8, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(8, new Integer(v));
		return v;
	}

	public int getReceiverTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(9, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(9, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(9, new Integer(v));
		return v;
	}
}
