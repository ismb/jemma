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

import com.google.gson.Gson;

public class frequencyAgilityServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	GatewayInterface gatewayInterface;
	
	Gson gson;

	public frequencyAgilityServlet(GatewayInterface _gatewayInterface) {

		gatewayInterface = _gatewayInterface;
		gson = new Gson();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		Object done = session.getValue("javaGallogon.isDone");
		if (done != null) {
			
			String timeoutString = null;
			String scanChannelString = null;
			String scanDurationString = null;

			Long timeout = -1l;
			Long scanChannel = 0l;
			Long scanDuration = (long) 0xFE;
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
				if (!timeoutString.toLowerCase().startsWith("0x"))
					timeoutString = "0x"+ timeoutString;
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

			Object scanChannelParam = request.getParameter(Resources.URI_SCANCHANNEL);

			scanChannelString = scanChannelParam.toString();
			try {
				scanChannel = Long.decode(scanChannelString);
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: '" + Resources.URI_SCANCHANNEL + "' parameter's value invalid. You provided: " + scanChannelString);
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
				_st.setMessage("Error: '" + Resources.URI_SCANCHANNEL + "' parameter's value invalid. You provided: " + scanChannelString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;

			}

			Object scanDurationParam = request.getParameter(Resources.URI_SCANDURATION);
			if (scanDurationParam != null) {
				scanDurationString = scanDurationParam.toString();
				try {
					scanDuration = Long.decode(scanDurationString);
					if (!Util.isUnsigned32(timeout)) {

						Info info = new Info();
						Status _st = new Status();
						_st.setCode((short) GatewayConstants.GENERAL_ERROR);
						_st.setMessage("Error: '" + Resources.URI_SCANDURATION + "' parameter's value invalid. You provided: " + scanDurationString);
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
					_st.setMessage("Error: '" + Resources.URI_SCANDURATION + "' parameter's value invalid. You provided: " + scanDurationString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					response.getOutputStream().print(gson.toJson(info));
					return;

				}
			}

			Status _result;
			try {
				_result = gatewayInterface.frequencyAgilitySync(timeout, scanChannel.shortValue(), scanDuration.shortValue());
				Info _st = new Info();
				_st.setStatus(_result);
				response.getOutputStream().print(gson.toJson(_st));
				return;
			} catch (GatewayException e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: '" + Resources.URI_SCANDURATION + "' parameter's value invalid. You provided: " + scanDurationString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;
			} catch (Exception e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: '" + Resources.URI_SCANDURATION + "' parameter's value invalid. You provided: " + scanDurationString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				response.getOutputStream().print(gson.toJson(info));
				return;
			}
		} else {
			Detail detail = new Detail();
			Info info = new Info();
			Status status = new Status();
			status.setCode((short) GatewayConstants.GENERAL_ERROR);
			status.setMessage("User not logged");
			info.setStatus(status);
			info.setDetail(detail);
			response.getOutputStream().print(gson.toJson(info));
			return;

		}

	}

}
