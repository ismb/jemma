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

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.osgi.framework.InvalidSyntaxException;

public class CmaCommandProvider implements CommandProvider {

	private String defaultAppliancePid = null;
	private IConnectionAdminService connectionAdminService = null;

	public synchronized void setConnectionAdminService(IConnectionAdminService s) {
		this.connectionAdminService = s;
	}

	public synchronized void unsetConnectionAdminService(IConnectionAdminService s) {
		if (this.connectionAdminService == s) {
			this.connectionAdminService = null;
		}
	}

	public void _cma(CommandInterpreter ci) {
		if (!checkConnectionAdminService(ci))
			return;

		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_cma_" + command, new Class[] { CommandInterpreter.class });
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

	public void _cma_list(CommandInterpreter ci) {
		ArrayList bindRules = this.connectionAdminService.getBindRules();

		if (bindRules.size() == 0) {
			ci.println("Rules: <none>");
			return;
		} else {
			ci.println("Rule pid\tRule");
			ci.println("--------\t--------------------------------");
		}

		for (int i = 0; i < bindRules.size(); i++) {
			ci.println(i + "\t\t" + bindRules.get(i));
		}
	}

	public void _cma_del(CommandInterpreter ci) {
		String rulePid = ci.nextArgument();
		if (rulePid == null) {
			this.printUsage(ci);
			return;
		}
		try {
			this.connectionAdminService.removeBindRule(rulePid);
		} catch (HacException e) {
			ci.println(e.getMessage());
		}
	}

	public void _cma_add(CommandInterpreter ci) {
		String rule = ci.nextArgument();
		if (rule == null) {
			this.printUsage(ci);
			return;
		}
		try {
			this.connectionAdminService.addBindRule(rule);
		} catch (InvalidSyntaxException e) {
			ci.println("invalid syntax");
		}

	}

	private boolean checkConnectionAdminService(CommandInterpreter ci) {
		if (this.connectionAdminService == null) {
			ci.print("ConnectionAdminService not started");
			return false;
		}

		return true;
	}

	void printConnections(CommandInterpreter ci, String appliancePid) {
		if (!checkConnectionAdminService(ci))
			return;
		String[] peerAppliancesPids;
		try {
			peerAppliancesPids = this.connectionAdminService.getPeerAppliancesPids(appliancePid);
		} catch (HacException e) {
			ci.print("the appliance seems unconnected");
			return;
		}
		ci.print("connected with [ ");
		if (peerAppliancesPids != null) {
			for (int i = 0; i < peerAppliancesPids.length; i++) {
				ci.print(peerAppliancesPids[i] + ", ");
			}
		}
		ci.print("]");
		return;
	}

	public void _connect(CommandInterpreter ci) {
		if (!checkConnectionAdminService(ci))
			return;

		String appliance1Pid = ci.nextArgument();
		String appliance2Pid = ci.nextArgument();

		if ((appliance1Pid != null) && (appliance2Pid == null)) {
			this.printConnections(ci, appliance1Pid);
			return;
		}

		if ((appliance1Pid == null) && (appliance2Pid == null)) {
			((ConnectionAdminService) this.connectionAdminService).dumpConnections();
			return;
		}

		try {
			this.connectionAdminService.createConnection(appliance1Pid, appliance2Pid);
			ci.println("Appliances connected successfully");
		} catch (ApplianceException e) {
			ci.println("Error connecting appliances");
		}
	}

	public void _disconnect(CommandInterpreter ci) {
		if (!checkConnectionAdminService(ci))
			return;

		String appliance1Pid = ci.nextArgument();
		String appliance2Pid = ci.nextArgument();

		if (appliance2Pid == null) {
			// only one parameter
			if (this.defaultAppliancePid != null) {
				appliance2Pid = appliance1Pid;
				appliance1Pid = this.defaultAppliancePid;
			} else {
				ci.println("implicit appliance not set: use setapp command.");
				return;
			}
		}

		if ((appliance1Pid != null) && (appliance2Pid != null)) {
			try {
				this.connectionAdminService.deactivateBind(appliance1Pid, appliance2Pid);
				ci.println("Connection between '" + appliance1Pid + "' and '" + appliance2Pid + "' deactivated successfully.");
				return;
			} catch (Exception e) {
				ci.println("Error deactivating connnection");
				return;
			}
		}
	}

	private static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}

	protected void printArray(CommandInterpreter ci, Object[] array) {
		ci.println("[");
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				ci.println("	" + array[i] + ", ");
			}
		}
		ci.println("]");
	}

	public String getHelp() {
		String help = "---Automation@Home Connecion Manager---\n";
		help += "\tcma list list the currently active connection rules\n";
		help += "\tcma add <rule> - add an LDAP rule (the rule is applied immediately). Example: '(&(pid1=ah.app.Greenathome)(pid2=*))'\n";
		help += "\tcma del <rule id> - deletes the specified rule\n\n";

		return help;
	}

	public void printUsage(CommandInterpreter ci) {
		ci.println("Invalid syntax");
	}

	/* shortcuts */

	public void _conn(CommandInterpreter ci) {
		_connect(ci);
	}

	public void _disconn(CommandInterpreter ci) {
		_disconnect(ci);
	}
}
