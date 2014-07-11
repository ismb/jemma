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
package org.energy_home.jemma.ah.zigbee;

import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;

import org.energy_home.jemma.ah.hac.IHacDevice;

public interface ZigBeeDevice extends IHacDevice {

	public void remove();
	
	public String getIeeeAddress();

	public ServiceDescriptor getServiceDescriptor();
	
	public NodeDescriptor getNodeDescriptor();
	
	public NodeServices getNodeServices();

	/**
	 * The invoke method sends the passed frame and wait for the answer from the
	 * destination zigbee node. The caller remains blocked until the answer has
	 * been received. A ZigBeeException exception is raised in case of error
	 * (for instance a timeout)
	 * 
	 * @param clusterId
	 *            The clusterId of the zcl frame
	 * 
	 * @param zclFrame
	 *            The ZCL frame
	 * 
	 * @return The ZCL frame containing the response from the remote node or
	 *         null if the zclFrame parameter indicates that the default
	 *         response is disabled.
	 * 
	 * @throws ZigBeeException
	 */
	public IZclFrame invoke(short clusterId, IZclFrame zclFrame) throws ZigBeeException;
	
	/**
	 * The invoke method sends the passed frame and wait for the answer from the
	 * destination zigbee node. The caller remains blocked until the answer has
	 * been received. A ZigBeeException exception is raised in case of error
	 * (for instance a timeout)
	 * 	 * @param profileId
	 *            The profileId of the zcl frame
	 * 
	 * @param clusterId
	 *            The clusterId of the zcl frame
	 * 
	 * @param zclFrame
	 *            The ZCL frame
	 * 
	 * @return The ZCL frame containing the response from the remote node or
	 *         null if the zclFrame parameter indicates that the default
	 *         response is disabled.
	 * 
	 * @throws ZigBeeException
	 */
	public IZclFrame invoke(short profileId, short clusterId, IZclFrame zclFrame) throws ZigBeeException;

	/**
	 * This method sends the frame and don't wait for the answer. The response
	 * will be notified to the caller by means of the listener.
	 * 
	 * @param clusterId
	 * @param zclFrame
	 * @return True if successful.
	 */
	public boolean post(short clusterId, IZclFrame zclFrame);
	
	/**
	 * This method sends the frame and don't wait for the answer. The response
	 * will be notified to the caller by means of the listener.
	 * 
	 * @param clusterId
	 * @param profileId
	 * @param zclFrame
	 * @return True if successful.
	 */
	public boolean post(short profileId, short clusterId, IZclFrame zclFrame);	

	public boolean setListener(ZigBeeDeviceListener listener);

	public boolean setListener(short clusterId, int side, ZigBeeDeviceListener listener);

	public boolean removeListener(short clusterId, int side, ZigBeeDeviceListener listener);
	
	public boolean removeListener(ZigBeeDeviceListener listener);
	
	/**
	 * Enables the partition cluster on specific clusterId, commandId
	 * @param clusterId
	 * @return
	 */
	
	public boolean enablePartitionServer(short clusterId, short commandId);
	public boolean disablePartitionServer(short clusterId, short commandId);

	public void injectZclFrame(short clusterId, IZclFrame assembledZclFrame);
}
