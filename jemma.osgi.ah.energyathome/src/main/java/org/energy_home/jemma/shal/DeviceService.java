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

// ***** OSGi service associated to a Device exposed in the OSGi framework (a device service could be exposed also by a server application)
public interface DeviceService extends EndPoint {
	// Mandatory property for the end point associated OSGi service (DeviceService and DeviceServiceListener)
	public static final String DEVICE_TYPE_PROPERTY = "shal.device.type";
	
	// Mandatory property for the device associated OSGi service (DeviceService and DeviceServiceListener)
	public static final String DEVICE_NETWORK_TYPE_PROPERTY = "shal.device.network";

	// Mandatory property for the end point associated OSGi service (DeviceService and DeviceServiceListener)
	public static final String DEVICE_INTERFACE_TYPES_PROPERTY = "shal.device.interfaces";
	
	public DeviceInterface[] getDeviceInterfaces();
	
	public DeviceInterface getDeviceInterface(Class<? extends DeviceInterface> clazz);
}
