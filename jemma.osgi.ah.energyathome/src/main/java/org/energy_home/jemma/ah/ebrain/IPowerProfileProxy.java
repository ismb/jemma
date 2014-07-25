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
package org.energy_home.jemma.ah.ebrain;

import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo.EnergyPhaseScheduleTime;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileState;

public interface IPowerProfileProxy {

	public static float INVALID_FORMATTING_VALUE = -1;	
	//------------------------------------------------------------------------------------------------------------
	// ATTRIBUTE & COMMANDS for PowerProfile Cluster Server
	//------------------------------------------------------------------------------------------------------------
	public static final short ENERGY_FORMATTING_DECIMAL_DIGITS_MASK = 0x7; // bits 0-2
	public static final short ENERGY_FORMATTING_INTEGER_DIGITS_MASK = 0x38; // bits 3-6

	//------------------------------------------------------------------------------------------------------------
	// GET&SET Attributes that are proxied to the remote zigbee device
	//------------------------------------------------------------------------------------------------------------
	// this should not be necessary since this info is included in PP notifications. 
	short getTotalProfileNum(String applianceId);

	// this should not be necessary since we assume ALL white goods support this.
	boolean isMultiplePhasesSchedulingSupported(String applianceId);

	boolean isEnergyRemoteSupported(String applianceId);

	// unit is Watt/hour. This call is necessary.
	float getEnergyFormatting(String applianceId);

	// The ScheduleMode attribute describes the criteria that should be used by the Power Profile cluster
	// 0 == Schedule Mode Cheapest / 1 == Schedule Mode Greenest
	short getScheduleMode(String applianceId);

	void setScheduleMode(String applianceId, short ScheduleMode);

	//------------------------------------------------------------------------------------------------------------
	// COMMANDS
	//------------------------------------------------------------------------------------------------------------
	void notifyProposedEnergyPhasesSchedule(String applianceId, short powerProfileID, EnergyPhaseScheduleTime[] epst);

//	PowerProfileTimeConstraints retrieveProfileScheduleConstraints(String applianceId, short powerProfileID);
//
//	EnergyPhaseScheduleTime[] retrieveEnergyPhasesScheduleTime(String applianceId, short powerProfileID);

	PowerProfileState[] retrieveAllPowerProfilesState(String applianceId);

	PowerProfileInfo retrievePowerProfile(String applianceId, short powerProfileID);
}