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

import java.util.Map;

/**
 * Represents a service cluster exposed by an appliance on a specific end point.
 * This interface exposes a set of generic methods that can be used to
 * read/write attributes, invoke commands and subscribe changes for attribute
 * that supports asynchronous notification.
 * 
 * @see IEndPoint
 * @see IAppliance
 **/
public interface IServiceCluster {
	/**
	 * The constant used to specify a client side service cluster
	 * implementation. The value is {@value} .
	 */
	public static final int CLIENT_SIDE = 0;
	/**
	 * The constant used to specify a server side service cluster
	 * implementation. The value is {@value} .
	 */
	public static final int SERVER_SIDE = 1;

	/**
	 * Returns the end point that exposes this service cluster
	 * 
	 * @return The {@code IEndPoint} interface that exposes this service cluster
	 */
	public IEndPoint getEndPoint();

	/**
	 * Returns the name of this service cluster (e.g.
	 * {@code "org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer"} in case
	 * of a server side implementation of the OnOff ZigBee cluster)
	 * 
	 * @return The name of this service cluster
	 */
	public String getName();

	/**
	 * Returns the type of this service cluster (e.g.
	 * {@code "org.energy_home.jemma.ah.cluster.zigbee.general.OnOff"} in case of a
	 * client or server side implementation of the OnOff ZigBee cluster)
	 * 
	 * @return The name of this service cluster
	 */
	public String getType();

	/**
	 * Returns the side of this service cluster implementation (client or
	 * server)
	 * 
	 * @return Returns {@link #SERVER_SIDE} in case of server side
	 *         implementation, {@link #CLIENT_SIDE} in case of client side
	 *         implementation
	 */
	public int getSide();

	public boolean isEmpty();
	
	/**
	 * Checks the availability of service exposed by this service cluster
	 * 
	 * @return {@code true} if this cluster services are currently accessible,
	 *         {@code false} otherwise
	 */
	public boolean isAvailable();

	/**
	 * Requests the current active subscription parameters for a connected end
	 * point and a specific attribute exposed by this service cluster
	 * 
	 * @param attributeName
	 *            The name of the attribute (e.g.
	 *            {@code
	 *            org.energy_home.jemma.ah.cluster.zigbee
	 *            .general.OnOffServer.ATTR_OnOff_NAME)}
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            can be used to get the reference to the notification interface
	 *            ( {@link IEndPointRequestContext#getPeerEndPoint()},
	 *            {@link IEndPoint#getServiceClustersListener()})
	 * @return The subscription parameters ({@link ISubscriptionParameters})
	 *         currently configured for the connected end point, {@code null} in
	 *         case no subscription is active for the specified connected end
	 *         point
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 * 
	 */
	public ISubscriptionParameters getAttributeSubscription(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException;

	// public ISubscriptionParameters getAttributeSubscription(String
	// attributeName, Object attributeSelector, IEndPointRequestContext
	// endPointRequestContext)
	// throws ApplianceException, ServiceClusterException;

	/**
	 * Subscribes an attribute implemented by this service cluster so that
	 * changes will be notified to a specific connected end point
	 * 
	 * @param attributeName
	 *            The name of the attribute (e.g.
	 *            {@code
	 *            org.energy_home.jemma.ah.cluster.zigbee
	 *            .general.OnOffServer.ATTR_OnOff_NAME)}
	 * @param parameters
	 *            The requested subscription parameters (
	 *            {@link ISubscriptionParameters})
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            will receive subsequent notifications (
	 *            {@link IEndPointRequestContext#getPeerEndPoint()},
	 *            {@link IEndPoint#getServiceClustersListener()})
	 * @return The subscription parameters ({@code ISubscriptionParameters})
	 *         configured for the connected end point (the returned subscription
	 *         parameters can be different from the requested ones due to
	 *         specific policies established by the framework or by appliances),
	 *         {@code null} in case no subscription has been activated
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 * 
	 */
	public ISubscriptionParameters setAttributeSubscription(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException;

	public Map getAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException;
	
	public void removeAllSubscriptions(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException;
	
	public IAttributeValue getLastNotifiedAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)  throws ApplianceException, ServiceClusterException ;
	
	// public ISubscriptionParameters setAttributeSubscription(String
	// attributeName, Object attributeSelector, ISubscriptionParameters
	// parameters,
	// IEndPointRequestContext endPointRequestContext) throws
	// ApplianceException, ServiceClusterException;

	/**
	 * Reads an attribute implemented by this service cluster
	 * 
	 * @param attributeName
	 *            The name of the attribute (e.g.
	 *            {@code
	 *            org.energy_home.jemma.ah.cluster.zigbee
	 *            .general.OnOffServer.ATTR_OnOff_NAME)}
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            can be used to get the reference to the notification interface
	 *            ( {@link IEndPointRequestContext#getPeerEndPoint()},
	 *            {@link IEndPoint#getServiceClustersListener()})
	 * @return The {@link IAttributeValue} containing the value read for the
	 *         requested attribute
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 */
	public IAttributeValue getAttributeValue(String attributeName, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException;

	/**
	 * Writes an attribute exposed by this service cluster
	 * 
	 * @param attributeName
	 *            The name of the attribute (e.g.
	 *            {@code
	 *            org.energy_home.jemma.ah.cluster.zigbee
	 *            .general.OnOffServer.ATTR_OnOff_NAME)}
	 * @param attributeValue
	 *            The value
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            can be used to get the reference to the notification interface
	 *            ( {@link IEndPointRequestContext#getPeerEndPoint()},
	 *            {@link IEndPoint#getServiceClustersListener()})
	 * @return The written attribute value or {@code null} if the request
	 *         context specifies that no confirmation is required
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 */
	public IAttributeValue setAttributeValue(String attributeName, Object attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException;

	/**
	 * Invokes a command implemented by this service cluster
	 * 
	 * @param commandName
	 *            The name of the command to invoke (e.g.
	 *            {@code
	 *            org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer.CMD_Toggle_NAME
	 *            * })
	 * @param parameters
	 *            An array with all the parameters associated to the specific
	 *            command
	 * @param endPointRequestContext
	 *            The request context, including the connected end point that
	 *            can be used to get the reference to the notification interface
	 *            ( {@link IEndPointRequestContext#getPeerEndPoint()},
	 *            {@link IEndPoint#getServiceClustersListener()})
	 * @return The response object associated to the requested command,
	 *         {@code null} in case no response is expected for the specified
	 *         command
	 * @throws ApplianceException
	 *             In case of generic errors (e.g. end point not available or
	 *             invalid appliance connection)
	 * @throws ServiceClusterException
	 *             In case of specific problems during the execution of the
	 *             requested operation (e.g. an error code returned by the
	 *             associated physical ZigBee device)
	 */
	public Object execCommand(String commandName, Object[] parameters, IEndPointRequestContext endPointRequestContext)
			throws ApplianceException, ServiceClusterException;
	
	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException;
}
