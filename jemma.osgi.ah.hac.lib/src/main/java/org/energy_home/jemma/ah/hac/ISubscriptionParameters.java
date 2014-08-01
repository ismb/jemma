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

import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;

//* <ul>
//* <li>0,0,0 -> tutti i cambiamenti</li>
//* <li>1000,0,0 -> tutti i cambiamenti, ma non piu` frequentemente di 1 sec</li>
//* <li>0,10000,0 -> tutti i cambiamenti, in ogni caso un reporting ogni 10
//* secondi almeno (anche se non c'e` cambiamento)</li>
//* <li>1000,10000,0 -> tutti i cambiamenti, ma filtrati in modo da avere al piu`
//* un report ogni secondo e comunque sempre ogni 10 secondi</li>
//* <li>10000, 10000, * -> reporting fisso ogni 10 secondi</li>
//* </ul>

/**
 * Describes the parameters of a subscription for an appliance end point service
 * cluster's attribute
 * 
 */
public interface ISubscriptionParameters {

	public static final ISubscriptionParameters DEFAULT_SUBSCRIPTION_PARAMETERS = new SubscriptionParameters(2, 120, 0);
	/**
	 * Returns the minimum reporting interval parameter
	 * 
	 * @return The minimum reporting interval in seconds, <code>0</code> if no
	 *         minimum reporting interval is defined
	 */
	public long getMinReportingInterval();

	/**
	 * Returns the maximum reporting interval parameter
	 * 
	 * @return The maximum reporting interval in seconds, <code>0</code> if no
	 *         maximum reporting interval is defined
	 */
	public long getMaxReportingInterval();

	/**
	 * Returns the minimum reportable change parameter
	 * 
	 * @return The minimum reportable change depending on the attribute type,
	 *         <code>0</code> in case all changes need to be reported
	 */
	public double getReportableChange();

}
