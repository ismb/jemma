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
package org.energy_home.jemma.internal.ah.eh.esp;


import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.ebrain.EnergyBrainCore;
import org.energy_home.jemma.ah.ebrain.IMeteringProxy;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;

public class SimpleMeteringServerObject extends ServiceCluster implements SimpleMeteringServer {
	
	private EnergyBrainCore energyBrain;
	
	public SimpleMeteringServerObject(EnergyBrainCore energyBrain) throws ApplianceException {
		super();
		this.energyBrain = energyBrain;
	}

	public long getCurrentSummationDelivered(IEndPointRequestContext context) throws ApplianceException {
		return (long) IMeteringProxy.INVALID_ENERGY_CONSUMPTION_VALUE;
	}

	public short getDemandFormatting(IEndPointRequestContext context) throws ApplianceException {
		return 3;
	}

	public int getIstantaneousDemand(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		String smartInfoId = energyBrain.getSmartInfoExchangeId();
		if (smartInfoId != null)
			return (int)(energyBrain.getIstantaneousDemandPower(smartInfoId));
		else 
			return (int) IMeteringProxy.INVALID_INSTANTANEOUS_POWER_VALUE;
	}

	public short getMeteringDeviceType(IEndPointRequestContext context) throws ApplianceException {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getPowerFactor(IEndPointRequestContext context) throws ApplianceException {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getStatus(IEndPointRequestContext context) throws ApplianceException {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getSummationFormatting(IEndPointRequestContext context) throws ApplianceException {
		return 3;
	}

	public short getUnitOfMeasure(IEndPointRequestContext context) throws ApplianceException {
		return 0; // Kw
	}

	public long getCurrentSummationReceived(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		throw new UnsupportedClusterAttributeException();
	}

	public int getMultiplier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return 1;
	}

	public int getDivisor(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return 1;
	}

}
