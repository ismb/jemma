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
 * Groups all error situations related to the interaction with an appliance that
 * are not tied to specific service cluster errors
 * 
 * @see ApplianceValidationException
 * @see ServiceClusterException
 * 
 */
public class ApplianceException extends Exception {
	private static final long serialVersionUID = -7037542567715808995L;

	/**
	 * Creates an {@code ApplianceException} on the specified error description
	 * 
	 * @param errorDescription
	 *            describes the type of error
	 */
	public ApplianceException(String errorDescription) {
		super(errorDescription);
	}
}
