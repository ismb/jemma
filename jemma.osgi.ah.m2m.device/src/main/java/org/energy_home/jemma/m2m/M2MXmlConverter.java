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
package org.energy_home.jemma.m2m;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.energy_home.jemma.utils.xml.jaxb.XmlConverter;

public class M2MXmlConverter extends XmlConverter {
	private static M2MXmlConverter connectionInstance;
	private static M2MXmlConverter coreInstance;

	public static final String JAXB_CONNECTION_NAMESPACE = "http://schemas.telecomitalia.it/m2m/connection";
	public static final String JAXB_CONNECTION_CONTEXT_PATH = "org.energy_home.jemma.m2m.connection";
	public static final String JAXB_CORE_NAMESPACE = "http://schemas.telecomitalia.it/m2m";
	public static String JAXB_CORE_CONTEXT_PATH = "org.energy_home.jemma.m2m";

	public static Map<String, String> JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP;
	
	static {
		JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP = new HashMap<String, String>();
		JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP.put(XmlConverter.XML_SCHEMA_INSTANCE_NAMESPACE, "xsi");
		JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP.put(XmlConverter.XML_SCHEMA_NAMESPACE, "xs");

		try {
			Class clazz = Class.forName("org.energy_home.jemma.m2m.ah.ObjectFactory");
			JAXB_CORE_CONTEXT_PATH += ":org.energy_home.jemma.m2m.ah";
			JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP.put("http://schemas.telecomitalia.it/ah", "ah");
		} catch (Exception e) {}
	}	
	
	public synchronized static M2MXmlConverter getConnectionConverter() {
		if (connectionInstance == null)
			connectionInstance = new M2MXmlConverter(JAXB_CONNECTION_CONTEXT_PATH,
					JAXB_CONNECTION_NAMESPACE, null, 1);
		return connectionInstance;
	}

	public synchronized static M2MXmlConverter getCoreConverter() {
		if (coreInstance == null) {
			coreInstance = new M2MXmlConverter(JAXB_CORE_CONTEXT_PATH, JAXB_CORE_NAMESPACE, 
					JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP,1);
		}
		return coreInstance;
	}

	protected M2MXmlConverter(String contextPath, String defaultNamespace, Map<String, String> nameSpacePreferredPrefixMap,
			int poolMaxSize) {
		super(contextPath, defaultNamespace, nameSpacePreferredPrefixMap, poolMaxSize);
	}

	protected JAXBContext createJaxbContext(String contextPath) throws JAXBException {
		ClassLoader cl = ObjectFactory.class.getClassLoader();
		return JAXBContext.newInstance(contextPath, cl);
	}

}
