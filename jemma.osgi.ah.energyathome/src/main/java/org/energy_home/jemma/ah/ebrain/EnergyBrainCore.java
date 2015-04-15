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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo.EnergyPhaseScheduleTime;
import org.energy_home.jemma.ah.ebrain.IOverloadStatusListener.OverloadStatus;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileState;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileTimeConstraints;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.ah.ebrain.algo.EnergyAllocator;
import org.energy_home.jemma.ah.ebrain.algo.ParticleSwarmScheduler;
import org.energy_home.jemma.ah.ebrain.algo.SolarIrradianceProfile;
import org.energy_home.jemma.ah.ebrain.algo.SolarIrradianceProfile.SkyCover;
import org.energy_home.jemma.ah.ebrain.algo.SwarmStatistics;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;
import org.energy_home.jemma.shal.DeviceInfo;

//public class EnergyBrainCore extends MeteringCore implements IPowerAndControlListener {
public class EnergyBrainCore extends MeteringCore implements IPowerAndControlListener {
	private static final Logger LOG = LoggerFactory.getLogger( EnergyBrainCore.class );
	
	public static final long SCHEDULER_RUNNING_TIME = 3000;
	public static final int SCHEDULER_SWARM_SIZE = 10;
	public static final int TIME_TOLERANCE_EQUALITY = 3 * 60 * 1000; // 3 minutes

	private static EnergyBrainCore instance;
	
	private static SolarIrradianceProfile sky;
	
	public static EnergyBrainCore getInstance() {
		if (instance == null) instance = new EnergyBrainCore();
		return instance;
	}
	
	private IPowerAndControlProxy powerControlProxy;
	private float[] energyAllocation = EnergyAllocator.newEnergyAllocation();
	
	protected EnergyBrainCore() {
		try {
			dailyTariff = DailyTariff.getInstance(TwoTierDailyTariff.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public IPowerAndControlProxy getPowerProfileProxy() {
		return powerControlProxy;
	}
	public void setPowerProfileProxy(IPowerAndControlProxy proxy) {
		if (proxy == null) throw new IllegalArgumentException("The argument cannot be null.");
		powerControlProxy = proxy;
	}

	
	public WhiteGoodInfo getWhiteGoodInfo(String applianceId) {
		return (WhiteGoodInfo)super.getApplianceInfo(applianceId);
	}
	
	public void notifyPowerProfile(String applianceId, PowerProfileInfo ppi) {
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		float formatting = appliance.getEnergyFormatting();
		if (formatting <= 0) {
			formatting = 1;
			//formatting = powerControlProxy.getEnergyFormatting(appliance.getApplianceId());
			appliance.setEnergyFormatting(formatting);
		}
		ppi.initPhases(formatting);
		appliance.notifyPowerProfile(ppi);
	}

	
	public void notifyAllPowerProfilesState(String applianceId, PowerProfileState[] powerProfilesState) {
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		for (int i = 0; i < powerProfilesState.length; ++i) {
			PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, powerProfilesState[i].getPowerProfileID());
			if (ppi != null) ppi.setProfileCurrentState(powerProfilesState[i]);
		}
	}

	
	public void notifyEnergyPhasesScheduleTime(String applianceId, short powerProfileID, EnergyPhaseScheduleTime[] scheduledPhases) {
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, powerProfileID);
		if (ppi != null) ppi.setApplianceScheduledEnergyPhases(scheduledPhases);
	}

	
	public void notifyPowerProfileScheduleConstraints(String applianceId, PowerProfileTimeConstraints profileConstraints) {
		if (!profileConstraints.isValid()) throw new IllegalArgumentException(
				String.format("Invalid Time Constraints, start-after=%d, stop-before=%d", profileConstraints.getStartAfterDelay(), profileConstraints.getStopBeforeDelay()));
		
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, profileConstraints.getPowerProfileID());
		if (ppi != null) ppi.setTimeConstraints(profileConstraints);
	}

	
	public float calculatePowerProfilePrice(String applianceId, short powerProfileID, int delay) {
		if (delay < 0 || delay > PowerProfileTimeConstraints.MAX_SCHEDULING_DELAY)
		{
			LOG.error("Invalid time delay!");
			throw new IllegalArgumentException("Invalid Time Delay " + delay);
		}
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, powerProfileID);
		
		if (ppi == null) throw new IllegalStateException(powerProfileID + " Profile ID Unknown.");
		
		// check that the appliance is running
		if (ppi.getProfileCurrentState().isApplianceStarted())
		{
			LOG.error("The appliance {} has already started, can't calculate price",applianceId);
			throw new IllegalStateException("Cannot Calculate Price of a Running Appliance - " + PowerProfileState.getNameOf(ppi.getProfileCurrentState()));
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, delay);
		long priceRequestTime = c.getTimeInMillis();
		
