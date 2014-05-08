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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.ah.io.CedacIO;
import org.energy_home.jemma.ah.io.Color;
import org.energy_home.jemma.ah.io.PlatformsUtil;

//FIXME highly redundant with FlexGatewayLed1 - unify ?
public class FlexGatewayLed2 {

	private static final Logger LOG = LoggerFactory.getLogger( FlexGatewayLed2.class );
	protected static String target = "cedac";
	protected static String cedacLedFolder = "/sys/devices/platform/flex_hmi.0";
	protected static int RED_COLOR = 0;
	protected static int GREEN_COLOR = 1;
	protected static int BLUE = 2;

	/**
	 * Write the right command to the led 1 or 2
	 * 
	 * @param led
	 *            The led number (1 or 2)
	 * @param value
	 *            The command:
	 * @return
	 */

	public static boolean setLedOnCedac(int led, String value) {
		if (led != 1 && led != 2) {
			return false;
		}

		if (PlatformsUtil.getOS().equals("linux")) {
			File cmdLedFile = null;

			cmdLedFile = new File(cedacLedFolder + "/led" + led + "_cmd");

			if (cmdLedFile.exists() && cmdLedFile.isFile()) {
			} else {
				LOG.warn("The led file doesn't exist");
				return false;
			}

			try {
				FileWriter out = new FileWriter(cmdLedFile);
				out.write(value + "\n");
				out.close();
				return true;

			} catch (Exception e) {
				LOG.error("Exception setting led " + e.getMessage(),e);
			}
		}
		return false;
	}

	static boolean setLed1OnCedac(int color, boolean setOn, int blinkSpeed) {
		String colorName;
		if (color == RED_COLOR) {
			colorName = "red";
		} else if (color == GREEN_COLOR) {
			colorName = "green";
		} else {
			// defaults to red
			colorName = "red";
		}

		// set blink speed
		if (blinkSpeed > 0) {
			setLedOnCedac(1, "blink_on" + " " + blinkSpeed);
		} else {
			setLedOnCedac(1, "blink_off");
		}
		if (setOn)
			setLedOnCedac(1, colorName + " 255");
		else
			setLedOnCedac(1, colorName + " 0");

		return false;
	}
	
	public static boolean setRgbLedOnCedac(Color color, boolean pulseRed, boolean pulseGreen, boolean pulseBlue, int pulsePeriod, int blinkPeriod) {
		
		if (pulseBlue) {
			setLedOnCedac(2, "blue" + " " + color.getBlue());
			setLedOnCedac(2, "pulse_blue_start");
		}
		else {
			setLedOnCedac(2, "pulse_blue_stop");
			setLedOnCedac(2, "blue" + " " + color.getBlue());
		}
		if (pulseGreen) {
			setLedOnCedac(2, "green" + " " + color.getGreen());
			setLedOnCedac(2, "pulse_green_start");
		}
		else {
			setLedOnCedac(2, "pulse_green_stop");
			setLedOnCedac(2, "green" + " " + color.getGreen());
		}
		if (pulseRed) {
			setLedOnCedac(2, "red" + " " + color.getRed());
			setLedOnCedac(2, "pulse_red_start");
		}
		else {
			setLedOnCedac(2, "pulse_red_stop");
			setLedOnCedac(2, "red" + " " + color.getRed());
		}
		
		if (blinkPeriod > 0) {
			setLedOnCedac(2, "blink_on " + blinkPeriod);
		}
		else {
			setLedOnCedac(2, "blink_off");
		}
		
		return false;
	}
}
