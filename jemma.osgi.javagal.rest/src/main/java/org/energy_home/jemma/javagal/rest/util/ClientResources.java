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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.rest.Activator;
import org.energy_home.jemma.javagal.rest.PropertiesManager;
import org.energy_home.jemma.javagal.rest.RestApsMessageListener;
import org.energy_home.jemma.javagal.rest.RestMessageListener;
import org.energy_home.jemma.javagal.rest.RestClientManagerAndListener;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.zgd.GalExtenderProxy;
import org.energy_home.jemma.zgd.GatewayInterface;

/**
 * Resource's class for a Rest client.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ClientResources {
	private RestManager restManager;
	private ClientKey clientKey;

	/**
	 * Creates a new instance.
	 * 
	 * @param _propertiesManager
	 *            the properties manager.
	 * @param _gatewayInterface
	 *            the gateway interface.
	 * @param _clientKey
	 *            the client key object.
	 * @param _restManager
	 *            the rest manager.
	 */
	public ClientResources(PropertiesManager _propertiesManager,
			GatewayInterface _gatewayInterface, ClientKey _clientKey,
			RestManager _restManager) {
		this.gatewayInterface = _gatewayInterface;
		this.propertiesManager = _propertiesManager;
		this.restManager = _restManager;
		this.clientKey = _clientKey;
	}

	private static final Logger LOG = LoggerFactory.getLogger( ClientResources.class );
	private ConcurrentHashMap<Long, RestMessageListener> messageCallbacksEventListeners = new ConcurrentHashMap<Long, RestMessageListener>();
	private ConcurrentHashMap<Long, RestApsMessageListener> messageApscallbacksEventListeners = new ConcurrentHashMap<Long, RestApsMessageListener>();
	
	private RestClientManagerAndListener clientEventListener = null;
	private PropertiesManager propertiesManager = null;
	private int counterException;

	/**
	 * Resets the exception's counter.
	 */
	public synchronized void resetCounter() {
		counterException = 0;
	}

	public synchronized void addToCounterException() {
		counterException = counterException + 1;
		if (counterException > restManager.getPropertiesManager()
				.getnumberOfConnectionFail()) {
			if (propertiesManager.getDebugEnabled())
				LOG.debug("Deleting Client...");
			try {
				((GalExtenderProxy) gatewayInterface).deleteProxy();
				restManager.removeClientObjectKey(clientKey);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * Associates a brand new {@code RestClientManagerAndListener} to a Rest
	 * client.
	 */
	public void setGatewayEventListener() {
		if (clientEventListener == null) {
			clientEventListener = new RestClientManagerAndListener(
					propertiesManager, this);

			gatewayInterface.setGatewayEventListener(clientEventListener);
			if (propertiesManager.getDebugEnabled())
				LOG.debug("Gateway Event listener registered!");

			if (propertiesManager.getDebugEnabled())
				LOG.debug("Gateway Event listener registered!");
		}
	}

	private GatewayInterface gatewayInterface;

	/**
	 * Gets the gateway interface associated to this client.
	 * 
	 * @return the gateway interface.
	 */
	public GatewayInterface getGatewayInterface() {
		return gatewayInterface;
	}

	/**
	 * Sets the gateway interface associated to this client.
	 * 
	 * @param gatewayInterface
	 *            the gateway interface to set.
	 */
	public void setGatewayInterface(GatewayInterface gatewayInterface) {
		this.gatewayInterface = gatewayInterface;
	}

	/**
	 * Sets the map of callbacks event listeners.
	 * 
	 * @param callbacksEventListeners
	 *            the map of callbacks event listeners to set.
	 */
	public void setmessageCallbacksEventListeners(
			ConcurrentHashMap<Long, RestMessageListener> callbacksEventListeners) {
		this.messageCallbacksEventListeners = callbacksEventListeners;
	}
	
	
	
	/**
	 * Sets the map of callbacks event listeners.
	 * 
	 * @param callbacksEventListeners
	 *            the map of callbacks event listeners to set.
	 */
	public void setMessageApsCallbacksEventListeners(
			ConcurrentHashMap<Long, RestApsMessageListener> callbacksEventListeners) {
		this.messageApscallbacksEventListeners = callbacksEventListeners;
	}

	/**
	 * Gets the client event listener associated to this client.
	 * 
	 * @return the client event listener associated to this client.
	 */
	public synchronized RestClientManagerAndListener getClientEventListener() {
		return clientEventListener;
	}

	/**
	 * Sets the client event listener associated to this client.
	 * 
	 * @param clientEventListener
	 *            the client event listener to set.
	 */
	public synchronized void setClientEventListener(
			RestClientManagerAndListener clientEventListener) {
		this.clientEventListener = clientEventListener;
	}

	/**
	 * Gets the map of callbacks event listeners.
	 * 
	 * @return the map of callbacks event listeners.
	 */
	public synchronized ConcurrentHashMap<Long, RestApsMessageListener> getApsCallbacksEventListeners() {
		return messageApscallbacksEventListeners;
	}
	
	
	/**
	 * Gets the map of callbacks event listeners.
	 * 
	 * @return the map of callbacks event listeners.
	 */
	public synchronized ConcurrentHashMap<Long, RestMessageListener> getCallbacksEventListeners() {
		return messageCallbacksEventListeners;
	}

}
