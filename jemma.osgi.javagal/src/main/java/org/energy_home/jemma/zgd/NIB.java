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

public interface NIB {
	static final short	nwkSequenceNumber				= 0x81;
	static final short	nwkPassiveAckTimeout			= 0x82;
	static final short	nwkMaxBroadcastRetries			= 0x83;
	static final short	nwkMaxChildren					= 0x84;
	static final short	nwkMaxDepth						= 0x85;
	static final short	nwkMaxRouters					= 0x86;
	static final short	nwkNeighborTable				= 0x87;
	static final short	nwkNetworkBroadcastDeliveryTime	= 0x88;
	static final short	nwkReportConstantCost			= 0x89;
	static final short	nwkRouteTable					= 0x8b;
	static final short	nwkSymLink						= 0x8f;
	static final short	nwkCapabilityInformation		= 0x8d;
	static final short	nwkAddrAlloc					= 0x90;
	static final short	nwkUseTreeRouting				= 0x91;
	static final short	nwkManagerAddr					= 0x92;
	static final short	nwkMaxSourceRoute				= 0x93;
	static final short	nwkUpdateId						= 0x94;
	static final short	nwkTransactionPersistenceTime	= 0x95;
	static final short	nwkNetworkAddress				= 0x96;
	static final short	nwkStackProfile					= 0x97;
	static final short	nwkBroadcastTransactionTable	= 0x98;
	static final short	nwkGroupIDTable					= 0x99;
	static final short	nwkExtendedPANID				= 0x9a;
	static final short	nwkUseMulticast					= 0x9b;
	static final short	nwkRouteRecordTable				= 0x9c;
	static final short	nwkIsConcentrator				= 0x9d;
	static final short	nwkConcentratorRadius			= 0x9e;
	static final short	nwkConcentratorDiscoveryTime	= 0x9f;
	static final short	nwkSecurityLevel				= 0xa0;
	static final short	nwkSecurityMaterialSet			= 0xa1;
	static final short	nwkActiveKeySeqNumber			= 0xa2;
	static final short	nwkAllFresh						= 0xa3;
	static final short	nwkSecureAllFrames				= 0xa5;
	static final short	nwkLinkStatusPeriod				= 0xa6;
	static final short	nwkRouterAgeLimit				= 0xa7;
	static final short	nwkUniqueAddr					= 0xa8;
	static final short	nwkAddressMap					= 0xa9;
	static final short	nwkTimeStamp					= 0x8c;
	static final short	nwkPANId						= 0x80;
	static final short	nwkTxTotal						= 0x8d;
}
