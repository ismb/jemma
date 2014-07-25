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
package org.energy_home.jemma.ah.cluster.zigbee.eh;

import java.util.Arrays;

public class PowerProfileResponse {

	public short TotalProfileNum;
	public short PowerProfileID;
	public org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase[] PowerProfileTransferredPhases;

	public PowerProfileResponse() {
	}

	public PowerProfileResponse(short TotalProfileNum, short PowerProfileID,
			org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase[] PowerProfileTransferredPhases) {
		this.TotalProfileNum = TotalProfileNum;
		this.PowerProfileID = PowerProfileID;
		this.PowerProfileTransferredPhases = PowerProfileTransferredPhases;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append("TotalProfileNum=").append(TotalProfileNum);
		sb.append(", PowerProfileID=").append(PowerProfileID);
		sb.append(", PowerProfileTransferredPhases=").append(Arrays.toString(PowerProfileTransferredPhases));
		sb.append("}");
		return sb.toString();		
	}

}
