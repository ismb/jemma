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
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileTimeConstraints;

/*
 * 0x00  PowerProfileNotification  M
 * 0x01  PowerProfileResponse  M  >>>> MISSING!!!
 * 0x02  PowerProfileStateResponse M  >>>> MISSING!!!
 * 0x03  GetPowerProfilePrice  O
 * 0x04  PowerProfilesStateNotification  M
 * 0x05  GetOverallSchedulePrice   O
 * 0x06  EnergyPhasesScheduleRequest  M
 * 0x07  EnergyPhasesScheduleStateResponse  M  >>>> MISSING!!!
 * 0x08  EnergyPhasesScheduleStateNotification  M
 * 0x09  PowerProfileScheduleConstraintsNotification  M
 * 0x0A  PowerProfileScheduleConstraintsResponse  M  >>>> MISSING!!!
 * 0x0B  GetPowerProfilePrice Extended  O
 *
 * 
 * N.B. the 'get' prefix should be reserved in general to properties or to any method that won't need
 * computation that may take some time. If that's not the case is best to avoid using 'get' and use
 * another a more meaningful prefix, like 'calculate' or 'compute'
 */

public interface IPowerProfileListener {
	
	void notifyPowerProfile(String applianceId, PowerProfileInfo powerProfile);
	void notifyAllPowerProfilesState(String applianceId, PowerProfileState[] powerProfilesState);
	void notifyEnergyPhasesScheduleTime(String applianceId, short powerProfileID, EnergyPhaseScheduleTime[] scheduledPhases);
	void notifyPowerProfileScheduleConstraints(String applianceId, PowerProfileTimeConstraints profileConstraints);

	float calculatePowerProfilePrice(String applianceId, short powerProfileID, int delay);
	// the difference here is that the profileID is not applicable: OVERALL_SCHEDULE_ID
	float calculateOverallSchedulePrice();
	
	EnergyPhaseScheduleTime[] calculateEnergyPhasesSchedule(String applianceId, short powerProfileID);
}
