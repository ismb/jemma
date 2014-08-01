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

import org.energy_home.jemma.ah.cluster.zigbee.general.AlarmsClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.AlarmsServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.GetAlarmResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclAlarmsClient extends ZclServiceCluster implements AlarmsClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 9;

	public ZclAlarmsClient() throws ApplianceException {
		super();
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
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		AlarmsServer c = ((AlarmsServer) getSinglePeerCluster((AlarmsServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseResetAlarm(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseResetAllAlarms(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseGetAlarm(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseResetAlarmLog(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclAlarmsClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execAlarm(short AlarmCode, int ClusterIdentifier, IEndPointRequestContext context) throws ApplianceException,
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

	protected IZclFrame parseResetAlarm(AlarmsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short AlarmCode = ZclDataTypeEnum8.zclParse(zclFrame);
		int ClusterIdentifier = ZclDataTypeUI16.zclParse(zclFrame);
		o.execResetAlarm(AlarmCode, ClusterIdentifier, null);
		return null;
	}

	protected IZclFrame parseResetAllAlarms(AlarmsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execResetAllAlarms(null);
		return null;
	}

	protected IZclFrame parseGetAlarm(AlarmsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		GetAlarmResponse r = o.execGetAlarm(null);
		int size = ZclGetAlarmResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclGetAlarmResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseResetAlarmLog(AlarmsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execResetAlarmLog(null);
		return null;
	}

}
