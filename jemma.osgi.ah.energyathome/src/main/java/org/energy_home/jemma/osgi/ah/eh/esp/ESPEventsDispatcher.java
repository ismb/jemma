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
package org.energy_home.jemma.osgi.ah.eh.esp;

import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESPEventsDispatcher implements IESPEventsDispatcher {
	//private static final Log log = LogFactory.getLog(ESPEventsDispatcher.class);
	private static final Logger LOG = LoggerFactory.getLogger( ESPEventsDispatcher.class );
	
	private Object eventAdminSync = new Object();
	private EventAdmin eventAdmin;

	public void setEventAdmin(EventAdmin s) {
		synchronized (eventAdminSync) {
			eventAdmin = s;
		}
	}

	public void unsetEventAdmin(EventAdmin s) {
		synchronized (eventAdminSync) {
			if (s == eventAdmin)
				eventAdmin = null;
		}
	}
	
	public void postEvent(String topic, Map props) {
		synchronized (eventAdminSync) {
			if (this.eventAdmin != null) {
				try {
					LOG.debug("ESP posted event " + topic);
					this.eventAdmin.postEvent(new Event(topic, props));
				} catch (Exception e) {
					LOG.error("Excpetion on postEvent",e);
				}
			}
		}
	}
}
