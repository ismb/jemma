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
package org.energy_home.jemma.m2m.connection;


import java.io.InputStream;
import java.io.OutputStream;

import org.energy_home.jemma.m2m.M2MXmlConverter;
import org.energy_home.jemma.utils.xml.jaxb.XmlConverter;

public class M2MConnectionXmlObject implements Cloneable {

	private static XmlConverter converterFactory;
	
	public M2MConnectionXmlObject()
	{
		if(converterFactory==null)
		{
			converterFactory= M2MXmlConverter.getConnectionConverter();
		}
	}

	
	public static byte[] getByteArray(Object o) {
		return converterFactory.getByteArray(o);
	}

	public static String getString(Object o) {
		return converterFactory.getString(o);
	}

	public static String getPrintableString(Object o) {
		return converterFactory.getPrintableString(o);
	}

	public static String getFormattedString(Object o) {
		return converterFactory.getFormattedString(o);
	}

	public static M2MConnectionXmlObject getObject(String xmlString) {
		return (M2MConnectionXmlObject) converterFactory.getObject(xmlString);
	}

	public static M2MConnectionXmlObject readObject(InputStream in) {
		return (M2MConnectionXmlObject) converterFactory.readObject(in);
	}

	public static void writeObject(Object object, OutputStream out) {
		converterFactory.writeObject(object, out);
	}

	public static M2MConnectionXmlObject loadFromFile(String filePath) {
		return (M2MConnectionXmlObject) converterFactory.loadFromFile(filePath);
	}

	public static boolean saveToFile(String filePath, Object object) {
		return converterFactory.saveToFile(filePath, object);
	}

	public Object clone() throws CloneNotSupportedException {
		return converterFactory.getObject(this.toXmlString());
	}

	public String toXmlString() {
		return converterFactory.getString(this);
	}

	public String toXmlPrintableString() {
		return converterFactory.getPrintableString(this);
	}

	public String toXmlFormattedString() {
		return converterFactory.getFormattedString(this);
	}

}
