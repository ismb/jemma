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

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfileScheduleConstraintsResponse {

	public static PowerProfileScheduleConstraintsResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		PowerProfileScheduleConstraintsResponse r = new PowerProfileScheduleConstraintsResponse();
		r.PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		r.StartAfter = ZclDataTypeUI16.zclParse(zclFrame);
		r.StopBefore = ZclDataTypeUI16.zclParse(zclFrame);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, PowerProfileScheduleConstraintsResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.PowerProfileID);
		ZclDataTypeUI16.zclSerialize(zclFrame, r.StartAfter);
		ZclDataTypeUI16.zclSerialize(zclFrame, r.StopBefore);
	}

	public static int zclSize(PowerProfileScheduleConstraintsResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.PowerProfileID);
		size += ZclDataTypeUI16.zclSize(r.StartAfter);
		size += ZclDataTypeUI16.zclSize(r.StopBefore);
		return size;
	}

}
