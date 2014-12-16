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
package org.energy_home.jemma.ah.webui.energyathome;

import java.util.Vector;

import javax.servlet.Servlet;

import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebApplication {
	private String rootUrl = "/";
	private HttpService httpService = null;
	
	private static final Logger LOG = LoggerFactory.getLogger(WebApplication.class);

	Vector resources = new Vector();
	Vector servlets = new Vector();

	private HttpContext httpContext;

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public void registerResource(String alias, String path) {
		this.resources.add(new Resource(alias, path));
	}

	public void registerResource(String alias, Servlet servlet) {
		this.servlets.add(new ServletResource(alias, servlet));
	}

	public synchronized void bindHttpService(HttpService s) {
		this.httpService = s;
		this.bindResources();
	}

	protected synchronized void unbindHttpService(HttpService s) {
		if (this.httpService == s) {
			this.unbindResources();
			this.httpService = null;
		}
	}

	private void bindResources() {
		if (httpService != null) {
			for (int i = 0; i < resources.size(); i++) {
				Resource r = (Resource) resources.get(i);
				try {
					httpService.registerResources(this.toAlias(this.rootUrl + r.getAlias()), r.getPath(), this.getHttpContext());
				} catch (Throwable e) {
					LOG.error("Error registering resources: {}",e);
					continue;
				}
			}

			for (int i = 0; i < servlets.size(); i++) {
				ServletResource sr = (ServletResource) servlets.get(i);
				try {
					httpService.registerServlet(this.toAlias(this.rootUrl + sr.getAlias()), sr.getServlet(), null, this.getHttpContext());
				} catch (Exception e) {
					LOG.error("Error registering servlet: {}",e);
					continue;
				} 
			}
		}
	}

	private String toAlias(String alias) {
		if (alias.endsWith("/")) {
			String a = alias.substring(0, alias.length() - 1);
			return a;
		}
			
		return alias;
	}

	private void unbindResources() {
		if (this.httpService != null) {
			for (int i = 0; i < resources.size(); i++) {
				Resource r = (Resource) resources.get(i);
				try {
					httpService.unregister(this.rootUrl + r.getAlias());
				} catch (Exception e) {
					continue;
				}
			}
			for (int i = 0; i < servlets.size(); i++) {
				ServletResource sr = (ServletResource) servlets.get(i);
				try {
					httpService.unregister(this.rootUrl + sr.getAlias());
				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	private HttpContext getHttpContext() {
		if (httpContext != null) {
			return httpContext;
		} else if (httpService != null) {
			return this.httpService.createDefaultHttpContext();
		}
		return null;
	}
	public void setHttpContext(HttpContext httpContext) {
			this.httpContext = httpContext;
	
	}
}
