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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnergyCostPowerInfo", propOrder = {
    "duration",
    "deltaEnergy",
    "cost",
    "minCost",  
    "maxCost",
    "powerInfo"
})
public class EnergyCostPowerInfo {
	private static final long serialVersionUID = -1589348206634934789L;
    @XmlElement(name = "Duration")
	private long duration;
    @XmlElement(name = "DeltaEnergy")
	private Float deltaEnergy;
    @XmlElement(name = "Cost")
    private Float cost;
    @XmlElement(name = "MinCost")
    private Float minCost;
    @XmlElement(name = "MaxCost")
    private Float maxCost;	
    @XmlElement(name = "PowerInfo")
	private MinMaxPowerInfo powerInfo;

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getDuration() {
		return duration;
	}
	
	public void setDeltaEnergy(Float deltaEnergy) {
		this.deltaEnergy = deltaEnergy;
	}

	public Float getDeltaEnergy() {
		return deltaEnergy;
	}	
	
	public void setCost(Float cost) {
		this.cost = cost;
	}

	public Float getCost() {
		return cost;
	}

	public void setMinCost(Float minCost) {
		this.minCost = minCost;
	}

	public Float getMinCost() {
		return minCost;
	}	
	
	public void setMaxCost(Float maxCost) {
		this.maxCost = maxCost;
	}

	public Float getMaxCost() {
		return maxCost;
	}

	public void setPowerInfo(MinMaxPowerInfo powerInfo) {
		this.powerInfo = powerInfo;
	}

	public MinMaxPowerInfo getPowerInfo() {
		return powerInfo;
	}
	
}
