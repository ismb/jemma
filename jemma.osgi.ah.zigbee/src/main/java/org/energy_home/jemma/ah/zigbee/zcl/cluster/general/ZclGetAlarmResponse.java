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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.general;

import org.energy_home.jemma.ah.cluster.zigbee.general.GetAlarmResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;

public class ZclGetAlarmResponse {

	public static GetAlarmResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		GetAlarmResponse r = new GetAlarmResponse();
		r.Status = ZclDataTypeEnum8.zclParse(zclFrame);
		r.AlarmCode = ZclDataTypeEnum8.zclParse(zclFrame);
		r.ClusterIdentifier = ZclDataTypeUI16.zclParse(zclFrame);
		r.Timestamp = ZclDataTypeUI32.zclParse(zclFrame);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, GetAlarmResponse r) throws ZclValidationException {
		ZclDataTypeEnum8.zclSerialize(zclFrame, r.Status);
		ZclDataTypeEnum8.zclSerialize(zclFrame, r.AlarmCode);
		ZclDataTypeUI16.zclSerialize(zclFrame, r.ClusterIdentifier);
		ZclDataTypeUI32.zclSerialize(zclFrame, r.Timestamp);
	}

	public static int zclSize(GetAlarmResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(r.Status);
		size += ZclDataTypeEnum8.zclSize(r.AlarmCode);
		size += ZclDataTypeUI16.zclSize(r.ClusterIdentifier);
		size += ZclDataTypeUI32.zclSize(r.Timestamp);
		return size;
	}

}
