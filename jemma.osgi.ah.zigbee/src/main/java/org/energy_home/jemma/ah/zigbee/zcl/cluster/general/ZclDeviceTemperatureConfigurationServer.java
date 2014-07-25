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

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.general.DeviceTemperatureConfigurationServer;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;

public class ZclDeviceTemperatureConfigurationServer extends ZclServiceCluster implements DeviceTemperatureConfigurationServer,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_CurrentTemperature_NAME, new ZclAttributeDescriptor(0,
				ZclDeviceTemperatureConfigurationServer.ATTR_CurrentTemperature_NAME, new ZclDataTypeI16(), null, true, 1));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_MinTempExperienced_NAME, new ZclAttributeDescriptor(1,
				ZclDeviceTemperatureConfigurationServer.ATTR_MinTempExperienced_NAME, new ZclDataTypeI16(), null, true, 1));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_MaxTempExperienced_NAME, new ZclAttributeDescriptor(3,
				ZclDeviceTemperatureConfigurationServer.ATTR_MaxTempExperienced_NAME, new ZclDataTypeI16(), null, true, 1));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_OverTempTotalDwell_NAME, new ZclAttributeDescriptor(4,
				ZclDeviceTemperatureConfigurationServer.ATTR_OverTempTotalDwell_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName
				.put(ZclDeviceTemperatureConfigurationServer.ATTR_DeviceTempAlarmMask_NAME, new ZclAttributeDescriptor(16,
						ZclDeviceTemperatureConfigurationServer.ATTR_DeviceTempAlarmMask_NAME, new ZclDataTypeBitmap8(), null,
						true, 0));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_LowTempThreshold_NAME, new ZclAttributeDescriptor(17,
				ZclDeviceTemperatureConfigurationServer.ATTR_LowTempThreshold_NAME, new ZclDataTypeI16(), null, true, 0));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_HighTempThreshold_NAME, new ZclAttributeDescriptor(17,
				ZclDeviceTemperatureConfigurationServer.ATTR_HighTempThreshold_NAME, new ZclDataTypeI16(), null, true, 0));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_LowTempDwellTripPoint_NAME,
				new ZclAttributeDescriptor(17, ZclDeviceTemperatureConfigurationServer.ATTR_LowTempDwellTripPoint_NAME,
						new ZclDataTypeUI24(), null, true, 0));
		attributesMapByName.put(ZclDeviceTemperatureConfigurationServer.ATTR_HighTempDwellTripPoint_NAME,
				new ZclAttributeDescriptor(17, ZclDeviceTemperatureConfigurationServer.ATTR_HighTempDwellTripPoint_NAME,
						new ZclDataTypeUI24(), null, true, 0));
	}

	public ZclDeviceTemperatureConfigurationServer() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		return ((IZclAttributeDescriptor) attributesMapByName.get(name));
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(int id) {
		Iterator iterator = attributesMapByName.values().iterator();
		// FIXME: generate it and optimize!!!!
		for (; iterator.hasNext();) {
			IZclAttributeDescriptor attributeDescriptor = (IZclAttributeDescriptor) iterator.next();
			if (attributeDescriptor.zclGetId() == id)
				return attributeDescriptor;
		}
		return null;
	}

	public int getCurrentTemperature(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		int v = ZclDataTypeI16.zclParse(zclFrame);
		setCachedAttributeObject(0, new Integer(v));
		return v;
	}

	public int getMinTempExperienced(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		int v = ZclDataTypeI16.zclParse(zclFrame);
		setCachedAttributeObject(1, new Integer(v));
		return v;
	}

	public int getMaxTempExperienced(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		int v = ZclDataTypeI16.zclParse(zclFrame);
		setCachedAttributeObject(3, new Integer(v));
		return v;
	}

	public int getOverTempTotalDwell(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(4, new Integer(v));
		return v;
	}

	public void setDeviceTempAlarmMask(short DeviceTempAlarmMask, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 16;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(DeviceTempAlarmMask);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, DeviceTempAlarmMask);
		issueSet(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getDeviceTempAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(16, new Short(v));
		return v;
	}

	public void setLowTempThreshold(int LowTempThreshold, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeI16.zclSize(LowTempThreshold);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeI16.ZCL_DATA_TYPE);
		ZclDataTypeI16.zclSerialize(zclFrame, LowTempThreshold);
		issueSet(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getLowTempThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeI16.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public void setHighTempThreshold(int HighTempThreshold, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeI16.zclSize(HighTempThreshold);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeI16.ZCL_DATA_TYPE);
		ZclDataTypeI16.zclSerialize(zclFrame, HighTempThreshold);
		issueSet(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getHighTempThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeI16.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public void setLowTempDwellTripPoint(int LowTempDwellTripPoint, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeUI24.zclSize(LowTempDwellTripPoint);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI24.ZCL_DATA_TYPE);
		ZclDataTypeUI24.zclSerialize(zclFrame, LowTempDwellTripPoint);
		issueSet(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getLowTempDwellTripPoint(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeUI24.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public void setHighTempDwellTripPoint(int HighTempDwellTripPoint, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeUI24.zclSize(HighTempDwellTripPoint);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI24.ZCL_DATA_TYPE);
		ZclDataTypeUI24.zclSerialize(zclFrame, HighTempDwellTripPoint);
		issueSet(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getHighTempDwellTripPoint(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeUI24.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

}
