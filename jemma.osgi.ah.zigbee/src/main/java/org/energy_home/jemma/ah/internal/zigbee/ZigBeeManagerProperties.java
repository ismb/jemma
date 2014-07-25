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

public class ZigBeeManagerProperties extends ConfigurationAdminProperties {
	public static final String PROP_ENABLE_LQI = "org.energy_home.jemma.ah.adapter.zigbee.lqi";
	public static final String PROP_DISCOVERY_DELAY = "org.energy_home.jemma.ah.adapter.zigbee.discovery.delay";
	public static final String PROP_DISCOVERY_INITIAL_DELAY = "org.energy_home.jemma.ah.adapter.zigbee.discovery.initialdelay";
	public static final String PROP_ZGD_RECONNECT_DELAY = "org.energy_home.jemma.ah.adapter.zigbee.reconnect";
	public static final String PROP_TIMEOUTS = "org.energy_home.jemma.ah.adapter.zigbee.timeouts";

	static final boolean DEFAULT_ENABLE_LQI = false;
	static final int DEFAULT_DISCOVERY_DELAY = 0;
	static final int DEFAULT_DISCOVERY_INITIAL_DELAY = 15;
	static final int DEFAULT_ZGD_RECONNECT_DELAY = 5;
	static final int DEFAULT_TIMEOUTS = 7000;

	public int getReconnectToJGalDelay() {
		return this.getProperty(PROP_ZGD_RECONNECT_DELAY, DEFAULT_ZGD_RECONNECT_DELAY);
	}

	public int getDiscoveryDelay() {
		return this.getProperty(PROP_DISCOVERY_DELAY, DEFAULT_DISCOVERY_DELAY);
	}

	public int getInitialDiscoveryDelay() {
		return this.getProperty(PROP_DISCOVERY_INITIAL_DELAY, DEFAULT_DISCOVERY_INITIAL_DELAY);
	}

	public boolean isLqiEnabled() {
		return this.getProperty(PROP_ENABLE_LQI, DEFAULT_ENABLE_LQI);
	}
}
