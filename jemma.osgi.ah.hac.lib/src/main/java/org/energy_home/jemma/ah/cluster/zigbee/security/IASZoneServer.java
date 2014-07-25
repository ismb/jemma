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
package org.energy_home.jemma.ah.cluster.zigbee.security;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface IASZoneServer {

	final static String ATTR_ZoneState_NAME = "ZoneState";
	final static String ATTR_ZoneType_NAME = "ZoneType";
	final static String ATTR_ZoneStatus_NAME = "ZoneStatus";
	final static String ATTR_IAS_CIE_Address_NAME = "IAS_CIE_Address";
	final static String ATTR_ZoneID_NAME = "ZoneID";

	public short getZoneState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getZoneType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getZoneStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public byte[] getIAS_CIE_Address(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setIAS_CIE_Address(byte[] IAS_CIE_Address, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getZoneID(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
