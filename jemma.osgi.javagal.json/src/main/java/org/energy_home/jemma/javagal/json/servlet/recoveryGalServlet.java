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
package org.energy_home.jemma.javagal.json.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.energy_home.jemma.javagal.json.constants.Resources;
import org.energy_home.jemma.javagal.json.util.Util;
import org.energy_home.jemma.zgd.GalExtenderProxy;
import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class recoveryGalServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	Gson gson;

	private final static Logger LOG=LoggerFactory.getLogger(recoveryGalServlet.class);
	
	public recoveryGalServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
		gson = new Gson();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		Object done = session.getValue("javaGallogon.isDone");
		Detail detail = new Detail();
		Info info = new Info();
		Status status = new Status();
		if (done != null) {
			try {
				((GalExtenderProxy)gatewayInterface).recoveryGal();
				status.setCode((short)GatewayConstants.SUCCESS);
				status.setMessage("Gal recovery process started");
			} catch (Exception e) {
				LOG.error("Error recovering GAL: {}",e);
			
				status.setCode((short) GatewayConstants.GENERAL_ERROR);
				
				status.setMessage("Error recovering gal: "+e.getMessage());
				
			}
			status.setCode((short)GatewayConstants.SUCCESS);
			status.setMessage("Gal recovery process started");
		}else{
			
			status.setCode((short) GatewayConstants.GENERAL_ERROR);
			status.setMessage("User not logged");
		}
		info.setStatus(status);
		info.setDetail(detail);
		response.getOutputStream().print(gson.toJson(info));
	}

}
