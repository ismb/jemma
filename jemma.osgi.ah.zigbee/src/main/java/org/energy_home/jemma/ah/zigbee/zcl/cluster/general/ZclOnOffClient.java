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

import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclOnOffClient extends ZclServiceCluster implements OnOffClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 6;

	public ZclOnOffClient() throws ApplianceException {
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
		OnOffServer c = ((OnOffServer) getSinglePeerCluster((OnOffServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseOff(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseOn(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseToggle(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseOnWithDuration(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclOnOffClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseOff(OnOffServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execOff(null);
		return null;
	}

	protected IZclFrame parseOn(OnOffServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execOn(null);
		return null;
	}

	protected IZclFrame parseToggle(OnOffServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execToggle(null);
		return null;
	}

	protected IZclFrame parseOnWithDuration(OnOffServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int OnDuration = ZclDataTypeUI16.zclParse(zclFrame);
		o.execOnWithDuration(OnDuration, null);
		return null;
	}

}
