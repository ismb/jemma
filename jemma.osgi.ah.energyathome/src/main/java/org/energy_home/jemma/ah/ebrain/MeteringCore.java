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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.ebrain.IOverloadStatusListener.OverloadStatus;
import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;
import org.energy_home.jemma.shal.DeviceInfo;
import org.energy_home.jemma.shal.DeviceListener;
import org.energy_home.jemma.shal.DeviceService;

public class MeteringCore implements IMeteringListener, DeviceListener {
	private static final Logger LOG = LoggerFactory.getLogger(MeteringCore.class);

	private static final IOverloadStatusListener dummyOverloadListener = new IOverloadStatusListener() {

		public void notifyOverloadStatusUpdate(OverloadStatus status) {
			// TODO Auto-generated method stub

		}
	};

	private static final ICloudServiceProxy dummyCloudProxy = new ICloudServiceProxy() {
		public void storeEvent(String applianceId, long time, int eventType) throws Exception {
		}

		public void storeApplianceStatistics(String applianceId, long time, ApplianceLog applianceLog) throws Exception {
		}

		public ContentInstance retrieveDeliveredEnergySummation(String applianceId) {
			return null;
		}

		public void storeReceivedEnergy(String applianceId, long time, double totalEnergy) throws Exception {
			// TODO Auto-generated method stub
		}

		public void storeDeliveredEnergy(String applianceId, long time, double totalEnergy) throws Exception {
			// TODO Auto-generated method stub
		}

		public void storeDeliveredEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo) throws Exception {
			// TODO Auto-generated method stub
		}

