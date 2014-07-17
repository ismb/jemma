
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetHolidayScheduleResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetHolidayScheduleResponse {


    public static GetHolidayScheduleResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetHolidayScheduleResponse r = new GetHolidayScheduleResponse();
        r.HolidayScheduleID = ZclDataTypeUI8 .zclParse(zclFrame);
        r.Status = ZclDataTypeUI8 .zclParse(zclFrame);
        r.ZigBeeLocalStartTime = ZclDataTypeUI32 .zclParse(zclFrame);
        r.ZigBeeLocalEndTime = ZclDataTypeUI32 .zclParse(zclFrame);
        r.OperatingModeDuringHoliday = ZclDataTypeEnum8 .zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetHolidayScheduleResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.HolidayScheduleID);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Status);
        ZclDataTypeUI32 .zclSerialize(zclFrame, r.ZigBeeLocalStartTime);
        ZclDataTypeUI32 .zclSerialize(zclFrame, r.ZigBeeLocalEndTime);
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.OperatingModeDuringHoliday);
    }

    public static int zclSize(GetHolidayScheduleResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(r.HolidayScheduleID);
        size += ZclDataTypeUI8 .zclSize(r.Status);
        size += ZclDataTypeUI32 .zclSize(r.ZigBeeLocalStartTime);
        size += ZclDataTypeUI32 .zclSize(r.ZigBeeLocalEndTime);
        size += ZclDataTypeEnum8 .zclSize(r.OperatingModeDuringHoliday);
        return size;
    }

}
