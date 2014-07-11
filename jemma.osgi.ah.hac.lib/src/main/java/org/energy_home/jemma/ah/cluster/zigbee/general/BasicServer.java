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
package org.energy_home.jemma.ah.cluster.zigbee.general;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface BasicServer {

	final static String ATTR_ZCLVersion_NAME = "ZCLVersion";
	final static String ATTR_ApplicationVersion_NAME = "ApplicationVersion";
	final static String ATTR_StackVersion_NAME = "StackVersion";
	final static String ATTR_HWVersion_NAME = "HWVersion";
	final static String ATTR_ManufacturerName_NAME = "ManufacturerName";
	final static String ATTR_ModelIdentifier_NAME = "ModelIdentifier";
	final static String ATTR_DateCode_NAME = "DateCode";
	final static String ATTR_PowerSource_NAME = "PowerSource";
	final static String ATTR_LocationDescription_NAME = "LocationDescription";
	final static String ATTR_PhysicalEnvironment_NAME = "PhysicalEnvironment";
	final static String ATTR_DeviceEnabled_NAME = "DeviceEnabled";
	final static String ATTR_AlarmMask_NAME = "AlarmMask";
	final static String ATTR_DisableLocalConfig_NAME = "DisableLocalConfig";

	public short getZCLVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getApplicationVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getStackVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getHWVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getManufacturerName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getModelIdentifier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getDateCode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPowerSource(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getLocationDescription(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setLocationDescription(String LocationDescription, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getPhysicalEnvironment(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setPhysicalEnvironment(short PhysicalEnvironment, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public boolean getDeviceEnabled(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDeviceEnabled(boolean DeviceEnabled, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setAlarmMask(short AlarmMask, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getDisableLocalConfig(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDisableLocalConfig(short DisableLocalConfig, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

}
