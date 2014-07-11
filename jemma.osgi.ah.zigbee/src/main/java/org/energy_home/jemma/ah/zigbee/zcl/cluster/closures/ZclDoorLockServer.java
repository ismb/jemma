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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclDoorLockServer extends ZclServiceCluster implements DoorLockServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 257;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclDoorLockServer.ATTR_LockState_NAME, new ZclAttributeDescriptor(0,
				ZclDoorLockServer.ATTR_LockState_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_LockType_NAME, new ZclAttributeDescriptor(1,
				ZclDoorLockServer.ATTR_LockType_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_ActuatorEnabled_NAME, new ZclAttributeDescriptor(2,
				ZclDoorLockServer.ATTR_ActuatorEnabled_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_DoorState_NAME, new ZclAttributeDescriptor(3,
				ZclDoorLockServer.ATTR_DoorState_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_DoorOpenEvents_NAME, new ZclAttributeDescriptor(4,
				ZclDoorLockServer.ATTR_DoorOpenEvents_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_DoorClosedEvents_NAME, new ZclAttributeDescriptor(5,
				ZclDoorLockServer.ATTR_DoorClosedEvents_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_OpenPeriod_NAME, new ZclAttributeDescriptor(6,
				ZclDoorLockServer.ATTR_OpenPeriod_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofLogRecordsSupported_NAME, new ZclAttributeDescriptor(16,
				ZclDoorLockServer.ATTR_NumberofLogRecordsSupported_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofTotalUsersSupported_NAME, new ZclAttributeDescriptor(17,
				ZclDoorLockServer.ATTR_NumberofTotalUsersSupported_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofPINUsersSupported_NAME, new ZclAttributeDescriptor(18,
				ZclDoorLockServer.ATTR_NumberofPINUsersSupported_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofRFIDUsersSupported_NAME, new ZclAttributeDescriptor(19,
				ZclDoorLockServer.ATTR_NumberofRFIDUsersSupported_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofWeekDaySchedulesSupportedPerUser_NAME, new ZclAttributeDescriptor(
				20, ZclDoorLockServer.ATTR_NumberofWeekDaySchedulesSupportedPerUser_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofYearDaySchedulesSupportedPerUser_NAME, new ZclAttributeDescriptor(
				21, ZclDoorLockServer.ATTR_NumberofYearDaySchedulesSupportedPerUser_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_NumberofHolidaySchedulesSupported_NAME, new ZclAttributeDescriptor(22,
				ZclDoorLockServer.ATTR_NumberofHolidaySchedulesSupported_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_MaxPINCodeLength_NAME, new ZclAttributeDescriptor(23,
				ZclDoorLockServer.ATTR_MaxPINCodeLength_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_MinPINCodeLength_NAME, new ZclAttributeDescriptor(24,
				ZclDoorLockServer.ATTR_MinPINCodeLength_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_MaxRFIDCodeLength_NAME, new ZclAttributeDescriptor(25,
				ZclDoorLockServer.ATTR_MaxRFIDCodeLength_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_MinRFIDCodeLength_NAME, new ZclAttributeDescriptor(26,
				ZclDoorLockServer.ATTR_MinRFIDCodeLength_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_EnableLogging_NAME, new ZclAttributeDescriptor(32,
				ZclDoorLockServer.ATTR_EnableLogging_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_Language_NAME, new ZclAttributeDescriptor(33,
				ZclDoorLockServer.ATTR_Language_NAME, new ZclDataTypeString(24), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_LEDSettings_NAME, new ZclAttributeDescriptor(34,
				ZclDoorLockServer.ATTR_LEDSettings_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_AutoRelockTime_NAME, new ZclAttributeDescriptor(35,
				ZclDoorLockServer.ATTR_AutoRelockTime_NAME, new ZclDataTypeUI32(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_SoundVolume_NAME, new ZclAttributeDescriptor(36,
				ZclDoorLockServer.ATTR_SoundVolume_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_Operatingmode_NAME, new ZclAttributeDescriptor(37,
				ZclDoorLockServer.ATTR_Operatingmode_NAME, new ZclDataTypeEnum8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_SupportedOperatingModes_NAME, new ZclAttributeDescriptor(38,
				ZclDoorLockServer.ATTR_SupportedOperatingModes_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_DefaultConfigurationRegister_NAME, new ZclAttributeDescriptor(39,
				ZclDoorLockServer.ATTR_DefaultConfigurationRegister_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_EnableLocalProgramming_NAME, new ZclAttributeDescriptor(40,
				ZclDoorLockServer.ATTR_EnableLocalProgramming_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_EnableOneTouchLocking_NAME, new ZclAttributeDescriptor(41,
				ZclDoorLockServer.ATTR_EnableOneTouchLocking_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_EnableInsideStatusLED_NAME, new ZclAttributeDescriptor(42,
				ZclDoorLockServer.ATTR_EnableInsideStatusLED_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_EnablePrivacyModeButton_NAME, new ZclAttributeDescriptor(43,
				ZclDoorLockServer.ATTR_EnablePrivacyModeButton_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_Wrongcodeentrylimit_NAME, new ZclAttributeDescriptor(48,
				ZclDoorLockServer.ATTR_Wrongcodeentrylimit_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_UserCodeTemporaryDisableTime_NAME, new ZclAttributeDescriptor(49,
				ZclDoorLockServer.ATTR_UserCodeTemporaryDisableTime_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_SendPINovertheAir_NAME, new ZclAttributeDescriptor(50,
				ZclDoorLockServer.ATTR_SendPINovertheAir_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_RequirePINforRFOperation_NAME, new ZclAttributeDescriptor(51,
				ZclDoorLockServer.ATTR_RequirePINforRFOperation_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_ZigBeeSecurityLevel_NAME, new ZclAttributeDescriptor(52,
				ZclDoorLockServer.ATTR_ZigBeeSecurityLevel_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_AlarmMask_NAME, new ZclAttributeDescriptor(64,
				ZclDoorLockServer.ATTR_AlarmMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_KeypadOperationEventMask_NAME, new ZclAttributeDescriptor(65,
				ZclDoorLockServer.ATTR_KeypadOperationEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_RFOperationEventMask_NAME, new ZclAttributeDescriptor(66,
				ZclDoorLockServer.ATTR_RFOperationEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_ManualOperationEventMask_NAME, new ZclAttributeDescriptor(67,
				ZclDoorLockServer.ATTR_ManualOperationEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_RFIDOperationEventMask_NAME, new ZclAttributeDescriptor(68,
				ZclDoorLockServer.ATTR_RFIDOperationEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_KeypadProgrammingEventMask_NAME, new ZclAttributeDescriptor(69,
				ZclDoorLockServer.ATTR_KeypadProgrammingEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_RFProgrammingEventMask_NAME, new ZclAttributeDescriptor(70,
				ZclDoorLockServer.ATTR_RFProgrammingEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
		attributesMapByName.put(ZclDoorLockServer.ATTR_RFIDProgrammingEventMask_NAME, new ZclAttributeDescriptor(71,
				ZclDoorLockServer.ATTR_RFIDProgrammingEventMask_NAME, new ZclDataTypeBitmap16(), null, true, 1));
	}

	public ZclDoorLockServer() throws ApplianceException {
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

	public void execLockDoorResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execUnlockDoorResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execToggleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execUnlockWithTimeoutResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeEnum8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeEnum8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetLogRecordResponse(int LogEntryID, long Timestamp, short EventType, short Source, short EventIDAlarmCode,
			int UserID, String PIN, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(LogEntryID);
		size += ZclDataTypeUI32.zclSize(Timestamp);
		size += ZclDataTypeEnum8.zclSize(EventType);
		size += ZclDataTypeUI8.zclSize(Source);
		size += ZclDataTypeUI8.zclSize(EventIDAlarmCode);
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeString.zclSize(PIN);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeUI16.zclSerialize(zclFrame, LogEntryID);
		ZclDataTypeUI32.zclSerialize(zclFrame, Timestamp);
		ZclDataTypeEnum8.zclSerialize(zclFrame, EventType);
		ZclDataTypeUI8.zclSerialize(zclFrame, Source);
		ZclDataTypeUI8.zclSerialize(zclFrame, EventIDAlarmCode);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeString.zclSerialize(zclFrame, PIN);
		issueExec(zclFrame, 11, context);
	}

	public void execSetPINCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(5);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetPINCodeResponse(int UserID, short UserStatus, short UserType, String Code, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(UserStatus);
		size += ZclDataTypeUI8.zclSize(UserType);
		size += ZclDataTypeString.zclSize(Code);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserStatus);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserType);
		ZclDataTypeString.zclSerialize(zclFrame, Code);
		issueExec(zclFrame, 11, context);
	}

	public void execClearPINCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(7);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execClearAllPINCodesResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(8);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execSetUserStatusResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(9);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetUserStatusResponse(int UserID, short UserStatus, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(UserStatus);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(10);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserStatus);
		issueExec(zclFrame, 11, context);
	}

	public void execSetWeekdayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(11);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetWeekdayScheduleResponse(short ScheduleID, int UserID, short Status, short DaysMask, short StartHour,
			short StartMinute, short EndHour, short EndMinute, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(ScheduleID);
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(Status);
		size += ZclDataTypeBitmap8.zclSize(DaysMask);
		size += ZclDataTypeUI8.zclSize(StartHour);
		size += ZclDataTypeUI8.zclSize(StartMinute);
		size += ZclDataTypeUI8.zclSize(EndHour);
		size += ZclDataTypeUI8.zclSize(EndMinute);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(12);
		ZclDataTypeUI8.zclSerialize(zclFrame, ScheduleID);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, DaysMask);
		ZclDataTypeUI8.zclSerialize(zclFrame, StartHour);
		ZclDataTypeUI8.zclSerialize(zclFrame, StartMinute);
		ZclDataTypeUI8.zclSerialize(zclFrame, EndHour);
		ZclDataTypeUI8.zclSerialize(zclFrame, EndMinute);
		issueExec(zclFrame, 11, context);
	}

	public void execClearWeekdayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(13);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execSetYearDayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(14);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetYearDayScheduleResponse(short ScheduleID, int UserID, short Status, long ZigBeeLocalStartTime,
			long ZigBeeLocalEndTime, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(ScheduleID);
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(Status);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalStartTime);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalEndTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(15);
		ZclDataTypeUI8.zclSerialize(zclFrame, ScheduleID);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalStartTime);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalEndTime);
		issueExec(zclFrame, 11, context);
	}

	public void execClearYearDayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(16);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execSetHolidayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(17);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetHolidayScheduleResponse(short HolidayScheduleID, short Status, long ZigBeeLocalStartTime,
			long ZigBeeLocalEndTime, short OperatingModeDuringHoliday, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(HolidayScheduleID);
		size += ZclDataTypeUI8.zclSize(Status);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalStartTime);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalEndTime);
		size += ZclDataTypeEnum8.zclSize(OperatingModeDuringHoliday);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(18);
		ZclDataTypeUI8.zclSerialize(zclFrame, HolidayScheduleID);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalStartTime);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalEndTime);
		ZclDataTypeEnum8.zclSerialize(zclFrame, OperatingModeDuringHoliday);
		issueExec(zclFrame, 11, context);
	}

	public void execClearHolidayScheduleResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(19);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execSetUsertypeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(20);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetUsertypeResponse(int UserID, short UserType, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeEnum8.zclSize(UserType);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(21);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeEnum8.zclSerialize(zclFrame, UserType);
		issueExec(zclFrame, 11, context);
	}

	public void execSetRFIDCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(22);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execGetRFIDCodeResponse(int UserID, short UserStatus, short UserType, String RFIDCode,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(UserStatus);
		size += ZclDataTypeUI8.zclSize(UserType);
		size += ZclDataTypeString.zclSize(RFIDCode);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(23);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserStatus);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserType);
		ZclDataTypeString.zclSerialize(zclFrame, RFIDCode);
		issueExec(zclFrame, 11, context);
	}

	public void execClearRFIDCodeResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(24);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execClearAllRFIDCodesResponse(short Status, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Status);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(25);
		ZclDataTypeUI8.zclSerialize(zclFrame, Status);
		issueExec(zclFrame, 11, context);
	}

	public void execOperationEventNotification(short OperationEventSource, short OperationEventCode, int UserID, short PIN,
			long ZigBeeLocalTime, String Data, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(OperationEventSource);
		size += ZclDataTypeUI8.zclSize(OperationEventCode);
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(PIN);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalTime);
		size += ZclDataTypeString.zclSize(Data);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(32);
		ZclDataTypeUI8.zclSerialize(zclFrame, OperationEventSource);
		ZclDataTypeUI8.zclSerialize(zclFrame, OperationEventCode);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, PIN);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalTime);
		ZclDataTypeString.zclSerialize(zclFrame, Data);
		issueExec(zclFrame, 11, context);
	}

	public void execProgrammingEventNotification(short ProgramEventSource, short OperationEventCode, int UserID, short PIN,
			short UserType, short UserStatus, long ZigBeeLocalTime, String Data, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(ProgramEventSource);
		size += ZclDataTypeUI8.zclSize(OperationEventCode);
		size += ZclDataTypeUI16.zclSize(UserID);
		size += ZclDataTypeUI8.zclSize(PIN);
		size += ZclDataTypeUI8.zclSize(UserType);
		size += ZclDataTypeUI8.zclSize(UserStatus);
		size += ZclDataTypeUI32.zclSize(ZigBeeLocalTime);
		size += ZclDataTypeString.zclSize(Data);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(33);
		ZclDataTypeUI8.zclSerialize(zclFrame, ProgramEventSource);
		ZclDataTypeUI8.zclSerialize(zclFrame, OperationEventCode);
		ZclDataTypeUI16.zclSerialize(zclFrame, UserID);
		ZclDataTypeUI8.zclSerialize(zclFrame, PIN);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserType);
		ZclDataTypeUI8.zclSerialize(zclFrame, UserStatus);
		ZclDataTypeUI32.zclSerialize(zclFrame, ZigBeeLocalTime);
		ZclDataTypeString.zclSerialize(zclFrame, Data);
		issueExec(zclFrame, 11, context);
	}

	public short getLockState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(0, new Short(v));
		return v;
	}

	public short getLockType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(1, new Short(v));
		return v;
	}

	public boolean getActuatorEnabled(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(2, new Boolean(v));
		return v;
	}

	public short getDoorState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(3, new Short(v));
		return v;
	}

	public long getDoorOpenEvents(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(4, new Long(v));
		return v;
	}

	public long getDoorClosedEvents(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(5, new Long(v));
		return v;
	}

	public int getOpenPeriod(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(6, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(6, new Integer(v));
		return v;
	}

	public int getNumberofLogRecordsSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getNumberofTotalUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getNumberofPINUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getNumberofRFIDUsersSupported(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(19, context);
		int v = ZclDataTypeUI16.zclParse(zclFrame);
		setCachedAttributeObject(19, new Integer(v));
		return v;
	}

	public short getNumberofWeekDaySchedulesSupportedPerUser(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(20, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(20, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(20, new Short(v));
		return v;
	}

	public short getNumberofYearDaySchedulesSupportedPerUser(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(21, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(21, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(21, new Short(v));
		return v;
	}

	public short getNumberofHolidaySchedulesSupported(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(22, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(22, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(22, new Short(v));
		return v;
	}

	public short getMaxPINCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getMinPINCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(24, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(24, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(24, new Short(v));
		return v;
	}

	public short getMaxRFIDCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(25, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(25, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(25, new Short(v));
		return v;
	}

	public short getMinRFIDCodeLength(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(26, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(26, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(26, new Short(v));
		return v;
	}

	public boolean getEnableLogging(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(32, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(32, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(32, new Boolean(v));
		return v;
	}

	public String getLanguage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			String objectResult = null;
			objectResult = ((String) getValidCachedAttributeObject(33, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(33, context);
		String v = ZclDataTypeString.zclParse(zclFrame);
		setCachedAttributeObject(33, v);
		return v;
	}

	public short getLEDSettings(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public long getAutoRelockTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(35, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(35, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(35, new Long(v));
		return v;
	}

	public short getSoundVolume(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(36, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(36, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(36, new Short(v));
		return v;
	}

	public short getOperatingmode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(37, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(37, context);
		short v = ZclDataTypeEnum8.zclParse(zclFrame);
		setCachedAttributeObject(37, new Short(v));
		return v;
	}

	public int getSupportedOperatingModes(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(38, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(38, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(38, new Integer(v));
		return v;
	}

	public int getDefaultConfigurationRegister(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(39, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(39, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(39, new Integer(v));
		return v;
	}

	public boolean getEnableLocalProgramming(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(40, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(40, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(40, new Boolean(v));
		return v;
	}

	public boolean getEnableOneTouchLocking(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(41, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(41, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(41, new Boolean(v));
		return v;
	}

	public boolean getEnableInsideStatusLED(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(42, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(42, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(42, new Boolean(v));
		return v;
	}

	public boolean getEnablePrivacyModeButton(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(43, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(43, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(43, new Boolean(v));
		return v;
	}

	public short getWrongcodeentrylimit(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(48, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(48, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(48, new Short(v));
		return v;
	}

	public short getUserCodeTemporaryDisableTime(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(49, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(49, context);
		short v = ZclDataTypeUI8.zclParse(zclFrame);
		setCachedAttributeObject(49, new Short(v));
		return v;
	}

	public boolean getSendPINovertheAir(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(50, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(50, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(50, new Boolean(v));
		return v;
	}

	public boolean getRequirePINforRFOperation(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(51, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(51, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(51, new Boolean(v));
		return v;
	}

	public short getZigBeeSecurityLevel(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getAlarmMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(64, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(64, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(64, new Integer(v));
		return v;
	}

	public int getKeypadOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(65, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(65, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(65, new Integer(v));
		return v;
	}

	public int getRFOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(66, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(66, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(66, new Integer(v));
		return v;
	}

	public int getManualOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(67, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(67, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(67, new Integer(v));
		return v;
	}

	public int getRFIDOperationEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(68, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(68, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(68, new Integer(v));
		return v;
	}

	public int getKeypadProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(69, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(69, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(69, new Integer(v));
		return v;
	}

	public int getRFProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(70, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(70, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(70, new Integer(v));
		return v;
	}

	public int getRFIDProgrammingEventMask(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Integer objectResult = null;
			objectResult = ((Integer) getValidCachedAttributeObject(71, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.intValue();
			}
		}
		IZclFrame zclFrame = readAttribute(71, context);
		int v = ZclDataTypeBitmap16.zclParse(zclFrame);
		setCachedAttributeObject(71, new Integer(v));
		return v;
	}

}
