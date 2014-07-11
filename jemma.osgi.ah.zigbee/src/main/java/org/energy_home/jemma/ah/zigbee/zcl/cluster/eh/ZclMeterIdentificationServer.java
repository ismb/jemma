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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.eh;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.eh.MeterIdentificationServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI24;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;

public class ZclMeterIdentificationServer
    extends ZclServiceCluster
    implements MeterIdentificationServer, ZigBeeDeviceListener
{

    public final static short CLUSTER_ID = 2817;
    final static HashMap attributesMapByName = new HashMap();
    final static HashMap attributesMapById = new HashMap();

    static {
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_CompanyName_NAME, new ZclAttributeDescriptor(0, ZclMeterIdentificationServer.ATTR_CompanyName_NAME, new ZclDataTypeString(16), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_MeterTypeID_NAME, new ZclAttributeDescriptor(1, ZclMeterIdentificationServer.ATTR_MeterTypeID_NAME, new ZclDataTypeUI16(), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_DataQualityID_NAME, new ZclAttributeDescriptor(4, ZclMeterIdentificationServer.ATTR_DataQualityID_NAME, new ZclDataTypeUI16(), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_CustomerName_NAME, new ZclAttributeDescriptor(5, ZclMeterIdentificationServer.ATTR_CustomerName_NAME, new ZclDataTypeString(16), null, false, 0));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_Model_NAME, new ZclAttributeDescriptor(6, ZclMeterIdentificationServer.ATTR_Model_NAME, new ZclDataTypeOctets(16), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_PartNumber_NAME, new ZclAttributeDescriptor(7, ZclMeterIdentificationServer.ATTR_PartNumber_NAME, new ZclDataTypeOctets(16), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_ProductRevision_NAME, new ZclAttributeDescriptor(8, ZclMeterIdentificationServer.ATTR_ProductRevision_NAME, new ZclDataTypeOctets(6), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_SoftwareRevision_NAME, new ZclAttributeDescriptor(10, ZclMeterIdentificationServer.ATTR_SoftwareRevision_NAME, new ZclDataTypeOctets(6), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_UtilityName_NAME, new ZclAttributeDescriptor(11, ZclMeterIdentificationServer.ATTR_UtilityName_NAME, new ZclDataTypeString(16), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_POD_NAME, new ZclAttributeDescriptor(12, ZclMeterIdentificationServer.ATTR_POD_NAME, new ZclDataTypeString(16), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_AvailablePower_NAME, new ZclAttributeDescriptor(13, ZclMeterIdentificationServer.ATTR_AvailablePower_NAME, new ZclDataTypeI24(), null, false, 1));
        attributesMapByName.put(ZclMeterIdentificationServer.ATTR_PowerThreshold_NAME, new ZclAttributeDescriptor(14, ZclMeterIdentificationServer.ATTR_PowerThreshold_NAME, new ZclDataTypeI24(), null, false, 1));
    }

    public ZclMeterIdentificationServer()
        throws ApplianceException
    {
        super();
    }

    protected int getClusterId() {
        return CLUSTER_ID;
    }

    protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
        return ((IZclAttributeDescriptor) attributesMapByName.get(name));
    }

    protected IZclAttributeDescriptor getAttributeDescriptor(int id) {
        Iterator iterator = attributesMapByName.values().iterator();
        // FIXME: generate it and optimize!!!!
		for (; iterator.hasNext();) {
			IZclAttributeDescriptor attributeDescriptor = (IZclAttributeDescriptor) iterator.next();
			if (attributeDescriptor.zclGetId() == id)
				return attributeDescriptor;
		}
		return null;
    }

    public String getCompanyName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(0, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(0, v);
        return v;
    }

    public int getMeterTypeID(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(1, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(1, new Integer(v));
        return v;
    }

    public int getDataQualityID(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(4, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(4, new Integer(v));
        return v;
    }

    public void setCustomerName(String CustomerName, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int attrId = 5;
        int size = 3;
        size += ZclDataTypeString.zclSize(CustomerName);
        IZclFrame zclFrame = new ZclFrame(0, size);
        zclFrame.appendUInt16(attrId);
        zclFrame.appendUInt8(ZclDataTypeString.ZCL_DATA_TYPE);
        ZclDataTypeString.zclSerialize(zclFrame, CustomerName);
        issueSet(ZclMeterIdentificationServer.CLUSTER_ID, zclFrame, attrId, context);
    }

    public String getCustomerName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(5, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(5, v);
        return v;
    }

    public byte[] getModel(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            byte[] objectResult = null;
            objectResult = ((byte[]) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(6, context);
        byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
        setCachedAttributeObject(6, v);
        return v;
    }

    public byte[] getPartNumber(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            byte[] objectResult = null;
            objectResult = ((byte[]) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(7, context);
        byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
        setCachedAttributeObject(7, v);
        return v;
    }

    public byte[] getProductRevision(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            byte[] objectResult = null;
            objectResult = ((byte[]) getValidCachedAttributeObject(8, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(8, context);
        byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
        setCachedAttributeObject(8, v);
        return v;
    }

    public byte[] getSoftwareRevision(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            byte[] objectResult = null;
            objectResult = ((byte[]) getValidCachedAttributeObject(10, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(10, context);
        byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
        setCachedAttributeObject(10, v);
        return v;
    }

    public String getUtilityName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(11, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(11, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(11, v);
        return v;
    }

    public String getPOD(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(12, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(12, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(12, v);
        return v;
    }

    public int getAvailablePower(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(13, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(13, context);
        int v = ZclDataTypeI24 .zclParse(zclFrame);
        setCachedAttributeObject(13, new Integer(v));
        return v;
    }

    public int getPowerThreshold(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(14, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(14, context);
        int v = ZclDataTypeI24 .zclParse(zclFrame);
        setCachedAttributeObject(14, new Integer(v));
        return v;
    }

}
