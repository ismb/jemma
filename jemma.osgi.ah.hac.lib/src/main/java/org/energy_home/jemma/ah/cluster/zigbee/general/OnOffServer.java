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

public interface OnOffServer {

	final static String ATTR_OnOff_NAME = "OnOff";
	final static String ATTR_MaxOnDuration_NAME = "MaxOnDuration";
	final static String ATTR_CurrentOnDuration_NAME = "CurrentOnDuration";
	final static String CMD_Off_NAME = "Off";
	final static String CMD_On_NAME = "On";
	final static String CMD_Toggle_NAME = "Toggle";
	final static String CMD_OnWithDuration_NAME = "OnWithDuration";

	public boolean getOnOff(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getMaxOnDuration(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentOnDuration(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execOff(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execOn(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execToggle(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execOnWithDuration(int OnDuration, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

}
