
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetRFIDCodeResponse {

    public int UserID;
    public short UserStatus;
    public short UserType;
    public String RFIDCode;

    public GetRFIDCodeResponse() {
    }

    public GetRFIDCodeResponse(int UserID, short UserStatus, short UserType, String RFIDCode) {
        this.UserID = UserID;
        this.UserStatus = UserStatus;
        this.UserType = UserType;
        this.RFIDCode = RFIDCode;
    }

}
