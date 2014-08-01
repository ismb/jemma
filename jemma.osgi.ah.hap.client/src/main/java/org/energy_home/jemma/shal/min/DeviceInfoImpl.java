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
package org.energy_home.jemma.shal.min;

import org.energy_home.jemma.shal.DeviceConfiguration;
import org.energy_home.jemma.shal.DeviceDescriptor;
import org.energy_home.jemma.shal.DeviceInfo;

public class DeviceInfoImpl implements DeviceInfo {

	private String endPointId;
	private String persistentId;
	private DeviceDescriptor descriptor;
	private DeviceConfiguration configuration;

	public DeviceInfoImpl(String endPointId, String persistentId, DeviceDescriptor descriptor, DeviceConfiguration configuration) {
		this.endPointId = endPointId;
		this.persistentId = persistentId;
		this.descriptor = descriptor;
		this.configuration = configuration;
	}
	
	public String getEndPointId() {
		return endPointId;
	}

	public String getPersistentId() {
		return persistentId;
	}

	public DeviceDescriptor getDescriptor() {
		return descriptor;
	}

	public DeviceConfiguration getConfiguration() {
		return configuration;
	}

	public String toString() {
		return "DeviceInfo [endPointId=" + endPointId + ", persistentId=" + persistentId + ", descriptor=" + descriptor
				+ ", configuration=" + configuration + "]";
	}

}
