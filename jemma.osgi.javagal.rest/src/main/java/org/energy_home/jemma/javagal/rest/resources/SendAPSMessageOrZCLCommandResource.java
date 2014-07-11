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
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageResult;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageResult;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.ZCLCommand;
import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.AppendableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;


/**
 *  Resource file used to manage the API POST:sendAPSMessage, sendZCLCommand
 *  
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class SendAPSMessageOrZCLCommandResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Post
	public void processPost(String body) {

		// Uri parameters check
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
			return ;

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
				return ;

			}
			// if (timeout < 0 || timeout > 0xffffffff) {
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
				return ;

			}
		}
		short service;
		String serviceString = (String) getRequest().getAttributes().get(
				Resources.PARAMETER_SERVICE);
		try {
			service = Short.parseShort(serviceString, 16);
		} catch (NullPointerException npe) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_SERVICE
					+ "' parameter's value invalid. You provided: "
					+ serviceString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;

		} catch (NumberFormatException nfe) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_SERVICE
					+ "' parameter's value invalid. You provided: "
					+ serviceString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;

		}

		if (!Util.isUnsigned8((long) service)) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_SERVICE
					+ "' parameter's value invalid. You provided: "
					+ serviceString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;

		}

		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

		// Actual Gal call
		
		APSMessage apsMessage = null;
		ZCLCommand zclCommand = null;
		

		try {
			apsMessage = Util.unmarshal(body, APSMessage.class);
		} catch (Exception je) {
			
		}
		
		
		try {
			zclCommand = Util.unmarshal(body, ZCLCommand.class);
		} catch (Exception je) {
			
		}
		
		
		
		

		if (apsMessage != null) {
			// It's a Send APSMessage invocation
			try {
				if (urilistenerParam == null) {
					// Sync call because urilistener not present.
					// Only Asynch is admitted.
					proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();
					int txTime = Util.currentTimeMillis();
					proxyGalInterface.sendAPSMessage(apsMessage);
					Info _info = new Info();
					Status st=new Status();
					st.setCode((short)GatewayConstants.SUCCESS);
					_info.setStatus(st);
					Info.Detail detail = new Info.Detail();
					APSMessageResult apsMessageResult = new APSMessageResult();
					apsMessageResult.setConfirmStatus(0);
					apsMessageResult.setTxTime(txTime);
					detail.setAPSMessageResult(apsMessageResult);
					
					_info.setDetail(detail);
						getResponse().setEntity(Util.marshal(_info),
							MediaType.TEXT_XML);
					
					
					return ;
				} else {
					// Async call. We know here that urilistenerParam is not
					// null...
					urilistener = urilistenerParam.getValue();
					// Process async. If urilistener equals "", don't send the
					// result but wait that the IPHA polls for it using the
					// request
					// identifier.

					proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();

					// TODO control if it's correct this invocation/result
					proxyGalInterface.sendAPSMessage(timeout, apsMessage);
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
				return ;
			}
		} 
		
		
		else if (zclCommand != null)
		{
			
			

			// It's a Send ZCLCommand invocation
			try {
				// Gal Manager check
				proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();

				if (urilistenerParam == null) {
					// Only Asynch is admitted.

					
					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("No Urilistener, Only Asynch is admitted");
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return ;
					
				
				} else {
					// Async call. We know here that urilistenerParam is not
					// null...
					urilistener = urilistenerParam.getValue();
					// Process async. If urilistener equals "", don't send the
					// result but wait that the IPHA polls for it using the
					// request identifier.

					ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
					proxyGalInterface = rcmal.getGatewayInterface();
					rcmal.getClientEventListener().setZclCommandDestination(urilistener);
					proxyGalInterface.sendZCLCommand(timeout, zclCommand);
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					Info info = new Info();
					info.setStatus(new Status());
					info.setRequestIdentifier(Util.getRequestIdentifier());
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return ;
				}
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
				return ;
			}
		}
		
		
		else
		{
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Wrong xml");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info),
					MediaType.APPLICATION_XML);
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