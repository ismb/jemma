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
package org.energy_home.jemma.m2m.ah;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PowerInfo", propOrder = {
    "minPower",
    "minPowerTime",
    "maxPower",
    "maxPowerTime"})
public class MinMaxPowerInfo implements Serializable {
	private static final long serialVersionUID = 2254228193422737520L;
	
	@XmlElement(name = "MinPower")
	volatile float minPower;
	@XmlElement(name = "MinPowerTime")
	volatile long minPowerTime;
	@XmlElement(name = "MaxPower")
	volatile float maxPower;
	@XmlElement(name = "MaxPowerTime")
	volatile long maxPowerTime;
	
	@XmlTransient
	volatile float currentPower;
	@XmlTransient
	volatile long currentTime;
	
	public MinMaxPowerInfo() {
		reset();
	}
	
	public MinMaxPowerInfo(MinMaxPowerInfo other) {
		currentPower = other.currentPower;
		currentTime = other.currentTime;
		minPower = other.minPower;
		minPowerTime = other.minPowerTime;
		maxPower = other.maxPower;
		maxPowerTime = other.maxPowerTime;
	}
	
	public float getCurrentPower() {
		return currentPower;
	}
	public long getCurrentPowerTime() {
		return currentTime;
	}
	public float getMinPower() {
		return minPower;
	}
	public void setMinPower(float power) {
		minPower = power;
	}
	public long getMinPowerTime() {
		return minPowerTime;
	}
	public void setMinPowerTime(long time) {
		minPowerTime = time;
		if (currentTime < minPowerTime)
			currentTime = minPowerTime;
	}
	public float getMaxPower() {
		return maxPower;
	}
	public void setMaxPower(float power) {
		maxPower = power;
	}
	public long getMaxPowerTime() {
		return maxPowerTime;
	}
	public void setMaxPowerTime(long time) {
		maxPowerTime = time;
		if (currentTime < maxPowerTime)
			currentTime = maxPowerTime;
	}
	
	public void setCurrentPower(float power, long time) {
		currentPower = power;
		currentTime = time;
		if (power > maxPower) {
			maxPower = power;
			maxPowerTime = time;
		}
		if (power < minPower) {
			minPower = power;
			minPowerTime = time;
		}
	}
	
	public boolean isValid() {
		return maxPower >= minPower;
	}
	
	public void reset() {
		minPowerTime = maxPowerTime = 0;
		minPower = Float.POSITIVE_INFINITY;
		maxPower = -1;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("current power = ").append(currentPower);
		sb.append("\ncurrent power time = ").append(currentTime);
		sb.append("\nmax power = ").append(maxPower);
		sb.append(" - time = ").append(maxPowerTime);
		sb.append("\nmin power = ").append(minPower);
		sb.append(" - time = ").append(minPowerTime);
		sb.append('\n');
		return sb.toString();
	}
}
