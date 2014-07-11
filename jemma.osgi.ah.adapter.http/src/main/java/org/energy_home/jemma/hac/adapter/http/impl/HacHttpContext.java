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
package org.energy_home.jemma.hac.adapter.http.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpContext;

public class HacHttpContext implements HttpContext {
	
	private HttpContext ctx;

	public HacHttpContext(HttpContext ctx) {
		this.ctx = ctx;
	}

	public String getMimeType(String page) {		
		String mime = ctx.getMimeType(page);
		if (mime == null) {
			if (page.endsWith(".manifest"))  {  
				return "text/cache-manifest";  
			}
			else if (page.endsWith(".css")) {
				return "text/css";  
			}
			else if (page.endsWith(".js")) {
				return "text/javascript";
			}
			return null;
		}
		return mime;
	}

	public URL getResource(String resource) {
		if (resource.endsWith("/")) {
			// the resource name ends with slash, defaults to index.html
			resource += "index.html";
		}
		URL u = ctx.getResource(resource);
		return u;
	}

	public boolean handleSecurity(HttpServletRequest arg0, HttpServletResponse arg1) throws IOException {
		return true;
	}
}
