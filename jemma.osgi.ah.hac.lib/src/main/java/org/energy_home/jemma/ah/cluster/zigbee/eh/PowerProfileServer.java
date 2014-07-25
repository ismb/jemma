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

public interface PowerProfileServer {

	final static String ATTR_TotalProfileNum_NAME = "TotalProfileNum";
	final static String ATTR_MultipleScheduling_NAME = "MultipleScheduling";
	final static String ATTR_EnergyFormatting_NAME = "EnergyFormatting";
	final static String ATTR_EnergyRemote_NAME = "EnergyRemote";
	final static String ATTR_ScheduleMode_NAME = "ScheduleMode";
	final static String CMD_PowerProfileRequest_NAME = "PowerProfileRequest";
	final static String CMD_PowerProfileStateRequest_NAME = "PowerProfileStateRequest";
	final static String CMD_EnergyPhasesScheduleNotification_NAME = "EnergyPhasesScheduleNotification";
	final static String CMD_PowerProfileScheduleConstraintsRequest_NAME = "PowerProfileScheduleConstraintsRequest";
	final static String CMD_EnergyPhasesScheduleStateRequest_NAME = "EnergyPhasesScheduleStateRequest";

	public short getTotalProfileNum(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getMultipleScheduling(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getEnergyFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnergyRemote(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getScheduleMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setScheduleMode(short ScheduleMode, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public PowerProfileResponse execPowerProfileRequest(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public PowerProfileStateResponse execPowerProfileStateRequest(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execEnergyPhasesScheduleNotification(short PowerProfileID, ScheduledPhase[] ScheduledPhases,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public PowerProfileScheduleConstraintsResponse execPowerProfileScheduleConstraintsRequest(short PowerProfileID,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public EnergyPhasesScheduleStateResponse execEnergyPhasesScheduleStateRequest(short PowerProfileID,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
