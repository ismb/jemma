
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.SetYearDayScheduleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclSetYearDayScheduleResponse {


    public static SetYearDayScheduleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        SetYearDayScheduleResponse r = new SetYearDayScheduleResponse();
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, SetYearDayScheduleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(SetYearDayScheduleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.Status);
        return size;
    }

}
