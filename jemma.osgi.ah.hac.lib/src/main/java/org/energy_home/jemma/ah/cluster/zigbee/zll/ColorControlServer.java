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
package org.energy_home.jemma.ah.cluster.zigbee.zll;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface ColorControlServer {

	final static String ATTR_CurrentHue_NAME = "CurrentHue";
	final static String ATTR_CurrentSaturation_NAME = "CurrentSaturation";
	final static String ATTR_RemainingTime_NAME = "RemainingTime";
	final static String ATTR_CurrentX_NAME = "CurrentX";
	final static String ATTR_CurrentY_NAME = "CurrentY";
	final static String ATTR_DriftCompensation_NAME = "DriftCompensation";
	final static String ATTR_CompensationText_NAME = "CompensationText";
	final static String ATTR_ColorTemperature_NAME = "ColorTemperature";
	final static String ATTR_ColorMode_NAME = "ColorMode";
	final static String ATTR_NumberOfPrimaries_NAME = "NumberOfPrimaries";
	final static String ATTR_Primary1X_NAME = "Primary1X";
	final static String ATTR_Primary1Y_NAME = "Primary1Y";
	final static String ATTR_Primary1Intensity_NAME = "Primary1Intensity";
	final static String ATTR_Primary2X_NAME = "Primary2X";
	final static String ATTR_Primary2Y_NAME = "Primary2Y";
	final static String ATTR_Primary2Intensity_NAME = "Primary2Intensity";
	final static String ATTR_Primary3X_NAME = "Primary3X";
	final static String ATTR_Primary3Y_NAME = "Primary3Y";
	final static String ATTR_Primary3Intensity_NAME = "Primary3Intensity";
	final static String ATTR_Primary4X_NAME = "Primary4X";
	final static String ATTR_Primary4Y_NAME = "Primary4Y";
	final static String ATTR_Primary4Intensity_NAME = "Primary4Intensity";
	final static String ATTR_Primary5X_NAME = "Primary5X";
	final static String ATTR_Primary5Y_NAME = "Primary5Y";
	final static String ATTR_Primary5Intensity_NAME = "Primary5Intensity";
	final static String ATTR_Primary6X_NAME = "Primary6X";
	final static String ATTR_Primary6Y_NAME = "Primary6Y";
	final static String ATTR_Primary6Intensity_NAME = "Primary6Intensity";
	final static String ATTR_WhitePointX_NAME = "WhitePointX";
	final static String ATTR_WhitePointY_NAME = "WhitePointY";
	final static String ATTR_ColorPointRX_NAME = "ColorPointRX";
	final static String ATTR_ColorPointRY_NAME = "ColorPointRY";
	final static String ATTR_ColorPointRIntensity_NAME = "ColorPointRIntensity";
	final static String ATTR_ColorPointGX_NAME = "ColorPointGX";
	final static String ATTR_ColorPointGY_NAME = "ColorPointGY";
	final static String ATTR_ColorPointGIntensity_NAME = "ColorPointGIntensity";
	final static String ATTR_ColorPointBX_NAME = "ColorPointBX";
	final static String ATTR_ColorPointBY_NAME = "ColorPointBY";
	final static String ATTR_ColorPointBIntensity_NAME = "ColorPointBIntensity";
	final static String ATTR_EnhancedCurrentHue_NAME = "EnhancedCurrentHue";
	final static String ATTR_EnhancedColorMode_NAME = "EnhancedColorMode";
	final static String ATTR_ColorLoopActive_NAME = "ColorLoopActive";
	final static String ATTR_ColorLoopDirection_NAME = "ColorLoopDirection";
	final static String ATTR_ColorLoopTime_NAME = "ColorLoopTime";
	final static String ATTR_ColorLoopStartEnhancedHue_NAME = "ColorLoopStartEnhancedHue";
	final static String ATTR_ColorLoopStoredEnhancedHue_NAME = "ColorLoopStoredEnhancedHue";
	final static String ATTR_ColorCapabilities_NAME = "ColorCapabilities";
	final static String ATTR_ColorTempPhysicalMin_NAME = "ColorTempPhysicalMin";
	final static String ATTR_ColorTempPhysicalMax_NAME = "ColorTempPhysicalMax";
	final static String CMD_MoveToHue_NAME = "MoveToHue";
	final static String CMD_MoveHue_NAME = "MoveHue";
	final static String CMD_StepHue_NAME = "StepHue";
	final static String CMD_MoveToSaturation_NAME = "MoveToSaturation";
	final static String CMD_MoveSaturation_NAME = "MoveSaturation";
	final static String CMD_StepSaturation_NAME = "StepSaturation";
	final static String CMD_MoveToHueAndSaturation_NAME = "MoveToHueAndSaturation";
	final static String CMD_MoveToColor_NAME = "MoveToColor";
	final static String CMD_MoveColor_NAME = "MoveColor";
	final static String CMD_StepColor_NAME = "StepColor";
	final static String CMD_MoveToColorTemperature_NAME = "MoveToColorTemperature";

	public short getCurrentHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCurrentSaturation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getDriftCompensation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getCompensationText(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorTemperature(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getNumberOfPrimaries(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary1X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary1Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary1Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary2X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary2Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary2Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary3X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary3Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary3Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary4X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary4Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary4Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary5X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary5Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary5Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary6X(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPrimary6Y(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPrimary6Intensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getWhitePointX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getWhitePointY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointRX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointRY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorPointRIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointGX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointGY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorPointGIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointBX(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorPointBY(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorPointBIntensity(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getEnhancedCurrentHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getEnhancedColorMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorLoopActive(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getColorLoopDirection(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorLoopTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorLoopStartEnhancedHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorLoopStoredEnhancedHue(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorCapabilities(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorTempPhysicalMin(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getColorTempPhysicalMax(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execMoveToHue(short Hue, short Direction, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveHue(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStepHue(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveToSaturation(short Saturation, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveSaturation(short MoveMode, short Rate, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStepSaturation(short StepMode, short StepSize, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveToHueAndSaturation(short Hue, short Saturation, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveToColor(int ColorX, int ColorY, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execMoveColor(int RateX, int RateY, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execStepColor(int StepX, int StepY, int TransitionTime, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execMoveToColorTemperature(short ColorTemperature, int TransitionTime, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

}
