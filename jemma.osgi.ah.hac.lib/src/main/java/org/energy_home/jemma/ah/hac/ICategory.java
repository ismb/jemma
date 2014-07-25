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
 * Represents a device category.
 */
public interface ICategory {

	/**
	 * Used to retrieve the unique Persistent IDentifier (PID) for the category
	 * 
	 * @return The category PID
	 */
	public String getPid();

	/**
	 * Used to query the category name
	 * 
	 * @return The category name
	 */
	public String getName();

	/**
	 * Used to retrieve the icon filename associated to the category interface.
	 * 
	 * @return The category icon filename (i.e. "oven.png"). The user interface
	 *         will look for this filename in standard directories. If an icon
	 *         with such a filename is not found, a default one is selected for
	 *         representing the category on the user interface
	 */
	public String getIconName();

}
