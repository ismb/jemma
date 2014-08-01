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
import org.energy_home.jemma.zgd.jaxb.Status;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:frequencyAgilitySync, frequencyAgility
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class FrequenceAgilityResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet(String body) {
		String urilistener = null;
		String timeoutString = null;
		String scanChannelString = null;
		String scanDurationString = null;

		Long timeout = -1l;
		Long scanChannel = 0l;
		Long scanDuration = (long) 0xFE;

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
			getResponse().setEntity(Util.marshal(info),
					MediaType.APPLICATION_XML);
			return;

		} else {
			timeoutString = timeoutParam.getValue().trim();
			try {
				timeout = Long.decode(timeoutString);
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
		}

		Parameter scanChannelParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_SCANCHANNEL);
		scanChannelString = scanChannelParam.getValue().trim();
		try {
			scanChannel = Long.decode(scanChannelString);
			if (!Util.isUnsigned32(timeout)) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: '" + Resources.URI_SCANCHANNEL
						+ "' parameter's value invalid. You provided: "
						+ scanChannelString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;
			}
		} catch (NumberFormatException nfe) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: '" + Resources.URI_SCANCHANNEL
					+ "' parameter's value invalid. You provided: "
					+ scanChannelString);
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info),
					MediaType.APPLICATION_XML);
			return;

		}

		Parameter scanDurationParam = getRequest().getResourceRef()
				.getQueryAsForm().getFirst(Resources.URI_SCANDURATION);
		if (scanDurationParam != null) {
			scanDurationString = scanDurationParam.getValue().trim();
			try {
				scanDuration = Long.decode(scanDurationString);
				if (!Util.isUnsigned32(timeout)) {

					Info info = new Info();
					Status _st = new Status();
					_st.setCode((short) GatewayConstants.GENERAL_ERROR);
					_st.setMessage("Error: '" + Resources.URI_SCANDURATION
							+ "' parameter's value invalid. You provided: "
							+ scanDurationString);
					info.setStatus(_st);
					Info.Detail detail = new Info.Detail();
					info.setDetail(detail);
					getResponse().setEntity(Util.marshal(info),
							MediaType.APPLICATION_XML);
					return;

				}
			} catch (NumberFormatException nfe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: '" + Resources.URI_SCANDURATION
						+ "' parameter's value invalid. You provided: "
						+ scanDurationString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}
		}

		try {
			Parameter urilistenerParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

			if (urilistenerParam != null) {
				urilistener = urilistenerParam.getValue();
				ClientResources rcmal = getRestManager().getClientObjectKey(
						Util.getPortFromUriListener(urilistener),
						getClientInfo().getAddress());
				proxyGalInterface = rcmal.getGatewayInterface();

				rcmal.getClientEventListener()
						.setFrequencyAgilityResultDestination(urilistener);
				proxyGalInterface.frequencyAgility(timeout,
						scanChannel.shortValue(), scanDuration.shortValue());
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

			} else {
				// Sync
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();
				Status _result = proxyGalInterface.frequencyAgilitySync(
						timeout, scanChannel.shortValue(),
						scanDuration.shortValue());
				Info _st = new Info();
				_st.setStatus(_result);
				getResponse().setEntity(Util.marshal(_st), MediaType.TEXT_XML);

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
			getResponse().setEntity(Util.marshal(info),
					MediaType.APPLICATION_XML);
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