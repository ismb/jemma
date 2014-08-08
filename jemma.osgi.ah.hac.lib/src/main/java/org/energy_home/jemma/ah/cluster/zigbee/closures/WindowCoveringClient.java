
package org.energy_home.jemma.ah.cluster.zigbee.closures;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface WindowCoveringClient {

    final static String CMD_UpOpen_NAME = "UpOpen";
    final static String CMD_DownClose_NAME = "DownClose";
    final static String CMD_Stop_NAME = "Stop";
    final static String CMD_GoToLiftValue_NAME = "GoToLiftValue";
    final static String CMD_GoToLiftPercentage_NAME = "GoToLiftPercentage";
    final static String CMD_GoToTiltValue_NAME = "GoToTiltValue";
    final static String CMD_GoToTiltPercentage_NAME = "GoToTiltPercentage";

    public void execUpOpen(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execDownClose(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execStop(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execGoToLiftValue(int LiftValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execGoToLiftPercentage(short PercentageLiftValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execGoToTiltValue(int TiltValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void execGoToTiltPercentage(short PercentageTiltValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
