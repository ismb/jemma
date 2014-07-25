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
package org.energy_home.jemma.ah.zigbee.zcl.lib;

public class ZclByteUtils {

	public static final byte[] invert(byte abyte0[]) {
		if (abyte0 == null || abyte0.length == 0)
			return abyte0;
		byte abyte1[] = new byte[abyte0.length];
		for (int i = 0; i < abyte0.length; i++)
			abyte1[i] = abyte0[abyte0.length - i - 1];

		return abyte1;
	}

	public static final void invert(byte abyte0[], int i, byte abyte1[], int j, int k) {
		if (abyte0 == null || abyte0.length == 0 || k == 0)
			return;
		int l = Math.min(abyte0.length - 1, (i + k) - 1);
		for (int i1 = l; i1 >= i; i1--) {
			if (j >= abyte1.length)
				throw new ArrayIndexOutOfBoundsException("Destination array has insuffiecient length. Destination offset reached "
						+ j + " while dest length is " + abyte1.length);
			abyte1[j] = abyte0[i1];
			j++;
		}

	}

	public static final void _16BitsToLittleEndian(int i, byte abyte0[], int j) {
		abyte0[j] = (byte) (0xff & i >>> 0);
		abyte0[j + 1] = (byte) (0xff & i >>> 8);
	}

	public static final int _16BitLittleEndianToInt(byte abyte0[], int i) {
		int j = 0xffff & abyte0[i + 1] << 8;
		j += 0xff & abyte0[i];
		return j;
	}

	public static final void _24BitsToLittleEndian(int i, byte abyte0[], int j) {
		abyte0[j] = (byte) (0xff & i);
		abyte0[j + 1] = (byte) (0xff & i >>> 8);
		abyte0[j + 2] = (byte) (0xff & i >>> 16);
	}

	public static final int _24BitLittleEndianToInt(byte abyte0[], int i) {
		int j = 0xffffff & abyte0[i + 2] << 16;
		j += 0xffff & abyte0[i + 1] << 8;
		j += 0xff & abyte0[i];
		return j;
	}

	public static final void _32BitsToLittleEndian(int i, byte abyte0[], int j) {
		abyte0[j] = (byte) (0xff & i);
		abyte0[j + 1] = (byte) (0xff & i >>> 8);
		abyte0[j + 2] = (byte) (0xff & i >>> 16);
		abyte0[j + 3] = (byte) (0xff & i >>> 24);
	}

	public static final void _64BitsToLittleEndian(long l, byte abyte0[], int i) {
		abyte0[i] = (byte) (int) (255L & l);
		abyte0[i + 1] = (byte) (int) (255L & l >>> 8);
		abyte0[i + 2] = (byte) (int) (255L & l >>> 16);
		abyte0[i + 3] = (byte) (int) (255L & l >>> 24);
		abyte0[i + 4] = (byte) (int) (255L & l >>> 32);
		abyte0[i + 5] = (byte) (int) (255L & l >>> 40);
		abyte0[i + 6] = (byte) (int) (255L & l >>> 48);
		abyte0[i + 7] = (byte) (int) (255L & l >>> 56);
	}

	public static final int _32BitLittleEndianToInt(byte abyte0[], int i) {
		int j = abyte0[i + 3] << 24;
		j += 0xffffff & abyte0[i + 2] << 16;
		j += 0xffff & abyte0[i + 1] << 8;
		j += 0xff & abyte0[i];
		return j;
	}

	public static final long _64BitLittleEndianToLong(byte abyte0[], int i) {
		long l = 0L;
		l = ((long) abyte0[i + 7] << 56) + ((long) (abyte0[i + 6] & 0xff) << 48) + ((long) (abyte0[i + 5] & 0xff) << 40)
				+ ((long) (abyte0[i + 4] & 0xff) << 32) + ((long) (abyte0[i + 3] & 0xff) << 24)
				+ ((long) (abyte0[i + 2] & 0xff) << 16) + ((long) (abyte0[i + 1] & 0xff) << 8)
				+ ((long) (abyte0[i + 0] & 0xff) << 0);
		return l;
	}

	public static final void floatToSemiPrecision(float f, byte abyte0[], int i) {
		int j = Float.floatToIntBits(f);
		int k = j >> 23;
		k &= 0xff;
		int l = j << 9;
		l >>>= 9;
		int i1 = l >> 13;
		int j1 = (j >>> 31) << 15;
		int k1;
		if (k == 255) {
			k1 = 31744;
			k1 |= j1;
		} else if (k == 0) {
			k1 = j1 | i1;
		} else {
			int l1 = k - 112;
			l1 <<= 10;
			k1 = j1 | l1 | i1;
		}
		_16BitsToLittleEndian(k1, abyte0, i);
	}

	public static final float semiPrecisionToFloat(byte abyte0[], int i) {
		int j = _16BitLittleEndianToInt(abyte0, i);
		int k = (j >>> 15) << 31;
		int l = (j << 22) >>> 9;
		int i1 = (j << 17) >>> 27;
		int j1 = 0;
		if (i1 == 31)
			j1 = 255;
		else if (i1 == 0)
			j1 = 0;
		else
			j1 = i1 + 112;
		j1 <<= 23;
		int k1 = k | l | j1;
		float f = Float.intBitsToFloat(k1);
		return f;
	}

	public static final boolean match(byte abyte0[], byte abyte1[]) {
		if (abyte0 == abyte1)
			return true;
		if (abyte0 == null && abyte1 != null || abyte0 != null && abyte1 == null)
			return false;
		if (abyte0.length != abyte1.length)
			return false;
		for (int i = 0; i < abyte0.length; i++)
			if (abyte0[i] != abyte1[i])
				return false;

		return true;
	}

	public static void main(String args[]) throws Throwable {
		float f = (float) Math.pow(2D, -14D);
		System.out.println("input float: " + f);
		byte abyte0[] = new byte[2];
		int i = 0;
		floatToSemiPrecision(f, abyte0, i);
		float f1 = semiPrecisionToFloat(abyte0, 0);
		System.out.println("result float:" + f1);
		if (f1 == f)
			System.out.println("We have a match!");
		byte byte0 = -5;
		int j = (byte0 >>> 31) << 15;
		System.out.println(j);
	}

	public static String bytesToHexString(byte abyte0[], int i, int j) {
		StringBuffer stringbuffer = new StringBuffer("[");
		int k = Math.min(i + j, abyte0.length);
		for (int l = i; l < k; l++) {
			stringbuffer.append("0x");
			stringbuffer.append(toHexString(0xff & abyte0[l]));
			if (l < k - 1)
				stringbuffer.append(", ");
		}

		stringbuffer.append("]");
		return stringbuffer.toString();
	}

	public static String bytesToHexString(byte abyte0[]) {
		if (abyte0 == null)
			return "null";
		else
			return bytesToHexString(abyte0, 0, abyte0.length);
	}

	public static String toHexString(int i) {
		char ac[] = new char[32];
		int j = 32;
		byte byte0 = 16;
		int k = byte0 - 1;
		do {
			ac[--j] = HEX_DIGITS[i & k];
			i >>>= 4;
		} while (i != 0);
		return new String(ac, j, 32 - j);
	}

	static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
}
