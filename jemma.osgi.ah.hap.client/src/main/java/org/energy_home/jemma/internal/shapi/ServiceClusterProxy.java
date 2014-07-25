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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;

public  class ServiceClusterProxy extends ServiceCluster {
	private static final String CORE_CLUSTERS_ATTRIBUTE_ID_PREFIX = "ah.cluster.ah";
	private static final String CORE_CLUSTERS_ATTRIBUTE_NAME_PREFIX = "org.energy_home.jemma.ah.cluster.ah";
	private static final String TELECOMITALIA_PACKAGE_PREFIX = "org.energy_home.jemma.";
	
	protected static final Logger LOG = LoggerFactory.getLogger( ServiceClusterProxy.class );
	
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
	
	private void manageSubscriptions(String appliancePid, Integer endPointId, String attributeId, ContentInstance ci) {
		if (subscriptionManager.checkActiveSubscriptions()) {
			ContentInstanceItems cisItems = new ContentInstanceItems();
			cisItems.setAddressedId(ahm2mHapService.getLocalAddressedId(appliancePid, endPointId, attributeId));
			cisItems.getContentInstances().add(ci);
			subscriptionManager.notifyContentInstanceItems(cisItems);
		}
	}	
	
	private String getAttributeId(String clusterName, String attributeName) {
		if (clusterName == null || clusterName.equals("")) {
			return attributeName;
		} else if (clusterName.equals(ConfigServer.class.getName())) {
			String attributeId = clusterName + "." + attributeName;
			return attributeId.substring(TELECOMITALIA_PACKAGE_PREFIX.length());
		} else {
			return getAttributeId(attributeName);
		}		
	}
	
	protected String getAttributeId(String attributeName) {
		return null;
	}
	
	protected Object decodeAttributeValue(String appliancePid, int endPointId, String attributeId, Object value) {
		return value;
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
	
	public ContentInstance execCommand(String appliancePid, int endPointId, String containerName, ContentInstance ci) throws ApplianceException, ServiceClusterException {
		return null;
	}
	
	public final void sendAttributeValue(String appliancePid, int endPointId, String clusterName, String attributeName, long timestamp, Object value, boolean batchRequest) {
		try {
			String attributeId = getAttributeId(clusterName, attributeName);
			if (attributeId == null) {
				LOG.debug("Received attribute value for unexported attribute: " + clusterName + ", " + attributeName);
			} else {
				Object decodedValue = decodeAttributeValue(appliancePid, endPointId, attributeName, value);	
				sendAttributeValue(appliancePid, endPointId, attributeId, timestamp, decodedValue, batchRequest);
			}
		} catch (Exception e) {
			LOG.error("Error in notifyAttributeValue for appliance " + appliancePid, e);
		}
	}
	
	public final void sendAttributeValue(String appliancePid, int endPointId, String attributeId, long timestamp, Object value, boolean batchRequest) throws HacException {
		ContentInstance ci = ahm2mHapService.sendAttributeValue(appliancePid, endPointId, attributeId, timestamp, value, batchRequest);	
		manageSubscriptions(appliancePid, endPointId, attributeId, ci);	
	}
	
	public final void notifyAttributeValue(String attributeName, IAttributeValue attributeValue, IEndPointRequestContext endPointRequestContext) {
		try {
			IEndPoint peerEndPoint = endPointRequestContext.getPeerEndPoint();
			String appliancePid = peerEndPoint.getAppliance().getPid();
			int endPointId = peerEndPoint.getId();
			sendAttributeValue(appliancePid, endPointId, HacCommon.getPeerClusterName(getName()), attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), true);		
		} catch (Exception e) {
			LOG.error("notifyAttributeValue error", e);
		}
	}
	
}
