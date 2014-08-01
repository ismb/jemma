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
package org.energy_home.jemma.internal.ah.eh.esp;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class ServiceClusterProxy extends ServiceCluster  {	
	public ServiceClusterProxy() throws ApplianceException {
		super();
	}

	protected IServiceCluster getServiceCluster(DeviceProxy deviceProxy, String clusterName) {
		if (deviceProxy == null)
			return null;
		return deviceProxy.getServiceCluster(clusterName);
	}
	
	protected IEndPointRequestContext getApplicationRequestContext(DeviceProxy applianceProxy, boolean isConfirmationRequired) {
		if (applianceProxy == null)
			return null;
		return applianceProxy.getApplicationRequestContext(isConfirmationRequired);		
	}
	
	protected IEndPointRequestContext getLastReadApplicationRequestContext(DeviceProxy applianceProxy) {
		if (applianceProxy == null)
			return null;
		return applianceProxy.getLastReadApplicationRequestContext();		
	}
	
	
	
	protected String getApplianceId(IEndPointRequestContext context) {
		int endPointId = context.getPeerEndPoint().getId();
		String applianceId = context.getPeerEndPoint().getAppliance().getPid();
		if (endPointId != IEndPoint.DEFAULT_END_POINT_ID) {
			StringBuilder sb = new StringBuilder(applianceId);
			sb.append(ESPApplication.APPLIANCE_ID_SEPARATOR);
			sb.append(context.getPeerEndPoint().getId());
			applianceId = sb.toString();
		} 
		return applianceId;
	}
}
