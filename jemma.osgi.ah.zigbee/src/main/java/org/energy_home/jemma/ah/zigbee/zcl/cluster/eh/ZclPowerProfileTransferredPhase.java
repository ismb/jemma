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

import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfileTransferredPhase {
	public static PowerProfileTransferredPhase zclParse(IZclFrame zclFrame) throws ZclValidationException {
		PowerProfileTransferredPhase p = new PowerProfileTransferredPhase();
		p.EnergyPhaseID = ZclDataTypeUI8.zclParse(zclFrame);
		p.MacroPhaseID = ZclDataTypeUI8.zclParse(zclFrame);
		p.ExpectedDuration = ZclDataTypeUI16.zclParse(zclFrame);
		p.PeakPower = ZclDataTypeUI16.zclParse(zclFrame);
		p.Energy = ZclDataTypeUI16.zclParse(zclFrame);
		p.MaxActivationDelay = ZclDataTypeUI16.zclParse(zclFrame);
		return p;
	}

	public static void zclSerialize(IZclFrame zclFrame, PowerProfileTransferredPhase value) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, value.EnergyPhaseID);
		ZclDataTypeUI8.zclSerialize(zclFrame, value.MacroPhaseID);
		ZclDataTypeUI16.zclSerialize(zclFrame, value.ExpectedDuration);
		ZclDataTypeUI16.zclSerialize(zclFrame, value.PeakPower);
		ZclDataTypeUI16.zclSerialize(zclFrame, value.Energy);
		ZclDataTypeUI16.zclSerialize(zclFrame, value.MaxActivationDelay);
	}

	public static int zclSize(PowerProfileTransferredPhase value) throws ZclValidationException {
		return 10;
	}
}
