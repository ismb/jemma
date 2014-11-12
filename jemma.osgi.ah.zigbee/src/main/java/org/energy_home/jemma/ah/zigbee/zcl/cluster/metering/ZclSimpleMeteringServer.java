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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.metering;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI48;

public class ZclSimpleMeteringServer extends ZclServiceCluster implements SimpleMeteringServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 1794;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, new ZclAttributeDescriptor(0,
				ZclSimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, new ZclDataTypeUI48(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_CurrentSummationReceived_NAME, new ZclAttributeDescriptor(1,
				ZclSimpleMeteringServer.ATTR_CurrentSummationReceived_NAME, new ZclDataTypeUI48(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_PowerFactor_NAME, new ZclAttributeDescriptor(6,
				ZclSimpleMeteringServer.ATTR_PowerFactor_NAME, new ZclDataTypeI8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_Status_NAME, new ZclAttributeDescriptor(512,
				ZclSimpleMeteringServer.ATTR_Status_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_UnitOfMeasure_NAME, new ZclAttributeDescriptor(768,
				ZclSimpleMeteringServer.ATTR_UnitOfMeasure_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_Multiplier_NAME, new ZclAttributeDescriptor(769,
				ZclSimpleMeteringServer.ATTR_Multiplier_NAME, new ZclDataTypeUI24(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_Divisor_NAME, new ZclAttributeDescriptor(770,
				ZclSimpleMeteringServer.ATTR_Divisor_NAME, new ZclDataTypeUI24(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_SummationFormatting_NAME, new ZclAttributeDescriptor(771,
				ZclSimpleMeteringServer.ATTR_SummationFormatting_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_DemandFormatting_NAME, new ZclAttributeDescriptor(772,
				ZclSimpleMeteringServer.ATTR_DemandFormatting_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_MeteringDeviceType_NAME, new ZclAttributeDescriptor(774,
				ZclSimpleMeteringServer.ATTR_MeteringDeviceType_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclSimpleMeteringServer.ATTR_IstantaneousDemand_NAME, new ZclAttributeDescriptor(1024,
				ZclSimpleMeteringServer.ATTR_IstantaneousDemand_NAME, new ZclDataTypeI24(), null, true, 1));
	}

	public ZclSimpleMeteringServer() throws ApplianceException {
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

	public long getCurrentSummationDelivered(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		long v = ZclDataTypeUI48.zclParse(zclFrame);
		setCachedAttributeObject(0, new Long(v));
		return v;
	}

	public long getCurrentSummationReceived(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		long v = ZclDataTypeUI48.zclParse(zclFrame);
		setCachedAttributeObject(1, new Long(v));
		return v;
	}

	public short getPowerFactor(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		short v = ZclDataTypeI8.zclParse(zclFrame);
		setCachedAttributeObject(6, new Short(v));
		return v;
	}

	public short getStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(512, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(512, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(512, new Short(v));
		return v;
	}

	public short getUnitOfMeasure(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(768, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(768, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(768, new Short(v));
		return v;
	}

	public int getMultiplier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(769, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(769, context);
		int v = ZclDataTypeUI24.zclParse(zclFrame);
		setCachedAttributeObject(769, new Integer(v));
		return v;
	}

	public int getDivisor(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(770, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(770, context);
		int v = ZclDataTypeUI24.zclParse(zclFrame);
		setCachedAttributeObject(770, new Integer(v));
		return v;
	}

	public short getSummationFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(771, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(771, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(771, new Short(v));
		return v;
	}

	public short getDemandFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(772, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(772, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(772, new Short(v));
		return v;
	}

	public short getMeteringDeviceType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(774, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(774, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(774, new Short(v));
		return v;
	}

	public int getIstantaneousDemand(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1024, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1024, context);
		int v = ZclDataTypeI24.zclParse(zclFrame);
		setCachedAttributeObject(1024, new Integer(v));
		return v;
	}

}
