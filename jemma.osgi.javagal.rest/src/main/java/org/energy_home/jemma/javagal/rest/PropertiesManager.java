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
package org.energy_home.jemma.javagal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties manager class.
 * <p>
 * Loads/saves from/to a ".properties" file the desired values for the JavaGal
 * execution. It's THE way to control a number of parameters at startup.
 */
/**
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
@Deprecated //FIXME to be removed: see same class in javagal bundle
public class PropertiesManager {
	private static final Logger LOG = LoggerFactory.getLogger( PropertiesManager.class );
	public Properties props;

	public PropertiesManager(URL _url) {
		LOG.debug("PropertiesManager - Costructor - Loading configuration file...");
		InputStream in = null;
		try {
			in = _url.openStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			props = new Properties();
			props.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LOG.debug("PropertiesManager - Costructor - Configuration file loaded!");

	}

	/**
	 * Get the debug messages enabled status 
	 **/
	public boolean getDebugEnabled() {
		String _value = props.getProperty("debugEnabled");
		return (_value.equalsIgnoreCase("0")) ? false : true;

	}

	/**
	 * Decide if the Network Root URI can be obtained by appending the
	 * net/default' suffix (SELECT 1), or by appending the net/<ExtendedPANId>'
	 * suffix (SELECT 0)
	 * */
	public int getUseDefaultNWKRootURI() {
		String _value = props.getProperty("UseDefaultNWKRootURI");
		return Integer.parseInt(_value);

	}

	/**
	 * HTTP option application timeout (in seconds) - Note: for remote
	 * connection between GW and IPHA insert a value higher than 1. Set to zero
	 * to completely disable caching
	 */
	public int getHttpOptTimeout() {
		String _value = props.getProperty("httpOptTimeout");
		return Integer.parseInt(_value);

	}

	public int getnumberOfConnectionFail() {
		String _value = props.getProperty("numberOfConnectionFail");
		return Integer.parseInt(_value);

	}

	public void setDebugEnabled(Boolean _debug) {
		props.setProperty("debugEnabled", _debug.toString());

	}

	/* Debug */
	public int getIPPort() {
		String _value = props.getProperty("serverPorts");
		return Integer.parseInt(_value);

	}

	private short readShort(String key) {
		return Short.parseShort(props.getProperty(key).trim());
	}

	private short readShortHex(String key) {
		String read = props.getProperty(key);
		read = read.trim();
		// The read string starts with the prefix 0x that we bust ignore
		read = read.substring(2);
		return ((short) Integer.parseInt(read, 16));
	}

	private int readIntHex(String key) {
		String read = props.getProperty(key);
		read = read.trim();
		// The read string starts with the prefix 0x that we bust ignore
		read = read.substring(2);
		return Integer.parseInt(read, 16);
	}

}
