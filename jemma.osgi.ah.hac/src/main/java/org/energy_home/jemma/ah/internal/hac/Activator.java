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
package org.energy_home.jemma.ah.internal.hac;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {
	
	private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

	public void start(BundleContext bc) throws Exception {
		LOG.info("Bundle jemma.osgi.ah.hac starting");
		boolean enableUpdateBugPatch = getProperty("org.energy_home.jemma.ah.updatepatch", false);
		if (enableUpdateBugPatch) {
			PatchUpdateBug.patchUpdateBugOnHac(bc, "/hac-config.xml");
			PatchUpdateBug.patchUpdateBugOnHac(bc, "/cms-config.xml");
		}
	}

	public void stop(BundleContext arg0) throws Exception {

	}
	
	boolean getProperty(String name, boolean defaultValue) {
		String value = System.getProperty("org.energy_home.jemma.ah.updatepatch");
		
		if (value != null) {
			if (value.equals("true")) {
				return true;
			}
			else if (value.equals("false")) {
				return false;
			}
		}
		return defaultValue;	
	}
}
