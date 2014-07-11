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
package org.energy_home.jemma.ah.zigbee.zcl.lib.types;



/**
 * ZigBee type constants
 * 
 * TODO: implementare tutti i tipi di ZigBee (in particolare quelli utilizzati da Indesit!
 */

public class ZclTypes {

	public static final short ZclData8Type = 0x08;
	public static final short ZclData16Type = 0x09;
	public static final short ZclData24Type = 0x0a;
	public static final short ZclData32Type = 0x0b;
	public static final short ZclBooleanType = 0x10;
	public static final short ZclBitmap8Type = 0x18;
	public static final short ZclBitmap16Type = 0x19;
	public static final short ZclBitmap24Type = 0x1a;
	public static final short ZclBitmap32Type = 0x1b;

	public static final short ZclUInt8Type = 0x20;
	public static final short ZclUInt16Type = 0x21;
	public static final short ZclUInt24Type = 0x22;
	public static final short ZclUInt32Type = 0x23;

	public static final short ZclUInt40Type = 0x24; // From Smart Energy Profile
	public static final short ZclUInt48Type = 0x25; // From Smart Energy Profile

	public static final short ZclInt8Type = 0x28;
	public static final short ZclInt16Type = 0x29;
	public static final short ZclInt24Type = 0x2a;
	public static final short ZclInt32Type = 0x2b;

	public static final short ZclEnum8Type = 0x30;
	public static final short ZclEnum16Type = 0x31;

	public static final short ZclOctetsType = 0x41;
	public static final short ZclStringType = 0x42;
	
	public static final short ZclClusterIDType = 0xe8;
	public static final short ZclAttributeIDType = 0xe9;
	public static final short ZclBACnetOIDType = 0xea;
	public static final short ZclIEEEAddressType = 0xf0;

	public static final short ZclFloatType = 0x99; // FIXME: put the right value
													// from specs!!!
	public static final short ZclUTCTime = 0xe2; // From Smart Energy Profile
}