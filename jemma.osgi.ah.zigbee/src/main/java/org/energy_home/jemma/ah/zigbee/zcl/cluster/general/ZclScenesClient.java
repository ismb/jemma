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

import org.energy_home.jemma.ah.cluster.zigbee.general.AddSceneResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveAllScenesResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveSceneResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.ScenesClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.ViewSceneResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclScenesClient extends ZclServiceCluster implements ScenesClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 5;

	public ZclScenesClient() throws ApplianceException {
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
		ScenesServer c = ((ScenesServer) getSinglePeerCluster((ScenesServer.class
				.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseAddScene(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseViewScene(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseRemoveScene(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseRemoveAllScenes(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseStoreScene(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseRecallScene(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parseGetSceneMembership(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclScenesClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execStoreScenesResponse(short Status, int GroupID, short SceneID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(Status);
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Status);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseAddScene(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		int TransitionTime = ZclDataTypeUI16.zclParse(zclFrame);
		String SceneName = ZclDataTypeString.zclParse(zclFrame);
		byte[] ExtensionFieldSet = zclFrame.parseOctets();
		AddSceneResponse r = o.execAddScene(GroupID, SceneID, TransitionTime, SceneName, ExtensionFieldSet,
				endPoint.getDefaultRequestContext());
		int size = ZclAddSceneResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclAddSceneResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseViewScene(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		ViewSceneResponse r = o.execViewScene(GroupID, SceneID, endPoint.getDefaultRequestContext());
		int size = ZclViewSceneResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclViewSceneResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseRemoveScene(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		RemoveSceneResponse r = o.execRemoveScene(GroupID, SceneID, endPoint.getDefaultRequestContext());
		int size = ZclRemoveSceneResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(2);
		ZclRemoveSceneResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseRemoveAllScenes(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		RemoveAllScenesResponse r = o.execRemoveAllScenes(GroupID, endPoint.getDefaultRequestContext());
		int size = ZclRemoveAllScenesResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(3);
		ZclRemoveAllScenesResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseStoreScene(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		o.execStoreScene(GroupID, SceneID, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseRecallScene(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		o.execRecallScene(GroupID, SceneID, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseGetSceneMembership(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		o.execGetSceneMembership(GroupID, endPoint.getDefaultRequestContext());
		return null;
	}

}
