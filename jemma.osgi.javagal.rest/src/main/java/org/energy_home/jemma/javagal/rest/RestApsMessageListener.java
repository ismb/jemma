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

import org.energy_home.jemma.javagal.rest.util.ThreadPoolManager;
import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.restlet.Context;

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
public class RestApsMessageListener implements APSMessageListener {

    private Long CalbackIdentifier = -1L;
    private String urilistener;
    private final Context context;
	private PropertiesManager _PropertiesManager;
    private ThreadPoolManager manager;
	/**
	 * Creates a new instance with a given callback, urilistener and client
	 * resource.
	 * <p>
	 * Rest clients interested to listen to Aps messages, resister themselves
	 * indicating an uri, here called urilistener, where they are listening for
	 * incoming notifications. In practice the clients opens an http server at
	 * the urilistener uri where this class can {@code POST} incoming
	 * notifications.
     * @param urilistener
     *            the urilistener.
     *
     */
	public RestApsMessageListener(String urilistener, PropertiesManager __PropertiesManager) {
		super();
        this.urilistener = urilistener;
        this.context = new Context();
		this._PropertiesManager = __PropertiesManager;
		context.getParameters().add("socketTimeout", ((Integer) (_PropertiesManager.getHttpOptTimeout() * 1000)).toString());
        manager = ThreadPoolManager.getInstance();
	}

	/**
	 * Notification of an incoming Aps message.
	 */
	synchronized public void notifyAPSMessage(final APSMessageEvent message) {

		if (urilistener != null)
            manager.notifyAPSMessage(context, message, CalbackIdentifier, urilistener);
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
	 * Sets the callback id.
	 * 
	 * @param id
	 *            the callback id to set.
	 */
	public void setCallBackId(Long id) {

		CalbackIdentifier = id;

	}



}
