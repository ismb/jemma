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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplianceManager;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceManager;
import org.energy_home.jemma.ah.hac.lib.ext.ConfigServerCluster;
import org.energy_home.jemma.ah.hac.lib.internal.AppliancesProxy;

/**
 * The base class that all the appliances of the A@H framework needs to extend
 * 
 * @see IManagedAppliance
 * 
 */
public class Appliance extends BasicAppliance implements IManagedAppliance {
	
	public static final String APPLIANCE_EPS_TYPES_PROPERTY = "ah.app.eps.types";
	public static final String APPLIANCE_EPS_IDS_PROPERTY = "ah.app.eps.ids";
	
//  In driver mode IManagedAppliance service is unregistered in case of zigbee node disconnection
	private static final String AH_HAC_LIGHT_MODE = "driver";
	private static final String AH_EXECUTION_MODE = System.getProperty("org.energy_home.jemma.ah.hac.mode");
	
	private static boolean isHacDriverModeActive() {
		return AH_HAC_LIGHT_MODE.equals(AH_EXECUTION_MODE);
	}
	
	private class ApplianceManagerImpl extends ApplianceManager {
		/**
		 * Returns the list of (client or server) cluster types implemented by one
		 * of this appliance's end point and matching the list of clusters exposed
		 * by a peer appliance's end point.
		 * <p>
		 * This method is used by the A@H framework to obtain the list of this
		 * appliance's service clusters objects (attributes and commands) to expose
		 * to the peer appliance when a connection is established.
		 * <p>
		 * It has to be re-implemented only if the appliance needs to customize the
		 * matching algorithm used to find the service clusters exposed to peer
		 * appliances through connections created by the A@H framework.
		 *
		 * @param endPointId
		 *            This appliance end point identifier on which the matching
		 *            algorithm has to be executed
		 * @param side
		 *            This appliance end point side (client or server) on which the
		 *            matching algorithm has to be executed
		 * @param peerApplianceDescriptor
		 *            The peer appliance descriptor (it can be used in custom
		 *            matching algorithms, implemented by specific appliance
		 *            classes)
		 * @param peerEndPointType
		 *            The peer end point type
		 * @param peerServiceClusterTypes
		 *            The service cluster types implemented by the peer appliance
		 * @param peerClusterListenerTypes
		 *            The service cluster types exposed by the peer appliance
		 *            without implementing any attributes/commands
		 * @return The list of found matching cluster types, {@code null} in case no
		 *         matching clusters are found.
		 */
		public String[] getMatchingClusterTypes(int endPointId, int side, IApplianceDescriptor peerApplianceDescriptor,
				String peerEndPointType, String[] peerServiceClusterTypes, String[] peerClusterListenerTypes) {
			if (peerServiceClusterTypes == null && peerClusterListenerTypes == null)
				return null;

			ArrayList result = new ArrayList();
			IEndPoint endPoint = getEndPoint(endPointId);
			String[] serviceClusterTypes = endPoint.getServiceClusterTypes(side);
			if (serviceClusterTypes == null)
				return null;

			String clusterType = null;
			for (int i = 0; i < serviceClusterTypes.length; i++) {
				// Match algorithm for peer service clusters
				clusterType = null;
				if (peerServiceClusterTypes != null) {
					for (int j = 0; j < peerServiceClusterTypes.length; j++) {
						if (serviceClusterTypes[i].equals(peerServiceClusterTypes[j])) {
							clusterType = serviceClusterTypes[i];
							break;
						}
						// TODO: add here implementation for matching proprietary
						// clusters
						/*
						 * int indexOfClusterTypePrefix =
						 * serviceClusterTypes[i].indexOf
						 * (HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX); if
						 * (indexOfClusterTypePrefix > 0) subClusterType =
						 * serviceClusterTypes
						 * [i].substring(indexOfClusterTypePrefix);
						 * indexOfClusterTypePrefix =
						 * peerServiceClusterTypes[j].indexOf
						 * (HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX); if
						 * (indexOfClusterTypePrefix > 0) peerSubClusterType =
						 * peerServiceClusterTypes
						 * [j].substring(indexOfClusterTypePrefix); if
						 * (subClusterType != null && peerSubClusterType != null &&
						 * subClusterType.equals(peerSubClusterType)) { clusterType
						 * = subClusterType; break; }
						 */
					}
					if (clusterType != null) {
						try {
							result.add(clusterType);
						} catch (Exception e) {
							LOG.warn(e.getMessage(), e);
						}
					}
				}
				if (clusterType == null && peerClusterListenerTypes != null) {
					// Match algorithm for peer cluster listeners
					for (int j = 0; j < peerClusterListenerTypes.length; j++) {
						if (serviceClusterTypes[i].equals(peerClusterListenerTypes[j])) {
							clusterType = serviceClusterTypes[i];
							break;
						}
						// TODO: add here implementation for matching proprietary
						// clusters
						/*
						 * int indexOfClusterTypePrefix =
						 * serviceClusterTypes[i].indexOf
						 * (HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX); if
						 * (indexOfClusterTypePrefix > 0) subClusterType =
						 * serviceClusterTypes
						 * [i].substring(indexOfClusterTypePrefix);
						 * indexOfClusterTypePrefix =
						 * peerClusterListenerTypes[j].indexOf
						 * (HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX); if
						 * (indexOfClusterTypePrefix > 0) peerSubClusterType =
						 * peerClusterListenerTypes
						 * [j].substring(indexOfClusterTypePrefix); if
						 * (subClusterType != null && peerSubClusterType != null &&
						 * subClusterType.equals(peerSubClusterType)) { clusterType
						 * = subClusterType; break; }
						 */
					}
					if (clusterType != null)
						try {
							result.add(clusterType);
						} catch (Exception e) {
							LOG.warn(e.getMessage(), e);
						}
				}
			}
			if (result.size() > 0) {
				String[] resultArray = new String[result.size()];
				return (String[]) result.toArray(resultArray);
			}
			return null;
		}	
		
