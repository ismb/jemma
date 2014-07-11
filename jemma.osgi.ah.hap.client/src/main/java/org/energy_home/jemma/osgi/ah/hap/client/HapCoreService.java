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
package org.energy_home.jemma.osgi.ah.hap.client;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceListener;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceObject;
import org.energy_home.jemma.ah.m2m.device.M2MNetworkScl;
import org.energy_home.jemma.internal.ah.hap.client.HapServiceManager;
import org.energy_home.jemma.internal.ah.hap.client.Utils;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.osgi.utils.equinox.console.TestCommandProvider;
import org.energy_home.jemma.utils.xml.jaxb.DateTimeConverter;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HapCoreService extends M2MHapServiceObject implements BundleActivator, CommandProvider, IHapCoreService, M2MHapServiceListener {

	private static final Logger LOG = LoggerFactory.getLogger( HapCoreService.class );
	
	private static long REAL_START_TIME = System.currentTimeMillis();
	private static long EMULATED_START_TIME = 0;

	static {
		String startTimeStr = System.getProperty("org.energy_home.jemma.ah.test.hap.client.startTime");
		if (!Utils.isNullOrEmpty(startTimeStr)) {
			Calendar c = DateTimeConverter.parseDateTime(startTimeStr);
			EMULATED_START_TIME = c.getTimeInMillis();
		}
	}
	
	private BundleContext bc = null;
	private ServiceRegistration cmdProviderServiceRegistration = null;
	private ServiceRegistration hapBasicServiceRegistration = null;
	private ContentInstance contentInstance = null;

	private Long getInstanceId(CommandInterpreter ci) {
		String strInstanceId = ci.nextArgument();
		Long instanceId = null;
		if (strInstanceId != null) {
			if (strInstanceId.equals("LATEST"))
				instanceId = M2MNetworkScl.CONTENT_INSTANCE_LATEST_ID;
			else if (strInstanceId.equals("OLDEST"))
				instanceId = M2MNetworkScl.CONTENT_INSTANCE_OLDEST_ID;
			else
				try {
					instanceId = Long.parseLong(strInstanceId);
					if (instanceId < 0)
						instanceId = null;
				} catch (Exception e) {
					instanceId = null;
				}
		}
		return instanceId;
	}

	private long getTimestamp(CommandInterpreter ci) {
		String strTimestamp = ci.nextArgument();
		Long timestamp = null;
		if (!Utils.isNullOrEmpty(strTimestamp)) {
			try {
				timestamp = new Long(Long.parseLong(strTimestamp));
			} catch (Exception e) {
			}
		}
		if (timestamp == null) {
			timestamp = new Long(System.currentTimeMillis());
		}
		return timestamp.longValue();
	}

	private void contentInstanceStoreAttributeValue(CommandInterpreter ci, String containerUrl, ContentInstance value,
			long timestamp, boolean isBatch) throws M2MHapException {
		contentInstance.setId(new Long(timestamp));
		AHContainerAddress containerId = AHContainerAddress.getAddressFromUrl(containerUrl);
		ContentInstance result = null;
		if (isBatch)
			result = createContentInstanceBatch(containerId, value);
		else
			result = createContentInstance(containerId, value);
		if (result != null)
			ci.println(result.toXmlFormattedString());
		else
			ci.println("Some problems occurred: create request returned a null content instance");
	}

	private void contentInstancesCacheRetrieve(CommandInterpreter ci, String containerUrl) {
		AHContainerAddress containerId = null;
		try {
			containerId = AHContainerAddress.getAddressFromUrl(containerUrl);
			ContentInstanceItemsList itemsList = getCachedLatestContentItemsList(containerId);
			if (itemsList != null) {
				ci.println(itemsList.toXmlFormattedString());
				return;
			}
			ci.println("No instance retrieved");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void contentInstancesRetrieve(CommandInterpreter ci, String containerUrl, long id) {
		AHContainerAddress containerId = null;
		try {
			containerId = AHContainerAddress.getAddressFromUrl(containerUrl);
			if (!containerId.isFilterAddress()) {
				ContentInstance instance = getContentInstance(containerId, id);
				if (instance != null) {
					ci.println(instance.toXmlFormattedString());
					return;
				}
			} else {
				ContentInstanceItemsList itemsList = getContentInstanceItemsList(containerId, id);
				if (itemsList != null) {
					ci.println(itemsList.toXmlFormattedString());
					return;
				}
			}
			ci.println("No instance retrieved");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void contentInstancesQuery(CommandInterpreter ci, String containerUrl, long startInstanceId, long endInstanceId,
			int calendarField, int calendarValue, boolean isCalendarQuery) {
		AHContainerAddress containerId = null;
		try {
			containerId = AHContainerAddress.getAddressFromUrl(containerUrl);
			if (!containerId.isFilterAddress()) {
				ContentInstanceItems items = null;
				items = getContentInstanceItems(containerId, startInstanceId, endInstanceId);
				if (items != null) {
					ci.println(items.toXmlFormattedString());
					return;
				}
			} else {
				ContentInstanceItemsList itemsList = null;
				itemsList = getContentInstanceItemsList(containerId, startInstanceId, endInstanceId);
				if (itemsList != null) {
					ci.println(itemsList.toXmlFormattedString());
					return;
				}
			}
			ci.println("No instance retrieved");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void start(BundleContext bc) throws Exception {
		this.bc = bc;
		setUser(null);
		setListener(this);
		
		Hashtable props = new Hashtable();
		props.put("osgi.command.scope", "hap");
		
		cmdProviderServiceRegistration = bc.registerService(CommandProvider.class.getName(), this, props);
		hapBasicServiceRegistration = bc.registerService(IHapCoreService.class.getName(), this, null);
	}

	public synchronized void stop(BundleContext bc) throws Exception {	
		if (hapBasicServiceRegistration != null) {
			hapBasicServiceRegistration.unregister();
		hapBasicServiceRegistration = null;
		
		if (cmdProviderServiceRegistration != null)
			cmdProviderServiceRegistration.unregister();
		}
		cmdProviderServiceRegistration = null;
		
		release();
	}
	
	public synchronized void serviceReset() {
		LOG.debug("Hap service reset");
		if (hapBasicServiceRegistration != null) {
			hapBasicServiceRegistration.unregister();
		}
		hapBasicServiceRegistration = bc.registerService(IHapCoreService.class.getName(), this, null);
	}

	public void hagConnected() {
		LOG.debug("Hag connected");
	}

	public void hagDisconnected() {
		LOG.debug("Hag disconnected");
	}	
	
	public void _hap(CommandInterpreter ci) {
		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			ci.println("Invalid hap command");
			return;
		} catch (NoSuchMethodException e) {
			ci.println("Invalid hap command");
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void _c(CommandInterpreter ci) {
		boolean connected = isConnected();
		if (connected == true)
			ci.println("Connected");
		else
			ci.println("Disconnected");
	}

	public void _t(CommandInterpreter ci) {
		long t = getLastSuccessfulBatchRequestTimestamp();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(t);
		ci.println("Last successful upload time: " + DateTimeConverter.printDateTime(gc));
	}

	public void _cli(CommandInterpreter ci) {
		contentInstance = new ContentInstance();
		String className = ci.nextArgument();
		if (className == null) {
			ci.println("Invalid class name");
			return;
		}
		String param = ci.nextArgument();
		Object value = null;
		Class c = null;
		try {
			if (className.indexOf('.') < 0) {
				try {
					c = Class.forName("java.lang." + className);
				} catch (Exception e) {
					c = Class.forName("org.energy_home.jemma.ah." + className);
				}
			} else
				c = Class.forName(className);
			if (param != null)
				value = c.getConstructor(String.class).newInstance(param);
			else
				value = c.newInstance();
			if (value == null)
				throw new InvalidParameterException();
			contentInstance.setContent(value);
			ci.println("Local content instance created");
		} catch (Exception e) {
			ci.println("Invalid class name");
		}
	}

	public void _gli(CommandInterpreter ci) {
		if (contentInstance == null)
			ci.println("No local content instance  available");
		else {
			ci.println(contentInstance.toXmlFormattedString());
		}
	}

	public void _sli(CommandInterpreter ci) {
		if (contentInstance == null) {
			ci.println("You need firts to create a local content instance ");
			return;
		}
		String fieldName = ci.nextArgument();
		if (fieldName == null) {
			ci.println("Invalid field name");
			return;
		}
		String fieldValue = ci.nextArgument();
		if (Utils.isNullOrEmpty(fieldValue)) {
			ci.println("Invalid field value");
			return;
		}
		try {
			String setMethodName = "set" + fieldName;
			Class c = contentInstance.getContent().getClass();
			Object o = contentInstance.getContent();
			Class[] paramTypes = null;
			Object param = null;
			Method[] methods = c.getMethods();
			Method method = null;
			int i;
			for (i = 0; i < methods.length; i++) {
				method = methods[i];
				if (method.getName().equals(setMethodName)) {
					paramTypes = method.getParameterTypes();
					if (paramTypes.length == 1) {
						param = TestCommandProvider.getObjectValue(paramTypes[0], fieldValue);
						method.invoke(o, param);
						break;
					}
				}
			}
			if (i == methods.length)
				ci.println("Invalid field name (remenber to user inital uppercase letters)");
			else
				ci.println("Field value setted");
		} catch (Exception e) {
			ci.println("Some problem occured while setting field value");
			e.printStackTrace();
		}
	}

	public void _ci(CommandInterpreter ci) {
		String containerUrl = ci.nextArgument();
		if (containerUrl == null) {
			ci.println("Invalid container id");
			return;
		}
		long timestamp = getTimestamp(ci);
		if (contentInstance == null) {
			ci.println("No local content instance available");
			return;
		}
		try {
			contentInstanceStoreAttributeValue(ci, containerUrl, contentInstance, timestamp, false);
			ci.println("Create instance ok");
		} catch (Exception e) {
			e.printStackTrace();
			ci.println("Create instance ko");
		}

	}

	public void _cb(CommandInterpreter ci) {
		String containerUrl = ci.nextArgument();
		if (containerUrl == null) {
			ci.println("Invalid container id");
			return;
		}
		long timestamp = getTimestamp(ci);
		if (contentInstance == null) {
			ci.println("No local content instance  available");
			return;
		}
		try {
			contentInstanceStoreAttributeValue(ci, containerUrl, contentInstance, timestamp, true);
			ci.println("Batch request stored");
		} catch (Exception e) {
			e.printStackTrace();
			ci.println("Batch request ko");
		}
	}

	public void _gci(CommandInterpreter ci) {
		String containerUrl = ci.nextArgument();
		contentInstancesCacheRetrieve(ci, containerUrl);
	}

	public void _gi(CommandInterpreter ci) {
		String containerUrl = ci.nextArgument();
		if (containerUrl == null) {
			ci.println("Invalid container id");
			return;
		}
		Long instanceId = getInstanceId(ci);
		if (instanceId == null) {
			ci.println("Invalid instance id");
			return;
		}
		contentInstancesRetrieve(ci, containerUrl, instanceId.longValue());
	}

	public void _qi(CommandInterpreter ci) {
		String containerUrl = ci.nextArgument();
		if (containerUrl == null) {
			ci.println("Invalid container id");
			return;
		}

		Long startInstanceId = getInstanceId(ci);
		if (startInstanceId == null) {
			ci.println("Invalid start instance id");
			return;
		}
		Long endInstanceId = getInstanceId(ci);
		if (endInstanceId == null) {
			ci.println("Invalid end instance id");
			return;
		}
		contentInstancesQuery(ci, containerUrl, startInstanceId, endInstanceId, 0, 0, false);
	}

	public void _gff(CommandInterpreter ci) {
		int ff = HapServiceManager.getFastForwardFactor();
		ci.println("Fast forward = " + ff);
	}

	public void _sff(CommandInterpreter ci) {
		String ffStr = ci.nextArgument();
		int ff = 1;
		if (ffStr == null) {
			ci.println("A fast forward value must be specified");
		} else {
			try {
				ff = Integer.parseInt(ffStr);
				HapServiceManager.setFastForwardFactor(ff);
				ci.println("Fast forward updated " + ff);
			} catch (Exception e) {
				ci.print("Fast forward value must be a positive interger value");
			}
		}
	}

	public void _test(CommandInterpreter ci) {
		String strNow;
		long nowQuery;
		String strNowQuery;
		String strOneHourAgo;
		String strOneDayAgo;
		String strOneWeekAgo;
		String strOneMonthAgo;
		String strOneYearAgo;

		if (EMULATED_START_TIME > 0) {
			strNow = " " + (EMULATED_START_TIME + (System.currentTimeMillis() - REAL_START_TIME)); // Used
																									// to
																									// generate
																									// a
																									// unique
																									// id
			nowQuery = EMULATED_START_TIME + 86400000l * 60;
			strNowQuery = " " + nowQuery;
			strOneHourAgo = " " + (nowQuery - 3600000l + 60000l);
			strOneDayAgo = " " + (nowQuery - 86400000l + 3600000l);
			strOneWeekAgo = " " + (nowQuery - 86400000l * 6);
			strOneMonthAgo = " " + (nowQuery - 86400000l * 29);
			strOneYearAgo = " " + (nowQuery - 86400000l * 335);
		} else {
			strNow = " " + System.currentTimeMillis();
			nowQuery = REAL_START_TIME;
			strNowQuery = " " + nowQuery;
			strOneHourAgo = " " + (nowQuery - 3600000l + 60000l);
			strOneDayAgo = " " + (nowQuery - 86400000l + 3600000l);
			strOneWeekAgo = " " + (nowQuery - 86400000l * 6);
			strOneMonthAgo = " " + (nowQuery - 86400000l * 29);
			strOneYearAgo = " " + (nowQuery - 86400000l * 335);
		}

		String[] testCommands = {
				// Appliance type container test
				"hap cli String ah.app.type.1",
				"hap gi ah.app.1234567890123450 LATEST",
				"hap ci ah.app.1234567890123450" + strNow,
				"hap gi ah.app.1234567890123450 LATEST",
				// End point type container test
				"hap cli String ah.ep.type.1",
				"hap gi ah.app.1234567890123450/1 LATEST",
				"hap ci ah.app.1234567890123450/1" + strNow,
				"hap gi ah.app.1234567890123450/1 LATEST",
				// Appliance events container test
				"hap cli Integer " + AHContainers.APPLIANCE_EVENT_STARTED,
				"hap ci ah.app.1234567890123450/0/ah.core.appliance.events" + strNow,
				"hap cli Integer " + AHContainers.APPLIANCE_EVENT_AVAILABLE,
				"hap ci ah.app.1234567890123450/0/ah.core.appliance.events" + strNow,
				"hap cli Integer " + AHContainers.APPLIANCE_EVENT_UNAVAILABLE,
				"hap ci ah.app.1234567890123450/0/ah.core.appliance.events" + strNow,
				"hap cli Integer " + AHContainers.APPLIANCE_EVENT_STOPPED,
				"hap ci ah.app.1234567890123450/0/ah.core.appliance.events" + strNow,
				// Configured end point name container test
				"hap cli String appliance_name_1",
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.Name LATEST",
				"hap ci ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.Name" + strNow,
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.Name LATEST",
				// Configured end point category pid container test
				"hap cli Integer 1",
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.CategoryPid LATEST",
				"hap ci ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.CategoryPid" + strNow,
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.CategoryPid LATEST",
				// Configured end point category pid container test
				"hap cli Integer 1",
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.LocationPid LATEST",
				"hap ci ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.LocationPid" + strNow,
				"hap gi ah.app.1234567890123450/1/ah.cluster.ah.ConfigServer.LocationPid LATEST",
				// Esp gui log container test
				"hap cli String test_log_1",
				"hap cb ah.eh.gui.log" + strNow,
				// Esp invalid current summation value received
				"hap cli Integer 1",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.events" + strNow,
				// Esp min power container test
				"hap cli Float 123.4",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.minPower" + strNow,
				// Esp max power container test
				"hap cli Float 1234.5",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.maxPower" + strNow,
				// Esp energy containers test
				"hap cli Double 12345.6",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.energySum" + strNow,
				"hap gi ah.app.1234567890123450/1/ah.eh.esp.energySum LATEST",
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.hourlyEnergySum" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.hourlyEnergySum" + strOneDayAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergySum" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergySum" + strOneWeekAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergySum" + strOneMonthAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergySum" + strOneMonthAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.monthlyEnergySum" + strOneYearAgo + strNowQuery,
				"hap qi ALL/1/ah.eh.esp.monthlyEnergySum" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 0 23", // Sunday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 24 47", // Monday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 48 71", // Tuesday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 72 95", // Wednesday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 96 119", // Thursday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 120 143", // Friday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyAvg 144 167", // Saturday
				// Esp cost containers test
				"hap cli FloatCDV", "hap sli Duration 120000", "hap sli Value 0.01",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.energyCost" + strNow, "hap cli FloatCDV", "hap sli Duration 5400000",
				"hap sli Value 0.1", "hap sli Min 0.075", "hap sli Max 0.125",
				"hap cb ah.app.1234567890123450/1/ah.eh.esp.energyCost" + strNow,
				"hap gi ah.app.1234567890123450/1/ah.eh.esp.energyCost LATEST",
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.hourlyEnergyCost" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.hourlyEnergyCost" + strOneDayAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergyCost" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergyCost" + strOneWeekAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergyCost" + strOneMonthAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.dailyEnergyCost" + strOneMonthAgo + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.monthlyEnergyCost" + strOneYearAgo + strNowQuery,
				"hap qi ALL/1/ah.eh.esp.monthlyEnergyCost" + strNowQuery + strNowQuery,
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 0 23", // Sunday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 24 47", // Monday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 48 71", // Tuesday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 72 95", // Wednesday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 96 119", // Thursday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 120 143", // Friday
				"hap qi ah.app.1234567890123450/1/ah.eh.esp.wdHourlyEnergyCostAvg 144 167", // Saturday
		};

		TestCommandProvider testCommandProvider = new TestCommandProvider(this, testCommands);
		testCommandProvider.test(ci);
	}

	public String getHelp() {
		String help = "--- Automation@Home - HAP Service ---\n";
		help += "\thap c - print connection status\n";
		help += "\thap t - primt last upload time\n";
		help += "\thap cli <class name> <constructor parameter> - create a local content instance using the specified java class for the content (if no class name is specified a null content value is generated; an optional constructor parameter can be specified for the content class constructor)\n";
		help += "\thap gli - return current local content instance value\n";
		help += "\thap sli <field name> <field value> - set current the value of the local instance content field (an initial uppercase letter must be used for field names)\n";
		help += "\thap ci <container id> <currentTimeMillis> - create a new content instance associated to the specified contained id using current local content instance value (System.currentTimeMillis value is used if no currentTimeMillis parameter is specified)\n";
		help += "\thap cb <container id> <currentTimeMillis> - add a content instance creation request for the specified contained id to the batch request using current local content instance value (System.currentTimeMillis value is used if no currentTimeMillis parameter is specified)\n";
		help += "\thap gi <container id> <instance id> - retrieve the content instance associated to the specified id (a non negative long value or 'latest' and 'oldest' string can be used)\n";
		help += "\thap qi <container id> <start instance id> <end instance id> - extends the 'gd' command by retrieving all instances whose identifiers fall in the specified interval\n";
		help += "\thap gci <container id> - retrieve the latest locally cached content instance for the specified container id\n";
		help += "\thap gff - print fast forward parameter current value\n";
		help += "\thap sff <value> - set fast forward parameter value\n";
		help += "\thap test - execute a test command sequence\n";
		return help;
	}

}
