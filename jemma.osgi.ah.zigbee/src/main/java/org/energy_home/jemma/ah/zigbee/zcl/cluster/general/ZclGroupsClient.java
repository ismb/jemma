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

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

import org.energy_home.jemma.ah.cluster.zigbee.general.AddGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GetGroupMembershipResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GroupsClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.GroupsServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.ViewGroupResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public class ZclGroupsClient extends ZclServiceCluster implements GroupsClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 4;
	final static HashMap attributesMap = new HashMap();

	static {
	}

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

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		return ((IZclAttributeDescriptor) attributesMap.get(name));
	}

	protected IZclFrame parseAddGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		String GroupName = ZclDataTypeString.zclParse(zclFrame);
		AddGroupResponse r = o.execAddGroup(GroupID, GroupName, null);
		int size = ZclAddGroupResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(0);
		ZclAddGroupResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseViewGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		ViewGroupResponse r = o.execViewGroup(GroupID, null);
		int size = ZclViewGroupResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclViewGroupResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseGetGroupMembership(GroupsServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short GroupCount = ZclDataTypeUI8.zclParse(zclFrame);
		// FIXME: unable handle parameter 'GroupList' of type 'array of uint16'
		GetGroupMembershipResponse r = null;
		// o.execGetGroupMembership(GroupCount, null);
		int size = ZclGetGroupMembershipResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(2);
		ZclGetGroupMembershipResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseRemoveGroup(GroupsServer o, IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		RemoveGroupResponse r = o.execRemoveGroup(GroupID, null);
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
		o.execAddGroupIfIdentifying(GroupID, GroupName, null);
		return null;
	}

}
