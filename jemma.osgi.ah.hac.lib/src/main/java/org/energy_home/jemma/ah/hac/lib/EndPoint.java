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
package org.energy_home.jemma.ah.hac.lib;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IPeerAppliancesListener;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.energy_home.jemma.ah.hac.lib.ext.PeerAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.PeerEndPoint;
import org.energy_home.jemma.ah.hac.lib.ext.PeerServiceClusterProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@code IEndPoint} interface
 * 
 * @see IEndPoint
 * @see BasicEndPoint
 * 
 */
public class EndPoint extends BasicEndPoint {
	private static final String INVALID_APPLIANCE_OBJECT_MESSAGE = "Invalid appliance object";
	private static final String INVALID_CLUSTER_NAME_MESSAGE = "Invalid cluster name";
	
	private static final String CLUSTER_DEFAULT_IMPL_POSTFIX = "Cluster";
	
	private static final Logger LOG = LoggerFactory.getLogger(EndPoint.class);
	
	IPeerAppliancesListener peerAppliancesListener;
	// Each item is indexed by peerAppliancePid and its value is an IEndPoint List
	HashMap peerAppliances;
	// Added to manage interaction without connections and proxies
	
	protected final synchronized void removeServiceCluster(String clusterName) throws ApplianceException {
		IServiceCluster serviceCluster = getServiceCluster(clusterName);
		if (serviceCluster == null)
			throw new ApplianceException(INVALID_CLUSTER_NAME_MESSAGE);

		if (serviceCluster.getSide() == IServiceCluster.SERVER_SIDE) {
			serverServiceClusters.remove(serviceCluster.getType());
		} else {
			clientServiceClusters.remove(serviceCluster.getType());
		}
		if (serviceCluster instanceof ServiceCluster)
			((ServiceCluster)serviceCluster).setEndPoint(null);
	}
	
	protected final synchronized ServiceCluster addServiceCluster(ServiceCluster serviceCluster, IServiceCluster serviceClusterProxy) throws ApplianceException {
		if (serviceCluster == null)
			throw new ApplianceException(INVALID_CLUSTER_OBJECT_MESSAGE);

		if (serviceCluster.getSide() == IServiceCluster.SERVER_SIDE) {
			serverServiceClusters.put(serviceCluster.getType(), serviceClusterProxy);
		} else {
			clientServiceClusters.put(serviceCluster.getType(), serviceClusterProxy);
		}
		serviceCluster.setEndPoint(this);
		return serviceCluster;
	}
	
	protected final void addClusterListener(String clusterName, IServiceClusterListener listener) throws ApplianceException {
		if (clusterName == null)
			throw new ApplianceException(INVALID_CLUSTER_NAME_MESSAGE);
		String type = HacCommon.getClusterType(clusterName);
		int side = HacCommon.getClusterSide(clusterName);

		if (side == IServiceCluster.SERVER_SIDE) {
			serverClusterListenerTypes.put(type, listener);
		} else {
			clientClusterListenerTypes.put(type, listener);
		}
	}	
	
	void configurationUpdated() {
		if (((Appliance)appliance).appliancesProxy != null)
			((Appliance)appliance).appliancesProxy.notifyConfigurationUpdated(appliance.getPid(), id);
	}
	
	void setId(int id) {
		this.id = id;
	}

	final void updatePeerAppliances(Map pid2AlreadyNotifiedEndPointIds) {
		String appliancePid = getAppliance().getPid();
		if (((Appliance)appliance).appliancesProxy != null) {
			if (pid2AlreadyNotifiedEndPointIds.get(((Appliance)appliance).appliancesProxy.getPid()) == null) {
				((Appliance)appliance).appliancesProxy.notifyAvailabilityUpdated(appliancePid);
				pid2AlreadyNotifiedEndPointIds.put(((Appliance)appliance).appliancesProxy.getPid(), new ArrayList(0));
			}
		}

		for (Iterator it = peerAppliances.values().iterator(); it.hasNext();) {
			PeerAppliance peerAppliance = (PeerAppliance) it.next();
			IEndPoint[] peerEndPoints = peerAppliance.getEndPoints();
			List alreadyNotifiedEndPointIds = (List)pid2AlreadyNotifiedEndPointIds.get(peerAppliance.getPid());
			if (alreadyNotifiedEndPointIds == null) {
				alreadyNotifiedEndPointIds = new ArrayList(1);
				pid2AlreadyNotifiedEndPointIds.put(peerAppliance.getPid(), alreadyNotifiedEndPointIds);
			}
			for (int i = 0; i < peerEndPoints.length; i++) {
				Integer endPointId = new Integer(peerEndPoints[i].getId());
				// A connection on end point 0 is never notified 
				if (endPointId.intValue() != IEndPoint.COMMON_END_POINT_ID && !alreadyNotifiedEndPointIds.contains(endPointId)) {
					((PeerEndPoint)peerEndPoints[i]).getManagedEndPoint().peerApplianceUpdated(appliancePid);
					alreadyNotifiedEndPointIds.add(endPointId);
				}
			}
		}
	}	
	
