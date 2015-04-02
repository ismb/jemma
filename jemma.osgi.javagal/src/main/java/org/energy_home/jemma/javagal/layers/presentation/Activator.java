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
package org.energy_home.jemma.javagal.layers.presentation;

import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.object.GatewayProperties;
import org.energy_home.jemma.zgd.GalExtenderProxy;
import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Osgi Activator implementation.
 * 
 * @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 
 */
public class Activator implements BundleActivator {
	private BundleContext bc;
	private GalExtenderProxyFactory _fac = null;
	private static final Logger LOG = LoggerFactory.getLogger( Activator.class );
	private ServiceRegistration gatewayInterfaceRegistration;
	private ServiceRegistration gatewayFactoryRegistration;

	private GatewayInterfaceServiceFactory gatewayInterfaceServiceFactory;

	private GatewayFactoryServiceFactory gatewayFactoryServiceFactory;

	public void start(BundleContext context) throws Exception {
		LOG.info("Starting Gal:Osgi...");
		bc = context;
		try {
			String _path = "config.properties";

			LOG.info("FILE Conf: " + _path);

			PropertiesManager PropertiesManager = new PropertiesManager(bc.getBundle().getResource(_path));

			if (context.getProperty(GatewayProperties.ZGD_DONGLE_URI_PROP_NAME) != null)
				PropertiesManager.props.setProperty(GatewayProperties.ZGD_DONGLE_URI_PROP_NAME, context.getProperty(GatewayProperties.ZGD_DONGLE_URI_PROP_NAME));
			if (context.getProperty(GatewayProperties.ZGD_DONGLE_SPEED_PROP_NAME) != null)
				PropertiesManager.props.setProperty(GatewayProperties.ZGD_DONGLE_SPEED_PROP_NAME, context.getProperty(GatewayProperties.ZGD_DONGLE_SPEED_PROP_NAME));
			if (context.getProperty(GatewayProperties.ZGD_DONGLE_TYPE_PROP_NAME) != null)
				PropertiesManager.props.setProperty(GatewayProperties.ZGD_DONGLE_TYPE_PROP_NAME, context.getProperty(GatewayProperties.ZGD_DONGLE_TYPE_PROP_NAME));
			if (context.getProperty(GatewayProperties.ZGD_GAL_ENABLE_LOG) != null)
				PropertiesManager.props.setProperty("debugEnabled", context.getProperty(GatewayProperties.ZGD_GAL_ENABLE_LOG));
			if (context.getProperty(GatewayProperties.ZGD_GAL_ENABLE_SERIAL_LOG) != null)
				PropertiesManager.props.setProperty("serialDataDebugEnabled", context.getProperty(GatewayProperties.ZGD_GAL_ENABLE_SERIAL_LOG));

			
			
			if (_fac == null)
				_fac = new GalExtenderProxyFactory(PropertiesManager);

			gatewayInterfaceServiceFactory = new GatewayInterfaceServiceFactory();
			gatewayInterfaceRegistration = bc.registerService(GatewayInterface.class.getName(), gatewayInterfaceServiceFactory, null);

			gatewayFactoryServiceFactory = new GatewayFactoryServiceFactory();
			gatewayFactoryRegistration = bc.registerService(GalExtenderProxyFactory.class.getName(), gatewayFactoryServiceFactory, null);

			LOG.info("Gal:Osgi Started!");
		} catch (Exception e) {
			if (_fac!= null)
				_fac.destroyGal();
			LOG.error("Error Creating Gal Osgi",e);
			
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
		if (_fac != null){

			_fac.destroyGal();
		}
		if (gatewayInterfaceServiceFactory != null) {
			if (gatewayInterfaceRegistration != null) {
				gatewayInterfaceRegistration.unregister();
				gatewayInterfaceRegistration = null;
			}
		}

		if (gatewayFactoryServiceFactory != null) {

			if (gatewayFactoryRegistration != null) {
				gatewayFactoryRegistration.unregister();
				gatewayFactoryRegistration = null;
			}
		}

		LOG.info("Gal Osgi Stopped!");
	}

	/**
	 * {@link GatewayInterface} Service Factory's implementation.
	 */
	public class GatewayInterfaceServiceFactory implements ServiceFactory {
		GatewayInterface gatewayInterface = null;


		public Object getService(Bundle bundle, ServiceRegistration reg) {
			try {
				gatewayInterface = _fac.createGatewayInterfaceObject();
				LOG.info("Called getService!");
				return gatewayInterface;
			} catch (Exception e) {
				LOG.error("Exception",e);
				return null;
			}
		}

		
		public void ungetService(Bundle bundle, ServiceRegistration reg, Object service) {
			try {
				((GalExtenderProxy) gatewayInterface).deleteProxy();
				
			} catch (Exception e) {
				LOG.error("Error deleting proxy from GAL",e);
			}

			LOG.info("Called UngetService!");
		}
	}

	/**
	 * {@link GalExtenderProxyFactory} Service Factory's implementation. It's a
	 * factory that provides a reference to a {@code GalExtenderProxyFactory}
	 * object.
	 */
	public class GatewayFactoryServiceFactory implements ServiceFactory {

		
		public Object getService(Bundle bundle, ServiceRegistration reg) {
			try {
				return _fac;
			} catch (Exception e) {
				LOG.error("Exception",e);
				return null;
			}
		}

		
		public void ungetService(Bundle bundle, ServiceRegistration reg, Object service) {

		}
	}

}
