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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.energy_home.jemma.hac.adapter.http.Base64;
import org.energy_home.jemma.hac.adapter.http.CustomJsonServlet;
import org.energy_home.jemma.hac.adapter.http.HttpImplementor;
import org.energy_home.jemma.hac.adapter.http.JsonRPC;
import org.energy_home.jemma.hac.adapter.http.ServiceNoParam;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register the url related to the Green@Home web application
 */

public class EnergyAtHome extends WebApplication implements HttpImplementor, HttpContext {

    private static class ServletConfigDisableGZIPWrapper implements ServletConfig{

        private ServletConfig wrapped=null;
		
		public ServletConfigDisableGZIPWrapper(ServletConfig wrapped){
			this.wrapped=wrapped;
		}

		public String getInitParameter(String name){
			// Disables gzip compression
			if(name.equals("gzip_threshold")){
				return "-1";
			}else{
				return wrapped.getInitParameter(name);
			}
		} 
		
		public Enumeration getInitParameterNames(){
			return wrapped.getInitParameterNames();
		}

		public ServletContext getServletContext(){
			return wrapped.getServletContext();
		}

		public String getServletName(){
			return wrapped.getServletName();
		} 
	}
 
	private UserAdmin userAdmin = null;
	private String applicationWebAlias = "/demo";
	//private String applicationWebAlias = "/energyathome";
	
	private String realm = "Energy@Home Login";
	private ComponentContext ctxt;

	private static final Logger LOG = LoggerFactory.getLogger( EnergyAtHome.class );
	private static final String PROP_ENABLE_AUTH = "it.telecomitalia.ah.energyathome.auth";
	private static final String PROP_ENABLE_HTTPS = "it.telecomitalia.ah.energyathome.https";
	private static final boolean DEFAULT_ENABLE_AUTH = true;
	private static final boolean DEFAULT_ENABLE_HTTPS = false;

	HttpAhBinder ahHttpAdapter = null;
	private BundleContext bc;

	JSONRPCBridge jsonRpcBridge;
	private boolean useBasic = false;
	private boolean enableSecurity = true;
	private boolean enableHttps = false;
	private ServiceRegistryProxy registryProxy;
	
	public Properties props = null;

	protected void activate(ComponentContext ctxt) {

		this.ctxt = ctxt;
		this.bc = ctxt.getBundleContext();

		jsonRpcBridge = JSONRPCBridge.getGlobalBridge();
		
		try {
			registryProxy = new ServiceRegistryProxy(this.bc, jsonRpcBridge);
			jsonRpcBridge.registerObject("OSGi", registryProxy);
		} catch (Throwable e) {
			LOG.error("Exception on activate",e);
		}
	}

	protected void deactivate(ComponentContext ctxt) {
		LOG.debug("deactivated");

		if (this.registryProxy != null) {
			jsonRpcBridge.unregisterObject("OSGi");
			this.registryProxy.close();
		}
	}

	protected synchronized void setHttpService(HttpService s) {
		ahHttpAdapter = new HttpAhBinder();

		this.setRootUrl(applicationWebAlias);

		Servlet customJsonServlet = new CustomJsonServlet(ahHttpAdapter, "");
		Servlet jsonRPC = new JsonRPC(ahHttpAdapter, "");

		this.registerResource("/", "webapp/ehdemo");
		this.registerResource("/gh", "webapp/gh");
		this.registerResource("/post-json", customJsonServlet);
		this.registerResource("/json-rpc", jsonRPC);
 		this.registerResource("/JSON-RPC", new JSONRPCServlet(){
			public void init(ServletConfig config) throws ServletException{
				super.init(new ServletConfigDisableGZIPWrapper(config));
			}
		}); 

		this.setHttpContext(this);

		this.update(null);
		super.bindHttpService(s);
	}

	protected synchronized void unsetHttpService(HttpService s) {
		this.unbindHttpService(s);
	}

