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

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class ConfigServerCluster extends ServiceCluster implements ConfigServer {

	public ConfigServerCluster() throws ApplianceException {
		super();
	}

	public boolean isAvailable() {
		return true;
	}

	private String configName = null;
	private String configLocationPid = null;
	private String configCategoryPid = null;
	private String configIconName = null;

	public String getName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.configName;
	}

	public String getLocationPid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.configLocationPid;
	}

	public String getCategoryPid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.configCategoryPid;
	}

	public String getIconName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.configIconName;
	}

	// public void setCategoryPid(String categoryPid, IEndPointRequestContext
	// context) throws ApplianceException,
	// ServiceClusterException {
	// if (this.categoryPid != null && !this.categoryPid.equals(categoryPid) ||
	// (this.categoryPid == null && categoryPid != null)) {
	// this.categoryPid = categoryPid;
	// ((Appliance) this.getEndPoint().getAppliance()).configUpdated();
	// }
	// }
	//
	// public void setIconName(String iconName, IEndPointRequestContext context)
	// throws ApplianceException, ServiceClusterException {
	// if (this.iconName != null && !this.iconName.equals(iconName) ||
	// (this.iconName == null && iconName != null)) {
	// this.iconName = iconName;
	// ((Appliance) this.getEndPoint().getAppliance()).configUpdated();
	// }
	// }
	//
	// public void setLocationPid(String locationPid, IEndPointRequestContext
	// context) throws ApplianceException,
	// ServiceClusterException {
	// if (this.locationPid != null && !this.locationPid.equals(locationPid) ||
	// (this.locationPid == null && locationPid != null)) {
	// this.locationPid = locationPid;
	// ((Appliance) this.getEndPoint().getAppliance()).configUpdated();
	// }
	// }
	//
	// public void setName(String name, IEndPointRequestContext context) throws
	// ApplianceException, ServiceClusterException {
	// if (this.name != null && !this.name.equals(name) || (this.name == null &&
	// name != null)) {
	// this.name = name;
	// ((Appliance) this.getEndPoint().getAppliance()).configUpdated();
	//
	// }
	// }

	public String getConfigName() {
		return this.configName;
	}

	public String getConfigLocationPid() {
		return this.configLocationPid;
	}

	public String getConfigCategoryPid() {
		return this.configCategoryPid;
	}

	public String getConfigIconName() {
		return this.configIconName;
	}
	
	public void setConfigName(String name) {
		this.configName = name;
		this.notifyAttributeValue(ATTR_NAME_NAME, new AttributeValue(name, System.currentTimeMillis()));
	}

	public void setConfigLocationPid(String locationPid) {
		this.configLocationPid = locationPid;
		this.notifyAttributeValue(ATTR_NAME_LOCATION_PID, new AttributeValue(locationPid, System.currentTimeMillis()));
	}

	public void setConfigCategoryPid(String categoryPid) {
		this.configCategoryPid = categoryPid;
		this.notifyAttributeValue(ATTR_NAME_CATEGORY_PID, new AttributeValue(categoryPid, System.currentTimeMillis()));
	}

	public void setConfigIconName(String iconName) {
		this.configIconName = iconName;
		this.notifyAttributeValue(ATTR_NAME_ICON_NAME, new AttributeValue(iconName, System.currentTimeMillis()));
	}

}
