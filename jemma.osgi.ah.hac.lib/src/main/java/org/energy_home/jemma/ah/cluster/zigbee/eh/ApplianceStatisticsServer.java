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
package org.energy_home.jemma.ah.cluster.zigbee.eh;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface ApplianceStatisticsServer {

	final static String ATTR_LogMaxSize_NAME = "LogMaxSize";
	final static String ATTR_LogQueueMaxSize_NAME = "LogQueueMaxSize";
	final static String CMD_LogRequest_NAME = "LogRequest";
	final static String CMD_LogQueueRequest_NAME = "LogQueueRequest";

	public long getLogMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getLogQueueMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public LogResponse execLogRequest(long LogID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public LogQueueResponse execLogQueueRequest(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
