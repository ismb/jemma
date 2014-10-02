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
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigInteger;

public class nodeDescriptorAndServicesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
GatewayInterface gatewayInterface;
	Gson gson;

	public nodeDescriptorAndServicesServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
		gson = new Gson();
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

			Object addressParam = request.getParameter(Resources.URI_ADDR);
			String addrString = addressParam.toString();
			Address addressObj = new Address();
			if (addrString.length() == 16) {
				BigInteger addressBigInteger = BigInteger.valueOf(Long.parseLong(addrString, 16));
				addressObj.setIeeeAddress(addressBigInteger);
			} else if (addrString.length() == 4) {
				Integer addressInteger = Integer.parseInt(addrString, 16);
				addressObj.setNetworkAddress(addressInteger);
			} else {
				Info info = Util.setError("Wrong address parameter");
				response.getOutputStream().print(gson.toJson(info));
				return;

			}

			NodeDescriptor nodeDescriptor;
			try {
				nodeDescriptor = gatewayInterface.getNodeDescriptorSync(timeout, addressObj);
				NodeServices nodeServices = gatewayInterface.startServiceDiscoverySync(timeout, addressObj);
				Detail detail = new Detail();
				detail.setNodeDescriptor(nodeDescriptor);
				detail.setNodeServices(nodeServices);

				Info info = Util.setSuccess(detail);
				response.getOutputStream().print(gson.toJson(info));
            } catch (GatewayException e) {
				Info info = Util.setError(e.getMessage());
				response.getOutputStream().print(gson.toJson(info));
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
