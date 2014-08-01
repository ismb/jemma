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

public class DEFTR {

	public short Acknowledge;
	public short FrameHeader;
	public short FrameType;
	public byte[] FramePayload;

	public DEFTR() {
	}

	public DEFTR(short Acknowledge, short FrameHeader, short FrameType, byte[] FramePayload) {
		this.Acknowledge = Acknowledge;
		this.FrameHeader = FrameHeader;
		this.FrameType = FrameType;
		this.FramePayload = FramePayload;
	}

}
