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
package org.energy_home.jemma.ah.zigbee.zcl.lib;

import java.util.Dictionary;
import java.util.List;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IHacDevice;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.DriverAppliance;
import org.energy_home.jemma.ah.internal.zigbee.ZigBeeManagerImpl;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAppliance;
import org.energy_home.jemma.ah.zigbee.zcl.IZclServiceCluster;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ZclAppliance extends DriverAppliance implements IZclAppliance, ZigBeeDeviceListener {

	private static final Logger LOG = LoggerFactory.getLogger( ZclAppliance.class );

	private String nodeMacAddress = null;
	private int attachedDevices = 0;
	
	protected boolean isGenericAppliance() {
		return false;
	}
	
	public ZclAppliance(String pid, Dictionary config) throws ApplianceException {
		super(pid, config);
		this.setAvailability(false);
	}

	public String zclGetNodeMacAddress() {
		return nodeMacAddress;
	}
	
	public synchronized void attach(IHacDevice device) {
		ZigBeeDevice zclDevice = (ZigBeeDevice) device;
		ServiceDescriptor service = zclDevice.getServiceDescriptor();
		int deviceId = service.getSimpleDescriptor().getApplicationDeviceIdentifier().intValue();
		int profileId = service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue();
		int endPointId = service.getEndPoint();
		List activeEndPointIds = zclDevice.getNodeServices().getActiveEndpoints();
		int epsNumber = activeEndPointIds.size();
		if (nodeMacAddress == null)
			nodeMacAddress = ZigBeeManagerImpl.getIeeeAddressHex(zclDevice.getNodeServices().getAddress());
		
		// FIXME: read manufacturer code somewhere
		ZclEndPoint serviceEndPoint = null;
		try {
			serviceEndPoint = this.zclGetEndPoint(epsNumber, profileId, deviceId, endPointId, 0, 
					service.getSimpleDescriptor().getApplicationOutputCluster(), service.getSimpleDescriptor().getApplicationInputCluster());
		} catch (ApplianceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (serviceEndPoint == null) {
			LOG.error("attaching device but no valid end point found");
			return;
		}
		serviceEndPoint.zclSetZigBeeDevice(zclDevice);
		String[] clusterEndPoints = serviceEndPoint.getServiceClusterNames();
		for (int j = 0; j < clusterEndPoints.length; j++) {
			IServiceCluster serviceCluster = serviceEndPoint.getServiceCluster(clusterEndPoints[j]);

			try {
				if (serviceCluster instanceof IZclServiceCluster)
					((IZclServiceCluster) serviceCluster).zclAttach(zclDevice);
			} catch (Exception e) {
				LOG.error("attaching clusterEndPoint to device in ZclAppliance ",e);
				continue;
			}
		}
		attachedDevices++;
		if ((!isGenericAppliance() && this.getEndPointTypes().length == attachedDevices+1) ||			
				(isGenericAppliance() && 
						attachedDevices == zclDevice.getNodeServices().getActiveEndpoints().size())) {
			attached();
			this.setAvailability(true);
		}
		zclDevice.setListener(this);
	}

	public synchronized void detach(IHacDevice device) throws ApplianceException {
		ZigBeeDevice zclDevice = (ZigBeeDevice) device;
		if (this.isAvailable()) {
			this.setAvailability(false);
		}
		attachedDevices--;

		IEndPoint[] serviceEndPoints = this.getEndPoints();
		IEndPoint serviceEndPoint = null;
		ZigBeeDevice attachedDevice = null;
		for (int i = 0; i < serviceEndPoints.length; i++) {
			serviceEndPoint = serviceEndPoints[i];
			if (serviceEndPoint instanceof ZclEndPoint &&
					(attachedDevice = ((ZclEndPoint)serviceEndPoint).zclGetZigBeeDevice()) != null &&
					attachedDevice.getServiceDescriptor().getEndPoint() == zclDevice.getServiceDescriptor().getEndPoint()) {
					((ZclEndPoint)serviceEndPoint).zclSetZigBeeDevice(null);
					String[] clusterEndPoints = serviceEndPoint.getServiceClusterNames();
					for (int j = 0; j < clusterEndPoints.length; j++) {
						IServiceCluster serviceCluster = serviceEndPoint.getServiceCluster(clusterEndPoints[j]);
						try {
							if (serviceCluster instanceof IZclServiceCluster)
								((IZclServiceCluster) serviceCluster).zclDetach(zclDevice);
						} catch (Exception e) {
							LOG.error("detaching clusterEndPoint to device in ZclAppliance ",e);
							continue;
						}
					}
					break;
			}
		}
		zclDevice.removeListener(this);
		detached();
	}

	public ZclEndPoint zclAddEndPoint(String endPointType) throws ApplianceException {
		return (ZclEndPoint) this.addEndPoint(new ZclEndPoint(endPointType));
	}
	
	public ZclEndPoint zclAddEndPoint(String endPointType, int endPointId) throws ApplianceException {
		return (ZclEndPoint) this.addEndPoint(new ZclEndPoint(endPointType), endPointId);
	}

	// protected synchronized void detach(Device d) {
	// if (this.zclDevice == d) {
	// this.setAvailability(false);
	// IEndPoint[] serviceEndPoints = this.getEndPoints();
	// for (int i = 0; i < serviceEndPoints.length; i++) {
	// IEndPoint serviceEndPoint = serviceEndPoints[i];
	//
	// String[] clusterEndPoints = serviceEndPoint.getServiceClusterNames();
	// for (int j = 0; j < clusterEndPoints.length; j++) {
	//
	// IServiceCluster serviceCluster =
	// serviceEndPoint.getServiceCluster(clusterEndPoints[j]);
	// if (serviceCluster instanceof IZclServiceCluster)
	// ((IZclServiceCluster) serviceCluster).zclDetach(this.zclDevice);
	// }
	// }
	//
	// this.zclDevice.removeListener(this);
	//
	// detached();
	//
	// this.setAvailability(false);
	// this.statusUpdated();
	// }
	// }

//	protected synchronized final void removeAttachedDevices() {	
//		IEndPoint[] zclEndPoints = getEndPoints();
//		ZigBeeDevice device = null; 
//		for (int i = 1; i < zclEndPoints.length; i++) {	
//			device = ((ZclEndPoint)zclEndPoints[i]).zclGetZigBeeDevice();
//			if (device != null)
//				device.remove();
//		}
//	}
	
	
	
	protected void attached() {
		LOG.debug("attached");
	}

	protected void detached() {
		LOG.debug("detached");
	}

	protected ZclEndPoint zclGetEndPoint(int epsNumber, int profile_id, int device_id, int end_point_id, int manufacturer_code, final List clientClusterIds, final List serverClusterIds) throws ApplianceException {
		ZclEndPoint endPoint = (ZclEndPoint) getEndPoint(end_point_id);
		if (endPoint == null)
			endPoint = (ZclEndPoint)getEndPoint(IEndPoint.DEFAULT_END_POINT_ID);
		return endPoint;
	}

	public void notifyEvent(int event) {
		if (event == ZigBeeDeviceListener.ANNOUNCEMENT) {
			LOG.debug("ZclAppliance " + this.getPid() + " received an announcement");
			// if the availability is already true don't update it!
			if (this.isAvailable()) {
				return;
			}

			this.setAvailability(true);
		} else if (event == ZigBeeDeviceListener.LEAVE) {
			LOG.debug("ZclAppliance " + this.getPid() + " left the network");
			// if the availability is already false don't update it!
			if (!this.isAvailable()) {
				return;
			}

			this.setAvailability(false);
		}
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
}
