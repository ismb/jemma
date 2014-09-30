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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;

/**
 * Register the url related to the Green@Home web application
 */

public class GalGuiHttpApplication extends DefaultWebApplication implements HttpImplementor, HttpContext {
    private UserAdmin userAdmin = null;
    private boolean userCreated = false;
	private ComponentContext ctxt;
	private HttpService httpService;
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
		URL u;
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

        String queryString = request.getRequestURI();

        HttpSession session = request.getSession(true);
        if (queryString.toLowerCase().startsWith(applicationWebAlias.toLowerCase())) {
            // this is a restricted area so performs login


            if (request.getMethod().equals("POST")) {
                String username64 = request.getParameter("username");
                String password64 = request.getParameter("password");

                String username = new String(Base64.decode(username64.getBytes()));
                String password = new String(Base64.decode(password64.getBytes()));

                if (!allowUser(username, password)) {
                    return redirectToLoginPage(request, response);
                } else {
                    session.putValue("javaGallogon.isDone", username);
                    try {
                        String target = (String) session.getValue("javaGalLogin.target");
                        if (target != null) response.sendRedirect(target);
                        else {
                            response.sendRedirect(applicationWebAlias + "/home.html");
                        }
                    } catch (Exception ignored) {
                        return false;
                    }
                }
            }
        }


        return true;
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
			user.getCredentials().put("org.energy_home.jemma.password", password);

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
