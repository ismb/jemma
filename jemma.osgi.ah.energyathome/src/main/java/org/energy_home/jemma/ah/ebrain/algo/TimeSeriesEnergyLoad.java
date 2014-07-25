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

public class TimeSeriesEnergyLoad {
	public static int MINUTES_IN_ONE_SLOT = 10; //  accumulate for 10' intervals
	public static int MILLISECS_IN_ONE_SLOT = MINUTES_IN_ONE_SLOT * 60 * 1000;
	public static int DAILY_SLOTS = 24 * 60 / MINUTES_IN_ONE_SLOT;

	
	private double[] timeSeries;
	private int maxDays, midnightSlot, completeDays, currentSlot;
	private Calendar currentTime;
	
	public TimeSeriesEnergyLoad(int days) {
		if (days < 2) throw new IllegalArgumentException("Needs at least two days of accumulated data.");
		
		maxDays = days;
		midnightSlot = (days -1) * DAILY_SLOTS;
		timeSeries = new double[days * DAILY_SLOTS];
		currentTime = Calendar.getInstance();
	}
	
	public int getDailySlots() {return DAILY_SLOTS;}
	
	
	
	public void addEnergyDelta(long time, double deltaEnergy) {
		currentTime.setTimeInMillis(time);
		int slot = 24 * currentTime.get(Calendar.HOUR_OF_DAY) + currentTime.get(Calendar.MINUTE);
		slot /= MINUTES_IN_ONE_SLOT;
		
		// mybe trigger a recomputation of the forecast
		boolean differentSlot = currentSlot != slot;
		slot += midnightSlot;
		if (slot >= timeSeries.length) {
			slideOneDayBack();
			slot -= DAILY_SLOTS;
		}
		timeSeries[slot] += deltaEnergy;
	}
	
	private void slideOneDayBack() {
		System.arraycopy(timeSeries, DAILY_SLOTS, timeSeries, 0, timeSeries.length - DAILY_SLOTS);
		if (completeDays < maxDays -1) ++completeDays;
		for (int i = timeSeries.length - DAILY_SLOTS; i < timeSeries.length; timeSeries[i++] = 0);
	}
	
	public int size() {
		return completeDays * DAILY_SLOTS + currentSlot;
	}
	
	// should be synchronized?
	public double[] toArray() {
		if (size() == 0) return null;
		double[] copySeries = new double[size()];
		int offset = (maxDays - completeDays -1) * DAILY_SLOTS;
		System.arraycopy(timeSeries, offset, copySeries, 0, copySeries.length);
		return copySeries;
	}
	
	public void setRandomLoad() {
		timeSeries[0] = 2000;
		int sign = 1;
		
		completeDays = maxDays - 1;
		currentSlot = 120;
		
		for (int i = 1; i < DAILY_SLOTS; ++i) {
			if (Math.random() > 0.7) {
				timeSeries[i] = 1;
			} else {
    			double val = 20 * Math.random();
    			if (i % 3 == 0) sign = Math.random() > 0.5 ? -1 : 1;
    			timeSeries[i] = Math.abs((sign * val) + timeSeries[i-1]);
			}
		}
		for (int j = 1; j < maxDays; ++j) {
    		for (int i = 0; i < DAILY_SLOTS; ++i) {
        			double val = -100 + 200 * Math.random();
        			double average = timeSeries[i + (j-1)*DAILY_SLOTS];
        			if (i > 0) average = (average + timeSeries[(i-1) + j*DAILY_SLOTS])/2;
        			timeSeries[i + j*DAILY_SLOTS] = Math.abs(average + val);
    		}
		}
	}
	
	
	public static void main(String args[]) {
		try {
			TimeSeriesEnergyLoad tsel =  new TimeSeriesEnergyLoad(7);
			tsel.setRandomLoad();
			double[] data = tsel.toArray();
			for (int i = 0; i < data.length; ++i) {
				System.out.println(i + " " + data[i]);
			}
			System.exit(0);
			
			for (int i = 0; i < 3000; ++i) {
				double val = 200 * Math.random();
				tsel.addEnergyDelta(System.currentTimeMillis(), val);
				Thread.sleep(1000);
			}
			
			//double[]
					data = tsel.toArray();
			for (int i = 0; i < data.length; ++i) {
				System.out.println(i + " " + data[i]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}