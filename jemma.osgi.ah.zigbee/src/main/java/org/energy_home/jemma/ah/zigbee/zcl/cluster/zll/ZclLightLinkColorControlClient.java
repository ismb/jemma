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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.zll;

import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclLightLinkColorControlClient extends ZclServiceCluster implements ColorControlClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 768;

	public ZclLightLinkColorControlClient() throws ApplianceException {
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
		org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer c = ((org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer) getSinglePeerCluster((ColorControlServer.class
				.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseMoveToHue(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseMoveHue(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseStepHue(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseMoveToSaturation(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseMoveSaturation(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseStepSaturation(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parseMoveToHueAndSaturation(c, zclFrame);
			break;
		case 7:
			responseZclFrame = parseMoveToColor(c, zclFrame);
			break;
		case 8:
			responseZclFrame = parseMoveColor(c, zclFrame);
			break;
		case 9:
			responseZclFrame = parseStepColor(c, zclFrame);
			break;
		case 10:
			responseZclFrame = parseMoveToColorTemperature(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclLightLinkColorControlClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseMoveToHue(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Hue = ZclDataTypeUI8.zclParse(zclFrame);
		short Direction = ZclDataTypeEnum8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToHue(Hue, Direction, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveHue(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short MoveMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short Rate = ZclDataTypeUI8.zclParse(zclFrame);
		o.execMoveHue(MoveMode, Rate, null);
		return null;
	}

	protected IZclFrame parseStepHue(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short StepMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short StepSize = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execStepHue(StepMode, StepSize, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveToSaturation(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Saturation = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToSaturation(Saturation, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveSaturation(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short MoveMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short Rate = ZclDataTypeUI8.zclParse(zclFrame);
		o.execMoveSaturation(MoveMode, Rate, null);
		return null;
	}

	protected IZclFrame parseStepSaturation(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short StepMode = ZclDataTypeEnum8.zclParse(zclFrame);
		short StepSize = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execStepSaturation(StepMode, StepSize, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveToHueAndSaturation(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Hue = ZclDataTypeUI8.zclParse(zclFrame);
		short Saturation = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToHueAndSaturation(Hue, Saturation, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveToColor(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int ColorX = ZclDataTypeUI16.zclParse(zclFrame);
		int ColorY = ZclDataTypeUI16.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToColor(ColorX, ColorY, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveColor(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int RateX = ZclDataTypeI16.zclParse(zclFrame);
		int RateY = ZclDataTypeI16.zclParse(zclFrame);
		o.execMoveColor(RateX, RateY, null);
		return null;
	}

	protected IZclFrame parseStepColor(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int StepX = ZclDataTypeI16.zclParse(zclFrame);
		int StepY = ZclDataTypeI16.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execStepColor(StepX, StepY, TransitionTime, null);
		return null;
	}

	protected IZclFrame parseMoveToColorTemperature(org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short ColorTemperature = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		o.execMoveToColorTemperature(ColorTemperature, TransitionTime, null);
		return null;
	}

}
