/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

	private static final String CORE_CLUSTERS_ATTRIBUTE_ID_PREFIX = "ah.cluster";
	private static final String CORE_CLUSTERS_ATTRIBUTE_NAME_PREFIX = "org.energy_home.jemma.ah.cluster";
	private static final String TELECOMITALIA_PACKAGE_PREFIX = "org.energy_home.jemma.";

	public static final int APPLIANCE_EVENT_STOPPED = 0;
	public static final int APPLIANCE_EVENT_STARTED = 1;
	public static final int APPLIANCE_EVENT_UNAVAILABLE = 3;
	public static final int APPLIANCE_EVENT_AVAILABLE = 4;
	
	public static final String attrId_ah_zigbee_network_status = "ah.zigbee.network.status";

	// Two alias for the same container
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

	static {
		Map<String, Class> attributeIdsMap = new HashMap<String, Class>(3);

		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_Name, String.class);
		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_CategoryPid, Integer.class);
		attributeIdsMap.put(attrId_ah_cluster_ah_ConfigServer_LocationPid, Integer.class);
		
		attributeIdsMap.put(attrId_ah_zigbee_network_status, Integer.class);

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

	public static String getClusterAttributeId(String clusterName, String attributeName) {
		// TODO: check for performance enhancement
		if (clusterName == null)
			return attributeName;
		String attributeId = clusterName + "." + attributeName;
		if (attributeId.startsWith(CORE_CLUSTERS_ATTRIBUTE_NAME_PREFIX))
			return attributeId.substring(TELECOMITALIA_PACKAGE_PREFIX.length());
		return null;
	}
}
