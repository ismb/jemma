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
package org.energy_home.jemma.ah.ebrain;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarUtil {
	
	public static final int MINUTES_IN_ONE_TIME_SLOT = 1; // typically each slot is 1 minute (can be less).
	public static final int MILLISECONDS_IN_ONE_TIME_SLOT = 1000 * 60 * MINUTES_IN_ONE_TIME_SLOT;
	public static final int SLOTS_IN_ONE_HOUR = 60 / MINUTES_IN_ONE_TIME_SLOT;
	public static final int SLOTS_IN_ONE_DAY = 24 * SLOTS_IN_ONE_HOUR;
	public static final long MILLISECONDS_IN_ONE_DAY = 24 * 60 * 60 * 1000L;
	public static final float HOURS_IN_ONE_SLOT = MINUTES_IN_ONE_TIME_SLOT / 60.0f;
	
	private static int easterDay, easterMonth, mondayEasterDay, mondayEasterMonth;
	
	private static final SimpleDateFormat minutesFormatter = new SimpleDateFormat("EEE,kk:mm");
	private static final SimpleDateFormat secondsFormatter = new SimpleDateFormat("EEE,kk:mm:ss");
	
	private static long timeOffset;
	
	static {
		computeEasterDay();
		timeOffset = Calendar.getInstance().getTimeZone().getOffset(System.currentTimeMillis());
	}

	public static String toSecondString(long time) {
		return secondsFormatter.format(time);
	}
	public static String toMinuteString(long time) {
		return minutesFormatter.format(time);
	}
	public static String toMinuteString(Calendar c) {
		return minutesFormatter.format(c.getTimeInMillis());
	}
	public static String toMinuteString(int slot) {
		return minutesFormatter.format(getMillisOf(slot));
	}
	
	public static int getSlotOf(long millis) {
		Calendar c = Calendar.getInstance();
		int todayDay = c.get(Calendar.DAY_OF_YEAR);
		c.setTimeInMillis(millis);
		int timeDay = c.get(Calendar.DAY_OF_YEAR);
		int daySlots = (timeDay - todayDay) * SLOTS_IN_ONE_DAY;
		return daySlots + getSlotOf(c);
	}
	
	// problem with this is that the EPOC is GMT=0,
	// in Italy have GMT+1 plus the day-saving hour needs to be considered
	public static int getSlotOf2(long millis) {
		millis %= MILLISECONDS_IN_ONE_DAY;
		millis += timeOffset;
		return (int)(millis / MILLISECONDS_IN_ONE_TIME_SLOT);
	}
	
	public static int getSlotOf(Calendar c) {
		return getSlotOf(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
	}
	
	public static int getSlotOf(int hours, int minutes) {
		return (60 * hours + minutes) / MINUTES_IN_ONE_TIME_SLOT;
	}
	

	public static Calendar getCalendarOf(int slot) {
		Calendar c = Calendar.getInstance();
		int minutes = slot * MINUTES_IN_ONE_TIME_SLOT;
		c.set(Calendar.HOUR_OF_DAY, minutes / 60);
		c.set(Calendar.MINUTE, minutes % 60);
		return c;
	}
	
	public static long getMillisOf(int slot) {
		return getCalendarOf(slot).getTimeInMillis();
	}
	
	public static int slotsFromMinutes(int minutes) {
		int slot = minutes / MINUTES_IN_ONE_TIME_SLOT;
		// round to next slot if it exceeds a fraction slot
		if (minutes % MINUTES_IN_ONE_TIME_SLOT > 0) ++slot;
		return slot;
	}
	
	public static int slotsFromMillis(long millis) {
		// always assumes it needs to be rounded to the next slot
		return 1 + (int)(millis / MILLISECONDS_IN_ONE_TIME_SLOT);
	}
	
	public static int minutesFromSlots(int slots) {
		return slots * MINUTES_IN_ONE_TIME_SLOT;
	}

	public static int getSlotInterval(Calendar start, Calendar end) {
		int minutes = getMinutesInterval(start, end);
		return slotsFromMinutes(minutes);
	}

	public static int getMinutesInterval(Calendar start, Calendar end) {
		if (start.after(end)) throw new IllegalArgumentException("Start time must preceede End time.");

		int minutes = end.get(Calendar.MINUTE) - start.get(Calendar.MINUTE);
		minutes += 60 * (end.get(Calendar.HOUR_OF_DAY) - start.get(Calendar.HOUR_OF_DAY));
		minutes += 24 * 60 * Math.abs(end.get(Calendar.DAY_OF_WEEK) - start.get(Calendar.DAY_OF_WEEK));
		return minutes;
	}
	
	public static int getMinutesSinceMidnight(Calendar c) {
		return 60 * c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.MINUTE);
	}
	
	public static int getSecondsSinceMidnight(Calendar c) {
		return 3600 * c.get(Calendar.HOUR_OF_DAY) + 60 * c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
	}
	

	public static void setNextRoundHour(Calendar c) {
		// add an hour from now
		c.add(Calendar.HOUR_OF_DAY, 1);
		// reset all other fields fraction of the hour
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}
	
	public static void setNextRoundMinute(Calendar c) {
		// add a minute from now
		c.add(Calendar.MINUTE, 1);
		// reset all other fields fraction of the minute
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}
	
	
	/*
	 * Italian Bank Holidays
        01. gen 2012	Do	 Se 52	Capodanno
        06. gen 2012	Ve	 Se 01	Epifania
        08. apr 2012	Do	 Se 14	Pasqua
        09. apr 2012	Lu	 Se 15	Lunedi' di Pasqua (Pasquetta)
        25. apr 2012	Me	 Se 17	Liberazione Italia
        01. mag 2012	Ma	 Se 18	Festa del lavoro
        02. giu 2012	Sa	 Se 22	Festa della Repubblica Italia
        15. ago 2012	Me	 Se 33	Assunzione
        01. nov 2012	Gi	 Se 44	Ognissanti
        08. dic 2012	Sa	 Se 49	Immacolata Concezione
        25. dic 2012	Ma	 Se 52	Natale
		26. dic 2012	Me	 Se 52	Santo Stefano
	 */
	public static boolean isHolyday(Calendar c) {
		int month = c.get(Calendar.MONTH);
		int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		
		if (month == Calendar.JANUARY) {
			if (dayOfMonth == 1) return true; // Capodanno
			if (dayOfMonth == 6) return true; // Epifania
		}
		if (month == Calendar.APRIL && dayOfMonth == 25) return true; // Liberazione Italia
		if (month == Calendar.MAY && dayOfMonth == 1) return true; // Festa del lavoro
		if (month == Calendar.JUNE && dayOfMonth == 2) return true; // Festa della Repubblica Italia
		if (month == Calendar.AUGUST && dayOfMonth == 15) return true; // Assunzione
		if (month == Calendar.NOVEMBER && dayOfMonth == 1) return true; // Ognissanti
		if (month == Calendar.DECEMBER) {
			if (dayOfMonth == 8) return true; // Immacolata Concezione
			if (dayOfMonth == 25) return true; // Natale
			if (dayOfMonth == 26) return true; // Santo Stefano
		}
		if (month == easterMonth && dayOfMonth == easterDay) return true; // Pasqua
		if (month == mondayEasterMonth && dayOfMonth == mondayEasterDay) return true; // Pasquetta
		
		return false;
	}

  /*
   * Compute the day of the year that Easter falls on. Step names E1 E2 etc.,
   * are direct references to Knuth, Vol 1, p 155.
   */
	private static void computeEasterDay() {
	    int year, golden, century, x, z, d, epact, n;
	    
	    Calendar calendar = Calendar.getInstance();
	    year = calendar.get(Calendar.YEAR);
	    golden = (year % 19) + 1; /* E1: metonic cycle */
	    century = (year / 100) + 1; /* E2: e.g. 1984 was in 20th C */
	    x = (3 * century / 4) - 12; /* E3: leap year correction */
	    z = ((8 * century + 5) / 25) - 5; /* E3: sync with moon's orbit */
	    d = (5 * year / 4) - x - 10;
	    epact = (11 * golden + 20 + z - x) % 30; /* E5: epact */
	    if ((epact == 25 && golden > 11) || epact == 24) epact++;
	    n = 44 - epact;
	    n += 30 * (n < 21 ? 1 : 0); /* E6: */
	    n += 7 - ((d + n) % 7);
	    if (n > 31) {/* E7: */
	    	easterDay = n - 31;
	    	mondayEasterDay = easterDay + 1;
	    	easterMonth = mondayEasterMonth = Calendar.APRIL;
	    } else {
	    	easterDay = n;
	    	easterMonth = Calendar.MARCH;
	    	if (++n > 31) {
		    	mondayEasterDay = n - 31;
		    	mondayEasterMonth = Calendar.APRIL;
		    } else {
		    	mondayEasterDay = easterDay + 1;
		    	mondayEasterMonth = Calendar.APRIL;
		    }
	    }
	}
}
