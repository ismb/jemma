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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.general;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclLevelControlServer extends ZclServiceCluster implements LevelControlServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 8;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclLevelControlServer.ATTR_CurrentLevel_NAME, new ZclAttributeDescriptor(0,
				ZclLevelControlServer.ATTR_CurrentLevel_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLevelControlServer.ATTR_RemainingTime_NAME, new ZclAttributeDescriptor(1,
				ZclLevelControlServer.ATTR_RemainingTime_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLevelControlServer.ATTR_OnOffTransitionTime_NAME, new ZclAttributeDescriptor(16,
				ZclLevelControlServer.ATTR_OnOffTransitionTime_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLevelControlServer.ATTR_OnLevel_NAME, new ZclAttributeDescriptor(17,
				ZclLevelControlServer.ATTR_OnLevel_NAME, new ZclDataTypeUI8(), null, true, 1));
	}

	public ZclLevelControlServer() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		return ((IZclAttributeDescriptor) attributesMapByName.get(name));
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(int id) {
		Iterator iterator = attributesMapByName.values().iterator();
		// FIXME: generate it and optimize!!!!
		for (; iterator.hasNext();) {
			IZclAttributeDescriptor attributeDescriptor = (IZclAttributeDescriptor) iterator.next();
			if (attributeDescriptor.zclGetId() == id)
				return attributeDescriptor;
		}
		return null;
	}

	public void execMoveToLevel(short Level, int TransitionTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Level);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI8.zclSerialize(zclFrame, Level);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMove(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(MoveMode);
		size += ZclDataTypeUI8.zclSize(Rate);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeEnum8.zclSerialize(zclFrame, MoveMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, Rate);
		issueExec(zclFrame, 11, context);
	}

	public void execStep(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(StepMode);
		size += ZclDataTypeUI8.zclSize(StepSize);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);
		ZclDataTypeEnum8.zclSerialize(zclFrame, StepMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, StepSize);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execStop(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(3);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveToLevelWithOnOff(short Level, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Level);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeUI8.zclSerialize(zclFrame, Level);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveWithOnOff(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(MoveMode);
		size += ZclDataTypeUI8.zclSize(Rate);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(5);
		ZclDataTypeEnum8.zclSerialize(zclFrame, MoveMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, Rate);
		issueExec(zclFrame, 11, context);
	}

	public void execStepWithOnOff(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(StepMode);
		size += ZclDataTypeUI8.zclSize(StepSize);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeEnum8.zclSerialize(zclFrame, StepMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, StepSize);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execStopWithOnOff(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(7);
		issueExec(zclFrame, 11, context);
	}

	public short getCurrentLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

	public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(1, new Integer(v));
		return v;
	}

	public int getOnOffTransitionTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16, new Integer(v));
		return v;
	}

	public short getOnLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(17, new Short(v));
		return v;
	}

}
