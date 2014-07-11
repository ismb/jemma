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


import java.util.Calendar;

import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;
import org.energy_home.jemma.shal.DeviceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class is the hub of all information, data, state of a remote appliance. It contains a working status 
 * of a remote appliance and is the object passed to the various logic and algorithms to perform calculations
 * and decide actions.
 */

public class ApplianceInfo {
	private static final Logger LOG = LoggerFactory.getLogger( ApplianceInfo.class );

	public static final int MILLISECONDS_IN_MINUTE = 60 * 1000;
	public static final int MILLISECONDS_IN_HOUR = MILLISECONDS_IN_MINUTE * 60;
	public static long VALID_TIME_NOTIFICATION_INTERVAL = 12 * 60 * 60 * 1000; // 12 hours
	
	// energy, power and cost infos
	public static long MAX_VALID_EPOC_THRESHOLD = 100 * 24 * 3600 * 1000L; // 100 days * 24 hours * 3600 secs * 1000 millisecs
	public static int MILLISECS_IN_ONE_MINUTE = 60 * 1000;
	public static long MAX_SCHEDULING_WINDOW = 24 * 60 * MILLISECS_IN_ONE_MINUTE - 100; // 24 hours
	
	//public static short priceDecimals = TRAILING_DIGIT_TENTHS;
	
	//private static final PowerProfileInfo[] EMPTY_PROFILES = new PowerProfileInfo[0];
	
	/*
	 * NON STATIC FIELDS / METHODS
	 */
	protected String applianceId;
	protected boolean isAvailable;
	//private ApplianceType type;
	protected DeviceInfo deviceInfo;
	
	/*
	 *  The TotalProfileNum attribute represents the total number of profiles supported by the device.
	 */
	//private short totalProfileNum;
	/*
	 * The MultipleScheduling attribute specifies if the server side of the Power Profile cluster supports
	 * the scheduling of multiple Energy Phases or it does support the scheduling of a single energy
	 * phase of the Power profile at a time. 
	 */
	//private boolean multipleSchedulingSupported;
	/*
	 * The EnergyFormatting attribute SummationFormatting provides a method to properly decipher
	 * the number of digits and the decimal location of the values found in the Summation Information
	 * Set of attributes. This attribute is to be decoded as follows:
	 * Bits 0 to 2: Number of Digits to the right of the Decimal Point.
	 * Bits 3 to 6: Number of Digits to the left of the Decimal Point.
	 * Bit 7: If set, suppress leading zeros.
	 */
	//private float energyFormatting = IPowerProfileProxy.INVALID_FORMATTING_VALUE;
	/*
	 * The EnergyRemote attribute describes the remote control status of the power profile server
	 * cluster (e.g. appliance), which means the selection operated by the user on the remote control
	 * feature of theDevice.
	 */ 

	private float summationFormatting = IMeteringProxy.INVALID_FORMATTING_VALUE;
	private float demandFormatting = IMeteringProxy.INVALID_FORMATTING_VALUE;
	
	
	private double lastTotalEnergy;  // last value of the energy sent by the appliance
	private long lastNotificationTime;
	private long lastValidEnergyTime;
	
	private MinMaxPowerInfo powerInfo = new MinMaxPowerInfo();
	private EnergyCostInfo lastCostEnergy = new EnergyCostInfo(); // last value of the energy in the cost calculation
	private EnergyCostInfo accumulatedEnergyCost = new EnergyCostInfo();

	//private PowerProfileInfo[] powerProfileInfos = EMPTY_PROFILES;
	

	public ApplianceInfo(String id) {
		applianceId = id;
	}
	
	public ApplianceInfo(DeviceInfo info) {
		deviceInfo = info;
		applianceId = deviceInfo.getPersistentId();
	}

	public String getApplianceId() {
		return applianceId;
	}
	
	public boolean isAvailable() {
		return isAvailable;
	}
	
	public void setAvailable(boolean b) {
		isAvailable = b;
	}
	
/*
	public ApplianceType getApplianceType() {
		return type;
	}
	public void setApplianceType(ApplianceType type) {
		this.type = type;
	}
*/
	
	public DeviceType getApplianceType() {
		return deviceInfo != null? deviceInfo.getDescriptor().getDeviceType() : DeviceType.Other;
	}
	/*
	public short getTotalProfileNum() {
		return totalProfileNum;
	}
	public void setTotalProfileNum(short totalProfileNum) {
		this.totalProfileNum = totalProfileNum;
		ensureCapacityProfiles(totalProfileNum);
	}

	public boolean isMultipleSchedulingSupported() {
		return multipleSchedulingSupported;
	}
	public void setMultipleSchedulingSupported(boolean multipleSchedulingSupported) {
		this.multipleSchedulingSupported = multipleSchedulingSupported;
	}

	public float getEnergyFormatting() {
		return energyFormatting;
	}

	public void setEnergyFormatting(float energyFormatting) {
		this.energyFormatting = energyFormatting;
	}
	*/
	public float getSummationFormatting() {
		return summationFormatting;
	}

	public void setSummationFormatting(float summationFormatting) {
		this.summationFormatting = summationFormatting;
	}
	
