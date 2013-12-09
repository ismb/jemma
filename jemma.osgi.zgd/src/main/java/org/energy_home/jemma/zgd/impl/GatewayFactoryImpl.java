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
package org.energy_home.jemma.zgd.impl;

import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.energy_home.jemma.zgd.GatewayFactory;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.GatewayProperties;
import org.energy_home.jemma.zgd.jaxb.ObjectFactory;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

public class GatewayFactoryImpl extends GatewayFactory {
	
	static String jaxbPackage = "org.energy_home.jemma.zgd.jaxb";
	
	private GatewayProperties properties;
	private Client client;
	private Component component;
	private JAXBContext context;
//	private NamespacePrefixMapper mapper;
	private RestletEventListener restlet;
	
	public GatewayFactoryImpl() {}
	public GatewayFactoryImpl(GatewayProperties prop) throws Exception {
		init(prop);
	}
	
	GatewayProperties getProperties() {
		return properties;
	}
	
	Client getClient() {
		return client;
	}

	RestletEventListener getRestlet() {
		return restlet;
	}
	
	public JaxbConverter createConverter() throws JAXBException, IOException {
		String namespace = properties.getProperty(GatewayProperties.REST_NAMESPACE);
		JaxbConverter converter = new JaxbConverter(context, namespace);
		//converter.getMarshaller().setProperty(prefixMapperProperty, mapper);
		return converter;
	}
	
	protected void init(GatewayProperties prop) throws Exception {
		properties = prop;
		context = JAXBContext.newInstance(jaxbPackage, ObjectFactory.class.getClassLoader());
//		mapper = new NamespacePrefixMapper() {
//			String gatewayNS = properties.getProperty(GatewayProperties.GATEWAY_NAMESPACE);
//			String restNS = properties.getProperty(GatewayProperties.REST_NAMESPACE);
//			String gatewayPrefix = properties.getProperty(GatewayProperties.GATEWAY_NAMESPACE_PREFIX);
//			String restPrefix = properties.getProperty(GatewayProperties.REST_NAMESPACE_PREFIX);
//			
//			@Override
//			public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
//				if (namespaceUri.equals(gatewayNS)) return gatewayPrefix;
//				if (namespaceUri.equals(restNS)) return restPrefix;
//				return "";
//			}
//		};
//		
		client = new Client(Protocol.HTTP);
		client.setConnectTimeout(Integer.parseInt(properties.getProperty(GatewayProperties.CONNECTION_TIMEOUT)));
		
		component = new Component();
		Integer port=Integer.parseInt(properties.getProperty(GatewayProperties.LOCAL_PORT));
		component.getServers().add(Protocol.HTTP, port);
		restlet = new RestletEventListener(createConverter());
		component.getDefaultHost().attachDefault(restlet);
		
		if (properties.getProperty(GatewayProperties.ENABLE_RESTLET_CONSOLE).equalsIgnoreCase("false")) {
			Context.getCurrentLogger().setUseParentHandlers(false);
			component.getLogger().setUseParentHandlers(false);
		}
		
		component.start();
	}

	public void close() throws Exception {
		try {restlet.stop();} catch (Exception e) {}
		try {component.getDefaultHost().stop();} catch (Exception e) {}
		try {component.stop();} catch (Exception e) {}
		try {client.stop();} catch (Exception e) {}
	}
	
	public GatewayInterface createGatewayObject() throws Exception {
		return new GatewayObjectImpl(this);
	}
}
