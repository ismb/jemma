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
package org.energy_home.jemma.utils.datetime;

public class DateUtils {
	public static final long DEFAULT_INITIAL_TIME = 1288566000000l; // 00:00 1st November 2010 (GMT+1)

    public static final long MILLISEC_IN_ONE_MINUTE = 60000;
	public static final long MILLISEC_IN_ONE_HOUR = 3600000;
	public static final long MILLISEC_IN_ONE_DAY = 86400000;

    public static boolean isDateTimeOk() {
		return System.currentTimeMillis() > DateUtils.DEFAULT_INITIAL_TIME;
	}

}
