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

public class PowerProfileTransferredPhase {
	public short EnergyPhaseID;
	public short MacroPhaseID;
	public int ExpectedDuration;
	public int PeakPower;
	public int Energy;
	public int MaxActivationDelay;

	public PowerProfileTransferredPhase(short EnergyPhaseID, short MacroPhaseID, int ExpectedDuration, int PeakPower, int Energy,
			int MaxActivationDelay) {
		this.EnergyPhaseID = EnergyPhaseID;
		this.MacroPhaseID = MacroPhaseID;
		this.ExpectedDuration = ExpectedDuration;
		this.PeakPower = PeakPower;
		this.Energy = Energy;
		this.MaxActivationDelay = MaxActivationDelay;

	}

	public PowerProfileTransferredPhase() {
		// TODO Auto-generated constructor stub
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("EnergyPhaseId=").append(EnergyPhaseID);
		sb.append(", MacroPhaseID=").append(MacroPhaseID);
		sb.append(", ExpectedDuration=").append(ExpectedDuration);
		sb.append(", PeakPower=").append(PeakPower);
		sb.append(", Energy=").append(Energy);
		sb.append(", MaxActivationDelay=").append(MaxActivationDelay);
		sb.append("}");
		return sb.toString();		
	}
}
