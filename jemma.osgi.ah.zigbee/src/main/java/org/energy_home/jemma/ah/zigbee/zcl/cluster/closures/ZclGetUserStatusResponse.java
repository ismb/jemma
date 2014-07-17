
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetUserStatusResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetUserStatusResponse {


    public static GetUserStatusResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetUserStatusResponse r = new GetUserStatusResponse();
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.UserStatus = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetUserStatusResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.UserStatus);
    }

    public static int zclSize(GetUserStatusResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeUI8 .zclSize(r.UserStatus);
        return size;
    }

}
