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


import java.util.Calendar;

import org.energy_home.jemma.ah.ebrain.algo.DailyTariff;
/*
 * Daily Tariff for the week. It assumes that each profile is a segment that specifies a tariff
 * in KW/hour for a given start/end interval. Cost unit is expressed in euros,
 * Time units are 4-digit integer, the 2 highest are the hour and the 2 lowest are the minutes.
 * Each Daily profile will be an array of such interval profiles.
 *
 */
public class TwoTierDailyTariff extends DailyTariff {
	public TwoTierDailyTariff() throws Exception {
		// to optimize tariff selection, tariffs must be ordered from the most convenient to to worst
		TariffIntervals[] workdays = new TariffIntervals[] {
			//new TariffIntervals(0.0395f, new int[] {000, 800, 1900, 2400}),
			//new TariffIntervals(0.1095f, new int[] {800, 1900})
			//new TariffIntervals(0.14537f, new int[] {000, 800, 1900, 2400}),
			//new TariffIntervals(0.16686f, new int[] {800, 1900})
			//new TariffIntervals(0.161961071181481f, new int[] {000, 800, 1900, 2400}),
			//new TariffIntervals(0.170771071181482f, new int[] {800, 1900})
			//new TariffIntervals(0.161961071181481f, new int[] {000, 800, 1900, 2400}),
			//new TariffIntervals(0.170771071181482f, new int[] {800, 1900})
			new TariffIntervals(0.19f, new int[] {000, 800, 1900, 2400}),
			new TariffIntervals(0.20f, new int[] {800, 1900})
		};
			
		TariffIntervals[] weekends = new TariffIntervals[] {
			//new TariffIntervals(0.14537f, new int[] {000, 2400})
			//new TariffIntervals(0.161961071181481f, new int[] {000, 2400})
			new TariffIntervals(0.19f, new int[] {000, 2400})
		};
		
		for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; ++day) {
			setDailyTariff(workdays, day);
		}
		setDailyTariff(weekends, Calendar.SATURDAY);
		setDailyTariff(weekends, Calendar.SUNDAY);
	}
}
