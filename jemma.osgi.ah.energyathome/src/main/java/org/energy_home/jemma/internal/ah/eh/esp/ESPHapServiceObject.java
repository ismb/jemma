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
package org.energy_home.jemma.internal.ah.eh.esp;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.ebrain.ApplianceInfo;
import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.ebrain.ICloudServiceProxy;
import org.energy_home.jemma.ah.eh.esp.ESPException;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.EHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.EnergyCostPowerInfo;
import org.energy_home.jemma.m2m.ah.FloatCDV;
import org.energy_home.jemma.m2m.ah.FloatDV;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;
import org.energy_home.jemma.utils.datetime.DateUtils;

public class ESPHapServiceObject extends ESPContainersDataUtils implements ICloudServiceProxy {
	private static final Logger LOG = LoggerFactory.getLogger( ESPHapServiceObject.class );

	private static final String ONLY_MONTH_RESOLUTION_SUPPORTED = "Only month resolution is supported";
	private static final String INVALID_APPLIANCE_PID_OR_RESOLUTION = "Invalid appliance pid or resolution";
	
	IM2MHapService hapService = null;
	private ESPHapServiceCache espHapCache = null;
	private ESPGwTodayCache espGwTodayCache = null;
	// Photovoltaic production meter cached data
	private ESPGwTodayCache espGwTodayProductionCache = null;

	public ESPHapServiceObject(IM2MHapService hapService) {
		this.hapService = hapService;		
	}
	
	public void resetCache(boolean enabled) {
		this.espHapCache = new ESPHapServiceCache(this);
		if (enabled) {
			this.espGwTodayCache= new ESPGwTodayCache();
		} else {
			this.espGwTodayCache= null;
		}
	}
	
	public void applianceConnected(String applianceId, EnergyCostInfo aeci, boolean isAvailable) throws M2MHapException {
		if (espGwTodayCache != null) {
			espGwTodayCache.add(applianceId, aeci);
		}		
	}
	
	public void applianceAvailabilityUpdated(String applianceId, boolean isAvailable) throws M2MHapException {
		// Nothing to do
	}

	public void applianceDisconnected(String applianceId) throws M2MHapException {
		if (espGwTodayCache != null) {
			espGwTodayCache.remove(applianceId);
		}
	}	
	
