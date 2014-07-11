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

import java.util.HashMap;
import java.util.Map;

import org.energy_home.jemma.ah.cluster.zigbee.eh.GetOverallSchedulePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceExtendedResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;


/*
 * This class is the hub of all information, data, state of a remote appliance. It contains a working status 
 * of a remote appliance and is the object passed to the various logic and algorithms to perform calculations
 * and decide actions.
 */
@Deprecated //FIXME by Riccardo seems like an old version: can we remove it ?
public class ApplianceInfoOld {

	private class PowerProfileInfo {
		short profileId;
		PowerProfile profileCurrentState;
		PowerProfileTransferredPhase[] profilePhases;
		ScheduledPhase[] currentSchedule, proposedSchedule;
		int startAfter, endBefore;
		// should be converted into minutes.
		long contraintTimeNotification, currentStateTimeNotification;
		long currentScheduleTimeNotification, proposedScheduleTimeNotification;
		
		PowerProfileInfo(short id) {
			profileId = id;
		}
	}
	
	
	// static fields
	private static Map<String, ApplianceInfoOld> appliances = new HashMap<String, ApplianceInfoOld>();
	

	public static ApplianceInfoOld getAppliance(String id) {
		return appliances.get(id);
	}
	
	public static ApplianceInfoOld getOrCreateAppliance(String id) {
		ApplianceInfoOld ap = appliances.get(id);
		if (ap == null) {
			ap = new ApplianceInfoOld(id);
			appliances.put(id,  ap);
		}
		return ap;
	}
	
	public static final int ISO4217_CURRENCY_CODE = 978; // euro currency
	public static final short TRAILING_DIGIT_CENTS = 2; // trailing digits: cents of euros
	public static final short TRAILING_DIGIT_TENTHS = 3; // trailing digits: thousandths of euros
	public static final int MILLISECONDS_IN_MINUTE = 60 * 1000;
	public static long VALID_TIME_NOTIFICATION_INTERVAL = 12 * 60 * 60 * 1000; // 12 hours
	
	public static short priceDecimals = TRAILING_DIGIT_TENTHS;
	
	
	private PowerProfileInfo[] powerProfileInfos;

	private void allocateProfileInfo(short totalNum) {
		if (powerProfileInfos == null) powerProfileInfos = new PowerProfileInfo[totalNum +1];
		if (powerProfileInfos.length < totalNum) {
			PowerProfileInfo[] ppi = new PowerProfileInfo[totalNum +1];
			System.arraycopy(powerProfileInfos, 0, ppi, 0, powerProfileInfos.length);
			powerProfileInfos = ppi;
		}
	}
	private PowerProfileInfo getPowerProfileInfo(short profileId) {
		if (powerProfileInfos == null || profileId >= powerProfileInfos.length) allocateProfileInfo(profileId);
		PowerProfileInfo ppi = powerProfileInfos[profileId];
		if (ppi == null) {
			ppi = new PowerProfileInfo(profileId);
			powerProfileInfos[profileId] = ppi;
		}
		return ppi;
	}
	
	private String applianceId;
	private boolean isSmartInfo;
	
