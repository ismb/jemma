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

/**
 * Represents a location. The Location is used in the A@H framework to specify
 * where an appliance is located in the house. A Location has a Name. It has a
 * graphical representation, and may be compared with other locations.
 */

public interface ILocation {
	
	public static final String PROP_LOCATION_NAME = "org.energy_home.jemma.ah.location.name";
	public static final String PROP_LOCATION_PID = "org.energy_home.jemma.ah.location.pid";
	public static final String PROP_LOCATION_ICON = "org.energy_home.jemma.ah.location.icon";
	
	/**
	 * Returns the location name (i.e. "Kitchen").
	 * 
	 * @return The location name
	 */
	public String getName();

	/**
	 * Retrieves the filename of the icon that have to be used by the user
	 * interface to represent graphically this location. Valid examples of icon
	 * filename are ("garden.png", "kitchen.png"). The location filename must
	 * not include the path.
	 * 
	 * @return The icon filename
	 */

	public String getIconName();

	/**
	 * Gets the Location pid, the persistent id used to identify the location in
	 * the system. The location pid is unique over all the objects implementing
	 * the location interface.
	 * 
	 * @return The PID associated to this location
	 */
	public String getPid();
}
