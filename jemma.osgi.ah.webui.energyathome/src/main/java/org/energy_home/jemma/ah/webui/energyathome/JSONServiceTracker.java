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
import java.util.HashMap;
import java.util.Map;

import org.jabsorb.JSONRPCBridge;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class JSONServiceTracker extends ServiceTracker {

	static public ServiceTracker createJSONServiceTracker(BundleContext context) throws Exception {
		final String assertMsg = "param context is null!";

		if (context == null)
			throw new IllegalArgumentException(assertMsg);

		Filter filter = context.createFilter("(bind.protocol=json-rpc)");

		return new JSONServiceTracker(context, filter, null);
	}

	private JSONServiceTracker(BundleContext context, Filter filter, ServiceTrackerCustomizer customizer) {
		super(context, filter, customizer);
	}

	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);
		Long serviceId = (Long) reference.getProperty("service.id");
		return service;
	}

	public void removedService(ServiceReference reference, Object service) {
		Long serviceId = (Long) reference.getProperty("service.id");
		try {
			JSONRPCBridge.getGlobalBridge().unregisterObject(serviceId.toString());
		} catch (Exception e) {
			
		}
	}

	public ArrayList getServiceReferences(String clazz) throws Exception {
		ServiceReference[] services = null;
		try {
			services = this.context.getServiceReferences(clazz, "(bind.protocol=json-rpc)");
		} catch (InvalidSyntaxException e) {
			throw new Exception(e);
		}
		
		ArrayList array = new ArrayList();
		
		for (int i = 0; i < services.length; i++) {
			Long serviceId = (Long) services[i].getProperty("service.id");
			Map props = new HashMap();
			props.put("service.id", serviceId);
			props.put("interface.name", clazz);
			
			array.add(props);
		}
		return array;
	}

	public String findService(Map props) throws Exception {
		ServiceReference[] services = null;
		
		Integer serviceId = (Integer) props.get("service.id");
		String clazz = (String) props.get("interface.name");
		if (serviceId == null || clazz == null) 
			throw new Exception("bad reference");
		
		services = this.context.getServiceReferences(clazz, "(service.id=" + serviceId + ")");
			if (services == null) 
				throw new RuntimeException();
			
			if (services.length > 1) {
				throw new Exception("too many services");
			}
			
			Object service = this.context.getService(services[0]);
			JSONRPCBridge.getGlobalBridge().registerObject(serviceId.toString(), service);	
			return serviceId.toString();
	}

	public void unbind(String serviceId) {
		try {
			JSONRPCBridge.getGlobalBridge().unregisterObject(serviceId);
		} catch (Exception e) {
			
		}	
	}
}
