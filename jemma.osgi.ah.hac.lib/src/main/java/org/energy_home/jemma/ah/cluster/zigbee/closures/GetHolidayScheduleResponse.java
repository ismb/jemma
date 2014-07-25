
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetHolidayScheduleResponse {

    public short HolidayScheduleID;
    public short Status;
    public long ZigBeeLocalStartTime;
    public long ZigBeeLocalEndTime;
    public short OperatingModeDuringHoliday;

    public GetHolidayScheduleResponse() {
    }

    public GetHolidayScheduleResponse(short HolidayScheduleID, short Status, long ZigBeeLocalStartTime, long ZigBeeLocalEndTime, short OperatingModeDuringHoliday) {
        this.HolidayScheduleID = HolidayScheduleID;
        this.Status = Status;
        this.ZigBeeLocalStartTime = ZigBeeLocalStartTime;
        this.ZigBeeLocalEndTime = ZigBeeLocalEndTime;
        this.OperatingModeDuringHoliday = OperatingModeDuringHoliday;
    }

}
