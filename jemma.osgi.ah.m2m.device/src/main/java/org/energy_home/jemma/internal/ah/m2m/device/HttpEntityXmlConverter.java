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
package org.energy_home.jemma.internal.ah.m2m.device;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.energy_home.jemma.m2m.M2MXmlConverter;
import org.energy_home.jemma.m2m.ObjectFactory;
import org.energy_home.jemma.utils.xml.jaxb.XmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpEntityXmlConverter extends XmlConverter {
	private static final Logger LOG = LoggerFactory.getLogger( HttpEntityXmlConverter.class );

	private static HttpEntityXmlConverter connectionInstance;
	private static HttpEntityXmlConverter coreInstance;

	public static final String HTTP_ENTITY_CONTENT_TYPE = "application/xml";

	public synchronized static HttpEntityXmlConverter getConnectionConverter() {
		if (connectionInstance == null)
			connectionInstance = new HttpEntityXmlConverter(M2MXmlConverter.JAXB_CONNECTION_CONTEXT_PATH,
					M2MXmlConverter.JAXB_CONNECTION_NAMESPACE, null, 1);
		return connectionInstance;
	}

	public synchronized static HttpEntityXmlConverter getCoreConverter() {
		if (coreInstance == null) {
			coreInstance = new HttpEntityXmlConverter(M2MXmlConverter.JAXB_CORE_CONTEXT_PATH, M2MXmlConverter.JAXB_CORE_NAMESPACE,  
					M2MXmlConverter.JAXB_CORE_NAMESPACES_PREFERRED_PREFIX_MAP, 1);
		}
		return coreInstance;
	}

	protected HttpEntityXmlConverter(String contextPath, String defaultNamespace, Map<String, String> nameSpacePreferredPrefixMap,
			int poolMaxSize) {
		super(contextPath, defaultNamespace, nameSpacePreferredPrefixMap, poolMaxSize);
	}

	protected JAXBContext createJaxbContext(String contextPath) throws JAXBException {
		ClassLoader cl = ObjectFactory.class.getClassLoader();
		return JAXBContext.newInstance(contextPath, cl);
	}

	public Object getObject(HttpEntity entity) throws JAXBException, IllegalStateException, IOException {
		Object o = readObject(entity.getContent());
		//FIXME LOG.isDebugEnabled ... shall we avoid this ?
		if (LOG.isDebugEnabled())
			LOG.debug("toObject:\n" + getPrintableString(o));
		return o;
	}

	public HttpEntity getEntity(Object object) throws JAXBException, UnsupportedEncodingException {
		ByteArrayOutputStream b = getConverter().getByteArrayOutputStream(object);
		if (LOG.isDebugEnabled())
			LOG.debug("toEntity:\n" + getPrintableString(b.toString()));
		ByteArrayEntity entity = new ByteArrayEntity(b.toByteArray());
		entity.setContentType(HTTP_ENTITY_CONTENT_TYPE);

		return entity;
	}

}
