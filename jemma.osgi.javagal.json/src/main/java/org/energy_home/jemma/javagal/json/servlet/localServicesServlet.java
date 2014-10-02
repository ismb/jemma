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
import org.energy_home.jemma.javagal.json.constants.Resources;
import org.energy_home.jemma.javagal.json.util.Util;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class localServicesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	Gson gson;

	public localServicesServlet(GatewayInterface _gatewayInterface) {
		gatewayInterface =_gatewayInterface; 
		gson = new Gson();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		Object done = session.getValue("javaGallogon.isDone");
		if (done != null) {

			

			String timeoutString;
			Long timeout;
			Object timeoutParam = request.getParameter(Resources.URI_PARAM_TIMEOUT);
			if (timeoutParam == null) {

				String error = "Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter missing.";
				Info info = Util.setError(error);
				response.getOutputStream().print(gson.toJson(info));
				return;
			} else {
				timeoutString = timeoutParam.toString();
				if (!timeoutString.toLowerCase().startsWith("0x"))
					timeoutString = "0x"+ timeoutString;
				try {
					timeout = Long.decode(timeoutString);
					if (!Util.isUnsigned32(timeout)) {
						String error = "Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString;
						Info info = Util.setError(error);
						response.getOutputStream().print(gson.toJson(info));
						return;
					}
				} catch (NumberFormatException nfe) {
					Info info = Util.setError(nfe.getMessage());
					response.getOutputStream().print(gson.toJson(info));
					return;
				}
			}

			SimpleDescriptor simpleDescriptor;
			StringBuilder sb = new StringBuilder();
			String s;
			try {
				while ((s = request.getReader().readLine()) != null) {
					sb.append(s);
				}

				simpleDescriptor = gson.fromJson(sb.toString(), SimpleDescriptor.class);

				short endPoint = gatewayInterface.configureEndpoint(timeout, simpleDescriptor);
				if (endPoint > 0) {


					Info.Detail detail = new Info.Detail();
					detail.setEndpoint(endPoint);
					Info info = Util.setSuccess(detail);
					response.getOutputStream().print(gson.toJson(info));

                } else {

					String error = "Error creating end point. Not created.";
					Info info = Util.setError(error);
					response.getOutputStream().print(gson.toJson(info));

                }

			} catch (Exception e) {
				Info info = Util.setError(e.getMessage());
				response.getOutputStream().print(gson.toJson(info));

            }

		} else {
			Info info = Util.setError("User not logged");
			response.getOutputStream().print(gson.toJson(info));

        }
	}

}
