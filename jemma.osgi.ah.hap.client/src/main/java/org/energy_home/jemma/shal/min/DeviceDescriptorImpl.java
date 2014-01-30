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

import java.util.Arrays;

import org.energy_home.jemma.shal.DeviceDescriptor;

public class DeviceDescriptorImpl implements DeviceDescriptor {

	private DeviceType deviceType;
	private NetworkType networkType;
	private String[] deviceInterfaceTypes;

	public DeviceDescriptorImpl(DeviceType deviceType, NetworkType networkType, String[] deviceInterfaceTypes) {
		this.deviceType = deviceType;
		this.networkType = networkType;
		this.deviceInterfaceTypes = deviceInterfaceTypes;
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}

	public NetworkType getNetworkType() {
		return networkType;
	}

	public String[] getImplementedDeviceInterfaceTypes() {
		return deviceInterfaceTypes;
	}

	public String toString() {
		return "DeviceDescriptor [deviceType=" + deviceType + ", networkType=" + networkType + ", deviceInterfaceTypes="
				+ Arrays.toString(deviceInterfaceTypes) + "]";
	}

}
