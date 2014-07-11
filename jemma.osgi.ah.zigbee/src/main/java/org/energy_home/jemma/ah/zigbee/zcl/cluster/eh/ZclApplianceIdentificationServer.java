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

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceIdentificationServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeOctets;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclApplianceIdentificationServer extends ZclServiceCluster implements ApplianceIdentificationServer,
		ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2816;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_BasicIdentification_NAME, new ZclAttributeDescriptor(0,
				ZclApplianceIdentificationServer.ATTR_BasicIdentification_NAME, new ZclDataTypeOctets(7), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_CompanyName_NAME, new ZclAttributeDescriptor(16,
				ZclApplianceIdentificationServer.ATTR_CompanyName_NAME, new ZclDataTypeString(16), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_CompanyId_NAME, new ZclAttributeDescriptor(17,
				ZclApplianceIdentificationServer.ATTR_CompanyId_NAME, new ZclDataTypeUI16(), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_BrandName_NAME, new ZclAttributeDescriptor(18,
				ZclApplianceIdentificationServer.ATTR_BrandName_NAME, new ZclDataTypeString(16), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_BrandId_NAME, new ZclAttributeDescriptor(19,
				ZclApplianceIdentificationServer.ATTR_BrandId_NAME, new ZclDataTypeUI16(), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_Model_NAME, new ZclAttributeDescriptor(20,
				ZclApplianceIdentificationServer.ATTR_Model_NAME, new ZclDataTypeString(16), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_PartNumber_NAME, new ZclAttributeDescriptor(21,
				ZclApplianceIdentificationServer.ATTR_PartNumber_NAME, new ZclDataTypeString(16), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_ProductRevision_NAME, new ZclAttributeDescriptor(22,
				ZclApplianceIdentificationServer.ATTR_ProductRevision_NAME, new ZclDataTypeString(6), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_SoftwareRevision_NAME, new ZclAttributeDescriptor(23,
				ZclApplianceIdentificationServer.ATTR_SoftwareRevision_NAME, new ZclDataTypeString(6), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_ProductTypeName_NAME, new ZclAttributeDescriptor(24,
				ZclApplianceIdentificationServer.ATTR_ProductTypeName_NAME, new ZclDataTypeString(2), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_ProductTypeId_NAME, new ZclAttributeDescriptor(25,
				ZclApplianceIdentificationServer.ATTR_ProductTypeId_NAME, new ZclDataTypeUI16(), null, false, 1));
		attributesMapByName.put(ZclApplianceIdentificationServer.ATTR_CECEDSpecificationVersion_NAME, new ZclAttributeDescriptor(
				26, ZclApplianceIdentificationServer.ATTR_CECEDSpecificationVersion_NAME, new ZclDataTypeUI8(), null, false, 1));
	}

	public ZclApplianceIdentificationServer() throws ApplianceException {
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

	public byte[] getBasicIdentification(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			byte[] objectResult = null;
			objectResult = ((byte[]) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		byte[] v = ZclDataTypeOctets.zclParse(zclFrame);
		setCachedAttributeObject(0, v);
		return v;
	}

	public String getCompanyName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(16, v);
		return v;
	}

	public int getCompanyId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public String getBrandName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(18, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(18, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(18, v);
		return v;
	}

	public int getBrandId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(19, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(19, new Integer(v));
		return v;
	}

	public String getModel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(20, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(20, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(20, v);
		return v;
	}

	public String getPartNumber(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(21, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(21, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(21, v);
		return v;
	}

	public String getProductRevision(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(22, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(22, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(22, v);
		return v;
	}

	public String getSoftwareRevision(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(23, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(23, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(23, v);
		return v;
	}

	public String getProductTypeName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(24, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(24, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(24, v);
		return v;
	}

	public int getProductTypeId(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(25, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(25, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(25, new Integer(v));
		return v;
	}

	public short getCECEDSpecificationVersion(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(26, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(26, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(26, new Short(v));
		return v;
	}

}
