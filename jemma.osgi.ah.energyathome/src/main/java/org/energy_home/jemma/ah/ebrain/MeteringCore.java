/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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






//import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.energy_home.jemma.shal.DeviceInfo;
import org.energy_home.jemma.shal.DeviceListener;
import org.energy_home.jemma.shal.DeviceService;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;

public class MeteringCore implements IMeteringListener, IOnOffListener, DeviceListener {
	private static final Log log = LogFactory.getLog(MeteringCore.class.getSimpleName());

	private static final ICloudServiceProxy dummyCloudProxy = new ICloudServiceProxy() {
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
		
		public void storeDeliveredPower(String applianceId, long time, float power) throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		public void storeOnOffStatus(String appliancePid, long timestamp, boolean value) throws Exception {
			// TODO Auto-generated method stub
			
		}

		public void storeDeliveredEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo)
				throws Exception {
			// TODO Auto-generated method stub			
		}

		public void storeReceivedEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo)
				throws Exception {
			// TODO Auto-generated method stub		
		}


	};

	public static final long SMART_INFO_SUMMATION_MIN_INTERVAL = 2;
	public static final long SMART_INFO_SUMMATION_MAX_INTERVAL = 120;
	public static final double SMART_INFO_SUMMATION_DELTA_VALUE = 1;

	public static final long DEFAULT_SUMMATION_MIN_INTERVAL = 120;
	public static final long DEFAULT_SUMMATION_MAX_INTERVAL = 120;
	public static final double DEFAULT_SUMMATION_DELTA_VALUE = 4500;
	
	public static final long DEFAULT_ONOFF_MIN_INTERVAL = 2;
	public static final long DEFAULT_ONOFF_MAX_INTERVAL = 120;

	public static final long SMART_INFO_INST_DEMAND_MIN_INTERVAL = 2;
	public static final long SMART_INFO_INST_DEMAND_MAX_INTERVAL = 120;
	public static final float SMART_INFO_INST_DEMAND_DELTA_VALUE = 5;

	public static final long DEFAULT_INST_DEMAND_MIN_INTERVAL = 2;
	public static final long DEFAULT_INST_DEMAND_MAX_INTERVAL = 120;
	public static final float DEFAULT_INST_DEMAND_DELTA_VALUE = 5;

	private static MeteringCore instance;

	public static MeteringCore getInstance() {
		if (instance == null)
			instance = new MeteringCore();
		return instance;
	}

	protected Map<String, ApplianceInfo> appliances = new ConcurrentHashMap<String, ApplianceInfo>();

	protected IOnOffProxy onOffProxy;
	protected IMeteringProxy meteringProxy;
	protected ICloudServiceProxy cloudProxy;
	protected DailyTariff dailyTariff;
	protected float upperPowerThreshold = 3300; // Watts/Hour
	protected float peakProducedPower = 0; // Watts/Hour

	protected SmartMeterInfo smartInfoExchange;
	protected SmartMeterInfo smartInfoProduction;

	protected boolean checkMeteringSubscriptions = true;
	
	protected MeteringCore() {
		try {
			dailyTariff = DailyTariff.getInstance();
			setCloudServiceProxy(null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void refreshCurrentSummationDeliveredSubscription(ApplianceInfo appliance) {
		float formatting = getOrRetrieveSummationFormatting(appliance);
		if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE) {
			long minReportingInterval = 0;
			long maxReportingInterval = 0;
			double deltaValue = 0;
			DeviceType type = appliance.getApplianceType();
			
			if (type == DeviceType.Meter) {
				minReportingInterval = SMART_INFO_SUMMATION_MIN_INTERVAL;
				maxReportingInterval = SMART_INFO_SUMMATION_MAX_INTERVAL;
				deltaValue = SMART_INFO_SUMMATION_DELTA_VALUE / formatting;
			} else {
				minReportingInterval = DEFAULT_SUMMATION_MIN_INTERVAL;
				maxReportingInterval = DEFAULT_SUMMATION_MAX_INTERVAL;
				deltaValue = DEFAULT_SUMMATION_DELTA_VALUE / formatting;
			}
			
			String applianceId = appliance.getApplianceId();

			meteringProxy.subscribeCurrentSummationDelivered(applianceId, minReportingInterval,	maxReportingInterval, deltaValue);
			if (checkMeteringSubscriptions) {
				double summation = meteringProxy.getCurrentSummationDelivered(applianceId);
				notifyCurrentSummationDelivered(applianceId, System.currentTimeMillis(), summation);
				// TODO: complete/review logging
				log.info("refreshCurrentSummationDeliveredSubscription - read power and summation for appliance " + applianceId
						+ ": summation=" + summation);

			}
		}
	}
	
	private void refreshCurrentSummationReceivedSubscription(ApplianceInfo appliance) {
		float formatting = getOrRetrieveSummationFormatting(appliance);
		if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE) {
			double deltaValue = SMART_INFO_SUMMATION_DELTA_VALUE / formatting;
			String applianceId = appliance.getApplianceId();
			meteringProxy.subscribeCurrentSummationReceived(applianceId, SMART_INFO_SUMMATION_MIN_INTERVAL, SMART_INFO_SUMMATION_MAX_INTERVAL, deltaValue);
			if (checkMeteringSubscriptions) {
				double summation = meteringProxy.getCurrentSummationReceived(applianceId);
				notifyCurrentSummationReceived(applianceId, System.currentTimeMillis(), summation);
				// TODO: complete/review logging
				log.info("refreshCurrentSummationReceivedSubscription - read power and summation for appliance " + applianceId
						+ ": received=" + summation);
			}
		}
	}
		
	private void refreshInstantaneousDemandSubscription(ApplianceInfo appliance) {
		float formatting = getOrRetrieveDemandFormatting(appliance);
		if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE) {
			long minReportingInterval = 0;
			long maxReportingInterval = 0;
			float deltaValue = 0;

			if (appliance.getApplianceType() == DeviceType.Meter) {
				minReportingInterval = SMART_INFO_INST_DEMAND_MIN_INTERVAL;
				maxReportingInterval = SMART_INFO_INST_DEMAND_MAX_INTERVAL;
				deltaValue = SMART_INFO_INST_DEMAND_DELTA_VALUE / formatting;
			} else {
				minReportingInterval = DEFAULT_INST_DEMAND_MIN_INTERVAL;
				maxReportingInterval = DEFAULT_INST_DEMAND_MAX_INTERVAL;
				deltaValue = DEFAULT_INST_DEMAND_DELTA_VALUE / formatting;
			}
			
			String applianceId = appliance.getApplianceId();
			meteringProxy.subscribeIstantaneousDemand(applianceId, minReportingInterval, maxReportingInterval, deltaValue);
			if (checkMeteringSubscriptions) {
				float power = meteringProxy.getIstantaneousDemand(applianceId);
				notifyIstantaneousDemandPower(applianceId, System.currentTimeMillis(), power);
				// TODO: complete/review logging
				log.info("refreshInstantaneousDemandSubscription - read power and summation for appliance " + applianceId
						+ ": power=" + power);
			}
		}
	}
	
	private void refreshOnOffSubscription(ApplianceInfo appliance) {
		String applianceId = appliance.getApplianceId();
		onOffProxy.subscribeStatus(applianceId, DEFAULT_ONOFF_MIN_INTERVAL, DEFAULT_ONOFF_MAX_INTERVAL);
		if (checkMeteringSubscriptions) {
			Boolean onOff = onOffProxy.getStatus(applianceId);
			if (onOff != null) {
				notifyStatus(applianceId, System.currentTimeMillis(), onOff);
				// TODO: complete/review logging
				log.info("refreshOnOffSubscription - read onoff status for appliance " + applianceId
						+ ": received=" + onOff);
			} else {
				// TODO: complete/review logging
				log.error("refreshOnOffSubscription - onoff status read failed for appliance " + applianceId);
			}
		}
	}

	public void periodicTask() {
		log.debug(String.format("Periodic task: %s", appliances));
		if (checkMeteringSubscriptions) {
			for (ApplianceInfo appliance : appliances.values()) {
				if (appliance.isAvailable()) {
					try {
						// check if last notifications are still valid, if not renew subscriptions
						// peakProducedPower > 0 means there is a solar panel
						if ((appliance == smartInfoExchange || appliance == smartInfoProduction) && peakProducedPower > 0) {
							if (System.currentTimeMillis() - ((SmartMeterInfo)appliance).getProducedEnergyTime() > 1500 * DEFAULT_SUMMATION_MAX_INTERVAL) {
								log.error(String.format("Periodic task - invalid current summation received subscription for appliance %s", appliance.getApplianceId()));
								refreshCurrentSummationReceivedSubscription(appliance);
							}
						}
							
						if (smartInfoProduction != appliance) {
							if (System.currentTimeMillis() - appliance.getAccumulatedEnergyTime() > 1500 * DEFAULT_SUMMATION_MAX_INTERVAL) {
								log.error(String.format("Periodic task - invalid current summation delivered subscription for appliance %s", appliance.getApplianceId()));
								refreshCurrentSummationDeliveredSubscription(appliance);
							}

							if (System.currentTimeMillis() - appliance.getIstantaneousPowerTime() > 1500 * DEFAULT_INST_DEMAND_MAX_INTERVAL) {
								log.error(String.format("Periodic task - invalid instantaneous demand subscription for appliance %s", appliance.getApplianceId()));
								refreshInstantaneousDemandSubscription(appliance);
							}
						}
						// TODO: no check is currently performed on onoff attribute subscriptions
						
					} catch (Exception e) {
						log.error(String.format("Periodic task error while initializing subscriptions for appliance %s",
										appliance.getApplianceId()), e);
					}
				}
			}
		}
		try {
			if (smartInfoExchange != null && smartInfoExchange.isAvailable()) meteringProxy.getIstantaneousDemand(smartInfoExchange.getApplianceId());
			if (smartInfoProduction != null && smartInfoProduction.isAvailable()) meteringProxy.getIstantaneousDemand(smartInfoProduction.getApplianceId());
		
		} catch (Exception e) {
			log.error("Periodic task error while reading smart-info attributes", e);
		}
	}


	public IMeteringProxy getMeteringProxy() {
		return meteringProxy;
	}

	public void setMeteringProxy(IMeteringProxy proxy) {
		if (proxy == null)
			throw new IllegalArgumentException("The argument cannot be null.");
		meteringProxy = proxy;
	}


	public IOnOffProxy getOnOffProxy() {
		return onOffProxy;
	}

	public void setOnOffProxy(IOnOffProxy proxy) {
		if (proxy == null)
			throw new IllegalArgumentException("The argument cannot be null.");
		this.onOffProxy = proxy;
	}
	
	public ICloudServiceProxy getCloudServiceProxy() {
		return cloudProxy;
	}

	public void setCloudServiceProxy(ICloudServiceProxy proxy) {
		if (proxy != null)
			cloudProxy = proxy;
		else
			cloudProxy = dummyCloudProxy;
	}

	public Class<? extends DailyTariff> getDailyTariff() {
		return dailyTariff.getClass();
	}

	public void setDailyTariff(Class<? extends DailyTariff> clazz) throws InstantiationException,
			IllegalAccessException {
		dailyTariff = DailyTariff.getInstance(clazz);
	}

	public float getUpperPowerThreshold() {
		return upperPowerThreshold;
	}

	public void setUpperPowerThreshold(float upper) {
		upperPowerThreshold = upper;
	}
	
	public float getPeakProducedPower() {
		return peakProducedPower;
	}

	public void setPeakProducedPower(float upper) {
		peakProducedPower = upper;
	}

	public void setCheckMeteringSubscriptions(boolean enabled) {
		checkMeteringSubscriptions = enabled;
	}
	
	public ApplianceInfo getApplianceInfo(String applianceId) {
		return appliances.get(applianceId);
	}

	/***************************************************************************************************************************
	 * (non-Javadoc)
	 * 
	 * @throws M2MHapException
	 * @see org.energy_home.jemma.ah.ebrain.IMeteringListener
	 */

	// BEWARE: all notification values are raw data that need to be multiplied
	// by the relative formatting
	public void notifyIstantaneousDemandPower(String applianceId, long time, float power) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);

		if (power == IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE) {
			log.warn(String.format("Invalid Instantaneous Demand %f, from %s, at %s", power, applianceId,
					CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time, ICloudServiceProxy.EVENT_INVALID_INST_DEMAND_VALUE);
			} catch (Exception e) {
				log.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveDemandFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				power *= formatting;
			else
				power = IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE;
		}
		if (power != IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			try {
				cloudProxy.storeDeliveredPower(applianceId, time, power);
			} catch (Exception e) {
				log.error("Error while storing delivered power on HAP platform", e);
			}
		}
		
		appliance.setIstantaneousPower(power, time);
	}

	// BEWARE: all notification values are raw data that need to be multiplied
	// by the relative formatting
	public void notifyCurrentSummationDelivered(String applianceId, long time, double totalEnergy) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);

		if (totalEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			log.warn(String.format("Invalid Current Summation %f, from %s, at %s", totalEnergy, applianceId,
					CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time,
						ICloudServiceProxy.EVENT_INVALID_CURRENT_SUMMATION_DELIVERED_VALUE);
			} catch (Exception e) {
				log.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveSummationFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				totalEnergy *= formatting;
			else
				totalEnergy = IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE;
		}

		log.info(String.format("Current summation delivered %f, from %s, at %s", totalEnergy, applianceId,
				CalendarUtil.toSecondString(time)));
		if (totalEnergy != IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			try {
				cloudProxy.storeDeliveredEnergy(applianceId, time, totalEnergy);
			} catch (Exception e) {
				log.error("Error while storing delivered energy on HAP platform", e);
			}
		}
		EnergyCostInfo eci = appliance.updateEnergyCost(time, totalEnergy);
		if (eci != null && eci.isValid()) {
			MinMaxPowerInfo powerInfo = appliance.getMinMaxPowerInfo();
			try {
				cloudProxy.storeDeliveredEnergyCostPowerInfo(applianceId, eci, powerInfo);
			} catch (Exception e) {
				log.error("Error while storing energy cost power info on HAP platform", e);
			}
		}
	}

	// this method is only called by a smartInfo, either production or exchange
	public void notifyCurrentSummationReceived(String applianceId, long time, double totalEnergy) {
		SmartMeterInfo appliance = (SmartMeterInfo)getApplianceInfo(applianceId);

		if (totalEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			log.warn(String.format("Invalid Current Summation %f, from %s, at %s", totalEnergy, applianceId,
					CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time,
						ICloudServiceProxy.EVENT_INVALID_CURRENT_SUMMATION_RECEIVED_VALUE);
			} catch (Exception e) {
				log.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveSummationFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				totalEnergy *= formatting;
			else
				totalEnergy = IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE;
		}

		log.info(String.format("Current summation received %f, from %s, at %s", totalEnergy, applianceId,
				CalendarUtil.toSecondString(time)));
		if (totalEnergy != IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			try {
				cloudProxy.storeReceivedEnergy(applianceId, time, totalEnergy);
			} catch (Exception e) {
				log.error("Error while storing received energy on HAP platform", e);
			}
		}
		EnergyCostInfo eci = appliance.updateProducedEnergy(time, totalEnergy);
		if (eci != null && eci.isValid()) {
			MinMaxPowerInfo powerInfo = appliance.getMinMaxPowerInfo();
			try {
				cloudProxy.storeReceivedEnergyCostPowerInfo(applianceId, eci, powerInfo);
			} catch (Exception e) {
				log.error("Error while storing energy cost power info on HAP platform", e);
			}
		}
	}
	
	public void notifyStatus(String applianceId, long time, Boolean status) {
		try {
			cloudProxy.storeOnOffStatus(applianceId, time, status);
		} catch (Exception e) {
			log.error("Error while storing onoff status on HAP platform", e);
		}		
	}

	public double getCurrentSummationReceived(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * public MinMaxPowerInfo getInstantaneosPowerInfo(String applianceId) {
	 * ApplianceInfo appliance = getApplianceInfo(applianceId);
	 * 
	 * if (appliance != smartInfo) return appliance.getMinMaxPowerInfo();
	 * 
	 * // ENEL fix: we need to see if the current total power is greater than
	 * the sum of all smart appliances float summedPower = 0; MinMaxPowerInfo
	 * totalPower = smartInfo.getMinMaxPowerInfo();
	 * 
	 * // start by setting the sum with the negative of the smart-info value, as
	 * it will be added again in the next loop // and we don't want that
	 * summedPower = -smartInfo.getIstantaneousPower(); for (ApplianceInfo a :
	 * appliances.values()) { summedPower += a.getIstantaneousPower(); }
	 * 
	 * if (totalPower.getCurrentPower() < summedPower) {
	 * totalPower.setCurrentPower(summedPower, System.currentTimeMillis()); }
	 * return totalPower; }
	 * 
	 * 
	 * public double getApplianceAccumulatedEnergy(String applianceId) {
	 * ApplianceInfo appliance = getApplianceInfo(applianceId); return
	 * appliance.getAccumulatedEnergy(); }
	 * 
	 * 
	 * public long getApplianceAccumulatedEnergyTime(String applianceId) {
	 * ApplianceInfo appliance = getApplianceInfo(applianceId); return
	 * appliance.getAccumulatedEnergyTime(); }
	 */

	public String getSmartInfoExchangeId() {
		if (smartInfoExchange == null)
			return null;
		return smartInfoExchange.getApplianceId();
	}
	
	public String getSmartInfoProductionId() {
		if (smartInfoProduction == null)
			return null;
		return smartInfoProduction.getApplianceId();
	}
	
	public float getIstantaneousProducedPower() {
		if (smartInfoProduction == null)
			return 0;
		float producedPower = smartInfoProduction.getMeanProducedPower();
		if (producedPower > peakProducedPower) {
			log.error("Produced power greater than configured peak power");
			producedPower = peakProducedPower;
		}
		return producedPower;
	}

	public float getIstantaneousSoldPower() {
		if (smartInfoExchange == null)
			return 0;
		float soldPower = smartInfoExchange.getMeanProducedPower();
		if (soldPower > peakProducedPower) {
			log.error("Sold power greater than configured peak power");
			soldPower = peakProducedPower;
		}
		
		return soldPower;
	}

	public float getTotalIstantaneousDemandPower() {
		// ENEL fix: check if the current total power is greater than the sum of
		// all smart appliances
		float totalPower = 0;
		if (smartInfoExchange != null)
			totalPower = smartInfoExchange.getIstantaneousPower() + getIstantaneousProducedPower() - getIstantaneousSoldPower();

		// BEWARE: this loop sums all power values including the smart-info
		// itself
		float summedPower = 0;
		for (ApplianceInfo a : appliances.values()) {
			summedPower += a.getIstantaneousPower();
		}
		return Math.max(summedPower - totalPower, totalPower);
	}

	public float getIstantaneousDemandPower(String applianceId) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		return appliance.getIstantaneousPower();
	}

	public double getCurrentSummationDelivered(String applianceId) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		return appliance.getAccumulatedEnergy();
	}

	public EnergyCostInfo getAccumulatedEnergyCost(String applianceId) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		return appliance.getAccumulatedEnergyCost();
	}

	private float getOrRetrieveSummationFormatting(ApplianceInfo appliance) {
		float formatting = appliance.getSummationFormatting();
		if (formatting == IMeteringProxy.INVALID_FORMATTING_VALUE) {
			formatting = meteringProxy.getSummationFormatting(appliance.getApplianceId());
			appliance.setSummationFormatting(formatting);
		}
		return formatting;
	}

	private float getOrRetrieveDemandFormatting(ApplianceInfo appliance) {
		float formatting = appliance.getDemandFormatting();
		if (formatting == IMeteringProxy.INVALID_FORMATTING_VALUE) {
			formatting = meteringProxy.getDemandFormatting(appliance.getApplianceId());
			appliance.setDemandFormatting(formatting);
		}
		return formatting;
	}

	public String getEndPointId() {
		return "EnergyBrain";
	}

	public void notifyDeviceAdded(DeviceInfo info) {
		String applianceId = info.getPersistentId();
		ApplianceInfo appliance = appliances.get(applianceId);
		if (appliance == null) {
			if (info.getConfiguration().getCategory() == DeviceCategory.Meter) {
				smartInfoExchange = new SmartMeterInfo(info);
				appliance = smartInfoExchange;
			} else if (info.getConfiguration().getCategory() == DeviceCategory.ProductionMeter) {
				smartInfoProduction = new SmartMeterInfo(info);
				appliance =smartInfoProduction;
			} else {
				appliance = new ApplianceInfo(info);
			}
			appliances.put(applianceId, appliance);
		}
		if (appliance.getAccumulatedEnergyTime() == 0) {
			ContentInstance ci = cloudProxy.retrieveDeliveredEnergySummation(applianceId);
			if (ci != null) {
				Long timestamp = ci.getId();
				Double energySummation = (Double)ci.getContent();
				appliance.updateEnergyCost(timestamp.longValue(), energySummation.doubleValue());
			}
		}
	}

	public void notifyDeviceRemoved(DeviceInfo info) {
		String applianceId = info.getPersistentId();
		if (smartInfoExchange != null && applianceId.equals(smartInfoExchange.getApplianceId())) {
			smartInfoExchange = null;
		} else if (smartInfoProduction != null && applianceId.equals(smartInfoProduction.getApplianceId())) {
			smartInfoProduction = null;
		} 
		appliances.remove(applianceId);
	}

	public void notifyDeviceDescriptorUpdated(DeviceInfo info) {}

	public void notifyDeviceConfigurationUpdated(DeviceInfo info) {}

	protected void deviceAvailabilityUpdated(String applianceId, boolean isAvailable) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		appliance.setAvailable(isAvailable);
		if (appliance.isAvailable()) {
			if (appliance != smartInfoProduction) {
				refreshCurrentSummationDeliveredSubscription(appliance);
				refreshInstantaneousDemandSubscription(appliance);
			}
			if ((appliance == smartInfoProduction || appliance == smartInfoExchange) && peakProducedPower > 0) {
				refreshCurrentSummationReceivedSubscription(appliance);
			}
			if (appliance.getApplianceType() == DeviceType.SmartPlug) {
				refreshOnOffSubscription(appliance);
			}
		} else {
			appliance.setIstantaneousPower(0, System.currentTimeMillis());
		}

	}

	public void notifyDeviceServiceAvailable(DeviceInfo info, DeviceService deviceService) {
		String applianceId = info.getPersistentId();
		deviceAvailabilityUpdated(applianceId, true);
	}

	public void notifyDeviceServiceUnavailable(DeviceInfo info) {
		String applianceId = info.getPersistentId();
		deviceAvailabilityUpdated(applianceId, false);	
	}

}
