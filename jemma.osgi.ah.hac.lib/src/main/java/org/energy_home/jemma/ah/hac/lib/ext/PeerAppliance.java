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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.EndPoint;

public class PeerAppliance implements IAppliance {
	protected Appliance managedAppliance;
	protected EndPoint linkedEndPoint;
	private Map endPoints;
	private boolean isPeerValid;
	
	private boolean containsOnlyCommonClientClusters = true;
	
	EndPoint getLinkedEndPoint() {
		return this.linkedEndPoint;
	}
	
	boolean isPeerValid() {
		return this.isPeerValid;
	}
	
	public PeerAppliance(Appliance managedAppliance, EndPoint linkedEndPoint) {
		this.managedAppliance = managedAppliance;
		this.linkedEndPoint = linkedEndPoint;
		this.isPeerValid = true;
		if (managedAppliance != null)
			this.endPoints = new HashMap();
	}
	

	public void addPeerEndPoint(PeerEndPoint endPoint) throws ApplianceException {
		this.endPoints.put(new Integer(endPoint.getId()), endPoint);
		endPoint.setPeerAppliance(this);
		if (!endPoint.containsOnlyCommonClustersListeners)
			containsOnlyCommonClientClusters = false;
	}

	public void setPeerValid(boolean isPeerValid) {
		this.isPeerValid = isPeerValid;
	}
	
	public Map getEndPointsMap() {
		return endPoints;
	}
	
	public boolean containsOnlyCommonClientClusters() {
		return containsOnlyCommonClientClusters;
	}
	
	/****** IAppliance ******/

	public boolean isSingleton() {
		return managedAppliance.isSingleton();
	}
	
	public String getPid() {
		return managedAppliance.getPid();
	}

	public IApplianceDescriptor getDescriptor() {
		return managedAppliance.getDescriptor();
	}

	public boolean isDriver() {
		return managedAppliance.isDriver();
	}

	public Dictionary getCustomConfiguration() {
		return managedAppliance.getCustomConfiguration();
	}
	
	public Dictionary getConfiguration() {
		return managedAppliance.getConfiguration();
	}
	
	public boolean isValid() {
		return managedAppliance.isValid() && isPeerValid;
	}

	public boolean isAvailable() {
		return managedAppliance.isAvailable() && isPeerValid;
	}
	
	public IEndPoint[] getEndPoints() {
		if (endPoints.size() > 0) {
			IEndPoint[] endPointArray = new IEndPoint[endPoints.size()];
			return (IEndPoint[]) endPoints.values().toArray(endPointArray);
		} else
			return null;
	}

	public int[] getEndPointIds() {
		return managedAppliance.getEndPointIds();
	}

	public String[] getEndPointTypes() {
		return managedAppliance.getEndPointTypes();
	}
	
	public IEndPoint getEndPoint(int id) {
		return (IEndPoint) endPoints.get(new Integer(id));
	}

	public String toString() {
		return getPid();
	}
	
}
