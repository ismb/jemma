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

import org.energy_home.jemma.ah.cluster.zigbee.general.TransferPartitionedFrameCommand;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclTransferPartitionedFrameCommand {
	public static TransferPartitionedFrameCommand zclParse(IZclFrame zclFrame) throws ZclValidationException {
		TransferPartitionedFrameCommand r = new TransferPartitionedFrameCommand();
		r.FragmentationOptions = ZclDataTypeEnum8.zclParse(zclFrame);
		// Ottiene il campo PartitionIndicator in funzione della lunghezza
		// definita dal bit IndicatorLength all'interno del campo
		// FragmentationOptions
		r.PartitionIndicator = ((r.FragmentationOptions & 0x02) > 0) ? zclFrame.parseUInt16() : zclFrame.parseUInt8();
		r.PartitionedFrame = ZclDataTypeOctets.zclParse(zclFrame);
		return r;
	}

	public static void zclSerialize(IZclFrame zclFrame, TransferPartitionedFrameCommand r) throws ZclValidationException {		
		ZclDataTypeUI8.zclSerialize(zclFrame, r.FragmentationOptions);
		if (r.PartitionIndicator > 0xFF)
			ZclDataTypeUI16.zclSerialize(zclFrame, r.PartitionIndicator);
		else
			ZclDataTypeUI8.zclSerialize(zclFrame, (short) r.PartitionIndicator);
		ZclDataTypeOctets.zclSerialize(zclFrame, r.PartitionedFrame);
	}
	
	public static int zclSize(TransferPartitionedFrameCommand r) throws ZclValidationException {
		int size = 1 + ((r.PartitionIndicator > 0xFF) ? 2 : 1) + 1 + r.PartitionedFrame.length;
		return size;
	}
}