
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetYearDayScheduleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetYearDayScheduleResponse {


    public static GetYearDayScheduleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetYearDayScheduleResponse r = new GetYearDayScheduleResponse();
        r.ScheduleID = ZclDataTypeUI8 .zclParse(zclFrame);
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        r.ZigBeeLocalStartTime = ZclDataTypeUI32 .zclParse(zclFrame);
        r.ZigBeeLocalEndTime = ZclDataTypeUI32 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetYearDayScheduleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.ScheduleID);
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
        ZclDataTypeUI32 .zclSerialize(zclFrame, r.ZigBeeLocalStartTime);
        ZclDataTypeUI32 .zclSerialize(zclFrame, r.ZigBeeLocalEndTime);
    }

    public static int zclSize(GetYearDayScheduleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.ScheduleID);
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeUI8 .zclSize(r.Status);
        size += ZclDataTypeUI32 .zclSize(r.ZigBeeLocalStartTime);
        size += ZclDataTypeUI32 .zclSize(r.ZigBeeLocalEndTime);
        return size;
    }

}
