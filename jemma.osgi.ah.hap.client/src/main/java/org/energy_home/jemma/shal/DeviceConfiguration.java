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
package org.energy_home.jemma.shal;

public interface DeviceConfiguration {
	// Optional property for the end point associated to a device (suggested by driver or application)
	public static final String DEVICE_NICKNAME_PROPERTY = "shal.device.nickname";
	
	// Optional property for the end point associated to a device (suggested by driver or application)
	public static final String DEVICE_CATEGORY_PROPERTY = "shal.device.category";
	
	// Optional property for the end point associated to a device (suggested by driver or application)
	public static final String DEVICE_LOCATION_PROPERTY = "shal.device.location";
	
	public enum DeviceCategory {		
		Other, Lamp, WaterHeater, TV, PC, Oven, Iron, Refrigerator, DishWasher,
		AirConditioner, WashingMachine, Meter, JollySmartPlug, ProductionMeter, SecondaryMeter,
		Printer, ModemRouter, DecoderRecorderPlayer, HomeTheatreStereo, PlayStation, MediaCenter,
		Freezer, WasherDryer, VacuumCleaner, HairDryer, BreadMachine, CoffeeMachine, Toaster,
		FoodRobot, WaterPurifier, Hob, ElectricHeater, SwimmingPoolPump, Hood
	}
	
	public String getNickname();
	
	public DeviceCategory getCategory();
	
	public DeviceLocation getLocation();
}
