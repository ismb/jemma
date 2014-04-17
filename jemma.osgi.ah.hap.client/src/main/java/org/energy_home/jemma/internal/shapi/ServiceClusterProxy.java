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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.internal.ah.hap.client.AHM2MHapService;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;

public  class ServiceClusterProxy extends ServiceCluster {
	protected static final Log log = LogFactory.getLog(ServiceClusterProxy.class);
	
	public static boolean isAnUnconfirmedCommand(AHContainerAddress containerAddress) {
		//String containerName = containerAddress.getContainerName();
		return true;
	}
	
	public static String[] getClusterAndAttributeNamesForNotCachedAttributes(AHContainerAddress containerAddress) {
		String containerName = containerAddress.getContainerName();
		String attributeName = null;
		if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_cycleTarget0))
			attributeName = ApplianceControlServer.ATTR_CycleTarget0_NAME;
		else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_cycleTarget1))
			attributeName = ApplianceControlServer.ATTR_CycleTarget1_NAME;
		else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_temperatureTarget0))
			attributeName = ApplianceControlServer.ATTR_TemperatureTarget0_NAME;
		else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_temperatureTarget1))
			attributeName = ApplianceControlServer.ATTR_TemperatureTarget1_NAME;
		if (attributeName != null)
			return new String[] {ApplianceControlServer.class.getName(), attributeName};
			else
		return null;
	}
	
	protected AHM2MHapService ahm2mHapService;
	protected ApplianceProxyList applianceProxyList;
	protected ISubscriptionManager subscriptionManager;
	
	private void manageSubscriptions(String appliancePid, Integer endPointId, String clusterName, String attributeName, ContentInstance ci) {
		if (subscriptionManager.checkActiveSubscriptions()) {
			ContentInstanceItems cisItems = new ContentInstanceItems();
			cisItems.setAddressedId(ahm2mHapService.getLocalAddressedId(appliancePid, endPointId, clusterName, attributeName));
			cisItems.getContentInstances().add(ci);
			subscriptionManager.notifyContentInstanceItems(cisItems);
		}
	}	
	
	public ServiceClusterProxy (ApplianceProxyList applianceProxyList, AHM2MHapService ahm2mHapService, ISubscriptionManager subscriptionManager) throws ApplianceException {
		super();
		this.applianceProxyList = applianceProxyList;
		this.ahm2mHapService = ahm2mHapService;
		this.subscriptionManager = subscriptionManager;
	}
	
	public void initServiceCluster(ApplianceProxy applianceProxy) {	
	}
	
	public void checkServiceCluster(ApplianceProxy applianceProxy) {		
	}
	
	public ContentInstance execCommand(String appliancePid, int endPointId, String containerName, ContentInstance ci) {
		return null;
	}
	
	public void notifyAttributeValue(String appliancePid, int endPointId, String clusterName, String attributeName, long timestamp, Object value, boolean batchRequest) {
		try {
			ContentInstance ci = ahm2mHapService.sendAttributeValue(appliancePid, endPointId, clusterName, attributeName, timestamp, value, batchRequest);	
			manageSubscriptions(appliancePid, endPointId, clusterName, attributeName, ci);		
		} catch (Exception e) {
			log.error("Error in notifyAttributeValue for appliance " + appliancePid, e);
		}
	}
	
	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue, IEndPointRequestContext endPointRequestContext) {
		try {
			IEndPoint peerEndPoint = endPointRequestContext.getPeerEndPoint();
			String appliancePid = peerEndPoint.getAppliance().getPid();
			int endPointId = peerEndPoint.getId();
			notifyAttributeValue(appliancePid, endPointId, HacCommon.getPeerClusterName(getName()), attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), true);		
		} catch (Exception e) {
			log.error("notifyAttributeValue error", e);
		}
	}
	
}
