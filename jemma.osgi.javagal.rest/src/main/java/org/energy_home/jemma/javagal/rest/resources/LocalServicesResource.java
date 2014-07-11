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
package org.energy_home.jemma.javagal.rest.resources;

import static org.energy_home.jemma.javagal.rest.util.Util.INTERNAL_TIMEOUT;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:getLocalServices. POST:configureEndpoint. DELETE:ClearEndPoint 
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class LocalServicesResource extends ServerResource {


	private GatewayInterface proxyGalInterface;
	private String timeoutString = null;
	private Long timeout = (long) INTERNAL_TIMEOUT;

	@Get
	public void processGet() {
		String epString = (String) getRequest().getAttributes().get(
				Resources.PARAMETER_EP);

		if (epString == null) {

			// GetLocalServices (GET method with /{ep} not present)
			NodeServices services = null;
			try {
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();
				services = proxyGalInterface.getLocalServices();
				Detail _det = new Detail();
				_det.setNodeServices(services);
				Info _info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.SUCCESS);
				_info.setStatus(_st);
				_info.setDetail(_det);
				getResponse().setEntity(Util.marshal(_info),
						MediaType.APPLICATION_XML);
				return;
			} catch (NullPointerException npe) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(npe.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			} catch (Exception e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(e.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}

		} else {
			Detail _det = new Detail();
			Info _info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.SUCCESS);
			_info.setStatus(_st);
			_det.getValue().add(ResourcePathURIs.WSNCONNECTION);
			_info.setDetail(_det);
			getResponse().setEntity(Util.marshal(_info), MediaType.APPLICATION_XML);

			return;

		}

	}

	@Post
	public void processPost(String body) {

		String epString = (String) getRequest().getAttributes().get(
				Resources.PARAMETER_EP);

		if (epString == null) {
			Parameter timeoutParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst(ResourcePathURIs.TIMEOUT_PARAM);
			if (timeoutParam != null) {
				timeoutString = timeoutParam.getValue().trim();
				try {
					timeout = Long.decode(Resources.HEX_PREFIX + timeoutString);
				} catch (NumberFormatException nfe) {
					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage(nfe.getMessage());
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return;

				}
				// if (timeout < 0 || timeout > 0xffff) {
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: optional '"
							+ ResourcePathURIs.TIMEOUT_PARAM
							+ "' parameter's value invalid. You provided: "
							+ timeoutString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return;

				}
			}

			SimpleDescriptor simpleDescriptor;
			try {
				simpleDescriptor = Util.unmarshal(body, SimpleDescriptor.class);
			} catch (Exception jbe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Malformed SimpleDesriptor in request");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}

			// Actual Gal call

			try {
				// Gal Manager check
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();

				// ConfigureEndpoint
				short endPoint = proxyGalInterface.configureEndpoint(timeout,
						simpleDescriptor);
				if (endPoint > 0) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.SUCCESS);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					detail.setEndpoint(endPoint);
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return;

				} else {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error in creating end point. Not created.");
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return;

				}
			} catch (NullPointerException npe) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(npe.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			} catch (Exception e1) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(e1.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;
			}

		}
	}

	@Delete
	public void precessDelete() {

		try {

			String epString = "";
			epString = (String) getRequest().getAttributes().get(
					Resources.PARAMETER_EP);

			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			Short endpoint = Short.parseShort(epString, 16);

			// ClearEndpoint
			proxyGalInterface.clearEndpoint(endpoint);
			Info i = new Info();

			Status st = new Status();
			st.setCode((short) GatewayConstants.SUCCESS);
			i.setStatus(st);
			getResponse().setEntity(Util.marshal(i), MediaType.APPLICATION_XML);

			return;
		} catch (NullPointerException npe) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(npe.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		} catch (Exception e) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(e.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		}
	}

	/**
	 * Gets the RestManager.
	 * 
	 * @return the RestManager.
	 */
	private RestManager getRestManager() {
		return ((GalManagerRestApplication) getApplication()).getRestManager();
	}
}