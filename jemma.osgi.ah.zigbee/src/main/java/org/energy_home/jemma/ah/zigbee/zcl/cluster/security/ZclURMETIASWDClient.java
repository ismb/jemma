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

import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
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

public class ZclURMETIASWDClient extends ZclServiceCluster implements ZigBeeDeviceListener, IASZoneClient {

	public final static short CLUSTER_ID = 1282;

	public ZclURMETIASWDClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor[] getPeerClusterAttributeDescriptors() {
		return ZclIASZoneServer.attributeDescriptors;
	}

	public void execZoneStatusChangeNotification(int ZoneStatus, short ExtendedStatus, short ZoneID, int Delay,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeBitmap16.zclSize(ZoneStatus);
		size += ZclDataTypeBitmap8.zclSize(ExtendedStatus);
		size += ZclDataTypeUI8.zclSize(ZoneID);
		size += ZclDataTypeUI16.zclSize(Delay);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeBitmap16.zclSerialize(zclFrame, ZoneStatus);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, ExtendedStatus);
		ZclDataTypeUI8.zclSerialize(zclFrame, ZoneID);
		ZclDataTypeUI16.zclSerialize(zclFrame, Delay);
		issueExec(zclFrame, 11, context);
	}

	public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum16.zclSize(ZoneType);
		size += ZclDataTypeUI16.zclSize(ManufacturerCode);
		
		
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeEnum16.zclSerialize(zclFrame, ZoneType);
		ZclDataTypeUI16.zclSerialize(zclFrame, ManufacturerCode);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
		return (ZclZoneEnrollResponse.zclParse(zclResponseFrame));
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0:
			return ZclDataTypeEnum8.zclSize(((short) 0));
		case 1:
			return ZclDataTypeEnum16.zclSize(((int) 0));
		case 2:
			return ZclDataTypeBitmap16.zclSize(((int) 0));
		case 16:
			return ZclDataTypeIEEEAddress.zclSize(0);
		case 17:
			return ZclDataTypeUI8.zclSize(((short) 0));
		default:
			throw new UnsupportedClusterAttributeException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ApplianceException,
			ServiceClusterException {
		IASZoneServer c = ((IASZoneServer) getSinglePeerCluster((IASZoneServer.class.getName())));
		switch (attrId) {
		case 0: {
			short v;
			v = c.getZoneState(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeEnum8.ZCL_DATA_TYPE);
			ZclDataTypeEnum8.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 1: {
			int v;
			v = c.getZoneType(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeEnum16.ZCL_DATA_TYPE);
			ZclDataTypeEnum16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 2: {
			int v;
			v = c.getZoneStatus(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeBitmap16.ZCL_DATA_TYPE);
			ZclDataTypeBitmap16.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 16: {
			byte[] v;
			v = c.getIAS_CIE_Address(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeIEEEAddress.ZCL_DATA_TYPE);
			ZclDataTypeIEEEAddress.zclSerialize(zclResponseFrame, v);
			break;
		}
		case 17: {
			short v;
			v = c.getZoneID(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		default:
			return false;
		}
		return true;
	}

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {
		IASZoneServer c = ((IASZoneServer) getSinglePeerCluster((IASZoneServer.class.getName())));
		switch (attrId) {
		case 0:
		case 1:
		case 2:
		case 17:
			return ZCL.READ_ONLY;
		case 16: {
			byte[] v = ZclDataTypeIEEEAddress.zclParse(zclFrame);
			c.setIAS_CIE_Address(v, endPoint.getDefaultRequestContext());
			break;
		}
		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
		return ZCL.SUCCESS;
	}

}