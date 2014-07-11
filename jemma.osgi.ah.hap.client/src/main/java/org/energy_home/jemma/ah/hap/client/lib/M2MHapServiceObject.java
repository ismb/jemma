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
package org.energy_home.jemma.ah.hap.client.lib;

import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.internal.ah.hap.client.AHM2MContainerAddress;
import org.energy_home.jemma.internal.ah.hap.client.HapServiceManager;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;

public class M2MHapServiceObject implements IM2MHapService {

	private HapServiceManager hapServiceManager;
	M2MHapServiceListener listener = null;
	private String user;

	public synchronized void setListener(M2MHapServiceListener listener) {
		if (this.listener != null)
			hapServiceManager.removeListener(this.listener);
		if (listener != null)
			hapServiceManager.addListener(listener);
		this.listener = listener;
	}
	
	public M2MHapServiceObject() {
		this.hapServiceManager = HapServiceManager.get();
		hapServiceManager.addReference();
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getLocalHagId() {
		return hapServiceManager.getLocalHagId(user);
	}
	
	public boolean isConnected() {
		return hapServiceManager.isConnected(user);
	}

	public long getLastSuccessfulBatchRequestTimestamp() {
		return hapServiceManager.getLastBatchRequestTimestamp(user);
	}

	public AHContainerAddress getContainerAddressFromUrl(String urlOrAddressedId) {
		return AHM2MContainerAddress.getAddressFromUrl(urlOrAddressedId);
	}
	
	public AHContainerAddress getContainerAddress(String containerName) {
		return hapServiceManager.getContainerAddress(user, containerName);
	}

	public AHContainerAddress getHagContainerAddress(String containerName) {
		return hapServiceManager.getHagContainerAddress(user, containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String containerName) {
		return hapServiceManager.getLocalContainerAddress(user, containerName);
	}
	
	public AHContainerAddress getHagContainerAddress(String appliancePid, Integer endPointId, String containerName) {
		return hapServiceManager.getHagContainerAddress(user, appliancePid, endPointId, containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String appliancePid, Integer endPointId, String containerName) {
		return hapServiceManager.getLocalContainerAddress(user, appliancePid, endPointId, containerName);
	}
	
	public AHContainerAddress getHagContainerAddress(String appliancePid, String endPointId, String containerName) {
		return hapServiceManager.getHagContainerAddress(user, appliancePid, endPointId, containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String appliancePid, String endPointId, String containerName) {
		return hapServiceManager.getLocalContainerAddress(user, appliancePid, endPointId, containerName);
	}
	
	public ContentInstance getLatestContentInstance(AHContainerAddress containerId) throws M2MHapException {
		return hapServiceManager.getContentInstance(user, containerId, M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID);
	}

	public ContentInstance getOldestContentInstance(AHContainerAddress containerId) throws M2MHapException {
		return hapServiceManager.getContentInstance(user, containerId, M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID);
	}
	
	public ContentInstance getContentInstance(AHContainerAddress containerId, long instanceId) throws M2MHapException {
		return hapServiceManager.getContentInstance(user, containerId, instanceId);
	}

	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerId, long instanceId)
			throws M2MHapException {
		return hapServiceManager.getContentInstanceItemsList(user, containerId, instanceId);
	}

	public ContentInstanceItems getContentInstanceItems(AHContainerAddress containerId, long startInstanceId, long endInstanceId)
			throws M2MHapException {
		return hapServiceManager.getContentInstanceItems(user, containerId, startInstanceId, endInstanceId);
	}

	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerIdFilter, long startInstanceId,
			long endInstanceId) throws M2MHapException {
		return hapServiceManager.getContentInstanceItemsList(user, containerIdFilter, startInstanceId, endInstanceId);
	}

	public ContentInstance createContentInstanceBatch(AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		return hapServiceManager.createContentInstanceBatch(user, containerId, instanceId, content);
	}

	public ContentInstance createContentInstanceBatch(AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		return hapServiceManager.createContentInstanceBatch(user, containerId, contentInstance);
	}

	public ContentInstance createContentInstance(AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		return hapServiceManager.createContentInstance(user, containerId, instanceId, content);
	}

	public ContentInstance createContentInstance(AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		return hapServiceManager.createContentInstance(user, containerId, contentInstance);
	}
	
	public ContentInstance createContentInstanceQueued(AHContainerAddress containerId, long instanceId, Object content, boolean sync)
			throws M2MHapException {
		return hapServiceManager.createContentInstanceQueued(user, containerId, instanceId, content, sync);
	}

	public ContentInstance createContentInstanceQueued(AHContainerAddress containerId, ContentInstance contentInstance, boolean sync)
			throws M2MHapException {
		return hapServiceManager.createContentInstanceQueued(user, containerId, contentInstance, sync);
	}

	public ContentInstance getCachedLatestContentInstance(AHContainerAddress containerName) throws M2MHapException {
		return hapServiceManager.getCachedLatestContentInstance(user, containerName);
	}

	public ContentInstanceItemsList getCachedLatestContentItemsList(AHContainerAddress containerIdFilter) throws M2MHapException {
		return hapServiceManager.getCachedLatestContentInstanceItemsList(user, containerIdFilter);
	}	
	
	
	public ContentInstance getLocalContentInstance(AHContainerAddress containerAddressFilter) 
			throws M2MHapException {
		return hapServiceManager.getLocalContentInstance(user, containerAddressFilter);
	}
	
	public ContentInstanceItemsList getLocalContentInstanceItemsList(AHContainerAddress containerAddressFilter) throws M2MHapException {
		return hapServiceManager.getLocalContentInstanceItemsList(user, containerAddressFilter);
	}
	
	public ContentInstanceItemsList getLocalContentInstanceItemsList(AHContainerAddress containerAddressFilter,
			long startInstanceId, long endInstanceId) throws M2MHapException {
		return hapServiceManager.getLocalContentInstanceItemsList(user, containerAddressFilter, startInstanceId, endInstanceId);
	}
	
	public void release() {
		setListener(null);
		hapServiceManager.removeReference();
	}


}
