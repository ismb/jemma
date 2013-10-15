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
package org.energy_home.jemma.internal.ah.eh.esp;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.IServiceCluster;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.ebrain.EnergyBrainCore;
import org.energy_home.jemma.ah.ebrain.IMeteringProxy;
import org.energy_home.jemma.ah.ebrain.TwoTierDailyTariff;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPException;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.shal.DeviceConfiguration;
import org.energy_home.jemma.shal.DeviceDescriptor;
import org.energy_home.jemma.shal.DeviceInfo;
import org.energy_home.jemma.shal.DeviceConfiguration.DeviceCategory;
import org.energy_home.jemma.shal.DeviceDescriptor.DeviceType;
import org.energy_home.jemma.shal.DeviceDescriptor.NetworkType;
import org.energy_home.jemma.shal.internal.DeviceConfigurationImpl;
import org.energy_home.jemma.shal.internal.DeviceDescriptorImpl;
import org.energy_home.jemma.shal.internal.DeviceInfoImpl;
import org.energy_home.jemma.utils.thread.ExecutorService;
import org.osgi.service.http.HttpService;

public class ESPApplication extends HttpServlet implements IApplicationService, ESPService {
	private static final Log log = LogFactory.getLog(ESPApplication.class);
	
	private static final long PERIODIC_TASK_TIMEOUT = 60000;
	private static final String INVALID_APPLIANCE_PID_ERR_MSG = "Invalid appliance pid";
	private static final String EMULATED_START_TIME_STR = System.getProperty("it.telecomitalia.ah.test.hap.client.startTime");
	public static final boolean ENABLE_ATTRIBUTE_READ_BEFORE_SUBSCRIPTION = Utils.isNullOrEmpty(EMULATED_START_TIME_STR);

	// ESP appliance and end point properties
//	private static final String APPLIANCE_TYPE = "ah.app.EnergyServicePortal";
//	public static final String APPLIANCE_FRIENDLY_NAME = "Energy Service Portal";
//	public static final IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
//			APPLIANCE_FRIENDLY_NAME);
	private static Hashtable APPLIANCE_INITIAL_CONFIG = new Hashtable(1);
	public static final float FIRST_POWER_THRESHOLD_FACTOR = 1.1f;

	// Application constants
	public static final Integer DEFAULT_END_POINT_ID = new Integer(1);
	public static final int MAX_NUMBER_OF_SMART_PLUGS = 8;
	public static final int MAX_NUMBER_OF_WHITE_GOODS = 3;
	public static final int MAX_NUMBER_OF_APPLIANCES = MAX_NUMBER_OF_SMART_PLUGS + MAX_NUMBER_OF_WHITE_GOODS + 1;

	public static final float MAX_HOURLY_DELTA_ENERGY = 21000;
	
//	static {
//		APPLIANCE_INITIAL_CONFIG.put(IAppliance.APPLIANCE_NAME_PROPERTY, APPLIANCE_FRIENDLY_NAME);
//	}

	private static void mapESPException(String msg, Exception e) throws ESPException {
		log.error(msg + e.getClass().getName());
		if (log.isDebugEnabled())
			log.error("", e);
		throw new ESPException(msg);
	}
	
	private HttpService httpService;
	
	private ApplianceProxyList applianceProxyList = new ApplianceProxyList();
	private EnergyBrainCore energyBrain = null;
	private OnOffClusterProxy onOffProxy = null;
	private MeteringClusterProxy meteringProxy = null;
	private PowerAndControlClusterProxy whiteGoodProxy = null;
	long smartInfoLastCurrentSummation = (long)IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE; // 0xffffffffffffL;

	// TODO: still needs to be moved in energy brain
	private SimpleMeteringServerObject meteringServerObject = null;	
	
	private ExecutorService executorService = null;

	private ESPHapServiceObject hapObject = null;
	private boolean useLocalCache;

	// *** Various private methods

	private String getSafeAppliancePid(String appliancePid) throws ESPException {
		String safePid = (appliancePid == null) ? energyBrain.getSmartInfoExchangeId() : appliancePid;
		if (safePid == null)
			throw new ESPException(INVALID_APPLIANCE_PID_ERR_MSG);
		return safePid;
	}
	
	public ESPApplication() throws ApplianceException {
		log.info("ESP constructor called");
	}

	// *** Methods used by component service
	
