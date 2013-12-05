/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import java.util.HashMap;
import java.util.Map;

public class ApplianceProxyList {
	private Map<String, ApplianceProxy> applianceProxyMap = new HashMap<String, ApplianceProxy>(ESPApplication.MAX_NUMBER_OF_APPLIANCES);
	
	public ApplianceProxy getApplianceProxy(String appliancePid) {
		return applianceProxyMap.get(appliancePid);
	}
	
	public ApplianceProxy addApplianceProxy(ApplianceProxy applianceProxy) {
		return applianceProxyMap.put(applianceProxy.getAppliance().getPid(), applianceProxy);
	}
	
	public ApplianceProxy removeApplianceProxy(String appliancePid) {
		return applianceProxyMap.remove(appliancePid);
	}
	
	public void clear() {
		applianceProxyMap.clear();
	}
}
