/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.zgd.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayFactory;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.GatewayProperties;
import org.energy_home.jemma.zgd.Trace;
import org.energy_home.jemma.zgd.jaxb.*;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;

public class VerySimpleClient implements APSMessageListener, GatewayEventListener {
	GatewayFactory factory;
	GatewayInterface gateway;
	short localEndpoint;
	Address lastNodeAddress;
	boolean useNVMNetworkSetting = true;
	
	
	public static void main(String args[]) {
		Trace.setTrace(new Trace());
		new VerySimpleClient();
	}
	
	public VerySimpleClient() {
		GatewayProperties prop = new GatewayProperties();
		try {
			factory = GatewayFactory.getInstance(prop);
			gateway = factory.createGatewayObject();
			gateway.setGatewayEventListener(this);
			
						
			localEndpoint = 1;
			// start default init sequence
			startDefaultInitSequence();
			

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	void startDefaultInitSequence() throws Exception {
		// start discovery announcement;
		gateway.startNodeDiscovery(0, GatewayConstants.DISCOVERY_ANNOUNCEMENTS);
		
		// subscribe liveness;
		gateway.subscribeNodeRemoval(0, GatewayConstants.DISCOVERY_FRESHNESS | GatewayConstants.DISCOVERY_LEAVE);
		
		// confugure local endpoint
		/*(gateway.clearEndpoint((short) localEndpoint);
	    SimpleDescriptor sd = new SimpleDescriptor();
	    sd.setEndPoint(new Short(localEndpoint));
	    sd.setApplicationDeviceIdentifier(new Integer(0x0500)); // ESP
	    sd.setApplicationProfileIdentifier(new Integer(0x0104)); // ESP
	    List inputClusters = sd.getApplicationOutputCluster();
	    inputClusters.add(new Integer(ZclIdentifyClient.CLUSTER_ID));
	    inputClusters.add(new Integer(ZclSimpleMeteringClient.CLUSTER_ID));
	 
	    localEndpoint = gateway.configureEndpoint(100, sd);
		
		// create APS callback
		long callbackId = gateway.createAPSCallback(localEndpoint, this);
		
		// start discovery announcement;
		gateway.startNodeDiscovery(0, GatewayConstants.DISCOVERY_ANNOUNCEMENTS);
		
		// subscribe liveness;
		gateway.subscribeNodeRemoval(0, GatewayConstants.DISCOVERY_FRESHNESS);*/
		
		if (useNVMNetworkSetting) {
			gateway.resetDongle(0, GatewayConstants.RESET_USE_NVMEMORY);
		} else {
			// start gateway device
			dongleResetResult(new Status());
    		//gateway.startGatewayDevice(0);
		}
	}
	
	void sendAPSMessage(short endpoint) throws Exception {
    	APSMessage msg = new APSMessage();
    	msg.setDestinationAddressMode(GatewayConstants.EXTENDED_ADDRESS_MODE);
    	msg.setDestinationAddress(lastNodeAddress);
     	msg.setDestinationEndpoint(endpoint);
    	msg.setSourceEndpoint(localEndpoint);
    	msg.setClusterID(0x900);
    	msg.setProfileID(0x107);
    	byte[] data = new byte[] {0x10, 1, 0, 0, 0};
    	msg.setData(data);
    
    	TxOptions tx = new TxOptions();
    	tx.setAcknowledged(false);
    	tx.setPermitFragmentation(false);
    	tx.setSecurityEnabled(false);
    	tx.setUseNetworkKey(false);
    	msg.setTxOptions(tx);
    	msg.setRadius((short)10);

    	Trace.println("Sending APS Message");
    	Trace.printf("Destination IEEE Address: (%x)\n", msg.getDestinationAddress().getIeeeAddress());
    	Trace.printf("Destination Endpoint: %x\n", msg.getDestinationEndpoint());
    	Trace.printf("Cluster ID: %x\n", msg.getClusterID());
    	Trace.printf("Profile ID: %x\n", msg.getProfileID());
    	Trace.printf("Source Endpoint: %x\n", msg.getSourceEndpoint());
    	Trace.printf("Radius: %d\n", msg.getRadius());
    	Trace.printf("TxOptions Acknowledged: %b\n", msg.getTxOptions().isAcknowledged());
    	Trace.printf("TxOptions Permit Fragmentation: %b\n", msg.getTxOptions().isPermitFragmentation());
    	Trace.printf("TxOptions Security Enabled: %b\n", msg.getTxOptions().isSecurityEnabled());
    	Trace.printf("TxOptions Use Network Key: %b\n", msg.getTxOptions().isUseNetworkKey());
		data = msg.getData();
		Trace.print("Data Packet: ");
		for (byte b : data) Trace.printf("%02x ", b);
		Trace.println("");
		
		gateway.sendAPSMessage(msg);
	}

	
	
	public void notifyAPSMessage(APSMessageEvent msg) {
		Trace.println("Received APS message from GAL");
		Trace.printf("APS Status: (%x)\n", msg.getAPSStatus());
		Trace.printf("Cluster ID: (%x)\n", msg.getClusterID());
		Trace.printf("Destination Network Address: (%x)\n", msg.getDestinationAddress().getNetworkAddress());
		Trace.printf("Destination Endpoint: (%x)\n", msg.getDestinationEndpoint());
		Trace.printf("Profile ID: (%x)\n", msg.getProfileID());
		Trace.printf("Security Status: %s\n", msg.getSecurityStatus().value());
		Trace.printf("Source Network Address: (%x)\n", msg.getSourceAddress().getNetworkAddress());
		Trace.printf("Source IEEE Address: (%x)\n", msg.getSourceAddress().getIeeeAddress());
		Trace.printf("Source Endpoint: (%x)\n", msg.getSourceEndpoint());
		byte[] data = msg.getData();
		for (byte b : data) Trace.printf("%02x ", b);
		Trace.println("");
	}

	
	public void gatewayStartResult(Status status) {
		Trace.println("Gateway Started notification from GAL " + status.getCode());
	}

	
	public void nodeDiscovered(Status s, WSNNode node) {
		Trace.println("Node Discovered notification from GAL");
		Address a = node.getAddress();
		Trace.printf("Alias Address: %s\n", a.getAliasAddress());
		Trace.printf("Extended Address: (%x)\n", a.getIeeeAddress());
		Trace.printf("Short Address: (%x)\n", a.getNetworkAddress());
		this.lastNodeAddress = a;
		
		a = node.getParentAddress();
		if (a != null) {
			Trace.printf("Parent Node Full Address: (%x)\n", a.getIeeeAddress());
			Trace.printf("Parent Node Short Address: (%x)\n", a.getNetworkAddress());
		}
		Trace.printf("Start Index: %d\n", node.getStartIndex());
		List<AssociatedDevices> list = node.getAssociatedDevices();
		for (AssociatedDevices d : list) {
			Trace.printf("Total number: %d", d.getTotalNumber());
			List<SonNode> sons = d.getSonNode();
			for (SonNode i : sons) {
				Trace.printf("Son Node Short Address: %d\n", i.getShortAddr());
			}
		}
		
		// now retrieve the active endpoints
		if (lastNodeAddress.getNetworkAddress() > 0) { // skip the coordinator
    		try {
    			gateway.startServiceDiscovery(0, lastNodeAddress);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
		}
	}
	
	public void nodeRemoved(Status s, WSNNode node) {
		Trace.println("Node Removed notification from GAL");
		Address a = node.getAddress();
		Trace.printf("Alias Address: %s\n", a.getAliasAddress());
		Trace.printf("Extended Address: (%x)\n", a.getIeeeAddress());
		Trace.printf("Short Address: (%x)\n", a.getNetworkAddress());
		this.lastNodeAddress = a;
	}

	public void servicesDiscovered(Status s, NodeServices services) {
		Trace.println("Services Discoverd notification from GAL");
		Address a = services.getAddress();
		Trace.printf("Alias Address: %s\n", a.getAliasAddress());
		Trace.printf("Extended Address: (%x)\n", a.getIeeeAddress());
		Trace.printf("Short Address: (%x)\n", a.getNetworkAddress());

		List<ActiveEndpoints> list = services.getActiveEndpoints();
		for (ActiveEndpoints aep : list) {
			Trace.printf("End Point: (%x)\n", aep.getEndPoint());
		}
		
		try {
			// retrieve the simple descriptor of the 1st endpoint
			gateway.getServiceDescriptor(0, a, list.get(0).getEndPoint());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	
	public void serviceDescriptorRetrieved(Status s, ServiceDescriptor service) {
		Trace.println("Service Descriptor notification from GAL");
		Address a = service.getAddress();
		Trace.printf("Alias Address: %s\n", a.getAliasAddress());
		Trace.printf("Extended Address: (%x)\n", a.getIeeeAddress());
		Trace.printf("Short Address: (%x)\n", a.getNetworkAddress());

		// should contain exactly 1 element in the list
		Trace.printf("End Point: (%x)\n", service.getEndPoint());
		
		SimpleDescriptor sd = service.getSimpleDescriptor();
		Trace.printf("Application Device Identifier: (%x)\n", sd.getApplicationDeviceIdentifier());
		Trace.printf("Application Device Version: (%x)\n", sd.getApplicationDeviceVersion());
		Trace.printf("Application Profile Identifier: (%x)\n", sd.getApplicationProfileIdentifier());
		Trace.printf("Input Clusters: ");
		List<Integer> clus = sd.getApplicationInputCluster();
		for (int c : clus) {
			Trace.printf("%04x ", c);
		}
		Trace.printf("\nOutput Clusters: ");
		clus = sd.getApplicationOutputCluster();
		for (int c : clus) {
			Trace.printf("%04x ", c);
		}
		Trace.println("");
		
		
		// now wait a bit and send a message:
		try {
			Thread.sleep(5000);
			sendAPSMessage(service.getEndPoint());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void leaveResult(Status status) {
		// TODO Auto-generated method stub
		
	}

	public void permitJoinResult(Status status) {
		// TODO Auto-generated method stub
		
	}

	public void nodeDescriptorRetrieved(Status status, NodeDescriptor node) {
		// TODO Auto-generated method stub		
	}

	public void dongleResetResult(Status status) {
		// TODO Auto-generated method stub
		if (status.getCode() == GatewayConstants.SUCCESS) {
    		// start gateway device
    		try {
    			// confugure local endpoint
    			gateway.clearEndpoint(localEndpoint);
    		    
    			SimpleDescriptor sd = new SimpleDescriptor();
    		    sd.setEndPoint(new Short(localEndpoint));
    		    sd.setApplicationDeviceIdentifier(new Integer(0x0050)); // ESP
    		    sd.setApplicationProfileIdentifier(new Integer(0x0104)); // ESP
    		    List inputClusters = sd.getApplicationOutputCluster();
    		    //inputClusters.add(new Integer(ZclIdentifyClient.CLUSTER_ID));
    		    //inputClusters.add(new Integer(ZclSimpleMeteringClient.CLUSTER_ID));
				// TODO power profile client 
    		    // appliance control client
    		    // appliance identification client
    		    // meter identification client
    		    // meter server
    		    // time server
    		    // partitioning .....
    			long callbackId = gateway.createAPSCallback(localEndpoint, this);

    		    gateway.startGatewayDevice(0);
    		    
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void bindingResult(Status status) {
	}

	public void unbindingResult(Status status) {
	}

	public void nodeBindingsRetrieved(Status status, BindingList bindings) {
	}
}
