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
package org.energy_home.jemma.javagal.gui.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.energy_home.jemma.javagal.gui.Activator;
import org.energy_home.jemma.javagal.gui.GalGuiHttpApplication;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;/*
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
*/
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;



public class channelServlet extends HttpServlet {
	GalGuiHttpApplication parent;
	
	public channelServlet (GalGuiHttpApplication _parent)
	{
		
		parent = _parent;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*Detail _det = new Detail();
		
		JSONObject jsonResponse = new JSONObject();
		
		Short channel = 0;
		
		try {
			channel = parent.getGatewayInterface().getChannelSync(5000);
		} catch (GatewayException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		_det.getValue().add(channel.toString());
		Info _info = new Info();
		Status _st = new Status();
		_st.setCode((short) GatewayConstants.SUCCESS);
		_info.setStatus(_st);
		_info.setDetail(_det);
		
		Method[] methods = _info.getClass().getMethods();

		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			JSONObject jsonSignature = new JSONObject();
			try {
				jsonResponse.put(method.getName(), jsonSignature);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		response.getOutputStream().print(jsonResponse.toString());
		*/
		
		
	

		
	
	}

}
