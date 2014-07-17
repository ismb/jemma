
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetRFIDCodeResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetRFIDCodeResponse {


    public static GetRFIDCodeResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetRFIDCodeResponse r = new GetRFIDCodeResponse();
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.UserStatus = ZclDataTypeUI8 .zclParse(zclFrame);
        r.UserType = ZclDataTypeUI8 .zclParse(zclFrame);
        r.RFIDCode = ZclDataTypeString.zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetRFIDCodeResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.UserStatus);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.UserType);
        ZclDataTypeString.zclSerialize(zclFrame, r.RFIDCode);
    }

    public static int zclSize(GetRFIDCodeResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeUI8 .zclSize(r.UserStatus);
        size += ZclDataTypeUI8 .zclSize(r.UserType);
        size += ZclDataTypeString.zclSize(r.RFIDCode);
        return size;
    }

}
