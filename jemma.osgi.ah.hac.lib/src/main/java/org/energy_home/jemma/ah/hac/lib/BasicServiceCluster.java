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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.InvalidPeerApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.energy_home.jemma.ah.hac.lib.ext.PeerEndPoint;

/**
 * Basic implementation of {@link IServiceCluster} interface
 * 
 */
public class BasicServiceCluster implements IServiceCluster, IServiceClusterListener {
	static final String APPLIANCE_INVALID_OR_NOT_AVAILABLE = "Appliance invalid or not available";
	static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error";	
	static final String CLUSTER_PACKAGE_NAME_PREFIX = "it.telecomitalia.";
	static final String INVALID_CLUSTER_CLASS_MESSAGE = "Invalid cluster class";	
	static final ISubscriptionParameters SUBSCRIPTION_PARAMETERS = new SubscriptionParameters();
	
	String name = null;
	String type = null;
	int side;
	boolean isEmpty = false;

	HashMap subscriptions = new HashMap();
	HashMap lastNotifiedAttributeValues = new HashMap();
	Object clusterInterfaceImpl;
	Class clusterInterfaceClass;
	Map getterMethods;
	Map setterMethods;
	Map execMethods;
	Map gettersMethodNameToAttributeName;
	Map settersMethodNameToAttributeName;
	Map execsMethodNameToCommandName;	
	
	protected IAppliance appliance;
	protected EndPoint endPoint;

	private ISubscriptionParameters internalGetAttributeSubscription(String attributeName, IEndPointRequestContext endPointRequestContext) {
		HashMap attributeSubscriptions = (HashMap) subscriptions.get(attributeName);
		if (attributeSubscriptions == null)
			return null;
		else {
			ISubscriptionParameters params = null;
			if (endPointRequestContext == null) {
				for (Iterator iterator = attributeSubscriptions.values().iterator(); iterator.hasNext();) {
					params = (ISubscriptionParameters) iterator.next();
					break;
				}
			} else {
				return (ISubscriptionParameters) attributeSubscriptions.get(endPointRequestContext.getPeerEndPoint());
			}
			return params;
		}
	}
	
	protected final void init(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
		if (clusterInterfaceImpl == null)
			throw new ApplianceException(INVALID_CLUSTER_CLASS_MESSAGE);
		this.clusterInterfaceImpl = clusterInterfaceImpl;
		this.clusterInterfaceClass = clusterInterfaceClass;
		String name = clusterInterfaceClass.getName();
		if (name.endsWith(HacCommon.CLUSTER_NAME_CLIENT_POSTFIX)) {
			this.name = name;
			this.side = IServiceCluster.CLIENT_SIDE;
			this.type = this.name.substring(0, this.name.indexOf(HacCommon.CLUSTER_NAME_CLIENT_POSTFIX));
		} else if (name.endsWith(HacCommon.CLUSTER_NAME_SERVER_POSTFIX)) {
			this.name = name;
			this.side = IServiceCluster.SERVER_SIDE;
			this.type = name.substring(0, name.indexOf(HacCommon.CLUSTER_NAME_SERVER_POSTFIX));
		} else 
			throw new ApplianceException(INVALID_CLUSTER_CLASS_MESSAGE);

		this.getterMethods = new HashMap();
		this.setterMethods = new HashMap();
		this.execMethods = new HashMap();

		this.gettersMethodNameToAttributeName = new HashMap();
		this.settersMethodNameToAttributeName = new HashMap();
		this.execsMethodNameToCommandName = new HashMap();

		Method[] clusterMethod = clusterInterfaceClass.getMethods();
		if (clusterMethod == null || clusterMethod.length == 0) {
			isEmpty = true;
			return;
		}
		String key = null;
		for (int i = 0; i < clusterMethod.length; i++) {
			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_ATTRIBUTE_GETTER_PREFIX)) {
				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_ATTRIBUTE_GETTER_PREFIX.length());
				this.getterMethods.put(key, clusterMethod[i]);
				this.gettersMethodNameToAttributeName.put(clusterMethod[i].getName(), key);
			}
//			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_ATTRIBUTE_SELECT_PREFIX)) {
//				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_ATTRIBUTE_SELECT_PREFIX.length());
//				this.selectMethods.put(key, clusterMethod[i]);
//				this.selectMethodNameToAttributeName.put(clusterMethod[i].getName(), key);
//			} else 
			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_ATTRIBUTE_SETTER_PREFIX)) {
				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_ATTRIBUTE_SETTER_PREFIX.length());
				this.setterMethods.put(key, clusterMethod[i]);
				this.settersMethodNameToAttributeName.put(clusterMethod[i].getName(), key);
			}
