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

public interface IApplianceControlProxy {

	//------------------------------------------------------------------------------------------------------------
	// ATTRIBUTE & COMMANDS for ApplianceControl Cluster Server
	//------------------------------------------------------------------------------------------------------------
	public static final short OVERALL_POWER_ABOVE_AVAILABLE_POWER_LEVEL = 0;
	public static final short OVERALL_POWER_ABOVE_POWER_THRESHOLD_LEVEL = 1;
	public static final short OVERALL_POWER_BACK_BELOW_AVAILABLE_POWER_LEVEL = 2;
	public static final short OVERALL_POWER_BACK_BELOW_POWER_THRESHOLD_LEVEL = 3;
	public static final short OVERALL_POWER_POTENTIALLY_ABOVE_AVAILABLE_POWER_LEVEL_ON_START = 4;

	//------------------------------------------------------------------------------------------------------------
	// GET&SET Attributes that are proxied to the remote zigbee device
	//------------------------------------------------------------------------------------------------------------
	int getStartTime(String applianceId);

	int getFinishTime(String applianceId);

	int getRemainingTime(String applianceId);

	//------------------------------------------------------------------------------------------------------------
	// COMMANDS
	//------------------------------------------------------------------------------------------------------------
	/*	6.2.2.3.1 Execution of a Command
		This basic message is used to remotely control and to program household appliances. Examples of control are START, STOP and PAUSE. */
	void executeCommand(String applianceId, short command);

	// ApplianceCurrentState retrieveApplianceState(String applianceId);
	//void writeFunctions(String applianceId, WriteAttributeRecord[] war);
	void overloadPause(String applianceId);

	void overloadResume(String applianceId);

	void notifyOverloadWarning(String applianceId, short event);

}