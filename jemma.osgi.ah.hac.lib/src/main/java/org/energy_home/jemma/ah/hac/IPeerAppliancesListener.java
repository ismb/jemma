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
package org.energy_home.jemma.ah.hac;

import org.energy_home.jemma.ah.hac.lib.Appliance;

/**
 * This interface is used to notify connections and disconnection of peer
 * appliances.
 * <P>
 * An appliance interested in receiving these notifications needs to register
 * this interface through the method
 * {@link Appliance#registerPeerAppliancesListener(IPeerAppliancesListener)}
 * 
 * @see Appliance
 * @see IMangedAppliance
 * 
 */
public interface IPeerAppliancesListener {
	/**
	 * Notifies that a new connection has been established with a peer appliance
	 * 
	 * @param peerAppliancePid
	 *            The pid that uniquely identifies the connected peer appliance
	 */
	public void notifyPeerApplianceConnected(String peerAppliancePid);

	/**
	 * Notifies that the connection with the specified peer appliance has been
	 * updated
	 * 
	 * @param peerAppliancePid
	 *            The pid that uniquely identifies the connected peer appliance
	 */
	public void notifyPeerApplianceUpdated(String peerAppliancePid);

	/**
	 * Notifies that the connection with the specified peer appliance has been
	 * removed
	 * 
	 * @param peerAppliancePid
	 *            The pid that uniquely identifies the connected peer appliance
	 */
	public void notifyPeerApplianceDisconnected(String peerAppliancePid);

}
