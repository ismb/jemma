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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.measurement.OccupancySensingServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclOccupancySensingServer extends ZclServiceCluster implements OccupancySensingServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 1030;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_Occupancy_NAME, new ZclAttributeDescriptor(0,
				ZclOccupancySensingServer.ATTR_Occupancy_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_OccupancySensorType_NAME, new ZclAttributeDescriptor(1,
				ZclOccupancySensingServer.ATTR_OccupancySensorType_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_PIROccupiedToUnoccupiedDelay_NAME, new ZclAttributeDescriptor(16,
				ZclOccupancySensingServer.ATTR_PIROccupiedToUnoccupiedDelay_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_PIRUnccupiedToOccupiedDelay_NAME, new ZclAttributeDescriptor(17,
				ZclOccupancySensingServer.ATTR_PIRUnccupiedToOccupiedDelay_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_UltraSonicOccupiedToUnoccupiedDelay_NAME,
				new ZclAttributeDescriptor(32, ZclOccupancySensingServer.ATTR_UltraSonicOccupiedToUnoccupiedDelay_NAME,
						new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclOccupancySensingServer.ATTR_UltraSonicPIRUnccupiedToOccupiedDelay_NAME,
				new ZclAttributeDescriptor(33, ZclOccupancySensingServer.ATTR_UltraSonicPIRUnccupiedToOccupiedDelay_NAME,
						new ZclDataTypeUI8(), null, true, 1));
	}

	public ZclOccupancySensingServer() throws ApplianceException {
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

	public short getOccupancy(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

	public short getOccupancySensorType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(1, new Short(v));
		return v;
	}

	public short getPIROccupiedToUnoccupiedDelay(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(16, new Short(v));
		return v;
	}

	public short getPIRUnccupiedToOccupiedDelay(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getUltraSonicOccupiedToUnoccupiedDelay(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
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

	public short getUltraSonicPIRUnccupiedToOccupiedDelay(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(33, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(33, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(33, new Short(v));
		return v;
	}
}
