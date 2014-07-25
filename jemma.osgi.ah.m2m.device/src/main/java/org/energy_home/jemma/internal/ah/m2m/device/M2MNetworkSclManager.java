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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.m2m.Scl;
import org.energy_home.jemma.m2m.SclStatusEnumeration;
import org.energy_home.jemma.utils.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M2MNetworkSclManager {

	private static final Logger LOG = LoggerFactory.getLogger( M2MNetworkSclManager.class );

	private static String buildUri(String nwkSclHostAddress, M2MContainerAddress containerId, String startInstanceId, String endInstanceId) {
		// Additional parameters: startResultIndex, maxResultNumber
		StringBuilder strUri = new StringBuilder(nwkSclHostAddress);
		strUri.append(containerId.getContentInstancesUrl());
		if (startInstanceId != null && endInstanceId == null) {
			strUri.append(startInstanceId);
		} else if (endInstanceId != null) {
			strUri.append("?startInstanceId=");
			strUri.append(startInstanceId);
			strUri.append("&endInstanceId=");
			strUri.append(endInstanceId);
		}
		return strUri.toString();
	}

	private String getContentInstanceId(long instanceId) {
		String strInstanceId = null;
		if (instanceId == M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID)
			strInstanceId = M2MConstants.URL_CIS_ID_LATEST_ALIAS;
		else if (instanceId == M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID)
			strInstanceId = M2MConstants.URL_CIS_ID_OLDEST_ALIAS;
		else
			strInstanceId = Long.toString(instanceId);
		return strInstanceId;
	}

	private Scl scl = new Scl();
	private M2MDeviceManager deviceManager;

	// These properties are set to null in shutdown method
	private RestClient restClient;
	private HttpEntityXmlConverter jaxbConverterFactory;
	private String nwkSclHostAddress = null;
	private String nwkSclHagUri = null;
	private URI nwkSclBaseUri = null;
	private URI sclCisBatchRequestUri = null;

	private void lockRequestResources() throws M2MServiceException {
		deviceManager.signalNetworkSclRequest(true);
	}

	private void releaseRequestResources(HttpResponse response) {
		boolean reconnect = false;
		if (response != null)
			if (RestClient.isUnauthorized(response))
				reconnect = true;
		try {
			if (response != null)
				restClient.consume(response);
		} catch (Exception e) {
			LOG.error("Exception on releaseRequestResources", e);
		}
		deviceManager.signalNetworkSclRequestCompletion(reconnect);
	}

	private void updateScl(SclStatusEnumeration status) throws M2MServiceException {
		HttpResponse response = null;
		Scl sclW = new Scl();
		sclW.setOnLineStatus(status);
		scl.setOnLineStatus(status);
		try {
			response = restClient.put(
					new URI(nwkSclBaseUri.toString() + M2MConstants.URL_SCLS + M2MConstants.URL_SLASH + scl.getId()),
					jaxbConverterFactory.getEntity(sclW));
			M2MUtils.checkHttpResponseStatus(response);
			jaxbConverterFactory.getObject(response.getEntity());
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while updating scl online status: " + scl.getOnLineStatus());
		} finally {
			restClient.consume(response);
		}
	}

	M2MNetworkSclManager(M2MDeviceManager deviceManager) {
		this.deviceManager = deviceManager;
		setDeviceConfig((M2MDeviceConfigObject) deviceManager.getConfiguration());
	}

	M2MDeviceManager getM2MDeviceObject() {
		return deviceManager;
	}

	void setDeviceConfig(M2MDeviceConfigObject deviceConfig) {
		try {
			nwkSclBaseUri = new URI(deviceConfig.getNetworkSclBaseUri());
			nwkSclHostAddress = M2MConstants.URL_HTTP_PREFIX + nwkSclBaseUri.getHost() + M2MConstants.URL_PORT_PREFIX + nwkSclBaseUri.getPort();
			scl.setId(deviceConfig.getSclId());
			sclCisBatchRequestUri = new URI(nwkSclBaseUri + M2MConstants.URL_CIS_BATCH_REQUEST);
			nwkSclHagUri = nwkSclBaseUri + M2MConstants.URL_SCLS + M2MConstants.URL_SLASH + scl.getId();
		} catch (URISyntaxException e) {
			LOG.error("URISyntaxException on setDeviceConfig", e);
		}
	}

	void startup() throws M2MServiceException {
		M2MDeviceConfigObject deviceConfig = (M2MDeviceConfigObject) deviceManager.getConfiguration();
		setDeviceConfig(deviceConfig);
		jaxbConverterFactory = HttpEntityXmlConverter.getCoreConverter();

		// Currently a fixed address is used for network scl base id (see
		// constructor)
		// try {
		// nwkSclBaseUri = new URI(connectionParams.getNwkSclBaseId());
		// sclCisBatchRequestUri = new URI(nwkSclBaseUri +
		// URL_CIS_BATCH_REQUEST);
		// } catch (URISyntaxException e) {
		// M2MUtils.mapDeviceException(log, e,
		// "Startup method: invalid base uri configuration");
		// }
		restClient = RestClient.get();
		restClient.setCredential(nwkSclBaseUri.getHost(), nwkSclBaseUri.getPort(), deviceConfig.getSclId(),
				deviceConfig.getNetworkSclBaseToken());
		// scl.setId(connectionParams.getId());
		updateScl(SclStatusEnumeration.ONLINE);
	}

	void shutdown() {
		try {
			updateScl(SclStatusEnumeration.OFFLINE);
			// scl.setId(null);
			restClient.release();
		} catch (M2MServiceException e) {
			LOG.warn("M2MServiceException while setting offline status", e);
		} catch (Exception e) {
			LOG.error("Generic exception in shutdown method", e);
		}
		restClient = null;
		jaxbConverterFactory = null;
		// nwkSclBaseUri = null;
		// sclCisBatchRequestUri = null;
	}

	public String getSclId(String user) {
		return scl.getId();
	}
	
	public ContentInstance getSclContentInstance(String user, M2MContainerAddress containerId, long instanceId)
			throws M2MServiceException {
		if (containerId.isFilterAddress())
			throw new M2MServiceException("Container id cannot be a filter id");
		String strInstanceId = getContentInstanceId(instanceId);
		HttpResponse response = null;
		URI uri = null;
		try {
			lockRequestResources();
			uri = new URI(buildUri(nwkSclHostAddress, containerId, strInstanceId, null));
			response = restClient.get(uri);
			if (RestClient.isNotFoundStatus(response))
				return null;
			M2MUtils.checkHttpResponseStatus(response);
			return (ContentInstance) jaxbConverterFactory.getObject(response.getEntity());
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while getting latest scl content instance " + strInstanceId
					+ " for container " + containerId.getUrl());
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}

	public ContentInstanceItemsList getSclContentInstanceItemsList(String user, M2MContainerAddress containerFilterId, long instanceId)
			throws M2MServiceException {
		if (!containerFilterId.isFilterAddress())
			throw new M2MServiceException("Container id must be a filter id");
		String strInstanceId = getContentInstanceId(instanceId);
		HttpResponse response = null;
		URI uri = null;
		try {
			lockRequestResources();
			uri = new URI(buildUri(nwkSclHostAddress, containerFilterId, strInstanceId, null));
			response = restClient.get(uri);
			if (RestClient.isNotFoundStatus(response))
				return null;
			M2MUtils.checkHttpResponseStatus(response);
			return (ContentInstanceItemsList) jaxbConverterFactory.getObject(response.getEntity());
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while getting latest scl content instance " + strInstanceId
					+ " for container " + containerFilterId.getUrl());
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}
	
	public ContentInstanceItems getSclContentInstanceItems(String user, M2MContainerAddress containerId, long startInstanceId,
			long endInstanceId) throws M2MServiceException {
		if (containerId.isFilterAddress())
			throw new M2MServiceException("Container id cannot be a filter id");
		HttpResponse response = null;
		URI uri = null;
		String strStartInstanceId = getContentInstanceId(startInstanceId);
		String strEndInstanceId = getContentInstanceId(endInstanceId);
		try {
			lockRequestResources();
			uri = new URI(buildUri(nwkSclHostAddress, containerId, strStartInstanceId, strEndInstanceId));
			response = restClient.get(uri);
			if (RestClient.isNotFoundStatus(response))
				return null;
			M2MUtils.checkHttpResponseStatus(response);
			return (ContentInstanceItems) jaxbConverterFactory.getObject(response.getEntity());
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e,
					"Error while getting content instance items for container " + containerId.getUrl() + " - "
							+ strStartInstanceId + ", " + strEndInstanceId);
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}

	public ContentInstanceItemsList getSclContentInstanceItemsList(String user, M2MContainerAddress containerFilterId,
			long startInstanceId, long endInstanceId) throws M2MServiceException {
		if (!containerFilterId.isFilterAddress())
			throw new M2MServiceException("Container id must be a filter id");
		HttpResponse response = null;
		URI uri = null;
		String strStartInstanceId = getContentInstanceId(startInstanceId);
		String strEndInstanceId = getContentInstanceId(endInstanceId);
		try {
			lockRequestResources();
			uri = new URI(buildUri(nwkSclHostAddress, containerFilterId, strStartInstanceId, strEndInstanceId));
			response = restClient.get(uri);
			if (RestClient.isNotFoundStatus(response))
				return null;
			M2MUtils.checkHttpResponseStatus(response);
			return (ContentInstanceItemsList) jaxbConverterFactory.getObject(response.getEntity());
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while getting content instance items list for container "
					+ containerFilterId.getUrl() + " - " + strStartInstanceId + ", " + strEndInstanceId);
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}

	public ContentInstance createSclContentInstance(String user, M2MContainerAddress containerId, ContentInstance instance)
			throws M2MServiceException {
		HttpResponse response = null;
		URI uri = null;
		try {
			lockRequestResources();
			uri = new URI(buildUri(nwkSclHostAddress, containerId, null, null));
			response = restClient.post(uri, jaxbConverterFactory.getEntity(instance));
			M2MUtils.checkHttpResponseStatus(response);
			ContentInstance ciResponse = (ContentInstance) jaxbConverterFactory.getObject(response.getEntity());
			return ciResponse;
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while creating content instance");
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}

	public ContentInstancesBatchResponse sendContentInstanceBatchRequest(String user, ContentInstancesBatchRequest cibr)
			throws M2MServiceException {
		HttpResponse response = null;
		try {
			lockRequestResources();
			response = restClient.post(sclCisBatchRequestUri, jaxbConverterFactory.getEntity(cibr));
			M2MUtils.checkHttpResponseStatus(response);
			ContentInstancesBatchResponse cibResponse = (ContentInstancesBatchResponse) jaxbConverterFactory.getObject(response
					.getEntity());
			return cibResponse;
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while sending content instance batch request");
			return null;
		} finally {
			releaseRequestResources(response);
		}
	}
	
	public HttpResponse httpGet(String user, String requestUri) throws M2MServiceException {
		HttpResponse response = null;
		try {
			lockRequestResources();
			response = restClient.get(new URI(nwkSclHostAddress + requestUri));
			//response = restClient.get(new URI(nwkSclHagUri+relativeUri));
			return response;
		} catch (Exception e) {
			M2MUtils.mapDeviceException(LOG, e, "Error while sending a get http request");
			return null;
		} finally {
			releaseRequestResources(null);
		}
	}

}
