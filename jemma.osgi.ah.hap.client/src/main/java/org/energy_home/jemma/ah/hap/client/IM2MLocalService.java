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
package org.energy_home.jemma.ah.hap.client;

import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;

public interface IM2MLocalService {
	
	public String getLocalHagId();
	
	public AHContainerAddress getContainerAddress(String containerName) throws M2MHapException;
	
	public AHContainerAddress getContainerAddress(String appliancePid, String endPointId, String containerName) throws M2MHapException;

	public AHContainerAddress getContainerAddress(String appliancePid, Integer endPointId, String containerName) throws M2MHapException;
	
	public ContentInstance getLatestContentInstance(AHContainerAddress containerAddress) throws M2MHapException;
	
	public ContentInstanceItemsList getLatestContentInstanceItemsList(AHContainerAddress containerAddressFilter) throws M2MHapException;

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, long instanceId, Object content)
			throws M2MHapException;

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, ContentInstance contentInstance)
			throws M2MHapException;
	
	public ContentInstancesBatchResponse sendContentInstanceBatchRequest(ContentInstancesBatchRequest contentInstancesBatchRequest)
			throws M2MHapException;
	
}
