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

import org.energy_home.jemma.ah.hac.lib.Appliance;

/**
 * Provides the description of an appliance class
 * 
 * @see Appliance
 * 
 */
public interface IApplianceDescriptor {
	/**
	 * Returns the appliance type name associated to an appliance class
	 * 
	 * @return The appliance type name
	 */
	public String getType();

	/**
	 * Returns the device type associated to an appliance class
	 * 
	 * @return The physical device type if this descriptor refers to a {@code
	 *         driver appliance}, {@code null} otherwise
	 */
	public String getDeviceType();

	/**
	 * Returns a friendly name associated to an appliance class (the friendly
	 * name is used by the A@H framework to generate a unique initial name for
	 * each instance of the appliance class).
	 * 
	 * @return The friendly name of an appliance
	 */
	public String getFriendlyName();
}
