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
package org.energy_home.jemma.ah.webui.energyathome;


import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jabsorb.JSONRPCBridge;
import org.osgi.framework.BundleContext;

public class ServiceRegistryProxy {

	private StaticJSONServiceTracker jSONServiceTracker;
	private static final Log log = LogFactory.getLog(EnergyAtHome.class);

	public ServiceRegistryProxy(BundleContext bc, JSONRPCBridge jsonRpcBridge) {
		try {
			jSONServiceTracker = (StaticJSONServiceTracker) StaticJSONServiceTracker.createJSONServiceTracker(bc);
			jSONServiceTracker.open();
		} catch (Exception e) {
			log.debug(e);
		}
	}

	public ArrayList find (String clazz) throws Exception {
		return this.jSONServiceTracker.getServiceReferences(clazz);
	}

	public String bind (Map props) throws Exception {
		return this.jSONServiceTracker.findService(props);
	}
	
	public void unbind (String serviceId) throws Exception {
		this.jSONServiceTracker.unbind(serviceId);
	}
	
	public void close() {
		this.jSONServiceTracker.close();
	}
}
