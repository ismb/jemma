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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceClusterProxy implements InvocationHandler  {
	
	private static final Logger LOG = LoggerFactory.getLogger(ServiceClusterProxy.class);
	
	private static final String APPLICATION_NOT_READY = "No service cluster available: application not ready";
	
	private static final String GET_END_POINT_CLUSTER_METHOD = "getEndPoint";
	private static final String GET_NAME_CLUSTER_METHOD = "getName";
	private static final String GET_TYPE_CLUSTER_METHOD = "getType";
	private static final String GET_SIDE_CLUSTER_METHOD = "getSide";

	private AppliancesBasicProxy appliancesProxy;
	private Class clusterInterfaceClass;
	private String clusterName, clusterType;
	private int clusterSide;
	private IEndPointRequestContextCheck endPointRequestContextCheck;
	
	public ServiceClusterProxy(AppliancesBasicProxy appliancesProxy, Class clusterInterfaceClass, IEndPointRequestContextCheck endPointRequestContextCheck) throws ApplianceException {
		this.appliancesProxy = appliancesProxy;
		this.clusterInterfaceClass = clusterInterfaceClass;
		this.clusterName = clusterInterfaceClass.getName();
		this.clusterType = HacCommon.getClusterType(clusterName);
		this.clusterSide = HacCommon.getClusterSide(clusterName);
		this.endPointRequestContextCheck = endPointRequestContextCheck;			
	}
	
	public ServiceCluster getFirstServiceCluster() {
		IEndPoint[] endPoints = appliancesProxy.getEndPoints();
		ServiceCluster sc = null;
		// First proxy end point is not included
		for (int i = 2; i < endPoints.length; i++) {
			sc = (ServiceCluster) endPoints[i].getServiceCluster(clusterName);
			if (sc != null) {
				break;
			}
		}
		return sc;
	}
	
//	public ServiceCluster getBestAvailableServiceCluster() {	
//		IEndPoint[] endPoints = appliancesProxy.getEndPoints();
//		ServiceCluster sc = null;
//		// First proxy end point is not included
//		for (int i = 2; i < endPoints.length; i++) {
//			sc = (ServiceCluster) endPoints[i].getServiceCluster(clusterName);
//			if (sc != null && sc.isAvailable() ) {
//				break;
//			}
//		}
//		return sc;
//	}
	
	public ServiceCluster[] getAvailableServiceClusters() {		
		List scList = new ArrayList(1);	
		IEndPoint[] endPoints = appliancesProxy.getEndPoints();
		ServiceCluster sc = null;
		// First proxy end point is not included
		for (int i = 2; i < endPoints.length; i++) {
			sc = (ServiceCluster) endPoints[i].getServiceCluster(clusterName);
			if (sc != null && sc.isAvailable()) {
				scList.add(sc);
			}
		}
		ServiceCluster[] scArray = new ServiceCluster[scList.size()];
		scList.toArray(scArray);
		return scArray;
	}
	
	public Class getClusterInterfaceClass() {
		return clusterInterfaceClass;
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		if (method.getName().equals(GET_END_POINT_CLUSTER_METHOD)) {
			result = getEndPoint();
		} else if (method.getName().equals(GET_NAME_CLUSTER_METHOD)) {
			result = getName();
		} else if (method.getName().equals(GET_TYPE_CLUSTER_METHOD)) {
			result = getType();
		} else if (method.getName().equals(GET_SIDE_CLUSTER_METHOD)) {
			result = getSide();
		} else {
			IEndPointRequestContext endPointRequestContext = null;
			if (endPointRequestContextCheck != null && args != null && args.length > 0
					&& args[args.length - 1] instanceof IEndPointRequestContext)
			try {
				endPointRequestContextCheck.checkRequestContext((IEndPointRequestContext) args[args.length - 1]);
			} catch (NotAuthorized e) {
				if (!method.getDeclaringClass().equals(IServiceClusterListener.class))
					throw e;
				else 
					// Not authorized exception are not raised for service cluster listener methods
					return null;
			}

			boolean multipleCalls = method.getReturnType().equals(Void.TYPE);
			ServiceCluster[] serviceClusters = getAvailableServiceClusters();
			for (int i = 0; i < serviceClusters.length; i++) {
				ServiceCluster sc = serviceClusters[i];
				try {
					if (result == null)
						result = method.invoke(sc, args);
					else
						method.invoke(sc, args);
					if (!multipleCalls)
						break;
				} catch (Throwable e) {
					if (e instanceof InvocationTargetException) {
						InvocationTargetException ite = (InvocationTargetException) e;
						Throwable t = ite.getCause();
						if (t != null && t instanceof Exception)
							throw t;
					}
					LOG.debug(e.getMessage(), e);
					throw new ServiceClusterException("Unknown error while executing application service cluster request");
				}
			}
		}
		return result;
	}


	public IEndPoint getEndPoint() {
		return appliancesProxy.getEndPoint(IEndPoint.DEFAULT_END_POINT_ID);
	}

	public String getName() {
		return clusterName;
	}

	public String getType() {
		return clusterType;
	}

	public int getSide() {
		return clusterSide;
	}
	
//	public boolean isAvailable() {
//		ServiceCluster sc = getBestAvailableServiceCluster();
//		if (sc != null)
//			return true;
//		else
//			return false;
//	}
//		
//	public boolean isEmpty() {
//		//TODO:!!! Check if it is correct
//		return clusterInterfaceClass.getMethods().length == 0;
//	}
//	
//	public ISubscriptionParameters getAttributeSubscription(String attributeName, IEndPointRequestContext endPointRequestContext)
//			throws ApplianceException, ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.getAttributeSubscription(attributeName, endPointRequestContext);
//	}
//
//	public ISubscriptionParameters setAttributeSubscription(String attributeName, ISubscriptionParameters parameters,
//			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.setAttributeSubscription(attributeName, parameters, endPointRequestContext);
//	}
//
//	public Map getAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
//			ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.getAllSubscriptions(endPointRequestContext);
//	}
//
//	public void removeAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
//			ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		serviceCluster.removeAllSubscriptions(endPointRequestContext);
//	}
//
//	public IAttributeValue getLastNotifiedAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)
//			throws ApplianceException, ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.getLastNotifiedAttributeValue(attributeName, endPointRequestContext);
//	}
//
//	public IAttributeValue getAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)
//			throws ApplianceException, ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.getAttributeValue(attributeName, endPointRequestContext);
//	}
//
//	public IAttributeValue setAttributeValue(String attributeName, Object attributeValue,
//			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.setAttributeValue(attributeName, attributeValue, endPointRequestContext);
//	}
//
//	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
//			ServiceClusterException {
//		ServiceCluster serviceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(serviceCluster, endPointRequestContext);
//		return serviceCluster.getSupportedAttributeNames(endPointRequestContext);
//	}
//	
//	public Object execCommand(String commandName, Object[] parameters, IEndPointRequestContext endPointRequestContext)
//			throws ApplianceException, ServiceClusterException {
//		ServiceCluster bestServiceCluster = getBestAvailableServiceCluster();
//		checkSeviceClusterAndContext(bestServiceCluster, endPointRequestContext);
//		Object result = bestServiceCluster.execCommand(commandName, parameters, endPointRequestContext);
//		Method m = bestServiceCluster.getMethod(commandName);
//		if (m.getReturnType() == null) {
//			ServiceCluster[] serviceClusters = getAllAvailableServiceClusters();
//			for (int i = 0; i < serviceClusters.length; i++) {
//				ServiceCluster sc = serviceClusters[i];
//				if (sc != bestServiceCluster) {
//					try {
//						checkSeviceClusterAndContext(sc, endPointRequestContext);
//						sc.execCommand(commandName, parameters, endPointRequestContext);
//					} catch (Exception e) {						
//					}
//				}
//			}
//		}
//		return result;
//	}
//
//	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue,
//			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
//		ServiceCluster[] serviceClusters = getAllAvailableServiceClusters();
//		for (int i = 0; i < serviceClusters.length; i++) {
//			ServiceCluster sc = serviceClusters[i];
//			checkSeviceClusterAndContext(sc, endPointRequestContext);
//			sc.notifyAttributeValue(attributeName, attributeValue);			
//		}		
//	}	
}
