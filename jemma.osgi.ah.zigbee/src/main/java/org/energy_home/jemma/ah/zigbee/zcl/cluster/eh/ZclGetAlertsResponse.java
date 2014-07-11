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

import org.energy_home.jemma.ah.cluster.zigbee.eh.GetAlertsResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetAlertsResponse {

	public static GetAlertsResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		GetAlertsResponse r = new GetAlertsResponse();
		int size = ZclDataTypeUI8.zclParse(zclFrame);
		r.Alerts = new int[size];
		for (int i = 0; i < size; i++) {
			r.Alerts[i] = ZclDataTypeUI24.zclParse(zclFrame);
		}
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, GetAlertsResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, (short) r.Alerts.length);
		for (int i = 0; (i < r.Alerts.length); i++) {
			ZclDataTypeUI24.zclSerialize(zclFrame, r.Alerts[i]);
		}
	}

	public static int zclSize(GetAlertsResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize((short) 0);
		size += (r.Alerts.length * ZclDataTypeUI24.zclSize(0));
		return size;
	}

}
