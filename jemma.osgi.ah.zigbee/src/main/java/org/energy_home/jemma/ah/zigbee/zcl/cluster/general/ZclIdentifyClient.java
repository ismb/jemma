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

import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyQueryResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclTypes;

public class ZclIdentifyClient extends ZclServiceCluster implements IdentifyClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 3;

	public ZclIdentifyClient() throws ApplianceException {
		super();
	}

	protected IZclAttributeDescriptor[] getPeerAttributeDescriptors() {
		return ZclIdentifyServer.attributeDescriptors;
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

		// here we don't implement any manufacturer specific commands
		if (zclFrame.isManufacturerSpecific())
			return false;

		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		IdentifyServer c = ((IdentifyServer) getSinglePeerCluster((IdentifyServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseIdentify(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseIdentifyQuery(c, zclFrame);
			break;

		default:
			throw new ZclException(ZCL.UNSUP_CLUSTER_COMMAND);
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			} else
				return true;
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclIdentifyClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0x00:
			return ZclDataTypeUI16.zclSize((short) 0);

		default:
			throw new UnsupportedClusterOperationException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ServiceClusterException,
			ApplianceException {
		IdentifyServer c = ((IdentifyServer) getSinglePeerCluster((IdentifyServer.class.getName())));

		switch (attrId) {
		case 0x00: {
			int v = c.getIdentifyTime(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, (short) ZclDataTypeUI16.ZCL_DATA_TYPE);
			ZclDataTypeUI16.zclSerialize(zclResponseFrame, v);
			break;
		}

		default:
			return false;
		}
		return true;
	}

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {

		IdentifyServer c = ((IdentifyServer) getSinglePeerCluster((IdentifyServer.class.getName())));

		switch (attrId) {
		case 0x00: {
			if (dataType != ZclTypes.ZclUInt16Type) {
				return ZCL.INVALID_DATA_TYPE;
			}

			int IdentifyTime = ZclDataTypeUI16.zclParse(zclFrame);
			c.setIdentifyTime(IdentifyTime, endPoint.getDefaultRequestContext());
			break;
		}

		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
		return ZCL.SUCCESS;
	}

	protected IZclFrame parseIdentify(IdentifyServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int IdentifyTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execIdentify(IdentifyTime, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseIdentifyQuery(IdentifyServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		IdentifyQueryResponse r = o.execIdentifyQuery(endPoint.getDefaultRequestContext());
		if (r.Timeout > 0) {
			int size = ZclIdentifyQueryResponse.zclSize(r);
			IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
			zclResponseFrame.setCommandId(0);
			ZclIdentifyQueryResponse.zclSerialize(zclResponseFrame, r);
			return zclResponseFrame;
		} else
			return null;
	}
}
