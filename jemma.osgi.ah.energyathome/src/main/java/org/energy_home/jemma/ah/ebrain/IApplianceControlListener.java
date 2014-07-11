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
package org.energy_home.jemma.ah.ebrain;


public interface IApplianceControlListener {
	public static final short STATUS_OFF = 0x1;
	public static final short STATUS_STAND_BY = 0x2;
	public static final short STATUS_PROGRAMMED = 0x3;
	public static final short STATUS_PROGRAMMED_WAITING_TO_START = 0x4;
	public static final short STATUS_RUNNING = 0x5;
	public static final short STATUS_PAUSED = 0x6;
	public static final short STATUS_END_PROGRAMMED = 0x7;
	public static final short STATUS_FAILURE = 0x8;
	public static final short STATUS_PROGRAM_INTERRUPTED = 0x9;
	public static final short STATUS_IDLE = 0xA;
	public static final short STATUS_RINSE_HOLD = 0xB;
	public static final short STATUS_SERVICE = 0xC;
	public static final short STATUS_SUPERFREEZING = 0xD;
	public static final short STATUS_SUPERHEATING = 0xF;
	

	/* only bits 0..3 should be checked so, instead of doing
	 * if (status == REMOTE_ENABLED_REMOTE_AND_ENERGY_CONTROL) ...
	 * should instead be
	 * if ((status & REMOTE_ENABLED_REMOTE_AND_ENERGY_CONTROL) != 0) ...
	 */
	public static final short REMOTE_FLAGS_DISABLED = 0;
	public static final short REMOTE_FLAGS_ENABLED_REMOTE_AND_ENERGY_CONTROL = 0x1;
	public static final short REMOTE_FLAGS_TEMPORARILY_DISABLED = 0x7;
	public static final short REMOTE_FLAGS_ENABLED_REMOTE_CONTROL = 0xF;
	
	// ApplianceStatus represents the current status of household appliance.
	// RemoteEnableFlags is updated continuously when appliance state remote-controllability changes.
	// ApplianceStatus2 contains non-standardized or proprietary data.
	void notifyApplianceState(String applianceId, short applianceStatus, short remoteEnableFlags, int applianceStatus2);
}
