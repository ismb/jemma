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
package org.energy_home.jemma.javagal.rest.util;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.rest.PropertiesManager;
import org.energy_home.jemma.javagal.rest.RestApsMessageListener;
import org.energy_home.jemma.javagal.rest.RestClientManagerAndListener;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.zgd.GalExtenderProxy;
import org.energy_home.jemma.zgd.GatewayInterface;

/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ClientResources {
	private RestManager restManager;
	private ClientKey clientKey;

	public ClientResources(PropertiesManager _propertiesManager,
			GatewayInterface _gatewayInterface, ClientKey _clientKey,
			RestManager _restManager) {
		this.gatewayInterface = _gatewayInterface;
		this.propertiesManager = _propertiesManager;
		this.restManager = _restManager;
		this.clientKey = _clientKey;
	}

	private Log logger = LogFactory.getLog(ClientResources.class);
	private ConcurrentHashMap<Long, RestApsMessageListener> callbacksEventListeners = new ConcurrentHashMap<Long, RestApsMessageListener>();
	private RestClientManagerAndListener clientEventListener = null;
	private PropertiesManager propertiesManager = null;
	private int counterException;

	public synchronized void resetCounter() {
		counterException = 0;
	}

	public synchronized void addToCounterException() {
		counterException = counterException + 1;
		if (counterException > restManager.getPropertiesManager()
				.getnumberOfConnectionFail()) {
			if (propertiesManager.getDebugEnabled())
				logger.info("Deleting Client...");
			try {
				((GalExtenderProxy) gatewayInterface).deleteProxy();
				restManager.removeClientObjectKey(clientKey);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void setGatewayEventListener() {
		if (clientEventListener == null) {
			clientEventListener = new RestClientManagerAndListener(
					propertiesManager, this);

			gatewayInterface.setGatewayEventListener(clientEventListener);
			if (propertiesManager.getDebugEnabled())
				logger.info("\n\rGateway Event listener registered!\n\r");

			if (propertiesManager.getDebugEnabled())
				logger.info("\n\rGateway Event listener registered!\n\r");
		}
	}

	private GatewayInterface gatewayInterface;

	public GatewayInterface getGatewayInterface() {
		return gatewayInterface;
	}

	public void setGatewayInterface(GatewayInterface gatewayInterface) {
		this.gatewayInterface = gatewayInterface;
	}

	public void setCallbacksEventListeners(
			ConcurrentHashMap<Long, RestApsMessageListener> callbacksEventListeners) {
		this.callbacksEventListeners = callbacksEventListeners;
	}

	public synchronized RestClientManagerAndListener getClientEventListener() {
		return clientEventListener;
	}

	public synchronized void setClientEventListener(
			RestClientManagerAndListener clientEventListener) {
		this.clientEventListener = clientEventListener;
	}

	public synchronized ConcurrentHashMap<Long, RestApsMessageListener> getCallbacksEventListeners() {
		return callbacksEventListeners;
	}

}
