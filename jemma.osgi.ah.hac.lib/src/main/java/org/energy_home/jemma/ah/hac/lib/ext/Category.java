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

import org.energy_home.jemma.ah.hac.ICategory;

/**
 * This class provides a straightforward implementation for the Category @see
 * Category interface.
 * 
 * @author 00918161
 * 
 */
public class Category implements ICategory {
	String name = null;
	String iconName = null;
	String pid = null;

	public Category(String pid, String name, String iconName) {
		this.pid = pid;
		this.name = name;
		this.iconName = iconName;
	}

	public String getName() {
		return name;
	}

	public String getIconName() {
		return iconName;
	}

	public String getPid() {
		return this.pid;
	}
}
