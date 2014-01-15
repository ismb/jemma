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
package org.energy_home.jemma.javagal.layers.object;

import java.util.Properties;

/**
 * Gateway Properties class.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
@SuppressWarnings("serial")
public class GatewayProperties extends Properties {
	public static final String GATEWAY_NAMESPACE = "it.telecomitalia.zgd.gateway-namespace";
	public static final String REST_NAMESPACE = "it.telecomitalia.zgd.rest-namespace";
	public static final String GATEWAY_NAMESPACE_PREFIX = "it.telecomitalia.zgd.gateway-prefix";
	public static final String REST_NAMESPACE_PREFIX = "it.telecomitalia.zgd.rest-prefix";
	public static final String GATEWAY_ROOT_URI = "it.telecomitalia.zgd.gateway-root";
	public static final String NETWORK_RESOURCES_URI = "it.telecomitalia.zgd.network-resources";
	public static final String LOCAL_PORT = "it.telecomitalia.zgd.local-port";
	public static final String LOCAL_ADDRESS = "it.telecomitalia.zgd.local-address";
	public static final String PUBLIC_ADDRESS_RESOLUTION = "it.telecomitalia.zgd.public-address-resolution";
	public static final String USE_PUBLIC_ADDRESS_RESOLUTION = "it.telecomitalia.zgd.use-public-address-resolution";
	public static final String FACTORY_IMPLEMENTATION_CLASS = "it.telecomitalia.zgd.factory-implementation-class";
	public static final String ENABLE_RESTLET_CONSOLE = "it.telecomitalia.zgd.enable-restlet-console";
	public static final String CONNECTION_TIMEOUT = "it.telecomitalia.zgd.connection-timeout";
	
	/*Added by Marco*/
	public static final String ZGD_DONGLE_URI_PROP_NAME = "zgd.dongle.uri";
	public static final String ZGD_DONGLE_TYPE_PROP_NAME = "zgd.dongle.type";
	public static final String ZGD_DONGLE_SPEED_PROP_NAME = "zgd.dongle.speed";
	public static final String ZGD_DEBUG_PROP_NAME = "zgd.log.debug";
	
	/**
	 * Creates a new instance setting up a set of initial properties for the
	 * gateway.
	 */	
	public GatewayProperties() {
		// set default values
		setProperty(FACTORY_IMPLEMENTATION_CLASS, "it.telecomitalia.zgd.impl.GatewayFactoryImpl");
		setProperty(GATEWAY_NAMESPACE, "http://www.zigbee.org/GWGSchema");
		setProperty(REST_NAMESPACE, "http://www.zigbee.org/GWGRESTSchema");		
		setProperty(GATEWAY_NAMESPACE_PREFIX, "cs");
		setProperty(REST_NAMESPACE_PREFIX, "rs");
		setProperty(GATEWAY_ROOT_URI, "http://localhost:9000");
		setProperty(NETWORK_RESOURCES_URI, "/net/default");
		setProperty(LOCAL_PORT, "9100");
		setProperty(LOCAL_ADDRESS, "");
		setProperty(PUBLIC_ADDRESS_RESOLUTION, "http://whatismyip.com/automation/n09230945.asp");
		setProperty(USE_PUBLIC_ADDRESS_RESOLUTION, "false");
		setProperty(ENABLE_RESTLET_CONSOLE, "false");
		setProperty(CONNECTION_TIMEOUT, "3000");
	}
}
