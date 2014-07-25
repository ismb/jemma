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

public interface TimeServer {

	final static String ATTR_Time_NAME = "Time";
	final static String ATTR_TimeStatus_NAME = "TimeStatus";
	final static String ATTR_TimeZone_NAME = "TimeZone";
	final static String ATTR_DstStart_NAME = "DstStart";
	final static String ATTR_DstEnd_NAME = "DstEnd";
	final static String ATTR_DstShift_NAME = "DstShift";
	final static String ATTR_StandardTime_NAME = "StandardTime";
	final static String ATTR_LocalTime_NAME = "LocalTime";
	final static String ATTR_LastSetTime_NAME = "LastSetTime";
	final static String ATTR_ValidUntilTime_NAME = "ValidUntilTime";

	public long getTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getTimeStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getTimeZone(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setTimeZone(long TimeZone, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getDstStart(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDstStart(long DstStart, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getDstEnd(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDstEnd(long DstEnd, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getDstShift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setDstShift(long DstShift, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getStandardTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getLocalTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getLastSetTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getValidUntilTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	public void setValidUntilTime(long ValidUntilTime, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
