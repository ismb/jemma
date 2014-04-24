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
package org.energy_home.jemma.ah.io;

import java.lang.reflect.Method;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.io.flexgateway.FlexGatewayLed2;

public class CedacIOConsole implements CommandProvider {

	public void _setled(CommandInterpreter ci) {
		String ledNumber = ci.nextArgument();
		String cmd = "";
		String command = ci.nextArgument();
		while (command != null) {
			cmd += command + " ";
			command = ci.nextArgument();
		}

		System.out.println(ledNumber + " " + cmd);
		FlexGatewayLed2.setLedOnCedac(Integer.parseInt(ledNumber), cmd);
	}

	public String getHelp() {
		String help = "---CedacIO---\n";
		help += "\tio setled <led number> <command>\n";
		return help;
	}

	public void _io(CommandInterpreter ci) {
		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			return;
		} catch (NoSuchMethodException e) {
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			return;
		}
	}
}
