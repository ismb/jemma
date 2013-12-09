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
package org.energy_home.jemma.osgi.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.internal.utils.datetime.DateTimeObject;
import org.energy_home.jemma.internal.utils.thread.ExecutorObject;
import org.energy_home.jemma.utils.datetime.DateTimeService;
import org.energy_home.jemma.utils.datetime.DateUtils;
import org.energy_home.jemma.utils.thread.ExecutorService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator, ServiceFactory {
	private static final Log log = LogFactory.getLog(Activator.class);
	
	private BundleContext bc;
	private ExecutorObject executorObject;
	private DateTimeObject dateTimeObject;
	private ServiceRegistration executorRegistration;
	private ServiceRegistration dateTimeRegistration;
	
	public void start(BundleContext bundleContext) throws Exception {
		this.bc = bundleContext;
		dateTimeObject = new DateTimeObject();
		executorRegistration = bc.registerService(ExecutorService.class.getName(), this, null);
		dateTimeRegistration = bc.registerService(DateTimeService.class.getName(), dateTimeObject, null);
		// A periodic date time check is scheduled if initial dateTime is invalid 
		if (!dateTimeObject.isDateTimeOk()) { 
			log.warn("Osgi utils activator started with date time check ko");
			executorObject = new ExecutorObject(bc.getBundle().getSymbolicName());
			executorObject.scheduleTask(new Runnable() {			
				public void run() {
					if (DateUtils.isDateTimeOk()) {
						if (dateTimeRegistration != null)
							dateTimeRegistration.unregister();
						dateTimeObject = new DateTimeObject();
						dateTimeRegistration = bc.registerService(DateTimeService.class.getName(), dateTimeObject, null);
						log.info("Osgi utils activator periodic date time check ok... restarting DateTimeService");
						executorObject.release();
					} else {
						log.warn("Osgi utils activator periodic date time check ko");
					}
				}
			}, DateUtils.MILLISEC_IN_ONE_MINUTE, DateUtils.MILLISEC_IN_ONE_MINUTE);
		}
	}
	
	public void stop(BundleContext arg0) throws Exception {
		try {
			if (executorRegistration != null)
				executorRegistration.unregister();
			if (dateTimeRegistration != null)
				dateTimeRegistration.unregister();
			if (executorObject != null)
				executorObject.release();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		String bundleName = bundle.getSymbolicName();
		log.info("Created Executor Service for bundle " + bundleName);
		return new ExecutorObject(bundle.getSymbolicName());
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		((ExecutorObject) service).release();
		log.info("Released Executor Service for bundle " + bundle.getSymbolicName());
	}

}
