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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for APSMessage complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="APSMessage">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DestinationAddressMode" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" minOccurs="0"/>
 *         &lt;element name="DestinationAddress" type="{http://www.zigbee.org/GWGSchema}Address"/>
 *         &lt;element name="DestinationEndpoint" type="{http://www.zigbee.org/GWGSchema}Endpoint"/>
 *         &lt;element name="SourceEndpoint" type="{http://www.zigbee.org/GWGSchema}Endpoint"/>
 *         &lt;element name="ProfileID" type="{http://www.zigbee.org/GWGSchema}ProfileIdentifier" minOccurs="0"/>
 *         &lt;element name="ClusterID" type="{http://www.zigbee.org/GWGSchema}ClusterIdentifier"/>
 *         &lt;element name="Data" type="{http://www.w3.org/2001/XMLSchema}hexBinary"/>
 *         &lt;element name="TxOptions" type="{http://www.zigbee.org/GWGSchema}TxOptions"/>
 *         &lt;element name="Radius" type="{http://www.w3.org/2001/XMLSchema}unsignedByte"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "APSMessage", propOrder = {
    "destinationAddressMode",
    "destinationAddress",
    "destinationEndpoint",
    "sourceEndpoint",
    "profileID",
    "clusterID",
    "data",
    "txOptions",
    "radius"
})
public class APSMessage implements Serializable {

    @XmlElement(name = "DestinationAddressMode")
    @XmlSchemaType(name = "unsignedInt")
    protected Long destinationAddressMode;
    @XmlElement(name = "DestinationAddress", required = true)
    protected Address destinationAddress;
    @XmlElement(name = "DestinationEndpoint")
    protected short destinationEndpoint;
    @XmlElement(name = "SourceEndpoint")
    protected short sourceEndpoint;
    @XmlElement(name = "ProfileID")
    protected Integer profileID;
    @XmlElement(name = "ClusterID")
    protected int clusterID;
    @XmlElement(name = "Data", required = true, type = String.class)
    @XmlJavaTypeAdapter(HexBinaryAdapter.class)
    @XmlSchemaType(name = "hexBinary")
    protected byte[] data;
    @XmlElement(name = "TxOptions", required = true)
    protected TxOptions txOptions;
    @XmlElement(name = "Radius")
    @XmlSchemaType(name = "unsignedByte")
    protected short radius;

    /**
     * Gets the value of the destinationAddressMode property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDestinationAddressMode() {
        return destinationAddressMode;
    }

    /**
     * Sets the value of the destinationAddressMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDestinationAddressMode(Long value) {
        this.destinationAddressMode = value;
    }

    /**
     * Gets the value of the destinationAddress property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getDestinationAddress() {
        return destinationAddress;
    }

    /**
     * Sets the value of the destinationAddress property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setDestinationAddress(Address value) {
        this.destinationAddress = value;
    }

    /**
     * Gets the value of the destinationEndpoint property.
     * 
     */
    public short getDestinationEndpoint() {
        return destinationEndpoint;
    }

    /**
     * Sets the value of the destinationEndpoint property.
     * 
     */
    public void setDestinationEndpoint(short value) {
        this.destinationEndpoint = value;
    }

    /**
     * Gets the value of the sourceEndpoint property.
     * 
     */
    public short getSourceEndpoint() {
        return sourceEndpoint;
    }

    /**
     * Sets the value of the sourceEndpoint property.
     * 
     */
    public void setSourceEndpoint(short value) {
        this.sourceEndpoint = value;
    }

    /**
     * Gets the value of the profileID property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getProfileID() {
        return profileID;
    }

    /**
     * Sets the value of the profileID property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setProfileID(Integer value) {
        this.profileID = value;
    }

    /**
     * Gets the value of the clusterID property.
     * 
     */
    public int getClusterID() {
        return clusterID;
    }

    /**
     * Sets the value of the clusterID property.
     * 
     */
    public void setClusterID(int value) {
        this.clusterID = value;
    }

    /**
     * Gets the value of the data property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Sets the value of the data property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setData(byte[] value) {
        this.data = ((byte[]) value);
    }

    /**
     * Gets the value of the txOptions property.
     * 
     * @return
     *     possible object is
     *     {@link TxOptions }
     *     
     */
    public TxOptions getTxOptions() {
        return txOptions;
    }

    /**
     * Sets the value of the txOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link TxOptions }
     *     
     */
    public void setTxOptions(TxOptions value) {
        this.txOptions = value;
    }

    /**
     * Gets the value of the radius property.
     * 
     */
    public short getRadius() {
        return radius;
    }

    /**
     * Sets the value of the radius property.
     * 
     */
    public void setRadius(short value) {
        this.radius = value;
    }

}