		public final void peerApplianceConnected(EndPoint endPoint, IAppliance peerAppliance) {
			if (endPoint.peerAppliancesListener != null) {
				try {
					endPoint.peerAppliancesListener.notifyPeerApplianceConnected(peerAppliance.getPid());
				} catch (Throwable e) {
					LOG.warn("exeption raised by notifyPeerApplianceConnected of appliance " + peerAppliance.getPid(), e);
				}
			}
		}
		
		public final void setAppliancesProxy(AppliancesProxy proxy) {
			Appliance.this.appliancesProxy = proxy;
		}
		
		public final void addPeerAppliance(EndPoint endPoint, IAppliance peerAppliance) {
			String peerAppliancePid = peerAppliance.getPid();
			endPoint.peerAppliances.put(peerAppliancePid, peerAppliance);
		}

		public final void peerApplianceDisconnected(EndPoint endPoint, IAppliance peerAppliance) {
			// TODO: add here subscription management code
			if (endPoint.peerAppliancesListener != null) {
				try {
					endPoint.peerAppliancesListener.notifyPeerApplianceDisconnected(peerAppliance.getPid());
				} catch (Throwable e) {
					LOG.warn("exception returned by notifyPeerApplianceDisconnected() for appliance " + peerAppliance.getPid(), e);
				}
			}
		}
		
		public final void removePeerAppliance(EndPoint endPoint, IAppliance peerAppliance) {
			// TODO: add here subscription management code
			String peerAppliancePid = peerAppliance.getPid();
			endPoint.peerAppliances.remove(peerAppliancePid);
		}
		
		public final Dictionary getCustomConfiguration() {
			return Appliance.this.getCustomConfiguration();
		}	
	}
	
	AppliancesProxy appliancesProxy;
	ApplianceManagerImpl applianceManager = new ApplianceManagerImpl();
	
