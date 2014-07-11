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


import java.util.Calendar;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.SignalStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.WriteAttributeRecord;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;

public class ApplianceControlClusterProxy extends ServiceClusterProxy implements ApplianceControlClient {
	public boolean isRemoteControlEnabled (short remoteEnabledFlags) {
		int maskedValue = remoteEnabledFlags & 0xf;
		return (maskedValue == 0x1 || maskedValue == 0xf);
	}
	
	public short toApplianceCommand(short applianceStatus) {
		short applianceCommand;
		switch (applianceStatus) {
		// Running status
		case 0x05:
			applianceCommand = 0x01; // Start command
			break;
		// Pause status
		case 0x06:
			applianceCommand = 0x03; // Pause command
			break;
		// Idle status
		case 0x0a:
			applianceCommand = 0x02; // Stop command
			break;
		// OverloadPause
		case 0x0100:
			applianceCommand = 0x03; // Pause command
			break;
		// OverloadPauseResume
		case 0x0101:
			applianceCommand = 0x01; // Start command
			break;
		default:
			applianceCommand = -1;
		}
		return applianceCommand;
	}
	
	public synchronized long fromApplianceTime(int time, boolean returnMinutes) {
		if (time == 0)
			return 0;
		boolean relative = (time & 0xC0) == 0; 
		int hours = (time & 0xFF00) >> 8;
		int minutes = time & 0x003F;
		// Approximate to minutes
		calendar.setTimeInMillis((System.currentTimeMillis()/60000)*60000);
		if (relative) {
			if (returnMinutes) 
				return minutes+hours*60;
			calendar.add(Calendar.HOUR_OF_DAY, hours);
			calendar.add(Calendar.MINUTE, minutes);
		} else {
			calendar.set(Calendar.HOUR_OF_DAY, hours);
			calendar.set(Calendar.MINUTE, minutes);
		}
		if (returnMinutes) {
			long deltaMillisecs = calendar.getTimeInMillis() - System.currentTimeMillis();
			if (deltaMillisecs < 0)
				deltaMillisecs = 0;
			return deltaMillisecs/60000;
		} else {
			return calendar.getTimeInMillis();			
		}
	}
	
