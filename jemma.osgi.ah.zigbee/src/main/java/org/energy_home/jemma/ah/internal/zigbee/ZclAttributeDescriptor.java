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
package org.energy_home.jemma.ah.internal.zigbee;

import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclAbstractDataType;

public class ZclAttributeDescriptor implements IZclAttributeDescriptor {

	private Class javaClass = null;
	private String name = null;
	private ZclAbstractDataType zclAbstractDataType = null;
	private int attrId;
	private boolean isReportable = false;
	private int accessType;
	private int size;
	
	public ZclAttributeDescriptor(int attrId, String name, ZclAbstractDataType zclAbstractDataType, Class javaClass, boolean isReportable, int accessType) {
		this.attrId = attrId;
		this.name = name;
		this.zclAbstractDataType = zclAbstractDataType;
		this.javaClass = javaClass;
		this.isReportable  = isReportable;
		this.accessType = accessType;
		this.size = 0;
	}

	public Class getJavaClass() {
		return javaClass;
	}

	public String getName() {
		return name;
	}

	public ZclAbstractDataType zclGetDataType() {
		return zclAbstractDataType;
	}

	public int zclGetId() {
		return attrId;
	}
	public boolean isWritable() {
		return ((accessType == IZclAttributeDescriptor.ACCESS_RW) || (accessType == IZclAttributeDescriptor.ACCESS_RO));
	}

	public int accessMode() {
		return this.accessType;
	}

	public boolean isReportable() {
		return isReportable;
	}
}
