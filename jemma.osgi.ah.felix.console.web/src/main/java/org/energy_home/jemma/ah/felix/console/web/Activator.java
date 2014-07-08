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
package org.energy_home.jemma.ah.felix.console.web;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// Unused (service is registered through declarative service)
public class Activator implements BundleActivator {
	public void start(BundleContext bundleContext) throws Exception {
		HacWebCommandProvider jmxPlugin = new HacWebCommandProvider();
		Dictionary props = new Properties();
		props.put("felix.webconsole.label", HacWebCommandProvider.LABEL);
		props.put("felix.webconsole.title", HacWebCommandProvider.NAME);
		bundleContext.registerService(javax.servlet.Servlet.class.getName(), jmxPlugin, props);
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}
}