	public void setHapService(IM2MHapService hapService) {
		hapObject = new ESPHapServiceObject(hapService);
		log.info("Hap Service registered");
	}

	public void unsetHapService(IM2MHapService hapService) {
		hapObject = null;
		log.info("Hap Service unregistered");
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
		log.info("Executor Service registered");
	}

	public void unsetExecutorService(ExecutorService executorService) {
		this.executorService = null;
		log.info("Executor Service unregistered");
	}

	private Boolean started = false;
	
	// Hack used to limit periodic requests for users without microgeneration plants
	private long lastPeriodicRequestTime = 0;
	private static long MIN_PERIODIC_REQUEST_TIME_INTERVAL = 60 * 5 * 1000; // 5 minutes
	
	protected void periodicTask() {
		try {
			long startTime = System.currentTimeMillis();
			log.info(String.format("Periodic task execution -> START %s", startTime));
			// Hack used to limit periodic requests for users without microgeneration plants
/*			if (ESPConfiguration.getConfigParameters().getPeakProducedPower() == 0 &&
					(startTime - lastPeriodicRequestTime < MIN_PERIODIC_REQUEST_TIME_INTERVAL)) {
				log.info(String.format("Periodic task execution -> FILTERED"));		
			}*/
			lastPeriodicRequestTime = System.currentTimeMillis();		
			energyBrain.periodicTask();
			log.info(String.format("Periodic task execution -> END %s", System.currentTimeMillis()));
		} catch (Exception e) {
			log.error("Error during periodic task execution", e);
		}
	}
	
	protected void start() {
		synchronized (started) {
			log.info("ESP startup method invoked");
			try {
				// Configuration initialization
				ESPConfiguration.loadProperties();
				ESPConfigParameters configParameters = ESPConfiguration.getConfigParameters();
				// Energy brain and proxy initialization
				energyBrain = EnergyBrainCore.getInstance();
				energyBrain.setCheckMeteringSubscriptions(ESPConfiguration.isCheckSubscriptionsEnabled());
				energyBrain.setCloudServiceProxy(hapObject);
				try {
					energyBrain.setDailyTariff(TwoTierDailyTariff.class);
				} catch (InstantiationException e) {
					log.error("An instantiation error occurred while initializing energy brain daily tariff", e);
				} catch (IllegalAccessException e) {
					log.error("An illegal access error occurred while initializing energy brain daily tariff", e);
				}
				if (configParameters != null) {
					energyBrain.setUpperPowerThreshold(configParameters.getContractualPowerThreshold() * FIRST_POWER_THRESHOLD_FACTOR);			
					energyBrain.setPeakProducedPower(configParameters.getPeakProducedPower());
				} else {
					energyBrain.setUpperPowerThreshold(ESPConfigParameters.DEFAULT_CONTRACTUAL_POWER_THRESHOLD
							* FIRST_POWER_THRESHOLD_FACTOR);
					energyBrain.setPeakProducedPower(ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER);
				}
				//energyBrain.setTariffTrailingDigits(EnergyBrain.TRAILING_DIGIT_CENTS);
				useLocalCache = ESPConfiguration.getUseLocalCache();
				if (configParameters != null && configParameters.getPeakProducedPower() > 0) {	
					// TODO: in case of generated power no cache is managed by the gateway
					useLocalCache = false;
				}
				hapObject.resetCache(useLocalCache);
			
				try {
					whiteGoodProxy = new PowerAndControlClusterProxy(applianceProxyList, energyBrain);
				} catch (Exception e) {
					log.error("Some probelm occurred while creating power and control cluster proxy", e);
				}
				energyBrain.setPowerProfileProxy(whiteGoodProxy);
				meteringProxy = new MeteringClusterProxy(applianceProxyList, energyBrain);
				energyBrain.setMeteringProxy(meteringProxy);	
				onOffProxy = new OnOffClusterProxy(applianceProxyList, energyBrain);
				energyBrain.setOnOffProxy(onOffProxy);
				
				meteringServerObject = new SimpleMeteringServerObject(energyBrain);			
				
				executorService.scheduleTask(new Runnable() {
					public void run() {
						try {
							periodicTask();	
						} catch (Exception e) {
							log.error("ESP periodic task error", e);
						}
					}
				}, PERIODIC_TASK_TIMEOUT, PERIODIC_TASK_TIMEOUT);
			} catch (Exception e) {
				log.error("Some problem occurred during startup", e);
			}
			log.info("ESP startup method exited");
			started = true;
		}
	}

