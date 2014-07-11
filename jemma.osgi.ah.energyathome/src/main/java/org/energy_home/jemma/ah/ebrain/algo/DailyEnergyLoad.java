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

public class DailyEnergyLoad {
	
	public static int MINUTES_IN_ONE_SLOT = 10; // accumulate for 10' intervals
	public static int SECONDS_IN_ONE_SLOT = MINUTES_IN_ONE_SLOT * 60;
	public static int MILLISECS_IN_ONE_SLOT = MINUTES_IN_ONE_SLOT * 60 * 1000;
	public static int DAILY_SLOTS = 24 * 60 / MINUTES_IN_ONE_SLOT;

	private double[] realData, forecastData, smoothedLevel, smoothedSeason;
	private int lastSlot, updates;
	private Calendar calendar;
	
	// The best  results were obtained  with the smoothing constants alpha=0.7 beta=0 (no trend) and gamma=0.3
	private double alpha = 0.3, gamma = 0.7;

	public DailyEnergyLoad() {
		realData = new double[DAILY_SLOTS];
		forecastData = new double[DAILY_SLOTS];
		smoothedLevel = new double[DAILY_SLOTS];
		smoothedSeason = new double[DAILY_SLOTS];
		calendar = Calendar.getInstance();
	}
	
	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}


	public void addEnergyDelta(long startTime, long endTime, double deltaEnergy) {
		calendar.setTimeInMillis(startTime);
		int startSecs = CalendarUtil.getSecondsSinceMidnight(calendar);
		int startSlot = startSecs / SECONDS_IN_ONE_SLOT;
		
		calendar.setTimeInMillis(endTime);
		int endSlot = CalendarUtil.getMinutesSinceMidnight(calendar) / MINUTES_IN_ONE_SLOT;
		
		long deltaSlot = (endTime - startTime) / MILLISECS_IN_ONE_SLOT;
		
		if (deltaSlot == 0 && startSlot == endSlot) {
			realData[startSlot] += deltaEnergy;
		} else {
			if (deltaSlot == 0) {
				int startPortion = SECONDS_IN_ONE_SLOT * (startSlot +1) - startSecs;
				long s = (endTime - startTime) / 1000;
				double energyStart = deltaEnergy * startPortion / s;
				realData[startSlot] += energyStart;
				realData[endSlot] = deltaEnergy - energyStart;
				update(startSlot);
			} else {
    			deltaEnergy /= deltaSlot +2;
    			realData[startSlot] += deltaEnergy;
    			int s = startSlot;
    			do {
    				update(s);
    				s = (s + 1) % DAILY_SLOTS;
    				realData[s] = deltaEnergy;
    			} while (s != endSlot);
			}
		}
	}
	
	
	private void computeSeasonalSmoothing() {
		double average = 0;
		for (int i = 0; i < DAILY_SLOTS; ++i) {
			average += realData[i];
		}
		average /= DAILY_SLOTS;
		
		for (int i = 0; i < DAILY_SLOTS; ++i) {
			smoothedSeason[i] = realData[i] / average;
		}
	}
	
	public void update(int currentSlot) {
		if (updates == DAILY_SLOTS) computeSeasonalSmoothing();

		int previousSlot = currentSlot == 0 ? DAILY_SLOTS -1 : currentSlot -1;
		if (updates == 0) smoothedLevel[currentSlot] = realData[currentSlot];
		else if (updates < DAILY_SLOTS) {
			smoothedLevel[currentSlot] = alpha * realData[currentSlot] + (1 - alpha) * smoothedLevel[previousSlot];
		} else {
			smoothedLevel[currentSlot] = alpha * realData[currentSlot] / smoothedSeason[currentSlot] + (1 - alpha) * smoothedLevel[previousSlot];
			smoothedSeason[currentSlot] = gamma * realData[currentSlot] / smoothedLevel[currentSlot] + (1 - gamma) * smoothedSeason[currentSlot];
		}
		++updates;
	}
	
	public double[] forecast(int startSlot) {
		for (int i = 0; i < DAILY_SLOTS; ++i) {
			// Calculate forecast
			int s = (startSlot + i) % DAILY_SLOTS;
			forecastData[i] = smoothedLevel[s] * smoothedSeason[s];
		}
		return forecastData;
	}


	public void setRandomLoad() {
		realData[0] = 100;
		int sign = 1;
		lastSlot = 0;
		update(lastSlot);
		for (int i = 1; i < DAILY_SLOTS; ++i) {
			if (Math.random() > 0.7) {
				realData[i] = 1;
			} else {
    			double val = 200 * Math.random();
    			if (i % 3 == 0) sign = Math.random() > 0.5 ? -1 : 1;
    			realData[i] = Math.abs(sign * val + realData[i - 1]);
			}
			update(lastSlot);
			lastSlot = i;
		}
		realData[0] = 100;
		sign = 1;
		//lastSlot = 0;
		for (int j = 0; j < 8; ++j) {
    		for (int i = 1; i < DAILY_SLOTS; ++i) {
        		double val = 200 * Math.random();
        		if (i % 3 == 0) sign = Math.random() > 0.5 ? -1 : 1;
        		realData[i] = Math.abs(sign * val + realData[i - 1]);
    			update(lastSlot);
    			lastSlot = i;
    		}
		}
	}

	public static void main(String args[]) {
		try {
			DailyEnergyLoad dload = new DailyEnergyLoad();
			long t1 = System.currentTimeMillis();
			long t2 = t1 + 3 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 2000);
			t1 = t2;
			t2 += 3 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 200);
			t1 = t2;
			t2 += 4 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 600);
			t1 = t2;
			t2 += 4 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 400);
			t1 = t2;
			t2 += 5 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 300);
			t1 = t2;
			t2 += 4 * 60 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 6000);
			t1 = t2;
			t2 += 2 * 60 * 1000;
			dload.addEnergyDelta(t1, t2, 300);
			
			dload.setRandomLoad();
			
			
			double[] data = dload.realData;
			for (int i = 0; i < data.length; ++i) {
				System.out.println(i + " " + data[i]);
			}
			data = dload.forecast(0);
			for (int i = 0; i < data.length; ++i) {
				System.out.println(i + " " + data[i]);
			}
			System.exit(0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
