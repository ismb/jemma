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

import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyQueryResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifyServerCluster extends ServiceCluster implements IdentifyServer {
	private static final Logger LOG = LoggerFactory.getLogger(IdentifyServerCluster.class);

	final static String[] supportedAttributes = { IdentifyServer.ATTR_IdentifyTime_NAME };

	private static final long DEFAULT_MILLISEC_DELAY = 30000;

	private IdentifyService service;

	public IdentifyServerCluster(IdentifyService service) throws ApplianceException {
		super();
		this.service = service;
	}

	public int getIdentifyTime(IEndPointRequestContext context) throws ApplianceException {
		LOG.debug("getIdentifyTime");
		return (int) (service.getIdentifyDelay() / 1000);
	}

	public IdentifyQueryResponse execIdentifyQuery(IEndPointRequestContext context) throws ApplianceException {
		LOG.debug("execIdentifyQuery");
		return new IdentifyQueryResponse((int) (service.getIdentifyDelay() / 1000));
	}

	public void execIdentify(int IdentifyTime, IEndPointRequestContext context) throws ApplianceException {
		LOG.debug("execIdentify");
		service.setIdentifyDelay(1000 * IdentifyTime);
	}

	public void setIdentifyTime(int IdentifyTime, IEndPointRequestContext context) throws ApplianceException {
		LOG.debug("setIdentifyTime " + IdentifyTime);
		service.setIdentifyDelay(1000 * IdentifyTime);
	}

	public String[] getSupportedAttributeNames(IEndPointRequestContext endPointRequestContext) throws ApplianceException,
			ServiceClusterException {
		return supportedAttributes;
	}
}
