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

public interface DeviceTemperatureConfigurationServer {

	final static String ATTR_CurrentTemperature_NAME = "CurrentTemperature";
	final static String ATTR_MinTempExperienced_NAME = "MinTempExperienced";
	final static String ATTR_MaxTempExperienced_NAME = "MaxTempExperienced";
	final static String ATTR_OverTempTotalDwell_NAME = "OverTempTotalDwell";
	final static String ATTR_DeviceTempAlarmMask_NAME = "DeviceTempAlarmMask";
	final static String ATTR_LowTempThreshold_NAME = "LowTempThreshold";
	final static String ATTR_HighTempThreshold_NAME = "HighTempThreshold";
	final static String ATTR_LowTempDwellTripPoint_NAME = "LowTempDwellTripPoint";
	final static String ATTR_HighTempDwellTripPoint_NAME = "HighTempDwellTripPoint";

	public int getCurrentTemperature(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getMinTempExperienced(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getMaxTempExperienced(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getOverTempTotalDwell(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getDeviceTempAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDeviceTempAlarmMask(short DeviceTempAlarmMask, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getLowTempThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setLowTempThreshold(int LowTempThreshold, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getHighTempThreshold(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setHighTempThreshold(int HighTempThreshold, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getLowTempDwellTripPoint(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setLowTempDwellTripPoint(int LowTempDwellTripPoint, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getHighTempDwellTripPoint(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setHighTempDwellTripPoint(int HighTempDwellTripPoint, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

}
