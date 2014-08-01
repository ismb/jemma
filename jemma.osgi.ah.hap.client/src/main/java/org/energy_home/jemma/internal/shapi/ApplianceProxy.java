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
package org.energy_home.jemma.internal.shapi;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;

public class ApplianceProxy {
	private static final long TEN_YEARS_IN_MILLISECS = 10 * 365 * 24 * 3600 * 1000;
	
	private IApplicationEndPoint applicationEndPoint;
	private IAppliance appliance;
	private IEndPointRequestContext lastReadRequestContext = null;

	private void setApplicationEndPoint(IApplicationEndPoint applicationEndPoint) {
		this.applicationEndPoint = applicationEndPoint;
	}
	
	private void setAppliance(IAppliance appliance) {
		this.appliance = appliance;
	}
	
	public ApplianceProxy (IApplicationEndPoint applicationEndPoint, IAppliance appliance) {
		this.setApplicationEndPoint(applicationEndPoint);
		this.setAppliance(appliance);
		if (applicationEndPoint != null)
			lastReadRequestContext = applicationEndPoint.getRequestContext(true, TEN_YEARS_IN_MILLISECS);
	}
	
	public IEndPointRequestContext getApplicationRequestContext() {
		if (applicationEndPoint == null)
			return null;
		else
			return applicationEndPoint.getDefaultRequestContext();
	}
	
	public IEndPointRequestContext getLastReadApplicationRequestContext() {
		return lastReadRequestContext;
	}
	
	public IEndPointRequestContext getApplicationRequestContext(Boolean isConfirmationRequired) {
		if (applicationEndPoint == null)
			return null;
		else
			return applicationEndPoint.getDefaultRequestContext(true);
	}

	public IEndPointRequestContext getApplicationRequestContext(Boolean isConfirmationRequired, long maxAgeForAttributeValues) {
		if (applicationEndPoint == null)
			return null;
		else
			return applicationEndPoint.getRequestContext(isConfirmationRequired, maxAgeForAttributeValues);
	}
	
	public IAppliance getAppliance() {
		return appliance;
	}
	
	public IEndPoint getEndPoint(int endPointId) {
		return appliance.getEndPoint(endPointId);
	}
	
	public IServiceCluster getServiceCluster(int endPointId, String clusterName) {
		IEndPoint endPoint = appliance.getEndPoint(endPointId);
		if (endPoint == null)
			return null;
		return endPoint.getServiceCluster(clusterName);
	}

}
