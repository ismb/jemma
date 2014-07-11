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

import org.energy_home.jemma.ah.cluster.zigbee.measurement.RelativeHumidityMeasurementServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclRelativeHumidityMeasurementServer extends ZclServiceCluster implements RelativeHumidityMeasurementServer,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 1029;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclRelativeHumidityMeasurementServer.ATTR_MeasuredValue_NAME, new ZclAttributeDescriptor(0,
				ZclRelativeHumidityMeasurementServer.ATTR_MeasuredValue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclRelativeHumidityMeasurementServer.ATTR_MinMeasuredValue_NAME, new ZclAttributeDescriptor(1,
				ZclRelativeHumidityMeasurementServer.ATTR_MinMeasuredValue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclRelativeHumidityMeasurementServer.ATTR_MaxMeasuredValue_NAME, new ZclAttributeDescriptor(2,
				ZclRelativeHumidityMeasurementServer.ATTR_MaxMeasuredValue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclRelativeHumidityMeasurementServer.ATTR_Tolerance_NAME, new ZclAttributeDescriptor(3,
				ZclRelativeHumidityMeasurementServer.ATTR_Tolerance_NAME, new ZclDataTypeUI16(), null, true, 1));
	}

	public ZclRelativeHumidityMeasurementServer() throws ApplianceException {
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

	public int getMeasuredValue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getMinMeasuredValue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(1, new Integer(v));
		return v;
	}

	public int getMaxMeasuredValue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(2, new Integer(v));
		return v;
	}

	public int getTolerance(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(3, new Integer(v));
		return v;
	}

}
