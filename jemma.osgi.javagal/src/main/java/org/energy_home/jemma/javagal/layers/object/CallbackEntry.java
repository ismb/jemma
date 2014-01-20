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
package org.energy_home.jemma.javagal.layers.object;

import org.energy_home.jemma.javagal.layers.business.GalController;

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.jaxb.Callback;


/**
 * Callback entry class carrying the actual callback, the destination and the
 * {@code apsCallbackIdentifier}. The entry will be then inserted in
 * {@link GalController#listCallback}. 
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class CallbackEntry {
	private int proxyIdentifier;
	private Long apsCallbackIdentifier;
	private Callback callback;
	private APSMessageListener destination;

	/**
	 * Creates a new empty {@link Callback} entry instance.
	 */
	public CallbackEntry() {
	}

	/**
	 * Gets the destination's APS message listener.
	 * 
	 * @return the destination.
	 */
	public APSMessageListener getDestination() {
		return destination;
	}

	/**
	 * Sets the destination's APS message listener.
	 * 
	 * @param destination
	 *            the APS message listener to set as destination.
	 */
	public void setDestination(APSMessageListener destination) {
		this.destination = destination;
	}

	/**
	 * Gets the {@code Callback} this object refers to.
	 * 
	 * @return the carried {@code Callback}.
	 */
	public Callback getCallback() {
		return callback;
	}

	/**
	 * Sets the {@code Callback} this object refers to.
	 * 
	 * @param callback
	 *            the {@code Callback} to carry in this object.
	 */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	/**
	 * Gets the callback's identifier.
	 * 
	 * @return the callback's identifier.
	 */
	public Long getApsCallbackIdentifier() {
		return apsCallbackIdentifier;
	}

	/**
	 * Sets the callback's identifier.
	 * 
	 * @param apsCallbackIdentifier
	 *            the callback's identifier.
	 */
	public void setApsCallbackIdentifier(Long apsCallbackIdentifier) {
		this.apsCallbackIdentifier = apsCallbackIdentifier;
	}

	/**
	 * Gets the proxy's identifier for this callback entry.
	 * 
	 * @return the proxy identifier.
	 */
	public int getProxyIdentifier() {
		return proxyIdentifier;
	}

	/**
	 * Sets the proxy's identifier for this callback entry.
	 * 
	 * @param proxyIdentifier
	 *            the proxy identifier to set.
	 */
	public void setProxyIdentifier(int proxyIdentifier) {
		this.proxyIdentifier = proxyIdentifier;
	}
}