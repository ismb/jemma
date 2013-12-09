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
/*
 * The JAXBContext class is thread safe, but the Marshaller, Unmarshaller, and Validator classes
 * are not thread safe. Creating Unmarshaller could be relatively an expensive operation.
 * In that case, consider pooling Unmarshaller objects. Different threads may reuse one
 * Unmarshaller instance, as long as you don't use one instance from two threads at the same time.
*/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.namespace.QName;

import org.energy_home.jemma.zgd.Trace;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class JaxbConverter {
//	static String prefixMapperProperty = "com.sun.xml.internal.bind.namespacePrefixMapper";
	static String prefixMapperProperty = "com.sun.xml.bind.namespacePrefixMapper";

	private JAXBContext context;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;
	private String namespace = "";

	JAXBContext getContext() {
		return context;
	}
	Marshaller getMarshaller() {
		return marshaller;
	}
	Unmarshaller getUnmarshaller() {
		return unmarshaller;
	}
	
//	JaxbConverter(JAXBContext c, String ns) throws JAXBException {
//		this(c, ns, null);
//	}
	
	public JaxbConverter(JAXBContext c, String ns, NamespacePrefixMapper nm) throws JAXBException {
		context = c;
		namespace = ns;
		marshaller = c.createMarshaller();
		unmarshaller = c.createUnmarshaller();
		unmarshaller.setEventHandler(new DefaultValidationEventHandler());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//		if (nm != null) marshaller.setProperty(prefixMapperProperty, nm);
	}
	
	public JaxbConverter(JAXBContext c, String ns) throws JAXBException {
		context = c;
		namespace = ns;
		marshaller = c.createMarshaller();
		unmarshaller = c.createUnmarshaller();
		unmarshaller.setEventHandler(new DefaultValidationEventHandler());
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
//		if (nm != null) marshaller.setProperty(prefixMapperProperty, nm);
	}
	
	public Representation toRepresentation(Object object) throws IOException, JAXBException {
		QName qname = new QName(namespace, object.getClass().getSimpleName());
		@SuppressWarnings("unchecked")
		JAXBElement<?> element = new JAXBElement<Object>(qname, (Class<Object>)object.getClass(), object);
		return toRepresentation(element);
	}
	
	Representation toRepresentation(final JAXBElement<?> element) throws IOException, JAXBException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		synchronized (marshaller) {
			marshaller.marshal(element, baos);
		}
		final IOException[] ioex = new IOException[1];
		Representation rep = new OutputRepresentation(MediaType.APPLICATION_XML, baos.size()) {
			public void write(OutputStream os) {
				try {
					baos.writeTo(os);
				} catch (IOException e) {
					ioex[0] = e;
				}
			}
		};
		if (ioex[0] != null) throw ioex[0];
		return doLog(rep);
	}
	
	
	Info getInfo(Response response) throws IOException, JAXBException {
		org.restlet.data.Status httpStatus = response.getStatus();
		if (httpStatus.isError())
			throw new IOException(httpStatus.getCode() + " - " + httpStatus.getDescription());
		return getInfo(response.getEntity());
	}
	
	Info getInfo(Representation rep) throws IOException, JAXBException {
		rep = doLog(rep);
		synchronized (unmarshaller) {
			JAXBElement<?> element = (JAXBElement<?>)unmarshaller.unmarshal(rep.getStream());
			return (Info)element.getValue();
		}
	}
	
	private Representation doLog(Representation rep) throws IOException {
		if (Trace.isTrace()) {
			String xml = rep.getText();
			Trace.println(xml);
			rep = new StringRepresentation(xml, MediaType.TEXT_XML, null, CharacterSet.UTF_8);
		}
		return rep;
	}
}
