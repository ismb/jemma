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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogQueueResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI32;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUTCTime;

public class ZclApplianceStatisticsServer extends ZclServiceCluster implements ApplianceStatisticsServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 2819;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclApplianceStatisticsServer.ATTR_LogMaxSize_NAME, new ZclAttributeDescriptor(0,
				ZclApplianceStatisticsServer.ATTR_LogMaxSize_NAME, new ZclDataTypeUI32(), null, false, 1));
		attributesMapByName.put(ZclApplianceStatisticsServer.ATTR_LogQueueMaxSize_NAME, new ZclAttributeDescriptor(1,
				ZclApplianceStatisticsServer.ATTR_LogQueueMaxSize_NAME, new ZclDataTypeUI8(), null, false, 1));
	}

	public ZclApplianceStatisticsServer() throws ApplianceException {
		super();
	}

	public void zclAttach(ZigBeeDevice device) {
		super.zclAttach(device);

		// LogResponse
		device.enablePartitionServer(CLUSTER_ID, (short) 0x01);
		// LogNotification
		device.enablePartitionServer(CLUSTER_ID, (short) 0x00);
	}

	public void zclDetach(ZigBeeDevice device) {
		super.zclDetach(device);

		// LogResponse
		device.disablePartitionServer(CLUSTER_ID, (short) 0x01);
		// LogNotification
		device.disablePartitionServer(CLUSTER_ID, (short) 0x00);
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
		ApplianceStatisticsClient c = ((ApplianceStatisticsClient) getSinglePeerCluster((ApplianceStatisticsClient.class.getName())));
		switch (commandId) {
		case 0:
			responseZclFrame = parseLogNotification(c, zclFrame);
			break;
		case 3:
			responseZclFrame = parseStatisticsAvailable(c, zclFrame);
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
			device.post(ZclApplianceStatisticsServer.CLUSTER_ID, responseZclFrame);
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
	
	protected Collection getAttributeDescriptors() {
		return attributesMapByName.values();
	}

	public LogResponse execLogRequest(long LogID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI32.zclSize(LogID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI32.zclSerialize(zclFrame, LogID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclLogResponse.zclParse(zclResponseFrame));
	}

	public LogQueueResponse execLogQueueRequest(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZclFrame zclFrame = new ZclFrame(1);
		zclFrame.setCommandId(1);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 2, context);
		return (ZclLogQueueResponse.zclParse(zclResponseFrame));
	}

	protected IZclFrame parseLogNotification(ApplianceStatisticsClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		long Timestamp = ZclDataTypeUTCTime.zclParse(zclFrame);
		long LogID = ZclDataTypeUI32.zclParse(zclFrame);
		long LogLength = ZclDataTypeUI32.zclParse(zclFrame);
		// FIXME: parse array must accept a long type.
		byte[] LogPayload = zclFrame.parseArray((int) LogLength);
		o.execLogNotification(Timestamp, LogID, LogLength, LogPayload, endPoint.getDefaultRequestContext());
		return null;
	}

	protected IZclFrame parseStatisticsAvailable(ApplianceStatisticsClient o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		o.execStatisticsAvailable(endPoint.getDefaultRequestContext());
		return null;
	}

	public long getLogMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			Long objectResult = null;
			objectResult = ((Long) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult.longValue();
			}
		}
		IZclFrame zclFrame = readAttribute(0, context);
		long v = ZclDataTypeUI32.zclParse(zclFrame);
		setCachedAttributeObject(0, new Long(v));
		return v;
	}

	public short getLogQueueMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

}
