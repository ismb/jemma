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

package org.energy_home.jemma.ah.zigbee.zcl.cluster.general;

import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveSceneResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclRemoveSceneResponse {


    public static RemoveSceneResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        RemoveSceneResponse r = new RemoveSceneResponse();
        r.Status = ZclDataTypeEnum8 .zclParse(zclFrame);
        r.GroupID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.SceneID = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, RemoveSceneResponse r)
        throws ZclValidationException
    {
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.Status);
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.GroupID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.SceneID);
    }

    public static int zclSize(RemoveSceneResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeEnum8 .zclSize(r.Status);
        size += ZclDataTypeUI16 .zclSize(r.GroupID);
        size += ZclDataTypeUI8 .zclSize(r.SceneID);
        return size;
    }

}
