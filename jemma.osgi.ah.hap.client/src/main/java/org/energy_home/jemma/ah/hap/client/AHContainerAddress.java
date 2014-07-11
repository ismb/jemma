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
package org.energy_home.jemma.ah.hap.client;


import java.util.WeakHashMap;

import org.energy_home.jemma.ah.m2m.device.M2MContainerAddress;
import org.energy_home.jemma.internal.ah.hap.client.AHM2MContainerAddress;

public class AHContainerAddress {
	private static final String INVALID_CONTAINER_ID = "Invalid container id";

	private static final WeakHashMap<Integer, AHContainerAddress> ahNetworkContainerAddressesMap = new WeakHashMap<Integer, AHContainerAddress>();

	public static final String DEFAULT_APPLIANCE_PREFIX = "ah.app.";
	public static final String DEFAULT_END_POINT_ID = new Integer(1).toString();
	public static final String ALL_ID_FILTER = M2MContainerAddress.ALL_ID_FILTER;
	public static final String POSITIVE_ID_FILTER = M2MContainerAddress.POSITIVE_ID_FILTER;

	// Note: null and empty strings generate the same hash code
	private static int getHashCode(String s0, String s1, String s2, String s3, Boolean b) {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((s0 == null) ? 0 : s0.hashCode());
		result = prime * result + ((s1 == null) ? 0 : s1.hashCode());
		result = prime * result + ((s2 == null) ? 0 : s2.hashCode());
		result = prime * result + ((s3 == null) ? 0 : s3.hashCode());
		result = prime * result + ((b == null) ? 0 : b.hashCode());
		return result;
	}

	public static AHContainerAddress getAddressFromUrl(String urlOrAddressedId) {
		return new AHM2MContainerAddress(urlOrAddressedId);
	}
	
	public static AHContainerAddress getNetworkAddress(String hagId, String appliancePid, String endPointId, String containerName) {
		return getNetworkAddress(hagId, appliancePid, endPointId, containerName, false);
	}
	
	public static AHContainerAddress getNetworkAddress(String hagId, String appliancePid, String endPointId, String containerName, boolean isLocal) {
		// TODO: check for a more efficient implementation
		int key = getHashCode(hagId, appliancePid, endPointId, containerName, new Boolean(isLocal));
		AHContainerAddress containerAddress = null;
		synchronized (ahNetworkContainerAddressesMap) {
			containerAddress = ahNetworkContainerAddressesMap.get(new Integer(key));
			if (containerAddress == null) {
				containerAddress = new AHM2MContainerAddress(hagId, appliancePid, endPointId, containerName, isLocal, false);
				ahNetworkContainerAddressesMap.put(key, containerAddress);
			}
		}
		return containerAddress;
	}
	
	protected M2MContainerAddress m2mContainerAddress = null;
	protected String appliancePid = null;
	protected String endPointId = null;
	protected String containerName = null;

	private boolean isAppliancePid(String part) {
		// TODO: this test recognize ALL as an appliance filter only
		return part.startsWith(AHM2MContainerAddress.DEFAULT_APPLIANCE_PREFIX) 
				|| M2MContainerAddress.isFilterWord(part);
	}
	
	protected AHContainerAddress(String urlOrAddressedId) {
		m2mContainerAddress = new M2MContainerAddress(urlOrAddressedId);
		String[] parts = m2mContainerAddress.getContainerIdParts();
		if (parts == null || parts.length == 0)
			throw new IllegalArgumentException(INVALID_CONTAINER_ID);
		if (parts.length == 1 && isAppliancePid(parts[0])) {
			appliancePid = parts[0];
		} else if (parts.length == 2  && isAppliancePid(parts[0])) {
			appliancePid = parts[0];
			endPointId = parts[1];
		} else if (isAppliancePid(parts[0])) {
			appliancePid = parts[0];
			endPointId = parts[1];
			containerName = parts[2];
		} else if (parts.length == 1) {
			containerName = parts[0];
		} else
			throw new IllegalArgumentException(INVALID_CONTAINER_ID);
	}

	protected AHContainerAddress(String hagId, String appliancePid, String endPointId, String containerName, boolean isLocal, boolean isProxy) throws IllegalArgumentException {
		this.appliancePid = appliancePid;
		if (endPointId != null)
			this.endPointId = endPointId.toString();
		this.containerName = containerName;

		m2mContainerAddress = new M2MContainerAddress(hagId,
				containerName == null ? ((endPointId == null) ? new String[] { appliancePid } : new String[] { appliancePid,
						endPointId }) : ((appliancePid == null) ? new String[] { containerName } : new String[] { appliancePid,
						endPointId, containerName }), isLocal, isProxy);
	}
	
	public boolean isFilterAddress() {
		return m2mContainerAddress.isFilterAddress();
	}

	public boolean isLocalAddress() {
		return m2mContainerAddress.isLocalAddress();
	}

	public boolean isProxyAddress() {
		return m2mContainerAddress.isProxyAddress();
	}

	public String getHagId() {
		return m2mContainerAddress.getSclId();
	}

	public String getAppliancePid() {
		return appliancePid;
	}

	public String getEndPointId() {
		return endPointId;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getUrl() {
		return m2mContainerAddress.getUrl();
	}
	
	public String getContentInstancesUrl() {
		return m2mContainerAddress.getContentInstancesUrl();
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;

		return this.m2mContainerAddress.equals(((AHContainerAddress) obj).m2mContainerAddress);
	}

	public int hashCode() {
		int hashCode = 23;
		hashCode = hashCode * 29 + this.m2mContainerAddress.hashCode();
		return hashCode;
	}
	
	public String toString() {
		return getUrl();
	}

}
