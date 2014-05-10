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
package org.energy_home.jemma.javagal.rest;

import org.energy_home.jemma.javagal.rest.resources.BindingsResource;
import org.energy_home.jemma.javagal.rest.resources.CallbackResource;
import org.energy_home.jemma.javagal.rest.resources.CallbacksResource;
import org.energy_home.jemma.javagal.rest.resources.CallbacksShorthandAllServicesResource;
import org.energy_home.jemma.javagal.rest.resources.CallbacksShorthandForEndpointResource;
import org.energy_home.jemma.javagal.rest.resources.ChannelResource;
import org.energy_home.jemma.javagal.rest.resources.FrequenceAgilityResource;
import org.energy_home.jemma.javagal.rest.resources.GetNodeDescriptorResource;
import org.energy_home.jemma.javagal.rest.resources.GetServiceDescriptorResource;
import org.energy_home.jemma.javagal.rest.resources.GetVersionResource;
import org.energy_home.jemma.javagal.rest.resources.InformationBaseResource;
import org.energy_home.jemma.javagal.rest.resources.LeaveAllResource;
import org.energy_home.jemma.javagal.rest.resources.ListAddressesResource;
import org.energy_home.jemma.javagal.rest.resources.LocalServicesResource;
import org.energy_home.jemma.javagal.rest.resources.NodesResource;
import org.energy_home.jemma.javagal.rest.resources.PermitJoinAllResource;
import org.energy_home.jemma.javagal.rest.resources.PermitJoinResource;
import org.energy_home.jemma.javagal.rest.resources.ReadServiceCacheResource;
import org.energy_home.jemma.javagal.rest.resources.ResetResource;
import org.energy_home.jemma.javagal.rest.resources.SendAPSMessageOrZCLCommandResource;
import org.energy_home.jemma.javagal.rest.resources.SendZdpAndInterPANAndLeaveResource;
import org.energy_home.jemma.javagal.rest.resources.ServicesResource;
import org.energy_home.jemma.javagal.rest.resources.StartupResource;
import org.energy_home.jemma.javagal.rest.resources.UnbindingsResource;
import org.energy_home.jemma.javagal.rest.resources.allLqiInformationClass;
import org.energy_home.jemma.javagal.rest.resources.firstLevelReources;
import org.energy_home.jemma.javagal.rest.resources.lqiInformationClass;
import org.energy_home.jemma.javagal.rest.resources.netDefaultIbLevelResource;
import org.energy_home.jemma.javagal.rest.resources.netDefaultLevelReources;
import org.energy_home.jemma.javagal.rest.resources.netDefaultLocalnodeAllservicesLevelResources;
import org.energy_home.jemma.javagal.rest.resources.netDefaultLocalnodeLevelReources;
import org.energy_home.jemma.javagal.rest.resources.netDefaultLocalnodeServicesEndPointLevelResources;
import org.energy_home.jemma.javagal.rest.resources.netLevelReources;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * The core Rest application. It associates incoming uri's to resources where
 * the right elaboration is made.
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GalManagerRestApplication extends Application {
	private RestManager restManager;
	

	/**
	 * Creates a new instance with a given rest manager.
	 * 
	 * @param restManager
	 *            the rest manager.
	 */
	public GalManagerRestApplication(RestManager restManager) {
		super();
		this.restManager = restManager;
	}

	/**
	 * Creates a root Restlet that will receive all incoming calls and
	 * associates every uri to its resource class.
	 */
	@Override
	public synchronized Restlet createInboundRoot() {
		/*
		 * Create a Restlet router that routes each call to a new instance of
		 * the relative resource.
		 */
		Router router = new Router(getContext());

		/**
		 * ********** ROOT RESOURCES **********
		 */

		/*
		 * Defines default route "/"
		 */
		router.attach(Resources.GW_ROOT_URI + "/", firstLevelReources.class);

		
		/*
		 * Defines Reset route "/reset"
		 */
		router.attach(Resources.GW_ROOT_URI + ResourcePathURIs.RESET,
				ResetResource.class);

		/*
		 * Defines StartupGatewayDevice route "/startup"
		 */
		router.attach(Resources.GW_ROOT_URI + ResourcePathURIs.STARTUP,
				StartupResource.class);

		/*
		 * Defines getVersion route "/version"
		 */
		router.attach(Resources.GW_ROOT_URI + ResourcePathURIs.VERSION,
				GetVersionResource.class);

		/*
		 * ********** NETWORK RESOURCES **********
		 */

		/*
		 * Defines ListAddresses route "/net"
		 */
		router.attach(Resources.GW_ROOT_URI + Resources.NET_ROOT_URI,
				netLevelReources.class);

		/*
		 * Defines ListAddresses route "/net/default"
		 */
		router.attach(Resources.GW_ROOT_URI + Resources.NWT_ROOT_URI,
				netDefaultLevelReources.class);

		
		/*
		 * Defines getChannel route "net/default/channel"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.CHANNEL,
				ChannelResource.class);

		/*
		 * Defines Information Base route "net/default/ib/"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.INFOBASE + "/"
				, netDefaultIbLevelResource.class);

		/*
		 * Defines Information Base route "net/default/ib/{attr}"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.INFOBASE + "/"
				+ Resources.URI_ATTR, InformationBaseResource.class);

		
		
		/*
		 * Defines ListAddresses route "/net/default/aliases"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.ALIASES,
				ListAddressesResource.class);

		/*
		 * Defines Callbacks route "/net/default/callbacks"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.CALLBACKS,
				CallbacksResource.class);

		/*
		 * Defines Callbacks route "/net/default/callbacks/{id}"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.CALLBACKS
				+ Resources.URI_ID, CallbackResource.class);

		
		/*
		 * "/net/default/localnode"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.LOCALNODE,
				netDefaultLocalnodeLevelReources.class);

		/*
		 * "/net/default/localnode/allservices"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.LOCALNODE
				+ ResourcePathURIs.ALLSERVICES,
				netDefaultLocalnodeAllservicesLevelResources.class);

		/*
		 * "/net/default/localnode/allservices/wsnconnection"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_ALLSERVICES_WSNCONNECTION,
				CallbacksShorthandAllServicesResource.class);

		/*
		 * Defines Frequency Agility route
		 * "/net/default/localnode/frequencyagility?timeout={0:x8}&scanChannel={1}&scanDuration={2}"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.URI_FREQUENCY_AGILITY,
				FrequenceAgilityResource.class);

		/*
		 * Defines LocalServices route "/net/default/localnode/services"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_SERVICES,
				LocalServicesResource.class);

		/*
		 * Defines LocalServices route "/net/default/localnode/services/{ep}"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_SERVICES + Resources.URI_ENDPOINT,
				LocalServicesResource.class);

		/*
		 * "/net/default/localnode/services/{ep}/"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_SERVICES + Resources.URI_ENDPOINT,
				netDefaultLocalnodeServicesEndPointLevelResources.class);

		/*
		 * "/net/default/localnode/services/{ep}/wsnconnection"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_SERVICES + Resources.URI_ENDPOINT
				+ ResourcePathURIs.WSNCONNECTION,
				CallbacksShorthandForEndpointResource.class);

		/*
		 * Defines Send APS Message route
		 * "/net/default/localnode/services/{0:x2}/wsnconnection/message?timeout={1:x8}"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.LOCALNODE_SERVICES + Resources.URI_SERVICE
				+ ResourcePathURIs.SEND_APSMESSAGE,
				SendAPSMessageOrZCLCommandResource.class);

		/*
		 * Defines PermitjoinAll route "/net/default/allwsnnodes"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.ALLWSNNODES,
				LeaveAllResource.class);

		/*
		 * Defines route "/net/default/allwsnnodes/services?mode=cache"
		 */
		router.attach(Resources.NWT_ROOT_URI
				+ ResourcePathURIs.ALLWSNNODES_SERVICES,
				ReadServiceCacheResource.class);

		/*
		 * Defines PermitjoinAll route "/net/default/allwsnnodes/permitjoin"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.ALLPERMIT_JOIN,
				PermitJoinAllResource.class);

		/*
		 * Defines GenNetworkCache route "/net/default/wsnnodes"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES,
				NodesResource.class);

		/*
		 * Defines GenNetworkCache route "/net/default/wsnnodes/{addr}"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR, SendZdpAndInterPANAndLeaveResource.class);

		/*
		 * GetNodeDescriptor route "/net/default/wsnnodes/{addr}/bindings"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.BINDINGS,
				BindingsResource.class);

		/*
		 * GetNodeDescriptor route "/net/default/wsnnodes/{addr}/nodedescriptor"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.NODEDESCRIPTOR,
				GetNodeDescriptorResource.class);

		/*
		 * GetNodeDescriptor route "/net/default/wsnnodes/{addr}/unbindings"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.UNBINDINGS,
				UnbindingsResource.class);

		/*
		 * GetServiceDescriptor route
		 * "/net/default/wsnnodes/{addr}/services/{ep}"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.SERVICES
				+ Resources.URI_ENDPOINT, GetServiceDescriptorResource.class);

		/*
		 * LocalServices route "/net/default/wsnnodes/{addr}/services"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.SERVICES,
				ServicesResource.class);

		/*
		 * Defines Permitjoin route "/net/default/wsnnodes/{addr}/permitjoin"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_AOI + ResourcePathURIs.PERMIT_JOIN,
				PermitJoinResource.class);
		
		/*
		 * LocalServices route "/net/default/wsnnodes/lqi"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.ALLWSNNODES
				+  ResourcePathURIs.LQIINFORMATION,
				allLqiInformationClass.class);
		
		/*
		 * LocalServices route "/net/default/wsnnodes/{addr}/lqi"
		 */
		router.attach(Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES
				+ Resources.URI_ADDR + ResourcePathURIs.LQIINFORMATION,
				lqiInformationClass.class);

		return router;
	}

	/**
	 * Gets the rest manager.
	 * 
	 * @return the rest manager.
	 */
	public RestManager getRestManager() {
		return restManager;
	}

	/**
	 * Sets the rest manager.
	 * 
	 * @param restManager
	 *            the rest manager to set.
	 */
	public void setRestManager(RestManager restManager) {
		this.restManager = restManager;
	}
}
