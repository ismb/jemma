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

package org.energy_home.jemma.javagal.json.util;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	/**
	 * Conventional internal timeout value.
	 */
	public final static long INTERNAL_TIMEOUT = 5000;
	private final static Long unsigned8MaxValue;
	private final static Long unsigned16MaxValue;
	private final static Long unsigned32MaxValue;
	private static final String EMPTY_STRING = "";
	/**
	 * String representation for Unicode Transformation Format, 8 bit.
	 */
	public static final String UTF8_CHAR_ENCODING = "UTF-8";
	private static Random r;

	private static final Logger LOG = LoggerFactory.getLogger( Util.class );

	static {
		unsigned8MaxValue = Long.decode("0xff");
		unsigned16MaxValue = Long.decode("0xffff");
		unsigned32MaxValue = Long.decode("0xffffffff");
		r = new Random();
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
	 * 16 bits value or not (2 bytes).
	 * 
	 * @param candidate
	 *            the candidate Long.
	 * @return true if the value is actually an unsigned 16 bits, false
	 *         otherwise.
	 */
	synchronized public static boolean isUnsigned16(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned16MaxValue));
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

}
