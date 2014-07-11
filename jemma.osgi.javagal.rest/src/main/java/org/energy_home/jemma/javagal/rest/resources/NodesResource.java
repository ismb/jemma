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
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;

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
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *  Resource file used to manage the API GET:readNodeCache, subscribeNodeRemoval, startNodeDiscovery.
 *  
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class NodesResource extends ServerResource {
	private GatewayInterface proxyGalInterface = null;

	@Get
	public void processGet() throws ResourceException {
		// Uri parameters check
		String modeString = null;

		Parameter modeParam = getRequest().getResourceRef().getQueryAsForm()
				.getFirst(Resources.URI_PARAM_MODE);
		if (modeParam != null) {
			modeString = modeParam.getValue();
		}
		if (modeString != null) {
			// Mode parameter is present, it's a Read Node Cache
			// Control mode parameter's validity
			if (!modeString.equals(Resources.URI_PARAM_CACHE)) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: optional '" + Resources.URI_PARAM_MODE
						+ "' parameter's value invalid. You provided: "
						+ modeString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}

			try {
				proxyGalInterface = getRestManager().getClientObjectKey(-1,
						getClientInfo().getAddress()).getGatewayInterface();
				// ReadNodeCache
				WSNNodeList _cache = proxyGalInterface.readNodeCache();

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.SUCCESS);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				detail.setWSNNodes(_cache);
				info.setDetail(detail);
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
			// Mode parameter not present, it's a
			// StartNodeDiscovery
			// or a
			// SubscribeNodeRemoval
			String timeoutString = null;
			String urilistener = null;
			Long timeout = -1l;

			// Timeout Parameter
			Parameter timeoutParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst(Resources.URI_PARAM_TIMEOUT);
			if (timeoutParam == null) {
				Detail _det = new Detail();
				Info _info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.SUCCESS);
				_info.setStatus(_st);
				_det.getValue().add(Resources.URI_ADDR);
				_info.setDetail(_det);
				getResponse().setEntity(Util.marshal(_info),
						MediaType.APPLICATION_XML);
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

			// Urilistener parameter
			Parameter urilistenerParam = getRequest().getResourceRef()
					.getQueryAsForm().getFirst(Resources.URI_PARAM_URILISTENER);

			// Urilistener is mandatory
			if (urilistenerParam == null) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory '"
						+ ResourcePathURIs.URILISTENER_PARAM
						+ "' parameter's is not present.");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			} else {
				urilistener = urilistenerParam.getValue();
			}

			// ReportOnExistingNodes parameter
			Parameter reportOnExistingNodesParam = getRequest()
					.getResourceRef().getQueryAsForm()
					.getFirst(ResourcePathURIs.DISCOVERY_INQUIRY);

			// ReportOnExistingNodes is no longer implemented
			if (reportOnExistingNodesParam != null) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: optional '"
						+ ResourcePathURIs.DISCOVERY_INQUIRY
						+ "' parameter's is no longer implemented.");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			}

			// ReportAnnouncements parameter
			Parameter reportAnnouncementsParam = getRequest().getResourceRef()
					.getQueryAsForm()
					.getFirst(ResourcePathURIs.DISCOVERY_ANNOUNCEMENTS);

			// Lqi parameter
			Parameter lqiParam = getRequest().getResourceRef().getQueryAsForm()
					.getFirst(ResourcePathURIs.DISCOVERY_LQI);

			// ReportLeave parameter
			Parameter reportLeaveParam = getRequest().getResourceRef()
					.getQueryAsForm()
					.getFirst(ResourcePathURIs.DISCOVERY_LEAVE);

			// Freshness parameter
			Parameter freshnessParam = getRequest().getResourceRef()
					.getQueryAsForm()
					.getFirst(ResourcePathURIs.DISCOVERY_FRESHNESS);

			// Calculating the discovery mask
			int discoveryMask = -1;
			if (reportAnnouncementsParam != null) {
				if (discoveryMask == -1) {
					discoveryMask = 0;
				}
				discoveryMask = discoveryMask
						| GatewayConstants.DISCOVERY_ANNOUNCEMENTS;
			}
			if (lqiParam != null) {
				if (discoveryMask == -1) {
					discoveryMask = 0;
				}
				discoveryMask = discoveryMask | GatewayConstants.DISCOVERY_LQI;
			}

			// Calculating the freshness mask
			int freshnessMask = -1;
			if (freshnessParam != null) {
				if (freshnessMask == -1) {
					freshnessMask = 0;
				}
				freshnessMask = freshnessMask
						| GatewayConstants.DISCOVERY_FRESHNESS;
			}
			if (reportLeaveParam != null) {
				if (freshnessMask == -1) {
					freshnessMask = 0;
				}
				freshnessMask = freshnessMask
						| GatewayConstants.DISCOVERY_LEAVE;
			}

			// Control if it's a Start Node Discovery or a SubscribeNodeRemoval
			// or a "Stop" request
			if ((discoveryMask == -1) && (freshnessMask == -1)) {
				// It's a "Stop" request
				try {
					proxyGalInterface = getRestManager().getClientObjectKey(-1,
							getClientInfo().getAddress()).getGatewayInterface();

					if (urilistener.toLowerCase().contains("nodediscovered"))
							proxyGalInterface.startNodeDiscovery(timeout, 0);
					if (urilistener.toLowerCase().contains("noderemoved"))
						proxyGalInterface.subscribeNodeRemoval(timeout, 0);
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
			} else if ((discoveryMask != -1) && (freshnessMask != -1)) {
				// Error: you cannot ask for both a Start Node Discovery and
				// SubscribeNodeRemoval in the same request

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: you cannot ask for both a Start Node Discovery and SubscribeNodeRemoval in the same request");
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return;

			} else if ((discoveryMask != -1) && (freshnessMask == -1)) {
				// It's a StartNodeDiscovery
				try {
					// Obtaining the listener
					ClientResources rcmal = getRestManager()
							.getClientObjectKey(
									Util.getPortFromUriListener(urilistener),
									getClientInfo().getAddress());
					proxyGalInterface = rcmal.getGatewayInterface();
					// Setting the urlilistener to the listener
					rcmal.getClientEventListener()
							.setNodeDiscoveredDestination(urilistener);

					// StartNodeDiscovery
					proxyGalInterface
							.startNodeDiscovery(timeout, discoveryMask);
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
			} else if ((discoveryMask == -1) && (freshnessMask != -1)) 
			{

				// It's a SubscribeNodeRemoval
				try {
					// Obtaining the listener
					ClientResources rcmal = getRestManager()
							.getClientObjectKey(
									Util.getPortFromUriListener(urilistener),
									getClientInfo().getAddress());
					proxyGalInterface = rcmal.getGatewayInterface();
					// Setting the urlilistener to the listener
					rcmal.getClientEventListener().setNodeRemovedDestination(
							urilistener);

					// SubscribeNodeRemoval
					proxyGalInterface.subscribeNodeRemoval(timeout,
							freshnessMask);
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