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
package org.energy_home.jemma.internal.ah.hap.client;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceObject;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
 
public class AHM2MHapService {
	private static final Log log = LogFactory.getLog(AHM2MHapService.class);
	
	static boolean isHapServiceAvailable() {
		try {
			Class clazz = IM2MHapService.class;
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	private M2MHapServiceObject m2mHapService = null;
	
	private List toAttributeValueList(ContentInstanceItems cis) {
		if (cis == null)
			return null;
		List result = new LinkedList();
		ContentInstance ci = null;
		for (Iterator iterator = cis.getContentInstances().iterator(); iterator.hasNext();) {
			ci = (ContentInstance) iterator.next();
			result.add(new AttributeValue(ci.getContent(), ci.getId().longValue()));
		}
		return result;
	}
	
	private Map toAttributeValueMap(ContentInstanceItemsList cisList) {
		if (cisList == null)
			return null;
		List itemsList =  cisList.getContentInstanceItems();
		Map result = new HashMap(itemsList.size());
		ContentInstanceItems items = null;
		AHContainerAddress containerId = null;
		for (Iterator iterator = itemsList.iterator(); iterator.hasNext();) {
			items = (ContentInstanceItems) iterator.next();
			containerId = AHContainerAddress.getAddressFromUrl(items.getAddressedId());
			result.put(containerId.getAppliancePid(), toAttributeValueList(items));	
		}
		return result;
	}
	
	public AHM2MHapService(IM2MHapService hapService) {
		this.m2mHapService = (M2MHapServiceObject) hapService;
	}

	public IM2MHapService getM2MHapService() {
		return this.m2mHapService;
	}
	
	public void sendAttributeValue(String attributeName, long timestamp, Object value, boolean batchRequest) throws HacException {
		sendAttributeValue(null, null, null, attributeName, timestamp, value, batchRequest);
	}

	public void sendAttributeValue(String attributeName, IAttributeValue attributeValue, boolean batchRequest) throws HacException {
		sendAttributeValue(null, null, null, attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), batchRequest);
	}

	public void sendAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			long timestamp, Object value, boolean batchRequest) throws HacException {
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			if (batchRequest)
				m2mHapService.createContentInstanceBatch(containerId, timestamp, value);
			else
				m2mHapService.createContentInstance(containerId, timestamp, value);
		} catch (Exception e) {
			log.error("sendAttributeValue error", e);
			throw new HacException("sendAttributeValue error");
		}	
	}
	
	public void sendAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue, boolean batchRequest) throws HacException {
		sendAttributeValue(appliancePid, endPointId, clusterName, attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), batchRequest);	
	}

	public void storeAttributeValue(String attributeName, long timestamp, Object value, boolean sync) throws HacException {
		storeAttributeValue(null, null, null, attributeName, timestamp, value, sync);
	}

	public void storeAttributeValue(String attributeName, IAttributeValue attributeValue, boolean sync) throws HacException {
		storeAttributeValue(null, null, null, attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), sync);
	}

	public void storeAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			long timestamp, Object value, boolean sync) throws HacException {
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			m2mHapService.createContentInstanceQueued(containerId, timestamp, value, sync);
		} catch (Exception e) {
			log.error("storeAttributeValue error", e);
			throw new HacException("storeAttributeValue error");
		}		
	}

	public void storeAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue, boolean sync) throws HacException {
		storeAttributeValue(appliancePid, endPointId, clusterName, attributeName, attributeValue.getTimestamp(), attributeValue.getValue(), sync);
	}

	public IAttributeValue getLastestAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName)
			throws HacException {
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getLatestContentInstance(containerId);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			log.error("getLastestAttributeValue error", e);
			throw new HacException("getLastestAttributeValue error");
		}	
	}

	public IAttributeValue getLastestAttributeValue(String attributeName) throws HacException {
		return getLastestAttributeValue(null, null, null, attributeName);
	}

	public IAttributeValue getOldestAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName)
			throws HacException {
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getOldestContentInstance(containerId);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			log.error("getOldestAttributeValue error", e);
			throw new HacException("getOldestAttributeValue error");
		}
	}

	public IAttributeValue getOldestAttributeValue(String attributeName) throws HacException {
		return getOldestAttributeValue(null, null, null, attributeName);
	}
	
	public IAttributeValue getAttributeValue(String attributeName, long timestamp) throws HacException {
		return getAttributeValue(null, null, null, attributeName, timestamp);
	}
	
	public IAttributeValue getAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName, long timestamp) throws HacException {
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getContentInstance(containerId, timestamp);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			log.error("getOldestAttributeValue error", e);
			throw new HacException("getOldestAttributeValue error");
		}
	}

	public List getAttributeValuesList(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			long startTime, long endTime) throws HacException {
		List result = null;
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstanceItems cis = m2mHapService.getContentInstanceItems(containerId, startTime, endTime);
			result = toAttributeValueList(cis);
		} catch (Exception e) {
			log.error("sendAttributeValue error", e);
			throw new HacException("sendAttributeValue error");
		}	
		return result;
	}

	public List getAttributeValuesList(String attributeName, long startTime, long endTime) throws HacException {
		return getAttributeValuesList(null, null, null, attributeName, startTime, endTime);
	}
	
	public Map getAttributeValuesMap(String appliancePidFilter, Integer endPointId, String clusterName, String attributeName,
			long startTime, long endTime) throws HacException {
		Map result = null;
		try {
			String attributeId = null;
			if (clusterName != null)
				attributeId = AHContainers.getClusterAttributeId(clusterName, attributeName);
			else 
				attributeId = attributeName;
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePidFilter, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstanceItemsList cisList = m2mHapService.getContentInstanceItemsList(containerId, startTime, endTime);
			result = toAttributeValueMap(cisList);
		} catch (Exception e) {
			log.error("sendAttributeValue error", e);
			throw new HacException("sendAttributeValue error");
		}	
		return result;
	}

}
