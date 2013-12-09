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
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

class ConcurrencyRestClient {
	private Client restClient;
	
	ConcurrencyRestClient(Client client) {
		restClient = client;
	}
	
	synchronized Response get(String uri) {
		return restClient.get(uri);
	}
	
	synchronized Response put(String uri, Representation rep) {
		return restClient.put(uri, rep);
	}
	
	synchronized Response post(String uri, Representation rep) {
		return restClient.post(uri, rep);
	}
	
	synchronized Response delete(String uri) {
		return restClient.delete(uri);
	}

	synchronized Response handle(Method action, String uri, Representation rep) {
		return restClient.handle(new Request(action, uri, rep));
	}
}
