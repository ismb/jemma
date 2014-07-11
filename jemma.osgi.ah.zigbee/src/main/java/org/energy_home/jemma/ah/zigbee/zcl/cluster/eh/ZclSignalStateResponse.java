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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.eh;

import org.energy_home.jemma.ah.cluster.zigbee.eh.SignalStateResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclSignalStateResponse {


    public static SignalStateResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        SignalStateResponse r = new SignalStateResponse();
        r.ApplianceStatus = ZclDataTypeEnum8 .zclParse(zclFrame);
        r.RemoteEnableFlags = ZclDataTypeUI8 .zclParse(zclFrame);
  
        try {
			r.ApplianceStatus2 = ZclDataTypeUI24 .zclParse(zclFrame);
		} catch (Exception e) {
			r.ApplianceStatus2 = 0;
		}
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, SignalStateResponse r)
        throws ZclValidationException
    {
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.ApplianceStatus);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.RemoteEnableFlags);
        ZclDataTypeUI24 .zclSerialize(zclFrame, r.ApplianceStatus2);
    }

    public static int zclSize(SignalStateResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeEnum8 .zclSize(r.ApplianceStatus);
        size += ZclDataTypeUI8 .zclSize(r.RemoteEnableFlags);
        size += ZclDataTypeUI24 .zclSize(r.ApplianceStatus2);
        return size;
    }

}
