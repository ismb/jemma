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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPException;
import org.energy_home.jemma.utils.datetime.DateUtils;

public class ESPConfiguration {
	private static final Log log = LogFactory.getLog(ESPConfiguration.class);
	
	private static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
	private static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
	private static final String ESP_CONFIG_FILENAME = "org.energy_home.jemma.ah.eh.esp.properties";

	private static final String SP_SYSTEM_PROPERTY_PREFIX = "org.energy_home.jemma.ah.eh.esp.";	
	
	private static final String CHECK_SUBSCRIPTIONS_ENABLED = "checkSubscriptionsEnabled";
	private static final String POWER_PROFILE_CLUSTER_ENABLED = "powerProfileClusterEnabled";
	private static final String REMOTE_HOST_ADDR = "remoteHostAddr";
	private static final String REMOTE_HOST_PORT = "remoteHostPort";
	private static final String INITIAL_TIME_PROPERTY_NAME = "initialTime";
	private static final String USE_LOCAL_CACHE_PROPERTY_NAME = "useLocalCache";
	private static final String CONTRACTUAL_POWER_THRESHOLD_PROPERTY_NAME = "contractualPowerThreshold";
	private static final String PEAK_PRODUCED_POWER_PROPERTY_NAME = "peakProducedPower";
	private static final String SMART_INFO_PID_PROPERTY_NAME = "smartInfoPid";

	private static File f = null;
	private static Properties configProperties = null;

	public static synchronized void loadProperties() throws ESPException {
		String instanceArea = null;
		URL url = null;

		instanceArea = System.getProperty(OSGI_INSTANCE_AREA);
		if (instanceArea == null) {
			instanceArea = System.getProperty(OSGI_CONFIGURATION_AREA);
			if (instanceArea == null) {
				throw new ESPException("Unable to get an area where to store preferences");
			}
		}
		try {
			url = new URL(instanceArea + ESP_CONFIG_FILENAME);
			configProperties = new Properties();
			f = new File(url.getFile());
			if (f.exists()) {
				configProperties.load(new FileInputStream(f));
			}
		} catch (Exception e) {
			log.error("", e);
			throw new ESPException("Problems while loading ESP configuration parameters");
		}		
	}

	private static synchronized void saveProperties() throws ESPException {
		if (!f.exists()) {
			if (!DateUtils.isDateTimeOk())
				throw new ESPException("Invalid local time during config file creation");
			try {
				f.createNewFile();
				configProperties.setProperty(INITIAL_TIME_PROPERTY_NAME, Long.toString(System.currentTimeMillis()));
			} catch (Exception e) {
				log.error("", e);
				configProperties.setProperty(INITIAL_TIME_PROPERTY_NAME, null);
				throw new ESPException("Config file creation failed: " + f.getAbsolutePath());
			}
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(f);
			configProperties.store(fos, "ESP Service configuration parameters");
		} catch (Exception e) {
			throw new ESPException("Config file save failed: " + f.getAbsolutePath());
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}

	public static Long getInitialConfigurationTime() {
		Long result = null;
		try {
			String strResult = configProperties.getProperty(INITIAL_TIME_PROPERTY_NAME);
			if (strResult != null)
				result = Long.valueOf(strResult);
		} catch (Exception e) {
			log.error("", e);
		}
		return result;
	}
	
	public static boolean getUseLocalCache() {
		boolean result = true;
		try {
			String strResult = configProperties.getProperty(USE_LOCAL_CACHE_PROPERTY_NAME);
			if (strResult != null)
				result = Boolean.parseBoolean(strResult);
		} catch (Exception e) {
			log.error("", e);
		}
		return result;
	}
	
	public static String getSmartInfoPid() {
		return configProperties.getProperty(SMART_INFO_PID_PROPERTY_NAME);
	}	
	
	public static boolean isCheckSubscriptionsEnabled() {
		boolean boolResult = true;
		String result = configProperties.getProperty(CHECK_SUBSCRIPTIONS_ENABLED);
		if (Utils.isNullOrEmpty(result))
			result = System.getProperty(SP_SYSTEM_PROPERTY_PREFIX + CHECK_SUBSCRIPTIONS_ENABLED);
		if (!Utils.isNullOrEmpty(result))
			try {
				boolResult = Boolean.parseBoolean(result);
			} catch (Exception e) {
			}
		return boolResult;
	}

	
	public static boolean isPowerProfileClusterEnabled() {
		boolean boolResult = true;
		String result = configProperties.getProperty(POWER_PROFILE_CLUSTER_ENABLED);
		if (Utils.isNullOrEmpty(result))
			result = System.getProperty(SP_SYSTEM_PROPERTY_PREFIX + POWER_PROFILE_CLUSTER_ENABLED);
		if (!Utils.isNullOrEmpty(result))
			try {
				boolResult = Boolean.parseBoolean(result);
			} catch (Exception e) {
			}
		return boolResult;
	}
	
	public static String getRemoteHostAddr() {
		String result = configProperties.getProperty(REMOTE_HOST_ADDR);
		if (Utils.isNullOrEmpty(result))
			result = System.getProperty(SP_SYSTEM_PROPERTY_PREFIX + REMOTE_HOST_ADDR);
		if (Utils.isNullOrEmpty(result))
			result = "10.38.0.1";	
		return result;
	}
	
	public static int getRemoteHostPort() {
		int intResult = 80;
		String result = configProperties.getProperty(REMOTE_HOST_PORT);
		if (Utils.isNullOrEmpty(result))
			result = System.getProperty(SP_SYSTEM_PROPERTY_PREFIX + REMOTE_HOST_PORT);
		if (!Utils.isNullOrEmpty(result))
			try {
				intResult = Integer.parseInt(result);
			} catch (Exception e) {
			}
		return intResult;
	}

	public static ESPConfigParameters getConfigParameters() {
		ESPConfigParameters configParameters = null;
		if (configProperties.getProperty(INITIAL_TIME_PROPERTY_NAME) != null) {
			try {
				String contractualPowerThreasholdProperty = configProperties.getProperty(CONTRACTUAL_POWER_THRESHOLD_PROPERTY_NAME);
				float contractualPowerThreashold = Float.parseFloat(contractualPowerThreasholdProperty);
				String peakProducedPowerProperty = configProperties.getProperty(PEAK_PRODUCED_POWER_PROPERTY_NAME);
				float peakProducedPower = (peakProducedPowerProperty == null || peakProducedPowerProperty.length() == 0) ? 
						0 : Float.parseFloat(peakProducedPowerProperty);;
				configParameters = new ESPConfigParameters(contractualPowerThreashold, peakProducedPower);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		return configParameters;
	}

	public static void setConfigParameters(ESPConfigParameters configParameters) throws ESPException {
		if (configParameters == null) {
			if (f.exists())
				f.delete();
			configProperties = new Properties();
		} else {
			configProperties.setProperty(CONTRACTUAL_POWER_THRESHOLD_PROPERTY_NAME,
					Float.toString(configParameters.getContractualPowerThreshold()));
			configProperties.setProperty(PEAK_PRODUCED_POWER_PROPERTY_NAME,
					Float.toString(configParameters.getPeakProducedPower()));
			saveProperties();
		}
	}

}
