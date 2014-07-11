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
package org.energy_home.jemma.ah.cluster.zigbee.general;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface PowerConfigurationServer {

    final static String ATTR_MainsVoltage_NAME = "MainsVoltage";
    final static String ATTR_MainsFrequency_NAME = "MainsFrequency";
    final static String ATTR_MainsAlarmMask_NAME = "MainsAlarmMask";
    final static String ATTR_MainsVoltageMinThreshold_NAME = "MainsVoltageMinThreshold";
    final static String ATTR_MainsVoltageMaxThreshold_NAME = "MainsVoltageMaxThreshold";
    final static String ATTR_MainsVoltageDwellTripPoint_NAME = "MainsVoltageDwellTripPoint";
    final static String ATTR_BatteryVoltage_NAME = "BatteryVoltage";
    final static String ATTR_BatteryManufacturer_NAME = "BatteryManufacturer";
    final static String ATTR_BatterySize_NAME = "BatterySize";
    final static String ATTR_BatteryAHrRating_NAME = "BatteryAHrRating";
    final static String ATTR_BatteryQuantity_NAME = "BatteryQuantity";
    final static String ATTR_BatteryRatedVoltage_NAME = "BatteryRatedVoltage";
    final static String ATTR_BatteryAlarmMask_NAME = "BatteryAlarmMask";
    final static String ATTR_BatteryVoltageMinThreshold_NAME = "BatteryVoltageMinThreshold";

    public int getMainsVoltage(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getMainsFrequency(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getMainsAlarmMask(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setMainsAlarmMask(short MainsAlarmMask, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getMainsVoltageMinThreshold(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setMainsVoltageMinThreshold(int MainsVoltageMinThreshold, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getMainsVoltageMaxThreshold(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setMainsVoltageMaxThreshold(int MainsVoltageMaxThreshold, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getMainsVoltageDwellTripPoint(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setMainsVoltageDwellTripPoint(int MainsVoltageDwellTripPoint, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatteryVoltage(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public String getBatteryManufacturer(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryManufacturer(String BatteryManufacturer, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatterySize(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatterySize(short BatterySize, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public int getBatteryAHrRating(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryAHrRating(int BatteryAHrRating, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatteryQuantity(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryQuantity(short BatteryQuantity, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatteryRatedVoltage(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryRatedVoltage(short BatteryRatedVoltage, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatteryAlarmMask(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryAlarmMask(short BatteryAlarmMask, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public short getBatteryVoltageMinThreshold(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    ;

    public void setBatteryVoltageMinThreshold(short BatteryVoltageMinThreshold, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

}
