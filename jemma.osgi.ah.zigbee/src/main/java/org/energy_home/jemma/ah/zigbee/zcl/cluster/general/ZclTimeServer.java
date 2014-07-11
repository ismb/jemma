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

import org.energy_home.jemma.ah.cluster.zigbee.general.TimeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;

public class ZclTimeServer extends ZclServiceCluster implements TimeServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 10;
	
	public static Map attributesMapByName = null;
	public static Map attributesMapById = null;

	static ZclAttributeDescriptor[] attributeDescriptors = {
			new ZclAttributeDescriptor(0, ZclTimeServer.ATTR_Time_NAME, new ZclDataTypeUTCTime(), null, true, 0),
			new ZclAttributeDescriptor(1, ZclTimeServer.ATTR_TimeStatus_NAME, new ZclDataTypeBitmap8(), null, true, 0),
			new ZclAttributeDescriptor(2, ZclTimeServer.ATTR_TimeZone_NAME, new ZclDataTypeI32(), null, true, 0),
			new ZclAttributeDescriptor(3, ZclTimeServer.ATTR_DstStart_NAME, new ZclDataTypeUI32(), null, true, 0),
			new ZclAttributeDescriptor(4, ZclTimeServer.ATTR_DstEnd_NAME, new ZclDataTypeUI32(), null, true, 0),
			new ZclAttributeDescriptor(5, ZclTimeServer.ATTR_DstShift_NAME, new ZclDataTypeI32(), null, true, 0),
			new ZclAttributeDescriptor(6, ZclTimeServer.ATTR_StandardTime_NAME, new ZclDataTypeUI32(), null, true, 1),
			new ZclAttributeDescriptor(7, ZclTimeServer.ATTR_LocalTime_NAME, new ZclDataTypeUI32(), null, true, 1),
			new ZclAttributeDescriptor(8, ZclTimeServer.ATTR_LastSetTime_NAME, new ZclDataTypeUTCTime(), null, true, 1),
			new ZclAttributeDescriptor(9, ZclTimeServer.ATTR_ValidUntilTime_NAME, new ZclDataTypeUTCTime(), null, true, 1) };

	static {
		attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
		attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
	}

	public ZclTimeServer() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		return ((IZclAttributeDescriptor) attributesMapByName.get(name));
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(int id) {
		return (IZclAttributeDescriptor) attributesMapById.get(id);
	}

	protected Collection getAttributeDescriptors() {
		return attributesMapByName.values();
	}

	public void setTime(long Time, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 0;
		int size = 3;
		size += ZclDataTypeUTCTime.zclSize(Time);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUTCTime.ZCL_DATA_TYPE);
		ZclDataTypeUTCTime.zclSerialize(zclFrame, Time);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public long getTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		long v = ZclDataTypeUTCTime.zclParse(zclFrame);
		setCachedAttributeObject(0, new Long(v));
		return v;
	}

	public void setTimeStatus(short TimeStatus, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 1;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(TimeStatus);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, TimeStatus);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getTimeStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(1, new Short(v));
		return v;
	}

	public void setTimeZone(long TimeZone, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 2;
		int size = 3;
		size += ZclDataTypeI32.zclSize(TimeZone);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
		ZclDataTypeI32.zclSerialize(zclFrame, TimeZone);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public long getTimeZone(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		long v = ZclDataTypeI32.zclParse(zclFrame);
		setCachedAttributeObject(2, new Long(v));
		return v;
	}

	public void setDstStart(long DstStart, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 3;
		int size = 3;
		size += ZclDataTypeUI32.zclSize(DstStart);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
		ZclDataTypeUI32.zclSerialize(zclFrame, DstStart);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public long getDstStart(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(3, new Long(v));
		return v;
	}

	public void setDstEnd(long DstEnd, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 4;
		int size = 3;
		size += ZclDataTypeUI32.zclSize(DstEnd);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI32.ZCL_DATA_TYPE);
		ZclDataTypeUI32.zclSerialize(zclFrame, DstEnd);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public long getDstEnd(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(4, new Long(v));
		return v;
	}

	public void setDstShift(long DstShift, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 5;
		int size = 3;
		size += ZclDataTypeI32.zclSize(DstShift);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeI32.ZCL_DATA_TYPE);
		ZclDataTypeI32.zclSerialize(zclFrame, DstShift);
		issueSet(ZclTimeServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public long getDstShift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		long v = ZclDataTypeI32.zclParse(zclFrame);
		setCachedAttributeObject(5, new Long(v));
		return v;
	}

	public long getStandardTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(6, new Long(v));
		return v;
	}

	public long getLocalTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(7, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(7, new Long(v));
		return v;
	}

	public long getLastSetTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(8, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(8, context);
		long v = ZclDataTypeUTCTime.zclParse(zclFrame);
		setCachedAttributeObject(8, new Long(v));
		return v;
	}

	public long getValidUntilTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(9, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(9, context);
		long v = ZclDataTypeUTCTime.zclParse(zclFrame);
		setCachedAttributeObject(9, new Long(v));
		return v;
	}

	public void setValidUntilTime(long ValidUntilTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		// TODO Auto-generated method stub
		
	}
}
