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

import org.energy_home.jemma.ah.cluster.zigbee.general.AddGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GetGroupMembershipResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GroupsClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.GroupsServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.ViewGroupResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGroupsClient extends ZclServiceCluster implements GroupsClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 4;

	public ZclGroupsClient() throws ApplianceException {
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
		GroupsServer c = ((GroupsServer) getSinglePeerCluster((GroupsServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseAddGroup(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseViewGroup(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseGetGroupMembership(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseRemoveGroup(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseRemoveAllGroups(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseAddGroupIfIdentifying(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclGroupsClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseAddGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		String GroupName = ZclDataTypeString.zclParse(zclFrame);
		AddGroupResponse r = o.execAddGroup(GroupID, GroupName, endPoint.getDefaultRequestContext());
		int size = ZclAddGroupResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclAddGroupResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseViewGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		ViewGroupResponse r = o.execViewGroup(GroupID, endPoint.getDefaultRequestContext());
		int size = ZclViewGroupResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclViewGroupResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseGetGroupMembership(GroupsServer o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
		short GroupCount = ZclDataTypeUI8.zclParse(zclFrame);
		int GroupList[] = new int[GroupCount];
		for (int i = 0; i < GroupCount; i++)  {
		 GroupList[i] = ZclDataTypeUI16.zclParse(zclFrame);
		}
		
        GetGroupMembershipResponse r = o.execGetGroupMembership(GroupList, endPoint.getDefaultRequestContext());
        int size = ZclGetGroupMembershipResponse.zclSize(r);
        IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
        zclResponseFrame.setCommandId(2);
        ZclGetGroupMembershipResponse.zclSerialize(zclResponseFrame, r);
        return zclResponseFrame;
    }

	protected IZclFrame parseRemoveGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		RemoveGroupResponse r = o.execRemoveGroup(GroupID, endPoint.getDefaultRequestContext());
		int size = ZclRemoveGroupResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(3);
		ZclRemoveGroupResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseRemoveAllGroups(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		o.execRemoveAllGroups(null);
		return null;
	}

	protected IZclFrame parseAddGroupIfIdentifying(GroupsServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		String GroupName = ZclDataTypeString.zclParse(zclFrame);
		o.execAddGroupIfIdentifying(GroupID, GroupName, endPoint.getDefaultRequestContext());
		return null;
	}

}
