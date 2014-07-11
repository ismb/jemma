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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.lib.ext.Location;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationsService implements ManagedServiceFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(LocationsService.class);
	
	private ConfigurationAdmin configAdmin;
	
	static final String FACTORY_PID = "org.energy_home.jemma.osgi.ah.hac.locations";
	
	public LocationsService() {
		
	}

	public void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}
	
	public void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {		
		if (this.configAdmin == configAdmin) {
			this.configAdmin = null;
		}
	}
	
	private Hashtable name2location = new Hashtable();
	private Hashtable pid2location = new Hashtable();

	public ILocation[] getLocations() {
		if (pid2location.size() == 0)
			return null;
		
		ILocation[] locationArray = new ILocation[pid2location.size()];
		return (ILocation[]) pid2location.values().toArray(locationArray);
	}

	public Location add(Location location) throws HacException {
		Configuration[] configurations;
		try {
			configurations = this.configAdmin.listConfigurations("(" + ILocation.PROP_LOCATION_NAME + "=" + location.getName() + ")");
			if (configurations!= null) {
				throw new HacException("Duplicate location name");
			}
			Dictionary props = new Hashtable();
			props.put(ILocation.PROP_LOCATION_NAME, location.getName());
			props.put(ILocation.PROP_LOCATION_ICON, location.getIconName());
			props.put(ILocation.PROP_LOCATION_PID, location.getPid());
			Configuration c = this.configAdmin.createFactoryConfiguration(FACTORY_PID);			
			c.update(props);
			
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
		return location;
	}

	public void clear() {		
		try {
			Configuration[] configurations = configAdmin.listConfigurations("(org.energy_home.jemma.ah.location.name=*)");
			if (configurations != null)
				for (int i = 0; i < configurations.length; i++) {
					configurations[i].delete();
				}
		} catch (IOException e) {
			LOG.warn(e.getMessage(), e);
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}

	public Location getByPid(String pid) {
		return (Location) this.pid2location.get(pid);
	}

	public void deleted(String pid) {
		
		Location location = null;
		try {
			location = (Location) pid2location.remove(pid);
		} catch (Throwable e) {
			LOG.warn(e.getMessage(), e);
			return;
		}
		
		if (location != null) {
			name2location.remove(location.getName());
		}
	}

	public String getName() {
		return null;
	}

	public void updated(String pid, Dictionary props) throws ConfigurationException {
		Location location = (Location) pid2location.get(pid);
		if (location == null) {
			location = new Location();
			location.update(props);
			name2location.put(location.getName(), location);
			pid2location.put(pid, location);
		}
		else {
			location.update(props);
		}
	}
}
