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

public interface APS_IB_AttrIDs {
	static final short	apsBindingTable				= 0xc1;
	static final short	apsDesignatedCoordinator	= 0xc3;
	static final short	apsChannelMask				= 0xc3;
	static final short	apsUseExdendedPANID			= 0xc4;
	static final short	apsGroupTable				= 0xc5;
	static final short	apsNonmemberRadius			= 0xc6;
	static final short	apsPermissionConfiguration	= 0xc7;
	static final short	apsUseInsecureJoin			= 0xc8;
	static final short	apsInterframeDelay			= 0xc9;
	static final short	apsLastChannelEnergy		= 0xca;
	static final short	apsLastChannelFailureRate	= 0xcb;
	static final short	apsChannelTimer				= 0xcc;
	static final short	apsMaxWindowSize			= 0xcd;
}
