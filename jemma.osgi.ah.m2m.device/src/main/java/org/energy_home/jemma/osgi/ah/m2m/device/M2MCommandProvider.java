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
package org.energy_home.jemma.osgi.ah.m2m.device;

import java.lang.reflect.Method;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.lib.M2MDeviceObject;
import org.energy_home.jemma.m2m.connection.ConnectionParameters;

public class M2MCommandProvider implements CommandProvider {

	private M2MDeviceObject device;

	public M2MCommandProvider(M2MDeviceObject device) {
		this.device = device;
	}

	public void _m2m(CommandInterpreter ci) {
		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			ci.println("Invalid m2m command");
			return;
		} catch (NoSuchMethodException e) {
			ci.println("Invalid m2m command");
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void _info(CommandInterpreter ci) {
		M2MDeviceConfig config = device.getConfiguration();
		ConnectionParameters connParams = device.getCurrentConnectionParameters();
		ci.print("Configuration: ");
		if (config == null)
			ci.println("No connection id configured");
		else {
			ci.println("Configuration:\n" + config.getProperties());
		}
		ci.println("Status: started=" + device.isStarted() + ", connected=" + device.isConnected());
		ci.println((connParams == null) ? "" : connParams.toXmlFormattedString());
	}

	public void _did(CommandInterpreter ci) {
		String cid = ci.nextArgument();
		if (cid == null) {
			ci.println("Invalid user identifier");
			return;
		}
		String token = ci.nextArgument();
		try {
			M2MDeviceConfig config = device.getConfiguration();
			config.setDeviceId(cid);
			if (token != null) {
				if (token.equals("/"))
					config.setDeviceToken(null);
				else
					config.setDeviceToken(token);
			}
			device.setConfiguration(config);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void _del(CommandInterpreter ci) {
		try {
			device.setConfiguration(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getHelp() {
		String help = "--- Automation@Home - M2M Device Service ---\n";
		help += "\tm2m info - print m2m device configuration and status information\n";
		help += "\tm2m did <device identifier> <device token> - update the configuration with the specified m2m device identifier (required) and device token (optional, use '/' char to reset to default password)\n";
		help += "\tm2m del - delete configuration files and restore default configuration parameters\n";
		return help;
	}

}
