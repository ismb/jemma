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

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IServiceCluster;

/**
 * Implementation of the {@code IServiceCluster} interface
 * 
 * @see IServiceCluster
 * @see BasicServiceCluster
 * 
 */
public class ServiceCluster extends BasicServiceCluster {
	final void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
		this.appliance = endPoint.getAppliance();
	}
	
	public ServiceCluster() throws ApplianceException {
		super();
	}
	
	public ServiceCluster(Object clusterInterfaceImpl, Class clusterInterfaceClass) throws ApplianceException {
		super(clusterInterfaceImpl, clusterInterfaceClass);
	}

	public final Object getClusterInterfaceImpl() {
		return clusterInterfaceImpl;
	}
	
	public final Class getClusterInterfaceClass() {
		return clusterInterfaceClass;
	}
}
