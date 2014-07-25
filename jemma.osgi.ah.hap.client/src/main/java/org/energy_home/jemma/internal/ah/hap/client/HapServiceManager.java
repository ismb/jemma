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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceListener;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.ah.m2m.device.lib.M2MDeviceListener;
import org.energy_home.jemma.ah.m2m.device.lib.M2MDeviceObject;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.M2MConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HapServiceManager implements Runnable, M2MDeviceListener {

	private static final Logger LOG = LoggerFactory.getLogger( HapServiceManager.class );
	
	public static final String BATCH_REQUESTS_FILE_NAME_PREFIX = "batch.request.";
	public static final String CACHE_LATEST_FILE_NAME_PREFIX = "cache.latest.";
	public static final String HAP_SERVER_COMMUNICATION_PROBLEMS = "Some problem occurred in Hap server communication";
	public static final String XML_OBJECT_CLONE_FAILED = "Xml object clone failed";

	private static final HapServiceManager instance = new HapServiceManager();
	private static final Random randomGenerator = new Random();

	private static int DELAY_PRECISION = 1000;
	private static int SUBSEQUENT_BUFFERED_BATCH_REQUESTS = 10;
	private static int CREATE_INSTANCE_DELAY = 60000;
	private static int referenceCounter = 0;

	private static final int MAX_BUFFERED_BATCH_REQUESTS = 280*3; // 288*3 = 3 days
	private static final int MAX_BUFFERED_LAST_CIS_ITEMS = 1;

	private static boolean FAST_FORWARD_FACTOR = false;
	
	static {
		randomGenerator.setSeed(System.currentTimeMillis());
	}
	
	public static int getFastForwardFactor() {
		int fastForwardFactor = 1;
		String fff = System.getProperty("org.energy_home.jemma.ah.hap.client.fastForwardFactor");
		try {
			if (!Utils.isNullOrEmpty(fff)) {
				fastForwardFactor = Integer.parseInt(fff);
			}
		} catch (Exception e) {
			LOG.error("Exception on getFastForwardFactor", e);
			fastForwardFactor = 1;
		}
		return fastForwardFactor;
	}

	public static void setFastForwardFactor(int value) {
		System.setProperty("org.energy_home.jemma.ah.hap.client.fastForwardFactor", new Integer(value).toString());
	}

	public static HapServiceManager get() {
		return instance;
	}

	public static boolean checkItemsOnContainerIdFilter(ContentInstanceItems items, M2MContainerAddress containerAddressFilter) {
		M2MContainerAddress itemsContainerAddress;
		try {
			itemsContainerAddress = new M2MContainerAddress(items.getAddressedId());
		} catch (IllegalArgumentException e) {
			LOG.error("Invalid items addressed id " + items.getAddressedId(), e);
			return false;
		}
		return M2MContainerAddress.match(itemsContainerAddress, containerAddressFilter);
	}

	public static boolean checkAttributeIdFilter(String[] attributeIdFilter, String containerNameOrAddressedId) {
		if (attributeIdFilter == null)
			return false;
		for (int i = 0; i < attributeIdFilter.length; i++) {
			if (attributeIdFilter[i] != null && containerNameOrAddressedId != null && containerNameOrAddressedId.contains(attributeIdFilter[i]))
				return true;
		}
		return false;
	}
	
	private class ServiceStatus {
		private boolean started = false;
		private boolean exited = false;

		void setStarted(boolean started) {
			this.started = started;
		}

		boolean isStarted() {
			return started;
		}

		void setExited(boolean exited) {
			this.exited = exited;
		}

		boolean isExited() {
			return exited;
		}
	}
	private ArrayList<M2MHapServiceListener> listeners = new ArrayList<M2MHapServiceListener>();
	
	private M2MDeviceObject m2mDevice = null;
	private M2MNetworkScl m2mNetworkScl = null;
	
	private Map<String, ContentInstanceItems> cisItems = new HashMap<String, ContentInstanceItems>();
	private Map<String, ContentInstanceItems> latestCisItems = new HashMap<String, ContentInstanceItems>();
	private Map<String, ContentInstanceItems> localCisItems = new TreeMap<String, ContentInstanceItems>();
	private XmlIndexedCircularBuffer batchCisItemsBuffer;
	private XmlIndexedCircularBuffer latestCisItemsBuffer;
	private long lastBatchRequestOKTimestamp = -1;
	private ServiceStatus serviceStatus = new ServiceStatus();
	private Thread t;
	private String deviceId = null;
	private Map<AHContainerAddress, HapContentInstancesQueue> syncContentInstancesQueuesMap = new ConcurrentHashMap<AHContainerAddress, HapContentInstancesQueue>();
	private Map<AHContainerAddress, HapContentInstancesQueue> contentInstancesQueuesMap = new ConcurrentHashMap<AHContainerAddress, HapContentInstancesQueue>();

	private void storeQueuedContentInstances(boolean sync) {
		LOG.debug("storeQueuedContentInstances call with isSync " + sync);
		if (m2mDevice.isConnected()) {
			HapContentInstancesQueue ciQueue = null;
			ContentInstance instance = null;
			Map<AHContainerAddress, HapContentInstancesQueue> queuesMap = sync ? syncContentInstancesQueuesMap
					: contentInstancesQueuesMap;
			try {
				for (Iterator<HapContentInstancesQueue> iterator = queuesMap.values().iterator(); iterator.hasNext();) {
					ciQueue = iterator.next();
					try {
						AHContainerAddress containerId = ciQueue.getContainerId();
						while (ciQueue.getQueueSize() > 0) {
							if (ciQueue.getLastCreatedContentInstance() == null) {
								instance = getContentInstance(null, containerId, M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID);
								ciQueue.setLastCreatedContentInstance(instance);
								if (instance == null)
									LOG.debug("storeQueuedContentInstances: retrieved latest content instance for container" + containerId + ": null\n");
								else
									LOG.debug("storeQueuedContentInstances: retrieved latest content instance for container" + containerId + ":\n"
											+ instance.toXmlFormattedString());
							}
							while (ciQueue.getQueueSize() > 0) {
								instance = ciQueue.getFirstContentInstance();
								instance = createContentInstance(null, containerId, instance, false);
								ciQueue.removeFirstContentInstance();
								ciQueue.setLastCreatedContentInstance(instance);
								LOG.debug("storeQueuedContentInstances: created content instance for container " + containerId + ":\n"
										+ instance.toXmlFormattedString());
							}
						}
					} catch (Exception e) {
						LOG.error("storeQueuedContentInstances: error while reading or writing content instances for container " + ciQueue.getContainerId(), e);
					}
				}
			} catch (Exception e) {
				LOG.error("storeQueuedContentInstances: generic error while sending content instances", e);
			}
		} else {
			LOG.debug("storeQueuedContentInstances: M2M Device disconnected");
		}
	}	
	
	private ContentInstancesBatchRequest createContentInstanceBatchRequest(Map<String, ContentInstanceItems> currentCisItems) {
		ContentInstancesBatchRequest cibReq = new ContentInstancesBatchRequest();
		ContentInstanceItems ciis = null;
		Map.Entry<String, ContentInstanceItems> pairs = null;
		for (Iterator<Map.Entry<String, ContentInstanceItems>> iterator = currentCisItems.entrySet().iterator(); iterator.hasNext();) {
			pairs = iterator.next();
			ciis = (ContentInstanceItems) pairs.getValue();
			cibReq.getContentInstanceItems().add(ciis);
			LOG.debug("Batch item processed: " + ciis.getAddressedId() + " (" + ciis.getContentInstances().size() + " items)");
		}
		return cibReq;
	}

	private ContentInstance getContentInstance(Map<String, ContentInstanceItems> currentCisItems, AHContainerAddress containerId) 
			throws M2MHapException {
		if (((AHM2MContainerAddress)containerId).isFilterAddress())
			throw new M2MHapException("Container id cannot be a filter");
		if (currentCisItems == null)
			return null;
		ContentInstanceItems ciItems = currentCisItems.get(((AHM2MContainerAddress)containerId).getM2MContainerAdress().getContentInstancesUrl());
		if (ciItems == null)
			return null;
		List<ContentInstance> instancesList = ciItems.getContentInstances();
		if (instancesList == null || instancesList.size() != 1)
			return null;
		return instancesList.get(0);
	}
	
	private ContentInstanceItemsList createContentInstanceItemsList(Map<String, ContentInstanceItems> currentCisItems, AHContainerAddress containerIdFilter,
			String[] containerNamesFilter) {
		return createContentInstanceItemsList(currentCisItems, containerIdFilter, containerNamesFilter, M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID, M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID);
	}
	
	private ContentInstanceItemsList createContentInstanceItemsList(Map<String, ContentInstanceItems> currentCisItems, AHContainerAddress containerIdFilter,
			String[] containerNamesFilter, long startInstanceId, long endInstanceId)
	{
		ContentInstanceItemsList ciisList = new ContentInstanceItemsList();
		ContentInstanceItems ciis = null;
		Map.Entry<String, ContentInstanceItems> pairs = null;
		for (Iterator<Map.Entry<String, ContentInstanceItems>> iterator = currentCisItems.entrySet().iterator(); iterator.hasNext();) {
			pairs = iterator.next();
			ciis = (ContentInstanceItems) pairs.getValue();
			if ((containerIdFilter == null || checkItemsOnContainerIdFilter(ciis, ((AHM2MContainerAddress)containerIdFilter).getM2MContainerAdress()))
					&& (containerNamesFilter == null || checkAttributeIdFilter(containerNamesFilter, ciis.getAddressedId()))) {
				ContentInstanceItems filteredCiis = new ContentInstanceItems();
				for (Iterator iterator2 = ciis.getContentInstances().iterator(); iterator2.hasNext();) {
					ContentInstance ci = (ContentInstance) iterator2.next();
					filteredCiis.setAddressedId(ciis.getAddressedId());
					boolean addContentInstance = true;
					if (startInstanceId != M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID && ci.getId() < startInstanceId)
						addContentInstance = false;
					else if (endInstanceId != M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID && ci.getId() > endInstanceId)
						addContentInstance = false;
					if (addContentInstance)
						filteredCiis.getContentInstances().add(ci);
				}
				ciisList.getContentInstanceItems().add(filteredCiis);
			}

//			log.info("Latest item processed " + ciis.getAddressedId());
		}
		return ciisList;
	}

	private void initPersistentBuffers(String cid) {
		if (cid == null) {
			LOG.debug("Invalid cid: empty buffers initialized");
			batchCisItemsBuffer = new XmlIndexedCircularBuffer(0);
			latestCisItemsBuffer = new XmlIndexedCircularBuffer(0);			
		} else if (!HapServiceConfiguration.USE_PERSISTENT_BUFFER) {
			LOG.debug("Persistent buffers not used");
			batchCisItemsBuffer = new XmlIndexedCircularBuffer(MAX_BUFFERED_BATCH_REQUESTS);
			latestCisItemsBuffer = new XmlIndexedCircularBuffer(MAX_BUFFERED_LAST_CIS_ITEMS);
		} else {
			LOG.debug("Persistent buffers used");
			batchCisItemsBuffer = new XmlIndexedCircularBuffer(MAX_BUFFERED_BATCH_REQUESTS, HapServiceConfiguration.BATCH_REQUESTS_DIR_NAME_PREFIX + cid,
					BATCH_REQUESTS_FILE_NAME_PREFIX);
			LOG.debug("BATCH CIS ITEMS REQUESTS BUFFER LOADED - SIZE:" + batchCisItemsBuffer.getSize());

			latestCisItemsBuffer = new XmlIndexedCircularBuffer(MAX_BUFFERED_LAST_CIS_ITEMS, HapServiceConfiguration.BATCH_REQUESTS_DIR_NAME_PREFIX + cid,
					CACHE_LATEST_FILE_NAME_PREFIX);
			latestCisItems = new HashMap<String, ContentInstanceItems>();
			ContentInstanceItemsList ciiList = (ContentInstanceItemsList) latestCisItemsBuffer.getLastItem();
			if (ciiList != null) {
				List<ContentInstanceItems> ciisListFile = ciiList.getContentInstanceItems();
				if (ciisListFile != null)
					for (ContentInstanceItems ciis : ciisListFile) {
						if (checkAttributeIdFilter(HapServiceConfiguration.CACHED_ATTRIBUTE_ID_FILTER, ciis.getAddressedId()))
							latestCisItems.put(ciis.getAddressedId(), ciis);
					}
			}
			LOG.debug("LATEST CIS ITEMS BUFFER LOADED - SIZE:" + latestCisItemsBuffer.getSize());
		}
	}
	
	private void initQueues() {
		syncContentInstancesQueuesMap.clear();
		contentInstancesQueuesMap.clear();
	}
	
	private void initLocalCache()  {
		localCisItems.clear();
	}

	private int sendContentInstanceBatchRequest() {
		ContentInstancesBatchRequest currentCisItems = null;
		ContentInstanceItemsList currentLatestCisItems = null;
		int batchBufferSize = batchCisItemsBuffer == null ? 0 : batchCisItemsBuffer.getSize();
		long now = System.currentTimeMillis();
		boolean itemsToSend = true;
		if (!HapServiceConfiguration.SEND_EMPTY_BATCH_REQUEST && cisItems.isEmpty()) {
			LOG.debug("Empty batch request not sent");
			itemsToSend = false;
		} 
		if (itemsToSend) {
			synchronized (cisItems) {
				currentCisItems = createContentInstanceBatchRequest(cisItems);
				cisItems.clear();
				// Only cached attributes are stored on file system
				currentLatestCisItems = createContentInstanceItemsList(latestCisItems, null, HapServiceConfiguration.CACHED_ATTRIBUTE_ID_FILTER);
				int contentInstanceItemListSize = currentCisItems.getContentInstanceItems().size();
				if (deviceId == null) {
					String errorMsg = "M2M Device invalid device id - " + contentInstanceItemListSize
							+ " content instance items discarded, buffer size = " + batchBufferSize;
					//FIXME Weird: logging depending on a variable ????
					if (contentInstanceItemListSize > 0) {
						LOG.error(errorMsg);
					} else {
						LOG.warn(errorMsg);
					}
					return batchBufferSize;
				} 
			}
			currentCisItems.setTimestamp(now);
		}

		boolean usingBuffer = false;
		if (batchBufferSize > 0) {
			usingBuffer = true;
			if (itemsToSend) {
				batchCisItemsBuffer.addItem(currentCisItems);
				latestCisItemsBuffer.addItem(currentLatestCisItems);
			}
			currentCisItems = (ContentInstancesBatchRequest) batchCisItemsBuffer.getFirstItem();
		}
		int batchRequestCounter = 0;
		try {
			while (currentCisItems != null && batchRequestCounter < SUBSEQUENT_BUFFERED_BATCH_REQUESTS) {
				if (!isLocalOnlyM2MDevice())
					m2mNetworkScl.sendContentInstanceBatchRequest(currentCisItems);
				// TODO add here parsing of batch request response to detect specific errors
				lastBatchRequestOKTimestamp = new Long(now);
				if (usingBuffer) {
					batchCisItemsBuffer.removeFirstItem();
					LOG.debug("USING BUFFER - batch request sent");
				} else {
					latestCisItemsBuffer.addItem(currentLatestCisItems);
					LOG.debug("Batch request sent");
				}
				batchRequestCounter++;
				currentCisItems = (ContentInstancesBatchRequest) batchCisItemsBuffer.getFirstItem();
			}
		} catch (M2MServiceException e1) {
			LOG.error("M2MException while sending batch request", e1);
			if (!usingBuffer && itemsToSend) {
				batchCisItemsBuffer.addItem(currentCisItems);
				latestCisItemsBuffer.addItem(currentLatestCisItems);
			}
		} catch (Exception e2) {
			// TODO add error management (e.g. remove from buffer if using it)
			LOG.error("Generic exception while sending batch request", e2);
		}
		batchBufferSize = batchCisItemsBuffer == null ? 0 : batchCisItemsBuffer.getSize();
		if (batchBufferSize > 0)
			LOG.debug("BUFFER SIZE: " + batchBufferSize);
		return batchBufferSize;
	}

	private void waitForThreadExit() {
		synchronized (serviceStatus) {
			while (!serviceStatus.isExited())
				try {
					serviceStatus.wait();
				} catch (InterruptedException e) {
				}
		}
	}

	public void addListener(M2MHapServiceListener listener) {
		synchronized (serviceStatus) {
			if (listener != null) {
				listeners.remove(listener);
				listeners.add(listener);
			}
		}
	}

	public void removeListener(M2MHapServiceListener listener) {
		synchronized (serviceStatus) {
			if (listener != null) {
				listeners.remove(listener);
			}
		}
	}
	
	public void startup() {
		LOG.debug("Hap Service starting...");
		HapServiceConfiguration.init();
		synchronized (serviceStatus) {
			if (serviceStatus.isStarted()) {
				LOG.debug("Hap Service already started");
				return;
			}
			m2mDevice = new M2MDeviceObject();
			m2mDevice.setListener(this);
			m2mNetworkScl = m2mDevice.getNetworkScl(null);
			t = new Thread(this, "Hap Service Thread");
			serviceStatus.setStarted(true);
			serviceStatus.setExited(false);
			t.start();
		}
		LOG.debug("Hap Service started");
	}

	public void shutdown() {
		LOG.debug("Hap Service shutdown initiated...");
		synchronized (serviceStatus) {
			if (serviceStatus.isStarted()) {
				serviceStatus.setStarted(false);
				// TODO check what happens if an interrupt is generated while
				// writing to files
				t.interrupt();
			}
			waitForThreadExit();
			m2mDevice.release();
			m2mDevice = null;
			m2mNetworkScl = null;
			//batchCisItemsBuffer = null;
			//latestCisItemsBuffer = null;
		}
		LOG.debug("Hap Service shutdown completed");
	}

	public void run() {
		int fastForwardFactor = getFastForwardFactor();
		if (fastForwardFactor < 0)
			return;
		long batch_request_timeout = (fastForwardFactor == 1) ? HapServiceConfiguration.BATCH_REQUEST_TIMEOUT : HapServiceConfiguration.BATCH_REQUEST_TIMEOUT / fastForwardFactor;
		long create_instance_delay = (fastForwardFactor == 1) ? CREATE_INSTANCE_DELAY : CREATE_INSTANCE_DELAY / fastForwardFactor;
		long current_batch_request_timeout = (long) (randomGenerator.nextFloat() * batch_request_timeout);
		long current_create_instance_delay = (long) (randomGenerator.nextFloat() * create_instance_delay);
		long batch_request_time_check = System.currentTimeMillis();
		long create_instance_time_check = System.currentTimeMillis();
		while (serviceStatus.isStarted()) {
			try {
				Thread.sleep(Math.min(current_create_instance_delay, current_batch_request_timeout));
				if (System.currentTimeMillis() - create_instance_time_check + DELAY_PRECISION > current_create_instance_delay) {
					storeQueuedContentInstances(true);
					storeQueuedContentInstances(false);				
					create_instance_time_check = System.currentTimeMillis();
				}
				if (System.currentTimeMillis() - batch_request_time_check + DELAY_PRECISION > current_batch_request_timeout) {
					batch_request_time_check = System.currentTimeMillis();
					sendContentInstanceBatchRequest();
					LOG.debug("Measured time for batch request " + (System.currentTimeMillis() - batch_request_time_check));
					batch_request_time_check = System.currentTimeMillis();
				}
				// TODO:!!! Add here notification based on service exposed by local scl
				fastForwardFactor = getFastForwardFactor();
				batch_request_timeout = (fastForwardFactor == 1) ? HapServiceConfiguration.BATCH_REQUEST_TIMEOUT : HapServiceConfiguration.BATCH_REQUEST_TIMEOUT
						/ fastForwardFactor;
				create_instance_delay = (fastForwardFactor == 1) ? CREATE_INSTANCE_DELAY : CREATE_INSTANCE_DELAY 
						/ fastForwardFactor;
				current_batch_request_timeout = batch_request_timeout;
				current_create_instance_delay = create_instance_delay;
			} catch (InterruptedException e) {
				// Sometimes interrupted exception is generated even if not
				// stopping the OSGi service
				LOG.debug("Interrupted exception in Hap Service Loop");
				sendContentInstanceBatchRequest();;
			}
		}
		synchronized (serviceStatus) {
			serviceStatus.setExited(true);
			serviceStatus.notifyAll();
		}
	}

	public void addReference() {
		synchronized (serviceStatus) {
			referenceCounter++;
			if (referenceCounter == 1)
				startup();
			LOG.debug("Added reference " + referenceCounter);
		}
	}

	public void removeReference() {
		synchronized (serviceStatus) {
			referenceCounter--;
			if (referenceCounter == 0)
				shutdown();
			LOG.debug("Removed reference " + referenceCounter);
		}
	}

	private void checkM2MDeviceStartedStatus() throws M2MHapException {
		if (!m2mDevice.isStarted())
			throw new M2MHapException("Hap service not started");
	}
	
	private void checkM2MDeviceConnectedStatus() throws M2MHapException {
		if (!m2mDevice.isConnected())
			throw new M2MHapException("Hap service not connected");
	}

	private boolean isLocalOnlyM2MDevice() {
		M2MDeviceConfig deviceConfig = m2mDevice.getConfiguration();
		return (deviceConfig != null) ? deviceConfig.isLocalOnly() : false;
	}

	public String getLocalHagId(String user) {
		return m2mDevice.getConfiguration().getSclId();
	}
	
	public boolean isConnected(String user) {
		return m2mDevice.isConnected();
	}

	public long getLastBatchRequestTimestamp(String user) {
		return lastBatchRequestOKTimestamp;
	}

	public AHContainerAddress getContainerAddress(String user, String containerName) {
		return AHM2MContainerAddress.getNetworkAddress(null, null, null, containerName);
	}
	
	public AHContainerAddress getHagContainerAddress(String user, String containerName) {
		String hagId = getLocalHagId(user);
		return AHM2MContainerAddress.getNetworkAddress(hagId, null, null, containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String user, String containerName) {
		return AHM2MContainerAddress.getNetworkAddress(null, null, null, containerName, true);
	}
	
	public AHContainerAddress getHagContainerAddress(String user, String appliancePid, Integer endPointId, String containerName) {
		return getHagContainerAddress(user, appliancePid, endPointId.toString(), containerName);
	}
	
	public AHContainerAddress getLocalContainerAddress(String user, String appliancePid, Integer endPointId, String containerName) {
		return getLocalContainerAddress(user, appliancePid, endPointId.toString(), containerName);
	}
	
	public AHContainerAddress getHagContainerAddress(String user, String appliancePid, String endPointId, String containerName) {
		String hagId = getLocalHagId(user);
		return AHM2MContainerAddress.getNetworkAddress(hagId, appliancePid, endPointId, containerName);
	}

	public AHContainerAddress getLocalContainerAddress(String user, String appliancePid, String endPointId, String containerName) {
		String hagId = getLocalHagId(user);
		return AHM2MContainerAddress.getNetworkAddress(hagId, appliancePid, endPointId, containerName, true);
	}
	
	public ContentInstance getCachedLatestContentInstance(String user, AHContainerAddress containerId) throws M2MHapException {
		synchronized (cisItems) {
			return getContentInstance(latestCisItems, containerId);
		}
//		ContentInstanceItemsList itemsListObject = getCachedLatestContentInstanceItemsList(user, containerId);
//		List<ContentInstanceItems> itemsList = itemsListObject.getContentInstanceItems();
//		if (itemsList != null && itemsList.size() == 1) {
//			List<ContentInstance> instancesList = itemsList.get(0).getContentInstances();
//			if (instancesList != null && instancesList.size() == 1)
//				return instancesList.get(0);
//		}
//		return null;
	}

	public ContentInstanceItemsList getCachedLatestContentInstanceItemsList(String user, AHContainerAddress containerIdFilter)
			throws M2MHapException {
		synchronized (cisItems) {	
			return createContentInstanceItemsList(latestCisItems, containerIdFilter, null);
		}
	}
	
	public ContentInstance getLocalContentInstance(String user, AHContainerAddress containerId) throws M2MHapException {
		return getContentInstance(localCisItems, containerId);
	}

	public ContentInstanceItemsList getLocalContentInstanceItemsList(String user, AHContainerAddress containerAddressFilter) {
		synchronized (localCisItems) {
			return createContentInstanceItemsList(localCisItems, containerAddressFilter, null);				
		}
	}
	
	public ContentInstanceItemsList getLocalContentInstanceItemsList(String user, AHContainerAddress containerAddressFilter,
			long startInstanceId, long endInstanceId) {
		synchronized (localCisItems) {
			return createContentInstanceItemsList(localCisItems, containerAddressFilter, null, startInstanceId, endInstanceId);
		}
	}
	
	public ContentInstance getContentInstance(String user, AHContainerAddress containerId, long instanceId) throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (isLocalOnlyM2MDevice)
			return null;
		checkM2MDeviceConnectedStatus();
		try {
			return m2mNetworkScl.getSclContentInstance(((AHM2MContainerAddress)containerId).getM2MContainerAdress(), instanceId);
		} catch (M2MServiceException e) {
			throw new M2MHapException(HAP_SERVER_COMMUNICATION_PROBLEMS);
		}
	}

	public ContentInstanceItemsList getContentInstanceItemsList(String user, AHContainerAddress containerId, long instanceId)
			throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (isLocalOnlyM2MDevice)
			return null;
		checkM2MDeviceConnectedStatus();
		try {
			return m2mNetworkScl.getSclContentInstanceItemsList(((AHM2MContainerAddress)containerId).getM2MContainerAdress(), instanceId);
		} catch (M2MServiceException e) {
			throw new M2MHapException(HAP_SERVER_COMMUNICATION_PROBLEMS);
		}
	}

	public ContentInstanceItems getContentInstanceItems(String user, AHContainerAddress containerId, long startInstanceId,
			long endInstanceId) throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (isLocalOnlyM2MDevice)
			return null;
		checkM2MDeviceConnectedStatus();
		try {
			return m2mNetworkScl.getSclContentInstanceItems(((AHM2MContainerAddress)containerId).getM2MContainerAdress(), startInstanceId, endInstanceId);
		} catch (M2MServiceException e) {
			throw new M2MHapException(HAP_SERVER_COMMUNICATION_PROBLEMS);
		}
	}

	public ContentInstanceItemsList getContentInstanceItemsList(String user, AHContainerAddress containerIdFilter, long startInstanceId,
			long endInstanceId) throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (isLocalOnlyM2MDevice)
			return null;
		checkM2MDeviceConnectedStatus();
		try {
			return m2mNetworkScl.getSclContentInstanceItemsList(((AHM2MContainerAddress)containerIdFilter).getM2MContainerAdress(), startInstanceId, endInstanceId);
		} catch (M2MServiceException e) {
			throw new M2MHapException(HAP_SERVER_COMMUNICATION_PROBLEMS);
		}
	}

	public ContentInstance createContentInstanceBatch(String user, AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		ContentInstance contentInstance = new ContentInstance();
		contentInstance.setContent(content);
		contentInstance.setId(new Long(instanceId));
		return createContentInstanceBatch(user, containerId, contentInstance);
	}

	public ContentInstance createContentInstanceBatch(String user, AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (!isLocalOnlyM2MDevice)
			checkM2MDeviceStartedStatus();
		createLocalContentInstance(user, containerId, contentInstance);
		if (checkAttributeIdFilter(HapServiceConfiguration.LOCAL_ONLY_ATTRIBUTE_ID_FILTER, containerId.getContainerName()))
			// Local only container
			return contentInstance;
		String contentInstanceAddresssedId = ((AHM2MContainerAddress)containerId).getM2MContainerAdress().getContentInstancesUrl();

		synchronized (cisItems) {
			ContentInstanceItems ciis = (ContentInstanceItems) cisItems.get(contentInstanceAddresssedId);
			ContentInstanceItems ciisLatest = null;
			if (ciis == null) {
				ciis = new ContentInstanceItems();
				ciis.setAddressedId(contentInstanceAddresssedId);
				cisItems.put(contentInstanceAddresssedId, ciis);
			}
			ciis.getContentInstances().add(contentInstance);
			String containerName = containerId.getContainerName();
			ciisLatest = new ContentInstanceItems();
			ciisLatest.setAddressedId(contentInstanceAddresssedId);
			ciisLatest.getContentInstances().add(contentInstance);
			latestCisItems.put(contentInstanceAddresssedId, ciisLatest);
		}
		return contentInstance;
	}

	public ContentInstance createContentInstance(String user, AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		ContentInstance contentInstance = new ContentInstance();
		contentInstance.setContent(content);
		contentInstance.setId(new Long(instanceId));
		return createContentInstance(user, containerId, contentInstance);
	}

	public ContentInstance createContentInstance(String user, AHContainerAddress containerId, ContentInstance contentInstance) 
			throws M2MHapException {
		return createContentInstance(user, containerId, contentInstance, true);
	}
	
	private ContentInstance createContentInstance(String user, AHContainerAddress containerId, ContentInstance contentInstance, boolean useLocalCache)
			throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (!isLocalOnlyM2MDevice)
			checkM2MDeviceStartedStatus();
		if (useLocalCache) {
			createLocalContentInstance(user, containerId, contentInstance);
			if (checkAttributeIdFilter(HapServiceConfiguration.LOCAL_ONLY_ATTRIBUTE_ID_FILTER, containerId.getContainerName()))
				// Local only container
				return contentInstance;
		}
		String contentInstanceAddresssedId = ((AHM2MContainerAddress)containerId).getM2MContainerAdress().getContentInstancesUrl();
		if (!isLocalOnlyM2MDevice) {
			checkM2MDeviceConnectedStatus();
			try {
				contentInstance = m2mNetworkScl.createSclContentInstance(((AHM2MContainerAddress)containerId).getM2MContainerAdress(), contentInstance);
			} catch (M2MServiceException e) {
				throw new M2MHapException(HAP_SERVER_COMMUNICATION_PROBLEMS);
			}
		}
		synchronized (cisItems) {
			ContentInstanceItems ciisLatest = null;
			String containerName = containerId.getContainerName();
			ciisLatest = new ContentInstanceItems();
			ciisLatest.setAddressedId(contentInstanceAddresssedId);
			ciisLatest.getContentInstances().add(contentInstance);
			latestCisItems.put(contentInstanceAddresssedId, ciisLatest);
		}
		return contentInstance;
	}
	
	public ContentInstance createContentInstanceQueued(String user, AHContainerAddress containerId, long instanceId, Object content, boolean sync) throws M2MHapException {
		ContentInstance contentInstance = new ContentInstance();
		contentInstance.setContent(content);
		contentInstance.setId(new Long(instanceId));
		return createContentInstanceQueued(user, containerId, contentInstance, sync);
	}
	
	public ContentInstance createContentInstanceQueued(String user, AHContainerAddress containerId, ContentInstance contentInstance, boolean sync) throws M2MHapException {
		boolean isLocalOnlyM2MDevice = isLocalOnlyM2MDevice();
		if (!isLocalOnlyM2MDevice)
			checkM2MDeviceStartedStatus();
		Map<AHContainerAddress, HapContentInstancesQueue> queuesMap = sync ? syncContentInstancesQueuesMap : contentInstancesQueuesMap;
		HapContentInstancesQueue ciQueue = null;
		createLocalContentInstance(user, containerId, contentInstance);
		if (checkAttributeIdFilter(HapServiceConfiguration.LOCAL_ONLY_ATTRIBUTE_ID_FILTER, containerId.getContainerName()))
			// Local only container
			return contentInstance;
		// TODO:!!! Remove when new alias for core configuration containers are managed by HAP platform
		String containerName = containerId.getContainerName();
		if (containerName != null) {
			String url; 
			if (containerName.equals(AHContainers.attrId_ah_core_config_name)) {
				url = containerId.getUrl();
				url = url.replace(AHContainers.attrId_ah_core_config_name, AHContainers.attrId_ah_cluster_ah_ConfigServer_Name);
				containerId = new AHM2MContainerAddress(url);
			} else if (containerName.equals(AHContainers.attrId_ah_core_config_category)) {
				url = containerId.getUrl();
				url = url.replace(AHContainers.attrId_ah_core_config_category, AHContainers.attrId_ah_cluster_ah_ConfigServer_CategoryPid);
				containerId = new AHM2MContainerAddress(url);
			} else if (containerName.equals(AHContainers.attrId_ah_core_config_location)) {
				url = containerId.getUrl();
				url = url.replace(AHContainers.attrId_ah_core_config_location, AHContainers.attrId_ah_cluster_ah_ConfigServer_LocationPid);
				containerId = new AHM2MContainerAddress(url);
			}
		}
		synchronized (queuesMap) {
			ciQueue = queuesMap.get(containerId);
			if (ciQueue == null) {
				ciQueue = new HapContentInstancesQueue((AHM2MContainerAddress)containerId, sync);
				queuesMap.put(containerId, ciQueue);
			}
			ciQueue.addContentInstance(contentInstance);
		}
		return contentInstance;
	}
	
	public void removeLocalCache(String user, String appliancePid) {
		synchronized (localCisItems) {
			for (Iterator iterator = localCisItems.keySet().iterator(); iterator.hasNext();) {
				String contentInstanceAddressedId = (String) iterator.next();
				AHM2MContainerAddress contentInstanceAddress = new AHM2MContainerAddress(contentInstanceAddressedId);
				if (appliancePid.equals(contentInstanceAddress.getAppliancePid()))
					iterator.remove();
			} 	
		}
	}
	
	public ContentInstance createLocalContentInstance(String user, AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		ContentInstance contentInstance = new ContentInstance();
		contentInstance.setContent(content);
		contentInstance.setId(new Long(instanceId));
// No creation time is initialized
//		contentInstance.setCreationTime(new Long(System.currentTimeMillis()));
		String contentInstanceAddresssedId = ((AHM2MContainerAddress)containerId).getM2MContainerAdress().getContentInstancesUrl();
		synchronized (localCisItems) {
			ContentInstanceItems ciisLatest = new ContentInstanceItems();
			if (contentInstanceAddresssedId.startsWith(M2MConstants.URL_SCL_BASE))
				contentInstanceAddresssedId = contentInstanceAddresssedId.replace(M2MConstants.URL_SCL_BASE, M2MConstants.URL_HAG_SCL_BASE);
			ciisLatest.setAddressedId(contentInstanceAddresssedId);
			ciisLatest.getContentInstances().add(contentInstance);
			localCisItems.put(contentInstanceAddresssedId, ciisLatest);		
		}
		return contentInstance;
	}
	
	public ContentInstance createLocalContentInstance(String user, AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		// A new copy of content instance is made and creation time is initialized
		return createLocalContentInstance(user, containerId, contentInstance.getId().longValue(), contentInstance.getContent());
	}	
	
	private void notifyServiceReset() {
		if (listeners.size() > 0) {
			// Array copy is needed because inside this cycle a new listener
			// can be added
			// or removed inside the networkSclConnected method
			M2MHapServiceListener[] listenersArray = new M2MHapServiceListener[listeners.size()];
			listeners.toArray(listenersArray);		
			for (int i = 0; i < listenersArray.length; i++) {
				listenersArray[i].serviceReset();
			}
		}		
	}
	
	private void checkDeviceIdUpdate() {
		synchronized (serviceStatus) {
			M2MDeviceConfig config = m2mDevice.getConfiguration();
			String newDeviceId = null;
			if (config.isValid())
				newDeviceId = config.getDeviceId();
			else
				LOG.warn ("M2M Device invalid configuration detected");
			if (deviceId == null && newDeviceId != null ||
					deviceId != null && newDeviceId == null ||
					deviceId != null && !deviceId.equals(newDeviceId)) {
				deviceId = newDeviceId;
				initPersistentBuffers(deviceId);
				initQueues();
				initLocalCache();
				notifyServiceReset();
			}			
		}
	}

	
	public void deviceStarted() {
		LOG.debug("M2M Device started");
		checkDeviceIdUpdate();
	}

	public void deviceStopped() {
		LOG.debug("M2M Device stopped");
	}
	
	public void deviceConfigUpdated() {
		LOG.debug("M2M Device device config updated");
// Device configuration updated is managed throgh stop and start forced on M2MDevice
//		checkDeviceIdUpdate();
	}

	public void networkSclConnected() {
		synchronized (serviceStatus) {
			if (listeners.size() > 0) {
				// Array copy is needed because inside this cycle a new listener
				// can be added
				// or removed inside the networkSclConnected method
				M2MHapServiceListener[] listenersArray = new M2MHapServiceListener[listeners.size()];
				listeners.toArray(listenersArray);		
				for (int i = 0; i < listenersArray.length; i++) {
					listenersArray[i].hagConnected();
				}
			}
		}
	}

	public void networkSclDisconnected() {
		synchronized (serviceStatus) {
			if (listeners.size() > 0) {
				// Array copy is needed because inside this cycle a new listener
				// can be added
				// or removed inside the networkSclConnected method
				M2MHapServiceListener[] listenersArray = new M2MHapServiceListener[listeners.size()];
				listeners.toArray(listenersArray);		
				for (int i = 0; i < listenersArray.length; i++) {
					listenersArray[i].hagDisconnected();
				}
			}
		}		
	}

	
}
