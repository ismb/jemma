/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.javagal.layers.object;

import org.energy_home.jemma.javagal.layers.business.GalController;

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.jaxb.Callback;


/**
 * Callback entry class carrying the actual callback, the destination and the
 * {@code apsCallbackIdentifier}. The entry will be then inserted in
 * {@link GalController#listCallback}. 
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class CallbackEntry {
	private int proxyIdentifier;
	private Long apsCallbackIdentifier;
	private Callback callback;
	private APSMessageListener destination;

	public CallbackEntry() {
	}

	public APSMessageListener getDestination() {
		return destination;
	}

	public void setDestination(APSMessageListener destination) {
		this.destination = destination;
	}

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public Long getApsCallbackIdentifier() {
		return apsCallbackIdentifier;
	}

	public void setApsCallbackIdentifier(Long apsCallbackIdentifier) {
		this.apsCallbackIdentifier = apsCallbackIdentifier;
	}

	public int getProxyIdentifier() {
		return proxyIdentifier;
	}

	public void setProxyIdentifier(int proxyIdentifier) {
		this.proxyIdentifier = proxyIdentifier;
	}
}