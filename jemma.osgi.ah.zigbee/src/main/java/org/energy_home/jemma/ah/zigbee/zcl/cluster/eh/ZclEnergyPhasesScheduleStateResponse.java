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

import org.energy_home.jemma.ah.cluster.zigbee.eh.EnergyPhasesScheduleStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclEnergyPhasesScheduleStateResponse {


    public static EnergyPhasesScheduleStateResponse zclParse(IZclFrame zclFrame)
        throws ZclValidationException
    {
        EnergyPhasesScheduleStateResponse r = new EnergyPhasesScheduleStateResponse();
        r.PowerProfileID = ZclDataTypeUI8 .zclParse(zclFrame);
        // r.NumOfScheduledEnergyPhases = ZclDataTypeUI8 .zclParse(zclFrame);
        
        // Nicola: start
		short NumsScheduledPhases = ZclDataTypeUI8.zclParse(zclFrame);

		ScheduledPhase[] ScheduledPhases = new ScheduledPhase[NumsScheduledPhases];
		for (int i = 0; i < NumsScheduledPhases; i++) {
			ScheduledPhases[i] = ZclScheduledPhase.zclParse(zclFrame);
		}
		
		r.ScheduledPhases = ScheduledPhases;
		
		// Nicola: end
        
        return r;
    }

//    public static void zclSerialize(IZclFrame zclFrame, EnergyPhasesScheduleStateResponse r)
//        throws ZclValidationException
//    {
//        ZclDataTypeUI8 .zclSerialize(zclFrame, r.PowerProfileID);
//        ZclDataTypeUI8 .zclSerialize(zclFrame, r.NumOfScheduledEnergyPhases);
//    }
//
//    public static int zclSize(EnergyPhasesScheduleStateResponse r)
//        throws ZclValidationException
//    {
//        int size = 0;
//        size += ZclDataTypeUI8 .zclSize(r.PowerProfileID);
//        size += ZclDataTypeUI8 .zclSize(r.NumOfScheduledEnergyPhases);
//        return size;
//    }

	public static void zclSerialize(IZclFrame zclFrame, EnergyPhasesScheduleStateResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.PowerProfileID);
		ZclDataTypeUI8.zclSerialize(zclFrame, (short) r.ScheduledPhases.length);
		for (int i = 0; i < r.ScheduledPhases.length; i++) {
			ZclScheduledPhase.zclSerialize(zclFrame, r.ScheduledPhases[i]);
		}
	}

	public static int zclSize(EnergyPhasesScheduleStateResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.PowerProfileID);
		size += 1;
		size += ZclScheduledPhase.zclSize(null) * r.ScheduledPhases.length;
		return size;
	}
}
