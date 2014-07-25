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
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class PeerServiceCluster implements IServiceCluster, IServiceClusterListener {

	private ServiceCluster managedServiceCluster;
	private IServiceCluster serviceClusterProxy;
	private PeerEndPoint peerEndPoint = null;
	
	protected ServiceCluster getManagedServiceCluster() {
		return managedServiceCluster;	
	}
	
	protected IServiceCluster getServiceCluster() {
		return serviceClusterProxy;		
	}
	
	public PeerServiceCluster(ServiceCluster managedServiceCluster, PeerEndPoint peerEndPoint) throws ApplianceException {
		this.managedServiceCluster = managedServiceCluster;
		this.peerEndPoint = peerEndPoint;
		Class clusterIf = managedServiceCluster.getClusterInterfaceClass();
		PeerServiceClusterProxy serviceClusterHandler = new PeerServiceClusterProxy(this);
		serviceClusterProxy = (IServiceCluster)Proxy.newProxyInstance(clusterIf.getClassLoader(), 
						new Class[] {IServiceCluster.class, clusterIf }, serviceClusterHandler);
	}
	
	public IEndPoint getEndPoint() {
		return peerEndPoint;
	}

	public String getName() {
		return managedServiceCluster.getName();
	}

	public String getType() {
		return managedServiceCluster.getType();
	}

	public int getSide() {
		return managedServiceCluster.getSide();
	}

	public boolean isEmpty() {
		return managedServiceCluster.isEmpty();
	}
	
	public boolean isAvailable() {
		return managedServiceCluster.isAvailable() && ((PeerAppliance)peerEndPoint.getAppliance()).isPeerValid();
	}

	public ISubscriptionParameters getAttributeSubscription(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.getAttributeSubscription(attributeName, endPointRequestContext);
	}

	public ISubscriptionParameters setAttributeSubscription(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.setAttributeSubscription(attributeName, parameters, endPointRequestContext);
	}
	
	public Map getAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
			ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.getAllSubscriptions(endPointRequestContext);
	}
	
	public void removeAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		managedServiceCluster.removeAllSubscriptions(endPointRequestContext);
	}
	public IAttributeValue getLastNotifiedAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.getLastNotifiedAttributeValue(attributeName, endPointRequestContext);
	}
	
	public IAttributeValue getAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.getAttributeValue(attributeName, endPointRequestContext);
	}
	
	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.getSupportedAttributeNames(endPointRequestContext);
	}

	public IAttributeValue setAttributeValue(String attributeName, Object attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.setAttributeValue(attributeName, attributeValue, endPointRequestContext);
	}

	public Object execCommand(String commandName, Object[] parameters, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		return managedServiceCluster.execCommand(commandName, parameters, endPointRequestContext);
	}

	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		endPointRequestContext = peerEndPoint.getPeerValidRequestContext(endPointRequestContext);
		managedServiceCluster.notifyAttributeValue(attributeName, attributeValue, endPointRequestContext);
	}
}
