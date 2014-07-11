/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2014 Istituto Superiore Mario Boella (http://www.ismb.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.ah.zigbee.appliances;

import java.util.Dictionary;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclLevelControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPowerConfigurationServer;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclEndPoint;
//import org.apache.commons.logging.Log; //// This line is related to Old Logging System
//import org.apache.commons.logging.LogFactory;// This line is related to Old Logging System
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ISMB-Pert
 * bitronhome Remote Control with four buttons

 */
public class ZclBitronhomeRemoteControlAppliance extends ZclAppliance {
	private ZclEndPoint endPoint = null;
	
	// private static final Log log = LogFactory.getLog(ZclBitronhomeRemoteControlAppliance.class); // This line is related to Old Logging System
	private static final Logger LOG = LoggerFactory.getLogger(ZclBitronhomeRemoteControlAppliance.class);

	public ZclBitronhomeRemoteControlAppliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);

		endPoint = this.zclAddEndPoint(IEndPointTypes.ZIGBEE_LEVEL_CONTROLLABLE_OUTPUT);
		
		// Bitronhome SMART-PLUG Clusters
		/**
		 * Endpoint:0x01
			|-------------------------------------------------|
			| Server Side     			|	    Client Side   |			
			|-------------------------------------------------|
			|			   			Mandatory    	          | 			
			| Basic(0x0000)	  			|	Basic(0x0000) 	  |			
			| Identify(0x0003)			|	On/Off(0x0006)	  |			 			
			| 	     None  				|Level Control(0x0008)|			
			|			    		Optional	              |			
			|Power Configuration(0x0001)|		None		  |
			|-------------------------------------------------|

		 */
		
		// Server Clusters
		endPoint.addServiceCluster(new ZclBasicServer()); 	 // adding Basic (0x0000)
		endPoint.addServiceCluster(new ZclIdentifyServer()); // adding Identify (0x0003)		
		endPoint.addServiceCluster(new ZclPowerConfigurationServer()); 	 // adding Power Configuration(0x0001)
		//Client Clusters
		endPoint.addServiceCluster(new ZclBasicClient());  	 // adding Basic (0x0000)
		endPoint.addServiceCluster(new ZclOnOffClient());  	 // adding On/Off (0x0006)
		endPoint.addServiceCluster(new ZclLevelControlClient());  	 // adding Level Control (0x0008)
		
		
		

		
		ConfigServer serviceCluster = (ConfigServer) this.getEndPoint(0).getServiceCluster("org.energy_home.jemma.ah.cluster.ah.ConfigServer");
		if (serviceCluster != null) {
			try {
				if (serviceCluster.getIconName(null) == null) {
					//serviceCluster.setIconName("lampadina.png", null);
				}
			} catch (ServiceClusterException e) {
				
			}
		}
	}

	protected void attached() {
		if (LOG.isDebugEnabled())	
			{ 
				LOG.debug("attached");
			}
	}

	protected void detached() {
		if(LOG.isDebugEnabled()){
			LOG.debug("detached");
		}
		
	}

}