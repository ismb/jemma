
package org.energy_home.jemma.ah.cluster.zigbee.lube;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface AirQualityServer {

    final static String ATTR_AirQualityIndex_NAME = "AirQualityIndex";

    public short getAirQualityIndex(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
