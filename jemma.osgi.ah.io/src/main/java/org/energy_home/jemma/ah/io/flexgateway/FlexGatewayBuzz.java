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
package org.energy_home.jemma.ah.io.flexgateway;


import java.io.File;
import java.io.FileWriter;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.ah.io.Color;
import org.energy_home.jemma.ah.io.PlatformsUtil;

public class FlexGatewayBuzz {

	protected static final Log log = LogFactory.getLog(FlexGatewayBuzz.class);
	protected static String target = "cedac";
	protected static String cedacBuzzFolder = "/sys/devices/platform/flex_hmi.0";
	protected static int RED_COLOR = 0;
	protected static int GREEN_COLOR = 1;
	protected static int BLUE = 2;

	/**
	 * Write the right command to the buzz
	 * 
	 * @param value
	 *            The command:
	 * @return
	 */

	public static boolean setBuzzOnCedac(String value) {

		if (PlatformsUtil.getOS().equals("linux")) {
			File cmdBuzzFile = null;

			cmdBuzzFile = new File(cedacBuzzFolder + "/buzz_cmd");

			if (cmdBuzzFile.exists() && cmdBuzzFile.isFile()) {
			} else {
				log.debug("The buzz file doesn't exist");
				return false;
			}

			try {
				FileWriter out = new FileWriter(cmdBuzzFile);
				out.write(value + "\n");
				out.close();
				return true;

			} catch (Exception e) {
				log.error("setting buzz " + e.getMessage());
			}
		}
		return false;
	}
	
	public static boolean cmdStartBuzzOnCedac() {
		
		setBuzzOnCedac("start");
		
		return false;
	}
	
	public static boolean cmdStopBuzzOnCedac() {
		
		setBuzzOnCedac("stop");
		
		return false;
	}
	
	public static boolean cmdStartBeepBuzzOnCedac() {
		
		setBuzzOnCedac("start_beep");
		
		return false;
	}
	
	public static boolean cmdStopBeepBuzzOnCedac() {
		
		setBuzzOnCedac("stop_beep");
		
		return false;
	}

	
	public static boolean cmdStartBeepPeriodBuzzOnCedac(int value) {
		
		setBuzzOnCedac("period " + value);
		setBuzzOnCedac("start");
		
		return false;
	}
}
