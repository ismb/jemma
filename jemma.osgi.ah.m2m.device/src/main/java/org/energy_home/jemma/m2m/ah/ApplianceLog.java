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
package org.energy_home.jemma.m2m.ah;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ApplianceLog", propOrder = {
    "logId",
    "logPayload"})
public class ApplianceLog {
	@XmlElement(name = "LogId")
	protected long logId;

	@XmlElement(name = "LogPayload")
	protected byte[] logPayload;

	public ApplianceLog() {}
	
	public ApplianceLog(long logId, byte[] logPayload) {
		this.logId = logId;
		this.logPayload = logPayload;
	}
	
	public long getLogId() {
		return logId;
	}

	public void setLogId(long logId) {
		this.logId = logId;
	}
	
	public byte[] getLogPayload() {
		return logPayload;
	}

	public void setLogPayload(byte[] logPayload) {
		this.logPayload = logPayload;
	}
	
	
}


//package org.energy_home.jemma.m2m.ah;
//
//import javax.xml.bind.annotation.XmlAccessType;
//import javax.xml.bind.annotation.XmlAccessorType;
//import javax.xml.bind.annotation.XmlElement;
//import javax.xml.bind.annotation.XmlSeeAlso;
//import javax.xml.bind.annotation.XmlType;
//
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlType(name = "ApplianceLog", propOrder = {
//    "value",
//    "duration"
//})
//@XmlSeeAlso({
//    FloatCDV.class
//})
//public class ApplianceLog {
//
//    @XmlElement(name = "Value")
//    protected Float value;
//    @XmlElement(name = "Duration")
//    protected long duration;
//
//    /**
//     * Gets the value of the value property.
//     * 
//     * @return
//     *     possible object is
//     *     {@link Float }
//     *     
//     */
//    public Float getValue() {
//        return value;
//    }
//
//    /**
//     * Sets the value of the value property.
//     * 
//     * @param value
//     *     allowed object is
//     *     {@link Float }
//     *     
//     */
//    public void setValue(Float value) {
//        this.value = value;
//    }
//
//    /**
//     * Gets the value of the duration property.
//     * 
//     */
//    public long getDuration() {
//        return duration;
//    }
//
//    /**
//     * Sets the value of the duration property.
//     * 
//     */
//    public void setDuration(long value) {
//        this.duration = value;
//    }
//
//}
