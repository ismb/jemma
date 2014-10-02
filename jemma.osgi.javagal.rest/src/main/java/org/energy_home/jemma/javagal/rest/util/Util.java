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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Random;

/**
 * Utility class.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Util {

	/**
	 * Conventional internal timeout value.
	 */
	public final static long INTERNAL_TIMEOUT = 5000;
	private final static Long unsigned8MaxValue;
    private final static Long unsigned32MaxValue;
	private static final String EMPTY_STRING = "";
    private static Random r;
	private static final Logger LOG = LoggerFactory.getLogger( Util.class );

	static {
		unsigned8MaxValue = Long.decode("0xff");
        unsigned32MaxValue = Long.decode("0xffffffff");
		r = new Random();
	}

	/**
	 * Unmarshal class.
	 * 
	 * @param content
	 *            the string containing the text to unmarshal.
	 * @param clasz
	 *            the class resulting from the unmarshal process.
	 * @return the unmarshalled object.
	 * @throws Exception
	 *             if an error occurs.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	synchronized public static <T> T unmarshal(String content, Class<T> clasz)
			throws Exception {
		JAXBContext jc = JAXBContext.newInstance(clasz);
		Unmarshaller u = jc.createUnmarshaller();
		Object o;
		byte[] _res;
		_res = content.getBytes("UTF-8");
		String __str;
		__str = new String(_res, "UTF-8");
        o = u.unmarshal(new StreamSource(new StringReader((!__str.startsWith("<") ? __str.substring(3) : __str))),
				clasz);
		return (T) ((JAXBElement) o).getValue();
	}

	/**
	 * Marshal class.
	 * 
	 * @param o
	 *            the object to marshall.
	 * 
	 * @return the marshalled representation.
	 */
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
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
					new NamespacePrefixMapperImpl());
			m.setProperty("com.sun.xml.internal.bind.xmlDeclaration",
					Boolean.FALSE);
			m.marshal(je, stringWriter);

            return stringWriter.toString();
		} catch (JAXBException e) {
			LOG.error("Exception on marshal : ", e);
			return EMPTY_STRING;
		}
	}

	/**
	 * Gets a reference to current time as int.
	 * 
	 * @return a reference to current time as int.
	 */
	synchronized public static int currentTimeMillis() {
		return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
	}

	/**
	 * Tells if the value contained in a candidate Long is actually an unsigned
	 * 8 bits value or not (1 byte).
	 * 
	 * @param candidate
	 *            the candidate Long.
	 * @return true if the value is actually an unsigned 8 bits, false
	 *         otherwise.
	 */
	synchronized public static boolean isUnsigned8(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned8MaxValue));
	}

    /**
	 * Tells if the value contained in a candidate Long is actually an unsigned
	 * 32 bits value or not (4 bytes).
	 * 
	 * @param candidate
	 *            the candidate Long.
	 * @return true if the value is actually an unsigned 32 bits, false
	 *         otherwise.
	 */
	synchronized public static boolean isUnsigned32(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned32MaxValue));
	}

	/**
	 * Gets the request identifier.
	 * 
	 * @return the request identifier.
	 * 
	 */
	public static byte[] getRequestIdentifier() {

        return new byte[]{ (byte) r.nextInt(), (byte) r.nextInt(),
                (byte) r.nextInt(), (byte) r.nextInt() };
	}

	/**
	 * Extract the port number from a given uri.
	 * 
	 * @param Uri
	 *            the uri.
	 * @return the extracted port number.
	 */
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
