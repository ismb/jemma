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
package org.energy_home.jemma.ah.hac.lib.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ApplianceFactory;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.Location;
import org.energy_home.jemma.ah.hac.lib.ext.TextConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHacService implements IHacService, CommandProvider {
	private static final Logger LOG = LoggerFactory.getLogger(SimpleHacService.class);
	
	private AppliancesProxy appliancesProxy = null;
	
	SimpleHacService(AppliancesProxy appliancesProxy) {
		this.appliancesProxy = appliancesProxy;
	}
	
	public boolean removeAppliance(String appliancePid) {
		ApplianceFactory factory = appliancesProxy.getApplianceFactory(appliancePid);
		if (factory == null) {
			LOG.debug("deleteAppliance error: no factory available for appliance " + appliancePid);
			return false;
		}
		factory.deleteAppliance(appliancePid, true);
		return true;
	}

	public Vector getAppliances() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector browseAppliances(int key_type, String key_value) {
		// TODO Auto-generated method stub
		return null;
	}

	public void clean() {
		// TODO Auto-generated method stub
		
	}

	public boolean reset(int level) {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getInquiredAppliances() {
		// TODO Auto-generated method stub
		return null;
	}

	public void installAppliance(String appliancePid, Dictionary props) throws HacException {		
	}

	public void enableAppliance(String appliancePid) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public void updateAppliance(String appliancePid, Dictionary props) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public void createAppliance(String appliancePid, Dictionary props) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public ILocation[] getLocations() {
		// TODO Auto-generated method stub
		return null;
	}

	public ICategory[] getCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	public ILocation getLocation(String appliancePid) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICategory getCategory(String appliancePid) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addCategory(ICategory category) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public void removeCategory(String categoryPid) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public Location addLocation(Location location) throws HacException {
		// TODO Auto-generated method stub
		return null;
	}

	public Dictionary getManagedConfiguration(String appliancePid) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNetworkOpen(String networkType) throws HacException {
		// TODO Auto-generated method stub
		return false;
	}

	public void openNetwork(String networkType) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public void openNetwork(String networkType, int duration) throws HacException {
		// TODO Auto-generated method stub
		
	}

	public void closeNetwork(String networkType) throws HacException {
		// TODO Auto-generated method stub
		
	}

	// HAC Command provider
	
	private String defaultAppliancePid = null;
	
	private static String invokeClusterMethod(IAppliancesProxy proxy, String appliancePid, Integer endPointId, String clusterName,
			String methodName, String[] params) {
		try {
			Object[] objectParams = TextConverter.getObjectParameters(Class.forName(clusterName), methodName, params,
					proxy.getRequestContext(true));
			return TextConverter.getTextRepresentation(proxy.invokeClusterMethod(appliancePid, endPointId, clusterName, methodName,
					objectParams));
		} catch (Throwable t) {
			LOG.debug(t.getMessage(), t);
			return TextConverter.getTextRepresentation(new Exception(t));
		}
	}
	
	public void _hac(CommandInterpreter ci) {
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

	
	
	public void _lsapp(CommandInterpreter ci) {
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

		ci.println("\n*** Installed appliances:");
		List appliances = appliancesProxy.getAppliances();
		for (Iterator iterator = appliances.iterator(); iterator.hasNext();) {
			IAppliance appliance = (IAppliance) iterator.next();
			if (printFullInfo) {
				ci.println("");
				printApplianceDetails(ci, appliance);
			} else {
				ci.println("\t" + appliance.getPid());	
			}
		}

		if (addInstallingAppliances) {
			ci.println("\n*** Installing appliances:");
			List installingAppliances = appliancesProxy.getInstallingAppliances();
			for (Iterator iterator = installingAppliances.iterator(); iterator.hasNext();) {
				IAppliance appliance = (IAppliance) iterator.next();
				if (printFullInfo) {
					ci.println("");
					printApplianceDetails(ci, appliance);
				} else { 
					ci.println("\t" + appliance.getPid());
				}
			}
		}

	}

	public void _lsapptypes(CommandInterpreter ci) {
		String[] factoryTypes = appliancesProxy.getApplianceFactoryTypes();
		ci.println("[");
		for (int i = 0; i < factoryTypes.length; i++) {
			ci.println("\t" + factoryTypes[i]);
		}

		ci.println("]");
	}

	public void _deleteapp(CommandInterpreter ci) {
		String appliancePid = ci.nextArgument();

		if (appliancePid == null) {
			printUsage(ci);
			return;
		}

		try {
			boolean result = removeAppliance(appliancePid);
			if (result)
				ci.println("Appliance with pid '" + appliancePid + "' deleted successfully.");
			else
				ci.println("Unable to delete appliance '" + appliancePid + "'. It doesn't exist or it is a singleton.");

		} catch (Exception e) {
			ci.println("Error '" + e.getMessage() + "' while deleting the appliance.");
			return;
		}
	}

	private static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}

	public void _setapp(CommandInterpreter ci) {
		String newDefaultAppliancePid = ci.nextArgument();

		if (newDefaultAppliancePid == null) {
			ci.println("default appliance Pid is " + defaultAppliancePid);
			if (defaultAppliancePid == null)
				return;
			IAppliance appliance = (IAppliance) appliancesProxy.getAppliance(defaultAppliancePid);
			IEndPoint[] endPoints = appliance.getEndPoints();
			for (int i = 0; i < endPoints.length; i++) {
				IEndPoint endPoint = endPoints[i];
				String[] serviceClustersNames = endPoint.getServiceClusterNames();
				printArray(ci, serviceClustersNames);
			}
		} else
			defaultAppliancePid = newDefaultAppliancePid;
	}

	private void printApplianceDetails(CommandInterpreter ci, IAppliance appliance) {
		if (appliance == null) {
			ci.println("Appliance not found");
			return;
		}
		IEndPoint commonEndPoint = appliance.getEndPoint(0);
		if (commonEndPoint != null) {
			ConfigServer configServer = (ConfigServer) commonEndPoint.getServiceCluster(ConfigServer.class.getName());

			try {
				ci.println("Appliance " + appliance.getPid() + " [\n\tname = " + configServer.getName(null)
						+ "\n\tcategory = " + configServer.getCategoryPid(null)
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
	
	public void _appliance(CommandInterpreter ci) {
		String appliancePid = ci.nextArgument();
		if (appliancePid == null)
			appliancePid = defaultAppliancePid;
		if (appliancePid != null) {
			IAppliance appliance = (IAppliance) appliancesProxy.getAppliance(appliancePid);
			printApplianceDetails(ci, appliance);
		}
	}

	public void _invoke(CommandInterpreter ci) {
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
		String clusterName = ci.nextArgument();
		String attributeName = ci.nextArgument();
		if (attributeName == null) {
			return;
		}
		String result = TextConverter.getTextRepresentation(appliancesProxy.getAttributeSubscription(defaultAppliancePid, null,
				clusterName, attributeName));
		ci.println(result);
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
		help += "\thac appliance <appliance pid> - prints all available information about the specified component\n";
		help += "\thac setapp <appliance pid> - set the current appliance\n";
		help += "\thac invoke <cluster name> <mehod name> <parameters list> - invoke a cluster method of the current appliance\n";
		help += "\thac subscribe <cluster name> <attribute name> <min> <max> <change> - subscribe a cluster attribute of the current appliance\n";
		help += "\thac substatus <cluster name> <attribute name> - return current subscription pareamters for a cluster attribute of the current appliance\n";
		return help;
	}

	public void printUsage(CommandInterpreter ci) {
		ci.println("Invalid syntax");
	}

}
