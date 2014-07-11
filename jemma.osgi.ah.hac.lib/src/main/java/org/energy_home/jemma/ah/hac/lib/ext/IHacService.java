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
package org.energy_home.jemma.ah.hac.lib.ext;

import java.util.Dictionary;
import java.util.Vector;

import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.ILocation;

/**
 * This service is currently used only by the A@H core services. It exposes a
 * set of interfaces used for the management of appliances, connections and
 * configuration info.
 * 
 */
public interface IHacService {

	/**
	 * Removes the Virtual Appliance that has the passed persistend identifier
	 * (pid).
	 * 
	 * @param appliancePid
	 *            pid of the Virtual Appliance to remove
	 * @return
	 */
	public boolean removeAppliance(String appliancePid);

	/**
	 * Returns the list of appliance's pids currently available.
	 * 
	 * @return A Vector of Strings representing the appliance's pids.
	 */
	public Vector getAppliances();

	/**
	 * Given a key type and a key value, this method returns the list of devices
	 * matching the criteria
	 * 
	 * @param key_type
	 *            The key type. May be name, location, type, ieee_addr,
	 *            short_addr
	 * 
	 * @param key_value
	 *            The value of the key. If key_value is none it means Any, If
	 *            the key_value is '', means not specified. For instance if
	 *            key_type is HAttr_category, and key_value = '',
	 * 
	 * @return VirtualAppliance list matching the key_value. The list may be
	 *         empty or null
	 */

	public Vector browseAppliances(int key_type, String key_value);

	/**
	 * Clean all the HAC configuration. In other words: - Deletes all the
	 * connections between Virtual Appliances - Deletes all the virtual
	 * appliances - Deletes all the locations
	 */
	public void clean();

	/**
	 * Reset the HAC configuration to factory default.
	 * 
	 * @param level
	 *            may be 0, for a full reset, level values different from 0 are
	 *            reserved
	 * 
	 * @return true if successful, false if not
	 */
	public boolean reset(int level);

	/**
	 * Returns the pids of the appliances that are still in the installing
	 * state.
	 * 
	 * @return an array of appliance pids.
	 */

	public String[] getInquiredAppliances();

	/**
	 * Completes the installation of a newly discovered appliance. The appliance
	 * must be already been created and must be in the installing state.
	 * 
	 * props parameter must contain at least the following properties:
	 * <ul>
	 * <li>ah.app.type (i.e. the factory type)</li>
	 * <li>ah.app.name, must contain the appliance name (must be unique across
	 * all the appliances of the system)</li>
	 * <li>ah.location.pid, must contain the appliance location pid</li>
	 * <li>ah.category.pid, must contain the appliance category pid</li>
	 * </ul>
	 * 
	 * The ah.app.type, ah.app.name are mandatory properties.
	 * 
	 * @param appliancePid
	 *            The appliance pid of the appliance to install. This pid is
	 *            forged by the system when a new appliance is detected and so
	 *            it must match the appliance pid of an already existing
	 *            appliance that is in the installing state. The 'appliance.pid'
	 *            property present in the props parameter is overridden with
	 *            this appliancePid.
	 * @param props
	 *            Dictionary of appliance props. The list of supported
	 *            properties is listed above
	 * 
	 * @throws HacException
	 *             This exception is raised in case of failure in installing the
	 *             appliance. One common cause of failure is that the appliance
	 *             has been already installed or a mandatory property is missing
	 *             from the passed props dictionary.
	 */

	public void installAppliance(String appliancePid, Dictionary props) throws HacException;

	/**
	 * Completes the installation of a newly discovered appliance. The appliance
	 * must be already been created and must be in the installing state.
	 * 
	 * @param appliancePid
	 *            The appliance pid of the appliance to install. This pid is
	 *            forged by the system when a new appliance is detected and so
	 *            it must match the appliance pid of an already existing
	 *            appliance that is in the installing state. The 'appliance.pid'
	 *            property present in the props parameter whould be overridden
	 *            with this appliancePid.
	 * 
	 * @throws HacException
	 *             This exception is raised in case of failure in installing the
	 *             appliance. One common cause of failure is that the appliance
	 *             has been already installed.
	 */

	public void enableAppliance(String appliancePid) throws HacException;

	/**
	 * Updates the properties of an appliance. The appliance may or may not
	 * already exist. If the appliance already exist the passed properties
	 * overrides the properties of the already existing appliance. The
	 * APPLIANCE_TYPE_PROPERTY cannot be overridden. If the appliance doesn't
	 * exist the appliance is created.
	 * 
	 * @param appliancePid
	 * @param props
	 * @throws HacException
	 */

	public void updateAppliance(String appliancePid, Dictionary props) throws HacException;

	/**
	 * Similar to installAppliance but creates an appliance from scratch. The
	 * appliance must not be in the 'installing' state.
	 * 
	 * @param appliancePid
	 * @param props
	 * @throws HacException
	 */

	public void createAppliance(String appliancePid, Dictionary props) throws HacException;

	public ILocation[] getLocations();

	public ICategory[] getCategories();
	
	public ILocation getLocation(String appliancePid);

	public ICategory getCategory(String appliancePid);

	public void addCategory(ICategory category) throws HacException;
	
	public void removeCategory(String categoryPid) throws HacException;

	/**
	 * Add a new Location to the table of possible locations.
	 * 
	 * @param location
	 *            A Location object with the new location name, icon and pid.
	 *            The pid must not be the same of an already existing Location.
	 * @throws HacException
	 */

	public Location addLocation(Location location) throws HacException;

	public Dictionary getManagedConfiguration(String appliancePid);

	public boolean isNetworkOpen(String networkType) throws HacException;
	
	public void openNetwork(String networkType) throws HacException;

	public void openNetwork(String networkType, int duration) throws HacException;

	public void closeNetwork(String networkType) throws HacException;
}
