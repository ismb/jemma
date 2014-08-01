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

import java.io.Serializable;

public class EnergyCostInfo implements Serializable {
	volatile float cost, minCost, maxCost;
	volatile long startTime, endTime;
	volatile double startEnergy, deltaEnergy;

	public EnergyCostInfo() {}
	
	// copy constructor
	public EnergyCostInfo(EnergyCostInfo other) {
		synchronized (other) {
    		cost = other.cost;
    		minCost = other.minCost;
    		maxCost = other.maxCost;
    		startTime = other.startTime;
    		endTime = other.endTime;
    		startEnergy = other.startEnergy;
    		deltaEnergy = other.deltaEnergy;
		}
	}
	
	public EnergyCostInfo(float c, float min, float max, double delta) {
		cost = c;
		minCost = min;
		maxCost = max;
		deltaEnergy = delta;
	}
	
	public float getCost() {
		return cost;
	}
	public float getMinCost() {
		return minCost;
	}
	public float getMaxCost() {
		return maxCost;
	}
	public long getStartTime() {
		return startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public long getDuration() {
		return endTime - startTime;
	}
	public double getStartTotalEnergy() {
		return startEnergy;
	}
	public double getDeltaEnergy() {
		return deltaEnergy;
	}
	public boolean isValid() {
		return endTime > startTime;
	}
	
	public void setStartEndTime(long start, long end) {
		startTime =  start;
		endTime = end;
	}
	
	public synchronized void addValues(EnergyCostInfo eci) {
		if (eci.isValid()) {
        	endTime = eci.endTime;
        	deltaEnergy += eci.deltaEnergy;
        	cost += eci.cost;
        	minCost += eci.minCost;
        	maxCost += eci.maxCost;
		}
	}
	
	public synchronized EnergyCostInfo copyAndReset() {
		EnergyCostInfo copy = new EnergyCostInfo(this);
		// reset
		reset(endTime);
		return copy;
	}
	
	public synchronized EnergyCostInfo copyAndReset(long referenceTime) {
		// check boundaries consistency
		if (referenceTime <= startTime || referenceTime > endTime) return null;
		
		EnergyCostInfo copy = new EnergyCostInfo(this);
		double energy = deltaEnergy * (referenceTime - startTime) / (endTime - startTime);
		copy.deltaEnergy = energy;
		copy.endTime = referenceTime;
		startEnergy += energy;
		deltaEnergy -= energy;
		cost = minCost = maxCost = 0;

		return copy;
	}
	
	public synchronized void reset(long time) {
		// reset
		startEnergy += deltaEnergy;
		deltaEnergy = 0;
		cost = minCost = maxCost = 0;
		startTime = endTime = time;
	}
	
	public synchronized void reset(long time, double energy) {
		// reset
		startEnergy = energy;
		deltaEnergy = 0;
		cost = minCost = maxCost = 0;
		startTime = endTime = time;
	}
	
//	public synchronized void reset(long time) {
//		// reset
//		if (time > endTime) time = endTime;
//		double energy = deltaEnergy * (time - startTime) / (endTime - startTime);
//		
//		startEnergy += energy;
//		deltaEnergy -= energy;
//		cost = minCost = maxCost = 0;
//		startTime = time;
//	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder("\ndelta energy = ").append(deltaEnergy);
		sb.append("\nstart time = ").append(startTime);
		sb.append("\nduration = ").append(getDuration()).append(" - ").append(getDuration() / 60000);
		sb.append("\ncost = ").append(cost);
		sb.append("\nmin cost = ");
		sb.append(cost == minCost ? "same" : minCost);
		sb.append("\nmax cost = ");
		sb.append(maxCost == minCost ? "same" : maxCost);
		sb.append('\n');
		return sb.toString();
	}
}