	public synchronized int toApplianceTime(int relativeMinutes, boolean relative) {
		int hours, minutes, mask, result;
		if (relative) {
			hours = relativeMinutes / 60;
			minutes = relativeMinutes % 60;
			calendar.setTimeInMillis(System.currentTimeMillis());
			mask = 0x00;
		} else {
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.MINUTE, relativeMinutes);
			hours = calendar.get(Calendar.HOUR_OF_DAY);
			minutes = calendar.get(Calendar.MINUTE);
			mask = 0x40;
		}
		result = (hours << 8) | mask | (minutes & 0x3F); 
		return result;
	}
	public synchronized int toApplianceTime(long time, boolean relative) {
		if (time == 0)
			return 0;
		int hours, minutes, mask, result;
		if (relative) {
			calendar.setTimeInMillis(System.currentTimeMillis());
			int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
			int currentMinutes = calendar.get(Calendar.MINUTE);
			calendar.setTimeInMillis(time);
			hours = calendar.get(Calendar.HOUR_OF_DAY)-currentHour;
			if (hours < 0) {
				hours = 0;
			}
			minutes = calendar.get(Calendar.MINUTE) - currentMinutes;
			if (minutes < 0) { 
				if (hours == 0) {
					minutes = 0;
				} else {
					hours -= 1;
					minutes += 60;
				}
			}
			mask = 0x00;
		} else {
			calendar.setTimeInMillis(time);
			hours = calendar.get(Calendar.HOUR_OF_DAY);
			minutes = calendar.get(Calendar.MINUTE);
			mask = 0x40;
		}
		result = (hours << 8) | mask | (minutes & 0x3F); 
		return result;
	}
	
	private Calendar calendar;
	
	public ApplianceControlClusterProxy(
			ApplianceProxyList applianceProxyList,
			AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
		calendar  = Calendar.getInstance();
	}

	protected String getAttributeId(String attributeName) {
		if (attributeName.equals(ApplianceControlServer.ATTR_StartTime_NAME))
			return AHContainers.attrId_ah_cluster_applctrl_startTime;
		if (attributeName.equals(ApplianceControlServer.ATTR_FinishTime_NAME))
			return AHContainers.attrId_ah_cluster_applctrl_finishTime;
		if (attributeName.equals(ApplianceControlServer.ATTR_RemainingTime_NAME))
			return AHContainers.attrId_ah_cluster_applctrl_remainingTime;
		return null;
	}

	@Override
	protected Object decodeAttributeValue(String appliancePid, int endPointId, String attributeName, Object value) {
		if (attributeName.equals(ApplianceControlServer.ATTR_StartTime_NAME) ||
				attributeName.equals(ApplianceControlServer.ATTR_FinishTime_NAME) ||
				attributeName.equals(ApplianceControlServer.ATTR_RemainingTime_NAME)) {
			try {
				if (attributeName.equals(ApplianceControlServer.ATTR_RemainingTime_NAME)) {
					int time = (int) fromApplianceTime(((Integer)value).intValue(), true);
					return new Integer(time);
				} else {
					long time = fromApplianceTime(((Integer)value).intValue(), false);
					return new Long(time);
				}
			} catch (Exception e) {
				LOG.error("Error while notifying time attribute for appliance " + appliancePid + ", endPoint " + endPointId, e);
			}
		} 
		return value;
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
			serviceCluster = eps[j].getServiceCluster(ApplianceControlServer.class.getName());		
			if (serviceCluster != null) {
				try {
					av = serviceCluster.getLastNotifiedAttributeValue(ApplianceControlServer.ATTR_StartTime_NAME, context);
					if (av != null && av.getValue() != null) {
						sendAttributeValue(appliancePid, eps[j].getId(), ApplianceControlServer.class.getName(), 
								ApplianceControlServer.ATTR_StartTime_NAME, av.getTimestamp(), av.getValue(), true);
					}
					av = serviceCluster.getLastNotifiedAttributeValue(ApplianceControlServer.ATTR_FinishTime_NAME, context);
					if (av != null && av.getValue() != null) {
						sendAttributeValue(appliancePid, eps[j].getId(), ApplianceControlServer.class.getName(), 
								ApplianceControlServer.ATTR_FinishTime_NAME, av.getTimestamp(), av.getValue(), true);
					}
					av = serviceCluster.getLastNotifiedAttributeValue(ApplianceControlServer.ATTR_RemainingTime_NAME, context);
					if (av != null && av.getValue() != null) {
						sendAttributeValue(appliancePid, eps[j].getId(), ApplianceControlServer.class.getName(), 
								ApplianceControlServer.ATTR_RemainingTime_NAME, av.getTimestamp(), av.getValue(), true);
					}
					SignalStateResponse stateResponse = ((ApplianceControlServer)serviceCluster).execSignalState(applianceProxy.getApplicationRequestContext());
					if (stateResponse != null) {
						sendAttributeValue(appliancePid,  eps[j].getId(), AHContainers.attrId_ah_cluster_applctrl_status, 
								System.currentTimeMillis(), stateResponse.ApplianceStatus, true);
						sendAttributeValue(appliancePid, eps[j].getId(), AHContainers.attrId_ah_cluster_applctrl_remoteControlEnabled, System.currentTimeMillis(), 
								isRemoteControlEnabled(stateResponse.RemoteEnableFlags), true);
					}
				} catch (Exception e) {
					LOG.error("Error while reading last notified appliance control attribute values for appliance " + appliance.getPid(),e);
				}				
			}
		}	
	}
	
	public void checkServiceCluster(ApplianceProxy applianceProxy) {
		IAppliance appliance = applianceProxy.getAppliance();
		if (!appliance.isAvailable())
			return;
		String appliancePid = appliance.getPid();
		IEndPoint[] eps = appliance.getEndPoints();
		ApplianceControlServer applianceControlServer = null;
		for (int j = 1; j < eps.length; j++) {
			applianceControlServer = (ApplianceControlServer) eps[j].getServiceCluster(ApplianceControlServer.class.getName());
			if (applianceControlServer != null)
				try {
					SignalStateResponse stateResponse = applianceControlServer.execSignalState(applianceProxy.getApplicationRequestContext());
					if (stateResponse != null) {
						sendAttributeValue(appliancePid, eps[j].getId(), AHContainers.attrId_ah_cluster_applctrl_status, System.currentTimeMillis(), 
								stateResponse.ApplianceStatus, true);
						sendAttributeValue(appliancePid, eps[j].getId(), AHContainers.attrId_ah_cluster_applctrl_remoteControlEnabled, System.currentTimeMillis(), 
								isRemoteControlEnabled(stateResponse.RemoteEnableFlags), true);
					}
				} catch (Exception e) {
					LOG.error("Error while retrieving appliance status for appliance" + appliance.getPid(),e);
				}
		}
	}
	
	public ContentInstance execCommand(String appliancePid, int endPointId, String containerName, ContentInstance ci) 
			throws ApplianceException, ServiceClusterException {
		ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
		IAppliance appliance = applianceProxy.getAppliance();
		if (!appliance.isAvailable())
			return null;
		IEndPoint endPoint = appliance.getEndPoint(endPointId);
		DeviceCategory deviceCategory = ahm2mHapService.getDeviceCategory(appliancePid, endPointId);
		if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_status)) {
			Short value = (Short) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				short command = toApplianceCommand(value.shortValue());
				if (command > 0)
					((ApplianceControlServer)applianceControl).execCommandExecution(command,  applianceProxy.getApplicationRequestContext());
				else 
					return null;
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_startTime)) {
			Long value = (Long) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_StartTime_NAME;
				record.value = toApplianceTime(value, true); //deviceCategory == null || !(deviceCategory == deviceCategory.Oven));
				IEndPointRequestContext epCtxt = applianceProxy.getApplicationRequestContext();
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, epCtxt);
				((ApplianceControlServer)applianceControl).execCommandExecution((short)0x01, epCtxt);

			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_finishTime)) {
			Long value = (Long) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_FinishTime_NAME;
				record.value = toApplianceTime(value, true); // deviceCategory == null || !(deviceCategory == deviceCategory.Oven));
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_remainingTime)) {
			Integer value = (Integer) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_RemainingTime_NAME;
				record.value = toApplianceTime(value, true);// deviceCategory == null || !(deviceCategory == deviceCategory.Oven));
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_cycleTarget0)) {
			Short value = (Short) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_CycleTarget0_NAME;
				record.value = value;
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_cycleTarget1)) {
			Short value = (Short) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_CycleTarget1_NAME;
				record.value = value;
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_temperatureTarget0)) {
			Integer value = (Integer) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_TemperatureTarget0_NAME;
				record.value = value;
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		} else if (containerName.equals(AHContainers.attrId_ah_cluster_applctrl_temperatureTarget1)) {
			Integer value = (Integer) ci.getContent();
			IServiceCluster applianceControl = applianceProxy.getServiceCluster(endPointId, ApplianceControlServer.class.getName());
			if (applianceControl != null && applianceControl.isAvailable()) {
				WriteAttributeRecord record = new WriteAttributeRecord();
				record.name = ApplianceControlServer.ATTR_TemperatureTarget1_NAME;
				record.value = value;
				((ApplianceControlServer)applianceControl).execWriteFunctions(new WriteAttributeRecord[] {record}, applianceProxy.getApplicationRequestContext());
			} else {
				return null;
			}
		}
		return ci;
	}
	

	public void execSignalStateNotification(short ApplianceStatus,
			short RemoteEnableFlags, int ApplianceStatus2,
			IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		IEndPoint peerEndPoint = context.getPeerEndPoint();
		int endPointId = peerEndPoint.getId();
		String appliancePid = peerEndPoint.getAppliance().getPid();
		try {
			sendAttributeValue(appliancePid, endPointId, AHContainers.attrId_ah_cluster_applctrl_status, System.currentTimeMillis(),
				new Short(ApplianceStatus), true);
			sendAttributeValue(appliancePid, endPointId, AHContainers.attrId_ah_cluster_applctrl_remoteControlEnabled, System.currentTimeMillis(), 
					isRemoteControlEnabled(RemoteEnableFlags), true);
		} catch (Exception e) {
			LOG.error("Error while receiving signal state notification appliance status for appliance " + appliancePid + ", endPoint " + endPointId, e);
		}	
	}

}
