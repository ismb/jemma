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
 * Message types enumeration.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public enum TypeMessage {
	/**
	 * The "Aps" message type.
	 */
	APS,
	/**
	 * The "Mode Select" message type.
	 */
	MODE_SELECT,
	/**
	 * The "Start Network" message type.
	 */
	START_NETWORK,
	/**
	 * The "Write Sas" message type.
	 */
	WRITE_SAS,
	/**
	 * The "Configure End Point" message type.
	 */
	CONFIGURE_END_POINT,
	/**
	 * The "APSME Set" message type.
	 */
	APSME_SET,
	/**
	 * The "APSME Get" message type.
	 */
	APSME_GET,
	/**
	 * The "Permit Join" message type.
	 */
	PERMIT_JOIN,
	/**
	 * The "Node Descriptor" message type.
	 */
	NODE_DESCRIPTOR,
	/**
	 * The "Channel Request" message type.
	 */
	CHANNEL_REQUEST,
	/**
	 * The "NMLE Get" message type.
	 */
	NMLE_GET,
	/**
	 * The "Stop Network" message type.
	 */
	STOP_NETWORK,
	/**
	 * The "Active End Point" message type.
	 */
	ACTIVE_EP,
	/**
	 * The "Read Extended Address" message type.
	 */
	READ_EXT_ADDRESS,
	/**
	 * The "Deregister End Point" message type.
	 */
	DEREGISTER_END_POINT,
	/**
	 * The "Get End Point List" message type.
	 */
	GET_END_POINT_LIST,
	/**
	 * The "Get Simple Descriptor" message type.
	 */
	GET_SIMPLE_DESCRIPTOR,
	/**
	 * The "Get Bindings" message type.
	 */
	GET_BINDINGS,
	/**
	 * The "Add Binding" message type.
	 */
	ADD_BINDING,
	/**
	 * The "Remove Binding" message type.
	 */
	REMOVE_BINDING,
	/**
	 * The "Network Update" message type.
	 */
	NWK_UPDATE,
	/**
	 * The "Partitioned Aps" message type.
	 */
	PARTITIONED_APS,
	/**
	 * The "Clear Device Key Pair Set" message type.
	 */
	CLEAR_DEVICE_KEY_PAIR_SET,
	/**
	 * The "Clear Neighbor Table Entry" message type.
	 */
	CLEAR_NEIGHBOR_TABLE_ENTRY,
	/**
	 * The "NMLE_SET Entry" message type.
	 */
	NMLE_SET,
	/**
	 * The "READ_IEEE_ADDRESS" message type.
	 */
	READ_IEEE_ADDRESS,
	/**
	 * The "LQI_REQ" message type.
	 */
	LQI_REQ,
	
	/**
	 * The "INTERPAN" message type.
	 */
	INTERPAN
}