	public float getDemandFormatting() {
		return demandFormatting;
	}

	public void setDemandFormatting(float demandFormatting) {
		this.demandFormatting = demandFormatting;
	}

	/*
	 * **************************************************************************************************
	 * Power Profile functions
	 */
	/*
	private void ensureCapacityProfiles(short totalNum) {
		if (powerProfileInfos.length < totalNum) {
			PowerProfileInfo[] ppi = new PowerProfileInfo[totalNum +1];
			System.arraycopy(powerProfileInfos, 0, ppi, 0, powerProfileInfos.length);
			powerProfileInfos = ppi;
		}
	}
	
	public PowerProfileInfo getPowerProfile(short profileId) {
		if (profileId >= powerProfileInfos.length) return null;
		return powerProfileInfos[profileId];
	}
	
	public void notifyPowerProfile(PowerProfileInfo ppi) {
		setTotalProfileNum((short)Math.max(ppi.getTotalProfileNum(), ppi.getProfileId()));
		
		PowerProfileInfo current = getPowerProfile(ppi.getProfileId());
		if (current != null) {
			ppi.setProfileCurrentState(current.getProfileCurrentState());
			ppi.setTimeConstraints(current.getTimeConstraints());
			//ppi.setProposedScheduledEnergyPhases(current.getProposedScheduledEnergyPhases());
			//ppi.setActualScheduledEnergyPhases(current.getActualScheduledEnergyPhases());
		}
		powerProfileInfos[ppi.getProfileId()] = ppi;
		//ppi.initPhases(getEnergyDecimalConverter());
	}
	*/

	/*
	 * **************************************************************************************************
	 * Metering and Cost functions
	 */
	
	public EnergyCostInfo getAccumulatedEnergyCost() {
		return accumulatedEnergyCost;
	}

	public void setAccumulatedEnergyCost(EnergyCostInfo eci) {
		accumulatedEnergyCost = eci;
		lastValidEnergyTime = eci.endTime;
		lastTotalEnergy = eci.startEnergy + eci.deltaEnergy;
	}
	
	public double getAccumulatedEnergy() {
		return lastTotalEnergy;
	}
	
	public long getAccumulatedEnergyTime() {
		return lastNotificationTime;
	}
	

	public EnergyCostInfo updateEnergyCost(long newTime, double newEnergy) {
		EnergyCostInfo eci = null;
		if (newTime < lastNotificationTime) {
			//FIXME by Riccardo: do we have any special reason for all these String.format calls inside debug messages in this class ?? if not, we should use normal logging messages to avoid function calls - leaving as it is right now
			//LOG.error(String.format("updateEnergyCost %s: received new time value %d before last valid time value %d, returning null", applianceId, newTime, lastNotificationTime));
			LOG.warn("updateEnergyCost "+applianceId+": received new time value "+newTime+" before last valid time value "+lastNotificationTime+", returning null");
			return eci;
		}
		
		lastNotificationTime = newTime;
		
		if (newEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			LOG.debug(String.format("updateEnergyCost %s: received INVALID_ENERGY_VALUE, returning null", applianceId));
			return eci;
		}
		
		try {
			long elapsedTime = newTime - lastValidEnergyTime;
			if (elapsedTime > 0 && elapsedTime < MAX_VALID_EPOC_THRESHOLD) {
				if (elapsedTime < MILLISECS_IN_ONE_MINUTE) {
					LOG.debug(String.format("updateEnergyCost %s: elapsed time < 1 minute, returning null", applianceId));
					return eci;
				}			
				
				double energyDelta = newEnergy - lastTotalEnergy;
				if (energyDelta > 0) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(lastValidEnergyTime);
					DailyTariff dt = DailyTariff.getInstance();
					eci = dt.computeMinMaxCosts(calendar, elapsedTime, energyDelta);
					LOG.debug(String.format("updateEnergyCost %s: energy cost computed", applianceId));
				} else {
					if (energyDelta < 0) {
						LOG.warn(String.format("updateEnergyCost %s: invalid energy delta: %s", applianceId, energyDelta));
						}
					energyDelta = 0; // safety net in case it's negative
					eci = new EnergyCostInfo();
				}
				
				eci.setStartEndTime(lastValidEnergyTime, newTime);
				accumulatedEnergyCost.addValues(eci);
			
			} else {
				LOG.warn(String.format("updateEnergyCost %s: received new time value %d , elapsed time %d > MAX_VALID_EPOC_THRESHOLD, resetting start time", applianceId, newTime, elapsedTime));
				accumulatedEnergyCost.reset(newTime);
			}
			
		} catch (Exception e) {
			LOG.error(String.format("updateEnergyCost for appliance %s exception", applianceId), e);
		}

		lastTotalEnergy = newEnergy;
		lastValidEnergyTime = newTime;
		return eci;
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
		if (ic == IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE) ic = 0;	
		powerInfo.setCurrentPower(ic, time);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("{");
		sb.append(applianceId).append(", ");
		sb.append(isAvailable).append(", ");
		sb.append(deviceInfo.getDescriptor().getDeviceType()).append(", ");
		sb.append(deviceInfo.getConfiguration().getCategory()).append("}");
		return sb.toString();
	}
}
