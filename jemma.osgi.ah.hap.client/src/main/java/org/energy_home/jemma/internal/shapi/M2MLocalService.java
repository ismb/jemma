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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.util.EntityUtils;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.EHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.IM2MLocalListener;
import org.energy_home.jemma.ah.hap.client.IM2MLocalService;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.internal.ah.hap.client.AHM2MContainerAddress;
import org.energy_home.jemma.internal.ah.hap.client.HapServiceManager;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItemStatus;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.m2m.ContentInstanceItemsStatus;
import org.energy_home.jemma.m2m.ContentInstancesBatchRequest;
import org.energy_home.jemma.m2m.ContentInstancesBatchResponse;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.m2m.M2MXmlConverter;
import org.energy_home.jemma.m2m.M2MXmlObject;
import org.energy_home.jemma.m2m.Scl;
import org.energy_home.jemma.m2m.SclItems;
import org.energy_home.jemma.m2m.SclStatusEnumeration;
import org.energy_home.jemma.m2m.Subscription;
import org.energy_home.jemma.m2m.SubscriptionItems;
import org.energy_home.jemma.utils.rest.RestClient;
import org.energy_home.jemma.utils.thread.ExecutorService;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M2MLocalService extends HttpServlet implements IApplicationService, ISubscriptionManager, IM2MLocalService {
	private static final Logger LOG = LoggerFactory.getLogger( M2MLocalService.class );

	private static final long PERIODIC_TASK_TIMEOUT = 117000;
	
	private static final String servletUri = "/HAP";
	private static final String sclsListUri = M2MConstants.URL_HAG_SCL_BASE + M2MConstants.URL_SCLS + M2MConstants.URL_SLASH + M2MContainerAddress.ALL_ID_FILTER;
	
	public static final String START_INSTANCE_ID_REQUEST_PARAM = "startInstanceId";
	public static final String END_INSTANCE_ID_REQUEST_PARAM = "endInstanceId";
	
	public static final String ALL_FILTER = "/" + AHContainerAddress.ALL_ID_FILTER;
	
	public static final String APPLIANCE_FILTER = "/APPLIANCE";
	public static final String APPLIANCE_FILTER_VALUE = M2MConstants.URL_SLASH + AHM2MContainerAddress.DEFAULT_APPLIANCE_PREFIX;
	
	public static final String DEVICES_EP_FILTER = "/DEVICE";
	public static final String DEVICES_EP_FILTER_VALUE = "/POS";
	
	public static final String NODES_EP_FILTER = "/NODE";
	public static final String NODES_EP_FILTER_VALUE = "/0";
	
	public static final String CONFIG_ATTR_FILTER = "/CONFIG";
	public static final String CONFIG_ATTR_FILTER_VALUE = "/ah.core.config.";
	
	public static final String STATUS_ATTR_FILTER = "/STATUS";
	public static final String STATUS_ATTR_FILTER_VALUE = "/ah.cluster.";
	
	public static final String EH_STATUS_ATTR_FILTER = "/EHSTATUS";
	public static final String EH_STATUS_ATTR_FILTER_VALUE = "/ah.eh.esp.";
	
	public static final String AAL_STATUS_ATTR_FILTER = "/AALSTATUS";
	public static final String AAL_STATUS_ATTR_FILTER_VALUE = "/ah.aal.";
	
	public static final String SH_STATUS_ATTR_FILTER = "/SHSTATUS";
	public static final String SH_STATUS_ATTR_FILTER_VALUE = "/ah.sh.";
	
	public static final String HTTP_RESPONSE_TYPE = "application/xml; charset=UTF-8";

	public static final int HTTP_RESPONSE_BUFFER_SIZE = 16 * 1024;
	
	public static final String HTTP_ENTITY_CONTENT_TYPE = "application/xml";
	
	public static boolean isAnUnconfirmedCommand(AHContainerAddress containerAddress) {
		String containerName = containerAddress.getContainerName();
		return containerName.equals(AHContainers.attrId_ah_zigbee_network_status) ||
				ServiceClusterProxy.isAnUnconfirmedCommand(containerAddress);
	}
	
	class NotificationTask implements Runnable {

		ContentInstanceItems cisItems;

		NotificationTask(ContentInstanceItems cisItems) {
			this.cisItems = cisItems;
		}
		
		void releaseRequestResources(HttpResponse response) {
			try {
				if (response != null)
					restClient.consume(response);
			} catch (Exception e) {
				LOG.error("releaseRequestResources: error while consuming rest client response", e);
			}
		}
		
		public HttpEntity getEntity(ContentInstanceItems cisItems) {
			ByteArrayEntity entity = null;
			try {
				byte[] b = xmlConverter.getByteArray(cisItems);
				entity = new ByteArrayEntity(b);
				entity.setContentType(HTTP_ENTITY_CONTENT_TYPE);
			} catch (Exception e) {
				LOG.error("Error while creating http entity from "+ cisItems.toXmlString(),e);
				return null;
			}
			return entity;
		}
		
		public void run() {
			synchronized (m2mLocalListenerList) {
				for (Iterator iterator = m2mLocalListenerList.iterator(); iterator.hasNext();) {
					IM2MLocalListener listener = (IM2MLocalListener) iterator.next();
					AHM2MContainerAddress containerAddressFilter = (AHM2MContainerAddress)listener.getContainerAddressFilter();
					if (containerAddressFilter == null ||
							HapServiceManager.checkItemsOnContainerIdFilter(cisItems, containerAddressFilter.getM2MContainerAdress()))
						listener.notifyContentInstanceItems(cisItems);
				}
			}
			HttpEntity entity = getEntity(cisItems);
			if (entity != null) {
				synchronized(subscriptionInfos) {
					for (Iterator iterator = subscriptionInfos.iterator(); iterator
							.hasNext();) {
						SubscriptionInfo subscriptionInfo = (SubscriptionInfo) iterator.next();
						HttpResponse response = null;
						try {
							AHM2MContainerAddress containerAddressFilter = subscriptionInfo.containerAddressFilter;
							if (containerAddressFilter == null ||
									HapServiceManager.checkItemsOnContainerIdFilter(cisItems, subscriptionInfo.containerAddressFilter.getM2MContainerAdress())) {
								response = restClient.post(subscriptionInfo.uri, entity);
								if (!RestClient.isOkOrCreatedStatus(response)) {
									LOG.warn("Removing subscriber: error " + response.getStatusLine().getStatusCode() + " while contacting " + subscriptionInfo.subscription.getContact());
									iterator.remove();
								}								
							}
						} catch (Exception e) {
							LOG.error("Error while sending request to subscriber " + subscriptionInfo.subscription.getContact(), e);	
							iterator.remove();
						} finally {
							releaseRequestResources(response);
						}	
					}
				}
			} 
		}	
	}
	
	private static RestClient restClient;
	
	private IAppliancesProxy appliancesProxy;	
	private ApplianceProxyList applianceProxyList = new ApplianceProxyList();
	
	private M2MNetworkScl networkScl;
	private AHM2MHapService hapService;	
	private ExecutorService executorService = null;

	private IServiceCluster[] serviceClusterProxies = new IServiceCluster[] {};
	
	private OnOffClusterProxy onOffClusterProxy;
	private MeteringClusterProxy meteringClusterProxy;
	private IASZoneClusterProxy iasClusterProxy;
	private TemperatureMeasurementClusterProxy temperatureMeasurementClusterProxy;
	private RelativeHumidityMeasurementClusterProxy relativeHumidityMeasurementClusterProxy;
	private IlluminanceMeasurementClusterProxy illuminanceMeasurementClusterProxy;
	private ApplianceControlClusterProxy applianceControlClusterProxy;	
	private ApplianceEventsAndAlertsClusterProxy applianceEventsAndAlertsClusterProxy;
	
	private INetworkManager zbNetworkManager;
	
	private SclItems sclItems = null;
	private Scl scl = null;
	
	private List<IM2MLocalListener> m2mLocalListenerList = new ArrayList<IM2MLocalListener>();
	List<SubscriptionInfo> subscriptionInfos;	
	String subscriptionItemsAddressedId;
	
	private String indexPage = null;

	private M2MXmlConverter xmlConverter;
	
	private Map<String, IApplianceConfiguration> applianceConfigurationUpdatesMap = new HashMap();

	
	private IApplianceConfiguration getApplianceConfiguration(String appliancePid, int endPoint) {
		IApplianceConfiguration applianceConfig = null;
		synchronized (applianceConfigurationUpdatesMap) {
			applianceConfig = applianceConfigurationUpdatesMap.get(appliancePid);
			if (applianceConfig == null) {
				IAppliance appliance = appliancesProxy.getAppliance(appliancePid);
				if (appliance == null)
					appliancesProxy.getInstallingAppliance(appliancePid);
				applianceConfig = appliancesProxy.getApplianceConfiguration(appliancePid);
				applianceConfigurationUpdatesMap.put(appliancePid, applianceConfig);
			}
		}
		return applianceConfig;
	}
	
	private boolean commitApplianceConfigurationUpdates() {
		boolean result = true;
		synchronized (applianceConfigurationUpdatesMap) {
			for (Iterator<IApplianceConfiguration> iterator = applianceConfigurationUpdatesMap.values().iterator(); iterator.hasNext();) {
				IApplianceConfiguration config = iterator.next();
				result = result & appliancesProxy.updateApplianceConfiguration(config);	
				iterator.remove();
			}
		}
		return result;
	}
	
	private static void initResponse(HttpServletResponse response) {
		response.setContentType(HTTP_RESPONSE_TYPE);
		response.setBufferSize(HTTP_RESPONSE_BUFFER_SIZE);
	}
	
	private String replaceFilters(String requestUri, boolean isProxy) {
		if (!isProxy) {
			requestUri = requestUri.replace(APPLIANCE_FILTER, APPLIANCE_FILTER_VALUE);
			requestUri = requestUri.replace(DEVICES_EP_FILTER, DEVICES_EP_FILTER_VALUE);
		} else {
			requestUri = requestUri.replace(APPLIANCE_FILTER, ALL_FILTER);
			requestUri = requestUri.replace(DEVICES_EP_FILTER, ALL_FILTER);
		}

		requestUri = requestUri.replace(CONFIG_ATTR_FILTER, CONFIG_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(NODES_EP_FILTER, NODES_EP_FILTER_VALUE);
		requestUri = requestUri.replace(STATUS_ATTR_FILTER, STATUS_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(EH_STATUS_ATTR_FILTER, EH_STATUS_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(AAL_STATUS_ATTR_FILTER, AAL_STATUS_ATTR_FILTER_VALUE);
		requestUri = requestUri.replace(SH_STATUS_ATTR_FILTER, SH_STATUS_ATTR_FILTER_VALUE);
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
			LOG.error("Error while opening zigbee network", e);
			return null;
		}
	}	
	
	private ContentInstance openZigBeeNetwork() {
		if (zbNetworkManager == null)
			return null;
		try {
			zbNetworkManager.openNetwork();
		} catch (Exception e) {
			LOG.error("Error while opening zigbee network", e);
		}
		return getZigBeeNetworkContentInstance();
	}
	
	private ContentInstance closeZigBeeNetwork() {
		if (zbNetworkManager == null)
			return null;
		try {
			zbNetworkManager.closeNetwork();
		} catch (Exception e) {
			LOG.error("Error while closing zigbee network", e);
		}
		return getZigBeeNetworkContentInstance();
	}
	

	
	private void initServiceClusters(ApplianceProxy applianceProxy) {
		for (int i = 0; i < serviceClusterProxies.length; i++) {
			ServiceClusterProxy proxy = (ServiceClusterProxy) serviceClusterProxies[i];
			proxy.initServiceCluster(applianceProxy);
		}
	}
	
	private void checkServiceClusters(ApplianceProxy applianceProxy) {
		for (int i = 0; i < serviceClusterProxies.length; i++) {
			ServiceClusterProxy proxy = (ServiceClusterProxy) serviceClusterProxies[i];
			proxy.checkServiceCluster(applianceProxy);
		}
	}
	
	private void periodicTask() {
		try {
			long startTime = System.currentTimeMillis();
			LOG.debug(String.format("Periodic task execution -> START %s", startTime));
			ApplianceProxy[] applianceProxyArray = applianceProxyList.getApplianceProxyArray();
			for (int i = 0; i < applianceProxyArray.length; i++) {
				ApplianceProxy applianceProxy = applianceProxyArray[i];
				if (applianceProxy.getAppliance().isAvailable())
				checkServiceClusters(applianceProxy);
			}
		} catch (Exception e) {
			LOG.error("Error during periodic task execution", e);
		}
	}
	
	public M2MLocalService(){
		LOG.debug("HapProxy constructor");
	}
	
	public void addNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			LOG.error("addNetworkManager: eceived invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}

	public void removeNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			LOG.warn("removeNetworkManager: received invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}
	
	public void setHttpService(HttpService httpService) {
		LOG.debug("setHttpService");
		try {
			httpService.registerServlet(servletUri, this, null, null);
		} catch (ServletException e) {
			LOG.error("setHttpService", e);
		} catch (NamespaceException e) {
			LOG.error("setHttpService", e);
		}

	}

	public void unsetHttpService(HttpService httpService) {
		LOG.debug("unsetHttpService");
		httpService.unregister(servletUri);
	}
	
	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
		LOG.debug("Executor Service registered");
	}

	public void unsetExecutorService(ExecutorService executorService) {
		this.executorService = null;
		LOG.debug("Executor Service unregistered");
	}
	
	private void initResources(String sclId) {
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
			
			long now = System.currentTimeMillis();
			subscriptionInfos = new ArrayList<SubscriptionInfo>(1);
			subscriptionItemsAddressedId = scl.getSclBaseId() + M2MConstants.URL_SUBSCRIPTIONS;
			
			sb.append("<h2>Gateway Resources (HAG)</h2>");
	
			sb.append("<h3>Core resources</h3>");
			
			sb.append("<a href=").append(M2MConstants.URL_HAG_SCL_BASE).append(M2MConstants.URL_SCLS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append(">Gateway description</a><br>");
	
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(AHContainers.attrId_ah_zigbee_network_status);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">ZigBee network status (0=closed, 1=open)</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance type list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(NODES_EP_FILTER).append(CONFIG_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance configuration list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(NODES_EP_FILTER).append(M2MConstants.URL_SLASH).append(AHContainers.attrId_ah_core_appliance_events);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Appliance events list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Device type list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER).append(CONFIG_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Device configuration list</a><br>");
				
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER).append(STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Device status list</a><br>");
			
			sb.append("<h3>Application resources</h3>");	
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER).append(EH_STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">Energy@home device status list</a><br>");	
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER).append(AAL_STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">AAL device status list</a><br>");
			
			sb.append("<a href=").append(scl.getSclBaseId()).append(M2MConstants.URL_CONTAINERS);
			sb.append(APPLIANCE_FILTER).append(DEVICES_EP_FILTER).append(SH_STATUS_ATTR_FILTER);
			sb.append(M2MConstants.URL_CONTENT_INSTANCES).append(">SmartHome device status list</a><br>");
					
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
			
			queryString = "?startInstanceId=" + (time) + "&endInstanceId="+ (time + 259200000); 
			sb.append("<a href=").append(hapScl).append(M2MConstants.URL_CONTAINERS);
			sb.append(M2MConstants.URL_SLASH).append(M2MContainerAddress.ALL_ID_FILTER).append("/1/");
			sb.append(EHContainers.attrId_ah_eh_esp_hourlyReceivedEnergyForecast).append(M2MConstants.URL_CONTENT_INSTANCES);
			sb.append(queryString).append(">Received hourly energy data forecast</a><br>");
		}
		sb.append("</body></html>");
		indexPage = sb.toString();
	}
	
	public void setM2MHapService(IM2MHapService hapService) {
		LOG.debug("setM2MHapService");
		this.hapService = new AHM2MHapService(hapService);
		try {	
			onOffClusterProxy = new OnOffClusterProxy(applianceProxyList, this.hapService, this);
			meteringClusterProxy = new MeteringClusterProxy(applianceProxyList, this.hapService, this);
			iasClusterProxy = new IASZoneClusterProxy(applianceProxyList, this.hapService, this);
			temperatureMeasurementClusterProxy = new TemperatureMeasurementClusterProxy(applianceProxyList, this.hapService, this);
			relativeHumidityMeasurementClusterProxy = new RelativeHumidityMeasurementClusterProxy(applianceProxyList, this.hapService, this);
			illuminanceMeasurementClusterProxy = new IlluminanceMeasurementClusterProxy(applianceProxyList, this.hapService, this);
			applianceControlClusterProxy = new ApplianceControlClusterProxy(applianceProxyList, this.hapService, this);
			applianceEventsAndAlertsClusterProxy = new ApplianceEventsAndAlertsClusterProxy(applianceProxyList, this.hapService, this);
			serviceClusterProxies = new IServiceCluster[] {onOffClusterProxy, meteringClusterProxy, temperatureMeasurementClusterProxy, 
					iasClusterProxy, relativeHumidityMeasurementClusterProxy, illuminanceMeasurementClusterProxy, 
					applianceControlClusterProxy, applianceEventsAndAlertsClusterProxy};
		} catch (Exception e) {
			LOG.error("Error while creating appliance cluster proxies", e);
		}
		initResources(hapService.getLocalHagId());
	}

	public void unsetM2MHapService(IM2MHapService hapService) {
		LOG.debug("unsetM2MHapService");
		onOffClusterProxy = null;
		meteringClusterProxy = null;
		iasClusterProxy = null;
		temperatureMeasurementClusterProxy = null;
		relativeHumidityMeasurementClusterProxy = null;
		illuminanceMeasurementClusterProxy = null;
		applianceControlClusterProxy = null;
		applianceEventsAndAlertsClusterProxy = null;
		serviceClusterProxies = new IServiceCluster[] {};
		this.hapService = null;
		sclItems = null;
		scl = null;
	}
	
	public void addM2MLocalListener(IM2MLocalListener m2mLocalListener) {
		synchronized (m2mLocalListenerList) {
			removeM2MLocalListener(m2mLocalListener);
			m2mLocalListenerList.add(m2mLocalListener);
		}
	}
	
	public void removeM2MLocalListener(IM2MLocalListener m2mLocalListener) {
		synchronized (m2mLocalListenerList) {
			for (Iterator iterator = m2mLocalListenerList.iterator(); iterator.hasNext();) {
				IM2MLocalListener listener = (IM2MLocalListener) iterator.next();
				if (listener.equals(m2mLocalListener))
					iterator.remove();
			}
		}		
	}
	
	public void setAppliancesProxy(IAppliancesProxy appProxy) {
		LOG.debug("setAppliancesProxy");
		this.appliancesProxy = appProxy;
	}

	public void unsetAppliancesProxy(IAppliancesProxy appProxy) {
		LOG.debug("unsetAppliancesProxy");
		this.appliancesProxy = null;
	}

	public void setM2MNetworkScl(M2MNetworkScl networkScl) {
		LOG.debug("setM2MNetworkScl");
		this.networkScl = networkScl;
	}

	public void unsetM2MNetworkScl(M2MNetworkScl httpService) {
		LOG.debug("unsetM2MNetworkScl");
		this.networkScl = null;
	}

	public void start() {
		LOG.debug("start");
		xmlConverter = M2MXmlConverter.getCoreConverter();
		restClient = RestClient.get();
		executorService.scheduleTask(new Runnable() {
			public void run() {
				try {
					periodicTask();	
				} catch (Exception e) {
					LOG.error("ESP periodic task error", e);
				}
			}
		}, PERIODIC_TASK_TIMEOUT, PERIODIC_TASK_TIMEOUT);
		
	}

	public void stop() {
		LOG.debug("stop");
		restClient.release();
		applianceProxyList.clear();
		xmlConverter = null;
	}
	
	private ContentInstance postContentInstance(AHContainerAddress containerAddress, ContentInstance ci) throws ApplianceException, ServiceClusterException {
		String containerName = containerAddress.getContainerName();
		if (containerName == null)
			return null;
		String appliancePid = containerAddress.getAppliancePid();
		String endPointIdStr = containerAddress.getEndPointId();
		if (containerName.equals(AHContainers.attrId_ah_zigbee_network_status)) {
			Integer open = (Integer) ci.getContent();
			if (open.intValue() > 0) {
				ci = openZigBeeNetwork();
			} else {
				ci = closeZigBeeNetwork();
			}
		} else if (appliancePid != null && endPointIdStr != null) {
			Integer endPointId; 
			try {
				endPointId = new Integer(endPointIdStr);
			} catch (Exception e) {
				LOG.error("Error while parsing endPointId",e);
				return null;
			}
			if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_Name) ||
					containerName.equals(AHContainers.attrId_ah_core_config_name)) {
				String name = (String) ci.getContent();	
				try {
					IApplianceConfiguration config = getApplianceConfiguration(appliancePid, endPointId.intValue());
					if (!config.updateName(endPointId, name))
						ci = null;
				} catch (Exception e) {
					LOG.error("Error while trying to modify appliance name", e);
					ci = null;
				}
			} else if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_CategoryPid) ||
				containerName.equals(AHContainers.attrId_ah_core_config_category)) {
				Integer categoryPid = (Integer) ci.getContent();
				try {
					IApplianceConfiguration config = getApplianceConfiguration(appliancePid, endPointId.intValue());
					if (!config.updateCategoryPid(endPointId, categoryPid.toString()))
						ci = null;
				} catch (Exception e) {
					LOG.error("Error while trying to modify appliance category", e);
					ci = null;
				}
			} else if (containerName.equals(AHContainers.attrId_ah_cluster_ah_ConfigServer_LocationPid) ||
					containerName.equals(AHContainers.attrId_ah_core_config_location)) {
				Integer locationPid = (Integer) ci.getContent();
				try {
					IApplianceConfiguration config = getApplianceConfiguration(appliancePid, endPointId.intValue());
					if (!config.updateLocationPid(endPointId, locationPid.toString()))
						ci = null;
				} catch (Exception e) {
					LOG.error("Error while trying to modify appliance location", e);
					ci = null;
				}
			} else 	{
				if (containerName.startsWith(AHContainers.attrId_ah_cluster_onoff_prexif))
					ci = onOffClusterProxy.execCommand(appliancePid, endPointId, containerName, ci);
				else if (containerName.startsWith(AHContainers.attrId_ah_cluster_applctrl_prexif))
					ci = applianceControlClusterProxy.execCommand(appliancePid, endPointId, containerName, ci);
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
						else if (ServiceClusterProxy.isAnUnconfirmedCommand(itemContainerAddress))
							itemStatus.setBatchStatus(HttpServletResponse.SC_ACCEPTED);
						else
							itemStatus.setBatchStatus(HttpServletResponse.SC_OK);
					}
				} catch (Exception e) {
					LOG.error("Exception on postBatchRequest", e);
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
		return (hagId != null && hagId.equals(hapService.getLocalHagId()));	
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
		if (requestUri.endsWith(M2MConstants.URL_SLASH))
			requestUri = requestUri.substring(0, requestUri.length()-1);
		if (requestUri.startsWith(M2MConstants.URL_SCL_BASE)) {
			if (networkScl == null) {
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return;
			}
			requestUri = replaceFilters(requestUri, true);
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
				LOG.error("service: error while sending request to hap service", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			} 
		} else if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			requestUri = replaceFilters(requestUri, false);
			try {
				M2MXmlObject xmlObject = null;
				if (requestUri.startsWith(subscriptionItemsAddressedId)) {
					if (requestUri.endsWith(M2MContainerAddress.ALL_ID_FILTER)) {
						SubscriptionItems subscriptionItems = new SubscriptionItems();
						for (Iterator iterator = subscriptionInfos.iterator(); iterator
								.hasNext();) {
							SubscriptionInfo subscriptionInfo = (SubscriptionInfo) iterator.next();
							subscriptionItems.getSubscriptions().add(subscriptionInfo.subscription);
						}
						writeXmlObject(servletResponse, subscriptionItems);
					} else {
						String subscriptionId = requestUri.substring(subscriptionItemsAddressedId.length()+1, requestUri.length());
						SubscriptionInfo subscriptionInfo = null;
						for (Iterator iterator = subscriptionInfos.iterator(); iterator.hasNext();) {
							subscriptionInfo = (SubscriptionInfo) iterator.next();
							if (subscriptionInfo.subscription.getId().equals(subscriptionId)) {
								break;
							} else
								subscriptionInfo = null;
						}
						if (subscriptionInfo != null)
							writeXmlObject(servletResponse, subscriptionInfo.subscription);
						else 
							servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);					
					}
				} else if (requestUri.endsWith(sclsListUri)) {
					scl.setOnLineStatus(hapService.isConnected() ? SclStatusEnumeration.ONLINE : SclStatusEnumeration.DISCONNECTED);
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
							LOG.error("Error while parsing stratInstanceId parameter",e);
						}
					}
					long endInstanceId = M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID;
					String endInstanceIdStr = servletRequest.getParameter(END_INSTANCE_ID_REQUEST_PARAM);	
					if (endInstanceIdStr != null && endInstanceIdStr.length() > 0) {
						try {
							endInstanceId = Long.parseLong(endInstanceIdStr);							
						} catch (Exception e) {
							LOG.error("Error while parsing stratInstanceId parameter",e);
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
					// TODO:!!! manage uri with a specific timestamp used for content instance id
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
						String[] clusterAndAttributeNames = ServiceClusterProxy.getClusterAndAttributeNamesForNotCachedAttributes(containerAddress);
						if (clusterAndAttributeNames == null) {
							xmlObject = hapService.getLocalContentInstance(containerAddress);
						} else {
							// A request is sent to the device   
							ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(containerAddress.getAppliancePid());
							IServiceCluster serviceCluster = applianceProxy.getServiceCluster(new Integer(containerAddress.getEndPointId()).intValue(), clusterAndAttributeNames[0]);
							IAttributeValue av = serviceCluster.getAttributeValue(clusterAndAttributeNames[1], applianceProxy.getApplicationRequestContext());
							ContentInstance ci = new ContentInstance();
							ci.setId(av.getTimestamp());
							ci.setContent(av.getValue());
							xmlObject = ci;
						}
						if (xmlObject != null && contentInstanceId == null) {
							// If latest or oldest is specified a ContentInstance is returned, otherwise a ContentInstanceItems 
							// with a single item is built
							xmlObject = getSingleContentInstanceItems(requestUri, (ContentInstance)xmlObject, startInstanceId, endInstanceId);
						}
					} else {
						xmlObject= hapService.getLocalContentInstanceItemsList(containerAddress, startInstanceId, endInstanceId);
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
				LOG.error("service: error while parsing local request", e);
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
		requestUri = replaceFilters(requestUri, false);
		if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			try {
				if (requestUri.equals(subscriptionItemsAddressedId)) {
					long now = System.currentTimeMillis();
					Subscription subscription = (Subscription) readXmlObject(servletRequest);
					if (subscription.getId() != null || subscription.getContact() == null) {
						servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
					} else {
						for (Iterator iterator = subscriptionInfos.iterator(); iterator.hasNext();) {
							// Only one subscription for each contact uri is allowed (last subscription overwrite a previous existing one)
							if (subscription.getContact().equals(((SubscriptionInfo) iterator.next()).subscription.getContact())) {
								iterator.remove();
								break;
							}
						}
						subscription.setCreationTime(now);
						subscription.setLastModifiedTime(now);
						subscription.setId(UUID.randomUUID().toString());
						writeXmlObject(servletResponse, subscription);
						subscriptionInfos.add(new SubscriptionInfo(subscription));
					}

				} else if (requestUri.endsWith(M2MConstants.URL_CIS_BATCH_REQUEST)) {
					ContentInstancesBatchRequest batchRequest = (ContentInstancesBatchRequest) readXmlObject(servletRequest);
					ContentInstancesBatchResponse batchResponse = postBatchRequest(batchRequest);
					writeXmlObject(servletResponse, batchResponse);
				} else {
					try {
						AHContainerAddress containerAddress = new AHM2MContainerAddress(requestUri);
						if (!isValidLocalHagId(containerAddress.getHagId()))
							servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
						String containerName = containerAddress.getContainerName();
						if (containerName != null) {
							ContentInstance ci = (ContentInstance)readXmlObject(servletRequest);
							ci = postContentInstance(containerAddress, ci);
							if (ci == null)
								servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND); 	
							else if (ServiceClusterProxy.isAnUnconfirmedCommand(containerAddress))
								servletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
							else
								servletResponse.setStatus(HttpServletResponse.SC_OK);
							writeXmlObject(servletResponse, ci);					
						} else {
							servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
						}
					} catch (Exception e) {
						LOG.error("Exception on doPost", e);
						servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
					}	
				}
				commitApplianceConfigurationUpdates();
			}
			catch (Exception e) {
				LOG.error("service: error while parsing local request", e);
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
		requestUri = replaceFilters(requestUri, false);
		if (requestUri.startsWith(M2MConstants.URL_HAG_SCL_BASE)) {
			try {
				if (requestUri.startsWith(subscriptionItemsAddressedId)) {
						if (requestUri.endsWith(M2MContainerAddress.ALL_ID_FILTER))
							servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
						else {
							String subscriptionId = requestUri.substring(subscriptionItemsAddressedId.length()+1, requestUri.length());
							SubscriptionInfo subscriptionInfo = null;
							synchronized(subscriptionInfos) {
								for (Iterator iterator = subscriptionInfos.iterator(); iterator.hasNext();) {
									subscriptionInfo = (SubscriptionInfo) iterator.next();
									if (subscriptionInfo.subscription.getId().equals(subscriptionId)) {
										iterator.remove();
										break;
									} 
								}	
							}
						}
				} else {
					AHContainerAddress containerAddress = new AHM2MContainerAddress(requestUri);
					if (!isValidLocalHagId(containerAddress.getHagId()))
						servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					String appliancePid = containerAddress.getAppliancePid();
					if (containerAddress.getAppliancePid() != null && containerAddress.getEndPointId() == null && 
							containerAddress.getContainerName() == null) {
						ContentInstance ci = hapService.getLocalContentInstance(containerAddress); 
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
				}
			} catch (Exception e) {
				LOG.error("service: error while parsing local request", e);
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
				hapService.storeAttributeValue(appliancePid, null, null, timestamp, applianceType, true);
			} catch (HacException e) {
				LOG.error("applianceAndEndPointTypesUpdated: exception while storing appliance type", e);
			}
			IEndPoint[] endPoints = appliance.getEndPoints();
			IEndPoint endPoint = null;
			for (int i = 0; i < endPoints.length; i++) {
				try {
					 endPoint = endPoints[i];
					 hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), null, timestamp, endPoint.getType(), true);
				} catch (HacException e) {
					LOG.error("applianceAndEndPointTypesUpdated: exception while storing appliance type", e);
				}
			}
		} catch (Exception e) {
			LOG.error("applianceAndEndPointTypesUpdated", e);
		}
	}
	
	private void applianceStatusUpdated(ApplianceProxy applianceProxy, int appStatus, long timestamp) {
		try {
			IAppliance appliance = applianceProxy.getAppliance();
			String appliancePid = appliance.getPid();
			hapService.storeAttributeValue(appliancePid, IEndPoint.COMMON_END_POINT_ID, AHContainers.attrId_ah_core_appliance_events, timestamp, new Integer(appStatus), false);
		} catch (Exception e) {
			LOG.error("applianceStatusUpdated", e);
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
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), ConfigServer.ATTR_NAME_NAME, timestamp, configServer.getName(confirmationRequestContext), true);
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), AHContainers.attrId_ah_core_config_name, timestamp, configServer.getName(applianceProxy.getApplicationRequestContext()), true);
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), ConfigServer.ATTR_NAME_LOCATION_PID, timestamp, locationPid != null ? new Integer(locationPid) : null, true);
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), AHContainers.attrId_ah_core_config_location, timestamp, locationPid != null ? new Integer(locationPid) : null, true);					
//			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), ConfigServer.ATTR_NAME_CATEGORY_PID, timestamp, categoryPid != null ? new Integer(categoryPid) : null, true);	
			hapService.storeAttributeValue(appliancePid, new Integer(endPoint.getId()), AHContainers.attrId_ah_core_config_category, timestamp, categoryPid != null ? new Integer(categoryPid) : null, true);	

		} catch (Exception e) {
			LOG.error("endPointConfigurationUpdated", e);
		}
	}

	
	private void configurationUpdated(ApplianceProxy applianceProxy, long timestamp) {
		try {
			IAppliance appliance = applianceProxy.getAppliance();
			IEndPoint[] endPoints = appliance.getEndPoints();
			for (int i = 0; i < endPoints.length; i++) {
				endPointConfigurationUpdated(applianceProxy, endPoints[i], timestamp);
			}
		} catch (Exception e) {
			LOG.error("configurationUpdated", e);
		}	
	}	
	
	// IApplicationService Interface
	public IServiceCluster[] getServiceClusters() {
		return serviceClusterProxies;
	}

	public void notifyApplianceAdded(IApplicationEndPoint endPoint, IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			if (appliance.isSingleton()) {
				LOG.debug("applianceConnected - singleton appliance " + appliancePid);
				return;				
			}
			ApplianceProxy applianceProxy = new ApplianceProxy(endPoint, appliance);
			applianceProxyList.addApplianceProxy(applianceProxy);		
			boolean isAvailable = appliance.isAvailable();
			long timestamp = System.currentTimeMillis();
			applianceAndEndPointTypesUpdated(appliance, timestamp);
			applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_STARTED, timestamp);
			timestamp++;
			if (isAvailable) {			
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_AVAILABLE, timestamp);
				initServiceClusters(applianceProxy);
			} else {
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_UNAVAILABLE, timestamp);
			}
			configurationUpdated(applianceProxy, timestamp);

		} catch (Exception e) {
			LOG.error("notifyApplianceAdded error", e);
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
			LOG.error("notifyApplianceRemoved error", e);
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
			if (isAvailable) {
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_AVAILABLE, timestamp);
				initServiceClusters(applianceProxy);
			} else {
				applianceStatusUpdated(applianceProxy, AHContainers.APPLIANCE_EVENT_UNAVAILABLE, timestamp);
			}
		} catch (Exception e) {
			LOG.error("notifyApplianceAvailabilityUpdated error", e);
		}		
	}
	
	public void notifyInstallingApplianceAdded(IAppliance appliance) {		
		// Currently not used 
	}

	public void notifyInstallingApplianceRemoved(IAppliance appliance) {
		// Currently not used 
	}

	public void notifyInstallingApplianceAvailabilityUpdated(IAppliance appliance) {
		// Currently not used 
	}


	// Subscription manager interface
	
	public boolean checkActiveSubscriptions() {
		return subscriptionInfos.size() > 0 || m2mLocalListenerList.size() > 0;
	}

	public void notifyContentInstanceItems(ContentInstanceItems cisItems) {
		executorService.addNearRealTimeOrderedTask(new M2MLocalService.NotificationTask(cisItems));
	}

	// IM2MLocalService

	public String getLocalHagId() {
		return hapService.getLocalHagId();
	}

	public AHContainerAddress getContainerAddress(String containerName) throws M2MHapException {
		return hapService.getLocalContainerAddress(containerName);
	}

	public AHContainerAddress getContainerAddress(String appliancePid, String endPointId, String containerName)
			throws M2MHapException {
		return hapService.getLocalContainerAddress(appliancePid, endPointId, containerName);
	}

	public AHContainerAddress getContainerAddress(String appliancePid, Integer endPointId, String containerName)
			throws M2MHapException {
		return hapService.getLocalContainerAddress(appliancePid, endPointId, containerName);
	}

	public ContentInstance getLatestContentInstance(AHContainerAddress containerAddress) throws M2MHapException {
		return hapService.getLocalContentInstance(containerAddress);
	}

	public ContentInstanceItemsList getLatestContentInstanceItemsList(AHContainerAddress containerAddressFilter)
			throws M2MHapException {
		return hapService.getLocalContentInstanceItemsList(containerAddressFilter);
	}

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, long instanceId, Object content)
			throws M2MHapException {
		ContentInstance ci = new ContentInstance();
		ci.setId(new Long(instanceId));
		ci.setContent(content);
		return createContentInstance(containerAddress, ci);
	}

	public ContentInstance createContentInstance(AHContainerAddress containerAddress, ContentInstance contentInstance)
			throws M2MHapException {
		try {
			return postContentInstance(containerAddress, contentInstance);
		} catch (Exception e) {
			LOG.error("Exception in createContentInstance method", e);
			throw new M2MHapException(e.getMessage());
		} 
	}

	public ContentInstancesBatchResponse sendContentInstanceBatchRequest(ContentInstancesBatchRequest contentInstancesBatchRequest)
			throws M2MHapException {
		try {
			return postBatchRequest(contentInstancesBatchRequest);
		} catch (Exception e) {
			LOG.error("Exception in sendContentInstanceBatchRequest method", e);
			throw new M2MHapException(e.getMessage());
		} 
	}
}