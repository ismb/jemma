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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.eh;

import org.energy_home.jemma.ah.cluster.zigbee.eh.DEFTR;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclDEFTR {

	public static DEFTR zclParse(IZclFrame zclFrame) throws ZclValidationException {
		DEFTR r = new DEFTR();

		r.Acknowledge = ZclDataTypeUI8.zclParse(zclFrame);
		if (r.Acknowledge == 0x5a) {
			r.FrameHeader = ZclDataTypeUI8.zclParse(zclFrame);
			r.FrameType = ZclDataTypeUI8.zclParse(zclFrame);
			r.FramePayload = ZclDataTypeOctets.zclParse(zclFrame);
		}
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, DEFTR r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.Acknowledge);
		if (r.Acknowledge == 0x5a) {
			ZclDataTypeUI8.zclSerialize(zclFrame, r.FrameHeader);
			ZclDataTypeUI8.zclSerialize(zclFrame, r.FrameType);
			ZclDataTypeOctets.zclSerialize(zclFrame, r.FramePayload);
		}
	}

	public static int zclSize(DEFTR r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.Acknowledge);
		if (r.Acknowledge == 0x5a) {
			size += ZclDataTypeUI8.zclSize(r.FrameHeader);
			size += ZclDataTypeUI8.zclSize(r.FrameType);
			size += ZclDataTypeOctets.zclSize(r.FramePayload);
		}
		return size;
	}
}
