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
package org.energy_home.jemma.ah.cluster.zigbee.closures;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface DoorLockServer {

	final static String ATTR_LockState_NAME = "LockState";
	final static String ATTR_LockType_NAME = "LockType";
	final static String ATTR_ActuatorEnabled_NAME = "ActuatorEnabled";
	final static String ATTR_DoorState_NAME = "DoorState";
	final static String ATTR_DoorOpenEvents_NAME = "DoorOpenEvents";
	final static String ATTR_DoorClosedEvents_NAME = "DoorClosedEvents";
	final static String ATTR_OpenPeriod_NAME = "OpenPeriod";
	final static String ATTR_NumberofLogRecordsSupported_NAME = "NumberofLogRecordsSupported";
	final static String ATTR_NumberofTotalUsersSupported_NAME = "NumberofTotalUsersSupported";
	final static String ATTR_NumberofPINUsersSupported_NAME = "NumberofPINUsersSupported";
	final static String ATTR_NumberofRFIDUsersSupported_NAME = "NumberofRFIDUsersSupported";
	final static String ATTR_NumberofWeekDaySchedulesSupportedPerUser_NAME = "NumberofWeekDaySchedulesSupportedPerUser";
	final static String ATTR_NumberofYearDaySchedulesSupportedPerUser_NAME = "NumberofYearDaySchedulesSupportedPerUser";
	final static String ATTR_NumberofHolidaySchedulesSupported_NAME = "NumberofHolidaySchedulesSupported";
	final static String ATTR_MaxPINCodeLength_NAME = "MaxPINCodeLength";
	final static String ATTR_MinPINCodeLength_NAME = "MinPINCodeLength";
	final static String ATTR_MaxRFIDCodeLength_NAME = "MaxRFIDCodeLength";
	final static String ATTR_MinRFIDCodeLength_NAME = "MinRFIDCodeLength";
	final static String ATTR_EnableLogging_NAME = "EnableLogging";
	final static String ATTR_Language_NAME = "Language";
	final static String ATTR_LEDSettings_NAME = "LEDSettings";
	final static String ATTR_AutoRelockTime_NAME = "AutoRelockTime";
	final static String ATTR_SoundVolume_NAME = "SoundVolume";
	final static String ATTR_Operatingmode_NAME = "Operatingmode";
	final static String ATTR_SupportedOperatingModes_NAME = "SupportedOperatingModes";
	final static String ATTR_DefaultConfigurationRegister_NAME = "DefaultConfigurationRegister";
	final static String ATTR_EnableLocalProgramming_NAME = "EnableLocalProgramming";
	final static String ATTR_EnableOneTouchLocking_NAME = "EnableOneTouchLocking";
	final static String ATTR_EnableInsideStatusLED_NAME = "EnableInsideStatusLED";
	final static String ATTR_EnablePrivacyModeButton_NAME = "EnablePrivacyModeButton";
	final static String ATTR_Wrongcodeentrylimit_NAME = "Wrongcodeentrylimit";
	final static String ATTR_UserCodeTemporaryDisableTime_NAME = "UserCodeTemporaryDisableTime";
	final static String ATTR_SendPINovertheAir_NAME = "SendPINovertheAir";
	final static String ATTR_RequirePINforRFOperation_NAME = "RequirePINforRFOperation";
	final static String ATTR_ZigBeeSecurityLevel_NAME = "ZigBeeSecurityLevel";
	final static String ATTR_AlarmMask_NAME = "AlarmMask";
	final static String ATTR_KeypadOperationEventMask_NAME = "KeypadOperationEventMask";
	final static String ATTR_RFOperationEventMask_NAME = "RFOperationEventMask";
	final static String ATTR_ManualOperationEventMask_NAME = "ManualOperationEventMask";
	final static String ATTR_RFIDOperationEventMask_NAME = "RFIDOperationEventMask";
	final static String ATTR_KeypadProgrammingEventMask_NAME = "KeypadProgrammingEventMask";
	final static String ATTR_RFProgrammingEventMask_NAME = "RFProgrammingEventMask";
	final static String ATTR_RFIDProgrammingEventMask_NAME = "RFIDProgrammingEventMask";
	final static String CMD_LockDoorResponse_NAME = "LockDoorResponse";
	final static String CMD_UnlockDoorResponse_NAME = "UnlockDoorResponse";
	final static String CMD_ToggleResponse_NAME = "ToggleResponse";
	final static String CMD_UnlockWithTimeoutResponse_NAME = "UnlockWithTimeoutResponse";
	final static String CMD_GetLogRecordResponse_NAME = "GetLogRecordResponse";
	final static String CMD_SetPINCodeResponse_NAME = "SetPINCodeResponse";
	final static String CMD_GetPINCodeResponse_NAME = "GetPINCodeResponse";
	final static String CMD_ClearPINCodeResponse_NAME = "ClearPINCodeResponse";
	final static String CMD_ClearAllPINCodesResponse_NAME = "ClearAllPINCodesResponse";
	final static String CMD_SetUserStatusResponse_NAME = "SetUserStatusResponse";
	final static String CMD_GetUserStatusResponse_NAME = "GetUserStatusResponse";
	final static String CMD_SetWeekdayScheduleResponse_NAME = "SetWeekdayScheduleResponse";
	final static String CMD_GetWeekdayScheduleResponse_NAME = "GetWeekdayScheduleResponse";
	final static String CMD_ClearWeekdayScheduleResponse_NAME = "ClearWeekdayScheduleResponse";
	final static String CMD_SetYearDayScheduleResponse_NAME = "SetYearDayScheduleResponse";
	final static String CMD_GetYearDayScheduleResponse_NAME = "GetYearDayScheduleResponse";
	final static String CMD_ClearYearDayScheduleResponse_NAME = "ClearYearDayScheduleResponse";
	final static String CMD_SetHolidayScheduleResponse_NAME = "SetHolidayScheduleResponse";
	final static String CMD_GetHolidayScheduleResponse_NAME = "GetHolidayScheduleResponse";
	final static String CMD_ClearHolidayScheduleResponse_NAME = "ClearHolidayScheduleResponse";
	final static String CMD_SetUsertypeResponse_NAME = "SetUsertypeResponse";
	final static String CMD_GetUsertypeResponse_NAME = "GetUsertypeResponse";
	final static String CMD_SetRFIDCodeResponse_NAME = "SetRFIDCodeResponse";
	final static String CMD_GetRFIDCodeResponse_NAME = "GetRFIDCodeResponse";
	final static String CMD_ClearRFIDCodeResponse_NAME = "ClearRFIDCodeResponse";
	final static String CMD_ClearAllRFIDCodesResponse_NAME = "ClearAllRFIDCodesResponse";
	final static String CMD_OperationEventNotification_NAME = "OperationEventNotification";
	final static String CMD_ProgrammingEventNotification_NAME = "ProgrammingEventNotification";

	public short getLockState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getLockType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getActuatorEnabled(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getDoorState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getDoorOpenEvents(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getDoorClosedEvents(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getOpenPeriod(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofLogRecordsSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofTotalUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofPINUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofRFIDUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getNumberofWeekDaySchedulesSupportedPerUser(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getNumberofYearDaySchedulesSupportedPerUser(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getNumberofHolidaySchedulesSupported(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getMaxPINCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getMinPINCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getMaxRFIDCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getMinRFIDCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnableLogging(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getLanguage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getLEDSettings(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public long getAutoRelockTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getSoundVolume(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getOperatingmode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getSupportedOperatingModes(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getDefaultConfigurationRegister(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnableLocalProgramming(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnableOneTouchLocking(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnableInsideStatusLED(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getEnablePrivacyModeButton(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getWrongcodeentrylimit(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getUserCodeTemporaryDisableTime(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public boolean getSendPINovertheAir(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public boolean getRequirePINforRFOperation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getZigBeeSecurityLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getKeypadOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRFOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getManualOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRFIDOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getKeypadProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRFProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getRFIDProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execLockDoorResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execUnlockDoorResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execToggleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execUnlockWithTimeoutResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetLogRecordResponse(int LogEntryID, long Timestamp, short EventType, short Source, short EventIDAlarmCode,
			int UserID, String PIN, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execSetPINCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetPINCodeResponse(int UserID, short UserStatus, short UserType, String Code, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

	public void execClearPINCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execClearAllPINCodesResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetUserStatusResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetUserStatusResponse(int UserID, short UserStatus, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetWeekdayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetWeekdayScheduleResponse(short ScheduleID, int UserID, short Status, short DaysMask, short StartHour,
			short StartMinute, short EndHour, short EndMinute, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execClearWeekdayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetYearDayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetYearDayScheduleResponse(short ScheduleID, int UserID, short Status, long ZigBeeLocalStartTime,
			long ZigBeeLocalEndTime, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execClearYearDayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetHolidayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetHolidayScheduleResponse(short HolidayScheduleID, short Status, long ZigBeeLocalStartTime,
			long ZigBeeLocalEndTime, short OperatingModeDuringHoliday, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execClearHolidayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetUsertypeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetUsertypeResponse(int UserID, short UserType, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execSetRFIDCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execGetRFIDCodeResponse(int UserID, short UserStatus, short UserType, String RFIDCode,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execClearRFIDCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execClearAllRFIDCodesResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execOperationEventNotification(short OperationEventSource, short OperationEventCode, int UserID, short PIN,
			long ZigBeeLocalTime, String Data, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execProgrammingEventNotification(short ProgramEventSource, short OperationEventCode, int UserID, short PIN,
			short UserType, short UserStatus, long ZigBeeLocalTime, String Data, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException;

}
