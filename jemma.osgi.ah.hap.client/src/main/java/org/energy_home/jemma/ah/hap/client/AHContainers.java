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

public class AHContainers {

	private static Map<String, Class> attrIdsClassMap = null;

	public static final String CORE_CONTAINERS_PREFIX = "ah.core.";
	public static final String CLUSTER_CONTAINERS_PREFIX = "ah.cluster.";
	
	public static final int APPLIANCE_EVENT_STOPPED = 0;
	public static final int APPLIANCE_EVENT_STARTED = 1;
	public static final int APPLIANCE_EVENT_UNAVAILABLE = 3;
	public static final int APPLIANCE_EVENT_AVAILABLE = 4;
	
	public static final float AH_SE_METERING_POWER_INVALID_VALUE = 0xFFFFFF;
	public static final double AH_SE_METERING_ENERGY_SUM_INVALID_VALUE = 0xFFFFFFFFFFFFL;
	
	public static final String attrId_ah_zigbee_network_status = "ah.zigbee.network.status";

	public static final String attrId_ah_core_appliance_events = "ah.core.appliance.events";
	// Two alias for the same container
	public static final String attrId_ah_cluster_ah_ConfigServer_Name = "ah.cluster.ah.ConfigServer.Name";
	public static final String attrId_ah_core_config_name = "ah.core.config.name";	
	// Two alias for the same container
	public static final String attrId_ah_cluster_ah_ConfigServer_CategoryPid = "ah.cluster.ah.ConfigServer.CategoryPid";
	public static final String attrId_ah_core_config_category = "ah.core.config.category";	
	// Two alias for the same container
	public static final String attrId_ah_cluster_ah_ConfigServer_LocationPid = "ah.cluster.ah.ConfigServer.LocationPid";
	public static final String attrId_ah_core_config_location = "ah.core.config.location";
		
	public static final String attrId_ah_cluster_metering_prefix = "ah.cluster.metering.";
//	public static final String attrId_ah_eh_esp_deliveredEnergySum = "ah.eh.esp.deliveredEnergySum";
	public static final String attrId_ah_cluster_metering_deliveredEnergySum = "ah.cluster.metering.deliveredEnergySum";	
//	public static final String attrId_ah_eh_esp_receivedEnergySum = "ah.eh.esp.receivedEnergySum";
	public static final String attrId_ah_cluster_metering_receivedEnergySum = "ah.cluster.metering.receivedEnergySum";	
//	public static final String attrId_ah_eh_esp_deliveredPower = "ah.eh.esp.deliveredPower";
	public static final String attrId_ah_cluster_metering_deliveredPower = "ah.cluster.metering.deliveredPower";	
//	public static final String attrId_ah_eh_esp_receivedPower = "ah.eh.esp.receivedPower";	
	public static final String attrId_ah_cluster_metering_receivedPower = "ah.cluster.metering.receivedPower";
	
	public static final String attrId_ah_cluster_onoff_prexif = "ah.cluster.onoff.";
//	public static final String attrId_ah_eh_esp_onOffStatus = "ah.eh.esp.onOffStatus";
//	public static final String attrId_ah_aal_onoffstatus = "ah.aal.onoffstatus";
	public static final String attrId_ah_cluster_onoff_status = "ah.cluster.onoff.status";	
//	public static final String attrId_ah_aal_batteryvoltage = "ah.aal.batteryvoltage";
	public static final String attrId_ah_cluster_power_batteryvoltage = "ah.cluster.power.batteryvoltage";
	
//	public static final String attrId_ah_aal_presence = "ah.aal.presence";
	public static final String attrId_ah_cluster_occupancy_prefix = "ah.cluster.occupancy.";
	public static final String attrId_ah_cluster_occupancy_presence = "ah.cluster.occupancy.presence";	

	//	public static final String attrId_ah_aal_tempstatus = "ah.gas.temperaturevalue";
	public static final String attrId_ah_cluster_temperature_prefix = "ah.cluster.temperature.";
	public static final String attrId_ah_cluster_temperature_value = "ah.cluster.temperature.value";
	
	public static final String attrId_ah_cluster_relhumidity_prefix = "ah.cluster.relhumidity.";
	public static final String attrId_ah_cluster_relhumidity_value = "ah.cluster.relhumidity.value";	

	//	public static final String attrId_ah_aal_illumstatus = "ah.aal.illuminance";
	public static final String attrId_ah_cluster_illuminance_prefix = "ah.cluster.illuminance.";
	public static final String attrId_ah_cluster_illuminance_value = "ah.cluster.illuminance.value";

