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
package org.energy_home.jemma.internal.ah.hap.client;

import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;

// This class has been added to avoid dependencies from M2MContainerAddress class in bundle importing hap packages
public class AHM2MContainerAddress extends AHContainerAddress {

	public AHM2MContainerAddress(String urlOrAddressedId) throws IllegalArgumentException {
		super(urlOrAddressedId);
	}

	public AHM2MContainerAddress(String hagId, String appliancePid, String endPointId, String containerName, boolean isLocal,
			boolean isProxy) throws IllegalArgumentException {
		super(hagId, appliancePid, endPointId, containerName, isLocal, isProxy);
	}

	public M2MContainerAddress getM2MContainerAdress() {
		return m2mContainerAddress;
	}

}
