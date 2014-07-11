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
 * Represents a value of an attribute read from an appliance service cluster
 * 
 * @see IServiceCluster
 * @see IServiceClusterListener
 */
public interface IAttributeValue {
	/**
	 * Constant used to specify that no timestamp information is available. The
	 * value is {@value}
	 */
	public static final long NO_TIMESTAMP = -1;

	/**
	 * Returns the attribute value read from an appliance service cluster
	 * 
	 * @return An object whose class depends on specific service cluster
	 *         attributes
	 */
	public Object getValue();

	/**
	 * The timestamp associated to read operation for this attribute
	 * 
	 * @return Return the milliseconds since January 1, 1970, 00:00:00 GMT
	 *         (known as "epoch")
	 */
	public long getTimestamp();
}
