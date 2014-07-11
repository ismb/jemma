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

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeServicesList;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 *  Resource file used to manage the API GET:readServicesCache, readNodeCache
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ReadServiceCacheResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void readServiceCacheGetmethod() {

		String modeString = null;
		Parameter modeParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_MODE);
		if (modeParam == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory '" + Resources.URI_PARAM_MODE
					+ "' parameter missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return;

		} else {
			modeString = modeParam.getValue().trim();
			if (modeString.equals("cache")) {

				try {
					proxyGalInterface = getRestManager().getClientObjectKey(-1,
							getClientInfo().getAddress()).getGatewayInterface();
					NodeServicesList _result = proxyGalInterface
							.readServicesCache();

					Info.Detail detail = new Info.Detail();
					detail.setNodeServicesList(_result);
					getResponse().setEntity(Util.marshal(detail),
							MediaType.APPLICATION_XML);
					return;

				} catch (Exception npe) {
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
				}

			} else {
				try {
					// Gal Manager check
					proxyGalInterface = getRestManager().getClientObjectKey(-1,
							getClientInfo().getAddress()).getGatewayInterface();
					WSNNodeList nodeList = proxyGalInterface.readNodeCache();
					Status status = new Status();
					status.setCode((short) GatewayConstants.SUCCESS);
					Info info = new Info();
					info.setStatus(status);
					Info.Detail detail = new Info.Detail();
					detail.setWSNNodes(nodeList);
					info.setDetail(detail);
					getResponse().setStatus(new org.restlet.data.Status(200));
					getResponse().setEntity(Util.marshal(info),
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