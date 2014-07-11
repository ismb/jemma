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

public class FlatDailyTariff extends DailyTariff {
	public FlatDailyTariff() throws Exception {
		TariffIntervals[] weekday = new TariffIntervals[] {
			new TariffIntervals(0.16686f, new int[] {000, 2400})};
			//new TariffIntervals(1.0f, new int[] {000, 2400})};
		
		for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; ++day) {
			setDailyTariff(weekday, day);
		}
	}
}