		// check to see if this calculation refers to the latest scheduling notification 
		long applianceScheduleTime = ppi.getApplianceScheduleTime();
		boolean isSchedulePrice = Math.abs(priceRequestTime - applianceScheduleTime) < TIME_TOLERANCE_EQUALITY;
		LOG.debug("is price referring to latest schedule: " + isSchedulePrice);
		LOG.debug("start appliance time = " + CalendarUtil.toMinuteString(c));
		
		EnergyPhaseInfo[] phases = ppi.getEnergyPhases();
		float maxPeakPower = 0;
		float cost = 0;
		
		// clear energy allocation
		for (int i = energyAllocation.length; --i >= 0; energyAllocation[i] = 0);
		//float[] energyAllocation = EnergyAllocator.newEnergyAllocation();

		int startSlot = CalendarUtil.getSlotOf(c);
		for (int i = 0; i < phases.length; ++i) {
			if (phases[i].getPeakPower() > maxPeakPower) maxPeakPower = phases[i].getPeakPower();

			float oneSlotEnergy = phases[i].getOneSlotMeanEnergy();
			int duration = phases[i].getSlotDuration();
			
			// crucial piece of code: here is where the scheduling of each phase is considered
			// BEWARE: the 1st phase's delay has been already accounted for
			if (isSchedulePrice && i > 0) {
				startSlot += phases[i].getScheduledSlot();
			}
			
			for (int j = duration; --j >= 0; energyAllocation[startSlot + j] = oneSlotEnergy);
			
			startSlot += duration;
		}
		cost = dailyTariff.computeCost(c, energyAllocation);

		LOG.debug("cost " + cost);
		
		// check potential overload
		try
		{
			float available = getCurrentAvailablePower();
			if (maxPeakPower >= available)
			{
				IOverloadStatusListener overloadListener= getOverloadStatusListener();
				if(overloadListener!=null)
				{
					overloadListener.notifyOverloadStatusUpdate(OverloadStatus.OverLoadRiskIfApplianceStarts);
				}
				currentOverloadStatus=OverloadStatus.OverLoadRiskIfApplianceStarts;
				powerControlProxy.notifyOverloadWarning(applianceId, IPowerAndControlProxy.OVERALL_POWER_POTENTIALLY_ABOVE_AVAILABLE_POWER_LEVEL_ON_START);
			}
		}catch(Throwable t)
		{
			LOG.error("Unable to check available power and check overload risk: {}",t);
		}
		
