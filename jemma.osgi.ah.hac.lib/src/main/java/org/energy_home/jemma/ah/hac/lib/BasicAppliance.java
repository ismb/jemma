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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplianceManager;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.lib.ext.ConfigServerCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of {@link IAppliance} interface
 * 
 */
public class BasicAppliance implements IAppliance {
	static final String INVALID_END_POINT_MESSAGE = "Invalid end point";
	static final Logger LOG = LoggerFactory.getLogger(Appliance.class);

	ApplianceFactory factory;
	boolean isDriver;
	boolean isAvailable;
	boolean isValid = true;

	String pid;
	HashMap endPoints;
	ConfigServerCluster basicServerCluster;
	IApplianceManager hacApplianceManager;	
	
	protected Dictionary configuration;	
	public BasicAppliance(String pid, Dictionary config) throws ApplianceException {
		this.pid = pid;
		this.isAvailable = false;
		this.isValid = true;
		this.isDriver = false;
		this.endPoints = new LinkedHashMap();
		this.configuration = config;
	}
	
	// ****** IAppliance ******/
	
	public boolean isSingleton() {
		return factory == null;
	}
	
	public final String getPid() {
		return this.pid;
	}

	/**
	 * This method needs to be re-implemented by appliances that doesn't have an
	 * associated appliance factory ({@code singleton logical appliances})
	 */
	public IApplianceDescriptor getDescriptor() {
		return factory.getDescriptor();
	}

	public Dictionary getConfiguration() {
		Hashtable result = new Hashtable(configuration.size());
		for(Enumeration e = configuration.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (!key.startsWith(APPLIANCE_CUSTOM_PROPERTIES_PREXIF))
				result.put(key, configuration.get(key));
		}
		return result;	
	}
	
	public Dictionary getCustomConfiguration() {
		Hashtable result = new Hashtable(configuration.size());
		for(Enumeration e = configuration.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.startsWith(APPLIANCE_CUSTOM_PROPERTIES_PREXIF))
				result.put(key, configuration.get(key));
		}
		return result;	
	}
	
	public final boolean isDriver() {
		return this.isDriver;
	}

	public final boolean isValid() {
		return this.isValid;
	}

	public final boolean isAvailable() {
		return this.isValid && this.isAvailable;
	}
	
	public final IEndPoint[] getEndPoints() {
		if (endPoints.size() > 0) {
			IEndPoint[] endPointArray = new IEndPoint[endPoints.size()];
			return (IEndPoint[]) endPoints.values().toArray(endPointArray);
		} else
			return null;
	}
	
	public final int[] getEndPointIds() {
		int[] endPointIds = new int[endPoints.size()];
		IEndPoint endPoint = null;
		int i = 0;
		for (Iterator iterator = endPoints.values().iterator(); iterator.hasNext();) {
			endPoint = (IEndPoint) iterator.next();
			endPointIds[i++] = endPoint.getId();
		}
		return endPointIds;
	}
	
	public final String[] getEndPointTypes() {
		String[] endPointTypes = new String[endPoints.size()];
		IEndPoint endPoint = null;
		int i = 0;
		for (Iterator iterator = endPoints.values().iterator(); iterator.hasNext();) {
			endPoint = (IEndPoint) iterator.next();
			endPointTypes[i++] = endPoint.getType();
		}
		return endPointTypes;
	}

	public final IEndPoint getEndPoint(int id) {
		return (IEndPoint) endPoints.get(new Integer(id));
	}
}
