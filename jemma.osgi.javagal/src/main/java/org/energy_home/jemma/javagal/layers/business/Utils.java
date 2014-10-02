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
package org.energy_home.jemma.javagal.layers.business;

/**
 * Utilities class. Provides convenient methods to manipulate data (conversion,
 * reversing, truncating and so on).
* @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
  *
 */
public class Utils {

	/**
	 * Converts an array of bytes to a String.
	 * 
	 * @param array
	 *            the array of bytes to convert.
	 * @return the converted String representation of the given array of bytes.
	 */
	public static String convertByteArrayToString(byte[] array) {
		StringBuilder sb = new StringBuilder();
        for (byte anArray : array) {
            sb.append(String.format("%02X", (0xFF & anArray)));
        }
		return sb.toString();
	}

    /**
	 * Build a channel mask as {@code byte[]} starting from the provided short.
	 * 
	 * @param channel
	 *            the channel mask contained in a short number
	 * @return the built channel mask.
	 */
	public static byte[] buildChannelMask(short channel) {
		byte[] tores = null;
		switch (channel) {
		case 0:
			tores= new byte[]{0x00,0x00,0x08,0x00};
			break;
		case 11:
			tores= new byte[]{0x00,0x00,0x08,0x00};
			break;
		case 12:
			tores= new byte[]{0x00,0x00,0x10,0x00};
			break;
		case 13:
			tores= new byte[]{0x00,0x00,0x20,0x00};
			break;
		case 14:
			tores= new byte[]{0x00,0x00,0x40,0x00};
			break;
		case 15:
			tores= new byte[]{0x00,0x00,(byte) 0x80,0x00};
			break;
		case 16:
			tores= new byte[]{0x00,0x01,0x00,0x00};
			break;
		case 17:
			tores= new byte[]{0x00,0x02,0x00,0x00};
			break;
		case 18:
			tores= new byte[]{0x00,0x04,0x00,0x00};
			break;
		case 19:
			tores= new byte[]{0x00,0x08,0x00,0x00};
			break;
		case 20:
			tores= new byte[]{0x00,0x10,0x00,0x00};
			break;
		case 21:
			tores= new byte[]{0x00,0x20,0x00,0x00};
			break;
		case 22:
			tores= new byte[]{0x00,0x40,0x00,0x00};
			break;
		case 23:
			tores= new byte[]{0x00,(byte) 0x80,0x00,0x00};
			break;
		case 24:
			tores= new byte[]{0x01,0x00,0x00,0x00};
			break;
		case 25:
			tores= new byte[]{0x02,0x00,0x00,0x00};
			break;
		case 26:
			tores= new byte[]{0x04,0x00,0x00,0x00};
			break;
		}
		return tores;
	}

	/**
	 * Makes a new {@code byte[]} as a subset of an original given one.
	 * 
	 * @param original
	 *            the original array.
	 * @param from
	 *            initial index position from where to copy.
	 * @param to
	 *            final index position until where to copy.
	 * @return the produced subset.
	 */
	public static byte[] copyOfRange(byte[] original, int from, int to) {
		int newLength = to - from;
		if (newLength < 0)
			throw new IllegalArgumentException(from + " > " + to);
		byte[] copy = new byte[newLength];
		System.arraycopy(original, from, copy, 0,
				Math.min(original.length - from, newLength));
		return copy;
	}
	
	public static byte[] copyOfRange(short[] original, int from, int to) {
		int newLength = to - from;
		if (newLength < 0)
			throw new IllegalArgumentException(from + " > " + to);
		byte[] copy = new byte[newLength];
		System.arraycopy(original, from, copy, 0,
				Math.min(original.length - from, newLength));
		return copy;
	}

	/**
	 * Merges two {@code byte[]}. The length of the resulting array is the sum
	 * of the lengths of the two arrays constituents. The resulting array just
	 * contains a copy of the first provided array and then a copy of the second
	 * provided array.
	 * 
	 * @param _top
	 *            the first array to merge.
	 * @param _bottom
	 *            the second array to merge.
	 * @return the resulting merged array.
	 */
	public static byte[] mergeBytesVect(byte[] _top, byte[] _bottom) {
		byte[] copy = new byte[_top.length + _bottom.length];
		for (int i = 0; i < copy.length; ++i) {
			copy[i] = i < _top.length ? _top[i] : _bottom[i - _top.length];
		}
		return copy;
	}

}