		return cost;
	}

	private float getCurrentAvailablePower() {
		//float available = upperPowerThreshold - super.getTotalInstantaneosPowerInfo(smartInfoId).getCurrentPower();
		float available = powerThresholds.getFirstThreshold() - super.getTotalIstantaneousDemandPower() + super.getIstantaneousProducedPower();
		return available;
	}

	
	public float calculateOverallSchedulePrice() {
		// TODO Auto-generated method stub
		return 0;
	}

	public EnergyPhaseScheduleTime[] getEnergyPhasesSchedule(String applianceId, short powerProfileID) {
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, powerProfileID);
		
		if (ppi == null) throw new IllegalStateException("Unknown Profile ID " + powerProfileID);
		return ppi.getProposedScheduledEnergyPhases();
	}
	
	public EnergyPhaseScheduleTime[] calculateEnergyPhasesSchedule(String applianceId, short powerProfileID) {
		WhiteGoodInfo appliance = getWhiteGoodInfo(applianceId);
		PowerProfileInfo ppi = getOrRetrievePowerProfile(appliance, powerProfileID);
		
		if (ppi == null) throw new IllegalStateException("Unknown Profile ID " + powerProfileID);
		
		PowerProfileState profileState = ppi.getProfileCurrentState();
		if (!profileState.isPowerProfileRemoteControllable())
			throw new IllegalStateException("Remotely Uncontrollable Profile State.");
		
		if (profileState.isApplianceStarted())
			throw new IllegalStateException("Unschedulable Profile State " + PowerProfileState.getNameOf(profileState));
		
		/*
		switch (profileState.getState()) {
			case PowerProfileState.POWER_PROFILE_PROGRAMMED:
				// not running, so the whole PP can be scheduled
				break;
			
			case PowerProfileState.ENERGY_PHASE_WAITING_TO_START:
				// for the time being, only if the 1st phase is considered then the scheduling is possible
				if (profileState.getEnergyPhaseID() == 1) break;

			// all these cases are not dealt with by now
			case PowerProfileState.POWER_PROFILE_IDLE:
			case PowerProfileState.ENERGY_PHASE_PAUSED:
			case PowerProfileState.ENERGY_PHASE_RUNNING:
			case PowerProfileState.ENERGY_PHASE_WAITING_PAUSED:
			default:
				throw new IllegalStateException("Unschedulable Profile State " + PowerProfileState.getNameOf(profileState));
		}
		*/
		return runSchedule(ppi);
	}
	
	public EnergyPhaseScheduleTime[]  runSchedule(PowerProfileInfo ppi) {
		try {
			EnergyAllocator ec = new EnergyAllocator(dailyTariff);
			ec.setPowerThreshold(powerThresholds.getContractualThreshold());
			
			float[] forecast;
			
			if (smartInfoProduction != null) {
				try{
					List<Float> hourlyData = getCloudServiceProxy().retrieveHourlyProducedEnergyForecast(smartInfoProduction.getApplianceId());
					if (hourlyData != null && hourlyData.size() > SolarIrradianceProfile.MINIMUM_INTERPOLATION_HOURS) {
						// set current value as most accurate than any forecast (obvious)
						hourlyData.set(0, super.getIstantaneousProducedPower());
						forecast = SolarIrradianceProfile.interpolate(hourlyData);
					
					} else {
						// fall back if the retrieved values are unavailable
						float maxProducedPower = super.getPeakProducedPower();
						if (sky == null) sky = new SolarIrradianceProfile(maxProducedPower, EnergyAllocator.NUMBER_OF_DAYS_HORIZON, SkyCover.ClearSky);
						forecast = sky.getSeries();
					}
					ec.setEnergyForecast(forecast);
				}catch(Throwable t){
					LOG.error("Error retreiving Energy production forecast from cloud, switching to default scheduling mode");
				}
			}

			ParticleSwarmScheduler swarm = new ParticleSwarmScheduler(ppi, ec, SCHEDULER_SWARM_SIZE);
			
			SwarmStatistics ss = null;//new SwarmStatistics();
    		int iterations = swarm.run(SCHEDULER_RUNNING_TIME, ss);

    		LOG.debug(String.format("Running time %d milliseconds, particles %d\n", SCHEDULER_RUNNING_TIME, SCHEDULER_SWARM_SIZE));
    		LOG.debug(ss + "total iterations: " + iterations);
    		LOG.debug("leader overload " + swarm.getLeastOverload());
    		LOG.debug("leader cost " + swarm.getLeastCost());
    		LOG.debug("leader tardiness " + swarm.getLeastTardiness());
    		LOG.debug("leader penalty " + swarm.getLeastPenalty());

    		LOG.debug(ppi.toString());
    		
    		return ppi.getProposedScheduledEnergyPhases();
		
		} catch (Exception e) {
			LOG.error("Excepion on runSchedule",e);
		}
		return null;
	}
	
	
	private PowerProfileInfo getOrRetrievePowerProfile(WhiteGoodInfo appliance, short profileId) {
		PowerProfileInfo ppi = appliance.getPowerProfile(profileId);
		if (ppi == null) ppi = powerControlProxy.retrievePowerProfile(appliance.getApplianceId(), profileId);
		if (ppi != null) appliance.notifyPowerProfile(ppi);
		return ppi;
	}
	

	
	public void notifyApplianceState(String applianceId, short applianceStatus, short remoteEnableFlags, int applianceStatus2) {
		// TODO Auto-generated method stub
		
	}

	public void notifyApplianceStatistics(String applianceId, long timestamp, ApplianceLog applianceLog) {
		try {
			getCloudServiceProxy().storeApplianceStatistics(applianceId, timestamp, applianceLog);
		} catch (Exception e) {
			LOG.error("Error while storing appliance statistics on HAP platform", e);
		}
	}
	
	public void notifyDeviceAdded(DeviceInfo info) {
		
		String applianceId = info.getPersistentId();
		ApplianceInfo appliance = appliances.get(applianceId);
		if (appliance == null) {
			if (info.getDescriptor().getDeviceType() == DeviceType.WhiteGood) {
				appliance = new WhiteGoodInfo(info);
				appliances.put(applianceId, appliance);
			
			} else super.notifyDeviceAdded(info);
		}
	}

	protected void deviceAvailabilityUpdated(String applianceId, boolean isAvailable) {
		super.deviceAvailabilityUpdated(applianceId, isAvailable);
		
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		if (appliance.getApplianceType() == DeviceType.WhiteGood && appliance.isAvailable()) {
			WhiteGoodInfo whiteGood = (WhiteGoodInfo) appliance;
			float formatting = whiteGood.getEnergyFormatting();
			if (formatting == IPowerProfileProxy.INVALID_FORMATTING_VALUE) {
    			formatting = powerControlProxy.getEnergyFormatting(applianceId);
    			whiteGood.setEnergyFormatting(formatting);
    		}
		}
	}
}
