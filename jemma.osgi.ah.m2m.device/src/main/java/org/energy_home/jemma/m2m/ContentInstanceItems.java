//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.10 at 10:38:31 AM CET 
//


package org.energy_home.jemma.m2m;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://schemas.telecomitalia.it/m2m}ContentInstance" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.telecomitalia.it/m2m}AddressedIdAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "contentInstances"
})
@XmlRootElement(name = "ContentInstanceItems")
public class ContentInstanceItems
    extends M2MXmlObject
{

    @XmlElement(name = "ContentInstance")
    protected List<ContentInstance> contentInstances;
    @XmlAttribute(name = "AddressedId")
    @XmlSchemaType(name = "anyURI")
    protected String addressedId;

    /**
     * Gets the value of the contentInstances property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contentInstances property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContentInstances().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContentInstance }
     * 
     * 
     */
    public List<ContentInstance> getContentInstances() {
        if (contentInstances == null) {
            contentInstances = new ArrayList<ContentInstance>();
        }
        return this.contentInstances;
    }

    /**
     * 
     * The addressedId property is used to define the scope of contained resources id property
     *                         
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAddressedId() {
        return addressedId;
    }

    /**
     * Sets the value of the addressedId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAddressedId(String value) {
        this.addressedId = value;
    }

}
