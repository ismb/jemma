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
package org.energy_home.jemma.javagal.layers;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;

import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;
import org.energy_home.jemma.javagal.layers.object.GatewayProperties;
import org.energy_home.jemma.zgd.jaxb.KeyType;
import org.energy_home.jemma.zgd.jaxb.LogicalType;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties manager class. Loads/saves from/to a ".properties" file the
 * desired values for JavaGal execution. It's THE way to control a number of
 * parameters at startup.
 * 
 * 
* @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 
 */
//FIXME Note by Riccardo: I'm deprecating this class: we should switch to ManagedService/ConfigAdmin service instead of this: it's more standard
@Deprecated
public class PropertiesManager {

	private static final Logger LOG = LoggerFactory.getLogger( PropertiesManager.class );
	/**
	 * Local StartupAttributeInfo reference.
	 */
	private StartupAttributeInfo sai = null;
	/**
	 * Available properties list.
	 */
	public Properties props;

	/**
	 * Creates a new instance with a specified URL. The URL must specify a
	 * formally correct .properties file.
	 * 
	 * @param _url
	 *            the URL pointing to a .properties file.
	 */
	public PropertiesManager(URL _url) {
		LOG.debug("PropertiesManager - Costructor - Loading configuration file...");
		InputStream in = null;
		try {
			in = _url.openStream();
		} catch (IOException e1) {
			LOG.error("Error opening stream",e1);
		} catch (Exception e1) {
			LOG.error("Error opening stream",e1);
		}
		try {
			props = new Properties();
			props.load(in);
		} catch (IOException e) {
			LOG.error("Error loading properties from inputstream",e);
		}
		LOG.debug("PropertiesManager - Costructor - Configuration file loaded!");
	}

	/**
	 * Gets DebugEnabled property.
	 * 
	 * @return the DebugEnabled value.
	 */
/*	public boolean getDebugEnabled() {
		String _value = props.getProperty("debugEnabled");
		
		return (_value.equalsIgnoreCase("0")) ? false : true;

	}*/
	
	
	

	/**
	 * Gets timeoutForWaitThread.
	 * 
	 * @return the timeoutForThread value.
	 */
	public int getTimeOutForWaitThread() {
		String _value = props.getProperty("TimeOutForWaitThread");
		return Integer.parseInt(_value);
	}
	
	
	
	/**
	 * Gets serialDataDebugEnabled property.
	 * 
	 * @return the serialDataDebugEnabled value.
	 */
	public boolean getserialDataDebugEnabled() {
		String _value = props.getProperty("serialDataDebugEnabled");
		
		return (_value.equalsIgnoreCase("0")) ? false : true;

	}
	
	
	
	
	

	/**
	 * Gets NumberOfThreadForAnyPool property.
	 * 
	 * @return the NumberOfThreadForAnyPool value.
	 */
	public int getNumberOfThreadForAnyPool() {
		String _value = props.getProperty("NumberOfThreadForAnyPool");
		return Integer.parseInt(_value);

	}

	/**
	 * Gets KeepAliveThread property.
	 * 
	 * @return the KeepAliveThread value.
	 */
	public int getKeepAliveThread() {
		String _value = props.getProperty("KeepAliveThread");
		return Integer.parseInt(_value);

	}
	
	
	public Boolean getzgdDump() {
        	String value = props.getProperty("dump");
        	return ((value == null) ? false : (!value.equalsIgnoreCase("0")));
    	}

    	public String getDirDump() {
        	return props.getProperty("dumpDir");
    	}
	

	/**
	 * Sets DebugEnabled property's value.
	 * 
	 * @param _debug
	 *            the value to set
	 */
	public void setDebugEnabled(Boolean _debug) {
		props.setProperty("debugEnabled", _debug.toString());

	}
	
	/**
	 * Sets DebugEnabled property's value.
	 * 
	 * @param _debug
	 *            the value to set
	 */
	public void setserialDataDebugEnabled(Boolean _debug) {
		props.setProperty("serialDataDebugEnabled", _debug.toString());

	}
	
	

