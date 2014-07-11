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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Version complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Version">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="VersionIdentifier" type="{http://www.w3.org/2001/XMLSchema}unsignedByte"/>
 *         &lt;element name="FeatureSetIdentifier" type="{http://www.w3.org/2001/XMLSchema}unsignedByte"/>
 *         &lt;element name="RPCProtocol" type="{http://www.zigbee.org/GWGSchema}RPCProtocol" maxOccurs="unbounded"/>
 *         &lt;element name="ManufacturerVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Version", propOrder = {
    "versionIdentifier",
    "featureSetIdentifier",
    "rpcProtocol",
    "manufacturerVersion"
})
public class Version implements Serializable{

    @XmlElement(name = "VersionIdentifier")
    @XmlSchemaType(name = "unsignedByte")
    protected short versionIdentifier;
    @XmlElement(name = "FeatureSetIdentifier")
    @XmlSchemaType(name = "unsignedByte")
    protected short featureSetIdentifier;
    @XmlElement(name = "RPCProtocol", required = true)
    protected List<RPCProtocol> rpcProtocol;
    @XmlElement(name = "ManufacturerVersion", required = true)
    protected String manufacturerVersion;

    /**
     * Gets the value of the versionIdentifier property.
     * 
     */
    public short getVersionIdentifier() {
        return versionIdentifier;
    }

    /**
     * Sets the value of the versionIdentifier property.
     * 
     */
    public void setVersionIdentifier(short value) {
        this.versionIdentifier = value;
    }

    /**
     * Gets the value of the featureSetIdentifier property.
     * 
     */
    public short getFeatureSetIdentifier() {
        return featureSetIdentifier;
    }

    /**
     * Sets the value of the featureSetIdentifier property.
     * 
     */
    public void setFeatureSetIdentifier(short value) {
        this.featureSetIdentifier = value;
    }

    /**
     * Gets the value of the rpcProtocol property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rpcProtocol property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRPCProtocol().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RPCProtocol }
     * 
     * 
     */
    public List<RPCProtocol> getRPCProtocol() {
        if (rpcProtocol == null) {
            rpcProtocol = new ArrayList<RPCProtocol>();
        }
        return this.rpcProtocol;
    }

    /**
     * Gets the value of the manufacturerVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufacturerVersion() {
        return manufacturerVersion;
    }

    /**
     * Sets the value of the manufacturerVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufacturerVersion(String value) {
        this.manufacturerVersion = value;
    }

}
