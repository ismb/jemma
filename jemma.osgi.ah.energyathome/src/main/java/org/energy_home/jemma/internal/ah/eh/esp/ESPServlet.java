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
package org.energy_home.jemma.internal.ah.eh.esp;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.utils.rest.RestClient;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESPServlet extends HttpServlet {	
	private static final long serialVersionUID = -615518450441731123L;

	private static final Logger LOG = LoggerFactory.getLogger( ESPServlet.class );
	
	private static final String servletUri = "/esp";	
	private static final String userRegistrationUri = "/esp/registerUser";
	private static final String authTokenUriStr = "/esp/authToken";
	
	private static final String remoteGetRegistrationUri = "/ehproxy/getregisteruri.php";
	private static final String remoteAuthTokenUriStr = "/ehproxy/gethapsession.php";
	
	private String remoteHostAddr;
	private int remoteHostPort;
	
	private URI userRegistrationGetUri;
	private URI remoteAuthTokenUri;

	private ESPService espService;

	private static RestClient restClient;
	
	private void releaseRequestResources(HttpResponse response) {
		try {
			if (response != null)
				restClient.consume(response);
		} catch (Exception e) {
			LOG.error("releaseRequestResources: error while consuming rest client response", e);
		}
	}
	
	protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws ServletException, IOException {
		String queryString = servletRequest.getRequestURI();

		if (queryString.startsWith(userRegistrationUri)) {
			if (servletRequest.getRemoteHost().equals(remoteHostAddr)) {
				LOG.warn("service: registration uri is not accessible through vpn proxy");
				servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else {
				HttpResponse response = null;
				try {
					response = restClient.get(userRegistrationGetUri);
					String responseUri = EntityUtils.toString(response.getEntity());
					servletResponse.sendRedirect(responseUri);
				} catch (Exception e) {
					LOG.error("service: error while connecting to userRegistrationGetUri", e);
					servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				} finally {
					releaseRequestResources(response);
				}					
			}
		} else if (queryString.startsWith(authTokenUriStr)) {
			HttpResponse response = null;
			PrintWriter pw = null;
			try {
				response = restClient.get(remoteAuthTokenUri);
				String token = EntityUtils.toString(response.getEntity());
				pw = servletResponse.getWriter();
				pw.println(token);
			} catch (Exception e) {
				LOG.error("service: error while connecting to remoteAuthTokenUri", e);
				servletResponse.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			} finally {
				releaseRequestResources(response);
				if (pw != null)
					try {
						pw.close();
					} catch (Exception e2) {
						LOG.error("service: error while releasing printwriter used for connection to remoteAuthTokenUri", e2);
					}
			}				
		} else{
			servletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		} 
	}
	
	public void start() {
		try {
			remoteHostAddr = ESPConfiguration.getRemoteHostAddr();
			remoteHostPort = ESPConfiguration.getRemoteHostPort();
			userRegistrationGetUri = new URI("http", null, remoteHostAddr, remoteHostPort, remoteGetRegistrationUri, null, null);
			remoteAuthTokenUri = new URI("http", null, remoteHostAddr, remoteHostPort, remoteAuthTokenUriStr, null, null);
		} catch (Exception e) {
			LOG.error("start: error while initializing ESPServlet", e);
		}
		restClient = RestClient.get();
	}
	
	public void stop() {
		restClient.release();
		userRegistrationGetUri = null;
		remoteAuthTokenUri = null;
	}
	
	public void setHttpService(HttpService httpService) {
		try {
			httpService.registerServlet(servletUri, this, null, null);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (NamespaceException e) {
			e.printStackTrace();
		}

	}
	
	public void unsetHttpService(HttpService httpService) {
		httpService.unregister(servletUri);
	}
	
	public void setESPService(ESPService espService) {
		this.espService = espService;
	}
	
	public void unsetESPService(ESPService espService) {
		this.espService = null;
	}
	
}
