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
package org.energy_home.jemma.javagal.layers.data.implementations.Utils;

/**
 * Utility class providing help methods on hexadecimal conversions.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class Hex {

	/**
	 * Converts a portion of a {@code byte[]} to its String representation. The
	 * resulting String will contain the hexadecimal representation of the bytes
	 * from index i to the end of the array.
	 * 
	 * @param data
	 *            the {@code byte[]} to convert.
	 * @param i
	 *            the starting index.
	 * @return the resulting String representation.
	 */
	public static String byteToHex(byte[] data, int i) {
		if (data == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		for (; i < data.length; i++) {
			buf.append(toHexChar((data[i] >>> 4) & 0x0F));
			buf.append(toHexChar(data[i] & 0x0F));
		}
		return buf.toString();
	}

	/**
	 * Converts a portion of a {@code byte[]} to its String representation. The
	 * resulting String will contain the hexadecimal representation of the next
	 * {@code len} bytes starting from index i.
	 * <p>
	 * Please note that no control about array bounds is made so {@code i + len}
	 * must be less than the array length. Is up to the developer to comply with
	 * this rule.
	 * 
	 * @param data
	 *            the {@code byte[]} to convert.
	 * @param i
	 *            the starting index.
	 * @param len
	 *            the number of bytes to convert.
	 * @return the resulting String representation.
	 */
	public static String byteToHex(byte[] data, int i, int len) {
		if (data == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		for (; i < len; i++) {
			buf.append(toHexChar((data[i] >>> 4) & 0x0F));
			buf.append(toHexChar(data[i] & 0x0F));
		}
		return buf.toString();
	}

	/**
	 * Converts a {@code byte[]} to its String representation. The resulting
	 * String will contain the hexadecimal representation of all bytes
	 * contained.
	 * 
	 * @param data
	 *            the {@code byte[]} to convert.
	 * @return the resulting String representation.
	 */
	public static String byteToHex(byte[] data) {
		return byteToHex(data, 0);
	}

	/**
	 * Converts an hexadecimal digit to a char. In particular, admitted values
	 * for the long parameter goes from 0 to 15, while returned chars varies
	 * from '0' to 'F'.
	 * 
	 * @param i
	 *            the long number to convert
	 * @return the corresponding char.
	 */
	public static char toHexChar(long i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('A' + (i - 10));
		}
	}

	/**
	 * Converts the first digits of a long number to its String hexadecimal
	 * representation.
	 * 
	 * @param value
	 *            the long number to convert.
	 * @param size
	 *            the number of digits to convert from the long number provided.
	 * @return the desired String representation.
	 */
	public static String toHexString(long value, int size) {
		StringBuffer buf = new StringBuffer();
		long data = (long) (value);
		for (int i = 0; i < size; i++) {
			buf.insert(0, toHexChar(data & 0x0F));
			buf.insert(0, toHexChar((data >>> 4) & 0x0F));
			data >>>= 8;
		}

		return buf.toString();
	}
}
