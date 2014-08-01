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
package org.energy_home.jemma.ah.ebrain.old;

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.ebrain.CalendarUtil;


public class PowerProfilePhaseExtended {
	// A % number that bias the allocated power consumption between the peak power (100%)
	// and the average power (0%)
	public static float BIAS_POWER_FACTOR = 0.84f;
	public static int INDEFINITE_DURATION = 0xffff;
	// limit allocation to 24 hours for indefinite durations
	public static int MAXIMUM_DURATION = 24 * 60;

	
	private PowerProfileTransferredPhase transferredPhase;
	// the average power is exactly the mean of the energy in the phase's duration
	private float averagePower;
	
	// the allocated power consumption during the scheduling which is a biased compromise
	// between the average and the peak power (see below)
	private float biasedPower;
	
	// the scheduled delay of this phase w.r.t. the previous one. For the 1st phase this
	// corresponds to the delay of the power-profile w.r.t. to the "current" time.
	private float scheduledDelay;
	
	// effective duration used for energy allocation;
	private int duration = MAXIMUM_DURATION;
	private int slotDuration;
	private int slotMaxDelay;
	
	public PowerProfilePhaseExtended(PowerProfileTransferredPhase phase) {
		this (phase, 1);
	}
	public PowerProfilePhaseExtended(PowerProfileTransferredPhase phase, float energyConverter) {
		transferredPhase = phase;
		if (phase.ExpectedDuration == INDEFINITE_DURATION || phase.Energy == 0) {
			averagePower = biasedPower = phase.PeakPower;
		} else {
			float energy = energyConverter * ((float)phase.Energy);
			// now the energy is in W/h while duration is in minutes so multiply by 60 and divide by duration.
			averagePower = 60 * energy / phase.ExpectedDuration;
			biasedPower = BIAS_POWER_FACTOR * (phase.PeakPower - averagePower);
			biasedPower += averagePower;
		}
		
		if (phase.ExpectedDuration < INDEFINITE_DURATION && phase.ExpectedDuration > 0) duration = phase.ExpectedDuration;
		slotDuration = CalendarUtil.slotsFromMinutes(duration);
		slotMaxDelay = CalendarUtil.slotsFromMinutes(phase.MaxActivationDelay);
	}

	public PowerProfileTransferredPhase getTransferredPhase() {
		return transferredPhase;
	}

	public float getRequiredPower() {
		return biasedPower;
	}
	
	public float getAgeragePower() {
		return averagePower;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public int getSlotDuration() {
		return slotDuration;
	}
	
	public int getSlotMaxDelay() {
		return slotMaxDelay;
	}
	
	public float getScheduledDelay() {
		return scheduledDelay;
	}
	
	public void setScheduledDelay(float time) {
		scheduledDelay = time;
	}
	
	public void allocateAverageEnergy(int start, float[] energyAllocation) {
		allocateEnergy(start, averagePower, energyAllocation);
	}
	
	public void allocateBiasedEnergy(int start, float[] energyAllocation) {
		allocateEnergy(start, biasedPower, energyAllocation);
	}
	
	public void allocatePeakEnergy(int start, float[] energyAllocation) {
		allocateEnergy(start, transferredPhase.PeakPower, energyAllocation);
	}
	
	private void allocateEnergy(int start, float energy, float[] energyAllocation) {
		for (int i = slotDuration; --i >= 0; energyAllocation[start + i] += energy);
	}
}