	/**
	 * Gets KeepAliveNumberOfAttempt property used in Discovery operation.
	 * 
	 * @return the KeepAliveNumberOfAttempt value.
	 */
	public int getKeepAliveNumberOfAttempt() {
		String _value = props.getProperty("keepAliveNumberOfAttempt");
		return Integer.parseInt(_value);

	}

	/**
	 * Gets KeepAliveThreshold property.
	 * 
	 * @return the KeepAliveThreshold value.
	 */
	public int getKeepAliveThreshold() {
		String _value = props.getProperty("keepAliveThreshold");
		return Integer.parseInt(_value);

	}
	
	/**
	 * Gets CommandTimeout property.
	 * 
	 * @return the CommandTimeout value.
	 */
	public long getCommandTimeoutMS() {
		String _value = props.getProperty("CommandTimeoutMS");
		return Long.parseLong(_value);

	}
	
	
	/**
	 * Gets TimeForcePingErrorSeconds property.
	 * 
	 * @return the TimeForcePingErrorSeconds value.
	 */
	public int getTimeForcePingErrorSeconds() {
		String _value = props.getProperty("TimeForcePingErrorSeconds");
		return Integer.parseInt(_value);

	}
	
	
	/**
	 * Gets TimeFreshnessErrorSeconds property.
	 * 
	 * @return the TimeFreshnessErrorSeconds value.
	 */
	public int getTimeFreshnessErrorSeconds() {
		String _value = props.getProperty("TimeFreshnessErrorSeconds");
		return Integer.parseInt(_value);

	}
	
	
	/**
	 * Gets TimeDiscoveryErrorSeconds property.
	 * 
	 * @return the TimeDiscoveryErrorSeconds value.
	 */
	public int getTimeDiscoveryErrorSeconds() {
		String _value = props.getProperty("TimeDiscoveryErrorSeconds");
		return Integer.parseInt(_value);

	}
	
	/**
	 * Gets TimeForcePingNewNodeSeconds property.
	 * 
	 * @return the TimeForcePingNewNodeSeconds value.
	 */
	public int getTimeForcePingNewNodeSeconds() {
		String _value = props.getProperty("TimeForcePingNewNodeSeconds");
		return Integer.parseInt(_value);

	}
	
	
	/**
	 * Gets TimeDiscoveryNewNodeSeconds property.
	 * 
	 * @return the TimeDiscoveryNewNodeSeconds value.
	 */
	public int getTimeDiscoveryNewNodeSeconds() {
		String _value = props.getProperty("TimeDiscoveryNewNodeSeconds");
		return Integer.parseInt(_value);

	}
	
	
	/**
	 * Gets TimeFreshnessNewNodeSeconds property.
	 * 
	 * @return the TimeFreshnessNewNodeSeconds value.
	 */
	public int getTimeFreshnessNewNodeSeconds() {
		String _value = props.getProperty("TimeFreshnessNewNodeSeconds");
		return Integer.parseInt(_value);

	}
	
	

	/**
	 * Gets ForcePingTimeout property.
	 * 
	 * @return the ForcePingTimeout value.
	 */
	public int getForcePingTimeout() {
		String _value = props.getProperty("forcePingTimeout");
		return Integer.parseInt(_value);

	}

	/**
	 * Gets AutoDiscoveryUnknownNodes property.
	 * 
	 * @return the AutoDiscoveryUnknownNodes value.
	 */
	public short getAutoDiscoveryUnknownNodes() {
		String _value = props.getProperty("autoDiscoveryUnknownNodes");
		return Short.parseShort(_value);

	}

