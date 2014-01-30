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
import org.energy_home.jemma.ah.cluster.zigbee.measurement.TemperatureMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AppliancesInitializationManager {
	protected static final Log log = LogFactory.getLog(AppliancesInitializationManager.class);
	
	public static final ISubscriptionParameters DEFAULT_PARAMS = new SubscriptionParameters(2,120,0);
	private IEndPointRequestContext requestContext;
	
	private static String getClusterDetails(IServiceCluster serviceCluster) {
		IEndPoint endPoint = serviceCluster.getEndPoint();
		return String.format("appliance %s,  endPoint %s, cluster %s", endPoint.getAppliance().getPid(), endPoint.getId(), serviceCluster.getName());
	}
	
	private void subscribe(IServiceCluster serviceCluster, String attributeName) {
		try {
			// Other applications can overwrite default subscription parameters
			if (serviceCluster.getAttributeSubscription(attributeName, requestContext) == null)
				serviceCluster.setAttributeSubscription(attributeName, DEFAULT_PARAMS, requestContext);
			else 
				return;
		} catch (Exception e) {
			log.warn(String.format("Error while subscribing attribute %s from %s", attributeName, getClusterDetails(serviceCluster)));
		}
	}
	
	private void initApplianceControlCluster(IServiceCluster serviceCluster) {
		subscribe(serviceCluster, ApplianceControlServer.ATTR_StartTime_NAME);
		subscribe(serviceCluster, ApplianceControlServer.ATTR_FinishTime_NAME);
		subscribe(serviceCluster, ApplianceControlServer.ATTR_RemainingTime_NAME);
	}
	
	private void initSimpleMeteringCluster(IServiceCluster serviceCluster) {
		SimpleMeteringServer sms = ((SimpleMeteringServer)serviceCluster);
		try {
			sms.getDemandFormatting(requestContext);
		} catch (Exception e) {
			log.error(String.format("Error while reading demand formatting from %s", getClusterDetails(serviceCluster)));
		} 
		try {
			sms.getSummationFormatting(requestContext);
		} catch (Exception e) {
			log.error(String.format("Error while reading summation formatting from %s", getClusterDetails(serviceCluster)));
		}
		subscribe(serviceCluster, SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME);
		subscribe(serviceCluster, SimpleMeteringServer.ATTR_IstantaneousDemand_NAME);
		subscribe(serviceCluster, SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME);
	}
	
	private void initOnOffCluster(IServiceCluster serviceCluster) {
		subscribe(serviceCluster, OnOffServer.ATTR_OnOff_NAME);
	}	
	
	private void initTemperatureMeasurementCluster(IServiceCluster serviceCluster) {
		subscribe(serviceCluster, TemperatureMeasurementServer.ATTR_MeasuredValue_NAME);
	}
	
	public AppliancesInitializationManager(IEndPointRequestContext requestContext) {
		this.requestContext = requestContext;
	}
	
	public void initAppliance(IAppliance appliance) {
		IEndPoint[] eps = appliance.getEndPoints();
		if (eps != null) {
			IEndPoint ep;
			IServiceCluster sc;
			for (int i = 1; i < eps.length; i++) {
				ep = eps[i];
				sc = ep.getServiceCluster(SimpleMeteringServer.class.getName());
				if (sc != null) {
					initSimpleMeteringCluster(sc);
				}
				sc = ep.getServiceCluster(OnOffServer.class.getName());
				if (sc != null) {
					initOnOffCluster(sc);
				}
				sc = ep.getServiceCluster(TemperatureMeasurementServer.class.getName());
				if (sc != null) {
					initTemperatureMeasurementCluster(sc);
				}
				sc = ep.getServiceCluster(ApplianceControlServer.class.getName());
				if (sc != null) {
					initApplianceControlCluster(sc);
				}
			}			
		}
	}

}
