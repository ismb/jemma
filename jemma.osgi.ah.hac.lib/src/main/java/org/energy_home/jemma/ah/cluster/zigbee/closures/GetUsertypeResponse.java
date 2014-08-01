
package org.energy_home.jemma.ah.cluster.zigbee.closures;


public class GetUsertypeResponse {

    public int UserID;
    public short UserType;

    public GetUsertypeResponse() {
    }

    public GetUsertypeResponse(int UserID, short UserType) {
        this.UserID = UserID;
        this.UserType = UserType;
    }

}
