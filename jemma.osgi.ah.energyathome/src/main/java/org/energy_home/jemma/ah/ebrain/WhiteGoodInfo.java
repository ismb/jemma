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


import org.energy_home.jemma.shal.DeviceInfo;

public class WhiteGoodInfo extends ApplianceInfo {
	
	private static final PowerProfileInfo[] EMPTY_PROFILES = new PowerProfileInfo[0];

	public WhiteGoodInfo(DeviceInfo info) {
		super(info);
	}
	
	
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
	private float energyFormatting = IPowerProfileProxy.INVALID_FORMATTING_VALUE;
	/*
	 * The EnergyRemote attribute describes the remote control status of the power profile server
	 * cluster (e.g. appliance), which means the selection operated by the user on the remote control
	 * feature of theDevice.
	 */ 
	
	
	private PowerProfileInfo[] powerProfileInfos = EMPTY_PROFILES;

	
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

	/*
	 * **************************************************************************************************
	 * Power Profile functions
	 */
	
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
}
