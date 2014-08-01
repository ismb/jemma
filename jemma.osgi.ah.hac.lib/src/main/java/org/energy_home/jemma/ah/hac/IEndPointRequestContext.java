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
 * This interface is used to access context information associated to a request
 * sent to a specific cluster ({@link IServiceCluster}). A {@code null} value
 * can be specified when requests are sent through a connection established by
 * the framework (the framework generates a default context using information
 * associated to the end points connection).
 * 
 */
public interface IEndPointRequestContext {
	/*
	 * public static final int REQUEST_ID_UNDEFINED = -1;
	 * 
	 * public int getRequestId(); public void setRequestId(int requestId);
	 */
	/**
	 * Checks if this request context specifies that a confirmation is needed
	 * for all requests, even those that doesn't return any values
	 * 
	 * @return {@code true} if a confirmation is always required, {@code false}
	 *         otherwise
	 */
	public boolean isConfirmationRequired();

	/**
	 * Set the request context by specifying if a confirmation is always needed
	 * for all requests
	 * 
	 * @param isConfirmationRequired
	 *            {@code true} if a confirmation is always required,
	 *            {@code false} otherwise
	 */
	public void setConfirmationRequired(boolean isConfirmationRequired);

	/**
	 * Returns the max age that is accepted for a read request of an attribute
	 * value using this context (appliances can used cached values read from a
	 * device if their age is less that the specified max age)
	 * 
	 * @return maxAgeForAttributeValues max age value expressed in milliseconds
	 * 
	 */
	public long getMaxAgeForAttributeValues();

	/**
	 * Set the max age that is accepted for a read request of an attribute value
	 * using this context (appliances can used cached values read from a device
	 * if their age is less that the specified max age)
	 * 
	 * @param maxAgeForAttributeValues
	 *            max age value expressed in milliseconds
	 * 
	 */
	public void setMaxAgeForAttributeValues(long maxAgeForAttributeValues);

	/**
	 * Returns the peer end point associated to the service cluster request
	 * 
	 * @return The {@link IEndPoint} interface that can be used to identify the
	 *         issuer of this request
	 */
	public IEndPoint getPeerEndPoint();

}
