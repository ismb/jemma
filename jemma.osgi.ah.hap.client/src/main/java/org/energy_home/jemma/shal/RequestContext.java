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
package org.energy_home.jemma.shal;

public class RequestContext {
	public static final long NO_REQUEST_TIME = -1;
	
	private EndPoint endPoint;
	private long time = NO_REQUEST_TIME;
	
	private void initEndPoint(EndPoint endPoint) {
		if (endPoint != null && 
				(endPoint instanceof DeviceService || endPoint instanceof DeviceListener))
			this.endPoint = endPoint;
		else
			throw new IllegalArgumentException("End point parameter must be a DeviceService or DeviceListener implementation");
	}
	
	public RequestContext(EndPoint endPoint) {
		initEndPoint(endPoint);
	}
	
	public RequestContext(EndPoint endPoint, long time) {
		this.time = time;
		initEndPoint(endPoint);
	}
	
	public final String getCallerEndPointId() {
		// TODO: check on registered services (DeviceService and DeviceServiceListeners), e.g. throw a static method of CommonServices 
		return endPoint.getEndPointId();
	}
	
	public final long getRequestTime() {
		return time;
	}
	
}
