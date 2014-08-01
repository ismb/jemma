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

public class EnergyPhaseInfo {
	
	public static class EnergyPhaseScheduleTime {
		private short energyPhaseID;
		private int scheduledDelay;
		
		public EnergyPhaseScheduleTime(short id, int time) {
			energyPhaseID = id;
			scheduledDelay = time;
		}
		
		public short getEnergyPhaseID() {
			return energyPhaseID;
		}
		public int getScheduledDelay() {
			return scheduledDelay;
		}
	}
	
	// % value that bias the allocated power consumption between the peak power (100%) and the average power (0%)
	public static final float BIAS_FACTOR = 0.85f;
	public static final int INDEFINITE_DURATION = 0xffff;
	// limit allocation to 24 hours for indefinite durations
	public static final int MAXIMUM_DURATION = 24 * 60;
	public static final int UNSCHEDULED_TIME = -1;
	
	
	private short energyPhaseID;
	private short macroPhaseID;
	// effective duration used for energy allocation;
	private int expectedDuration = MAXIMUM_DURATION;
	private int maxActivationDelay;
	private int peakPower;

	// is to be converted with the decimal formatting passed in the init method
	private float totalEnergy;
	
	// the energy in one slot for the average power.
	private float oneSlotMeanEnergy;
	
	// the allocated energy in one slot during the scheduling which is a biased delta
	// between the average and the peak power
	private float oneSlotBiasedPeakEnergy;
	
	// the scheduled slot of the phase when set by the scheduler
	private int scheduledSlot = UNSCHEDULED_TIME;
	private int slotDuration;
	private int slotMaxDelay;
		
	public EnergyPhaseInfo(short id) {
		energyPhaseID = id;
	}

	public short getEnergyPhaseID() {
		return energyPhaseID;
	}

	public short getMacroPhaseID() {
		return macroPhaseID;
	}

	public void setMacroPhaseID(short macroPhaseID) {
		this.macroPhaseID = macroPhaseID;
	}

	public int getExpectedDuration() {
		return expectedDuration;
	}

	public void setExpectedDuration(int expectedDuration) {
		this.expectedDuration = expectedDuration;
	}

	public int getPeakPower() {
		return peakPower;
	}

	public void setPeakPower(int peakPower) {
		this.peakPower = peakPower;
	}

	public float getTotalEnergy() {
		return totalEnergy;
	}

	public void setTotalEnergy(int totalEnergy) {
		this.totalEnergy = totalEnergy;
	}
	
	public int getMaxActivationDelay() {
		return maxActivationDelay;
	}

	public void setMaxActivationDelay(int maxActivationDelay) {
		this.maxActivationDelay = maxActivationDelay;
	}

	public float getMeanPower() {
		return oneSlotMeanEnergy * CalendarUtil.SLOTS_IN_ONE_HOUR;
	}
	
	public float getBiasedPeakPower() {
		return oneSlotBiasedPeakEnergy * CalendarUtil.SLOTS_IN_ONE_HOUR;
	}
	
	public float getOneSlotMeanEnergy() {
		return oneSlotMeanEnergy;
	}

	public float getOneSlotBiasedPeakEnergy() {
		return oneSlotBiasedPeakEnergy;
	}

	public int getSlotDuration() {
		return slotDuration;
	}
	
	public int getSlotMaxDelay() {
		return slotMaxDelay;
	}
	
	public int getScheduledSlot() {
		return scheduledSlot;
	}

	public void setScheduledSlot(int scheduledSlot) {
		this.scheduledSlot = scheduledSlot;
	}

	void init(float decimalFormatting) {
		totalEnergy *= decimalFormatting;

		// if energy is 0 use the peak power by duration; duration is in minutes so divide by 60
		if (totalEnergy == 0) totalEnergy = (float)peakPower * expectedDuration / 60;
		
		if (expectedDuration == INDEFINITE_DURATION || expectedDuration <= 0) expectedDuration = MAXIMUM_DURATION;
		slotDuration = CalendarUtil.slotsFromMinutes(expectedDuration);
		slotMaxDelay = CalendarUtil.slotsFromMinutes(maxActivationDelay);

		oneSlotMeanEnergy = totalEnergy / slotDuration;
		float oneSlotPeakEnergy = (float)peakPower / CalendarUtil.SLOTS_IN_ONE_HOUR;
		float biasDelta = BIAS_FACTOR * (oneSlotPeakEnergy - oneSlotMeanEnergy);
		oneSlotBiasedPeakEnergy = oneSlotMeanEnergy + biasDelta;
	}
	
	/*
	public void allocateMeanEnergy(int start, float[] energyAllocation) {
		allocateEnergy(start, oneSlotMeanEnergy, energyAllocation);
	}
	
	public void allocateBiasedPeakEnergy(int start, float[] energyAllocation) {
		allocateEnergy(start, oneSlotBiasedPeakEnergy, energyAllocation);
	}
	
	private void allocateEnergy(int start, float energySlot, float[] energyAllocation)  {
		for (int i = slotDuration; --i >= 0;)
			if (start + i < energyAllocation.length) energyAllocation[start + i] += energySlot;
			else energyAllocation[i] += energySlot * 100000;
	}
	*/	
	public String toString() {
		StringBuilder sb = new StringBuilder("\nEnergy Phase ID ").append(energyPhaseID);
		sb.append("\nduration = ").append(expectedDuration);
		sb.append("\nmax delay = ").append(maxActivationDelay);
		sb.append("\ntotal energy W/h = ").append(totalEnergy);
		sb.append("\npeak power = ").append(peakPower);
		sb.append("\nmean power = ").append(getMeanPower());
		sb.append("\nbiased peak power = ").append(getBiasedPeakPower());
		sb.append("\nscheduled delay = ").append(scheduledSlot);
		sb.append('\n');
		return sb.toString();
	}
}
