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
package org.energy_home.jemma.internal.shapi;

import org.energy_home.jemma.ah.cluster.zigbee.measurement.IlluminanceMeasurementClient;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.IlluminanceMeasurementServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hap.client.AHContainers;


public class IlluminanceMeasurementClusterProxy extends ServiceClusterProxy implements IlluminanceMeasurementClient {
	public IlluminanceMeasurementClusterProxy(ApplianceProxyList applianceProxyList, AHM2MHapService ahm2mHapService,
			ISubscriptionManager subscriptionManager) throws ApplianceException {
		super(applianceProxyList, ahm2mHapService, subscriptionManager);
	}

	protected String getAttributeId(String attributeName) {
		if (attributeName.equals(IlluminanceMeasurementServer.ATTR_MeasuredValue_NAME))
			return AHContainers.attrId_ah_cluster_illuminance_value;
		return null;
	}

	protected Object decodeAttributeValue(String appliancePid, int endPointId, String attributeName, Object value) {
		if (attributeName.equals(IlluminanceMeasurementServer.ATTR_MeasuredValue_NAME)) {
			//TODO:!!! to be tested
			int intValue = 10^((((Integer)value).intValue()-1)/10000);
			return new Integer(intValue);
		}
		return value;
	}

}
