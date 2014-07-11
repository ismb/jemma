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
package org.energy_home.jemma.ah.internal.io.sheeva;

import java.io.File;
import java.io.FileWriter;

import org.energy_home.jemma.ah.io.PlatformsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Led {

	private static final Logger LOG = LoggerFactory.getLogger( Led.class );
	protected static String target = "cedac";
	protected static String cedacLedFolder = "/sys/devices/platform/flex_hmi.0";
	protected static int RED_COLOR = 0;
	protected static int GREEN_COLOR = 1;
	protected static int BLUE = 2;

	/**
	 * Interface to the led system of the SheevaPlug
	 * 
	 * @param led
	 *            1 means heartbeat
	 * 
	 * @return true if successfully set, false in case of error.
	 */

	static boolean setLedOnSheeva(int led) {
		if (PlatformsUtil.getOS().equals("linux")) {
			File ledFile = null;

			if (target.equals("sheevaplug"))
				ledFile = new File("/sys/class/leds/plug:green:health/trigger");
			else if (target.equals("cedac"))
				ledFile = new File("/sys/devices/platform/flex_led.0/cmd");
			else
				return false;

			if (ledFile.exists() && ledFile.isFile()) {
			} else {
				LOG.warn("The led file doesn't exist");
				return false;
			}

			try {
				FileWriter out = new FileWriter(ledFile);
				String value = "";
				if (led == 0) {
					value = "none";
				} else if (led == 1) {
					value = "heartbeat";
				}

				out.write(value);
				out.close();
				return true;

			} catch (Exception e) {
				LOG.error("Exception setting led " + e.getMessage(),e);
			}
		}
		return false;
	}
}
