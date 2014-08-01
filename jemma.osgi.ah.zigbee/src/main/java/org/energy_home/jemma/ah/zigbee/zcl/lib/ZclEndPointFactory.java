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
package org.energy_home.jemma.ah.zigbee.zcl.lib;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZclEndPointFactory {

	private static final Logger LOG = LoggerFactory.getLogger( ZclEndPointFactory.class );
	
	public static final int ZIGBEE_ON_OFF_SWITCH_DEVICE_ID = 0x0000;
	public static final int ZIGBEE_LEVEL_CONTROLLABLE_OUTPUT_ID = 0x0003;

	public static final int ZIGBEE_LOAD_CONTROL_DEVICE_ID = 0x0051;
	public static final int ZIGBEE_WHITE_GOODS_ID = 0x0052;
	public static final int ZIGBEE_METERING_DEVICE_ID = 0x0053;
	
	public static final int ZIGBEE_ON_OFF_LIGHT_ID = 0x0100;
	public static final int ZIGBEE_DIMMABLE_LIGHT_ID = 0x0101;
	public static final int ZIGBEE_LIGHT_SENSOR_ID = 0x0106;
	public static final int ZIGBEE_OCCUPANCY_SENSOR_ID = 0x0107;

	public static final int ZIGBEE_THERMOSTAT_ID = 0x0301;
	public static final int ZIGBEE_TEMPERATURE_SENSOR_ID = 0x0302;
	
	public static final int ZIGBEE_IAS_ZONE_ID = 0x0402;
	
	private static String getEndPointType(int deviceId) {
		switch (deviceId) {
		case ZIGBEE_ON_OFF_SWITCH_DEVICE_ID:
			return IEndPointTypes.ZIGBEE_ON_OFF_SWITCH_DEVICE;
		case ZIGBEE_LEVEL_CONTROLLABLE_OUTPUT_ID:
			return IEndPointTypes.ZIGBEE_LEVEL_CONTROLLABLE_OUTPUT;	
		case ZIGBEE_LOAD_CONTROL_DEVICE_ID:
			return IEndPointTypes.ZIGBEE_LOAD_CONTROL_DEVICE;			
		case ZIGBEE_WHITE_GOODS_ID:
			return IEndPointTypes.ZIGBEE_WHITE_GOODS;	
		case ZIGBEE_METERING_DEVICE_ID:
			return IEndPointTypes.ZIGBEE_METERING_DEVICE;	
		case ZIGBEE_ON_OFF_LIGHT_ID:
			return IEndPointTypes.ZIGBEE_ON_OFF_LIGHT;	
		case ZIGBEE_DIMMABLE_LIGHT_ID:
			return IEndPointTypes.ZIGBEE_DIMMABLE_LIGHT;	
		case ZIGBEE_LIGHT_SENSOR_ID:
			return IEndPointTypes.ZIGBEE_LIGHT_SENSOR;	
		case ZIGBEE_OCCUPANCY_SENSOR_ID:
			return IEndPointTypes.ZIGBEE_OCCUPANCY_SENSOR;	
		case ZIGBEE_THERMOSTAT_ID:
			return IEndPointTypes.ZIGBEE_THERMOSTAT;	
		case ZIGBEE_TEMPERATURE_SENSOR_ID:
			return IEndPointTypes.ZIGBEE_TEMPERATURE_SENSOR;	
		case ZIGBEE_IAS_ZONE_ID:
			return IEndPointTypes.ZIGBEE_IAS_ZONE;				
		default:
			return IEndPointTypes.ZIGBEE_GENERIC_DEVICE;
		}		
	}
	
	public static ZclEndPoint getEndPoint(int profileId, int deviceId) throws ApplianceException {
		// TODO: different profile id needs to be managed
		String endPointType = getEndPointType(deviceId);
		if (endPointType == null)
			endPointType = String.format("ah.ep.zigbee.%s.%s", new Integer[]{new Integer(profileId), new Integer(deviceId)});		
		return new ZclEndPoint(endPointType);
	}
	
	public static void addServiceClusters(ZclEndPoint endPoint, int profileId, int deviceId, final int[] clientClusterIds, final int[] serverClusterIds) throws ApplianceException {
		ZclServiceCluster cluster = null;	
		Integer clusterId = null;
		for (int i = 0; i < serverClusterIds.length; i++) {
			clusterId = new Integer(serverClusterIds[i]);
			cluster = ZclServiceClusterFactory.getCluster(IServiceCluster.SERVER_SIDE, new Integer(profileId), clusterId);
			if (cluster != null) {
				endPoint.addServiceCluster(cluster);
			} else {
				LOG.warn("Unsupported server side cluster " + clusterId);
			}
		}
		for (int i = 0; i < clientClusterIds.length; i++) {
			clusterId = new Integer(clientClusterIds[i]);
			cluster = ZclServiceClusterFactory.getCluster(IServiceCluster.CLIENT_SIDE, new Integer(profileId), clusterId);
			if (cluster != null) {
				endPoint.addServiceCluster(cluster);
			} else {
				LOG.warn("Unsupported client side cluster " + clusterId);
			}	
		}
	}
}
