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
package org.energy_home.jemma.ah.hap.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.energy_home.jemma.m2m.ah.FloatCDV;
import org.energy_home.jemma.m2m.ah.FloatDV;

public class EHContainers {
	private static Map<String, Class> attrIdsClassMap = null;

	public static final int EVENT_INVALID_CURRENT_SUMMATION_DELIVERED_VALUE = 1;
	public static final int EVENT_INVALID_DELTA_ENERGY = 2;
	public static final int EVENT_INVALID_INST_DEMAND_VALUE = 3;
	public static final int EVENT_INVALID_CURRENT_SUMMATION_RECEIVED_VALUE = 4;

	public static final String attrId_ah_eh_gui_log = "ah.eh.gui.log";
	
	public static final String attrId_ah_eh_esp_events = "ah.eh.esp.events";
	
	// Container used by old version of Energy@home application
	public static final String attrId_ah_eh_esp_energySum = "ah.eh.esp.energySum";
	
	// Containers used for delivered and received energy
	public static final String attrId_ah_eh_esp_deliveredEnergySum = "ah.eh.esp.deliveredEnergySum";
	public static final String attrId_ah_eh_esp_receivedEnergySum = "ah.eh.esp.receivedEnergySum";
	
	// Unused containers (now managed in HAP client bundle)
	public static final String attrId_ah_eh_esp_deliveredPower = "ah.eh.esp.deliveredPower";	
	public static final String attrId_ah_eh_esp_receivedPower = "ah.eh.esp.receivedPower";		
	public static final String attrId_ah_eh_esp_onOffStatus = "ah.eh.esp.onOffStatus";	
	
	// Min and max power are instantaneous demand values filtered by Energy@home application 
	public static final String attrId_ah_eh_esp_minPower = "ah.eh.esp.minPower";
	public static final String attrId_ah_eh_esp_maxPower = "ah.eh.esp.maxPower";
	
	// Energy consumption containers (house consumptions are associated to exchange smart info device)
	public static final String attrId_ah_eh_esp_tenMinutesEnergy = "ah.eh.esp.tmEnergy";
	public static final String attrId_ah_eh_esp_hourlyEnergy = "ah.eh.esp.hourlyEnergy";
	public static final String attrId_ah_eh_esp_dailyEnergy = "ah.eh.esp.dailyEnergy";
	public static final String attrId_ah_eh_esp_monthlyEnergy = "ah.eh.esp.monthlyEnergy";
	public static final String attrId_ah_eh_esp_wdHourlyEnergyAvg = "ah.eh.esp.wdHourlyEnergyAvg";
	
	// Produced energy containers
	public static final String attrId_ah_eh_esp_tenMinutesReceivedEnergy = "ah.eh.esp.tmReceivedEnergy";
	public static final String attrId_ah_eh_esp_hourlyReceivedEnergy = "ah.eh.esp.hourlyReceivedEnergy";
	public static final String attrId_ah_eh_esp_dailyReceivedEnergy = "ah.eh.esp.dailyReceivedEnergy";
	public static final String attrId_ah_eh_esp_monthlyReceivedEnergy = "ah.eh.esp.monthlyReceivedEnergy";
	public static final String attrId_ah_eh_esp_wdHourlyReceivedEnergyAvg = "ah.eh.esp.wdHourlyReceivedEnergyAvg";
	
	// Produced energy forecast container
	public static final String attrId_ah_eh_esp_hourlyReceivedEnergyForecast = "ah.eh.esp.hourlyReceivedEnergyForecast";

	// Delivered energy containers
	public static final String attrId_ah_eh_esp_tenMinutesDeliveredEnergy = "ah.eh.esp.tmDeliveredEnergy";
	public static final String attrId_ah_eh_esp_hourlyDeliveredEnergy = "ah.eh.esp.hourlyDeliveredEnergy";
	public static final String attrId_ah_eh_esp_dailyDeliveredEnergy = "ah.eh.esp.dailyDeliveredEnergy";
	public static final String attrId_ah_eh_esp_monthlyDeliveredEnergy = "ah.eh.esp.monthlyDeliveredEnergy";
	public static final String attrId_ah_eh_esp_wdHourlyDeliveredEnergyAvg = "ah.eh.esp.wdHourlyDeliveredEnergyAvg";
	
