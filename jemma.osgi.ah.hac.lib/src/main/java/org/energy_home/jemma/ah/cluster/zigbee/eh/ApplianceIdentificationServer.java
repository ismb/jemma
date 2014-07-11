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

public interface ApplianceIdentificationServer {

	final static String ATTR_BasicIdentification_NAME = "BasicIdentification";
	final static String ATTR_CompanyName_NAME = "CompanyName";
	final static String ATTR_CompanyId_NAME = "CompanyId";
	final static String ATTR_BrandName_NAME = "BrandName";
	final static String ATTR_BrandId_NAME = "BrandId";
	final static String ATTR_Model_NAME = "Model";
	final static String ATTR_PartNumber_NAME = "PartNumber";
	final static String ATTR_ProductRevision_NAME = "ProductRevision";
	final static String ATTR_SoftwareRevision_NAME = "SoftwareRevision";
	final static String ATTR_ProductTypeName_NAME = "ProductTypeName";
	final static String ATTR_ProductTypeId_NAME = "ProductTypeId";
	final static String ATTR_CECEDSpecificationVersion_NAME = "CECEDSpecificationVersion";

	public byte[] getBasicIdentification(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getCompanyName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCompanyId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getBrandName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getBrandId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getModel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getPartNumber(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getProductRevision(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getSoftwareRevision(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getProductTypeName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getProductTypeId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCECEDSpecificationVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
