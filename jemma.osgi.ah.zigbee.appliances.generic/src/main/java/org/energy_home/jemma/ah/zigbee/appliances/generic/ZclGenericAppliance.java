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
package org.energy_home.jemma.ah.zigbee.appliances.generic;


import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPoint;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPointFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZclGenericAppliance extends ZclAppliance {
	private static final Logger LOG = LoggerFactory.getLogger( ZclGenericAppliance.class );

	private static final String ZIGBEE_CONFIG_PROPERTY_NAME = IAppliance.APPLIANCE_CUSTOM_PROPERTIES_PREXIF+"config";
	
	private static final String SEPARATOR = "|";
	private static final String EMPTY_VALUE = "/";
	private static final String ARRAY_SEPARATOR = ",";
	
	static class ZclEndPointDescriptor {
		int appEndPointId;
		int endPointId;
		int profileId;
		int deviceId;
		int[] clientClusterIds;
		int[] serverClusterIds;
		
		static private int[] parseIntArray(String strArray) {
			if (strArray.equals(EMPTY_VALUE))
				return new int[0];
			StringTokenizer st = new StringTokenizer(strArray, ARRAY_SEPARATOR);
			int[] result = new int[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				result[i] = Integer.parseInt(st.nextToken());
				i++;
			}
			return result;
		}
		
		static private void appendIntArray(int[] value, StringBuilder sb) {
			if (value == null || value.length == 0) {
				sb.append(EMPTY_VALUE);
			} else {
				for (int i = 0; i < value.length; i++) {
					sb.append(value[i]);
					if (i<value.length-1)
						sb.append(ARRAY_SEPARATOR);
				}
			}
		}
		
		public ZclEndPointDescriptor(int appEndPointId, int profileId, int deviceId, int endPointId, int[] clientClusterIds, int[] serverClusterIds)
		{
			this.appEndPointId = appEndPointId;
			this.endPointId = endPointId;
			this.profileId = profileId;
			this.deviceId = deviceId;
			this.clientClusterIds = clientClusterIds;
			this.serverClusterIds = serverClusterIds;
		}
		
		public ZclEndPointDescriptor(String value) throws ApplianceException {
			StringTokenizer st = new StringTokenizer(value, SEPARATOR);
			try {				
				this.appEndPointId = Integer.parseInt(st.nextToken());
				this.profileId = Integer.parseInt(st.nextToken());
				this.deviceId = Integer.parseInt(st.nextToken());
				this.endPointId = Integer.parseInt(st.nextToken());
				clientClusterIds = parseIntArray(st.nextToken());
				serverClusterIds = parseIntArray(st.nextToken());
			} catch (Exception e) {
				throw new ApplianceException("Invalid end point configuration");
			}
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder(String.valueOf(SEPARATOR));
			sb.append(appEndPointId);
			sb.append(SEPARATOR);
			sb.append(profileId);
			sb.append(SEPARATOR);
			sb.append(deviceId);
			sb.append(SEPARATOR);
			sb.append(endPointId);
			sb.append(SEPARATOR);
			appendIntArray(clientClusterIds, sb);
			sb.append(SEPARATOR);
			appendIntArray(serverClusterIds, sb);
			sb.append(SEPARATOR);
			return sb.toString();
		}
	}
	
	private static int[] toIntArray(List list) {
		if (list == null)
			return new int[0];
		int	size = list.size();
		int[] result = new int[size];
		int i = 0;
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Integer value = (Integer) iterator.next();
			if (value == null)
				throw new IllegalStateException();
			result[i] = value.intValue();
			i++;
		}
		return result;
	}
	
	private List customConfiguration = null;
	
	private ZclEndPoint zclAllocateEndPoint(int app_end_point_id, int profile_id, int device_id, int end_point_id, int manufacturer_code,
			int[] clientClusterIds, int[] serverClusterIds) throws ApplianceException {	
		ZclEndPoint endPoint = ZclEndPointFactory.getEndPoint(profile_id, device_id);
		this.addEndPoint(endPoint, app_end_point_id);	
		ZclEndPointFactory.addServiceClusters(endPoint, profile_id, device_id, clientClusterIds, serverClusterIds);
		return endPoint;
	}	
	
	protected boolean isGenericAppliance() {
		return true;
	}
	
	public Dictionary getCustomConfiguration() {
		Hashtable result = new Hashtable(1);
		String[] zigbeeConfigParameters = null;
		if (customConfiguration != null && customConfiguration.size() > 0) {
			zigbeeConfigParameters = new String[customConfiguration.size()];
			int i = 0;
			for (Iterator iterator = customConfiguration.iterator(); iterator.hasNext();) {
				ZclEndPointDescriptor epd = (ZclEndPointDescriptor) iterator.next();
				zigbeeConfigParameters[i] = epd.toString();
				i++;
			}
		}
		result.put(ZIGBEE_CONFIG_PROPERTY_NAME, zigbeeConfigParameters);
		return result;
	}

	public ZclGenericAppliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);
