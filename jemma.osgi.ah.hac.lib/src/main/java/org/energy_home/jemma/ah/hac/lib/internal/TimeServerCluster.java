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
package org.energy_home.jemma.ah.hac.lib.internal;

import java.util.Calendar;
import java.util.TimeZone;

import org.energy_home.jemma.ah.cluster.zigbee.general.TimeServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.InvalidValueException;
import org.energy_home.jemma.ah.hac.ReadOnlyAttributeException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class TimeServerCluster extends ServiceCluster implements TimeServer {
	// 00:00 1st November 2010 (GMT+1)
	private static final long DEFAULT_INITIAL_TIME = 1288566000000l;
	private static final long MILLISEC_IN_ONE_DAY = 86400000;
	private static final int MASTER_AND_MASTER_ZONE_DST_TIME_STATUS = 0x05;

	private static final long ZIGBEE_UTC_DELTA_SECONDS = 946684800;

	private static final long INVALID_TIME = 0xffffffffL;

	final static String[] supportedAttributes = { TimeServer.ATTR_Time_NAME, TimeServer.ATTR_TimeStatus_NAME,
			TimeServer.ATTR_TimeZone_NAME, TimeServer.ATTR_DstStart_NAME, TimeServer.ATTR_DstEnd_NAME,
			TimeServer.ATTR_DstShift_NAME, TimeServer.ATTR_StandardTime_NAME, TimeServer.ATTR_LocalTime_NAME,
			TimeServer.ATTR_LastSetTime_NAME };

	private static boolean isDateTimeOk() {
		return System.currentTimeMillis() > DEFAULT_INITIAL_TIME;
	}

	private int currentYear = -1;
	private Calendar calendar = Calendar.getInstance();
	private long dstStart, dstEnd, dstShift;

	public TimeServerCluster() throws ApplianceException {
		super();
		updateDstParameters();
	}

	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
			ServiceClusterException {
		return supportedAttributes;
	}

	private void updateDstParameters() {
		synchronized (calendar) {
			calendar.setTimeInMillis(System.currentTimeMillis());
			if (currentYear != calendar.get(Calendar.YEAR)) {
				dstShift = (calendar.getTimeZone().getDSTSavings()) / 1000;
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.set(calendar.get(Calendar.YEAR), 1, 1, 2, 0, 0);
				int offset = calendar.get(Calendar.DST_OFFSET);
				long millis = calendar.getTimeInMillis();
				int i = 0;
				int dayOfYear = calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
				while (offset == 0 && i < dayOfYear) {
					millis += MILLISEC_IN_ONE_DAY;
					calendar.setTimeInMillis(millis);
					offset = calendar.get(Calendar.DST_OFFSET);
					i++;
				}
				if (i >= dayOfYear)
					dstStart = INVALID_TIME;
				else
					dstStart = (millis - MILLISEC_IN_ONE_DAY) / 1000 - ZIGBEE_UTC_DELTA_SECONDS;
				i = 0;
				while (offset != 0 && i < dayOfYear) {
					millis += MILLISEC_IN_ONE_DAY;
					calendar.setTimeInMillis(millis);
					offset = calendar.get(Calendar.DST_OFFSET);
					i++;
				}
				if (i >= dayOfYear)
					dstEnd = INVALID_TIME;
				else
					dstEnd = (millis - MILLISEC_IN_ONE_DAY) / 1000 - ZIGBEE_UTC_DELTA_SECONDS;
				currentYear = calendar.get(Calendar.YEAR);
			}
		}
	}

	public short getTimeStatus(IEndPointRequestContext context) {
		return MASTER_AND_MASTER_ZONE_DST_TIME_STATUS;
	}

	public long getTime(IEndPointRequestContext context) {
		if (!isDateTimeOk()) {
			return INVALID_TIME;
		}
		long epoch = System.currentTimeMillis() / 1000;
		long time = epoch - ZIGBEE_UTC_DELTA_SECONDS;
		return time;
	}

	public long getTimeZone(IEndPointRequestContext context) {
		int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		Calendar c = Calendar.getInstance();
		return (offset - c.get(Calendar.DST_OFFSET)) / 1000;
	}

	public long getDstStart(IEndPointRequestContext context) {
		updateDstParameters();
		return dstStart;
	}

	public long getDstEnd(IEndPointRequestContext context) {
		updateDstParameters();
		return dstEnd;
	}

	public long getDstShift(IEndPointRequestContext context) {
		return dstShift;
	}

	public long getStandardTime(IEndPointRequestContext context) {
		long time = getTime(context);
		if (time == INVALID_TIME)
			return time;
		int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		Calendar c = Calendar.getInstance();
		return time + (offset - c.get(Calendar.DST_OFFSET)) / 1000;
	}

	public long getLocalTime(IEndPointRequestContext context) {
		long time = getTime(context);
		if (time == INVALID_TIME)
			return time;
		int offset = TimeZone.getDefault().getOffset(System.currentTimeMillis());
		return time + offset / 1000;
	}

	public long getLastSetTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new InvalidValueException();
	}

	public long getValidUntilTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}
	
	public void setTimeZone(long TimeZone, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new ReadOnlyAttributeException();
	}

	public void setDstStart(long DstStart, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new ReadOnlyAttributeException();
	}

	public void setDstEnd(long DstEnd, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new ReadOnlyAttributeException();
	}

	public void setDstShift(long DstShift, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new ReadOnlyAttributeException();
	}

	public void setValidUntilTime(long ValidUntilTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}
}