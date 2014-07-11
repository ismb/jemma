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
package org.energy_home.jemma.ah.hac.lib;

import org.energy_home.jemma.ah.hac.IApplianceDescriptor;

/**
 * Implementation of the {@code IApplianceDescriptor} interface
 * 
 * @see IApplianceDescriptor
 */
public class ApplianceDescriptor implements IApplianceDescriptor {

	private String type = null;
	private String deviceType = null;
	private String friendlyName = null;

	/**
	 * Creates an appliance class descriptor
	 * 
	 * @param type
	 *            The type associated to the appliance class
	 * @param deviceType
	 *            The device type associated to the appliance class in case of a
	 *            {@code driver appliance}, {@code null} in case of a {@code
	 *            logical appliance}
	 * @param friendlyName
	 *            A friendly name associated to the appliance class
	 */
	public ApplianceDescriptor(String type, String deviceType, String friendlyName) {
		this.type = type;
		this.friendlyName = friendlyName;
		this.deviceType = deviceType;
	}

	public ApplianceDescriptor(String type, String friendlyName) {
		this.type = type;
		this.friendlyName = friendlyName;
	}
	
	public String getType() {
		return this.type;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

}
