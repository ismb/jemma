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

public interface PowerProfileClient {

	final static String CMD_PowerProfileNotification_NAME = "PowerProfileNotification";
	final static String CMD_GetPowerProfilePrice_NAME = "GetPowerProfilePrice";
	final static String CMD_PowerProfilesStateNotification_NAME = "PowerProfilesStateNotification";
	final static String CMD_GetOverallSchedulePrice_NAME = "GetOverallSchedulePrice";
	final static String CMD_EnergyPhasesScheduleRequest_NAME = "EnergyPhasesScheduleRequest";
	final static String CMD_EnergyPhasesScheduleStateNotification_NAME = "EnergyPhasesScheduleStateNotification";
	final static String CMD_PowerProfileScheduleConstraintsNotification_NAME = "PowerProfileScheduleConstraintsNotification";
	final static String CMD_GetPowerProfilePriceExtended_NAME = "GetPowerProfilePriceExtended";

	public void execPowerProfileNotification(short TotalProfileNum, short PowerProfileID,
			PowerProfileTransferredPhase[] PowerProfileTransferredPhases, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public GetPowerProfilePriceResponse execGetPowerProfilePrice(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execPowerProfilesStateNotification(PowerProfile[] PowerProfiles,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public GetOverallSchedulePriceResponse execGetOverallSchedulePrice(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public EnergyPhasesScheduleResponse execEnergyPhasesScheduleRequest(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execEnergyPhasesScheduleStateNotification(short PowerProfileID, ScheduledPhase[] ScheduledPhases,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execPowerProfileScheduleConstraintsNotification(short PowerProfileID, int StartAfter, int StopBefore,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public GetPowerProfilePriceExtendedResponse execGetPowerProfilePriceExtended(short Options, short PowerProfileID, int PowerProfileStartTime,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
