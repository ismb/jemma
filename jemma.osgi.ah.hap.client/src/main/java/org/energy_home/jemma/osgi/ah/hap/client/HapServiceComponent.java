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
package org.energy_home.jemma.osgi.ah.hap.client;

import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.hap.client.M2MHapException;
import org.energy_home.jemma.ah.hap.client.lib.M2MHapServiceObject;
import org.energy_home.jemma.m2m.ContentInstance;
import org.energy_home.jemma.m2m.ContentInstanceItems;
import org.energy_home.jemma.m2m.ContentInstanceItemsList;
import org.energy_home.jemma.utils.datetime.DateTimeService;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;

public class HapServiceComponent extends M2MHapServiceObject implements IM2MHapService {
	private Bundle bundle;
	private DateTimeService dateTimeService;

	private static String getId(Bundle bundle) {
		if (bundle != null)
			return bundle.getSymbolicName() + "_" + bundle.getVersion();
		else
			return null;
	}

	private void checkDateTime() throws M2MHapException {
		if (dateTimeService == null || !dateTimeService.isDateTimeOk())
			throw new M2MHapException("Invalid current date/time");
	}
	
	public void start(ComponentContext context) {
		bundle = context.getUsingBundle();
		String user = getId(bundle);
		setUser(user);
	}

	public void stop(ComponentContext context) {
		release();
	}

	public void hapCoreServiceStarted(IHapCoreService hapService) {
		return;
	}
	
	public void hapCoreServiceStopped(IHapCoreService hapService) {
		return;
	}	
	
	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}
	
	public void unsetDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = null;
	}

	public ContentInstance getContentInstance(AHContainerAddress containerId, long instanceId) throws M2MHapException {
		checkDateTime();
		return super.getContentInstance(containerId, instanceId);
	}

	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerId, long instanceId)
			throws M2MHapException {
		checkDateTime();
		return super.getContentInstanceItemsList(containerId, instanceId);
	}

	public ContentInstanceItems getContentInstanceItems(AHContainerAddress containerId, long startInstanceId, long endInstanceId)
			throws M2MHapException {
		checkDateTime();
		return super.getContentInstanceItems(containerId, startInstanceId, endInstanceId);
	}

	public ContentInstanceItemsList getContentInstanceItemsList(AHContainerAddress containerIdFilter, long startInstanceId,
			long endInstanceId) throws M2MHapException {
		checkDateTime();
		return super.getContentInstanceItemsList(containerIdFilter, startInstanceId, endInstanceId);
	}

	public ContentInstance createContentInstanceBatch(AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstanceBatch(containerId, instanceId, content);
	}

	public ContentInstance createContentInstanceBatch(AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstanceBatch(containerId, contentInstance);
	}
	
	public ContentInstance createContentInstance(AHContainerAddress containerId, long instanceId, Object content)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstance(containerId, instanceId, content);
	}

	public ContentInstance createContentInstance(AHContainerAddress containerId, ContentInstance contentInstance)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstance(containerId, contentInstance);
	}

	public ContentInstance createContentInstanceQueued(AHContainerAddress containerId, long instanceId, Object content, boolean sync)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstanceQueued(containerId, instanceId, content, sync);
	}
	
	public ContentInstance createContentInstanceQueued(AHContainerAddress containerId, ContentInstance contentInstance, boolean sync)
			throws M2MHapException {
		checkDateTime();
		return super.createContentInstanceQueued(containerId, contentInstance, sync);
	}

}
