/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

import org.energy_home.jemma.ah.cluster.zigbee.general.GetGroupMembershipResponse;

public class ZclGetGroupMembershipResponse {

	public static GetGroupMembershipResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		GetGroupMembershipResponse r = new GetGroupMembershipResponse();
		r.Capacity = ZclDataTypeUI8.zclParse(zclFrame);
		r.GroupCount = ZclDataTypeUI8.zclParse(zclFrame);
		// FIXME: unable handle parameter 'GroupList' of type 'array of uint16'
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, GetGroupMembershipResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.Capacity);
		ZclDataTypeUI8.zclSerialize(zclFrame, r.GroupCount);
	}

	public static int zclSize(GetGroupMembershipResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.Capacity);
		size += ZclDataTypeUI8.zclSize(r.GroupCount);
		return size;
	}

}
