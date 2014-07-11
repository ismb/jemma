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

import org.energy_home.jemma.ah.hac.IManagedAppliance;

public class ManagedApplianceStatus {
	static final int STATUS_INSTALLING = 0;
	static final int STATUS_ENABLING = 1;
	static final int STATUS_ENABLED = 2;
	
	private IManagedAppliance appliance;
	private int status;
	private long lastSubscriptionRequestTime;
	
	ManagedApplianceStatus(IManagedAppliance appliance, int status) {
		this.appliance = appliance;
		this.status = status;
	}
	
	IManagedAppliance getAppliance() {
		return this.appliance;
	}
	
	int getStatus() {
		return this.status;
	}
	
	void setStatus(int status) {
		this.status = status;
	}
	
	long getLastSubscriptionRequestTime() {
		return this.lastSubscriptionRequestTime;
	}
	
	void setLastSubscriptionRequestTime(long time) {
		this.lastSubscriptionRequestTime = time;
	}
}