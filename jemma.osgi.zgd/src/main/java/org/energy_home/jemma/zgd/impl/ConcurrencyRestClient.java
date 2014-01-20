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

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.representation.Representation;

class ConcurrencyRestClient {
	private Client restClient;
	
	ConcurrencyRestClient(Client client) {
		restClient = client;
	}
	
	synchronized Response get(String uri) {
		Request req = new Request(Method.GET,uri);
		Response resp=new Response(req);
		
		restClient.handle(req,resp);
		
		req.commit(resp);
		
		return resp;
	}
	
	synchronized Response put(String uri, Representation rep) {
		Request r=new Request(Method.PUT, uri, rep);
		return restClient.handle(r);
	}
	
	synchronized Response post(String uri, Representation rep) {
		Request r=new Request(Method.POST, uri, rep);
		return restClient.handle(r);
	}
	
	synchronized Response delete(String uri) {
		Request r=new Request(Method.DELETE, uri);
		return restClient.handle(r);
	}

	synchronized Response handle(Method action, String uri, Representation rep) {
		return restClient.handle(new Request(action, uri, rep));
	}
}