	private void addDefaultEndPoint(EndPoint defaultEndPoint) throws ApplianceException {
		if (!defaultEndPoint.getType().equals(IEndPoint.COMMON_END_POINT_TYPE))
			throw new ApplianceValidationException("Invalid default cluster type or id");
		defaultEndPoint.setId(IEndPoint.COMMON_END_POINT_ID);

		this.endPoints.put(new Integer(defaultEndPoint.getId()), defaultEndPoint);
		defaultEndPoint.setAppliance(this);
		this.basicServerCluster = null;
		try {
			this.basicServerCluster = (ConfigServerCluster) defaultEndPoint.getServiceCluster(ConfigServer.class.getName());
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
		if (this.basicServerCluster == null) {
			try {
				this.basicServerCluster = new ConfigServerCluster();
				defaultEndPoint.addServiceCluster(basicServerCluster);
			} catch (ApplianceException e) {
				LOG.warn("Configuration cluster not available", e);
			}
		}
	}	
	
	private synchronized void statusUpdated() {
//  In driver mode IManagedAppliance service is unregistered in case of zigbee node disconnection
		if (isDriver && isHacDriverModeActive() && !this.isAvailable) {
			factory.deleteAppliance(getPid(), false);
			return;
		}
		
		EndPoint endPoint = null;
		Map pid2AlreadyNotifiedEndPointIds = new HashMap();
		if (endPoints != null) {
			for (Iterator iterator = endPoints.values().iterator(); iterator.hasNext();) {
				endPoint = (EndPoint) iterator.next();
				// Common end point never changes its status (it is always available) 
				if (endPoint.getId() != IEndPoint.COMMON_END_POINT_ID) {
					endPoint.updatePeerAppliances(pid2AlreadyNotifiedEndPointIds);				
				}
			}			
		}
	}
	
	void setApplianceFactory(ApplianceFactory factory) {
		this.factory = factory;
	}
	
	void updateConfig(Dictionary config) throws ApplianceException {
		try {
			this.configuration = config;
			ConfigServerCluster configServerCluster = null;
			if (config != null) {
				String newName = (String) config.get(IAppliance.APPLIANCE_NAME_PROPERTY);
				String newLocationPid = (String) config.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
				String newCategoryPid = (String) config.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);
				String newIconName = (String) config.get(IAppliance.APPLIANCE_ICON_PROPERTY);
				
				String[] newNames = (String[]) config.get(IAppliance.END_POINT_NAMES_PROPERTY);
				String[] newLocationPids = (String[]) config.get(IAppliance.END_POINT_LOCATION_PIDS_PROPERTY);
				String[] newCategoryPids = (String[]) config.get(IAppliance.END_POINT_CATEGORY_PIDS_PROPERTY);
				String[] newIconNames = (String[]) config.get(IAppliance.END_POINT_ICONS_PROPERTY);
				IEndPoint[] eps = getEndPoints();
				
				if (newNames != null || newLocationPids != null || newCategoryPids != null || newIconNames != null) {
					boolean updateNames = newNames != null && newNames.length == eps.length;
					boolean updateLocationPids = newLocationPids != null && newLocationPids.length == eps.length;
					boolean updateCategoryPids = newCategoryPids != null && newCategoryPids.length == eps.length;
					boolean updateIconNames = newIconNames != null && newIconNames.length == eps.length;
					for (int i = 0; i < eps.length; i++) {
						configServerCluster = (ConfigServerCluster) eps[i].getServiceCluster(ConfigServer.class.getName());
						if (updateNames && newNames[i] != null && !newNames[i].equals(configServerCluster.getConfigName())) {
							configServerCluster.setConfigName(newNames[i]);
						}
						if (updateLocationPids && newLocationPids[i] != null && !newLocationPids[i].equals(configServerCluster.getConfigLocationPid())) {
							configServerCluster.setConfigLocationPid(newLocationPids[i]);
						}
						if (updateCategoryPids && newCategoryPids[i] != null && !newCategoryPids[i].equals(configServerCluster.getConfigCategoryPid())) {
							configServerCluster.setConfigCategoryPid(newCategoryPids[i]);
						}
						if (updateIconNames && newIconNames[i] != null && !newIconNames[i].equals(configServerCluster.getConfigIconName())) {
							configServerCluster.setConfigIconName(newIconNames[i]);
						}
					}
				} else {
					for (int i = 0; i < eps.length; i++) {
						configServerCluster = (ConfigServerCluster) eps[i].getServiceCluster(ConfigServer.class.getName());
						if (newName != null && !newName.equals(configServerCluster.getConfigName())) {
							configServerCluster.setConfigName(newName);
						}
						if (newLocationPid != null && !newLocationPid.equals(configServerCluster.getConfigLocationPid())) {
							configServerCluster.setConfigLocationPid(newLocationPid);
						}
						if (newCategoryPid != null && !newCategoryPid.equals(configServerCluster.getConfigCategoryPid())) {
							configServerCluster.setConfigCategoryPid(newCategoryPid);
						}
						if (newIconName != null && !newIconName.equals(configServerCluster.getConfigIconName())) {
							configServerCluster.setConfigIconName(newIconName);
						}
					}
				}			
				configurationUpdated();
			}
			else {
				LOG.debug("updateConfig called on appliance " + this.pid + " with null config parameter");
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}

	}	
	
	/**
	 * Create an instance of a {@code driver} or {@code logical} appliance
	 * 
	 * @param pid
	 *            The Persistent IDentifier of this appliance
	 * @param config
	 *            A {@code Dictionary} with the initial configuration
	 * @param isDriver
	 *            {@code true} if this is a {@code driver appliance}, {@code
	 *            false} if this is a {@code logical appliance}
	 * @throws ApplianceException
	 *             In case some of the passed parameters are not supported by
	 *             this type of appliance
	 */
	public Appliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);
		this.isDriver = (this instanceof DriverAppliance);
		EndPoint defaultEndPoint = new EndPoint(IEndPoint.COMMON_END_POINT_TYPE);
		this.basicServerCluster = new ConfigServerCluster();
		defaultEndPoint.addServiceCluster((ServiceCluster) basicServerCluster);
		this.addDefaultEndPoint(defaultEndPoint);
	}
	
	protected synchronized void start() {
		try {
			updateConfig(configuration);
		} catch (ApplianceException e) {
			LOG.warn("Error while initializing appliance configuration", e);
		}
		if (!isDriver())
			this.setAvailability(true);
	}

	protected synchronized void stop() {
		if (!isDriver())
			this.setAvailability(false);
	}
	
	protected synchronized void configurationUpdated() {
		LOG.debug("Configuration updated in appliance '" + this.getPid() + "'");
		for (Iterator iterator = endPoints.values().iterator(); iterator.hasNext();) {
			((EndPoint)iterator.next()).configurationUpdated();	
		}
	}
	
	protected synchronized void removeEndPoint(int endPointId) {
		this.endPoints.remove(new Integer(endPointId));
	}
	
	public final void setAvailability(boolean availability) {
		boolean statusUpdateNeeded = false;
		if (this.isAvailable != availability)
			statusUpdateNeeded = true;
		this.isAvailable = availability;
		if (statusUpdateNeeded)
			statusUpdated();
	}	

	/**
	 * Add an end point associated to a specific type
	 * 
	 * @param endPointType
	 *            The type of the end point to be added
	 * @return The created {@link EndPoint} object
	 * @throws ApplianceException
	 *             In case of problems with the specified end point type
	 * 
	 * @see IEndPoint
	 */
	public synchronized final EndPoint addEndPoint(String endPointType) throws ApplianceException {
		return addEndPoint(new EndPoint(endPointType));
	}	
	
	public synchronized final EndPoint addEndPoint(EndPoint endPoint) throws ApplianceException {
		int id = this.endPoints.size();
		return addEndPoint(endPoint, id);
	}
	
	public synchronized final EndPoint addEndPoint(EndPoint endPoint, int id) throws ApplianceException {
		endPoint.setId(id);
		if (endPoint == null || endPoint.getType() == IEndPoint.COMMON_END_POINT_TYPE)
			throw new ApplianceValidationException(INVALID_END_POINT_MESSAGE);
		this.endPoints.put(new Integer(endPoint.getId()), endPoint);
		endPoint.setAppliance(this);

		try {
			endPoint.addServiceCluster(new ConfigServerCluster());
		} catch (ApplianceException e) {
			LOG.warn("Configuration cluster not available", e);
		}
		return endPoint;
	}	

	public ILocation[] getLocations() throws ApplianceValidationException {
		return applianceManager.getLocations();
	}

	public ILocation getLocation(String pid) throws ApplianceValidationException {
		return applianceManager.getLocation(pid);
	}

	public ICategory[] getCategories() throws ApplianceValidationException {
		return applianceManager.getCategories();
	}

	public ICategory getCategory(String pid) throws ApplianceValidationException {
		return applianceManager.getCategory(pid);
	}
	
	/**
	 * This method is invoked by the A@H framework when the {@code
	 * IManagedAppliance} interface is registered or when some configuration
	 * parameters are modified by the framework.
	 * 
	 * @param config
	 *            A {@code Dictionary} with all the configuration parameters
	 * 
	 * @throws ApplianceException
	 *             In case some of the passed configuration parameters are wrong
	 */	
	public final String[] getPeerAppliancesPids() {	
		Set peerPids = new HashSet();
		IAppliance[] peerAppliances = null;
		for (Iterator iterator = endPoints.values().iterator(); iterator.hasNext();) {
			peerAppliances = ((EndPoint)iterator.next()).getPeerAppliances();
			if (peerAppliances != null)
				for (int i = 0; i < peerAppliances.length; i++) {
					peerPids.add(peerAppliances[i].getPid());
				}		
		}
		String[] result = new String[peerPids.size()];
		return (String[]) peerPids.toArray(result);
		
	}

	public final String toString() {
		return getPid();
	}
	
	//**** IManagedAppliance methods
	
	public final IApplianceManager getApplianceManager() {
		return applianceManager;
	}
	
}