	void peerApplianceUpdated(String peerAppliancePid) {
		// TODO: add here subscription management code
		if (this.peerAppliancesListener != null) {
			try {
				peerAppliancesListener.notifyPeerApplianceUpdated(peerAppliancePid);
			} catch (Throwable e) {
				LOG.warn("exception returned by notifyPeerApplianceUpdated() for appliance " + peerAppliancePid, e);
			}
		}
	}
	
	void setAppliance(Appliance appliance) throws ApplianceException {
		if (appliance == null)
			throw new ApplianceException(INVALID_APPLIANCE_OBJECT_MESSAGE);
		this.appliance = appliance;
	}
	
	public EndPoint(String type) throws ApplianceException {
		super(type);
		this.peerAppliances = new HashMap();
	}
	
	/**
	 * Method used to register a service cluster listener associated to
	 * asynchronous interaction on all registered clusters for this end point
	 * 
	 * @param serviceClustersListener
	 *            The {@link IServiceClustersListener} interface used for
	 *            notifications
	 */
	public final void registerServiceClustersListener(IServiceClustersListener serviceClustersListener) {
		this.serviceClustersListener = serviceClustersListener;
	}

	/**
	 * Method used to remove a previously registered service cluster listener
	 */
	public final void unregisterServiceClustersListener() {
		this.serviceClustersListener = null;
	}

	public synchronized ServiceCluster addServiceCluster(ServiceCluster serviceCluster) throws ApplianceException {
		if (serviceCluster == null)
			throw new ApplianceException(INVALID_CLUSTER_OBJECT_MESSAGE);

		if (serviceCluster.getSide() == IServiceCluster.SERVER_SIDE) {
			serverServiceClusters.put(serviceCluster.getType(), serviceCluster);
		} else {
			clientServiceClusters.put(serviceCluster.getType(), serviceCluster);
		}
		serviceCluster.setEndPoint(this);
		return serviceCluster;
	}
	
	/**
	 * Method used to register a client or server service cluster whose
	 * attributes/command are implemented by this end point
	 * 
	 * @param clusterName
	 *            The service cluster name (e.g. {@code
	 *            org.energy_home.jemma.ah.zigbee.cluster.general.OnOffServer.class.getName
	 *            ()})
	 * @param clusterImpl
	 *            The object implementing the specific interface (e.g. {@code
	 *            org.energy_home.jemma.ah.zigbee.cluster.general.OnOffServer}
	 *            associated to the registered cluster. Passing
	 *            <code>null</code> as clusterImpl is the same as calling the
	 *            {@link registerCluster}. In this case <code>null</code> is
	 *            returned
	 * @return A {@link IServiceCluster} object created to manage service cluster
	 *         interactions or <code>null</code> if {@code clusterImpl} is
	 *         {@code null}.
	 * @throws ApplianceException
	 *             In case of some errors during the service cluster
	 *             registration (e.g. invalid implementation object)
	 */
	public final ServiceCluster registerCluster(String clusterName, Object clusterImpl) throws ApplianceException {
		Class clusterIf = null;

		if (clusterImpl == null) {
			this.registerCluster(clusterName);
			return null;
		}

		try {
			// TODO: needs to be mapped to a service for instantiation of proprietary services
			clusterIf = Class.forName(clusterName);
		} catch (Exception e) {
			throw new ApplianceValidationException("Invalid cluster interface class");
		}

		if (clusterIf == null)
			throw new ApplianceValidationException("Invalid cluster interface class");

		Method[] methods = clusterIf.getMethods();

		if (clusterImpl == null || !clusterIf.isAssignableFrom(clusterImpl.getClass())) {
			LOG.debug(clusterImpl.getClass().getName() + " must inherit from " + clusterName + " in order to use registerCluster(clusterName, implClass)");
			throw new ApplianceValidationException("Invalid cluster interface class");
		}

//		if (methods == null || methods.length == 0)
//			throw new ApplianceValidationException(
//					"Cluster interface has no method: use method registerCluster(clusterName) to register such a cluster");

		if (ServiceCluster.class.isAssignableFrom(clusterImpl.getClass())) {
			return addServiceCluster((ServiceCluster) clusterImpl);
		} else {
			try {
				PeerServiceClusterProxy serviceClusterHandler = new PeerServiceClusterProxy(clusterImpl, clusterIf);
				ServiceCluster serviceCluster = serviceClusterHandler.getServiceCluster();
				return addServiceCluster(serviceCluster, (IServiceCluster)Proxy.newProxyInstance(clusterIf.getClassLoader(), 
								new Class[] {IServiceCluster.class, clusterIf }, serviceClusterHandler));

			} catch (Exception e) {
				throw new ApplianceValidationException("End point cluster proxy instantiation error " + clusterIf.getClass().getName());
			}
		}
	}

