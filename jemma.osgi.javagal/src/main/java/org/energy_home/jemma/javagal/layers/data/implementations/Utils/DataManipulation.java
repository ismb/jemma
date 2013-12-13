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
package org.energy_home.jemma.javagal.layers.data.implementations.Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class with a number of data's manipulation methods. 
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class DataManipulation {

	public final static int SEQUENCE_START = 0x02;
	public final static int START_PAYLOAD_INDEX = 4;

	// Defining array of bytes to pass later to the key

	private static Log logger = LogFactory.getLog(DataManipulation.class);

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static short[] toVectFromList(List<Short> _list) {
		short[] _vect = new short[_list.size()];
		int i = 0;
		for (short s : _list)
			_vect[i++] = s;
		return _vect;

	}

	/**
	 * 
	 * @param _value
	 *            byte to check the value
	 * @param position
	 *            to check the bit
	 * @return true if the bit is 1
	 */
	/*
	 * public static boolean getBit(byte _value, int position) { return
	 * (((_value >> position) & 1) == 1); }
	 */
	/*
	 * public static boolean getBit(short _value, int position) { return
	 * (((_value >> position) & 1) == 1); }
	 */
	public static int toIntFromShort(byte hb, byte lb) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] { 0x00, 0x00, hb, lb });
		return bb.getInt();
	}

	public static long toLong(byte _1, byte _2, byte _3, byte _4, byte _5,
			byte _6, byte _7, byte _8) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[] { _1, _2, _3, _4, _5, _6,
				_7, _8 });
		return bb.getLong();
	}

	public static List<Byte> toByteList(Long toConvert, int pad) {
		List<Byte> toReturn = new ArrayList<Byte>();

		String inString = Long.toHexString(toConvert);

		int length = inString.length();

		// Characters must be even in number
		if (length % 2 != 0) {
			// System.out
			// .print("Odd number of chars in conversion from Long to byte[] ");
			inString = "0" + inString;
			length = inString.length();
		}

		for (int start = 0; start < length; start += 2) {
			toReturn.add((byte) Short.parseShort(
					inString.substring(start, Math.min(length, start + 2)), 16));
		}

		if (toReturn.size() < pad) {
			int diff = pad - toReturn.size();
			for (int i = 0; i < diff; i++) {
				toReturn.add(0, (byte) 0);
			}
		}

		return toReturn;
	}

	public static List<Byte> toByteList(Short toConvert, int pad) {
		List<Byte> toReturn = new ArrayList<Byte>();

		String inString = Integer.toHexString(toConvert);

		int length = inString.length();

		// Characters must be even in number
		if (length % 2 != 0) {
			// System.out
			// .print("Odd number of chars in conversion from Long to byte[] ");
			inString = "0" + inString;
			length = inString.length();
		}

		for (int start = 0; start < length; start += 2) {
			toReturn.add((byte) Short.parseShort(
					inString.substring(start, Math.min(length, start + 2)), 16));
		}

		if (toReturn.size() < pad) {
			int diff = pad - toReturn.size();
			for (int i = 0; i < diff; i++) {
				toReturn.add(0, (byte) 0);
			}
		}

		return toReturn;
	}

	/**
	 * the value is written from byte 0 to byte n
	 * 
	 */
	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putLong(x);
		return buffer.array();
	}

	/**
	 * the value is written from byte 0 to byte n
	 * 
	 */
	public static byte[] intToBytes(int x) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putInt(x);
		return buffer.array();
	}

	/**
	 * the value is written from byte 0 to byte n
	 * 
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
	 * the value is written from byte 0 to byte n
	 * 
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
	 * the value is written from byte 0 to byte n
	 * 
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

	/**
	 * print a short[] in Hex format
	 * 
	 */
	public static void logArrayHexRadix(String caption, short[] arr) {
		StringBuilder sb = new StringBuilder();
		for (short s : arr) {
			sb.append(String.format("%02X", s));
		}
		logger.info("\n\r" + caption + ":" + sb.toString() + "\n\r");
	}

	/**
	 * print a byte[] in Hex format
	 * 
	 */
	public static void logArrayHexRadix(String caption, byte[] arr) {
		StringBuilder sb = new StringBuilder();
		for (byte s : arr) {
			sb.append(String.format("%02X", s));
		}
		logger.info("\n\r" + caption + ":" + sb.toString() + "\n\r");
	}

	/**
	 * return a string that is the hex format of the byte[]
	 * 
	 */
	public static String convertBytesToString(byte[] arr) {
		String sb = new String();
		for (byte s : arr) {
			sb += String.format("%02X", s);
		}
		return sb;
	}

	/**
	 * Starts from a short[] and returns a sub array, converted as byte[]
	 * 
	 * @param array
	 *            the original array
	 * @param start
	 *            the start index, included
	 * @param stop
	 *            the stop index, included
	 * @return
	 */
	public static byte[] subByteArray(short[] array, int start, int stop) {
		byte[] toReturn = new byte[stop - start + 1];
		for (int i = start; i <= stop; i++) {
			toReturn[i - start] = (byte) array[i];
		}
		return toReturn;
	}

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
