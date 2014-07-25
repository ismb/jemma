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
package org.energy_home.jemma.hac.adapter.http.contexts;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.energy_home.jemma.hac.adapter.http.Base64;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

public class UserAdminBasicHttpContext implements HttpContext {

	private URL resourceBase;
	private String realm;
	private UserAdmin userAdmin;
	protected boolean enableHttps = false;
	private boolean enableSecurity = false;

	public UserAdminBasicHttpContext(URL resourceBase, URL configFile, String realm) {
		this.resourceBase = resourceBase;
		this.realm = realm;
	}
	
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	public void setConfigFile(URL configFile) {
	}
	
	public void setResourceBase(URL resourceBase) {
		this.resourceBase = resourceBase;
	}
	
	public void enableHttps (boolean enable) {
		this.enableHttps = enable;
	}
	
	public void enableSecurity (boolean security) {
		this.enableSecurity = security;
	}

	private boolean failAuthorization(HttpServletRequest request, HttpServletResponse response) {
		// force a session to be created
		request.getSession(true);
		response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		
		try {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
		catch (IOException e) {
			// do nothing
		}
		return false;
	}

	public String getMimeType(String name) {
		return null;
	}

	public URL getResource(String name) {
		try {
			return new URL(resourceBase, name);
		}
		catch (MalformedURLException e) {
			return null;
		}
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		// we need http scheme!
		if (enableHttps && !request.getScheme().equals("https")) {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			}
			catch (IOException e) {
				// do nothing
			}
			
			return false;
		}
		
		if (enableSecurity) {

			String auth = request.getHeader("Authorization");
			
			if (auth == null)
				return failAuthorization(request, response);
			
			StringTokenizer tokens = new StringTokenizer(auth);
			String authscheme = tokens.nextToken();
			
			if (!authscheme.equals("Basic"))
				return failAuthorization(request, response);
	
			String base64credentials = tokens.nextToken();
			String credentials = new String(Base64.decode(base64credentials.getBytes()));
			int colon = credentials.indexOf(':');
			String userid = credentials.substring(0, colon);
			String password = credentials.substring(colon + 1);
			Authorization subject = null;
			
			try {
				subject = login(request, userid, password);
			}
			catch (LoginException e) {
				return failAuthorization(request, response);
			}
			
			request.setAttribute(HttpContext.REMOTE_USER, userid);
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, authscheme);
			request.setAttribute(HttpContext.AUTHORIZATION, subject);
		}
		
		return true;
	}
	
	private Authorization login(HttpServletRequest request, final String userid, final String password) throws LoginException {
		
		Authorization a = null;
		try {
			a = (Authorization) request.getAttribute(HttpContext.AUTHORIZATION);
		} catch (Exception e) {
			throw new LoginException();
		}
		
		if (a != null) {
			return a;
		}
		
		// autenticate the user
		
		if (userAdmin != null) {
			User user = userAdmin.getUser("org.telecomitalia.hac.username", userid);
			if (user == null) {
				throw new LoginException();
			}
			if (!user.hasCredential("org.telecomitalia.hac.password", password)) {
				throw new LoginException();
			}
			
			return userAdmin.getAuthorization(user);
		}

		throw new LoginException();
	}

	public void setUserAdmin(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}
}