//			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_ATTRIBUTE_PUT_PREFIX)) {
//				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_ATTRIBUTE_PUT_PREFIX.length());
//				this.putMethods.put(key, clusterMethod[i]);
//				this.putMethodNameToAttributeName.put(clusterMethod[i].getName(), key);
//			} else 
			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_COMMAND_PREFIX)) {
				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_COMMAND_PREFIX.length());
				this.execMethods.put(key, clusterMethod[i]);
				this.execsMethodNameToCommandName.put(clusterMethod[i].getName(), key);
			} else {
				// TODO: check when continue statement is needed
				// throw new
				// ApplianceException(INVALID_CONNECTED_CLUSTER_CLASS); NICOLA
				continue;
			}
		}
	}	
	
	protected void updateSubscriptionMap(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) {
		synchronized (subscriptions) {
			// TODO: currently no check is made on the validity of the attribute
			// name
			HashMap attributeSubscriptions = (HashMap) subscriptions.get(attributeName);
			if (attributeSubscriptions != null && parameters == null) {
				attributeSubscriptions.remove(endPointRequestContext.getPeerEndPoint());
				return;
			}
			if (attributeSubscriptions == null) {
				attributeSubscriptions = new HashMap();
				subscriptions.put(attributeName, attributeSubscriptions);
			}
			if (parameters != null) {
				attributeSubscriptions.put(endPointRequestContext.getPeerEndPoint(), parameters);
			}
		}
	}

	protected void updateAllSubscriptionMap(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) {
		synchronized (subscriptions) {
			// TODO: currently no check is made on the validity of the attribute
			// name
			HashMap attributeSubscriptions = (HashMap) subscriptions.get(attributeName);
			if (attributeSubscriptions != null && parameters == null) {
				subscriptions.remove(attributeName);
				return;
			}
			if (attributeSubscriptions == null) {
				attributeSubscriptions = new HashMap();
				subscriptions.put(attributeName, attributeSubscriptions);
			} else {
				for (Iterator iterator = attributeSubscriptions.keySet().iterator(); iterator.hasNext();)
					attributeSubscriptions.put(iterator.next(), parameters);
			}
			if (parameters != null)
				attributeSubscriptions.put(endPointRequestContext.getPeerEndPoint(), parameters);		
		}

	}
	
	public BasicServiceCluster(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
		init(clusterInterfaceImpl, clusterInterfaceClass);
	}
	
	public BasicServiceCluster() throws ApplianceException {
		this.endPoint = null;

		Class clazz = this.getClass();
		Class[] ifs = null;
		Class clusterInterfaceClass = null;
		String name = null;
		boolean initializationOK = false;

		while (!clazz.equals(Object.class)) {
			ifs = clazz.getInterfaces();
			if (ifs != null ) {
				try {
					for (int i = 0; i < ifs.length; i++) {
						name = ifs[i].getName();
						/*
						 * if (name.startsWith(CLUSTER_PACKAGE_NAME_PREFIX)) name =
						 * name.substring(CLUSTER_PACKAGE_NAME_PREFIX.length());
						 */
						if (name.endsWith(HacCommon.CLUSTER_NAME_CLIENT_POSTFIX)) {
							if (initializationOK) {
								initializationOK = false;
								break;
							}
							initializationOK = true;
							clusterInterfaceClass = ifs[i];
						} else if (name.endsWith(HacCommon.CLUSTER_NAME_SERVER_POSTFIX)) {
							if (initializationOK) {
								initializationOK = false;
								break;
							}
							initializationOK = true;
							clusterInterfaceClass = ifs[i];
						}
					}
		
				} catch (Exception e) {
					e.printStackTrace();
					throw new ApplianceException(INVALID_CLUSTER_CLASS_MESSAGE);
				}
			}
			if (!initializationOK)
				clazz = clazz.getSuperclass();
			else 
				break;
		}
		if (!initializationOK)
			throw new ApplianceException(INVALID_CLUSTER_CLASS_MESSAGE);
		init(this, clusterInterfaceClass);
	}
	
	public void checkServiceClusterAvailability() throws ApplianceException {
		if (!isAvailable())
			throw new ApplianceException(APPLIANCE_INVALID_OR_NOT_AVAILABLE);
	}
	
	// ****** IServiceCluster ******/

	public final IEndPoint getEndPoint() {
		return this.endPoint;
	}

	public final String getName() {
		return this.name;
	}

	public final String getType() {
		return this.type;
	}

	public final int getSide() {
		return this.side;
	}

	public boolean isEmpty() {
		return isEmpty;
	}
	
	public boolean isAvailable() {
		if (endPoint == null)
			return true;
		// TODO: it needs to be extended to other common clusters
		return this.endPoint.isAvailable() || (this.appliance.isValid() && name.equals(ConfigServer.class.getName()));
	}
	
	public ISubscriptionParameters getAttributeSubscription(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		return internalGetAttributeSubscription(attributeName, endPointRequestContext);
	}

	public ISubscriptionParameters setAttributeSubscription(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		checkServiceClusterAvailability();
		if (parameters != null) {
			// Virtual appliance now notifies all changes to an attribute (no
			// min or max timeout is supported)
			updateSubscriptionMap(attributeName, SUBSCRIPTION_PARAMETERS, endPointRequestContext);
			// A null value is created to store the initial subscription time
			AttributeValue av = new AttributeValue(null);
			lastNotifiedAttributeValues.put(attributeName, av);
			return SUBSCRIPTION_PARAMETERS;
		} else {
			updateSubscriptionMap(attributeName, null, endPointRequestContext);
			return null;
		}

	}

	public void removeAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException{
		if (endPointRequestContext == null)
			return;
		synchronized (subscriptions) {
			for (Iterator iterator = subscriptions.values().iterator(); iterator.hasNext();) {
				HashMap subscriptions = (HashMap) iterator.next();
				for (Iterator iterator2 = subscriptions.keySet().iterator(); iterator2.hasNext();) {
					IEndPoint endPoint = (IEndPoint) iterator2.next();
					if (endPoint.equals(endPointRequestContext.getPeerEndPoint()))
						iterator.remove();
				}		
			}
		} 
	}
	
	public Map getAllSubscriptions(IEndPointRequestContext endPointRequestContext) {
		HashMap result = null;
		synchronized (subscriptions) {
			for (Iterator iterator = subscriptions.entrySet().iterator(); iterator.hasNext();) {
				Entry entryMap = (Entry) iterator.next();
				String attributeName = (String) entryMap.getKey();
				HashMap subscriptionList = (HashMap) entryMap.getValue();
				for (Iterator iterator2 = subscriptionList.entrySet().iterator(); iterator2.hasNext();) {
					Entry entry = (Entry) iterator2.next();
					IEndPoint endPoint = (IEndPoint) entry.getKey();
					if (endPointRequestContext == null || endPoint.equals(endPointRequestContext.getPeerEndPoint())) {
						if (result == null)
							result = new HashMap();
						result.put(attributeName, entry.getValue());
					}
				}		
			}
		}
		return result;
	}
	
	// Returns the last notified attribute value (doesn't read anything on the device)
	public final IAttributeValue getLastNotifiedAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext) {
		if (endPointRequestContext != null && internalGetAttributeSubscription(attributeName, endPointRequestContext) == null)
			return null;
		return (IAttributeValue) lastNotifiedAttributeValues.get(attributeName);
	}
	
	// ****** Method exposed to specific clusters ******/


	public final void notifyAttributeValue(String attributeName, IAttributeValue attributeValue) {
		lastNotifiedAttributeValues.put(attributeName, attributeValue);
		HashMap attributeSubscriptions = (HashMap) subscriptions.get(attributeName);
		EndPoint endPoint = (EndPoint)this.getEndPoint();
		IEndPoint peerEndPoint = null;
		if (attributeSubscriptions != null) {
			if (attributeValue != null && attributeValue.getTimestamp() == IAttributeValue.NO_TIMESTAMP)
				((AttributeValue) attributeValue).setTimestamp(System.currentTimeMillis());
			try {
				// TODO: first implementation notifies all changes independently of
				// subscription parameters
				for (Iterator iterator = attributeSubscriptions.keySet().iterator(); iterator.hasNext();) {
					peerEndPoint = (IEndPoint) iterator.next();
					if (!peerEndPoint.getAppliance().isValid()) {
						// Remove subscription if connection has been removed
						iterator.remove();
					} else {
						try {
							IServiceClustersListener allClustersListener = null;
							String peerClusterName = HacCommon.getPeerClusterName(this.getName());
							IServiceClusterListener	serviceClusterListener = null;
							if (peerEndPoint instanceof PeerEndPoint) {
								allClustersListener = ((PeerEndPoint)peerEndPoint).getServiceClustersListener();
								serviceClusterListener = ((PeerEndPoint)peerEndPoint).getServiceClusterListener(peerClusterName);
							} else {
								allClustersListener = ((BasicEndPoint)peerEndPoint).getServiceClustersListener();
								serviceClusterListener = ((BasicEndPoint)peerEndPoint).getServiceClusterListener(peerClusterName);						
							}
							if (allClustersListener != null || serviceClusterListener != null) {
								IEndPointRequestContext requestContext = null;
								if (peerEndPoint instanceof PeerEndPoint)
									requestContext = ((PeerEndPoint)endPoint.getPeerEndPoint(peerEndPoint.getAppliance().getPid(), peerEndPoint.getId())).getPeerDefaultRequestContext();
								else
									requestContext = ((BasicEndPoint)endPoint).getDefaultRequestContext();
								if (allClustersListener != null)
									allClustersListener.notifyAttributeValue(this.name, attributeName, attributeValue, requestContext);
								if (serviceClusterListener != null)
									serviceClusterListener.notifyAttributeValue(attributeName, attributeValue, requestContext);
							}
						} catch (ApplianceException e) {
							// TODO: currently when an appliance is disconnected the
							// subscription is not deleted
							e.printStackTrace();
							if (e instanceof InvalidPeerApplianceException)
								attributeSubscriptions.remove(peerEndPoint);
						} catch (ServiceClusterException se) {
							se.printStackTrace();
						} catch (Exception ge) {
							ge.printStackTrace();
						}
					}
				}				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/****** IServiceCluster ******/
	
	public IAttributeValue getAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		Object result = null;
		// if (this.serviceCluster.isGeneric()) {
		// return this.serviceCluster.getAttributeValue(attributeName,
		// newClusterRequestContext);
		// } else {
		checkServiceClusterAvailability();
		try {
			result = ((Method) getterMethods.get(attributeName)).invoke(clusterInterfaceImpl, new Object[] {endPointRequestContext});
		} catch (Exception e) {
			if (e instanceof ServiceClusterException)
				throw (ServiceClusterException) e;
			else if (e instanceof ApplianceException)
				throw (ApplianceException) e;
			else
				throw new ApplianceException(UNEXPECTED_ERROR_MESSAGE);
		}
		return new AttributeValue(result);
	}

	// public IAttributeValue selectAttributeValue(String attributeName, String
	// attributeSelector,
	// IEndPointRequestContext endPointRequestContext) throws
	// ApplianceException, ServiceClusterException {
	// IAttributeValue attributeValueResult = null;
	// Object result = null;
	// // if (this.serviceCluster.isGeneric()) {
	// // return this.serviceCluster.getAttributeValue(attributeName,
	// // newClusterRequestContext);
	// // } else {
	// try {
	// result = ((Method) selectMethods.get(attributeName)).invoke(this, new
	// Object[] { attributeSelector,
	// endPointRequestContext });
	// } catch (Exception e) {
	// if (e instanceof ServiceClusterException)
	// throw (ServiceClusterException) e;
	// else if (e instanceof ApplianceException)
	// throw (ApplianceException) e;
	// else
	// throw new ApplianceException(UNEXPECTED_ERROR_MESSAGE);
	// }
	// return new AttributeValue(result);
	// }

	public IAttributeValue setAttributeValue(String attributeName, Object attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		Object result = null;
		checkServiceClusterAvailability();
		try {
			result = ((Method) setterMethods.get(attributeName)).invoke(clusterInterfaceImpl,
					new Object[] { attributeValue, endPointRequestContext});
		} catch (Exception e) {
			if (e instanceof ServiceClusterException)
				throw (ServiceClusterException) e;
			else if (e instanceof ApplianceException)
				throw (ApplianceException) e;
			else
				throw new ApplianceException(UNEXPECTED_ERROR_MESSAGE);
		}
		return new AttributeValue(result);
	}

	// public IAttributeValue putAttributeValue(String attributeName, String
	// attributeSelector, Object attributeValue,
	// IEndPointRequestContext endPointRequestContext) throws
	// ApplianceException, ServiceClusterException {
	// Object result = null;
	//
	// try {
	// result = ((Method) putMethods.get(attributeName)).invoke(this, new Object
	// [] { attributeSelector, attributeValue,
	// endPointRequestContext });
	// } catch (Exception e) {
	// if (e instanceof ServiceClusterException)
	// throw (ServiceClusterException) e;
	// else if (e instanceof ApplianceException)
	// throw (ApplianceException) e;
	// else
	// throw new ApplianceException(UNEXPECTED_ERROR_MESSAGE);
	// }
	// return new AttributeValue(result);
	// }

	public Object execCommand(String commandName, Object[] parameters, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException {
		checkServiceClusterAvailability();
		try {
			int argsSize = (parameters == null) ? 1 : parameters.length + 1;

			Object[] args = new Object[argsSize];
			System.arraycopy(parameters, 0, args, 0, parameters.length);
			args[parameters.length] = endPointRequestContext;

			// TODO: check if this implementation is correct
			return ((Method) execMethods.get(commandName)).invoke(clusterInterfaceImpl, args);
		} catch (Exception e) {
			if (e instanceof ServiceClusterException)
				throw (ServiceClusterException) e;
			else if (e instanceof ApplianceException)
				throw (ApplianceException) e;
			else
				throw new ApplianceException(UNEXPECTED_ERROR_MESSAGE);
		}
	}	
	
	public void notifyAttributeValue(String attributeName, Object value) {
		Map attributeSubscriptions = (Map) subscriptions.get(attributeName);
		if (attributeSubscriptions != null && attributeSubscriptions.size() > 0)
			this.notifyAttributeValue(attributeName, new AttributeValue(value));
	}

	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		// Can be used in child class to receive notifications
	}

}