	/*
	 *  The TotalProfileNum attribute represents the total number of profiles supported by the device.
	 */
	private short totalProfileNum;
	/*
	 * The MultipleScheduling attribute specifies if the server side of the Power Profile cluster supports
	 * the scheduling of multiple Energy Phases or it does support the scheduling of a single energy
	 * phase of the Power profile at a time. 
	 */
	private boolean multipleSchedulingSupported;
	/*
	 * The EnergyFormatting attribute SummationFormatting provides a method to properly decipher
	 * the number of digits and the decimal location of the values found in the Summation Information
	 * Set of attributes. This attribute is to be decoded as follows:
	 * Bits 0 to 2: Number of Digits to the right of the Decimal Point.
	 * Bits 3 to 6: Number of Digits to the left of the Decimal Point.
	 * Bit 7: If set, suppress leading zeros.
	 */
	private short energyFormatting;
	/*
	 * The EnergyRemote attribute describes the remote control status of the power profile server
	 * cluster (e.g. appliance), which means the selection operated by the user on the remote control
	 * feature of theDevice.
	 */ 
	//private boolean energyRemote;
	/*
	 * The ScheduleMode attribute describes the criteria that should be used by the Power Profile
	 * cluster client side (e.g. energy management system) to schedule the power profiles.
	 */
	//private short scheduleMode;
	
	
	/*
	 * The PowerProfileState allows a device server to communicate its current
	 * Power Profile(s) to the client.
	 * The Power Profile record support the following fields:
	 * -  Power Profile ID: the identifier of the Power Profile as requested;
	 * -  Energy Phase ID: The current Energy Phase ID of the specific Profile ID;
	 * -  PowerProfileRemoteControl: it indicates if the PowerProfile is currently remotely
	 *    controllable or not;  if the Power Profile is not remotely controllable is cannot be
	 *    scheduled by a Power Profile client
	 * -  PowerProfileState: an enumeration field representing the current state of the Power
	 *    Profile
	 * 
	 * Enumeration  Value  Description
	 * POWER_PROFILE_IDLE  0x00  The PP is not defined in its parameters.
	 * POWER_PROFILE_PROGRAMMED  0x01  The PP is defined in its parameters but 
	 * without a scheduled time reference
	 * ENERGY_PHASE_RUNNING  0x03  An energy phase is running 
	 * ENERGY_PHASE_PAUSED  0x04  The current energy phase is paused
	 * ENERGY_PHASE_WAITING_TO_START  0x05  The Power Profile is in between two energy 
	 * phases (one ended, the other not yet started). If the first EnergyPhase is considered, this 
	 * state indicates that the whole power profile is not yet started, but it has been already 
	 * programmed to start
	 * ENERGY_PHASE_WAITING_PAUSED  0x06  The Power Profile is set to Pause when being 
	 * in the ENERGY_PHASE_WAITING_TO_START state.
	 * POWER_PROFILE_ENDED  0x07  The whole Power profile is terminated
	 */
	public static final short POWER_PROFILE_IDLE = 0;
	public static final short POWER_PROFILE_PROGRAMMED = 2;
	public static final short ENERGY_PHASE_RUNNING = 3;
	public static final short ENERGY_PHASE_PAUSED = 4;
	public static final short ENERGY_PHASE_WAITING_TO_START = 5;
	public static final short ENERGY_PHASE_WAITING_PAUSED = 6;
	public static final short POWER_PROFILE_ENDED = 7;

		
	private MinMaxPowerInfo powerInfo = new MinMaxPowerInfo();
	
	
	private ApplianceInfoOld(String id) {
		applianceId = id;
		// retrieve and set all/some attributes of the remote appliance (shown below).
	}
	
	public boolean isSmartInfo() {
		return isSmartInfo;
	}
	
	public void setSmartInfo(boolean b) {
		isSmartInfo = b;
	}
	
	public String getApplianceId() {
		return applianceId;
	}
	
	public short getTotalProfileNum() {
		return totalProfileNum;
	}

	public void setTotalProfileNum(short totalProfileNum) {
		this.totalProfileNum = totalProfileNum;
		allocateProfileInfo(totalProfileNum);
	}

	public boolean isMultipleSchedulingSupported() {
		return multipleSchedulingSupported;
	}

	public void setMultipleSchedulingSupported(boolean multipleSchedulingSupported) {
		this.multipleSchedulingSupported = multipleSchedulingSupported;
	}

	public short getEnergyFormatting() {
		return energyFormatting;
	}

	public void setEnergyFormatting(short energyFormatting) {
		this.energyFormatting = energyFormatting;
	}
	

	public PowerProfileTransferredPhase[] getPowerProfilePhases(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		return ppi.profilePhases;
	}
	
	public void setPowerProfilePhases(short totalNum, short profileId, PowerProfileTransferredPhase[] profilePhases) {
		allocateProfileInfo(totalNum);
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		ppi.profilePhases = profilePhases;
	}

	
	public PowerProfile getPowerProfileState(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		long now = System.currentTimeMillis();
		if (now - ppi.currentScheduleTimeNotification > VALID_TIME_NOTIFICATION_INTERVAL) return null; 
		return ppi.profileCurrentState;
	}
	
	public void setPowerProfilesState(PowerProfile[] profiles) {
		allocateProfileInfo((short)profiles.length);
		for (int i = profiles.length; --i >= 0;) {
			short profileId = profiles[i].PowerProfileID;
			PowerProfileInfo ppi = getPowerProfileInfo(profileId);
			ppi.profileCurrentState = profiles[i];
			ppi.currentStateTimeNotification = System.currentTimeMillis();
		}
	}

	public ScheduledPhase[] getEffectiveScheduledPhases(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		long now = System.currentTimeMillis();
		if (now - ppi.currentScheduleTimeNotification > VALID_TIME_NOTIFICATION_INTERVAL) return null; 
		return ppi.currentSchedule;
	}
	
	public long getEffectiveScheduledTime(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		return ppi.currentScheduleTimeNotification;
	}
	
	
	// called by the device to notify of the current used schedule.
	public boolean setEffectiveScheduledPhases(short profileId, ScheduledPhase[] scheduledPhases) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		ppi.currentSchedule = scheduledPhases;
		ppi.currentScheduleTimeNotification = System.currentTimeMillis();

