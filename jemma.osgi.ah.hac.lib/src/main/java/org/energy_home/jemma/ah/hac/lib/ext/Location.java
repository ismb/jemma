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
package org.energy_home.jemma.ah.hac.lib.ext;

import java.util.Dictionary;

import org.energy_home.jemma.ah.hac.ILocation;

/**
 * Implementation of the {@link org.energy_home.jemma.ah.hac.ILocation} interface
 */
public class Location implements ILocation {
	
	String name = null;
	String iconName = null;
	String pid = null;

	/**
	 * Constructor to create a LocationImpl class with the requested parameters
	 * 
	 * @param pid
	 *            The location persistent identifier.
	 * @param name
	 *            The Location name
	 * @param iconName
	 *            The icon filename
	 */
	public Location(String pid, String name, String iconName) {
		this.name = name;
		this.iconName = iconName;
		this.pid = pid;
	}
	
	public Location() {
	}

	public String getName() {
		return name;
	}

	public String getIconName() {
		return iconName;
	}

	/**
	 * Compares two Objects implementing the Location interface and returns true
	 * if they represent the same location. Two location must be considered
	 * equal, if they have the same persistent identifier.
	 * 
	 * @return true if the two object represents the same location
	 */

	public boolean equals(Object location) {
		if (location instanceof ILocation) {
			// FIXME: two locations are the same if the pid is the same!!!! Here
			// we compares names, instead.
			return (name.compareToIgnoreCase(((ILocation) location).getName()) == 0);
		}

		return false;
	}

	public String getPid() {
		return pid;
	}

	public void update(Dictionary props) {
		String name = (String) props.get(ILocation.PROP_LOCATION_NAME);
		String icon = (String) props.get(ILocation.PROP_LOCATION_ICON);
		String pid = (String) props.get(ILocation.PROP_LOCATION_PID);
		this.name = name;
		this.iconName = icon;
		this.pid = pid;
	}
}
