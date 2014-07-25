
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetLogRecordResponse {

    public int LogEntryID;
    public long Timestamp;
    public short EventType;
    public short Source;
    public short EventIDAlarmCode;
    public int UserID;
    public String PIN;

    public GetLogRecordResponse() {
    }

    public GetLogRecordResponse(int LogEntryID, long Timestamp, short EventType, short Source, short EventIDAlarmCode, int UserID, String PIN) {
        this.LogEntryID = LogEntryID;
        this.Timestamp = Timestamp;
        this.EventType = EventType;
        this.Source = Source;
        this.EventIDAlarmCode = EventIDAlarmCode;
        this.UserID = UserID;
        this.PIN = PIN;
    }

}
