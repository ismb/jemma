
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.UnlockDoorResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;

public class ZclUnlockDoorResponse {


    public static UnlockDoorResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        UnlockDoorResponse r = new UnlockDoorResponse();
        r.Status = ZclDataTypeEnum8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, UnlockDoorResponse r)
        throws ZclValidationException
    {
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(UnlockDoorResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeEnum8 .zclSize(r.Status);
        return size;
    }

}
