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

public interface ScenesServer {

	final static String ATTR_SceneCount_NAME = "SceneCount";
	final static String ATTR_CurrentScene_NAME = "CurrentScene";
	final static String ATTR_CurrentGroup_NAME = "CurrentGroup";
	final static String ATTR_SceneValid_NAME = "SceneValid";
	final static String ATTR_NameSupport_NAME = "NameSupport";
	final static String ATTR_LastConfiguredBy_NAME = "LastConfiguredBy";
	final static String CMD_AddScene_NAME = "AddScene";
	final static String CMD_ViewScene_NAME = "ViewScene";
	final static String CMD_RemoveScene_NAME = "RemoveScene";
	final static String CMD_RemoveAllScenes_NAME = "RemoveAllScenes";
	final static String CMD_StoreScene_NAME = "StoreScene";
	final static String CMD_RecallScene_NAME = "RecallScene";
	final static String CMD_GetSceneMembership_NAME = "GetSceneMembership";

	public short getSceneCount(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCurrentScene(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentGroup(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getSceneValid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getNameSupport(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public byte[] getLastConfiguredBy(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public AddSceneResponse execAddScene(int GroupID, short SceneID, int TransitionTime, String SceneName, byte[] ExtensionFieldSet,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public ViewSceneResponse execViewScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public RemoveSceneResponse execRemoveScene(int GroupID, short SceneID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public RemoveAllScenesResponse execRemoveAllScenes(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStoreScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execRecallScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetSceneMembership(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

}
