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

/**
 * This interface is used to notify a set of asynchronous events associated with
 * service clusters of connected peer appliances.
 * <p>
 * Events include the notification of changes subscribed for service cluster
 * attributes and the completion of previously asynchronously invoked operation
 * (e.g. reading or writing an attribute and executing a command)
 * 
 */
public interface IServiceClustersListener {
	/**
	 * Notifies a change for an attribute value that has been subscribed
	 * 
	 * @param clusterName
	 *            The name of the service cluster associated to this
	 *            notification
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
	public void notifyAttributeValue(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException;

	// public void notifyAttributeValue(String clusterName, String
	// attributeName, Object attributeSelector, IAttributeValue attributeValue,
	// IEndPointRequestContext endPointRequestContext) throws
	// ServiceClusterException, ApplianceException;

	/**
	 * Notifies the completion of a previously requested read operation
	 * 
	 * @param clusterName
	 *            The name of the service cluster associated to this
	 *            notification
	 * @param attributeName
	 *            The name of the attribute associated to this notification
	 * @param attributeValue
	 *            The read attribute value
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
	public void notifyReadResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException;

	/**
	 * Notifies the completion of a previously requested write operation
	 * 
	 * @param clusterName
	 *            The name of the service cluster associated to this
	 *            notification
	 * @param attributeName
	 *            The name of the attribute associated to this notification
	 * @param attributeValue
	 *            The written attribute value
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
	public void notifyWriteResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException;

	/**
	 * Notifies the completion of a previously invoked command
	 * 
	 * @param clusterName
	 *            The name of the service cluster associated to this
	 *            notification
	 * @param commandName
	 *            The executed command name
	 * @param response
	 *            The response associated to the invoked command, {@code null}
	 *            in case no response is expected for the specified command
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
	public void notifyCommandResponse(String clusterName, String commandName, Object response,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException;

}
