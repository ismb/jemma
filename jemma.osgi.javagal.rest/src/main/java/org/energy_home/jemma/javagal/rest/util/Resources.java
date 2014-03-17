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
package org.energy_home.jemma.javagal.rest.util;

/**
 * Resources class.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class Resources {
	/*
	 * Root URIs
	 */
	public final static String GW_ROOT_URI = "";
	// Note that we consider only one net, so we have the only "default" one
	public final static String NET_ROOT_URI = "/net";
	public static String NET_DEFAULT_ROOT_URI = "/default";
	public static void setNET_DEFAULT_ROOT_URI(String nET_DEFAULT_ROOT_URI) {
		NET_DEFAULT_ROOT_URI = "/" + nET_DEFAULT_ROOT_URI;
		NWT_ROOT_URI = GW_ROOT_URI + NET_ROOT_URI
				+ NET_DEFAULT_ROOT_URI;
	}

	public static String NWT_ROOT_URI = GW_ROOT_URI + NET_ROOT_URI
			+ NET_DEFAULT_ROOT_URI;

	/*
	 * URIs
	 */
	public final static String URI_PARAM_URILISTENER = "urilistener";
	public final static String URI_PARAM_TIMEOUT = "timeout";
	public final static String URI_PARAM_START = "start";
	public final static String URI_PARAM_INDEX = "index";
	public final static String URI_PARAM_MODE = "mode";
	public final static String URI_PARAM_CACHE = "cache";
	
	public final static String URI_ID = "/{id}";
	public final static String URI_ADDR = "/{addr}";
	public final static String URI_ENDPOINT = "/{ep}";
	public final static String URI_ATTR = "{attr}";
	public final static String URI_SERVICE = "/{service}";
	public final static String URI_AOI = "/{aoi}";
	public final static String URI_SCANCHANNEL = "scanChannel";
	public final static String URI_SCANDURATION = "scanDuration";
	public final static String URI_PARAM_START_MODE_RESET = "startMode";

	/*
	 * Parameters' strings
	 */
	public final static String PARAMETER_ID = "id";
	public final static String PARAMETER_EP = "ep";
	public final static String PARAMETER_ENDPOINT = "endpoint";
	public final static String PARAMETER_SERVICE = "service";
	public final static String PARAMETER_AOI = "aoi";
	public final static String PARAMETER_ADDR = "addr";

	public final static String HEX_PREFIX = "0x";

	/*
	 * Misc
	 */
	public final static String ZGD_NOT_READY = "ZGD not ready...";
}