		/*
		// check if accepted
		if (ppi.proposedSchedule != null) {
			for (int i = ppi.currentSchedule.length; --i >= 0;) {
				long t1 = ppi.currentSchedule[i].ScheduledTime + ppi.currentScheduleTimeNotification / MILLISECONDS_IN_MINUTE;
				long t2 = ppi.proposedSchedule[i].ScheduledTime + ppi.proposedScheduleTimeNotification / MILLISECONDS_IN_MINUTE;
				// give one minute tolerance
				if (Math.abs(t1 - t2) > 1) return false;
			}
		}
		*/
		return true;
	}

	public ScheduledPhase[] getPoposedScheduledPhases(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		return ppi.proposedSchedule;
	}

	// called by the energy brain as a request to propose a schedule.
	public void setProposedScheduledPhases(short profileId, ScheduledPhase[] scheduledPhases) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		ppi.proposedSchedule = scheduledPhases;
		ppi.proposedScheduleTimeNotification = System.currentTimeMillis();
	}
	
	public long getStartConstraint(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		long now = System.currentTimeMillis();
		if (now - ppi.contraintTimeNotification > VALID_TIME_NOTIFICATION_INTERVAL) return 0; 
		return ppi.contraintTimeNotification + ppi.startAfter * MILLISECONDS_IN_MINUTE;
	}
	
	public long getEndConstraint(short profileId) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		long now = System.currentTimeMillis();
		if (now - ppi.contraintTimeNotification > VALID_TIME_NOTIFICATION_INTERVAL) return 0;
		return ppi.contraintTimeNotification + ppi.endBefore * MILLISECONDS_IN_MINUTE;
	}
	
	public void setProfileScheduleConstraints(short profileId, int start, int end) {
		PowerProfileInfo ppi = getPowerProfileInfo(profileId);
		ppi.startAfter = start;
		ppi.endBefore = end;
		ppi.contraintTimeNotification = System.currentTimeMillis();
	}

	// the energy returned is in W/h 
	public float getEnergyDecimalConverter() {
		int decimals = energyFormatting & 0x03;
		if (decimals > 0) return (float)(1 / Math.pow(10, decimals));
		return 1;
	}
	
	public long encodePriceFormatting(float cost) {
		return Math.round(cost * Math.pow(10, priceDecimals));
	}
	
	public GetPowerProfilePriceResponse encodePowerProfilePrice(short profileId, float cost) {
		GetPowerProfilePriceResponse priceResponse = new GetPowerProfilePriceResponse();
		priceResponse.PowerProfileID = profileId;
		priceResponse.Currency = ISO4217_CURRENCY_CODE;
		priceResponse.PriceTrailingDigit = priceDecimals;
		priceResponse.Price = encodePriceFormatting(cost);
		return priceResponse;
	}
	public GetPowerProfilePriceExtendedResponse encodePowerProfilePriceExtended(short profileId, float cost) {
		GetPowerProfilePriceExtendedResponse priceResponse = new GetPowerProfilePriceExtendedResponse();
		priceResponse.PowerProfileID = profileId;
		priceResponse.Currency = ISO4217_CURRENCY_CODE;
		priceResponse.PriceTrailingDigit = priceDecimals;
		priceResponse.Price = encodePriceFormatting(cost);
		return priceResponse;
	}
	public GetOverallSchedulePriceResponse encodeOverallSchedulePrice(float cost) {
		GetOverallSchedulePriceResponse priceResponse = new GetOverallSchedulePriceResponse();
		priceResponse.Currency = ISO4217_CURRENCY_CODE;
		priceResponse.PriceTrailingDigit = priceDecimals;
		priceResponse.Price = encodePriceFormatting(cost);
		return priceResponse;
	}
	
	public MinMaxPowerInfo getMinMaxPowerInfo() {
		if (!isSmartInfo) return powerInfo;
		
		// ENEL fix: we need to see if the current total power is greater than the sum of all smart appliances
		float summedPower = 0;
		for (ApplianceInfoOld a : appliances.values()) {
			if (!a.getApplianceId().equals(applianceId)) summedPower += a.powerInfo.getCurrentPower();
		}
		if (powerInfo.getCurrentPower() < summedPower) {
			powerInfo.setCurrentPower(summedPower, System.currentTimeMillis());
		}
		return powerInfo;
	}
	
	public static float INVALID_POWER_VALUE = 0xFFFFFF;
	void setIstantaneousPower(float ic, long time) {
		if (ic == INVALID_POWER_VALUE) ic = 0;	
		powerInfo.setCurrentPower(ic, time);
	}

}
