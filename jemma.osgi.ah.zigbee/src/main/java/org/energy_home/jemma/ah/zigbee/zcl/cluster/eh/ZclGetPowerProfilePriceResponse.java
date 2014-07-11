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

import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceResponse;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclGetPowerProfilePriceResponse {

	public static GetPowerProfilePriceResponse zclParse(IZclFrame zclFrame) throws ZclValidationException {
		GetPowerProfilePriceResponse r = new GetPowerProfilePriceResponse();
		r.PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		r.Currency = ZclDataTypeUI16.zclParse(zclFrame);
		r.Price = ZclDataTypeUI32.zclParse(zclFrame);
		r.PriceTrailingDigit = ZclDataTypeUI8.zclParse(zclFrame);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, GetPowerProfilePriceResponse r) throws ZclValidationException {
		ZclDataTypeUI8.zclSerialize(zclFrame, r.PowerProfileID);
		ZclDataTypeUI16.zclSerialize(zclFrame, r.Currency);
		ZclDataTypeUI32.zclSerialize(zclFrame, r.Price);
		ZclDataTypeUI8.zclSerialize(zclFrame, r.PriceTrailingDigit);
	}

	public static int zclSize(GetPowerProfilePriceResponse r) throws ZclValidationException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(r.PowerProfileID);
		size += ZclDataTypeUI16.zclSize(r.Currency);
		size += ZclDataTypeUI32.zclSize(r.Price);
		size += ZclDataTypeUI8.zclSize(r.PriceTrailingDigit);
		return size;
	}

}
