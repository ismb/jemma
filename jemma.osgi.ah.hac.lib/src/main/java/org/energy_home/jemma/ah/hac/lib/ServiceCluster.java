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

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;

/**
 * Implementation of the {@code IServiceCluster} interface
 * 
 * @see IServiceCluster
 * @see BasicServiceCluster
 * 
 */
public class ServiceCluster extends BasicServiceCluster {
	private void init(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
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
			if (clusterMethod[i].getName().startsWith(HacCommon.CLUSTER_ATTRIBUTE_SETTER_PREFIX)) {
				key = clusterMethod[i].getName().substring(HacCommon.CLUSTER_ATTRIBUTE_SETTER_PREFIX.length());
				this.setterMethods.put(key, clusterMethod[i]);
				this.settersMethodNameToAttributeName.put(clusterMethod[i].getName(), key);
			}
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
	
	protected final void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
		if (endPoint == null)
			this.appliance = null;
		else
			this.appliance = (Appliance)endPoint.getAppliance();
	}

	public ServiceCluster() throws ApplianceException {
		this.endPoint = null;
		this.appliance = null;

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
					LOG.debug(e.getMessage(), e);
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
	
	public ServiceCluster(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
		init(clusterInterfaceImpl, clusterInterfaceClass);
	}

	public final Object getClusterInterfaceImpl() {
		return clusterInterfaceImpl;
	}
	
	public final Class getClusterInterfaceClass() {
		return clusterInterfaceClass;
	}
	
	public final Method getMethod(String commandName) {
		return (Method) execMethods.get(commandName);
	}
}
