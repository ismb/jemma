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
package org.energy_home.jemma.javagal.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class HttpBinder implements EventHandler, HttpServletBinder {

	private static final long serialVersionUID = 1L;

	protected final static Log log = LogFactory.getLog(HttpBinder.class);

	private HttpImplementor implementor = null;

	public HttpBinder() {
	}

	@Override
	public void bind(HttpImplementor implementor) {
		this.implementor = implementor;
	}

	@Override
	public Object invokeMethod(Object targetObject, String methodName, ArrayList paramValues) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getImplementor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(Event arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getObjectByPid(String pid) {
		// TODO Auto-generated method stub
		return null;
	}
}
