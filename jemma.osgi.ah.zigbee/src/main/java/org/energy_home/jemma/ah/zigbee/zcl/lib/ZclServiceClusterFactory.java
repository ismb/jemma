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

import java.util.HashMap;

import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceControlServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceEventsAndAlertsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceEventsAndAlertsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceIdentificationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceIdentificationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceStatisticsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceStatisticsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclMeterIdentificationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclMeterIdentificationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclPowerProfileClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclPowerProfileServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclAlarmsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclAlarmsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclDeviceTemperatureConfigurationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclDeviceTemperatureConfigurationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclGroupsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclGroupsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclLevelControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclLevelControlServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffSwitchConfigurationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffSwitchConfigurationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPowerConfigurationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPowerConfigurationServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclTimeClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclTimeServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.hvac.ZclThermostatClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.hvac.ZclThermostatServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclIlluminanceMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclIlluminanceMeasurementServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclOccupancySensingClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclOccupancySensingServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclRelativeHumidityMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclRelativeHumidityMeasurementServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclTemperatureMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclTemperatureMeasurementServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.metering.ZclSimpleMeteringClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.metering.ZclSimpleMeteringServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.security.ZclIASZoneClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.security.ZclIASZoneServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.wulian.ZclIRTransmitterClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.wulian.ZclIRTransmitterServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkColorControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkColorControlServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkLevelControlServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkOnOffServer;

public class ZclServiceClusterFactory {
	private static final int ZLL_PROFILE_ID = 0xc05e;

	private final static HashMap commonServerClusterMap = new HashMap();
	private final static HashMap commonClientClusterMap = new HashMap();

	private final static HashMap zllServerClusterMap = new HashMap();
	private final static HashMap zllClientClusterMap = new HashMap();

	private final static HashMap wulianServerClusterMap = new HashMap();
	private final static HashMap wulianClientClusterMap = new HashMap();

