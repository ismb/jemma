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

public class ZclDataTypeI8 extends ZclAbstractDataType {
	public static final int ZCL_DATA_TYPE = ZclTypes.ZclInt8Type;
	static final int BYTE_ARRAY_LENGTH = 1;
	static final boolean IS_ANALOG = true;
	
/*	static final Short MAX_VALUE = new Short((short) 0xff);
	static final Short MIN_VALUE = new Short((short) 0x00);
	static final Short STEP_VALUE = null;

	private static ZclDataTypeUI8 instance = null;

	static synchronized public ZclDataTypeUI8 get() {
		try {
			if (instance == null)
				instance = new ZclDataTypeUI8(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	static public ZclDataTypeUI8 get(Object defaultValue) {
		try {
			return new ZclDataTypeUI8(defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected ZclDataTypeUI8(Object defaultValue) throws ApplianceValidationException {
		super(Short.class.getCanonicalName(), defaultValue, null, MIN_VALUE, MAX_VALUE, STEP_VALUE);
	}

	public int zclGetDataType() {
		return ZCL_TYPE_UNSIGNED_8_BIT_INT;
	}

	public boolean zclIsDescreteDataType() {
		return false;
	}

	public int zclGetByteLength(Object value) {
		return BYTE_ARRAY_LENGTH;
	}

	public int zclParse(byte[] msg, int msgIndex, Object[] values, int valuesIndex) throws ZclValidationException {
		int l = 0xff & msg[msgIndex];
		values[valuesIndex] = new Integer(l);
		return msgIndex + 1;
	}
	
	public int zclSerialize(Object value, byte[] msg, int msgIndex) throws ZclValidationException {
		// TODO Auto-generated method stub
		return 0;
	}
	*/
	
	public static short zclParse(IZclFrame zclFrame) throws ZclValidationException {
		return zclFrame.parseUInt8();
	}
	
	public static void zclSerialize(IZclFrame zclFrame, short uint8)  throws ZclValidationException {
		zclFrame.appendUInt8(uint8);
	}
	
	public static int zclSize (short uint8)  throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}
	
	public void zclObjectSerialize(IZclFrame zclFrame, Object value) throws ZclValidationException {
		ZclDataTypeI8.zclSerialize(zclFrame, (short) ((Number) value).intValue());
	}

	public int zclObjectSize(Object value) throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}
	
	public Object zclParseToObject(IZclFrame zclFrame) throws ZclValidationException {
		return new Short(ZclDataTypeI8.zclParse(zclFrame));
	}

	public short zclGetDataType() {
		return ZCL_DATA_TYPE;
	}

	public boolean isAnalog() {
		return IS_ANALOG;
	}
}
