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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.EndPointRequestContext;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;

/**
 * Basic implementation of {@link IEndPoint} interface
 * 
 */
public class BasicEndPoint implements IApplicationEndPoint {
	static final int INVALID_END_POINT_ID = -1;
	protected static final String INVALID_CLUSTER_OBJECT_MESSAGE = "Invalid cluster object";

	private EndPointRequestContext endPointRequestContext = new EndPointRequestContext(this);
	private EndPointRequestContext endPointRequestContextNoConfirmation = new EndPointRequestContext(this, false, 0);
	String type;
	protected IAppliance appliance;
	protected int id;
	protected Map clientServiceClusters;
	protected Map serverServiceClusters;
	protected Map clientClusterListenerTypes;
	protected Map serverClusterListenerTypes;
	protected IServiceClustersListener serviceClustersListener;

	public BasicEndPoint(String type) {
		this.type = type;
		this.id = INVALID_END_POINT_ID;
		this.serverServiceClusters = new HashMap();
		this.clientServiceClusters = new HashMap();
		this.serverClusterListenerTypes = new HashMap();
		this.clientClusterListenerTypes = new HashMap();
		this.serviceClustersListener = null;
	}
	
	public final IEndPointRequestContext getDefaultRequestContext() {
			return this.endPointRequestContext;
	}
	
	public final IEndPointRequestContext getDefaultRequestContext(boolean isConfirmationRequired) {
		if (isConfirmationRequired)
			return this.endPointRequestContext;
		else 
			return this.endPointRequestContextNoConfirmation;
	}
	
	public IEndPointRequestContext getRequestContext(boolean isConfirmationRequired, long maxAgeForAttributeValues) {
		return new EndPointRequestContext(this, isConfirmationRequired, maxAgeForAttributeValues);
	}
	
	public final IEndPointRequestContext getValidRequestContext(IEndPointRequestContext endPointRequestContext) throws ServiceClusterException {
		if (endPointRequestContext == null)
			return this.endPointRequestContext;
		IEndPoint requestedEndPoint = endPointRequestContext.getPeerEndPoint();
		if (appliance.getPid() != requestedEndPoint.getAppliance().getPid() ||
				getId() != requestedEndPoint.getId())
			throw new ServiceClusterException("Invalid end point request context");
		return endPointRequestContext;
	}
	
	public IAppliance getAppliance() {
		return this.appliance;
	}

	public int getId() {
		return this.id;
	}

	public final String getType() {
		return this.type;
	}

	public boolean isAvailable() {
		return this.appliance.isAvailable() || this.id == IEndPoint.COMMON_END_POINT_ID;
	}
	
	public String[] getServiceClusterNames() {
		ArrayList clusterNamesList = new ArrayList(clientServiceClusters.size() + serverServiceClusters.size());
		for (Iterator iterator = clientServiceClusters.keySet().iterator(); iterator.hasNext();) {
			clusterNamesList.add(((String) iterator.next()) + HacCommon.CLUSTER_NAME_CLIENT_POSTFIX);
		}
		for (Iterator iterator = serverServiceClusters.keySet().iterator(); iterator.hasNext();) {
			clusterNamesList.add(((String) iterator.next()) + HacCommon.CLUSTER_NAME_SERVER_POSTFIX);
		}
		if (clusterNamesList.size() == 0)
			return null;
		else {
			String[] clusterNamesArray = new String[clusterNamesList.size()];
			return (String[]) clusterNamesList.toArray(clusterNamesArray);
		}
	}