	/**
	 * Method used to register a client or server cluster exposed by this end
	 * point without implementing any associated attributes/commands and no listener.
	 * 
	 * @param clusterName
	 *            The service cluster name (e.g. {@code
	 *            org.energy_home.jemma.ah.zigbee.cluster.general.OnOffServer.class.getName
	 *            ()})
	 * @throws ApplianceException
	 *             In case of some errors during the service cluster
	 *             registration (e.g. invalid implementation object)
	 */
	public final void registerCluster(String clusterName) throws ApplianceException {
		this.addClusterListener(clusterName, null);
	}

	/**
	 * Method used to register a client or server cluster exposed by this end
	 * point without implementing any associated attributes/commands.
	 * 
	 * @param clusterName
	 *            The service cluster name (e.g. {@code
	 *            org.energy_home.jemma.ah.zigbee.cluster.general.OnOffServer.class.getName
	 *            ()})
	 * @param clusterListener
	 * 			  Listener of the specified cluster
	 * @throws ApplianceException
	 *             In case of some errors during the service cluster
	 *             registration (e.g. invalid implementation object)
	 */
	public final void registerClusterListener(String clusterName, IServiceClusterListener listener) throws ApplianceException {
		this.addClusterListener(clusterName, listener);
	}	
	
	/**
	 * Retrieves the list of peer appliances connected to this end point
	 * 
	 * @return An array of {@code IAppliance} interfaces associated to the
	 *         connected peer appliances
	 */
	public final IAppliance[] getPeerAppliances() {
		IAppliance[] peerAppliancesArray = new IAppliance[peerAppliances.size()];
		peerAppliances.values().toArray(peerAppliancesArray);
		return peerAppliancesArray;
	}

	/**
	 * Retrieves a peer appliance connected to this end point by PID
	 * 
	 * @param peerAppliancePid
	 *            The requested peer appliance pid
	 * @return The {@code IAppliance} interface associated to the requested pid,
	 *         {@code null} if the connected peer appliance is not found
	 */
	public final IAppliance getPeerAppliance(String peerAppliancePid) {
		return (IAppliance) peerAppliances.get(peerAppliancePid);
	}
	
	/**
	 * Returns all the connected peer end points
	 * 
	 * @return An array of peer appliances {@link IEndPoint} currently connected
	 *         to this end point, {@code null} in case no connected end points
	 *         are found
	 */
	public final IEndPoint[] getPeerEndPoints() {
		List peerEndPoints = new ArrayList();
		PeerAppliance peerAppliance = null;
		Map endPoints = null;
		for (Iterator iterator = peerAppliances.values().iterator(); iterator.hasNext();) {
			peerAppliance = (PeerAppliance) iterator.next();
			endPoints = peerAppliance.getEndPointsMap();
			for (Iterator iterator2 = endPoints.values().iterator(); iterator2.hasNext();) {
				peerEndPoints.add((IEndPoint) iterator2.next());
			}
		}
		IEndPoint[] peerEndPointsArray = new IEndPoint[peerEndPoints.size()];
		peerEndPoints.toArray(peerEndPointsArray);
		return peerEndPointsArray;
	}

	/**
	 * Returns all the connected peer end points associated to a specific peer
	 * appliance
	 * 
	 * @param peerAppliancePid
	 *            The pid that uniquely identify the peer appliance
	 * @return An array of requested peer appliance {@link IEndPoint} currently
	 *         connected to this end point, {@code null} in case no connected
	 *         end points are found for the specified appliance
	 */
	public final IEndPoint[] getPeerEndPoints(String peerAppliancePid) {
		PeerAppliance peerAppliance = (PeerAppliance) peerAppliances.get(peerAppliancePid);
		if (peerAppliance == null)
			return null;
		Map endPoints = peerAppliance.getEndPointsMap();
		IEndPoint[] endPointsArray = new IEndPoint[endPoints.size()];
		endPoints.values().toArray(endPointsArray);
		return endPointsArray;
	}
	
