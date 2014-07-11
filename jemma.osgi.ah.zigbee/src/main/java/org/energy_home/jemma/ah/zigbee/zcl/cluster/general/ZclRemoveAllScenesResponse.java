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

import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveAllScenesResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclRemoveAllScenesResponse {

	public static RemoveAllScenesResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		RemoveAllScenesResponse r = new RemoveAllScenesResponse();
		r.Status = ZclDataTypeEnum8.zclParse(zclFrame);
		r.GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, RemoveAllScenesResponse r) throws ZclValidationException {
		ZclDataTypeEnum8.zclSerialize(zclFrame, r.Status);
		ZclDataTypeUI16.zclSerialize(zclFrame, r.GroupID);
	}

	public static int zclSize(RemoveAllScenesResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(r.Status);
		size += ZclDataTypeUI16.zclSize(r.GroupID);
		return size;
	}

}
