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
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class startUpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	Gson gson;

	public startUpServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
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
			Object startParam = request.getParameter(Resources.URI_PARAM_START);
			String startParamString;
			startParamString = startParam.toString();
			if (!(startParamString.equals("true") || startParamString.equals("false"))) {
				String error = "Error: mandatory '" + Resources.URI_PARAM_START + "' parameter's value invalid. You provided: " + startParamString;
				Info info = Util.setError(error);
				response.getOutputStream().print(gson.toJson(info));
				return;
			}
			if (startParamString.equals("true")) {
				// Sync startGatewayDevice
				try {
					StartupAttributeInfo defaultStartUpAttributeInfo;
					defaultStartUpAttributeInfo = gson.fromJson(request.getReader(), StartupAttributeInfo.class);
					Status result = gatewayInterface.startGatewayDeviceSync(timeout, defaultStartUpAttributeInfo);
					Info info = new Info();
					info.setStatus(result);
					response.getOutputStream().print(gson.toJson(info));

                } catch (Exception e) {
					Info info = Util.setError(e.getMessage());
					response.getOutputStream().print(gson.toJson(info));
                }
			}
		} else {
            Info info = Util.setError("User not logged");
			response.getOutputStream().print(gson.toJson(info));

        }
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			Object indexParam = request.getParameter(Resources.URI_PARAM_INDEX);
			String indexParamString;
			Long index;
			if (indexParam == null) {
				String error = "Index parameter is mandatory";
				Info info = Util.setError(error);
				response.getOutputStream().print(gson.toJson(info));
				return;
			} else
				indexParamString = indexParam.toString();
			try {
				index = Long.decode("0x" + indexParamString);
			} catch (NumberFormatException nfe) {
				Info info = Util.setError(nfe.getMessage());
				response.getOutputStream().print(gson.toJson(info));
				return;
			}
			try {
				StartupAttributeInfo sai = gatewayInterface.readStartupAttributeSet(index.shortValue());
				Info.Detail detail = new Info.Detail();
				detail.setStartupAttributeInfo(sai);
				Info infoToReturn = Util.setSuccess(detail);
				response.getOutputStream().print(gson.toJson(infoToReturn));

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
