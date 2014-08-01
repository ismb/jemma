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

import java.util.ArrayList;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.osgi.framework.InvalidSyntaxException;

public interface IConnectionAdminService {

	//public void statusUpdated(String pid);

	public ArrayList getBindRules();
	
	public boolean removeBindRule(String pid) throws HacException;

	/**
	 * Deactivates all the binds starting from the appliance
	 * 
	 * @param appliancePid
	 *            The pid of the appliance
	 * @return true if successful
	 */

	public boolean deactivateBinds(String appliancePid) throws HacException;

	/**
	 * Removes all the bind rules and drops all the binds between the appliances
	 */

	public void deleteAllRules();

	public boolean deactivateBind(String appliance1Pid, String appliance2Pid);

	public String[] getPeerAppliancesPids(String appliancePid) throws HacException;
	
	public IAppliance[] getPeerAppliances(String appliancePid, int endPoint) throws HacException;

	public IAppliance[] getPeerAppliances(String appliancePid, int endPointId, int propertyKey, String propertyValue) throws HacException;

	/**
	 * 
	 * Create a 'connection' between the two passed VA pids. The connection will
	 * be active as soon two VA with such a pid will be available. The passed
	 * arrays specify which pin have to be connected. The connection works only
	 * if the pins are compatible. The compatibility is checked as soon as both
	 * VA are running and, in any case any time one of the two VA starts again
	 * after being stopped. In case of non-compability, the connection is not
	 * issued. A connection is a relationship between to VA.
	 * 
	 * @param appliance1Pid
	 *            Pid of the first VA to connect (i.e VA1)
	 * @param appliance1Pins
	 *            Pins of VA1 that have to be connected
	 * @param appliance2Pid
	 *            Pid of the second VA to connect (i.e VA2)
	 * @param appliance2Pins
	 *            Pins of VA2 that have to be connected
	 * @return
	 * @throws ApplianceException
	 */

	public boolean createConnection(String appliance1Pid, String appliance2Pid) throws ApplianceException;

	public void addBindRule(String rule) throws InvalidSyntaxException;
}