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

import org.energy_home.jemma.ah.cluster.zigbee.general.BasicServer;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclBasicServer extends ZclServiceCluster implements BasicServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 0;

	static Map attributesMapByName = null;
	static Map attributesMapById = null;

	static ZclAttributeDescriptor[] attributeDescriptors = {
			new ZclAttributeDescriptor(0, ZclBasicServer.ATTR_ZCLVersion_NAME, new ZclDataTypeUI8(), null, true, 1),
			new ZclAttributeDescriptor(1, ZclBasicServer.ATTR_ApplicationVersion_NAME, new ZclDataTypeUI8(), null, true, 1),
			new ZclAttributeDescriptor(2, ZclBasicServer.ATTR_StackVersion_NAME, new ZclDataTypeUI8(), null, true, 1),
			new ZclAttributeDescriptor(3, ZclBasicServer.ATTR_HWVersion_NAME, new ZclDataTypeUI8(), null, true, 1),
			new ZclAttributeDescriptor(4, ZclBasicServer.ATTR_ManufacturerName_NAME, new ZclDataTypeString(32), null, true, 1),
			new ZclAttributeDescriptor(5, ZclBasicServer.ATTR_ModelIdentifier_NAME, new ZclDataTypeString(32), null, true, 1),
			new ZclAttributeDescriptor(6, ZclBasicServer.ATTR_DateCode_NAME, new ZclDataTypeString(32), null, true, 1),
			new ZclAttributeDescriptor(7, ZclBasicServer.ATTR_PowerSource_NAME, new ZclDataTypeEnum8(), null, true, 1),
			new ZclAttributeDescriptor(16, ZclBasicServer.ATTR_LocationDescription_NAME, new ZclDataTypeString(16), null, true, 0),
			new ZclAttributeDescriptor(17, ZclBasicServer.ATTR_PhysicalEnvironment_NAME, new ZclDataTypeEnum8(), null, true, 0),
			new ZclAttributeDescriptor(18, ZclBasicServer.ATTR_DeviceEnabled_NAME, new ZclDataTypeBoolean(), null, true, 0),
			new ZclAttributeDescriptor(19, ZclBasicServer.ATTR_AlarmMask_NAME, new ZclDataTypeBitmap8(), null, true, 0),
			new ZclAttributeDescriptor(20, ZclBasicServer.ATTR_DisableLocalConfig_NAME, new ZclDataTypeBitmap8(), null, true, 0) };

	static {
		attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
		attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
	}

	public ZclBasicServer() throws ApplianceException {
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

	public short getZCLVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

	public short getApplicationVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getStackVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getHWVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(3, new Short(v));
		return v;
	}

	public String getManufacturerName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(4, v);
		return v;
	}

	public String getModelIdentifier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(5, v);
		return v;
	}

	public String getDateCode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(6, v);
		return v;
	}

	public short getPowerSource(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(7, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(7, new Short(v));
		return v;
	}

	public void setLocationDescription(String LocationDescription, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 16;
		int size = 3;
		size += ZclDataTypeString.zclSize(LocationDescription);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeString.ZCL_DATA_TYPE);
		ZclDataTypeString.zclSerialize(zclFrame, LocationDescription);
		issueSet(ZclBasicServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public String getLocationDescription(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(16, v);
		return v;
	}

	public void setPhysicalEnvironment(short PhysicalEnvironment, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 17;
		int size = 3;
		size += ZclDataTypeEnum8.zclSize(PhysicalEnvironment);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeEnum8.ZCL_DATA_TYPE);
		ZclDataTypeEnum8.zclSerialize(zclFrame, PhysicalEnvironment);
		issueSet(ZclBasicServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getPhysicalEnvironment(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(17, new Short(v));
		return v;
	}

	public void setDeviceEnabled(boolean DeviceEnabled, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 18;
		int size = 3;
		size += ZclDataTypeBoolean.zclSize(DeviceEnabled);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBoolean.ZCL_DATA_TYPE);
		ZclDataTypeBoolean.zclSerialize(zclFrame, DeviceEnabled);
		issueSet(ZclBasicServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public boolean getDeviceEnabled(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(18, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(18, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(18, new Boolean(v));
		return v;
	}

	public void setAlarmMask(short AlarmMask, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int attrId = 19;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(AlarmMask);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, AlarmMask);
		issueSet(ZclBasicServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(19, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(19, new Short(v));
		return v;
	}

	public void setDisableLocalConfig(short DisableLocalConfig, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 20;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(DisableLocalConfig);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, DisableLocalConfig);
		issueSet(ZclBasicServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getDisableLocalConfig(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(20, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(20, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(20, new Short(v));
		return v;
	}

}
