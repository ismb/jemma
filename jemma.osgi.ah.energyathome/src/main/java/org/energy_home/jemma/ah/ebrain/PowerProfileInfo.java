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

import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo.EnergyPhaseScheduleTime;

/*
 * Due to clash in names, a PP is given this name. Unfortunately in the cluster interfaces the name
 * PowerProfile is used to represent PP a PP state
 */

public class PowerProfileInfo {
	public static final int MILLISECS_IN_ONE_MINUTE = 60 * 1000;
	public static final int TIME_TOLERANCE_EQUALITY = 5 * 60 * 1000; // 5 minutes
	public static final int INDEFINITE_DURATION = 0xffff;

	public static class PowerProfileState {
		/*
		 * The PowerProfileState allows a device server to communicate its current
		 * Power Profile(s) to the client.
		 * The Power Profile record support the following fields:
		 * - Power Profile ID: the identifier of the Power Profile as requested;
		 * - Energy Phase ID: The current Energy Phase ID of the specific Profile ID; this value shall be
		 *    set to invalid 0xFF when PowerProfileState indicates a Power Profile in POWER_PROFILE_IDLE state
		 * - PowerProfileRemoteControl: it indicates if the PowerProfile is currently remotely
		 *   controllable or not;  if the Power Profile is not remotely controllable is cannot be
		 *    scheduled by a Power Profile client
		 * - PowerProfileState: an enumeration field representing the current state of the Power
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
		public static final short POWER_PROFILE_PROGRAMMED = 1;
		public static final short ENERGY_PHASE_RUNNING = 3;
		public static final short ENERGY_PHASE_PAUSED = 4;
		public static final short ENERGY_PHASE_WAITING_TO_START = 5;
		public static final short ENERGY_PHASE_WAITING_PAUSED = 6;
		public static final short POWER_PROFILE_ENDED = 7;
		
		public static final short INVALID_POWER_PROFILE_ID = 0xff;
		
		public static final String getNameOf(PowerProfileState pps) {
			switch (pps.state) {
			case POWER_PROFILE_IDLE: return "POWER_PROFILE_IDLE";
			case POWER_PROFILE_PROGRAMMED: return "POWER_PROFILE_PROGRAMMED";
			case ENERGY_PHASE_RUNNING: return "ENERGY_PHASE_RUNNING";
			case ENERGY_PHASE_PAUSED: return "ENERGY_PHASE_PAUSED";
			case ENERGY_PHASE_WAITING_TO_START: return "ENERGY_PHASE_WAITING_TO_START";
			case ENERGY_PHASE_WAITING_PAUSED: return "ENERGY_PHASE_WAITING_PAUSED";
			case POWER_PROFILE_ENDED: return "POWER_PROFILE_ENDED";
			default: return "UNKNOWN_POWER_PROFILE_STATE " + pps.state;
			}
		}
		
		private short powerProfileId = INVALID_POWER_PROFILE_ID;
		private short energyPhaseId;
		private boolean isRemoteControllable;
		private short state;
		private long stateTimeNotification;
		//private long phaseRunningTime;
		
		public PowerProfileState() {}
		public PowerProfileState(short id) {
			powerProfileId = id;
			isRemoteControllable = true;
			state = POWER_PROFILE_IDLE;
			stateTimeNotification = System.currentTimeMillis();
		}
		
		public short getPowerProfileID() {
			return powerProfileId;
		}
		public void setPowerProfileID(short powerProfileID) {
			this.powerProfileId = powerProfileID;
		}
		public short getEnergyPhaseID() {
			return energyPhaseId;
		}
		public void setEnergyPhaseID(short energyPhaseID) {
			this.energyPhaseId = energyPhaseID;
		}
		public boolean isPowerProfileRemoteControllable() {
			return isRemoteControllable;
		}
		public void setPowerProfileRemoteControllable(boolean isPowerProfileRemoteControllable) {
			this.isRemoteControllable = isPowerProfileRemoteControllable;
		}
		public short getState() {
			return state;
		}
		public void setState(short state) {
			this.state = state;
		}
		public long getStateTimeNotification() {
			return stateTimeNotification;
		}
		
        public boolean isApplianceStarted() {
            // all phases other than one means it's started
            if (energyPhaseId > 1) return true;
            if (state == ENERGY_PHASE_PAUSED || state == ENERGY_PHASE_RUNNING) return true;
            // in all other states of the 1st phase are not considered running
            return false;
      }
		
		public String toString() {
			StringBuilder sb = new StringBuilder("\nPower Profile State for: ").append(powerProfileId);
			sb.append("\nenergy phase ID = ").append(energyPhaseId);
			sb.append("\nremote controllable = " + isRemoteControllable);
			sb.append("\nprofile state = ").append(getNameOf(this));
			sb.append("\nnotification time = ").append(CalendarUtil.toSecondString(stateTimeNotification));
			sb.append('\n');
			return sb.toString();
		}
	}
	
	public static class PowerProfileTimeConstraints {
		public static final int MAX_SCHEDULING_INTERVAL = 24 * 60;
		public static final int MAX_SCHEDULING_DELAY = 24 * 60;
		
		private short powerProfileID;
		private int startAfter;
		private int stopBefore;
		private long contraintTimeNotification;
		private boolean isValid;
		
		public PowerProfileTimeConstraints(short id) {
			this(id, 0, MAX_SCHEDULING_INTERVAL);
		}
		public PowerProfileTimeConstraints(short id, int start, int end) {
			contraintTimeNotification = System.currentTimeMillis();
			powerProfileID = id;
			if (start < 0) start = 0;
			if (end == INDEFINITE_DURATION || end < 0) end = MAX_SCHEDULING_INTERVAL;
			startAfter = start;
			stopBefore = end;
			isValid = start < end
					&& start <= MAX_SCHEDULING_DELAY
					&& end - start <= MAX_SCHEDULING_INTERVAL;
		}
		
		public short getPowerProfileID() {
			return powerProfileID;
		}
		public long getStartAfterConstraint() {
			return contraintTimeNotification + startAfter * MILLISECS_IN_ONE_MINUTE;
		}
		public long getStopBeforeConstraint() {
			return contraintTimeNotification + stopBefore * MILLISECS_IN_ONE_MINUTE;
		}
		
		public int getStartAfterDelay() {
			return startAfter;
		}
		
		public int getStopBeforeDelay() {
			return stopBefore;
		}
		
		public boolean isValid() {
			return isValid;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder("\nPower Profile Constraints for: ").append(powerProfileID);
			sb.append("\nnotification time= ").append(CalendarUtil.toSecondString(contraintTimeNotification));
			sb.append("\nstart after= ").append(CalendarUtil.toSecondString(getStartAfterConstraint()));
			sb.append("\nstop before= ").append(CalendarUtil.toSecondString(getStopBeforeConstraint()));
			sb.append('\n');
			return sb.toString();
		}
	}
	
	private static final PowerProfileState EMPTY_STATE = new PowerProfileState();
	private static final EnergyPhaseInfo[] EMPTY_PHASES = new EnergyPhaseInfo[0];
	private static final EnergyPhaseScheduleTime[] EMPTY_SCHEDULE = new EnergyPhaseScheduleTime[0];

	private short profileId;
	private short totalProfileNum;
	private EnergyPhaseInfo[] energyPhases = EMPTY_PHASES;
	
	private EnergyPhaseScheduleTime[] applianceScheduledPhases = EMPTY_SCHEDULE;
	private long applianceScheduleTimeNotification;
	
	private PowerProfileState profileCurrentState = EMPTY_STATE;
	private PowerProfileTimeConstraints profileTimeConstraints;
	
	
	public PowerProfileInfo(short powerProfileID) {
		profileId = powerProfileID;
		profileTimeConstraints = new PowerProfileTimeConstraints(powerProfileID);
	}
	
	public EnergyPhaseScheduleTime[] getProposedScheduledEnergyPhases() {
		int currentTimeSlot = CalendarUtil.getSlotOf(Calendar.getInstance());
		int delay = energyPhases[0].getScheduledSlot() - currentTimeSlot;

		if (delay < -TIME_TOLERANCE_EQUALITY) 
			throw new IllegalStateException("Obsolete Schedule: Start Time << Current Time.");
		if (delay < 0) delay = 0;
				
		EnergyPhaseScheduleTime[] proposedSchedule = new EnergyPhaseScheduleTime[energyPhases.length];
		
		proposedSchedule[0] = new EnergyPhaseScheduleTime(energyPhases[0].getEnergyPhaseID(), delay);

		for (int i = 1; i < energyPhases.length; ++i) {
			proposedSchedule[i] = new EnergyPhaseScheduleTime(energyPhases[i].getEnergyPhaseID(), energyPhases[i].getScheduledSlot());
		}

		return proposedSchedule;
	}

	public EnergyPhaseScheduleTime[] getApplianceScheduledEnergyPhases() {
		return applianceScheduledPhases;
	}
	
	public long getApplianceScheduleTime() {
		if (applianceScheduledPhases.length == 0) return 0;
		return applianceScheduleTimeNotification +
				applianceScheduledPhases[0].getScheduledDelay() * MILLISECS_IN_ONE_MINUTE;
	}
	
	public void setApplianceScheduledEnergyPhases(EnergyPhaseScheduleTime[] epst) {
		applianceScheduledPhases = epst;
		applianceScheduleTimeNotification = System.currentTimeMillis();
	}
	
	public short getProfileId() {
		return profileId;
	}
	public short getTotalProfileNum() {
		return totalProfileNum;
	}
	public void setTotalProfileNum(short totalProfileNum) {
		this.totalProfileNum = totalProfileNum;
	}
	public EnergyPhaseInfo[] getEnergyPhases() {
		return energyPhases;
	}
	public void setEnergyPhases(EnergyPhaseInfo[] energyPhases) {
		this.energyPhases = energyPhases;
	}
	public void setTimeConstraints(PowerProfileTimeConstraints pptc) {
		profileTimeConstraints = pptc;
	}
	public PowerProfileTimeConstraints getTimeConstraints() {
		return profileTimeConstraints;
	}
	public PowerProfileState getProfileCurrentState() {
		return profileCurrentState;
	}
	public void setProfileCurrentState(PowerProfileState pcs) {
		// some behavior here to update running-time of same phase
		profileCurrentState = pcs;
	}
	
	public void initPhases(float energyConverter) {
		for (int i = energyPhases.length; --i >= 0;) {
			energyPhases[i].init(energyConverter);
		}
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("\nPower Profile ID ").append(profileId);
		sb.append(profileCurrentState);
		sb.append(profileTimeConstraints);
		sb.append("\nProfile scheduled time = ").append(CalendarUtil.toMinuteString(energyPhases[0].getScheduledSlot()));
		sb.append('\n');
		for (int i = 0; i < energyPhases.length; ++i) {
			sb.append(energyPhases[i]);
		}
		sb.append('\n');
		return sb.toString();
	}
}
