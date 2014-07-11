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

import java.util.Calendar;

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/*
 * This informative object represents a smart appliance and is a container
 * of the latest received profile (to evolve into a set of profiles).
 */
public class SmartAppliance {
	private static final Logger LOG = LoggerFactory.getLogger( SmartAppliance.class );
	
	public static final short STATE_UNKNOWN = 0;
	public static final short STATE_OFF = 1; // switched off by the user.
	public static final short STATE_ON = 2; // active and unstoppable
	public static final short STATE_SUSPENDED = 11;
	public static final short STATE_PENDING_SUSPENSION = 12;
	public static final short STATE_PENDING_ACTIVATION = 13;
	public static final short STATE_ACTIVE = 14; // active but suspendable
	
	public static String stateToString(short state) {
		switch (state) {
    		case STATE_OFF: return "STATE_OFF";
    		case STATE_ON: return "STATE_ON";
    		case STATE_SUSPENDED: return "STATE_SUSPENDED";
    		case STATE_PENDING_SUSPENSION: return "STATE_PENDING_SUSPENSION";
    		case STATE_ACTIVE: return "STATE_ACTIVE";
    		case STATE_PENDING_ACTIVATION: return "STATE_PENDING_ACTIVATION";
    		default: return "STATE_UNKNOWN";
		}
	}

	//private ApplianceDescriptor descriptor;
	private String applianceId;
	private short priority;

	private PowerProfileResponse profile;
	private PowerProfilePhaseExtended[] extendedPhases;
	
	// state change info
	protected volatile short currentState;
	private long lastStateChangeTime;
	
	// energy, power and cost info
	public static long MAX_VALID_EPOC_THRESHOLD = 100 * 24 * 3600 * 1000L; // 100 days * 24 hours * 3600 secs * 1000 millisecs
	public static int MILLISECS_IN_ONE_MINUTE = 60 * 1000;
	public static long MAX_SCHEDULING_WINDOW = 18 * 60 * MILLISECS_IN_ONE_MINUTE - 100; // 24 hours
	public static float INVALID_POWER_VALUE = 0xFFFFFF;
	public static double INVALID_ENERGY_VALUE = 0xFFFFFFFFFFFFL;
	//public static double MAX_VALID_ENERGY_THRESHOLD = 21000D / (3600 * 1000); // max delta energy per millisec (21KW/H)
	//public static int ENERGY_UPDATE_FREQUENCY_TOLERANCE = 10; // in minutes
	
	
	private MinMaxPowerInfo powerInfo = new MinMaxPowerInfo();
	//private float currentPowerConsumption;
	//private float maxPowerConsumption = 0;
	//private float minPowerConsumption = Float.POSITIVE_INFINITY;
	private double lastTotalEnergy;
	//private long lastPowerTime;
	private long lastNotificationTime;
	private long lastValidEnergyTime;
	//private long maxPowerConsumptionTime;
	//private long minPowerConsumptionTime;

		
	private EnergyCostInfo accumulatedEnergyCost = new EnergyCostInfo();

	public EnergyCostInfo getAccumulatedEnergyCost() {
		return accumulatedEnergyCost;
	}

	public void setAccumulatedEnergyCost(EnergyCostInfo eci) {
		accumulatedEnergyCost = eci;
		lastValidEnergyTime = eci.getEndTime();
		lastTotalEnergy = eci.getStartTotalEnergy() + eci.getDeltaEnergy();
	}
	
	public double getAccumulatedEnergy() {
		return lastTotalEnergy;
	}
	
	public long getAccumulatedEnergyTime() {
		return lastNotificationTime;
	}
	

