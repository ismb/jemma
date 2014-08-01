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
package org.energy_home.jemma.ah.hac.lib.internal;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class EndPointProxy extends EndPoint implements IEndPoint {
	private Map serviceClusterProxyMap = new HashMap();
	
	public EndPointProxy(String type) throws ApplianceException {
		super(type);
	}		
	
	public void removeServiceCluster(ServiceCluster serviceCluster) throws ApplianceException {
		String clusterName = serviceCluster.getName();
		super.removeServiceCluster(clusterName);
	}
	
	public ServiceClusterProxy getServiceClusterProxy(String clusterName) {
		return (ServiceClusterProxy) serviceClusterProxyMap.get(clusterName);
	}
	
	public synchronized void addServiceClusterProxy(ServiceClusterProxy serviceClusterProxy) throws ApplianceException {
		if (serviceClusterProxy == null || getServiceCluster(serviceClusterProxy.getName()) != null)
			throw new ApplianceException(INVALID_CLUSTER_OBJECT_MESSAGE);
		Class clusterIf = serviceClusterProxy.getClusterInterfaceClass();
		IServiceCluster serviceCluster = (IServiceCluster)Proxy.newProxyInstance(clusterIf.getClassLoader(), 
				new Class[] {IServiceCluster.class, IServiceClusterListener.class, clusterIf}, serviceClusterProxy);
		if (serviceClusterProxy.getSide() == IServiceCluster.SERVER_SIDE) {
			serverServiceClusters.put(serviceClusterProxy.getType(), serviceCluster);
		} else {
			clientServiceClusters.put(serviceClusterProxy.getType(), serviceCluster);
		}
		serviceClusterProxyMap.put(serviceClusterProxy.getName(), serviceClusterProxy);
	}
	
	public synchronized void checkAndRemoveEmptyServiceClusterProxy(String clusterName) throws ApplianceException {
		ServiceClusterProxy serviceClusterProxy = (ServiceClusterProxy)serviceClusterProxyMap.get(clusterName);
		if (serviceClusterProxy != null) {
			if (serviceClusterProxy.getFirstServiceCluster() == null) {
				removeServiceCluster(clusterName);
				serviceClusterProxyMap.remove(clusterName);
			}
		} else
			return;
	}
	
}