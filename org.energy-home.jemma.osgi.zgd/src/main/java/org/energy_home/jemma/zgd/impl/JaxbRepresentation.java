/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
import java.io.OutputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.SaxRepresentation;

class JaxbRepresentation<T> extends SaxRepresentation {
	
	private static JaxbConverter jaxbConverter;
	
	static JaxbConverter getJaxbContext() {
		return jaxbConverter;
	}

	static void setJaxbContext(JaxbConverter c) {
		jaxbConverter = c;
	}

	private static String defaultNamespace;
	
	static String getDefaultNamespace() {
		return defaultNamespace;
	}

	static void setDefaultNamespace(String defNamespace) {
		defaultNamespace = defNamespace;
	}

	private T object;
	private String name;

	String getOverrideName() {
		return name;
	}

	void setOverrideName(String n) {
		name = n;
	}

	// constructor when marshaling
	JaxbRepresentation(T o) {
		super(MediaType.APPLICATION_XML);
		object = o;
	}

	// constructor when unmarshaling
	JaxbRepresentation(Representation r) {
		super(r);
	}

	@SuppressWarnings("unchecked")
	T getObject() {
		T result = null;
		try {
			synchronized (jaxbConverter.getUnmarshaller()) {
				JAXBElement<T> element = (JAXBElement<T>)jaxbConverter.getUnmarshaller().unmarshal(getSaxSource());
				result = element.getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public void write(OutputStream outputStream) throws IOException {
		try {
			if (name == null) name = object.getClass().getSimpleName();
			QName qname = new QName(getDefaultNamespace(), name);
			JAXBElement<T> element = new JAXBElement<T>(qname, (Class<T>)object.getClass(), object);
			synchronized (jaxbConverter.getMarshaller()) {
				jaxbConverter.getMarshaller().marshal(element, outputStream);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
