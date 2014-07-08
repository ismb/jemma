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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.9-03/31/2009 04:14 PM(snajper)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.03 at 05:23:14 PM CEST 
//


package org.energy_home.jemma.zgd.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NWKMessageResult complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NWKMessageResult">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="NWKStatus" type="{http://www.w3.org/2001/XMLSchema}unsignedShort"/>
 *         &lt;element name="NsduHandle" type="{http://www.w3.org/2001/XMLSchema}unsignedByte"/>
 *         &lt;element name="TxTime" type="{http://www.zigbee.org/GWGSchema}unsigned32Bit"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NWKMessageResult", propOrder = {
    "nwkStatus",
    "nsduHandle",
    "txTime"
})
public class NWKMessageResult {

    @XmlElement(name = "NWKStatus")
    @XmlSchemaType(name = "unsignedShort")
    protected int nwkStatus;
    @XmlElement(name = "NsduHandle")
    @XmlSchemaType(name = "unsignedByte")
    protected short nsduHandle;
    @XmlElement(name = "TxTime")
    protected long txTime;

    /**
     * Gets the value of the nwkStatus property.
     * 
     */
    public int getNWKStatus() {
        return nwkStatus;
    }

    /**
     * Sets the value of the nwkStatus property.
     * 
     */
    public void setNWKStatus(int value) {
        this.nwkStatus = value;
    }

    /**
     * Gets the value of the nsduHandle property.
     * 
     */
    public short getNsduHandle() {
        return nsduHandle;
    }

    /**
     * Sets the value of the nsduHandle property.
     * 
     */
    public void setNsduHandle(short value) {
        this.nsduHandle = value;
    }

    /**
     * Gets the value of the txTime property.
     * 
     */
    public long getTxTime() {
        return txTime;
    }

    /**
     * Sets the value of the txTime property.
     * 
     */
    public void setTxTime(long value) {
        this.txTime = value;
    }

}
