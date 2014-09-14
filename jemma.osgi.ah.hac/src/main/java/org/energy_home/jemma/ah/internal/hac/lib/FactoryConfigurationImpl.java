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

import org.osgi.service.cm.Configuration;

public class FactoryConfigurationImpl implements Configuration {

	Dictionary props = new Hashtable();
	private String pid;
	private String bundleLocation;
	private Dictionary configurations = new Hashtable();

	public FactoryConfigurationImpl(String pid) {
		this.pid = pid;
	}

	public void delete() throws IOException {
	}

	public String getBundleLocation() {
		return null;
	}

	public String getFactoryPid() {
		return this.pid;
	}

	public String getPid() {
		return pid;
	}

	public Dictionary getProperties() {
		return null;
	}

	public void setBundleLocation(String bundleLocation) {
		this.bundleLocation = bundleLocation;
	}

	public void update() throws IOException {
	}

	public void update(Dictionary pid) throws IOException {
	}

	public Configuration getConfiguration(String pid) {
		return (Configuration) this.configurations.get(pid);
	}

	public void put(Configuration configuration) {
		this.configurations.put(configuration.getPid(), configuration);
	}

	@Override
	public long getChangeCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
