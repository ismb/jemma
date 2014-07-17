
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.ClearRFIDCodeResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclClearRFIDCodeResponse {


    public static ClearRFIDCodeResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        ClearRFIDCodeResponse r = new ClearRFIDCodeResponse();
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, ClearRFIDCodeResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(ClearRFIDCodeResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.Status);
        return size;
    }

}