		public void storeReceivedEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo) throws Exception {
			// TODO Auto-generated method stub
		}

		public List<Float> retrieveHourlyProducedEnergyForecast(String applianceId) {
			// TODO Auto-generated method stub
			return null;
		}


	};

	// TODO: check merge, different values in 3.3.0
	// private static final long SMART_INFO_POLLING_MIN_INTERVAL_MILLISEC = 10 *
	// 1000;//30 * 1000
	// private static final long SMART_INFO_POLLING_NORMAL_INTERVAL_MILLISEC =
	// 10 * 1000;// 60 * 3 *1000;
	private static final long SMART_INFO_POLLING_MIN_INTERVAL_MILLISEC = 30 * 1000;
	private static final long SMART_INFO_POLLING_NORMAL_INTERVAL_MILLISEC = 60 * 3 * 1000;

	public static final long SMART_INFO_SUMMATION_MIN_INTERVAL = 2;

	// TODO: check merge, different values in 3.3.0
	// public static final long SMART_INFO_SUMMATION_MAX_INTERVAL = 15; //120;
	// MaximumReportingInterval
	// public static final double SMART_INFO_SUMMATION_DELTA_VALUE = 5; //1
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

	protected ICloudServiceProxy cloudProxy = dummyCloudProxy;

	protected OverloadStatus currentOverloadStatus = OverloadStatus.NoOverloadWarning;
	private IOverloadStatusListener overloadStatusListener = dummyOverloadListener;

	protected DailyTariff dailyTariff;
	protected PowerThresholds powerThresholds = new PowerThresholds(3000);

	protected float peakProducedPower = 0; // Watts/Hour

	protected SmartMeterInfo smartInfoExchange;
	protected SmartMeterInfo smartInfoProduction;

	protected boolean checkMeteringSubscriptions = true;

	protected long smartInfoPollingTimeInterval = SMART_INFO_POLLING_NORMAL_INTERVAL_MILLISEC;
	protected long lastSmartInfoPollingTime = 0;

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

			meteringProxy.subscribeCurrentSummationDelivered(applianceId, minReportingInterval, maxReportingInterval, deltaValue);
			// meteringProxy.subscribeCurrentSummationDelivered(applianceId,5,10,5);
			if (checkMeteringSubscriptions) {
				double summation = meteringProxy.getCurrentSummationDelivered(applianceId);
				notifyCurrentSummationDelivered(applianceId, System.currentTimeMillis(), summation);
				// TODO: complete/review logging
				LOG.debug("refreshCurrentSummationDeliveredSubscription - read power and summation for appliance " + applianceId + ": summation=" + summation);

			}
		}
	}
	
	private void refreshCurrentSummationReceivedSubscription(ApplianceInfo appliance) {
		float formatting = getOrRetrieveSummationFormatting(appliance);
		if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE) {
			double deltaValue = SMART_INFO_SUMMATION_DELTA_VALUE / formatting;
			String applianceId = appliance.getApplianceId();
			meteringProxy.subscribeCurrentSummationReceived(applianceId, SMART_INFO_SUMMATION_MIN_INTERVAL, SMART_INFO_SUMMATION_MAX_INTERVAL, deltaValue);
			// meteringProxy.subscribeCurrentSummationReceived(applianceId,5,10,5);
			if (checkMeteringSubscriptions) {
				double summation = meteringProxy.getCurrentSummationReceived(applianceId);
				notifyCurrentSummationReceived(applianceId, System.currentTimeMillis(), summation);
				// TODO: complete/review logging
				LOG.debug("refreshCurrentSummationReceivedSubscription - read power and summation for appliance " + applianceId + ": received=" + summation);
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
			// meteringProxy.subscribeIstantaneousDemand(applianceId,5,10,5);

			if (checkMeteringSubscriptions) {
				float power = meteringProxy.getIstantaneousDemand(applianceId);
				notifyIstantaneousDemandPower(applianceId, System.currentTimeMillis(), power);
				// TODO: complete/review logging
				LOG.debug("refreshInstantaneousDemandSubscription - read power and summation for appliance " + applianceId + ": power=" + power);
			}
		}
	}

	private void checkOverloadStatus() {
		// The following formula takes into accounts monitored appliances
		// instantaneous power consumptions data
		float signedPower = getTotalIstantaneousDemandPower() - (getIstantaneousProducedPower() - getIstantaneousSoldPower());

		// Update smart infos polling frequency depending on current
		// instantaneous delivered power
		if (signedPower >= powerThresholds.getNextToContractualThreshold()) {
			smartInfoPollingTimeInterval = SMART_INFO_POLLING_MIN_INTERVAL_MILLISEC;
		} else {
			smartInfoPollingTimeInterval = SMART_INFO_POLLING_NORMAL_INTERVAL_MILLISEC;
		}

		// Generate overload warnings depending on current instantaneous
		// delivered power
		try {
			if (signedPower <= powerThresholds.getContractualThreshold() && !currentOverloadStatus.equals(OverloadStatus.NoOverloadWarning)) {
				currentOverloadStatus = OverloadStatus.NoOverloadWarning;
				overloadStatusListener.notifyOverloadStatusUpdate(currentOverloadStatus);
			} else if (signedPower > powerThresholds.getContractualThreshold() && signedPower <= powerThresholds.getFirstThreshold() && !currentOverloadStatus.equals(OverloadStatus.ContractualPowerThresholdWarning)) {
				currentOverloadStatus = OverloadStatus.ContractualPowerThresholdWarning;
				overloadStatusListener.notifyOverloadStatusUpdate(currentOverloadStatus);
			} else if (signedPower > powerThresholds.getFirstThreshold() && signedPower <= powerThresholds.getSecondThreshold() && !currentOverloadStatus.equals(OverloadStatus.FirstPowerThresholdWarning)) {
				currentOverloadStatus = OverloadStatus.FirstPowerThresholdWarning;
				overloadStatusListener.notifyOverloadStatusUpdate(currentOverloadStatus);
			} else if (signedPower > powerThresholds.getSecondThreshold() && !currentOverloadStatus.equals(OverloadStatus.SecondPowerThresholdWarning)) {
				currentOverloadStatus = OverloadStatus.SecondPowerThresholdWarning;
				overloadStatusListener.notifyOverloadStatusUpdate(currentOverloadStatus);
			}
		} catch (Exception e) {
			LOG.debug("checkSmartInfosData error while checking wanrings", e);
		}
	}

	public void periodicTask() {
		LOG.debug(String.format("Periodic task: %s", appliances));
		if (checkMeteringSubscriptions) {
			for (ApplianceInfo appliance : appliances.values()) {
				if (appliance.isAvailable()) {
					try {
						// check if last notifications are still valid, if not
						// renew subscriptions
						// peakProducedPower > 0 means there is a solar panel
						if ((appliance == smartInfoExchange || appliance == smartInfoProduction) && peakProducedPower > 0) {
							// TODO: check merge, different values in 3.3.0
							// if (System.currentTimeMillis() -
							// ((SmartMeterInfo)appliance).getProducedEnergyTime()
							// > 1500 * DEFAULT_SUMMATION_MAX_INTERVAL) {
							if (System.currentTimeMillis() - ((SmartMeterInfo) appliance).getProducedEnergyTime() > 2500 * DEFAULT_SUMMATION_MAX_INTERVAL) {
								LOG.warn(String.format("Periodic task - invalid current summation received subscription for appliance %s", appliance.getApplianceId()));
								refreshCurrentSummationReceivedSubscription(appliance);
							}
						}

						if (smartInfoProduction != appliance) {
							// TODO: ADDED BY MARCO -- SALTARE QUESTO PASSO SE L APPLIANCE NON SUPPORTA IL METER CLUSTER, MEGLIO SE SI ESEGUE UN CONTROLLO SULLA PRESENZA DEL CLUSTER METERING 0x0702
							if ((appliance.getApplianceType() == DeviceType.WINDOW_COVERING) || (appliance.getApplianceType() == DeviceType.DOOR_LOCK))
								continue;

							
							// TODO: check merge, different values in 3.3.0
							// if (System.currentTimeMillis() -
							// appliance.getAccumulatedEnergyTime() > 1500 *
							// DEFAULT_SUMMATION_MAX_INTERVAL) {
							if (System.currentTimeMillis() - appliance.getAccumulatedEnergyTime() > 2500 * DEFAULT_SUMMATION_MAX_INTERVAL) {
								LOG.warn(String.format("Periodic task - invalid current summation delivered subscription for appliance %s", appliance.getApplianceId()));
								refreshCurrentSummationDeliveredSubscription(appliance);
							}

							// TODO: check merge, different values in 3.3.0
							// if (System.currentTimeMillis() -
							// appliance.getIstantaneousPowerTime() > 1500 *
							// DEFAULT_INST_DEMAND_MAX_INTERVAL) {
							if (System.currentTimeMillis() - appliance.getIstantaneousPowerTime() > 2500 * DEFAULT_INST_DEMAND_MAX_INTERVAL) {
								LOG.warn(String.format("Periodic task - invalid instantaneous demand subscription for appliance %s", appliance.getApplianceId()));
								refreshInstantaneousDemandSubscription(appliance);
							}
						}

					} catch (Exception e) {
						LOG.warn(String.format("Periodic task error while initializing subscriptions for appliance %s", appliance.getApplianceId()), e);
					}
				}
			}
		}

		// Send requests to smart infos depending on current polling frequency
		try {
			LOG.debug("lastSmartInfoPollingTime:{}",lastSmartInfoPollingTime);
			LOG.debug("smartInfoPollingTimeInterval:{}",smartInfoPollingTimeInterval);
			if (System.currentTimeMillis() - lastSmartInfoPollingTime >= smartInfoPollingTimeInterval) {
				if (smartInfoExchange != null && smartInfoExchange.isAvailable()) {
					smartInfoExchange.setNextTotalEnergyValidValues(1);
					smartInfoExchange.setNextProducedEnergyValidValues(1);
					meteringProxy.getIstantaneousDemand(smartInfoExchange.getApplianceId());
				}
				if (smartInfoProduction != null && smartInfoProduction.isAvailable()) {
					smartInfoProduction.setNextTotalEnergyValidValues(1);
					smartInfoProduction.setNextProducedEnergyValidValues(1);
					meteringProxy.getIstantaneousDemand(smartInfoProduction.getApplianceId());
				}
				lastSmartInfoPollingTime = System.currentTimeMillis();
			}
		} catch (Exception e) {
			LOG.error("periodicTask error while reading smart info instantaneous demand", e);
		}
		// Always update forecast data for hourly produced energy (data are
		// cached by cloudProxy locally)
		if (smartInfoProduction != null)
			cloudProxy.retrieveHourlyProducedEnergyForecast(smartInfoProduction.getApplianceId());
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

	public IOverloadStatusListener getOverloadStatusListener() {
		return overloadStatusListener;
	}

	public void setOverloadStatusListener(IOverloadStatusListener listener) {
		if (listener != null)
			overloadStatusListener = listener;
		else
			overloadStatusListener = dummyOverloadListener;
	}

	public Class<? extends DailyTariff> getDailyTariff() {
		return dailyTariff.getClass();
	}

	public void setDailyTariff(Class<? extends DailyTariff> clazz) throws InstantiationException, IllegalAccessException {
		dailyTariff = DailyTariff.getInstance(clazz);
	}

	public PowerThresholds getPowerThresholds() {
		return powerThresholds;
	}

	public void setPowerThresholds(PowerThresholds thresholds) {
		powerThresholds = thresholds;
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

		// instantaneous demand values are discarded for production smart info
		if (appliance == smartInfoProduction)
			return;

		if (power == IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE || power == IMeteringProxy.INVALID_INSTANTANEOUS_POWER_STANDARD_VALUE) {
			LOG.warn(String.format("Invalid Instantaneous Demand %f, from %s, at %s", power, applianceId, CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time, ICloudServiceProxy.EVENT_INVALID_INST_DEMAND_VALUE);
			} catch (Exception e) {
				LOG.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveDemandFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				power *= formatting;
			else
				power = IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE;
		}

		appliance.setIstantaneousPower(power, time);

		checkOverloadStatus();
	}

	// BEWARE: all notification values are raw data that need to be multiplied
	// by the relative formatting
	public void notifyCurrentSummationDelivered(String applianceId, long time, double totalEnergy) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);

		// current summation delivered values are discarded for production
		// smartinfo
		if (appliance == smartInfoProduction)
			return;

		if (totalEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			LOG.warn(String.format("Invalid Current Summation %f, from %s, at %s", totalEnergy, applianceId, CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time, ICloudServiceProxy.EVENT_INVALID_CURRENT_SUMMATION_DELIVERED_VALUE);
			} catch (Exception e) {
				LOG.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveSummationFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				totalEnergy *= formatting;
			else
				totalEnergy = IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE;
		}

		LOG.debug(String.format("Current summation delivered %f, from %s, at %s", totalEnergy, applianceId, CalendarUtil.toSecondString(time)));
		// TODO: check merge, if condition was different
		// if (totalEnergy != IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
		EnergyCostInfo eci = appliance.updateEnergyCost(time, totalEnergy);
		if (eci != null && eci.isValid()) {
			try {
				cloudProxy.storeDeliveredEnergy(applianceId, time, totalEnergy);
			} catch (Exception e) {
				LOG.error("Error while storing delivered energy on HAP platform", e);
			}
			// TODO: check merge, 3 lines below were not in 3.3.0
			// }
			// EnergyCostInfo eci = appliance.updateEnergyCost(time,
			// totalEnergy);
			// if (eci != null && eci.isValid()) {
			MinMaxPowerInfo powerInfo = appliance.getMinMaxPowerInfo();
			try {
				cloudProxy.storeDeliveredEnergyCostPowerInfo(applianceId, eci, powerInfo);
			} catch (Exception e) {
				LOG.error("Error while storing energy cost power info on HAP platform", e);
			}
		}
	}

	// this method is only called by a smartInfo, either production or exchange
	public void notifyCurrentSummationReceived(String applianceId, long time, double totalEnergy) {
		// TODO: check merge, this 4 lines (if block) was in 3.3.0
		if (peakProducedPower <= 0) {
			LOG.warn("Received CurrentSummationReceived value with invalid configuration (peak produced power <= 0)");
			return;
		}

		SmartMeterInfo appliance = (SmartMeterInfo) getApplianceInfo(applianceId);

		if (totalEnergy == IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			LOG.warn(String.format("Invalid Current Summation %f, from %s, at %s", totalEnergy, applianceId, CalendarUtil.toSecondString(time)));
			try {
				cloudProxy.storeEvent(applianceId, time, ICloudServiceProxy.EVENT_INVALID_CURRENT_SUMMATION_RECEIVED_VALUE);
			} catch (Exception e) {
				LOG.error("Error while storing event on HAP platform", e);
			}
		} else {
			float formatting = getOrRetrieveSummationFormatting(appliance);
			if (formatting != IMeteringProxy.INVALID_FORMATTING_VALUE)
				totalEnergy *= formatting;
			else
				totalEnergy = IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE;
		}

		LOG.debug(String.format("Current summation received %f, from %s, at %s", totalEnergy, applianceId, CalendarUtil.toSecondString(time)));

		if (totalEnergy != IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE) {
			try {
				cloudProxy.storeReceivedEnergy(applianceId, time, totalEnergy);
			} catch (Exception e) {
				LOG.error("Error while storing received energy on HAP platform", e);
			}
		}

		EnergyCostInfo eci = appliance.updateProducedEnergy(time, totalEnergy);
		if (eci != null && eci.isValid()) {
			MinMaxPowerInfo powerInfo = appliance.getMinMaxPowerInfo();
			try {
				cloudProxy.storeReceivedEnergyCostPowerInfo(applianceId, eci, powerInfo);
			} catch (Exception e) {
				LOG.error("Error while storing energy cost power info on HAP platform", e);
			}
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
			LOG.error("Produced power greater than configured peak power");
			producedPower = peakProducedPower;
		}
		return producedPower;
	}

	public float getIstantaneousSoldPower() {
		if (smartInfoExchange == null)
			return 0;
		float soldPower = smartInfoExchange.getMeanProducedPower();
		if (soldPower > peakProducedPower) {
			LOG.error("Sold power greater than configured peak power");
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
		if (totalPower < 0)
			totalPower = 0;

		float summedPower = 0;
		for (ApplianceInfo a : appliances.values()) {
			if (a != smartInfoExchange && a != smartInfoProduction)
				summedPower += a.getIstantaneousPower();
		}
		return Math.max(summedPower, totalPower);
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
				appliance = smartInfoProduction;
			} else {
				appliance = new ApplianceInfo(info);
			}
			appliances.put(applianceId, appliance);
		}
		if (appliance.getAccumulatedEnergyTime() == 0) {
			ContentInstance ci = cloudProxy.retrieveDeliveredEnergySummation(applianceId);
			if (ci != null) {
				Long timestamp = ci.getId();
				Double energySummation = (Double) ci.getContent();
				appliance.updateEnergyCost(timestamp.longValue(), energySummation.doubleValue());
			}
		}
		if (appliance == smartInfoExchange) {
			smartInfoExchange.setNextTotalEnergyValidValues(0);
			smartInfoExchange.setNextProducedEnergyValidValues(0);
		} else if (appliance == smartInfoProduction) {
			smartInfoProduction.setNextTotalEnergyValidValues(0);
			smartInfoProduction.setNextProducedEnergyValidValues(0);
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

	public void notifyDeviceDescriptorUpdated(DeviceInfo info) {
	}

	public void notifyDeviceConfigurationUpdated(DeviceInfo info) {
	}

	protected void deviceAvailabilityUpdated(String applianceId, boolean isAvailable) {
		ApplianceInfo appliance = getApplianceInfo(applianceId);
		appliance.setAvailable(isAvailable);
		if (appliance.isAvailable()) {
			if (appliance != smartInfoProduction) {
				// TODO: ADDED BY MARCO -- SALTARE QUESTO PASSO SE L APPLIANCE NON SUPPORTA IL METER CLUSTER, MEGLIO SE SI ESEGUE UN CONTROLLO SULLA PRESENZA DEL CLUSTER METERING 0x0702
				if ((appliance.getApplianceType() == DeviceType.DOOR_LOCK) || (appliance.getApplianceType() == DeviceType.WINDOW_COVERING))
					return;
				refreshCurrentSummationDeliveredSubscription(appliance);
				refreshInstantaneousDemandSubscription(appliance);
			}
			if ((appliance == smartInfoProduction || appliance == smartInfoExchange) && peakProducedPower > 0) {
				refreshCurrentSummationReceivedSubscription(appliance);
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
