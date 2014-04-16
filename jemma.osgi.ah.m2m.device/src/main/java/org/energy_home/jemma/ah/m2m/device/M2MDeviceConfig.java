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
package org.energy_home.jemma.ah.m2m.device;

import java.util.Properties;

public interface M2MDeviceConfig {

	public boolean isLocalOnly();
	
	public boolean isValid();

	public Properties getProperties();

	public String getServerAddress();

	public void setServerAddress(String serverAddress);

	public int getServerPort();

	public void setServerPort(int serverPort);

	public String getDeviceId();

	public void setDeviceId(String userId);

	public void setDeviceToken(String deviceToken);

	public String getSclId();
	
	public long getConnectionRetryTimeout();

	public void setConnectionRetryTimeout(long connectionRetryTimeout);
	
}