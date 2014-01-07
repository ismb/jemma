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

/**
 * Gateway Statuses enumeration.
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public enum GatewayStatus {
	/**
	 * Gateway is in "Ready to start" state.
	 */
	GW_READY_TO_START,
	/**
	 * Gateway is in "Starting" state.
	 */
	GW_STARTING,
	/**
	 * Gateway is in "Started" state.
	 */
	GW_STARTED,
	/**
	 * Gateway is in "Running" state.
	 */
	GW_RUNNING,
	/**
	 * Gateway is in "Stopping" state.
	 */
	GW_STOPPING,
	/**
	 * Gateway is in "Stopped" state.
	 */
	GW_STOPPED
}
