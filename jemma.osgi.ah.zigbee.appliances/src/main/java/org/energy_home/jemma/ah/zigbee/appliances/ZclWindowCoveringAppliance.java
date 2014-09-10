package org.energy_home.jemma.ah.zigbee.appliances;

import java.util.Dictionary;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.closures.ZclDoorLockServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.closures.ZclWindowCoveringServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclGroupsServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclScenesServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.metering.ZclSimpleMeteringServer;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZclWindowCoveringAppliance extends ZclAppliance{

	private ZclEndPoint endPoint = null;
	
	private static final Logger LOG = LoggerFactory.getLogger( ZclWindowCoveringAppliance.class );
	
	public ZclWindowCoveringAppliance(String pid, Dictionary config)
			throws ApplianceException {
		super(pid, config);

		endPoint = this.zclAddEndPoint(IEndPointTypes.ZIGBEE_WINDOW_COVERING);

		// Server Clusters
		endPoint.addServiceCluster(new ZclWindowCoveringServer());
		endPoint.addServiceCluster(new ZclBasicServer());
		endPoint.addServiceCluster(new ZclIdentifyServer());
		endPoint.addServiceCluster(new ZclScenesServer());
		endPoint.addServiceCluster(new ZclGroupsServer());
	}
	

	protected void attached() {
		LOG.debug("ZclWindowCoveringAppliance attached");
	}

	protected void detached() {
		LOG.debug("ZclWindowCoveringAppliance detached");
	}


}
