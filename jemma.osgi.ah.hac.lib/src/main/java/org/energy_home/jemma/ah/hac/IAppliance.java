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
package org.energy_home.jemma.ah.hac;

import java.util.Dictionary;

/**
 * Represents the basic interface used in the OSGi framework to interact with a
 * physical device or an application that exposes its service in the A@H
 * framework. There are two different categories of appliances:
 * <p>
 * <ul>
 * <li>{@code driver appliances} represent physical devices and are created by
 * the associated driver ({@link IApplianceFactory} )</li>
 * <li>{@code logical appliances} represent virtual devices or application
 * logics implemented by bundle installed in the OSGi framework</li>
 * </ul>
 * Each appliance exposes its services through a set of end points (
 * {@link IEndPoint}), each of which implements a set of service clusters (
 * {@link IServiceCluster}).
 * <p>
 * In case of a driver appliance that represents a standard ZigBee device, each
 * ZigBee node's end point is associated to an appliance end point ({@code
 * IEndPoint}) and each ZigBee cluster is associated to an appliance service
 * cluster ({@code IServiceCluster})
 * 
 * @see {@link IManagedAppliance}
 */
public interface IAppliance {
	public static final String AH_PROPERTY_PREFIX = "ah.";
	/**
	 * Property name used for the appliance type. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_TYPE_PROPERTY = "ah.app.type";
	/**
	 * Integer key used to browse appliances by type. The value is {@value}
	 * 
	 * @see IManagedAppliance#browsePeerAppliances(int, String)
	 */
	public static final int APPLIANCE_TYPE_PROPERTY_KEY = 0;
	/**
	 * Property name used for the appliance name. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_NAME_PROPERTY = "ah.app.name";
	public static final String END_POINT_NAMES_PROPERTY = "ah.app.eps.names";
	/**
	 * Integer key used to browse appliances by name. The value is {@value}
	 * 
	 * @see IManagedAppliance#browsePeerAppliances(int, String)
	 */
	public static final int APPLIANCE_NAME_PROPERTY_KEY = 1;
	/**
	 * Property name used for the location PID. The value is {@value}
	 * 
	 * @see ILocation
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_LOCATION_PID_PROPERTY = "ah.location.pid";
	public static final String END_POINT_LOCATION_PIDS_PROPERTY = "ah.eps.location.pids";
	/**
	 * Integer key used to browse appliances by location PID. The value is * *
	 * {@value}
	 * 
	 * @see ILocation
	 * @see IManagedAppliance#browsePeerAppliances(int, String)
	 */
	public static final int APPLIANCE_LOCATION_PID_PROPERTY_KEY = 2;
	/**
	 * Property name used for the category PID. The value is {@value}
	 * 
	 * @see ICategory
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_CATEGORY_PID_PROPERTY = "ah.category.pid";
	public static final String END_POINT_CATEGORY_PIDS_PROPERTY = "ah.eps.category.pids";
	/**
	 * Property name used for identifying the appliance persistent identifier
	 */
	public static final String APPLIANCE_PID = "appliance.pid";
	/**
	 * Integer key used to browse appliances by category PID. The value is * *
	 * {@value}
	 * 
	 * @see ICategory
	 * @see IManagedAppliance#browsePeerAppliances(int, String)
	 */
	public static final int APPLIANCE_CATEGORY_PID_PROPERTY_KEY = 3;
	/**
	 * Property name used for the icon name. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_ICON_PROPERTY = "ah.icon";
	public static final String END_POINT_ICONS_PROPERTY = "ah.eps.icons";
	/**
	 * Integer key used to browse appliances by its PID. The value is {@value}
	 * 
	 * @see IManagedAppliance#browsePeerAppliances(int, String)
	 */
	public static final int APPLIANCE_PID_PROPERTY_KEY = 5;

	/**
	 * Property name used for end points' types array. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_EPS_TYPES_PROPERTY = "ah.app.eps.types";
	
	/**
	 * Property name used for end points' identifiers array. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_EPS_IDS_PROPERTY = "ah.app.eps.ids";
	
	/**
	 * Property name prefix used for custom appliance properties. The value is {@value}
	 * 
	 * @see IAppliance#getConfiguration()
	 */
	public static final String APPLIANCE_CUSTOM_PROPERTIES_PREXIF = "ah.app.custom.";
	
	/**
	 * Check if the associated appliance is a singleton
	 * 
	 * @return True if the associated appliance is a singleton, false otherwise
	 */
	public boolean isSingleton();
	
	/**
	 * Returns the Persistent IDentifier (PID) associated to an instance of an
	 * appliance
	 * 
	 * @return The PID that uniquely identifies the appliance instance and, in
	 *         case of a driver appliance, also the associated physical device
	 */
	public String getPid();

	/**
	 * Returns a set of properties associated to a specific appliance type; if a
	 * factory is associated to this appliance, this method returns the same
	 * descriptor as those returned by the
	 * {@link IApplianceFactory#getDescriptor()} method
	 * 
	 * @return The {@link IApplianceDescriptor} associated to this appliance
	 */
	public IApplianceDescriptor getDescriptor();

	/**
	 * Returns a dictionary with a set of configuration parameters associated to
	 * the appliance and managed by the A@H Framework. The framework is responsible 
	 * to store and load the configuration parameters into the appliance when the 
	 * {@code IManagedAppliance} is registered.
	 * 
	 * @return A {@code Dictionary} with all the configuration parameters
	 */
	public Dictionary getConfiguration();
	
	/**
	 * Returns a dictionary with a set of custom configuration parameters associated to
	 * the appliance and managed by the A@H Framework. The framework is responsible 
	 * to store and load the configuration parameters into the appliance when the 
	 * {@code IManagedAppliance} is registered. All keys used in the dictionary 
	 * that don't start with {@link IAppliance#APPLIANCE_CUSTOM_PROPERTIES_PREXIF} 
	 * are discarded
	 * 
	 * @return A {@code Dictionary} with all the custom configuration parameters
	 */
	public Dictionary getCustomConfiguration();
	
	/**
	 * Checks if this appliance is associated to a physical device through a
	 * driver
	 * 
	 * @return {@code true} if this is a driver appliance, {@code false}
	 *         otherwise
	 */
	public boolean isDriver();

	/**
	 * Checks if this appliance instance is valid (if a reference to an {@code
	 * IAppliance} interface is saved, this method enables to check if the
	 * reference is still valid )
	 * 
	 * @return {@code true} if this is a valid appliance instance, {@code false}
	 *         otherwise
	 */
	public boolean isValid();
	
	/**
	 * Return the appliance availability
	 * @return {@code true} if all this appliance end point are available, {@code false} otherwise
	 */
	public boolean isAvailable();

	/**
	 * Returns the list of all implemented appliance's end points
	 * 
	 * @return An array with all implemented {@link IEndPoint} associated to
	 *         this appliance
	 */
	public IEndPoint[] getEndPoints();
	
	/**
	 * Returns the list of all implemented appliance's end points identifiers
	 * 
	 * @return An array with all end point identifiers associated to
	 *         this appliance
	 */
	public int[] getEndPointIds();
	
	/**
	 * Returns the list of all implemented appliance's end points types
	 * 
	 * @return An array with all end point types associated to
	 *         this appliance
	 */
	public String[] getEndPointTypes();

	/**
	 * Return the appliance end point associated to a specific end point
	 * identifier
	 * 
	 * @param id
	 *            the end point identifier {@link IEndPoint#getId()}
	 * @return The end point associated to the identifier, {@code null} if no
	 *         end point is found for the specified identifier
	 */
	public IEndPoint getEndPoint(int id);

}