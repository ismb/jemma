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
package org.energy_home.jemma.zgd;

import java.math.BigInteger;
import java.net.URI;
import java.net.URL;

import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.GalController;

/**
 * Factory class for {@link GalExtenderProxy} objects. Every object created by
 * this factory is a separate and independent proxy to the unique
 * {@link GalController} instance and is identified by its own proxy identifier
 * id.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GalExtenderProxyFactory {

	/**
	 * Gal Controller reference.
	 */
	GalController gal = null;

	/**
	 * Uniquely identifies the gal proxy instance.
	 */
	private int proxyIdentifierSequence;

	/**
	 * Constructs a GalExtender Proxy Factory.
	 * 
	 * @param _prop
	 *            the properties manager whose properties are to be used by this
	 *            instance
	 * @throws Exception
	 *             if the {@link GalController} contructor fails due to some
	 *             internal error.
	 */
	public GalExtenderProxyFactory(PropertiesManager _prop) throws Exception {
		proxyIdentifierSequence = 0;
		gal = new GalController(_prop);

	}

	/**
	 * Creates a gateway interface with its own proxy identifier.
	 * 
	 * @return a newly created gateway interface
	 * @throws Exception
	 *             if the {@link GalExtenderProxy} constructor fails due to some
	 *             internal error.
	 */
	public synchronized GatewayInterface createGatewayInterfaceObject() throws Exception {
		proxyIdentifierSequence++;
		return new GalExtenderProxy(proxyIdentifierSequence, gal);

	}

	/**
	 * Destroys the Gal controller instance.
	 * 
	 * @throws Exception
	 *             if the gal controller fails to disconnect the dongle.
	 */
	public synchronized void destroyGal() throws Exception {
		if (gal != null) {
			gal.getDataLayer().destroy();
			gal.getDataLayer().getIKeyInstance().disconnect();
			gal = null;
		}
	}

	/**
	 * Get the ExtendedPanId of the Gal.
	 * 
	 * @throws Exception.
	 */
	public BigInteger getExtendedPanId() {
		return gal.getPropertiesManager().getExtendedPanId();
	}

}
