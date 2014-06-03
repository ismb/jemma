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
package org.energy_home.jemma.ah.internal.configurator;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.configurator.IConfigurator;

public class ConfiguratorCommandProvider implements CommandProvider {
	IConfigurator configurator = null;

	protected void setConfigurator(IConfigurator configurator) {
		this.configurator = configurator;
	}

	protected void unsetConfigurator(IConfigurator configurator) {
		if (this.configurator == configurator) {
			this.configurator = null;
		}
	}

	public String getHelp() {
		String help = "";
		help += "---Configurator Commands---\n";
		help += "\tcfgload [<configuration>] - loads a configuration. The default configuartion is 'EmptyConfig'\n";
		return help;
	}

	public void _cfgload(CommandInterpreter ci) {
		synchronized (this) {
			String name = ci.nextArgument();
			if (name == null) {
				name = "EmptyConfig";
			}
			try {
				configurator.loadConfiguration(name);
			} catch (Exception e) {
				ci.println(e.getMessage());
			}
		}
	}
}
