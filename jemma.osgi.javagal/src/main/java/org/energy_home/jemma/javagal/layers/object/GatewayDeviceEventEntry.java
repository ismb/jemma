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
package org.energy_home.jemma.javagal.layers.object;

import org.energy_home.jemma.zgd.GatewayEventListener;


/**
 * Helper class that associates a {code GatewayEventListener} to a
 * {@code proxyIdentifier}.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 * @param <T>
 *            accepts any {@code GatewayEventListener}'s extending class.
 */
public class GatewayDeviceEventEntry<T extends GatewayEventListener> {
	long proxyIdentifier;
	private T GatewayEventListener;
	private int discoveryMask;
	private int freshnessMask;
	
	/**
	 * Gets the gateway event listener carried by this
	 * {@code GatewayDeviceEventEntry}.
	 * 
	 * @return the gateway event listener.
	 */
	public T   getGatewayEventListener() {
		return GatewayEventListener;
	}

	/**
	 * Sets the gateway event listener that will be carried by this
	 * {@code GatewayDeviceEventEntry}.
	 * 
	 * @param gatewayEventListener
	 *            the gateway event listener to set.
	 */
	public void setGatewayEventListener(
			T  gatewayEventListener) {
		GatewayEventListener = gatewayEventListener;
	}

	/**
	 * Gets the proxy identifer.
	 * 
	 * @return the proxy identifier.
	 */
	public long getProxyIdentifier() {
		return proxyIdentifier;
	}

	/**
	 * Sets the proxy identifer.
	 * 
	 * @param proxyIdentifier
	 *            the proxy identifier to set.
	 */
	public void setProxyIdentifier(long proxyIdentifier) {
		this.proxyIdentifier = proxyIdentifier;
	}

	/**
	 * Gets the discovery mask.
	 * 
	 * @return the discovery mask.
	 */
	public int getDiscoveryMask() {
		return discoveryMask;
	}

	/**
	 * Sets the discovery mask.
	 * 
	 * @param discoveryMask
	 *            the discovery mask to set.
	 */
	public void setDiscoveryMask(int discoveryMask) {
		this.discoveryMask = discoveryMask;
	}

	/**
	 * Gets the freshness mask.
	 * 
	 * @return the freshness mask.
	 */
	public int getFreshnessMask() {
		return freshnessMask;
	}

	/**
	 * Sets the freshness mask.
	 * 
	 * @param freshnessMask
	 *            the freshness mask to set.
	 */
	public void setFreshnessMask(int freshnessMask) {
		this.freshnessMask = freshnessMask;
	}
}
