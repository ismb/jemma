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
package org.energy_home.jemma.ah.ebrain;


public interface IMeteringProxy {
//	public static final short FORMATTING_DECIMAL_DIGITS_MASK = 0x7; // bits 0-2
//	public static final short FORMATTING_INTEGER_DIGITS_MASK = 0x38; // bits 3-6

	public static float INVALID_FORMATTING_VALUE = -1;	
	public static final float INVALID_INSTANTANEOUS_POWER_VALUE = 0xFFFFFF;
	public static final float INVALID_INSTANTANEOUS_POWER_STANDARD_VALUE = 0x800000;
	public static final double INVALID_ENERGY_CONSUMPTION_VALUE = 0xFFFFFFFFFFFFL;
	
	// contains the Average	Power Factor ratio in 1/100's
	short getPowerFactor(String applianceId);
	// provides indicators reflecting the current error conditions
	short getStatus(String applianceId);
	// for the Energy being measured unit is KWatt/Hr
	short getUnitOfMeasure(String applianceId);
	// enumerated values representing Energy, Gas, Water, Thermal, and mirrored	metering devices
	short getMeteringDeviceType(String applianceId);


	// provides a method to properly decipher the number of digits and the 	decimal
	// location of the values found in the Summation Information Set of attributes
	float getSummationFormatting(String applianceId);
	
	// represents the most recent summed value of Energy and consumed in the premise. 
	double getCurrentSummationDelivered(String applianceId);
	void subscribeCurrentSummationDelivered(String applianceId, long minReportingInterval, long maxReportingInterval, double deltaValue);
	
	// represents the most recent summed value of Energy and consumed in the premise. 
	double getCurrentSummationReceived(String applianceId);
	void subscribeCurrentSummationReceived(String applianceId, long minReportingInterval, long maxReportingInterval, double deltaValue);
		
	
	// provides a method to properly decipher the number of digits and the decimal
	// location of the values found in the Demand related attributes.
	float getDemandFormatting(String applianceId);

	// represents the current Demand of Energy delivered or received at the premise.
	// Positive values indicate Demand delivered to the premise where negative values
	// indicate demand received from the premise. In E@H describes the total power used
	// by the entire house at a given instant in time, and it is measured in KW.
	float getIstantaneousDemand(String applianceId);
	void subscribeIstantaneousDemand(String applianceId, long minReportingInterval, long maxReportingInterval, float deltaValue);
}
