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
	 * in KW/hour for a given start/end interval. Cost unit is expressed in hundredth-millesimals of euro,
	 * Time units are 4-digit integer, the 2 highest are the hour and the 2 lowest are the minutes.
	 * Each Daily profile will be an array of such interval profiles.
	 *
	 */
public class ThreeTierDailyTariff extends DailyTariff {
	public static final float F1_TARIFF = 0.25f;
	public static final float F2_TARIFF = 0.170771f;
	public static final float F3_TARIFF = 0.09f;

	public ThreeTierDailyTariff() throws Exception {
		// to optimize tariff selection, tariffs must be ordered from the most convenient to to worst
		TariffIntervals[] workdays = new TariffIntervals[] {
			new TariffIntervals(F3_TARIFF, new int[] {000, 800, 2300, 2400}),
			new TariffIntervals(F2_TARIFF, new int[] {800, 900, 2000, 2300}),
			new TariffIntervals(F1_TARIFF, new int[] {900, 2000})
		};
			
		TariffIntervals[] saturday = new TariffIntervals[] {
			new TariffIntervals(F3_TARIFF, new int[] {000, 800, 2300, 2400}),
			new TariffIntervals(F2_TARIFF, new int[] {800, 2300})
		};
		TariffIntervals[] sunday = new TariffIntervals[] {
				new TariffIntervals(F3_TARIFF, new int[] {000,  2400})
		};
		
		for (int day = Calendar.MONDAY; day <= Calendar.FRIDAY; ++day) {
			setDailyTariff(workdays, day);
		}
		setDailyTariff(saturday, Calendar.SATURDAY);
		setDailyTariff(sunday, Calendar.SUNDAY);
	}
}
