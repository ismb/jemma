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

import org.energy_home.jemma.zgd.jaxb.Callback;
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ZDOCallbackEntry {
	private Long proxyIdentifier;
	private Callback callback;
	// private APSMessageListener destination;
	private short TranseqNumber;

	// public APSMessageListener getDestination() {
	// return destination;
	// }
	//
	// public void setDestination(APSMessageListener destination) {
	// this.destination = destination;
	// }

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public short getTranseqNumber() {
		return TranseqNumber;
	}

	public void setTranseqNumber(short transeqNumber) {
		TranseqNumber = transeqNumber;
	}

	public Long getProxyIdentifier() {
		return proxyIdentifier;
	}

	public void setProxyIdentifier(Long proxyIdentifier) {
		this.proxyIdentifier = proxyIdentifier;
	}
}