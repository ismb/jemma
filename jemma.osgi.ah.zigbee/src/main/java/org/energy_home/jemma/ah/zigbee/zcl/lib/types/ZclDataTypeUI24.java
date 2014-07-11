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

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;

public class ZclDataTypeUI24 extends ZclAbstractDataType {
	public static final int ZCL_DATA_TYPE = ZclTypes.ZclUInt24Type;
	private static final int BYTE_ARRAY_LENGTH = 3;
	static final boolean IS_ANALOG = true;
	
/*
	private static final Integer MAX_VALUE = new Integer(0xffff);
	private static final Integer MIN_VALUE = new Integer(0x0000);
	private static final Integer STEP_VALUE = null;


	private static ZclDataTypeUI24 instance = null;
	
	static synchronized public ZclDataTypeUI24 get() {
		try {
			if (instance == null)
				instance = new ZclDataTypeUI24(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}
	
	static public ZclDataTypeUI24 get(Object defaultValue) {
		try {
			return new ZclDataTypeUI24(defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected ZclDataTypeUI24(Object defaultValue) throws ApplianceValidationException {
		super(Integer.class.getCanonicalName(), defaultValue, null, MIN_VALUE, MAX_VALUE, STEP_VALUE);
	}

	public int zclGetDataType() {
		return ZCL_TYPE_UNSIGNED_8_BIT_INT;
	}

	public boolean zclIsDescreteDataType() {
		return false;
	}
	
	public int zclGetByteLength(Object value) throws ZclValidationException{
		return BYTE_ARRAY_LENGTH;
	}

	public int zclParse(byte[] msg, int msgIndex, Object[] values, int valuesIndex) throws ZclValidationException {
		int i1 = ZclByteUtils._16BitLittleEndianToInt(msg, msgIndex);
		values[valuesIndex] = new Integer(i1);
		return msgIndex + 2;
	}

	public int zclSerialize(Object value, byte[] msg, int msgIndex) throws ZclValidationException{
		int l = ((Integer) value).intValue();
		ZclByteUtils._16BitsToLittleEndian(l, msg, msgIndex);
		return msgIndex + 2;
	}*/
	
	public static int zclParse(IZclFrame zclFrame) throws ZclValidationException {
		return zclFrame.parseUInt24();
	}
	
	public static void zclSerialize(IZclFrame zclFrame, int uint)  throws ZclValidationException {
		zclFrame.appendUInt24(uint);
	}

	public static int zclSize(int value) {
		return BYTE_ARRAY_LENGTH;
	}
	
	public boolean isAnalog() {
		return IS_ANALOG;
	}

	public short zclGetDataType() {
		return ZCL_DATA_TYPE;
	}

	public void zclObjectSerialize(IZclFrame zclFrame, Object value) throws ZclValidationException {
		ZclDataTypeUI24.zclSerialize(zclFrame, (int) ((Number) value).intValue());
	}

	public int zclObjectSize(Object value) throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}

	public Object zclParseToObject(IZclFrame zclFrame) throws ZclValidationException {
		return new Integer(ZclDataTypeUI24.zclParse(zclFrame));
	}
}
