/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IAttributeValuesListener;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.energy_home.jemma.ah.hap.client.AALContainers;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.EHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.SHContainers;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItemStatus;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsStatus;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.m2m.M2MXmlConverter;
import org.energy_home.jemma.m2m.M2MXmlObject;
import org.energy_home.jemma.m2m.Scl;
import org.energy_home.jemma.m2m.SclItems;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class HapProxy extends HttpServlet implements IApplicationService, IAttributeValuesListener {
	private static final Log log = LogFactory.getLog(HapProxy.class);

	private static final String servletUri = "/HAP";
	private static final String sclsListUri = M2MConstants.URL_HAG_SCL_BASE + M2MConstants.URL_SCLS + M2MConstants.URL_SLASH + M2MContainerAddress.ALL_ID_FILTER;
	
	public static final String START_INSTANCE_ID_REQUEST_PARAM = "startInstanceId";
	public static final String END_INSTANCE_ID_REQUEST_PARAM = "endInstanceId";
	
	public static final String ACTIVE_APP_FILTER = "/ACTIVE";
	public static final String ACTIVE_APP_FILTER_VALUE = M2MConstants.URL_SLASH + AHM2MContainerAddress.DEFAULT_APPLIANCE_PREFIX;
	
	public static final String DEVICES_EP_FILTER = "/DEVICES";
	public static final String DEVICES_EP_FILTER_VALUE = "/POS";
	
	public static final String NODES_EP_FILTER = "/NODES";
	public static final String NODES_EP_FILTER_VALUE = "/0";
	
	public static final String CONFIG_ATTR_FILTER = "/CONFIG";
	public static final String CONFIG_ATTR_FILTER_VALUE = "/ah.core.config.";
	
	public static final String STATUS_ATTR_FILTER = "/STATUS";
	public static final String STATUS_ATTR_FILTER_VALUE = "/ah.sh.";
	
	public static final String EH_STATUS_ATTR_FILTER = "/EHSTATUS";
	public static final String EH_STATUS_ATTR_FILTER_VALUE = "/ah.eh.esp.";
	
	public static final String AAL_STATUS_ATTR_FILTER = "/AALSTATUS";
	public static final String AAL_STATUS_ATTR_FILTER_VALUE = "/ah.aal.";
	
	public static final String HTTP_RESPONSE_TYPE = "application/xml; charset=UTF-8";

	public static final int HTTP_RESPONSE_BUFFER_SIZE = 16 * 1024;
	
	private class ApplianceConfiguration {
		private Map<Integer, Map> endPointsConfigurations = new HashMap(1);
		
		Map<Integer, Map> getEndPointConfigurations() {
			return endPointsConfigurations;
		}
		
		Map getEndPointConfiguration(int endPointId) {
			return endPointsConfigurations.get(new Integer(endPointId));
		}
	}
	
	private IAppliancesProxy appliancesProxy;	
	private ApplianceProxyList applianceProxyList = new ApplianceProxyList();
	
	private M2MNetworkScl networkScl;
	private AHM2MHapService hapService;
	
	private INetworkManager zbNetworkManager;
	
	private SclItems sclItems = null;
	private Scl scl = null;
	private String indexPage = null;

	private M2MXmlConverter xmlConverter;
	
	private Map<String, ApplianceConfiguration> applianceConfigUpdates = new HashMap();
	
	private boolean addConfigurationUpdate(String appliancePid, String endPointId, String property, Object value) {
		synchronized (applianceConfigUpdates) {
			Map config = null;
			ApplianceConfiguration applianceConfiguration = applianceConfigUpdates.get(appliancePid);
			if (applianceConfiguration != null)
				config = applianceConfiguration.getEndPointConfiguration(new Integer(endPointId));
			if (config == null)
				config = appliancesProxy.getApplianceConfiguration(appliancePid, new Integer(0));
			if (config != null) {
				config.put(property, value);
				if (applianceConfiguration == null) {
					applianceConfiguration = new ApplianceConfiguration();
					applianceConfigUpdates.put(appliancePid, applianceConfiguration);
				}
				applianceConfiguration.getEndPointConfigurations().put(new Integer(endPointId), config);
				return true;
			} 
			return false;
		}
	}
	
	private void updateApplianceConfigurations() {
		synchronized (applianceConfigUpdates) {
			for (Iterator<Entry<String, ApplianceConfiguration>> iterator = applianceConfigUpdates.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, ApplianceConfiguration> entry = (Entry<String, ApplianceConfiguration>) iterator.next();
				String appliancePid = entry.getKey();
				for (Iterator<Entry<Integer, Map>> iterator2 = entry.getValue().getEndPointConfigurations().entrySet().iterator(); iterator2.hasNext();) {
					Entry<Integer, Map> entry2 = (Entry<Integer, Map>) iterator2.next();
					appliancesProxy.updateApplianceConfiguration(appliancePid, entry2.getKey(), entry2.getValue());
					iterator2.remove();
				}
				iterator.remove();
			}
		}	
	}
	
	private static void initResponse(HttpServletResponse response) {
		response.setContentType(HTTP_RESPONSE_TYPE);
		response.setBufferSize(HTTP_RESPONSE_BUFFER_SIZE);
	}
	
	private String replaceFilters(String requestUri) {
		requestUri = requestUri.replace(ACTIVE_APP_FILTER, ACTIVE_APP_FILTER_VALUE);
		requestUri = requestUri.replace(DEVICES_EP_FILTER, DEVICES_EP_FILTER_VALUE);
		requestUri = requestUri.replace(CONFIG_ATTR_FILTER, CONFIG_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(NODES_EP_FILTER, NODES_EP_FILTER_VALUE);
		requestUri = requestUri.replace(STATUS_ATTR_FILTER, STATUS_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(EH_STATUS_ATTR_FILTER, EH_STATUS_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(AAL_STATUS_ATTR_FILTER, AAL_STATUS_ATTR_FILTER_VALUE);
		return requestUri;
	}
	
	private void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse) throws IOException {
		HttpEntity entity = proxyResponse.getEntity();
		if (entity != null) {
			OutputStream servletOutputStream = servletResponse.getOutputStream();
			try {
				entity.writeTo(servletOutputStream);
			} finally {
				try {
					EntityUtils.consume(entity);
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					if (servletOutputStream != null)
						servletOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void writeXmlObject(HttpServletResponse servletResponse, M2MXmlObject object) throws IOException {
		OutputStream servletOutputStream = servletResponse.getOutputStream();
		try {
			xmlConverter.writeObject(object, servletOutputStream);
		} finally {
			try {
				if (servletOutputStream != null)
					servletOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Object readXmlObject(HttpServletRequest servletRequest) throws IOException {
		InputStream servletInputStream = servletRequest.getInputStream();
		try {
			return xmlConverter.readObject(servletInputStream);
		} finally {
			try {
				if (servletInputStream != null)
					servletInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void writeStringObject(HttpServletResponse servletResponse, String object) throws IOException {
		PrintWriter pw = servletResponse.getWriter();
		try {
			if (indexPage != null)
				pw.append(object);
		} finally {
			if (pw != null)
				pw.close();
		}
	}
	
	private ContentInstance getZigBeeNetworkContentInstance() {
		if (zbNetworkManager == null)
			return null;
		try {
			boolean isOpen = zbNetworkManager.isNetworkOpen();
			ContentInstance ci = new ContentInstance();
			ci.setId(System.currentTimeMillis());
			if (isOpen)
				ci.setContent(new Integer(1));
			else
				ci.setContent(new Integer(0));
			return ci;
		} catch (Exception e) {
			log.error("Error while opening zigbee network", e);
			return null;
		}
	}	
	
	private ContentInstance openZigBeeNetwork() {
		if (zbNetworkManager == null)
			return null;
		try {
			zbNetworkManager.openNetwork();
		} catch (Exception e) {
			log.error("Error while opening zigbee network", e);
		}
		return getZigBeeNetworkContentInstance();
	}
	
	private ContentInstance closeZigBeeNetwork() {
		if (zbNetworkManager == null)
			return null;
		try {
			zbNetworkManager.closeNetwork();
		} catch (Exception e) {
			log.error("Error while closing zigbee network", e);
		}
		return getZigBeeNetworkContentInstance();
	}
	
	public HapProxy(){
		log.info("HapProxy constructor");
	}
	
	public void addNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			log.error("addNetworkManager: eceived invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}

	public void removeNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			log.error("removeNetworkManager: eceived invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}
	
	public void setHttpService(HttpService httpService) {
		log.debug("setHttpService");
		try {
			httpService.registerServlet(servletUri, this, null, null);
		} catch (ServletException e) {
			log.error("setHttpService", e);
		} catch (NamespaceException e) {
			log.error("setHttpService", e);
		}

	}

	public void unsetHttpService(HttpService httpService) {
		log.debug("unsetHttpService");
		httpService.unregister(servletUri);
	}
	
	private void initIndexPage(String sclId) {
		StringBuilder sb = new StringBuilder("<html><body><h1>M2M Resources</h1>");
		if (sclId == null) {
			sb.append("<h2>Resources not available (gateway identifier not yet configured)</h2>");
		} else {
			scl = new Scl();
			scl.setId(sclId);
			scl.setCreationTime(System.currentTimeMillis());
			scl.setLastModifiedTime(System.currentTimeMillis());
			scl.setSclBaseId(M2MConstants.URL_HAG_SCL_BASE + M2MConstants.URL_SCLS + M2MConstants.URL_SLASH + scl.getId());
			sclItems = new SclItems();
			sclItems.setAddressedId(M2MConstants.URL_HAG_SCL_BASE + M2MConstants.URL_SCLS);
			List<Scl> sclList = sclItems.getScls();
			sclList.add(scl);
			
			sb.append("<h2>Gateway Resources (HAG)</h2>");
	
			sb.append("<h3>Core resources</h3>");
			
			sb.append("<a href=").append(M2MConstants.URL_HAG_SCL_BASE).append(M2MConstants.URL_SCLS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append(">Gateway description</a><br>");
	
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(AHContainers.attrId_ah_zigbee_network_status);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">ZigBee network status (0=closed, 1=open)</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance type list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(NODES_EP_FILTER).append(CONFIG_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance configuration list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(NODES_EP_FILTER).append(M2MConstants.URL_SLASH).append(AHContainers.attrId_ah_core_appliance_events);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance events list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(DEVICES_EP_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Device type list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(DEVICES_EP_FILTER).append(CONFIG_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Device configuration list</a><br>");
			
			sb.append("<h3>Application resources</h3>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(DEVICES_EP_FILTER).append(STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Smart Home device status list</a><br>");	
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(DEVICES_EP_FILTER).append(EH_STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Energy@home device status list</a><br>");	
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(ACTIVE_APP_FILTER).append(DEVICES_EP_FILTER).append(AAL_STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">AAL device status list</a><br>");
					
			sb = sb.append("<h2>Cloud Resources (HAP)</h2>");
				
			String hapScl = scl.getSclBaseId().replace("HAG", "SB");
			long time = System.currentTimeMillis();
			String queryString = "?startInstanceId=" + (time - 86400000) + "&endInstanceId="+ time;
			
			sb.append("<h3>Application resources</h3>");
			
			// Currently works only with default end point id (1) used in Energy@home trial		
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_deliveredEnergySum).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Delivered energy sum data</a><br>");
			
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_receivedEnergySum).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Received energy sum data</a><br>");
			
			// Currently works only with default end point id (1) used in Energy@home trial
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_hourlyEnergy).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Hourly energy consumption data</a><br>");
			
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_hourlyDeliveredEnergy).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Delivered hourly energy data</a><br>");
			
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergy).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Received hourly energy data</a><br>");
		}
		sb.append("</body></html>");
		indexPage = sb.toString();
	}
	
	public void setM2MHapService(IM2MHapService hapService) {
		log.debug("setM2MHapService");
		this.hapService = new AHM2MHapService(hapService);
		initIndexPage(hapService.getLocalHagId());
	}

	public void unsetM2MHapService(IM2MHapService hapService) {
		log.debug("unsetM2MHapService");
		this.hapService = null;
		sclItems = null;
		scl = null;
	}
	
	public void setAppliancesProxy(IAppliancesProxy appProxy) {
		log.debug("setAppliancesProxy");
		this.appliancesProxy = appProxy;
	}

	public void unsetAppliancesProxy(IAppliancesProxy appProxy) {
		log.debug("unsetAppliancesProxy");
		this.appliancesProxy = null;
	}

	public void setM2MNetworkScl(M2MNetworkScl networkScl) {
		log.debug("setM2MNetworkScl");
		this.networkScl = networkScl;
	}

	public void unsetM2MNetworkScl(M2MNetworkScl httpService) {
		log.debug("unsetM2MNetworkScl");
		this.networkScl = null;
	}

	public void start() {
		log.debug("start");
		xmlConverter = M2MXmlConverter.getCoreConverter();
	}

	public void stop() {
		log.debug("stop");
		applianceProxyList.clear();
		xmlConverter = null;
	}
	
	private boolean isAnUnconfirmedCommand(AHContainerAddress containerAddress) {
		String containerName = containerAddress.getContainerName();
		return containerName.equals(AHContainers.attrId_ah_zigbee_network_status) ||
				containerName.equals(EHContainers.attrId_ah_eh_esp_onOffStatus) ||
				containerName.equals(AALContainers.attrId_ah_aal_onoffstatus) ||
				containerName.equals(SHContainers.attrId_ah_sh_onoff_status);
	}
	
	private ContentInstance postContentInstance(AHContainerAddress containerAddress, ContentInstance ci) throws ApplianceException, ServiceClusterException {
		String containerName = containerAddress.getContainerName();
		if (containerName == null)
			return null;
		String appliancePid = containerAddress.getAppliancePid();
		String endPointId = containerAddress.getEndPointId();
		if (containerName.equals(AHContainers.attrId_ah_zigbee_network_status)) {
			Integer open = (Integer) ci.getContent();
			if (open.intValue() > 0) {
				ci = openZigBeeNetwork();
			} else {
				ci = closeZigBeeNetwork();
			}
		} else if (appliancePid != null && endPointId != null) {
			Integer endPointIdInteger = null; 
			try {
				endPointIdInteger = new Integer(endPointId);
			} catch (Exception e) {
				log.error("Error while parsing endPointId");
				return null;
			}
			if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_Name) ||
					containerName.equals(AHContainers.attrId_ah_core_config_name)) {
				String name = (String) ci.getContent();	
				try {
					if (!addConfigurationUpdate(appliancePid, endPointId, IAppliance.APPLIANCE_NAME_PROPERTY, name))
						ci = null;
				} catch (Exception e) {
					log.error("Error while trying to modify appliance name", e);
					ci = null;
				}
			} else if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_CategoryPid) ||
				containerName.equals(AHContainers.attrId_ah_core_config_category)) {
				Integer categoryPid = (Integer) ci.getContent();
				try {
					if (!addConfigurationUpdate(appliancePid, endPointId, IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, categoryPid.toString()))
						ci = null;
				} catch (Exception e) {
					log.error("Error while trying to modify appliance category", e);
					ci = null;
				}
			} else if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_LocationPid) ||
					containerName.equals(AHContainers.attrId_ah_core_config_location)) {
				Integer locationPid = (Integer) ci.getContent();
				try {
					if (!addConfigurationUpdate(appliancePid, endPointId, IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, locationPid.toString()))
						ci = null;
				} catch (Exception e) {
					log.error("Error while trying to modify appliance location", e);
					ci = null;
				}
			} else if (containerName.equals(EHContainers.attrId_ah_eh_esp_onOffStatus) ||
					containerName.equals(AALContainers.attrId_ah_aal_onoffstatus) ||
					containerName.equals(SHContainers.attrId_ah_sh_onoff_status)) {	
				Boolean value = (Boolean) ci.getContent();
				
				ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
				if (applianceProxy == null)
					return null;			
				IServiceCluster onOffServer = applianceProxy.getServiceCluster(endPointIdInteger.intValue(), OnOffServer.class.getName()); 
				if (onOffServer != null && onOffServer.isAvailable()) {
					if (value)
						((OnOffServer)onOffServer).execOn(applianceProxy.getApplicationRequestContext());
					else
						((OnOffServer)onOffServer).execOff(applianceProxy.getApplicationRequestContext());
				} else {
					return null;
				}
			}
		}
		if (ci != null)
			ci.setId(new Long(System.currentTimeMillis()));
		return ci;
	}
	
	private ContentInstancesBatchResponse postBatchRequest(ContentInstancesBatchRequest batchRequest) {
		ContentInstancesBatchResponse batchResponse = new ContentInstancesBatchResponse();
		List<ContentInstanceItemsStatus> responseStatusList = batchResponse.getContentInstanceItemsStatuses();
		List<ContentInstanceItems> itemsList = batchRequest.getContentInstanceItems();
		for (Iterator iterator = itemsList.iterator(); iterator.hasNext();) {
			ContentInstanceItems contentInstanceItems = (ContentInstanceItems) iterator.next();
			ContentInstanceItemsStatus itemsStatus = new ContentInstanceItemsStatus();
			itemsStatus.setAddressedId(contentInstanceItems.getAddressedId());
			responseStatusList.add(itemsStatus);
			for (Iterator iterator2 = contentInstanceItems.getContentInstances().iterator(); iterator2.hasNext();) {
				ContentInstance ci = (ContentInstance) iterator2.next();
				ContentInstanceItemStatus itemStatus = new ContentInstanceItemStatus();
				itemStatus.setResourceId(new Long(System.currentTimeMillis())); 
				itemsStatus.getContentInstanceItemStatuses().add(itemStatus);
				try {
					AHContainerAddress itemContainerAddress = new AHM2MContainerAddress(contentInstanceItems.getAddressedId());
					if (!isValidLocalHagId(itemContainerAddress.getHagId())) {
						itemStatus.setBatchStatus(HttpServletResponse.SC_NOT_FOUND);
					} else {
						ci = postContentInstance(itemContainerAddress, ci);
						if (ci == null)
							itemStatus.setBatchStatus(HttpServletResponse.SC_NOT_FOUND);
						else if (isAnUnconfirmedCommand(itemContainerAddress))
							itemStatus.setBatchStatus(HttpServletResponse.SC_ACCEPTED);
						else
							itemStatus.setBatchStatus(HttpServletResponse.SC_OK);
					}
				} catch (Exception e) {
					log.error("", e);
					itemStatus.setBatchStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				}				
			}
		}
		return batchResponse;
	}
	
	private ContentInstanceItems getSingleContentInstanceItems(String uri, ContentInstance ci, long startInstanceId, long endInstanceId) {
		ContentInstanceItems ciItems = new ContentInstanceItems();
		ciItems.setAddressedId(uri);
		boolean addContentInstance = true;
		if (startInstanceId != M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID && ci.getId() < startInstanceId)
			addContentInstance = false;
		else if (endInstanceId != M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID && ci.getId() > endInstanceId)
			addContentInstance = false;
		if (addContentInstance)
			ciItems.getContentInstances().add(ci);
		return ciItems;
	}
	
	private boolean isValidLocalHagId(String hagId) {
		return (hagId != null && hagId.equals(hapService.getM2MHapService().getLocalHagId()));	
	}
	
	protected void doGet(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException,
			IOException {
		String requestUri = servletRequest.getRequestURI();
		String method = servletRequest.getParameter("method");
		if (requestUri.endsWith(servletUri)) {
			writeStringObject(servletResponse, indexPage);
			return;
		}
		initResponse(servletResponse);
		requestUri = replaceFilters(requestUri);
		if (requestUri.endsWith(M2MConstants.URL_SLASH))
			requestUri = requestUri.substring(0, requestUri.length()-1);
		if (requestUri.startsWith(M2MConstants.URL_SCL_BASE)) {
			if (networkScl == null) {
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return;
			}

			HttpResponse proxyResponse = null;
			OutputStream out = null;

			try {
				String queryString = servletRequest.getQueryString();
				proxyResponse = networkScl.httpGet(requestUri+"?"+queryString);
				int statusCode = proxyResponse.getStatusLine().getStatusCode();
				if (statusCode == HttpStatus.SC_OK) {
					copyResponseEntity(proxyResponse, servletResponse);
				} else {
					// TODO check status code mapping
					servletResponse.sendError(statusCode);
				}
			} catch (Exception e) {
				log.error("service: error while sending request to hap service", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			} 
		} else if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			try {
				M2MXmlObject xmlObject = null;
				if (requestUri.endsWith(sclsListUri)) {
					if (sclItems != null)
						writeXmlObject(servletResponse, sclItems);
					else 
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				} else if (scl != null && requestUri.endsWith(scl.getSclBaseId())) {
					writeXmlObject(servletResponse, scl);
				} else if (scl != null && requestUri.startsWith(scl.getSclBaseId()+M2MConstants.URL_SLASH)) {
					long startInstanceId = M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID;
					String startInstanceIdStr = servletRequest.getParameter(START_INSTANCE_ID_REQUEST_PARAM);
					if (startInstanceIdStr != null && startInstanceIdStr.length() > 0) {
						try {
							startInstanceId = Long.parseLong(startInstanceIdStr);							
						} catch (Exception e) {
							log.error("Error while parsing stratInstanceId parameter");
						}
					}
					long endInstanceId = M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID;
					String endInstanceIdStr = servletRequest.getParameter(END_INSTANCE_ID_REQUEST_PARAM);	
					if (endInstanceIdStr != null && endInstanceIdStr.length() > 0) {
						try {
							endInstanceId = Long.parseLong(endInstanceIdStr);							
						} catch (Exception e) {
							log.error("Error while parsing stratInstanceId parameter");
						}
					}
					String contentInstanceId = null; 
					if (requestUri.endsWith(M2MConstants.URL_CIS_ID_LATEST_ALIAS)) {
						contentInstanceId = M2MConstants.URL_CIS_ID_LATEST_ALIAS;
						requestUri = requestUri.replace(M2MConstants.URL_CIS_ID_LATEST_ALIAS, "");
					}
					if (requestUri.endsWith(M2MConstants.URL_CIS_ID_OLDEST_ALIAS)) {
						contentInstanceId = M2MConstants.URL_CIS_ID_OLDEST_ALIAS;
						requestUri = requestUri.replace(M2MConstants.URL_CIS_ID_OLDEST_ALIAS, "");
					}
					AHContainerAddress containerAddress = new AHM2MContainerAddress(requestUri);
					if (!isValidLocalHagId(containerAddress.getHagId()))
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);					
					boolean isFilter = containerAddress.isFilterAddress();
					String containerName = containerAddress.getContainerName();
					if (containerName != null && containerName.equals(AHContainers.attrId_ah_zigbee_network_status)) {
						xmlObject = getZigBeeNetworkContentInstance();
						// If latest or oldest is specified a ContentInstance is returned, otherwise a ContentInstanceItems 
						// with a single item is built
						if (xmlObject != null && contentInstanceId == null) {
							xmlObject = getSingleContentInstanceItems(requestUri, (ContentInstance)xmlObject, startInstanceId, endInstanceId);
						}
					} else if (!isFilter) {
						xmlObject = hapService.getM2MHapService().getLocalContentInstance(containerAddress);
						if (xmlObject != null && contentInstanceId == null) {
							// If latest or oldest is specified a ContentInstance is returned, otherwise a ContentInstanceItems 
							// with a single item is built
							xmlObject = getSingleContentInstanceItems(requestUri, (ContentInstance)xmlObject, startInstanceId, endInstanceId);
						}
					} else {
						xmlObject= hapService.getM2MHapService().getLocalContentInstanceItemsList(containerAddress, startInstanceId, endInstanceId);
					}
					
					if (xmlObject == null) {
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					} else {
						writeXmlObject(servletResponse, xmlObject);
					}
				} else {
					servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			} catch (Exception e) {
				log.error("service: error while parsing local request", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			} 
		} else {
			servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected void doPost(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException,
			IOException {
		String requestUri = servletRequest.getRequestURI();
		initResponse(servletResponse);
		requestUri = replaceFilters(requestUri);
		if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			try {
				if (requestUri.endsWith(M2MConstants.URL_CIS_BATCH_REQUEST)) {
					ContentInstancesBatchRequest batchRequest = (ContentInstancesBatchRequest) readXmlObject(servletRequest);
					ContentInstancesBatchResponse batchResponse = postBatchRequest(batchRequest);
					writeXmlObject(servletResponse, batchResponse);
				} else {
					AHContainerAddress containerAddress = new AHM2MContainerAddress(requestUri);
					if (!isValidLocalHagId(containerAddress.getHagId()))
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					String containerName = containerAddress.getContainerName();
					if (containerName != null) {
						ContentInstance ci = (ContentInstance)readXmlObject(servletRequest);
						ci = postContentInstance(containerAddress, ci);
						if (ci == null)
							servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND); 	
						else if (isAnUnconfirmedCommand(containerAddress))
							servletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
						else
							servletResponse.setStatus(HttpServletResponse.SC_OK);
						writeXmlObject(servletResponse, ci);					
					} else {
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				}
				updateApplianceConfigurations();
			}
			catch (Exception e) {
				log.error("service: error while parsing local request", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			} 
		} else {
			servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	protected void doDelete(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException,
			IOException {
		String requestUri = servletRequest.getRequestURI();
		initResponse(servletResponse);
		requestUri = replaceFilters(requestUri);
		if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			try {
				AHContainerAddress containerAddress = new AHM2MContainerAddress(requestUri);
				if (!isValidLocalHagId(containerAddress.getHagId()))
					servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				String appliancePid = containerAddress.getAppliancePid();
				if (containerAddress.getAppliancePid() != null && containerAddress.getEndPointId() == null && 
						containerAddress.getContainerName() == null) {
					ContentInstance ci = hapService.getM2MHapService().getLocalContentInstance(containerAddress); 
					boolean result = false;
					// Only existing resources are deleted
					if (ci != null) {
						result = appliancesProxy.deleteAppliance(appliancePid);
						if (!result) {
							servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
							return;
						}
					} else {
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
						return;
					}

				} else {
					servletResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
				}
				ContentInstance ci = new ContentInstance();
				ci.setId(System.currentTimeMillis());
				writeXmlObject(servletResponse, ci);
			} catch (Exception e) {
				log.error("service: error while parsing local request", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			}
		} else {
			servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}	

	// ****************** 
	
	private void applianceAndEndPointTypesUpdated(IAppliance appliance, long timestamp) {
		try {
			String appliancePid = appliance.getPid();
			String applianceType = appliance.getDescriptor().getType();
			
			try {
				hapService.storeAttributeValue(appliancePid, null, null, null, timestamp, applianceType, true);
			} catch (HacException e) {
				log.error("applianceAndEndPointTypesUpdated: exception while storing appliance type", e);
			}
			IEndPoint[] endPoints = appliance.getEndPoints();
			IEndPoint endPoint = null;
			for (int i = 0; i < endPoints.length; i++) {
				try {
					 endPoint = endPoints[i];
					 hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), null, null, timestamp, endPoint.getType(), true);
				} catch (HacException e) {
					log.error("applianceAndEndPointTypesUpdated: exception while storing appliance type", e);
				}
			}
		} catch (Exception e) {
			log.error("applianceAndEndPointTypesUpdated", e);
		}
	}
	
	private void applianceStatusUpdated(ApplianceProxy applianceProxy, int appStatus, long timestamp) {
		try {
			IAppliance appliance = applianceProxy.getAppliance();
			String appliancePid = appliance.getPid();
			hapService.storeAttributeValue(appliancePid, IEndPoint.COMMON_END_POINT_ID, null, AHContainers.attrId_ah_core_appliance_events, timestamp, new Integer(appStatus), false);
		} catch (Exception e) {
			log.error("applianceStatusUpdated", e);
		}
	}	
	
	private void endPointConfigurationUpdated(ApplianceProxy applianceProxy, IEndPoint endPoint, long timestamp) {
		try {
			IAppliance appliance = applianceProxy.getAppliance();
			String appliancePid = appliance.getPid();
			String clusterName = ConfigServer.class.getName();
			ConfigServer configServer = (ConfigServer) endPoint.getServiceCluster(clusterName);
			String categoryPid = configServer.getCategoryPid(applianceProxy.getApplicationRequestContext());
			String locationPid = configServer.getLocationPid(applianceProxy.getApplicationRequestContext());
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), clusterName, ConfigServer.ATTR_NAME_NAME, timestamp, configServer.getName(confirmationRequestContext), true);
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), null, AHContainers.attrId_ah_core_config_name, timestamp, configServer.getName(applianceProxy.getApplicationRequestContext()), true);
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), clusterName, ConfigServer.ATTR_NAME_LOCATION_PID, timestamp, locationPid != null ? new Integer(locationPid) : null, true);
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), null, AHContainers.attrId_ah_core_config_location, timestamp, locationPid != null ? new Integer(locationPid) : null, true);					
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), clusterName, ConfigServer.ATTR_NAME_CATEGORY_PID, timestamp, categoryPid != null ? new Integer(categoryPid) : null, true);	
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), null, AHContainers.attrId_ah_core_config_category, timestamp, categoryPid != null ? new Integer(categoryPid) : null, true);	

		} catch (Exception e) {
			log.error("endPointConfigurationUpdated", e);
		}
	}
	
	private void configurationUpdated(ApplianceProxy applianceProxy, long timestamp) {
		try {
			IAppliance appliance = applianceProxy.getAppliance();
			String appliancePid = appliance.getPid();
			IEndPoint[] endPoints = appliance.getEndPoints();
			for (int i = 0; i < endPoints.length; i++) {
				endPointConfigurationUpdated(applianceProxy, endPoints[i], timestamp);
			}
		} catch (Exception e) {
			log.error("configurationUpdated", e);
		}	
	}	
	
	// IApplicationService Interface
	public IServiceCluster[] getServiceClusters() {
		// No service cluster are exported
		return null;
	}

	public void notifyApplianceAdded(IApplicationEndPoint endPoint, IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			if (appliance.isSingleton()) {
				log.info("applianceConnected - singleton appliance " + appliancePid);
				return;				
			}
			ApplianceProxy applianceProxy = new ApplianceProxy(endPoint, appliance);
			applianceProxyList.addApplianceProxy(applianceProxy);		
			boolean isAvailable = appliance.isAvailable();
			long timestamp = System.currentTimeMillis();
			applianceAndEndPointTypesUpdated(appliance, timestamp);
			applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_STARTED, timestamp);
			timestamp++;
			if (isAvailable)
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_AVAILABLE, timestamp);
			else 
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_UNAVAILABLE, timestamp);
			configurationUpdated(applianceProxy, timestamp);
		} catch (Exception e) {
			log.error("notifyApplianceAdded error", e);
		}	
	}

	public void notifyApplianceRemoved(IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
			if (applianceProxy == null)
				return;
			applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_STOPPED, System.currentTimeMillis());	
			applianceProxyList.removeApplianceProxy(appliance.getPid());	
		} catch (Exception e) {
			log.error("notifyApplianceRemoved error", e);
		}		
	}
	
	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
			if (applianceProxy == null)
				return;
			boolean isAvailable = appliance.isAvailable();
			long timestamp = System.currentTimeMillis();
			if (isAvailable)
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_AVAILABLE, timestamp);
			else
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_UNAVAILABLE, timestamp);			
		} catch (Exception e) {
			log.error("notifyApplianceAvailabilityUpdated error", e);
		}		
	}

	public void notifyAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue) {
		// The IAttributeValuesListener in not currently registerd by happroxy component (not used)
		ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
		if (applianceProxy == null)
			log.info ("notifyAttributeValue called with a not yet connected appliance " + appliancePid);
		else 
			log.info(String.format("notifyAttributeValue: appliancePid=%s, endPointId=%s, clusterName=%s, attributeName=%s, timestamp=%s, value=%s",
					appliancePid, endPointId, clusterName, attributeName, attributeValue.getTimestamp(), attributeValue.getValue()));

	}


}