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

import org.energy_home.jemma.ah.cluster.zigbee.general.PowerConfigurationServer;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerConfigurationServer extends ZclServiceCluster implements PowerConfigurationServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 1;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsVoltage_NAME, new ZclAttributeDescriptor(0,
				ZclPowerConfigurationServer.ATTR_MainsVoltage_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsFrequency_NAME, new ZclAttributeDescriptor(1,
				ZclPowerConfigurationServer.ATTR_MainsFrequency_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsAlarmMask_NAME, new ZclAttributeDescriptor(16,
				ZclPowerConfigurationServer.ATTR_MainsAlarmMask_NAME, new ZclDataTypeBitmap8(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsVoltageMinThreshold_NAME, new ZclAttributeDescriptor(17,
				ZclPowerConfigurationServer.ATTR_MainsVoltageMinThreshold_NAME, new ZclDataTypeUI16(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsVoltageMaxThreshold_NAME, new ZclAttributeDescriptor(18,
				ZclPowerConfigurationServer.ATTR_MainsVoltageMaxThreshold_NAME, new ZclDataTypeUI16(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_MainsVoltageDwellTripPoint_NAME, new ZclAttributeDescriptor(19,
				ZclPowerConfigurationServer.ATTR_MainsVoltageDwellTripPoint_NAME, new ZclDataTypeUI16(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryVoltage_NAME, new ZclAttributeDescriptor(32,
				ZclPowerConfigurationServer.ATTR_BatteryVoltage_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryManufacturer_NAME, new ZclAttributeDescriptor(48,
				ZclPowerConfigurationServer.ATTR_BatteryManufacturer_NAME, new ZclDataTypeString(16), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatterySize_NAME, new ZclAttributeDescriptor(49,
				ZclPowerConfigurationServer.ATTR_BatterySize_NAME, new ZclDataTypeUI8(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryAHrRating_NAME, new ZclAttributeDescriptor(50,
				ZclPowerConfigurationServer.ATTR_BatteryAHrRating_NAME, new ZclDataTypeUI16(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryQuantity_NAME, new ZclAttributeDescriptor(51,
				ZclPowerConfigurationServer.ATTR_BatteryQuantity_NAME, new ZclDataTypeUI8(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryRatedVoltage_NAME, new ZclAttributeDescriptor(52,
				ZclPowerConfigurationServer.ATTR_BatteryRatedVoltage_NAME, new ZclDataTypeUI8(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryAlarmMask_NAME, new ZclAttributeDescriptor(53,
				ZclPowerConfigurationServer.ATTR_BatteryAlarmMask_NAME, new ZclDataTypeUI8(), null, true, 0));
		attributesMapByName.put(ZclPowerConfigurationServer.ATTR_BatteryVoltageMinThreshold_NAME, new ZclAttributeDescriptor(54,
				ZclPowerConfigurationServer.ATTR_BatteryVoltageMinThreshold_NAME, new ZclDataTypeUI8(), null, true, 0));
	}

	public ZclPowerConfigurationServer() throws ApplianceException {
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

	public int getMainsVoltage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getMainsFrequency(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(1, new Short(v));
		return v;
	}

	public void setMainsAlarmMask(short MainsAlarmMask, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 16;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(MainsAlarmMask);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, MainsAlarmMask);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getMainsAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public void setMainsVoltageMinThreshold(int MainsVoltageMinThreshold, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(MainsVoltageMinThreshold);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, MainsVoltageMinThreshold);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getMainsVoltageMinThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public void setMainsVoltageMaxThreshold(int MainsVoltageMaxThreshold, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int attrId = 18;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(MainsVoltageMaxThreshold);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, MainsVoltageMaxThreshold);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getMainsVoltageMaxThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(18, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(18, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(18, new Integer(v));
		return v;
	}

	public void setMainsVoltageDwellTripPoint(int MainsVoltageDwellTripPoint, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int attrId = 19;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(MainsVoltageDwellTripPoint);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, MainsVoltageDwellTripPoint);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getMainsVoltageDwellTripPoint(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(19, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(19, new Integer(v));
		return v;
	}

	public short getBatteryVoltage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(32, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(32, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(32, new Short(v));
		return v;
	}

	public void setBatteryManufacturer(String BatteryManufacturer, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 48;
		int size = 3;
		size += ZclDataTypeString.zclSize(BatteryManufacturer);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeString.ZCL_DATA_TYPE);
		ZclDataTypeString.zclSerialize(zclFrame, BatteryManufacturer);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public String getBatteryManufacturer(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(48, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(48, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(48, v);
		return v;
	}

	public void setBatterySize(short BatterySize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 49;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(BatterySize);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, BatterySize);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getBatterySize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(49, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(49, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(49, new Short(v));
		return v;
	}

	public void setBatteryAHrRating(int BatteryAHrRating, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 50;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(BatteryAHrRating);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, BatteryAHrRating);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getBatteryAHrRating(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(50, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(50, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(50, new Integer(v));
		return v;
	}

	public void setBatteryQuantity(short BatteryQuantity, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 51;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(BatteryQuantity);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, BatteryQuantity);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getBatteryQuantity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(51, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(51, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(51, new Short(v));
		return v;
	}

	public void setBatteryRatedVoltage(short BatteryRatedVoltage, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 52;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(BatteryRatedVoltage);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, BatteryRatedVoltage);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getBatteryRatedVoltage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(52, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(52, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(52, new Short(v));
		return v;
	}

	public void setBatteryAlarmMask(short BatteryAlarmMask, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 53;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(BatteryAlarmMask);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, BatteryAlarmMask);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getBatteryAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(53, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(53, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(53, new Short(v));
		return v;
	}

	public void setBatteryVoltageMinThreshold(short BatteryVoltageMinThreshold, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int attrId = 54;
		int size = 3;
		size += ZclDataTypeUI8.zclSize(BatteryVoltageMinThreshold);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
		ZclDataTypeUI8.zclSerialize(zclFrame, BatteryVoltageMinThreshold);
		issueSet(ZclPowerConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getBatteryVoltageMinThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(54, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(54, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(54, new Short(v));
		return v;
	}

}
