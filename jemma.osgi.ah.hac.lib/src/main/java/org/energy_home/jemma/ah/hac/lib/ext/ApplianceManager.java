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

import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplianceManager;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.internal.AppliancesProxy;

public abstract class ApplianceManager implements IApplianceManager {
	private static final String APPLIANCE_NOT_INITIALIZED = "Appliance not  initialized";
	
	private IHacService hacService;
	//private IConnectionAdminService connectionAdminService;

	public void setHacService(IHacService hacService) {
		this.hacService = hacService;
	}
	
//	public void configUpdated(Dictionary props) throws HacException {
//		hacService.updateConfiguration(managedAppliance.getPid(), props);
//	}

	public ILocation[] getLocations() throws ApplianceValidationException {
		if (hacService == null)
			throw new ApplianceValidationException(APPLIANCE_NOT_INITIALIZED);
		return hacService.getLocations();
	}

	public ILocation getLocation(String pid) throws ApplianceValidationException {
		if (hacService == null)
			throw new ApplianceValidationException(APPLIANCE_NOT_INITIALIZED);
		return hacService.getLocation(pid);
	}

	public ICategory[] getCategories() throws ApplianceValidationException {
		if (hacService == null)
			throw new ApplianceValidationException(APPLIANCE_NOT_INITIALIZED);
		return hacService.getCategories();
	}

	public ICategory getCategory(String pid) throws ApplianceValidationException {
		if (hacService == null)
			throw new ApplianceValidationException();
		return hacService.getCategory(pid);
	}
	
	public abstract String[] getMatchingClusterTypes(int endPointId, int side, IApplianceDescriptor peerApplianceDescriptor,
			String peerEndPointType, String[] peerServiceClusterTypes, String[] peerClusterListenerTypes);

	public abstract void peerApplianceConnected(EndPoint endPoint, IAppliance peerAppliance);
	
	public abstract void addPeerAppliance(EndPoint endPoint, IAppliance peerAppliance);

	public abstract void peerApplianceDisconnected(EndPoint endPoint, IAppliance peerAppliance);
	
	public abstract void removePeerAppliance(EndPoint endPoint, IAppliance peerAppliance);
	
	public abstract Object getCustomConfiguration();
	
	public abstract void setAppliancesProxy(AppliancesProxy proxy);

}
