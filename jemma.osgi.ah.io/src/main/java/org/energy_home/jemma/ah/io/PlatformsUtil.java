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
package org.energy_home.jemma.ah.io;

public class PlatformsUtil {

	private static String os;
	private static String arch;

	public static String getOS() {
		if (os != null)
			return os;
		String osName = System.getProperties().getProperty("os.name"); //$NON-NLS-1$
		if (osName.regionMatches(true, 0, Constants.OS_WIN32, 0, 3))
			return Constants.OS_WIN32;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_SUNOS))
			return Constants.OS_SOLARIS;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_LINUX))
			return Constants.OS_LINUX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_AIX))
			return Constants.OS_AIX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_HPUX))
			return Constants.OS_HPUX;
		if (osName.regionMatches(true, 0, Constants.INTERNAL_OS_MACOSX, 0, Constants.INTERNAL_OS_MACOSX.length()))
			return Constants.OS_MACOSX;
		return Constants.OS_UNKNOWN;
	}

	public static String getArch() {
		if (arch != null)
			return arch;
		String name = System.getProperties().getProperty("os.arch");
		if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_I386))
			return Constants.INTERNAL_ARCH_X86;
		else if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_AMD64))
			return Constants.INTERNAL_ARCH_X86_64;
		else if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_ARM))
			return Constants.INTERNAL_ARCH_ARM;

		return name;
	}

	public static String replace(String str) {
		String os = PlatformsUtil.getOS();
		String arch = PlatformsUtil.getArch();
		str = str.replaceFirst("%os", os);
		str = str.replaceFirst("%arch", arch);
		return str;
	}
}
