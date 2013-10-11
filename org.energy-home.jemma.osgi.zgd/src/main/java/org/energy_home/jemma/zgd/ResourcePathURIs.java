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
package org.energy_home.jemma.zgd;

public interface ResourcePathURIs {
	public static final String VERSION = "/version";
	public static final String INFOBASE = "/ib/";
	public static final String REQUESTS = "/requests/";
	public static final String NETWORKS = "/networks";
	public static final String RESET = "/reset";
	public static final String STARTUP = "/startup";
	
	public static final String CALLBACKS = "/callbacks";
	public static final String LOCALNODE_SERVICES = "/localnode/services";
	public static final String WSNCONNECTION = "/wsnconnection";
	public static final String LOCALNODE_ALLSERVICES_WSNCONNECTION = "/localnode/allservices/wsnconnection";
	public static final String ALIASES = "/aliases";
	public static final String WSNNODES = "/wsnnodes";
	public static final String ALLWSNNODES = "/allwsnnodes";
	public static final String ALLWSNNODES_SERVICES = "/allwsnnodes/services";	
	public static final String SERVICES = "/services";
	public static final String BINDINGS = "/bindings";
	public static final String UNBINDINGS = "/unbindings";
	public static final String NODEDESCRIPTOR = "/nodedescriptor";
	public static final String ALLPERMIT_JOIN = "/allwsnnodes/permitjoin";
	public static final String PERMIT_JOIN = "/permitjoin";
	public static final String SEND_APSMESSAGE = "/wsnconnection/message";
	
	public static final String MODE_CACHE = "mode=cache";
	public static final String URILISTENER_PARAM = "urilistener=";
	public static final String TIMEOUT_PARAM = "timeout=";
	public static final String INFINITE_TIMEOUT = "ffffffff";
	public static final String INDEX_PARAM = "index=";
	public static final String RESET_START_MODE = "startMode=0x";
	
	public static final String DISCOVERY_INQUIRY = "inquiry";
	public static final String DISCOVERY_ANNOUNCEMENTS = "announcements";
	public static final String DISCOVERY_LEAVE = "leave";
	public static final String DISCOVERY_LQI = "lqi";
	public static final String DISCOVERY_FRESHNESS = "freshness";
	
	public static final String REJOIN = "rejoin";
	public static final String REMOVE_CHILDREN = "remove-children";
}
