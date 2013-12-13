/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface GatewayEventListener {
	void gatewayStartResult(Status status);

	void nodeDiscovered(Status status, WSNNode node);

	void nodeRemoved(Status status, WSNNode node);

	void servicesDiscovered(Status status, NodeServices services);

	void serviceDescriptorRetrieved(Status status, ServiceDescriptor service);

	@Deprecated
	void nodeDescriptorRetrieved(Status status, NodeDescriptor node);

	void dongleResetResult(Status status);

	void bindingResult(Status status);

	void unbindingResult(Status status);

	void nodeBindingsRetrieved(Status status, BindingList bindings);

	@Deprecated
	void leaveResult(Status status);

	void permitJoinResult(Status status);

}
