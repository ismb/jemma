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
package org.energy_home.jemma.hac.adapter.http;


import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Authorization;

public class CustomJsonServlet extends HttpServlet {

	private static final long serialVersionUID = 7495225913754933111L;
	private HttpServletBinder httpAdapter = null;
	// int offset;
	//FIXME we should leave the log configuration to the log configuration file!
	@Deprecated 
	private boolean logEnabled = false;

	private static final Logger LOG = LoggerFactory.getLogger( CustomJsonServlet.class );

	public CustomJsonServlet(HttpServletBinder httpAdapter, String prefix) {
		this.httpAdapter = httpAdapter;
		// offset = prefix.length();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Authorization a = (Authorization) req.getAttribute(HttpContext.AUTHORIZATION);

		String objectid;
		String methodName;
		
		HttpSession session = req.getSession(true);
		

		objectid = req.getParameter("objectid");
		if (objectid == null) {
			sendHttpError(res, 100, "Error: objectid parameter not found");
			return;
		}

		methodName = req.getParameter("method");
		if (methodName == null) {
			sendHttpError(res, 100, "Error: missing parameter 'method'");
		}

		/* paramValues array contains pararm0, param1 .... param<params> */
		// process parameters
		int params = 0;
		ArrayList paramValues = new ArrayList();

		Object targetObject = null;

		if (logEnabled)
			LOG.debug("req querystring:" + req.getQueryString());

		// retrieve the object
		targetObject = httpAdapter.getObjectByPid(objectid);

		if ((targetObject instanceof IAppliance) && (targetObject != httpAdapter.getImplementor())) {
			IAppliance ac = (IAppliance) targetObject;
			paramValues.add(ac.getPid());
			targetObject = httpAdapter.getImplementor();
		}

		while (true) {
			String paramName = "param" + params;
			String paramValue = req.getParameter(paramName);
			if (paramValue == null) {
				break;
			}
			paramValues.add(paramValue);
			params++;
		}

		if (targetObject == null) {
			sendHttpError(res, 100, "Error: objectid not found");
			return;
		}

		try {
			Object result = httpAdapter.invokeMethod(targetObject, methodName, paramValues);
			if (logEnabled)
				LOG.debug("result" + result);
			sendHttpError(res, HttpServletResponse.SC_OK, (String) result);
		} catch (Exception e) {
			sendHttpError(res, 100, e.getMessage());
		}
	}

	private void sendHttpError(HttpServletResponse res, int err, String message) throws ServletException, IOException {
		res.setContentType("application/json");
		res.setHeader("Cache-Control", "no-cache");
		res.getWriter().write(message);
	}
}
