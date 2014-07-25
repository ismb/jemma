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
package org.energy_home.jemma.internal.shapi;

import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;


public class MeteringClusterProxy extends ServiceClusterProxy implements SimpleMeteringClient {
	public static float interpretFormatting(short formatting) {
		int decimals = formatting & 0x7;
		if (decimals > 0) return (float)(1000 / Math.pow(10, decimals));
		return 1000;
	}
	
	public MeteringClusterProxy(ApplianceProxyList applianceProxyList,
			AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
	}

	protected String getAttributeId(String attributeName) {
		if (attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME))
			return AHContainers.attrId_ah_cluster_metering_deliveredEnergySum;
		if (attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME))
			return AHContainers.attrId_ah_cluster_metering_receivedEnergySum;
		if (attributeName.equals(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME))
			return AHContainers.attrId_ah_cluster_metering_deliveredPower;
		return null;
	}

	protected Object decodeAttributeValue(String appliancePid, int endPointId, String attributeName, Object value) {
		ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
		IAppliance appliance = applianceProxy.getAppliance();
		IEndPoint endPoint = appliance.getEndPoint(endPointId);
		if (endPoint != null) {
			DeviceCategory deviceCategory = null;
			if (endPoint.getType().equals(IEndPointTypes.ZIGBEE_METERING_DEVICE))
				deviceCategory = ahm2mHapService.getDeviceCategory(appliance.getPid(), endPointId);
			SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer)endPoint.getServiceCluster(SimpleMeteringServer.class.getName()); 
			if (simpleMeteringServer != null) {
				if (attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME) ||
						attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME)) {
					try {
						short summationFormatting = simpleMeteringServer.getSummationFormatting(applianceProxy.getLastReadApplicationRequestContext());			
						Double energy = interpretFormatting(summationFormatting)*((Long)value).doubleValue(); 
						return energy;
					} catch (Exception e) {
						LOG.error("Error while reading summation formatting for appliance " + applianceProxy.getAppliance().getPid() + ", endPoint " + endPointId, e);
					}
				} else if (attributeName.equals(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME) 
						&& (deviceCategory == null || deviceCategory != DeviceCategory.ProductionMeter)){
					try {
						short demandFormatting = simpleMeteringServer.getDemandFormatting(applianceProxy.getLastReadApplicationRequestContext());			
						Float power = interpretFormatting(demandFormatting)*((Integer)value).floatValue(); 
						return power;
					} catch (Exception e) {
						LOG.error("Error while reading demand formatting for appliance " + applianceProxy.getAppliance().getPid()  + ", endPoint " + endPointId, e);
					}
				}
			}
		}
		return value;
	}
	
	public void notifyAttributeValue(String appliancePid, int endPointId, String clusterName, String attributeName, long timestamp, Object value, boolean isBatch) {

	}

	public void initServiceCluster(ApplianceProxy applianceProxy) {
		IAppliance appliance = applianceProxy.getAppliance();
		if (!appliance.isAvailable())
			return;
		String appliancePid = appliance.getPid();
		IEndPoint[] eps = appliance.getEndPoints();
		IEndPointRequestContext context = applianceProxy.getApplicationRequestContext();
		IServiceCluster serviceCluster;
		IAttributeValue av;
		for (int j = 1; j < eps.length; j++) {
			serviceCluster = eps[j].getServiceCluster(SimpleMeteringServer.class.getName());
			if (serviceCluster != null) {
				try {
					av = serviceCluster.getLastNotifiedAttributeValue(SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, context);
					if (av != null && av.getValue() != null) {
						notifyAttributeValue(appliancePid, eps[j].getId(), SimpleMeteringServer.class.getName(), 
								SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, av.getTimestamp(), av.getValue(), true);
					}
					av = serviceCluster.getLastNotifiedAttributeValue(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME, context);
					if (av != null && av.getValue() != null) {
						notifyAttributeValue(appliancePid, eps[j].getId(), SimpleMeteringServer.class.getName(), 
								SimpleMeteringServer.ATTR_IstantaneousDemand_NAME, av.getTimestamp(), av.getValue(), true);
					}
					av = serviceCluster.getLastNotifiedAttributeValue(SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME, context);
					if (av != null && av.getValue() != null) {
						notifyAttributeValue(appliancePid, eps[j].getId(), SimpleMeteringServer.class.getName(), 
								SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME, av.getTimestamp(), av.getValue(), true);
					}
				} catch (Exception e) {
					LOG.error("Error while reading last notified simple metering attribute values for appliance " + appliance.getPid(),e);
				}
			}
		}
	}
}
