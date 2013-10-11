/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import java.io.*;
import java.net.*;

import org.energy_home.jemma.zgd.jaxb.Info;


public class RestHttpClient {
	private JaxbConverter jaxbConverter;
	
	
	Info get(String uri) throws IOException {
		URL url = new URL(uri);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		//connection.setConnectTimeout(timeout);
		connection.setDoInput(true);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		InputStream is = connection.getInputStream();
		//Info info = jaxbConverter.getInfo(response);
		return null;
	}
	
	Info post(String uri) throws IOException {
		URL url = new URL(uri); 
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(true); 
        connection.setInstanceFollowRedirects(false); 
        connection.setRequestMethod("POST"); 
        connection.setRequestProperty("Content-Type", "application/xml"); 

        OutputStream os = connection.getOutputStream(); 
        //jaxbConverter.createMarshaller().marshal(customer, os); 
        os.flush(); 

        int responseCode = connection.getResponseCode();
        connection.disconnect();
        if (responseCode < 200 || responseCode > 299)
        	throw new IOException("Error loading xml from jaxb: " +  responseCode);  

		return null;
	}
}
