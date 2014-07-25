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

import org.energy_home.jemma.ah.cluster.zigbee.general.AlarmsClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.AlarmsServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.GetAlarmResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclAlarmsServer extends ZclServiceCluster implements AlarmsServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 9;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclAlarmsServer.ATTR_AlarmCount_NAME, new ZclAttributeDescriptor(0,
				ZclAlarmsServer.ATTR_AlarmCount_NAME, new ZclDataTypeUI16(), null, true, 1));
	}

	public ZclAlarmsServer() throws ApplianceException {
		super();
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		boolean handled;
		handled = super.notifyZclFrame(clusterId, zclFrame);
		if (handled) {
			return handled;
		}
		int commandId = zclFrame.getCommandId();
		if (zclFrame.isClientToServer()) {
			throw new ZclValidationException("invalid direction field");
		}
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		AlarmsClient c = ((AlarmsClient) getSinglePeerCluster((AlarmsClient.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseAlarm(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclAlarmsServer.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
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

	public void execResetAlarm(short AlarmCode, int ClusterIdentifier, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(AlarmCode);
		size += ZclDataTypeUI16.zclSize(ClusterIdentifier);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeEnum8.zclSerialize(zclFrame, AlarmCode);
		ZclDataTypeUI16.zclSerialize(zclFrame, ClusterIdentifier);
		issueExec(zclFrame, 11, context);
	}

	public void execResetAllAlarms(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		issueExec(zclFrame, 11, context);
	}

	public GetAlarmResponse execGetAlarm(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(2);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclGetAlarmResponse.zclParse(zclResponseFrame));
	}

	public void execResetAlarmLog(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(3);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseAlarm(AlarmsClient o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short AlarmCode = ZclDataTypeEnum8.zclParse(zclFrame);
		int ClusterIdentifier = ZclDataTypeUI16.zclParse(zclFrame);
		o.execAlarm(AlarmCode, ClusterIdentifier, null);
		return null;
	}

	public int getAlarmCount(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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
