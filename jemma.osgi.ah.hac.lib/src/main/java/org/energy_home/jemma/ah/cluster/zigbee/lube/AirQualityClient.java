
package org.energy_home.jemma.ah.cluster.zigbee.lube;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface AirQualityClient {

    final static String CMD_ExternalAirQualityIndexNotification_NAME = "ExternalAirQualityIndexNotification";

    public void execExternalAirQualityIndexNotification(short Data, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
