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

import org.energy_home.jemma.ah.cluster.zigbee.general.PollControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;

public class ZclPollControlClient extends ZclServiceCluster implements PollControlClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 32;

	public ZclPollControlClient() throws ApplianceException {
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
		PollControlServer c = ((PollControlServer) getSinglePeerCluster((PollControlServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseCheckInResponse(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseFastPollStop(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseSetLongPollInterval(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseSetShortPollInterval(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclPollControlClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseCheckInResponse(org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		boolean StartFastPolling = ZclDataTypeBoolean.zclParse(zclFrame);
		int FastPollTimeout = ZclDataTypeUI16.zclParse(zclFrame);
		o.execCheckInResponse(StartFastPolling, FastPollTimeout, null);
		return null;
	}

	protected IZclFrame parseFastPollStop(org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		o.execFastPollStop(null);
		return null;
	}

	protected IZclFrame parseSetLongPollInterval(org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		long NewLongPollInterval = ZclDataTypeUI32.zclParse(zclFrame);
		o.execSetLongPollInterval(NewLongPollInterval, null);
		return null;
	}

	protected IZclFrame parseSetShortPollInterval(org.energy_home.jemma.ah.cluster.zigbee.general.PollControlServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int NewShortPollInterval = ZclDataTypeUI16.zclParse(zclFrame);
		o.execSetShortPollInterval(NewShortPollInterval, null);
		return null;
	}

}
