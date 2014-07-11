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

import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockClient;
import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclDoorLockClient extends ZclServiceCluster implements DoorLockClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 257;

	public ZclDoorLockClient() throws ApplianceException {
		super();
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		boolean handled;
		handled = super.notifyZclFrame(clusterId, zclFrame);
		if (handled) {
			return handled;
		}
		int commandId = zclFrame.getCommandId();
		if (zclFrame.isServerToClient()) {
			throw new ZclValidationException("invalid direction field");
		}
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		DoorLockServer c = ((DoorLockServer) getSinglePeerCluster((DoorLockServer.class
				.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseLockDoorResponse(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parseUnlockDoorResponse(c, zclFrame);
			break;
		case 2:
			responseZclFrame = parseToggleResponse(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseUnlockWithTimeoutResponse(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseGetLogRecordResponse(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseSetPINCodeResponse(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parseGetPINCodeResponse(c, zclFrame);
			break;
		case 7:
			responseZclFrame = parseClearPINCodeResponse(c, zclFrame);
			break;
		case 8:
			responseZclFrame = parseClearAllPINCodesResponse(c, zclFrame);
			break;
		case 9:
			responseZclFrame = parseSetUserStatusResponse(c, zclFrame);
			break;
		case 10:
			responseZclFrame = parseGetUserStatusResponse(c, zclFrame);
			break;
		case 11:
			responseZclFrame = parseSetWeekdayScheduleResponse(c, zclFrame);
			break;
		case 12:
			responseZclFrame = parseGetWeekdayScheduleResponse(c, zclFrame);
			break;
		case 13:
			responseZclFrame = parseClearWeekdayScheduleResponse(c, zclFrame);
			break;
		case 14:
			responseZclFrame = parseSetYearDayScheduleResponse(c, zclFrame);
			break;
		case 15:
			responseZclFrame = parseGetYearDayScheduleResponse(c, zclFrame);
			break;
		case 16:
			responseZclFrame = parseClearYearDayScheduleResponse(c, zclFrame);
			break;
		case 17:
			responseZclFrame = parseSetHolidayScheduleResponse(c, zclFrame);
			break;
		case 18:
			responseZclFrame = parseGetHolidayScheduleResponse(c, zclFrame);
			break;
		case 19:
			responseZclFrame = parseClearHolidayScheduleResponse(c, zclFrame);
			break;
		case 20:
			responseZclFrame = parseSetUsertypeResponse(c, zclFrame);
			break;
		case 21:
			responseZclFrame = parseGetUsertypeResponse(c, zclFrame);
			break;
		case 22:
			responseZclFrame = parseSetRFIDCodeResponse(c, zclFrame);
			break;
		case 23:
			responseZclFrame = parseGetRFIDCodeResponse(c, zclFrame);
			break;
		case 24:
			responseZclFrame = parseClearRFIDCodeResponse(c, zclFrame);
			break;
		case 25:
			responseZclFrame = parseClearAllRFIDCodesResponse(c, zclFrame);
			break;
		case 32:
			responseZclFrame = parseOperationEventNotification(c, zclFrame);
			break;
		case 33:
			responseZclFrame = parseProgrammingEventNotification(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclDoorLockClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclFrame parseLockDoorResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execLockDoorResponse(Status, null);
		return null;
	}

	protected IZclFrame parseUnlockDoorResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execUnlockDoorResponse(Status, null);
		return null;
	}

	protected IZclFrame parseToggleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execToggleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseUnlockWithTimeoutResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execUnlockWithTimeoutResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetLogRecordResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int LogEntryID = ZclDataTypeUI16.zclParse(zclFrame);
		long Timestamp = ZclDataTypeUI32.zclParse(zclFrame);
		short EventType = ZclDataTypeEnum8.zclParse(zclFrame);
		short Source = ZclDataTypeUI8.zclParse(zclFrame);
		short EventIDAlarmCode = ZclDataTypeUI8.zclParse(zclFrame);
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		String PIN = ZclDataTypeString.zclParse(zclFrame);
		o.execGetLogRecordResponse(LogEntryID, Timestamp, EventType, Source, EventIDAlarmCode, UserID, PIN, null);
		return null;
	}

	protected IZclFrame parseSetPINCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetPINCodeResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetPINCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short UserStatus = ZclDataTypeUI8.zclParse(zclFrame);
		short UserType = ZclDataTypeUI8.zclParse(zclFrame);
		String Code = ZclDataTypeString.zclParse(zclFrame);
		o.execGetPINCodeResponse(UserID, UserStatus, UserType, Code, null);
		return null;
	}

	protected IZclFrame parseClearPINCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearPINCodeResponse(Status, null);
		return null;
	}

	protected IZclFrame parseClearAllPINCodesResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearAllPINCodesResponse(Status, null);
		return null;
	}

	protected IZclFrame parseSetUserStatusResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetUserStatusResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetUserStatusResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short UserStatus = ZclDataTypeUI8.zclParse(zclFrame);
		o.execGetUserStatusResponse(UserID, UserStatus, null);
		return null;
	}

	protected IZclFrame parseSetWeekdayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetWeekdayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetWeekdayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short ScheduleID = ZclDataTypeUI8.zclParse(zclFrame);
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		short DaysMask = ZclDataTypeBitmap8.zclParse(zclFrame);
		short StartHour = ZclDataTypeUI8.zclParse(zclFrame);
		short StartMinute = ZclDataTypeUI8.zclParse(zclFrame);
		short EndHour = ZclDataTypeUI8.zclParse(zclFrame);
		short EndMinute = ZclDataTypeUI8.zclParse(zclFrame);
		o.execGetWeekdayScheduleResponse(ScheduleID, UserID, Status, DaysMask, StartHour, StartMinute, EndHour, EndMinute, null);
		return null;
	}

	protected IZclFrame parseClearWeekdayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearWeekdayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseSetYearDayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetYearDayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetYearDayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short ScheduleID = ZclDataTypeUI8.zclParse(zclFrame);
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		long ZigBeeLocalStartTime = ZclDataTypeUI32.zclParse(zclFrame);
		long ZigBeeLocalEndTime = ZclDataTypeUI32.zclParse(zclFrame);
		o.execGetYearDayScheduleResponse(ScheduleID, UserID, Status, ZigBeeLocalStartTime, ZigBeeLocalEndTime, null);
		return null;
	}

	protected IZclFrame parseClearYearDayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearYearDayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseSetHolidayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetHolidayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetHolidayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short HolidayScheduleID = ZclDataTypeUI8.zclParse(zclFrame);
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		long ZigBeeLocalStartTime = ZclDataTypeUI32.zclParse(zclFrame);
		long ZigBeeLocalEndTime = ZclDataTypeUI32.zclParse(zclFrame);
		short OperatingModeDuringHoliday = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execGetHolidayScheduleResponse(HolidayScheduleID, Status, ZigBeeLocalStartTime, ZigBeeLocalEndTime,
				OperatingModeDuringHoliday, null);
		return null;
	}

	protected IZclFrame parseClearHolidayScheduleResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearHolidayScheduleResponse(Status, null);
		return null;
	}

	protected IZclFrame parseSetUsertypeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetUsertypeResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetUsertypeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short UserType = ZclDataTypeEnum8.zclParse(zclFrame);
		o.execGetUsertypeResponse(UserID, UserType, null);
		return null;
	}

	protected IZclFrame parseSetRFIDCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execSetRFIDCodeResponse(Status, null);
		return null;
	}

	protected IZclFrame parseGetRFIDCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short UserStatus = ZclDataTypeUI8.zclParse(zclFrame);
		short UserType = ZclDataTypeUI8.zclParse(zclFrame);
		String RFIDCode = ZclDataTypeString.zclParse(zclFrame);
		o.execGetRFIDCodeResponse(UserID, UserStatus, UserType, RFIDCode, null);
		return null;
	}

	protected IZclFrame parseClearRFIDCodeResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearRFIDCodeResponse(Status, null);
		return null;
	}

	protected IZclFrame parseClearAllRFIDCodesResponse(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeUI8.zclParse(zclFrame);
		o.execClearAllRFIDCodesResponse(Status, null);
		return null;
	}

	protected IZclFrame parseOperationEventNotification(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short OperationEventSource = ZclDataTypeUI8.zclParse(zclFrame);
		short OperationEventCode = ZclDataTypeUI8.zclParse(zclFrame);
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short PIN = ZclDataTypeUI8.zclParse(zclFrame);
		long ZigBeeLocalTime = ZclDataTypeUI32.zclParse(zclFrame);
		String Data = ZclDataTypeString.zclParse(zclFrame);
		o.execOperationEventNotification(OperationEventSource, OperationEventCode, UserID, PIN, ZigBeeLocalTime, Data, null);
		return null;
	}

	protected IZclFrame parseProgrammingEventNotification(org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer o,
			IZclFrame zclFrame) throws ApplianceException, ServiceClusterException {
		short ProgramEventSource = ZclDataTypeUI8.zclParse(zclFrame);
		short OperationEventCode = ZclDataTypeUI8.zclParse(zclFrame);
		int UserID = ZclDataTypeUI16.zclParse(zclFrame);
		short PIN = ZclDataTypeUI8.zclParse(zclFrame);
		short UserType = ZclDataTypeUI8.zclParse(zclFrame);
		short UserStatus = ZclDataTypeUI8.zclParse(zclFrame);
		long ZigBeeLocalTime = ZclDataTypeUI32.zclParse(zclFrame);
		String Data = ZclDataTypeString.zclParse(zclFrame);
		o.execProgrammingEventNotification(ProgramEventSource, OperationEventCode, UserID, PIN, UserType, UserStatus,
				ZigBeeLocalTime, Data, null);
		return null;
	}

}
