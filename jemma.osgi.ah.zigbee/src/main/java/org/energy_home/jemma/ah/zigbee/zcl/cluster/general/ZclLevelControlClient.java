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

import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclLevelControlClient extends ZclServiceCluster implements LevelControlClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 8;

	public ZclLevelControlClient() throws ApplianceException {
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
		LevelControlServer c = ((LevelControlServer) getSinglePeerCluster((LevelControlServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseMoveToLevel(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseMove(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseStep(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseStop(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseMoveToLevelWithOnOff(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseMoveWithOnOff(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parseStepWithOnOff(c, zclFrame);
			break;
		case 7:
			responseZclFrame = parseStopWithOnOff(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclLevelControlClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseMoveToLevel(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short Level = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToLevel(Level, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMove(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short MoveMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short Rate = ZclDataTypeUI8.zclParse(zclFrame);
		o.execMove(MoveMode, Rate, null);
		return null;
	}

	protected IZclFrame parseStep(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short StepMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short StepSize = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execStep(StepMode, StepSize, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseStop(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execStop(null);
		return null;
	}

	protected IZclFrame parseMoveToLevelWithOnOff(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short Level = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToLevelWithOnOff(Level, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveWithOnOff(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short MoveMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short Rate = ZclDataTypeUI8.zclParse(zclFrame);
		o.execMoveWithOnOff(MoveMode, Rate, null);
		return null;
	}

	protected IZclFrame parseStepWithOnOff(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short StepMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short StepSize = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execStepWithOnOff(StepMode, StepSize, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseStopWithOnOff(LevelControlServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		o.execStopWithOnOff(null);
		return null;
	}

}
