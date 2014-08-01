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
package org.energy_home.jemma.ah.cluster.zigbee.eh;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface MeterIdentificationServer {

    final static String ATTR_CompanyName_NAME = "CompanyName";
    final static String ATTR_MeterTypeID_NAME = "MeterTypeID";
    final static String ATTR_DataQualityID_NAME = "DataQualityID";
    final static String ATTR_CustomerName_NAME = "CustomerName";
    final static String ATTR_Model_NAME = "Model";
    final static String ATTR_PartNumber_NAME = "PartNumber";
    final static String ATTR_ProductRevision_NAME = "ProductRevision";
    final static String ATTR_SoftwareRevision_NAME = "SoftwareRevision";
    final static String ATTR_UtilityName_NAME = "UtilityName";
    final static String ATTR_POD_NAME = "POD";
    final static String ATTR_AvailablePower_NAME = "AvailablePower";
    final static String ATTR_PowerThreshold_NAME = "PowerThreshold";

    public String getCompanyName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getMeterTypeID(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getDataQualityID(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public String getCustomerName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setCustomerName(String CustomerName, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public byte[] getModel(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public byte[] getPartNumber(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public byte[] getProductRevision(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public byte[] getSoftwareRevision(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public String getUtilityName(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public String getPOD(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getAvailablePower(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getPowerThreshold(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

}
