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

public class ZclDataTypeUTCTime extends ZclAbstractDataType {
	public static final int ZCL_DATA_TYPE = ZclTypes.ZclUTCTime;
	private static final int BYTE_ARRAY_LENGTH = 4;
	static final boolean IS_ANALOG = false;
	
/*
	static final Short MAX_VALUE = new Short((short) 0xff);
	static final Short MIN_VALUE = new Short((short) 0x00);
	static final Short STEP_VALUE = null;
	
	private static ZclDataTypeUTCTime instance = null;

	static synchronized public ZclDataTypeUTCTime get() {
		try {
			if (instance == null)
				instance = new ZclDataTypeUTCTime(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return instance;
	}

	static public ZclDataTypeUTCTime get(Object defaultValue) {
		try {
			return new ZclDataTypeUTCTime(defaultValue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	protected ZclDataTypeUTCTime(Object defaultValue) throws ApplianceValidationException {
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
	
	public static long zclParse(IZclFrame zclFrame) throws ZclValidationException {
		return zclFrame.parseUTCTime();
	}
	
	public static void zclSerialize(IZclFrame zclFrame, long value)  throws ZclValidationException {
		zclFrame.appendUTCTime(value);
	}
	
	public static int zclSize (long value)  throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}
	public boolean isAnalog() {
		return IS_ANALOG;
	}

	public short zclGetDataType() {
		return ZCL_DATA_TYPE;
	}

	public void zclObjectSerialize(IZclFrame zclFrame, Object value) throws ZclValidationException {
		ZclDataTypeUI32.zclSerialize(zclFrame, (long) ((Number) value).longValue());
	}

	public int zclObjectSize(Object value) throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}

	public Object zclParseToObject(IZclFrame zclFrame) throws ZclValidationException {
		return new Long(ZclDataTypeUI32.zclParse(zclFrame));
	}

}
