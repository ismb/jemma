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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.zgd.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Osgi's activator for the javagal Rest package.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Activator implements BundleActivator {
	private static final Logger LOG = LoggerFactory.getLogger( Activator.class );
	private static BundleContext context;
	PropertiesManager PropertiesManager = null;

	private RestManager restManager;

	static BundleContext getContext() {
		return context;
	}

	ServiceTracker serviceTracker = null;

	/**
	 * Starts the osgi's bundle.
	 */
	public void start(BundleContext bundleContext) throws Exception {
		String _path = "config.properties";

		PropertiesManager = new PropertiesManager(bundleContext.getBundle().getResource(_path));

		if (PropertiesManager.getDebugEnabled())
			LOG.info("Starting Rest bundle");
		Activator.context = bundleContext;
		serviceTracker = new GatewayInterfaceFactoryTracker(context);
		serviceTracker.open();
		if (PropertiesManager.getDebugEnabled())
			LOG.info("jemma.osgi.javagal.rest bundle started");
	}

	/**
	 * Stops the osgi's bundle.
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		if (PropertiesManager.getDebugEnabled())
			LOG.info("jemma.osgi.javagal.rest bundle stopping");
		Activator.context = null;
		serviceTracker.close();
		serviceTracker = null;
		if (restManager != null) {
			restManager.setProxyActive(false);
			restManager.stopServer();
			restManager.deleteFactory();
		}
		if (PropertiesManager.getDebugEnabled())
			LOG.info("jemma.osgi.javagal.rest bundle stopped");
		PropertiesManager = null;
	}

	/**
	 * Factory tracker class for GatewayInterface objects.
	 * 
	 * @author
	 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
	 * 
	 */
	public class GatewayInterfaceFactoryTracker extends ServiceTracker {

		ServiceReference reference;
		GalExtenderProxyFactory gatewayFactory;

		BundleContext _context = null;

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
				Activator.LOG.debug("ZGD started. Rest Manager can serve requests.");

			return null;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {

		}

	}
}
