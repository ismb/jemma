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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.custom;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.custom.SimpleMetering4NoksServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;

public class ZclSimpleMetering4NoksServer extends ZclServiceCluster implements SimpleMetering4NoksServer, ZigBeeDeviceListener {

	final static short CLUSTER_ID = 12;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclSimpleMetering4NoksServer.ATTR_Power_NAME, new ZclAttributeDescriptor(0,
				ZclSimpleMetering4NoksServer.ATTR_Power_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclSimpleMetering4NoksServer.ATTR_Energy_NAME, new ZclAttributeDescriptor(1,
				ZclSimpleMetering4NoksServer.ATTR_Energy_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclSimpleMetering4NoksServer.ATTR_TimeValue_NAME, new ZclAttributeDescriptor(2,
				ZclSimpleMetering4NoksServer.ATTR_TimeValue_NAME, new ZclDataTypeUI32(), null, true, 1));
	}

	public ZclSimpleMetering4NoksServer() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected int getProfileId() {
		return 0xC23C;
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

	public long getPower(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(0, new Long(v));
		return v;
	}

	public long getEnergy(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(1, new Long(v));
		return v;
	}

	public long getTimeValue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(2, new Long(v));
		return v;
	}

}
