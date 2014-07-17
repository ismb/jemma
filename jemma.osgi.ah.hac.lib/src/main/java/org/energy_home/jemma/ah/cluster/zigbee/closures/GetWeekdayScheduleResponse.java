
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetWeekdayScheduleResponse {

    public short ScheduleID;
    public int UserID;
    public short Status;
    public short DaysMask;
    public short StartHour;
    public short StartMinute;
    public short EndHour;
    public short EndMinute;

    public GetWeekdayScheduleResponse() {
    }

    public GetWeekdayScheduleResponse(short ScheduleID, int UserID, short Status, short DaysMask, short StartHour, short StartMinute, short EndHour, short EndMinute) {
        this.ScheduleID = ScheduleID;
        this.UserID = UserID;
        this.Status = Status;
        this.DaysMask = DaysMask;
        this.StartHour = StartHour;
        this.StartMinute = StartMinute;
        this.EndHour = EndHour;
        this.EndMinute = EndMinute;
    }

}
