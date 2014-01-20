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
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

class ConcurrencyRestClient {

	
	private Client restClient;
	
	@Deprecated
	ConcurrencyRestClient(Client client) {
		restClient = client;
	}
	
	synchronized Response get(String uri) {
		
		
		//Request req = new Request(Method.GET,uri);
		//return restClient.handle(req);
		return handle(Method.GET,uri,null);
	}
	
	synchronized Response put(String uri, Representation rep) {
		//Request r=new Request(Method.PUT, uri, rep);
		//return restClient.handle(r);
		return handle(Method.PUT,uri,rep);
	}
	
	synchronized Response post(String uri, Representation rep) {
		//Request r=new Request(Method.POST, uri, rep);
		//return restClient.handle(r);
		return handle(Method.POST,uri,rep);
	}
	
	synchronized Response delete(String uri) {
		//Request r=new Request(Method.DELETE, uri);
		//return restClient.handle(r);
		return handle(Method.DELETE,uri,null);
	}

	/**
	 * Method sending http requests using native java http client  and wrapping responses in restlet-style objects
	 */
	synchronized Response handle(Method action, String uri, Representation rep) {
		Response resp = new Response(new Request(action,uri));
		URL url;
		try {
			url = new URL(uri);
		
		//create the request
		HttpURLConnection conn=(HttpURLConnection) url.openConnection();
		//System.out.println("METHOD: "+action.getName());
		conn.setRequestMethod(action.getName());
		
		conn.setUseCaches(false);
		conn.setDoInput(true);
		
		//fill request bod if needed
		if(action.compareTo(Method.POST)==0|| action.compareTo(Method.PUT)==0)
		{
			conn.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream (
	                 conn.getOutputStream ());
			
			BufferedReader r=new BufferedReader(new InputStreamReader(rep.getStream()));
			
			StringBuffer urlParameters=new StringBuffer();
			String line;
			 while((line = r.readLine()) != null) {
				 urlParameters.append(line);
				 urlParameters.append('\r');
			      }
			
		     wr.writeBytes (urlParameters.toString());
		     wr.flush ();
		     wr.close ();
		}else{
			conn.setDoOutput(false);
		}
		
		
		//fill response
		StringBuffer value= new StringBuffer();
		String line;
		
		//TODO: try to return directly the inputstream in the response
		BufferedReader r=new BufferedReader(new InputStreamReader(conn.getInputStream()));
		
		 while((line = r.readLine()) != null) {
		        value.append(line);
		        value.append('\r');
		      }
		r.close();
		//System.out.println("VALUE: "+value);
		
		resp.setEntity(value.toString(), MediaType.TEXT_XML);
		resp.setStatus(new Status(conn.getResponseCode()));
		//return restClient.handle(new Request(action, uri, rep));
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resp;
	}
}
