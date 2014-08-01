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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.IApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;

public class AppliancesProxy extends AppliancesBasicProxy implements IAppliancesProxy {
	
	//********** Internal miscellaneous methods
	
	private Object invoke(IServiceCluster serviceCluster, String methodName, Object[] params) throws Throwable {
		Method[] methods = serviceCluster.getClass().getMethods();
		Method method = null;
		Class[] methodParams;
		Throwable t = null;
		boolean methodFound = false;
		// TODO: check for an explicit cast to cluster interface or for a check
		// on method argument types
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getName().compareTo(methodName) == 0) {
				methodParams = method.getParameterTypes();
				if (methodParams.length == params.length) {
					methodFound = true;
					try {
						return method.invoke(serviceCluster, params);
					} catch (Exception e) {
						t = e.getCause();
						if (t == null)
							t = e;
					}
				}
			}
		}
		if (t != null)
			throw t;
		if (!methodFound)
			throw new Exception("Invalid cluter method or parameters");
		throw new Exception("Unknown exception");
	}
	
	private Object clusterInvoke(String appliancePid, Integer endPointId, String clusterName, String methodName, Object[] params) throws Throwable {
		Object result = null;
		IServiceCluster serviceCluster = getServiceCluster(appliancePid, endPointId, clusterName);
		if (serviceCluster != null) {
			if (methodName != null) {
				result = this.invoke((IServiceCluster)serviceCluster, methodName, params);
			} else {
				String msg = "invoke: method Name not found";
				LOG.error(msg);
				return new ServiceClusterException(msg);
			}
		} else {
			String msg = "invoke: method Name not found";
			LOG.error(msg);
			return new ServiceClusterException(msg);
		}
		return result;
	}
	
	private IEndPoint getMainEndPoint(String appliancePid) {
		IEndPoint ep = null;
		IAppliance appliance = getAppliance(appliancePid);
		if (appliance == null)
			appliance = getInstallingAppliance(appliancePid);
		if (appliance != null) {
			ep = appliance.getEndPoint(IEndPoint.DEFAULT_END_POINT_ID);
			if (ep == null) {
				IEndPoint[] eps = appliance.getEndPoints();
				if (eps != null && eps.length > 0)
					ep = eps[eps.length-1];
			}
		}
		return ep;
	}

	private IServiceCluster getServiceCluster(String appliancePid, Integer endPointId, String clusterName) {
		IEndPoint endPoint = null;
		IServiceCluster serviceCluster = null;
		IEndPoint ep = null;
		IAppliance appliance = getAppliance(appliancePid);
		if (appliance == null)
			appliance = getInstallingAppliance(appliancePid);
		if (appliance != null) {
			if (endPointId == null) {
				IEndPoint[] endPoints = appliance.getEndPoints();
				for (int i = 0; i < endPoints.length; i++) {
					endPoint = endPoints[i];
					serviceCluster = endPoint.getServiceCluster(clusterName);
					if (serviceCluster != null)
						break;
				}
			} else {
				endPoint = appliance.getEndPoint(endPointId.intValue());
				serviceCluster = endPoint.getServiceCluster(clusterName);
			}
		}
		return serviceCluster;
	}

	
	//********** Public constructor
	
	public AppliancesProxy() throws ApplianceException {
		super();
	}
	
	//********** IAppliancesProxy service interface
	
	public ILocation getLocation(String pid) {
		ILocation[] locations = getLocations();
		if (locations != null) {
			for (int i = 0; i < locations.length; i++) {
				if (locations[i].getPid().equals(pid))
					return locations[i];
			}
		}
		return null;
	}
	
	public ILocation[] getLocations() {
		synchronized (hacServiceSync) {
			if (hacService == null) {
				LOG.error("getLocations error: hac service not available");
				return null;
			}
			return hacService.getLocations();
		}
	}

	public ICategory getCategory(String pid) {
		ICategory[] categories = getCategories();
		if (categories != null) {
			for (int i = 0; i < categories.length; i++) {
				if (categories[i].getPid().equals(pid))
					return categories[i];
			}
		}
		return null;
	}
	
	public ICategory[] getCategories() {
		synchronized (hacServiceSync) {
			if (hacService == null) {
				LOG.error("getCategories error: hac service not available");
				return null;
			}
			return hacService.getCategories();
		}
	}
	
	public synchronized List getInstallingAppliances() {
		List list = new ArrayList(installingApplianceMap.size());
		for (Iterator iterator = installingApplianceMap.values().iterator(); iterator.hasNext();) {
			ManagedApplianceStatus proxy = (ManagedApplianceStatus) iterator.next();
			list.add(proxy.getAppliance());
		}
		return list;
	}

	public synchronized List getInstallingAppliancePids() {
		List list = new ArrayList(installingApplianceMap.size());
		for (Iterator iterator = installingApplianceMap.values().iterator(); iterator.hasNext();) {
			ManagedApplianceStatus proxy = (ManagedApplianceStatus) iterator.next();
			list.add(proxy.getAppliance().getPid());
		}
		return list;
	}
	
	public IAppliance getInstallingAppliance(String appliancePid) {
		ManagedApplianceStatus proxy = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
		if (proxy != null)
			return proxy.getAppliance();
		return null;
	}
	
	public IApplianceConfiguration getApplianceConfiguration(String appliancePid) {
		IAppliance appliance = getAppliance(appliancePid);
		if (appliance == null) {
			appliance = getInstallingAppliance(appliancePid);
		}
		if (appliance == null || appliance.isSingleton())
			return null;
		Map configuration = (Map) applianceConfigurationMap.get(appliancePid);
		if (configuration == null)
			return null;
		return new ApplianceConfiguration(appliance.getEndPointIds(), configuration);
	}
	
	public boolean updateApplianceConfiguration(IApplianceConfiguration applianceConfiguration) {
		Map config = ((ApplianceConfiguration)applianceConfiguration).getConfigurationMap();
		if (hacService == null) {
			LOG.error("updateApplianceConfiguration error: hac service not available");
			return false;
		}
		try {
			hacService.updateAppliance(applianceConfiguration.getAppliancePid(), new Hashtable(config));
		} catch (Exception e) {
			LOG.error("updateApplianceConfiguration error: some problems occurred while trying to update configuration through hac service", e);
			return false;
		}
		return true;
	}

	public boolean installAppliance(String appliancePid) {
		Map config = (Map) applianceConfigurationMap.get(appliancePid);
		if (config == null) {
			LOG.error("installAppliance error: no configuration available");
			return false;
		}	
		synchronized (hacServiceSync) {
			if (hacService == null) {
				LOG.error("installAppliance error: hac service not available");
				return false;
			}
			try {	
				hacService.installAppliance(appliancePid, new Hashtable(config));
			} catch (Exception e) {
				LOG.error("installAppliance error: some problems occurred while trying to install appliance through hac service", e);
				return false;
			}
		}
		return true;
	}
	
	public boolean deleteAppliance(String appliancePid) {
		synchronized (hacServiceSync) {
			if (hacService == null) {
				LOG.error("deleteAppliance error: hac service not available");
				return false;
			}
			try {
				return hacService.removeAppliance(appliancePid);
			} catch (Exception e) {
				LOG.error("deleteAppliance error: some problems occurred while trying to delete configuration through hac service", e);
				return false;
			}				
		}
	}
	
	public Object invokeClusterMethod(String appliancePid, Integer endPointId, String clusterName, String methodName, Object[] params) throws Exception {
		try {
		    return clusterInvoke(appliancePid, endPointId, clusterName, methodName, params);
		} catch (Throwable t) {
			LOG.error("invoke error", t);
			throw new Exception(t);
		}
	}

	public ISubscriptionParameters getAttributeSubscription(String appliancePid, Integer endPointId, String clusterName, String attributeName) {
		ISubscriptionParameters sp = null;
		IServiceCluster serviceCluster = getServiceCluster(appliancePid, endPointId, clusterName);
		if (serviceCluster != null && !isNullOrEmpty(attributeName)) {
			try {
				sp = serviceCluster.getAttributeSubscription(attributeName, confirmedRequestContext);
			} catch (Exception e) {
				LOG.error("getAttributeSubscription error", e);
			}
		} 
		return sp;		
	}

	public ISubscriptionParameters setAttributeSubscription(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			ISubscriptionParameters parameters) {
		ISubscriptionParameters sp = null;
		IServiceCluster serviceCluster = getServiceCluster(appliancePid, endPointId, clusterName);
		if (serviceCluster != null && !isNullOrEmpty(attributeName)) {
			try {
				sp = serviceCluster.setAttributeSubscription(attributeName, parameters, confirmedRequestContext);
			} catch (Exception e) {
				LOG.error("setAttributeSubscription error", e);
			}
		}
		return sp;
	}

	public IAttributeValue getLastNotifiedAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName) {
		ServiceCluster serviceCluster = (ServiceCluster)getServiceCluster(appliancePid, endPointId, clusterName);
		try {
			if (serviceCluster != null) {
				return serviceCluster.getLastNotifiedAttributeValue(attributeName, null);
			}
		} catch (Exception e) {
			LOG.error("Exception while reading last notified attribute", e);
		}
		return null;
	}
	
	public IAttributeValue getLastReadAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName) {
		ServiceCluster serviceCluster = (ServiceCluster)getServiceCluster(appliancePid, endPointId, clusterName);
		try {
			if (serviceCluster != null) {
				return serviceCluster.getAttributeValue(attributeName, lastReadRequestContext);
			}
		} catch (Exception e) {
			LOG.error("Exception while reading last notified attribute", e);
		}
		return null;
	}

	public Map getLastNotifiedAttributeValues(String appliancePid, Integer endPointId, String clusterName) {
		Map result = null;
		try {
			IServiceCluster serviceCluster = getServiceCluster(appliancePid, endPointId, clusterName);
			if (serviceCluster != null) {
				Map subscriptions = serviceCluster.getAllSubscriptions(null);
				if (subscriptions != null) {
					result = new HashMap(subscriptions.size());
					for (Iterator iterator = subscriptions.keySet().iterator(); iterator.hasNext();) {
						String attributeName = (String) iterator.next();
						IAttributeValue attributeValue = serviceCluster.getLastNotifiedAttributeValue(attributeName, null);
						if (attributeValue != null)
							result.put(attributeName, attributeValue);		
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Error while getting last notified attribute values", e);
		}
		return result;
	}
}