	/**
	 * Returns a peer appliance end point identified by the specified end point
	 * identifier 
	 * 
	 * @param peerAppliancePid
	 *            The pid that uniquely identify the peer appliance
	 * @param endPointId
	 *            The requested end point identifier
	 * @return The requested peer appliance end point {@link IEndPoint} currently
	 *         connected to this end point, {@code null} in case no connected
	 *         end points are found for the specified appliance
	 */
	public final IEndPoint getPeerEndPoint(String peerAppliancePid, int endPointId) {
		PeerAppliance peerAppliance = (PeerAppliance) peerAppliances.get(peerAppliancePid);
		if (peerAppliance == null)
			return null;
		return (IEndPoint) peerAppliance.getEndPointsMap().get(new Integer(endPointId));
	}

	/**
	 * Returns a list of specific (client or server) service clusters
	 * implemented by any of the peer appliances' end points connected to this
	 * end point
	 * 
	 * @param clusterName
	 *            The name of the requested peer appliances' service cluster
	 * @return An array of requested peer appliances' {@link IServiceCluster}
	 *         whose end points are currently connected to this end point,
	 *         {@code null} in case no connected service clusters are found for
	 *         the specified cluster name
	 */
	public IServiceCluster[] getPeerServiceClusters(String clusterName) {
		List peerServiceClusters = new ArrayList();
		
		if (((Appliance)appliance).appliancesProxy != null) {
			IEndPoint proxyEndPoint = ((Appliance)appliance).appliancesProxy.getEndPoint(DEFAULT_END_POINT_ID);
			IServiceCluster proxyServiceCluster = proxyEndPoint.getServiceCluster(clusterName);
			if (proxyServiceCluster != null)
				peerServiceClusters.add(proxyServiceCluster);
		}

		PeerAppliance peerAppliance = null;
		Map endPoints = null;
		IServiceCluster peerServiceCluster = null;
		for (Iterator iterator = peerAppliances.values().iterator(); iterator.hasNext();) {
			peerAppliance = (PeerAppliance) iterator.next();
			endPoints = peerAppliance.getEndPointsMap();
			for (Iterator iterator2 = endPoints.values().iterator(); iterator2.hasNext();) {
				peerServiceCluster = ((PeerEndPoint) iterator2.next()).getServiceCluster(clusterName);
				if (peerServiceCluster != null)
					peerServiceClusters.add(peerServiceCluster);
			}
		}
		IServiceCluster[] peerServiceClustersArray = new IServiceCluster[peerServiceClusters.size()];
		peerServiceClusters.toArray(peerServiceClustersArray);
		return peerServiceClustersArray;
	}

	/**
	 * Returns a specific (client or server) service cluster implemented by one
	 * of the peer appliances' end points connected to this end point
	 * 
	 * @param clusterName
	 *            The name of the requested peer appliances' service cluster
	 * @return One of the peer appliances' {@link IServiceCluster} whose end
	 *         point is currently connected to this end point, {@code null} in
	 *         case no connected service clusters are found for the specified
	 *         cluster name. In case multiple peer appliances' service clusters
	 *         are found, only one random instance is returned by this method.
	 */
	public final IServiceCluster getPeerServiceCluster(String clusterName) {
		if (((Appliance)appliance).appliancesProxy != null) {
			IEndPoint proxyEndPoint = ((Appliance)appliance).appliancesProxy.getEndPoint(DEFAULT_END_POINT_ID);
			IServiceCluster proxyServiceCluster = proxyEndPoint.getServiceCluster(clusterName);
			if (proxyServiceCluster != null)
				return proxyServiceCluster;
		}
		
		PeerAppliance peerAppliance = null;
		for (Iterator iterator = peerAppliances.values().iterator(); iterator.hasNext();) {
			peerAppliance = (PeerAppliance) iterator.next();
			if (peerAppliance == null)
				continue;	
			Map endPoints = peerAppliance.getEndPointsMap();
			IServiceCluster peerServiceCluster = null;
			for (Iterator iterator2 = endPoints.values().iterator(); iterator2.hasNext();) {
				peerServiceCluster = ((PeerEndPoint) iterator2.next()).getServiceCluster(clusterName);
				if (peerServiceCluster != null)
					return peerServiceCluster;
			}		
		}
		return null;
	}

