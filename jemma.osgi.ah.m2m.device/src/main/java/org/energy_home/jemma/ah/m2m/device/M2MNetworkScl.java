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
package org.energy_home.jemma.ah.m2m.device;

import org.apache.http.HttpResponse;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;


public interface M2MNetworkScl {

	public static final long CONTENT_INSTANCE_LATEST_ID = -1;
	public static final long CONTENT_INSTANCE_OLDEST_ID = -2;
	public static final long CONTENT_INSTANCE_INVALID_ID = Long.MIN_VALUE;

	public String getSclId();
	
	public ContentInstance getSclContentInstance(M2MContainerAddress containerAddress, long instanceId) throws M2MServiceException;

	public ContentInstanceItemsList getSclContentInstanceItemsList(M2MContainerAddress containerAddressFilter, long instanceId)
			throws M2MServiceException;

	public ContentInstanceItems getSclContentInstanceItems(M2MContainerAddress containerAddress, long startInstanceId, long endInstanceId)
			throws M2MServiceException;

	public ContentInstanceItemsList getSclContentInstanceItemsList(M2MContainerAddress containerAddressFilter, long startInstanceId,
			long endInstanceId) throws M2MServiceException;

	public ContentInstance createSclContentInstance(M2MContainerAddress containerAddress, ContentInstance contentInstance)
			throws M2MServiceException;

	public ContentInstancesBatchResponse sendContentInstanceBatchRequest(ContentInstancesBatchRequest cibr)
			throws M2MServiceException;
	
	public HttpResponse httpGet(String uri) throws M2MServiceException;

}
