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
package org.energy_home.jemma.ah.internal.zigbee;

public class Hex {
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
	
	public static String byteToHex(byte[] data) {
		return byteToHex(data, 0);
	}
	
	public static char toHexChar(long i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('A' + (i - 10));
		}
	}
	
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
