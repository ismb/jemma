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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.MessageListener;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

/**
 * Implementation of {@code APSMessageListener} interface for the Rest server.
 * <p>
 * Rest clients interested to listen to Aps messages, resister themselves
 * indicating an uri, here called urilistener, where they are listening for
 * incoming notifications. In practice the clients opens an http server at the
 * urilistener uri where this class can {@code POST} incoming notifications.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class RestMessageListener implements MessageListener {

	private static final Logger LOG = LoggerFactory.getLogger( RestMessageListener.class );
	private Long CalbackIdentifier = -1L;
	private Callback callback;
	private String urilistener;
	private ClientResources clientResource;
	private final Context context;
	private PropertiesManager _PropertiesManager;

	/**
	 * Creates a new instance with a given callback, urilistener and client
	 * resource.
	 * <p>
	 * Rest clients interested to listen to Aps messages, resister themselves
	 * indicating an uri, here called urilistener, where they are listening for
	 * incoming notifications. In practice the clients opens an http server at
	 * the urilistener uri where this class can {@code POST} incoming
	 * notifications.
	 * 
	 * @param callback
	 *            the callback.
	 * @param urilistener
	 *            the urilistener.
	 * @param _clientResource
	 *            the client resource.
	 */
	public RestMessageListener(Callback callback, String urilistener, ClientResources _clientResource, PropertiesManager __PropertiesManager) {
		super();
		this.callback = callback;
		this.urilistener = urilistener;
		this.clientResource = _clientResource;
		this.context = new Context();
		this._PropertiesManager = __PropertiesManager;
		context.getParameters().add("socketTimeout", ((Integer) (_PropertiesManager.getHttpOptTimeout() * 1000)).toString());

	}

	/**
	 * Notification of an incoming Aps message.
	 */
	synchronized public void notifyAPSMessage(final APSMessageEvent message) {

		if (urilistener != null) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try {

						ClientResource resource = new ClientResource(context, urilistener);
						Info info = new Info();
						Info.Detail detail = new Info.Detail();
						detail.setAPSMessageEvent(message);
						info.setDetail(detail);
						info.setEventCallbackIdentifier(CalbackIdentifier);
						String xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Unmarshaled" + xml);
						
						resource.post(xml, MediaType.APPLICATION_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						clientResource.addToCounterException();

					}
				}
			};
			thr.start();
		}

	}

	/**
	 * Gets the urilistener.
	 * 
	 * @return the urilistener.
	 */
	public String getUrilistener() {
		return urilistener;
	}

	/**
	 * Gets the callback.
	 * 
	 * @return the callback.
	 */
	public Callback getCallback() {
		return callback;
	}

	/**
	 * Sets the callback id.
	 * 
	 * @param id
	 *            the callback id to set.
	 */
	public void setCallBackId(Long id) {

		CalbackIdentifier = id;

	}

	@Override
	public void notifyInterPANMessage(final InterPANMessageEvent message) {
		if (urilistener != null) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try {

						ClientResource resource = new ClientResource(context, urilistener);
						Info info = new Info();
						Info.Detail detail = new Info.Detail();
						detail.setInterPANMessageEvent(message);
						info.setDetail(detail);
						info.setEventCallbackIdentifier(CalbackIdentifier);
						String xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Unmarshaled" + xml);
						resource.post(xml, MediaType.APPLICATION_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						clientResource.addToCounterException();

					}
				}
			};
			thr.start();
		}
		
	}

}
