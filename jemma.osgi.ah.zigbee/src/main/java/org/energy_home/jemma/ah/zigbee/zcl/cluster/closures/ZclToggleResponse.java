
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.ToggleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;

public class ZclToggleResponse {


    public static ToggleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        ToggleResponse r = new ToggleResponse();
        r.Status = ZclDataTypeEnum8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, ToggleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(ToggleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeEnum8 .zclSize(r.Status);
        return size;
    }

}
