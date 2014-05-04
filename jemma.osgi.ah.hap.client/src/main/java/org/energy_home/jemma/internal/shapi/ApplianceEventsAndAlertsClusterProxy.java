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

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceEventsAndAlertsClient;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hap.client.AHContainers;


public class ApplianceEventsAndAlertsClusterProxy extends ServiceClusterProxy implements ApplianceEventsAndAlertsClient {

	public ApplianceEventsAndAlertsClusterProxy(ApplianceProxyList applianceProxyList, AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
	}
	
	public void execAlertsNotification(int[] alerts, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		IEndPoint peerEndPoint = context.getPeerEndPoint();
		int endPointId = peerEndPoint.getId();
		String appliancePid = peerEndPoint.getAppliance().getPid();
		try {
			if (alerts.length > 0) {
				super.sendAttributeValue(appliancePid, endPointId, AHContainers.attrId_ah_cluster_applevents_alerts, System.currentTimeMillis(),
						new Integer(alerts[0]), true);
			}
		} catch (Exception e) {
			LOG.error("Error while receiving execAlertsNotification for appliance " + appliancePid + ", endPoint " + endPointId, e);
		}		
	}

	public void execEventNotification(short EventHeader, short EventIdentification, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		IEndPoint peerEndPoint = context.getPeerEndPoint();
		int endPointId = peerEndPoint.getId();
		String appliancePid = peerEndPoint.getAppliance().getPid();
		try {
			super.sendAttributeValue(appliancePid, endPointId, AHContainers.attrId_ah_cluster_applevents_event, System.currentTimeMillis(),
				new Short(EventIdentification), true);
		} catch (Exception e) {
			LOG.error("Error while receiving execAlertsNotification for appliance " + appliancePid + ", endPoint " + endPointId, e);
		}
	}

}
