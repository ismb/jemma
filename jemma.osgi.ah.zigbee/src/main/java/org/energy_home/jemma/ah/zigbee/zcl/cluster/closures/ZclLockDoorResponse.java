
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.LockDoorResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;

public class ZclLockDoorResponse {


    public static LockDoorResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        LockDoorResponse r = new LockDoorResponse();
        r.Status = ZclDataTypeEnum8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, LockDoorResponse r)
        throws ZclValidationException
    {
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(LockDoorResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeEnum8 .zclSize(r.Status);
        return size;
    }

}