	/**
	 * Returns a list of specific (client or server) service clusters
	 * implemented by all end points exposed by the specified peer appliance and
	 * connected to this end point
	 * 
	 * @param peerAppliancePid
	 *            The pid identifying the peer appliance
	 * @param clusterName
	 *            The name of the requested peer appliance's service clusters
	 * @return An array of requested peer appliance's {@link IServiceCluster}
	 *         whose end point is currently connected to this end point, {@code
	 *         null} in case no connected service clusters are found for the
	 *         specified appliance and cluster name.
	 */
	public final IServiceCluster[] getPeerServiceClusters(String peerAppliancePid, String clusterName) {
		PeerAppliance peerAppliance = (PeerAppliance) peerAppliances.get(peerAppliancePid);
		if (peerAppliance == null)
			return null;	
		List peerServiceClusters = new ArrayList();
		Map endPoints = peerAppliance.getEndPointsMap();
		IServiceCluster peerServiceCluster = null;
		for (Iterator iterator2 = endPoints.values().iterator(); iterator2.hasNext();) {
			peerServiceCluster = ((PeerEndPoint) iterator2.next()).getServiceCluster(clusterName);
			if (peerServiceCluster != null)
				peerServiceClusters.add(peerServiceCluster);
		}
		IServiceCluster[] peerServiceClustersArray = new IServiceCluster[peerServiceClusters.size()];
		peerServiceClusters.toArray(peerServiceClustersArray);
		return peerServiceClustersArray;
	}

	/**
	 * Returns a specific (client or server) service cluster implemented by one
	 * of the specified peer appliance's end points connected to this end point
	 * 
	 * @param peerAppliancePid
	 *            The pid identifying the peer appliance
	 * @param clusterName
	 *            The name of the requested peer appliance's service cluster
	 * @return One of the peer appliance's {@link IServiceCluster} whose end
	 *         point is currently connected to this end point, {@code null} in
	 *         case no service clusters are found for the specified appliance
	 *         and cluster name. In case multiple peer appliances' service
	 *         clusters are found, only one random instance is returned by this
	 *         method.
	 */
	public final IServiceCluster getPeerServiceCluster(String peerAppliancePid, String clusterName) {
		PeerAppliance peerAppliance = (PeerAppliance) peerAppliances.get(peerAppliancePid);
		if (peerAppliance == null)
			return null;	
		Map endPoints = peerAppliance.getEndPointsMap();
		IServiceCluster peerServiceCluster = null;
		for (Iterator iterator2 = endPoints.values().iterator(); iterator2.hasNext();) {
			peerServiceCluster = ((PeerEndPoint) iterator2.next()).getServiceCluster(clusterName);
			if (peerServiceCluster != null)
				return peerServiceCluster;
		}
		return null;
	}
	
	public final IServiceCluster getPeerServiceCluster(String peerAppliancePid, String clusterName, int endPointId) {
		PeerAppliance peerAppliance = (PeerAppliance) peerAppliances.get(peerAppliancePid);
		if (peerAppliance == null)
			return null;	
		IEndPoint peerEndPoint = peerAppliance.getEndPoint(endPointId);
		if (peerEndPoint != null)
			return peerEndPoint.getServiceCluster(clusterName);
		return null;
	}

	/*
	 * public final Class getServiceClusterInterfaceClass(String clusterType,
	 * int side) { Map serviceClusters = null; String clusterName =
	 * appliance.getApplianceManager().getClusterName(side, clusterType); if
	 * (clusterName == null) return null;
	 * 
	 * if (side == IServiceCluster.CLIENT_SIDE) serviceClusters =
	 * clientServiceClusters; else serviceClusters = serverServiceClusters; try
	 * { IServiceCluster serviceCluster = (IServiceCluster)
	 * serviceClusters.get(clusterType); if (serviceCluster == null) return
	 * null; return Class.forName(DEFAULT_CLUSTER_PACKAGE_PREFIX+clusterName); }
	 * catch (Exception e) { e.printStackTrace(); return null; } }
	 */

	/**
	 * Registers a listener for events associated to connection/disconnection of
	 * peer appliances.
	 * 
	 * @see IPeerAppliancesListener
	 * 
	 */
	public final void registerPeerAppliancesListener(IPeerAppliancesListener peerAppliancesListener) {
		this.peerAppliancesListener = peerAppliancesListener;
	}

	/**
	 * Remove a previously registered peer appliance connection/disconnection
	 * listener
	 * 
	 * @see IPeerAppliancesListener
	 */
	public final void unregisterPeerAppliancesListener() {
		this.peerAppliancesListener = null;
	}	

}
