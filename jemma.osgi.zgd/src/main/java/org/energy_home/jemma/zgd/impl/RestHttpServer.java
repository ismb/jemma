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
package org.energy_home.jemma.zgd.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
class RestHttpServer implements HttpHandler {
	private HttpServer server;
	private String host; // ip or host name
	private int port;
	public static final String URL_CONTEXT = "/dustin";

	RestHttpServer(String host, int port) {
		this.host = host;
		this.port = port;
	}

	void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext(URL_CONTEXT, this);
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
	}

	void stop() {
		if (server != null)
			server.stop(0);
	}

	/*
	 * HttpExchange encapsulates a HTTP request received and a response to be
	 * generated in one exchange. It provides methods for examining the request
	 * from the client, and for building and sending the response. The typical
	 * life-cycle of a HttpExchange is shown in the sequence below.
	 * 
	 * getRequestMethod() to determine the command getRequestHeaders() to
	 * examine the request headers (if needed)
	 * 
	 * getRequestBody() returns a InputStream for reading the request body.
	 * After reading the request body, the stream is close.
	 * 
	 * getResponseHeaders() to set any response headers, except content-length
	 * 
	 * sendResponseHeaders(int,long) to send the response headers. Must be
	 * called before next step.
	 * 
	 * getResponseBody() to get a OutputStream to send the response body. When
	 * the response body has been written, the stream must be closed to
	 * terminate the exchange.
	 * 
	 * Terminating exchanges Exchanges are terminated when both the request
	 * InputStream and response OutputStream are closed. Closing the
	 * OutputStream, implicitly closes the InputStream (if it is not already
	 * closed). However, it is recommended to consume all the data from the
	 * InputStream before closing it. The convenience method close() does all of
	 * these tasks. Closing an exchange without consuming all of the request
	 * body is not an error but may make the underlying TCP connection unusable
	 * for following exchanges. The effect of failing to terminate an exchange
	 * is undefined, but will typically result in resources failing to be
	 * freed/reused.
	 */
	@Override
	public void handle(HttpExchange httpExchange) throws IOException {
		InputStream is = httpExchange.getRequestBody();
		// read(is); // .. read the request body
		String response = "This is the response";
		httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
		final OutputStream os = httpExchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public static void main(String[] args) throws IOException {
		InetSocketAddress addr = new InetSocketAddress(8080);
		HttpServer server = HttpServer.create(addr, 0);
		server.createContext("/", new RootHandler());
		server.createContext("/foo/", new FooHandler());
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
		System.out.println("Server is listening on port 8080");
	}

	public static void printHeaders(HttpExchange exchange, PrintStream response) {
		Headers requestHeaders = exchange.getRequestHeaders();
		Set<String> keySet = requestHeaders.keySet();
		Iterator<String> iter = keySet.iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			response.println(key + " = " + requestHeaders.get(key));
		}
	}

	public static void printBody(HttpExchange exchange, PrintStream response) throws IOException {
		BufferedReader body = new BufferedReader(new InputStreamReader(exchange.getRequestBody()));
		String bodyLine;
		while ((bodyLine = body.readLine()) != null) {
			response.println(bodyLine);
		}
	}
}

class RootHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();

		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/plain");
		exchange.sendResponseHeaders(200, 0);

		PrintStream response = new PrintStream(exchange.getResponseBody());
		response.println("context: ROOT; method: " + requestMethod);
		response.println("--- headers ---");
		RestHttpServer.printHeaders(exchange, response);
		if (requestMethod.equalsIgnoreCase("POST")) {
			response.println("=== body ===");
			RestHttpServer.printBody(exchange, response);
		}
		response.close();
	}
}

class FooHandler implements HttpHandler {
	public void handle(HttpExchange exchange) throws IOException {
		String requestMethod = exchange.getRequestMethod();

		Headers responseHeaders = exchange.getResponseHeaders();
		responseHeaders.set("Content-Type", "text/plain");
		exchange.sendResponseHeaders(200, 0);

		PrintStream response = new PrintStream(exchange.getResponseBody());
		response.println("context: FOO; method: " + requestMethod);
		RestHttpServer.printHeaders(exchange, response);
		response.close();
	}
}
