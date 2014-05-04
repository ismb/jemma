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


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceObject;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class AHM2MHapService {
	private static final Logger LOG = LoggerFactory.getLogger( AHM2MHapService.class );
	
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

	public DeviceCategory getDeviceCategory(String appliancePid, Integer endPointId) {
		ContentInstance epCategoryCi = getLocalContentInstance(appliancePid, endPointId, AHContainers.attrId_ah_core_config_category);
		if (epCategoryCi == null)
			return null;
		Integer categoryPid = (Integer)epCategoryCi.getContent();
		if (DeviceCategory.values().length >= categoryPid.intValue())
			return categoryPid == null ? null : DeviceCategory.values()[categoryPid.intValue()-1];
		else 
			return DeviceCategory.Other;
	}

	public String getLocalHagId() {
		return m2mHapService.getLocalHagId();
	}

	public boolean isConnected() {
		return m2mHapService.isConnected();
	}

	public AHContainerAddress getLocalContainerAddress(String containerName) {
		return m2mHapService.getLocalContainerAddress(containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String appliancePid, String endPointId, String containerName) {
		return m2mHapService.getLocalContainerAddress(appliancePid, endPointId, containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String appliancePid, Integer endPointId, String containerName) {
		return m2mHapService.getLocalContainerAddress(appliancePid, endPointId, containerName);
	}
	
	public ContentInstance getLocalContentInstance(AHContainerAddress containerAddress) throws M2MHapException {
		return m2mHapService.getLocalContentInstance(containerAddress);
	}

	public ContentInstanceItemsList getLocalContentInstanceItemsList(AHContainerAddress containerAddress) throws M2MHapException {
		return m2mHapService.getLocalContentInstanceItemsList(containerAddress);
	}
	
	public ContentInstanceItemsList getLocalContentInstanceItemsList(AHContainerAddress containerAddress, long startInstanceId,
			long endInstanceId) throws M2MHapException {
		return m2mHapService.getLocalContentInstanceItemsList(containerAddress, startInstanceId, endInstanceId);
	}
	
	public ContentInstance sendAttributeValue(String attributeId, long timestamp, Object value, boolean batchRequest) throws HacException {
		return sendAttributeValue(null, null, attributeId, timestamp, value, batchRequest);
	}

	public ContentInstance sendAttributeValue(String attributeId, IAttributeValue attributeValue, boolean batchRequest) throws HacException {
		return sendAttributeValue(null, null, attributeId, attributeValue.getTimestamp(), attributeValue.getValue(), batchRequest);
	}

	public String getLocalAddressedId(String appliancePid, Integer endPointId, String attributeId) {
		AHContainerAddress containerId = m2mHapService.getLocalContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
		if (containerId == null)
			return null;
		else 
			return containerId.getUrl()+M2MConstants.URL_CONTENT_INSTANCES;				
	}
	
	public ContentInstance getLocalContentInstance(String appliancePid, Integer endPointId, String attributeId) {
		AHContainerAddress containerId = m2mHapService.getLocalContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
		if (containerId == null)
			return null;
		else
			try {
				return m2mHapService.getLocalContentInstance(containerId);
			} catch (Exception e) {
				return null;
			}
	}
	
	public ContentInstance sendAttributeValue(String appliancePid, Integer endPointId, String attributeId,
			long timestamp, Object value, boolean batchRequest) throws HacException {
		try {
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			if (batchRequest)
				return m2mHapService.createContentInstanceBatch(containerId, timestamp, value);
			else
				return m2mHapService.createContentInstance(containerId, timestamp, value);
		} catch (Exception e) {
			LOG.error("sendAttributeValue error", e);
			throw new HacException("sendAttributeValue error");
		}	
	}
	
	public void sendAttributeValue(String appliancePid, Integer endPointId, String attributeId,
			IAttributeValue attributeValue, boolean batchRequest) throws HacException {
		sendAttributeValue(appliancePid, endPointId, attributeId, attributeValue.getTimestamp(), attributeValue.getValue(), batchRequest);	
	}

	public ContentInstance storeAttributeValue(String attributeId, long timestamp, Object value, boolean sync) throws HacException {
		return storeAttributeValue(null, null, attributeId, timestamp, value, sync);
	}

	public ContentInstance storeAttributeValue(String attributeId, IAttributeValue attributeValue, boolean sync) throws HacException {
		return storeAttributeValue(null, null, attributeId, attributeValue.getTimestamp(), attributeValue.getValue(), sync);
	}

	public ContentInstance storeAttributeValue(String appliancePid, Integer endPointId, String attributeId,
			long timestamp, Object value, boolean sync) throws HacException {
		try {
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			return m2mHapService.createContentInstanceQueued(containerId, timestamp, value, sync);
		} catch (Exception e) {
			LOG.error("storeAttributeValue error", e);
			throw new HacException("storeAttributeValue error");
		}		
	}

	public ContentInstance storeAttributeValue(String appliancePid, Integer endPointId, String attributeId,
			IAttributeValue attributeValue, boolean sync) throws HacException {
		return storeAttributeValue(appliancePid, endPointId, attributeId, attributeValue.getTimestamp(), attributeValue.getValue(), sync);
	}

	public IAttributeValue getLastestAttributeValue(String appliancePid, Integer endPointId, String attributeId)
			throws HacException {
		try {
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getLatestContentInstance(containerId);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			LOG.error("getLastestAttributeValue error", e);
			throw new HacException("getLastestAttributeValue error");
		}	
	}

	public IAttributeValue getLastestAttributeValue(String attributeId) throws HacException {
		return getLastestAttributeValue(null, null, attributeId);
	}

	public IAttributeValue getOldestAttributeValue(String appliancePid, Integer endPointId, String attributeId)
			throws HacException {
		try {
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getOldestContentInstance(containerId);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			LOG.error("getOldestAttributeValue error", e);
			throw new HacException("getOldestAttributeValue error");
		}
	}

	public IAttributeValue getOldestAttributeValue(String attributeId) throws HacException {
		return getOldestAttributeValue(null, null, attributeId);
	}
	
	public IAttributeValue getAttributeValue(String attributeId, long timestamp) throws HacException {
		return getAttributeValue(null, null, attributeId, timestamp);
	}
	
	public IAttributeValue getAttributeValue(String appliancePid, Integer endPointId, String attributeId, long timestamp) throws HacException {
		try {
			AHContainerAddress containerId = m2mHapService.getHagContainerAddress(appliancePid, (endPointId != null ? endPointId.toString() : null), attributeId);
			ContentInstance ci = m2mHapService.getContentInstance(containerId, timestamp);
			return new AttributeValue(ci.getContent(), ci.getId().longValue());
		} catch (Exception e) {
			LOG.error("getOldestAttributeValue error", e);
			throw new HacException("getOldestAttributeValue error");
		}
	}

}
