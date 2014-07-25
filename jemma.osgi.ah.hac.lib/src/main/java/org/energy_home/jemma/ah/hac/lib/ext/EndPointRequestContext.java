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

import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;

/**
 * Implementation of the {@code IEndPointRequestContext} interface
 * 
 * @see IEndPointRequestContext
 * 
 */
public class EndPointRequestContext implements IEndPointRequestContext {

	private boolean isConfirmationRequired = true;
	private IEndPoint peerEndPoint = null;
	private long maxAgeForAttributeValues = 0;

	/**
	 * (TODO: to be removed)
	 */
	public static final IEndPointRequestContext ConfirmationRequired = new EndPointRequestContext(true, 0);

	/**
	 * Create a new end point with confirmation required set to {@code true} and
	 * no associated peer end point
	 */
	public EndPointRequestContext() {
	}

	/**
	 * Creates a new end point with confirmation required option set to the
	 * specified value and no associated peer end point
	 * 
	 * @param isConfirmationRequired
	 *            The confirmation required option
	 * @param maxAgeForAttributeValues
	 *            The max age in milliseconds accepted for values read for an attributes
	 */
	public EndPointRequestContext(boolean isConfirmationRequired, long maxAgeForAttributeValues) {
		this.isConfirmationRequired = isConfirmationRequired;
		this.maxAgeForAttributeValues = maxAgeForAttributeValues;
	}

	/**
	 * Creates a new end point with confirmation required option set to {@code
	 * true} and the specified associated peer end point
	 * 
	 * @param peerEndPoint
	 *            The associated peer end point
	 */
	public EndPointRequestContext(IEndPoint peerEndPoint) {
		this.peerEndPoint = peerEndPoint;
	}

	/**
	 * Creates a new end point with the specified confirmation required option
	 * and associated peer end point
	 * 
	 * @param peerEndPoint
	 *            The associated peer end point
	 * @param isConfirmationRequired
	 *            The confirmation required option
	 */
	public EndPointRequestContext(IEndPoint peerEndPoint, boolean isConfirmationRequired, long maxAgeForAttributeValues) {
		this.peerEndPoint = peerEndPoint;
		this.isConfirmationRequired = isConfirmationRequired;
		this.maxAgeForAttributeValues = maxAgeForAttributeValues;
	}

	public boolean isConfirmationRequired() {
		return this.isConfirmationRequired;
	}

	public void setConfirmationRequired(boolean isConfrmationRequired) {
		this.isConfirmationRequired = isConfrmationRequired;
	}

	public IEndPoint getPeerEndPoint() {
		return this.peerEndPoint;
	}

	protected void setPeerEndPoint(IEndPoint peerEndPoint) {
		this.peerEndPoint = peerEndPoint;
	}

	public long getMaxAgeForAttributeValues() {
		return this.maxAgeForAttributeValues;
	}

	public void setMaxAgeForAttributeValues(long maxAgeForAttributeValues) {
		this.maxAgeForAttributeValues = maxAgeForAttributeValues;
	}

}
