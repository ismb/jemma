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
package org.energy_home.jemma.ah.ebrain.algo;


import java.util.Calendar;

import org.energy_home.jemma.ah.ebrain.CalendarUtil;
import org.energy_home.jemma.ah.ebrain.EnergyCostInfo;
import org.energy_home.jemma.ah.ebrain.TwoTierDailyTariff;

/*
 * Daily Tariff for the week. It assumes that each profile is a segment that specifies a tariff
 * in KW/hour for a given start/end interval. Unit cost is expressed in hundredth-millesimals of euro,
 * Time units are 4-digit integer, the 2 highest are the hour and the 2 lowest are the minutes.
 * Each Daily profile will be an array of such interval profiles.
 */

public abstract class DailyTariff {

	private static DailyTariff instance;
	//private static List<DailyTariff> singletons = new ArrayList<DailyTariff>();
/*
	static Reference<DailyTariff> getWeakInstance() {
		return new WeakReference<DailyTariff>(instance);
	}
*/
	public static DailyTariff getInstance() throws InstantiationException, IllegalAccessException {
		if (instance == null) return getInstance(TwoTierDailyTariff.class);
		return instance;
	}
	
	public static DailyTariff getInstance(Class<? extends DailyTariff> clazz) throws InstantiationException, IllegalAccessException {
		if (instance == null || instance.getClass() != clazz) instance = clazz.newInstance();
		return instance;
	}
	
/*
	static <T extends DailyTariff> T getInstance2(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		for (DailyTariff instance : singletons) {
			if (instance.getClass() == clazz) return clazz.cast(instance);
		}
		log.debug("new tariff " + clazz.getSimpleName());
		T instance = clazz.newInstance();
		singletons.add(instance);
		return instance;
	}
*/
	// this matrix is a 7 rows, each for every day of the week, and every column are the
	// tariff intervals for that day.
	protected TariffIntervals[][] weekTariffIntervals = new TariffIntervals[7][];
	protected float[][] slotsWeekTariffs = new float[7][];
	
	
	protected DailyTariff() {}
	
	protected void setDailyTariff(TariffIntervals[] tariffIntervals, int dayOfWeek) throws Exception {
		// Calendar.DAY_OF_WEEK goes from 1 to 7
		// 1st check to see if we already have a 'normalized' slot daily-tariff, else make a new one;
		for (int i = 0; i < 7; ++i) {
			if (tariffIntervals.equals(weekTariffIntervals[i])) {
				slotsWeekTariffs[dayOfWeek -1] = slotsWeekTariffs[i];
				weekTariffIntervals[dayOfWeek -1] = weekTariffIntervals[i];
				return;
			}
		}
		
		// not found, then create a new slot array
		float[] slotTariff = new float[CalendarUtil.SLOTS_IN_ONE_DAY];
		slotsWeekTariffs[dayOfWeek -1] = slotTariff;
		weekTariffIntervals[dayOfWeek -1] = tariffIntervals;

		for (TariffIntervals ti : tariffIntervals) {
			// the tariff is expressed 'per hour', so divide by the number of slots in one hour
			int[] slots = ti.getSlotIntervals();
			for (int i = 0; i < slots.length; i +=2) {
				for (int j = slots[i]; j < slots[i+1]; slotTariff[j++] = ti.getTariff());
			}
		}
	}
	
