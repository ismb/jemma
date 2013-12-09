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
package org.energy_home.jemma.internal.ah.eh.esp;

import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.ebrain.IOnOffListener;
import org.energy_home.jemma.ah.ebrain.IOnOffProxy;

public class OnOffClusterProxy extends ServiceClusterProxy implements OnOffClient, IServiceClusterListener, IOnOffProxy {
	private static final Log log = LogFactory.getLog(OnOffClusterProxy.class);
	private ApplianceProxyList proxyList;
	private IOnOffListener listener;
	
	private OnOffServer getMeteringServerCluster(ApplianceProxy applianceProxy) {
		// TODO: needs to be modified to manage multi end point devices
		return (OnOffServer) getServiceCluster(applianceProxy, IEndPoint.DEFAULT_END_POINT_ID, OnOffServer.class.getName());
	}
	
	public OnOffClusterProxy(ApplianceProxyList proxy, IOnOffListener listener) throws ApplianceException {
		this.proxyList = proxy;
		this.listener = listener;
	}
	
	public Boolean getStatus(String applianceId) {
		Boolean value = null;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			OnOffServer ofs = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			value = new Boolean(ofs.getOnOff(context));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
		
	}

	public Boolean setStatus(String applianceId, Boolean value) {
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			OnOffServer ofs = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			if (value)
				ofs.execOn(context);
			else
				ofs.execOff(context);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return value;
	}
	
	public void notifyAttributeValue(String attributeName, IAttributeValue peerAttributeValue, IEndPointRequestContext endPointRequestContext) {
		try {
			String appliancePid = getApplianceId(endPointRequestContext);
			if (attributeName.equals(OnOffServer.ATTR_OnOff_NAME)) {
				Boolean value = (Boolean)peerAttributeValue.getValue();
				listener.notifyStatus(appliancePid, peerAttributeValue.getTimestamp(), value);
			} else {
				log.warn("notifyAttributeValue - Received value for unmanaged attribute (" + attributeName + ") - " + appliancePid);
			}			
		} catch (Exception e) {
			log.error("notifyAttributeValue error", e);
		}
	}

	public void subscribeStatus(String applianceId, long minReportingInterval, long maxReportingInterval) {
		SubscriptionParameters params = new SubscriptionParameters(minReportingInterval, maxReportingInterval, 0);
		// TODO: needs to be extended to manage multiple end points devices
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			IServiceCluster serviceCluster = (IServiceCluster) getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			serviceCluster.setAttributeSubscription(OnOffServer.ATTR_OnOff_NAME, params, context);
		} catch (Exception e) {
			log.error("subscribeStatus error for appliance " + applianceId, e);
		}
	}
}
