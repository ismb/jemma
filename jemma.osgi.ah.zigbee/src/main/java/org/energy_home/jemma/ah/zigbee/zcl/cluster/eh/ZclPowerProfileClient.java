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
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclPowerProfileClient extends ZclServiceCluster implements PowerProfileClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 26;

	public ZclPowerProfileClient() throws ApplianceException {
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
		PowerProfileServer c = ((PowerProfileServer) getSinglePeerCluster((PowerProfileServer.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parsePowerProfileRequest(c, zclFrame);
			break;
		case 1:
			responseZclFrame = parsePowerProfileStateRequest(c, zclFrame);
			break;
		case 4:
			responseZclFrame = parseEnergyPhasesScheduleNotification(c, zclFrame);
			break;
		case 6:
			responseZclFrame = parsePowerProfileScheduleConstraintsRequest(c, zclFrame);
			break;
		case 7:
			responseZclFrame = parseEnergyPhasesScheduleStateRequest(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclPowerProfileClient.CLUSTER_ID, responseZclFrame);
			return true;
		}
		return false;
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	public void execPowerProfileNotification(short TotalProfileNum, short PowerProfileID,
			PowerProfileTransferredPhase[] PowerProfileTransferredPhases, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(TotalProfileNum);
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		size += (PowerProfileTransferredPhases.length * ZclPowerProfileTransferredPhase.zclSize(null));
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI8.zclSerialize(zclFrame, TotalProfileNum);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		size += (PowerProfileTransferredPhases.length * ZclPowerProfileTransferredPhase.zclSize(null));
		int i;
		for (i = 0; (i < PowerProfileTransferredPhases.length); i++) {
			ZclPowerProfileTransferredPhase.zclSerialize(zclFrame, PowerProfileTransferredPhases[i]);
		}
		issueExec(zclFrame, 11, context);
	}

	public GetPowerProfilePriceResponse execGetPowerProfilePrice(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 2, context);
		return (ZclGetPowerProfilePriceResponse.zclParse(zclResponseFrame));
	}

	public void execPowerProfilesStateNotification(PowerProfile[] PowerProfiles, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += (PowerProfiles.length * ZclPowerProfile.zclSize(null));
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeUI8.zclSerialize(zclFrame, (short) PowerProfiles.length);
		size += (PowerProfiles.length * ZclPowerProfile.zclSize(null));
		int i;
		for (i = 0; (i < PowerProfiles.length); i++) {
			ZclPowerProfile.zclSerialize(zclFrame, PowerProfiles[i]);
		}
		issueExec(zclFrame, 11, context);
	}

	public GetOverallSchedulePriceResponse execGetOverallSchedulePrice(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(5);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 3, context);
		return (ZclGetOverallSchedulePriceResponse.zclParse(zclResponseFrame));
	}

	public EnergyPhasesScheduleResponse execEnergyPhasesScheduleRequest(short PowerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 5, context);
		return (ZclEnergyPhasesScheduleResponse.zclParse(zclResponseFrame));
	}

	public void execEnergyPhasesScheduleStateNotification(short PowerProfileID, ScheduledPhase[] ScheduledPhases,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		size += (ScheduledPhases.length * ZclScheduledPhase.zclSize(null));
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(8);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		size += (ScheduledPhases.length * ZclScheduledPhase.zclSize(null));
		int i;
		for (i = 0; (i < ScheduledPhases.length); i++) {
			ZclScheduledPhase.zclSerialize(zclFrame, ScheduledPhases[i]);
		}
	}

	public void execPowerProfileScheduleConstraintsNotification(short PowerProfileID, int StartAfter, int StopBefore,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		size += ZclDataTypeUI16.zclSize(StartAfter);
		size += ZclDataTypeUI16.zclSize(StopBefore);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(9);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		ZclDataTypeUI16.zclSerialize(zclFrame, StartAfter);
		ZclDataTypeUI16.zclSerialize(zclFrame, StopBefore);
		issueExec(zclFrame, 11, context);
	}

	public GetPowerProfilePriceExtendedResponse execGetPowerProfilePriceExtended(short Options, short PowerProfileID,
			int PowerProfileStartTime, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeBitmap8.zclSize(Options);
		size += ZclDataTypeUI8.zclSize(PowerProfileID);
		size += ZclDataTypeUI16.zclSize(PowerProfileStartTime);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(11);
		ZclDataTypeBitmap8.zclSerialize(zclFrame, Options);
		ZclDataTypeUI8.zclSerialize(zclFrame, PowerProfileID);
		ZclDataTypeUI16.zclSerialize(zclFrame, PowerProfileStartTime);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 8, context);
		return (ZclGetPowerProfilePriceExtendedResponse.zclParse(zclResponseFrame));
	}

	protected IZclFrame parsePowerProfileRequest(PowerProfileServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		PowerProfileResponse r = o.execPowerProfileRequest(PowerProfileID, endPoint.getDefaultRequestContext());
		int size = ZclPowerProfileResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclPowerProfileResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parsePowerProfileStateRequest(PowerProfileServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		PowerProfileStateResponse r = o.execPowerProfileStateRequest(endPoint.getDefaultRequestContext());
		int size = ZclPowerProfileStateResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(2);
		ZclPowerProfileStateResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseEnergyPhasesScheduleNotification(PowerProfileServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		int ZclScheduledPhaseSize;
		ZclScheduledPhaseSize = ZclDataTypeBitmap8.zclParse(zclFrame);
		ScheduledPhase[] ScheduledPhases;
		ScheduledPhases = new ScheduledPhase[ZclScheduledPhaseSize];
		int i;
		for (i = 0; (i < ZclScheduledPhaseSize); i++) {
			ScheduledPhases[i] = ZclScheduledPhase.zclParse(zclFrame);
		}
		o.execEnergyPhasesScheduleNotification(PowerProfileID, ScheduledPhases, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parsePowerProfileScheduleConstraintsRequest(PowerProfileServer o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		PowerProfileScheduleConstraintsResponse r = o.execPowerProfileScheduleConstraintsRequest(PowerProfileID, endPoint.getDefaultRequestContext());
		int size = ZclPowerProfileScheduleConstraintsResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(10);
		ZclPowerProfileScheduleConstraintsResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseEnergyPhasesScheduleStateRequest(PowerProfileServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		short PowerProfileID = ZclDataTypeUI8.zclParse(zclFrame);
		EnergyPhasesScheduleStateResponse r = o.execEnergyPhasesScheduleStateRequest(PowerProfileID, endPoint.getDefaultRequestContext());
		int size = ZclEnergyPhasesScheduleStateResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(7);
		ZclEnergyPhasesScheduleStateResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}
	
}
