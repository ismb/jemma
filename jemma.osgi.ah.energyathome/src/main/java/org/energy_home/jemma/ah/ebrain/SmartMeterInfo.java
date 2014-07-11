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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.shal.DeviceInfo;

public class SmartMeterInfo extends ApplianceInfo {
	//TODO: check merge, line below not in 3.3.0
	//public static final int PRODUCED_ENERGY_MIN_INTERVAL = 10000;//MILLISECONDS_IN_MINUTE;
	private static final Logger LOG = LoggerFactory.getLogger( SmartMeterInfo.class );
	
	public SmartMeterInfo(DeviceInfo info) {
		super(info);
	}

	// Used to limit the number of received energy values that are used (-1 means all values are used) 
	private int nextTotalEnergyValidValues = -1;
	private int nextProducedEnergyValidValues = -1;
	
	private double lastProducedEnergy;  // last value of the energy sent by the appliance
	private float meanPower;
	private long lastValidProducedEnergyTime;
	private long lastProducedEnergyNotificationTime;
	private EnergyCostInfo accumulatedProducedEnergy = new EnergyCostInfo();
	
	public void setNextTotalEnergyValidValues (int i) {
		nextTotalEnergyValidValues = i;
	}
	public void setNextProducedEnergyValidValues (int i) {
		nextProducedEnergyValidValues = i;
	}
	
	public EnergyCostInfo updateEnergyCost(long newTime, double newEnergy) {
		if (nextTotalEnergyValidValues == 0) {
			LOG.debug("updateEnergyCost discarded value");
			return null;
		}
		if (nextTotalEnergyValidValues > 0)
			nextTotalEnergyValidValues--;
		return super.updateEnergyCost(newTime, newEnergy);
	}
	
	public EnergyCostInfo updateProducedEnergy(long newTime, double newEnergy) {
		if (nextProducedEnergyValidValues == 0) {
			LOG.debug("updateProducedEnergy discarded value");
			return null;
		}
		if (nextProducedEnergyValidValues > 0)
			nextProducedEnergyValidValues--;
		
		EnergyCostInfo eci = null;
		if (newTime < lastValidProducedEnergyTime) {
			LOG.warn(String.format("updateProducedEnergy: received new time value %d before last valid time value %d, returning null", newTime, lastValidProducedEnergyTime));
			return eci;
		}
		
		lastProducedEnergyNotificationTime = newTime;
		
		if (newEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			LOG.debug("updateProducedEnergy: received INVALID_ENERGY_VALUE, returning null");
			return eci;
		}
		
		long elapsedTime = newTime - lastValidProducedEnergyTime;
		if (elapsedTime > 0 && elapsedTime < MAX_VALID_EPOC_THRESHOLD) {
			//TODO: check merge, if condition was different in 3.3.0
			//if (elapsedTime < PRODUCED_ENERGY_MIN_INTERVAL) {
			if (elapsedTime < MILLISECS_IN_ONE_MINUTE) {
				LOG.debug(String.format("updateProducedEnergy %s: elapsed time < 1 minute, returning null", applianceId));
				return eci;
			}	
			
			double energyDelta = newEnergy - lastProducedEnergy;
			if (energyDelta < 0) energyDelta = 0;
			eci = new EnergyCostInfo(0, 0, 0, energyDelta);
			eci.setStartEndTime(lastValidProducedEnergyTime, newTime);
			accumulatedProducedEnergy.addValues(eci);
			
			meanPower = (float)(MILLISECONDS_IN_HOUR * energyDelta / elapsedTime);
		
		} else {
			LOG.warn(String.format("updateProducedEnergy: received new time value %d , elapsed time %d > MAX_VALID_EPOC_THRESHOLD, resetting start time", newTime, elapsedTime));
			accumulatedProducedEnergy.reset(newTime);
			meanPower = 0;
		}
		
		lastProducedEnergy = newEnergy;
		lastValidProducedEnergyTime = newTime;
		return eci;
	}
	
	public float getMeanProducedPower() {
		return meanPower;
	}
	
	public EnergyCostInfo getAccumulatedProducedEnergy() {
		return accumulatedProducedEnergy;
	}

	public void setAccumulatedProducedEnergy(EnergyCostInfo eci) {
		accumulatedProducedEnergy = eci;
		lastValidProducedEnergyTime = eci.endTime;
		lastProducedEnergy = eci.startEnergy + eci.deltaEnergy;
	}
	
	public double getProducedEnergy() {
		return lastProducedEnergy;
	}
	
	public long getProducedEnergyTime() {
		return lastProducedEnergyNotificationTime;
	}
}
