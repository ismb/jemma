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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.lib.ext.Location;

public class Locations {
	private Vector locations = new Vector();
	private Hashtable name2location = new Hashtable();
	private Hashtable pid2location = new Hashtable();

	public ILocation[] getLocations() {
		if (locations == null || locations.size() == 0)
			return null;
		ILocation[] locationArray = new ILocation[locations.size()];
		return (ILocation[]) locations.toArray(locationArray);
	}

	public void add(Location location) throws HacException {
		if (pid2location.get(location.getName()) == null) {
			name2location.put(location.getName(), location);
			pid2location.put(location.getPid(), location);
			locations.add(location);
		} else {
			throw new HacException("Duplicate category exception");
		}
	}

	public void clear() {
		this.locations.clear();
		this.name2location.clear();
		this.pid2location.clear();
	}

	public Location getByPid(String pid) {
		return (Location) this.pid2location.get(pid);
	}

	public Iterator iterator() {
		return locations.iterator();
	}
}
