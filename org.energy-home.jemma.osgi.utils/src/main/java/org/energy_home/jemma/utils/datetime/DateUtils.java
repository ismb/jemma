/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateUtils {
	public static final long DEFAULT_INITIAL_TIME = 1288566000000l; // 00:00 1st November 2010 (GMT+1)
	public static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

	public static final long MILLISEC_IN_ONE_MINUTE = 60000;
	public static final long MILLISEC_IN_ONE_HOUR = 3600000;
	public static final long MILLISEC_IN_ONE_DAY = 86400000;
	
	public static final int HOURS_IN_ONE_WEEK = 24 * 7;
	
	public static boolean isDateTimeOk() {
		return System.currentTimeMillis() > DateUtils.DEFAULT_INITIAL_TIME;
	}
	
	public static boolean isDateTimeOk(long timeInMillis) {
		return timeInMillis > DateUtils.DEFAULT_INITIAL_TIME;
	}

	public static XMLGregorianCalendar toXMLGregorianCalendar(long timestamp) {
		GregorianCalendar gc = new GregorianCalendar(GMT_TIME_ZONE);
		gc.setTimeInMillis(timestamp);

		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static long toTimestamp(XMLGregorianCalendar xgc) {
		return xgc.toGregorianCalendar().getTimeInMillis();
	}

	public static String toQueryStringValue(long timestamp) {
		XMLGregorianCalendar xgc = toXMLGregorianCalendar(timestamp);
		if (xgc != null)
			return xgc.toXMLFormat();
		return null;
	}

}
