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

import java.util.Map;

public interface IApplianceConfiguration {

	public String getAppliancePid() ;
	
	public int[] getEndPointIds();
	
	// If end point id is null, the common end point name is returned
	public String getName(Integer endPointId);

	// If endPointId is null, all end point name are updated
	public boolean updateName(Integer endPointId, String value);
	
	// If end point id is null, the common end point categogry pid is returned
	public String getCategoryPid(Integer endPointId);

	// If endPointId is null, all end point category pid are updated
	public boolean updateCategoryPid(Integer endPointId, String value);
	
	// If end point id is null, the common end point location pid is returned
	public String getLocationPid(Integer endPointId);
	
	// If endPointId is null, all end points location pid are updated
	public boolean updateLocationPid(Integer endPointId, String value);
	
	// If end point id is null, the common end point icon name is returned
	public String getIconName(Integer endPointId);

	// If endPointId is null, all end points icon name are updated
	public boolean updateIconName(Integer endPointId, String value);
	
	public Map getConfigurationMap();

}