	public EnergyCostInfo updateEnergyCost(long newTime, double newEnergy) {
		lastNotificationTime = newTime;
		EnergyCostInfo eci = null;
		if (newEnergy == INVALID_ENERGY_VALUE) {
			LOG.debug("received INVALID_ENERGY_VALUE, returning null");
			return eci;
		}
		
		try {
			long elapsedTime = newTime - lastValidEnergyTime;
			if (elapsedTime > 0 && elapsedTime < MAX_VALID_EPOC_THRESHOLD) {
				if (elapsedTime < MILLISECS_IN_ONE_MINUTE) {
					LOG.debug("elapsed time < 1 minute, returning null");
					return eci;
				}			
				
				double energyDelta = newEnergy - lastTotalEnergy;
				if (energyDelta > 0) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(lastValidEnergyTime);
					DailyTariff dt = DailyTariff.getInstance();
					eci = dt.computeMinMaxCosts(calendar, elapsedTime, energyDelta);
				} else {
					if (energyDelta < 0) {
						LOG.warn("invalid energy delta: " + energyDelta);
						}
					energyDelta = 0; // safety net in case it's negative
					eci = new EnergyCostInfo();
				}
				
				eci.setStartEndTime(lastValidEnergyTime, newTime);
				accumulatedEnergyCost.addValues(eci);
			
			} else {
				LOG.warn("elapsed time > MAX_VALID_EPOC_THRESHOLD, resetting start time");
				accumulatedEnergyCost.reset(newTime);
			}
			
		} catch (Exception e) {
			LOG.error("Exception on updateEnergyCost",e);
		}
		//maxPowerConsumption = 0;
		//minPowerConsumption = Float.POSITIVE_INFINITY;
		lastTotalEnergy = newEnergy;
		lastValidEnergyTime = newTime;
		return eci;
	}

	
	
	// info specific for scheduling appliances
	private Calendar earliestStartTime, latestEndTime;
	private long startAfter, endBefore;
	private int globalDuration, globalEnergy;
	
	public SmartAppliance(String id) {
		applianceId = id;
	}
	
	public String getApplianceId() {
		return applianceId;
	}
	
	public short getState() {
		return currentState;
	}
	
	public void setState(short state) {
		this.currentState = state;
		lastStateChangeTime = System.currentTimeMillis();
	}
	
	public long getLastStateChange() {
		return lastStateChangeTime;
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short p) {
		priority = p;
	}
	
	public MinMaxPowerInfo getMinMaxPowerInfo() {
		return powerInfo;
	}
	public float getIstantaneousPower() {
		return powerInfo.getCurrentPower();
	}
	
	public long getIstantaneousPowerTime() {
		return powerInfo.getCurrentPowerTime();
	}
	
	public void setIstantaneousPower(float ic, long time) {
		if (ic == INVALID_POWER_VALUE) ic = 0;	
		powerInfo.setCurrentPower(ic, time);
	}
	

	public PowerProfileResponse getPowerProfile() {
		return profile;
	}
	
	public void setPowerProfile(PowerProfileResponse profile) {
		this.profile = profile;
		PowerProfileTransferredPhase[] transferredPhases = profile.PowerProfileTransferredPhases;
		extendedPhases = new PowerProfilePhaseExtended[transferredPhases.length];
		globalDuration = globalEnergy = 0;
		for (int i = 0; i < transferredPhases.length; ++i) {
			extendedPhases[i] = new PowerProfilePhaseExtended(transferredPhases[i]);
			globalDuration += extendedPhases[i].getDuration();
			globalEnergy += extendedPhases[i].getRequiredPower();
		}
	}
	
	public PowerProfilePhaseExtended[] setPowerProfilePhases(PowerProfileTransferredPhase[] phases, float energyConverter) {
		return setPowerProfilePhases(phases, energyConverter, 0);
	}
	public PowerProfilePhaseExtended[] setPowerProfilePhases(PowerProfileTransferredPhase[] phases, float energyConverter, int offset) {
		extendedPhases = new PowerProfilePhaseExtended[phases.length - offset];
		for (int i = phases.length; --i >= offset;) {
			extendedPhases[i - offset] = new PowerProfilePhaseExtended(phases[i], energyConverter);
		}
		return extendedPhases;
	}
	
	
	public float allocatePowerProfile(float[] energyAllocation, int startSlot) {
		float maxPeakPower = 0;
		for (int i = 0; i < extendedPhases.length; ++i) {
			if (extendedPhases[i].getRequiredPower() > maxPeakPower) maxPeakPower = extendedPhases[i].getRequiredPower();
			extendedPhases[i].allocateAverageEnergy(startSlot, energyAllocation);
			startSlot += extendedPhases[i].getSlotDuration();
		}
		return maxPeakPower;
	}
	
	
	public long getEarliestProfileStart() {
		long now = System.currentTimeMillis();
		if (now > startAfter) startAfter = now;
		return startAfter;
	}
	
	public long getLatestProfileEnd() {
		if (endBefore - startAfter <= 0) endBefore = startAfter + MAX_SCHEDULING_WINDOW;
		return endBefore;
	}
	
	public void setPowerProfileConstraints(long start, long end) {
		startAfter = start;
		endBefore = end;
	}
	

	public PowerProfileTransferredPhase[] getProfileTransferredPhases() {
		return profile.PowerProfileTransferredPhases;
	}

	public PowerProfilePhaseExtended[] getProfilePhasesExtended() {
		return extendedPhases;
	}
	
	public Calendar getEarliestStartTime() {
		if (earliestStartTime == null) earliestStartTime = Calendar.getInstance();
		long now = System.currentTimeMillis();
		if (now > startAfter) startAfter = now;
		earliestStartTime.setTimeInMillis(startAfter);
		return earliestStartTime;
	}
	
	public long getProfileSchedulingWindow() {
		if (endBefore - startAfter <= 0) endBefore = startAfter + MAX_SCHEDULING_WINDOW;
		return endBefore - startAfter;
	}
	
	public void setEarliestStartTime(int delay) {
		earliestStartTime = Calendar.getInstance();
		earliestStartTime.add(Calendar.MINUTE, delay);
	}
	
	public Calendar getLatestEndTime() {
		if (latestEndTime == null) {
			latestEndTime = Calendar.getInstance();
			latestEndTime.add(Calendar.HOUR_OF_DAY, 24);
			//latestEndTime.add(Calendar.DAY_OF_MONTH, 1);
		}
		return latestEndTime;
	}
	
	public void setLatestEndTime(int delay) {
		latestEndTime = Calendar.getInstance();
		latestEndTime.add(Calendar.MINUTE, delay);
	}
	
	public int getProfileDuration() {
		return globalDuration;
	}
	
	public int getProfileRequiredEnergy() {
		return globalEnergy;
	}
}
