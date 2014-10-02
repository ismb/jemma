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

import com.google.gson.Gson;
import org.energy_home.jemma.javagal.json.util.Util;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
public class allLqiInformationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	Gson gson;

	public allLqiInformationsServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
		gson = new Gson();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		Object done = session.getValue("javaGallogon.isDone");
		if (done != null) {
            LQIInformation lqi;
			try {
				lqi = gatewayInterface.getLQIInformation();
			} catch (GatewayException e) {
				Info info = Util.setError(e.getMessage());
				response.getOutputStream().print(gson.toJson(info));
				return;
			} catch (Exception e) {
				Info info = Util.setError(e.getMessage());
				response.getOutputStream().print(gson.toJson(info));
				return;
			}
			Detail det = new Detail();
			det.getLQIInformation().add(lqi);
			Info info = Util.setSuccess(det);
			response.getOutputStream().print(gson.toJson(info));
		} else {
            Info info = Util.setError("user not logged");
			response.getOutputStream().print(gson.toJson(info));

        }
	}

}
