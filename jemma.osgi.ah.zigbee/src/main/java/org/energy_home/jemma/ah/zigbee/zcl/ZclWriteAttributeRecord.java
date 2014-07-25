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
package org.energy_home.jemma.ah.zigbee.zcl;

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI48;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclTypes;

public class ZclWriteAttributeRecord {
	public int Id;
	public short DataType;
	public Object Data;

	public static ZclWriteAttributeRecord zclParse(IZclFrame zclFrame) throws ZclValidationException {
		ZclWriteAttributeRecord r = new ZclWriteAttributeRecord();
		r.Id = ZclDataTypeUI16.zclParse(zclFrame);
		r.DataType = ZclDataTypeUI8.zclParse(zclFrame);
		switch (r.DataType) {
		case ZclTypes.ZclData8Type:
		case ZclTypes.ZclData16Type:
		case ZclTypes.ZclData24Type:
		case ZclTypes.ZclData32Type:
			// FIXME: raise an exception
			break;

		case ZclTypes.ZclBooleanType: {
			boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
			r.Data = (Object) new Boolean(v);
			break;
		}
		case ZclTypes.ZclBitmap8Type: {
			short v = ZclDataTypeBitmap8.zclParse(zclFrame);
			r.Data = (Object) new Short(v);
			break;
		}

		case ZclTypes.ZclBitmap16Type: {
			int v = ZclDataTypeBitmap16.zclParse(zclFrame);
			r.Data = (Object) new Integer(v);
			break;
		}
		case ZclTypes.ZclBitmap24Type: {
			int v = ZclDataTypeBitmap24.zclParse(zclFrame);
			r.Data = (Object) new Integer(v);
			break;
		}

		case ZclTypes.ZclBitmap32Type: {
			long v = ZclDataTypeBitmap32.zclParse(zclFrame);
			r.Data = (Object) new Long(v);
			break;
		}
		case ZclTypes.ZclUInt8Type: {
			short v = ZclDataTypeUI8.zclParse(zclFrame);
			r.Data = (Object) new Short(v);
			break;
		}

		case ZclTypes.ZclUInt16Type:
		case ZclTypes.ZclUInt24Type:
		case ZclTypes.ZclUInt32Type:
		case ZclTypes.ZclUInt40Type:
		case ZclTypes.ZclUInt48Type:
		case ZclTypes.ZclInt8Type:
		case ZclTypes.ZclInt16Type:
		case ZclTypes.ZclInt24Type:
		case ZclTypes.ZclInt32Type:
		case ZclTypes.ZclEnum8Type:
		case ZclTypes.ZclEnum16Type:
		case ZclTypes.ZclOctetsType: {
			byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
			// FIXME: to found a way to store a byte array.
			r.Data = null;
			break;
		}
		case ZclTypes.ZclStringType: {
			String v = ZclDataTypeString.zclParse(zclFrame);
			r.Data = v;
			break;
		}
		case ZclTypes.ZclClusterIDType:
		case ZclTypes.ZclFloatType:
		case ZclTypes.ZclUTCTime:
			// FIXME: raise an exception
			break;

		default:
			// FIXME: raise an exception
			break;

		}
		return r;
	}

	public static void zclSkip(IZclFrame zclFrame, short dataType) throws ZclValidationException {
		switch (dataType) {
		case ZclTypes.ZclData8Type:
		case ZclTypes.ZclBitmap8Type:
		case ZclTypes.ZclUInt8Type:
		case ZclTypes.ZclInt8Type:
		case ZclTypes.ZclEnum8Type:
			ZclDataTypeUI8.zclParse(zclFrame);
			break;

		case ZclTypes.ZclData16Type:
		case ZclTypes.ZclUInt16Type:
		case ZclTypes.ZclInt16Type:
		case ZclTypes.ZclEnum16Type:
		case ZclTypes.ZclClusterIDType:
			ZclDataTypeUI16.zclParse(zclFrame);
			break;

		case ZclTypes.ZclData24Type:
		case ZclTypes.ZclInt24Type:
		case ZclTypes.ZclBitmap24Type:
		case ZclTypes.ZclUInt24Type:
			ZclDataTypeUI24.zclParse(zclFrame);
			break;
		case ZclTypes.ZclData32Type:
		case ZclTypes.ZclUInt32Type:
		case ZclTypes.ZclBitmap32Type:
		case ZclTypes.ZclInt32Type:
			ZclDataTypeUI32.zclParse(zclFrame);
			break;

		case ZclTypes.ZclBooleanType:
			ZclDataTypeBoolean.zclParse(zclFrame);
			break;

		case ZclTypes.ZclUInt48Type:
			ZclDataTypeUI48.zclParse(zclFrame);

		case ZclTypes.ZclOctetsType:
			ZclDataTypeOctets.zclParse(zclFrame);
			break;
		case ZclTypes.ZclStringType:
			ZclDataTypeString.zclParse(zclFrame);
			break;

		case ZclTypes.ZclFloatType:
		case ZclTypes.ZclUInt40Type:
			// FIXME: not yet implemented
			break;

		case ZclTypes.ZclUTCTime:
			ZclDataTypeUTCTime.zclParse(zclFrame);
			break;

		default:
			// FIXME: raise an exception
			break;

		}
	}
}
