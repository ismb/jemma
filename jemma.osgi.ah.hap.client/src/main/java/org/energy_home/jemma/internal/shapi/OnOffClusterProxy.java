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

import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.m2m.ContentInstance;


public class OnOffClusterProxy extends ServiceClusterProxy implements OnOffClient {

	public OnOffClusterProxy(ApplianceProxyList applianceProxyList,
			AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
	}

	protected String getAttributeId(String attributeName) {
		if (attributeName.equals(OnOffServer.ATTR_OnOff_NAME))
			return AHContainers.attrId_ah_cluster_onoff_status;
		return null;
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
			serviceCluster = eps[j].getServiceCluster(OnOffServer.class.getName());
			if (serviceCluster != null)
				try {
					av = serviceCluster.getLastNotifiedAttributeValue(OnOffServer.ATTR_OnOff_NAME, context);
					if (av != null && av.getValue() != null) {
						sendAttributeValue(appliancePid, eps[j].getId(), OnOffServer.class.getName(), OnOffServer.ATTR_OnOff_NAME, av.getTimestamp(), av.getValue(), true);
					}
				} catch (Exception e) {
					LOG.error("Error while reading last notified onoff attribute value for appliance " + appliancePid,e);
				}
		}
	}
	
	public ContentInstance execCommand(String appliancePid, int endPointId, String containerName, ContentInstance ci) {
		try {
			ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
			IAppliance appliance = applianceProxy.getAppliance();
			if (!appliance.isAvailable())
				return null;
			IEndPoint endPoint = appliance.getEndPoint(endPointId);
			if (containerName.equals(AHContainers.attrId_ah_cluster_onoff_status)) {
				Boolean value = (Boolean) ci.getContent();
				IServiceCluster onOffServer = endPoint.getServiceCluster(OnOffServer.class.getName()); 
				if (onOffServer != null && onOffServer.isAvailable()) {
					if (value)
						((OnOffServer)onOffServer).execOn(applianceProxy.getApplicationRequestContext());
					else
						((OnOffServer)onOffServer).execOff(applianceProxy.getApplicationRequestContext());
				} else {
					return null;
				}
			}
			return ci;
		} catch (Exception e) {
			LOG.error("Error shile managing onoff command for appliance " + appliancePid + ", end point " + endPointId  + ", container " + containerName, e);
			return null;
		}
	}
}
