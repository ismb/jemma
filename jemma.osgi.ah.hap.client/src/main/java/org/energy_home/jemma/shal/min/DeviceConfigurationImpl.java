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
import org.energy_home.jemma.shal.DeviceLocation;

public class DeviceConfigurationImpl implements DeviceConfiguration {

	private String nickname;
	private DeviceCategory category;
	private DeviceLocation location;

	public DeviceConfigurationImpl(String nickname, DeviceCategory category, DeviceLocation location) {
		this.nickname = nickname;
		this.category = category;
		this.location = location;
	}
	
	public String getNickname() {
		return nickname;
	}

	public DeviceCategory getCategory() {
		return category;
	}

	public DeviceLocation getLocation() {
		return location;
	}

	public String toString() {
		return "DeviceConfiguration [nickname=" + nickname + ", category=" + category + ", location=" + location + "]";
	}

}
