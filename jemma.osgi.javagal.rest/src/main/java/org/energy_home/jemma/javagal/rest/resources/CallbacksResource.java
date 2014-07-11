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
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestMessageListener;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:getlistCallbacks. POST:createCallback
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class CallbacksResource extends ServerResource {

	private GatewayInterface proxyGalInterface = null;

	@Get
	public void represent() {

		// ListCallbacks
		try {
			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			CallbackIdentifierList callbacksList = proxyGalInterface
					.getlistCallbacks();
			
			Info.Detail detail = new Info.Detail();
			detail.setCallbacks(callbacksList);
			Info infoToReturn = new Info();
			Status status = new Status();
			status.setCode((short) GatewayConstants.SUCCESS);
			infoToReturn.setStatus(status);
			infoToReturn.setDetail(detail);
			getResponse().setEntity(Util.marshal(infoToReturn),
					MediaType.TEXT_XML);
			
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

	@Post
	public void processPost(String body) {

		// Uri parameters check
		String urilistener = null;
		Callback callback = null;
		try {
			callback = Util.unmarshal(body, Callback.class);
		} catch (Exception jbe) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Malformed Callback in request");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}
		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);
		if (urilistenerParam != null && !urilistenerParam.equals("")) {
			urilistener = urilistenerParam.getValue().trim();
		} else {
			urilistener = callback.getAction().getForwardingSpecification();
		}

		if (urilistener == null) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Mandatory " + Resources.URI_PARAM_URILISTENER
					+ " parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}

		// Actual Gal call
		try {

			ClientResources rcmal = getRestManager().getClientObjectKey(
					Util.getPortFromUriListener(urilistener),
					getClientInfo().getAddress());
			proxyGalInterface = rcmal.getGatewayInterface();

			RestMessageListener listener = new RestMessageListener(
					callback, urilistener,rcmal,getRestManager().getPropertiesManager());
			
			Long id = proxyGalInterface.createCallback(callback, listener);

			if (id >= 0) {
				listener.setCallBackId(id);
				rcmal.getCallbacksEventListeners().put(id, listener);
				
				Info.Detail detail = new Info.Detail();
				detail.setCallbackIdentifier(id);
				Info infoToReturn = new Info();
				Status status = new Status();
				status.setCode((short) GatewayConstants.SUCCESS);
				infoToReturn.setStatus(status);
				infoToReturn.setDetail(detail);
				getResponse().setEntity(Util.marshal(infoToReturn),
						MediaType.TEXT_XML);
				return;
				
			} else {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error in creating callback. Not created.");
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