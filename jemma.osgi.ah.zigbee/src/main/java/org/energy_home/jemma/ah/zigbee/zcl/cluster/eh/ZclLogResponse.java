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

import org.energy_home.jemma.ah.cluster.zigbee.eh.LogResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;

public class ZclLogResponse {

	public static LogResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		LogResponse r = new LogResponse();
		r.Timestamp = ZclDataTypeUTCTime.zclParse(zclFrame);
		r.LogID = ZclDataTypeUI32.zclParse(zclFrame);
		long LogLength = ZclDataTypeUI32.zclParse(zclFrame);
		r.LogPayload = zclFrame.parseArray((int) LogLength);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, LogResponse r) throws ZclValidationException {
		ZclDataTypeUTCTime.zclSerialize(zclFrame, r.Timestamp);
		ZclDataTypeUI32.zclSerialize(zclFrame, r.LogID);
		ZclDataTypeUI32.zclSerialize(zclFrame, r.LogPayload.length);
		zclFrame.appendArray(r.LogPayload);
	}

	public static int zclSize(LogResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUTCTime.zclSize(r.Timestamp);
		size += ZclDataTypeUI32.zclSize(r.LogID);
		size += ZclDataTypeUI32.zclSize(r.LogPayload.length);
		size += r.LogPayload.length;
		return size;
	}

}
