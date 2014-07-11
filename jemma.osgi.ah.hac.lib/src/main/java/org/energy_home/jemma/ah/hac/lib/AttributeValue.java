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

import org.energy_home.jemma.ah.hac.IAttributeValue;

/**
 * Implementation of the {@code IAttributeValue} interface
 * 
 * @see IAttributeValue
 * 
 */
public class AttributeValue implements IAttributeValue {
	protected Object value = null;
	protected long timestamp = IAttributeValue.NO_TIMESTAMP;

	/**
	 * Creates a new attribute value and initializes the timestamp to the
	 * current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(Object value) {
		this.value = value;
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code boolean} attribute value and initializes the
	 * timestamp to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(boolean value) {
		this.value = new Boolean(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code int} attribute value and initializes the timestamp
	 * to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(int value) {
		this.value = new Integer(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code long} attribute value and initializes the timestamp
	 * to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(long value) {
		this.value = new Long(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code short} attribute value and initializes the timestamp
	 * to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(short value) {
		this.value = new Short(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code float} attribute value and initializes the timestamp
	 * to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(float value) {
		this.value = new Float(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code double} attribute value and initializes the
	 * timestamp to the current time
	 * 
	 * @param value
	 *            The attribute value
	 */
	public AttributeValue(double value) {
		this.value = new Double(value);
		this.timestamp = System.currentTimeMillis();
	}

	/**
	 * Creates a new {@code boolean} attribute value and specifies the timestamp
	 * 
	 * @param value
	 *            The attribute value
	 * @param timestamp
	 *            The timestamp associated to the attribute value
	 */
	public AttributeValue(Object value, long timestamp) {
		this.value = value;
		this.timestamp = timestamp;
	}

	public Object getValue() {
		return this.value;
	}

	void setValue(Object value) {
		this.value = value;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	protected void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
