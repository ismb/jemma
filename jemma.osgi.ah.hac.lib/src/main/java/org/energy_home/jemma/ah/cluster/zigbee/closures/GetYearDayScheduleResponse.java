
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetYearDayScheduleResponse {

    public short ScheduleID;
    public int UserID;
    public short Status;
    public long ZigBeeLocalStartTime;
    public long ZigBeeLocalEndTime;

    public GetYearDayScheduleResponse() {
    }

    public GetYearDayScheduleResponse(short ScheduleID, int UserID, short Status, long ZigBeeLocalStartTime, long ZigBeeLocalEndTime) {
        this.ScheduleID = ScheduleID;
        this.UserID = UserID;
        this.Status = Status;
        this.ZigBeeLocalStartTime = ZigBeeLocalStartTime;
        this.ZigBeeLocalEndTime = ZigBeeLocalEndTime;
    }

}
