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

import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffSwitchConfigurationServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;

public class ZclOnOffSwitchConfigurationServer extends ZclServiceCluster implements OnOffSwitchConfigurationServer,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 7;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclOnOffSwitchConfigurationServer.ATTR_SwitchType_NAME, new ZclAttributeDescriptor(0,
				ZclOnOffSwitchConfigurationServer.ATTR_SwitchType_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclOnOffSwitchConfigurationServer.ATTR_SwitchActions_NAME, new ZclAttributeDescriptor(16,
				ZclOnOffSwitchConfigurationServer.ATTR_SwitchActions_NAME, new ZclDataTypeEnum8(), null, true, 0));
	}

	public ZclOnOffSwitchConfigurationServer() throws ApplianceException {
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

	public short getSwitchType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public void setSwitchActions(short SwitchActions, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 16;
		int size = 3;
		size += ZclDataTypeEnum8.zclSize(SwitchActions);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeEnum8.ZCL_DATA_TYPE);
		ZclDataTypeEnum8.zclSerialize(zclFrame, SwitchActions);
		issueSet(ZclOnOffSwitchConfigurationServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getSwitchActions(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(16, new Short(v));
		return v;
	}

}
