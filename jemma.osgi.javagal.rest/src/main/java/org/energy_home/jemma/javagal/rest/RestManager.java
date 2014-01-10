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
package org.energy_home.jemma.javagal.rest;


import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.rest.util.ClientKey;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class RestManager {
	private final ConcurrentHashMap<ClientKey, ClientResources> clientsMap = new ConcurrentHashMap<ClientKey, ClientResources>();
	private boolean proxyActive = false;
	private GalExtenderProxyFactory factory;
	private Log logger = LogFactory.getLog(RestManager.class);
	private Component component;
	private PropertiesManager _PropertiesManager = null;

	public PropertiesManager getPropertiesManager() {
		return _PropertiesManager;
	}

	public RestManager(PropertiesManager __PropertiesManager,
			GalExtenderProxyFactory factory) {
		this.proxyActive = true;
		this.factory = factory;
		_PropertiesManager = __PropertiesManager;
		// Create a new Component.
		component = new Component();
		// Add a new server listening on port.

		Server _newserver = new Server(Protocol.HTTP,
				_PropertiesManager.getIPPort());

		component.getServers().add(_newserver);

		// Attach the sample application.
		GalManagerRestApplication gmra = new GalManagerRestApplication(this);
		component.getDefaultHost().attach("", gmra);

		// Start the component.
		try {
			component.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean isProxyActive() {
		return this.proxyActive;
	}

	public synchronized void setProxyActive(boolean proxyActive) {
		this.proxyActive = proxyActive;
	}

	public GalExtenderProxyFactory getFactory() {
		return factory;
	}

	public void stopServer() {
		try {
			component.stop();
		} catch (Exception e) {
			if (_PropertiesManager.getDebugEnabled())
				logger.error("Error stopping rest server component:"
						+ e.getMessage());
		}

	}

	public void deleteFactory() {
		factory = null;

	}

	synchronized public ClientResources getClientObjectKey(int port,
			String address) throws Exception {
		if (!isProxyActive())
			return null;

		ClientKey clientKey = new ClientKey();

		// ...looking for the correspondence on the map
		ClientResources myClientToReturn = null;
		synchronized (clientsMap) {
			// Creating the key to look for on the map...
			clientKey.setPort(port);
			clientKey.setAddress(address);
			myClientToReturn = clientsMap.get(clientKey);
			if (myClientToReturn == null && clientKey.getPort() > -1) {
				clientKey.setPort(-1);
				myClientToReturn = clientsMap.get(clientKey);
				clientKey.setPort(port);
				if (myClientToReturn != null) {
					clientsMap.remove(clientKey);
					clientKey.setPort(port);
					clientsMap.put(clientKey, myClientToReturn);
				}

			} else if (myClientToReturn == null && clientKey.getPort() == -1) {
				for (Iterator<Entry<ClientKey, ClientResources>> it = clientsMap.entrySet().iterator(); it
						.hasNext();) {
					ClientKey p = (ClientKey) it.next().getKey();
					if (p.getAddress().equals(clientKey.getAddress())) {
						myClientToReturn = clientsMap.get(p);
						break;
					}
				}

			}

		}

		if (myClientToReturn != null) {
			if (clientKey.getPort() > -1)
				myClientToReturn.setGatewayEventListener();
			if (getPropertiesManager().getDebugEnabled()) {
				logger.info("***************Get proxy client:");
				logger.info("Port: " + clientKey.getPort());
				logger.info("Address: " + clientKey.getAddress());
			}

		} else {
			myClientToReturn = new ClientResources(getPropertiesManager(),
					getFactory().createGatewayInterfaceObject(), clientKey,
					this);
			if (clientKey.getPort() > -1)
				myClientToReturn.setGatewayEventListener();
			synchronized (clientsMap) {
				clientsMap.put(clientKey, myClientToReturn);
			}

			if (getPropertiesManager().getDebugEnabled()) {
				logger.info("***************New Gal proxy client created.");
				logger.info("Port: " + clientKey.getPort());
				logger.info("Address: " + clientKey.getAddress());
			}

		}
		return myClientToReturn;
	}

	synchronized public void removeClientObjectKey(ClientKey key)
			throws Exception {
		if (clientsMap.containsKey(key))
			clientsMap.remove(key);

	}

}