	public AHContainerAddress getEnergyApplianceContainerId(int resolution, String applianceId) throws ESPException {
		AHContainerAddress result = null;
		try {
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			switch (resolution) {
			case ESPService.HOUR_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyEnergy);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyEnergy);
				break;
			case ESPService.DAY_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyEnergy);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyEnergy);
				break;
			case ESPService.MONTH_RESOLUTION:
				//result =  hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyEnergy);
				result =  hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyEnergy);
				break;
			}			
		} catch (M2MHapException e) {
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		}
		if (result == null)
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		return result;
	}
	
	public AHContainerAddress getReceivedEnergyApplianceContainerId(int resolution, String applianceId) throws ESPException {
		AHContainerAddress result = null;
		try {
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			switch (resolution) {
			case ESPService.HOUR_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergy);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergy);
				break;
			case ESPService.DAY_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyReceivedEnergy);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyReceivedEnergy);
				break;
			case ESPService.MONTH_RESOLUTION:
				//result =  hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyReceivedEnergy);
				result =  hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyReceivedEnergy);
				break;
			}			
		} catch (M2MHapException e) {
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		}
		if (result == null)
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		return result;
	}

	public AHContainerAddress getEnergyCostApplianceContainerId(int resolution, String applianceId) throws ESPException {
		AHContainerAddress result = null;
		try {
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			switch (resolution) {
				case ESPService.HOUR_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyEnergyCost);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyEnergyCost);
				break;
			case ESPService.DAY_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyEnergyCost);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_dailyEnergyCost);
				break;
			case ESPService.MONTH_RESOLUTION:
				//result = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyEnergyCost);
				result = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_monthlyEnergyCost);
				break;		
			}
		} catch (M2MHapException e) {
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		}
		if (result == null)
			throw new ESPException(INVALID_APPLIANCE_PID_OR_RESOLUTION);
		return result;
	}	
	
	private Float getFloatValueMonthlyForecast(AHContainerAddress dailyContainerId, AHContainerAddress wdHourlyAvgContainerId)
			throws M2MHapException {
		Calendar c = Calendar.getInstance();
		long now = c.getTimeInMillis();
		int todayDayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		int todayMonth = c.get(Calendar.MONTH);
		int todayDayOfYear = c.get(Calendar.DAY_OF_YEAR);

		float currentMonthEstimation = 0;
		boolean useTodayEstimation = false;
		float todayEstimation = 0;
		long totalDuration = 0;
		long startTime = getNormalizedStartTime(c, now, ESPService.MONTH_RESOLUTION);
		long endTime = now;

		long lastTime = startTime;
		long lastDuration = 0;
		int todayEstimationFirstHour = 0;
		int todayEstimationLastHour = 0;

		ContentInstance ci = null;
		Float lastValue = null;
		Float value = null;

		// Retrieves current month daily consumption and sum
		ContentInstanceItems items = getItemsWithHapCache(c, dailyContainerId, startTime, endTime, ESPService.DAY_RESOLUTION);
		if (items != null) {
			List<ContentInstance> resultList = items.getContentInstances();
			if (resultList != null) {
				ContentInstance contentInstance = null;
				FloatDV floatDV = null;
				for (Iterator<ContentInstance> iterator = resultList.iterator(); iterator.hasNext();) {
					contentInstance = iterator.next();
					floatDV = (FloatDV) contentInstance.getContent();
					if (floatDV != null) {
						value = floatDV.getValue();
						if (!iterator.hasNext()) {
							lastValue = value;
							lastTime = contentInstance.getId().longValue();
							lastDuration = floatDV.getDuration();
						} else if (value != null) {
							currentMonthEstimation += value.floatValue();
							totalDuration += floatDV.getDuration();
						}
					}
				}
				LOG.debug("getFloatValueMonthlyForecast - current month daily consumption returned: monthTotal="						+ currentMonthEstimation + ", monthTotalDuration=" + totalDuration + ", lastValue=" + lastValue						+ ", lastTime=" + lastTime + ", lastDuration=" + lastDuration);
			}
		} else {
			return null;
		}

		// Fix duration error for all daily measures but the last one
		long durationError = (lastTime - startTime) - totalDuration;
		if (totalDuration != 0 && durationError > DateUtils.MILLISEC_IN_ONE_HOUR)
			currentMonthEstimation += currentMonthEstimation * ((float) durationError)/(lastTime-startTime) ;

		// // If total error for daily measures is greater than max error
		// tolerance a null value is returned
		// totalDuration += lastDuration;
		// if ((now - startTime) > totalDuration*(1+DURATION_ERROR_TOLERANCE))
		// return null;

		// If a partial estimation for today is available, use it current day
		// estimation
		if (lastDuration > 0 && lastTime >= getNormalizedStartTime(c, now, ESPService.DAY_RESOLUTION)) {
			c.setTimeInMillis(lastTime);
			todayEstimationFirstHour = c.get(Calendar.HOUR_OF_DAY);
			c.setTimeInMillis(lastTime + lastDuration);
			todayEstimationLastHour = c.get(Calendar.HOUR_OF_DAY) - 1;
			if (c.get(Calendar.DAY_OF_YEAR) != todayDayOfYear)
				todayEstimationLastHour = 23;
			int estimatedNrOfHours = todayEstimationLastHour - todayEstimationFirstHour + 1;
			if (estimatedNrOfHours > 0) {
				useTodayEstimation = true;
				// Fix last duration error
				todayEstimation = (lastValue / lastDuration) * estimatedNrOfHours * DateUtils.MILLISEC_IN_ONE_HOUR;
			}
		}

		// Calculate an estimation for the average consumption of each week day,
		// including today estimation
		float[] weekDayEstimation = { 0, 0, 0, 0, 0, 0, 0 };
		weekDayEstimation[todayDayOfWeek - 1] = todayEstimation;
		ContentInstanceItems weekDayItems = getNormalizedWeekDayItems(wdHourlyAvgContainerId, getHourlyDayOfWeekStartIndex(Calendar.SUNDAY),
				getHourlyDayOfWeekEndIndex(Calendar.SATURDAY));
		if (weekDayItems == null) {
			LOG.warn("getFloatValueMonthlyForecast - week day average consumption returned null items\n");
			return null;
		}
		List<ContentInstance> weekDayItemList = weekDayItems.getContentInstances();
		if (weekDayItemList == null || weekDayItemList.size() == 0) {
			LOG.warn("getFloatValueMonthlyForecast - week day average consumption returned null or 0 sized item list\n");
			return null;
		}
		LOG.debug("getFloatValueMonthlyForecast - week day average consumption returned\n" + weekDayItems);			
		
		int weekDayIndex = 1;
		int hourlyIndex = 0;
		int nrOfMissingAvgValues = 0;

		for (Iterator<ContentInstance> iterator = weekDayItemList.iterator(); iterator.hasNext();) {
			ci = (ContentInstance) iterator.next();
			value = toFloat(ci);
			if (value != null && value.floatValue() >= 0) {
				if (!useTodayEstimation || weekDayIndex != todayDayOfWeek
						|| (hourlyIndex < todayEstimationFirstHour || hourlyIndex > todayEstimationLastHour)) {
					weekDayEstimation[weekDayIndex - 1] += value.floatValue();
				}
			} else {
				nrOfMissingAvgValues++;
			}
			hourlyIndex++;
			if (hourlyIndex % 24 == 0) {
				weekDayIndex++;
				hourlyIndex = 0;
			}
		}
		if (nrOfMissingAvgValues * DateUtils.MILLISEC_IN_ONE_HOUR >= DateUtils.MILLISEC_IN_ONE_DAY) {
			LOG.debug("getFloatValueMonthlyForecast: too many average missing values - " + nrOfMissingAvgValues);
			return null;
		} else if (nrOfMissingAvgValues > 0) {
			LOG.debug("getFloatValueMonthlyForecast: found some missing values - " + nrOfMissingAvgValues);
		}

		// The following update to lastTime value is necessary to manage legal
		// time switch
		c.setTimeInMillis(lastTime);
		c.set(Calendar.HOUR_OF_DAY, 12);
		lastTime = c.getTimeInMillis();
		while (c.get(Calendar.MONTH) == todayMonth) {
			currentMonthEstimation += weekDayEstimation[c.get(Calendar.DAY_OF_WEEK) - 1];
			lastTime += DateUtils.MILLISEC_IN_ONE_DAY;
			c.setTimeInMillis(lastTime);
		}

		return currentMonthEstimation * ((float) (weekDayItemList.size() + nrOfMissingAvgValues)) / weekDayItemList.size();
	}

	
	public ContentInstanceItems getHourlyProducedEnergyForecast(AHContainerAddress containerId) throws M2MHapException {
		ContentInstanceItems result = null;
		if (result == null) {
			Calendar c = Calendar.getInstance();
			String containerName = containerId.getContainerName();
			long startTime = containerName.equals(EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergy) ? System.currentTimeMillis()-ONE_DAY_IN_MILLISEC : System.currentTimeMillis();
			long startInstanceId = getNormalizedStartTime(c, startTime, ESPService.HOUR_RESOLUTION);
			long endInstanceId = getNormalizedEndTime(c, startTime+ONE_DAY_IN_MILLISEC*3-ONE_HOUR_IN_MILLISEC, ESPService.HOUR_RESOLUTION);
			result = getNormalizedItems(c, containerId, startInstanceId, endInstanceId, ESPService.HOUR_RESOLUTION);
		}
		return result;
	}

	public List<Float> getHourlyProducedEnergyForecastWithHapCache(AHContainerAddress containerId) throws M2MHapException {
		ContentInstanceItems items = null;
		if (espHapCache != null)
			items = espHapCache.getHourlyProducedEnergyForecastCachedItems(containerId);
		if (items == null || items.getContentInstances().size() == 0) {
			LOG.debug("getHourlyProducedEnergyForecastWithHapCache returned null or empty list, trying to use previous day data");
			items = getHourlyProducedEnergyForecast(containerId);
		}
		LOG.debug("getHourlyProducedEnergyForecastWithHapCache returned: " + items);
		return toFloatValueList(items);	
	}
		
	public List<Float> retrieveHourlyProducedEnergyForecast(String applianceId) {
		List<Float> result = null;
		try {
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergyForecast);
			AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergyForecast);
			result = getHourlyProducedEnergyForecastWithHapCache(containerId);
		} catch (Exception e) {
			LOG.error("retrieveHourlyProducedEnergyForecast exception while retrieving forecast data for produced energy", e);
		}
		LOG.debug("retrieveHourlyProducedEnergyForecast returned " + result);
		if (result != null)
			return result;
		// Backup solution if no result are returned by previous query (uses produced energy data collected in the previous 24 hours)
		try {
			AHContainerAddress containerId = getReceivedEnergyApplianceContainerId(ESPService.HOUR_RESOLUTION, applianceId);
			result = getHourlyProducedEnergyForecastWithHapCache(containerId);
		} catch (Exception e) {
			LOG.error("retrieveHourlyProducedEnergyForecast exception while retrieving previous day data for produced energy", e);
		}
		LOG.debug("retrieveHourlyProducedEnergyForecast returned " + result);
		return result;
	}
	
	public ContentInstance retrieveDeliveredEnergySummation(String applianceId) {
		ContentInstance result = null;
		try {
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEnergySum);
			AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEnergySum);
			result = hapService.getCachedLatestContentInstance(containerId);
		} catch (Exception e) {
			LOG.error("retrieveEnergySummation", e);
		}
		return result;
	}	
	
	public void storeGuiLog(long timestamp, String msg) throws M2MHapException {
		AHContainerAddress containerId = hapService.getHagContainerAddress(EHContainers.attrId_ah_eh_gui_log);
		hapService.createContentInstanceBatch(containerId, timestamp, msg);
	}
	
