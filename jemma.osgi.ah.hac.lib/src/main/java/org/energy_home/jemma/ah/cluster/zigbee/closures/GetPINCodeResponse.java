
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetPINCodeResponse {

    public int UserID;
    public short UserStatus;
    public short UserType;
    public String Code;

    public GetPINCodeResponse() {
    }

    public GetPINCodeResponse(int UserID, short UserStatus, short UserType, String Code) {
        this.UserID = UserID;
        this.UserStatus = UserStatus;
        this.UserType = UserType;
        this.Code = Code;
    }

}
