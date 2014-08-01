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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.ah.ebrain.algo.OverloadDetectorListener;
import org.energy_home.jemma.ah.ebrain.algo.OverloadDetectorTask;
import org.energy_home.jemma.ah.ebrain.old.PowerProfilePrice;
import org.energy_home.jemma.ah.ebrain.old.SmartAppliance;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnergyBrain implements IBasicApplianceListener {
		protected static final Logger LOG = LoggerFactory.getLogger( EnergyBrain.class);
	
		private static final ICloudServiceProxy dummyHapProxy = new ICloudServiceProxy() {
		public void storeEvent(String applianceId, long time, int eventType) throws Exception {}
		public void storeApplianceStatistics(String applianceId, long time, ApplianceLog applianceLog) throws Exception {}
		public ContentInstance retrieveDeliveredEnergySummation(String applianceId) {
			return null;
		}
		public void storeReceivedEnergy(String applianceId, long time, double totalEnergy) throws Exception {
			// TODO Auto-generated method stub
			
		}
		public void storeDeliveredEnergy(String applianceId, long time, double totalEnergy) throws Exception {
			// TODO Auto-generated method stub
			
		}
		//TODO: check merge, was uncommented in 3.3.0
		/* Removed in demo_amsterdam version
		public void storeDeliveredPower(String applianceId, long time, float power) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		public void storeOnOffStatus(String appliancePid, long timestamp, boolean value) throws Exception {
			// TODO Auto-generated method stub
			
		}
		*/
		public void storeDeliveredEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		public void storeReceivedEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo)
				throws Exception {
			// TODO Auto-generated method stub
			
		}
		public List<Float> retrieveHourlyProducedEnergyForecast(String applianceId) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	public static final int ISO4217_CURRENCY_CODE = 978; // euro currency
	public static final short TRAILING_DIGIT_CENTS = 2; // trailing digits: cents of euros
	public static final short TRAILING_DIGIT_TENTHS = 3; // trailing digits: thousandths of euros
	
	protected static long encodeCostWithFormatting(float value, Short formatting) {
		int decimals = formatting & 0x07 -3;
		if (decimals < 0) throw new IllegalArgumentException("Incosisten formatting.");
		
		//v = (long)((value * Math.pow(10, decimals)) / 1000);
		return Math.round(value * Math.pow(10, decimals));
	}

	private static EnergyBrain instance;

	public static EnergyBrain getInstance() {
		if (instance == null) instance = new EnergyBrain();
		return instance;
	}

	private ICloudServiceProxy hapProxy = dummyHapProxy;
	protected String smartInfoId = "";
	protected float upperPowerThreshold = 3300; // watts per hour

	protected DailyTariff dailyTariff;
	protected short tariffTrailingDigits;
	
	protected Map<String, SmartAppliance> appliances = new HashMap<String, SmartAppliance>();
	protected Map<String, ApplianceInfo> applianceInfos = new HashMap<String, ApplianceInfo>();
		
	protected OverloadDetectorTask detector;
		
	protected EnergyBrain() {
		try {
			dailyTariff = DailyTariff.getInstance(TwoTierDailyTariff.class);
			setTariffTrailingDigits(TRAILING_DIGIT_TENTHS);
			setCloudServiceProxy(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ICloudServiceProxy getCloudServiceProxy() {
		return hapProxy;
	}
	public void setCloudServiceProxy(ICloudServiceProxy proxy) {
		if (proxy == null) hapProxy = dummyHapProxy;
		else hapProxy = proxy;
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
		float totalOverload = maxPeakPower + getTotalInstantaneosPowerInfo(smartInfoId).getCurrentPower() - upperPowerThreshold;
		if (detector != null) totalOverload += detector.computeTotalPowerUsage();

		return new PowerProfilePrice(powerProfileID, ISO4217_CURRENCY_CODE, price, tariffTrailingDigits, (int)totalOverload);
	}
	
	// this should be called by the SmartInfo device
	public void notifyTotalInstantaneosPowerUsage(String applianceId, long time, float instantaneousConsumption) {
		smartInfoId = applianceId;
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

	
	public ApplianceInfo getApplianceInfo(String applianceId) {
		ApplianceInfo appliance = applianceInfos.get(applianceId);
		if (appliance == null) {
			appliance = new ApplianceInfo(applianceId);
			applianceInfos.put(applianceId, appliance);
		}
		return appliance;
	}
	

	
	public void notifyApplianceAvailability(String applianceId, boolean isAvailable, ApplianceType category) {
		ApplianceInfo appliance= getApplianceInfo(applianceId);
		//appliance.setAvailable(isAvailable);
	}

	
	public boolean removeAppliance(String applianceId) {
		return applianceInfos.remove(applianceId) != null;
	}
}
