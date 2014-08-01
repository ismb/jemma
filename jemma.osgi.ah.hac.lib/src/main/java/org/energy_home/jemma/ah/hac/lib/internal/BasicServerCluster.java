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
package org.energy_home.jemma.ah.hac.lib.internal;

import org.energy_home.jemma.ah.cluster.zigbee.general.BasicServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class BasicServerCluster extends ServiceCluster implements BasicServer {

	public BasicServerCluster() throws ApplianceException {
		super();
	}
	
	final static String[] supportedAttributes = {  ATTR_ZCLVersion_NAME, ATTR_PowerSource_NAME };

	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
			ServiceClusterException {
		return supportedAttributes;
	}

	public short getZCLVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return 2;
	}

	public short getApplicationVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public short getStackVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public short getHWVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public String getManufacturerName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public String getModelIdentifier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public String getDateCode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public short getPowerSource(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return 4;
	}

	public String getLocationDescription(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public void setLocationDescription(String LocationDescription, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public short getPhysicalEnvironment(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public void setPhysicalEnvironment(short PhysicalEnvironment, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		throw new UnsupportedClusterAttributeException();

	}

	public boolean getDeviceEnabled(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public void setDeviceEnabled(boolean DeviceEnabled, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		throw new UnsupportedClusterAttributeException();

	}

	public short getAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public void setAlarmMask(short AlarmMask, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public short getDisableLocalConfig(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public void setDisableLocalConfig(short DisableLocalConfig, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

}
