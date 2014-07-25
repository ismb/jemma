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

import org.energy_home.jemma.ah.cluster.zigbee.general.AddSceneResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveAllScenesResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.RemoveSceneResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.ScenesClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.ScenesServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.ViewSceneResponse;
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
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBoolean;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeIEEEAddress;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclScenesServer extends ZclServiceCluster implements ScenesServer, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 5;
	final static HashMap attributesMapByName = new HashMap();
	final static HashMap attributesMapById = new HashMap();

	static {
		attributesMapByName.put(ZclScenesServer.ATTR_SceneCount_NAME, new ZclAttributeDescriptor(0,
				ZclScenesServer.ATTR_SceneCount_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclScenesServer.ATTR_CurrentScene_NAME, new ZclAttributeDescriptor(1,
				ZclScenesServer.ATTR_CurrentScene_NAME, new ZclDataTypeUI8(), null, true, 1));
		attributesMapByName.put(ZclScenesServer.ATTR_CurrentGroup_NAME, new ZclAttributeDescriptor(2,
				ZclScenesServer.ATTR_CurrentGroup_NAME, new ZclDataTypeUI16(), null, true, 1));
		attributesMapByName.put(ZclScenesServer.ATTR_SceneValid_NAME, new ZclAttributeDescriptor(3,
				ZclScenesServer.ATTR_SceneValid_NAME, new ZclDataTypeBoolean(), null, true, 1));
		attributesMapByName.put(ZclScenesServer.ATTR_NameSupport_NAME, new ZclAttributeDescriptor(4,
				ZclScenesServer.ATTR_NameSupport_NAME, new ZclDataTypeBitmap8(), null, true, 1));
		attributesMapByName.put(ZclScenesServer.ATTR_LastConfiguredBy_NAME, new ZclAttributeDescriptor(5,
				ZclScenesServer.ATTR_LastConfiguredBy_NAME, new ZclDataTypeIEEEAddress(), null, true, 1));
	}

	public ZclScenesServer() throws ApplianceException {
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
		ScenesClient c = ((ScenesClient) getSinglePeerCluster((ScenesClient.class.getName())));
		switch (commandId) {
		case 4:
			responseZclFrame = parseStoreScenesResponse(c, zclFrame);
			break;
		}
		if (responseZclFrame == null) {
			if (!zclFrame.isDefaultResponseDisabled()) {
				responseZclFrame = getDefaultResponse(zclFrame, statusCode);
			}
		}
		if (!(responseZclFrame == null)) {
			device.post(ZclScenesServer.CLUSTER_ID, responseZclFrame);
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

	public AddSceneResponse execAddScene(int GroupID, short SceneID, int TransitionTime, String SceneName,
			byte[] ExtensionFieldSet, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		size += ZclDataTypeUI16.zclSize(TransitionTime);
		size += ZclDataTypeString.zclSize(SceneName);
		size += ExtensionFieldSet.length;
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		ZclDataTypeUI16.zclSerialize(zclFrame, TransitionTime);
		ZclDataTypeString.zclSerialize(zclFrame, SceneName);
		zclFrame.appendOctets(ExtensionFieldSet);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 0, context);
		return (ZclAddSceneResponse.zclParse(zclResponseFrame));
	}

	public ViewSceneResponse execViewScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(1);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 1, context);
		return (ZclViewSceneResponse.zclParse(zclResponseFrame));
	}

	public RemoveSceneResponse execRemoveScene(int GroupID, short SceneID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 2, context);
		return (ZclRemoveSceneResponse.zclParse(zclResponseFrame));
	}

	public RemoveAllScenesResponse execRemoveAllScenes(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(3);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		IZclFrame zclResponseFrame = issueExec(zclFrame, 3, context);
		return (ZclRemoveAllScenesResponse.zclParse(zclResponseFrame));
	}

	public void execStoreScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(4);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		issueExec(zclFrame, 11, context);
	}

	public void execRecallScene(int GroupID, short SceneID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		size += ZclDataTypeUI8.zclSize(SceneID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(5);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		ZclDataTypeUI8.zclSerialize(zclFrame, SceneID);
		issueExec(zclFrame, 11, context);
	}

	public void execGetSceneMembership(int GroupID, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI16.zclSize(GroupID);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(6);
		ZclDataTypeUI16.zclSerialize(zclFrame, GroupID);
		issueExec(zclFrame, 11, context);
	}

	protected IZclFrame parseStoreScenesResponse(org.energy_home.jemma.ah.cluster.zigbee.general.ScenesClient o, IZclFrame zclFrame)
			throws ApplianceException, ServiceClusterException {
		short Status = ZclDataTypeEnum8.zclParse(zclFrame);
		int GroupID = ZclDataTypeUI16.zclParse(zclFrame);
		short SceneID = ZclDataTypeUI8.zclParse(zclFrame);
		o.execStoreScenesResponse(Status, GroupID, SceneID, null);
		return null;
	}

	public short getSceneCount(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getCurrentScene(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public int getCurrentGroup(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public boolean getSceneValid(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public short getNameSupport(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
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

	public byte[] getLastConfiguredBy(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		if (context != null) {
			byte[] objectResult = null;
			objectResult = ((byte[]) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
			if (objectResult != null) {
				return objectResult;
			}
		}
		IZclFrame zclFrame = readAttribute(5, context);
		byte[] v = zclFrame.parseOctets();
		setCachedAttributeObject(5, v);
		return v;
	}
}
