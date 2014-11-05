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
package org.energy_home.jemma.javagal.layers.object;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * Object carrying a {@code byte[]} of fixed length. The aims of this class is
 * to provide {@code byte[]} reuse. An instance of {@link ByteArrayObject}
 * contains a byte array where only the first {@link #count} bytes are to be
 * considered valid.
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 */
public class ByteArrayObject {
	/**
	 * The maximum array dimension.
	 */
	public final static short MAX_ARRAY_DIMENSION = 2024;
	private final static short START = 4;

	/**
	 * True if the valid values starts from zero, false otherwise.
	 */
	private boolean _startedFromZero = false;

	private final byte[] array;
	private int count;

	public ByteArrayObject(boolean startFromZero) {
		if (startFromZero) {
			_startedFromZero = true;
			array = new byte[MAX_ARRAY_DIMENSION];
			count = 0;
		} else {
			_startedFromZero = false;
			array = new byte[MAX_ARRAY_DIMENSION];
			count = START;

		}
	}

	/**
	 * Creates a new {@code ByteArrayObject} instance filled with a given
	 * {@code byte[]} buffer. Only the first {@code size} values present in the
	 * created byte array instance are to be considered valid.
	 * 
	 * @param buffer
	 *            the buffer of initial elements to put in the byte array
	 *            object.
	 * @param size
	 *            the size of the valid values on the byte array.
	 */
	public ByteArrayObject(byte[] buffer, int size) {
		_startedFromZero = true;
		array = new byte[size];
		if (buffer != null) {
			System.arraycopy(buffer, 0, array, 0, size);
		}
		count = size;

	}

	/**
	 * Adds a byte after the last currently valid one. Consequently the
	 * {@link #count} increments by one.
	 * 
	 * @param byteToAdd
	 *            the byte to add at the end.
	 */
	public void addByte(byte byteToAdd) {
		array[count++] = byteToAdd;
	}

	/**
	 * Adds the start sequence's byte to this byte array in its right position
	 * (the first byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the start sequence's byte to add.
	 */
	public void addStartSequance(byte byteToAdd) {
		array[0] = byteToAdd;
	}

	/**
	 * Adds the OP Group's byte to this byte array in its right position (the
	 * second byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the OP Group's byte to add.
	 */
	public void addOPGroup(byte byteToAdd) {
		array[1] = byteToAdd;
	}

	/**
	 * Adds the OP Codes's byte to this byte array in its right position (the
	 * third byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the OP Code's byte to add.
	 */
	public void addOPCode(byte byteToAdd) {
		array[2] = byteToAdd;
	}

	/**
	 * Adds lenght's byte to this byte array in its right position (the fourth
	 * byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the lenght's byte to add.
	 */
	public void addLength(byte byteToAdd) {
		array[3] = byteToAdd;
	}

	/**
	 * Adds a short converted as byte(s). Writes one or two bytes containing the
	 * given short value, in Big Endian byte order (from most significant to
	 * least significant). One or two bytes is indicated by the lenght argument.
	 * 
	 * @param valueToAdd
	 *            the short to add as its byte(s) representation.
	 * @param length
	 *            the desired lenght.
	 */
	public void addBytesShort(short valueToAdd, int length) {
		ByteBuffer buf = ByteBuffer.allocate(length).putShort(valueToAdd);
		for (byte x : buf.array())
			array[count++] = x;
	}

	/**
	 * Gets the entire raw backing {@code byte[]} byte array as is. Please note
	 * that all elements in the backing array are returned, even those after the
	 * {@code size} value that are to be considered invalid.
	 * 
	 * @return the byte array.
	 */
	public byte[] getArrayRealSize() {
		return Arrays.copyOfRange(getArray(), 0, getCount(true));

	}

	/**
	 * Gets the entire raw backing {@code byte[]} byte array as is. Please note
	 * that all elements in the backing array are returned, even those after the
	 * {@code size} value that are to be considered invalid.
	 * 
	 * @return the byte array.
	 */
	public byte[] getArray() {
		return array;
	}

	/**
	 * Gets the number of valid byte(s) carried by this byte array.
	 * 
	 * @param real
	 *            true if carried byte array is to be considered "real", false
	 *            otherwise.
	 * 
	 * @return the byte count.
	 */
	public int getCount(boolean real) {
		if (!real) {
			if (_startedFromZero)
				return count;
			else
				return count - START;
		} else
			return count;
	}

	public static char toHexChar(long i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('A' + (i - 10));
		}
	}

	/**
	 * Gives the entire byte array converted as a String. Every element is
	 * converted to its hexadecimal digit representation.
	 * 
	 * @return the hexadecimal string representation of the entire carried byte
	 *         array.
	 */
	public String ToHexString() {
		StringBuffer _res = new StringBuffer();
		byte[] _vect = getArray();
		for (int i = 0; i < getCount(true); i++) {
			_res.append(toHexChar((_vect[i] >>> 4) & 0x0F));
			_res.append(toHexChar(_vect[i] & 0x0F));
		}
		return _res.toString();
	}
}
