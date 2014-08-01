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

import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyQueryResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclIdentifyServer extends ZclServiceCluster implements IdentifyServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 3;

	static Map attributesMapByName = null;
	static Map attributesMapById = null;

	static ZclAttributeDescriptor[] attributeDescriptors = { new ZclAttributeDescriptor(0,
			ZclIdentifyServer.ATTR_IdentifyTime_NAME, new ZclDataTypeUI16(), null, true, 0) };

	static {
		attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
		attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
	}

	public ZclIdentifyServer() throws ApplianceException {
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

	public void execIdentify(int IdentifyTime, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(IdentifyTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI16.zclSerialize(zclFrame, IdentifyTime);
		issueExec(zclFrame, 11, context);
	}

	public IdentifyQueryResponse execIdentifyQuery(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
		return (ZclIdentifyQueryResponse.zclParse(zclResponseFrame));
	}

	public void setIdentifyTime(int IdentifyTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 0;
		int size = 3;
		size += ZclDataTypeUI16.zclSize(IdentifyTime);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeUI16.ZCL_DATA_TYPE);
		ZclDataTypeUI16.zclSerialize(zclFrame, IdentifyTime);
		issueSet(ZclIdentifyServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public int getIdentifyTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

}
