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
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceFactory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ext.Category;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.TextConverter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.device.DriverLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HacCommandProvider implements CommandProvider {
	private IHacService hacService = null;
	private String defaultAppliancePid = null;
	private HacDriverLocator hacLocator = null;

	private IAppliancesProxy appliancesProxy;
	
	private static final Logger LOG = LoggerFactory.getLogger(HacCommandProvider.class);

	private static String invokeClusterMethod(IAppliancesProxy proxy, String appliancePid, Integer endPointId, String clusterName,
			String methodName, String[] params) {
		try {
			Object[] objectParams = TextConverter.getObjectParameters(Class.forName(clusterName), methodName, params,
					proxy.getRequestContext(true));
			return TextConverter.getTextRepresentation(proxy.invokeClusterMethod(appliancePid, endPointId, clusterName, methodName,
					objectParams));
		} catch (Throwable t) {
			LOG.warn(t.getMessage());
			return TextConverter.getTextRepresentation(new Exception(t));
		}
	}

	public synchronized void setHacService(IHacService s) {
		this.hacService = s;
	}

	public synchronized void unsetHacService(IHacService s) {
		if (this.hacService == s) {
			this.hacService = null;
		}
	}

	public synchronized void setAppliancesProxy(IAppliancesProxy s) {
		this.appliancesProxy = s;
	}

	public synchronized void unsetAppliancesProxy(IAppliancesProxy s) {
		if (this.appliancesProxy == s) {
			this.appliancesProxy = null;
		}
	}

	public synchronized void setHacDeviceLocator(DriverLocator s) {
		if (s instanceof HacDriverLocator)
			this.hacLocator = (HacDriverLocator) s;
	}

	public synchronized void unsetHacDeviceLocator(DriverLocator s) {
		if (this.hacLocator == s) {
			this.hacLocator = null;
		}
	}

	public void _hac(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

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

	public void _hacl(CommandInterpreter ci) {
		if (!checkHacLocator(ci))
			return;

		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_hacl_" + command, new Class[] { CommandInterpreter.class });
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

	public void _lsapp(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		boolean printFullInfo = false;
		boolean addInstallingAppliances = false;

		String option;
		while ((option = ci.nextArgument()) != null) {
			if (option.equals("-c")) {
				printFullInfo = true;
			}
			if (option.equals("-a")) {
				addInstallingAppliances = true;
			}
		}

		if (!printFullInfo) {
			ci.println("[");
			Vector appliances = hacService.getAppliances();

			for (int i = 0; i < appliances.size(); i++) {
				String applianceName = (String) appliances.get(i);
				ci.println("\t" + applianceName);
			}

			if (addInstallingAppliances) {
				String[] pids = hacService.getInquiredAppliances();

				for (int j = 0; j < pids.length; j++) {
					IManagedAppliance appliance = ((HacService) hacService).getAppliance(pids[j]);
					if (appliance != null)
						ci.println("\t" + appliance.getPid() + " (not installed)");
				}
			}
			ci.println("]");
		}

	}

	public void _lsapptypes(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		Vector factories = ((HacService) hacService).getFactories();
		ci.println("[");
		for (int i = 0; i < factories.size(); i++) {
			IApplianceFactory factoryType = (IApplianceFactory) factories.get(i);
			ci.println("\t" + factoryType.getName());
		}

		ci.println("]");
	}

	private boolean checkHacService(CommandInterpreter ci) {
		if (hacService == null) {
			ci.print("Home Automation Core not active.");
			return false;
		}

		return true;
	}

	private boolean checkAppliancesProxy(CommandInterpreter ci) {
		if (appliancesProxy == null) {
			ci.print("Appliances Proxy not active.");
			return false;
		}

		return true;
	}

	private boolean checkHacLocator(CommandInterpreter ci) {
		if (hacLocator == null) {
			ci.print("Hac Locator not active.");
			return false;
		}

		return true;
	}

	public void _instapp(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String appliancePid = ci.nextArgument();

		if (appliancePid == null) {
			ci.println("You must specify at least an appliance pid");
		}

		try {
			((HacService) hacService).installAppliance(appliancePid);
		} catch (HacException e) {
			ci.println("Error '" + e.getMessage() + "' while installing an appliance");
			return;
		}
	}

	public void _createapp(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String applianceType = ci.nextArgument();
		String applianceName = ci.nextArgument();

		if (applianceType == null) {
			printUsage(ci);
			return;
		}

		Hashtable props = new Hashtable();
		if (applianceName != null) {
			props.put(IAppliance.APPLIANCE_NAME_PROPERTY, applianceName);
		}

		String pid = null;

		try {
			pid = ((HacService) hacService).createApplianceByFactory(applianceType, props);
		} catch (HacException e) {
			ci.println("Error '" + e.getMessage() + "' while creating appliance");
			return;
		}

		ci.println("Appliance with pid '" + pid + "' created successfully");
		//
		// if (appliances.size() == 0) {
		// ci.println("Appliance with '" + pid + "' created successfully");
		// }
		// else {
		// IAppliance name = (IAppliance) appliances.get(0);

		// }
	}

	public void _deleteapp(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String appliancePid = ci.nextArgument();

		if (appliancePid == null) {
			printUsage(ci);
			return;
		}

		try {
			boolean result = ((HacService) hacService).removeAppliance(appliancePid);
			if (result)
				ci.println("Appliance with pid '" + appliancePid + "' deleted successfully.");
			else
				ci.println("Unable to delete appliance '" + appliancePid + "'. It doesn't exist or it is a singleton.");

		} catch (Exception e) {
			ci.println("Error '" + e.getMessage() + "' while deleting the appliance.");
			return;
		}

	}

	public void _opennwk(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String networkType = ci.nextArgument();
		if (isNullOrEmpty(networkType))
			networkType = "ZigBee";
		try {
			hacService.openNetwork(networkType);
			ci.println("Network " + networkType + " opened");
		} catch (Exception e) {
			ci.println("Error '" + e.getMessage() + "' while opening network " + networkType);
			return;
		}

	}

	private static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}

	public void _closenwk(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String networkType = ci.nextArgument();
		if (isNullOrEmpty(networkType))
			networkType = "ZigBee";

		try {
			hacService.closeNetwork(networkType);
			ci.println("Network " + networkType + " closed");
		} catch (Exception e) {
			ci.println("Error '" + e.getMessage() + "' while closing network " + networkType);
			return;
		}
	}

	public void _setapp(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String newDefaultAppliancePid = ci.nextArgument();

		if (newDefaultAppliancePid == null) {
			ci.println("default appliance Pid is " + defaultAppliancePid);
			Vector v = hacService.browseAppliances(IAppliance.APPLIANCE_PID_PROPERTY_KEY, defaultAppliancePid);
			if (v != null && v.size() > 0) {
				IAppliance appliance = (IAppliance) v.get(0);
				IEndPoint[] endPoints = appliance.getEndPoints();
				for (int i = 0; i < endPoints.length; i++) {
					IEndPoint endPoint = endPoints[i];
					String[] serviceClustersNames = endPoint.getServiceClusterNames();
					printArray(ci, serviceClustersNames);
				}
			}
		} else
			defaultAppliancePid = newDefaultAppliancePid;
	}

	public void _appliance(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String appliancePid = ci.nextArgument();
		if (appliancePid == null)
			appliancePid = defaultAppliancePid;
		if (appliancePid != null) {
			Vector v = hacService.browseAppliances(IAppliance.APPLIANCE_PID_PROPERTY_KEY, appliancePid);
			if (v != null && v.size() > 0) {
				IAppliance appliance = (IAppliance) v.get(0);

				IEndPoint commonEndPoint = appliance.getEndPoint(0);
				if (commonEndPoint != null) {
					ConfigServer configServer = (ConfigServer) commonEndPoint.getServiceCluster(ConfigServer.class.getName());

					try {
						ci.println("Appliance " + appliance.getPid() + " [\n\tname = " + configServer.getName(null)
								+ "\n\tlocation = " + configServer.getLocationPid(null) + "\n]");
					} catch (Exception e) {
					}
				}
				IEndPoint[] endPoints = appliance.getEndPoints();
				for (int i = 0; i < endPoints.length; i++) {
					IEndPoint endPoint = endPoints[i];
					String[] serviceClustersNames = endPoint.getServiceClusterNames();
					ci.println("EndPoint " + endPoint.getId() + (endPoint.isAvailable() ? " (available)" : "") + " - "
							+ " of type " + endPoint.getType() + " ");

					printArray(ci, serviceClustersNames);
				}
			}
		}
	}

	public void _invoke(CommandInterpreter ci) {
		if (!checkAppliancesProxy(ci))
			return;

		String clusterName = ci.nextArgument();
		String methodName = ci.nextArgument();
		ArrayList alParams = new ArrayList();
		String param = null;
		while ((param = ci.nextArgument()) != null) {
			alParams.add(param);
		}
		String[] params = new String[alParams.size()];
		alParams.toArray(params);

		String result = invokeClusterMethod(appliancesProxy, defaultAppliancePid, null, clusterName, methodName, params);
		ci.println(result);

	}

	public void _subscribe(CommandInterpreter ci) {
		if (!checkAppliancesProxy(ci))
			return;

		String clusterName = ci.nextArgument();
		String attributeName = ci.nextArgument();
		String minString = ci.nextArgument();
		String maxString = ci.nextArgument();
		String repChangeString = ci.nextArgument();
		double repChange = 1;
		if (repChangeString != null) {
			repChange = Double.parseDouble(repChangeString);
		}
		ISubscriptionParameters parameters = null;
		if (minString != null && maxString != null && repChangeString != null)
			parameters = new SubscriptionParameters(Long.parseLong(minString), Long.parseLong(maxString), repChange);
		String result = TextConverter.getTextRepresentation(appliancesProxy.setAttributeSubscription(defaultAppliancePid, null,
				clusterName, attributeName, parameters));
		ci.println(result);
	}

	public void _substatus(CommandInterpreter ci) {
		if (!checkAppliancesProxy(ci))
			return;

		String clusterName = ci.nextArgument();
		String attributeName = ci.nextArgument();
		if (attributeName == null) {
			return;
		}
		String result = TextConverter.getTextRepresentation(appliancesProxy.getAttributeSubscription(defaultAppliancePid, null,
				clusterName, attributeName));
		ci.println(result);
	}

	public synchronized void _load(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String configFilename = ci.nextArgument();

		boolean result = ((HacService) hacService).loadConfiguration(configFilename, false);

		if (result)
			ci.println("Configuration loaded successfully");
		else
			ci.println("Error loading configuration");
	}

	public void _save(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		boolean result = ((HacService) hacService).saveConfiguration();

		if (result)
			ci.println("Configuration saved successfully");
		else
			ci.println("Error saving configuration");
	}

	public void _reset(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String arg = ci.nextArgument();
		if (arg == null) {
			ci.println("Missing reset type argument");
		}

		int level = 0;
		if (arg.equals("all")) {
			level = 0;
		} else {
			ci.println("wrong argument for reset (accepted 'all')");
			return;
		}

		boolean result = ((HacService) hacService).reset(level);

		if (result)
			ci.println("Reset issued successfully");
		else
			ci.println("Error resetting");
	}

	public void _hacl_find(CommandInterpreter ci) {
		String filter = ci.nextArgument();
		try {
			this.hacLocator.resolveBundles(filter);
		} catch (InvalidSyntaxException e) {
			ci.println("Invalid Sintax in LDAP expression");
		}
	}

	public void _addcategory(CommandInterpreter ci) {
		if (!checkHacService(ci))
			return;

		String pid = ci.nextArgument();
		if (pid == null) {
			ci.println("Missing pid argument");
			return;
		}

		String name = ci.nextArgument();
		if (name == null) {
			ci.println("Missing name argument");
			return;
		}

		String icon = ci.nextArgument();
		if (icon == null) {
			ci.println("Missing icon argument");
			return;
		}

		Category category = new Category(pid, name, icon);
		try {
			hacService.addCategory(category);
		} catch (HacException e) {
			ci.println("Error adding category '" + e.getMessage() + "'");
			return;
		}

		ci.println("category added successfully");
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
		String help = "---Automation@Home Home Automation Core---\n";
		help += "\thac lsapp [-c][-a] - list the appliances; add -c to display the complete info for each appliance;\n"
				+ "\t\tadd -a to display also those appliances not installed yet\n";
		help += "\thac lsapptypes - list the available appliance types\n";
		// help += "\thac save - save the current configuration permanently\n";
		// help += "\thac load - load a configuration\n";
		// help += "\thac listconf - load a configuration\n";
		help += "\thac createapp <appliance type> [ <appliance name> ] - create an appliance. If the appliance name is omitted this is assigned automatically by the system\n";
		help += "\thac deleteapp <appliance pid> - remove the specified appliance\n";
		help += "\thac instapp <appliance pid> - installs the appliance. The appliance must be in the 'installing' state.\n";
		help += "\thac opennwk <network type> - open the specified network (currently only 'ZigBee' netwokr type is supported)\n";
		help += "\thac closenwk <network type> - cloe the specified network to enable aspecified by the (currently only 'ZigBee' netwokr type is supported)\n";
		help += "\thac reset all -  to factory default\n";
		help += "\thac appliance <appliance pid> - prints all available information about the specified component\n";
		help += "\thac setapp <appliance pid> - set the current appliance\n";
		help += "\thac invoke <cluster name> <mehod name> <parameters list> - invoke a cluster method of the current appliance\n";
		help += "\thac invoke <cluster name> <mehod name> <parameters list> - invoke a cluster method of the current appliance\n";
		help += "\thac subscribe <cluster name> <attribute name> <min> <max> <change> - subscribe a cluster attribute of the current appliance\n";
		help += "\thac substatus <cluster name> <attribute name> - return current subscription pareamters for a cluster attribute of the current appliance\n";
		return help;
	}

	public void printUsage(CommandInterpreter ci) {
		ci.println("Invalid syntax");
	}
}