	/**
	 * Initialize a local StartupAttributeInfo object with predefined convenient
	 * fixed values.
	 */
	private void initDefaultStartupAttributeInfo() {
		sai = new StartupAttributeInfo();

		sai.setShortAddress(0xFFFF); // 2 bytes
		sai.setDeviceType(LogicalType.ROUTER);
		sai.setExtendedPANId(BigInteger.ZERO); // 8 bytes
		// APS use extended Pan Id miss on Startup Attribute Info 8 bytes
		sai.setPANId(0xFFFF); // 2 bytes
		sai.setChannelMask((long) 0x00); // 4 bytes
		sai.setProtocolVersion((short) 0x02); // 1 byte
		sai.setStackProfile((short) 0x02); // 1 byte
		sai.setStartupControl((short) 0x00); // 1 byte
		sai.setStartupAttributeSetIndex((short) 0x00); // 1 byte

		sai.setTrustCenterAddress(new BigInteger("00000000000000000000000000000000", 16)); // 16bytes
		sai.setTrustCenterMasterKey(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }); // 16
																																																																	// bytes
		sai.setNetworkKey(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }); // 16
																																																															// bytes
		sai.setUseInsecureJoin(true); // 1 byte
		sai.setPreconfiguredLinkKey(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }); // 16
																																																																	// bytes
		sai.setNetworkKeySeqNum((short) 0x00); // 1 byte
		sai.setNetworkKeyType(KeyType.HIGH_SECURITY); // 1 byte
		sai.setNetworkManagerAddress(0x0000); // 2 bytes
		sai.setScanAttempts((short) 0x03); // 1 byte
		sai.setTimeBetweenScans(0x6400); // 2 bytes
		sai.setRejoinInterval(0x0002); // 2 bytes
		sai.setMaxRejoinInterval(0x0E10); // 2 bytes
		sai.setIndirectPollRate(0x1C84); // 2 bytes
		sai.setParentRetryThreshold((short) 0x02); // 1 byte
		sai.setConcentratorFlag(false); // 1 byte
		sai.setConcentratorRadius((short) 0x05); // 1 byte
		sai.setConcentratorDiscoveryTime((short) 0x00); // 1 byte

	}

	/**
	 * Gets a StartupAttributeInfo instance with predefined convenient fixed
	 * values.
	 * 
	 * @return a conveniently initialized instance of a StartupAttributeInfo
	 *         object.
	 */
	public StartupAttributeInfo getSturtupAttributeInfo() {
		if (sai == null)
			initializeSturtupAttributeInfo();
		return sai;
	}

	/**
	 * Initialize a StartupAttributeInfo object taking values from currently
	 * loaded properties.
	 * 
	 * @return a StartupAttributeInfo object.
	 */
	public StartupAttributeInfo initializeSturtupAttributeInfo() {
		sai = new StartupAttributeInfo();
		initDefaultStartupAttributeInfo();
		/* DeviceType */
		short deviceTypeRead = readShort("DeviceType");
		LogicalType deviceType;
		switch (deviceTypeRead) {
		case 0:
			deviceType = LogicalType.CURRENT;
			break;
		case 1:
			deviceType = LogicalType.COORDINATOR;
			break;
		case 2:
			deviceType = LogicalType.ROUTER;
			break;
		case 3:
			deviceType = LogicalType.END_DEVICE;
			break;
		default:
			throw new RuntimeException("Wrong data in properties file:deviceType");
		}
		sai.setDeviceType(deviceType);

		/* Channel Mask */
		short channelMask = readShort("ChannelMask");
		if (!((channelMask == 0) || ((channelMask >= 11) && (channelMask <= 26)))) {
			throw new RuntimeException("Wrong data in properties file ChannelMask [valid range: 11 - 26, OR '0' for all channels]");
		}
		sai.setChannelMask((long) channelMask);

		/* PanId */
		int panId = readIntHex("PANId");
		sai.setPANId(panId);

		/* Extended PanId */
		BigInteger extendedPanId = new BigInteger(readByteArray("ExtendedPANId", 8));
		sai.setExtendedPANId(extendedPanId);

		/* StartupControlMode */
		short startUpControlMode = readShortHex("StartupControlMode");
		sai.setStartupControl(startUpControlMode);

		/* StartupSet */
		short startUpSet = readShortHex("StartupSet");
		sai.setStartupAttributeSetIndex(startUpSet);

		/* networkKey */
		byte[] networkKey = readByteArray("networkKey", 16);
		sai.setNetworkKey(networkKey);

		/* PreconfiguredLinkKey */
		byte[] preconfiguredLinkKey = readByteArray("preconfiguredLinkKey", 16);
		sai.setPreconfiguredLinkKey(preconfiguredLinkKey);

		return sai;

	}

	/**
	 * Sets values contained by a StartupAttributeInfo object to the Properties
	 * object.
	 * 
	 * @param sai
	 *            the StartupAttributeInfo object from which take values to set.
	 */
	public void SetStartupAttributeInfo(StartupAttributeInfo sai) {

		/* DeviceType */
		Short deviceType = Short.parseShort(props.getProperty("DeviceType"));
		if (sai.getDeviceType() != null) {
			switch (sai.getDeviceType()) {
			case CURRENT:
				deviceType = 0;
				break;
			case COORDINATOR:
				deviceType = 1;
				break;
			case ROUTER:
				deviceType = 2;
				break;
			case END_DEVICE:
				deviceType = 3;
				break;
			}
		}
		props.setProperty("DeviceType", deviceType.toString());

		/* Channel Mask */
		props.setProperty("ChannelMask", sai.getChannelMask().toString());

		/* PanId */
		props.setProperty("PANId", sai.getPANId().toString());

		/* Extended PanId */
		props.setProperty("ExtendedPANId", sai.getExtendedPANId().toString());

		/* StartupControlMode */
		props.setProperty("StartupControlMode", sai.getStartupControl().toString());

		/* StartupSet */
		//props.setProperty("StartupSet", String.valueOf(sai.getStartupSet()));

		/* networkKey */
		props.setProperty("networkKey", DataManipulation.convertBytesToString(sai.getNetworkKey()));

		/* PreconfiguredLinkKey */
		props.setProperty("preconfiguredLinkKey", DataManipulation.convertBytesToString(sai.getPreconfiguredLinkKey()));

	}

	/**
	 * Gets StartupControlMode property.
	 * 
	 * @return the StartupControlMode value.
	 */
	public short getStartupControlMode() {
		short startupControlMode = readShortHex("StartupControlMode");
		return startupControlMode;
	}

	/**
	 * Sets StartupControlMode property.
	 * 
	 * @param startupControlMode
	 *            the StartupControlMode value to set.
	 */
	public void setStartupControlMode(Short startupControlMode) {
		props.setProperty("StartupControlMode", String.format("%02X", startupControlMode));

	}

	/**
	 * Gets StartupSet property.
	 * 
	 * @return the StartupSet value.
	 */
	public short getStartupSet() {

		short startupSet = readShortHex("StartupSet");
		return startupSet;
	}

	/**
	 * Sets StartupSet property.
	 * 
	 * @param startupSet
	 *            the StartupSet value to set.
	 */
	public void setStartupSet(Short startupSet) {
		props.setProperty("StartupSet", String.format("%02X", startupSet));

	}

	/* zgd.dongle.uri */
	/**
	 * Gets zgdDongleUri property.
	 * 
	 * @return the zgdDongleUri value.
	 */
	public String getzgdDongleUri() {
		String uri = props.getProperty(GatewayProperties.ZGD_DONGLE_URI_PROP_NAME);
		return uri;
	}

	/* zgd.dongle.speed */
	/**
	 * Gets zgdDongleSpeed property.
	 * 
	 * @return the zgdDongleSpeed value.
	 */
	public int getzgdDongleSpeed() {
		int speed = Integer.parseInt(props.getProperty(GatewayProperties.ZGD_DONGLE_SPEED_PROP_NAME));
		return speed;
	}

	/* zgd.dongle.type */
	/**
	 * Gets zgdDongleType property.
	 * 
	 * @return the zgdDongleType value.
	 */
	public String getzgdDongleType() {
		String type = props.getProperty(GatewayProperties.ZGD_DONGLE_TYPE_PROP_NAME);
		return type;
	}

	/* AutoStart */
	/**
	 * Gets AutoStart property.
	 * 
	 * @return the AutoStart value.
	 */
	public short getAutoStart() {
		short autostart = readShort("autostart");
		return autostart;
	}

	/**
	 * Sets AutoStart property.
	 * 
	 * @param _auto
	 *            the AutoStart value to set.
	 */
	public void setAutoStart(Boolean _auto) {
		props.setProperty("autostart", _auto.toString());
	}

	/* SimpleDescriptor */
	/**
	 * Gets a SimpleDescriptor object filled with values read from Properties
	 * file.
	 * 
	 * @return a initialized SimpleDescriptor object.
	 */
	public SimpleDescriptor getSimpleDescriptorReadFromFile() {
		SimpleDescriptor sd = new SimpleDescriptor();
		// Simple descriptor
		short defaultEndPoint = readShortHex("DefaultEndPoint");
		sd.setEndPoint(defaultEndPoint);

		int applicationDeviceIdentifier = readIntHex("ApplicationDeviceIdentifier");
		sd.setApplicationDeviceIdentifier(applicationDeviceIdentifier);

		short applicationDeviceVersion = readShortHex("ApplicationDeviceVersion");
		sd.setApplicationDeviceVersion(applicationDeviceVersion);

		int applicationProfileIdentifier = readIntHex("ApplicationProfileIdentifier");
		sd.setApplicationProfileIdentifier(applicationProfileIdentifier);

		int[] applicationInputCluster = readIntArray("ApplicationInputCluster");
		for (int x : applicationInputCluster)
			sd.getApplicationInputCluster().add(x);

		int[] applicationOutputCluster = readIntArray("ApplicationOutputCluster");
		for (int x : applicationOutputCluster)
			sd.getApplicationOutputCluster().add(x);
		return sd;
	}

	/* ExtendedPANId */
	/**
	 * Gets ExtendedPANId property.
	 * 
	 * @return the ExtendedPANId value.
	 */
	public BigInteger getExtendedPanId() {
		BigInteger extendedPanId = new BigInteger(readByteArray("ExtendedPANId", 8));
		return extendedPanId;
	}

	/* Utility */
	/**
	 * Reads the key from Properties file as a {@code byte[]} of card size.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @param card
	 *            the number of bytes to convert.
	 * @return the resulting array.
	 */
	private byte[] readByteArray(String key, int card) {
		short[] temp = readShortArray(key, card);
		byte[] toReturn = new byte[card];
		for (int i = 0; i < card; i++) {
			toReturn[i] = (byte) temp[i];
		}
		return toReturn;
	}

	/**
	 * Reads the key from Properties file as a {@code short[]} of card size.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @param card
	 *            the number of bytes to convert.
	 * @return the resulting array.
	 */
	private short[] readShortArray(String key, int card) {
		String read = props.getProperty(key);
		read = read.trim();
		StringTokenizer st = new StringTokenizer(read, " ");
		int tokensCard = st.countTokens();
		if (tokensCard != card) {
			throw new RuntimeException("Wrong data in properties file:Key");
		}
		short[] toReturn = new short[tokensCard];
		int counter = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.trim();
			// The tokens starts with the prefix 0x that we bust ignore
			token = token.substring(2);
			toReturn[counter++] = (short) Integer.parseInt(token, 16);
		}
		return toReturn;
	}

	/**
	 * Reads the key from Properties file as a {@code int[]} of card size.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @return the resulting array.
	 */
	private int[] readIntArray(String key) {
		String read = props.getProperty(key);
		read = read.trim();
		StringTokenizer st = new StringTokenizer(read, " ");
		int tokensCard = st.countTokens();

		int[] toReturn = new int[tokensCard];
		int counter = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			token = token.trim();
			// The tokens starts with the prefix 0x that we bust ignore
			token = token.substring(2);
			toReturn[counter++] = Integer.parseInt(token, 16);
		}
		return toReturn;
	}

	/**
	 * Reads the key from Properties file as a {@code short}.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @return the resulting array.
	 */
	private short readShort(String key) {
		return Short.parseShort(props.getProperty(key).trim());
	}

	/**
	 * Reads the key from Properties file as an hexadecimal {@code short}.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @return the resulting number.
	 */
	private short readShortHex(String key) {
		String read = props.getProperty(key);
		read = read.trim();
		if (read.toLowerCase().startsWith("0x"))
			read = read.substring(2);
		return ((short) Integer.parseInt(read, 16));
	}

	/**
	 * Reads the key from Properties file as an hexadecimal {@code int}.
	 * 
	 * @param key
	 *            the key to read from Properties file.
	 * @return the resulting number.
	 */
	private int readIntHex(String key) {
		String read = props.getProperty(key);
		read = read.trim();
		if (read.toLowerCase().startsWith("0x"))
			read = read.substring(2);
		return Integer.parseInt(read, 16);
	}

}
