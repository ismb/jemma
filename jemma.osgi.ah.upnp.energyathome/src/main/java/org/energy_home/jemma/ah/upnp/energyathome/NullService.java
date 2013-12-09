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

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPService;
import org.osgi.service.upnp.UPnPStateVariable;

public class NullService implements UPnPService {

	private static final String ID = "urn:upnp-org:serviceId:NullService";
	private static final String type = "urn:schemas-upnp-org:service:NullService:1";
	private static final String version = "1";

	public UPnPAction getAction(String arg0) {
		return null;
	}

	public UPnPAction[] getActions() {
		// FIXME: the UPnP implementation we use must accept null as return
		// value. Unfortunately it does not!!!
		return new UPnPAction[0];
	}

	public String getId() {
		return ID;
	}

	public UPnPStateVariable getStateVariable(String arg0) {
		return null;
	}

	public UPnPStateVariable[] getStateVariables() {
		// FIXME: the UPnP implementation we use must accept null as return
		// value. Unfortunately it does not!!!
		return new UPnPStateVariable[0];
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}
}
