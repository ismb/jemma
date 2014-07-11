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
import org.energy_home.jemma.zgd.jaxb.Status;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestClientManagerAndListener;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 *  Resource file used to manage the API GET:resetDongleSync, resetDongle
 *  
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ResetResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void resetMethod(String body) {

		String urilistener = null;
		String timeoutString = null;
		Long timeout = -1l;
		short warmStartValue = 0x00;

		Parameter urilistenerParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);
		if (urilistenerParam != null) {
			urilistener = urilistenerParam.getValue().trim();
		}
		Parameter timeoutParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_TIMEOUT);
		if (timeoutParam != null) {
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
				_st.setMessage("Wrong timeout");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;
			}
		} else {
			// The timeout parameter is optional. If not provided we use a
			// default.
			timeout = (long) INTERNAL_TIMEOUT;
		}

		// TODO Set the right warm start value.
		String warmparamString = null;

		Parameter warmparam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_START_MODE_RESET);
		if (warmparam != null) {
			warmparamString = warmparam.getValue().trim();
			try {
				warmStartValue = Short.decode(warmparamString);
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

		} else {
			// The warm start value is mandatory!!
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("The warm start value is mandatory");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		}

		if (urilistenerParam == null) {
			// Sync reset
			try {
				proxyGalInterface = getRestManager().getClientObjectKey(-1, getClientInfo().getAddress()).getGatewayInterface();

				Status result = proxyGalInterface.resetDongleSync(timeout,
						warmStartValue);
				Info info = new Info();
				info.setStatus(result);
				getResponse().setEntity(Util.marshal(info), MediaType.TEXT_XML);

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
			// Async reset
			try {
				ClientResources rcmal = getRestManager().getClientObjectKey(Util.getPortFromUriListener(urilistener), getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();
				if (!urilistener.equals("")) {
					rcmal.getClientEventListener().setResetDestination(urilistener);
				}
				proxyGalInterface.resetDongle(timeout, warmStartValue);
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
			} catch (Exception e) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("The warm start value is mandatory");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;
			}
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