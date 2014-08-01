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
package org.energy_home.jemma.ah.cluster.zigbee.measurement;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface OccupancySensingServer {

    final static String ATTR_Occupancy_NAME = "Occupancy";
    final static String ATTR_OccupancySensorType_NAME = "OccupancySensorType";
    final static String ATTR_PIROccupiedToUnoccupiedDelay_NAME = "PIROccupiedToUnoccupiedDelay";
    final static String ATTR_PIRUnccupiedToOccupiedDelay_NAME = "PIRUnccupiedToOccupiedDelay";
    final static String ATTR_UltraSonicOccupiedToUnoccupiedDelay_NAME = "UltraSonicOccupiedToUnoccupiedDelay";
    final static String ATTR_UltraSonicPIRUnccupiedToOccupiedDelay_NAME = "UltraSonicPIRUnccupiedToOccupiedDelay";

    public short getOccupancy(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getOccupancySensorType(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getPIROccupiedToUnoccupiedDelay(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getPIRUnccupiedToOccupiedDelay(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getUltraSonicOccupiedToUnoccupiedDelay(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getUltraSonicPIRUnccupiedToOccupiedDelay(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
