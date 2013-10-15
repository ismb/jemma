/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.ah.internal.zigbee;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.MalformedMessageException;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.NotFoundException;
import org.energy_home.jemma.ah.hac.ReadOnlyAttributeException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.internal.util.timer.Timer;
import org.eclipse.equinox.internal.util.timer.TimerListener;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.ZigBeeException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPartitionServer;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.osgi.framework.ServiceReference;

import edu.emory.mathcs.backport.java.util.concurrent.SynchronousQueue;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

/**
 * The ZigBeeDeviceImpl class implements the ZigBeeDevice interface. The purpose
 * of this class is to implement the following features:
 * <ul>
 * <li>Automatic ping of the ZigBee device in order to verify if it is still
 * turned on or off. This is accolplished by doing the following things:
 * <ul>
 * <li>It keeps track of the successful configureReporting sent to the device.
 * Because the ZigBeeDeviceImpl instance is knows the timing it can start a
 * still-alive timer accordingly.</li>
 * <li>Each time a message arrives from the hw device this timer is restarted.</li>
 * <li>If no attribute reporting has been called by the driver, the ZigBeeDevice
 * polls the hw device after a certain amount of time. This time should be
 * greater than the poll time of the device, if the device is a sleeping device.
 * </li>
 * </ul>
 * </ul>
 */

public class ZigBeeDeviceImpl implements ZigBeeDevice, TimerListener {

	public static final int Disconnected = 0;
	public static final int Connected = 1;

	private ServiceDescriptor service;
	private int availState = Disconnected;

	int timeout = 3; // timeout

	private final int KeepAliveTimer = 0;
	private final int PingTimeoutTimer = 1;

	private int keepAliveTimeout = 60; // 1 minute

	private ZigBeeDeviceListener listener = null;
	private int consecutiveFailures = 0;
	private int maxFailuresBeforeDeadState = 3;

	byte seq = 30;

	private Timer timer;

	boolean warnings = false;

	private Hashtable pendingReplies = new Hashtable();
	private Hashtable listenersListClientSide = new Hashtable();
	private Hashtable listenersListServerSide = new Hashtable();

	private Lock messagesLock = new ReentrantLock();
	private Object lock = new Object();

	protected Log log = LogFactory.getLog(this.getClass());
	private ZigBeeDeviceListener driver;
	private ZigBeeManagerImpl zigbeeManager;
	private NodeDescriptor node = null;
	private NodeServices nodeServices;

	public ZigBeeDeviceImpl(ZigBeeManagerImpl zigbeeManager, Timer timer, ServiceDescriptor service) {
		this.zigbeeManager = zigbeeManager;
		this.timer = timer;
		this.service = service;
	}

	public ZigBeeDeviceImpl(ZigBeeManagerImpl zigbeeManager, Timer timer, NodeServices nodeServices, NodeDescriptor node, ServiceDescriptor service) {
		this.zigbeeManager = zigbeeManager;
		this.timer = timer;
		this.service = service;
		this.node = node;
		this.nodeServices = nodeServices;
	}

	/**
	 * Called by the Device Admin service to attach the device
	 * 
	 * @param driverRef
	 */

	public void attach(ServiceReference driverRef) {
		synchronized (lock) {
			this.driver = zigbeeManager.getService(driverRef);
			zigbeeManager.attach(this);
		}
	}

	public void detach() {
		synchronized (lock) {
			driver = null;
		}
	}

	public void noDriverFound() {
		synchronized (lock) {
			zigbeeManager.noDriverFound(this);
		}
	}

	String padding[] = { "0000000000000000", "000000000000000", "00000000000000", "0000000000000", "000000000000", "00000000000",
			"0000000000", "000000000", "00000000", "0000000", "000000", "00000", "0000", "000", "00", "0", "" };
	private boolean trackNode;

	public String getIeeeAddress() {
		String ieee = service.getAddress().getIeeeAddress().toString(16).toUpperCase();
		ieee = padding[ieee.length()] + ieee;
		return ieee;
	}

	protected short getEp() {
		return this.service.getEndPoint();
	}

	public ServiceDescriptor getServiceDescriptor() {
		return service;
	}

	public NodeDescriptor getNodeDescriptor() {
		return node;
	}

	public NodeServices getNodeServices() {
		return nodeServices;
	}

	public String getPid() {
		return getIeeeAddress();
	}

