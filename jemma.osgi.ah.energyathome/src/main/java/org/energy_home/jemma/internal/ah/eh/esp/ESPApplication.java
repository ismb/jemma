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
package org.energy_home.jemma.internal.ah.eh.esp;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.ebrain.EnergyBrainCore;
import org.energy_home.jemma.ah.ebrain.IMeteringProxy;
import org.energy_home.jemma.ah.ebrain.IOnOffListener;
import org.energy_home.jemma.ah.ebrain.IOverloadStatusListener;
import org.energy_home.jemma.ah.ebrain.PowerThresholds;
import org.energy_home.jemma.ah.ebrain.TwoTierDailyTariff;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPException;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.osgi.ah.eh.esp.IESPEventsDispatcher;
import org.energy_home.jemma.shal.DeviceConfiguration;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;
import org.energy_home.jemma.shal.DeviceDescriptor;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;
import org.energy_home.jemma.shal.DeviceDescriptor.NetworkType;
import org.energy_home.jemma.shal.DeviceInfo;
import org.energy_home.jemma.shal.min.DeviceConfigurationImpl;
import org.energy_home.jemma.shal.min.DeviceDescriptorImpl;
import org.energy_home.jemma.shal.min.DeviceInfoImpl;
import org.energy_home.jemma.utils.thread.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESPApplication extends HttpServlet implements IApplicationService, ESPService, IOnOffListener, IOverloadStatusListener {
	private static final Logger LOG = LoggerFactory.getLogger(ESPApplication.class);
	// TODO: check merge, different number in 3.3.0
	// private static final long PERIODIC_TASK_TIMEOUT = 15000;//30000
	private static final long PERIODIC_TASK_TIMEOUT = 30000;
	private static final String INVALID_APPLIANCE_PID_ERR_MSG = "Invalid appliance pid";
	private static final String EMULATED_START_TIME_STR = System.getProperty("org.energy_home.jemma.ah.test.hap.client.startTime");
	public static final boolean ENABLE_ATTRIBUTE_READ_BEFORE_SUBSCRIPTION = Utils.isNullOrEmpty(EMULATED_START_TIME_STR);

	// ESP appliance and end point properties
	// private static final String APPLIANCE_TYPE =
	// "ah.app.EnergyServicePortal";
	// public static final String APPLIANCE_FRIENDLY_NAME =
	// "Energy Service Portal";
	// public static final IApplianceDescriptor APPLIANCE_DESCRIPTOR = new
	// ApplianceDescriptor(APPLIANCE_TYPE, null,
	// APPLIANCE_FRIENDLY_NAME);
	// private static Hashtable APPLIANCE_INITIAL_CONFIG = new Hashtable(1);

	// Application constants
	public static final Integer DEFAULT_END_POINT_ID = new Integer(1);
	public static final int MAX_NUMBER_OF_SMART_PLUGS = 8;
	public static final int MAX_NUMBER_OF_WHITE_GOODS = 3;
	public static final int MAX_NUMBER_OF_APPLIANCES = MAX_NUMBER_OF_SMART_PLUGS + MAX_NUMBER_OF_WHITE_GOODS + 1;

	public static final float MAX_HOURLY_DELTA_ENERGY = 21000;

	public static final String APPLIANCE_ID_SEPARATOR = "-";

	public static final String OVERLOAD_RISK_IF_APPLIANCE_STARTS = "ah/eh/overload/OVERLOAD_RISK_IF_APPLIANCE_STARTS";
	public static final String NO_OVERLOAD_WARNING = "ah/eh/overload/NO_OVERLOAD";
	public static final String CONTRACTUAL_POWER_THRESHOLD_WARNING = "ah/eh/overload/CONTRACTUAL_WARNING";
	public static final String FIRST_POWER_THRESHOLD_WARNING = "ah/eh/overload/FIRST_WARNING";
	public static final String SECOND_POWER_THRESHOLD_WARNING = "ah/eh/overload/SECOND_WARNING";

	// static {
	// APPLIANCE_INITIAL_CONFIG.put(IAppliance.APPLIANCE_NAME_PROPERTY,
	// APPLIANCE_FRIENDLY_NAME);
	// }

	private static void mapESPException(String msg, Exception e) throws ESPException {
		LOG.error("Exception trapped: msg=" + msg, e);
		throw new ESPException(msg);
	}

	// Returns an array with two items: appliance pid and end point id
	public static String[] getDeviceIds(String applianceId) {
		String[] deviceIds = new String[2];
		if (applianceId.equals(AHContainerAddress.ALL_ID_FILTER)) {
			deviceIds[0] = AHContainerAddress.ALL_ID_FILTER;
			deviceIds[1] = AHContainerAddress.ALL_ID_FILTER;
		} else {
			StringTokenizer st = new StringTokenizer(applianceId, APPLIANCE_ID_SEPARATOR);
			int i = 0;
			while (st.hasMoreElements()) {
				deviceIds[i++] = (String) st.nextElement();
			}
			if (i == 1)
				deviceIds[1] = AHContainerAddress.DEFAULT_END_POINT_ID;
		}
		return deviceIds;
	}

	// Returns applianceId (= deviceId) from appliancePid and endPointId
	public static String getApplianceId(String appliancePid, int endPointId) {
		String result = appliancePid;
		if (endPointId != IEndPoint.DEFAULT_END_POINT_ID) {
			StringBuilder sb = new StringBuilder(appliancePid);
			sb.append(APPLIANCE_ID_SEPARATOR);
			sb.append(endPointId);
			result = sb.toString();
		}
		return result;
	}

	// Returns applianceId (= deviceId) from container address
	public static String getApplianceId(AHContainerAddress containerAddress) {
		String endPointId = containerAddress.getEndPointId();
		String result;
		if (!endPointId.equals(AHContainerAddress.DEFAULT_END_POINT_ID)) {
			StringBuilder sb = new StringBuilder(containerAddress.getAppliancePid());
			//sb.append(ESPApplication.APPLIANCE_ID_SEPARATOR);
			//sb.append(containerAddress.getEndPointId());
			result = sb.toString();
		} else {
			result = containerAddress.getAppliancePid();
		}
		return result;
	}

	private DeviceProxyList deviceProxyList = new DeviceProxyList();
	private EnergyBrainCore energyBrain = null;
	private OnOffClusterProxy onOffProxy = null;
	private MeteringClusterProxy meteringProxy = null;
	private PowerAndControlClusterProxy whiteGoodProxy = null;
	private IServiceCluster[] serviceClusters;
	long smartInfoLastCurrentSummation = (long) IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE; // 0xffffffffffffL;

	// TODO: still needs to be moved in energy brain
	private SimpleMeteringServerObject meteringServerObject = null;

	private ExecutorService executorService = null;

	private IESPEventsDispatcher eventsDispatcherService;
	private ESPHapServiceObject hapObject = null;
	private boolean useLocalCache;

	// *** Various private methods

	// Replaces null applianceId with smartinfo appliance id
	private String getSafeApplianceId(String applianceId) throws ESPException {
		String safePid = (applianceId == null) ? energyBrain.getSmartInfoExchangeId() : applianceId;
		if (safePid == null)
			throw new ESPException(INVALID_APPLIANCE_PID_ERR_MSG);
		return safePid;
	}

	public ESPApplication() throws ApplianceException {
		LOG.trace("ESP constructor called");
	}

	// *** Methods used by component service

	public void setESPEventsDispatcher(IESPEventsDispatcher espEventsDispatcher) {
		eventsDispatcherService = espEventsDispatcher;
		LOG.debug("ESP Events Dispatcher registered");
	}

	public void unsetESPEventsDispatcher(IESPEventsDispatcher espEventsDispatcher) {
		eventsDispatcherService = null;
		LOG.debug("ESP Events Dispatcher registered");
	}

	public void setHapService(IM2MHapService hapService) {
		hapObject = new ESPHapServiceObject(hapService);
		LOG.debug("Hap Service registered");
	}

	public void unsetHapService(IM2MHapService hapService) {
		hapObject = null;
		LOG.debug("Hap Service unregistered");
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
		LOG.debug("Executor Service registered");
	}

	public void unsetExecutorService(ExecutorService executorService) {
		this.executorService = null;
		LOG.debug("Executor Service unregistered");
	}

	private Boolean started = false;

	protected void periodicTask() {
		try {
			long startTime = System.currentTimeMillis();
			LOG.debug(String.format("Periodic task execution -> START %s", startTime));
				energyBrain.periodicTask();
			notifyOverloadStatusUpdate(currentOverloadStatus);
			LOG.debug(String.format("Periodic task execution -> END %s", System.currentTimeMillis()));
		} catch (Exception e) {
			LOG.error("Error during periodic task execution", e);
		}
	}

	protected void start() {
		synchronized (started) {
			LOG.debug("ESP startup method invoked");
			try {
				// Configuration initialization
				ESPConfiguration.loadProperties();
				ESPConfigParameters configParameters = ESPConfiguration.getConfigParameters();
				// Energy brain and proxy initialization
				energyBrain = EnergyBrainCore.getInstance();
				energyBrain.setCheckMeteringSubscriptions(ESPConfiguration.isCheckSubscriptionsEnabled());
				energyBrain.setCloudServiceProxy(hapObject);

				energyBrain.setOverloadStatusListener(this);

				try {
					energyBrain.setDailyTariff(TwoTierDailyTariff.class);
				} catch (InstantiationException e) {
					LOG.error("An instantiation error occurred while initializing energy brain daily tariff", e);
				} catch (IllegalAccessException e) {
					LOG.error("An illegal access error occurred while initializing energy brain daily tariff", e);
				}
				if (configParameters != null) {
					energyBrain.setPowerThresholds(new PowerThresholds(configParameters.getContractualPowerThreshold()));
					energyBrain.setPeakProducedPower(configParameters.getPeakProducedPower());
				} else {
					energyBrain.setPowerThresholds(new PowerThresholds(ESPConfigParameters.DEFAULT_CONTRACTUAL_POWER_THRESHOLD));
					energyBrain.setPeakProducedPower(ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER);
				}
				// energyBrain.setTariffTrailingDigits(EnergyBrain.TRAILING_DIGIT_CENTS);
				useLocalCache = ESPConfiguration.getUseLocalCache();
				if (configParameters != null && configParameters.getPeakProducedPower() > 0) {
					// TODO: in case of generated power no cache is managed by
					// the gateway
					useLocalCache = false;
				}
				hapObject.resetCache(useLocalCache);

				try {
					whiteGoodProxy = new PowerAndControlClusterProxy(deviceProxyList, energyBrain);
				} catch (Exception e) {
					LOG.error("Some probelm occurred while creating power and control cluster proxy", e);
				}
				energyBrain.setPowerProfileProxy(whiteGoodProxy);
				meteringProxy = new MeteringClusterProxy(deviceProxyList, energyBrain);
				energyBrain.setMeteringProxy(meteringProxy);

				meteringServerObject = new SimpleMeteringServerObject(energyBrain);

				onOffProxy = new OnOffClusterProxy(deviceProxyList, this);
				energyBrain.setOnOffProxy(onOffProxy);

				if (ESPConfiguration.isPowerProfileClusterEnabled())
					serviceClusters = new IServiceCluster[] { (IServiceCluster) meteringProxy, (IServiceCluster) onOffProxy, (IServiceCluster) whiteGoodProxy.asPowerProfileClient(), (IServiceCluster) whiteGoodProxy.asApplianceControlClient(), (IServiceCluster) whiteGoodProxy.asApplianceStatisticsClient(), (IServiceCluster) meteringServerObject };
				else
					serviceClusters = new IServiceCluster[] { (IServiceCluster) meteringProxy, (IServiceCluster) onOffProxy, (IServiceCluster) whiteGoodProxy.asApplianceControlClient(), (IServiceCluster) whiteGoodProxy.asApplianceStatisticsClient(), (IServiceCluster) meteringServerObject };

				executorService.scheduleTask(new Runnable() {
					public void run() {
						try {
							periodicTask();
						} catch (Exception e) {
							LOG.error("ESP periodic task error", e);
						}
					}
				}, PERIODIC_TASK_TIMEOUT, PERIODIC_TASK_TIMEOUT);
			} catch (Exception e) {
				LOG.error("Some problem occurred during startup", e);
			}
			LOG.debug("ESP startup method exited");
			started = true;
		}
	}

	protected void stop() {
		synchronized (started) {
			started = false;
			deviceProxyList.clear();
			energyBrain.setCloudServiceProxy(null);
			LOG.debug("ESP shutdown method invoked");
		}
	}

	protected DeviceType getDeviceType(IEndPoint ep) {
		String epType = ep.getType();
		DeviceType deviceType = null;
		if (epType.equals(IEndPointTypes.ZIGBEE_METERING_DEVICE))
			deviceType = DeviceType.Meter;
		else if (epType.equals(IEndPointTypes.ZIGBEE_LOAD_CONTROL_DEVICE))
			deviceType = DeviceType.Other;
		else if (epType.equals(IEndPointTypes.ZIGBEE_WHITE_GOODS))
			deviceType = DeviceType.WhiteGood;
		else if (epType.equals(IEndPointTypes.ZIGBEE_DIMMABLE_LIGHT))
			deviceType = DeviceType.Other;
		else if (epType.equals(IEndPointTypes.ZIGBEE_DRIMMER_SWITCH))
			deviceType = DeviceType.Other;
		else if (epType.equals(IEndPointTypes.ZIGBEE_DOOR_LOCK))
			deviceType = DeviceType.DOOR_LOCK;
		else if (epType.equals(IEndPointTypes.ZIGBEE_WINDOW_COVERING))
			deviceType = DeviceType.WINDOW_COVERING;
		else if (epType.equals(IEndPointTypes.ZIGBEE_WINDOW_COVERING_CONTROLLER))
			deviceType = DeviceType.Other;
		else if (epType.equals(IEndPointTypes.ZIGBEE_SMART_PLUG))
			deviceType = DeviceType.SmartPlug;
		else {
			LOG.warn("ESP unmanaged appliance type " + epType);
			return null;
		}
		return deviceType;
	}

	// **** IOverloadStatusListener interface methods
	private OverloadStatus currentOverloadStatus = OverloadStatus.NoOverloadWarning;

	public void notifyOverloadStatusUpdate(OverloadStatus status) {
		LOG.info("Sending Overload Status Update message: {}",status);
		currentOverloadStatus = status;
		try {
			switch (status) {
				case NoOverloadWarning:
					eventsDispatcherService.postEvent(NO_OVERLOAD_WARNING, null);
					break;
				case OverLoadRiskIfApplianceStarts:
					eventsDispatcherService.postEvent(OVERLOAD_RISK_IF_APPLIANCE_STARTS, null);
					break;
				case ContractualPowerThresholdWarning:
					eventsDispatcherService.postEvent(CONTRACTUAL_POWER_THRESHOLD_WARNING, null);
					break;
				case FirstPowerThresholdWarning:
					eventsDispatcherService.postEvent(FIRST_POWER_THRESHOLD_WARNING, null);
					break;
				case SecondPowerThresholdWarning:
					eventsDispatcherService.postEvent(SECOND_POWER_THRESHOLD_WARNING, null);
					break;
	
				default:
					break;
			}
		} catch (Exception e) {
			LOG.error("notifyStatusUpdate exception", e);
		}
	}

	// **** IApplicationService service interface methods

	public IServiceCluster[] getServiceClusters() {
		return serviceClusters;
	}

	public void notifyApplianceAdded(IApplicationEndPoint applicationEndPoint, IAppliance appliance) {
		try {
			if (appliance.isSingleton())
				return;
			String appliancePid = appliance.getPid();
			LOG.debug("notifyApplianceAdded " + appliancePid);
			IEndPoint[] eps = appliance.getEndPoints();
			IEndPoint ep = null;
			
			if (eps.length <= 1) {
				LOG.warn("notifyApplianceAdded error: invalid end point list");
				return;
			}
			
			for (int i = 1; i < eps.length; i++) {
				ep = eps[i];
				DeviceType deviceType = getDeviceType(ep);
				if (deviceType == null)
					return;
				// TODO: device service interfaces still needs to be integrated
				DeviceDescriptor descriptor = new DeviceDescriptorImpl(deviceType, NetworkType.ZigBee, null);
				ConfigServer configServer = (ConfigServer) appliance.getEndPoint(IEndPoint.COMMON_END_POINT_ID).getServiceCluster(ConfigServer.class.getName());
				DeviceConfiguration config = null;
				IEndPointRequestContext context = applicationEndPoint.getDefaultRequestContext();
				String nickname = configServer.getName(context);
				String categoryPidString = configServer.getCategoryPid(context);
				Integer categoryPid = categoryPidString == null ? null : new Integer(categoryPidString);
				// TODO: location are not yet initialized
				// TODO: check for enum order in the xml file
				config = new DeviceConfigurationImpl(nickname, (categoryPid == null || (DeviceCategory.values().length < categoryPid.intValue())) ? null : DeviceCategory.values()[categoryPid.intValue() - 1], null);
				String applianceId = getApplianceId(appliancePid, ep.getId());
				DeviceInfo info = new DeviceInfoImpl(applianceId, applianceId, descriptor, config);
				DeviceProxy applianceProxy = new DeviceProxy(applicationEndPoint, ep, info);
				deviceProxyList.addDeviceProxy(applianceProxy);
				energyBrain.notifyDeviceAdded(info);
				hapObject.applianceConnected(applianceId, energyBrain.getAccumulatedEnergyCost(applianceId), ep.isAvailable());
				if (ep.isAvailable()) {
					energyBrain.notifyDeviceServiceAvailable(info, null);
				}
			}
		} catch (Exception e) {
			LOG.error("notifyApplianceAdded error", e);
		}
	}

	public void notifyApplianceRemoved(IAppliance appliance) {
		try {
			if (appliance.isSingleton())
				return;
			String appliancePid = appliance.getPid();
			LOG.debug("notifyApplianceRemoved " + appliancePid);
			IEndPoint[] eps = appliance.getEndPoints();
			if (eps.length <= 1) {
				LOG.warn("notifyApplianceAdded error: invalid end point list");
				return;
			}
			for (int i = 1; i < eps.length; i++) {
				IEndPoint ep = eps[i];
				String applianceId = getApplianceId(appliancePid, ep.getId());
				DeviceProxy applianceProxy = deviceProxyList.getDeviceProxy(applianceId);
				if (applianceProxy == null)
					return;
				try {
					energyBrain.notifyDeviceRemoved(applianceProxy.getDeviceInfo());
					hapObject.applianceDisconnected(applianceId);
				} catch (Exception e) {
					LOG.error("Exception on notifyApplianceRemoved", e);
				}
				deviceProxyList.removeDeviceProxy(applianceId);
			}
		} catch (Exception e) {
			LOG.error("notifyApplianceRemoved error", e);
		}
	}

	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		try {
			if (appliance.isSingleton())
				return;
			String appliancePid = appliance.getPid();
			LOG.debug("notifyApplianceAvailabilityUpdated " + appliancePid);
			IEndPoint[] eps = appliance.getEndPoints();
			if (eps.length <= 1) {
				LOG.warn("notifyApplianceAdded error: invalid end point list");
				return;
			}
			for (int i = 1; i < eps.length; i++) {
				IEndPoint ep = eps[i];
				String applianceId = getApplianceId(appliancePid, ep.getId());
				DeviceProxy applianceProxy = deviceProxyList.getDeviceProxy(applianceId);
				if (applianceProxy == null)
					return;
				if (ep.isAvailable())
					energyBrain.notifyDeviceServiceAvailable(applianceProxy.getDeviceInfo(), null);
				else
					energyBrain.notifyDeviceServiceUnavailable(applianceProxy.getDeviceInfo());
				hapObject.applianceAvailabilityUpdated(applianceId, ep.isAvailable());
			}
		} catch (Exception e) {
			LOG.error("notifyApplianceAvailabilityUpdated error", e);
		}
	}

	// *** ESP service interface

	public Long getInitialConfigurationTime() {
		LOG.debug("ESPService - getInitialConfigurationTime()");
		Long initialTime = ESPConfiguration.getInitialConfigurationTime();
		LOG.debug("ESPService - getCurrentConfiguration returned " + initialTime);
		return initialTime;
	}

	public ESPConfigParameters getCurrentConfiguration() {
		LOG.debug("ESPService - getCurrentConfiguration()");
		ESPConfigParameters config = ESPConfiguration.getConfigParameters();
		LOG.debug("ESPService - getCurrentConfiguration returned " + config);
		return config;
	}

	public void setConfiguration(ESPConfigParameters config) throws ESPException {
		LOG.debug("ESPService - setConfiguration (config=" + config + ")");
		ESPConfiguration.setConfigParameters(config);
		if (config != null) {
			energyBrain.setPowerThresholds(new PowerThresholds(config.getContractualPowerThreshold()));
			energyBrain.setPeakProducedPower(config.getPeakProducedPower());
		} else {
			energyBrain.setPowerThresholds(new PowerThresholds(ESPConfigParameters.DEFAULT_CONTRACTUAL_POWER_THRESHOLD));
			energyBrain.setPeakProducedPower(ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER);
		}
		LOG.debug("ESPService - setConfiguration returned");
	}

	public void sendGuiLog(String msg) throws ESPException {
		try {
			long timestamp = System.currentTimeMillis();
			LOG.debug("ESPService - sendGuiLog (msg=" + msg + ", timestamp=" + timestamp + ")");
			hapObject.storeGuiLog(timestamp, msg);
			LOG.debug("ESPService - sendGuiLog returned");
		} catch (Exception e) {
			mapESPException("ESPService - Problem while storing log message", e);
		}
	}

	public float getTotalInstantaneousPowerFloatValue() throws ESPException {
		// Calculates Max(last smart info value, sum of last smart plugs values)
		// LOG.info("ESPService - getTotalInstantaneousPowerFloatValue()");
		float result = energyBrain.getTotalIstantaneousDemandPower();
		// LOG.info("ESPService - getTotalInstantaneousPowerFloatValue returned: "
		// + result);
		return result;
	}

	public float getInstantaneousProducedPowerFloatValue() throws ESPException {
		// LOG.info("ESPService - getInstantaneousProducedPowerFloatValue();
		float result = energyBrain.getIstantaneousProducedPower();
		// LOG.info("ESPService - getInstantaneousProducedPowerFloatValue() returned: "
		// + result);
		return result;
	}

	public float getInstantaneousSoldPowerFloatValue() throws ESPException {
		// LOG.info("ESPService - getInstantaneousSoldPowerFloatValue()");
		float result = energyBrain.getIstantaneousSoldPower();
		// LOG.info("ESPService - getInstantaneousSoldPowerFloatValue() returned: "
		// + result);
		return result;
	}

	public float getInstantaneousPowerFloatValue(String applianceId) throws ESPException {
		try {
			// LOG.info("ESPService - getInstantaneousPowerFloatValue(pid=" +
			// applianceId + ")");
			float result;
			String safeApplianceId = getSafeApplianceId(applianceId);
			result = energyBrain.getIstantaneousDemandPower(safeApplianceId);
			// LOG.info("ESPService - getInstantaneousPowerFloatValue returned: "
			// + result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ESPException("ESPService - Invalid appliance pid");
			
		}

	}

	public List<Float> getEnergyConsumption(String applianceId, long startTime, long endTime, int resolution) throws ESPException {
		LOG.debug("ESPService - getEnergyConsumption(pid=" + applianceId + ", startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
		String safeApplianceId = getSafeApplianceId(applianceId);
		try {
			List<Float> result = hapObject.getEnergyConsumption(safeApplianceId, startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getEnergyConsumption failed while getting data from hap service", e);
			return null;
		}
	}

	public List<Float> getProducedEnergy(long startTime, long endTime, int resolution) throws ESPException {
		LOG.debug("ESPService - getGeneratedEnergy(startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
		String applianceId = energyBrain.getSmartInfoProductionId();
		if (applianceId == null) {
			LOG.warn("ESPService - getEnergyConsumption called with no production smart info configured");
			return null;
		}
		try {
			List<Float> result = hapObject.getReceivedEnergy(applianceId, startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getGeneratedEnergy failed while getting data from hap service", e);
			return null;
		}
	}

	public List<Float> getSoldEnergy(long startTime, long endTime, int resolution) throws ESPException {
		LOG.debug("ESPService - getSoldEnergy(startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
		String applianceId = energyBrain.getSmartInfoExchangeId();
		if (applianceId == null) {
			LOG.warn("ESPService - getEnergyConsumption called with no exchange smart info configured");
			return null;
		}
		try {
			List<Float> result = hapObject.getReceivedEnergy(applianceId, startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getGeneratedEnergy failed while getting data from hap service", e);
			return null;
		}
	}

	public Map<String, List<Float>> getEnergyConsumption(long startTime, long endTime, int resolution) throws ESPException {
		try {
			LOG.debug("ESPService - getEnergyConsumption(startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
			Map<String, List<Float>> result = hapObject.getEnergyConsumption(startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getEnergyCost(String applianceId, long startTime, long endTime, int resolution) throws ESPException {
		LOG.debug("ESPService - getEnergyCost(pid=" + applianceId + ", startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
		String safeApplianceId = getSafeApplianceId(applianceId);
		try {
			List<Float> result = hapObject.getEnergyCost(safeApplianceId, startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyCost hap service returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getEnergyCost failed while getting data from hap service", e);
			return null;
		}
	}

	public Map<String, List<Float>> getEnergyCost(long startTime, long endTime, int resolution) throws ESPException {
		try {
			LOG.debug("ESPService - getEnergyCost(startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution + ")");
			Map<String, List<Float>> result = hapObject.getEnergyCost(startTime, endTime, resolution);
			LOG.debug("ESPService - getEnergyCost returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public Float getEnergyConsumptionForecast(String applianceId, int resolution) throws ESPException {
		String safeApplianceId = getSafeApplianceId(applianceId);
		try {
			LOG.debug("ESPService - getEnergyConsumptionForecast(pid=" + applianceId + ", resolution=" + resolution + ")");
			Float result = hapObject.getEnergyConsumptionForecast(safeApplianceId, resolution);
			LOG.debug("ESPService - getEnergyConsumptionForecast returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public Float getEnergyCostForecast(String applianceId, int resolution) throws ESPException {
		String safeApplianceId = getSafeApplianceId(applianceId);
		try {
			LOG.debug("ESPService - getEnergyCostForecast(pid=" + applianceId + ", resolution=" + resolution + ")");
			Float result = hapObject.getEnergyCostForecast(safeApplianceId, resolution);
			LOG.debug("ESPService - getEnergyCostForecast returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getWeekDayEnergyConsumpionAverage(String applianceApplianceId, int weekDay) throws ESPException {
		String safeApplianceId = getSafeApplianceId(applianceApplianceId);
		try {
			LOG.debug("ESPService - getWeekDayEnergyConsumpionAverage(pid=" + applianceApplianceId + ", weekDay=" + weekDay + ")");
			List<Float> result = hapObject.getWeekDayEnergyConsumpionAverage(safeApplianceId, weekDay);
			LOG.debug("ESPService - getWeekDayEnergyConsumpionAverage returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getWeekDayEnergyCostAverage(String applianceId, int weekDay) throws ESPException {
		String safeApplianceId = getSafeApplianceId(applianceId);
		try {
			LOG.debug("ESPService - getWeekDayEnergyCostAverage(pid=" + applianceId + ", weekDay=" + weekDay + ")");
			List<Float> result = hapObject.getWeekDayEnergyCostAverage(safeApplianceId, weekDay);
			LOG.debug("ESPService - getWeekDayEnergyCostAverage returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public void notifyStatus(String applianceId, long time, Boolean status) {
		try {
			LOG.debug("Received onoff status " + status + " from " + applianceId);
			// hapObject.storeOnOffStatus(applianceId, time,
			// status.booleanValue());
		} catch (Exception e) {
			LOG.error("Error while storing onoff status on HAP platform", e);
		}
	}

}
