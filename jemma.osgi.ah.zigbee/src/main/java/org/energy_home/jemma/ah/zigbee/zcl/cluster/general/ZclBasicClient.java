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

import org.energy_home.jemma.ah.cluster.zigbee.general.BasicClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.BasicServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclBasicClient extends ZclServiceCluster implements BasicClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 0;

	public ZclBasicClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor[] getPeerAttributeDescriptors() {
		return ZclBasicServer.attributeDescriptors;
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0x00:
			return ZclDataTypeUI8.zclSize((short) 0);

		case 0x07:
			return ZclDataTypeEnum8.zclSize((byte) 0);

		default:
			throw new UnsupportedClusterOperationException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ServiceClusterException,
			ApplianceException {
		BasicServer c = ((BasicServer) getSinglePeerCluster((BasicServer.class.getName())));

		switch (attrId) {
		case 0x00: {
			// ZCLVersion
			short v = c.getZCLVersion(null);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}

		case 0x07: {
			// PowerSource
			short v = c.getPowerSource(null);
			ZclDataTypeEnum8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeEnum8.ZCL_DATA_TYPE);
			ZclDataTypeEnum8.zclSerialize(zclResponseFrame, v);
			break;
		}

		default:
			return false;
		}
		return true;
	}
	
	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {

		switch (attrId) {
		case 0x00:
		case 0x07:
			return ZCL.READ_ONLY;

		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
	}
}
