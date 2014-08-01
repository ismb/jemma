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
package org.energy_home.jemma.zgd;

import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.ZCLMessage;
import org.energy_home.jemma.zgd.jaxb.ZDPMessage;

/**
 * Extension for {@link GatewayEventListener}.
 *  
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface GatewayEventListenerExtended extends GatewayEventListener {
	/**
	 * Called to notify Node Descriptor's Event to registered listeners.
	 * 
	 * @param status
	 *            the status to notify to the listener.
	 * @param node
	 *            the Node Descriptor to notify.
	 * @param addressOfInterest
	 *            the address of interest.
	 */
	void nodeDescriptorRetrievedExtended(Status status, NodeDescriptor node,
			Address addressOfInterest);

	/**
	 * Called to notify result from "Gateway Stop" operation.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void gatewayStopResult(Status status);

	/**
	 * Called to notify Leave Result's Event.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param addressOfInteres
	 *            the address of interest.
	 */
	void leaveResultExtended(Status status, Address addressOfInteres);

	/**
	 * Called to notify ZDP Command's Event.
	 * 
	 * @param message
	 *            the ZDP message to notify.
	 */
	void notifyZDPCommand(ZDPMessage message);

	/**
	 * Called to notify ZCL Command's Event.
	 * 
	 * @param message
	 *            the ZCL message to notify.
	 */
	void notifyZCLCommand(ZCLMessage message);

	/**
	 * Called to notify Frequency Agility (Network Update) response.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void FrequencyAgilityResponse(Status status);

}
