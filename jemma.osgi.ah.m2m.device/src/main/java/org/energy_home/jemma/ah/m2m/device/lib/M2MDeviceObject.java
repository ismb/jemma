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
package org.energy_home.jemma.ah.m2m.device.lib;

import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfigurator;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.internal.ah.m2m.device.M2MDeviceManager;
import org.energy_home.jemma.internal.ah.m2m.device.M2MNetworkSclObject;
import org.energy_home.jemma.m2m.connection.ConnectionParameters;

public class M2MDeviceObject implements M2MDeviceConfigurator {
	private M2MDeviceManager deviceObject;
	private M2MDeviceListener listener;

	public M2MDeviceObject() {
		this.deviceObject = M2MDeviceManager.get();
		deviceObject.addReference();
	}

	public boolean isStarted() {
		return deviceObject.isStarted();
	}

	public boolean isConnected() {
		return deviceObject.isConnected();
	}

	public M2MDeviceConfig getConfiguration() {
		return deviceObject.getConfiguration();
	}

	public void setConfiguration(M2MDeviceConfig config) throws M2MServiceException {
		deviceObject.setConfiguration(config);
	}

	public ConnectionParameters getCurrentConnectionParameters() {
		return deviceObject.getCurrentConnectionParameters();
	}

	public String getSclId() {
		return deviceObject.getConfiguration().getSclId();
	}
	
	public M2MNetworkScl getNetworkScl(String user) {
		return new M2MNetworkSclObject(deviceObject.getNetworkSclManager(), user);
	}

	public synchronized void setListener(M2MDeviceListener listener) {
		if (this.listener != null)
			deviceObject.removeListener(this.listener);
		if (listener != null)
			deviceObject.addListener(listener);
		this.listener = listener;
	}

	public synchronized void release() {
		setListener(null);
		deviceObject.removeReference();
	}



}
