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


import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.shal.DeviceInfo;

public class DeviceProxy {
	private static final long TEN_YEARS_IN_MILLISECS = 10 * 365 * 24 * 3600 * 1000;
	
	private IApplicationEndPoint applicationEndPoint;
	private IEndPoint endPoint;
	private DeviceInfo deviceInfo;

	private IEndPointRequestContext lastReadRequestContext = null;
	
	private void setApplicationEndPoint(IApplicationEndPoint applicationEndPoint) {
		this.applicationEndPoint = applicationEndPoint;
	}
	
	private void setEndPoint(IEndPoint endPoint) {
		this.endPoint = endPoint;
	}
	
	private void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	
	public DeviceProxy (IApplicationEndPoint applicationEndPoint, IEndPoint endPoint, DeviceInfo deviceInfo) {
		this.setApplicationEndPoint(applicationEndPoint);
		this.setEndPoint(endPoint);
		this.setDeviceInfo(deviceInfo);

		if (applicationEndPoint != null)
			lastReadRequestContext = applicationEndPoint.getRequestContext(true, TEN_YEARS_IN_MILLISECS);
		
	}
	
	public IEndPointRequestContext getLastReadApplicationRequestContext() {
		return lastReadRequestContext;
	}
	
	public IEndPointRequestContext getApplicationRequestContext() {
		if (applicationEndPoint == null)
			return null;
		else
			return applicationEndPoint.getDefaultRequestContext();
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
	
	public IEndPoint getEndPoint() {
		return endPoint;
	}
	
	public IServiceCluster getServiceCluster(String clusterName) {
		if (endPoint == null)
			return null;
		return endPoint.getServiceCluster(clusterName);
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}
}
