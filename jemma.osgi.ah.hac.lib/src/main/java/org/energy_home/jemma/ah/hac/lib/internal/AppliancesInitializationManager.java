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
package org.energy_home.jemma.ah.hac.lib.internal;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.IlluminanceMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.OccupancySensingServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.RelativeHumidityMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.TemperatureMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneServer;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppliancesInitializationManager {
	protected static final Logger LOG = LoggerFactory.getLogger(AppliancesInitializationManager.class);
	
	protected static boolean enableAutoInstall = false;
	
	private static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}
	
	static {
		String enableAutoInstallStr = System.getProperty("org.energy_home.jemma.ah.driver.autoinstall");
		if (!isNullOrEmpty(enableAutoInstallStr)) {
			try {
				enableAutoInstall = Boolean.parseBoolean(enableAutoInstallStr);
			} catch (Exception e) {
				LOG.warn(e.getMessage(), e);
			}
		}
	}

	
	private IEndPointRequestContext requestContext;
	
	private static String getClusterDetails(IServiceCluster serviceCluster) {
		IEndPoint endPoint = serviceCluster.getEndPoint();
		return String.format("appliance %s,  endPoint %s, cluster %s", endPoint.getAppliance().getPid(), endPoint.getId(), serviceCluster.getName());
	}
	
	private void subscribe(IServiceCluster serviceCluster, String attributeName, boolean installing) {
		subscribe(serviceCluster, attributeName, installing, null);
	}
	
	private void subscribe(IServiceCluster serviceCluster, String attributeName, boolean installing, ISubscriptionParameters params) {
		if (params == null)
			params = ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS;
		try {
			if (enableAutoInstall || installing || !(serviceCluster instanceof ServiceCluster)) {
				serviceCluster.setAttributeSubscription(attributeName, params, requestContext);				
			} else if (serviceCluster.getAttributeSubscription(attributeName, requestContext) == null) {
				// Other applications can overwrite default subscription parameters
				((ServiceCluster)serviceCluster).initAttributeSubscription(attributeName, params, requestContext);			
			}
		} catch (Exception e) {
			LOG.warn(String.format("Error while subscribing attribute %s from %s", attributeName, getClusterDetails(serviceCluster)), e);
		}
	}
	
	private void initApplianceControlCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, ApplianceControlServer.ATTR_StartTime_NAME, installing);
		subscribe(serviceCluster, ApplianceControlServer.ATTR_FinishTime_NAME, installing);
		subscribe(serviceCluster, ApplianceControlServer.ATTR_RemainingTime_NAME, installing);
	}
	
	private void initSimpleMeteringCluster(IServiceCluster serviceCluster, boolean installing) {
		SimpleMeteringServer sms = ((SimpleMeteringServer)serviceCluster);
		try {
			sms.getDemandFormatting(requestContext);
		} catch (Exception e) {
			LOG.warn(String.format("Error while reading demand formatting from %s", getClusterDetails(serviceCluster)));
		} 
		try {
			sms.getSummationFormatting(requestContext);
		} catch (Exception e) {
			LOG.warn(String.format("Error while reading summation formatting from %s", getClusterDetails(serviceCluster)));
		}
		subscribe(serviceCluster, SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, installing);
		subscribe(serviceCluster, SimpleMeteringServer.ATTR_IstantaneousDemand_NAME, installing);
	}
	
	private void initOnOffCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, OnOffServer.ATTR_OnOff_NAME, installing);
	}	
	
	private void initTemperatureMeasurementCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, TemperatureMeasurementServer.ATTR_MeasuredValue_NAME, installing);
	}
	
	private void initRelativeHumidityMeasurementCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, RelativeHumidityMeasurementServer.ATTR_MeasuredValue_NAME, installing);
	}
	
	private void initIlluminanceMeasurementCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, IlluminanceMeasurementServer.ATTR_MeasuredValue_NAME, installing);
	}
	
	private void initIASZoneCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, IASZoneServer.ATTR_ZoneStatus_NAME, installing);
	}
	
	private void initOccupancySensingCluster(IServiceCluster serviceCluster, boolean installing) {
		subscribe(serviceCluster, OccupancySensingServer.ATTR_Occupancy_NAME, installing);
	}
	
	public AppliancesInitializationManager(IEndPointRequestContext requestContext) {
		this.requestContext = requestContext;
	}
	
	public void initAppliance(IAppliance appliance, boolean installing) {
		IEndPoint[] eps = appliance.getEndPoints();
		if (eps != null) {
			IEndPoint ep;
			IServiceCluster sc;
			for (int i = 1; i < eps.length; i++) {
				ep = eps[i];
				sc = ep.getServiceCluster(SimpleMeteringServer.class.getName());
				if (sc != null) {
					initSimpleMeteringCluster(sc, installing);
				}
				sc = ep.getServiceCluster(OnOffServer.class.getName());
				if (sc != null) {
					initOnOffCluster(sc, installing);
				}
				sc = ep.getServiceCluster(TemperatureMeasurementServer.class.getName());
				if (sc != null) {
					initTemperatureMeasurementCluster(sc, installing);
				}
				sc = ep.getServiceCluster(RelativeHumidityMeasurementServer.class.getName());
				if (sc != null) {
					initRelativeHumidityMeasurementCluster(sc, installing);
				}
				sc = ep.getServiceCluster(IlluminanceMeasurementServer.class.getName());
				if (sc != null) {
					initIlluminanceMeasurementCluster(sc, installing);
				}
				sc = ep.getServiceCluster(IASZoneServer.class.getName());
				if (sc != null) {
					initIASZoneCluster(sc, installing);
				}
				sc = ep.getServiceCluster(OccupancySensingServer.class.getName());
				if (sc != null) {
					initOccupancySensingCluster(sc, installing);
				}
				sc = ep.getServiceCluster(ApplianceControlServer.class.getName());
				if (sc != null) {
					initApplianceControlCluster(sc, installing);
				}
			}			
		}
	}

}
