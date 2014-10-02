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

    public static void _16BitsToLittleEndian(int i, byte abyte0[], int j) {
		abyte0[j] = (byte) (0xff & i);
		abyte0[j + 1] = (byte) (0xff & i >>> 8);
	}

	public static int _16BitLittleEndianToInt(byte abyte0[], int i) {
		int j = 0xffff & abyte0[i + 1] << 8;
		j += 0xff & abyte0[i];
		return j;
	}

    public static void floatToSemiPrecision(float f, byte abyte0[], int i) {
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

	public static float semiPrecisionToFloat(byte abyte0[], int i) {
		int j = _16BitLittleEndianToInt(abyte0, i);
		int k = (j >>> 15) << 31;
		int l = (j << 22) >>> 9;
		int i1 = (j << 17) >>> 27;
		int j1;
		if (i1 == 31)
			j1 = 255;
		else if (i1 == 0)
			j1 = 0;
		else
			j1 = i1 + 112;
		j1 <<= 23;
		int k1 = k | l | j1;
        return Float.intBitsToFloat(k1);
	}

	public static boolean match(byte abyte0[], byte abyte1[]) {
		if (abyte0 == abyte1)
			return true;
		if (abyte0 == null || abyte1 == null)
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

}
