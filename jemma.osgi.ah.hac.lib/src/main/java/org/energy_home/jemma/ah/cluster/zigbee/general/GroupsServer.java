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
package org.energy_home.jemma.ah.cluster.zigbee.general;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface GroupsServer {

	final static String ATTR_NameSupport_NAME = "NameSupport";
	final static String CMD_AddGroup_NAME = "AddGroup";
	final static String CMD_ViewGroup_NAME = "ViewGroup";
	final static String CMD_GetGroupMembership_NAME = "GetGroupMembership";
	final static String CMD_RemoveGroup_NAME = "RemoveGroup";
	final static String CMD_RemoveAllGroups_NAME = "RemoveAllGroups";
	final static String CMD_AddGroupIfIdentifying_NAME = "AddGroupIfIdentifying";

	public short getNameSupport(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public AddGroupResponse execAddGroup(int GroupID, String GroupName, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public ViewGroupResponse execViewGroup(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public GetGroupMembershipResponse execGetGroupMembership(int[] GroupList, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public RemoveGroupResponse execRemoveGroup(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execRemoveAllGroups(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execAddGroupIfIdentifying(int GroupID, String GroupName, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

}
