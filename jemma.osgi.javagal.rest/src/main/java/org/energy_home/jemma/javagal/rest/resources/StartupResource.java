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

import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestClientManagerAndListener;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
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
 *  Resource file used to manage the API GET:readStartupAttributeSet. POST:configureStartupAttributeSet, startGatewayDeviceSync, startGatewayDevice. DELETE:stopNetworkSync, stopNetwork
 *
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class StartupResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void represent() {

		String indexString = "";
		Long index = -1l;

		Parameter indexParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_INDEX);
		if (indexParam == null)
		{
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Index parameter is mandatory");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		}
		else
			indexString = indexParam.getValue().trim();
		try {
			index = Long.decode("0x" + indexString);
		} catch (NumberFormatException nfe) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(nfe.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}
		// if (timeout < 0 || timeout > 0xffff) {
		if (!Util.isUnsigned8(index)) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT
					+ "' parameter's value invalid. You provided: "
					+ indexString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		}

		try {
			// Gal Manager check
			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			// ReadStartupAttributeSet
			StartupAttributeInfo sai = proxyGalInterface
					.readStartupAttributeSet(index.shortValue());

			Info.Detail detail = new Info.Detail();
			detail.setStartupAttributeInfo(sai);
			Info infoToReturn = new Info();
			Status status = new Status();
			status.setCode((short) GatewayConstants.SUCCESS);
			infoToReturn.setStatus(status);
			infoToReturn.setDetail(detail);
			getResponse().setEntity(Util.marshal(infoToReturn),
					MediaType.TEXT_XML);

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

	@Post
	public void processPost(String body) {

		// Uri parameters check
		String startString = null;
		String timeoutString = null;
		String urilistener = null;
		Long timeout = -1l;

		Parameter startParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_START);
		if (startParam == null) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory start parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		} else {
			startString = startParam.getValue();
			if (!(startString.equals("true") || startString.equals("false"))) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_START
						+ "' parameter's value invalid. You provided: "
						+ startString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}
		}

		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT
					+ "' parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		} else {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode("0x" + timeoutString);
			} catch (NumberFormatException nfe) {
			}
			// if (timeout < 0 || timeout > 0xffff) {
			if (!Util.isUnsigned32(timeout)) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '"
						+ Resources.URI_PARAM_TIMEOUT
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
		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

		// Actual Gal call
		if (urilistenerParam == null) {
			// Sync call because urilistener not present.
			if (startString.equals("true")) {
				// Real Startup (start=true)
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					proxyGalInterface = getRestManager().getClientObjectKey(-1,
							getClientInfo().getAddress()).getGatewayInterface();
					// StartGatewayDevice synch
					Status status = proxyGalInterface.startGatewayDeviceSync(
							timeout, sai);
					Info info = new Info();
					info.setStatus(status);
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
			} else {
				// ConfigureStartupAttributeInfo (start=false)
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					proxyGalInterface = getRestManager().getClientObjectKey(-1,
							getClientInfo().getAddress()).getGatewayInterface();
					// ConfigureStartupAttributeSet
					proxyGalInterface.configureStartupAttributeSet(sai);
					Info.Detail detail = new Info.Detail();
					Info infoToReturn = new Info();
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					infoToReturn.setStatus(status);
					infoToReturn.setDetail(detail);

					getResponse().setEntity(Util.marshal(infoToReturn),
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
		} else {
			// Async call
			// We know here that urilistenerParam is not null...
			urilistener = urilistenerParam.getValue();
			// Process async. If urilistener equals "", don't send the result
			// but wait that the IPHA polls for it using the request
			// identifier. Async is possible only if start=true
			if (startString.equals("true")) {
				// Real Startup
				StartupAttributeInfo sai;
				try {
					sai = Util.unmarshal(body, StartupAttributeInfo.class);
					// Gal Manager check
					ClientResources rcmal = getRestManager()
							.getClientObjectKey(
									Util.getPortFromUriListener(urilistener),
									getClientInfo().getAddress());

					proxyGalInterface = rcmal.getGatewayInterface();
					if (!urilistener.equals("")) {
						rcmal.getClientEventListener()
								.setStartGatewayDestination(urilistener);
					}
					proxyGalInterface.startGatewayDevice(timeout, sai);
					Info.Detail detail = new Info.Detail();
					Info infoToReturn = new Info();
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					infoToReturn.setStatus(status);
					infoToReturn.setRequestIdentifier(Util
							.getRequestIdentifier());
					infoToReturn.setDetail(detail);
					getResponse().setEntity(Util.marshal(infoToReturn),
							MediaType.TEXT_XML);
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
			} else {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: asynch call with start= false. You cannot make a ConfigureStartupAttributeInfo asynchronously.");
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
	public void stopMethod() {

		String timeoutString = null;
		String urilistener = null;
		Long timeout = -1l;

		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT
					+ "' parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		} else {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode("0x" + timeoutString);
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
			if (!Util.isUnsigned32(timeout)) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '"
						+ Resources.URI_PARAM_TIMEOUT + "' parameter missing.");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}
		}

		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

		try {
			if (urilistenerParam == null) {
				// Sync call because urilistener not present.
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();
				Status status = proxyGalInterface.stopNetworkSync(timeout);
				Info info = new Info();
				info.setStatus(status);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;
			} else {
				// Async call
				// We know here that urilistenerParam is not null...
				urilistener = urilistenerParam.getValue();

				ClientResources rcmal = getRestManager().getClientObjectKey(
						Util.getPortFromUriListener(urilistener),
						getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();
				rcmal.getClientEventListener().setGatewayStopDestination(
						urilistener);
				proxyGalInterface.stopNetwork(timeout);
				Info.Detail detail = new Info.Detail();
				Info infoToReturn = new Info();
				Status status = new Status();
				status.setCode((short) GatewayConstants.SUCCESS);
				infoToReturn.setStatus(status);
				infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
				infoToReturn.setDetail(detail);
				getResponse().setEntity(Util.marshal(infoToReturn),
						MediaType.TEXT_XML);
				return;
			}
		} catch (Exception e1) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(e1.getMessage());
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