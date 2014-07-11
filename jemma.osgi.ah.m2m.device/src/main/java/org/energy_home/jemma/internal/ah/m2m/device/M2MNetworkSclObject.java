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
package org.energy_home.jemma.internal.ah.m2m.device;

import org.apache.http.HttpResponse;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;

public class M2MNetworkSclObject implements M2MNetworkScl {
	private M2MNetworkSclManager networkSclManager;
	private String user;

	public M2MNetworkSclObject(M2MNetworkSclManager networkSclObject, String user) {
		this.user = user;
		this.networkSclManager = networkSclObject;
	}

	public String getSclId() {
		return networkSclManager.getSclId(user);
	}
	
	public ContentInstance getSclContentInstance(M2MContainerAddress containerId, long instanceId) throws M2MServiceException {
		return networkSclManager.getSclContentInstance(user, containerId, instanceId);
	}

	public ContentInstanceItemsList getSclContentInstanceItemsList(M2MContainerAddress containerFilterId, long instanceId)
			throws M2MServiceException {
		return networkSclManager.getSclContentInstanceItemsList(user, containerFilterId, instanceId);
	}

	public ContentInstanceItems getSclContentInstanceItems(M2MContainerAddress containerId, long startInstanceId, long endInstanceId)
			throws M2MServiceException {
		return networkSclManager.getSclContentInstanceItems(user, containerId, startInstanceId, endInstanceId);
	}

	public ContentInstanceItemsList getSclContentInstanceItemsList(M2MContainerAddress containerFilterId, long startInstanceId,
			long endInstanceId) throws M2MServiceException {
		return networkSclManager.getSclContentInstanceItemsList(user, containerFilterId, startInstanceId, endInstanceId);
	}

	public ContentInstance createSclContentInstance(M2MContainerAddress containerId, ContentInstance instance)
			throws M2MServiceException {
		return networkSclManager.createSclContentInstance(user, containerId, instance);
	}

	public ContentInstancesBatchResponse sendContentInstanceBatchRequest(ContentInstancesBatchRequest cibr)
			throws M2MServiceException {
		return networkSclManager.sendContentInstanceBatchRequest(user, cibr);
	}

	public HttpResponse httpGet(String uri) throws M2MServiceException {
		return networkSclManager.httpGet(user, uri);
	}

}
