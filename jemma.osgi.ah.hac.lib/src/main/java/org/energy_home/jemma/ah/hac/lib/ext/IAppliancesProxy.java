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
package org.energy_home.jemma.ah.hac.lib.ext;


import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAppliancesBasicProxy;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IConfigurationInfoService;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;

public interface IAppliancesProxy extends IAppliancesBasicProxy, IConfigurationInfoService {
	
	public abstract List getInstallingAppliances();
	
	public abstract List getInstallingAppliancePids();
	
	public abstract IAppliance getInstallingAppliance(String appliancePid);
	
	public IApplianceConfiguration getApplianceConfiguration(String appliancePid);
	
	public boolean updateApplianceConfiguration(IApplianceConfiguration config);
	
	public boolean installAppliance(String appliancePid); 
	
	public boolean deleteAppliance(String appliancePid);

	public abstract Object invokeClusterMethod(String appliancePid, Integer endPointId, String clusterName, String methodName, Object[] params) throws Exception;

	public abstract ISubscriptionParameters getAttributeSubscription(String appliancePid, Integer endPointId, String clusterName,
			String attributeName);

	public abstract ISubscriptionParameters setAttributeSubscription(String appliancePid, Integer endPointId, String clusterName,
			String attributeName, ISubscriptionParameters parameters);

	public abstract IAttributeValue getLastNotifiedAttributeValue(String appliancePid, Integer endPointId, String clusterName,
			String attributeName);

	public abstract Map getLastNotifiedAttributeValues(String appliancePid, Integer endPointId, String clusterName);
	
	public abstract IAttributeValue getLastReadAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName);

}