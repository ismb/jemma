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

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfile {

	public static PowerProfile zclParse(IZclFrame zclFrame) throws ZclValidationException {

		PowerProfile p = new PowerProfile();
		p.PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		p.EnergyPhaseID = ZclDataTypeUI8.zclParse(zclFrame);
		p.PowerProfileRemoteControl = ZclDataTypeBoolean.zclParse(zclFrame);
		p.PowerProfileState = ZclDataTypeEnum8.zclParse(zclFrame);

		return p;
	}

	public static void zclSerialize(IZclFrame zclFrame, PowerProfile value) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, value.PowerProfileID);
		ZclDataTypeUI8.zclSerialize(zclFrame, value.EnergyPhaseID);
		ZclDataTypeBoolean.zclSerialize(zclFrame, value.PowerProfileRemoteControl);
		ZclDataTypeEnum8.zclSerialize(zclFrame, value.PowerProfileState);
	}

	public static int zclSize(PowerProfile value) throws ZclValidationException {
		return 4;
	}
}