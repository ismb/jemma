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
import java.math.BigInteger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.energy_home.jemma.javagal.json.constants.Resources;
import org.energy_home.jemma.javagal.json.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;

import com.google.gson.Gson;

public class nodeServicesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	Gson gson;

	public nodeServicesServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
		gson = new Gson();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String timeoutString = null;
		Long timeout = -1l;

		Object timeoutParam = request.getParameter(Resources.URI_PARAM_TIMEOUT);

		if (timeoutParam == null) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			response.getOutputStream().print(gson.toJson(info));
			return;

		} else {
			timeoutString = timeoutParam.toString();
			try {
				timeout = Long.decode(timeoutString);
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					response.getOutputStream().print(gson.toJson(info));
					return;

				}
			} catch (NumberFormatException nfe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(nfe.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
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
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Wrong Address parameter");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			response.getOutputStream().print(gson.toJson(info));
			return;

		}

		Object endPointParam = request.getParameter(Resources.PARAMETER_ENDPOINT);
		String endPointString = endPointParam.toString();
		Integer endPointInt;
		if (endPointString.length() > 0) {
			try {
				endPointInt = Integer.parseInt(endPointString, 16);
				ServiceDescriptor serviceDescriptor = gatewayInterface.getServiceDescriptorSync(timeout, addressObj, endPointInt.shortValue());
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.SUCCESS);

				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				detail.setServiceDescriptor(serviceDescriptor);
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;
			} catch (GatewayException e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(e.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;
			} catch (Exception e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(e.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;
			}

		} else {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Wrong Address parameter");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			response.getOutputStream().print(gson.toJson(info));
			return;

		}

	}

}
