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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedApplianceServiceTracker extends ServiceTracker {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManagedApplianceServiceTracker.class);

	private HacService hacService;

	public ManagedApplianceServiceTracker(BundleContext bc, HacService hacService) {
		super(bc, IManagedAppliance.class.getName(), null);
		this.hacService = hacService;
	}

	public Object addingService(ServiceReference sr) {
		IManagedAppliance appliance = (IManagedAppliance) context.getService(sr);
		
		Map props = new ReadOnlyDictionary(sr);
		
		
		try {
			this.hacService.setManagedAppliance(appliance, props);
		} catch (ApplianceException e) {
			LOG.warn(e.getMessage(), e);
		}
		
		return appliance;
	}

	public void modifiedService(ServiceReference sr, Object o) {
		IManagedAppliance appliance = (IManagedAppliance) o;
		Map props = new ReadOnlyDictionary(sr);
		this.hacService.updatedManagedAppliance(appliance, props);
	}

	public void removedService(ServiceReference sr, Object o) {
		IManagedAppliance appliance = (IManagedAppliance) o;
		this.hacService.unsetManagedAppliance(appliance);
		this.context.ungetService(sr);
	}
}
