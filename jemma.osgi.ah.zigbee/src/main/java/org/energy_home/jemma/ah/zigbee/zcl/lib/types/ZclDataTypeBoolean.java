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

public class ZclDataTypeBoolean extends ZclAbstractDataType {
	private static final int BYTE_ARRAY_LENGTH = 1;
	public static final int ZCL_DATA_TYPE = ZclTypes.ZclBooleanType;
	static final boolean IS_ANALOG = false;
		
	public static boolean zclParse(IZclFrame zclFrame) throws ZclValidationException {
		return zclFrame.parseBoolean();
	}
	
	public static void zclSerialize(IZclFrame zclFrame, boolean value)  throws ZclValidationException {
		zclFrame.appendBoolean(value);
	}
	
	public static int zclSize (boolean bool)  throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}

	public boolean isAnalog() {
		return IS_ANALOG;
	}

	public short zclGetDataType() {
		return ZCL_DATA_TYPE;
	}

	public void zclObjectSerialize(IZclFrame zclFrame, Object value) throws ZclValidationException {
		ZclDataTypeBoolean.zclSerialize(zclFrame, ((Boolean) value).booleanValue());
	}

	public int zclObjectSize(Object value) throws ZclValidationException {
		return BYTE_ARRAY_LENGTH;
	}

	public Object zclParseToObject(IZclFrame zclFrame) throws ZclValidationException {
		return new Boolean(ZclDataTypeBoolean.zclParse(zclFrame));
	}
}