	public IZclFrame invoke(short clusterId, IZclFrame zclFrame) throws ZigBeeException {
		return invoke((short) this.service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue(), clusterId, zclFrame);
	}

	public IZclFrame invoke(short profileId, short clusterId, IZclFrame zclFrame) throws ZigBeeException {
		// check if the message contains requires a default answer

		long hash = calculateTxRxHash(clusterId, zclFrame);

		SynchronousQueue sq = new SynchronousQueue();

		messagesLock.lock();
		pendingReplies.put(new Long(hash), sq);
		messagesLock.unlock();

		if (log.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(true, hash, profileId, clusterId, zclFrame);

		synchronized (lock) {
			// TODO: do we need to synchronize here?
			boolean res = zigbeeManager.post(this, profileId, clusterId, zclFrame);
			if (!res)
				throw new ZigBeeException("error sending message to ZigBee device");
		}
		try {
			// here it should block till the matching response is received
			// or a timeout has occurred.

			timeout = 4;

			if (clusterId == 2819)
				timeout = 100;

			IZclFrame zclResponseFrame = (IZclFrame) sq.poll(timeout, TimeUnit.SECONDS);
			if (zclResponseFrame == null) {
				this.logZclMessage(false, hash, profileId, clusterId, null);

				if (trackNode) {
					synchronized (lock) {
						this.transmissionFailed();
					}
				}
				messagesLock.lock();
				pendingReplies.remove(new Long(hash));
				messagesLock.unlock();
				throw new ZigBeeException("timeout");
			}

			if (log.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
				this.logZclMessage(false, hash, profileId, clusterId, zclFrame);

			return zclResponseFrame;
		} catch (InterruptedException e) {
			throw new ZigBeeException("interrupted system call during post");
		}

	}

	private void logZclMessage(boolean outgoing, long hash, short profileId, short clusterId, IZclFrame zclFrame) {
		if (zclFrame != null) {
			if (outgoing) {
				if (hash != -1)
					log.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Tx > 0x" + Hex.toHexString(clusterId, 2)
							+ " [clusterId] " + zclFrame.toString());
				else
					log.debug(this.getPid() + ": " + "        " + " [hash]: Tx > 0x" + Hex.toHexString(clusterId, 2)
							+ " [clusterId] " + zclFrame.toString());
			}
			else
				log.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Rx > 0x" + Hex.toHexString(clusterId, 2)
						+ " [clusterId] " + zclFrame.toString());
		} else {
			log.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Rx > timeout in poll");
		}
	}

