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
package org.energy_home.jemma.javagal.layers.object;

import java.nio.ByteBuffer;

/**
 * Object carrying a {@code byte[]} of fixed length (
 * {@link #MAX_ARRAY_DIMENSION}).
 * <p>
 * The aims of this class is to provide {@code byte[]} reuse. An instance of
 * {@link ByteArrayObject} contains a byte array where only the first
 * {@link #byteCount} bytes are to be considered valid.
 * <p>
 * Prior to recycle an object, the {@link #reset()} method is to be called. This
 * resets the {@link #byteCount} to zero and so the object can be safely reused. 
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ByteArrayObject {
	public final static short MAX_ARRAY_DIMENSION = 8192;
	private final static short START = 4;
	private boolean _startedFromZero = false;

	private final byte[] byteArray;
	private int byteCount;

	public ByteArrayObject() {
		_startedFromZero = false;
		byteArray = new byte[MAX_ARRAY_DIMENSION];
		byteCount = START;
	}

	public ByteArrayObject(byte[] buffer, int size) {
		_startedFromZero = true;
		byteArray = buffer;
		byteCount = size;
	}

	public void addByte(byte byteToAdd) {
		byteArray[byteCount++] = byteToAdd;
	}

	public void addOPGroup(byte byteToAdd) {
		byteArray[1] = byteToAdd;
	}

	public void addOPCode(byte byteToAdd) {
		byteArray[2] = byteToAdd;
	}

	public void addLength(byte byteToAdd) {
		byteArray[3] = byteToAdd;
	}

	public void addStartSequance(byte byteToAdd) {
		byteArray[0] = byteToAdd;
	}

	public void addBytesShort(short valueToAdd, int length) {
		ByteBuffer buf = ByteBuffer.allocate(length).putShort(valueToAdd);
		for (byte x : buf.array())
			byteArray[byteCount++] = x;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public byte[] getRealByteArray() {
		byte[] vect = getByteArray();
		byte[] _data = new byte[(_startedFromZero) ? byteCount
				: (byteCount - START)];
		for (int i = 0; i < _data.length; i++)
			_data[i] = vect[START + i];
		return _data;
	}

	public byte[] getPartialRealByteArray(int offset, int count) {
		byte[] vect = getRealByteArray();
		byte[] tores = new byte[count];
		int x = 0;
		for (int i = offset; i < (offset + count); i++)
			tores[x++] = vect[i];
		return tores;
	}

	public int getByteCount(boolean real) {
		if (!real) {
			if (_startedFromZero)
				return byteCount;
			else
				return byteCount - START;
		} else
			return byteCount;
	}

	public String ToHexString() {
		StringBuffer _res = new StringBuffer();
		byte[] _vect = getByteArray();
		for (int i = 0; i < getByteCount(true); i++)
			_res.append(String.format("%02X", _vect[i]));
		return _res.toString();
	}
}
