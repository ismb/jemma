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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.ebrain.CalendarUtil;
import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.ah.ebrain.algo.OverloadDetectorListener;
import org.energy_home.jemma.ah.ebrain.algo.OverloadDetectorTask;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//FIXME by Riccardo seems like an "old" outdated implementation: can we remove it
@Deprecated
public class EnergyBrainOld {
	public static final int ISO4217_CURRENCY_CODE = 978; // euro currency
	public static final short TRAILING_DIGIT_CENTS = 2; // trailing digits: cents of euros
	public static final short TRAILING_DIGIT_TENTHS = 3; // trailing digits: thousandths of euros
	
	private static long encodeCostWithFormatting(float value, Short formatting) {
		int decimals = formatting & 0x07 -3;
		if (decimals < 0) throw new IllegalArgumentException("Incosisten formatting.");
		
		//v = (long)((value * Math.pow(10, decimals)) / 1000);
		return Math.round(value * Math.pow(10, decimals));
	}

	private static final Logger LOG = LoggerFactory.getLogger( EnergyBrainOld.class );

	private static EnergyBrainOld instance;
	static {instance = new EnergyBrainOld();}
	public static EnergyBrainOld getInstance() {return instance;}

	private String SmartInfoId = "";
	private float upperPowerThreshold = 3300; // watts per hour

	private DailyTariff dailyTariff;
	private short tariffTrailingDigits;
	
	private Map<String, SmartAppliance> appliances = new HashMap<String, SmartAppliance>();
	//private Map<String, AccumulatedEnergyCostPowerInfo> costs = new HashMap<String, AccumulatedEnergyCostPowerInfo>();
		
	private OverloadDetectorTask detector;
		
