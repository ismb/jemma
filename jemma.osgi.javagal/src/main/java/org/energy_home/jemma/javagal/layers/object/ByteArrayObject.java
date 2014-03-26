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

/**
 * Object carrying a {@code byte[]} of fixed length.
 * The aims of this class is to provide {@code byte[]} reuse. An instance of
 * {@link ByteArrayObject} contains a byte array where only the first
 * {@link #byteCount} bytes are to be considered valid.
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ByteArrayObject {
	/**
	 * The maximum array dimension.
	 */
	public final static short MAX_ARRAY_DIMENSION = 1024;
	private final static short START = 4;
	/**
	 * True if the valid values starts from zero, false otherwise.
	 */
	private boolean _startedFromZero = false;

	private final byte[] byteArray;
	private int byteCount;

	/**
	 * Creates a new empty {@code ByteArrayObject} instance.
	 */
	public ByteArrayObject() {
		_startedFromZero = false;
		byteArray = new byte[MAX_ARRAY_DIMENSION];
		byteCount = START;
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
		byteArray = buffer;
		byteCount = size;
	}

	/**
	 * Adds a byte after the last currently valid one. Consequently the
	 * {@link #byteCount} increments by one.
	 * 
	 * @param byteToAdd
	 *            the byte to add at the end.
	 */
	public void addByte(byte byteToAdd) {
		byteArray[byteCount++] = byteToAdd;
	}

	/**
	 * Adds the OP Group's byte to this byte array in its right position (the
	 * second byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the OP Group's byte to add.
	 */
	public void addOPGroup(byte byteToAdd) {
		byteArray[1] = byteToAdd;
	}

	/**
	 * Adds the OP Codes's byte to this byte array in its right position (the
	 * third byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the OP Code's byte to add.
	 */
	public void addOPCode(byte byteToAdd) {
		byteArray[2] = byteToAdd;
	}

	/**
	 * Adds lenght's byte to this byte array in its right position (the fourth
	 * byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the lenght's byte to add.
	 */
	public void addLength(byte byteToAdd) {
		byteArray[3] = byteToAdd;
	}

	/**
	 * Adds the start sequence's byte to this byte array in its right position
	 * (the first byte in sequence).
	 * 
	 * @param byteToAdd
	 *            the start sequence's byte to add.
	 */
	public void addStartSequance(byte byteToAdd) {
		byteArray[0] = byteToAdd;
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
			byteArray[byteCount++] = x;
	}

	/**
	 * Gets the entire raw backing {@code byte[]} byte array as is. Please note
	 * that all elements in the backing array are returned, even those after the
	 * {@code size} value that are to be considered invalid.
	 * 
	 * @return the byte array.
	 */
	public byte[] getByteArray() {
		return byteArray;
	}

	/**
	 * Gets a {@code byte[]} containing just the valid values carried by this
	 * byte array object.
	 * 
	 * @return the real byte array.
	 */
	public byte[] getRealByteArray() {
		byte[] vect = getByteArray();
		byte[] _data = new byte[(_startedFromZero) ? byteCount
				: (byteCount - START)];
		for (int i = 0; i < _data.length; i++)
			_data[i] = vect[START + i];
		return _data;
	}

	/**
	 * Gets part of the "real byte array" contained in this byte array object.
	 * The "real byte array" is the sub array containing just the valid values.
	 * 
	 * @param offset
	 *            the offset to start from.
	 * @param count
	 *            the size of returned sub byte array.
	 * @return the sub byte array to return.
	 */
	public byte[] getPartialRealByteArray(int offset, int count) {
		byte[] vect = getRealByteArray();
		byte[] tores = new byte[count];
		int x = 0;
		for (int i = offset; i < (offset + count); i++)
			tores[x++] = vect[i];
		return tores;
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
	public int getByteCount(boolean real) {
		if (!real) {
			if (_startedFromZero)
				return byteCount;
			else
				return byteCount - START;
		} else
			return byteCount;
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
		byte[] _vect = getByteArray();
		for (int i = 0; i < getByteCount(true); i++)
			_res.append(String.format("%02X", _vect[i]));
		return _res.toString();
	}
}
