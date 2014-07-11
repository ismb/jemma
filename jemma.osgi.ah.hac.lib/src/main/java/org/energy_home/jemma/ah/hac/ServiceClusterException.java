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
 * Error situations related to the interaction with an attribute or command
 * exposed by a specific service cluster implemented by an appliance
 */
public class ServiceClusterException extends Exception {
	private static final long serialVersionUID = 7535837214076829691L;

	/**
	 * Creates a {@code ServiceClusterException} on the specified error
	 * description (TODO: remove this constructor)
	 * 
	 * @param errorDescription
	 *            describes the type of error
	 * 
	 */
	public ServiceClusterException(String errorDescription) {
		super(errorDescription);
	}
}
