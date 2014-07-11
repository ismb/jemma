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
package org.energy_home.jemma.m2m;


public abstract class M2MConstants {
	private static String CONFIGURED_CLIENT_VERSION = null;
	static {
		String clientVersion = System.getProperty("org.energy_home.jemma.m2m.device.enableProtocolVersion");
		if (clientVersion == null || clientVersion.equals("true"))
			// TODO:!!! client version need to be read from manifest file
			CONFIGURED_CLIENT_VERSION = "1.2.15";
		else
			CONFIGURED_CLIENT_VERSION = null;
	}
	
	public static final String CLIENT_VERSION = CONFIGURED_CLIENT_VERSION;
	
	// No connection with M2M Network platform is performed if this id is configured for the M2M Device
	public static final String LOCAL_ONLY_DEVICE_ID = "local";
	
	public static final String URL_HTTP_PREFIX = "http://";
	public static final String URL_PORT_PREFIX = ":";
	public static final String URL_SLASH = "/";
	public static final String URL_CONNECTION_BASE = "/HAP/CONNECTION";
	public static final String URL_SCL_BASE = "/HAP/SC/SB";
	public static final String URL_LIST = "/LIST";
	public static final String URL_SCLS = "/SCLS";
	public static final String URL_ACCESS_RIGHTS = "/ARS";
	public static final String URL_CONTAINERS = "/CS";
	public static final String URL_SUBSCRIPTIONS = "/SUBS";
	public static final String URL_CONTENT_INSTANCES = "/CIS";
	public static final String URL_DEFAULT_ACCESS_RIGHT = "/defaultAR";
	public static final String URL_CIS_BATCH_REQUEST = "/CS/ALL/CIS";
	public static final String URL_CIS_ID_LATEST_ALIAS = "/LATEST";
	public static final String URL_CIS_ID_OLDEST_ALIAS = "/OLDEST";
	
	public static final String URL_HAG_SCL_BASE = "/HAP/SC/HAG";

}
