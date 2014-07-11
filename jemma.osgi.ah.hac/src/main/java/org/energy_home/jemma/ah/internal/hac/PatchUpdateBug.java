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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatchUpdateBug {
		
	private static final Logger LOG = LoggerFactory.getLogger(PatchUpdateBug.class);

	public static boolean patchUpdateBugOnHac(BundleContext bc, String configFilename) {
		File srcFile = bc.getDataFile(configFilename);
		if (srcFile.exists()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("configuration file " + configFilename + " found in storage area of bundle "
						+ bc.getBundle().getBundleId());
			}
			Bundle[] bundles = bc.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				if (bundles[i].getSymbolicName().equals("org.energy_home.jemma.osgi.ah.hac.lib")) {
					// checks if in this bundle storage area there is an
					// available configuration in xml format.
					BundleContext hacLibBundleContext = bundles[i].getBundleContext();
					Version version = bundles[i].getVersion();
					if (hacLibBundleContext == null) {
						LOG.debug("The bundle has not yet started");
						// The bundle is has not been started, yet!!!
						return false;
					}
					//if (version.compareTo(new Version("3.0.0")) <= 0) {
						File dstFile = hacLibBundleContext.getDataFile(configFilename);
						if (!dstFile.exists()) {
							// copy the xml file into the current bundle storage
							// area
							if (copyfile(srcFile, dstFile)) {
								// copied successfully: delete the old file
								srcFile.delete();
								return true;
							}
						}
					//}
				}
			}
		}
		return false;
	}

	public static boolean copyfile(File srFile, File dtFile) {
		try {
			InputStream in = new FileInputStream(srFile);
			OutputStream out = new FileOutputStream(dtFile);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			LOG.trace("File copied.");
		} catch (FileNotFoundException ex) {
			LOG.warn(ex.getMessage() + " in the specified directory.", ex);
			return false;
		} catch (IOException e) {
			LOG.warn(e.getMessage(),e);
			return false;
		}
		return true;
	}
}
