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
package org.energy_home.jemma.ah.eh.esp;

public class ESPConfigParameters {
	public static final float DEFAULT_CONTRACTUAL_POWER_THRESHOLD = 3000;
	public static final float DEFAULT_PEAK_PRODUCED_POWER = 0;
	
	private float contractualPowerThreshold = DEFAULT_CONTRACTUAL_POWER_THRESHOLD;
	private float peakProducedPower = DEFAULT_PEAK_PRODUCED_POWER;

	private void init(float contractualPowerThreshold, float peakProducedPower) {
		this.contractualPowerThreshold = contractualPowerThreshold;
		this.peakProducedPower = peakProducedPower;
	}
	
	public ESPConfigParameters() {
		init(DEFAULT_CONTRACTUAL_POWER_THRESHOLD, DEFAULT_PEAK_PRODUCED_POWER);
	}
	
	public ESPConfigParameters(float contractualPowerThreshold) {
		init(contractualPowerThreshold, DEFAULT_PEAK_PRODUCED_POWER);
	}

	public ESPConfigParameters(float contractualPowerThreshold, float peakProducedPower) {
		init(contractualPowerThreshold, peakProducedPower);
	}

	public float getContractualPowerThreshold() {
		return contractualPowerThreshold;
	}
	
	public void setContractualPowerThreshold(float contractualPowerThreshold) {
		this.contractualPowerThreshold =contractualPowerThreshold;
	}
	
	public float getPeakProducedPower() {
		return peakProducedPower;
	}
	
	public void setPeakProducedPower(float peakProducedPower) {
		this.peakProducedPower = peakProducedPower;
	}
	
	public String toString() {
		return "ESPConfigParameters [contractualPowerThreshold=" + contractualPowerThreshold + 
				", peakProducedPower=" + peakProducedPower + "]";
	}

}