//		Integer[] endPointIds = null;
//		try {
//			int[] endPointIdsPrimitive = (int[]) config.get(IAppliance.APPLIANCE_EPS_IDS_PROPERTY);
//			if (endPointIdsPrimitive != null) {
//				endPointIds = new Integer[endPointIdsPrimitive.length];
//				for (int i = 0; i < endPointIdsPrimitive.length; i++) {
//					endPointIds[i] = new Integer(endPointIdsPrimitive[i]);
//				}				
//			}
//		} catch (Exception e) {
//			// FIXME: Sometimes an array of Integer object is stored in configuration admin
//			endPointIds = (Integer[]) config.get(IAppliance.APPLIANCE_EPS_IDS_PROPERTY);
//		}
//		String[] endPointTypes = (String[]) config.get(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY);
		String[] customConfigurationObject = (String[]) config.get(ZIGBEE_CONFIG_PROPERTY_NAME);
		customConfiguration = new ArrayList();
		ZclEndPointDescriptor epd = null;
		if (customConfigurationObject != null) {
			for (int i = 0; i < customConfigurationObject.length; i++) {
				epd = new ZclEndPointDescriptor(customConfigurationObject[i]);
				customConfiguration.add(epd);
			}	
		}
		// Automatic allocation of ep 0
		for (Iterator iterator = customConfiguration.iterator(); iterator.hasNext();) {
			epd = (ZclEndPointDescriptor) iterator.next();
			zclAllocateEndPoint(epd.appEndPointId, epd.profileId, epd.deviceId, epd.endPointId, 0, epd.clientClusterIds, epd.serverClusterIds);
		}
		return;
	}

	protected void attached() {
		LOG.debug("attached");
		//XXX Here can be added with a timer critical initialization code (e.g. sleeping end device reporting configuration, reading zone status type,...)
		// Also additional custom appliance configuration can be updated (e.g. device subtype like ias contact, ias leak, ...)  
	}

	protected void detached() {
		LOG.debug("detached");
	}

	protected synchronized ZclEndPoint zclGetEndPoint(int epsNumber, int profile_id, int device_id, int end_point_id, int manufacturer_code,
			List clientClusterIds, List serverClusterIds) throws ApplianceException {
		ZclEndPoint endPoint = null;	
		int applEndPointId = end_point_id;
		if (epsNumber == 1)
			// Used for compatibility: device with only one end point always use end point id 1
			applEndPointId = IEndPoint.DEFAULT_END_POINT_ID;	
		endPoint = (ZclEndPoint) this.getEndPoint(applEndPointId);	
		
		if (endPoint == null) {
			int[] clientIntClusterIds = toIntArray(clientClusterIds);
			int[] serverIntClusterIds = toIntArray(serverClusterIds);
			endPoint = zclAllocateEndPoint(applEndPointId, profile_id, device_id, end_point_id, manufacturer_code, clientIntClusterIds, serverIntClusterIds);
			ZclEndPointDescriptor epd = new ZclEndPointDescriptor(applEndPointId, profile_id, device_id, end_point_id, clientIntClusterIds, serverIntClusterIds);
			customConfiguration.add(epd);
		}	
		return endPoint;
	}

}
