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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class with a number of data's manipulation methods.
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class DataManipulation {

	/**
	 * Start sequence in received frames.
	 */
	public final static int SEQUENCE_START = 0x02;
	/**
	 * Index of the first payload's byte in frames.
	 */
	public final static int START_PAYLOAD_INDEX = 4;

	// Defining array of bytes to pass later to the key

	private static final Logger LOG = LoggerFactory.getLogger(DataManipulation.class);

	/**
	 * Converts a string to an array of bytes.
	 * 
	 * @param s
	 *            the string to convert.
	 * @return the converted array of bytes.
	 */
	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * Creates an int starting from two given shorts. An int is composed of four
	 * bytes. Numbering the four bytes from 1 (the most important) to 4 (the
	 * least important), the converted int will be formed placing the two given
	 * bytes in the two less important places of the created int.
	 * <p>
	 * More formally the low byte will be placed in position 4, the high byte
	 * will be placed in position 3, while positions 1 and 2 will be set at
	 * zero.
	 * 
	 * @param hb
	 *            the high byte.
	 * @param lb
	 *            the low byte.
	 * @return the result int.
	 */
	public static int toIntFromShort(byte hb, byte lb) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] { 0x00, 0x00, hb, lb });
		return bb.getInt();
	}

	/**
	 * Creates a long starting from eight given bytes. A long is composed of
	 * eight bytes. Numbering them from 1 (the most important) to 8 (the least
	 * important), the given bytes will be placed in the place indicated by
	 * their respective names.
	 * 
	 * @param _1
	 *            byte placed in position 1
	 * @param _2
	 *            byte placed in position 2
	 * @param _3
	 *            byte placed in position 3
	 * @param _4
	 *            byte placed in position 4
	 * @param _5
	 *            byte placed in position 5
	 * @param _6
	 *            byte placed in position 6
	 * @param _7
	 *            byte placed in position 7
	 * @param _8
	 *            byte placed in position 8
	 * @return the resulting long
	 */
	public static long toLong(byte _1, byte _2, byte _3, byte _4, byte _5, byte _6, byte _7, byte _8) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] { _1, _2, _3, _4, _5, _6, _7, _8 });
		return bb.getLong();
	}

	/**
	 * Starts from a {@code byte[]} and returns a sub array, converted as
	 * {@code byte[]}.
	 * 
	 * @param array
	 *            the original array
	 * @param start
	 *            the start index, included
	 * @param stop
	 *            the stop index, included
	 * @return the converted sub array
	 */
	public static byte[] subByteArray(byte[] array, int start, int stop) {
		byte[] toReturn = new byte[(stop-start)+1];
		System.arraycopy(array, start, toReturn, 0, ((stop-start)+1));
		return toReturn;
	}

	/**
	 * Converts a {@code long} to a {@code byte[]}. A long is composed of eight
	 * bytes. Writes eight bytes containing the given long value, in the current
	 * byte order. Numbering them from 0 (the most important) to 7 (the least
	 * important), the resulting array will have all them placed in the same
	 * position.
	 * 
	 * @return the converted list.
	 * @param x
	 *            the long to convert
	 * @return the resulting array.
	 */
	private static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putLong(x);
		return buffer.array();
	}

	/**
	 * Converts an {@code int} to a {@code byte[]}. An int is composed of four
	 * bytes. Writes four bytes containing the given int value, in the current
	 * byte order. Numbering them from 0 (the most important) to 5 (the least
	 * important), the resulting array will have all them placed in the same
	 * position.
	 * 
	 * @param x
	 *            the int to convert.
	 * @return the resulting array.
	 */
	private static byte[] intToBytes(int x) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(x);
		return buffer.array();
	}

	/**
	 * Converts a {@code BigInteger} to a {@code byte[]}. The resulting array
	 * will have all bytes contained in the BigInteger placed in the same
	 * position (from byte 0 to byte n). The pad parameter indicate the minimum
	 * size of the resulting array. In case its size would be less then the one
	 * indicated in the pad parameter, a number of leading zeros will be
	 * inserted.
	 * 
	 * @param toConvert
	 *            the BigInteger to convert.
	 * @param pad
	 *            the minimum size of the returned array.
	 * @return the resulting array.
	 */
	public static byte[] toByteVect(BigInteger toConvert, int pad) {
		byte[] toReturn = new byte[pad];
		byte[] byteArray = toConvert.toByteArray();
		int i;
		for (i = 0; i <= (pad - byteArray.length - 1); i++)
			toReturn[i] = 0;
		int x = 0;
		for (; i <= (toReturn.length - 1); i++)
			toReturn[i] = byteArray[x++];
		return toReturn;
	}

	/**
	 * Converts a {@code Long} to a {@code byte[]}. A Long is composed of eight
	 * bytes. In case pad is set to 8, writes eight bytes containing the given
	 * Long value, in the current byte order. The pad parameter indicate the
	 * minimum size of the resulting array. In case its size would be less then
	 * the one indicated in the pad parameter, a number of leading zeros will be
	 * inserted.
	 * 
	 * @param toConvert
	 *            the Long to convert.
	 * @param pad
	 *            the minimum size of the returned array.
	 * @return the resulting array.
	 */
	public static byte[] toByteVect(Long toConvert, int pad) {
		byte[] toReturn = new byte[pad];
		byte[] byteArray = longToBytes(toConvert);
		int i;
		for (i = 0; i <= (pad - byteArray.length - 1); i++)
			toReturn[i] = 0;
		int x = 0;
		for (; i <= (toReturn.length - 1); i++)
			toReturn[i] = byteArray[x++];
		return toReturn;

	}

	/**
	 * Converts an {@code int} to a {@code byte[]}. An int is composed of four
	 * bytes. In case pad is set to 4, writes eight bytes containing the given
	 * int value, in the current byte order. The pad parameter indicate the
	 * minimum size of the resulting array. In case its size would be less then
	 * the one indicated in the pad parameter, a number of leading zeros will be
	 * inserted.
	 * 
	 * @param toConvert
	 *            the int to convert.
	 * @param pad
	 *            the minimum size of the returned array.
	 * @return the resulting array.
	 */
	public static byte[] toByteVect(int toConvert, int pad) {
		byte[] toReturn = new byte[pad];
		byte[] byteArray = intToBytes(toConvert);
		int i;
		for (i = 0; i <= (pad - byteArray.length - 1); i++)
			toReturn[i] = 0;
		int x = 0;
		for (; i <= (toReturn.length - 1); i++)
			toReturn[i] = byteArray[x++];
		return toReturn;
	}

	

	

	public static String convertArrayBytesToString(byte[] arr) {
		StringBuilder sb = new StringBuilder();
		for (byte s : arr) {
			sb.append(String.format("%02X", s));
		}
		return sb.toString();
	}

	/**
	 * Produces an hexadecimal string representation of a given {@code byte[]}.
	 * 
	 * @param arr
	 *            the array to convert.
	 * @return the produced hexadecimal string representation.
	 */
	public static String convertBytesToString(byte[] arr) {
		StringBuilder sb = new StringBuilder();
		for (byte s : arr) {
			sb.append(String.format("%02X", s));
		}
		return sb.toString();
	}

	/**
	 * Reverses the order of elements in a given {@code byte[]}.
	 * 
	 * @param _vect
	 *            the array to reverse.
	 * @return the reversed array.
	 */
	public static byte[] reverseBytes(byte[] _vect) {
		if (_vect == null) {
			return null;
		}
		byte[] _toReverse = new byte[_vect.length];
		for (int i = 0; i < _vect.length; i++)
			_toReverse[i] = _vect[i];

		int i = 0;
		int j = _toReverse.length - 1;
		byte tmp;
		while (j > i) {
			tmp = _toReverse[j];
			_toReverse[j] = _toReverse[i];
			_toReverse[i] = tmp;
			j--;
			i++;
		}
		return _toReverse;

	}

}
