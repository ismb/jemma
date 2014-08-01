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

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfileResponse {

	public static PowerProfileResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		PowerProfileResponse r = new PowerProfileResponse();
		r.TotalProfileNum = ZclDataTypeUI8.zclParse(zclFrame);
		r.PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		short NumPowerProfilesTransferredPhases = ZclDataTypeUI8.zclParse(zclFrame);

		PowerProfileTransferredPhase[] PowerProfileTransferredPhases = new PowerProfileTransferredPhase[NumPowerProfilesTransferredPhases];

		for (int i = 0; i < NumPowerProfilesTransferredPhases; i++) {
			PowerProfileTransferredPhases[i] = ZclPowerProfileTransferredPhase.zclParse(zclFrame);
		}

		r.PowerProfileTransferredPhases = PowerProfileTransferredPhases;
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, PowerProfileResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.TotalProfileNum);
		ZclDataTypeUI8.zclSerialize(zclFrame, r.PowerProfileID);
		ZclDataTypeUI8.zclSerialize(zclFrame, (short) r.PowerProfileTransferredPhases.length);
		for (int i = 0; i < r.PowerProfileTransferredPhases.length; i++) {
			ZclPowerProfileTransferredPhase.zclSerialize(zclFrame, r.PowerProfileTransferredPhases[i]);
		}
	}

	public static int zclSize(PowerProfileResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.TotalProfileNum);
		size += ZclDataTypeUI8.zclSize(r.PowerProfileID);
		size += 1;
		size += ZclPowerProfile.zclSize(null) * r.PowerProfileTransferredPhases.length;
		return size;
	}

}
