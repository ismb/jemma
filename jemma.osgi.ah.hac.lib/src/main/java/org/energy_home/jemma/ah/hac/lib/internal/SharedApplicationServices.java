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

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IServiceCluster;

public class SharedApplicationServices implements IApplicationService {

	private IServiceCluster[] serviceClusters = null; 
	
	public SharedApplicationServices() throws ApplianceException {
		serviceClusters = new IServiceCluster[] { new IASZoneClientCluster() };
	}
	
	public IServiceCluster[] getServiceClusters() {
		return serviceClusters;
	}

	public void notifyApplianceAdded(IApplicationEndPoint endPoint, IAppliance appliance) {
		// TODO Auto-generated method stub
		
	}

	public void notifyApplianceRemoved(IAppliance appliance) {
		// TODO Auto-generated method stub
		
	}

	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		// TODO Auto-generated method stub
		
	}

}
