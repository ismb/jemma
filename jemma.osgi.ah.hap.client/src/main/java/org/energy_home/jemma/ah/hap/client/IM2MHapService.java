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
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;

/**
 * 
 * M2M Hap Service interface
 *
 */
public interface IM2MHapService {
	
	public String getLocalHagId();
	
	/**
	 * Check the connection with the network platform
	 * 
	 * @return {@code true} if the client has successfully completed the
	 *         authentication at startup, {@code false} otherwise
	 */
	public boolean isConnected();

	/**
	 * Check the latest successful batch request operation
	 * 
	 * @return The timestamp associated to the last successful batch request (-1
	 *         in case no successful batch request has been yet completed)
	 */
	public long getLastSuccessfulBatchRequestTimestamp();
	
	public AHContainerAddress getContainerAddressFromUrl(String urlOrAddressedId) throws M2MHapException;
	
	public AHContainerAddress getContainerAddress(String containerName) throws M2MHapException;
	
	public AHContainerAddress getHagContainerAddress(String containerName) throws M2MHapException;
	
	public AHContainerAddress getHagContainerAddress(String appliancePid, String endPointId, String containerName) throws M2MHapException;
	
	public AHContainerAddress getHagContainerAddress(String appliancePid, Integer endPointId, String containerName) throws M2MHapException;
	
	public ContentInstance getLatestContentInstance(AHContainerAddress containerAddress) throws M2MHapException;

	public ContentInstance getOldestContentInstance(AHContainerAddress containerAddress) throws M2MHapException;
	
	public ContentInstance getContentInstance(AHContainerAddress containerAddress, long instanceId) throws M2MHapException;

	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerAddress, long instanceId)
			throws M2MHapException;

	public ContentInstanceItems getContentInstanceItems(AHContainerAddress containerAddress, long startInstanceId, long endInstanceId)
			throws M2MHapException;
	
	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerAddressFilter, long startInstanceId,
			long endInstanceId) throws M2MHapException;
	
	public ContentInstance createContentInstanceBatch(AHContainerAddress containerAddress, long instanceId, Object content)
			throws M2MHapException;

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, long instanceId, Object content)
			throws M2MHapException;
	
	public ContentInstance createContentInstanceBatch(AHContainerAddress containerAddress, ContentInstance contentInstance)
			throws M2MHapException;

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, ContentInstance contentInstance)
			throws M2MHapException;
		
	public ContentInstance createContentInstanceQueued(AHContainerAddress containerAddress, long instanceId, Object content, boolean sync)
		throws M2MHapException;
	
	public ContentInstance createContentInstanceQueued(AHContainerAddress containerAddress, ContentInstance contentInstance, boolean sync)
		throws M2MHapException;	
	
	public ContentInstance getCachedLatestContentInstance(AHContainerAddress containerAddress) throws M2MHapException;

	public ContentInstanceItemsList getCachedLatestContentItemsList(AHContainerAddress containerAddressFilter) throws M2MHapException;
	
}
