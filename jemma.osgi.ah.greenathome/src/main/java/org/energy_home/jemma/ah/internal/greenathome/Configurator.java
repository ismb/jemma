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
package org.energy_home.jemma.ah.internal.greenathome;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.device.Driver;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//XXX Is this related to Configuration ? Shall it be harmonized with COnfigAdmin ?
public class Configurator implements Driver, ServiceTrackerCustomizer {

	private BundleContext bc;

	private HashMap trackedDevices = new HashMap();

	private static final Logger LOG = LoggerFactory.getLogger( Configurator.class );

	public void activate(BundleContext bc, Map props) {
		this.bc = bc;
	}

	public String attach(ServiceReference s) throws Exception {
		ServiceTracker deviceTracker = new ServiceTracker(this.bc, s, this);
		deviceTracker.open();
		trackedDevices.put(s, deviceTracker);
		return null;
	}

	public int match(ServiceReference arg0) throws Exception {
		return 10;
	}

	public Object addingService(ServiceReference sr) {
		return sr;
	}

	public void modifiedService(ServiceReference sr, Object arg1) {
		LOG.debug("service modified");

		Dictionary props = this.getServiceProperties(sr);
	}

	private Dictionary getServiceProperties(ServiceReference sr) {
		String[] keys = sr.getPropertyKeys();
		Dictionary props = new Hashtable();
		for (int i = 0; i < keys.length; i++) {
			props.put(keys[i], sr.getProperty(keys[i]));
		}

		return props;
	}

	public void removedService(ServiceReference sr, Object arg1) {
		LOG.debug("Service has been removed");
		trackedDevices.remove(sr);
	}

	public void getPendingDevices() {

		for (Iterator it = trackedDevices.values().iterator(); it.hasNext();) {
			ServiceReference sr = (ServiceReference) it.next();

		}
	}
}
