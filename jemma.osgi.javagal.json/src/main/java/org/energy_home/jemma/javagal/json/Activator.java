package org.energy_home.jemma.javagal.json;

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

import org.energy_home.jemma.zgd.GalExtenderProxy;
import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	private static final Logger LOG = LoggerFactory.getLogger( Activator.class );
	private static BundleContext context;
	GalExtenderProxyFactory gatewayFactory;
	ServletContainer container;
	HttpService httpService;

	static BundleContext getContext() {
		return context;
	}

	ServiceTracker serviceTracker = null;
	ServiceTracker httpserviceTracker = null;

	/**
	 * Starts the osgi's bundle.
	 */
	public void start(BundleContext bundleContext) throws Exception {

		context = bundleContext;
		serviceTracker = new GatewayInterfaceFactoryTracker(context);
		serviceTracker.open();
		LOG.debug("Starting javagal.Json");
	}

	/**
	 * Stops the osgi's bundle.
	 */
	public void stop(BundleContext bundleContext) throws Exception {

		Activator.context = null;
		serviceTracker.close();
		serviceTracker = null;
		LOG.debug("Stopping javagal.Json");
	}

	public class GatewayInterfaceFactoryTracker extends ServiceTracker {

		ServiceReference reference;

		BundleContext _context = null;

		public GatewayInterfaceFactoryTracker(BundleContext context) {

			super(context, GalExtenderProxyFactory.class.getName(), null);
			_context = context;
		}

		@Override
		public Object addingService(ServiceReference reference) {

			gatewayFactory = (GalExtenderProxyFactory) context.getService(reference);

			httpserviceTracker = new ServiceTracker(context, HttpService.class.getName(), null) {
				public void removedService(ServiceReference reference, Object service) {
					container.unregister();
					try {
						((GalExtenderProxy)container.gatewayInterface).deleteProxy();
					} catch (Exception e) {
						e.printStackTrace();
					}

					container = null;
					httpService = null;
					

				}

				public Object addingService(ServiceReference reference) {

					try {
						httpService = (HttpService) this.context.getService(reference);
						
						container = new ServletContainer(httpService, gatewayFactory.createGatewayInterfaceObject());
						
					} catch (Exception exception) {
						exception.printStackTrace();
					}
					return null;
				}
			};

			httpserviceTracker.open();

			return null;
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			gatewayFactory = null;
			httpserviceTracker.remove(reference);
		}

	}
}
