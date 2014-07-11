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
package org.energy_home.jemma.utils.xml.jaxb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

public class JaxbConverter {
//	private static String prefixMapperProperty = "com.sun.xml.internal.bind.namespacePrefixMapper";
	private static String prefixMapperProperty = "com.sun.xml.bind.namespacePrefixMapper";

	public static final String UTF8_CHAR_ENCODING = "UTF8";
		
	private JAXBContext jaxbContext;
	private Marshaller xmlMarshaller;
	private Unmarshaller xmlUnmarshaller;
	private String namespace;

	private XmlConverter factory;

	private JAXBElement<?> getJaxbElement(Object object) {
		QName qname = new QName(namespace, object.getClass().getSimpleName());
		return new JAXBElement<Object>(qname, (Class<Object>) object.getClass(), object);
	}

	protected JaxbConverter(XmlConverter factory) throws JAXBException {
		this.factory = factory;
		namespace = factory.getDefaultNamespace();
		jaxbContext = factory.createJaxbContext(factory.getContextPath());
		xmlMarshaller = jaxbContext.createMarshaller();
		xmlUnmarshaller = jaxbContext.createUnmarshaller();
//		NamespacePrefixMapper nsPrefixMapper = factory.getNamespacePrefixMapper();
//		if (nsPrefixMapper != null)
		
	}
	
	public final ByteArrayOutputStream getByteArrayOutputStream(Object o) throws JAXBException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		synchronized (xmlMarshaller) {
			xmlMarshaller.marshal(getJaxbElement(o), bos);
		}
		return bos;
	}

	public final ByteArrayOutputStream getFormattedByteArrayOutputStream(Object o) throws JAXBException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
		synchronized (xmlMarshaller) {
			xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			try {
				xmlMarshaller.marshal(getJaxbElement(o), bos);
			} finally {
				xmlMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			}
		}
		return bos;
	}
	
	public final Object readObject(InputStream in) throws JAXBException, IOException {
		InputStreamReader utf8Reader = null;
		Object o = null;
		synchronized (xmlUnmarshaller) {
			utf8Reader = new InputStreamReader(in, UTF8_CHAR_ENCODING);
			o = xmlUnmarshaller.unmarshal(utf8Reader);
			if (o instanceof JAXBElement)
				return ((JAXBElement)o).getValue();
			else 
				return o;
		}
	}

	public final void writeObject(Object o, OutputStream out) throws JAXBException, UnsupportedEncodingException {
		OutputStreamWriter osw = null;
		synchronized (xmlMarshaller) {
			osw = new OutputStreamWriter(out, UTF8_CHAR_ENCODING);
			xmlMarshaller.marshal(getJaxbElement(o), osw);
		}
	}

}
