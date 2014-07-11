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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.EHContainers;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ah.FloatCDV;
import org.energy_home.jemma.m2m.ah.FloatDV;
import org.energy_home.jemma.utils.datetime.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESPContainersDataUtils {
	private static final Logger LOG = LoggerFactory.getLogger( ESPContainersDataUtils.class );
	
	public static final long FIVE_MINUTES_IN_MILLISEC = 5*60000l; 
	
	public static final long ONE_HOUR_IN_MILLISEC = 3600000l;
	
	public static final long ONE_DAY_IN_MILLISEC = 86400000l;
	
	public static final float DURATION_ERROR_TOLERANCE = 0.30f;
	
	public static int addIntermediateItems(Calendar c, long previousTime, long nextTime, ContentInstanceItems resultItems, int resolution, int lastAddedItems) {		
		List<ContentInstance> resultList = resultItems.getContentInstances();
		long normalizedStartTime = getNormalizedStartTime(c, previousTime, resolution);
		int startDst = c.get(Calendar.DST_OFFSET);
		long normalizedEndTime = getNormalizedEndTime(c, nextTime, resolution);
//		if (normalizedEndTime-nextTime <= 1000) {
//			// This hack is necessary to manage some of the hap server approximation errors
//			normalizedEndTime = getNormalizedEndTime(c, nextTime+1000, resolution);
//			LOG.warn("Hap millisecond approximation error");
//		}
		int endDst = c.get(Calendar.DST_OFFSET);
		
		int itemsToBeAdded = getResultListSize(c, normalizedStartTime, normalizedEndTime, resolution);
		itemsToBeAdded = itemsToBeAdded - 2;
		if (lastAddedItems < 0 && itemsToBeAdded + lastAddedItems >= 0) {
			// This hack is necessary to manage some of the hap server approximation on milliseconds
			itemsToBeAdded = itemsToBeAdded + lastAddedItems;
			LOG.warn("getNextItemsToAdd negative value compensated");
		}
		int itemsAdded = 0;
		if (itemsToBeAdded < 0) {
			LOG.warn("getNextItemsToAdd returned a negative value: " + itemsToBeAdded);
			itemsAdded = itemsToBeAdded;
		} else if (itemsToBeAdded > 0) {
			if (resolution == ESPService.HOUR_RESOLUTION && endDst > startDst) {
				normalizedStartTime += 1;
				while (itemsAdded < itemsToBeAdded) {		
					normalizedStartTime += ONE_HOUR_IN_MILLISEC;
					c.setTimeInMillis(normalizedStartTime);
					startDst = c.get(Calendar.DST_OFFSET);
					if (startDst == endDst) {		
						resultList.add(getDefaultContentInstance(AHContainerAddress.getAddressFromUrl(resultItems.getAddressedId()).getContainerName(), normalizedStartTime)); 
						itemsAdded++;
						break;
					} else {
						resultList.add(null);
						itemsAdded++;
					}
				}			
			}	
			while (itemsAdded < itemsToBeAdded) {	
				resultList.add(null);
				itemsAdded++;				
			}
		}
		return itemsAdded;
	}

	public static int getHourlyDayOfWeekStartIndex(int dayOfWeek) {
		return 24 * (dayOfWeek - 1);
	}

	public static int getHourlyDayOfWeekEndIndex(int dayOfWeek) {
		return 23 + 24 * (dayOfWeek - 1);
	}

	public static long getNormalizedStartTime(Calendar c, long startTime, int resolution) {
		c.setTimeInMillis(startTime);
		c.set(Calendar.MILLISECOND, 0);
		switch (resolution) {
		case ESPService.YEAR_RESOLUTION:
			c.set(Calendar.MONTH, 0);
		case ESPService.MONTH_RESOLUTION:
			c.set(Calendar.DAY_OF_MONTH, 1);
		case ESPService.DAY_RESOLUTION:
			c.set(Calendar.HOUR_OF_DAY, 0);
		case ESPService.HOUR_RESOLUTION:
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
		}
		long time = c.getTimeInMillis();
		return time;
	}

	public static long getNormalizedEndTime(Calendar c, long endTime, int resolution) {
		c.setTimeInMillis(endTime);
		c.set(Calendar.MILLISECOND, 999);
		switch (resolution) {
		case ESPService.YEAR_RESOLUTION:
			c.set(Calendar.MONTH, 11);
		case ESPService.MONTH_RESOLUTION:
			c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		case ESPService.DAY_RESOLUTION:
			c.set(Calendar.HOUR_OF_DAY, 23);
		case ESPService.HOUR_RESOLUTION:
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
		}
		long time = c.getTimeInMillis();
		return time;
	}

	public static int getResultListSize(Calendar c, long normalizedStartTime, long normalizedEndTime, int resolution) {
		int result = 0;
		long additionalHours = 0;
		int startDst, endDst;

		switch (resolution) {
		case ESPService.MONTH_RESOLUTION:
			c.setTimeInMillis(normalizedStartTime);
			int startYear = c.get(Calendar.YEAR);
			int startMonth = c.get(Calendar.MONTH);
			c.setTimeInMillis(normalizedEndTime);
			int endYear = c.get(Calendar.YEAR);
			int endMonth = c.get(Calendar.MONTH);
			if (startYear == endYear) {
				result = endMonth - startMonth + 1;
			} else if (endYear > startYear) {
				result = (endYear - startYear - 1) * 12 + (12 - startMonth) + endMonth + 1;
			} else
				result = 0;
			break;
		case ESPService.DAY_RESOLUTION:
			c.setTimeInMillis(normalizedStartTime);
			startDst = c.get(Calendar.DST_OFFSET);
			c.setTimeInMillis(normalizedEndTime);
			endDst = c.get(Calendar.DST_OFFSET);
			additionalHours = endDst - startDst;
			if (additionalHours != 0)
				LOG.debug("Legal/solar switch error correction: " + additionalHours);
			result = (int) ((normalizedEndTime - normalizedStartTime + additionalHours) / DateUtils.MILLISEC_IN_ONE_DAY) + 1;
			break;
		case ESPService.HOUR_RESOLUTION:
			c.setTimeInMillis(normalizedStartTime);
			startDst = c.get(Calendar.DST_OFFSET);
			c.setTimeInMillis(normalizedEndTime);
			endDst = c.get(Calendar.DST_OFFSET);
			additionalHours = endDst - startDst;
			if (additionalHours != 0)
				LOG.debug("Legal/solar switch error correction: " + additionalHours);
			result = (int) ((normalizedEndTime - normalizedStartTime + additionalHours) / DateUtils.MILLISEC_IN_ONE_HOUR) + 1;
			break;
		}
		if (result < 0) {
			result = 0;
		}
		
		if (result > 50) {
			LOG.warn("getResultListSize return more than 50 results");
		}
		return result;
	}

	public static boolean checkDuration(Calendar c, long startTime, long duration, int resolution) {
		// TODO: check for a more efficient implementation
		long now = System.currentTimeMillis();
		long endTime;
		
		if (startTime+duration >= now)
			// Test added to manage produced energy forecast (it is assumed that hourly forecast data are already normalized)
			return true;

		Long initialConfigurationTime = ESPConfiguration.getInitialConfigurationTime();
		long initialTime = initialConfigurationTime != null ? initialConfigurationTime.longValue() : DateUtils.DEFAULT_INITIAL_TIME;

		// Partial values min error limit are calculated using initialTime for
		// installation hour, day and month
		// Partial values max error limit are calculated using now for current
		// hour, day and month
		switch (resolution) {
		case ESPService.MONTH_RESOLUTION:
			startTime = getNormalizedStartTime(c, startTime, ESPService.MONTH_RESOLUTION);
			endTime = getNormalizedEndTime(c, startTime, ESPService.MONTH_RESOLUTION);
			break;
		case ESPService.DAY_RESOLUTION:
			startTime = getNormalizedStartTime(c, startTime, ESPService.DAY_RESOLUTION);
			endTime = getNormalizedEndTime(c, startTime, ESPService.DAY_RESOLUTION);
			break;
		case ESPService.HOUR_RESOLUTION:
			startTime = getNormalizedStartTime(c, startTime, ESPService.HOUR_RESOLUTION);
			endTime = getNormalizedEndTime(c, startTime, ESPService.HOUR_RESOLUTION);
			break;
		default:
			return false;
		}
		startTime = Math.max(startTime, initialTime);
		endTime = Math.min(now, endTime);
		long maxExpectedDuration = endTime - startTime + 1;
		if (endTime == now)
			endTime = now-FIVE_MINUTES_IN_MILLISEC;
		long minExpectedDuration = endTime - startTime + 1;
		if (minExpectedDuration < 0)
			minExpectedDuration = 0; 
		boolean result = duration >= minExpectedDuration * (1 - DURATION_ERROR_TOLERANCE)
				&& duration <= maxExpectedDuration * (1 + DURATION_ERROR_TOLERANCE);
		// Check for DST change
		if (!result && resolution == ESPService.HOUR_RESOLUTION) {
			c.setTimeInMillis(startTime-1);
			int startDst = c.get(Calendar.DST_OFFSET);
			c.setTimeInMillis(endTime+1);
			int endDst = c.get(Calendar.DST_OFFSET);
			int dstOffset = endDst - startDst;
			// TODO: patch to be reviewed when hap server is modified (correct test should be dstOffset < 0) 
			if (dstOffset != 0) {
				maxExpectedDuration = maxExpectedDuration + Math.abs(dstOffset);
				result = duration >= minExpectedDuration * (1 - DURATION_ERROR_TOLERANCE)
					&& duration <= maxExpectedDuration * (1 + DURATION_ERROR_TOLERANCE);
			}
		}
		return result;
	}

	public static ContentInstance getDefaultContentInstance(String attributeName, long id) {
		ContentInstance ci = null;
		try {
			Class clazz = EHContainers.getAttributeIdClass(attributeName);
			FloatDV content = (FloatDV) clazz.newInstance();
			content.setValue(0f);
			content.setDuration(0);
			ci = new ContentInstance();
			ci.setContent(content);
			ci.setCreationTime(System.currentTimeMillis());
			ci.setId(id);
		} catch (Exception e) {
			LOG.error("Exception on getDefaultContentInstance", e);
		} 
		return ci;
	}
	
	public static boolean checkDeltaEnergyMaxValue(EnergyCostInfo eci) {
		return (eci.getDeltaEnergy()/eci.getDuration()*ONE_HOUR_IN_MILLISEC) < ESPApplication.MAX_HOURLY_DELTA_ENERGY;
	}
	
	public static FloatCDV getCostCDV(EnergyCostInfo eci) {
		FloatCDV cost = new FloatCDV();	
		if (checkDeltaEnergyMaxValue(eci))
			cost.setValue(new Float(eci.getCost()));
		else 
			return null;
		cost.setDuration(eci.getDuration());
		if (eci.getMinCost() != eci.getMaxCost()) {
			cost.setMin(new Float(eci.getMinCost()));
			cost.setMax(new Float(eci.getMaxCost()));
		}
		return cost;
	}
	
	public static FloatDV getEnergyDV(EnergyCostInfo eci) {
		FloatDV energy = new FloatDV();
		if (checkDeltaEnergyMaxValue(eci))
			energy.setValue(new Float(eci.getDeltaEnergy()));
		else 
			return null;
		energy.setDuration(eci.getDuration());
		return energy;
	}
	
	public static FloatDV toFloatDV(ContentInstance ci) {
		if (ci == null)
			return null;
		return (FloatDV) ci.getContent();
	}
	
	public static Float toFloat(ContentInstance ci) {
		FloatDV floatDV = toFloatDV(ci);
		if (floatDV == null)
			return null;
		return floatDV.getValue();
	}
	
	public static List<Float> toFloatValueList(ContentInstanceItems items) {
		if (items == null)
			return null;

		List<ContentInstance> itemList = items.getContentInstances();
		if (itemList == null)
			return null;
		List<Float> resultList = new ArrayList<Float>(itemList.size());
		ContentInstance contentInstance = null;
		for (Iterator<ContentInstance> iterator = itemList.iterator(); iterator.hasNext();) {
			contentInstance = (ContentInstance) iterator.next();
			resultList.add(toFloat(contentInstance));
		}
		return resultList;
	}	
	
	public static void addFloatDV(FloatDV result, FloatDV addendum) {	
		result.setDuration(result.getDuration()+addendum.getDuration());
		Float f1 = result.getValue();
		Float f2 = addendum.getValue();
		if (f1 == null && f2 != null)
			result.setValue(f2);
		else if (f1 != null && f2 != null)
			result.setValue(new Float(f1.floatValue()+f2.floatValue()));
	}
	
	public static void addFloatCDV(FloatCDV result, FloatCDV addendum) {	
		addFloatDV(result, addendum);
		Float f1 = result.getMin();
		Float f2 = addendum.getMin();
		if (f1 == null && f2 != null)
			result.setMin(f2);
		else if (f1 != null && f2 != null)
			result.setMin(new Float(f1.floatValue()+f2.floatValue()));
		f1 = result.getMax();
		f2 = addendum.getMax();
		if (f1 == null && f2 != null)
			result.setMax(f2);
		else if (f1 != null && f2 != null)
			result.setMax(new Float(f1.floatValue()+f2.floatValue()));
	}
	
	public static void addContentInstance(ContentInstance result, ContentInstance addendum) {
		if (addendum == null)
			return;
		Object content = result.getContent();
		if (content instanceof FloatCDV)
			addFloatCDV((FloatCDV)content, (FloatCDV)addendum.getContent());
		else if (content instanceof FloatDV)
			addFloatDV((FloatDV)content, (FloatDV)addendum.getContent());
	}
	
	public static Map<String, List<Float>> toFloatValueListMap(ContentInstanceItemsList itemsList) throws M2MHapException {
		Map<String, List<Float>> result = null;
		if (itemsList != null) {
			result = new HashMap<String, List<Float>>(itemsList.getContentInstanceItems().size());
			ContentInstanceItems items = null;
			AHContainerAddress containerId = null;
			for (Iterator<ContentInstanceItems> iterator = itemsList.getContentInstanceItems().iterator(); iterator.hasNext();) {
				items = iterator.next();
				containerId = AHContainerAddress.getAddressFromUrl(items.getAddressedId());
				result.put(ESPApplication.getApplianceId(containerId), toFloatValueList(items));
			}
		}
		return result;
	}
	
	public static ContentInstanceItems getNormalizedItems(ContentInstanceItems items, Calendar c, long normalizedStartTime, long normalizedEndTime,
			int resolution) throws M2MHapException {
		if (items == null)
			return null;
		ContentInstanceItems result = new ContentInstanceItems();
		List<ContentInstance> resultList = result.getContentInstances();
		result.setAddressedId(items.getAddressedId());
		// TODO: check if it is possible to force the original list to be a linked list
		List<ContentInstance> itemList = items.getContentInstances();
		int resultSize = getResultListSize(c, normalizedStartTime, normalizedEndTime, resolution);
		int lastAddedItems = 0;
		if (resultSize > 0) {
			boolean additionalItemsRequired = resultSize > itemList.size();
			long previousTime = normalizedStartTime - DateUtils.MILLISEC_IN_ONE_HOUR;
			for (Iterator<ContentInstance> iterator = itemList.iterator(); iterator.hasNext();) {
				ContentInstance contentInstance = (ContentInstance) iterator.next();
				if (additionalItemsRequired) {
					lastAddedItems = addIntermediateItems(c, previousTime, contentInstance.getId().longValue(), result, resolution, lastAddedItems);
				}
				previousTime = contentInstance.getId().longValue();
				FloatDV content = (FloatDV) contentInstance.getContent();
				if (content != null && checkDuration(c, contentInstance.getId().longValue(), content.getDuration(), resolution))
					resultList.add(contentInstance);
				else {
					LOG.debug("getNormalizedItems - Added null item,  invalid duration: value=" + content.getValue() +  							", startTime=" + contentInstance.getId().longValue() + ", duration=" + content.getDuration());
					resultList.add(null);
				}

			}
			if (resultList.size() < resultSize)
				addIntermediateItems(c, previousTime, normalizedEndTime+1, result, resolution, lastAddedItems);
		}
		return result;
	}
	
	public static ContentInstanceItemsList getNormalizedItemsList(ContentInstanceItemsList itemsList, Calendar c, long normalizedStartTime, long normalizedEndTime,
			int resolution) throws M2MHapException {
		if (itemsList == null)
			return null;
		// TODO: add filter for deleted appliances 
		ContentInstanceItemsList result = new ContentInstanceItemsList();
		List<ContentInstanceItems> list = itemsList.getContentInstanceItems();
		List<ContentInstanceItems> resultList = result.getContentInstanceItems();
		ContentInstanceItems items = null;
		for (Iterator<ContentInstanceItems> iterator = list.iterator(); iterator.hasNext();) {
			items = (ContentInstanceItems) iterator.next();
			resultList.add(getNormalizedItems(items, c, normalizedStartTime, normalizedEndTime, resolution));			
		}		
		return result;
	}

	public static ContentInstanceItems getNormalizedWeekDayItems(ContentInstanceItems items, long startInstanceId, long endInstanceId) {
		ContentInstanceItems result = new ContentInstanceItems();
		result.setAddressedId(items.getAddressedId());
		List<ContentInstance> resultList = result.getContentInstances();
		long id = startInstanceId-1;
		if (items != null) {
			List<ContentInstance> itemList = items.getContentInstances();
			if (itemList != null) {
				ContentInstance contentInstance = null;
				long previousId = id;
				for (Iterator<ContentInstance> iterator = itemList.iterator(); iterator.hasNext();) {
					contentInstance = (ContentInstance) iterator.next();
					id = contentInstance.getId().longValue();
					while (previousId < id - 1) {
						resultList.add(null);
						previousId++;
					}
					resultList.add(contentInstance);
					previousId = id;
				}
			}
		}
		while (id < endInstanceId) {
			resultList.add(null);
			id++;
		}
		return result;
	}	
	
}