	private EnergyBrainOld() {
		try {
			dailyTariff = DailyTariff.getInstance();//TwoTierDailyTariff.class);
			setTariffTrailingDigits(TRAILING_DIGIT_TENTHS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getTariffTrailingDigits() {
		return tariffTrailingDigits;
	}
	
	public void setTariffTrailingDigits(short trailing) {
		tariffTrailingDigits = trailing;
	}
	
	public float getUpperPowerThreshold() {
		return upperPowerThreshold;
	}

	public void setUpperPowerThreshold(float powerThreshold) {
		this.upperPowerThreshold = powerThreshold;
	}
	
	public Class<? extends DailyTariff> getDailyTariff() {
		return dailyTariff.getClass();
	}

	public void setDailyTariff(Class<? extends DailyTariff> clazz) throws InstantiationException, IllegalAccessException {
		dailyTariff = DailyTariff.getInstance(clazz);
	}
	/*
	public synchronized Map<String, AccumulatedEnergyCostPowerInfo> getAccumulatedCostInfos() {
		return costs;
	}

	public synchronized void setAccumulatedCostInfos(Map<String, AccumulatedEnergyCostPowerInfo> map) {
		costs = map;
		for (Entry<String, AccumulatedEnergyCostPowerInfo> entry : map.entrySet()) {
			SmartAppliance appliance = getAppliance(entry.getKey());
			appliance.setAccumulatedEnergyCost(entry.getValue());
		}
	}
	
	/*
	//TODO generate appliance from restored state
	public void setApplianceDescriptor(ApplianceDescriptor ad, String applianceId) {
		try {
			SmartAppliance appliance = getAppliance(applianceId, true);
		} catch (ApplianceException e) {}
	}
	*/
	public void notifyApplianceState(short state, String applianceId) {
		getAppliance(applianceId).setState(state);
	}

	public void notifyPowerProfile(short totalProfileNum, short powerProfileID, PowerProfileTransferredPhase[] phases, String applianceId) {
		PowerProfileResponse profile = new PowerProfileResponse(totalProfileNum, powerProfileID, phases);
		getAppliance(applianceId).setPowerProfile(profile);
	}
	
	public PowerProfilePrice getPowerProfilePrice(short powerProfileID, int delay, String applianceId) throws IllegalArgumentException {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		PowerProfileTransferredPhase[] phases = appliance.getProfileTransferredPhases();
		Calendar scheduledTime = Calendar.getInstance();
		if (delay > 0) scheduledTime.add(Calendar.MINUTE, delay);
		
		LOG.debug("start appliance time = " + scheduledTime.get(Calendar.HOUR_OF_DAY) + ':' + scheduledTime.get(Calendar.MINUTE));
		
		int maxPeakPower = 0;
		float cost = 0;
		for (int i = 0; i < phases.length; ++i) {
			if (phases[i].PeakPower > maxPeakPower) maxPeakPower = phases[i].PeakPower;
			// TODO check correct position of the decimal in energy to cast from long to double
			long millisecs = phases[i].ExpectedDuration * 60 * 1000;
			EnergyCostInfo eci = dailyTariff.computeMinMaxCosts(scheduledTime, millisecs, (double)phases[i].Energy);
			//LOG.debug(eci);
			cost += eci.getCost();
			scheduledTime.add(Calendar.MINUTE, phases[i].ExpectedDuration);
		}
		
		LOG.debug("cost " + cost);

		long price = Math.round(cost * Math.pow(10, tariffTrailingDigits));
		
		// this might be a negative number, in that case meaning the still available energy
		float totalOverload = maxPeakPower + getTotalInstantaneosPowerInfo(SmartInfoId).getCurrentPower() - upperPowerThreshold;
		if (detector != null) totalOverload += detector.computeTotalPowerUsage();

		return new PowerProfilePrice(powerProfileID, ISO4217_CURRENCY_CODE, price, tariffTrailingDigits, (int)totalOverload);
	}
	
	public EnergyCostInfo computePowerProfileCost(ApplianceInfoOld app, short profileId, long start) throws IllegalArgumentException {
		SmartAppliance appliance = getAppliance(app.getApplianceId());
		PowerProfileTransferredPhase[] phases = app.getPowerProfilePhases(profileId);
		PowerProfilePhaseExtended[] extendedPhases = appliance.setPowerProfilePhases(phases, app.getEnergyDecimalConverter());
		
		Calendar scheduledTime = Calendar.getInstance();
		scheduledTime.setTimeInMillis(start);
		int startSlot = CalendarUtil.getSlotOf(scheduledTime);
		
		float[] energyAllocation = new float[CalendarUtil.SLOTS_IN_ONE_DAY * 2];

		float maxPeakPower = appliance.allocatePowerProfile(energyAllocation, startSlot);
		float cost = dailyTariff.computeCost(scheduledTime, energyAllocation);
		EnergyCostInfo eci = new EnergyCostInfo(cost, cost, cost, maxPeakPower);
		return eci;
	}
	
	// propose a schedule for the given appliance
	public ScheduledPhase[] proposeProfileSchedule(ApplianceInfoOld app, short profileId) {
		SmartAppliance appliance = getAppliance(app.getApplianceId());
		PowerProfileTransferredPhase[] phases = app.getPowerProfilePhases(profileId);
		PowerProfile profileState = app.getPowerProfileState(profileId);
		
		int phaseOffset = 0;
		
		if (profileState != null) {
			if (!profileState.PowerProfileRemoteControl) return null;
			switch (profileState.PowerProfileState) {
				case ApplianceInfoOld.POWER_PROFILE_IDLE:
				case ApplianceInfoOld.POWER_PROFILE_ENDED:
				case ApplianceInfoOld.POWER_PROFILE_PROGRAMMED:
					// not running, so the whole PP can be scheduled
					phaseOffset = 0;
					break;
				case ApplianceInfoOld.ENERGY_PHASE_PAUSED:
				case ApplianceInfoOld.ENERGY_PHASE_RUNNING:
					// this very phase cannot be rescheduled. We need to know when this phase
					// completes and use that as the earliest start for the next phase and also use the
					// max activation delay of the next phase as the latest start.
					phaseOffset = profileState.EnergyPhaseID;
					break;
				case ApplianceInfoOld.ENERGY_PHASE_WAITING_PAUSED:
				case ApplianceInfoOld.ENERGY_PHASE_WAITING_TO_START:
					// this very phase can be rescheduled. We need to know when its max delay will
					// expires and use that as the latest start for it.
					phaseOffset = profileState.EnergyPhaseID -1;
					break;
				default: return null;
			}
		}
		PowerProfilePhaseExtended[] extendedPhases = appliance.setPowerProfilePhases(phases, app.getEnergyDecimalConverter(), phaseOffset);
		
		/*
		ParticleSwarmScheduler swarm =  new ParticleSwarmScheduler(appliance, new EnergyAllocator(c, dt), swarmSize);
		int iterations = swarm.run(timeLimit);
		swarm.setBestSchedule();
		ScheduledPhase[] proposal = appliance.generateSchedule();
		appInfo.setProposedScheduledPhases(proposal);*/
		return null;
	}
	
	// propose a reschedule of the current scheduled devices to see if there's improvement.
	// Perhaps use a different energy allocator.
	public Map<String, ScheduledPhase[]> proposeOverallSchedule() {
		//????? which scheduled appliances should be considered, what criteria make sense?
		// defined a structure to return the overall reschedule
		return null;
	}
	
	// called to notify of the scheduled appliance. Must adjust energy allocator with the info.
	// and the smart appliance
	public void notifyPowerProfileSchedule(String applianceId, ScheduledPhase[] phases, long referenceTime) {
		SmartAppliance appliance = getAppliance(applianceId);
		//appliance.setScheduledProfile(appInfo.getEffectiveScheduledPhases());
		//appliance.allocateScheduledEnerrgy(energyAllocator.getScheduledEnergy());
	}
	

	// this should be called by the SmartInfo device
	public void notifyTotalInstantaneosPowerUsage(String applianceId, long time, float instantaneousConsumption) {
		SmartInfoId = applianceId;
		notifyApplianceInstantaneosPowerUsage(applianceId, time, instantaneousConsumption);
	}

	public void notifyApplianceInstantaneosPowerUsage(String applianceId, long time, float instantaneousConsumption) {
		SmartAppliance appliance = getAppliance(applianceId);
		appliance.setIstantaneousPower(instantaneousConsumption, time);
	}
	
	
	public MinMaxPowerInfo getTotalInstantaneosPowerInfo(String applianceId) {
		// ENEL fix: we need to see if the current total power is greater than the sum of all smart appliances
		float summedPower = 0;
		MinMaxPowerInfo smartInfoPower = null;
		for (SmartAppliance sa : appliances.values()) {
			if (sa.getApplianceId().equals(applianceId)) smartInfoPower = sa.getMinMaxPowerInfo();
			else summedPower += sa.getIstantaneousPower();
		}
		// this case the SmartInfo has not been found yet
		if (smartInfoPower == null) smartInfoPower = new MinMaxPowerInfo();

		if (smartInfoPower.getCurrentPower() < summedPower) {
    		smartInfoPower.setCurrentPower(summedPower, System.currentTimeMillis());
		}
		return smartInfoPower;
	}


	/*
	public float getApplianceInstantaneosPowerUsage(String applianceId) {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.getIstantaneousPower();
	}
	
	public long getApplianceInstantaneosPowerTime(String applianceId) {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.getIstantaneousPowerTime();
	}
	*/
	public MinMaxPowerInfo getApplianceInstantaneosPowerInfo(String applianceId) {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.getMinMaxPowerInfo();
	}
	
	// this should be called by the SmartInfo device
	public EnergyCostInfo notifyTotalAccumulatedEnergy(String applianceId, long time, double totalEnergy) {
		return notifyApplianceAccumulatedEnergy(applianceId, time, totalEnergy);
	}
	
	public EnergyCostInfo notifyApplianceAccumulatedEnergy(String applianceId, long time, double totalEnergy) {
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.updateEnergyCost(time, totalEnergy);
	}
	
	public EnergyCostInfo getAccumulatedEnergyCost(String applianceId) {
		return getAppliance(applianceId).getAccumulatedEnergyCost();
	}
	
	
	public double getApplianceAccumulatedEnergy(String applianceId) {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.getAccumulatedEnergy();
	}
	
	public long getApplianceAccumulatedEnergyTime(String applianceId) {
		if (!appliances.containsKey(applianceId)) throw new IllegalArgumentException(applianceId + " Appliance Not Found.");
		
		SmartAppliance appliance = getAppliance(applianceId);
		return appliance.getAccumulatedEnergyTime();
	}
	

	public Set<String> getApplianceIds() {
		return appliances.keySet();
	}
		
	public SmartAppliance getAppliance(String applianceId) {
		/*if (detector == null) setEnergyBrainListener(new EnergyBrainListener() {
			
			
			public void notifyUnderload(SmartAppliance a) {
				// TODO Auto-generated method stub
				
			}
			
			
			public void notifySafeLoad() {
				// TODO Auto-generated method stub
				
			}
			
			
			public void notifyOverload(SmartAppliance a) {
				// TODO Auto-generated method stub
				
			}
		});*/

		SmartAppliance appliance = appliances.get(applianceId);
		if (appliance == null) {
			appliance = new SmartAppliance(applianceId);
			appliances.put(applianceId, appliance);
			if (detector != null) detector.addAppliance(appliance);
		}
		return appliance;
	}

	public void setEnergyBrainListener(OverloadDetectorListener l) {
		if (detector != null) detector.close();
		detector = new OverloadDetectorTask(l);
	}
}
