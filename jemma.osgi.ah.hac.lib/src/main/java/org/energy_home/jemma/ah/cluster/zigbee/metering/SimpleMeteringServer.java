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
package org.energy_home.jemma.ah.cluster.zigbee.metering;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface SimpleMeteringServer {

	final static String ATTR_CurrentSummationDelivered_NAME = "CurrentSummationDelivered";
	final static String ATTR_CurrentSummationReceived_NAME = "CurrentSummationReceived";
	final static String ATTR_PowerFactor_NAME = "PowerFactor";
	final static String ATTR_Status_NAME = "Status";
	final static String ATTR_UnitOfMeasure_NAME = "UnitOfMeasure";
	final static String ATTR_Multiplier_NAME = "Multiplier";
	final static String ATTR_Divisor_NAME = "Divisor";
	final static String ATTR_SummationFormatting_NAME = "SummationFormatting";
	final static String ATTR_DemandFormatting_NAME = "DemandFormatting";
	final static String ATTR_MeteringDeviceType_NAME = "MeteringDeviceType";
	final static String ATTR_IstantaneousDemand_NAME = "IstantaneousDemand";

	public long getCurrentSummationDelivered(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getCurrentSummationReceived(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPowerFactor(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getUnitOfMeasure(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getMultiplier(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getDivisor(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getSummationFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getDemandFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getMeteringDeviceType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getIstantaneousDemand(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
