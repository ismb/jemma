/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.ah.eh.esp;


import java.util.List;
import java.util.Map;

public interface ESPService {
	public static final int NO_VALID_RESOLUTION = -1;
	public static final int HOUR_RESOLUTION = 1;
	public static final int DAY_RESOLUTION = 2;
	public static final int MONTH_RESOLUTION = 3;
	public static final int YEAR_RESOLUTION = 4;

	public static float INVALID_INSTANTANEOUS_POWER_VALUE = 0xFFFFFF;
	public static double INVALID_ENERGY_CONSUMPTION_VALUE = 0xFFFFFFFFFFFFL;

	/**
	 * Return initial configuration time
	 * 
	 * @return Initial configuration time or null in case no active
	 *         configuration is found
	 */
	public Long getInitialConfigurationTime();

	/**
	 * Return current configured parameters
	 * 
	 * @return Current configured parameters, null in case no active
	 *         configuration is found
	 */
	public ESPConfigParameters getCurrentConfiguration();

	/**
	 * Update current configuration parameters
	 * 
	 * @param config
	 *            Configuration parameters or null to reset all configuration
	 *            parameters
	 */
	public void setConfiguration(ESPConfigParameters config) throws ESPException;

	public void sendGuiLog(String msg) throws ESPException;

	/**
	 * Returns the last total instantaneous power (W) calclulated from values
	 * read from all the available appliances
	 * 
	 * @return The max value between the last value read from the smart info
	 *         appliance (when available) and the sum of all the values read
	 *         from other available appliances
	 * @throws ESPException
	 */
	public float getTotalInstantaneousPowerFloatValue() throws ESPException;

	public float getInstantaneousProducedPowerFloatValue() throws ESPException;
	
	public float getInstantaneousSoldPowerFloatValue() throws ESPException;
	
	/**
	 * Returns the last instantaneous power (W) read from the specified
	 * appliance
	 * 
	 * @param appliancePid
	 *            The appliance pid or null for smart info appliance
	 * @return the last instantaneous power read from the appliance (0 in case
	 *         the appliance is not available,
	 *         {@value #INVALID_INSTANTANEOUS_POWER_VALUE} in case last notified
	 *         value was invalid)
	 * @throws ESPException
	 */
	public float getInstantaneousPowerFloatValue(String appliancePid) throws ESPException;
	
	public List<Float> getEnergyConsumption(String appliancePid, long startTime, long endTime, int resolution) throws ESPException;
	
	public List<Float> getProducedEnergy(long startTime, long endTime, int resolution) throws ESPException;
	
	public List<Float> getSoldEnergy(long startTime, long endTime, int resolution) throws ESPException;

	public Map<String, List<Float>> getEnergyConsumption(long startTime, long endTime, int resolution) throws ESPException;

	public List<Float> getEnergyCost(String appliancePid, long startTime, long endTime, int resolution) throws ESPException;

	public Map<String, List<Float>> getEnergyCost(long startTime, long endTime, int resolution) throws ESPException;

	public Float getEnergyConsumptionForecast(String appliancePid, int resolution) throws ESPException;

	public Float getEnergyCostForecast(String appliancePid, int resolution) throws ESPException;

	public List<Float> getWeekDayEnergyConsumpionAverage(String appliancePid, int weekDay) throws ESPException;

	public List<Float> getWeekDayEnergyCostAverage(String appliancePid, int weekDay) throws ESPException;

}
