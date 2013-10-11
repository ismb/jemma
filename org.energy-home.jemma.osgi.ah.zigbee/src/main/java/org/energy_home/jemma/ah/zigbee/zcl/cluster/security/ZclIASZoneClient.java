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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.security;

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public class ZclIASZoneClient
    extends ZclServiceCluster
    implements IASZoneClient, ZigBeeDeviceListener
{

    public final static short CLUSTER_ID = 1280;

    public ZclIASZoneClient()
        throws ApplianceException
    {
        super();
    }

    protected int getClusterId() {
        return CLUSTER_ID;
    }

    public void execZoneStatusChangeNotification(int ZoneStatus, short ExtendedStatus, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeEnum16 .zclSize(ZoneStatus);
        size += ZclDataTypeEnum8 .zclSize(ExtendedStatus);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(0);
        ZclDataTypeEnum16 .zclSerialize(zclFrame, ZoneStatus);
        ZclDataTypeEnum8 .zclSerialize(zclFrame, ExtendedStatus);
        issueExec(zclFrame, 11, context);
    }

    public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeEnum16 .zclSize(ZoneType);
        size += ZclDataTypeUI16 .zclSize(ManufacturerCode);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(1);
        ZclDataTypeEnum16 .zclSerialize(zclFrame, ZoneType);
        ZclDataTypeUI16 .zclSerialize(zclFrame, ManufacturerCode);
        IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
        return (ZclZoneEnrollResponse.zclParse(zclResponseFrame));
    }

}