	static {
		commonServerClusterMap.put(new Integer(ZclBasicServer.CLUSTER_ID), ZclBasicServer.class);
		commonServerClusterMap.put(new Integer(ZclIdentifyServer.CLUSTER_ID), ZclIdentifyServer.class);
		commonServerClusterMap.put(new Integer(ZclGroupsServer.CLUSTER_ID), ZclGroupsServer.class);
		commonClientClusterMap.put(new Integer(ZclTimeServer.CLUSTER_ID), ZclTimeServer.class);

		commonServerClusterMap.put(new Integer(ZclOnOffServer.CLUSTER_ID), ZclOnOffServer.class);
		commonServerClusterMap.put(new Integer(ZclSimpleMeteringServer.CLUSTER_ID), ZclSimpleMeteringServer.class);
		commonServerClusterMap.put(new Integer(ZclMeterIdentificationServer.CLUSTER_ID), ZclMeterIdentificationServer.class);

		commonServerClusterMap
				.put(new Integer(ZclApplianceIdentificationServer.CLUSTER_ID), ZclApplianceIdentificationServer.class);
		commonServerClusterMap.put(new Integer(ZclPowerProfileServer.CLUSTER_ID), ZclPowerProfileServer.class);
		commonServerClusterMap.put(new Integer(ZclApplianceStatisticsServer.CLUSTER_ID), ZclApplianceStatisticsServer.class);
		commonServerClusterMap.put(new Integer(ZclApplianceControlServer.CLUSTER_ID), ZclApplianceControlServer.class);
		commonServerClusterMap.put(new Integer(ZclApplianceEventsAndAlertsServer.CLUSTER_ID),
				ZclApplianceEventsAndAlertsServer.class);
		
		commonServerClusterMap.put(new Integer(ZclTemperatureMeasurementServer.CLUSTER_ID), ZclTemperatureMeasurementServer.class);
		commonServerClusterMap.put(new Integer(ZclRelativeHumidityMeasurementServer.CLUSTER_ID),
				ZclRelativeHumidityMeasurementServer.class);
		commonServerClusterMap.put(new Integer(ZclIlluminanceMeasurementServer.CLUSTER_ID), ZclIlluminanceMeasurementServer.class);
		commonServerClusterMap.put(new Integer(ZclOccupancySensingServer.CLUSTER_ID), ZclOccupancySensingServer.class);
		commonServerClusterMap.put(new Integer(ZclAlarmsServer.CLUSTER_ID), ZclAlarmsServer.class);
		commonServerClusterMap.put(new Integer(ZclIASZoneServer.CLUSTER_ID), ZclIASZoneServer.class);
		commonServerClusterMap.put(new Integer(ZclThermostatServer.CLUSTER_ID), ZclThermostatServer.class);
		commonServerClusterMap.put(new Integer(ZclDeviceTemperatureConfigurationServer.CLUSTER_ID),
				ZclDeviceTemperatureConfigurationServer.class);
		commonServerClusterMap.put(new Integer(ZclLevelControlServer.CLUSTER_ID), ZclLevelControlServer.class);
		commonServerClusterMap.put(new Integer(ZclOnOffSwitchConfigurationServer.CLUSTER_ID),
				ZclOnOffSwitchConfigurationServer.class);
		commonServerClusterMap.put(new Integer(ZclPowerConfigurationServer.CLUSTER_ID),
				ZclPowerConfigurationServer.class);

		commonClientClusterMap.put(new Integer(ZclBasicClient.CLUSTER_ID), ZclBasicClient.class);
		commonClientClusterMap.put(new Integer(ZclIdentifyClient.CLUSTER_ID), ZclIdentifyClient.class);
		commonClientClusterMap.put(new Integer(ZclGroupsClient.CLUSTER_ID), ZclGroupsClient.class);
		commonClientClusterMap.put(new Integer(ZclTimeClient.CLUSTER_ID), ZclTimeClient.class);

		commonClientClusterMap.put(new Integer(ZclOnOffClient.CLUSTER_ID), ZclOnOffClient.class);
		commonClientClusterMap.put(new Integer(ZclSimpleMeteringClient.CLUSTER_ID), ZclSimpleMeteringClient.class);
		commonClientClusterMap.put(new Integer(ZclMeterIdentificationClient.CLUSTER_ID), ZclMeterIdentificationClient.class);

		commonClientClusterMap
				.put(new Integer(ZclApplianceIdentificationClient.CLUSTER_ID), ZclApplianceIdentificationClient.class);
		commonClientClusterMap.put(new Integer(ZclPowerProfileClient.CLUSTER_ID), ZclPowerProfileClient.class);
		commonClientClusterMap.put(new Integer(ZclApplianceStatisticsClient.CLUSTER_ID), ZclApplianceStatisticsClient.class);
		commonClientClusterMap.put(new Integer(ZclApplianceControlClient.CLUSTER_ID), ZclApplianceControlClient.class);
		commonClientClusterMap.put(new Integer(ZclApplianceEventsAndAlertsClient.CLUSTER_ID),
				ZclApplianceEventsAndAlertsClient.class);

		commonClientClusterMap.put(new Integer(ZclTemperatureMeasurementClient.CLUSTER_ID), ZclTemperatureMeasurementClient.class);
		commonClientClusterMap.put(new Integer(ZclRelativeHumidityMeasurementClient.CLUSTER_ID),
				ZclRelativeHumidityMeasurementClient.class);
		commonClientClusterMap.put(new Integer(ZclIlluminanceMeasurementClient.CLUSTER_ID), ZclIlluminanceMeasurementClient.class);
		commonClientClusterMap.put(new Integer(ZclOccupancySensingClient.CLUSTER_ID), ZclOccupancySensingClient.class);
		commonClientClusterMap.put(new Integer(ZclAlarmsClient.CLUSTER_ID), ZclAlarmsClient.class);
		commonClientClusterMap.put(new Integer(ZclIASZoneClient.CLUSTER_ID), ZclIASZoneClient.class);
		commonClientClusterMap.put(new Integer(ZclThermostatClient.CLUSTER_ID), ZclThermostatClient.class);
		commonClientClusterMap.put(new Integer(ZclDeviceTemperatureConfigurationClient.CLUSTER_ID),
				ZclDeviceTemperatureConfigurationClient.class);
		commonClientClusterMap.put(new Integer(ZclLevelControlClient.CLUSTER_ID), ZclLevelControlClient.class);
		commonClientClusterMap.put(new Integer(ZclOnOffSwitchConfigurationClient.CLUSTER_ID),
				ZclOnOffSwitchConfigurationClient.class);	
		commonClientClusterMap.put(new Integer(ZclPowerConfigurationClient.CLUSTER_ID),
				ZclPowerConfigurationClient.class);

		zllServerClusterMap.put(new Integer(ZclLightLinkIdentifyServer.CLUSTER_ID), ZclLightLinkIdentifyServer.class);
		zllServerClusterMap.put(new Integer(ZclLightLinkLevelControlServer.CLUSTER_ID), ZclLightLinkLevelControlServer.class);
		zllServerClusterMap.put(new Integer(ZclLightLinkOnOffServer.CLUSTER_ID), ZclLightLinkOnOffServer.class);
		zllServerClusterMap.put(new Integer(ZclLightLinkColorControlServer.CLUSTER_ID), ZclLightLinkColorControlServer.class);

		zllClientClusterMap.put(new Integer(ZclLightLinkColorControlClient.CLUSTER_ID), ZclLightLinkColorControlClient.class);

		// Wulian Clusters
		wulianClientClusterMap.put(new Integer(ZclIRTransmitterClient.CLUSTER_ID & 0xffff), ZclIRTransmitterClient.class);
		wulianServerClusterMap.put(new Integer(ZclIRTransmitterServer.CLUSTER_ID & 0xffff), ZclIRTransmitterServer.class);
	}

	public static ZclServiceCluster getCluster(int clusterSide, Integer profileId, Integer clusterId) {
		HashMap serverClusterMap = commonServerClusterMap;
		HashMap clientClusterMap = commonClientClusterMap;
		HashMap backupServerClusterMap = null;
		HashMap backupClientClusterMap = null;
		if (profileId == ZLL_PROFILE_ID) {
			serverClusterMap = zllServerClusterMap;
			clientClusterMap = zllClientClusterMap;
			backupServerClusterMap = commonServerClusterMap;
			backupClientClusterMap = commonClientClusterMap;
		}

		try {
			Class clazz = null;
			switch (clusterSide) {
			case IServiceCluster.CLIENT_SIDE:
				if ((clazz = (Class) clientClusterMap.get(clusterId)) != null) {
					return (ZclServiceCluster) clazz.newInstance();
				} else if (backupClientClusterMap != null && (clazz = (Class) backupClientClusterMap.get(clusterId)) != null) {
					return (ZclServiceCluster) clazz.newInstance();
				} else if ((clazz = (Class) wulianClientClusterMap.get(clusterId)) != null) {
					return (ZclServiceCluster) clazz.newInstance();
				}
			case IServiceCluster.SERVER_SIDE:
				if ((clazz = (Class) serverClusterMap.get(clusterId)) != null) {
					return (ZclServiceCluster) clazz.newInstance();
				} else if ((backupClientClusterMap != null && (clazz = (Class) backupServerClusterMap.get(clusterId)) != null)) {
					return (ZclServiceCluster) clazz.newInstance();
				} else if ((clazz = (Class) wulianServerClusterMap.get(clusterId)) != null) {
					return (ZclServiceCluster) clazz.newInstance();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
