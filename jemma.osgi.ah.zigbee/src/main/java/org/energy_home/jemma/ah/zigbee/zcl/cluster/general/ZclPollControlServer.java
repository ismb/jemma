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

import org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;

public class ZclPollControlServer extends ZclServiceCluster implements PollControlServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 32;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclPollControlServer.ATTR_ChecknInInterval_NAME, new ZclAttributeDescriptor(0,
				ZclPollControlServer.ATTR_ChecknInInterval_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_LongPollInterval_NAME, new ZclAttributeDescriptor(1,
				ZclPollControlServer.ATTR_LongPollInterval_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_ShortPollInterval_NAME, new ZclAttributeDescriptor(2,
				ZclPollControlServer.ATTR_ShortPollInterval_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_FastPollTimeout_NAME, new ZclAttributeDescriptor(3,
				ZclPollControlServer.ATTR_FastPollTimeout_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_CheckInIntervalMin_NAME, new ZclAttributeDescriptor(4,
				ZclPollControlServer.ATTR_CheckInIntervalMin_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_LongPollIntervalMin_NAME, new ZclAttributeDescriptor(5,
				ZclPollControlServer.ATTR_LongPollIntervalMin_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclPollControlServer.ATTR_FastPollTimeoutMax_NAME, new ZclAttributeDescriptor(6,
				ZclPollControlServer.ATTR_FastPollTimeoutMax_NAME, new ZclDataTypeUI16(), null, true, 1));
	}

	public ZclPollControlServer() throws ApplianceException {
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

	public void execCheckInResponse(boolean StartFastPolling, int FastPollTimeout, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeBoolean.zclSize(StartFastPolling);
		size += ZclDataTypeUI16.zclSize(FastPollTimeout);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeBoolean.zclSerialize(zclFrame, StartFastPolling);
		ZclDataTypeUI16.zclSerialize(zclFrame, FastPollTimeout);
		issueExec(zclFrame, 11, context);
	}

	public void execFastPollStop(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		issueExec(zclFrame, 11, context);
	}

	public void execSetLongPollInterval(long NewLongPollInterval, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI32.zclSize(NewLongPollInterval);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);
		ZclDataTypeUI32.zclSerialize(zclFrame, NewLongPollInterval);
		issueExec(zclFrame, 11, context);
	}

	public void execSetShortPollInterval(int NewShortPollInterval, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(NewShortPollInterval);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeUI16.zclSerialize(zclFrame, NewShortPollInterval);
		issueExec(zclFrame, 11, context);
	}

	public long getChecknInInterval(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public long getLongPollInterval(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getShortPollInterval(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getFastPollTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public long getCheckInIntervalMin(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(4, new Long(v));
		return v;
	}

	public long getLongPollIntervalMin(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(5, new Long(v));
		return v;
	}

	public int getFastPollTimeoutMax(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(6, new Integer(v));
		return v;
	}

}
