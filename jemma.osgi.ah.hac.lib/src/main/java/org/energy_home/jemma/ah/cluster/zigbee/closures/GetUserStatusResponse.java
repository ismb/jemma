
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetUserStatusResponse {

    public int UserID;
    public short UserStatus;

    public GetUserStatusResponse() {
    }

    public GetUserStatusResponse(int UserID, short UserStatus) {
        this.UserID = UserID;
        this.UserStatus = UserStatus;
    }

}