	protected void stop() {
		synchronized (started) {
			started = false;
			applianceProxyList.clear();
			energyBrain.setCloudServiceProxy(null);
			log.info("ESP shutdown method invoked");
		}
	}
	
	protected DeviceType getDeviceType(IEndPoint ep) {
		String epType = ep.getType();
		DeviceType deviceType = null;
		if (epType.equals(IEndPointTypes.ZIGBEE_METERING_DEVICE)) {
			deviceType = DeviceType.Meter;
		}
		else if (epType.equals(IEndPointTypes.ZIGBEE_LOAD_CONTROL_DEVICE))
			deviceType = DeviceType.SmartPlug;
		else if (epType.equals(IEndPointTypes.ZIGBEE_WHITE_GOODS))
			deviceType = DeviceType.WhiteGood;
		else {
			log.info("ESP unmanaged appliance type " + epType);
			return null;
		}
		return deviceType;
	}
	
	// **** IApplicationService service interface methods
	
	public IServiceCluster[] getServiceClusters() {
		return new IServiceCluster[] { 
				(IServiceCluster) meteringProxy,
				(IServiceCluster) onOffProxy,
				(IServiceCluster) whiteGoodProxy.asPowerProfileClient(),
				(IServiceCluster) whiteGoodProxy.asApplianceControlClient(),
				(IServiceCluster) whiteGoodProxy.asApplianceStatisticsClient(),
				(IServiceCluster) meteringServerObject
				};
	}
	