	// Energy cost containers 
	public static final String attrId_ah_eh_esp_energyCost = "ah.eh.esp.energyCost";
	public static final String attrId_ah_eh_esp_tenMinutesEnergyCost = "ah.eh.esp.tmEnergyCost";
	public static final String attrId_ah_eh_esp_hourlyEnergyCost = "ah.eh.esp.hourlyEnergyCost";
	public static final String attrId_ah_eh_esp_dailyEnergyCost = "ah.eh.esp.dailyEnergyCost";
	public static final String attrId_ah_eh_esp_monthlyEnergyCost = "ah.eh.esp.monthlyEnergyCost";
	public static final String attrId_ah_eh_esp_wdHourlyEnergyCostAvg = "ah.eh.esp.wdHourlyEnergyCostAvg";	
	
	// Energy cost power info (consumptions)
	public static final String attrId_ah_eh_esp_ecpi = "ah.eh.esp.ecpi";
	public static final String attrId_ah_eh_esp_tmEcpi = "ah.eh.esp.tmEcpi";
	public static final String attrId_ah_eh_esp_hrEcpi = "ah.eh.esp.hrEcpi";
	public static final String attrId_ah_eh_esp_dlEcpi = "ah.eh.esp.dlEcpi";
	public static final String attrId_ah_eh_esp_mnEcpi = "ah.eh.esp.mnEcpi";
	public static final String attrId_ah_eh_esp_wdHrEcpiAvg = "ah.eh.esp.wdHrEcpiAvg";
	
	// Delivered energy cost power info (equivalent to consumptions for all plugs and smartinfo in case of no micro generation plants)
	public static final String attrId_ah_eh_esp_deliveredEcpi = "ah.eh.esp.deliveredEcpi";
	public static final String attrId_ah_eh_esp_tmDeliveredEcpi = "ah.eh.esp.tmDeliveredEcpi";
	public static final String attrId_ah_eh_esp_hrDeliveredEcpi = "ah.eh.esp.hrDeliveredEcpi";
	public static final String attrId_ah_eh_esp_dlDeliveredEcpi = "ah.eh.esp.dlDeliveredEcpi";
	public static final String attrId_ah_eh_esp_mnDeliveredEcpi = "ah.eh.esp.mnDeliveredEcpi";
	
	public static final String attrId_ah_eh_esp_appStats = "ah.eh.esp.appStats";
	
	static {
		Map<String, Class> attributeIdsMap = new HashMap<String, Class>(12);

		attributeIdsMap.put(attrId_ah_eh_esp_minPower, Float.class);
		attributeIdsMap.put(attrId_ah_eh_esp_maxPower, Float.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_deliveredPower, Float.class);
		attributeIdsMap.put(attrId_ah_eh_esp_receivedPower, Float.class);
		attributeIdsMap.put(attrId_ah_eh_esp_deliveredEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_eh_esp_receivedEnergySum, Double.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_onOffStatus, Boolean.class);		
		
		attributeIdsMap.put(attrId_ah_eh_esp_energySum, Double.class);
		attributeIdsMap.put(attrId_ah_eh_esp_tenMinutesEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_hourlyEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_dailyEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_monthlyEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_wdHourlyEnergyAvg, FloatDV.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_deliveredEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_eh_esp_tenMinutesDeliveredEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_hourlyDeliveredEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_dailyDeliveredEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_monthlyDeliveredEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_wdHourlyDeliveredEnergyAvg, FloatDV.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_receivedEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_eh_esp_tenMinutesReceivedEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_hourlyReceivedEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_dailyReceivedEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_monthlyReceivedEnergy, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_wdHourlyReceivedEnergyAvg, FloatDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_hourlyReceivedEnergyForecast, FloatDV.class);

		attributeIdsMap.put(attrId_ah_eh_esp_energyCost, FloatCDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_tenMinutesEnergyCost, FloatCDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_hourlyEnergyCost, FloatCDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_dailyEnergyCost, FloatCDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_monthlyEnergyCost, FloatCDV.class);
		attributeIdsMap.put(attrId_ah_eh_esp_wdHourlyEnergyCostAvg, FloatDV.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_events, Integer.class);

		attributeIdsMap.put(attrId_ah_eh_gui_log, String.class);
		
		attributeIdsMap.put(attrId_ah_eh_esp_appStats, ApplianceLog.class);
		
		attrIdsClassMap = Collections.unmodifiableMap(attributeIdsMap);
	};

	public static Class getAttributeIdClass(String attributeId) {
		return attrIdsClassMap.get(attributeId);
	}

	public static String[] getAttributeIds() {
		String[] result = new String[attrIdsClassMap.size()];
		attrIdsClassMap.keySet().toArray(result);
		return result;
	}
}
