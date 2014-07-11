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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerServiceClusterProxy implements InvocationHandler  {
	
	private static final Logger LOG = LoggerFactory.getLogger(PeerServiceClusterProxy.class);
	
	private IServiceCluster serviceCluster = null;
	private ServiceCluster serviceClusterImpl = null;
	private boolean isPeerServiceCluster = false;
	private IEndPoint endPoint;

	private void initServiceClusterImpl() {
		this.serviceClusterImpl = (serviceCluster instanceof PeerServiceCluster) ? 
				((PeerServiceCluster) serviceCluster).getManagedServiceCluster() :
				(ServiceCluster) serviceCluster;
		this.endPoint = serviceCluster.getEndPoint();
		if (this.serviceCluster instanceof PeerServiceCluster)
			isPeerServiceCluster = true;
	}
	
	public PeerServiceClusterProxy(IServiceCluster serviceCluster) {
		this.serviceCluster = serviceCluster;
		initServiceClusterImpl();
	}
	
	public PeerServiceClusterProxy(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
		this.serviceCluster = new ServiceCluster(clusterInterfaceImpl, clusterInterfaceClass);
		initServiceClusterImpl();
	}

	public ServiceCluster getServiceCluster() {
		return serviceClusterImpl;
	}			
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable { 
		try {
			if (method.getDeclaringClass().equals(IServiceCluster.class)) {				
				return method.invoke(serviceCluster, args);
			} else {
				serviceClusterImpl.checkServiceClusterAvailability();
				if (isPeerServiceCluster)
					args[args.length-1] = ((PeerEndPoint)endPoint).getPeerValidRequestContext((IEndPointRequestContext) args[args.length-1]);
				return method.invoke(serviceClusterImpl.getClusterInterfaceImpl(), args);
			}
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) {
				InvocationTargetException ite = (InvocationTargetException) e;
				Throwable t = ite.getCause();
				if (t != null && t instanceof Exception)
					throw t;
			}
			LOG.debug(e.getMessage(), e);
			throw new ServiceClusterException("Unknown error while executing service cluster request");
		}
	}	

}
