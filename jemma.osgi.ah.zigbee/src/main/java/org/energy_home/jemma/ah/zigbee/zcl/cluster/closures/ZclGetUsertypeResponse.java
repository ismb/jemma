
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetUsertypeResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclGetUsertypeResponse {


    public static GetUsertypeResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetUsertypeResponse r = new GetUsertypeResponse();
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.UserType = ZclDataTypeEnum8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetUsertypeResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.UserType);
    }

    public static int zclSize(GetUsertypeResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeEnum8 .zclSize(r.UserType);
        return size;
    }

}