	protected synchronized void setUserAdmin(UserAdmin s) {
		this.userAdmin = s;
	}

	protected synchronized void unsetUserAdmin(UserAdmin s) {
		if (this.userAdmin == s)
			this.userAdmin = null;
	}

	public Object getObjectByPid(String pid) {
		return pid;
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
		URL u = this.ctxt.getBundleContext().getBundle().getResource(name);
		return u;
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {

		// we need http scheme!
		if (enableHttps && !request.getScheme().equals("https")) {
			try {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
			} catch (IOException e) {
				// do nothing
			}
			return false;
		}
		
		String queryString = request.getRequestURI();
		
		if (queryString.equals(applicationWebAlias + "/conf") || (queryString.equals(applicationWebAlias + "/conf/"))) {
			response.sendRedirect(applicationWebAlias + "/conf/index_embedded.html");
			return true;
		}
		else if (queryString.equals(applicationWebAlias) || (queryString.equals(applicationWebAlias + "/"))) {
			response.sendRedirect(applicationWebAlias + "/index.html");
			return true;			
		}

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
				if (queryString.startsWith(applicationWebAlias + "/conf")) {
					// this is a restricted area so performs login

					String a = request.getMethod();
					String submit = request.getParameter("submit");
					if (submit != null) {
						String username = request.getParameter("username");
						String password = request.getParameter("password");
						if (!allowUser(username, password)) {
							return redirectToLoginPage(request, response);
						} else {
							session.putValue("logon.isDone", username);
							try {
								String target = (String) session.getValue("login.target");
								if (target != null)
									response.sendRedirect(target);
								else {
									response.sendRedirect(applicationWebAlias + "/conf/index_embedded.html");
								}
							} catch (Exception ignored) {
								return false;
							}
						}
					} else {
						if (queryString.equals(applicationWebAlias + "/conf/login.html")) {
							return true;
						} else {
//							session.putValue("login.target", HttpUtils.getRequestURL(request).toString());
							session.putValue("login.target", applicationWebAlias + "/conf/index_embedded.html");
							Object done = session.getValue("logon.isDone");
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

		if (request.getRequestURI().endsWith(".png")) {
			response.setHeader("Cache-Control", "public, max-age=10000");
		}

		/*if (request.getRequestURI().endsWith(".js")) {
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Cache-Control", "public, max-age=0");
		}*/

		// response.addHeader(HttpServletResponse, arg1)

		return true;
	}

	private boolean redirectToLoginPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/*String redirect = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
				+ applicationWebAlias + "/conf/login.html";
		*/
		String redirect=applicationWebAlias+"/conf/login.html";
		response.sendRedirect(redirect);
		return true;
	}

	private boolean allowUser(String username, String password) {
		if (userAdmin != null) {
			User user = userAdmin.getUser("org.energy_home.jemma.username", username);
			if (user == null) {
				return false;
			}
			if (!user.hasCredential("org.energy_home.jemma.password", password)) {
				return false;
			}

			return true;
		}
		return false;
	}

	private Authorization login(HttpServletRequest request, final String username, final String password) throws LoginException {
		Authorization a = (Authorization) request.getAttribute(HttpContext.AUTHORIZATION);
		if (a != null) {
			return a;
		}

		if (userAdmin != null) {
			User user = userAdmin.getUser("it.telecomitalia.username", username);
			if (user == null) {
				throw new LoginException();
			}
			if (!user.hasCredential("it.telecomitalia.password", password)) {
				throw new LoginException();
			}

			return userAdmin.getAuthorization(user);
		}

		throw new LoginException();
	}

	public void modified(ComponentContext ctxt, Map props) {
		update(props);
	}

	private void update(Map props) {
		this.enableSecurity = getProperty(props, PROP_ENABLE_AUTH, DEFAULT_ENABLE_AUTH);
		this.enableHttps = getProperty(props, PROP_ENABLE_HTTPS, DEFAULT_ENABLE_HTTPS);
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
