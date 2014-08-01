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
package org.energy_home.jemma.ah.hac.lib;

import org.energy_home.jemma.ah.hac.ISubscriptionParameters;

/**
 * Implementation of the {@code ISubscriptionParameters} interface
 * 
 * @see ISubscriptionParameters
 * 
 */
public class SubscriptionParameters implements ISubscriptionParameters {
	long minReportingInterval = 0;
	long maxReportingInterval = 0;

	double reportableChange = 0;

	/**
	 * This constructor initializes all the subscription parameters with {@code
	 * 0}, so that all modifications to the associated attribute value will be
	 * notified
	 */
	public SubscriptionParameters() {
		this.minReportingInterval = 0;
		this.maxReportingInterval = 0;
		this.reportableChange = 0.0;
	}

	/**
	 * This constructor explicitly specifies all the subscription parameters
	 * 
	 * @param minReportingInterval
	 *            The minimum reporting interval parameter
	 * @param maxReportingInterval
	 *            The maximum reporting interval parameter
	 * @param reportableChange
	 *            The minimum reportable change parameter
	 */
	public SubscriptionParameters(long minReportingInterval, long maxReportingInterval, double reportableChange) {
		this.minReportingInterval = minReportingInterval;
		this.maxReportingInterval = maxReportingInterval;
		this.reportableChange = reportableChange;

	}

	public long getMaxReportingInterval() {
		return this.maxReportingInterval;
	}

	public long getMinReportingInterval() {
		return this.minReportingInterval;
	}

	public double getReportableChange() {
		return this.reportableChange;
	}

}
