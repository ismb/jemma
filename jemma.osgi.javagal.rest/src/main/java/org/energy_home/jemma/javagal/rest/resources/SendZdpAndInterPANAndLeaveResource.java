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

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageResult;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.ZDPCommand;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:URL menu. POST:sendZDPCommand.
 * DELETE:deleteCallBack
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SendZdpAndInterPANAndLeaveResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet() {
		Detail _det = new Detail();
		Info _info = new Info();
		Status _st = new Status();
		_st.setCode((short) GatewayConstants.SUCCESS);
		_info.setStatus(_st);
		_det.getValue().add(ResourcePathURIs.BINDINGS);
		_det.getValue().add(ResourcePathURIs.UNBINDINGS);
		_det.getValue().add(ResourcePathURIs.NODEDESCRIPTOR);
		_det.getValue().add(ResourcePathURIs.SERVICES);
		_det.getValue().add(ResourcePathURIs.PERMIT_JOIN);
		_det.getValue().add(ResourcePathURIs.LQIINFORMATION);
		_info.setDetail(_det);

		getResponse().setEntity(Util.marshal(_info), MediaType.TEXT_XML);
		return;

	}

	@Post
	public void processPost(String body) {

		// Uri parameters check
		String timeoutString = null;
		String urilistener = null;
		String aoiString = null; // Note aoiString is correct even if we read the addr parameter and we call it aoi
		Long timeout = -1l;
		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm().getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam == null) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		} else {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode("0x" + timeoutString);
				// if (timeout < 0 || timeout > 0xffffffff) {
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
					return;

				}
			} catch (NumberFormatException nfe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
				return;

			}
		}

		aoiString = (String) getRequest().getAttributes().get(Resources.PARAMETER_ADDR);

		if (aoiString == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: " + Resources.URI_ADDR + " missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}

		Address address = new Address();
		if (aoiString.length() > 4) {
			// IEEEAddress
			BigInteger ieee = new BigInteger(aoiString, 16);
			address.setIeeeAddress(ieee);
		} else {
			// ShortAddress
			Integer shortAddress = new Integer(Integer.parseInt(aoiString, 16));
			address.setNetworkAddress(shortAddress);
		}

		Parameter urilistenerParam = getRequest().getResourceRef().getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

		ZDPCommand zdpCommand = null;

		InterPANMessage interPANMessage = null;

		try {
			zdpCommand = Util.unmarshal(body, ZDPCommand.class);
		} catch (Exception je) {

		}

		try {
			interPANMessage = Util.unmarshal(body, InterPANMessage.class);
		} catch (Exception je) {

		}

		if (interPANMessage != null) {

			// It's a Send InterPan message invocation
			try {
				if (urilistenerParam == null) {
					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Sync call because urilistener not present. Not implemented. Only asynch is admitted");
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
					return;
				} else {
					urilistener = urilistenerParam.getValue();
					ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
					proxyGalInterface = rcmal.getGatewayInterface();
					if (rcmal.getClientEventListener() != null)
						rcmal.getClientEventListener().setInterPANCommandDestination(urilistener);
					proxyGalInterface.sendInterPANMessage(timeout, interPANMessage);
					Info.Detail detail = new Info.Detail();
					Info infoToReturn = new Info();
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					infoToReturn.setStatus(status);
					infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
					infoToReturn.setDetail(detail);
					getResponse().setEntity(Util.marshal(infoToReturn), MediaType.TEXT_XML);
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

		else if (zdpCommand != null) {

			// It's a Send Zdp Message message invocation
			try {
				if (urilistenerParam == null) {
					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Sync call because urilistener not present. Not implemented. Only asynch is admitted");
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
					return;
				} else {
					urilistener = urilistenerParam.getValue();
					ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
					proxyGalInterface = rcmal.getGatewayInterface();
					if (rcmal.getClientEventListener() != null)
						rcmal.getClientEventListener().setZdpCommandDestination(urilistener);
					proxyGalInterface.sendZDPCommand(timeout, zdpCommand);
					Info.Detail detail = new Info.Detail();
					Info infoToReturn = new Info();
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					infoToReturn.setStatus(status);
					infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
					infoToReturn.setDetail(detail);
					getResponse().setEntity(Util.marshal(infoToReturn), MediaType.TEXT_XML);
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

		} else {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Wrong xml");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
		}

	}

	@Delete
	public void processDelete() {
		Address address = new Address();

		String timeoutString = null;
		String urilistener = null;
		Long timeout = -1l;
		// addrString parameters check
		String addrString = (String) getRequest().getAttributes().get(Resources.PARAMETER_ADDR);
		if (addrString != null) {
			if (addrString.length() > 4) {
				// IEEEAddress
				BigInteger ieee = new BigInteger(addrString, 16);
				address.setIeeeAddress(ieee);
			} else {
				// ShortAddress
				Integer shortAddress = new Integer(Integer.parseInt(addrString, 16));
				address.setNetworkAddress(shortAddress);
			}
		} else {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.PARAMETER_ADDR + "' parameter's value invalid. You provided: " + addrString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}

		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm().getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's not present");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		} else {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode("0x" + timeoutString);
				// if (timeout < 0 || timeout > 0xffff) {
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
					return;

				}
			} catch (NumberFormatException nfe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_TIMEOUT + "' parameter's value invalid. You provided: " + timeoutString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
				return;

			}
		}
		Parameter urilistenerParam = getRequest().getResourceRef().getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

		try {
			if (urilistenerParam == null) {
				// Sync call because urilistener not present.

				// Check for Gal Interface
				proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();

				// TODO exists also leaveSync(timeout, addrOfInterest, mask)
				// Leave
				Status status = proxyGalInterface.leaveSync(timeout, address, 0);
				Info info = new Info();
				info.setStatus(status);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info), MediaType.TEXT_XML);
				return;
			} else {
				// Async call
				// We know here that urilistenerParam is not null...
				urilistener = urilistenerParam.getValue();
				// Process async. If urilistener equals "", don't send the
				// result
				// but wait that the IPHA polls for it using the request
				// identifier. Async is possible only if start=true

				ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();

				rcmal.getClientEventListener().setLeaveResultDestination(urilistener);
				proxyGalInterface.leave(timeout, address);
				Info.Detail detail = new Info.Detail();
				Info infoToReturn = new Info();
				Status status = new Status();
				status.setCode((short) GatewayConstants.SUCCESS);
				infoToReturn.setStatus(status);
				infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
				infoToReturn.setDetail(detail);
				getResponse().setEntity(Util.marshal(infoToReturn), MediaType.TEXT_XML);
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
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;
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