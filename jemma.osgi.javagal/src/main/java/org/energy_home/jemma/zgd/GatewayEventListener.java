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

import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

/**
 * ZGD's event listener's interface. This work is an implementation of a ZigBee
 * Gateway Device following the specifications depicted in ZigBee Document
 * 075468r35 "Network Device: Gateway Specification" (revision 35), from now on
 * called SPECS.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public interface GatewayEventListener {
	/**
	 * Called to notify the status after Gateway Start operation. The start
	 * operation attempts to start/create a ZigBee network.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void gatewayStartResult(Status status);

	/**
	 * Notification for StartNodeDiscovery procedure. See SPECS 6.4.10 and
	 * 6.4.11.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param node
	 *            the node to notify.
	 */
	void nodeDiscovered(Status status, WSNNode node);

	/**
	 * Called when a node leaves the network. See SPECS 6.4.12.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param node
	 *            the removed node.
	 */
	void nodeRemoved(Status status, WSNNode node);

	/**
	 * Notification for StartServiceDiscovery procedure. See SPECS 6.4.14 and
	 * 6.4.15.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param services
	 *            the services to notify.
	 */
	void servicesDiscovered(Status status, NodeServices services);

	/**
	 * Notification for GetServiceDescriptor procedure. See SPECS 6.4.16.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param service
	 *            the service to notify.
	 */
	void serviceDescriptorRetrieved(Status status, ServiceDescriptor service);

	/**
	 * Notification for GetNodeDescriptor procedure. See SPECS 6.4.7.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param node
	 *            the node descriptor to notify.
	 */
	@Deprecated
	void nodeDescriptorRetrieved(Status status, NodeDescriptor node);

	/**
	 * Notification for Reset procedure. See SPECS 6.9.11 and 6.9.12.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void dongleResetResult(Status status);

	/**
	 * Notification for binding procedure.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void bindingResult(Status status);

	/**
	 * Notification for Unbinding procedure.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void unbindingResult(Status status);

	/**
	 * Notification for GetBindingList procedure. See SPECS 6.7.15.
	 * 
	 * @param status
	 *            the status to notify.
	 * @param bindings
	 *            the bindings list to notify.
	 */
	void nodeBindingsRetrieved(Status status, BindingList bindings);

	/**
	 * Notification for Leave procedure. See SPECS 6.4.26 and 6.4.27.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	@Deprecated
	void leaveResult(Status status);

	/**
	 * Notification for PermitJoin procedure. See SPECS 6.4.28. The PermitJoin
	 * procedure opens a ZigBee network to a single node, and for a specified
	 * duration, to be able to associate new nodes.
	 * 
	 * @param status
	 *            the status to notify.
	 */
	void permitJoinResult(Status status);

}
