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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.ebrain.ApplianceInfo;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;

// TODO: code needs to be reviewed (only week day cache is currently used)
public class ESPHapServiceCache {
	private static final Logger LOG = LoggerFactory.getLogger( ESPHapServiceCache.class );
	
	private static final long THIRTY_MINUTES_IN_MILLISEC = 30*60000L;
	private static final int ONE_WEEK_IN_HOUR = 168;

	private class QueryResult<T> {
		private long queryTime;
		Calendar queryCalendar;
		private T result;
		
		QueryResult(T items, long queryTime) {
			this.queryTime = queryTime;
			this.queryCalendar = Calendar.getInstance();
			this.queryCalendar.setTimeInMillis(queryTime);
			this.result = items;
		}
		
		T getResult() {
			return result;
		}
		
		long getQueryTime() {
			return queryTime;
		}
		
		Calendar getQueryCalendar() {
			return queryCalendar;
		}
	}
	
	private ESPHapServiceObject hapObject;	
	private Map<AHContainerAddress, QueryResult> cacheItemsMap = new HashMap<AHContainerAddress, QueryResult>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);
	private Map<AHContainerAddress, QueryResult> cacheItemsListMap = new HashMap<AHContainerAddress, QueryResult>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);
	private Map<AHContainerAddress, QueryResult> cacheWeekDayItemsMap = new HashMap<AHContainerAddress, QueryResult>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);
	private Map<AHContainerAddress, QueryResult> cacheHourlyProducedEnergyForecastItemsMap = new HashMap<AHContainerAddress, QueryResult>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);

	private static int getCurrentCacheResolution(Calendar calendar, AHContainerAddress containerId, long startTime, long endTime, int resolution) {
		int currentDay;
		int startDay;
		int endDay;
		int currentMonth;
		int startMonth;
		int endMonth;
		int currentYear;
		int startYear;
		int endYear;
		boolean useCache = false;
		int cacheLimitResolution = ESPService.NO_VALID_RESOLUTION;
		
		long now = System.currentTimeMillis();
		calendar.setTimeInMillis(now);
		LOG.debug("useCache method - start query interval check");

		switch (resolution) {
		case ESPService.HOUR_RESOLUTION:
			currentDay = calendar.get(Calendar.DAY_OF_YEAR);
			currentYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(startTime);
			startDay = calendar.get(Calendar.DAY_OF_YEAR);
			startYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(endTime);
			endDay = calendar.get(Calendar.DAY_OF_YEAR);
			endYear = calendar.get(Calendar.YEAR);		
			useCache =(currentDay == startDay && currentDay == endDay  && currentYear == startYear && currentYear == endYear);
			if (useCache)
				cacheLimitResolution = ESPService.DAY_RESOLUTION;
			break;
		case ESPService.DAY_RESOLUTION:
			currentMonth = calendar.get(Calendar.MONTH);
			currentYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(startTime);
			startMonth = calendar.get(Calendar.MONTH);
			startYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(endTime);
			endMonth = calendar.get(Calendar.MONTH);
			endYear = calendar.get(Calendar.YEAR);	
			useCache =(currentMonth == startMonth && currentMonth == endMonth  && currentYear == startYear && currentYear == endYear);
			if (useCache)
				cacheLimitResolution = ESPService.MONTH_RESOLUTION;
			break;
		case ESPService.MONTH_RESOLUTION:
			currentYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(startTime);
			startYear = calendar.get(Calendar.YEAR);
			calendar.setTimeInMillis(endTime);
			endYear = calendar.get(Calendar.YEAR);	
			useCache = (currentYear == startYear && currentYear == endYear);
			if (useCache)
				cacheLimitResolution = ESPService.YEAR_RESOLUTION;
			break;
		}
		
		LOG.debug("useCache method - end query interval check - millisec elapsed: " + (System.currentTimeMillis()- now));
		return cacheLimitResolution;
	}
	
	private static QueryResult getCachedQueryResult(Map<AHContainerAddress, QueryResult> cacheMap, Calendar calendar, AHContainerAddress containerId)  {
		QueryResult cache = cacheMap.get(containerId);
		if (cache != null) {
			long now = System.currentTimeMillis();
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			if (cache.getQueryCalendar().get(Calendar.HOUR_OF_DAY) != currentHour) {
				cache = null;
				cacheMap.put(containerId, null);
				LOG.debug("Invalidate cached query " + containerId);
			}
		}
		return cache;
	}
	
	private static ContentInstanceItems filterContentInstanceItems(ContentInstanceItems items, long startId, long endId) {
		if (items == null)
			return null;
		ContentInstanceItems result = new ContentInstanceItems();
		result.setAddressedId(items.getAddressedId());
		List<ContentInstance> itemList = items.getContentInstances();
		List<ContentInstance> resultList = result.getContentInstances();
		ContentInstance ci;
		for (Iterator<ContentInstance> iterator = itemList.iterator(); iterator.hasNext();) {
			ci = (ContentInstance) iterator.next();
			if (ci != null && ci.getId() >= startId && ci.getId() <= endId) {
				resultList.add(ci);
			}			
		}
		return result;
	}
	
	private static ContentInstanceItemsList filterContentInstanceItemsList(ContentInstanceItemsList itemsList, long startTime, long endTime) {
		if (itemsList == null)
			return null;
		ContentInstanceItemsList resultList = new ContentInstanceItemsList();
		List<ContentInstanceItems> cisResultList = resultList.getContentInstanceItems();
		List<ContentInstanceItems> cisList = itemsList.getContentInstanceItems();

		for (Iterator<ContentInstanceItems> iterator = cisList.iterator(); iterator.hasNext();) {
			ContentInstanceItems items = (ContentInstanceItems) iterator.next();
			cisResultList.add(filterContentInstanceItems(items, startTime, endTime));
			
		}
		return resultList;
	}
	
	public ESPHapServiceCache(ESPHapServiceObject hapObject) {
		this.hapObject = hapObject;
	}
	
	public ContentInstanceItems getCachedItems(Calendar calendar, AHContainerAddress containerId, long startTime, long endTime, int resolution) throws M2MHapException {
		int cacheResolution = getCurrentCacheResolution(calendar, containerId, startTime, endTime, resolution);
		if (cacheResolution == ESPService.NO_VALID_RESOLUTION)
			return null;			

		QueryResult<ContentInstanceItems> cache = getCachedQueryResult(cacheItemsMap, calendar, containerId);
		long now = System.currentTimeMillis();
		if (cache == null) {
			ContentInstanceItems items = hapObject.getNormalizedItems(calendar, containerId, 
					ESPHapServiceObject.getNormalizedStartTime(calendar, now, cacheResolution), 
					ESPHapServiceObject.getNormalizedEndTime(calendar, now, cacheResolution), 
					resolution);	
			if (items != null) {
				cache = new QueryResult<ContentInstanceItems>(items, System.currentTimeMillis());
				cacheItemsMap.put(containerId, cache);
				LOG.debug("Created local cache for container " + containerId + "\n:" + items.toXmlPrintableString());
			} 
		}
		if (cache == null)
			return null;
		return filterContentInstanceItems(cache.getResult(), startTime, endTime);
	}
	
	public ContentInstanceItemsList getCachedItemsList(Calendar calendar, AHContainerAddress containerId, long startTime, long endTime, int resolution) throws M2MHapException {
		int cacheResolution = getCurrentCacheResolution(calendar, containerId, startTime, endTime, resolution);
		if (cacheResolution == ESPService.NO_VALID_RESOLUTION)
			return null;			

		QueryResult<ContentInstanceItemsList> cache = getCachedQueryResult(cacheItemsListMap, calendar, containerId);
		long now = System.currentTimeMillis();
		long cacheStartTime = ESPHapServiceObject.getNormalizedStartTime(calendar, now, cacheResolution);
		long cacheEndTime = ESPHapServiceObject.getNormalizedEndTime(calendar, now, cacheResolution);
		if (cache == null) {
			ContentInstanceItemsList itemsList = hapObject.getNormalizedItemsList(calendar, containerId,  
					cacheStartTime, cacheEndTime, resolution);	
			if (itemsList != null) {
				cache = new QueryResult<ContentInstanceItemsList>(itemsList, System.currentTimeMillis());
				cacheItemsListMap.put(containerId, cache);
				LOG.debug("Created local cache for container " + containerId + "\n:" + itemsList.toXmlPrintableString());
			} 
		}
		if (cache == null || startTime < cacheStartTime || endTime > cacheEndTime)
			return null;
		return filterContentInstanceItemsList(cache.getResult(), startTime, endTime);
	}
	
	public ContentInstanceItems getWeekDayCachedItems(AHContainerAddress containerId, long startId, long endId) throws M2MHapException {
		QueryResult<ContentInstanceItems> cache = getCachedQueryResult(cacheWeekDayItemsMap, Calendar.getInstance(), containerId);		
		if (cache == null) {
			ContentInstanceItems items = hapObject.getWeekDayItems(containerId, 0, ONE_WEEK_IN_HOUR-1);
			if (items != null) {
				cache = new QueryResult<ContentInstanceItems>(items, System.currentTimeMillis());
				cacheWeekDayItemsMap.put(containerId, cache);
				LOG.debug("Created local cache for container " + containerId + "\n:" + items.toXmlPrintableString());
			} 
		}
		if (cache == null)
			return null;
		return filterContentInstanceItems(cache.getResult(), startId, endId);
	}
	
	public ContentInstanceItems getHourlyProducedEnergyForecastCachedItems(AHContainerAddress containerId) throws M2MHapException {
		QueryResult<ContentInstanceItems> cache = getCachedQueryResult(cacheHourlyProducedEnergyForecastItemsMap, Calendar.getInstance(), containerId);		
		if (cache == null) {
			ContentInstanceItems items = hapObject.getHourlyProducedEnergyForecast(containerId);
			if (items != null) {
				cache = new QueryResult<ContentInstanceItems>(items, System.currentTimeMillis());
				cacheHourlyProducedEnergyForecastItemsMap.put(containerId, cache);
				LOG.debug("Created local cache for container " + containerId + "\n:" + items.toXmlPrintableString());
			} 
		}
		if (cache == null)
			return null;
		return cache.getResult();
	}

}
