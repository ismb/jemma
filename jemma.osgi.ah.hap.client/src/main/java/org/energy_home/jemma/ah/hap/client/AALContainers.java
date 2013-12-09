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

public class AALContainers {
	private static Map<String, Class<?>> attrIdsClassMap = null;

	public static final String attrId_ah_aal_presence = "ah.aal.presence";
	public static final String attrId_ah_aal_doorstatus = "ah.aal.doorstatus";
	// MF 3/8/2012 added temp status management
	public static final String attrId_ah_aal_tempstatus = "ah.gas.temperaturevalue";
	// MF 19/12/2012 added battery voltage and water loose management
	public static final String attrId_ah_aal_batteryvoltage = "ah.aal.batteryvoltage";
	public static final String attrId_ah_aal_waterstatus = "ah.aal.waterstatus";
	// MF 14/1/2013 added on off and illumination status management
	public static final String attrId_ah_aal_onoffstatus = "ah.aal.onoffstatus";
	public static final String attrId_ah_aal_illumstatus = "ah.aal.illuminance";
	
	static {
		Map<String, Class<?>> attributeIdsMap = new HashMap<String, Class<?>>(2);
		attributeIdsMap.put(attrId_ah_aal_doorstatus, Boolean.class);
		attributeIdsMap.put(attrId_ah_aal_presence, Boolean.class);
		attributeIdsMap.put(attrId_ah_aal_tempstatus, Float.class);
		
		attributeIdsMap.put(attrId_ah_aal_batteryvoltage, Float.class);
		attributeIdsMap.put(attrId_ah_aal_waterstatus, Boolean.class);

		attributeIdsMap.put(attrId_ah_aal_onoffstatus, Boolean.class);
		attributeIdsMap.put(attrId_ah_aal_illumstatus, Integer.class);
		
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
