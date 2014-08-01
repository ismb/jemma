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

import org.energy_home.jemma.ah.hac.IEndPoint;

/**
 * This interface is associated to the client service cluster used to manage
 * some common appliance configuration parameters (name, location and category)
 * in the A@H framework.
 * <p>
 * This cluster can be added to the common management end point (
 * {@link IEndPoint#COMMON_END_POINT_ID}) by appliances that wants to read or
 * modify these peer appliances' configuration parameters.
 * 
 * @see ConfigServer
 * 
 */
public interface ConfigClient {
}
