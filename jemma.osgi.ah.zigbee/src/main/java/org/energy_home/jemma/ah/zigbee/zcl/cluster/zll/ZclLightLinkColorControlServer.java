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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.zll;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclLightLinkColorControlServer extends ZclServiceCluster implements ColorControlServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 768;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_CurrentHue_NAME, new ZclAttributeDescriptor(0,
				ZclLightLinkColorControlServer.ATTR_CurrentHue_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_CurrentSaturation_NAME, new ZclAttributeDescriptor(1,
				ZclLightLinkColorControlServer.ATTR_CurrentSaturation_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_RemainingTime_NAME, new ZclAttributeDescriptor(2,
				ZclLightLinkColorControlServer.ATTR_RemainingTime_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_CurrentX_NAME, new ZclAttributeDescriptor(3,
				ZclLightLinkColorControlServer.ATTR_CurrentX_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_CurrentY_NAME, new ZclAttributeDescriptor(4,
				ZclLightLinkColorControlServer.ATTR_CurrentY_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_DriftCompensation_NAME, new ZclAttributeDescriptor(5,
				ZclLightLinkColorControlServer.ATTR_DriftCompensation_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_CompensationText_NAME, new ZclAttributeDescriptor(6,
				ZclLightLinkColorControlServer.ATTR_CompensationText_NAME, new ZclDataTypeString(266), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorTemperature_NAME, new ZclAttributeDescriptor(7,
				ZclLightLinkColorControlServer.ATTR_ColorTemperature_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorMode_NAME, new ZclAttributeDescriptor(8,
				ZclLightLinkColorControlServer.ATTR_ColorMode_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_NumberOfPrimaries_NAME, new ZclAttributeDescriptor(16,
				ZclLightLinkColorControlServer.ATTR_NumberOfPrimaries_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary1X_NAME, new ZclAttributeDescriptor(17,
				ZclLightLinkColorControlServer.ATTR_Primary1X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary1Y_NAME, new ZclAttributeDescriptor(18,
				ZclLightLinkColorControlServer.ATTR_Primary1Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary1Intensity_NAME, new ZclAttributeDescriptor(19,
				ZclLightLinkColorControlServer.ATTR_Primary1Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary2X_NAME, new ZclAttributeDescriptor(21,
				ZclLightLinkColorControlServer.ATTR_Primary2X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary2Y_NAME, new ZclAttributeDescriptor(22,
				ZclLightLinkColorControlServer.ATTR_Primary2Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary2Intensity_NAME, new ZclAttributeDescriptor(23,
				ZclLightLinkColorControlServer.ATTR_Primary2Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary3X_NAME, new ZclAttributeDescriptor(25,
				ZclLightLinkColorControlServer.ATTR_Primary3X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary3Y_NAME, new ZclAttributeDescriptor(26,
				ZclLightLinkColorControlServer.ATTR_Primary3Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary3Intensity_NAME, new ZclAttributeDescriptor(27,
				ZclLightLinkColorControlServer.ATTR_Primary3Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary4X_NAME, new ZclAttributeDescriptor(32,
				ZclLightLinkColorControlServer.ATTR_Primary4X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary4Y_NAME, new ZclAttributeDescriptor(33,
				ZclLightLinkColorControlServer.ATTR_Primary4Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary4Intensity_NAME, new ZclAttributeDescriptor(34,
				ZclLightLinkColorControlServer.ATTR_Primary4Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary5X_NAME, new ZclAttributeDescriptor(36,
				ZclLightLinkColorControlServer.ATTR_Primary5X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary5Y_NAME, new ZclAttributeDescriptor(37,
				ZclLightLinkColorControlServer.ATTR_Primary5Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary5Intensity_NAME, new ZclAttributeDescriptor(38,
				ZclLightLinkColorControlServer.ATTR_Primary5Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary6X_NAME, new ZclAttributeDescriptor(40,
				ZclLightLinkColorControlServer.ATTR_Primary6X_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary6Y_NAME, new ZclAttributeDescriptor(41,
				ZclLightLinkColorControlServer.ATTR_Primary6Y_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_Primary6Intensity_NAME, new ZclAttributeDescriptor(42,
				ZclLightLinkColorControlServer.ATTR_Primary6Intensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_WhitePointX_NAME, new ZclAttributeDescriptor(48,
				ZclLightLinkColorControlServer.ATTR_WhitePointX_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_WhitePointY_NAME, new ZclAttributeDescriptor(49,
				ZclLightLinkColorControlServer.ATTR_WhitePointY_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointRX_NAME, new ZclAttributeDescriptor(50,
				ZclLightLinkColorControlServer.ATTR_ColorPointRX_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointRY_NAME, new ZclAttributeDescriptor(51,
				ZclLightLinkColorControlServer.ATTR_ColorPointRY_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointRIntensity_NAME, new ZclAttributeDescriptor(52,
				ZclLightLinkColorControlServer.ATTR_ColorPointRIntensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointGX_NAME, new ZclAttributeDescriptor(54,
				ZclLightLinkColorControlServer.ATTR_ColorPointGX_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointGY_NAME, new ZclAttributeDescriptor(55,
				ZclLightLinkColorControlServer.ATTR_ColorPointGY_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointGIntensity_NAME, new ZclAttributeDescriptor(56,
				ZclLightLinkColorControlServer.ATTR_ColorPointGIntensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointBX_NAME, new ZclAttributeDescriptor(58,
				ZclLightLinkColorControlServer.ATTR_ColorPointBX_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointBY_NAME, new ZclAttributeDescriptor(59,
				ZclLightLinkColorControlServer.ATTR_ColorPointBY_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorPointBIntensity_NAME, new ZclAttributeDescriptor(60,
				ZclLightLinkColorControlServer.ATTR_ColorPointBIntensity_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_EnhancedCurrentHue_NAME, new ZclAttributeDescriptor(16384,
				ZclLightLinkColorControlServer.ATTR_EnhancedCurrentHue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_EnhancedColorMode_NAME, new ZclAttributeDescriptor(16385,
				ZclLightLinkColorControlServer.ATTR_EnhancedColorMode_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorLoopActive_NAME, new ZclAttributeDescriptor(16386,
				ZclLightLinkColorControlServer.ATTR_ColorLoopActive_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorLoopDirection_NAME, new ZclAttributeDescriptor(16387,
				ZclLightLinkColorControlServer.ATTR_ColorLoopDirection_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorLoopTime_NAME, new ZclAttributeDescriptor(16388,
				ZclLightLinkColorControlServer.ATTR_ColorLoopTime_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorLoopStartEnhancedHue_NAME, new ZclAttributeDescriptor(16389,
				ZclLightLinkColorControlServer.ATTR_ColorLoopStartEnhancedHue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorLoopStoredEnhancedHue_NAME, new ZclAttributeDescriptor(16390,
				ZclLightLinkColorControlServer.ATTR_ColorLoopStoredEnhancedHue_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorCapabilities_NAME, new ZclAttributeDescriptor(16394,
				ZclLightLinkColorControlServer.ATTR_ColorCapabilities_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorTempPhysicalMin_NAME, new ZclAttributeDescriptor(16395,
				ZclLightLinkColorControlServer.ATTR_ColorTempPhysicalMin_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclLightLinkColorControlServer.ATTR_ColorTempPhysicalMax_NAME, new ZclAttributeDescriptor(16396,
				ZclLightLinkColorControlServer.ATTR_ColorTempPhysicalMax_NAME, new ZclDataTypeUI16(), null, true, 1));
	}

	public ZclLightLinkColorControlServer() throws ApplianceException {
		super();
	}

	protected int getProfileId() {
		return 260;
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

	public void execMoveToHue(short Hue, short Direction, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Hue);
		size += ZclDataTypeEnum8.zclSize(Direction);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI8.zclSerialize(zclFrame, Hue);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Direction);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveHue(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
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

	public void execStepHue(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
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

	public void execMoveToSaturation(short Saturation, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Saturation);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeUI8.zclSerialize(zclFrame, Saturation);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveSaturation(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(MoveMode);
		size += ZclDataTypeUI8.zclSize(Rate);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeEnum8.zclSerialize(zclFrame, MoveMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, Rate);
		issueExec(zclFrame, 11, context);
	}

	public void execStepSaturation(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(StepMode);
		size += ZclDataTypeUI8.zclSize(StepSize);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(5);
		ZclDataTypeEnum8.zclSerialize(zclFrame, StepMode);
		ZclDataTypeUI8.zclSerialize(zclFrame, StepSize);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveToHueAndSaturation(short Hue, short Saturation, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Hue);
		size += ZclDataTypeUI8.zclSize(Saturation);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeUI8.zclSerialize(zclFrame, Hue);
		ZclDataTypeUI8.zclSerialize(zclFrame, Saturation);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveToColor(int ColorX, int ColorY, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(ColorX);
		size += ZclDataTypeUI16.zclSize(ColorY);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(7);
		ZclDataTypeUI16.zclSerialize(zclFrame, ColorX);
		ZclDataTypeUI16.zclSerialize(zclFrame, ColorY);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveColor(int RateX, int RateY, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeI16.zclSize(RateX);
		size += ZclDataTypeI16.zclSize(RateY);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(8);
		ZclDataTypeI16.zclSerialize(zclFrame, RateX);
		ZclDataTypeI16.zclSerialize(zclFrame, RateY);
		issueExec(zclFrame, 11, context);
	}

	public void execStepColor(int StepX, int StepY, int TransitionTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeI16.zclSize(StepX);
		size += ZclDataTypeI16.zclSize(StepY);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(9);
		ZclDataTypeI16.zclSerialize(zclFrame, StepX);
		ZclDataTypeI16.zclSerialize(zclFrame, StepY);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public void execMoveToColorTemperature(short ColorTemperature, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(ColorTemperature);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(10);
		ZclDataTypeUI8.zclSerialize(zclFrame, ColorTemperature);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		issueExec(zclFrame, 11, context);
	}

	public short getCurrentHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getCurrentSaturation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(1, new Short(v));
		return v;
	}

	public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(2, new Integer(v));
		return v;
	}

	public int getCurrentX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(3, new Integer(v));
		return v;
	}

	public int getCurrentY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(4, new Integer(v));
		return v;
	}

	public short getDriftCompensation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(5, new Short(v));
		return v;
	}

	public String getCompensationText(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(6, v);
		return v;
	}

	public int getColorTemperature(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(7, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(7, new Integer(v));
		return v;
	}

	public short getColorMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(8, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(8, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(8, new Short(v));
		return v;
	}

	public short getNumberOfPrimaries(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(16, new Short(v));
		return v;
	}

	public int getPrimary1X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(17, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(17, new Integer(v));
		return v;
	}

	public int getPrimary1Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(18, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(18, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(18, new Integer(v));
		return v;
	}

	public short getPrimary1Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(19, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(19, new Short(v));
		return v;
	}

	public int getPrimary2X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(21, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(21, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(21, new Integer(v));
		return v;
	}

	public int getPrimary2Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(22, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(22, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(22, new Integer(v));
		return v;
	}

	public short getPrimary2Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(23, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(23, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(23, new Short(v));
		return v;
	}

	public int getPrimary3X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(25, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(25, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(25, new Integer(v));
		return v;
	}

	public int getPrimary3Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(26, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(26, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(26, new Integer(v));
		return v;
	}

	public short getPrimary3Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(27, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(27, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(27, new Short(v));
		return v;
	}

	public int getPrimary4X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(32, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(32, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(32, new Integer(v));
		return v;
	}

	public int getPrimary4Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(33, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(33, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(33, new Integer(v));
		return v;
	}

	public short getPrimary4Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(34, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(34, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(34, new Short(v));
		return v;
	}

	public int getPrimary5X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(36, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(36, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(36, new Integer(v));
		return v;
	}

	public int getPrimary5Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(37, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(37, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(37, new Integer(v));
		return v;
	}

	public short getPrimary5Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(38, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(38, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(38, new Short(v));
		return v;
	}

	public int getPrimary6X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(40, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(40, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(40, new Integer(v));
		return v;
	}

	public int getPrimary6Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(41, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(41, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(41, new Integer(v));
		return v;
	}

	public short getPrimary6Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(42, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(42, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(42, new Short(v));
		return v;
	}

	public int getWhitePointX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(48, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(48, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(48, new Integer(v));
		return v;
	}

	public int getWhitePointY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(49, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(49, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(49, new Integer(v));
		return v;
	}

	public int getColorPointRX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(50, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(50, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(50, new Integer(v));
		return v;
	}

	public int getColorPointRY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(51, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(51, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(51, new Integer(v));
		return v;
	}

	public short getColorPointRIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(52, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(52, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(52, new Short(v));
		return v;
	}

	public int getColorPointGX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(54, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(54, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(54, new Integer(v));
		return v;
	}

	public int getColorPointGY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(55, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(55, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(55, new Integer(v));
		return v;
	}

	public short getColorPointGIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(56, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(56, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(56, new Short(v));
		return v;
	}

	public int getColorPointBX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(58, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(58, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(58, new Integer(v));
		return v;
	}

	public int getColorPointBY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(59, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(59, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(59, new Integer(v));
		return v;
	}

	public short getColorPointBIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(60, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(60, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(60, new Short(v));
		return v;
	}

	public int getEnhancedCurrentHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16384, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16384, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16384, new Integer(v));
		return v;
	}

	public short getEnhancedColorMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16385, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16385, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(16385, new Short(v));
		return v;
	}

	public short getColorLoopActive(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16386, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16386, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(16386, new Short(v));
		return v;
	}

	public short getColorLoopDirection(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(16387, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16387, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(16387, new Short(v));
		return v;
	}

	public int getColorLoopTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16388, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16388, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16388, new Integer(v));
		return v;
	}

	public int getColorLoopStartEnhancedHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16389, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16389, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16389, new Integer(v));
		return v;
	}

	public int getColorLoopStoredEnhancedHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16390, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16390, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16390, new Integer(v));
		return v;
	}

	public int getColorCapabilities(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16394, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16394, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16394, new Integer(v));
		return v;
	}

	public int getColorTempPhysicalMin(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16395, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16395, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16395, new Integer(v));
		return v;
	}

	public int getColorTempPhysicalMax(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(16396, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(16396, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(16396, new Integer(v));
		return v;
	}

}
