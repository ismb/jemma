
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import org.energy_home.jemma.ah.cluster.zigbee.closures.GetLogRecordResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetLogRecordResponse {


    public static GetLogRecordResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        GetLogRecordResponse r = new GetLogRecordResponse();
        r.LogEntryID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.Timestamp = ZclDataTypeUI32 .zclParse(zclFrame);
        r.EventType = ZclDataTypeEnum8 .zclParse(zclFrame);
        r.Source = ZclDataTypeUI8 .zclParse(zclFrame);
        r.EventIDAlarmCode = ZclDataTypeUI8 .zclParse(zclFrame);
        r.UserID = ZclDataTypeUI16 .zclParse(zclFrame);
        r.PIN = ZclDataTypeString.zclParse(zclFrame);
        return r;
    }

    public static void zclSerialize(IZclFrame zclFrame, GetLogRecordResponse r)
        throws ZclValidationException
    {
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.LogEntryID);
        ZclDataTypeUI32 .zclSerialize(zclFrame, r.Timestamp);
        ZclDataTypeEnum8 .zclSerialize(zclFrame, r.EventType);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.Source);
        ZclDataTypeUI8 .zclSerialize(zclFrame, r.EventIDAlarmCode);
        ZclDataTypeUI16 .zclSerialize(zclFrame, r.UserID);
        ZclDataTypeString.zclSerialize(zclFrame, r.PIN);
    }

    public static int zclSize(GetLogRecordResponse r)
        throws ZclValidationException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(r.LogEntryID);
        size += ZclDataTypeUI32 .zclSize(r.Timestamp);
        size += ZclDataTypeEnum8 .zclSize(r.EventType);
        size += ZclDataTypeUI8 .zclSize(r.Source);
        size += ZclDataTypeUI8 .zclSize(r.EventIDAlarmCode);
        size += ZclDataTypeUI16 .zclSize(r.UserID);
        size += ZclDataTypeString.zclSize(r.PIN);
        return size;
    }

}
