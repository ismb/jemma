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
import java.util.List;

import org.energy_home.jemma.ah.ebrain.CalendarUtil;

public class SolarIrradianceProfile {
	
	public static int MINIMUM_INTERPOLATION_HOURS = 4;
	
	public static float cosineInterpolate(float y1, float y2, float mu) {
	    float mu2 = (float)(1.0 - Math.cos(mu * Math.PI)) * 0.5f;
	    return (y1 * (1.0f - mu2)) + (y2 * mu2);
	}

	public static float linearInterpolate(float y1, float y2, float mu) {
		return y1 * (1 - mu) + y2 * mu ;
	}


	public static float[] interpolate(List<Float> hourlyData) {
		if (hourlyData == null) throw new IllegalArgumentException("The hourly data cannot be null.");
		if (hourlyData.size() < MINIMUM_INTERPOLATION_HOURS) throw new IllegalArgumentException("The hourly data must contain at least " + MINIMUM_INTERPOLATION_HOURS);
	
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		int slot = CalendarUtil.getSlotOf(c);

		float energyForecast[] = EnergyAllocator.newEnergyAllocation();

		int finalIndex = Math.min(hourlyData.size() -1, 23);
		while (finalIndex > MINIMUM_INTERPOLATION_HOURS && hourlyData.get(finalIndex) == null) --finalIndex;
		while (finalIndex > MINIMUM_INTERPOLATION_HOURS && hourlyData.get(finalIndex) == 0) --finalIndex;
		
		if (hourlyData.get(finalIndex) == null) hourlyData.set(finalIndex, 0.0f);

		// normalize to minute
		for (int i = finalIndex; i >=0; --i) {
			Float value = hourlyData.get(i);
			if (value != null && value > 0) hourlyData.set(i, value * CalendarUtil.HOURS_IN_ONE_SLOT);
		}

		
		int i = 0;
		while (i < finalIndex) {
			int steps = 0;
			
			Float y1 = hourlyData.get(i);
			Float y2;
			
			do {
				++i;
				y2 = hourlyData.get(i);
				steps += CalendarUtil.SLOTS_IN_ONE_HOUR;
			
			} while (y2 == null);
			
			
			for (float j = 0; j < steps; ++j) {
				energyForecast[slot++] = cosineInterpolate(y1, y2, j / steps);
			}
		}
		
		return energyForecast;
	}
	
	
	
	public static enum SkyCover {
		Dark, ClearSky, Overcast, PartlyClouded
	}
	
	
	
	private SkyCover skyCover;
	private float oneSlotMaxEnergy;
	private float[] series;
	private int numDays;
	
	
	public SkyCover getSkyCover() {
		return skyCover;
	}
	public float[] getSeries() {
		return series;
	}
	public int getNumDays() {
		return numDays;
	}
	
	
	public SolarIrradianceProfile(float maxPower) {
		this(maxPower, 1, SkyCover.ClearSky);
	}
	public SolarIrradianceProfile(float maxPower, int days) {
		this(maxPower, days, SkyCover.ClearSky);
	}
	public SolarIrradianceProfile(float maxPower, int days, SkyCover sky) {
		numDays = days;
		skyCover = sky;
		series = new float[days * CalendarUtil.SLOTS_IN_ONE_DAY];
		oneSlotMaxEnergy = maxPower * CalendarUtil.HOURS_IN_ONE_SLOT;
		
		initializeClearSky();
		if (sky == SkyCover.Overcast) initializeOvercast();
		if (sky == SkyCover.PartlyClouded) initializePartlyClouded();
	}
	
	private void initializeClearSky() {
		// suppose 8 hours light form 08:00 to 20:00
		// suppose max ideal output is 2000 Watt/Hour
		// cosine function is from slot 48 to slot 120
		
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR_OF_DAY, 8);
		
		Calendar end = Calendar.getInstance();
		end.set(Calendar.HOUR_OF_DAY, 20);
		
		int interval = CalendarUtil.getSlotInterval(start, end);
		int startSlot = CalendarUtil.getSlotOf(start);
		int endSlot = CalendarUtil.getSlotOf(end);
		int s = -interval / 2;
		
		for (int i = startSlot; i <= endSlot; i++) {
			series[i] = oneSlotMaxEnergy * (float)Math.pow(Math.cos(Math.PI * s++ / interval), 1.5);
		}
		
		for (int d = 1; d < numDays; ++d) {
			System.arraycopy(series, 0, series, d * CalendarUtil.SLOTS_IN_ONE_DAY, CalendarUtil.SLOTS_IN_ONE_DAY);
		}
	}
	
	
	private void initializeOvercast() {
		for (int i = series.length; --i >= 0;) {
			if (series[i] <= 0) series[i] = 0;
			else series[i] = (float)Math.pow(series[i], 0.6);
		}
	}
	
	private void initializePartlyClouded() {
		double r = Math.random();
		for (int i = series.length; --i >= 0;) {
			if (i % 3 == 0) r = 0.1 + 0.9 * Math.random();
			series[i] *= r;
		}
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < CalendarUtil.SLOTS_IN_ONE_DAY; ++i) {
			sb.append(series[i]).append(',');
			if ((i % CalendarUtil.SLOTS_IN_ONE_HOUR) == 0) sb.append('\n');
		}
		return sb.toString();
	}
}
	

