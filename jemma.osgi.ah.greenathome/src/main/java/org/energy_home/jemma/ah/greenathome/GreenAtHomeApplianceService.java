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
package org.energy_home.jemma.ah.greenathome;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;

public interface GreenAtHomeApplianceService {

	/*
	public AttributeValue getAttribute(String peerAppliancePid, String name) throws ApplianceException, ServiceClusterException,
			Exception;
*/
	public AttributeValue getAttribute(String name) throws Exception;

	public void setAttribute(String name, Object value) throws Exception;

	public IAppliance[] getDevices();

	//public Vector getInfos();
	
	//added for demo
	//public Vector getInfosDemo();

	public ArrayList getAppliancesConfigurations() throws ApplianceException, ServiceClusterException;
	
	//public ArrayList<Hashtable<String, String>> getNoServerCustomDevice() throws ApplianceException, ServiceClusterException;

	//public Hashtable getInfo(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException;

	public Hashtable getApplianceConfiguration(String appliancePid) throws ApplianceException, ServiceClusterException;

	public ICategory[] getCategories(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException;

	public ICategory[] getCategories(String appliancePid) throws ApplianceException, ServiceClusterException;

	public ICategory[] getCategories() throws ApplianceException, ServiceClusterException;

	public ILocation[] getLocations() throws ApplianceException, ServiceClusterException;

	public boolean setCategory(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException;

	public void setCategory(IAppliance peerAppliance, String category) throws ApplianceException, ServiceClusterException;

	public void setLocation(IAppliance peerAppliance, String locationName) throws ApplianceException, ServiceClusterException;

	public void removeDevice(String appliancePid) throws ApplianceException;

	public boolean setDeviceState(IAppliance peerAppliance, int state);
	public boolean setDeviceState(IAppliance peerAppliance, int state, short value);

	public int getDeviceState(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException;
	public int getDeviceState(IAppliance peerAppliance, int state) throws ApplianceException, ServiceClusterException;

	public Object getObjectByPid(String pid);

	public double readPower(IAppliance peerAppliance) throws Exception;

	public void startInquiry(short timeout) throws Exception;

	public void stopInquiry() throws Exception;

	public ArrayList getInquiredDevices();

	/**
	 * Configure an Appliance. The props dictionary contains the properties for
	 * the device that have to be configured. The following properties are
	 * mandatory:
	 * 
	 * <ul>
	 * <li><code>ah.app.name</code>The name of the new appliance</li>
	 * <li><code>ah.app.type</code>The type of the new appliance</li>
	 * </ul>
	 * 
	 * The following properties are optional:
	 * <ul>
	 * <li><code>ah.icon</code>The filename of the icon</li>
	 * <li><code>ah.location.pid</code>The pid of the device location</li>
	 * <li><code>ah.category.pid</code>The pid of the device category</li>
	 * </ul>
	 * 
	 * 
	 * All the properties must be of class java.lang.String<br>
	 * The addInquiredDevice method attempt to create the new device. If some
	 * errors occurs an exception is raised.
	 * 
	 * @param deviceInfo
	 * @throws HacException
	 */
	public void installAppliance(Dictionary deviceInfo) throws ApplianceException, HacException;

	public void updateAppliance(Dictionary props) throws ApplianceException;

	public final static int Minute = 0;
	public final static int Hour = 1;
	public final static int Day = 2;
	public final static int Month = 3;
	public final static int Year = 4;

	public final static int LAST = 0;
	public final static int FIRST = 1;
	public final static int MAX = 2;
	public final static int MIN = 3;
	public final static int AVG = 4;
	public final static int DELTA = 5;

	/**
	 * Retrieve measurements related to a specific attribute for a specific time
	 * interval on a specific appliance. The appliance must be connected to the
	 * GreenAtHome appliance.
	 * 
	 * @param appliancePid
	 *            The pid of the appliance we are interested in. An
	 *            ApplianceException exception occurs if the appliance does not
	 *            exist.
	 * 
	 * @param attributeName
	 *            Name of the attribute
	 * @param startTime
	 *            The epoch in milliseconds of the starting time of the
	 *            requested interval.
	 * @param endTime
	 *            The epoch in milliseconds of the end time of the requested
	 *            interval
	 * @param resolution
	 *            The the granularity of the received data. Resolution may be
	 *            one of the following constants: Minute, Hour, Day, Month,
	 *            Year, None. If the resolution is None the raw data is
	 *            returned.
	 * 
	 * @param fitResolution
	 *            If true, states that the startTime and endTime timestamp have
	 *            to be rounded to the selected resolution. For instance if
	 *            resolution is Month, startTime is rounded to the timestamp of
	 *            the begining of the month startTime belongs to and endTime is
	 *            rounded to the last millisecond of the month it fits into.
	 * @param processType
	 *            Represents a post processing function. The value may be
	 *            <ul>
	 *            <li>
	 *            <code>LAST</code> returns the last sample for each interval</li>
	 *            <li><code>FIRST</code> returns the first sample for each
	 *            interval</li>
	 *            <li>
	 *            <code>MAX</code> returns the maximum value of the measure for
	 *            each interval</li>
	 *            <li>
	 *            <code>MIN</code> returns the minimum value of the measure for
	 *            each interval</li>
	 *            <li>
	 *            <code>MIN</code> returns the average value of the measure on
	 *            each interval</li>
	 *            </ul>
	 * @return an array list of XXXX instances
	 * 
	 * @throws Exception
	 *             In case of error.
	 */
	List getAttributeData(String appliancePid, String attributeName, long startTime, long endTime, int resolution,
			boolean fitResolution, int processType) throws Exception;

	/**
	 * Retrieve measurements related to a specific attribute for a specific time
	 * interval on all the appliances connected to the GreenAtHome appliance.
	 * The returned value is a dictionary with the appliancePid as key and the
	 * returned measure as value (see getAttributeData for details)
	 * 
	 * @throws Exception
	 */
	Map getAttributeData(String attributeName, long startTime, long endTime, int resolution, boolean fitResolution, int processType)
			throws Exception;

	/**
	 * Returns the forecasts for the specified attributeName of the appliance
	 * with pid appliancePid.
	 * 
	 * @param appliancePid
	 *            The appliance containing the attribute
	 * @param attributeName
	 *            The name of the attribute
	 * @param timestamp
	 *            A timestamp in milliseconds within the interval of time
	 *            specified by the resolution parameter
	 * @param resolution
	 *            The interval resolution (size). See the getAttributeData for a
	 *            description of the resolution attribute
	 * @return The requested information
	 * 
	 * @throws Exception
	 */
	public Float getForecast(String appliancePid, String attributeName, long timestamp, int resolution) throws Exception;

	/**
	 * Returns the average value of the specified attribute
	 * 
	 * @param appliancePid
	 *            the pid of the appliance that exports this attribute
	 * @param attributeName
	 *            the name of the attribute
	 * @param weekday
	 *            The day of the week (as defined in the java.utils.Calendar)
	 * @return An array containing 24 values representing....
	 * 
	 * @throws Exception
	 * 
	 */
	public List getWeekDayAverage(String appliancePid, String attributeName, int weekday) throws Exception;

	public void initialProvisioning();

	public void loadConfiguration(String filename) throws Exception;

	public boolean reset(int value) throws Exception;

	public void setHapConnectionId(String connectionId);

	public boolean isHapClientConnected();

	public long getHapLastUploadTime();

	public void sendGuiLog(String msg) throws Exception;

	public long currentTimeMillis();

	public Long getInitialConfigurationTime();
	
	
	//TODO: check merge, methods below not in 3.3.0
	/*
	 * 
	 * 
	 * METODI AGGIUNTI INTECS
	 * 
	 * 
	 */
	
	public Hashtable getDeviceClusters(String appliancePid);
	
	public ArrayList getCategoriesWithPid() throws ApplianceValidationException;
	
	//public Hashtable getInfoNew(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException;
	
//	public Vector getInfos();
	
	
	/**
	 * Methods for the LevelControl cluster
	 * 
	 */
	public short levelControlGetCurrentValue(String appliancePid);
	
	public boolean levelControlExecMoveToLevelWithOnOff(String appliancePid, short Level, int TransitionTime);
	
	public boolean levelControlExecMoveToLevel(String appliancePid, short Level, int TransitionTime);
	
	public boolean levelControlExecStopWithOnOff(String appliancePid);
	
	/**
	 * Methods for the ColorControl cluster
	 * 
	 */
	public boolean colorControlMoveToColorHSL(String appliancePid, short hue, short saturation, short level, int transitionTime);
	
	public boolean colorControlMoveToColorHS(String appliancePid, short hue, short saturation, int transitionTime);
	
	public Hashtable colorControlGetColorHSL(String appliancePid);
	
	public Hashtable colorControlGetColorHS(String appliancePid);
	
	public boolean colorControlMoveToColorXYL(String appliancePid, int X,int Y,short level, int transitionTime);
	
	/**
	 * Methods for the ApplianceControl cluster
	 * 
	 */
	public int applianceControlGetStartTime(String appliancePid);
	
	public int applianceControlGetFinishTime(String appliancePid);
	
	public int applianceControlGetRemainingTime(String appliancePid);
	
//	public boolean applianceControlExecCommandExecution(String appliancePid, short CommandId);
	
	public Hashtable applianceControlExecSignalState(String appliancePid);
	
	public short applianceControlGetCycleTarget0(String appliancePid);
	
	public short applianceControlGetCycleTarget1(String appliancePid);
	
	public int applianceControlGetTemperatureTarget0(String appliancePid);
	
	public int applianceControlGetTemperatureTarget1(String appliancePid);
	
//	public boolean applianceControlExecWriteFunctions(String appliancePid,WriteAttributeRecord[] WriteAttributeRecords);
	
	
	public boolean setDeviceState(String appliancePid, int state);
	public boolean setDeviceState(String appliancePid, int state, short value);
	
	public int getDeviceState(String appliancePid) throws ApplianceException, ServiceClusterException;
	public int getDeviceState(String appliancePid, int state) throws ApplianceException, ServiceClusterException;
	
	public Hashtable testFunction(String appliancePid, String p1, int p2, int p3);
	
	public List getDailyPVForecast();
	
	public String getDailyPVForecastDebug();
	
}