	public void notifyApplianceAdded(IApplicationEndPoint applicationEndPoint, IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			log.info("notifyApplianceAdded " + appliancePid);
			IEndPoint ep = appliance.getEndPoint(IEndPoint.DEFAULT_END_POINT_ID);
			if (ep == null) {
				log.error("notifyApplianceAdded error: null default end point id");
				return;
			}
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
			config = new DeviceConfigurationImpl(nickname, categoryPid == null ? null : DeviceCategory.values()[categoryPid.intValue()-1], null);
			
			DeviceInfo info = new DeviceInfoImpl(appliancePid, appliancePid, descriptor, config);
			ApplianceProxy applianceProxy = new ApplianceProxy(applicationEndPoint, appliance, info);
			applianceProxyList.addApplianceProxy(applianceProxy);
			
			energyBrain.notifyDeviceAdded(info);
			hapObject.applianceConnected(appliancePid, energyBrain.getAccumulatedEnergyCost(appliancePid), ep.isAvailable());	
			if (ep.isAvailable()) {
				energyBrain.notifyDeviceServiceAvailable(info, null);
			}
		} catch (Exception e) {
			log.error("notifyApplianceAdded error", e);
		}	
	}

	public void notifyApplianceRemoved(IAppliance appliance) {
		try {
			if (appliance.isSingleton())
				return;
			String appliancePid = appliance.getPid();
			log.info("notifyApplianceRemoved " + appliancePid);
			ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliance.getPid());
			if (applianceProxy == null)
				return;
			try {
				energyBrain.notifyDeviceRemoved(applianceProxy.getDeviceInfo());
				hapObject.applianceDisconnected(appliancePid);				
			} catch (Exception e) {
				log.error(e);
			}
			applianceProxyList.removeApplianceProxy(appliancePid);
		} catch (Exception e) {
			log.error("notifyApplianceRemoved error", e);
		}
	}

	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		try {
			String appliancePid = appliance.getPid();
			log.info("notifyApplianceAvailabilityUpdated " + appliancePid);
			ApplianceProxy applianceProxy = applianceProxyList.getApplianceProxy(appliancePid);
			if (applianceProxy == null)
				return;
			IEndPoint ep = appliance.getEndPoint(IEndPoint.DEFAULT_END_POINT_ID);
			if (ep == null) {
				log.error("notifyApplianceAvailabilityUpdated error: null default end point id");
				return;
			}			
			if (ep.isAvailable())
				energyBrain.notifyDeviceServiceAvailable(applianceProxy.getDeviceInfo(), null);
			else
				energyBrain.notifyDeviceServiceUnavailable(applianceProxy.getDeviceInfo());
			hapObject.applianceAvailabilityUpdated(appliancePid, ep.isAvailable());	
		} catch (Exception e) {
			log.error("notifyApplianceAvailabilityUpdated error", e);
		}
	}

	// *** ESP service interface

	public Long getInitialConfigurationTime() {
		log.info("ESPService - getInitialConfigurationTime()");
		Long initialTime = ESPConfiguration.getInitialConfigurationTime();
		log.info("ESPService - getCurrentConfiguration returned " + initialTime);
		return initialTime;
	}

	public ESPConfigParameters getCurrentConfiguration() {
		log.info("ESPService - getCurrentConfiguration()");
		ESPConfigParameters config = ESPConfiguration.getConfigParameters();
		log.info("ESPService - getCurrentConfiguration returned " + config);
		return config;
	}

	public void setConfiguration(ESPConfigParameters config) throws ESPException {
		log.info("ESPService - setConfiguration (config=" + config + ")");
		ESPConfiguration.setConfigParameters(config);
		if (config != null) {
			energyBrain.setUpperPowerThreshold(config.getContractualPowerThreshold() * FIRST_POWER_THRESHOLD_FACTOR);
			energyBrain.setPeakProducedPower(config.getPeakProducedPower());
		} else {
			energyBrain.setUpperPowerThreshold(ESPConfigParameters.DEFAULT_CONTRACTUAL_POWER_THRESHOLD
					* FIRST_POWER_THRESHOLD_FACTOR);
			energyBrain.setPeakProducedPower(ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER);
		}
		log.info("ESPService - setConfiguration returned");
	}

	public void sendGuiLog(String msg) throws ESPException {
		try {
			long timestamp = System.currentTimeMillis();
			log.info("ESPService - sendGuiLog (msg=" + msg + ", timestamp=" + timestamp + ")");
			hapObject.storeGuiLog(timestamp, msg);
			log.info("ESPService - sendGuiLog returned");
		} catch (Exception e) {
			mapESPException("ESPService - Problem while storing log message", e);
		}
	}

	public float getTotalInstantaneousPowerFloatValue() throws ESPException {
		// Calculates Max(last smart info value, sum of last smart plugs values)
		// log.info("ESPService - getTotalInstantaneousPowerFloatValue()");
		float result = energyBrain.getTotalIstantaneousDemandPower();
		// log.info("ESPService - getTotalInstantaneousPowerFloatValue returned: "
		// + result);
		return result;
	}

	public float getInstantaneousProducedPowerFloatValue() throws ESPException {
		// log.info("ESPService - getInstantaneousGeneratedPowerFloatValue(pid=" +
		// appliancePid + ")");
		float result = energyBrain.getIstantaneousProducedPower();
		// log.info("ESPService - getInstantaneousGeneratedPowerFloatValue returned: "
		// + result);
		return result;	
	}

	public float getInstantaneousSoldPowerFloatValue() throws ESPException {
		// log.info("ESPService - getInstantaneousSoldPowerFloatValue(pid=" +
		// appliancePid + ")");
		float result = energyBrain.getIstantaneousSoldPower();
		// log.info("ESPService - getInstantaneousSoldPowerFloatValue returned: "
		// + result);
		return result;
	}

	public float getInstantaneousPowerFloatValue(String appliancePid) throws ESPException {
		try {
			// log.info("ESPService - getInstantaneousPowerFloatValue(pid=" +
			// appliancePid + ")");
			float result;
			String safePid = getSafeAppliancePid(appliancePid);
			result = energyBrain.getIstantaneousDemandPower(safePid);
			// log.info("ESPService - getInstantaneousPowerFloatValue returned: "
			// + result);
			return result;
		} catch (Exception e) {
			throw new ESPException("ESPService - Invalid appliance pid");
		}

	}
	
	public List<Float> getEnergyConsumption(String appliancePid, long startTime, long endTime, int resolution) throws ESPException {
		log.info("ESPService - getEnergyConsumption(pid=" + appliancePid + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", resolution=" + resolution + ")");
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			List<Float> result = hapObject.getEnergyConsumption(safePid, startTime, endTime, resolution);
			log.info("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getEnergyConsumption failed while getting data from hap service", e);
			return null;
		}
	}
	
	public List<Float> getProducedEnergy(long startTime, long endTime, int resolution) throws ESPException {
		log.info("ESPService - getGeneratedEnergy(startTime=" + startTime + ", endTime=" + endTime
				+ ", resolution=" + resolution + ")");
		String pid = energyBrain.getSmartInfoProductionId();
		if (pid == null) {
			log.warn("ESPService - getEnergyConsumption called with no production smart info configured");
			return null;
		}
		try {
			List<Float> result = hapObject.getReceivedEnergy(pid, startTime, endTime, resolution);
			log.info("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getGeneratedEnergy failed while getting data from hap service", e);
			return null;
		}
	}

	public List<Float> getSoldEnergy(long startTime, long endTime, int resolution) throws ESPException {
		log.info("ESPService - getSoldEnergy(startTime=" + startTime + ", endTime=" + endTime
				+ ", resolution=" + resolution + ")");
		String pid = energyBrain.getSmartInfoExchangeId();
		if (pid == null) {
			log.warn("ESPService - getEnergyConsumption called with no exchange smart info configured");
			return null;
		}
		try {
			List<Float> result = hapObject.getReceivedEnergy(pid, startTime, endTime, resolution);
			log.info("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getGeneratedEnergy failed while getting data from hap service", e);
			return null;
		}
	}

	public Map<String, List<Float>> getEnergyConsumption(long startTime, long endTime, int resolution) throws ESPException {
		try {
			log.info("ESPService - getEnergyConsumption(startTime=" + startTime + ", endTime=" + endTime + ", resolution="
					+ resolution + ")");
			Map<String, List<Float>> result = hapObject.getEnergyConsumption(startTime, endTime, resolution);
			log.info("ESPService - getEnergyConsumption returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getEnergyCost(String appliancePid, long startTime, long endTime, int resolution) throws ESPException {
		log.info("ESPService - getEnergyCost(pid=" + appliancePid + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", resolution=" + resolution + ")");
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			List<Float> result = hapObject.getEnergyCost(safePid, startTime, endTime, resolution);
			log.info("ESPService - getEnergyCost hap service returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - getEnergyCost failed while getting data from hap service", e);
			return null;
		}
	}

	public Map<String, List<Float>> getEnergyCost(long startTime, long endTime, int resolution) throws ESPException {
		getSafeAppliancePid(null);
		try {
			log.info("ESPService - getEnergyCost(startTime=" + startTime + ", endTime=" + endTime + ", resolution=" + resolution
					+ ")");
			Map<String, List<Float>> result = hapObject.getEnergyCost(startTime, endTime, resolution);
			log.info("ESPService - getEnergyCost returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public Float getEnergyConsumptionForecast(String appliancePid, int resolution) throws ESPException {
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			log.info("ESPService - getEnergyConsumptionForecast(pid=" + appliancePid + ", resolution=" + resolution + ")");
			Float result = hapObject.getEnergyConsumptionForecast(safePid, resolution);
			log.info("ESPService - getEnergyConsumptionForecast returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public Float getEnergyCostForecast(String appliancePid, int resolution) throws ESPException {
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			log.info("ESPService - getEnergyCostForecast(pid=" + appliancePid + ", resolution=" + resolution + ")");
			Float result = hapObject.getEnergyCostForecast(safePid, resolution);
			log.info("ESPService - getEnergyCostForecast returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getWeekDayEnergyConsumpionAverage(String appliancePid, int weekDay) throws ESPException {
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			log.info("ESPService - getWeekDayEnergyConsumpionAverage(pid=" + appliancePid + ", weekDay=" + weekDay + ")");
			List<Float> result = hapObject.getWeekDayEnergyConsumpionAverage(safePid, weekDay);
			log.info("ESPService - getWeekDayEnergyConsumpionAverage returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

	public List<Float> getWeekDayEnergyCostAverage(String appliancePid, int weekDay) throws ESPException {
		String safePid = getSafeAppliancePid(appliancePid);
		try {
			log.info("ESPService - getWeekDayEnergyCostAverage(pid=" + appliancePid + ", weekDay=" + weekDay + ")");
			List<Float> result = hapObject.getWeekDayEnergyCostAverage(safePid, weekDay);
			log.info("ESPService - getWeekDayEnergyCostAverage returned: " + result);
			return result;
		} catch (Exception e) {
			mapESPException("ESPService - Problem while getting energy consumption data from hap service", e);
			return null;
		}
	}

}
