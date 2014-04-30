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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.utils.encrypt.TripleDESEnc;

public class M2MDeviceConfigObject implements M2MDeviceConfig {
	private static final Log log = LogFactory.getLog(M2MDeviceConfigObject.class);
	private static final String SP_SYSTEM_PROPERTY_PREFIX = "org.energy_home.jemma.m2m.device.";
	private static final String SP_CONNECTION_CONFIG_DIR = SP_SYSTEM_PROPERTY_PREFIX + "configDir";
	private static final String CONFIG_FILE_NAME = SP_SYSTEM_PROPERTY_PREFIX + "config.properties";
	private static final String CONNECTION_ID_PREFIX = "cid-";
	private static final String NETWORK_SCL_ID_PREFIX = "hag-";

	private static final String SERVER_ADDRESS_PROPERTY_KEY = "serverAddress";
	private static final String SERVER_PORT_PROPERTY_KEY = "serverPort";
	private static final String DEVICE_ID_PROPERTY_KEY = "deviceId";
	private static final String DEVICE_TOKEN_PROPERTY_KEY = "deviceToken";
	private static final String CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY = "connectionRetryTimeout";

	private static String configFilePath = null;
	private static Properties DEFAULT_CONFIG_PROPERTIES = new Properties();
	private static Properties configProperties = new Properties();
	private static TripleDESEnc enc = null;

	static {
		DEFAULT_CONFIG_PROPERTIES.setProperty(SERVER_ADDRESS_PROPERTY_KEY, "10.38.0.1");
		DEFAULT_CONFIG_PROPERTIES.setProperty(SERVER_PORT_PROPERTY_KEY, "8080");
		DEFAULT_CONFIG_PROPERTIES.setProperty(CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY, "60000");
		DEFAULT_CONFIG_PROPERTIES.setProperty(DEVICE_ID_PROPERTY_KEY, UUID.randomUUID().toString());

		String encKey = null;
		try {
			//TODO: check merge, class org.energy_home.jemma.internal.ah.m2m.device.EHSettings not in JEMMA!
			Class ehSettingsClass = Class.forName("org.energy_home.jemma.internal.ah.m2m.device.EHSettings");
			Field field = ehSettingsClass.getDeclaredField("key");
			field.setAccessible(true);
			encKey = (String) field.get(null);
		} catch (Exception e) {}
		try {
			if (!isNullOrEmpty(encKey))
				enc = TripleDESEnc.getInstance(encKey);
		} catch (Exception e) {
			log.error("Problem while initializing encryption", e);
		}
		
		String configDir = System.getProperty(SP_CONNECTION_CONFIG_DIR);
		if (configDir != null) {
			if (isNullOrEmpty(configDir))
				configFilePath = CONFIG_FILE_NAME;
			else
				configFilePath = configDir + "/" + CONFIG_FILE_NAME;
		} else
			configFilePath = null;
		loadConfigProperties();
	}
	
	public static boolean checkUserId(String userId) {
		if (userId == null || userId.length() == 0)
			return false;
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(userId);
		return !matcher.find();
	}
	
	private static String initConfigProperty(String key) {
		String value = configProperties.getProperty(key);
		if (isNullOrEmpty(value)) {
			value = System.getProperty(SP_SYSTEM_PROPERTY_PREFIX + key);
			if (isNullOrEmpty(value)) {
				value = DEFAULT_CONFIG_PROPERTIES.getProperty(key);
			}
			if (value != null)
				configProperties.setProperty(key, value);
		}
		return value;
	}

