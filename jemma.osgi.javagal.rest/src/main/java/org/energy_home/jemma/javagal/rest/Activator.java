package org.energy_home.jemma.javagal.rest;

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

import java.io.File;
import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.zgd.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Activator implements BundleActivator {
	Log log = LogFactory.getLog(Activator.class);
	private static BundleContext context;
	PropertiesManager PropertiesManager = null;

	private RestManager restManager;

	static BundleContext getContext() {
		return context;
	}

	ServiceTracker serviceTracker = null;

	public void start(BundleContext bundleContext) throws Exception {
		String _path = File.separator + "config.properties";

		PropertiesManager = new PropertiesManager(bundleContext.getBundle().getResource(_path));

		if (PropertiesManager.getDebugEnabled())
			log.info("Starting Rest bundle");
		Activator.context = bundleContext;
		serviceTracker = new GatewayInterfaceFactoryTracker(context);
		serviceTracker.open();
		if (PropertiesManager.getDebugEnabled())
			log.info("Rest bundle started!");
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (PropertiesManager.getDebugEnabled())
			log.info("Stopping Rest bundle");
		Activator.context = null;
		serviceTracker.close();
		serviceTracker = null;
		if (restManager != null) {
			restManager.setProxyActive(false);
			restManager.stopServer();
			restManager.deleteFactory();
		}
		if (PropertiesManager.getDebugEnabled())
			log.info("Stopped Rest bundle");
		PropertiesManager = null;
	}

	public class GatewayInterfaceFactoryTracker extends ServiceTracker {

		ServiceReference reference;
		GalExtenderProxyFactory gatewayFactory;

		BundleContext _context = null;
		private final Log logger = LogFactory.getLog(GatewayInterfaceFactoryTracker.class);

		public GatewayInterfaceFactoryTracker(BundleContext context) {
			// super(context, GalExtenderProxyFactory.class.getName(), null);
			super(context, GalExtenderProxyFactory.class.getName(), null);
			_context = context;
		}

		@Override
		public Object addingService(ServiceReference reference) {

			gatewayFactory = (GalExtenderProxyFactory) context.getService(reference);

			if (PropertiesManager.getUseDefaultNWKRootURI() == 0) {
				Resources.setNET_DEFAULT_ROOT_URI(String.format("%016X", gatewayFactory.getExtendedPanId()));
			}

			restManager = new RestManager(PropertiesManager, gatewayFactory);

			if (PropertiesManager.getDebugEnabled())
				logger.info("\n\rZGD started. Rest Manager can serve requests.\n\r");

			return null;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {

		}

	}
}
