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

public interface PollControlServer {

    final static String ATTR_ChecknInInterval_NAME = "ChecknInInterval";
    final static String ATTR_LongPollInterval_NAME = "LongPollInterval";
    final static String ATTR_ShortPollInterval_NAME = "ShortPollInterval";
    final static String ATTR_FastPollTimeout_NAME = "FastPollTimeout";
    final static String ATTR_CheckInIntervalMin_NAME = "CheckInIntervalMin";
    final static String ATTR_LongPollIntervalMin_NAME = "LongPollIntervalMin";
    final static String ATTR_FastPollTimeoutMax_NAME = "FastPollTimeoutMax";
    final static String CMD_CheckInResponse_NAME = "CheckInResponse";
    final static String CMD_FastPollStop_NAME = "FastPollStop";
    final static String CMD_SetLongPollInterval_NAME = "SetLongPollInterval";
    final static String CMD_SetShortPollInterval_NAME = "SetShortPollInterval";

    public long getChecknInInterval(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public long getLongPollInterval(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getShortPollInterval(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getFastPollTimeout(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public long getCheckInIntervalMin(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public long getLongPollIntervalMin(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getFastPollTimeoutMax(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execCheckInResponse(boolean StartFastPolling, int FastPollTimeout, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execFastPollStop(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execSetLongPollInterval(long NewLongPollInterval, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execSetShortPollInterval(int NewShortPollInterval, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
