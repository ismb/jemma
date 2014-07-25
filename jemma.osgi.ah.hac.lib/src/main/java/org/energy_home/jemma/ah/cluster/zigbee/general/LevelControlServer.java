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

public interface LevelControlServer {

	final static String ATTR_CurrentLevel_NAME = "CurrentLevel";
	final static String ATTR_RemainingTime_NAME = "RemainingTime";
	final static String ATTR_OnOffTransitionTime_NAME = "OnOffTransitionTime";
	final static String ATTR_OnLevel_NAME = "OnLevel";
	final static String CMD_MoveToLevel_NAME = "MoveToLevel";
	final static String CMD_Move_NAME = "Move";
	final static String CMD_Step_NAME = "Step";
	final static String CMD_Stop_NAME = "Stop";
	final static String CMD_MoveToLevelWithOnOff_NAME = "MoveToLevelWithOnOff";
	final static String CMD_MoveWithOnOff_NAME = "MoveWithOnOff";
	final static String CMD_StepWithOnOff_NAME = "StepWithOnOff";
	final static String CMD_StopWithOnOff_NAME = "StopWithOnOff";

	public short getCurrentLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getOnOffTransitionTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getOnLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execMoveToLevel(short Level, int TransitionTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execMove(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStep(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execStop(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execMoveToLevelWithOnOff(short Level, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveWithOnOff(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStepWithOnOff(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execStopWithOnOff(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
