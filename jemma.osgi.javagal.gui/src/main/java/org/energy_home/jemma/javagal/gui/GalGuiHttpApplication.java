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

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;

/**
 * Register the url related to the Green@Home web application
 */

public class GalGuiHttpApplication extends DefaultWebApplication implements HttpImplementor, HttpContext {

	
	private ComponentContext ctxt;
	private String applicationWebAlias = "/javaGalWebGui";
	//private String realm = "Gal Login";
	
	private static final Log log = LogFactory.getLog(GalGuiHttpApplication.class);
	//private static final String PROP_ENABLE_AUTH = "it.telecomitalia.ah.energyathome.auth";
	//private static final String PROP_ENABLE_HTTPS = "it.telecomitalia.ah.energyathome.https";
	//private static final boolean DEFAULT_ENABLE_AUTH = true;
	//private static final boolean DEFAULT_ENABLE_HTTPS = false;

	HttpBinder HttpAdapter = null;
	private BundleContext bc;

	//private boolean useBasic = false;
	//private boolean enableSecurity = true;
	//private boolean enableHttps = false;
	
	
	protected void activate(ComponentContext ctxt) {

		this.ctxt = ctxt;
		this.bc = ctxt.getBundleContext();

		
		
	}

	public void deactivate() {
		log.debug("deactivated");

		
	}
	
	


	protected synchronized void setHttpService(HttpService s) {
		HttpAdapter = new HttpBinder();
		this.setRootUrl(applicationWebAlias);
		this.registerResource("/", "webapp");
		this.setHttpContext(this);
		this.update(null);
		super.bindHttpService(s);
	}

	protected synchronized void unsetHttpService(HttpService s) {
		this.unbindHttpService(s);
	}

	
	public Object getObjectByPid(String pid) {
		return pid;
	}

	private boolean failAuthorization(HttpServletRequest request, HttpServletResponse response) {
		// force a session to be created
		/*
		request.getSession(true);
		response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");

		try {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (IOException e) {
			// do nothing
		}
		*/
		return false;
	}

	public String getMimeType(String page) {
		// TODO addd PNG JPG and GIF files
		if (page.endsWith(".manifest")) {
			return "text/cache-manifest";
		} else if (page.endsWith(".css")) {
			return "text/css";
		} else if (page.endsWith(".js")) {
			return "text/javascript";
		} else if (page.endsWith(".html")) {
			return "text/html";
		}
		return null;
	}

	public URL getResource(String name) {
		URL u = this.bc.getBundle().getResource(name);
		return u;
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {

		
		return true;
	}

	private void update(Map props) {
		}

	private boolean getProperty(Map props, String name, boolean value) {
		if (props == null) {
			return value;
		}
		Object prop = props.get(name);
		if (prop == null) {
			return value;
		}
		return ((Boolean) prop).booleanValue();
	}
}
