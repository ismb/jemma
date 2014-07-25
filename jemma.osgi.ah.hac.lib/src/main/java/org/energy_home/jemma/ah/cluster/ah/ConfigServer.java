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
package org.energy_home.jemma.ah.cluster.ah;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceManager;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

/**
 * This interface is associated to the server service cluster used to manage
 * some common appliance configuration parameters (name, location and category)
 * in the A@H framework.
 * <p>
 * This cluster is automatically added to the common management end point (
 * {@link IEndPoint#COMMON_END_POINT_ID}) when an appliance is created and is
 * used to expose appliance configuration parameters to the framework and to
 * other appliances.
 * 
 */
public interface ConfigServer {
	/**
	 * Constant for the name of the service cluster attribute used to expose the
	 * name of an appliance in the A@H framework. The value is {@value} .
	 */
	public final static String ATTR_NAME_NAME = "Name";
	/**
	 * Constant for the name of the service cluster attribute used to expose the
	 * location pid associated to an appliance in the A@H framework. The value
	 * is {@value} .
	 * 
	 * @see IApplianceManager#getLocation(String)
	 */
	public final static String ATTR_NAME_LOCATION_PID = "LocationPid";
	/**
	 * Constant for the name of the service cluster attribute used to expose the
	 * category pid associated to an appliance in the A@H framework. The value
	 * is {@value} .
	 * 
	 * @see IApplianceManager#getCategory(String)
	 */
	public final static String ATTR_NAME_CATEGORY_PID = "CategoryPid";
	/**
	 * Constant for the name of the service cluster attribute used to expose the
	 * icon name associated to an appliance in the A@H framework. The value is *
	 * * {@value} .
	 */
	public final static String ATTR_NAME_ICON_NAME = "IconName";

	// TODO: check to add in all generated classes for zigbee clusters
	public final static String[] ATTR_NAMES = { ATTR_NAME_NAME, ATTR_NAME_LOCATION_PID, ATTR_NAME_CATEGORY_PID, ATTR_NAME_ICON_NAME };

	/**
	 * Reads the current name configured for an appliance in the A@H framework
	 * 
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @return The name of the appliance
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	public String getName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	/**
	 * Writes the name associated to the configuration of an appliance in the
	 * A@H framework
	 * 
	 * @param name
	 *            The new name associated to the appliance
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	// public void setName(String name, IEndPointRequestContext context) throws
	// ApplianceException, ServiceClusterException;

	/**
	 * Reads the current location pid configured for an appliance in the A@H
	 * framework
	 * 
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @return The location pid currently assigned to the appliance
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	public String getLocationPid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	/**
	 * Writes the location pid associated to the configuration of an appliance
	 * in the A@H framework
	 * 
	 * @param locationPid
	 *            The new location pid associated to the appliance
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	// public void setLocationPid(String locationPid, IEndPointRequestContext
	// context) throws ApplianceException,
	// ServiceClusterException;

	/**
	 * Reads the current category pid configured for an appliance in the A@H
	 * framework
	 * 
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @return The category pid currently assigned to the appliance
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	public String getCategoryPid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;
	
	/**
	 * Writes the current category pid associated to the configuration of an
	 * appliance in the A@H framework
	 * 
	 * @param categoryPid
	 *            The new category pid associated to the appliance
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	// public void setCategoryPid(String categoryPid, IEndPointRequestContext
	// context) throws ApplianceException,
	// ServiceClusterException;

	/**
	 * Reads the current icon name configured for an appliance in the A@H
	 * framework
	 * 
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @return The icon name currently assigned to the appliance
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	public String getIconName(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;	
	
	/**
	 * Writes the icon name associated to the configuration of an appliance in
	 * the A@H framework
	 * 
	 * @param name
	 *            The new icon name associated to the appliance
	 * @param context
	 *            The request context (see {@link IEndPointRequestContext})
	 * @throws ApplianceException
	 *             In case of generic errors
	 * @throws ServiceClusterException
	 *             In case of specific problems associated with the method
	 *             invoked on the service cluster
	 */
	// public void setIconName(String iconName, IEndPointRequestContext context)
	// throws ApplianceException, ServiceClusterException;

}
