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
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:getServiceDescriptorSync, getServiceDescriptor
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class GetServiceDescriptorResource extends ServerResource {
	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet(String body) {

		String urilistener;

		try {
			Address _add = new Address();
			Short _ep;
			// addrString parameters check
			String addrString = (String) getRequest().getAttributes().get(
					"addr");
			if (addrString != null) {
				if (addrString.length() > 4)// IEEEAddress
				{
					BigInteger iee = new BigInteger(addrString, 16);
					_add.setIeeeAddress(iee);
				} else // ShortAddress
				{
					Integer _sa = new Integer(Integer.parseInt(addrString, 16));
					_add.setNetworkAddress(_sa);
				}
			} else {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_MODE
						+ "' parameter's value invalid. You provided: "
						+ addrString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}

			// epString parameters check
			String epString = (String) getRequest().getAttributes().get("ep");
			if (epString != null) {
				_ep = Short.parseShort(epString, 16);
			} else {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '" + Resources.URI_ENDPOINT
						+ "' parameter's value invalid. You provided: "
						+ epString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}
			String timeoutString = null;
			Long timeout = -1L;
			Parameter timeoutParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst("timeout");
			if (timeoutParam != null) {
				timeoutString = timeoutParam.getSecond();
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
					return ;

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
					return ;

				}
			}

			Parameter urilistenerParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

			if (urilistenerParam == null) {
				// Sync call because urilistener not present.
				// Gal Manager check
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();
				ServiceDescriptor sd = proxyGalInterface
						.getServiceDescriptorSync(timeout, _add, _ep);
				
				Detail _det = new Detail();
				_det.setServiceDescriptor(sd);
				Info _info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.SUCCESS);
				_info.setStatus(_st);
				_info.setDetail(_det);
				getResponse().setEntity(Util.marshal(_info), MediaType.APPLICATION_XML);
				return;
				
			} else {
				// Async call. We know here that urilistenerParam is not null...
				urilistener = urilistenerParam.getValue();
				// Process async. If urilistener equals "", don't send the
				// result but wait that the IPHA polls for it using the request
				// identifier.
				ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();

				rcmal.getClientEventListener().setServiceDescriptorDestination(urilistener);
				proxyGalInterface.getServiceDescriptor(timeout, _add, _ep);
				Info.Detail detail = new Info.Detail();
				Info infoToReturn = new Info();
				Status status = new Status();
				status.setCode((short) GatewayConstants.SUCCESS);
				infoToReturn.setStatus(status);
				infoToReturn.setRequestIdentifier(Util.getRequestIdentifier());
				infoToReturn.setDetail(detail);
				getResponse().setEntity(Util.marshal(infoToReturn),
						MediaType.TEXT_XML);
				return ;
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
			return ;

		} catch (Exception e) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(e.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;
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
