
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.ClearAllRFIDCodesResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclClearAllRFIDCodesResponse {


    public static ClearAllRFIDCodesResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        ClearAllRFIDCodesResponse r = new ClearAllRFIDCodesResponse();
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, ClearAllRFIDCodesResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(ClearAllRFIDCodesResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.Status);
        return size;
    }

}
