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
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ParserLocker {

	public final static short INVALID_ID = (short) -1;

	private long id;
	private Status status;
	private TypeMessage type;

	private String _Key;

	private Object _objectOfResponse;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public void setId(long _id) {
		id = _id;
	}

	public ParserLocker() {
		id = new Date().getTime();
		status = new Status();
		status.setCode(INVALID_ID);
	}

	public TypeMessage getType() {
		return type;
	}

	public void setType(TypeMessage type) {
		this.type = type;
	}

	public Object get_objectOfResponse() {
		return _objectOfResponse;
	}

	public void set_objectOfResponse(Object _objectOfResponse) {
		this._objectOfResponse = _objectOfResponse;
	}

	public String get_Key() {
		return _Key;
	}

	public void set_Key(String _Key) {
		this._Key = _Key;
	}

}
