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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.equinox.internal.util.timer.Timer;
import org.eclipse.equinox.internal.util.timer.TimerListener;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyQueryResponse;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.ZigBeeMngrService;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.closures.ZclDoorLockClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.closures.ZclWindowCoveringClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceEventsAndAlertsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceIdentificationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclApplianceStatisticsClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclMeterIdentificationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclPowerProfileClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclBasicServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyQueryResponse;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclIdentifyServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclLevelControlClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclOnOffServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPartitionServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPowerConfigurationClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclTimeServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.hvac.ZclThermostatClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.lube.ZclAirQualityClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclIlluminanceMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclOccupancySensingClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclRelativeHumidityMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.measurement.ZclTemperatureMeasurementClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.metering.ZclSimpleMeteringClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.metering.ZclSimpleMeteringServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.security.ZclIASZoneClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.zll.ZclLightLinkColorControlClient;
import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Device;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.TxOptions;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class InstallationStatus implements Serializable {
	private static final long serialVersionUID = 3982442253260584361L;

	public static final int ANNOUNCEMENT_RECEIVED = 1;
	public static final int WAITING_FOR_SERVICES = 2;
	public static final int ACTIVE_ENDPOINTS_RETRIEVED = 3;
	public static final int WAITING_FOR_SERVICE_DESCRIPTOR = 4;
	public static final int WAITING_FOR_NODE_DESCRIPTOR = 5;
	public static final int INSTALLED = 6;
	public static final int SERVICE_DESCRIPTOR_RETRIEVED = 7;

	private int status = 0;
	private Address address;
	private int retryCounter = 5;
	private NodeDescriptor nodeDescriptor;
	private int currentEpIndex = -1;
	private long time;
	private HashMap activeEpsMap = new HashMap();
	private NodeServices services;

	public InstallationStatus(Address a) {
		this.address = a;
	}

	public int getStatus() {
		return status;
	}

	public Address getAddress() {
		return this.address;
	}

	public void setCurrentService(int i) {
		this.currentEpIndex = i;
	}

	public int getRetryCounter() {
		return retryCounter--;
	}

	public void resetRetryCounter() {
		retryCounter = 4;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setNodeDescriptor(NodeDescriptor node) {
		this.nodeDescriptor = node;
	}

	public void setNodeServices(NodeServices services) {
		this.services = services;
	}

	public NodeServices getNodeServices() {
		return this.services;
	}

	public int getCurrentService() {
		return this.currentEpIndex;
	}

	public NodeDescriptor getNodeDescriptor() {
		return this.nodeDescriptor;
	}

	public long getTime() {
		return time;
	}

	public void refreshTime() {
		this.time = System.currentTimeMillis();
	}

	public ServiceDescriptor getServiceDescriptor(short endPoint) {
		return (ServiceDescriptor) this.activeEpsMap.get(new Short(endPoint));
	}

	public void putServiceDescriptor(short endPoint, ServiceDescriptor service) {
		this.activeEpsMap.put(new Short(endPoint), service);
	}

	public void addServiceDescriptor(short endPoint, ServiceDescriptor service) {
		this.activeEpsMap.put(new Short(endPoint), service);
	}

	public String toString() {
		return "InstallationStatus [ieee=" + ZigBeeManagerImpl.getIeeeAddressHex(this.getAddress()) + ", status: " + this.status + "]";
	}
}

class ActiveEpInstallationStatus {
	short activeEndPoint;
	ServiceDescriptor service;

}

public class ZigBeeManagerImpl implements TimerListener, APSMessageListener, GatewayEventListener, ZigBeeMngrService, INetworkManager {

	private Timer timer;

	private Vector zigbeeDevices = new Vector();

	// private Hashtable ieee2service = new Hashtable();
	private Hashtable ieee2devices = new Hashtable();
	private Hashtable ieee2sr = new Hashtable();

	/**
	 * Used to track the status of a ZigBee device installation phase. This is a
	 * dictionary indexed on ieee address { String ieeeAddress,
	 * InstallationStatus status }
	 */

	private ConcurrentHashMap devicesUnderInstallation = new ConcurrentHashMap();
	private ConcurrentHashMap devicesInstalled = new ConcurrentHashMap();
	private ConcurrentLinkedQueue discoveredNodesQueue = new ConcurrentLinkedQueue();
	private ConcurrentLinkedQueue inProcessNode = new ConcurrentLinkedQueue();
	private ConcurrentHashMap installedDevices = new ConcurrentHashMap();

	private boolean enableRxTxLogs = true;
	private boolean enableLockingLogs = false;
	private boolean enableNotifyFrameLogs = true;
	private boolean enableDiscoveryLogs = true;
	private boolean enableDsLogs = false;

	private boolean enableAllClusters = false;
	private boolean enableEnergyAtHomeClusters = true;

	private static final int JGalReconnectTimer = 2;
	private static final int discoveryTimer = 3;
	private static final int permitJoinAllTimer = 4;
	private static final int galCommandTimer = 5;

	private GatewayInterface gateway;

	// FIXME: currently we use EP 8 because of a problem in the GAL
	private short localEndpoint = 1;
	protected long callbackId = -1;
	SimpleDescriptor sd = null;

	private long timeout = 7000;
	private int timeoutOffset = 2;

	int galRunning = 0;
	// for accessing the data structures
	// private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	private Object sLock = new Object(); // for DS bind and unbind methods
	private ComponentContext ctxt;

	/**
	 * By default use the nvram.
	 */
	private boolean useNVMNetworkSetting = true;
	private boolean handleMultipleEps = true;

	/**
	 * enable/disable caching in memory of the information related to the
	 * discovered ZigBee Nodes (Node Descriptor, Active EPs and Simple
	 * Descriptors). If this variable is enabled, the node InstallationStatus
	 * instance is put in an Hashtable indexed on the ZigBee node IEEE address.
	 */
	private boolean cacheDiscoveryInfos = true;
	/**
	 * enable/disable persistence on the cached InstallationStatus objects. This
	 * flag is meaningful only if cacheDiscoveryInfos is true.
	 */
	private boolean dumpDiscoveryInfos = true;
	/**
	 * If false, only the sleeping end-devices descriptors are made persistent
	 * and retrieved back on startup. This flag doesn't have any effect if
	 * dumpDiscoveryInfos is false.
	 */
	private boolean dumpAllDevices = false;

	private Properties properties;

	private EventAdmin eventAdmin;
	// FIXME Mass-rename LOG to LOG for consistancy
	private static final Logger LOG = LoggerFactory.getLogger(ZigBeeManagerImpl.class);

	public static final String propertyFilename = "org.energy_home.jemma.ah.zigbee.properties";
	private String propertiesFilename = ".";
	private ZigBeeManagerProperties cmProps = new ZigBeeManagerProperties();

	/**
	 * The name of the file used to store the cache of discovered devices.
	 */
	private String cacheFilename = "cache.dump";
	private File cacheFile = null;
	private ExecutorService executor = null;

	public ZigBeeManagerImpl() {

		executor = Executors.newFixedThreadPool(15, new ThreadFactory() {

			@Override
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-NetworkManager");
			}
		});

		if (executor instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) executor).setKeepAliveTime(3, TimeUnit.MINUTES);
			((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
		}
	}

	protected synchronized void activate(ComponentContext ctxt, Map props) {

		this.ctxt = ctxt;
		if (enableDsLogs)
			LOG.debug("activated");

		this.propertiesFilename = this.ctxt.getBundleContext().getProperty("osgi.instance.area") + propertyFilename;
		this.cacheFile = this.ctxt.getBundleContext().getDataFile(cacheFilename);

		update(props);
		handleBundleUpgrade();

		if (cacheDiscoveryInfos) {
			loadDiscoveredDevicesDb();
		}

		if (!isGalRunning()) {
			this.bindGal();
		}

	}

	private void handleBundleUpgrade() {
		this.loadProperties();
	}

	private void update(Map props) {
		if (enableDsLogs)
			LOG.debug("received configuration");

		boolean enableLqi = cmProps.isLqiEnabled();

		cmProps.update(props);

		if (cmProps.isLqiEnabled() != enableLqi) {
			if (isGalRunning()) {
				if (cmProps.isLqiEnabled()) {
					timerStart(discoveryTimer, cmProps.getInitialDiscoveryDelay());
				} else {
					timerCancel(discoveryTimer);
				}
			}

			if (enableDsLogs)
				LOG.debug("updated enableLqi to '" + cmProps.isLqiEnabled() + "'");
		}
	}

	protected synchronized void deactivate(ComponentContext ctxt) {

		if (enableDsLogs)
			LOG.debug("deactivated");
		cancelAllTimers();

		try {
			unbindGal();
		} catch (Exception e) {
			LOG.error("error unbinding gal", e);
		}

		if (cacheDiscoveryInfos) {
			dumpDiscoveredDevicesDb(false);
		}

	}

	protected synchronized void modified(ComponentContext ctxt, Map props) {
		update(props);

	}

	protected void setTimer(Timer timer) {
		synchronized (sLock) {
			this.timer = timer;
		}
	}

	protected void unsetTimer(Timer timer) {
		synchronized (sLock) {
			this.timer = null;
		}
	}

	private void timerStart(int event, int timePeriod) {
		synchronized (sLock) {
			Timer time = (Timer) getTimer();
			time.notifyAfter(this, timePeriod, event);
		}
	}

	private Timer getTimer() {
		return timer;
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		synchronized (sLock) {
			this.eventAdmin = eventAdmin;
		}
	}

	public synchronized void unsetEventAdmin(EventAdmin eventAdmin) {
		synchronized (sLock) {
			if (this.eventAdmin == eventAdmin)
				this.eventAdmin = null;
		}
	}

	private void timerCancel(int event) {
		synchronized (sLock) {
			Timer time = (Timer) getTimer();
			time.removeListener(this, event);
		}
	}

	private void cancelAllTimers() {
		timerCancel(discoveryTimer);
		timerCancel(JGalReconnectTimer);
		timerCancel(permitJoinAllTimer);
		timerCancel(galCommandTimer);
	}

	private void printAPSMessageEvent(APSMessageEvent msg) {
		String s = "";
		s += getIeeeAddressHex(msg.getSourceAddress()) + ":" + " Thr " + Thread.currentThread().getId() + ": notifyAPSMessage()" + " " + msg.getClusterID() + " ";
		s += Hex.byteToHex(msg.getData(), 0);
		LOG.debug(s);
	}

	private boolean trackNode;
	private String galIeeeAddress;

	/**
	 * Utility method for retrieving the ieee address from the Address class
	 * 
	 * @param address
	 *            The Address class instance
	 * 
	 * @return A string representing the hex representation (16 Hex digits) of
	 *         the IEEE address.
	 */

	public static final String getIeeeAddressHex(Address address) {
		if (address == null)
			return null;

		if (address.getIeeeAddress() != null) {
			String ieee = address.getIeeeAddress().toString(16).toUpperCase();
			ieee = Utils.padding[ieee.length()] + ieee;
			return ieee;
		} else
			return null;
	}

	public static final String getNodePid(Address address) {

		if (address == null)
			return null;

		if (address.getIeeeAddress() != null) {
			String ieee = address.getIeeeAddress().toString(16).toUpperCase();
			ieee = Utils.padding[ieee.length()] + ieee;
			return ieee;
		} else
			return null;
	}

	protected static String getIeeeAddress(Address a) {
		if (a.getIeeeAddress() != null) {
			return a.getIeeeAddress().toString();
		} else
			return null;
	}

	/**
	 * Called when a message has been received from ZigBee
	 */
	public void notifyAPSMessage(final APSMessageEvent msg) {
		executor.execute(new Runnable() {
			public void run() {

				LOG.debug("Ah.Zigbee -- notifyAPSMessage");
				if (enableNotifyFrameLogs)
					printAPSMessageEvent(msg);

				if (msg.getDestinationEndpoint() == 0xFF) {

					LOG.debug("Ah.Zigbee -- notifyAPSMessage BROADCAST");
					handleBroadcastMessages(msg);
					return;
				}

				int cluster = msg.getClusterID();

				// forward the message to the peer device
				Address srcAddress = msg.getSourceAddress();
				String nodePid = getNodePid(srcAddress);

				if (nodePid == null) {
					LOG.debug("message discarded because the src node ieee address is not present");
					return;
				}

				if ((LOG != null) && (enableNotifyFrameLogs)) {
					LOG.debug(getIeeeAddressHex(srcAddress) + ": Thr " + Thread.currentThread().getId() + ": messageReceived()");
				}

				// Drop messages that doesn't belong to the exported
				// clusters
				if (enableNotifyFrameLogs)
					printAPSMessageEvent(msg);

				Vector devices = (Vector) ieee2devices.get(nodePid);

				ZclFrame zclFrame = new ZclFrame(msg.getData());

				int clusterID = msg.getClusterID();
				// If profileID == 0 or Default responce received

				if (msg.getProfileID() > 0 && zclFrame.getCommandId() != 0x0b) {
					if (!checkGatewaySimpleDescriptor(clusterID, zclFrame)) {
						// FIXME: qui dovremmo dare un errore differente a
						// seconda se il
						// comando' e' generale e manufacturer specific
						IZclFrame zclResponseFrame = getDefaultResponse(zclFrame, ZCL.UNSUP_CLUSTER_COMMAND);
						LOG.error("APS message coming from clusterID 0x" + Hex.toHexString(clusterID, 4) + ". This clusterId is not supported by the gateway");
						post(msg, zclResponseFrame);
						return;
					}
				}

				if (devices != null) {
					Iterator it = devices.iterator();
					boolean epFound = false;
					while (it.hasNext()) {
						ZigBeeDeviceImpl device = (ZigBeeDeviceImpl) it.next();
						if (device.getEp() == msg.getSourceEndpoint()) {
							if (enableNotifyFrameLogs) {
								LOG.debug("notifyZclFrame() : Thr " + Thread.currentThread().getId() + " " + msg.getClusterID() + " message to ep " + device.getEp());
							}
							try {
								device.notifyZclFrame((short) msg.getClusterID(), zclFrame);
							} catch (ZclException e) {
								// TODO: check merge, following if was
								// commented in
								// 3.3.0
								// if
								// (!zclFrame.isDefaultResponseDisabled()) {
								IZclFrame zclResponseFrame = getDefaultResponse(zclFrame, e.getStatusCode());
								post(msg, zclResponseFrame);
								LOG.error(getIeeeAddressHex(srcAddress) + ": messageReceived(): Sent to device a default response with status code " + e.getStatusCode());
								// }

							}

							if (enableNotifyFrameLogs) {
								LOG.debug("after notifyZclFrame() : Thr " + Thread.currentThread().getId() + " " + msg.getClusterID() + " message to ep " + device.getEp());
							}
							epFound = true;

							break;
						}
					}
					if (!epFound && LOG.isDebugEnabled())
						LOG.error("not found any matching ep for the incoming message");

				} else {
					IZclFrame zclResponseFrame;
					InstallationStatus installationStatus = getInstallingDevice(srcAddress);
					if (installationStatus != null) {
						LOG.error(getIeeeAddressHex(srcAddress) + ": received a message from a node that is not installed. Reply with TIMEOUT");
						zclResponseFrame = getDefaultResponse(zclFrame, 0x94);
					} else {
						LOG.error("received a message from an unknown node " + getIeeeAddressHex(srcAddress) + " . Reply with TIMEOUT");

						zclResponseFrame = getDefaultResponse(zclFrame, 0x94);
					}

					post(msg, zclResponseFrame);
				}

				if (enableNotifyFrameLogs) {
					LOG.debug(getIeeeAddressHex(msg.getSourceAddress()) + ": " + " Thr " + Thread.currentThread().getId() + ": leave notifyAPSMessage()");
				}

			}
		});
	}

	private boolean checkGatewaySimpleDescriptor(int clusterID, IZclFrame zclFrame) {
		if (sd != null) {
			List clusters;
			if (zclFrame.isClientToServer()) {
				clusters = sd.getApplicationInputCluster();
			} else {
				clusters = sd.getApplicationOutputCluster();

			}
			if ((clusters == null) || (clusters != null) && (!clusters.contains(clusterID)))
				return false;

			return true;
		}
		return false;
	}

	private IZclFrame getDefaultResponse(IZclFrame zclFrame, int statusCode) {
		IZclFrame responseZclFrame = zclFrame.createResponseFrame(2);
		responseZclFrame.setCommandId(ZCL.ZclDefaultRsp);
		responseZclFrame.appendUInt8(zclFrame.getCommandId());
		responseZclFrame.setFrameType(IZclFrame.GENERAL_COMMAND);
		responseZclFrame.appendUInt8(statusCode);
		return responseZclFrame;
	}

	public void inquiryCompleted(int inquiryStatus) {
		LOG.debug("inquiryCompleted: not used!");
	}

	public void nodeDiscovered(final Status status, final WSNNode node) {

		executor.execute(new Runnable() {
			public void run() {
				LOG.debug("=======> Nodo node.getAddress().getIeeeAddress() = " + node.getAddress().getIeeeAddress());
				LOG.debug("=======> Nodo node.getAddress().getNetworkAddress() = " + node.getAddress().getNetworkAddress());

				if (status.getCode() != GatewayConstants.SUCCESS) {
					LOG.error("called nodeDiscovered with status different from SUCCESS, message is '" + status.getMessage() + "'");
					return;
				}

				Address a = node.getAddress();

				if (enableDiscoveryLogs)
					LOG.info(getIeeeAddressHex(a) + ": node discovered");

				// skip the coordinator
				if (a.getNetworkAddress().intValue() == 0) {
					galIeeeAddress = getIeeeAddressHex(a);
					if (enableDiscoveryLogs)
						LOG.debug("discovered node with address 0. Skipping it");
					return;
				}

				nodeDiscovered(a);

			}
		});
	}

	private void nodeDiscovered(Address a) {
		String nodePid = getNodePid(a);
		Vector devices = (Vector) this.getDevices(nodePid);
		if (devices == null) {
			// This is a new node
			if (enableDiscoveryLogs)
				LOG.debug(getIeeeAddressHex(a) + ": announcement from a new node");

			// starts installation process
			InstallationStatus installationStatus = this.getInstallingDevice(a);
			if (installationStatus == null) {
				if (enableDiscoveryLogs)
					LOG.debug(getIeeeAddressHex(a) + ": discovered new device ... installing it");
				installationStatus = this.addInstallingDevice(a);
				installationStatus.refreshTime();
				installationStatus.setStatus(InstallationStatus.ANNOUNCEMENT_RECEIVED);

				synchronized (discoveredNodesQueue) {
					discoveredNodesQueue.offer(installationStatus);
				}
			} else {
				if (installationStatus.getStatus() == InstallationStatus.ANNOUNCEMENT_RECEIVED) {
					if (enableDiscoveryLogs)
						LOG.debug(getIeeeAddressHex(a) + ": duplicate announcement");

					long age = System.currentTimeMillis() - installationStatus.getTime();
					if (enableDiscoveryLogs)
						LOG.debug(getIeeeAddressHex(a) + ": the announcement has an age of " + age + " ms");

					if (age > 20000) {
						synchronized (discoveredNodesQueue) {
							if (!discoveredNodesQueue.contains(installationStatus)) {
								discoveredNodesQueue.offer(installationStatus);
							} else {
								LOG.error(getIeeeAddressHex(a) + ": too old ... restartarting discovery");
							}
						}
					}
				} else {
					if (enableDiscoveryLogs)
						LOG.debug(getIeeeAddressHex(a) + ": discovery process in progress for device ");
				}
			}

			this.handleNextDiscoveredNode();
		} else {
			if (enableDiscoveryLogs)
				LOG.info(getIeeeAddressHex(a) + ": received announcement from an already known node");
			// notifies all devices
			Iterator it = devices.iterator();
			while (it.hasNext()) {
				ZigBeeDeviceImpl device = (ZigBeeDeviceImpl) it.next();
				device.announce();
			}
		}
	}

	public void printTables() {
		StringBuilder info = new StringBuilder();
		synchronized (inProcessNode) {
			info.append("\n\rinProcessNode (" + inProcessNode.size() + "):");

			for (Iterator iterator = inProcessNode.iterator(); iterator.hasNext();) {
				info.append("\n\r" + ((InstallationStatus) iterator.next()).toString());
			}
		}
		synchronized (discoveredNodesQueue) {
			info.append("\n\rdiscoveredNodesQueue(" + discoveredNodesQueue.size() + "):");

			for (Iterator iterator = discoveredNodesQueue.iterator(); iterator.hasNext();) {
				info.append("\n\r" + ((InstallationStatus) iterator.next()).toString());
			}
		}
		synchronized (devicesUnderInstallation) {
			info.append("\n\rdevicesUnderInstallation (" + devicesUnderInstallation.size() + "):");

			for (Iterator iterator = devicesUnderInstallation.values().iterator(); iterator.hasNext();) {
				info.append("\n\r" + ((InstallationStatus) iterator.next()).toString());
			}
		}

		synchronized (installedDevices) {

			info.append("\n\rtinstalledDevices (" + installedDevices.size() + "):");

			for (Iterator iterator = installedDevices.values().iterator(); iterator.hasNext();) {
				info.append("\n\r" + ((InstallationStatus) iterator.next()).toString());
			}
		}

		LOG.info(info.toString());
	}

	private void startNodeDiscoveryProcess(InstallationStatus installationStatus) {
		Address a = installationStatus.getAddress();
		String nodePid = getNodePid(installationStatus.getAddress());
		Vector devices = (Vector) this.getDevices(nodePid);
		if (devices == null) {
			try {
				if (enableDiscoveryLogs) {
					LOG.debug(getIeeeAddressHex(a) + ": beginning device discovery");
				}
				installationStatus.setStatus(InstallationStatus.WAITING_FOR_NODE_DESCRIPTOR);
				gateway.getNodeDescriptor(timeout, a);
				timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
				if (enableDiscoveryLogs) {
					LOG.debug(getIeeeAddressHex(a) + ": called getNodeDescriptor()");
				}
			} catch (Exception e) {
				this.terminateDeviceDiscovery(installationStatus);
				this.handleNextDiscoveredNode();
			}
		}

	}

	public void servicesDiscovered(final Status status, final NodeServices services) {

		executor.execute(new Runnable() {
			public void run() {

				if (status.getCode() != GatewayConstants.SUCCESS) {

					timerCancel(galCommandTimer);

					// in case of failure services is null and there is no
					// way to
					// retrieve the Address so try to guess it.
					InstallationStatus installingDevice = getInstallingDevice(InstallationStatus.WAITING_FOR_SERVICES);

					if (installingDevice != null) {
						Address a = installingDevice.getAddress();
						LOG.error(getIeeeAddressHex(a) + ": servicesDiscovered callback returned error code " + status.getCode() + "'. Guessed address '" + getIeeeAddressHex(a));

						// retries until retry counter goes to 0
						if (installingDevice.getRetryCounter() > 0) {
							try {
								LOG.debug(getIeeeAddressHex(a) + ": retry startServiceDiscovery()");
								gateway.startServiceDiscovery(timeout, a);
								timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
								return;
							} catch (Exception e) {
								LOG.error("exception in startServiceDiscovery() ", e);
							}
						}

						// abort installation of this node
						LOG.error(getIeeeAddressHex(a) + ": too many retries for getting services");
						terminateDeviceDiscovery(installingDevice);
						handleNextDiscoveredNode();
						return;
					}

					return;
				}

				Address a = services.getAddress();

				timerCancel(galCommandTimer);
				InstallationStatus installingDevice = getInstallingDevice(a);
				if (installingDevice == null) {
					LOG.error(getIeeeAddressHex(a) + ": unsolicited serviceDiscovered()");
					terminateDeviceDiscovery(installingDevice);
					handleNextDiscoveredNode();
					return;
				}

				if (enableDiscoveryLogs)
					LOG.debug(getIeeeAddressHex(installingDevice.getAddress()) + ": discovered " + services.getActiveEndpoints().size() + " endpoint(s)");

				if ((services.getActiveEndpoints().size() > 1) && (!handleMultipleEps)) {
					LOG.warn("sorry but currently (in this version) we handle only the first one!");
				}

				installingDevice.setNodeServices(services);
				installingDevice.setStatus(InstallationStatus.ACTIVE_ENDPOINTS_RETRIEVED);
				installingDevice.resetRetryCounter();

				List activeEndpoints = services.getActiveEndpoints();

				for (int i = 0; i < activeEndpoints.size(); i++) {
					installingDevice.setCurrentService(i);
					ActiveEndpoints ep = (ActiveEndpoints) activeEndpoints.get(i);

					try {
						installingDevice.setStatus(InstallationStatus.WAITING_FOR_SERVICE_DESCRIPTOR);
						if (enableDiscoveryLogs)
							LOG.debug(getIeeeAddressHex(a) + ": getting Service Descriptor for EP " + ep.getEndPoint());
						gateway.getServiceDescriptor(timeout, a, ep.getEndPoint());
						timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
					} catch (Exception e) {
						terminateDeviceDiscovery(installingDevice);
						handleNextDiscoveredNode();
					}
					break;
				}
			}
		});

	}

	public void serviceDescriptorRetrieved(final Status status, final ServiceDescriptor service) {

		executor.execute(new Runnable() {
			public void run() {

				timerCancel(galCommandTimer);
				Address a = service.getAddress();
				String ieeeAddress = getIeeeAddressHex(a);
				InstallationStatus installingDevice = getInstallingDevice(a);
				if (installingDevice == null) {
					Exception st = new Exception();
					LOG.error("installingDevice was null, printing stacktrace",st);
				}

				if ((status.getCode() != GatewayConstants.SUCCESS) || (installingDevice == null)) {
					// in case of failure services is null and there is no
					// way to
					// retrieve the Address so try to guess it.
					installingDevice = getInstallingDevice(InstallationStatus.WAITING_FOR_SERVICE_DESCRIPTOR);
					if (installingDevice != null) {
						a = installingDevice.getAddress();
						LOG.error(getIeeeAddressHex(a) + ": serviceDescriptorRetrieved callback returned error code " + status.getCode() + "'. Guessed address '" + getIeeeAddressHex(a));

						// retries until retry counter goes to 0
						if (installingDevice.getRetryCounter() > 0) {
							try {
								int i = installingDevice.getCurrentService();
								if (i >= 0) {
									NodeServices services = installingDevice.getNodeServices();
									ActiveEndpoints ep = (ActiveEndpoints) services.getActiveEndpoints().get(i);
									if (enableDiscoveryLogs)
										LOG.debug(getIeeeAddressHex(a) + ": getting Service Descriptor for EP " + ep.getEndPoint());
									gateway.getServiceDescriptor(timeout, a, ep.getEndPoint());
									timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
									return;
								} else {
									LOG.error(getIeeeAddressHex(a) + ": wrong ep index stored into InstallationStatus. Abort installation of this node");
								}
							} catch (Exception e) {
								LOG.error(getIeeeAddressHex(a) + ": exception in startServiceDiscovery(). Abort installation of this node", e);
							}
						} else {
							int i = installingDevice.getCurrentService();
							if (i >= 0) {
								NodeServices services = installingDevice.getNodeServices();
								ActiveEndpoints ep = (ActiveEndpoints) services.getActiveEndpoints().get(i);
								LOG.error(getIeeeAddressHex(a) + ": too many retries for serviceDescriptor for ep " + ep.getEndPoint() + ". Abort installation of this node");
							}
						}

						terminateDeviceDiscovery(installingDevice);
						handleNextDiscoveredNode();
						return;
					} else {
						LOG.error("unable to find an associated installation status: unsolicited serviceDescriptorRetrieved()");
					}
					return;
				}

				installingDevice.addServiceDescriptor(service.getEndPoint(), service);

				if (handleMultipleEps) {
					int retrievedServiceIndex = installingDevice.getCurrentService();
					installingDevice.resetRetryCounter();
					List activeEndpoints = installingDevice.getNodeServices().getActiveEndpoints();
					retrievedServiceIndex++;
					if (retrievedServiceIndex < activeEndpoints.size()) {
						try {
							installingDevice.setCurrentService(retrievedServiceIndex);
							installingDevice.setStatus(InstallationStatus.WAITING_FOR_SERVICE_DESCRIPTOR);
							ActiveEndpoints ep = (ActiveEndpoints) activeEndpoints.get(retrievedServiceIndex);
							if (enableDiscoveryLogs)
								LOG.debug(getIeeeAddressHex(a) + ": getting Service Descriptor for EP " + ep.getEndPoint());
							gateway.getServiceDescriptor(timeout, a, ep.getEndPoint());
							timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
							return;
						} catch (Exception e) {
							terminateDeviceDiscovery(installingDevice);
							handleNextDiscoveredNode();
						}
					}
				}

				installingDevice.setStatus(InstallationStatus.INSTALLED);
				try {
					finalizeNode(installingDevice);
				} catch (Exception e) {
					LOG.error("exception", e);
					if (cacheDiscoveryInfos) {
						// dump to file the currently discovered devices
						// descriptors
						updateDiscoveredDevicesDb(ieeeAddress, installingDevice);
					}
					terminateDeviceDiscovery(installingDevice);
					handleNextDiscoveredNode();
					return;
				}

				if (cacheDiscoveryInfos) {
					// dump to file the currently discovered devices
					// descriptors
					updateDiscoveredDevicesDb(ieeeAddress, installingDevice);
				}

				terminateDeviceDiscovery(installingDevice);
				handleNextDiscoveredNode();

			}
		});
	}

	/**
	 * Stores the newly discovered infos into a persistent storage
	 * 
	 * @param ieeeAddress
	 * @param installingDevice
	 */
	private void updateDiscoveredDevicesDb(String ieeeAddress, InstallationStatus installingDevice) {
		synchronized (installedDevices) {
			InstallationStatus installationStatus = (InstallationStatus) installedDevices.get(ieeeAddress);
			if (installationStatus == null) {
				installedDevices.put(ieeeAddress, installingDevice);
				if (dumpDiscoveryInfos) {
					// dump on filesystem only the sleeping end devices.
					dumpDiscoveredDevicesDb(dumpAllDevices);
				}
			}
		}
	}

	private void finalizeNode(InstallationStatus installingDevice) {

		NodeServices nodeServices = installingDevice.getNodeServices();
		List activeEndpoints = nodeServices.getActiveEndpoints();
		Address a = nodeServices.getAddress();
		String nodePid = getNodePid(a);
		String[] endPoints = new String[activeEndpoints.size()];
		for (int i = 0; i < activeEndpoints.size(); i++) {
			ActiveEndpoints ep = (ActiveEndpoints) activeEndpoints.get(i);
			try {
				ServiceDescriptor service = installingDevice.getServiceDescriptor(ep.getEndPoint());
				endPoints[i] = service.getSimpleDescriptor().getApplicationProfileIdentifier() + "." + service.getSimpleDescriptor().getApplicationDeviceIdentifier() + "." + new Short(service.getEndPoint());
				ZigBeeDevice device = createDevice(installingDevice, service, endPoints);
				if (device != null) {
					// add the device to our db
					Vector devices = this.getDevices(nodePid);
					if (devices == null) {
						devices = new Vector();
						this.ieee2devices.put(nodePid, devices);
					}
					devices.add(device);
				}
			} catch (Exception e) {
				LOG.error(getIeeeAddressHex(installingDevice.getAddress()) + ": Error creating device for ep " + ep.getEndPoint() + ": skip it!");
				continue;

			}
		}
	}

	private void handleNextDiscoveredNode() {
		synchronized (inProcessNode) {

			if (inProcessNode.size() > 0) {
				// still discovering node properties
				return;
			}
		}
		InstallationStatus is = null;
		synchronized (discoveredNodesQueue) {

			if (discoveredNodesQueue.size() > 0) {
				is = (InstallationStatus) discoveredNodesQueue.poll();
				synchronized (inProcessNode) {
					inProcessNode.offer(is);
				}
				startNodeDiscoveryProcess(is);
			} else {
				if (enableDiscoveryLogs)
					LOG.debug("installation queue is empty");
			}
		}

	}

	private Vector getDevices(String nodePid) {
		return (Vector) this.ieee2devices.get(nodePid);
	}

	protected Collection getNodes() {
		return ieee2devices.values();
	}

	/**
	 * Creates a new service representing the newly detected ZigBee device. This
	 * OSGi Device follows the Device Admin sepcification.
	 * 
	 * @param node
	 * @param ep
	 * @return
	 */

	private ZigBeeDevice createDevice(InstallationStatus installingDevice, ServiceDescriptor service, String[] endPoints) {

		Hashtable deviceProps = new Hashtable();

		NodeDescriptor node = installingDevice.getNodeDescriptor();
		int deviceId = service.getSimpleDescriptor().getApplicationDeviceIdentifier().intValue();
		int profileId = service.getSimpleDescriptor().getApplicationProfileIdentifier().intValue();

		String ieeeAddr = getIeeeAddress(service.getAddress());

		if (enableDiscoveryLogs) {
			LOG.debug("new node detected ieeeAddr = '" + ieeeAddr + "', ");
			LOG.debug("profileId = '" + Integer.toString(profileId) + "', ");
			LOG.debug("deviceId = '" + Integer.toString(deviceId) + "', ");
		}

		if (node == null) {
			LOG.error("here node should not be null");
		}

		if (enableDiscoveryLogs)
			LOG.debug("manufacturerCode = '" + node.getManufacturerCode() + "', ");

		deviceProps.put(org.osgi.service.device.Constants.DEVICE_CATEGORY, "ZigBee");
		deviceProps.put(org.osgi.service.device.Constants.DEVICE_SERIAL, ieeeAddr);
		deviceProps.put(org.osgi.framework.Constants.SERVICE_PID, ieeeAddr + "-" + new Short(service.getEndPoint()));

		deviceProps.put("zigbee.device.ep.id", new Short(service.getEndPoint()));
		deviceProps.put("zigbee.device.profile.id", new Integer(profileId));
		deviceProps.put("zigbee.device.device.id", new Integer(deviceId));
		deviceProps.put("zigbee.device.eps", endPoints);
		deviceProps.put("zigbee.device.eps.number", new Integer(endPoints.length));

		deviceProps.put("zigbee.device.manufacturer.id", node.getManufacturerCode());
		ZigBeeDeviceImpl device = new ZigBeeDeviceImpl(this, timer, installingDevice.getNodeServices(), node, service, deviceProps);

		// this registration starts the driver location process!
		String InfoStr = "";
		InfoStr = "Creating Device: " + ieeeAddr + "\n\r";
		for (Object key : deviceProps.keySet()) {
			InfoStr += ((String) key + ":" + deviceProps.get(key) + "\n\r");
		}
		LOG.info(InfoStr);
		ServiceRegistration deviceServiceReg = ctxt.getBundleContext().registerService(ZigBeeDevice.class.getName(), device, deviceProps);

		Vector deviceRegs = null;
		String nodePid = getNodePid(service.getAddress());
		synchronized (ieee2sr) {
			deviceRegs = (Vector) ieee2sr.get(nodePid);
			if (deviceRegs == null) {
				deviceRegs = new Vector();
				ieee2sr.put(nodePid, deviceRegs);
			}
		}
		deviceRegs.add(deviceServiceReg);
		return device;
	}

	private boolean add(ZigBeeDevice hacDevice) {
		zigbeeDevices.add(hacDevice);
		return true;
	}

	protected boolean post(ZigBeeDevice device, short profileId, short clusterId, IZclFrame zclFrame) {
		if (gateway == null) {
			LOG.error("post(): jgal not bound");
			return false;
		}

		APSMessage msg = new APSMessage();

		ServiceDescriptor service = device.getServiceDescriptor();

		// patch to clean the network address, otherwise the GAL uses it,
		// instead of ieee address
		// TODO: move this patch when the service descriptor is assigned to
		// the
		// device.
		Address a = service.getAddress();
		a.setNetworkAddress(null);

		if (enableRxTxLogs)
			LOG.debug(getIeeeAddressHex(a) + ": sending message");

		msg.setDestinationAddressMode(new Long(GatewayConstants.EXTENDED_ADDRESS_MODE));
		msg.setDestinationAddress(service.getAddress());
		msg.setDestinationEndpoint(service.getEndPoint());
		msg.setSourceEndpoint(localEndpoint);

		msg.setClusterID(clusterId & 0xffff);
		msg.setProfileID(new Integer(profileId & 0xffff));
		msg.setData(zclFrame.getData());

		TxOptions tx = new TxOptions();
		tx.setAcknowledged(isSleepyEndDevice(msg.getDestinationAddress().getIeeeAddress()));
		tx.setPermitFragmentation(false);
		tx.setSecurityEnabled(false);
		tx.setUseNetworkKey(true);
		msg.setTxOptions(tx);
		msg.setRadius((short) 10);

		try {
			gateway.sendAPSMessage(msg);
		} catch (IOException e) {
			LOG.error("IOException, message not sent :" + e.getMessage());
			return false;
		} catch (GatewayException e) {
			LOG.error("GatewayException, message not sent :" + e.getMessage());
			return false;
		} catch (Exception e) {
			LOG.error("Exception, message not sent :" + e.getMessage(), e);
			return false;
		}

		return true;
	}

	protected boolean post(APSMessageEvent srcMsgEvent, IZclFrame zclFrame) {
		if (gateway == null) {
			LOG.error("post(): jgal not bound");
			return false;
		}

		APSMessage msg = new APSMessage();

		msg.setDestinationAddressMode(new Long(GatewayConstants.EXTENDED_ADDRESS_MODE));
		msg.setDestinationAddress(srcMsgEvent.getSourceAddress());
		msg.setDestinationEndpoint(srcMsgEvent.getSourceEndpoint());
		msg.setSourceEndpoint(localEndpoint);

		msg.setClusterID(srcMsgEvent.getClusterID() & 0xffff);
		msg.setProfileID(srcMsgEvent.getProfileID());
		msg.setData(zclFrame.getData());

		TxOptions tx = new TxOptions();
		tx.setAcknowledged(isSleepyEndDevice(msg.getDestinationAddress().getIeeeAddress()));
		tx.setPermitFragmentation(false);
		tx.setSecurityEnabled(false);
		tx.setUseNetworkKey(true);
		msg.setTxOptions(tx);
		msg.setRadius((short) 10);

		try {
			gateway.sendAPSMessage(msg);
		} catch (IOException e) {
			LOG.error("IOException, message not sent :" + e.getMessage());
			return false;
		} catch (GatewayException e) {
			LOG.error("GatewayException, message not sent :" + e.getMessage());
			return false;
		} catch (Exception e) {
			LOG.error("Exception, message not sent :" + e.getMessage(), e);
			return false;
		}

		LOG.debug("Thread " + Thread.currentThread().getId() + ": message sent");
		return true;
	}

	public void noDriverFound(ZigBeeDevice device) {
		String InfoStr = "";
		InfoStr = "no driver found for device:" + device.getIeeeAddress() + "\n\r";
		for (Object key : ((ZigBeeDeviceImpl) device).getProps().keySet()) {
			InfoStr += ((String) key + ":" + ((ZigBeeDeviceImpl) device).getProps().get(key) + "\n\r");
		}
		LOG.error(InfoStr);
	}

	public void attach(ZigBeeDevice device) {
		// a driver has been attached to the device.
		try {
			String InfoStr = "Created device:" + device.getIeeeAddress() + "\n\r";
			for (Object key : ((ZigBeeDeviceImpl) device).getProps().keySet()) {
				InfoStr += ((String) key + ":" + ((ZigBeeDeviceImpl) device).getProps().get(key) + "\n\r");
			}
			LOG.info(InfoStr);
			add(device);

		} catch (Exception e) {
			LOG.error("element not present in installing Devices list");
		}
	}

	public synchronized void timer(int event) {
		switch (event) {
		case JGalReconnectTimer:

			boolean galBound = false;

			galBound = bindGal();

			if (!galBound) {
				tryReconnectToJGal(cmProps.getReconnectToJGalDelay());
			}

			break;

		case discoveryTimer:

			if (gateway != null) {
				try {
					LOG.debug("started discovery");
					int discoveryTimeout = ((cmProps.getDiscoveryDelay() - 2) > 10 ? 10 : (cmProps.getDiscoveryDelay() - 2)) * 1000;
					gateway.startNodeDiscovery(discoveryTimeout, GatewayConstants.DISCOVERY_LQI);
				} catch (Exception e) {
					tryReconnectToJGal(cmProps.getReconnectToJGalDelay());
					break;
				}
				if (cmProps.getDiscoveryDelay() > 0)
					timerStart(discoveryTimer, cmProps.getDiscoveryDelay());
			}

			break;

		case permitJoinAllTimer:

			timerCancel(permitJoinAllTimer);
			this.terminateDeviceDiscoveryForJoinedDevices();
			this.postEvent("ah/zigbee/CLOSE_NETWORK", null);

			break;

		case galCommandTimer:

			LOG.warn("galCommandTimer expired");
			// if this timer expires, it means that the GAL was not sending a
			// calback for node descriptor or service discriptor or active
			// endpoints. We need to start to process a new node.

			synchronized (inProcessNode) {
				if (this.inProcessNode.size() == 0) {
					LOG.error("galCommandTimer expired but no nodes are in the inProcessNode queue");
					// TESTME: we start the discovery process on a new node.
					this.handleNextDiscoveredNode();
					break;
				}
			}

			// try to recover
			InstallationStatus installingDevice = null;
			synchronized (inProcessNode) {
				installingDevice = (InstallationStatus) this.inProcessNode.poll();
			}
			LOG.error(getIeeeAddressHex(installingDevice.getAddress()) + ": no response from jgal. Try to recover.");
			Status status = new Status();

			switch (installingDevice.getStatus()) {

			case InstallationStatus.WAITING_FOR_NODE_DESCRIPTOR:
				// Simulates the callback with GatewayConstants.TIMEOUT
				// error
				status.setCode((short) GatewayConstants.TIMEOUT);
				this.nodeDescriptorRetrieved(status, null);
				break;

			case InstallationStatus.WAITING_FOR_SERVICES:
				// Simulates the callback with GatewayConstants.TIMEOUT
				// error
				status.setCode((short) GatewayConstants.TIMEOUT);
				this.servicesDiscovered(status, null);
				break;

			case InstallationStatus.WAITING_FOR_SERVICE_DESCRIPTOR:
				// Simulates the callback with GatewayConstants.TIMEOUT
				// error
				status.setCode((short) GatewayConstants.TIMEOUT);
				this.serviceDescriptorRetrieved(status, null);
				break;

			default:
				LOG.debug("no actions to recover!");
				this.terminateDeviceDiscovery(installingDevice);
				this.handleNextDiscoveredNode();
			}

			break;
		}
	}

	protected void availStateUpdated(ZigBeeDevice device, int availState) {
		if (availState == ZigBeeDeviceImpl.Disconnected) {
			LOG.debug("device " + device.getIeeeAddress() + " is unreachable");
		} else if (availState == ZigBeeDeviceImpl.Connected) {
			LOG.debug("device " + device.getIeeeAddress() + " is now reachable");
		}
	}

	protected ZigBeeDeviceListener getService(ServiceReference driverRef) {
		return (ZigBeeDeviceListener) ctxt.getBundleContext().getService(driverRef);
	}

	protected void setGatewayInterface(GatewayInterface r) {
		synchronized (sLock) {
			gateway = r;
		}
	}

	protected void unsetGatewayInterface(GatewayInterface r) {
		synchronized (sLock) {
			if (r == gateway) {
				gateway = null;
			}
		}
	}

	public void gatewayStartResult(final Status status) {

		executor.execute(new Runnable() {
			public void run() {

				if (status.getCode() == 0) {
					if (LOG != null)
						LOG.info("zigbee network up and running.");

					synchronized (sLock) {
						galRunning = 2;

						if (!getUseNVM())
							setUseNVM(true);

						if (cmProps.isLqiEnabled())
							timerStart(discoveryTimer, cmProps.getInitialDiscoveryDelay());

						// register any cached node
						finalizeNodes();
					}

				} else {
					if (status.getCode() == GatewayConstants.NETWORK_FAILURE) {
						synchronized (sLock) {
							LOG.error("ZigBeeGateway started with status code: NETWORK_FAILURE");
						}
					} else {
						LOG.info("ZigBeeGateway started with status code " + status.getCode());
					}
				}
			}
		});
	}

	public void dongleResetResult(final Status status) {

		MyThread thr = new MyThread(this) {

			public void run() {
				Object parameter = this.getParameter();
				if (status.getCode() == GatewayConstants.SUCCESS) {
					synchronized (sLock) {
						try {
							if (gateway == null) {
								LOG.warn("dongleResetResult(): gateway is null");
								return;
							}
							// configure local endpoint
							try {
								gateway.clearEndpoint(localEndpoint);
							} catch (Exception e) {
								LOG.error("exception in clearEndpoint of endpoint " + localEndpoint + " " + e.getMessage());
							}
							// TODO the following input clusters have to be
							// configurable
							// from Config Admin or props file
							// FIXME: this simple descriptor MUST be set to null
							// when
							// the gal is detached.
							sd = new SimpleDescriptor();
							sd.setEndPoint(new Short(localEndpoint));
							sd.setApplicationDeviceVersion(new Short((short) 0x01));
							sd.setApplicationDeviceIdentifier(new Integer(0x0050)); // ESP
							sd.setApplicationProfileIdentifier(new Integer(0x0104)); // HA
							List outputClusters = sd.getApplicationOutputCluster();
							List inputClusters = sd.getApplicationInputCluster();

							// TODO NB: the following input clusters have to be
							// configurable from Config Admin or props file

							outputClusters.add(new Integer(ZclSimpleMeteringClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclMeterIdentificationClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclPowerProfileClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclApplianceStatisticsClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclApplianceControlClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclApplianceIdentificationClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclApplianceEventsAndAlertsClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclOnOffClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclPowerConfigurationClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclRelativeHumidityMeasurementClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclDoorLockClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclAirQualityClient.CLUSTER_ID));
							outputClusters.add(new Integer(ZclWindowCoveringClient.CLUSTER_ID));

							if (enableEnergyAtHomeClusters) {
								// This is the list of Client side clusters
								// supported by E@H
								outputClusters.add(new Integer(ZclBasicClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclIdentifyClient.CLUSTER_ID));
							}

							if (enableAllClusters) {
								outputClusters.add(new Integer(ZclOccupancySensingClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclIASZoneClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclTemperatureMeasurementClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclIlluminanceMeasurementClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclRelativeHumidityMeasurementClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclLightLinkColorControlClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclLevelControlClient.CLUSTER_ID));
								outputClusters.add(new Integer(ZclThermostatClient.CLUSTER_ID));
							}

							// This is the list of Server side clusters
							// supported by E@H
							inputClusters.add(new Integer(ZclBasicServer.CLUSTER_ID));
							inputClusters.add(new Integer(ZclIdentifyServer.CLUSTER_ID));
							inputClusters.add(new Integer(ZclTimeServer.CLUSTER_ID));

							if (enableEnergyAtHomeClusters) {
								inputClusters.add(new Integer(ZclSimpleMeteringServer.CLUSTER_ID));
								inputClusters.add(new Integer(ZclPartitionServer.CLUSTER_ID));
							}
							if (enableAllClusters) {
								inputClusters.add(new Integer(ZclOnOffServer.CLUSTER_ID));
							}

							localEndpoint = gateway.configureEndpoint(10000, sd);
							// start discovery announcement
							gateway.startNodeDiscovery(0, GatewayConstants.DISCOVERY_ANNOUNCEMENTS);
							// subscribe liveness
							gateway.subscribeNodeRemoval(0, GatewayConstants.DISCOVERY_FRESHNESS | GatewayConstants.DISCOVERY_LEAVE);
							// register local callback
							callbackId = gateway.createAPSCallback(localEndpoint, (ZigBeeManagerImpl) parameter);
							if (callbackId == -1) {
								LOG.error("createAPSCallback returned -1");
							}

							// start gateway device
							gateway.startGatewayDevice(0);
						} catch (Exception e) {
							LOG.error("Exception in dongleResetResult",e);
						}
					}

				} else {
					LOG.error("dongleResetResult returned error code " + status.getCode());
				}
			}
		};

		new Thread(thr).start();
	}

	public void leaveResult(Status status) {

	}

	public void permitJoinResult(final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				synchronized (sLock) {
					LOG.debug("t" + 5);
					LOG.debug("permit join returned status " + status.getCode());
					if (status.getCode() == 0) {
						postEvent("ah/zigbee/OPEN_NETWORK", null);
					}
				}
			}
		});
	}

	private void tryReconnectToJGal(int delay) {
		timerStart(JGalReconnectTimer, delay);
		if (LOG != null)
			LOG.info("retry to reconnect to ZigbeeGatewayDevice in " + delay + " s");
	}

	private boolean bindGal() {
		if ((this.gateway == null) || (this.timer == null)) {
			return false;
		}

		String testevent = this.ctxt.getBundleContext().getProperty("org.energy_home.jemma.ah.zigbee.zcl.testevent");

		try {
			if (Boolean.parseBoolean(testevent)) {
				// enables only those clusters meaningful for the HA 1.2
				// Testevent
				this.enableAllClusters = false;
				this.enableEnergyAtHomeClusters = false;
			} else {
				// enables all clusters
				this.enableAllClusters = true;
				this.enableEnergyAtHomeClusters = true;
			}
		} finally {
			// do nothing
		}

		this.terminateDeviceDiscoveryAll();
		try {
			gateway.setGatewayEventListener(this);
		} catch (Exception e1) {
			LOG.error("Error setting myself as Gateway event listener",e1);
			// FIXME: what I have to do here?
		}

		try {
			if (getUseNVM()) {
				// use the non volatile memory ram
				LOG.info("starting zigbee gateway. Use NVRAM");
				gateway.resetDongle(0, GatewayConstants.RESET_USE_NVMEMORY);
			} else {
				LOG.info("starting zigbee gateway. Don't use NVRAM");
				gateway.resetDongle(0, GatewayConstants.RESET_COMMISSIONING_ASSOCIATION);
			}
		} catch (IOException e) {
			LOG.error("exception when starting zigbee gateway: " + e.getMessage());
			return false;
		} catch (GatewayException e) {
			LOG.error("exception when starting zigbee gateway: " + e.getMessage());
			return false;
		} catch (Exception e) {
			LOG.error("exception when starting zigbee gateway: " + e.getMessage());
			return false;
		}

		galRunning = 1;

		// connected!
		return true;
	}

	private boolean unbindGal() {
		if (enableDsLogs)
			LOG.debug("unbindGal");

		this.terminateDeviceDiscoveryAll();

		try {
			gateway.setGatewayEventListener(null);
			gateway.startNodeDiscovery(0, GatewayConstants.DISCOVERY_STOP);
		} catch (Exception e) {
			LOG.error("exception while calling startNodeDiscovery() or stopping node discovery: " + e.getMessage());
		}
		try {
			if (callbackId != -1)
				gateway.deleteCallback(this.callbackId);
			callbackId = -1;
		} catch (Exception e) {
			LOG.error("Exception", e);
		}

		unregisterAllDevices();

		galRunning = 0;
		return true;
	}

	private void unregisterAllDevices() {
		Set keys = ieee2sr.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			try {
				String nodePid = (String) iterator.next();
				Vector deviceRegs = (Vector) this.ieee2sr.get(nodePid);
				if (deviceRegs != null) {
					this.ieee2devices.remove(nodePid);
					for (Iterator iterator2 = deviceRegs.iterator(); iterator2.hasNext();) {
						ServiceRegistration deviceReg = (ServiceRegistration) iterator2.next();
						deviceReg.unregister();
					}
				}
			} catch (ConcurrentModificationException e) {
				LOG.error("Error unregistering a device service because of concurrent modification",e);
				return;
			}
		}
		ieee2sr.clear();
	}

	public void nodeDescriptorRetrieved(final Status status, final NodeDescriptor node) {

		executor.execute(new Runnable() {
			public void run() {

				// guess the installing device because the node descriptor
				// doesn't
				// contain the address of the device

				timerCancel(galCommandTimer);
				if (gateway == null) {
					LOG.warn("in nodeDescriptorRetrieved() detected that gateway has been removed");
					return;
				}

				InstallationStatus installingDevice = getInstallingDevice(InstallationStatus.WAITING_FOR_NODE_DESCRIPTOR);
				if (installingDevice == null) {
					LOG.warn("received a node descriptor from an unsolicited node");
					handleNextDiscoveredNode();
					return;
				}

				String nodePid = getNodePid(installingDevice.getAddress());
				String nodeIeeeAddressHex = getIeeeAddressHex(installingDevice.getAddress());

				if (status.getCode() != 0) {
					LOG.error(nodeIeeeAddressHex + ": nodeDescriptorRetrieved callback returned error code " + status.getCode() + " Message:" + status.getMessage() + " '. Guessed pid '" + nodePid);
					if (installingDevice.getRetryCounter() > 0) {
						try {
							gateway.getNodeDescriptor(timeout, installingDevice.getAddress());
							LOG.debug(nodeIeeeAddressHex + ": called getNodeDescriptor()");
							timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
							return;
						} catch (Exception e) {
							LOG.error("Exception", e);
						}
					}

					// abort installation of this node
					LOG.error(nodeIeeeAddressHex + ": too many retries for getting node descriptor");
					terminateDeviceDiscovery(installingDevice);
					handleNextDiscoveredNode();
					return;
				}

				if (enableDiscoveryLogs)
					LOG.debug(nodeIeeeAddressHex + ": retrieved node descriptor");

				// update the state
				installingDevice.setStatus(InstallationStatus.WAITING_FOR_SERVICES);
				installingDevice.resetRetryCounter();
				installingDevice.setNodeDescriptor(node);

				try {
					if (enableDiscoveryLogs)
						LOG.debug(nodeIeeeAddressHex + ": startServiceDiscovery()");
					gateway.startServiceDiscovery(timeout, installingDevice.getAddress());
					timerStart(galCommandTimer, (int) (timeout / 1000) + timeoutOffset);
				} catch (Exception e) {
					terminateDeviceDiscovery(installingDevice);
					handleNextDiscoveredNode();
				}

			}
		});
	}

	protected Dictionary getConfiguration() {
		Dictionary config = new Hashtable();
		config.put("org.energy_home.jemma.ah.adapter.zigbee.lqi", cmProps.isLqiEnabled() + "");
		config.put("org.energy_home.jemma.ah.adapter.zigbee.reconnect", cmProps.getReconnectToJGalDelay() + "");
		config.put("org.energy_home.jemma.ah.adapter.zigbee.discovery.delay", cmProps.getDiscoveryDelay() + "");
		config.put("org.energy_home.jemma.ah.adapter.zigbee.discovery.initialdelay", cmProps.getInitialDiscoveryDelay() + "");
		return config;
	}

	public void nodeRemoved(final Status status, final WSNNode node) {

		executor.execute(new Runnable() {
			public void run() {

				// notifies the node
				// TODO: if the node has short address 0 it means that the
				// dongle is
				// crashed.
				if (status.getCode() == 0) {
					String nodePid = getNodePid(node.getAddress());

					synchronized (devicesInstalled) {
						devicesInstalled.remove(node.getAddress().getIeeeAddress());
					}
					Vector deviceRegs = (Vector) ieee2sr.get(nodePid);
					if (deviceRegs != null) {
						LOG.debug(getIeeeAddressHex(node.getAddress()) + ": node has been removed");
						ieee2sr.remove(nodePid);
						ieee2devices.remove(nodePid);
						for (Iterator iterator = deviceRegs.iterator(); iterator.hasNext();) {
							ServiceRegistration deviceReg = (ServiceRegistration) iterator.next();
							deviceReg.unregister();
						}
					} else {
						LOG.warn(nodePid + ": unknown node has been removed");
					}

					if (cacheDiscoveryInfos) {
						synchronized (installedDevices) {

							installedDevices.remove(nodePid);

						}
						if (dumpDiscoveryInfos) {
							dumpDiscoveredDevicesDb(dumpAllDevices);
						}
					}
				}

			}
		});
	}

	protected boolean isGalRunning() {
		return this.galRunning == 2;
	}

	public synchronized void permitJoin(short duration) throws Exception {
		try {
			if (gateway == null) {
				throw new Exception("zgd not started");
			}

			gateway.permitJoinAll(timeout, duration);
			if (duration == 0) {
				this.postEvent("ah/zigbee/CLOSE_NETWORK", null);
				this.terminateDeviceDiscoveryForJoinedDevices();
				this.handleNextDiscoveredNode();
			} else {

				timerCancel(permitJoinAllTimer);
				this.postEvent("ah/zigbee/OPEN_NETWORK", null);
				timerStart(permitJoinAllTimer, duration);
			}
		} catch (Exception e) {
			LOG.error("Exception in PermitJoin()", e);
			throw e;
		}

	}

	/**
	 * Stops the discovery process for all those devices that were not in the
	 * network
	 */

	private void terminateDeviceDiscoveryAll() {

		this.timerCancel(galCommandTimer);

		// send leave to any new node under processing
		synchronized (inProcessNode) {
			for (Iterator iterator = this.inProcessNode.iterator(); iterator.hasNext();) {
				InstallationStatus installationStatus = (InstallationStatus) iterator.next();
				// FIXME: Devo farlo per tutti?
				this.terminateDeviceDiscovery(installationStatus);
			}
		}

		// the following device are not under processing
		synchronized (discoveredNodesQueue) {
			for (Iterator iterator = this.discoveredNodesQueue.iterator(); iterator.hasNext();) {
				InstallationStatus installationStatus = (InstallationStatus) iterator.next();
				this.terminateDeviceDiscovery(installationStatus);
			}
		}
		synchronized (inProcessNode) {
			if (inProcessNode.size() > 0) {
				LOG.error("inProcessNode is not empty!");
			}
			inProcessNode.clear();
		}
		synchronized (discoveredNodesQueue) {
			if (discoveredNodesQueue.size() > 0) {
				LOG.error("discoveredNodesQueue is not empty!");
			}
			discoveredNodesQueue.clear();
		}
		synchronized (devicesUnderInstallation) {

			if (devicesUnderInstallation.size() > 0) {
				LOG.error("devicesUnderInstallation is not empty!");
			}
			devicesUnderInstallation.clear();
		}

	}

	// remove from our list (and send a leave) only those devices just entered
	// because the network has been opened
	private void terminateDeviceDiscoveryForJoinedDevices() {
		// send leave to any new node under processing
		synchronized (inProcessNode) {

			for (Iterator iterator = inProcessNode.iterator(); iterator.hasNext();) {
				InstallationStatus installationStatus = (InstallationStatus) iterator.next();
				if (this.hasJoined(installationStatus)) {
					this.timerCancel(galCommandTimer);
					this.terminateDeviceDiscovery(installationStatus);
				}
			}
		}
		// the following device are not under processing.
		synchronized (discoveredNodesQueue) {
			for (Iterator iterator = discoveredNodesQueue.iterator(); iterator.hasNext();) {
				InstallationStatus installationStatus = (InstallationStatus) iterator.next();

				if (this.hasJoined(installationStatus)) {
					this.terminateDeviceDiscovery(installationStatus);
				}
			}
		}
	}

	/**
	 * Given an InstallationStatus remove the device from any queue.
	 * 
	 * @param installationStatus
	 */
	private void terminateDeviceDiscovery(InstallationStatus installationStatus) {
		String nodePid = getNodePid(installationStatus.getAddress());
		if (hasJoined(installationStatus)) {
			try {
				LOG.debug("in terminateDeviceDiscovery() sending leave to node " + getIeeeAddressHex(installationStatus.getAddress()));
				gateway.leave(4000, installationStatus.getAddress());
			} catch (Exception e) {
				LOG.error("Exception", e);
			}
		}
		synchronized (devicesUnderInstallation) {
			synchronized (devicesInstalled) {
				devicesInstalled.put(installationStatus.getAddress().getIeeeAddress(), this.devicesUnderInstallation.get(nodePid));
			}

			this.devicesUnderInstallation.remove(nodePid);
		}
		synchronized (discoveredNodesQueue) {
			discoveredNodesQueue.remove(installationStatus);
		}
		synchronized (inProcessNode) {
			inProcessNode.remove(installationStatus);
		}

	}

	private boolean hasJoined(InstallationStatus installationStatus) {
		return false;
	}

	private synchronized InstallationStatus addInstallingDevice(Address a) {

		String nodePid = getNodePid(a);
		InstallationStatus installationStatus = new InstallationStatus(a);
		synchronized (devicesUnderInstallation) {

			this.devicesUnderInstallation.put(nodePid, installationStatus);
		}
		return installationStatus;

	}

	private InstallationStatus getInstallingDevice(Address a) {
		synchronized (sLock) {
			String nodePid = getNodePid(a);
			return (InstallationStatus) this.devicesUnderInstallation.get(nodePid);
		}
	}

	private InstallationStatus getInstallingDevice(int deviceStatus) {
		// MARCO-->E' qui che succede il problema del lock, questa funzione
		// restituisce null e nessuno testa il fatto che il risultato sia null
		synchronized (devicesUnderInstallation) {
			Enumeration keys = this.devicesUnderInstallation.keys();
			while (keys.hasMoreElements()) {
				String nodePid = (String) keys.nextElement();

				InstallationStatus installationStatus = (InstallationStatus) this.devicesUnderInstallation.get(nodePid);
				if (installationStatus.getStatus() == deviceStatus) {
					// this.devicesUnderInstallation.remove(nodeIeeeAddress);
					// LOG.debug("error in getting EPs, removing node '" +
					// nodeIeeeAddress + "' from installing devices");
					return installationStatus;
				}
			}
			return null;
		}
	}

	/**
	 * The following method is called when a broadcast message arrives. It
	 * handles the IdentifyQuery message. FIXME: this must not be here but the
	 * IdentifyQuery must be handled by the driver.
	 * 
	 * @param msg
	 */

	private void handleBroadcastMessages(APSMessageEvent msg) {

		ZclFrame zclFrame = new ZclFrame(msg.getData());

		int commandId = zclFrame.getCommandId();
		if (zclFrame.isServerToClient()) {
			LOG.error("invalid direction field in broadcast message");
			return;
		}

		IZclFrame zclResponseFrame = null;

		switch (commandId) {
		case 1:
			IdentifyQueryResponse r = new IdentifyQueryResponse(0xFFFF);
			int size;
			try {
				size = ZclIdentifyQueryResponse.zclSize(r);
				zclResponseFrame = zclFrame.createResponseFrame(size);
				zclResponseFrame.setCommandId(0);
				ZclIdentifyQueryResponse.zclSerialize(zclResponseFrame, r);
				LOG.debug("Sending Identify Query response");
			} catch (ZclValidationException e) {
				LOG.error("Exception", e);
			}

			break;
		}

		if (zclResponseFrame != null) {
			APSMessage responseMsg = new APSMessage();

			if (LOG.isDebugEnabled())
				LOG.debug("Gateway" + ": Sync T > 0x" + Hex.toHexString(msg.getClusterID() & 0xffff, 2) + "(clusterId) " + zclResponseFrame.toString());

			LOG.debug("sending message to node " + getIeeeAddressHex(msg.getSourceAddress()));

			responseMsg.setDestinationAddressMode(msg.getSourceAddressMode());
			responseMsg.setDestinationAddress(msg.getSourceAddress());
			responseMsg.setDestinationEndpoint(msg.getSourceEndpoint());
			responseMsg.setSourceEndpoint(localEndpoint);

			responseMsg.setClusterID(msg.getClusterID() & 0xffff);
			responseMsg.setProfileID(msg.getProfileID() & 0xffff);
			responseMsg.setData(zclResponseFrame.getData());

			TxOptions tx = new TxOptions();
			tx.setAcknowledged(isSleepyEndDevice(msg.getDestinationAddress().getIeeeAddress()));
			tx.setPermitFragmentation(false);
			tx.setSecurityEnabled(false);
			tx.setUseNetworkKey(true);
			responseMsg.setTxOptions(tx);
			responseMsg.setRadius((short) 10);

			try {
				gateway.sendAPSMessage(responseMsg);
			} catch (IOException e) {
				LOG.error("IOException, message not sent :" + e.getMessage());
			} catch (GatewayException e) {
				LOG.error("GatewayException, message not sent :" + e.getMessage());
			} catch (Exception e) {
				LOG.error("Exception, message not sent :" + e.getMessage(), e);
			}
		}
	}

	private void unregisterDevice(String nodePid) {
		Vector deviceRegs = (Vector) this.ieee2sr.get(nodePid);
		if (deviceRegs != null) {
			this.ieee2sr.remove(nodePid);
			this.ieee2devices.remove(nodePid);
			for (Iterator iterator = deviceRegs.iterator(); iterator.hasNext();) {
				ServiceRegistration deviceReg = (ServiceRegistration) iterator.next();
				deviceReg.unregister();
			}
		}

		if (cacheDiscoveryInfos) {
			synchronized (installedDevices) {
				InstallationStatus installationStatus = (InstallationStatus) this.installedDevices.remove(nodePid);

				if (this.enableDiscoveryLogs && installationStatus != null)
					LOG.debug(nodePid + ": removed Node from installedDevices table");

				if (dumpDiscoveryInfos && (installationStatus != null)) {
					this.dumpDiscoveredDevicesDb(dumpAllDevices);
				}
			}
		}
	}

	public synchronized void removeDevice(String nodePid) throws Exception {

		Vector devices = (Vector) ieee2devices.get(nodePid);
		if (devices == null || devices.size() == 0)
			return;
		LOG.debug(nodePid + ": deleting node with ieee address ");
		LOG.debug("in terminateDeviceDiscovery() sending leave to node " + nodePid);
		try {
			gateway.leave(timeout, ((ZigBeeDevice) devices.get(0)).getServiceDescriptor().getAddress());
		} catch (Exception e) {
			LOG.error(nodePid + ": exception in leave(): " + e.getMessage());
		}
		this.unregisterDevice(nodePid);

	}

	private boolean isSleepyEndDevice(BigInteger ieee) {
		if (ieee != null) {
			synchronized (devicesInstalled) {

				InstallationStatus status = (InstallationStatus) devicesInstalled.get(ieee);
				if (status == null)
					return true;
				else {
					if (status.getNodeDescriptor() != null && status.getNodeDescriptor().getMACCapabilityFlag() != null)
						return status.getNodeDescriptor().getMACCapabilityFlag().isReceiverOnWhenIdle() ? true : false;
					else
						return true;
				}
			}
		} else
			return true;
	}

	protected void remove(ZigBeeDevice device) throws Exception {
		ServiceDescriptor service = device.getServiceDescriptor();
		this.removeDevice(getIeeeAddressHex(service.getAddress()));
	}

	public void enableNVM() {
		synchronized (sLock) {
			this.setUseNVM(true);
		}
	}

	public void disableNVM() {
		synchronized (sLock) {
			this.setUseNVM(false);
		}
	}

	public boolean getNVMStatus() {
		synchronized (sLock) {
			return this.getUseNVM();
		}
	}

	private void setUseNVM(boolean useNVM) {
		LOG.debug("setUseNVM to " + useNVM);
		this.useNVMNetworkSetting = useNVM;
		this.saveProperties();
	}

	private boolean getUseNVM() {
		LOG.debug("returned UseNVM: " + this.useNVMNetworkSetting);
		return this.useNVMNetworkSetting;
	}

	private boolean useDataFileDir = false;

	private void loadDiscoveredDevicesDb() {
		List sleepingEndDevices = new ArrayList();

		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(cacheFile));
			sleepingEndDevices = (ArrayList) in.readObject();
			in.close();

			for (Iterator iterator = sleepingEndDevices.iterator(); iterator.hasNext();) {
				InstallationStatus installationStatus = (InstallationStatus) iterator.next();
				Address a = installationStatus.getAddress();
				String ieeeAddress = getIeeeAddressHex(a);
				synchronized (installedDevices) {

					installedDevices.put(ieeeAddress, installationStatus);
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error("cache file not found");
		} catch (Exception e) {
			LOG.error("exeption reading cache dump. Wrong format?", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error("IOException closing cache dump", e);
				}
			}
		}
	}

	/**
	 * Dump to the filesystem the set of devices that have been discovered since
	 * now If the saveAll flag is false, only sleeping end devices are actually
	 * stored.
	 */

	private void dumpDiscoveredDevicesDb(boolean saveAll) {

		if (enableDiscoveryLogs) {
			if (saveAll)
				LOG.debug("Dump persistently ALL discovered devices");
			else
				LOG.debug("Dump persistently SLEEPING discovered devices");
		}

		ObjectOutputStream out = null;
		List devicesToDump = new ArrayList();

		try {
			synchronized (installedDevices) {

				for (Iterator iterator = installedDevices.values().iterator(); iterator.hasNext();) {
					InstallationStatus installationStatus = (InstallationStatus) iterator.next();
					if (installationStatus.getStatus() == InstallationStatus.INSTALLED) {
						if (!installationStatus.getNodeDescriptor().getMACCapabilityFlag().isReceiverOnWhenIdle() || saveAll) {
							devicesToDump.add(installationStatus);
						}
					}
				}
			}
			out = new ObjectOutputStream(new FileOutputStream(cacheFile));
			out.writeObject(devicesToDump);
			out.close();
		} catch (IOException e) {
			LOG.error("IOException writing cache dump", e);
		} catch (Exception e) {
			LOG.error("exception writing cache dump", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOG.error("IOException closing cache dump", e);
				}
			}
		}
	}

	private void finalizeNodes() {
		synchronized (installedDevices) {
			Iterator it = installedDevices.values().iterator();
			while (it.hasNext()) {
				InstallationStatus installationStatus = (InstallationStatus) it.next();
				try {
					finalizeNode(installationStatus);
				} catch (Exception e) {
					LOG.error("exception while finalizing Node " + getIeeeAddress(installationStatus.getAddress()));
				}
			}
		}
	}

	private void loadProperties() {
		if (this.properties != null)
			return;

		this.properties = new Properties();
		File propertiesFile;

		try {
			if (useDataFileDir)
				propertiesFile = this.ctxt.getBundleContext().getDataFile(propertyFilename);
			else {
				URL url = new URL(propertiesFilename);
				propertiesFile = new File(url.getFile());
			}

			this.properties.load(new FileInputStream(propertiesFile));
			this.useNVMNetworkSetting = Boolean.parseBoolean(this.properties.getProperty("usenvm", "false"));
		} catch (Exception e) {
			try {
				this.setUseNVM(false);
			} catch (Exception e1) {
				LOG.error("unable to write back the property file: " + e1.getMessage());
			}
		}
	}

	private boolean saveProperties() {
		if (properties == null)
			properties = new Properties();

		try {
			if (this.properties != null) {
				File propertiesFile;
				if (useDataFileDir) {
					propertiesFile = this.ctxt.getBundleContext().getDataFile(propertyFilename);
				} else {
					URL url = new URL(propertiesFilename);
					propertiesFile = new File(url.getFile());
				}
				this.properties.setProperty("usenvm", Boolean.toString(getUseNVM()));
				properties.store(new FileOutputStream(propertiesFile), null);
			}
		} catch (IOException e) {
			LOG.error("unable to save " + propertiesFilename, e);
			return false;
		}
		return true;
	}

	protected void addToBinding(String nodeIeeeAdddress, short nodeEp, short clusterId) throws IOException, GatewayException, Exception {

		if (this.gateway != null) {
			Vector devices = (Vector) this.getDevices(nodeIeeeAdddress);
			if (devices == null) {
				throw new Exception("node not found");
			}

			BindingList bl = new BindingList();
			Binding b = new Binding();
			bl.getBinding().add(b);
			b.setClusterID(clusterId & 0xffff);
			b.setSourceEndpoint(nodeEp);
			b.setSourceIEEEAddress(new BigInteger(nodeIeeeAdddress, 16));
			Device dev = new Device();
			dev.setAddress(new BigInteger(this.galIeeeAddress, 16));
			dev.setEndpoint(this.localEndpoint);
			b.getDeviceDestination().add(dev);
			this.gateway.addBinding(0, b);
		}
	}

	protected void removeToBinding(String nodeIeeeAdddress, short nodeEp, short clusterId) throws IOException, GatewayException, Exception {

		if (this.gateway != null) {
			Vector devices = (Vector) this.getDevices(nodeIeeeAdddress);
			if (devices == null) {
				throw new Exception("node not found");
			}

			BindingList bl = new BindingList();
			Binding b = new Binding();
			bl.getBinding().add(b);
			b.setClusterID(clusterId & 0xffff);
			b.setSourceEndpoint(nodeEp);
			b.setSourceIEEEAddress(new BigInteger(nodeIeeeAdddress, 16));
			Device dev = new Device();
			dev.setAddress(new BigInteger(this.galIeeeAddress, 16));
			dev.setEndpoint(this.localEndpoint);
			b.getDeviceDestination().add(dev);
			this.gateway.removeBinding(0, b);
		}
	}

	private void postEvent(String topic, Map props) {
		if (this.eventAdmin != null) {
			try {
				this.eventAdmin.postEvent(new Event(topic, props));
			} catch (Exception e) {
				LOG.error("Exception", e);
			}
		}
	}

	private long lastOpenRequestTimestamp = 0;
	private static final short DEFAULT_OPEN_REQUEST_DURATION = 180;

	public boolean isNetworkOpen() {
		return System.currentTimeMillis() - lastOpenRequestTimestamp < DEFAULT_OPEN_REQUEST_DURATION * 1000;
	}

	public void openNetwork() throws Exception {
		openNetwork(DEFAULT_OPEN_REQUEST_DURATION);
		lastOpenRequestTimestamp = System.currentTimeMillis();
	}

	public void openNetwork(int duration) throws Exception {
		permitJoin((short) duration);

		lastOpenRequestTimestamp = System.currentTimeMillis();
	}

	public void closeNetwork() throws Exception {
		permitJoin((short) 0);
		lastOpenRequestTimestamp = 0;
	}

	public void bindingResult(Status status) {
		// TODO Auto-generated method stub

	}

	public void unbindingResult(Status status) {
		// TODO Auto-generated method stub
		LOG.debug("Received unbindingResult()");
	}

	public void nodeBindingsRetrieved(Status status, BindingList bindings) {
		// TODO Auto-generated method stub
		LOG.debug("Received nodeBindingsRetrieved()");
	}

	public boolean isRxTxLogEnabled() {
		return this.enableRxTxLogs;
	}

	public boolean isNotifyFrameLogEnabled() {
		return this.enableNotifyFrameLogs;
	}

	public class MyThread implements Runnable {

		Object parameter = null;

		public MyThread(Object _parameter) {
			parameter = _parameter;
		}

		public void run() {
		}

		public Object getParameter() {
			return parameter;
		}

	}
}
