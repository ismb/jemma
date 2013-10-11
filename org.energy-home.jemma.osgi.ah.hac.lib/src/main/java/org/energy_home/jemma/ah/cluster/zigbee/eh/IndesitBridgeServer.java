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
package org.energy_home.jemma.ah.cluster.zigbee.eh;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface IndesitBridgeServer {

	final static String CMD_DBFT_NAME = "DBFT";
	final static String CMD_DEFT_NAME = "DEFT";
	final static String CMD_DEFTWA_NAME = "DEFTWA";

	public DBFTR execDBFT(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public DEFTR execDEFT(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public DEFTWAR execDEFTWA(short I2CBusAddress, short FrameHeader, short FrameType, byte[] FramePayload,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
