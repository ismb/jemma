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

public class PowerProfile {
	public short PowerProfileID;
	public short EnergyPhaseID;
	public boolean PowerProfileRemoteControl;
	public short PowerProfileState;

	public PowerProfile(short PowerProfileID, short EnergyPhaseID, boolean PowerProfileRemoteControl, short PowerProfileState) {
		this.PowerProfileID = PowerProfileID;
		this.EnergyPhaseID = EnergyPhaseID;
		this.PowerProfileRemoteControl = PowerProfileRemoteControl;
		this.PowerProfileState = PowerProfileState;
	}

	public PowerProfile() {
		// TODO Auto-generated constructor stub
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("PowerProfileID=").append(PowerProfileID);
		sb.append(", EnergyPhaseID=").append(EnergyPhaseID);
		sb.append(", PowerProfileRemoteControl=").append(PowerProfileRemoteControl);
		sb.append(", PowerProfileState=").append(PowerProfileState);
		sb.append("}");
		return sb.toString();		
	}
}
