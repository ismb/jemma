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

public interface IServiceClusterListener {
	/**
	 * Notifies a change for an attribute value that has been subscribed
	 * for a specific cluster
	 * 
	 * @param attributeName
	 *            The name of the attribute associated to this notification
	 * @param attributeValue
	 *            The attribute value associated to this notification
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            has issued this notification
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 */
	public void notifyAttributeValue(String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException;
}
