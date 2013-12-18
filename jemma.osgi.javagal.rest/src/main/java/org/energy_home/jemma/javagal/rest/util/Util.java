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

package org.energy_home.jemma.javagal.rest.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.rest.PropertiesManager;

import com.sun.xml.internal.fastinfoset.sax.Properties;

/**
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Util {

	public final static long INTERNAL_TIMEOUT = 5000;
	private final static Long unsigned8MaxValue;
	private final static Long unsigned16MaxValue;
	private final static Long unsigned32MaxValue;
	private static final String EMPTY_STRING = "";
	public static final String UTF8_CHAR_ENCODING = "UTF-8";
	private static Random r;
	private static Log logger = LogFactory.getLog(Util.class);

	static {
		unsigned8MaxValue = Long.decode("0xff");
		unsigned16MaxValue = Long.decode("0xffff");
		unsigned32MaxValue = Long.decode("0xffffffff");
		r = new Random();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	synchronized public static <T> T unmarshal(String content, Class<T> clasz)
			throws Exception {
		JAXBContext jc = JAXBContext.newInstance(clasz);
		Unmarshaller u = jc.createUnmarshaller();
		Object o = null;
		byte[] _res = null;
		_res = content.getBytes("UTF-8");
		String __str = "";
		__str = new String(_res, "UTF-8");
		StringBuffer xmlStr = new StringBuffer(
				(!__str.startsWith("<") ? __str.substring(3) : __str));
		o = u.unmarshal(new StreamSource(new StringReader(xmlStr.toString())),
				clasz);
		return (T) ((JAXBElement) o).getValue();
	}

	@SuppressWarnings("unchecked")
	synchronized public static <T> String marshal(Object o) {

		StringWriter stringWriter = new StringWriter();
		try {
			JAXBContext jc = JAXBContext.newInstance(o.getClass());
			Marshaller m = jc.createMarshaller();
			QName _qname = new QName("http://www.zigbee.org/GWGRESTSchema", o
					.getClass().getSimpleName());
			JAXBElement<T> je = new JAXBElement<T>(_qname,
					(Class<T>) o.getClass(), ((T) o));
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
					new NamespacePrefixMapperImpl());
			m.setProperty("com.sun.xml.internal.bind.xmlDeclaration",
					Boolean.FALSE);
			m.marshal(je, stringWriter);
			String _tores = stringWriter.toString();
			logger.info("Marshall OutPut:\n\r" + _tores);
			return _tores;
		} catch (JAXBException e) {
			logger.error("\n\rException on marshal : " + e.getMessage());
			return EMPTY_STRING;
		}
	}

	synchronized public static int currentTimeMillis() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	synchronized public static boolean isUnsigned8(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned8MaxValue));
	}

	synchronized public static boolean isUnsigned16(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned16MaxValue));
	}

	synchronized public static boolean isUnsigned32(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned32MaxValue));
	}

	public static byte[] getRequestIdentifier() {

		byte[] rid = { (byte) r.nextInt(), (byte) r.nextInt(),
				(byte) r.nextInt(), (byte) r.nextInt() };
		return rid;
	}

	public static int getPortFromUriListener(String Uri) {

		try {
			if (Uri.toLowerCase().contains("http://"))
				Uri = Uri.substring(7);
			int start = -1;
			int end = -1;
			for (int i = 0; i < Uri.length(); i++) {
				char x = Uri.charAt(i);
				if (x == ':')
					start = i + 1;
				if (start > -1 && x == '/') {
					end = i;
					break;
				}
			}
			String _port = Uri.substring(start, end);
			return Integer.parseInt(_port);
		} catch (Exception e) {
			return -1;
		}
	}

}
