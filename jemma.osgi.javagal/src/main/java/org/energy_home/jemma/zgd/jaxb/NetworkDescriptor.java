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

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for NetworkDescriptor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NetworkDescriptor">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExtendedPanId" type="{http://www.zigbee.org/GWGSchema}unsigned64Bit" minOccurs="0"/>
 *         &lt;element name="LogicalChannel" type="{http://www.zigbee.org/GWGSchema}unsigned16Bit" minOccurs="0"/>
 *         &lt;element name="StackProfile" type="{http://www.zigbee.org/GWGSchema}unsignedNibble" minOccurs="0"/>
 *         &lt;element name="ZigBeeVersion" type="{http://www.zigbee.org/GWGSchema}unsignedNibble" minOccurs="0"/>
 *         &lt;element name="BeaconOrder" type="{http://www.zigbee.org/GWGSchema}unsignedNibble" minOccurs="0"/>
 *         &lt;element name="SuperFrameOrder" type="{http://www.zigbee.org/GWGSchema}unsignedNibble" minOccurs="0"/>
 *         &lt;element name="PermitJoining" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="RouterCapacity" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="EndDeviceCapacity" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="NWKRootURI" type="{http://www.w3.org/2001/XMLSchema}anyURI" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NetworkDescriptor", propOrder = {
    "extendedPanId",
    "logicalChannel",
    "stackProfile",
    "zigBeeVersion",
    "beaconOrder",
    "superFrameOrder",
    "permitJoining",
    "routerCapacity",
    "endDeviceCapacity",
    "nwkRootURI"
})
public class NetworkDescriptor {

    @XmlElement(name = "ExtendedPanId")
    protected BigInteger extendedPanId;
    @XmlElement(name = "LogicalChannel")
    protected Integer logicalChannel;
    @XmlElement(name = "StackProfile")
    protected Short stackProfile;
    @XmlElement(name = "ZigBeeVersion")
    protected Short zigBeeVersion;
    @XmlElement(name = "BeaconOrder")
    protected Short beaconOrder;
    @XmlElement(name = "SuperFrameOrder")
    protected Short superFrameOrder;
    @XmlElement(name = "PermitJoining")
    protected Boolean permitJoining;
    @XmlElement(name = "RouterCapacity")
    protected Boolean routerCapacity;
    @XmlElement(name = "EndDeviceCapacity")
    protected Boolean endDeviceCapacity;
    @XmlElement(name = "NWKRootURI")
    @XmlSchemaType(name = "anyURI")
    protected String nwkRootURI;

    /**
     * Gets the value of the extendedPanId property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getExtendedPanId() {
        return extendedPanId;
    }

    /**
     * Sets the value of the extendedPanId property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setExtendedPanId(BigInteger value) {
        this.extendedPanId = value;
    }

    /**
     * Gets the value of the logicalChannel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getLogicalChannel() {
        return logicalChannel;
    }

    /**
     * Sets the value of the logicalChannel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setLogicalChannel(Integer value) {
        this.logicalChannel = value;
    }

    /**
     * Gets the value of the stackProfile property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getStackProfile() {
        return stackProfile;
    }

    /**
     * Sets the value of the stackProfile property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setStackProfile(Short value) {
        this.stackProfile = value;
    }

    /**
     * Gets the value of the zigBeeVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getZigBeeVersion() {
        return zigBeeVersion;
    }

    /**
     * Sets the value of the zigBeeVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setZigBeeVersion(Short value) {
        this.zigBeeVersion = value;
    }

    /**
     * Gets the value of the beaconOrder property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getBeaconOrder() {
        return beaconOrder;
    }

    /**
     * Sets the value of the beaconOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setBeaconOrder(Short value) {
        this.beaconOrder = value;
    }

    /**
     * Gets the value of the superFrameOrder property.
     * 
     * @return
     *     possible object is
     *     {@link Short }
     *     
     */
    public Short getSuperFrameOrder() {
        return superFrameOrder;
    }

    /**
     * Sets the value of the superFrameOrder property.
     * 
     * @param value
     *     allowed object is
     *     {@link Short }
     *     
     */
    public void setSuperFrameOrder(Short value) {
        this.superFrameOrder = value;
    }

    /**
     * Gets the value of the permitJoining property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPermitJoining() {
        return permitJoining;
    }

    /**
     * Sets the value of the permitJoining property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPermitJoining(Boolean value) {
        this.permitJoining = value;
    }

    /**
     * Gets the value of the routerCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRouterCapacity() {
        return routerCapacity;
    }

    /**
     * Sets the value of the routerCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRouterCapacity(Boolean value) {
        this.routerCapacity = value;
    }

    /**
     * Gets the value of the endDeviceCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEndDeviceCapacity() {
        return endDeviceCapacity;
    }

    /**
     * Sets the value of the endDeviceCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEndDeviceCapacity(Boolean value) {
        this.endDeviceCapacity = value;
    }

    /**
     * Gets the value of the nwkRootURI property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNWKRootURI() {
        return nwkRootURI;
    }

    /**
     * Sets the value of the nwkRootURI property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNWKRootURI(String value) {
        this.nwkRootURI = value;
    }

}
