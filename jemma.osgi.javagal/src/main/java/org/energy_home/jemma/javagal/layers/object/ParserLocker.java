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
package org.energy_home.jemma.javagal.layers.object;

import org.energy_home.jemma.zgd.jaxb.Status;

import java.util.Date;

/**
 * Helper class to manage synchronization's locks.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ParserLocker {

	/**
	 * Conventional invalid id value.
	 */
	public final static short INVALID_ID = (short) -1;

	private long id;
	private Status status;
	private TypeMessage type;

	private String _Key;

	private Object _objectOfResponse;

	/**
	 * Gets the status.
	 * 
	 * @return the status.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the status to set.
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id.
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param _id
	 *            the id to set.
	 */
	public void setId(long _id) {
		id = _id;
	}

	/**
	 * Creates a new instance.
	 */
	public ParserLocker() {
		id = new Date().getTime();
		status = new Status();
		status.setCode(INVALID_ID);
	}

	/**
	 * Gets the message's type.
	 * 
	 * @return the type of message.
	 */
	public TypeMessage getType() {
		return type;
	}

	/**
	 * Sets the message's type.
	 * 
	 * @param type
	 *            the message's type to set.
	 */
	public void setType(TypeMessage type) {
		this.type = type;
	}

	/**
	 * Gets the response's object.
	 * 
	 * @return the response's object.
	 */
	public Object get_objectOfResponse() {
		return _objectOfResponse;
	}

	/**
	 * Sets the response's object.
	 * 
	 * @param _objectOfResponse
	 *            the response's object to set.
	 */
	public void set_objectOfResponse(Object _objectOfResponse) {
		this._objectOfResponse = _objectOfResponse;
	}

	/**
	 * Gets the key.
	 * 
	 * @return the key.
	 */
	public String get_Key() {
		return _Key;
	}

	/**
	 * Sets the key.
	 * 
	 * @param _Key
	 *            the key to set.
	 */
	public void set_Key(String _Key) {
		this._Key = _Key;
	}

}
