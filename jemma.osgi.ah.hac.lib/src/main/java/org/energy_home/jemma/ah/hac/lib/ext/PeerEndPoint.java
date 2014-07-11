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
package org.energy_home.jemma.ah.hac.lib.ext;

import java.lang.reflect.Proxy;

import org.energy_home.jemma.ah.cluster.ah.ConfigClient;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.InvalidPeerApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.BasicEndPoint;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class PeerEndPoint extends BasicEndPoint implements IEndPoint {
	private static final String INVALID_APPLIANCE_OBJECT_MESSAGE = "Invalid appliance object";
	private static final String INVALID_CLUSTER_NAME_ERROR_MESSAGE = "Invalid cluster name error";
	
	private EndPoint managedEndPoint;
	// Corresponding managed peer end point (used to verify IEndPointRequestContext in notification)
	private EndPointRequestContext managedEndPointRequestContext = null;
	
	protected boolean containsOnlyCommonClustersListeners = true;
	
	private synchronized void addServiceCluster(PeerServiceCluster serviceCluster, IServiceCluster serviceClusterProxy) throws ApplianceException {
		if (serviceCluster == null)
			throw new ApplianceException(INVALID_CLUSTER_OBJECT_MESSAGE);

		if (serviceCluster.getSide() == IServiceCluster.SERVER_SIDE) {
			serverServiceClusters.put(serviceCluster.getType(), serviceClusterProxy);
		} else {
			clientServiceClusters.put(serviceCluster.getType(), serviceClusterProxy);
		}
	}
	
	final void setPeerAppliance(PeerAppliance peerAppliance) throws ApplianceException {
		if (peerAppliance == null)
			throw new ApplianceException(INVALID_APPLIANCE_OBJECT_MESSAGE);
		this.appliance = peerAppliance;
	}
	
	public PeerEndPoint(EndPoint endPoint) {
		super(endPoint.getType());
		this.id = endPoint.getId();
		this.managedEndPoint = endPoint;
	}

//	public Map getServiceClustersMap(int side) {
//	switch (side) {
//	case IServiceCluster.SERVER_SIDE:
//		return this.serverServiceClusters; 
//	case IServiceCluster.CLIENT_SIDE:
//		return this.clientServiceClusters; 
//	default:
//		return null;
//	}
//}	
	
	public EndPoint getManagedEndPoint() {
		return managedEndPoint;
	}
	
	/****** Methods used by the hac service or the peer appliance container class ******/
	public final void registerClusterListener(String clusterName) throws ApplianceException {
		if (clusterName == null)
			throw new ApplianceException(INVALID_CLUSTER_NAME_ERROR_MESSAGE);
		if (!clusterName.equals(ConfigClient.class.getName()))
			containsOnlyCommonClustersListeners = false;
		String type = HacCommon.getClusterType(clusterName);
		int side = HacCommon.getClusterSide(clusterName);

		if (side == IServiceCluster.SERVER_SIDE) {
			serverClusterListenerTypes.put(type, null);
		} else {
			clientClusterListenerTypes.put(type, null);
		}
	}
	
	public IEndPointRequestContext getPeerDefaultRequestContext() throws ApplianceException, ServiceClusterException {
		return getPeerValidRequestContext(null);
	}
	
	public IEndPointRequestContext getPeerValidRequestContext(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		PeerAppliance peerAppliance = (PeerAppliance)appliance;
		if (!peerAppliance.isPeerValid())
			throw new InvalidPeerApplianceException();
		synchronized (this) {
			if (managedEndPointRequestContext == null || managedEndPointRequestContext.getPeerEndPoint() == null) {
				EndPoint lep = peerAppliance.getLinkedEndPoint();
				managedEndPointRequestContext = new EndPointRequestContext(managedEndPoint.getPeerEndPoint(lep.getAppliance().getPid(), lep.getId()));
			}
		}
		if (endPointRequestContext == null || 
				(endPointRequestContext.getMaxAgeForAttributeValues() == 0 && endPointRequestContext.isConfirmationRequired() == true))
			return managedEndPointRequestContext;
		return new EndPointRequestContext(managedEndPointRequestContext.getPeerEndPoint(), 
				endPointRequestContext.isConfirmationRequired(), endPointRequestContext.getMaxAgeForAttributeValues());	
	}
	
	public final void registerCluster(ServiceCluster serviceCluster) throws ApplianceException {
		Class clusterIf = serviceCluster.getClusterInterfaceClass();
		try {
			if (!serviceCluster.getName().equals(ConfigClient.class.getName()))
				containsOnlyCommonClustersListeners = false;
			PeerServiceCluster peerServiceCluster = new PeerServiceCluster(serviceCluster, this);			
			PeerServiceClusterProxy serviceClusterHandler = new PeerServiceClusterProxy(peerServiceCluster);
			addServiceCluster(peerServiceCluster, (IServiceCluster)Proxy.newProxyInstance(clusterIf.getClassLoader(), 
							new Class[] {IServiceCluster.class, clusterIf }, serviceClusterHandler));
		} catch (Exception e) {
			throw new ApplianceValidationException("End point cluster proxy instantiation error " + clusterIf.getClass().getName());
		}
	}
	
	/****** IEndPoint ******/
	public IAppliance getAppliance() {
		return appliance;
	}

	public int getId() {
		return managedEndPoint.getId();
	}

	public boolean isAvailable() {
		return this.managedEndPoint.isAvailable() && ((PeerAppliance)appliance).isPeerValid();
	}

	public IServiceClustersListener getServiceClustersListener() {
		return managedEndPoint.getServiceClustersListener();
	}
	
	public IServiceClusterListener getServiceClusterListener(String clusterName) {
		return managedEndPoint.getServiceClusterListener(clusterName); 
	}

}
