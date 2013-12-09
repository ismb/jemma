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

import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.ebrain.IMeteringListener;
import org.energy_home.jemma.ah.ebrain.IMeteringProxy;

public class MeteringClusterProxy extends ServiceClusterProxy implements SimpleMeteringClient, IServiceClusterListener, IMeteringProxy {
	private static final Log log = LogFactory.getLog(MeteringClusterProxy.class);

	// general zigbee rules to convert fixed decimals in floating decimals
	// NOTE: also converts from KW/H to W/H
	public static float interptetFormatting(short formatting) {
		int decimals = formatting & 0x7;
		if (decimals > 0) return (float)(1000 / Math.pow(10, decimals));
		return 1000;
	}
	
	private ApplianceProxyList proxyList;
	private IMeteringListener listener;
	
	private SimpleMeteringServer getMeteringServerCluster(ApplianceProxy applianceProxy) {
		// TODO: needs to be modified to manage multi end point devices
		return (SimpleMeteringServer) getServiceCluster(applianceProxy, IEndPoint.DEFAULT_END_POINT_ID, SimpleMeteringServer.class.getName());
	}
	
	public MeteringClusterProxy(ApplianceProxyList proxy, IMeteringListener listener) throws ApplianceException {
		super();
		this.proxyList = proxy;
		this.listener = listener;
	}
	
	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) {
		try {
			String appliancePid = getApplianceId(endPointRequestContext);
			if (attributeName.equals(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME)) {
				float value = ((Integer)attributeValue.getValue()).floatValue();
				listener.notifyIstantaneousDemandPower(appliancePid, attributeValue.getTimestamp(), value);
			} else if (attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME)) {
				double value = ((Long)attributeValue.getValue()).doubleValue();
				listener.notifyCurrentSummationDelivered(appliancePid, attributeValue.getTimestamp(), value);		
			} else if (attributeName.equals(SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME)) {
				double value = ((Long)attributeValue.getValue()).doubleValue();
				listener.notifyCurrentSummationReceived(appliancePid, attributeValue.getTimestamp(), value);
			} else {
				log.warn("notifyAttributeValue - Received value for unmanaged attribute (" + attributeName + ") - " + appliancePid);
			}			
		} catch (Exception e) {
			log.error("notifyAttributeValue error", e);
		}
	}
	
	public short getPowerFactor(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public short getStatus(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public short getUnitOfMeasure(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public short getMeteringDeviceType(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public float getSummationFormatting(String applianceId) {

		float decimalFormatting = INVALID_FORMATTING_VALUE;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			SimpleMeteringServer sms = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			short zigbeeFormating = sms.getSummationFormatting(context);
			decimalFormatting = interptetFormatting(zigbeeFormating);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decimalFormatting;
	}

	
	public double getCurrentSummationDelivered(String applianceId) {
		double value = INVALID_ENERGY_CONSUMPTION_VALUE;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			SimpleMeteringServer sms = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			value = sms.getCurrentSummationDelivered(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public double getCurrentSummationReceived(String applianceId) {
		double value = INVALID_ENERGY_CONSUMPTION_VALUE;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			SimpleMeteringServer sms = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			value = sms.getCurrentSummationReceived(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}	
	
	public float getDemandFormatting(String applianceId) {
		float decimalFormatting = INVALID_FORMATTING_VALUE;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			SimpleMeteringServer sms = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			short zigbeeFormating = sms.getDemandFormatting(context);
			decimalFormatting = interptetFormatting(zigbeeFormating);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decimalFormatting;
	}

	
	public float getIstantaneousDemand(String applianceId) {
		float value = INVALID_INSTANTANEOUS_POWER_VALUE;
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			SimpleMeteringServer sms = getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			value = sms.getIstantaneousDemand(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	
	public void subscribeIstantaneousDemand(String applianceId, long minReportingInterval, long maxReportingInterval, float deltaValue) {
		SubscriptionParameters params = new SubscriptionParameters(minReportingInterval, maxReportingInterval, deltaValue);
		// TODO: needs to be extended to manage multiple end points devices
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			IServiceCluster serviceCluster = (IServiceCluster) getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			serviceCluster.setAttributeSubscription(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME, params, context);
		} catch (Exception e) {
			log.error("subscribeIstantaneousDemand error for appliance " + applianceId, e);
		}
	}
	
	public void subscribeCurrentSummationDelivered(String applianceId, long minReportingInterval, long maxReportingInterval, double deltaValue) {
		SubscriptionParameters params = new SubscriptionParameters(minReportingInterval, maxReportingInterval, deltaValue);
		// TODO: needs to be extended to manage multiple end points devices
		try {
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			IServiceCluster serviceCluster = (IServiceCluster) getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			serviceCluster.setAttributeSubscription(SimpleMeteringServer.ATTR_CurrentSummationDelivered_NAME, params, context);
		} catch (Exception e) {
			log.error("subscribeCurrentSummationDelivered error for appliance " + applianceId, e);
		}
	}	
	
	public void subscribeCurrentSummationReceived(String applianceId, long minReportingInterval, long maxReportingInterval, double deltaValue) {
		SubscriptionParameters params = new SubscriptionParameters(minReportingInterval, maxReportingInterval, deltaValue);
		// TODO: needs to be extended to manage multiple end points devices
		try {		
			ApplianceProxy applianceProxy = proxyList.getApplianceProxy(applianceId);
			IServiceCluster serviceCluster = (IServiceCluster) getMeteringServerCluster(applianceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(applianceProxy, true);
			serviceCluster.setAttributeSubscription(SimpleMeteringServer.ATTR_CurrentSummationReceived_NAME, params, context);
		} catch (Exception e) {
			log.error("subscribeCurrentSummationReceived error for appliance " + applianceId, e);
		}
	}

}
