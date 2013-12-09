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
package org.energy_home.jemma.ah.app.impl;

import org.energy_home.jemma.ah.app.EnergyAtHomeApp;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator, EnergyAtHomeApp {
	private ServiceRegistration service = null;
	private Dictionary properties = new Hashtable();

	public void start(BundleContext context) throws Exception {
		// Imposta le proprieta' del servizio
		properties.put(new String("version"), new String(context.getBundle().getVersion().toString()));

		// Registra il servizio nell'ambiente OSGi
		service = context.registerService(EnergyAtHomeApp.class.getName(), this, properties);
	}

	public void stop(BundleContext context) throws Exception {
		// Rimuove il servizio dall'ambiente OSGi
		service.unregister();
	}
}
