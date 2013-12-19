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

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.Info;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class RestApsMessageListener implements APSMessageListener {

	private Log log = LogFactory.getLog(RestApsMessageListener.class);
	private Long CalbackIdentifier = -1L;
	private Callback callback;
	private String urilistener;
	private ClientResources clientResource;
	
	
	
	public RestApsMessageListener(Callback callback, String urilistener, ClientResources _clientResource) {
		super();
		this.callback = callback;
		this.urilistener = urilistener;
		this.clientResource = _clientResource;
	
	}

	synchronized public void notifyAPSMessage(final APSMessageEvent message) {

		if (urilistener != null) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try
					{
								
					ClientResource resource = new ClientResource(urilistener);
					Info info = new Info();
					Info.Detail detail = new Info.Detail();
					detail.setAPSMessageEvent(message);
					info.setDetail(detail);
					info.setEventCallbackIdentifier(CalbackIdentifier);
					String xml = Util.marshal(info);
					resource.post(xml, MediaType.APPLICATION_XML);
					resource.release();
					resource = null;
					clientResource.resetCounter();
					}
					catch(Exception e)
					{
						clientResource.addToCounterException();
						
					}
				}
			};
			thr.start();
		}

	}

	public String getUrilistener() {
		return urilistener;
	}

	public Callback getCallback() {
		return callback;
	}

	public void setCallBackId(Long id) {

		CalbackIdentifier = id;

	}
	
	
	
	
}
