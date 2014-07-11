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
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.Status;

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestClientManagerAndListener;
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
 *  Resource file used to manage the API GET:startServiceDiscoverySync, startServiceDiscovery
 *  
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ServicesResource extends ServerResource {

	private GatewayInterface proxyGalInterface;
	private String timeoutString = null;
	private Long timeout = (long) INTERNAL_TIMEOUT;

	@Get
	public void processGet() {
		String addrString = (String) getRequest().getAttributes().get("addr");
		Address address = new Address();
		if (addrString.length() > 4) {
			BigInteger addressBigInteger = BigInteger.valueOf(Long.parseLong(
					addrString, 16));
			address.setIeeeAddress(addressBigInteger);
		} else {
			Integer addressInteger = Integer.parseInt(addrString, 16);
			address.setNetworkAddress(addressInteger);
		}

		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam != null) {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode(Resources.HEX_PREFIX + timeoutString);
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
			} catch (NumberFormatException nfe) {

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

		// Urilistener parameter
		String urilistener = null;
		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);
		// Urilistener is mandatory
		if (urilistenerParam != null) {
			// TODO Marco why getSecond() and not getValue()?
			// urilistener = urilistenerParam.getSecond();
			urilistener = urilistenerParam.getValue();
		}

		try {
			if (urilistener == null) {
				// Synch StartServiceDiscovery
				proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();
				NodeServices node = proxyGalInterface
						.startServiceDiscoverySync(timeout, address);
				
				
				Info.Detail detail = new Info.Detail();
				detail.setNodeServices(node);
				Info infoToReturn = new Info();
				Status status = new Status();
				status.setCode((short) GatewayConstants.SUCCESS);
				infoToReturn.setStatus(status);
				infoToReturn.setDetail(detail);
				getResponse().setEntity(Util.marshal(infoToReturn),
						MediaType.TEXT_XML);
				
				return ;
			} else {
				// Asynch
				ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();
				if (!urilistener.equals("")) {
					rcmal.getClientEventListener().setNodeServicesDestination(urilistener);
				}
				proxyGalInterface.startServiceDiscovery(timeout, address);
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