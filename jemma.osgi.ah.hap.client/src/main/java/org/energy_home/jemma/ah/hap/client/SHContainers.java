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

// Smart Home Containers
public class SHContainers {
	private static Map<String, Class<?>> attrIdsClassMap = null;

//	public static final String attrId_ah_eh_esp_deliveredEnergySum = "ah.eh.esp.deliveredEnergySum";
	public static final String attrId_ah_sh_meter_deliveredEnergySum = "ah.sh.meter.deliveredEnergySum";
	
//	public static final String attrId_ah_eh_esp_receivedEnergySum = "ah.eh.esp.receivedEnergySum";
	public static final String attrId_ah_sh_meter_receivedEnergySum = "ah.sh.meter.receivedEnergySum";
	
//	public static final String attrId_ah_eh_esp_deliveredPower = "ah.eh.esp.deliveredPower";
	public static final String attrId_ah_sh_meter_deliveredPower = "ah.sh.meter.deliveredPower";
	
//	public static final String attrId_ah_eh_esp_receivedPower = "ah.eh.esp.receivedPower";	
	public static final String attrId_ah_sh_meter_receivedPower = "ah.sh.meter.receivedPower";
	
//	public static final String attrId_ah_eh_esp_onOffStatus = "ah.eh.esp.onOffStatus";
//	public static final String attrId_ah_aal_onoffstatus = "ah.aal.onoffstatus";
	public static final String attrId_ah_sh_onoff_status = "ah.sh.onoff.status";
	
//	public static final String attrId_ah_aal_presence = "ah.aal.presence";
	public static final String attrId_ah_sh_occupancy_presence = "ah.sh.occupancy.presence";
	
//	public static final String attrId_ah_aal_doorstatus = "ah.aal.doorstatus";
	public static final String attrId_ah_sh_iascontact_open = "ah.sh.iascontact.open";
	
//	public static final String attrId_ah_aal_tempstatus = "ah.gas.temperaturevalue";
	public static final String attrId_ah_sh_temperature_value = "ah.sh.temperature.value";

	public static final String attrId_ah_sh_relhumidity_value = "ah.sh.relhumidity.value";
	
//	public static final String attrId_ah_aal_batteryvoltage = "ah.aal.batteryvoltage";
	public static final String attrId_ah_sh_power_batteryvoltage = "ah.sh.power.batteryvoltage";
	
//	public static final String attrId_ah_aal_waterstatus = "ah.aal.waterstatus";
	public static final String attrId_ah_sh_iaswater_leak = "ah.sh.iaswater.leak";
	
//	public static final String attrId_ah_aal_illumstatus = "ah.aal.illuminance";
	public static final String attrId_ah_sh_illuminance_value = "ah.sh.illuminance.value";
	
	static {
		Map<String, Class<?>> attributeIdsMap = new HashMap<String, Class<?>>(2);
		attributeIdsMap.put(attrId_ah_sh_iascontact_open, Boolean.class);
		attributeIdsMap.put(attrId_ah_sh_occupancy_presence, Boolean.class);
		attributeIdsMap.put(attrId_ah_sh_temperature_value, Float.class);
		
		attributeIdsMap.put(attrId_ah_sh_power_batteryvoltage, Float.class);
		attributeIdsMap.put(attrId_ah_sh_iaswater_leak, Boolean.class);

		attributeIdsMap.put(attrId_ah_sh_onoff_status, Boolean.class);
		attributeIdsMap.put(attrId_ah_sh_illuminance_value, Integer.class);
		
		attributeIdsMap.put(attrId_ah_sh_meter_deliveredPower, Float.class);
		attributeIdsMap.put(attrId_ah_sh_meter_receivedPower, Float.class);
		attributeIdsMap.put(attrId_ah_sh_meter_deliveredEnergySum, Double.class);
		attributeIdsMap.put(attrId_ah_sh_meter_receivedEnergySum, Double.class);
		
		attrIdsClassMap = Collections.unmodifiableMap(attributeIdsMap);
}

	// see org.energy_home.jemma.ah.eh.EHContainers, same code ;
	public static Class<?> getAttributeIdClass(String attributeId) {
		return attrIdsClassMap.get(attributeId);
	}

	public static String[] getAttributeIds() {
		String[] result = new String[attrIdsClassMap.size()];
		attrIdsClassMap.keySet().toArray(result);
		return result;
	}
}
