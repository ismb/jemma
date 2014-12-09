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
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

/**
 * Register the url related to the Green@Home web application
 */

public class GalGuiHttpApplication extends DefaultWebApplication implements HttpImplementor, HttpContext {
	private boolean enableHttps = false;
	private boolean useBasic = false;
	private UserAdmin userAdmin = null;
	private boolean enableSecurity = true;
	private boolean userCreated = false;
	private ComponentContext ctxt;
	private HttpService httpService;
	private String realm = "javaGalGui Login";
	private String applicationWebAlias = "";
	private static final Logger LOG = LoggerFactory.getLogger(GalGuiHttpApplication.class);

	HttpBinder HttpAdapter = null;
	private BundleContext bc;

	protected synchronized void activate(ComponentContext ctxt) {
		this.ctxt = ctxt;
		this.bc = ctxt.getBundleContext();
		applicationWebAlias = "/" + this.ctxt.getProperties().get("rootContext").toString();
		HttpAdapter = new HttpBinder();
		setRootUrl(applicationWebAlias);
		registerResource("/", "webapp");
		setHttpContext(this);
		super.bindHttpService(httpService);

		LOG.debug("Bundle Active now: rootContext is: " + applicationWebAlias);
	}

	public synchronized void deactivate() {
		this.ctxt = null;
		this.bc = null;
		userCreated = false;
		LOG.debug("deactivated");
	}

	protected synchronized void setUserAdmin(UserAdmin s) {
		this.userAdmin = s;

	}

	protected synchronized void unsetUserAdmin(UserAdmin s) {
		if (this.userAdmin == s)
			this.userAdmin = null;
	}

	protected void installUsers() {

		String username = bc.getProperty("org.energy_home.jemma.username");
		String password = bc.getProperty("org.energy_home.jemma.password");

		User adminUser = (User) createRole(userAdmin, "Administrators", Role.USER);

		setUserCredentials(adminUser, password);

		if (userAdmin.getRole("Administrators") == null) {
			Group administrator = (Group) createRole(userAdmin, "Administrators", Role.GROUP);
			administrator.addMember(adminUser);

		} else
			((Group) userAdmin.getRole("Administrators")).addMember(adminUser);

		userCreated = true;

	}

	protected Role createRole(UserAdmin ua, String name, int roleType) {

		Role role = ua.createRole(name, roleType);
		if (role == null) {
			role = ua.getRole(name);
		}

		return role;

	}

	protected void setHttpService(HttpService s) {
		httpService = s;

	}

	protected void unsetHttpService(HttpService s) {
		this.unbindHttpService(s);
		httpService = null;
	}

	public String getMimeType(String page) {
		if (page.endsWith(".manifest")) {
			return "text/cache-manifest";
		} else if (page.endsWith(".css")) {
			return "text/css";
		} else if (page.endsWith(".js")) {
			return "application/javascript";
		} else if (page.endsWith(".html")) {
			return "text/html";
		}
		else if (page.endsWith(".ico")) {
			return "image/x-icon";
		}
		else if (page.endsWith(".png")) {
			return "image/png";
		}
		else 
			return null;
	}

	public URL getResource(String name) {
		URL u = null;
		if (name.endsWith("/")) {
			// the resource name ends with slash, defaults to index.html
			name += "home.html";
		}
		
		if (name.equals("webapp/"))
			u = this.bc.getBundle().getResource(name + "home.html");
		else
			u = this.bc.getBundle().getResource(name);
		return u;
	}

