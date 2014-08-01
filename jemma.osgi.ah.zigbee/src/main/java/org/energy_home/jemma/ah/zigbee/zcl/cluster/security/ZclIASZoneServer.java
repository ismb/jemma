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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.security;

import java.util.Collection;
import java.util.Map;

import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeIEEEAddress;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclIASZoneServer extends ZclServiceCluster implements ZigBeeDeviceListener, IASZoneServer {

	public final static short CLUSTER_ID = 1280;
	static Map attributesMapByName = null;
	static Map attributesMapById = null;
	static ZclAttributeDescriptor[] attributeDescriptors = null;

	static {
		attributeDescriptors = new ZclAttributeDescriptor[5];
		attributeDescriptors[0] = new ZclAttributeDescriptor(0, ZclIASZoneServer.ATTR_ZoneState_NAME, new ZclDataTypeEnum8(), null,
				true, 1);
		attributeDescriptors[1] = new ZclAttributeDescriptor(1, ZclIASZoneServer.ATTR_ZoneType_NAME, new ZclDataTypeEnum16(), null,
				true, 1);
		attributeDescriptors[2] = new ZclAttributeDescriptor(2, ZclIASZoneServer.ATTR_ZoneStatus_NAME, new ZclDataTypeBitmap16(),
				null, true, 1);
		attributeDescriptors[3] = new ZclAttributeDescriptor(16, ZclIASZoneServer.ATTR_IAS_CIE_Address_NAME,
				new ZclDataTypeIEEEAddress(), null, true, 0);
		attributeDescriptors[4] = new ZclAttributeDescriptor(17, ZclIASZoneServer.ATTR_ZoneID_NAME, new ZclDataTypeUI8(), null,
				true, 1);
		attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
		attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
	}

	public ZclIASZoneServer() throws ApplianceException {
		super();
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
		IASZoneClient c = ((IASZoneClient) getSinglePeerCluster((IASZoneClient.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseZoneStatusChangeNotification(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseZoneEnrollRequest(c, zclFrame);
			break;
		default:
			return false;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		} else {
			device.post(ZclIASZoneServer.CLUSTER_ID, responseZclFrame);
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

	protected IZclFrame parseZoneStatusChangeNotification(IASZoneClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		int ZoneStatus = ZclDataTypeBitmap16.zclParse(zclFrame);
		short ExtendedStatus = ZclDataTypeBitmap8.zclParse(zclFrame);
		short ZoneID = 0xff;
		int Delay = 0;
		boolean gotZoneId = false;
		
		// Added the following code to mantain the compatibility with HA 1.1 devices
		try {
			ZoneID = ZclDataTypeUI8.zclParse(zclFrame);
			gotZoneId = true;
			Delay = ZclDataTypeUI16.zclParse(zclFrame);
		} catch (Exception e) {
			if (gotZoneId)
				throw new ZclValidationException("missing Delay in ZoneStatusChangeNotification");	
		}

		if (o == null) {
			return null;
		}
		o.execZoneStatusChangeNotification(ZoneStatus, ExtendedStatus, ZoneID, Delay, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseZoneEnrollRequest(IASZoneClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		int ZoneType = ZclDataTypeEnum16.zclParse(zclFrame);
		int ManufacturerCode = ZclDataTypeUI16.zclParse(zclFrame);
		ZoneEnrollResponse r = o.execZoneEnrollRequest(ZoneType, ManufacturerCode, endPoint.getDefaultRequestContext());
		int size = ZclZoneEnrollResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclZoneEnrollResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	public short getZoneState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

	public int getZoneType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		int v = ZclDataTypeEnum16.zclParse(zclFrame);
		setCachedAttributeObject(1, new Integer(v));
		return v;
	}

	public int getZoneStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(2, new Integer(v));
		return v;
	}

	public void setIAS_CIE_Address(byte[] IAS_CIE_Address, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 16;
		int size = 3;
		size += ZclDataTypeIEEEAddress.zclSize(IAS_CIE_Address.length);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeIEEEAddress.ZCL_DATA_TYPE);
		ZclDataTypeIEEEAddress.zclSerialize(zclFrame, IAS_CIE_Address);
		issueSet(ZclIASZoneServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public byte[] getIAS_CIE_Address(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			byte[] objectResult = null;
			objectResult = ((byte[]) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		byte[] v = ZclDataTypeIEEEAddress.zclParse(zclFrame);
		setCachedAttributeObject(16, v);
		return v;
	}

	public short getZoneID(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(17, new Short(v));
		return v;
	}

}