	/**
	 * Handles messages received for this device from the ZigBee Manager
	 * 
	 * @return true, if the message has been handled TODO: throw an exception in
	 *         case of errors in the incoming message
	 * @throws ZclException
	 */

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws ZclException {

		long hash = calculateTxRxHash(clusterId, zclFrame);

		if (log.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(false, hash,
					(short) this.service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue(), clusterId, zclFrame);

		if (trackNode) {
			deviceAlive();
		}

		if (isPartitioningCluster(clusterId)) {
			// the incoming frame is directed to the Partitioning Cluster
			// TODO: checks if client to server o server to client

			if (zclFrame.isClientToServer()) {
				// this is a message sent from the PartitioningClient to the
				// Partitioning Server clusters
				if (this.partitionFsmServer != null) {
					try {
						boolean handled = this.partitionFsmServer.notifyZclFrame(clusterId, zclFrame);
						if (handled)
							return handled;
					} catch (Exception e) {
						log.error("Exception in calling partition frame notifyZclFrame", e);
					}
				}
			}
		}

		messagesLock.lock();
		SynchronousQueue sq = (SynchronousQueue) pendingReplies.remove(new Long(hash));
		messagesLock.unlock();

		if (sq == null) {
			// simply sends the message to the upper layer. If any exception
			// arises this exception is decoded and sent back to the source
			// ZigBee device
			notifyListeners(clusterId, zclFrame);
		} else {
			try {
				sq.put(zclFrame);
			} catch (InterruptedException e) {
				if (log.isErrorEnabled())
					log.error("exception", e);

				return false;
			}
		}

		return true;
	}

	private void notifyListeners(short clusterId, IZclFrame zclFrame) throws ZclException {
		
		if (log.isDebugEnabled() && zigbeeManager.isNotifyFrameLogEnabled())
		log.debug("notify listeners for cluster " + clusterId);
		Vector listeners = null;

		if (zclFrame.isClientToServer()) {
			listeners = (Vector) listenersListClientSide.get(new Short(clusterId));
		}
		else {
			listeners = (Vector) listenersListServerSide.get(new Short(clusterId));
		}

		if ((listeners != null) && (listeners.size() > 0)) {			
			boolean handled = false;
			Throwable exception = null;
			
			for (int i = 0; i < listeners.size(); i++) {
				ZigBeeDeviceListener listener = (ZigBeeDeviceListener) listeners.get(i);
				
				try {
					handled = handled || listener.notifyZclFrame(clusterId, zclFrame);
				} catch (Throwable e) {
					// FIXME reply with an error sent to the peer device
					log.error("Exception while notifyZclFrame to clusterId " + clusterId, e);
					exception = e;
				}
			}
			
			if (!handled) {
				if (exception != null) {
					int status;
					if (exception instanceof ZclException) {
						status = ((ZclException) exception).getStatusCode();
					}
					else {
						status = upperLayerException2ZCLStatusCode(exception);
						
					}
					throw new ZclException(status);
				}
				
				// Altrough there are listeners, no one of them handled the incoming command
				log.debug("the command was not handled by any listener");
				if (zclFrame.isManufacturerSpecific()) {
					if (zclFrame.getFrameType() == IZclFrame.GENERAL_COMMAND) {
						throw new ZclException(ZCL.UNSUP_MANUF_GENERAL_COMMAND);
					}
					else if (zclFrame.getFrameType() == IZclFrame.CLUSTER_COMMAND){
						throw new ZclException(ZCL.UNSUP_MANUF_CLUSTER_COMMAND);
					}
					else {
						throw new ZclException(ZCL.NOT_AUTHORIZED);
					}
				}
				else {
					if (zclFrame.getFrameType() == IZclFrame.GENERAL_COMMAND) {
						throw new ZclException(ZCL.UNSUP_GENERAL_COMMAND);
					}
					else if (zclFrame.getFrameType() == IZclFrame.CLUSTER_COMMAND){
						throw new ZclException(ZCL.UNSUP_CLUSTER_COMMAND);
					}
					else {
						throw new ZclException(ZCL.NOT_AUTHORIZED);
					}					
				}
			}
		} else {
			log.debug("no listeners set");
			throw new ZclException(ZCL.NOT_AUTHORIZED);
		}
	}

	private int upperLayerException2ZCLStatusCode(Throwable e) {
		if (e instanceof UnsupportedClusterOperationException) {
			return ZCL.UNSUP_CLUSTER_COMMAND;
		}
		else if (e instanceof UnsupportedClusterAttributeException) {
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
		else if (e instanceof ReadOnlyAttributeException) {
			return ZCL.READ_ONLY;
		}
		else if (e instanceof NotAuthorized) {
			return ZCL.NOT_AUTHORIZED;
		}
		else if (e instanceof MalformedMessageException) {
			return ZCL.MALFORMED_COMMAND;
		}
		else if (e instanceof ServiceClusterException) {
			return ZCL.FAILURE;
		}
		else if (e instanceof NotFoundException) {
			return ZCL.FAILURE;
		}
		else if (e instanceof ZclException) {
			return ((ZclException) e).getStatusCode();
		}
		
		return ZCL.NOT_AUTHORIZED;
	}

	private void notifyListeners(int event) {
		if (this.listener != null)
			this.listener.notifyEvent(event);
	}

	public boolean post(short clusterId, IZclFrame zclFrame) {
		return this.post((short) this.service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue(), clusterId,
				zclFrame);
	}

	public boolean post(short profileId, short clusterId, IZclFrame zclFrame) {

		if (log.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(true, -1, profileId, clusterId, zclFrame);

		synchronized (lock) {
			return zigbeeManager.post(this, profileId, clusterId, zclFrame);
		}
	}

	public boolean isConnected() {
		synchronized (lock) {
			return (availState == Connected);
		}
	}

	private void deviceAlive() {
		timerStart(KeepAliveTimer, keepAliveTimeout);
		this.consecutiveFailures = 0;
		updateAvailableState(Connected);
	}

	private void deviceNotResponding() {
		this.notifyListeners(ZigBeeDeviceListener.LEAVE);
	}

	private void timerStart(int event, int timePeriod) {
		Timer time = (Timer) this.getTimer();
		time.notifyAfter(this, timePeriod, event);
	}

	private Timer getTimer() {
		return timer;
	}

	public void timer(int event) {
		switch (event) {
		case KeepAliveTimer:
			break;

		case PingTimeoutTimer:
			synchronized (lock) {
				this.transmissionFailed();
			}
			break;
		}
	}

	private void updateAvailableState(int newState) {
		if (availState != newState) {
			availState = newState;
			zigbeeManager.availStateUpdated(this, availState);
		}
	}

	private void transmissionFailed() {
		this.consecutiveFailures++;
		if (this.consecutiveFailures > maxFailuresBeforeDeadState) {
			this.consecutiveFailures = 0;
			this.deviceNotResponding();
		}
	}

	private long calculateTxRxHash(short clusterId, IZclFrame zclFrame) {
		long hash = clusterId;
		hash = hash << 8;
		return zclFrame.getSequenceNumber() & 0xFF | hash;
	}

	protected void announce() {
		synchronized (lock) {
			deviceAlive();
			log.debug("received an announcement on device " + getIeeeAddress());
			this.notifyListeners(ZigBeeDeviceListener.ANNOUNCEMENT);
		}
	}

	public boolean setListener(short clusterId, int side, ZigBeeDeviceListener listener) {
		synchronized (lock) {
			Hashtable listenersList = null;
			if (side == ZclServiceCluster.CLIENT_SIDE) {
				listenersList = listenersListClientSide;
			} 
			else if (side == ZclServiceCluster.SERVER_SIDE) {
				listenersList = listenersListServerSide;
			}
			else {
				return false;
			}

			Vector listeners = (Vector) listenersList.get(new Short(clusterId));
			if (listeners == null) {
				listeners = new Vector();
				listenersList.put(new Short(clusterId), listeners);
			}

			listeners.add(listener);
			return true;
		}
	}

	protected Vector getListeners(short clusterId) {
		synchronized (lock) {
			Vector listeners = (Vector) listenersListClientSide.get(new Short(clusterId));
			return listeners;
		}
	}

	public boolean removeListener(short clusterId, int side, ZigBeeDeviceListener listener) {
		synchronized (lock) {
			Hashtable listenersList = null;
			if (side == ZclServiceCluster.CLIENT_SIDE) {
				listenersList = listenersListClientSide;
			}
			else if (side == ZclServiceCluster.SERVER_SIDE) {
				listenersList = listenersListServerSide;
			} else {
				return false;
			}

			Vector listeners = (Vector) listenersList.get(new Short(clusterId));
			if (listeners != null) {
				return listeners.remove(listener);
			}
			return false;
		}
	}

	public boolean setListener(ZigBeeDeviceListener listener) {
		synchronized (lock) {
			if (this.listener != null)
				log.fatal("error. Another listener already set!");

			this.listener = listener;
			return true;
		}
	}

	public boolean removeListener(ZigBeeDeviceListener listener) {
		synchronized (lock) {
			if (this.listener == listener) {
				this.listener = null;
				return true;
			} else {
				log.fatal("error. Removing a listener never set!");
			}

			return false;
		}
	}

	public void remove() {
		synchronized (lock) {
			try {
				this.zigbeeManager.remove(this);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	ZclPartitionServer partitionFsmServer = null;

	public boolean enablePartitionServer(short clusterId, short commandId) {
		if (partitionFsmServer == null) {
			try {
				partitionFsmServer = new ZclPartitionServer((ZigBeeDevice) this);
				partitionFsmServer.zclAttach(this);

			} catch (ApplianceException e) {
				log.debug("Error creating ZclPartitionFsmServer", e);
				return false;
			}
		}
		return partitionFsmServer.enablePartitioning(clusterId, commandId);
	}

	public boolean disablePartitionServer(short clusterId, short commandId) {
		if (partitionFsmServer == null)
			return false;
		return partitionFsmServer.disablePartitioning(clusterId, commandId);
	}

	protected boolean isPartitioningCluster(short clusterId) {
		return (clusterId == 0x0016);
	}

	public void injectZclFrame(short clusterId, IZclFrame zclFrame) {
		try {
			this.notifyZclFrame(clusterId, zclFrame);
		} catch (ZclException e) {
			log.error("exception", e);
		}
	}
}
