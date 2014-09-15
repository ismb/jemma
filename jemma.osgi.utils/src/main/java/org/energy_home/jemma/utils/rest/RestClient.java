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
package org.energy_home.jemma.utils.rest;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class RestClient {
	private static final int SO_TIMEOUT = 1000;
	private static final String HTTP_CONTENT_TYPE = "application/xml";

	// private static URI resolveUri(URI baseUri, String relativeUri) {
	// URI uri = baseUri;
	// if (relativeUri != null)
	// uri = URIUtils.resolve(baseUri, relativeUri);
	// return uri;
	// }

	public static RestClient get() {
		return new RestClient();
	}

	public static int getResponseStatus(HttpResponse response) {
		return response.getStatusLine().getStatusCode();
	}

	public static boolean isUnauthorized(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED;
	}

	public static boolean isNotFoundStatus(HttpResponse response) {
		return response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND;
	}

	public static boolean isOkOrCreatedStatus(HttpResponse response) {
		int status = response.getStatusLine().getStatusCode();
		return status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED;
	}

	private DefaultHttpClient httpClient = null;
	private HttpContext httpContext = null;

	private RestClient() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));

		ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
		// TODO: check for final values
		// Decrease max total connection to 10 (default is 20)
		connectionManager.setMaxTotal(10);
		// Increase default max connection per route to 5 (default is 2)
		connectionManager.setDefaultMaxPerRoute(10);
		// // Increase max connections for localhost:80 to 50
		// HttpHost localhost = new HttpHost("locahost", 80);
		// cm.setMaxForRoute(new HttpRoute(localhost), 50);
		// // Increase max connections for a specific host to 10
		// connectionManager.setMaxForRoute(new HttpRoute(httpHost), 10);

		httpClient = new DefaultHttpClient(connectionManager);
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
		// Default to HTTP 1.0
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		// The time it takes to open TCP connection.
		// httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
		// 15000);
		// Timeout when server does not send data.
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT);

		// Some tuning that is not required for bit tests.
		// httpClient.getParams().setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK,
		// false);
		// httpClient.getParams().setParameter(CoreConnectionPNames.TCP_NODELAY,
		// true);
		httpContext = new BasicHttpContext();
	}

	// TODO: check for multi thread access (it should be ok)
	private HttpResponse send(HttpUriRequest uriRequest) throws ClientProtocolException, IOException {
		uriRequest.setHeader("Accept", HTTP_CONTENT_TYPE);
		return httpClient.execute(uriRequest, httpContext);
	}

	public void setCredential(String hostname, int port, String username, String password) {
		HttpHost httpHost = new HttpHost(hostname, port);
		httpClient.getCredentialsProvider().setCredentials(new AuthScope(hostname, port),
				new UsernamePasswordCredentials(username, password));
		// Create AuthCache instance
		AuthCache authCache = new BasicAuthCache();
		// Generate BASIC scheme object and add it to the local auth cache
		BasicScheme basicAuth = new BasicScheme();
		authCache.put(httpHost, basicAuth);
		// Add AuthCache to the execution context
		httpContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
	}

	public HttpResponse get(URI uri) throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(uri);
		return send(request);
	}

	public HttpResponse post(URI uri, HttpEntity entity) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(uri);
		request.setEntity(entity);
		return send(request);
	}

	public HttpResponse put(URI uri, HttpEntity entity) throws ClientProtocolException, IOException {
		HttpPut request = new HttpPut(uri);
		request.setEntity(entity);
		return send(request);
	}

	public void consume(HttpResponse response) {
		if (response != null)
			try {
				EntityUtils.consume(response.getEntity());
			} catch (Exception e) {
				// TODO: add here error recovery
				e.printStackTrace();
			}
		// Added to relase tcp connection after consuming each response
		// httpClient.getConnectionManager().closeIdleConnections(0,
		// TimeUnit.MILLISECONDS);
	}

	public void release() {
		httpClient.getConnectionManager().shutdown();
		httpClient = null;
		httpContext = null;
	}

}
