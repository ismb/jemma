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
package org.energy_home.jemma.ah.hac;

import org.osgi.service.device.Device;

/**
 * Represents a physical device managed by the A@H framework.
 * 
 * @see IManagedAppliance#getAttachedDevice()
 * @see org.osgi.device.service.Device
 * 
 */
public interface IHacDevice extends Device {
	/**
	 * Retrieves the Persistent IDentifier associated to the Device service
	 * registered by the base driver in the OSGi framework
	 * 
	 * @return The {@code String} representing the persistent identifier of this
	 *         device
	 */
	public String getPid();
	
	/**
	 * Force Removal of the Device
	 */
	public void remove();
}
