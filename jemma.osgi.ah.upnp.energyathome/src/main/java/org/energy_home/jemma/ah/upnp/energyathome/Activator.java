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
package org.energy_home.jemma.ah.upnp.energyathome;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.upnp.UPnPDevice;

public class Activator implements BundleActivator {

	private ServiceRegistration upnpReg = null;

	private HacUPnPDevice upnpDevice = null;

	public BundleContext bc;

	public void start(BundleContext bc) throws BundleException {
		this.bc = bc;
		upnpDevice = new HacUPnPDevice();
		upnpReg = bc.registerService(UPnPDevice.class.getName(), upnpDevice, upnpDevice.getDescriptions(""));

	}

	public void stop(BundleContext bc) throws BundleException {
		if (upnpReg != null) {
			upnpReg.unregister();
			upnpReg = null;
		}
	}
}
