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

import org.energy_home.jemma.ah.cluster.zigbee.general.AddGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GetGroupMembershipResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.GroupsServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveGroupResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.ViewGroupResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGroupsServer extends ZclServiceCluster implements GroupsServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 4;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclGroupsServer.ATTR_NameSupport_NAME, new ZclAttributeDescriptor(0,
				ZclGroupsServer.ATTR_NameSupport_NAME, new ZclDataTypeBitmap8(), null, true, 1));
	}

	public ZclGroupsServer() throws ApplianceException {
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

	public AddGroupResponse execAddGroup(int GroupID, String GroupName, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeString.zclSize(GroupName);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeString.zclSerialize(zclFrame, GroupName);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
		return (ZclAddGroupResponse.zclParse(zclResponseFrame));
	}

	public ViewGroupResponse execViewGroup(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclViewGroupResponse.zclParse(zclResponseFrame));
	}

	public GetGroupMembershipResponse execGetGroupMembership(int[] GroupList, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 1;
		if (GroupList != null) {
			size += GroupList.length * 2;
		}

		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);

		if (GroupList != null) {
			ZclDataTypeUI8.zclSerialize(zclFrame, (short) GroupList.length);
			for (int i = 0; i < GroupList.length; i++) {
				ZclDataTypeUI16.zclSerialize(zclFrame, GroupList[i]);
			}
		} else {
			ZclDataTypeUI8.zclSerialize(zclFrame, (short) 0);
		}

		IZclFrame zclResponseFrame = issueExec(zclFrame, 2, context);
		return (ZclGetGroupMembershipResponse.zclParse(zclResponseFrame));
	}

	public RemoveGroupResponse execRemoveGroup(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 3, context);
		return (ZclRemoveGroupResponse.zclParse(zclResponseFrame));
	}

	public void execRemoveAllGroups(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(4);
		issueExec(zclFrame, 11, context);
	}

	public void execAddGroupIfIdentifying(int GroupID, String GroupName, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeString.zclSize(GroupName);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(5);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeString.zclSerialize(zclFrame, GroupName);
		issueExec(zclFrame, 11, context);
	}

	public short getNameSupport(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

}
