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
package org.energy_home.jemma.ah.internal.zigbee;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility wrapping class for Configuration Admin properties.
 */
public class ConfigurationAdminProperties {
	private Map props = new HashMap();
	
	// TODO: read also system properties
	
	protected boolean getProperty(String name, boolean defaultValue) {
		Object prop = props.get(name);
		if (prop == null) {
			return defaultValue;
		}
		try {
			return ((Boolean) prop).booleanValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}

	protected int getProperty(String name, int defaultValue) {
		Object prop = props.get(name);
		if (prop == null) {
			return defaultValue;
		}
		try {
			return ((Integer) prop).intValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	protected int getProperty(String name, short defaultValue) {
		Object prop = props.get(name);
		if (prop == null) {
			return defaultValue;
		}
		try {
			return ((Short) prop).intValue();
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	public Object getProperty(String name) {
		return null;
	}
	
	public Object getDefaultValue(String name) {
		return null;
	}
	
	
	/**
	 * Updates the properties
	 * @param props
	 */

	public void update(Map props) {
		this.props = props;
	}
}
