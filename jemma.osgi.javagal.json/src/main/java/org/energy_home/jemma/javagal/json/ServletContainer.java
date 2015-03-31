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
package org.energy_home.jemma.javagal.json;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.energy_home.jemma.javagal.json.constants.ResourcePathURIs;
import org.energy_home.jemma.javagal.json.constants.Resources;
import org.energy_home.jemma.javagal.json.servlet.allLqiInformationsServlet;
import org.energy_home.jemma.javagal.json.servlet.allPermitJoinServlet;
import org.energy_home.jemma.javagal.json.servlet.channelServlet;
import org.energy_home.jemma.javagal.json.servlet.frequencyAgilityServlet;
import org.energy_home.jemma.javagal.json.servlet.getInfoBaseAttributesServlet;
import org.energy_home.jemma.javagal.json.servlet.localServicesServlet;
import org.energy_home.jemma.javagal.json.servlet.nodeDescriptorAndServicesServlet;
import org.energy_home.jemma.javagal.json.servlet.nodeServicesServlet;
import org.energy_home.jemma.javagal.json.servlet.recoveryGalServlet;
import org.energy_home.jemma.javagal.json.servlet.resetServlet;
import org.energy_home.jemma.javagal.json.servlet.startUpServlet;
import org.energy_home.jemma.javagal.json.servlet.versionServlet;
import org.energy_home.jemma.javagal.json.servlet.wsnNodesServlet;
import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

public class ServletContainer implements HttpSessionListener{

	HttpService service;
	GatewayInterface gatewayInterface;
	String prefix = "/json";
	
	public ServletContainer(HttpService _service, GatewayInterface _gatewayInterface) {
		
		service = _service;
		gatewayInterface = _gatewayInterface;
		try {
			registerServlets();
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private void registerServlets() {
		try {
			
		
			
			/* json/version */
			service.registerServlet(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.VERSION, new versionServlet(gatewayInterface), null, null);
			/* json/net/default/channel */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.CHANNEL, new channelServlet(gatewayInterface), null, null);
			/*
			 * json/net/default/wsnnodes GET
			 * 
			 * Leave DELETE
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES, new wsnNodesServlet(gatewayInterface), null, null);
			/* json/net/default/allwsnnodes/lqi */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.ALLWSNNODES + ResourcePathURIs.LQIINFORMATION, new allLqiInformationsServlet(gatewayInterface), null, null);
			/*
			 * json/net/default/localnode/frequencyagility?timeout={0:x8}&
			 * scanChannel={1:x2}&scanDuration={2:x2}
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.URI_FREQUENCY_AGILITY, new frequencyAgilityServlet(gatewayInterface), null, null);
			/*
			 * json/net/default/wsnnodes/nodedescriptorservicelist?timeout={0:x8}
			 * &address={1:x2/8}
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES + ResourcePathURIs.NODEDESCRIPTORSERVICELIST, new nodeDescriptorAndServicesServlet(gatewayInterface), null, null);

			/*
			 * Defines Reset route
			 * "json/reset?timeout={0,08x}&startMode={1,01x}"
			 */
			service.registerServlet(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.RESET, new resetServlet(gatewayInterface), null, null);
			/*
			 * Defines PermitjoinAll route
			 * "json/net/default/allwsnnodes/permitjoin"
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.ALLPERMIT_JOIN, new allPermitJoinServlet(gatewayInterface), null, null);
			/*
			 * Defines StartupGatewayDevice route
			 * "json/startup?timeout={0,08x}&start=true" POST Defines
			 * readStartupAttributeSet route
			 * "json/startup?timeout={0,08x}&index={1,01x}" GET
			 */
			service.registerServlet(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.STARTUP, new startUpServlet(gatewayInterface), null, null);
			/*
			 * Defines GetServiceDescriptor route
			 * "json/net/default/wsnnodes/services/"
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES + ResourcePathURIs.SERVICES, new nodeServicesServlet(gatewayInterface), null, null);
			/*
			 * Defines LocalServices route "/net/default/localnode/services"
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.LOCALNODE_SERVICES, new localServicesServlet(gatewayInterface), null, null);

			/*
			 * Defines InfoBase route "/net/default/ib"
			 */
			service.registerServlet(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.INFOBASE, new getInfoBaseAttributesServlet(gatewayInterface), null, null);
			
			service.registerServlet(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.RECOVERY, new recoveryGalServlet(gatewayInterface), null, null);
			
			
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamespaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void unregister() {
		/* Remove route json/version */
		service.unregister(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.VERSION);
		/* Remove route json/net/default/channel */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.CHANNEL);
		/* Remove route json/net/default/wsnnodes */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES);
		/* Remove route json/net/default/allwsnnodes/lqi */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.ALLWSNNODES + ResourcePathURIs.LQIINFORMATION);
		/* Remove route json/net/default/localnode/frequencyagility */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.URI_FREQUENCY_AGILITY);
		/*
		 * Remove route
		 * json/net/default/wsnnodes/nodedescriptorservicelist?timeout={0:x8}&
		 * address={1:x2/8}
		 */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES + ResourcePathURIs.NODEDESCRIPTORSERVICELIST);

		/*
		 * Remove route "json/reset"
		 */
		service.unregister(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.RESET);

		/*
		 * Remove route "/net/default/allwsnnodes/permitjoin"
		 */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.ALLPERMIT_JOIN);

		/*
		 * Remove route "json/startup"
		 */
		service.unregister(prefix + Resources.GW_ROOT_URI + ResourcePathURIs.STARTUP);
		/*
		 * Remove route "json/net/default/wsnnodes/services/"
		 */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.WSNNODES + ResourcePathURIs.SERVICES);
		/*
		 * Defines LocalServices route "/net/default/localnode/services"
		 */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.LOCALNODE_SERVICES);

		/*
		 * Defines InfoBaseAttribute route "/net/default/ib"
		 */
		service.unregister(prefix + Resources.NWT_ROOT_URI + ResourcePathURIs.INFOBASE);
		
		
		
	}

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		
	}
}
