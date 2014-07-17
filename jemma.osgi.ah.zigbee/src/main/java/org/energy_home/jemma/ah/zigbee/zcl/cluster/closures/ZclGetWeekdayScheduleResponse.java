
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetWeekdayScheduleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetWeekdayScheduleResponse {


    public static GetWeekdayScheduleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetWeekdayScheduleResponse r = new GetWeekdayScheduleResponse();
        r.ScheduleID = ZclDataTypeUI8 .zclParse(zclFrame);
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        r.DaysMask = ZclDataTypeBitmap8 .zclParse(zclFrame);
        r.StartHour = ZclDataTypeUI8 .zclParse(zclFrame);
        r.StartMinute = ZclDataTypeUI8 .zclParse(zclFrame);
        r.EndHour = ZclDataTypeUI8 .zclParse(zclFrame);
        r.EndMinute = ZclDataTypeUI8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetWeekdayScheduleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.ScheduleID);
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
        ZclDataTypeBitmap8 .zclSerialize(zclFrame, r.DaysMask);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.StartHour);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.StartMinute);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.EndHour);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.EndMinute);
    }

    public static int zclSize(GetWeekdayScheduleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.ScheduleID);
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeUI8 .zclSize(r.Status);
        size += ZclDataTypeBitmap8 .zclSize(r.DaysMask);
        size += ZclDataTypeUI8 .zclSize(r.StartHour);
        size += ZclDataTypeUI8 .zclSize(r.StartMinute);
        size += ZclDataTypeUI8 .zclSize(r.EndHour);
        size += ZclDataTypeUI8 .zclSize(r.EndMinute);
        return size;
    }

}
