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

import java.util.List;

import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.MinMaxPowerInfo;


public interface ICloudServiceProxy {
	public static final int EVENT_INVALID_CURRENT_SUMMATION_DELIVERED_VALUE = 1;
	public static final int EVENT_INVALID_DELTA_ENERGY_VALUE = 2;
	public static final int EVENT_INVALID_INST_DEMAND_VALUE = 3;
	public static final int EVENT_INVALID_CURRENT_SUMMATION_RECEIVED_VALUE = 4;
	
	List<Float> retrieveHourlyProducedEnergyForecast(String applianceId);
	
	ContentInstance retrieveDeliveredEnergySummation(String applianceId);

	void storeReceivedEnergy(String applianceId, long time, double totalEnergy) throws Exception;
	
	void storeDeliveredEnergy(String applianceId, long time, double totalEnergy) throws Exception;
		
	void storeDeliveredEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo) throws Exception;
	
	void storeReceivedEnergyCostPowerInfo(String applianceId, EnergyCostInfo eci, MinMaxPowerInfo powerInfo) throws Exception;
	
	void storeEvent(String applianceId, long time, int eventType) throws Exception;
	
	void storeApplianceStatistics(String applianceId, long time, ApplianceLog applianceLog) throws Exception;
	
}