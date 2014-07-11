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
package org.energy_home.jemma.ah.zigbee.zcl;

/**
 * This exception class is used to represent ZCL status codes.
 */
public class ZclException extends Exception {
	private static final long serialVersionUID = 1528742844625466431L;

	private int statusCode = -1;

	/**
	 * Creates an ZclException on the specified ZCL status code
	 * 
	 * @param errorDescription
	 *            describes the type of error
	 */
	public ZclException(int statusCode) {
		this.statusCode = statusCode;
	}

	public ZclException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * getter to retrieve the ZCL status code associated to this exception
	 * class. The ZCL status codes are well described in the ZigBee Clusster
	 * Library specifications
	 * 
	 * @return the ZCL status code.
	 */

	public int getStatusCode() {
		return this.statusCode;
	}
}
