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
package org.energy_home.jemma.ah.cluster.zigbee.eh;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface ApplianceControlServer {


	final static String ATTR_StartTime_NAME = "StartTime";
	final static String ATTR_FinishTime_NAME = "FinishTime";
	final static String ATTR_RemainingTime_NAME = "RemainingTime";
	final static String ATTR_CycleTarget0_NAME = "CycleTarget0";
	final static String ATTR_CycleTarget1_NAME = "CycleTarget1";
	final static String ATTR_TemperatureTarget0_NAME = "TemperatureTarget0";
	final static String ATTR_TemperatureTarget1_NAME = "TemperatureTarget1";
	final static String ATTR_Spin_NAME = "Spin";
	final static String ATTR_SuperCoolMode_NAME = "SuperCoolMode";
	final static String ATTR_SuperFreezeMode_NAME = "SuperFreezeMode";
	final static String ATTR_NormalMode_NAME = "NormalMode";
	final static String ATTR_EcoMode_NAME="EcoMode";
	final static String ATTR_HolidayMode_NAME = "HolidayMode";
	final static String ATTR_IceParty_NAME = "IceParty";
	final static String ATTR_RemoteControl_NAME = "RemoteControl";
	
	final static String CMD_CommandExecution_NAME = "CommandExecution";
	final static String CMD_SignalState_NAME = "SignalState";
	final static String CMD_WriteFunctions_NAME = "WriteFunctions";
	final static String CMD_OverloadPauseResume_NAME = "OverloadPauseResume";
	final static String CMD_OverloadPause_NAME = "OverloadPause";
	final static String CMD_OverloadWarning_NAME = "OverloadWarning";
	
	final static short CMD_Start_ID=1;
	final static short CMD_Stop_ID=2;
	final static short CMD_Pause_ID=3;

	public int getStartTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getFinishTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCycleTarget0(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	public short getCycleTarget1(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	public int getTemperatureTarget0(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	public int getTemperatureTarget1(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	public void execCommandExecution(short CommandId, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public SignalStateResponse execSignalState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execWriteFunctions(WriteAttributeRecord[] WriteAttributeRecords, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execOverloadPauseResume(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execOverloadPause(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execOverloadWarning(short WarningEvent, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;
	
	public short getSpin(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;

	public boolean getEcoMode(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
	
	public boolean getNormalMode(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
	
	public boolean getHolidayMode(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
	
	public boolean getIceParty(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
	
	public boolean getSuperCoolMode(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
	
	public boolean getSuperFreezeMode(IEndPointRequestContext context) throws ApplianceException,ServiceClusterException;
}
