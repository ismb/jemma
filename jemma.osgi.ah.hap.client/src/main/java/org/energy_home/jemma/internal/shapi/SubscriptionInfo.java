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
package org.energy_home.jemma.internal.shapi;


import java.net.URI;
import java.net.URISyntaxException;

import org.energy_home.jemma.internal.ah.hap.client.AHM2MContainerAddress;
import org.energy_home.jemma.m2m.Subscription;

public class SubscriptionInfo {
	Subscription subscription;
	URI uri;
	AHM2MContainerAddress containerAddressFilter = null;

	SubscriptionInfo(Subscription subscription) throws URISyntaxException {
		this.subscription = subscription;
		this.uri = new URI(subscription.getContact());
		String addressedIdFilter = subscription.getAddressedIdFilter();
		try {
			if (addressedIdFilter != null)
				containerAddressFilter = new AHM2MContainerAddress(addressedIdFilter);			
		} catch (Exception e) {
			containerAddressFilter = null;
		}
	}
}