	public static final String attrId_ah_cluster_ias_prexif = "ah.cluster.ias.";
//	public static final String attrId_ah_aal_doorstatus = "ah.aal.doorstatus";
	public static final String attrId_ah_cluster_iascontact_open = "ah.cluster.iascontact.open";	
//	public static final String attrId_ah_aal_waterstatus = "ah.aal.waterstatus";
	public static final String attrId_ah_cluster_iaswater_leak = "ah.cluster.iaswater.leak";
	public static final String attrId_ah_cluster_iasgas_leak = "ah.cluster.iasgas.leak";
	
	public static final String attrId_ah_cluster_applctrl_prexif = "ah.cluster.applctrl.";
	public static final String attrId_ah_cluster_applctrl_status = "ah.cluster.applctrl.status";
	public static final String attrId_ah_cluster_applctrl_remoteControlEnabled = "ah.cluster.applctrl.remoteControlEnabled";
	// Absolute time expressed as currentTimeMillis 
	public static final String attrId_ah_cluster_applctrl_startTime = "ah.cluster.applctrl.startTime";
	public static final String attrId_ah_cluster_applctrl_finishTime = "ah.cluster.applctrl.finishTime";
	// Relative time expressed in minutes
	public static final String attrId_ah_cluster_applctrl_remainingTime = "ah.cluster.applctrl.remainingTime";
	public static final String attrId_ah_cluster_applctrl_cycleTarget0 = "ah.cluster.applctrl.cycleTarget0";	
	public static final String attrId_ah_cluster_applctrl_cycleTarget1 = "ah.cluster.applctrl.cycleTarget1";	
	public static final String attrId_ah_cluster_applctrl_temperatureTarget0 = "ah.cluster.applctrl.temperatureTarget0";	
	public static final String attrId_ah_cluster_applctrl_temperatureTarget1 = "ah.cluster.applctrl.temperatureTarget1";	
	
	public static final String attrId_ah_cluster_applevents_prefix = "ah.cluster.applevents.";
	public static final String attrId_ah_cluster_applevents_event = "ah.cluster.applevents.event";
	public static final String attrId_ah_cluster_applevents_alerts = "ah.cluster.applevents.alerts";
	
	public static final String attrId_ah_cluster_powerprofile_prefix = "ah.cluster.powerprofile.";
	
	static {
		Map<String, Class> attributeIdsMap = new HashMap<String, Class>(3);

		attributeIdsMap.put(attrId_ah_zigbee_network_status, Integer.class);
		
		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_Name, String.class);
		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_CategoryPid, Integer.class);
		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_LocationPid, Integer.class);
		
		attributeIdsMap.put(attrId_ah_core_config_name, String.class);
		attributeIdsMap.put(attrId_ah_core_config_category, Integer.class);
		attributeIdsMap.put(attrId_ah_core_config_location, Integer.class);
		
		attributeIdsMap.put(attrId_ah_cluster_metering_deliveredEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_cluster_metering_receivedEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_cluster_metering_deliveredPower, Float.class);
		attributeIdsMap.put(attrId_ah_cluster_metering_receivedPower, Float.class);
		
		attributeIdsMap.put(attrId_ah_cluster_onoff_status, Boolean.class);
		attributeIdsMap.put(attrId_ah_cluster_power_batteryvoltage, Float.class);
		
		attributeIdsMap.put(attrId_ah_cluster_occupancy_presence, Boolean.class);
		attributeIdsMap.put(attrId_ah_cluster_temperature_value, Float.class);
		attributeIdsMap.put(attrId_ah_cluster_relhumidity_value, Float.class);
		attributeIdsMap.put(attrId_ah_cluster_illuminance_value, Integer.class);
		
		attributeIdsMap.put(attrId_ah_cluster_iascontact_open, Boolean.class);
		attributeIdsMap.put(attrId_ah_cluster_iaswater_leak, Boolean.class);
		attributeIdsMap.put(attrId_ah_cluster_iasgas_leak, Boolean.class);

		attributeIdsMap.put(attrId_ah_cluster_applctrl_status, Short.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_remoteControlEnabled, Boolean.class);		
		attributeIdsMap.put(attrId_ah_cluster_applctrl_startTime, Long.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_finishTime, Long.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_remainingTime, Integer.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_cycleTarget0, Short.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_cycleTarget1, Short.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_temperatureTarget0, Integer.class);
		attributeIdsMap.put(attrId_ah_cluster_applctrl_temperatureTarget1, Integer.class);
		
		attributeIdsMap.put(attrId_ah_cluster_applevents_event, Short.class);
		attributeIdsMap.put(attrId_ah_cluster_applevents_alerts, Integer.class);
		
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