	// get the daily tariff array considering holydays as sundays
	private float[] getSlotDailyTariffProfile(Calendar calendar) {
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek != Calendar.SUNDAY && CalendarUtil.isHolyday(calendar)) dayOfWeek = Calendar.SUNDAY;
		return slotsWeekTariffs[dayOfWeek -1];
	}
	
	// compute the cost of a 'flat' energy value in the specified duration interval
	// and also the minimum & maximum costs in the specified time range
	public EnergyCostInfo computeMinMaxCosts(Calendar calendar, long duration, double deltaEnergy) {
		float[] slotTariff = getSlotDailyTariffProfile(calendar);

		// NOTE: the tariff is Kwatt/hour, while the 'energy' parameter is in watt/hour
		// therefore the necessary division by 1000
		double energy = deltaEnergy * 0.001;
		int slotDuration = CalendarUtil.slotsFromMillis(duration);
		double oneSlotEnergy = energy / slotDuration;

		int tariffSlot = CalendarUtil.getSlotOf(calendar);
		double cost = 0;
		float min = Float.POSITIVE_INFINITY;
		float max = 0;
		while (slotDuration-- > 0) {
			cost += slotTariff[tariffSlot] * oneSlotEnergy;
			if (min > slotTariff[tariffSlot]) min = slotTariff[tariffSlot];
			if (max < slotTariff[tariffSlot]) max = slotTariff[tariffSlot];
			if (++tariffSlot >= slotTariff.length) {
    			// roll to the day after
    			tariffSlot = 0;
    			calendar.add(Calendar.DAY_OF_MONTH, 1);
    			slotTariff = getSlotDailyTariffProfile(calendar);
    		}
		}

		min *= energy;
		max *= energy;
		return new EnergyCostInfo((float)cost, min, max, deltaEnergy);
	}
	
	
	// compute the cost using the energy allocated in the buffer. The boolean flag specifies
	// whether the buffer is time relative to the Calendar object or absolute, i.e. if the
	// zero slot of the buffer is the calendar , or zero is the time 00:00
	public float computeCost(Calendar calendar, float[] energyAllocation) {
		// Initialize slotTariff to the current Daily Tariff Profile
		float[] slotTariff = getSlotDailyTariffProfile(calendar);
		int tariffSlot = CalendarUtil.getSlotOf(calendar);
		
		int startSlot = tariffSlot;
		int endSlot = energyAllocation.length;
		while (energyAllocation[--endSlot] == 0 && endSlot > startSlot);
		
		double cost = 0;
		int addedDays = 0;
		while (startSlot <= endSlot) {
			cost +=	slotTariff[tariffSlot++] * energyAllocation[startSlot++];
			if (tariffSlot >= slotTariff.length) {
				// roll to the next day
				tariffSlot = 0;
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				++addedDays;
				slotTariff = getSlotDailyTariffProfile(calendar);
			}
		}
		// before returning restore the calendar
		calendar.add(Calendar.DAY_OF_MONTH, -addedDays);
		// NOTE: the tariff is Kwatt/hour, while the 'energy' parameter is in watt/hour
		// therefore the necessary division by 1000
		return (float)(cost * 0.001);
	}
	
	/*
	public float computeCost1(Calendar calendar, float[] energyAllocation) {
		return computeCost1(calendar, energyAllocation, true);
	}
	public float computeCost1(Calendar calendar, float[] energyAllocation, boolean absoluteTimeAllocation) {
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) -1;
		int tariffSlot = CalendarUtil.getSlotOf(calendar);
		int startSlot = absoluteTimeAllocation ? tariffSlot : 0;
		int endSlot = energyAllocation.length;
		while (energyAllocation[--endSlot] == 0);

		TariffIntervals[] tariffIntervals = weekTariffIntervals[dayOfWeek];
		
		double cost = 0;
		while (startSlot <= endSlot) {
    		// find the tariff interval that includes this time slot
    		for (int i = 0; i < tariffIntervals.length; ++i) {
    			int[] slotBounds = tariffIntervals[i].getSlotIntervals();
    			float tariff = tariffIntervals[i].getTariff();
    			for (int t = 0; t < slotBounds.length; t += 2) {
    				while (tariffSlot >= slotBounds[t] && tariffSlot < slotBounds[t+1]) {
    					// interval found, now get the intersection
    					cost += tariff * energyAllocation[startSlot++];
    					if (++tariffSlot >= CalendarUtil.DAILY_TIME_SLOTS) {
    		    			// roll to the next day
    		    			tariffIntervals = weekTariffIntervals[++dayOfWeek %7];
    		    			tariffSlot -= CalendarUtil.DAILY_TIME_SLOTS;
    		    		}
    				}
    			}		
    		}
		}
		// the tariff is expressed 'per hour', so divide by the number of slots in one hour
		return (float)cost;// * CalendarUtil.SLOT_TARIFF_COEFFICIENT;
	}
	*/
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int day = 0; day < 7; ++day) {
			sb.append("\nDay ").append(day);
			for (int i = 0; i < weekTariffIntervals[day].length; ++i) {
				int [] bounds = weekTariffIntervals[day][i].getTimeIntervals();
				float cost = weekTariffIntervals[day][i].getTariff();
				sb.append("\n").append(cost).append(" @ ");
				for (int j = 0; j < bounds.length; j+=2) {
					sb.append("[").append(bounds[j]).append(',');
					sb.append(bounds[j+1]).append(']');
				}
			}
		}
		return sb.toString();
	}


	
	public static class TariffIntervals {
    	private float tariff;
    	private int[] timeIntervals;
    	private int[] slotIntervals;
    	
		public TariffIntervals(float tariff, int[] intervals) {
			// make sure the time intervals are even (each start needs an end)
			if ((intervals.length % 2) != 0) throw new IllegalArgumentException("Uneven time intervals.");
    		this.tariff = tariff;
    		this.timeIntervals = intervals;
    		slotIntervals = new int[intervals.length];
    		
    		for (int j = 0; j < intervals.length; ++j) {
    			// get the hours from the coded time
    			int hours = intervals[j] / 100;
    			// get the minutes
    			int minutes = intervals[j] % 100;
    			slotIntervals[j] = CalendarUtil.getSlotOf(hours, minutes);
			}
  
    	}
		
		public boolean equals(Object o) {
			if (o == this) return true;
	        if (o == null || o.getClass() != this.getClass()) return false;
	        TariffIntervals other = (TariffIntervals)o;
	        if (tariff != other.getTariff()) return false;
	        int[] otherIntervals = other.getTimeIntervals();
	        if (timeIntervals.length != otherIntervals.length) return false;
	        for (int i = 0; i < timeIntervals.length; ++i)
	        	if (timeIntervals[i] != otherIntervals[i]) return false;
	        return true;
		}
		
    	public float getTariff() {
    		return tariff;
    	}
    	
		public int[] getSlotIntervals() {
			return slotIntervals;
		}
   
    	public int[] getTimeIntervals() {
    		return timeIntervals;
    	}
	}
}
