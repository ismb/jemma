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

import java.util.Arrays;

public class EnergyPhasesScheduleResponse {

	public short PowerProfileID;
	public ScheduledPhase[] ScheduledPhases;

	public EnergyPhasesScheduleResponse() {
	}

	public EnergyPhasesScheduleResponse(short PowerProfileID, ScheduledPhase[] ScheduledPhases) {
		this.PowerProfileID = PowerProfileID;
		this.ScheduledPhases = ScheduledPhases;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("PowerProfileID=").append(PowerProfileID);
		sb.append(", ScheduledPhases=").append(Arrays.toString(ScheduledPhases));
		sb.append("}");
		return sb.toString();		
	}
}
