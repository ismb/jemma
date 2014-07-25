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
package org.energy_home.jemma.ah.zigbee.zcl.cluster.eh;

import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.eh.EnergyPhasesScheduleResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.EnergyPhasesScheduleStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetOverallSchedulePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceExtendedResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfileServer extends ZclServiceCluster implements PowerProfileServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 26;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclPowerProfileServer.ATTR_TotalProfileNum_NAME, new ZclAttributeDescriptor(0,
				ZclPowerProfileServer.ATTR_TotalProfileNum_NAME, new ZclDataTypeUI8(), null, false, 1));
		attributesMapByName.put(ZclPowerProfileServer.ATTR_MultipleScheduling_NAME, new ZclAttributeDescriptor(1,
				ZclPowerProfileServer.ATTR_MultipleScheduling_NAME, new ZclDataTypeBoolean(), null, false, 1));
		attributesMapByName.put(ZclPowerProfileServer.ATTR_EnergyFormatting_NAME, new ZclAttributeDescriptor(2,
				ZclPowerProfileServer.ATTR_EnergyFormatting_NAME, new ZclDataTypeBitmap8(), null, false, 1));
		attributesMapByName.put(ZclPowerProfileServer.ATTR_EnergyRemote_NAME, new ZclAttributeDescriptor(3,
				ZclPowerProfileServer.ATTR_EnergyRemote_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclPowerProfileServer.ATTR_ScheduleMode_NAME, new ZclAttributeDescriptor(4,
				ZclPowerProfileServer.ATTR_ScheduleMode_NAME, new ZclDataTypeBitmap8(), null, true, 0));
	}

	public ZclPowerProfileServer() throws ApplianceException {
		super();
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		boolean handled;
		handled = super.notifyZclFrame(clusterId, zclFrame);
		if (handled) {
			return handled;
		}
		int commandId = zclFrame.getCommandId();
		if (zclFrame.isClientToServer()) {
			throw new ZclValidationException("invalid direction field");
		}
		IZclFrame responseZclFrame = null;
		ZigBeeDevice device = getZigBeeDevice();
		int statusCode = ZCL.SUCCESS;
		PowerProfileClient c = ((PowerProfileClient) getSinglePeerCluster((PowerProfileClient.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parsePowerProfileNotification(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseGetPowerProfilePrice(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parsePowerProfilesStateNotification(c, zclFrame);
			break;
		case 5:
			responseZclFrame = parseGetOverallSchedulePrice(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parseEnergyPhasesScheduleRequest(c, zclFrame);
			break;
		case 8:
			responseZclFrame = parseEnergyPhasesScheduleStateNotification(c, zclFrame);
			break;
		case 9:
			responseZclFrame = parsePowerProfileScheduleConstraintsNotification(c, zclFrame);
			break;
		case 11:
			responseZclFrame = parseGetPowerProfilePriceExtended(c, zclFrame);
			break;
			
		default:
			throw new ZclException(ZCL.UNSUP_CLUSTER_COMMAND);
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
			else {
				return true;
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclPowerProfileServer.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
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

	public PowerProfileResponse execPowerProfileRequest(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclPowerProfileResponse.zclParse(zclResponseFrame));
	}

	public PowerProfileStateResponse execPowerProfileStateRequest(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 2, context);
		return (ZclPowerProfileStateResponse.zclParse(zclResponseFrame));
	}

	public void execEnergyPhasesScheduleNotification(short PowerProfileID, ScheduledPhase[] ScheduledPhases,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		size += ZclDataTypeUI8.zclSize((short)ScheduledPhases.length);
		size += (ScheduledPhases.length * ZclScheduledPhase.zclSize(null));
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		ZclDataTypeUI8.zclSerialize(zclFrame, (short)ScheduledPhases.length);
		int i;
		
		for (i = 0; (i < ScheduledPhases.length); i++) {
			ZclScheduledPhase.zclSerialize(zclFrame, ScheduledPhases[i]);
		}
		issueExec(zclFrame, 11, context);
	}

	public PowerProfileScheduleConstraintsResponse execPowerProfileScheduleConstraintsRequest(short PowerProfileID,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 10, context);
		return (ZclPowerProfileScheduleConstraintsResponse.zclParse(zclResponseFrame));
	}

	public EnergyPhasesScheduleStateResponse execEnergyPhasesScheduleStateRequest(short PowerProfileID,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(7);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 7, context);
		return (ZclEnergyPhasesScheduleStateResponse.zclParse(zclResponseFrame));
	}

	protected IZclFrame parsePowerProfileNotification(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short TotalProfileNum = ZclDataTypeUI8.zclParse(zclFrame);
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		int ZclPowerProfileTransferredPhaseSize;
		ZclPowerProfileTransferredPhaseSize = ZclDataTypeBitmap8.zclParse(zclFrame);
		PowerProfileTransferredPhase[] PowerProfileTransferredPhases;
		PowerProfileTransferredPhases = new PowerProfileTransferredPhase[ZclPowerProfileTransferredPhaseSize];
		int i;
		for (i = 0; (i < ZclPowerProfileTransferredPhaseSize); i++) {
			PowerProfileTransferredPhases[i] = ZclPowerProfileTransferredPhase.zclParse(zclFrame);
		}
		o.execPowerProfileNotification(TotalProfileNum, PowerProfileID, PowerProfileTransferredPhases, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseGetPowerProfilePrice(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		GetPowerProfilePriceResponse r = o.execGetPowerProfilePrice(PowerProfileID, endPoint.getDefaultRequestContext());
		int size = ZclGetPowerProfilePriceResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(2);
		ZclGetPowerProfilePriceResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parsePowerProfilesStateNotification(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileCount = ZclDataTypeUI8.zclParse(zclFrame);
		PowerProfile[] PowerProfiles;
		PowerProfiles = new PowerProfile[PowerProfileCount];
		int i;
		for (i = 0; (i < PowerProfileCount); i++) {
			PowerProfiles[i] = ZclPowerProfile.zclParse(zclFrame);
		}
		o.execPowerProfilesStateNotification(PowerProfiles, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseGetOverallSchedulePrice(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		GetOverallSchedulePriceResponse r = o.execGetOverallSchedulePrice(endPoint.getDefaultRequestContext());
		int size = ZclGetOverallSchedulePriceResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(3);
		ZclGetOverallSchedulePriceResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseEnergyPhasesScheduleRequest(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		EnergyPhasesScheduleResponse r = o.execEnergyPhasesScheduleRequest(PowerProfileID, endPoint.getDefaultRequestContext());
		int size = ZclEnergyPhasesScheduleResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(5);
		ZclEnergyPhasesScheduleResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseEnergyPhasesScheduleStateNotification(PowerProfileClient o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {		
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		int ZclScheduledPhaseSize;
		ZclScheduledPhaseSize = ZclDataTypeBitmap8.zclParse(zclFrame);
		ScheduledPhase[] ScheduledPhases;
		ScheduledPhases = new ScheduledPhase[ZclScheduledPhaseSize];
		int i;
		for (i = 0; (i < ZclScheduledPhaseSize); i++) {
			ScheduledPhases[i] = ZclScheduledPhase.zclParse(zclFrame);
		}
		o.execEnergyPhasesScheduleStateNotification(PowerProfileID, ScheduledPhases, endPoint.getDefaultRequestContext());			
		return null;
	}

	protected IZclFrame parsePowerProfileScheduleConstraintsNotification(PowerProfileClient o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		int StartAfter = ZclDataTypeUI16.zclParse(zclFrame);
		int StopBefore = ZclDataTypeUI16.zclParse(zclFrame);
		o.execPowerProfileScheduleConstraintsNotification(PowerProfileID, StartAfter, StopBefore, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseGetPowerProfilePriceExtended(PowerProfileClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
        short Options = ZclDataTypeBitmap8 .zclParse(zclFrame);
        short PowerProfileID = ZclDataTypeUI8 .zclParse(zclFrame);
        int PowerProfileStartTime = ZclDataTypeUI16 .zclParse(zclFrame);
        GetPowerProfilePriceExtendedResponse r = o.execGetPowerProfilePriceExtended(Options, PowerProfileID, PowerProfileStartTime, endPoint.getDefaultRequestContext());
        int size = ZclGetPowerProfilePriceExtendedResponse.zclSize(r);
        IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
        zclResponseFrame.setCommandId(8);
        ZclGetPowerProfilePriceExtendedResponse.zclSerialize(zclResponseFrame, r);
        return zclResponseFrame;		
	}

	public short getTotalProfileNum(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public boolean getMultipleScheduling(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(1, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(1, new Boolean(v));
		return v;
	}

	public short getEnergyFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(2, context);
		
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(2, new Short(v));
		return v;
	}

	public boolean getEnergyRemote(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Boolean objectResult = null;
			objectResult = ((Boolean) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.booleanValue();
			}
		}
		IZclFrame zclFrame = readAttribute(3, context);
		boolean v = ZclDataTypeBoolean.zclParse(zclFrame);
		setCachedAttributeObject(3, new Boolean(v));
		return v;
	}

	public void setScheduleMode(short ScheduleMode, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int attrId = 4;
		int size = 3;
		size += ZclDataTypeBitmap8.zclSize(ScheduleMode);
		IZclFrame zclFrame = new ZclFrame(0, size);
		zclFrame.appendUInt16(attrId);
		zclFrame.appendUInt8(ZclDataTypeBitmap8.ZCL_DATA_TYPE);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, ScheduleMode);
		issueSet(ZclPowerProfileServer.CLUSTER_ID, zclFrame, attrId, context);
	}

	public short getScheduleMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Short objectResult = null;
			objectResult = ((Short) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.shortValue();
			}
		}
		IZclFrame zclFrame = readAttribute(4, context);
		short v = ZclDataTypeBitmap8.zclParse(zclFrame);
		setCachedAttributeObject(4, new Short(v));
		return v;
	}

}