//	public void storeDeliveredPower(String applianceId, long timestamp, float value) throws M2MHapException {
//		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
//		AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredPower);
//		hapService.createContentInstanceBatch(containerId, timestamp, value);	
//	}
//	
//	public void storeOnOffStatus(String applianceId, long timestamp, boolean value) throws M2MHapException {
//		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
//		AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_onOffStatus);
//		hapService.createContentInstanceBatch(containerId, timestamp, value);
//	}	
	
	public void storeDeliveredEnergy(String applianceId, long timestamp, double energyTotal) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEnergySum);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEnergySum);
		Double value = new Double(energyTotal);
		hapService.createContentInstanceBatch(containerId, timestamp, value);
	}
	
	public void storeDeliveredEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo pi) throws M2MHapException {
		if (espGwTodayCache != null) {
			espGwTodayCache.update(applianceId);
		}	
		
		EnergyCostPowerInfo ecpi = new EnergyCostPowerInfo();
		synchronized (pi) {
			try {
				if (pi != null && pi.isValid()) {
					ecpi.setPowerInfo(new MinMaxPowerInfo(pi));
				} else {
					LOG.warn("storeEnergyCostPowerInfo - invalid or null power info");
				}
			} finally {
				pi.reset();
			}
		}
		FloatCDV cost = getCostCDV(eci);
		if (cost != null) {
			ecpi.setDuration(eci.getDuration());
			ecpi.setDeltaEnergy((float)eci.getDeltaEnergy());
			ecpi.setCost(cost.getValue());
			ecpi.setMinCost(cost.getMin());
			ecpi.setMaxCost(cost.getMax());	
			String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
			// applianceId DEVE contenere l'EndPointId come identificativo
			//AHContainerAddress  containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEcpi);
			AHContainerAddress  containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_deliveredEcpi);
			ContentInstance ci = hapService.createContentInstanceBatch(containerId, eci.getStartTime(), ecpi);
			LOG.debug("EnergyCostPowerInfo:\n" + ci.toXmlFormattedString());
		} else 
			storeEvent(applianceId, eci.getStartTime(), EHContainers.EVENT_INVALID_DELTA_ENERGY);
	}
	
	public void storeReceivedEnergy(String applianceId, long timestamp, double energyTotal) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_receivedEnergySum);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_receivedEnergySum);
		Double value = new Double(energyTotal);
		hapService.createContentInstanceBatch(containerId, timestamp, value);		
	}
	
	
	public void storeReceivedEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo pi) throws M2MHapException {
		// TODO No cache is currently supported for received energy
		return;
	}
	
	/* (non-Javadoc)
	 * @see org.energy_home.jemma.internal.ah.eh.esp.IHapProxy#storeEvent(java.lang.String, long, int)
	 */
	public void storeEvent(String applianceId, long timestamp, int eventType) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_events);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_events);
		hapService.createContentInstanceBatch(containerId, timestamp, new Integer(eventType));
	}

	public void storeApplianceStatistics(String applianceId, long timestamp, ApplianceLog applianceLog) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_appStats);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_appStats);
		ContentInstance ci = hapService.createContentInstanceBatch(containerId, timestamp, applianceLog);	
		System.out.println("ContentInstance:\n" + ci.toXmlPrintableString());
	}

	public ContentInstanceItems getItems(Calendar c, AHContainerAddress containerId, long startInstanceId, long endInstanceId) throws M2MHapException {
		LOG.debug("getItems(containerId=" + containerId + ", startInstanceId=" + startInstanceId + ", endInstanceId="				+ endInstanceId + ")");
		String attributeId = containerId.getContainerName();
		String applianceId = ESPApplication.getApplianceId(containerId);
		ContentInstanceItems result = null;
		CacheQueryResult cacheResult = null;

		if (espGwTodayCache != null) {
			if (attributeId == EHContainers.attrId_ah_eh_esp_hourlyEnergy
					|| attributeId == EHContainers.attrId_ah_eh_esp_dailyEnergy
					|| attributeId == EHContainers.attrId_ah_eh_esp_monthlyEnergy) {
				cacheResult = espGwTodayCache.getHourlyEnergyConsumptionResult(applianceId, startInstanceId, endInstanceId);
			} else if (attributeId == EHContainers.attrId_ah_eh_esp_hourlyEnergyCost
					|| attributeId == EHContainers.attrId_ah_eh_esp_dailyEnergyCost
					|| attributeId == EHContainers.attrId_ah_eh_esp_monthlyEnergyCost) {
				cacheResult = espGwTodayCache.getHourlyEnergyCostResult(applianceId, startInstanceId, endInstanceId);
			}
			if (cacheResult != null && cacheResult.isComplete()) {
				LOG.debug("getItems resolved on local gw cache");
				result = new ContentInstanceItems();
				result.setAddressedId(containerId.getContentInstancesUrl());
			} else {
				result = hapService.getContentInstanceItems(containerId, startInstanceId, endInstanceId);
			} 
			if (cacheResult != null) {
				if (attributeId == EHContainers.attrId_ah_eh_esp_hourlyEnergy
						|| attributeId == EHContainers.attrId_ah_eh_esp_hourlyEnergyCost)
					espGwTodayCache.merge(applianceId, result.getContentInstances(), cacheResult, ESPService.HOUR_RESOLUTION);
				else if (attributeId == EHContainers.attrId_ah_eh_esp_dailyEnergy
						|| attributeId == EHContainers.attrId_ah_eh_esp_dailyEnergyCost)
					espGwTodayCache.merge(applianceId, result.getContentInstances(), cacheResult, ESPService.DAY_RESOLUTION);
				else if (attributeId == EHContainers.attrId_ah_eh_esp_monthlyEnergy
						|| attributeId == EHContainers.attrId_ah_eh_esp_monthlyEnergyCost)
					espGwTodayCache.merge(applianceId, result.getContentInstances(), cacheResult, ESPService.MONTH_RESOLUTION);
				LOG.debug("getItems result merged with local gw cache");
			}
		} else {
			result = hapService.getContentInstanceItems(containerId, startInstanceId, endInstanceId);
		}
		if (result != null && result.getContentInstances() != null) {
			LOG.debug("getItems returned: contentInstances size=" + result.getContentInstances().size());
		}
		else if (result == null) {
			LOG.warn("getItems returned: contentInstanceItems=null");
		}
		else {
			LOG.warn("getItems returned: contentInstances=null");
		}
		return result;
	}
	public ContentInstanceItems getItemsWithHapCache(Calendar c, AHContainerAddress containerId, long startInstanceId, long endInstanceId, int resolution) throws M2MHapException{
// TODO: local cache for query on hap server are currently used only for week day hourly average values
//		ContentInstanceItems items = null;
//		if (espHapCache != null)
//			items = espHapCache.getCachedItems(c, containerId, startInstanceId, endInstanceId, resolution);
//		if (items == null)
//			items = getContentInstanceItems(c, containerId, startInstanceId, endInstanceId);
//		return items;
		return getItems(c, containerId, startInstanceId, endInstanceId);	
	}
	public ContentInstanceItems getNormalizedItems(Calendar c, AHContainerAddress containerId, long startInstanceId, long endInstanceId, int resolution) throws M2MHapException{
		ContentInstanceItems items = getItemsWithHapCache(c, containerId, startInstanceId, endInstanceId, resolution);
		return getNormalizedItems(items, c, startInstanceId, endInstanceId, resolution);
	}
	public List<Float> getEnergyConsumption(String applianceId, long startTime, long endTime, int resolution)
			throws M2MHapException, ESPException {
		AHContainerAddress containerId = getEnergyApplianceContainerId(resolution, applianceId);
		Calendar c = Calendar.getInstance();
		long startInstanceId = getNormalizedStartTime(c, startTime, resolution);
		long endInstanceId = getNormalizedEndTime(c, endTime, resolution);
		return toFloatValueList(getNormalizedItems(c, containerId, startInstanceId, endInstanceId, resolution));
	}
	public List<Float> getReceivedEnergy(String applianceId, long startTime, long endTime, int resolution)
			throws M2MHapException, ESPException {
		AHContainerAddress containerId = getReceivedEnergyApplianceContainerId(resolution, applianceId);
		Calendar c = Calendar.getInstance();
		long startInstanceId = getNormalizedStartTime(c, startTime, resolution);
		long endInstanceId = getNormalizedEndTime(c, endTime, resolution);
		return toFloatValueList(getNormalizedItems(c, containerId, startInstanceId, endInstanceId, resolution));
	}
	
	public List<Float> getEnergyCost(String applianceId, long startTime, long endTime, int resolution) throws M2MHapException, ESPException {
		AHContainerAddress containerId = getEnergyCostApplianceContainerId(resolution, applianceId);
		Calendar c = Calendar.getInstance();
		long startInstanceId = getNormalizedStartTime(c, startTime, resolution);
		long endInstanceId = getNormalizedEndTime(c, endTime, resolution);
		return toFloatValueList(getNormalizedItems( c, containerId, startInstanceId, endInstanceId, resolution));
	}
	
	public ContentInstanceItemsList getItemsList(Calendar c, AHContainerAddress containerIdFilter, long startInstanceId, long endInstanceId) throws M2MHapException {
		// TODO: gw cache is not used for queries that include more than an appliance (Map result)
		ContentInstanceItemsList itemsList = hapService.getContentInstanceItemsList(containerIdFilter, startInstanceId, endInstanceId);
		if (itemsList != null && itemsList.getContentInstanceItems() != null) {
			LOG.debug("getItemsList returned: contentInstances size=" + itemsList.getContentInstanceItems().size());
		}
		else if (itemsList == null) {
			LOG.warn("getItemsList returned: contentInstanceItemsList=null");
		}
		else {
			LOG.warn("getItemsList returned: contentInstanceItems=null");
		}
		return itemsList;
	}
	public ContentInstanceItemsList getItemsListWithHapCache(Calendar c, AHContainerAddress containerId, long startInstanceId, long endInstanceId, int resolution) throws M2MHapException {
// TODO: local cache for query on hap server are currently used only for week day hourly average values		
//		ContentInstanceItemsList itemsList = null;
//		if (espHapCache != null)
//			itemsList = espHapCache.getCachedItemsList(c, containerId, startInstanceId, endInstanceId, resolution);
//		if (itemsList == null)
//			itemsList = getContentInstanceItemsList(c, containerId, startInstanceId, endInstanceId);	
//		return itemsList;
		return getItemsList(c, containerId, startInstanceId, endInstanceId);	
	}
	public ContentInstanceItemsList getNormalizedItemsList(Calendar c, AHContainerAddress containerId, long startInstanceId, long endInstanceId, int resolution) throws M2MHapException{
		ContentInstanceItemsList itemsList = getItemsListWithHapCache(c, containerId, startInstanceId, endInstanceId, resolution);
		return getNormalizedItemsList(itemsList, c, startInstanceId, endInstanceId, resolution);
	}	
	public Map<String, List<Float>> getEnergyConsumption(long startTime, long endTime, int resolution) throws M2MHapException, ESPException {
		AHContainerAddress containerFilterId = getEnergyApplianceContainerId(resolution, AHContainerAddress.ALL_ID_FILTER);
		Calendar c = Calendar.getInstance();
		long startInstanceId = getNormalizedStartTime(c, startTime, resolution);
		long endInstanceId = getNormalizedEndTime(c, endTime, resolution);
		return toFloatValueListMap(getNormalizedItemsList(c, containerFilterId, startInstanceId, endInstanceId, resolution));
	}
	public Map<String, List<Float>> getEnergyCost(long startTime, long endTime, int resolution) throws M2MHapException, ESPException {
		AHContainerAddress containerFilterId = getEnergyCostApplianceContainerId(resolution, AHContainerAddress.ALL_ID_FILTER);
		Calendar c = Calendar.getInstance();
		long startInstanceId = getNormalizedStartTime(c, startTime, resolution);
		long endInstanceId = getNormalizedEndTime(c, endTime, resolution);
		return toFloatValueListMap(getNormalizedItemsList(c, containerFilterId, startInstanceId, endInstanceId, resolution));
	}

	public ContentInstanceItems getWeekDayItems(AHContainerAddress containerId, long startInstanceId, long endInstanceId) throws M2MHapException{
		ContentInstanceItems result =  hapService.getContentInstanceItems(containerId, startInstanceId, endInstanceId);
		if (result != null && result.getContentInstances() != null) {
			LOG.debug("getWeekDayItems returned: contentInstances size=" + result.getContentInstances().size());
		}
		else if (result == null) {
			LOG.warn("getWeekDayItems returned: contentInstanceItems=null");
		}
		else {
			LOG.warn("getWeekDayItems returned: contentInstances=null");
		}
		return result;
	}		
	public ContentInstanceItems getWeekDayItemsWithHapCache(AHContainerAddress containerId, long startInstanceId, long endInstanceId) throws M2MHapException {
		ContentInstanceItems result = null;
		if (espHapCache != null)
			result = espHapCache.getWeekDayCachedItems(containerId, startInstanceId, endInstanceId);
		if (result == null)
			result = getWeekDayItems(containerId, startInstanceId, endInstanceId);
		return result;
	}
	public ContentInstanceItems getNormalizedWeekDayItems(AHContainerAddress containerId, long startInstanceId, long endInstanceId) throws M2MHapException {
		ContentInstanceItems items = getWeekDayItemsWithHapCache(containerId, startInstanceId, endInstanceId);
		return getNormalizedWeekDayItems(items, startInstanceId, endInstanceId);
	}	
	public List<Float> getWeekDayEnergyConsumpionAverage(String applianceId, int weekDay) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyAvg);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyAvg);
		long startInstanceId = getHourlyDayOfWeekStartIndex(weekDay);
		long endInstanceId = getHourlyDayOfWeekEndIndex(weekDay);
		return toFloatValueList(getNormalizedWeekDayItems(containerId, startInstanceId, endInstanceId));
	}
	public List<Float> getWeekDayEnergyCostAverage(String applianceId, int weekDay) throws M2MHapException {
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress containerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyCostAvg);
		AHContainerAddress containerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyCostAvg);
		long startInstanceId = getHourlyDayOfWeekStartIndex(weekDay);
		long endInstanceId = getHourlyDayOfWeekEndIndex(weekDay);
		return toFloatValueList(getNormalizedWeekDayItems(containerId, startInstanceId, endInstanceId));
	}
	
	public Float getEnergyConsumptionForecast(String applianceId, int resolution) throws M2MHapException, ESPException {
		if (resolution != ESPService.MONTH_RESOLUTION)
			throw new ESPException(ONLY_MONTH_RESOLUTION_SUPPORTED);
		AHContainerAddress dailyContainerId = getEnergyApplianceContainerId(ESPService.DAY_RESOLUTION, applianceId);
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress wdHourlyAvgContainerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyAvg);
		AHContainerAddress wdHourlyAvgContainerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyAvg);
		Float currentMonthValue = getFloatValueMonthlyForecast(dailyContainerId, wdHourlyAvgContainerId);
		return currentMonthValue;
	}
	public Float getEnergyCostForecast(String applianceId, int resolution) throws M2MHapException, ESPException {
		if (resolution != ESPService.MONTH_RESOLUTION)
			throw new ESPException(ONLY_MONTH_RESOLUTION_SUPPORTED);
		AHContainerAddress dailyContainerId = getEnergyCostApplianceContainerId(ESPService.DAY_RESOLUTION, applianceId);
		String[] deviceIds = ESPApplication.getDeviceIds(applianceId);
		// applianceId DEVE contenere l'EndPointId come identificativo
		//AHContainerAddress wdHourlyAvgContainerId = hapService.getHagContainerAddress(deviceIds[0], deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyCostAvg);
		AHContainerAddress wdHourlyAvgContainerId = hapService.getHagContainerAddress(applianceId, deviceIds[1], EHContainers.attrId_ah_eh_esp_wdHourlyEnergyCostAvg);
		Float currentMonthValue = getFloatValueMonthlyForecast(dailyContainerId, wdHourlyAvgContainerId);
		return currentMonthValue;
	}

}
