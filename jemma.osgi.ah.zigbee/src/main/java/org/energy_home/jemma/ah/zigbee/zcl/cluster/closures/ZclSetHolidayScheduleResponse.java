
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.SetHolidayScheduleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclSetHolidayScheduleResponse {


    public static SetHolidayScheduleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        SetHolidayScheduleResponse r = new SetHolidayScheduleResponse();
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, SetHolidayScheduleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
    }

    public static int zclSize(SetHolidayScheduleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.Status);
        return size;
    }

}
