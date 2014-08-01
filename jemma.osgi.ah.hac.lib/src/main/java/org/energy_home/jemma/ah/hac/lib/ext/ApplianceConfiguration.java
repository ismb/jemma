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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;

/**
 * Appliance configuration object uses only end point specific configuration properties 
 *
 */
public class ApplianceConfiguration implements IApplianceConfiguration {
	public static final String AH_STATUS_PROPERTY_NAME = "ah.status";
	
	private Map endPointConfigs = null;
	private int[] endPointIds;
	
	private String getEndPointProperty(Integer endPointId, String applianceProperty, String endPointProperty) {
		int configId = 0;
		if (endPointId != null)
			configId = endPointId.intValue();
		
		String[] values = (String[]) endPointConfigs.get(endPointProperty);
		if (values != null) {
			for (int i = 0; i < endPointIds.length; i++) {
				if (configId == endPointIds[i])
					return values[i];
			}
		} else {
			return (String) endPointConfigs.get(applianceProperty);
		}
		return null;
	}
	
	private boolean updateEndPointProperty(Integer endPointId, String applianceProperty, String endPointProperty, String endPointValue) {
		boolean result = false;
		
		int configId = -1;
		if (endPointId != null) {
			configId = endPointId.intValue();
		}
		String applianceValue = null;
		if (configId <= 0) {
			applianceValue = endPointValue;
			endPointConfigs.put(applianceProperty, endPointValue);
		} else {
			applianceValue = (String) endPointConfigs.get(applianceProperty);
			if (applianceValue == null && endPointValue != null)
				endPointConfigs.put(applianceProperty, endPointValue);
		}

		String[] values = (String[]) endPointConfigs.get(endPointProperty);
		if (values == null) {
			values = new String[endPointIds.length];
			endPointConfigs.put(endPointProperty, values);
			for (int i = 0; i < endPointIds.length; i++) {
				values[i] = applianceValue;
			}
		}
		for (int i = 0; i < endPointIds.length; i++) {
			if (configId < 0 || configId == endPointIds[i]) {
				values[i] = endPointValue;
				result = true;
			} 
		}			

		return result;
	}
	
	private void initConfiguration(Map config, String applianceProperty, String endPointsProperty) {
		String value = (String) config.get(applianceProperty);
		if (value != null) {	
			endPointConfigs.put(applianceProperty, value);
		}
		String[] values = (String[]) config.get(endPointsProperty);
		if (values != null) {
			endPointConfigs.put(endPointsProperty, values);
		} 
	}
	
	public ApplianceConfiguration(int[] endPointIds, Map config) {
		this.endPointIds = endPointIds;
		this.endPointConfigs = new HashMap(5);
		String ahStatus = (String)config.get(AH_STATUS_PROPERTY_NAME);
		if (ahStatus != null && !ahStatus.equals(""))
			endPointConfigs.put(AH_STATUS_PROPERTY_NAME, ahStatus);
		endPointConfigs.put(IAppliance.APPLIANCE_PID, config.get(IAppliance.APPLIANCE_PID));
		endPointConfigs.put(IAppliance.APPLIANCE_TYPE_PROPERTY, config.get(IAppliance.APPLIANCE_TYPE_PROPERTY));
		initConfiguration(config, IAppliance.APPLIANCE_NAME_PROPERTY, IAppliance.END_POINT_NAMES_PROPERTY);
		initConfiguration(config, IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, IAppliance.END_POINT_CATEGORY_PIDS_PROPERTY);
		initConfiguration(config, IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, IAppliance.END_POINT_LOCATION_PIDS_PROPERTY);
		initConfiguration(config, IAppliance.APPLIANCE_ICON_PROPERTY, IAppliance.END_POINT_ICONS_PROPERTY);
	}
	
	public String getAppliancePid() {
		return (String)endPointConfigs.get(IAppliance.APPLIANCE_PID);
	}
	
	public int[] getEndPointIds() {
		return endPointIds;
	}
	
//	public boolean updateName(String value) {
//		boolean result = true;
//		for (int i = 0; i < endPointIds.length; i++) {
//			result = result & updateName(endPointIds[i], value);
//		}
//		return result;
//	}
//	
//	public boolean updateCategoryPid(String value) {
//		boolean result = true;
//		for (int i = 0; i < endPointIds.length; i++) {
//			result = result & updateCategoryPid(endPointIds[i], value);
//		}
//		return result;
//	}
//	
//	public boolean updateLocationPid(String value) {
//		boolean result = true;
//		for (int i = 0; i < endPointIds.length; i++) {
//			result = result & updateLocationPid(endPointIds[i], value);
//		}
//		return result;
//	}
//	
//	public boolean updateIconName(String value) {
//		boolean result = true;
//		for (int i = 0; i < endPointIds.length; i++) {
//			result = result & updateIconName(endPointIds[i], value);
//		}
//		return result;
//	}
	
	public boolean updateName(Integer endPointId, String value) {
		return updateEndPointProperty(endPointId, IAppliance.APPLIANCE_NAME_PROPERTY, IAppliance.END_POINT_NAMES_PROPERTY, value);
	}
	
	public boolean updateCategoryPid(Integer endPointId, String value) {
		return updateEndPointProperty(endPointId, IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, IAppliance.END_POINT_CATEGORY_PIDS_PROPERTY, value);
	}
	
	public boolean updateLocationPid(Integer endPointId, String value) {
		return updateEndPointProperty(endPointId, IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, IAppliance.END_POINT_LOCATION_PIDS_PROPERTY, value);
	}
	
	public boolean updateIconName(Integer endPointId, String value) {
		return updateEndPointProperty(endPointId, IAppliance.APPLIANCE_ICON_PROPERTY, IAppliance.END_POINT_ICONS_PROPERTY, value);

	}
	
	public String getName(Integer endPointId) {
		return getEndPointProperty(endPointId, IAppliance.APPLIANCE_NAME_PROPERTY, IAppliance.END_POINT_NAMES_PROPERTY);		
	}

	public String getCategoryPid(Integer endPointId) {
		return getEndPointProperty(endPointId,  IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, IAppliance.END_POINT_CATEGORY_PIDS_PROPERTY);	
	}

	public String getLocationPid(Integer endPointId) {
		return getEndPointProperty(endPointId, IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, IAppliance.END_POINT_LOCATION_PIDS_PROPERTY);	
	}

	public String getIconName(Integer endPointId) {
		return getEndPointProperty(endPointId, IAppliance.APPLIANCE_ICON_PROPERTY, IAppliance.END_POINT_ICONS_PROPERTY);	
	}
	
	public Map getConfigurationMap() {
		return Collections.unmodifiableMap(endPointConfigs);
	}

}
