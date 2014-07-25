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
package org.energy_home.jemma.shal;

public interface EndPoint {
	// Mandatory property for the DeviceListener OSGi service (specifies an array of strings with all requested services)
	public static final String END_POINT_CLIENT_SERVICE_TYPES_PROPERTY = "shal.client.interfaces";
	
	// Each id is composed by a prefix associated to the HAN network and a unique id of the device (e.g. mac address):
	// zigbee.<mac address>/<end point id>
	// A specific prexic can be used to identify appliancation components: app.<unique id>/<numeric id> 
	// (es. app.esp, app.aal, app.esp/1)
	public String getEndPointId();
}