	private static final void loadConfigProperties() {
		configProperties = new Properties();
		if (configFilePath != null) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(configFilePath);
				configProperties.load(in);
			} catch (Exception e) {
				log.warn("Configuration file does not exists - " + configFilePath, e);
				initConfigProperty(DEVICE_TOKEN_PROPERTY_KEY);
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {
						log.error("", e);
					}
			}
		} else {
			initConfigProperty(DEVICE_ID_PROPERTY_KEY);
			initConfigProperty(DEVICE_TOKEN_PROPERTY_KEY);
		}
		initConfigProperty(SERVER_ADDRESS_PROPERTY_KEY);
		initConfigProperty(SERVER_PORT_PROPERTY_KEY);
		initConfigProperty(CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY);
	}

	private static synchronized boolean deleteFileAndResetConfigProperties() {
		boolean result = true;
		configProperties = new Properties();
		if (configFilePath != null) {
			try {
				File f = new File(configFilePath);
				if (f.exists()) {
					f.delete();
				}
			} catch (Exception e) {
				log.error("", e);
				result = false;
			}
		} else {
			initConfigProperty(DEVICE_ID_PROPERTY_KEY);
			initConfigProperty(DEVICE_TOKEN_PROPERTY_KEY);
		}
		initConfigProperty(SERVER_ADDRESS_PROPERTY_KEY);
		initConfigProperty(SERVER_PORT_PROPERTY_KEY);
		initConfigProperty(CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY);
		return result;
	}

	public static synchronized M2MDeviceConfigObject updateConfigProperties(M2MDeviceConfigObject config) {
		if (config == null) {
			deleteFileAndResetConfigProperties();
		} else {
			configProperties = config.getProperties();
			if (configFilePath != null) {
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(configFilePath);
					configProperties.store(out, "M2M Device configuration paramaters");
				} catch (Exception e) {
					log.error("", e);
					if (out != null)
						try {
							out.close();
						} catch (IOException e1) {
							log.error("", e1);
						}
				}
			}
		}
		return new M2MDeviceConfigObject();
	}

	private String serverAddress;
	private int serverPort;

	private String baseUri;
	private String connectionBaseUri = null;
	private String networkSclBaseUri = null;

	private String deviceId;

	private String deviceToken;
	private String networkSclBaseToken;

	private long connectionRetryTimeout;

	private void updateBaseUri() {
		this.baseUri = M2MConstants.URL_HTTP_PREFIX + serverAddress + M2MConstants.URL_PORT_PREFIX + serverPort;
	}
	
	public M2MDeviceConfigObject() {
		serverAddress = configProperties.getProperty(SERVER_ADDRESS_PROPERTY_KEY);
		serverPort = Integer.parseInt(configProperties.getProperty(SERVER_PORT_PROPERTY_KEY));
		deviceId = configProperties.getProperty(DEVICE_ID_PROPERTY_KEY);
		deviceToken = configProperties.getProperty(DEVICE_TOKEN_PROPERTY_KEY);			
		connectionRetryTimeout = Long.parseLong(configProperties.getProperty(CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY));
		updateBaseUri();
	}

	public String getConnectionBaseUri() {
		return connectionBaseUri == null ? baseUri + M2MConstants.URL_CONNECTION_BASE : connectionBaseUri;
	}

	public void setConnectionBaseUri(String connectionBaseUri) {
		this.connectionBaseUri = connectionBaseUri;
	}

	public String getNetworkSclBaseUri() {
		return networkSclBaseUri == null ? baseUri + M2MConstants.URL_SCL_BASE : networkSclBaseUri;
	}

	public void setNetworkSclBaseUri(String networkSclBaseUri) {
		this.networkSclBaseUri = networkSclBaseUri;
	}

	public String getConnectionId() {
		return deviceId != null ? CONNECTION_ID_PREFIX + deviceId : null;
	}
	
	public String getSclId() {
		return deviceId != null ? NETWORK_SCL_ID_PREFIX + deviceId : null;
	}

	public String getConnectionToken() {
		if (!isNullOrEmpty(deviceToken))
			return deviceToken;
		String cid = getConnectionId();
		if (cid == null || enc == null)
			return null;	
		try {
			return enc.hexEncrypt(cid);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}		
	}

	public String getNetworkSclBaseToken() {
		return networkSclBaseToken;
	}

	public void setNetworkSclBaseToken(String networkSclBaseToken) {
		this.networkSclBaseToken = networkSclBaseToken;
	}

	// *** M2MDeviceConfig interface

	public boolean isLocalOnly() {
		return deviceId.equals(M2MConstants.LOCAL_ONLY_DEVICE_ID);
	}
	
	public boolean isValid() {
		return deviceId != null && !deviceId.equals("");
	}

	public Properties getProperties() {
		Properties p = new Properties();
		p.setProperty(SERVER_ADDRESS_PROPERTY_KEY, serverAddress);
		p.setProperty(SERVER_PORT_PROPERTY_KEY, Integer.toString(serverPort));
		p.setProperty(DEVICE_ID_PROPERTY_KEY, deviceId == null ? "" : deviceId);
		p.setProperty(DEVICE_TOKEN_PROPERTY_KEY, (isNullOrEmpty(deviceToken)) ? "" : deviceToken); 
		p.setProperty(CONNECTION_RETRY_TIMEOUT_PROPERTY_KEY, Long.toString(connectionRetryTimeout));
		return p;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
		updateBaseUri();
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
		updateBaseUri();
	}

	public int getServerPort() {
		return serverPort;
	}

	public String getDeviceId() {
		if (deviceId == null || deviceId.equals(""))
			return null;
		return deviceId;
	}

	public void setDeviceId(String userId) throws IllegalArgumentException {
		if (!checkUserId(userId))
			throw new IllegalArgumentException("Device id cannot contain any white space char");
		this.deviceId = userId;
	}

	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}

	public long getConnectionRetryTimeout() {
		return connectionRetryTimeout;
	}

	public void setConnectionRetryTimeout(long connectionRetryTimeout) {
		this.connectionRetryTimeout = connectionRetryTimeout;
	}
	
	private static final boolean isNullOrEmpty(String s) {
		return (s == null || (s.length() == 0));
	}
}
