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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.io.File;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Led {

	private static final Logger LOG = LoggerFactory.getLogger(Led.class);
	
	/**
	 * Interface to the led system of the SheevaPlug
	 * 
	 * @param led
	 *            1 means heartbeat
	 * 
	 * @return true if successfully set, false in case of error.
	 */

	static boolean setLed(int led) {
		String osName = System.getProperty("os.name");

		if (osName.equals("Linux")) {
			File ledFile = new File("/sys/class/leds/plug:green:health/trigger");

			if (ledFile.exists() && ledFile.isFile()) {
			} else {
				LOG.debug("The led file doesn't exist");
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
				LOG.debug("setting led " + e.getMessage());
			}
		}
		return false;
	}
}