	@Override
	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getRequestURI().contains("favicon.ico"))
			return true;
		else
			LOG.debug("Http Request:" + request.getRequestURI());
		

		if (enableHttps && !request.getScheme().equals("https")) {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (IOException e) {
				// do nothing
			}
			return false;
		}

		String queryString = request.getRequestURI();

		if (enableSecurity) {
			if (useBasic) {
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
				} catch (LoginException e) {
					return failAuthorization(request, response);
				}

				request.setAttribute(HttpContext.REMOTE_USER, userid);
				request.setAttribute(HttpContext.AUTHENTICATION_TYPE, authscheme);
				request.setAttribute(HttpContext.AUTHORIZATION, subject);
			} else {
				HttpSession session = request.getSession(true);
				if (queryString.toLowerCase().startsWith(applicationWebAlias.toLowerCase())) {
					// this is a restricted area so performs login

					
					if (request.getMethod() == "POST" && session.getValue("javaGallogon.isDone")==null) {
						String username64 = request.getParameter("username");
						String password64 = request.getParameter("password");
						
						String username=null;
						String password=null;
						try{
							username = new String(Base64.decode(username64.getBytes()));
							password = new String(Base64.decode(password64.getBytes()));
						}catch(Exception e)
						{
							LOG.error("Error decoding user/password");
							return false;
						}
								
						if (!allowUser(username, password)) {
							return redirectToLoginPage(request, response);
						} else {
							session.putValue("javaGallogon.isDone", username);
							try {
								String target = (String) session.getValue("javaGalLogin.target");
								if (target != null) {

									response.sendRedirect(target);
								} else {
									response.sendRedirect(applicationWebAlias + "/home.html");
								}
							} catch (Exception ignored) {
								return false;
							}
						}
					} else {
						if (queryString.toLowerCase().equals(applicationWebAlias.toLowerCase() + "/login.html")) {
							return true;
						} else {
							session.putValue("javaGalLogin.target", applicationWebAlias + "/home.html");
							Object done = session.getValue("javaGallogon.isDone");
							if (done == null) {
								if (request.getMethod().equals("GET")) {
									return redirectToLoginPage(request, response);
								} else {
									response.sendError(HttpServletResponse.SC_FORBIDDEN);
									return false;
								}
							}

						}
					}
				}
			}
		}

		

		return true;
	}

	@Override
	public Object getObjectByPid(String pid) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean redirectToLoginPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String redirect = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + applicationWebAlias + "/login.html";
		response.sendRedirect(redirect);
		return true;
	}

	private void setUserCredentials(User user, String password) {
		Object currentCredential = user.getProperties().get("org.energy_home.jemma.username");
		if (currentCredential == null) {
			user.getProperties().put("org.energy_home.jemma.username", user.getName().toLowerCase());
			try{
				user.getCredentials().put("org.energy_home.jemma.password", password);
			}catch(Exception e)
			{
				LOG.error("Error setting password");
			}

		}

	}

	private boolean allowUser(String username, String password) {

		if (userAdmin != null) {
			if (!userCreated)
				installUsers();
			User user = userAdmin.getUser("org.energy_home.jemma.username", username);
			if (user == null)
				return false;
			if (!user.hasCredential("org.energy_home.jemma.password", password)) {
				return false;
			} else {
				Group group = (Group) userAdmin.getRole("Administrators");
				if (group == null) {
					return false;
				} else {
					for (Role x : group.getMembers())
					{
						if (x.getName().equalsIgnoreCase(username))
							return true;
					}
					return false;
				}
			}
		} else
			return false;

	}

	private boolean failAuthorization(HttpServletRequest request, HttpServletResponse response) {
		// force a session to be created
		request.getSession(true);
		response.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");

		try {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (IOException e) {
			// do nothing
		}
		return false;
	}

	private Authorization login(HttpServletRequest request, final String username, final String password) throws LoginException {
		Authorization a = (Authorization) request.getAttribute(HttpContext.AUTHORIZATION);
		if (a != null) {
			return a;
		}

		if (userAdmin != null) {
			User user = userAdmin.getUser("org.energy_home.jemma.username", username);
			if (user == null) {
				throw new LoginException();
			}
			if (!user.hasCredential("org.energy_home.jemma.password", password)) {
				throw new LoginException();
			}

			return userAdmin.getAuthorization(user);
		}

		throw new LoginException();
	}
}
