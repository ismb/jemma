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


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ah.FloatDV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESPGwTodayCache extends ESPContainersDataUtils {	
	private static final Logger LOG = LoggerFactory.getLogger( ESPGwTodayCache.class );
	
	private static final int ENERGY_DATA_TYPE = 0;
	private static final int COST_DATA_TYPE = 1;
	
	private static final int HOURS_IN_A_DAY = 24;
	
	private class CachedData {
		private Calendar calendar;
		private Calendar startTime = null;
		private Calendar endTime = null;
		private String applianceId;
		private EnergyCostInfo aeci; 
		private List<EnergyCostInfo> hourlyCachedData;
		
		CachedData(String applianceId, EnergyCostInfo aeci) {
			this.applianceId = applianceId;
			this.aeci = aeci;
			hourlyCachedData = new ArrayList<EnergyCostInfo>(HOURS_IN_A_DAY);
			calendar = Calendar.getInstance();
			synchronized (aeci) {
				aeci.copyAndReset();
				hourlyCachedData.add(aeci);
			}			
		}
		
		CacheQueryResult getCachedData(long startId, long endId, int dataType) {
			if (startTime == null) {
				LOG.warn("getCachedData called with null startTime/endTime for CachedData class, appliance " + applianceId);
				return null;
			}
			boolean isComplete = (startId >= startTime.getTimeInMillis()) && (endId <= endTime.getTimeInMillis());		
			List<ContentInstance> resultList = new ArrayList<ContentInstance>();
			CacheQueryResult result = new CacheQueryResult(isComplete, resultList);
			EnergyCostInfo eci;
			ContentInstance ci;
			for (Iterator<EnergyCostInfo> iterator = hourlyCachedData.iterator(); iterator.hasNext();) {
				eci = (EnergyCostInfo) iterator.next();
				synchronized (eci) {
					if (eci.isValid()) {
						long id = eci.getStartTime();
						if (id >= startId && id <= endId) {
							Object content;
							if (dataType == ENERGY_DATA_TYPE)
								content = getEnergyDV(eci);
							else if (dataType == COST_DATA_TYPE)
								content = getCostCDV(eci);
							else
								throw new IllegalArgumentException("Invalid data type: only energy and cost data are cached");
							if (content != null) {
								ci = new ContentInstance();
								ci.setId(eci.getStartTime());
								ci.setCreationTime(eci.getEndTime());
								ci.setContent(content);
								resultList.add(ci);
							}
						}
					}
				}
			}
			return result;
		}
		
		void mergeData(List<ContentInstance> resultList, CacheQueryResult cachedResult, int resolution) {
			if (startTime == null) {
				LOG.warn("mergeData called with null startTime/endTime for CachedData class, appliance " + applianceId);
				return;
			}
			long startId = 0;
			long endId = 0;
			ContentInstance ci = null;
			if (resultList.size() > 0) {
				ci = resultList.get(resultList.size()-1);
				startId = ci.getId();
				endId = startId + ((FloatDV) ci.getContent()).getDuration();	
			}
			if (resolution == ESPService.HOUR_RESOLUTION) {
				boolean addAll = false;
				ContentInstance cachedCi;
				long cachedStartId;
				for (Iterator<ContentInstance> iterator = cachedResult.getResult().iterator(); iterator.hasNext();) {
					cachedCi = (ContentInstance) iterator.next();
					cachedStartId = cachedCi.getId();
					if (addAll) {
						resultList.add(cachedCi);	
					} else if (cachedStartId >= startId) {
						// TODO: the following test is needed because millisenconds are not managed by hap server
						if (cachedStartId - startId <= 1000)  {
							ci.setId(cachedCi.getId());
							ci.setContent(cachedCi.getContent());
							ci.setCreationTime(cachedCi.getCreationTime());
							addAll = true;
						} else if (cachedStartId >= endId) {
							Calendar c = Calendar.getInstance();
							c.setTimeInMillis(startId);
							int startIdHourOfDay = c.get(Calendar.HOUR_OF_DAY);
							int startIdDayOfYear = c.get(Calendar.DAY_OF_YEAR);
							int startIdYear = c.get(Calendar.YEAR);	
							c.setTimeInMillis(cachedStartId);
							if (c.get(Calendar.HOUR_OF_DAY) == startIdHourOfDay && 
									c.get(Calendar.DAY_OF_YEAR) == startIdDayOfYear && 
									c.get(Calendar.YEAR) == startIdYear)
								addContentInstance(ci, cachedCi);
							else
								resultList.add(cachedCi);
							addAll = true;
						}
					} 
				}
			} else {
				if (ci != null) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(startId);
					if (c.get(Calendar.YEAR) != startTime.get(Calendar.YEAR) ||
							(resolution == ESPService.DAY_RESOLUTION && c.get(Calendar.DAY_OF_YEAR) != startTime.get(Calendar.DAY_OF_YEAR)) ||
							(resolution == ESPService.MONTH_RESOLUTION && c.get(Calendar.MONTH) != startTime.get(Calendar.MONTH)))
						ci = null;
				}
	
				for (Iterator<ContentInstance> iterator = cachedResult.getResult().iterator(); iterator.hasNext();) {
					ContentInstance cachedCi = (ContentInstance) iterator.next();
					if (cachedCi.getId() >= endId) {
						if (ci == null) {
							ci = cachedCi;
							resultList.add(ci);
						} else {
							addContentInstance(ci, cachedCi);
						}
					}
				}
			}
		}
		
		synchronized String getPrintableCacheData() {
			StringBuffer sb = new StringBuffer("\nCached energy cost data for ");
			sb.append(applianceId);
			sb.append("\n");
			EnergyCostInfo eci;
			for (Iterator<EnergyCostInfo> iterator = hourlyCachedData.iterator(); iterator.hasNext();) {
				eci = (EnergyCostInfo) iterator.next();
				sb.append(eci);
			}		
			return sb.toString();
		}
		
		synchronized void update() {	
			long execTimeMillis = System.currentTimeMillis();
			boolean logCachedData = false;
			synchronized (aeci) {
				if (aeci == null || !aeci.isValid()) {
					LOG.warn("Cache update called with an invalid or null energy cost info for appliance " + applianceId);
					return;
				}
				long aeciStartTime, aeciEndTime;
				int aeciStartHourOfDay, aeciStartDayOfYear, aeciStartYear;	
				int aeciEndHourOfDay, aeciEndDayOfYear, aeciEndYear;
				LOG.debug("Starting cache update for appliance " + applianceId);
				aeciStartTime = aeci.getStartTime();
				calendar.setTimeInMillis(aeciStartTime);
				aeciStartHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
				aeciStartDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
				aeciStartYear = calendar.get(Calendar.YEAR);
				aeciEndTime = aeci.getEndTime();
				calendar.setTimeInMillis(aeciEndTime);
				aeciEndHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
				aeciEndDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
				aeciEndYear = calendar.get(Calendar.YEAR);
				
				LOG.debug("Cache update for appliance " + applianceId + " - finished date/time initialization - elapsed time in millisec: " + (System.currentTimeMillis()-execTimeMillis));
				execTimeMillis = System.currentTimeMillis();			
	
				if (startTime == null) {
					startTime = Calendar.getInstance();
					endTime = Calendar.getInstance();
					startTime.setTimeInMillis(aeciStartTime);
					getNormalizedEndTime(endTime, aeciStartTime, ESPService.DAY_RESOLUTION);
				}
				
				if (aeciStartDayOfYear != aeciEndDayOfYear ||
						aeciStartYear != aeciEndYear) {
					getNormalizedStartTime(startTime, aeciEndTime, ESPService.DAY_RESOLUTION);
					getNormalizedEndTime(endTime, aeciEndTime, ESPService.DAY_RESOLUTION);
					hourlyCachedData.clear();
					aeci.copyAndReset();
					hourlyCachedData.add(aeci);
					LOG.info("New day - energy and cost cache reset " + applianceId);
					return;
				} 
	
				if (aeciStartHourOfDay != aeciEndHourOfDay) {
					EnergyCostInfo lastEci = aeci.copyAndReset();
					if (hourlyCachedData.size() == 0)
						hourlyCachedData.add(lastEci);
					else
						hourlyCachedData.set(hourlyCachedData.size()-1, lastEci);	
					hourlyCachedData.add(aeci);
					logCachedData = true;
					LOG.info("New hour - energy and cost cache update " + applianceId);
				} 
			}

			LOG.debug("Cache update for appliance " + applianceId + " - energy and cost cache update finished - elapsed time in millisec: " + (System.currentTimeMillis()-execTimeMillis));
			if (logCachedData) {
				LOG.debug(getPrintableCacheData());
			}	
		}
	}
	
	private Map<String, CachedData> cacheMap;
	
	public ESPGwTodayCache() {
		cacheMap = new ConcurrentHashMap<String, ESPGwTodayCache.CachedData>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);
	}
	
	public void add(String applianceId, EnergyCostInfo aeci) {
		cacheMap.put(applianceId, new CachedData(applianceId, aeci));
		LOG.debug("Added gw cache for appliance " + applianceId);
	}
	
	public void remove(String applianceId) {
		cacheMap.remove(applianceId);
		LOG.debug("Removed gw cache for appliance " + applianceId);
	}
	
	public void update(String applianceId) {
		CachedData cache = cacheMap.get(applianceId);
		if (cache == null) {
			LOG.warn("Update on gw cache called with an invalid appliance pid " + applianceId);
			return;
		}
		cache.update();
	}
	
	public CacheQueryResult getHourlyEnergyConsumptionResult(String applianceId, long startInstanceId, long endInstanceId) {	
		CachedData cache = cacheMap.get(applianceId);
		if (cache == null) {
			LOG.warn("getHourlyEnergyConsumptionResult called with an invalid appliance pid " + applianceId);
			return null;
		}
		if (endInstanceId < startInstanceId)
			return new CacheQueryResult(true, new ArrayList<ContentInstance>(0));
		return cache.getCachedData(startInstanceId, endInstanceId, ENERGY_DATA_TYPE);	
		
	}
	
	public CacheQueryResult getHourlyEnergyCostResult(String applianceId, long startInstanceId, long endInstanceId) {
		CachedData cache = cacheMap.get(applianceId);
		if (cache == null) {
			LOG.warn("getHourlyEnergyCostResult called with an invalid appliance pid " + applianceId);
			return null;
		}
		if (endInstanceId < startInstanceId)
			return new CacheQueryResult(true, new ArrayList<ContentInstance>(0));
		return cache.getCachedData(startInstanceId, endInstanceId, COST_DATA_TYPE);	
	}
	
	// resultList cannot be null
	public void merge(String applianceId, List<ContentInstance> resultList, CacheQueryResult cachedResult, int resolution) {
		if (cachedResult == null || cachedResult.getResult() == null)
			return;
		CachedData cache = cacheMap.get(applianceId);
		if (cache == null) {
			LOG.warn("merge called with an invalid appliance pid " + applianceId);
			return;
		}
		cache.mergeData(resultList, cachedResult, resolution);
	}
	
}
