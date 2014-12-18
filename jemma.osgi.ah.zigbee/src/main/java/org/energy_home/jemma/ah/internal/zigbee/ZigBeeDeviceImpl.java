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
package org.energy_home.jemma.ah.internal.zigbee;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.equinox.internal.util.timer.Timer;
import org.eclipse.equinox.internal.util.timer.TimerListener;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.MalformedMessageException;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.NotFoundException;
import org.energy_home.jemma.ah.hac.ReadOnlyAttributeException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.ZigBeeException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	int timeout = 10; // timeout

	private final int KeepAliveTimer = 0;
	private final int PingTimeoutTimer = 1;

	private int keepAliveTimeout = 60; // 1 minute

	private ZigBeeDeviceListener listener = null;
	private int consecutiveFailures = 0;
	private int maxFailuresBeforeDeadState = 3;

	byte seq = 30;

	private Timer timer;

	boolean warnings = false;

	private ConcurrentHashMap pendingReplies = new ConcurrentHashMap();
	private Hashtable listenersListClientSide = new Hashtable();
	private Hashtable listenersListServerSide = new Hashtable();

	private Object lockTimeoutTimer = new Object();
	private Object lockDevice = new Object();
	private Object lockService = new Object();
	private Object lockDriver = new Object();
	private Object lockPost = new Object();
	private Object lockStatus = new Object();
	private Object lockListener = new Object();

	private static final Logger LOG = LoggerFactory.getLogger(ZigBeeDeviceImpl.class);
	private ZigBeeDeviceListener driver;
	private ZigBeeManagerImpl zigbeeManager;
	private NodeDescriptor node = null;
	private NodeServices nodeServices;
	private Hashtable props;

	public Hashtable getProps() {
		return props;
	}

	public ZigBeeDeviceImpl(ZigBeeManagerImpl zigbeeManager, Timer timer, ServiceDescriptor service) {
		this.zigbeeManager = zigbeeManager;
		this.timer = timer;
		this.service = service;
	}

	public ZigBeeDeviceImpl(ZigBeeManagerImpl zigbeeManager, Timer timer, NodeServices nodeServices, NodeDescriptor node, ServiceDescriptor service, Hashtable props) {
		this.zigbeeManager = zigbeeManager;
		this.timer = timer;
		this.service = service;
		this.node = node;
		this.nodeServices = nodeServices;
		this.props = props;
	}

	/**
	 * Called by the Device Admin service to attach the device
	 * 
	 * @param driverRef
	 */

	public void attach(ServiceReference driverRef) {
		synchronized (lockService) {
			this.driver = zigbeeManager.getService(driverRef);
			zigbeeManager.attach(this);
		}
	}

	public void detach() {
		synchronized (lockService) {
			driver = null;
		}
	}

	public void noDriverFound() {
		synchronized (lockDriver) {
			zigbeeManager.noDriverFound(this);
		}
	}

	String padding[] = { "0000000000000000", "000000000000000", "00000000000000", "0000000000000", "000000000000", "00000000000", "0000000000", "000000000", "00000000", "0000000", "000000", "00000", "0000", "000", "00", "0", "" };
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

	public synchronized IZclFrame invoke(short profileId, short clusterId, IZclFrame zclFrame) throws ZigBeeException {

		// check if the message contains requires a default answer

		long hash = calculateTxRxHash(clusterId, zclFrame);

		LOG.debug("Sending a ZCLFrame with Key:" + hash + " -- for CLuster:" + clusterId + " --- TO:" + this.getIeeeAddress());
		long key = new Long(hash);
		SynchronousQueue sq = new SynchronousQueue();

		pendingReplies.put(key, sq);

		if (LOG.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(true, hash, profileId, clusterId, zclFrame);

		synchronized (lockPost) {
			// TODO: do we need to synchronize here?
			boolean res = zigbeeManager.post(this, profileId, clusterId, zclFrame);
			if (!res) {
				pendingReplies.remove(key);
				throw new ZigBeeException("error sending message to ZigBee device Ieee:" + this.getIeeeAddress() + " -- ProfileId:" + profileId + " -- ClusterID:" + clusterId);
			}
		}
		try {
			// here it should block till the matching response is received
			// or a timeout has occurred.

			IZclFrame zclResponseFrame;

			if (clusterId == 2819)
				zclResponseFrame = (IZclFrame) sq.poll(100, TimeUnit.SECONDS);
			else
				zclResponseFrame = (IZclFrame) sq.poll(timeout, TimeUnit.SECONDS);

			if (zclResponseFrame == null) {
				this.logZclMessage(false, hash, profileId, clusterId, null);

				if (trackNode) {
					synchronized (lockPost) {
						this.transmissionFailed();
					}
				}
				pendingReplies.remove(new Long(hash));
				throw new ZigBeeException("No response from ZigBee device Ieee:" + this.getIeeeAddress() + " -- ProfileId:" + profileId + " -- ClusterID:" + clusterId);

			}

			if (LOG.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
				this.logZclMessage(false, hash, profileId, clusterId, zclFrame);

			return zclResponseFrame;
		} catch (Exception e) {
			throw new ZigBeeException("Exception during post: " + e.getMessage());
		}

	}

	private void logZclMessage(boolean outgoing, long hash, short profileId, short clusterId, IZclFrame zclFrame) {
		if (zclFrame != null) {
			if (outgoing) {
				if (hash != -1)
					LOG.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Tx > 0x" + Hex.toHexString(clusterId, 2) + " [clusterId] " + zclFrame.toString());
				else
					LOG.debug(this.getPid() + ": " + "        " + " [hash]: Tx > 0x" + Hex.toHexString(clusterId, 2) + " [clusterId] " + zclFrame.toString());
			} else
				LOG.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Rx > 0x" + Hex.toHexString(clusterId, 2) + " [clusterId] " + zclFrame.toString());
		} else {
			LOG.debug(this.getPid() + ": " + Hex.toHexString(hash, 4) + " [hash]: Rx > timeout in poll");
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

		LOG.debug("Received a ZCLFrame with Key:" + hash + " for clusterId: " + clusterId + " --- Data:" + zclFrame.toString());
		if (LOG.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(false, hash, (short) this.service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue(), clusterId, zclFrame);

		if (trackNode) {
			deviceAlive();
		}

		if (isPartitioningCluster(clusterId)) {
			// the incoming frame is directed to the Partitioning Cluster
			if (zclFrame.isClientToServer()) {
				// this is a message sent from the PartitioningClient to the
				// Partitioning Server clusters that is implemented on the
				// gateway
				if (this.partitionServerImpl != null) {
					try {
						boolean handled = this.partitionServerImpl.notifyZclFrame(clusterId, zclFrame);
						if (handled)
							return handled;
					} catch (Exception e) {
						LOG.error("Exception in calling partition frame notifyZclFrame", e);
					}
				}
			}
		}

		/* skip Reporting Attributes */
		if (!zclFrame.isClientToServer() && zclFrame.getCommandId() == 10) {
			notifyListeners(clusterId, zclFrame);
		} else {
			SynchronousQueue sq = null;
			sq = (SynchronousQueue) pendingReplies.remove(new Long(hash));
			if (sq == null) {
				// simply sends the message to the upper layer. If any
				// exception
				// arises this exception is decoded and sent back to the
				// source
				// ZigBee device
				notifyListeners(clusterId, zclFrame);
			} else {
				try {
					LOG.debug("THID: " + Thread.currentThread().getId() + " before sq.put(zclFrame) Hash:" + String.format("%04X", hash));

					sq.put(zclFrame);

					LOG.debug("THID: " + Thread.currentThread().getId() + " after sq.put(zclFrame)");

				} catch (Exception e) {
					LOG.error("exception", e);

					return false;
				}
			}

		}

		return true;

	}

	private void notifyListeners(short clusterId, IZclFrame zclFrame) throws ZclException {

		if (LOG.isDebugEnabled() && zigbeeManager.isNotifyFrameLogEnabled())
			LOG.debug("notify listeners for cluster " + clusterId);

		Vector listeners = null;

		if (zclFrame.isClientToServer()) {
			listeners = (Vector) listenersListClientSide.get(new Short(clusterId));
		} else {
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
					LOG.error("Exception while notifyZclFrame to clusterId " + clusterId, e);
					exception = e;
				}
			}

			if (!handled) {
				if (exception != null) {
					int status;
					if (exception instanceof ZclException) {
						status = ((ZclException) exception).getStatusCode();
					} else {
						status = upperLayerException2ZCLStatusCode(exception);

					}
					throw new ZclException(status);
				}

				// Altrough there are listeners, no one of them handled the
				// incoming command
				LOG.debug("the command was not handled by any listener");
				if (zclFrame.isManufacturerSpecific()) {
					if (zclFrame.getFrameType() == IZclFrame.GENERAL_COMMAND) {
						throw new ZclException(ZCL.UNSUP_MANUF_GENERAL_COMMAND);
					} else if (zclFrame.getFrameType() == IZclFrame.CLUSTER_COMMAND) {
						throw new ZclException(ZCL.UNSUP_MANUF_CLUSTER_COMMAND);
					} else {
						throw new ZclException(ZCL.NOT_AUTHORIZED);
					}
				} else {
					if (zclFrame.getFrameType() == IZclFrame.GENERAL_COMMAND) {
						throw new ZclException(ZCL.UNSUP_GENERAL_COMMAND);
					} else if (zclFrame.getFrameType() == IZclFrame.CLUSTER_COMMAND) {
						throw new ZclException(ZCL.UNSUP_CLUSTER_COMMAND);
					} else {
						throw new ZclException(ZCL.NOT_AUTHORIZED);
					}
				}
			}
		} else {
			LOG.debug("no listeners set");
			throw new ZclException(ZCL.NOT_AUTHORIZED);
		}
	}

	private int upperLayerException2ZCLStatusCode(Throwable e) {
		if (e instanceof ZclException) {
			return ((ZclException) e).getStatusCode();
		} else if (e instanceof ServiceClusterException) {
			if (e instanceof UnsupportedClusterOperationException) {
				return ZCL.UNSUP_CLUSTER_COMMAND;
			} else if (e instanceof UnsupportedClusterAttributeException) {
				return ZCL.UNSUPPORTED_ATTRIBUTE;
			} else if (e instanceof ReadOnlyAttributeException) {
				return ZCL.READ_ONLY;
			} else if (e instanceof NotAuthorized) {
				return ZCL.NOT_AUTHORIZED;
			} else if (e instanceof MalformedMessageException) {
				return ZCL.MALFORMED_COMMAND;
			} else if (e instanceof NotFoundException) {
				return ZCL.NOT_FOUND;
			} else
				return ZCL.FAILURE;
		}

		return ZCL.NOT_AUTHORIZED;
	}

	private void notifyListeners(int event) {
		if (this.listener != null)
			this.listener.notifyEvent(event);
	}

	public boolean post(short clusterId, IZclFrame zclFrame) {
		return this.post((short) this.service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue(), clusterId, zclFrame);
	}

	public boolean post(short profileId, short clusterId, IZclFrame zclFrame) {

		if (LOG.isDebugEnabled() && zigbeeManager.isRxTxLogEnabled())
			this.logZclMessage(true, -1, profileId, clusterId, zclFrame);

		synchronized (lockPost) {
			return zigbeeManager.post(this, profileId, clusterId, zclFrame);
		}
	}

	public boolean isConnected() {
		synchronized (lockStatus) {
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
			synchronized (lockTimeoutTimer) {
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
		String Key = String.format("%04X%02X", clusterId, zclFrame.getSequenceNumber());
		long hash = Long.parseLong(Key, 16);
		return hash;
	}

	protected void announce() {
		synchronized (lockDevice) {
			deviceAlive();
			LOG.debug("received an announcement on device " + getIeeeAddress());
			this.notifyListeners(ZigBeeDeviceListener.ANNOUNCEMENT);
		}
	}

	public boolean setListener(short clusterId, int side, ZigBeeDeviceListener listener) {
		synchronized (lockListener) {
			Hashtable listenersList = null;
			if (side == ZclServiceCluster.CLIENT_SIDE) {
				listenersList = listenersListClientSide;
			} else if (side == ZclServiceCluster.SERVER_SIDE) {
				listenersList = listenersListServerSide;
			} else {
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
		synchronized (lockListener) {
			Vector listeners = (Vector) listenersListClientSide.get(new Short(clusterId));
			return listeners;
		}
	}

	public boolean removeListener(short clusterId, int side, ZigBeeDeviceListener listener) {
		synchronized (lockListener) {
			Hashtable listenersList = null;
			if (side == ZclServiceCluster.CLIENT_SIDE) {
				listenersList = listenersListClientSide;
			} else if (side == ZclServiceCluster.SERVER_SIDE) {
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
		synchronized (lockListener) {
			if (this.listener != null)
				LOG.warn("error. Another listener already set!");

			this.listener = listener;
			return true;
		}
	}

	public boolean removeListener(ZigBeeDeviceListener listener) {
		synchronized (lockListener) {
			if (this.listener == listener) {
				this.listener = null;
				return true;
			} else {
				LOG.error("error. Removing a listener never set!");
			}

			return false;
		}
	}

	public void remove() {
		synchronized (lockDevice) {
			try {
				this.zigbeeManager.remove(this);
			} catch (Exception e) {
				LOG.error("Exception on remove", e);
			}
		}
	}

	ZclPartitionServerImpl partitionServerImpl = null;

	public boolean enablePartitionServer(short clusterId, short commandId) {
		if (partitionServerImpl == null) {
			try {
				// FIXME: maybe the following call is better: partitionClient =
				// new ZclPartitionClient(new ZciPartitionServerImpl());
				partitionServerImpl = new ZclPartitionServerImpl();
				partitionServerImpl.zclAttach(this);
			} catch (ApplianceException e) {
				LOG.debug("Error creating ZclPartitionFsmServer", e);
				return false;
			}
		}
		return partitionServerImpl.enablePartitioning(clusterId, commandId);
	}

	public boolean disablePartitionServer(short clusterId, short commandId) {
		if (partitionServerImpl == null)
			return false;

		return partitionServerImpl.disablePartitioning(clusterId, commandId);
	}

	protected boolean isPartitioningCluster(short clusterId) {
		return (clusterId == 0x0016);
	}

	public void injectZclFrame(short clusterId, IZclFrame zclFrame) {
		try {
			this.notifyZclFrame(clusterId, zclFrame);
		} catch (ZclException e) {
			LOG.error("exception on injectZclFrame", e);
		}
	}
}
