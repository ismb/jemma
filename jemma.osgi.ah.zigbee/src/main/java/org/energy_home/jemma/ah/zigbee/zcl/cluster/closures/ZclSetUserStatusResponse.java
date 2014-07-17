
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.SetUserStatusResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclSetUserStatusResponse {


    public static SetUserStatusResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        SetUserStatusResponse r = new SetUserStatusResponse();
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, SetUserStatusResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(SetUserStatusResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.Status);
        return size;
    }

}