	public IServiceCluster[] getServiceClusters(int clusterSide) {
		IServiceCluster[] clusterTypes = null;
		int i = 0;
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			clusterTypes = new IServiceCluster[clientServiceClusters.size()];
			for (Iterator iterator = clientServiceClusters.values().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (IServiceCluster) iterator.next();
			break;
		case IServiceCluster.SERVER_SIDE:
			clusterTypes = new IServiceCluster[serverServiceClusters.size()];
			for (Iterator iterator = serverServiceClusters.values().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (IServiceCluster) iterator.next();
			break;
		}
		return clusterTypes;
	}
	
	public IServiceCluster[] getServiceClusters() {
		IServiceCluster[] clusterTypes = new IServiceCluster[serverServiceClusters.size()+clientServiceClusters.size()];
		int i = 0;
		for (Iterator iterator = clientServiceClusters.values().iterator(); iterator.hasNext();)
			clusterTypes[i++] = (IServiceCluster) iterator.next();
		for (Iterator iterator = serverServiceClusters.values().iterator(); iterator.hasNext();)
			clusterTypes[i++] = (IServiceCluster) iterator.next();
		return clusterTypes;
	}
	
	public String[] getServiceClusterTypes(int clusterSide) {
		String[] clusterTypes = null;
		int i = 0;
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			clusterTypes = new String[clientServiceClusters.size()];
			for (Iterator iterator = clientServiceClusters.keySet().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (String) iterator.next();
			break;
		case IServiceCluster.SERVER_SIDE:
			clusterTypes = new String[serverServiceClusters.size()];
			for (Iterator iterator = serverServiceClusters.keySet().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (String) iterator.next();
			break;
		}
		return clusterTypes;
	}

	public String[] getAdditionalClusterNames() {
		ArrayList clusterNamesList = new ArrayList(clientServiceClusters.size() + serverServiceClusters.size());
		for (Iterator iterator = clientClusterListenerTypes.keySet().iterator(); iterator.hasNext();) {
			clusterNamesList.add(((String) iterator.next()) + HacCommon.CLUSTER_NAME_CLIENT_POSTFIX);
		}
		for (Iterator iterator = serverClusterListenerTypes.keySet().iterator(); iterator.hasNext();) {
			clusterNamesList.add(((String) iterator.next()) + HacCommon.CLUSTER_NAME_SERVER_POSTFIX);
		}
		if (clusterNamesList.size() == 0)
			return null;
		else {
			String[] clusterNamesArray = new String[clusterNamesList.size()];
			return (String[]) clusterNamesList.toArray(clusterNamesArray);
		}
	}

	public String[] getAdditionalClusterTypes(int clusterSide) {
		String[] clusterTypes = null;
		int i = 0;
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			clusterTypes = new String[clientClusterListenerTypes.size()];
			for (Iterator iterator = clientClusterListenerTypes.keySet().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (String) iterator.next();
			break;
		case IServiceCluster.SERVER_SIDE:
			clusterTypes = new String[serverClusterListenerTypes.size()];
			for (Iterator iterator = serverClusterListenerTypes.keySet().iterator(); iterator.hasNext();)
				clusterTypes[i++] = (String) iterator.next();
			break;
		}
		return clusterTypes;
	}

	private IServiceCluster getServiceCluster(String clusterType, int clusterSide) {
		IServiceCluster serviceCluster = null;
		// TODO: extended cluster types need to be tested
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			serviceCluster = (IServiceCluster) clientServiceClusters.get(clusterType);
			/*
			 * if
			 * (clusterType.startsWith(HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX
			 * )) { String extendedClusterType = null; int indexOfClusterPrefix;
			 * for (Iterator iterator =
			 * clientServiceClusters.keySet().iterator(); iterator.hasNext();) {
			 * extendedClusterType = (String) iterator.next();
			 * indexOfClusterPrefix =
			 * extendedClusterType.indexOf(HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX
			 * ); if (indexOfClusterPrefix > 0 &&
			 * extendedClusterType.substring(indexOfClusterPrefix
			 * ).equals(clusterType)) { return (IServiceCluster)
			 * clientServiceClusters.get(extendedClusterType); } } }
			 */
			break;
		case IServiceCluster.SERVER_SIDE:
			serviceCluster = (IServiceCluster) serverServiceClusters.get(clusterType);
			/*
			 * if
			 * (clusterType.startsWith(HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX
			 * )) { String extendedClusterType = null; int indexOfClusterPrefix;
			 * for (Iterator iterator =
			 * serverServiceClusters.keySet().iterator(); iterator.hasNext();) {
			 * extendedClusterType = (String) iterator.next();
			 * indexOfClusterPrefix =
			 * extendedClusterType.indexOf(HacCommon.AH_DEFAULT_CLUSTER_TYPE_PREFIX
			 * ); if (indexOfClusterPrefix > 0 &&
			 * extendedClusterType.substring(indexOfClusterPrefix
			 * ).equals(clusterType)) { return (IServiceCluster)
			 * serverServiceClusters.get(extendedClusterType); } } }
			 */
			break;
		}
		return serviceCluster;
	}
	
	public IServiceClusterListener getServiceClusterListener(String clusterName) {
		int clusterSide = HacCommon.getClusterSide(clusterName);
		String clusterType = HacCommon.getClusterType(clusterName);
		IServiceClusterListener serviceClusterListener = null;
		IServiceCluster serviceCluster = getServiceCluster(clusterType, clusterSide);
		if (serviceCluster != null && serviceCluster instanceof IServiceClusterListener)
			return (IServiceClusterListener)serviceCluster;
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			serviceClusterListener = (IServiceClusterListener) clientClusterListenerTypes.get(clusterType);
			break;
		case IServiceCluster.SERVER_SIDE:
			serviceClusterListener = (IServiceClusterListener) serverClusterListenerTypes.get(clusterType);
			break;
		}
		return serviceClusterListener;
	}

	public IServiceCluster getServiceCluster(String clusterName) {
		int side = HacCommon.getClusterSide(clusterName);
		String type = HacCommon.getClusterType(clusterName);
		return getServiceCluster(type, side);
	}

	public IServiceClustersListener getServiceClustersListener() {
		return this.serviceClustersListener;
	}
	

}
