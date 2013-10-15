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
package org.energy_home.jemma.zgd.impl;

public interface EventPathURIs {
	public static final String ALL_RESPONSES = "/responses/{type}";
	public static final String STARTUP_RESPONSE = "/zgd/responses/startup";
	public static final String RESET_RESPONSE = "/zgd/responses/reset";
	public static final String NODE_DISCOVERED = "/zgd/responses/nodediscovered";
	public static final String NODE_REMOVED = "/zgd/responses/noderemoved";
	public static final String SERVICES_DISCOVERED = "/zgd/responses/services";
	public static final String SERVICE_DESCRIPTOR = "/zgd/responses/servicedescriptor";
	public static final String NODE_DESCRIPTOR = "/zgd/responses/nodedescriptor";
	public static final String LEAVE_RESPONSE = "/zgd/responses/leave";
	public static final String PERMITJOIN_RESPONSE = "/zgd/responses/permitjoin";
	public static final String NODE_BINDING_RESPONSE = "/zgd/responses/binding";
	public static final String NODE_UNBINDING_RESPONSE = "/zgd/responses/unbinding";
	public static final String NODE_BINDING_LIST_RESPONSE = "/zgd/responses/bindinglist";
	
	public static final String ALL_EVENTS = "/zgd/events/{type}";
	public static final String ZDP_NOTIFY_EVENT = "/zgd/events/zdpcommand";
	public static final String ZCL_NOTIFY_EVENT = "/zgd/events/zclcommand";
	public static final String APS_NOTIFY_EVENT = "/zgd/events/apsmessage";
	//public static final String CALLBACK_EVENT = "/zgd/events/callback";
}
