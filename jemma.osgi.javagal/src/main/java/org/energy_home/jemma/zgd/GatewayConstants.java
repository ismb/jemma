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
package org.energy_home.jemma.zgd;

/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface GatewayConstants {
	public static String GATEWAY_NAMESPACE = "http://www.zigbee.org/GWGSchema";
	public static String REST_NAMESPACE = "http://www.zigbee.org/GWGRESTSchema";
	public static String GATEWAY_NAMESPACE_PREFIX = "crt";
	public static String REST_NAMESPACE_PREFIX = "tns";
	
	
	public static final int DISCOVERY_STOP = 0;
	public static final int DISCOVERY_INQUIRY = 1;
	public static final int DISCOVERY_ANNOUNCEMENTS = 2;
	public static final int DISCOVERY_LEAVE = 4;
	public static final int DISCOVERY_LQI = 8;
	public static final int DISCOVERY_FRESHNESS = 16;
	
	public static final int LEAVE_REMOVE_CHILDERN = 1;
    public static final int LEAVE_REJOIN = 2;
	
	public static final short RESET_COMMISSIONING_ASSOCIATION = 0;
	public static final short RESET_USE_NVMEMORY = 1;
	public static final short RESET_COMMISSIONING_SILENTSTART = 2;
	
	
	public final  int BROADCAST_ADDRESS = 0xffff;
	public final int ROUTER_BROADCAST_ADDRESS = 0xfffc;
	public final long INFINITE_TIMEOUT = 0xffffffffL;
	
	public static short PERMITJOIN_FOREVER = 0xff;
	public static short PERMITJOIN_NEVER = 0;
	
	
/*	PS Information Base
	The APS information base comprises the attributes required to manage the APS
	layer of a device. The attributes of the AIB are listed in Table 2.24. The security-
	related AIB attributes are described in sub-clause 4.4.10.*/
	
	public static final short APS_BINDING_TABLE = 0xc1;
	public static final int APS_DESIGNATED_COORDINATOR = 0xc2;
	public static final int APS_CHANNEL_MASK = 0xc3; // 195
	public static final int APS_USE_EXTENDED_PANID = 0xc4;
	public static final int APS_GROUP_TABLE = 0xc5;
	public static final int APS_NONMEMBER_RADIUS = 0xc6;
	public static final int APS_PERMISSIONS_CONFIGURATION = 0xc7;
	public static final int APS_USE_INSECURE_JOIN = 0xc8;
	public static final int APS_INTERFRAME_DELAY = 0xc9;
	public static final int APS_LAST_CHANNEL_ENERGY = 0xca;
	public static final int APS_LAST_CHANNEL_FAILURE_RATE = 0xcb;
	public static final int APS_CHANNEL_TIMER = 0xcc;
	public static final int APS_MAX_WINDOW_SIZE = 0xcd;

	/*
	 * The addressing mode used for the DestinationAddress parameter (see [R1]
	 * sub-clause APSDE-DATA.request DstAddrMode parameter.) A value of
	 * AliasAddress indicates that the DestinationAddress is an alias address.
	 * If this parameter is omitted then it is assumed that a binding table
	 * entry exists in the GW that determines the destination.
	 */
	
	public final static short ADDRESS_MODE_SHORT = 2;
	public final static short EXTENDED_ADDRESS_MODE = 3;
	public final static short ADDRESS_MODE_ALIAS = 16;

	

	
	
	
	public static final int SUCCESS = 0;
	public static final int TIMEOUT = 1;
	public static final int GENERAL_ERROR = 2;
	public static final int PARAMETER_MISSING = 3;
	public static final int INVALID_VALUE = 4;
	public static final int NETWORK_NOT_READY = 5;
	public static final int EMPTY = 6;
	public static final int NOT_ALLOWED = 7;
	public static final int MEMORY_ERROR = 8;
	public static final int APS_FAILURE = 9;
	public static final int NETWORK_FAILURE = 10;
}
