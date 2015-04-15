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
package org.energy_home.jemma.javagal.layers.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.implementations.ApsMessageManager;
import org.energy_home.jemma.javagal.layers.business.implementations.Discovery_Freshness_ForcePing;
import org.energy_home.jemma.javagal.layers.business.implementations.GatewayEventManager;
import org.energy_home.jemma.javagal.layers.business.implementations.MessageManager;
import org.energy_home.jemma.javagal.layers.business.implementations.ZdoManager;
import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.CallbackEntry;
import org.energy_home.jemma.javagal.layers.object.GatewayDeviceEventEntry;
import org.energy_home.jemma.javagal.layers.object.GatewayStatus;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.MyRunnable;
import org.energy_home.jemma.javagal.layers.object.NeighborTableLis_Record;
import org.energy_home.jemma.javagal.layers.object.ParserLocker;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.MessageListener;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;
import org.energy_home.jemma.zgd.jaxb.LQINode;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.Neighbor;
import org.energy_home.jemma.zgd.jaxb.NeighborList;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;
import org.energy_home.jemma.zgd.jaxb.NodeServicesList;
import org.energy_home.jemma.zgd.jaxb.RPCProtocol;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Version;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JavaGal Controller. Only one instance of this object can exists at a time.
 * All clients can access this instance via their dedicated proxies (see
 * {@link org.energy_home.jemma.zgd.GalExtenderProxy}).
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */

public class GalController {
	private ExecutorService executor = null;
	private GatewayStatus _gatewayStatus = GatewayStatus.GW_READY_TO_START;
	private Long CallbackIdentifier = (long) 1;
	private List<WrapperWSNNode> NetworkCache = Collections.synchronizedList(new LinkedList<WrapperWSNNode>());
	private List<CallbackEntry> listCallback = Collections.synchronizedList(new LinkedList<CallbackEntry>());
	private List<GatewayDeviceEventEntry> listGatewayEventListener = Collections.synchronizedList(new LinkedList<GatewayDeviceEventEntry>());
	// FIXME mass-rename logger to LOG when ready
	private static final Logger LOG = LoggerFactory.getLogger(GalController.class);
	private ApsMessageManager apsManager = null;
	private SimpleDescriptor lastEndPoint = null;
	private StartupAttributeInfo lastSai = null;
	private MessageManager messageManager = null;

	private ZdoManager zdoManager = null;
	private GatewayEventManager _gatewayEventManager = null;
	private Boolean _Gal_in_Dyscovery_state = false;

	private ParserLocker _lockerStartDevice;
	private IDataLayer DataLayer = null;
	private Discovery_Freshness_ForcePing _discoveryManager = null;
	PropertiesManager PropertiesManager = null;
	private ManageMapPanId manageMapPanId;
	private String networkPanID = null;

	public String getNetworkPanID() {
		return networkPanID;
	}

	public void setNetworkPanID(String panID) {
		networkPanID = panID;
	}

	public ManageMapPanId getManageMapPanId() {
		return manageMapPanId;
	}

	/**
	* A method that schedules a network recovery on GAL every day at 00:05
	**/
	private void scheduleResetTimerTask()
	{
		TimerTask timerTask=new TimerTask() {
			
			public void run() {
				try {
					recoveryGAL();
				} catch (Exception e) {
					LOG.error("Error invoking recoveryGAL",e);
				}
			}
		};
		Timer timer=new Timer();
	
		//start at 00:05 every day, from tomorrow
		Calendar cal=Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 00);
		cal.set(Calendar.MINUTE, 05);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		
		timer.scheduleAtFixedRate(timerTask, cal.getTime(), 24 * 60 * 60 * 1000);
	}
	
	/**
	 * Initialize the DataLayer class, with the relative RS-232 conection Used,
	 * also for the Rest Api
	 */
	private void initializeGAL() throws Exception {

		LOG.debug("Gal Version: " + getVersion().getManufacturerVersion());

		/* Used for reset GAL */
		resetGateway();
		/* End of reset section */
		
		//schedule a daily GAL recovery
		scheduleResetTimerTask();
		
		if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
			DataLayer = new DataFreescale(this);
			DataLayer.initialize();
			try {

				DataLayer.getIKeyInstance().initialize();
			} catch (Exception e) {
				DataLayer.getIKeyInstance().disconnect();
				throw e;
			}
		} else {
			LOG.error("No Platform found for ZigBee dongle");
			throw new Exception("No platform found for ZigBee dongle");
		}
		/*
		 * Check if is auto-start mode is set to true into the configuration
		 * file
		 */

		if (DataLayer.getIKeyInstance().isConnected()) {
			if (PropertiesManager.getAutoStart() == 1) {
				try {
					executeAutoStart();
				} catch (Exception e) {

					LOG.error("Error on autostart!", e);
				}
			} else {
				short _EndPoint = 0;
				_EndPoint = DataLayer.configureEndPointSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
				if (_EndPoint == 0)
					throw new Exception("Error on configure endpoint");

			}
		}

		LOG.info("***Gateway is ready now... Current GAL Status: {} ***",getGatewayStatus().toString());

	}

	/**
	 * recovery of the GAL,
	 */
	public void recoveryGAL() throws Exception {
		LOG.debug("Current number of threads: {}", Thread.getAllStackTraces().size());
		MyRunnable thr = new MyRunnable(this) {
			
			public void run() {

				try {
					LOG.error("********GAL node is not responding or recovery procedure was invoked...Starting recovery procedue. Wait...");
					LOG.error("********STARTING RECOVERY...");

					/* Used for reset GAL */
					resetGateway();
					
					/* End of reset section */
					if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
						LOG.error("Re-creating DataLayer Object for FreeScale chip");
						DataLayer = new DataFreescale((GalController) this.getParameter());
						LOG.error("Initializing data Layer");
						DataLayer.initialize();
						try {
							DataLayer.getIKeyInstance().initialize();
						} catch (Exception e) {
							LOG.error("Exception Initializing DataLayer: {}",e);
							DataLayer.getIKeyInstance().disconnect();
							throw e;
						}
					} else{
						LOG.error("No Platform found for ZigBee dongle");
						throw new Exception("No platform found for ZigBee dongle");
						
					}
					if (DataLayer.getIKeyInstance().isConnected()) {
						short _EndPoint = 0;
						if (lastEndPoint == null) {
							_EndPoint = configureEndpoint(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
							if (_EndPoint == 0)
								throw new Exception("Error on configure endpoint");
						} else {
							_EndPoint = configureEndpoint(PropertiesManager.getCommandTimeoutMS(), lastEndPoint);
							if (_EndPoint == 0)
								throw new Exception("Error on configure endpoint");

						}
						Status st = null;
						if (lastSai != null) {
							st = startGatewayDevice(PropertiesManager.getCommandTimeoutMS(), -1, lastSai, false);

						} else {
							st = startGatewayDevice(PropertiesManager.getCommandTimeoutMS(), -1, PropertiesManager.getSturtupAttributeInfo(), false);
						}
						if (st.getCode() != GatewayConstants.SUCCESS)
							throw new Exception("Error on starting gal" + st.getMessage());
						else {

							LOG.info("***Gateway is ready now... Current GAL Status: " + getGatewayStatus().toString() + "***");
						}
					}else{
						LOG.error("DataLayer instance was not connected, Endpoints not configured");
					}
					LOG.error("********RECOVERY DONE!");

					return;
				} catch (Exception e1) {
					LOG.error("Error resetting GAL");
				} 

			}
		};
		LOG.error("Starting recoveryGAL thread");
		new Thread(thr).start();
	}

	/**
	 * Creates a new instance with a {@code PropertiesManager} as the desired
	 * configuration.
	 * 
	 * @param _properties
	 *            the PropertiesManager containing the desired configuration for
	 *            the Gal controller.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public GalController(PropertiesManager _properties) throws Exception {
		PropertiesManager = _properties;
		zdoManager = new ZdoManager(this);
		apsManager = new ApsMessageManager(this);
		messageManager = new MessageManager(this);
		_gatewayEventManager = new GatewayEventManager(this);
		manageMapPanId = new ManageMapPanId(this);
		_lockerStartDevice = new ParserLocker();
		_discoveryManager = new Discovery_Freshness_ForcePing(this);
		executor = Executors.newFixedThreadPool(getPropertiesManager().getNumberOfThreadForAnyPool(), new ThreadFactory() {

			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-GalController");
			}
		});

		if (executor instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) executor).setKeepAliveTime(getPropertiesManager().getKeepAliveThread(), TimeUnit.MINUTES);
			((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);
		}
		initializeGAL();
	}

	/**
	 * Gets the PropertiesManager instance.
	 * 
	 * @return the PropertiesManager instance.
	 */
	public PropertiesManager getPropertiesManager() {
		return PropertiesManager;

	}

	/**
	 * Gets the list of gateway event listeners. The Gal mantains a list of
	 * registered {@code GatewayEventListener}. When an gateway event happens
	 * all the relative listeners will be notified.
	 * 
	 * @return the list of gateway event listeners.
	 * @see GatewayEventListener
	 * @see GatewayDeviceEventEntry
	 */

	public List<GatewayDeviceEventEntry> getListGatewayEventListener() {
		synchronized (listGatewayEventListener) {
			return listGatewayEventListener;
		}

	}

	/**
	 * Gets the list of registered callbacks. The callbacks are registered in a
	 * {@code CallbackEntry} acting as a convenient container.
	 * 
	 * @return the list of registered callbacks.
	 * @see CallbackEntry
	 */
	public List<CallbackEntry> getCallbacks() {
		synchronized (listCallback) {
			return listCallback;
		}
	}

	/**
	 * Gets a discovery manager.
	 * 
	 * @return the discovery manager.
	 */
	public Discovery_Freshness_ForcePing getDiscoveryManager() {
		synchronized (_discoveryManager) {
			return _discoveryManager;
		}
	}

	/**
	 * Gets the Aps manager.
	 * 
	 * @return the Aps manager.
	 */
	@Deprecated
	public ApsMessageManager getApsManager() {
		synchronized (apsManager) {
			return apsManager;
		}
	}

	/**
	 * Gets the Message manager APS/INTERPAN.
	 * 
	 * @return the message manager.
	 */
	public MessageManager getMessageManager() {
		synchronized (messageManager) {

			return messageManager;
		}
	}

	/**
	 * Gets the Zdo manager.
	 * 
	 * @return the Zdo manager.
	 */
	public ZdoManager getZdoManager() {
		synchronized (zdoManager) {
			return zdoManager;
		}
	}

	/**
	 * Gets the actual data layer implementation.
	 * 
	 * @return the actual data layer implementation.
	 */
	public IDataLayer getDataLayer() {
		synchronized (DataLayer) {
			return DataLayer;
		}

	}

	/**
	 * Returns {@code true} if the Gal is in discovery state.
	 * 
	 * @return {@code true} if the Gal is in discovery state; {@code false}
	 *         otherwise.
	 */
	public synchronized Boolean get_Gal_in_Dyscovery_state() {
		return _Gal_in_Dyscovery_state;
	}

	/**
	 * Sets the discovery state for the Gal.
	 * 
	 * @param GalonDyscovery
	 *            whether the Gal is in discovery state {@code true} or not
	 *            {@code false}.
	 */
	public synchronized void set_Gal_in_Dyscovery_state(Boolean GalonDyscovery) {
		_Gal_in_Dyscovery_state = GalonDyscovery;
	}

	/**
	 * Gets the gateway event manager.
	 * 
	 * @return the gateway event manager.
	 */
	public GatewayEventManager get_gatewayEventManager() {
		return _gatewayEventManager;
	}

	private WrapperWSNNode GalNode = null;

	/**
	 * Allows the creation of an endpoint to which is associated a
	 * {@code SimpleDescriptor}. The operation is synchronous and lasts for a
	 * maximum timeout time.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param desc
	 *            the {@code SimpleDescriptor}.
	 * @return a short representing the endpoint.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public short configureEndpoint(long timeout, SimpleDescriptor desc) throws IOException, Exception, GatewayException {

		if ((desc.getApplicationInputCluster().size() + desc.getApplicationOutputCluster().size()) > 30/*
																										 * 60
																										 * Bytes
																										 */) {
			throw new Exception("Simple Descriptor Out Of Memory");
		} else {
			short result = DataLayer.configureEndPointSync(timeout, desc);
			lastEndPoint = desc;
			return SerializationUtils.clone(result);
		}
	}

	/**
	 * Retrieves the local services (the endpoints) on which the GAL is running
	 * and listening
	 * 
	 * @return the local services.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeServices getLocalServices() throws IOException, Exception, GatewayException {
		NodeServices result = DataLayer.getLocalServices(getPropertiesManager().getCommandTimeoutMS());
		if (GalNode != null && GalNode.get_node().getAddress() != null) {
			result.setAddress(GalNode.get_node().getAddress());
			synchronized (getNetworkcache()) {
				for (WrapperWSNNode o : getNetworkcache()) {

					if (o.get_node() != null && o.get_node().getAddress() != null && o.get_node().getAddress().getNetworkAddress().equals(get_GalNode().get_node().getAddress().getNetworkAddress())) {
						o.set_nodeServices(result);
						result = o.get_nodeServices();
						break;
					}
				}
			}
		}
		return SerializationUtils.clone(result);
	}

	/**
	 * Returns the list of NodeServices of all nodes into the network
	 * 
	 * @return the list of NodeServices for every node.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeServicesList readServicesCache() throws IOException, Exception, GatewayException {
		NodeServicesList list = new NodeServicesList();
		synchronized (getNetworkcache()) {
			for (WrapperWSNNode o : getNetworkcache()) {
				if (o.get_nodeServices() != null)
					list.getNodeServices().add(o.get_nodeServices());
			}
		}
		return SerializationUtils.clone(list);
	}

	/**
	 * Autostart's execution.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void executeAutoStart() throws Exception {
		LOG.info("Executing AutoStart procedure...");
		short _EndPoint = DataLayer.configureEndPointSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
		if (_EndPoint > 0x00) {
			LOG.info("Configure EndPoint completed...");
			Status _statusStartGatewayDevice = DataLayer.startGatewayDeviceSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSturtupAttributeInfo());
			if (_statusStartGatewayDevice.getCode() == 0x00) {
				LOG.info("StartGateway Device completed...");
				return;
			}

		}
	}

	/**
	 * Returns the list of active nodes and connected to the ZigBee network from
	 * the cache of the GAL
	 * 
	 * @return the list of active nodes connected.
	 */
	public WSNNodeList readNodeCache() {
		WSNNodeList _list = new WSNNodeList();
		synchronized (getNetworkcache()) {
			for (WrapperWSNNode x : getNetworkcache()) {
				if (x.is_discoveryCompleted())
					_list.getWSNNode().add(x.get_node());
			}
			return SerializationUtils.clone(_list);
		}
	}

	/**
	 * Returns the list of associated nodes in the network, and for each node
	 * gives the short and the IEEE Address
	 * 
	 * @return the list of associated nodes in the network.
	 */
	public Aliases listAddress() {
		Aliases _list = new Aliases();

		long counter = 0;
		synchronized (getNetworkcache()) {
			for (WrapperWSNNode x : getNetworkcache()) {
				if (x.is_discoveryCompleted()) {
					_list.getAlias().add(x.get_node().getAddress());
					counter++;
				}
			}
		}
		_list.setNumberOfAlias(counter);
		return SerializationUtils.clone(_list);
	}

	/**
	 * Returns the list of neighbor of the selected nodes of the network by the
	 * address
	 * 
	 * @param aoi
	 *            the address of interest
	 * @return the list of neighbor of the nodes
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public LQIInformation getLQIInformation(Address aoi) throws IOException, Exception, GatewayException {

		LQIInformation _lqi = new LQIInformation();
		WrapperWSNNode x = new WrapperWSNNode(this, String.format("%04X", aoi.getNetworkAddress()));
		WSNNode node = new WSNNode();
		node.setAddress(aoi);
		x.set_node(node);
		x = getFromNetworkCache(x);
		if (x != null) {
			if (x.is_discoveryCompleted()) {
				LQINode _lqinode = new LQINode();
				Mgmt_LQI_rsp _rsp = x.get_Mgmt_LQI_rsp();
				_lqinode.setNodeAddress(x.get_node().getAddress().getIeeeAddress());
				if (_rsp != null && _rsp.NeighborTableList != null) {
					for (NeighborTableLis_Record _n1 : _rsp.NeighborTableList) {
						Neighbor e = new Neighbor();
						try {
							Integer _shortAddress = getShortAddress_FromIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
							e.setShortAddress(_shortAddress);
							e.setDepth((short) _n1._Depth);
							e.setDeviceTypeRxOnWhenIdleRelationship(_n1._RxOnWhenIdle);
							e.setIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
							e.setLQI((short) _n1._LQI);
							e.setExtendedPANId(BigInteger.valueOf(_n1._Extended_PAN_Id));
							e.setPermitJoining((short) _n1._Permitting_Joining);
							_lqinode.getNeighborList().getNeighbor().add(e);
						} catch (Exception ex) {
							LOG.error(ex.getMessage());
						}

					}
				}

				_lqi.getLQINode().add(_lqinode);
			}
			return SerializationUtils.clone(_lqi);

		} else
			throw new Exception("Address not found!");

	}

	/**
	 * Returns the list of neighbor of all nodes of the network
	 * 
	 * @return the list of neighbor of all nodes
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public LQIInformation getAllLQIInformations() throws IOException, Exception, GatewayException {
		LQIInformation _lqi = new LQIInformation();

		synchronized (getNetworkcache()) {
			for (WrapperWSNNode x : getNetworkcache()) {
				LOG.debug("Node: {} - DiscoveryCompleted: {}",x.get_node().getAddress().getNetworkAddress(), x.is_discoveryCompleted());
				if (x.is_discoveryCompleted()) {
					LQINode _lqinode = new LQINode();
					Mgmt_LQI_rsp _rsp = x.get_Mgmt_LQI_rsp();
					if (x.get_node().getAddress().getIeeeAddress() != null) {
						_lqinode.setNodeAddress(x.get_node().getAddress().getIeeeAddress());
						if (_rsp != null && _rsp.NeighborTableList != null) {
							NeighborList _list0 = new NeighborList();
							for (NeighborTableLis_Record _n1 : _rsp.NeighborTableList) {
								try {
									Neighbor e = new Neighbor();
									Integer _shortAddress = getShortAddress_FromIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
									e.setShortAddress(_shortAddress);
									e.setDepth((short) _n1._Depth);
									e.setDeviceTypeRxOnWhenIdleRelationship(_n1._Device_Type_RxOnWhenIdle_Relationship);
									e.setIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
									e.setExtendedPANId(BigInteger.valueOf(_n1._Extended_PAN_Id));
									e.setPermitJoining((short) _n1._Permitting_Joining);
									e.setLQI((short) _n1._LQI);
									_list0.getNeighbor().add(e);
								} catch (Exception ex) {
									LOG.error(ex.getMessage());
								}
							}
							_lqinode.setNeighborList(_list0);
						}
						_lqi.getLQINode().add(_lqinode);
					}
				}
			}
		}
		return SerializationUtils.clone(_lqi);
	}

	/**
	 * Retrieves the informations about the NodeDescriptor of a ZigBee node
	 * 
	 * @param timeout
	 *            the timeout
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param addrOfInterest
	 *            the address of interest.
	 * @param Async
	 *            whether the operation will be synchronously or not.
	 * @return the resulting {@code NodeDescriptor}
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeDescriptor getNodeDescriptor(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final boolean Async) throws IOException, Exception, GatewayException {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {

			executor.execute(new MyRunnable(this) {

				public void run() {
					NodeDescriptor nodeDescriptor = new NodeDescriptor();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							nodeDescriptor = DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);
							WrapperWSNNode x = new WrapperWSNNode((GalController) this.getParameter(), String.format("%04X", addrOfInterest.getNetworkAddress()));
							WSNNode node = new WSNNode();
							node.setAddress(addrOfInterest);
							x.set_node(node);
							x = getFromNetworkCache(x);
							if (x != null)
								x.setNodeDescriptor(nodeDescriptor);

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						} catch (IOException e) {

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
						get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

					}

				}
			});
			return null;

		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				NodeDescriptor nodeDescriptor = DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);
				WrapperWSNNode x = new WrapperWSNNode(this, String.format("%04X", addrOfInterest.getNetworkAddress()));
				WSNNode node = new WSNNode();
				node.setAddress(addrOfInterest);
				x.set_node(node);
				x = getFromNetworkCache(x);
				if (x != null)
					x.setNodeDescriptor(nodeDescriptor);
				return SerializationUtils.clone(nodeDescriptor);
			} else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	/**
	 * Gets the current channel.
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @return the current channel.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public short getChannelSync(long timeout) throws IOException, Exception, GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING)
			return DataLayer.getChannelSync(timeout);
		else
			throw new GatewayException("Gal is not in running state!");

	}

	/**
	 * Allows to start/create a ZigBee network using the
	 * {@code StartupAttributeInfo} class as a previously configured parameter.
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @param _requestIdentifier
	 *            the request identifier
	 * @param sai
	 *            the {@code StartupAttributeInfo}
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */

	public Status startGatewayDevice(final long timeout, final int _requestIdentifier, final StartupAttributeInfo sai, final boolean Async) throws IOException, Exception, GatewayException {
		// The network can start only from those two gateway status...
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START || getGatewayStatus() == GatewayStatus.GW_STOPPED) {

						setGatewayStatus(GatewayStatus.GW_STARTING);
						try {
							Status _res = DataLayer.startGatewayDeviceSync(timeout, sai);
							if (_res.getCode() == GatewayConstants.SUCCESS) {
								LOG.debug("WriteSas completed!");
								_lockerStartDevice.setId(0);
								_lockerStartDevice.getObjectLocker().poll(timeout, TimeUnit.MILLISECONDS);
								if (_lockerStartDevice.getId() > 0) {
									lastSai = sai;
									LOG.info("Gateway Started now!");

								} else {
									setGatewayStatus(GatewayStatus.GW_READY_TO_START);

									LOG.error("*******Gateway NOT Started!");
									_res.setCode((short) GatewayConstants.GENERAL_ERROR);
									_res.setMessage("No Network Event Running received!");

								}
							} else {
								LOG.error("*******Gateway NOT Started!");
							}

							get_gatewayEventManager().notifyGatewayStartResult(_res);
						} catch (IOException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, _s);

						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());

							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, _s);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());

							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, _s);

						}
					} else {
						// ...from all others, throw an exception
						String message = "Trying to start Gateway Device in " + getGatewayStatus() + " state.";
						LOG.debug(message);
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, _s);

					}
				}
			});
			return null;
		} else {
			Status _status;
			if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START || getGatewayStatus() == GatewayStatus.GW_STOPPED) {
				setGatewayStatus(GatewayStatus.GW_STARTING);

				_status = DataLayer.startGatewayDeviceSync(timeout, sai);

				if (_status.getCode() == GatewayConstants.SUCCESS) {
					_lockerStartDevice.setId(0);
					_lockerStartDevice.getObjectLocker().poll(timeout, TimeUnit.MILLISECONDS);
					if (_lockerStartDevice.getId() > 0) {
						lastSai = sai;
						LOG.info("***Gateway Started now!****");
					} else {
						setGatewayStatus(GatewayStatus.GW_READY_TO_START);

						LOG.error("Gateway NOT Started!");
						_status.setCode((short) GatewayConstants.GENERAL_ERROR);
						_status.setMessage("No Network Event Running received!");
					}
				}

				get_gatewayEventManager().notifyGatewayStartResult(_status);
			} else {
				// ...from all others, throw an exception
				String message = "Trying to start Gateway Device in " + getGatewayStatus() + " state.";
				LOG.debug(message);

				throw new GatewayException(message);
			}
			return SerializationUtils.clone(_status);

		}

	}

	/**
	 * Starts/creates a ZigBee network using configuration loaded from
	 * {@code PropertiesManager}.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status startGatewayDevice(long timeout, int _requestIdentifier, boolean Async) throws IOException, Exception, GatewayException {
		StartupAttributeInfo sai = PropertiesManager.getSturtupAttributeInfo();
		return startGatewayDevice(timeout, _requestIdentifier, sai, Async);

	}

	/**
	 * Resets the GAl with the ability to set whether to delete the
	 * NonVolatileMemory to the next reboot
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @param _requestIdentifier
	 *            the request identifier
	 * @param mode
	 *            the desired mode
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status resetDongle(final long timeout, final int _requestIdentifier, final short mode, final boolean Async) throws IOException, Exception, GatewayException {
		if (mode == GatewayConstants.RESET_COMMISSIONING_ASSOCIATION) {
			PropertiesManager.setStartupSet((short) 0x18);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl((short) 0x00);

		} else if (mode == GatewayConstants.RESET_USE_NVMEMORY) {
			PropertiesManager.setStartupSet((short) 0x00);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl((short) 0x04);

		} else if (mode == GatewayConstants.RESET_COMMISSIONING_SILENTSTART) {
			PropertiesManager.setStartupSet((short) 0x18);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl((short) 0x04);
		}
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					try {

						Status _s = new Status();
						_s.setCode((short) GatewayConstants.SUCCESS);
						_s.setMessage("Reset Done");
						initializeGAL();
						get_gatewayEventManager().notifyResetResult(_s);

					} catch (Exception e) {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(e.getMessage());
						get_gatewayEventManager().notifyResetResult(_s);
					}

				}
			});
			return null;
		} else {

			Status _s = new Status();
			_s.setCode((short) GatewayConstants.SUCCESS);
			_s.setMessage("Reset Done");
			initializeGAL();
			get_gatewayEventManager().notifyResetResult(_s);
			return SerializationUtils.clone(_s);
		}

	}

	/**
	 * Stops the network.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status stopNetwork(final long timeout, final int _requestIdentifier, boolean Async) throws Exception, GatewayException {
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						setGatewayStatus(GatewayStatus.GW_STOPPING);

						Status _res = null;
						try {
							_res = DataLayer.stopNetworkSync(timeout);
							get_gatewayEventManager().notifyGatewayStopResult(_res);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, _s);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, _s);

						}

					} else {

						String message = "Trying to stop Gateway Device in " + getGatewayStatus() + " state.";
						LOG.info(message);
						
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, _s);

					}
				}
			});
			return null;
		} else {
			Status _status;
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				setGatewayStatus(GatewayStatus.GW_STOPPING);
				_status = DataLayer.stopNetworkSync(timeout);
				get_gatewayEventManager().notifyGatewayStopResult(_status);
			} else {
				// ...from all others, throw an exception
				String message = "Trying to stop Gateway Device in " + getGatewayStatus() + " state.";
				throw new GatewayException(message);
			}
			return SerializationUtils.clone(_status);

		}
	}

	public String APSME_GETSync(short attrId) throws Exception, GatewayException {
		return DataLayer.APSME_GETSync(PropertiesManager.getCommandTimeoutMS(), attrId);
	}

	public String MacGetPIBAttributeSync(short attrId) throws Exception, GatewayException {
		return DataLayer.MacGetPIBAttributeSync(PropertiesManager.getCommandTimeoutMS(), attrId);
	}

	public void APSME_SETSync(short attrId, String value) throws Exception, GatewayException {
		DataLayer.APSME_SETSync(PropertiesManager.getCommandTimeoutMS(), attrId, value);
	}

	public String NMLE_GetSync(short ilb, short iEntry) throws IOException, Exception, GatewayException {
		String _value = DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), ilb, iEntry);
		/* Refresh value of the PanId */
		if (ilb == 80)
			setNetworkPanID(_value);

		return _value;

	}

	public void NMLE_SetSync(short attrId, String value) throws Exception, GatewayException {
		DataLayer.NMLE_SETSync(PropertiesManager.getCommandTimeoutMS(), attrId, value);
	}

	/**
	 * Creates a callback to receive APS/ZDP/ZCL messages using a class of
	 * filters.
	 * 
	 * @param proxyIdentifier
	 *            the proxy identifier for the callback
	 * @param callback
	 *            the callback
	 * @param listener
	 *            the listener where messages for the callback will be notified.
	 * @return the callback's identifier.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	@Deprecated
	public long createCallback(int proxyIdentifier, Callback callback, APSMessageListener listener) throws IOException, Exception, GatewayException {
		CallbackEntry callbackEntry = new CallbackEntry();
		callbackEntry.setCallback(callback);
		callbackEntry.setDestination(listener);
		callbackEntry.setProxyIdentifier(proxyIdentifier);
		long id = getCallbackIdentifier();
		callbackEntry.setCallbackIdentifier(id);
		synchronized (listCallback) {
			listCallback.add(callbackEntry);
		}
		return id;
	}

	/**
	 * Creates a callback to receive APS/ZDP/ZCL/InterPAN messages using a class
	 * of filters.
	 * 
	 * @param proxyIdentifier
	 *            the proxy identifier for the callback
	 * @param callback
	 *            the callback
	 * @param listener
	 *            the listener where messages for the callback will be notified.
	 * @return the callback's identifier.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public long createCallback(int proxyIdentifier, Callback callback, MessageListener listener) throws IOException, Exception, GatewayException {
		CallbackEntry callbackEntry = new CallbackEntry();
		callbackEntry.setCallback(callback);
		callbackEntry.setGenericDestination(listener);
		callbackEntry.setProxyIdentifier(proxyIdentifier);
		long id = getCallbackIdentifier();
		callbackEntry.setCallbackIdentifier(id);
		synchronized (listCallback) {
			listCallback.add(callbackEntry);
		}
		return id;
	}

	/**
	 * Deletes a callback.
	 * 
	 * @param id
	 *            the callback's id.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void deleteCallback(long id) throws IOException, Exception, GatewayException {
		short _index = -1;
		short index = -1;

		for (CallbackEntry x : listCallback) {
			_index++;
			if (x.getCallbackIdentifier().equals(id)) {
				index = _index;
				break;
			}
		}

		if (index > -1)
			synchronized (listCallback) {
				listCallback.remove(index);
			}

		else
			throw new GatewayException("Callback with id " + id + " not present");

	}

	/**
	 * Activation of discovery procedures of the services (the endpoints) for a
	 * node connected to the ZigBee network.
	 * 
	 * @param timeout
	 *            the desired timout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param aoi
	 *            the address of interest.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the discovered node services.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeServices startServiceDiscovery(final long timeout, final int _requestIdentifier, final Address aoi, boolean Async) throws IOException, Exception, GatewayException {
		if (aoi.getNetworkAddress() == null && aoi.getIeeeAddress() != null)
			aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (aoi.getIeeeAddress() == null && aoi.getNetworkAddress() != null)
			aoi.setIeeeAddress(getIeeeAddress_FromShortAddress(aoi.getNetworkAddress()));

		if (Async) {
			executor.execute(new MyRunnable(this) {
				public void run() {
					NodeServices _newNodeService = new NodeServices();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						List<Short> _s = null;
						try {
							_s = DataLayer.startServiceDiscoverySync(timeout, aoi);
							Status _ok = new Status();
							_ok.setCode((short) 0x00);
							_newNodeService.setAddress(aoi);

							for (Short x : _s) {
								ActiveEndpoints _n = new ActiveEndpoints();
								_n.setEndPoint(x);
								_newNodeService.getActiveEndpoints().add(_n);
							}

							WrapperWSNNode x = new WrapperWSNNode((GalController) this.getParameter(), String.format("%04X", aoi.getNetworkAddress()));
							WSNNode node = new WSNNode();
							node.setAddress(aoi);
							x.set_node(node);
							x = getFromNetworkCache(x);
							if (x != null) {
								x.set_nodeServices(_newNodeService);
							}
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _ok, _newNodeService);
						} catch (IOException e) {
							_newNodeService.setAddress(aoi);
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, _newNodeService);
						} catch (GatewayException e) {
							_newNodeService.setAddress(aoi);
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, _newNodeService);
						} catch (Exception e) {
							_newNodeService.setAddress(aoi);
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, _newNodeService);
						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state");
						get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, _newNodeService);

					}
				}

			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				List<Short> _result = DataLayer.startServiceDiscoverySync(timeout, aoi);

				NodeServices _newNodeService = new NodeServices();
				_newNodeService.setAddress(aoi);

				for (Short x : _result) {
					ActiveEndpoints _n = new ActiveEndpoints();
					_n.setEndPoint(x);
					_newNodeService.getActiveEndpoints().add(_n);
				}
				WrapperWSNNode x = new WrapperWSNNode(this, String.format("%04X", aoi.getNetworkAddress()));
				WSNNode node = new WSNNode();
				node.setAddress(aoi);
				x.set_node(node);

				x = getFromNetworkCache(x);
				if (x != null) {
					x.set_nodeServices(_newNodeService);
				}

				return SerializationUtils.clone(_newNodeService);
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	/**
	 * Returns the list of all callbacks to which you have previously
	 * registered.
	 * 
	 * @param requestIdentifier
	 *            the request identifier.
	 * @return the callback's list.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public CallbackIdentifierList listCallbacks(int requestIdentifier) throws IOException, Exception, GatewayException {
		CallbackIdentifierList toReturn = new CallbackIdentifierList();
		for (CallbackEntry ce : listCallback) {
			if (ce.getProxyIdentifier() == requestIdentifier)
				toReturn.getCallbackIdentifier().add(ce.getCallbackIdentifier());
		}
		return SerializationUtils.clone(toReturn);
	}

	/**
	 * Registration callback to receive notifications about events. The
	 * registering client is identified by the proxy identifier parameter.
	 * 
	 * @param listener
	 *            the listener to registers.
	 * @param proxyIdentifier
	 *            the proxy identifier.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void setGatewayEventListener(GatewayEventListener listener, int proxyIdentifier) {
		boolean _listenerFound = false;
		synchronized (getListGatewayEventListener()) {

			for (int i = 0; i < getListGatewayEventListener().size(); i++) {
				if (getListGatewayEventListener().get(i).getProxyIdentifier() == proxyIdentifier) {
					if (listener == null) {
						getListGatewayEventListener().remove(i);

						synchronized (getCallbacks()) {
							for (CallbackEntry x : getCallbacks()) {
								if (x.getProxyIdentifier() == proxyIdentifier)
									try {
										deleteCallback(x.getCallbackIdentifier());
									} catch (IOException e) {
										LOG.error("Error deleting callback",e);
									} catch (GatewayException e) {
										LOG.error("Error deleting callback",e);
									} catch (Exception e) {
										LOG.error("Error deleting callback",e);
									}
							}
						}
						LOG.info("Removing Listener for: {}", proxyIdentifier);
						return;
					} else {
						_listenerFound = true;
						break;
					}
				}

			}
		}

		if ((!_listenerFound) && (listener != null)) {
			GatewayDeviceEventEntry<GatewayEventListener> gdee = new GatewayDeviceEventEntry<GatewayEventListener>();
			gdee.setGatewayEventListener(listener);
			gdee.setProxyIdentifier(proxyIdentifier);
			getListGatewayEventListener().add(gdee);

		}
	}

	/**
	 * Sends an Aps message.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param message
	 *            the message to send.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void sendAPSMessage(long timeout, long _requestIdentifier, APSMessage message) throws IOException, Exception, GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
			/*
			 * if (message.getDestinationAddress().getNetworkAddress() == null
			 * && message.getDestinationAddress().getIeeeAddress() != null)
			 * message.getDestinationAddress().setNetworkAddress(
			 * getShortAddress_FromIeeeAddress
			 * (message.getDestinationAddress().getIeeeAddress())); if
			 * (message.getDestinationAddress().getIeeeAddress() == null &&
			 * message.getDestinationAddress().getNetworkAddress() != null)
			 * message.getDestinationAddress().setIeeeAddress(
			 * getIeeeAddress_FromShortAddress
			 * (message.getDestinationAddress().getNetworkAddress()));
			 */
			DataLayer.sendApsSync(timeout, message);
		} else
			throw new GatewayException("Gal is not in running state!");
	}

	/**
	 * Sends an InterPAN message.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param message
	 *            the message to send.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void sendInterPANMessage(long timeout, long _requestIdentifier, InterPANMessage message) throws IOException, Exception, GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
			DataLayer.sendInterPANMessaSync(timeout, message);
		} else
			throw new GatewayException("Gal is not in running state!");
	}

	/**
	 * Disassociates a node from the network.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param addrOfInterest
	 *            the address of interest.
	 * @param mask
	 *            the mask.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status leave(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final int mask, final boolean Async) throws IOException, Exception, GatewayException {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {
			executor.execute(new MyRunnable(this) {
				public void run() {
					Status _s = null;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						if (!addrOfInterest.getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {

							try {

								leavePhilips(timeout, _requestIdentifier, addrOfInterest);
								_s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
								if (_s.getCode() == GatewayConstants.SUCCESS) {
									/* get the node from cache */
									WrapperWSNNode x = new WrapperWSNNode((GalController) this.getParameter(), String.format("%04X", addrOfInterest.getNetworkAddress()));
									WSNNode node = new WSNNode();
									node.setAddress(addrOfInterest);
									x.set_node(node);
									x = getFromNetworkCache(x);
									if (x != null) {
										x.abortTimers();
										get_gatewayEventManager().nodeRemoved(_s, x.get_node());
										getNetworkcache().remove(x);
										get_gatewayEventManager().notifyleaveResult(_s);
										get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);
									}
								}

							} catch (IOException e) {
								Status _s1 = new Status();
								_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
								_s1.setMessage(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							} catch (GatewayException e) {
								Status _s1 = new Status();
								_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
								_s1.setMessage(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							} catch (Exception e) {
								Status _s1 = new Status();
								_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
								_s1.setMessage(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							}
						} else {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage("Is not possible Leave the GAL!");
							get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
							get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

						}

					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
						get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

					}

				}

			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				if (!addrOfInterest.getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {
					leavePhilips(timeout, _requestIdentifier, addrOfInterest);
					Status _s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
					if (_s.getCode() == GatewayConstants.SUCCESS) {
						/* get the node from cache */
						WrapperWSNNode x = new WrapperWSNNode(this, String.format("%04X", addrOfInterest.getNetworkAddress()));
						WSNNode node = new WSNNode();
						node.setAddress(addrOfInterest);
						x.set_node(node);
						x = getFromNetworkCache(x);
						if (x != null) {
							x.abortTimers();
							get_gatewayEventManager().nodeRemoved(_s, x.get_node());
							getNetworkcache().remove(x);
							get_gatewayEventManager().notifyleaveResult(_s);
							get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);
						}
					}

					return SerializationUtils.clone(_s);
				} else
					throw new GatewayException("Is not possible Leave the GAL!");
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	private void leavePhilips(final long timeout, final int _requestIdentifier, final Address addrOfInterest) throws IOException, Exception, GatewayException {
		/* Check if the device is the Philips light */
		WrapperWSNNode wrapNode = new WrapperWSNNode(this, String.format("%04X", addrOfInterest.getNetworkAddress()));
		WSNNode node = new WSNNode();
		node.setAddress(addrOfInterest);
		wrapNode.set_node(node);
		wrapNode = getFromNetworkCache(wrapNode);
		if (wrapNode != null) {

			NodeDescriptor nodeDescriptor = null;
			if (wrapNode.getNodeDescriptor() == null)
				nodeDescriptor = getNodeDescriptor(timeout, _requestIdentifier, addrOfInterest, false);

			else
				nodeDescriptor = wrapNode.getNodeDescriptor();

			/* Philips Device Led */
			if (nodeDescriptor.getManufacturerCode() == 4107) {

				LOG.info("####Executing leave for Philips Light");

				Address broadcast = new Address();
				broadcast.setNetworkAddress(0xffff);

				/* ScanRequest */
				InterPANMessage scanReqCommand = new InterPANMessage();
				scanReqCommand.setSrcAddressMode(3);
				scanReqCommand.setSrcAddress(GalNode.get_node().getAddress());
				scanReqCommand.setSrcPANID(Integer.parseInt(getNetworkPanID(), 16));
				scanReqCommand.setDstAddressMode(2);
				scanReqCommand.setDestinationAddress(broadcast);
				scanReqCommand.setDestPANID(getManageMapPanId().getPanid(wrapNode.get_node().getAddress().getIeeeAddress()));
				scanReqCommand.setProfileID(49246);
				scanReqCommand.setClusterID(4096);
				scanReqCommand.setASDULength(9);
				scanReqCommand.setASDU(new byte[] { 0x11, 0x01, 0x00, (byte) 0xCA, (byte) 0xFE, (byte) 0xCA, (byte) 0xFE, 0x02, 0x33 });
				sendInterPANMessage(timeout, _requestIdentifier, scanReqCommand);

				// Thread.sleep(1000);

				/* ScanRequest */
				InterPANMessage resetCommand = new InterPANMessage();
				resetCommand.setSrcAddressMode(3);
				resetCommand.setSrcAddress(GalNode.get_node().getAddress());
				resetCommand.setSrcPANID(Integer.parseInt(getNetworkPanID(), 16));
				resetCommand.setDstAddressMode(2);
				resetCommand.setDestinationAddress(broadcast);
				resetCommand.setDestPANID(getManageMapPanId().getPanid(wrapNode.get_node().getAddress().getIeeeAddress()));
				resetCommand.setProfileID(49246);
				resetCommand.setClusterID(4096);
				resetCommand.setASDULength(7);
				resetCommand.setASDU(new byte[] { 0x11, 0x03, 0x07, (byte) 0xCA, (byte) 0xFE, (byte) 0xCA, (byte) 0xFE });
				sendInterPANMessage(timeout, _requestIdentifier, resetCommand);

				LOG.info("####End leave for Philips Light");

			}

		}

	}

	/**
	 * Opens a ZigBee network to a single node, and for a specified duration, to
	 * be able to associate new nodes.
	 * 
	 * @param timeout
	 *            the desired timout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param addrOfInterest
	 *            the address of interest.
	 * @param duration
	 *            the duration.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status permitJoin(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final short duration, final boolean Async) throws IOException, GatewayException, Exception {
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {

					Status _s = new Status();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							_s = DataLayer.permitJoinSync(timeout, addrOfInterest, duration, (byte) 0x00);
							get_gatewayEventManager().notifypermitJoinResult(_s);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
						} catch (GatewayException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
						} catch (Exception e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
					}

				}

			});
			return null;
		} else {
			Status _s;
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				try {
					_s = DataLayer.permitJoinSync(timeout, addrOfInterest, duration, (byte) 0x00);
					get_gatewayEventManager().notifypermitJoinResult(_s);
					return SerializationUtils.clone(_s);
				} catch (IOException e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
					throw e;
				} catch (GatewayException e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
					throw e;
				} catch (Exception e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, _s1);
					throw e;
				}
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	/**
	 * Allows the opening of the ZigBee network to all nodes, and for a
	 * specified duration, to be able to associate new nodes
	 * 
	 * @param timeout
	 *            the desired timout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param duration
	 *            the duration.
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 */
	public Status permitJoinAll(final long timeout, final int _requestIdentifier, final short duration, final boolean Async) throws IOException, Exception {
		final Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					Status _s;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							_s = DataLayer.permitJoinAllSync(timeout, _add, duration, (byte) 0x00);
							get_gatewayEventManager().notifypermitJoinResult(_s);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(_s1);
						} catch (Exception e) {
							Status _s2 = new Status();
							_s2.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s2.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(_s2);
						}
					} else {

						Status _s2 = new Status();
						_s2.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s2.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifypermitJoinResult(_s2);
					}

				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				Status _s = DataLayer.permitJoinAllSync(timeout, _add, duration, (byte) 0x00);
				get_gatewayEventManager().notifypermitJoinResult(_s);
				return SerializationUtils.clone(_s);
			} else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	/**
	 * Activation of the discovery procedures of the nodes in a ZigBee network.
	 * Each node will produce a notification by the announcement
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param requestIdentifier
	 *            the request identifier
	 * @param discoveryMask
	 *            the discovery mask
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void startNodeDiscovery(long timeout, int requestIdentifier, int discoveryMask) throws GatewayException {
		int _index = -1;
		LOG.debug("Called startNodeDiscovery Mask: {} - Timeout: {}",discoveryMask, timeout);
		_index = existIntolistGatewayEventListener(requestIdentifier);
		if (_index != -1) {
			/* if exist */
			if (((discoveryMask & GatewayConstants.DISCOVERY_ANNOUNCEMENTS) > 0) || (discoveryMask == GatewayConstants.DISCOVERY_STOP)) {
				synchronized (listGatewayEventListener.get(_index)) {
					listGatewayEventListener.get(_index).setDiscoveryMask(discoveryMask);
				}
			}
			if (((discoveryMask & GatewayConstants.DISCOVERY_LQI) > 0) && (timeout > 1)) {

				/* Clear the Network Cache */
				LinkedList<Integer> _toremove = new LinkedList<Integer>();
				int i = 0;
				synchronized (getNetworkcache()) {
					for (WrapperWSNNode x : getNetworkcache()) {
						if (!x.get_node().getAddress().getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {
							x.abortTimers();
							_toremove.add(i);
						}
						i++;
					}
					for (Integer x : _toremove)
						getNetworkcache().remove(x);

				}

				/* Only one element (GALNode) */

				synchronized (GalNode) {
					GalNode.set_Mgmt_LQI_rsp(null);
					GalNode.set_discoveryCompleted(false);
					GalNode.setTimerForcePing(getPropertiesManager().getForcePingTimeout());
					GalNode.setTimerFreshness(getPropertiesManager().getKeepAliveThreshold());
					GalNode.setTimerDiscovery(0);
				}
				if (LOG.isDebugEnabled()) {
					long __timeout = 0;
					if (timeout == 0)
						timeout = GatewayConstants.INFINITE_TIMEOUT;
					__timeout = timeout / 1000 + ((timeout % 1000 > 0) ? 1 : 0);
					LOG.debug("Global Discovery Started(" + __timeout + " seconds)!");
				}
			} else if ((discoveryMask == GatewayConstants.DISCOVERY_STOP) || (timeout == 1)) {

				LOG.info("Global Discovery Stopped!");

			}
		} else {

			LOG.error("Error on startNodeDiscovery: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
			throw new GatewayException("No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
		}

	}

	/**
	 * Removal of a node from the network.
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @param requestIdentifier
	 *            the request identifier
	 * @param discoveryFreshness_mask
	 *            the freshness mask
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void subscribeNodeRemoval(long timeout, int requestIdentifier, int discoveryFreshness_mask) throws GatewayException {
		int _index = -1;
		if ((_index = existIntolistGatewayEventListener(requestIdentifier)) != -1) {

			if (((discoveryFreshness_mask & GatewayConstants.DISCOVERY_LEAVE) > 0) || (discoveryFreshness_mask == GatewayConstants.DISCOVERY_STOP)) {
				listGatewayEventListener.get(_index).setFreshnessMask(discoveryFreshness_mask);
			}

		} else {

			LOG.error("Error on subscribeNodeRemoval: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");

			throw new GatewayException("No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
		}

	}

	/**
	 * Gets the Gateway Status.
	 * 
	 * @return the gateway status
	 * @see GatewayStatus
	 */
	public GatewayStatus getGatewayStatus() {
		synchronized (_gatewayStatus) {
			return _gatewayStatus;
		}
	}

	/**
	 * Gets the version for the ZigBee Gateway Device.
	 * 
	 * @return the version
	 * @see Version
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public static Version getVersion() throws IOException, Exception, GatewayException {
		Version v = new Version();
		org.osgi.framework.Version version = FrameworkUtil.getBundle(GalController.class).getVersion();
		v.setManufacturerVersion(version.getMajor() + "." + version.getMinor() + "." + version.getMicro());
		v.getRPCProtocol().add(RPCProtocol.REST);
		return SerializationUtils.clone(v);
	}

	/**
	 * Sets Gateway's status.
	 * 
	 * @param gatewayStatus
	 *            the gateway's status to set
	 * @see GatewayStatus
	 */
	public synchronized void setGatewayStatus(final GatewayStatus gatewayStatus) {

		if (gatewayStatus == GatewayStatus.GW_RUNNING) {
			/* Get The Network Address of the GAL */
			Runnable thr = new MyRunnable(this) {

				@Override
				public void run() {

					/* Read the PanID of the Network */
					String _networkPanID = null;
					while (_networkPanID == null) {
						try {
							_networkPanID = DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), (short) 0x80, (short) 0x00);
						} catch (Exception e) {

							LOG.error("Error retrieving the PanID of the Network!");

						}
					}
					networkPanID = _networkPanID;

					/* Read the ShortAddress of the GAL */
					String _NetworkAdd = null;
					while (_NetworkAdd == null) {
						try {
							_NetworkAdd = DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), (short) 0x96, (short) 0x00);
						} catch (Exception e) {

							LOG.error("Error retrieving the Gal Network Address!");

						}
					}
					/* Read the IEEEAddress of the GAL */
					BigInteger _IeeeAdd = null;
					while (_IeeeAdd == null) {
						try {
							_IeeeAdd = DataLayer.readExtAddressGal(PropertiesManager.getCommandTimeoutMS());
						} catch (Exception e) {

							LOG.error("Error retrieving the Gal IEEE Address!");

						}
					}
					WSNNode galNode = new WSNNode();
					Address _add = new Address();
					_add.setNetworkAddress(Integer.parseInt(_NetworkAdd, 16));

					_add.setIeeeAddress(_IeeeAdd);
					galNode.setAddress(_add);

					WrapperWSNNode galNodeWrapper = new WrapperWSNNode(((GalController) this.getParameter()), String.format("%04X", _add.getNetworkAddress()));

					/* Read the NodeDescriptor of the GAL */
					NodeDescriptor _NodeDescriptor = null;
					while (_NodeDescriptor == null) {
						try {
							_NodeDescriptor = DataLayer.getNodeDescriptorSync(PropertiesManager.getCommandTimeoutMS(), _add);
							if (_NodeDescriptor != null) {
								if (galNode.getCapabilityInformation() == null)
									galNode.setCapabilityInformation(new MACCapability());
								galNode.getCapabilityInformation().setReceiverOnWhenIdle(_NodeDescriptor.getMACCapabilityFlag().isReceiverOnWhenIdle());
								galNode.getCapabilityInformation().setAllocateAddress(_NodeDescriptor.getMACCapabilityFlag().isAllocateAddress());
								galNode.getCapabilityInformation().setAlternatePanCoordinator(_NodeDescriptor.getMACCapabilityFlag().isAlternatePanCoordinator());
								galNode.getCapabilityInformation().setDeviceIsFFD(_NodeDescriptor.getMACCapabilityFlag().isDeviceIsFFD());
								galNode.getCapabilityInformation().setMainsPowered(_NodeDescriptor.getMACCapabilityFlag().isMainsPowered());
								galNode.getCapabilityInformation().setSecuritySupported(_NodeDescriptor.getMACCapabilityFlag().isSecuritySupported());
								galNodeWrapper.set_node(galNode);
								galNodeWrapper.reset_numberOfAttempt();
								galNodeWrapper.set_discoveryCompleted(true);

								/* If the Node Not Exists */
								if (getFromNetworkCache(galNodeWrapper) == null) {
									if (LOG.isDebugEnabled()) {
										String shortAdd = (galNodeWrapper.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", galNodeWrapper.get_node().getAddress().getNetworkAddress()) : "NULL";
										String IeeeAdd = (galNodeWrapper.get_node().getAddress().getIeeeAddress() != null) ? String.format("%08X", galNodeWrapper.get_node().getAddress().getIeeeAddress()) : "NULL";

										LOG.debug("Adding node from [SetNetworkStatus Announcement] into the NetworkCache IeeeAddress: {} --- Short: {}",IeeeAdd , shortAdd);
									}
									getNetworkcache().add(galNodeWrapper);
								}
								/* The GAl node is already present into the DB */
								else {
									galNodeWrapper = getFromNetworkCache(galNodeWrapper);
									galNodeWrapper.abortTimers();
									galNodeWrapper.set_node(galNode);
								}

							} else {

								LOG.error("ERROR on Read Node Descriptor of Gal.!");

							}

						} catch (Exception e) {

							LOG.error("Error retrieving the Gal Node Descriptor!");

						}
					}
					/* Executing the command(PermitJoin==0) to close network */
					Status _permitjoin = null;
					while (_permitjoin == null || ((_permitjoin != null) && (_permitjoin.getCode() != GatewayConstants.SUCCESS))) {
						try {
							_permitjoin = DataLayer.permitJoinSync(PropertiesManager.getCommandTimeoutMS(), _add, (short) 0x00, (byte) 0x01);
							if (_permitjoin.getCode() != GatewayConstants.SUCCESS) {
								Status _st = new Status();
								_st.setCode((short) GatewayConstants.GENERAL_ERROR);
								_st.setMessage("Error on permitJoin(0) for the GAL node on startup!");

								get_gatewayEventManager().notifyGatewayStartResult(_st);

							}
						} catch (Exception e) {

							LOG.error("Error retrieving the Gal Node Descriptor!");

						}

					}

					if (!galNodeWrapper.isSleepyOrEndDevice()) {
						/* If the Node is NOT a sleepyEndDevice */

						if (PropertiesManager.getKeepAliveThreshold() > 0) {
							/* Execute the Freshness */
							galNodeWrapper.setTimerFreshness(PropertiesManager.getKeepAliveThreshold());
						}
						if (PropertiesManager.getForcePingTimeout() > 0) {
							/* Execute the ForcePing */
							galNodeWrapper.setTimerForcePing(1);
						}

					}

					set_GalNode(galNodeWrapper);
					/* Notify Gal Node */

					/* Saving the Panid in order to leave the Philips light */
					getManageMapPanId().setPanid(galNodeWrapper.get_node().getAddress().getIeeeAddress(), getNetworkPanID());
					/**/

					_lockerStartDevice.setId(1);
					try {
						if (_lockerStartDevice.getObjectLocker().size() == 0)
							_lockerStartDevice.getObjectLocker().put((byte) 0);
					} catch (InterruptedException e) {

					}

					_gatewayStatus = gatewayStatus;

					Status _s = new Status();
					_s.setCode((short) 0x00);
					LOG.debug("\n\rNodeDiscovered From SetGatewayStatus: {}", String.format("%04X", galNodeWrapper.get_node().getAddress().getNetworkAddress()) + "\n\r");

					try {
						get_gatewayEventManager().nodeDiscovered(_s, galNodeWrapper.get_node());
					} catch (Exception e) {

						LOG.error("Error calling nodeDiscovered for the GAL node!");

					}
				}

			};

			new Thread(thr).start();

		} else if (gatewayStatus == GatewayStatus.GW_STOPPED) {
			/* Stop Discovery */
			LOG.info("Stopping Discovery and Freshness procedures...");

			_discoveryManager = null;
			/* Remove all nodes from the cache */
			getNetworkcache().clear();
			_gatewayStatus = gatewayStatus;

		} else
			_gatewayStatus = gatewayStatus;

	}

	/**
	 * Gets the Aps callback identifier.
	 * 
	 * @return the aps callback identifier.
	 */
	public long getCallbackIdentifier() {
		synchronized (this) {
			if (CallbackIdentifier == Long.MAX_VALUE) {
				CallbackIdentifier = (long) 1;
			}
			return CallbackIdentifier++;
		}
	}

	/**
	 * Gets the Gal node.
	 * 
	 * @return the gal node.
	 * @see WrapperWSNNode
	 */
	public WrapperWSNNode get_GalNode() {
		synchronized (GalNode) {
			return GalNode;
		}
	}

	/**
	 * Removes a SimpleDescriptor or an endpoint.
	 * 
	 * @param endpoint
	 *            the endpoint to remove
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status clearEndpoint(short endpoint) throws IOException, Exception, GatewayException {
		Status _s = DataLayer.clearEndpointSync(getPropertiesManager().getCommandTimeoutMS(), endpoint);
		return SerializationUtils.clone(_s);
	}

	/**
	 * Sets a node.
	 * 
	 * @param _GalNode
	 *            the node to set.
	 * @see WSNNode
	 */
	public synchronized void set_GalNode(WrapperWSNNode _GalNode) {
		GalNode = _GalNode;
	}

	/**
	 * Gets the list of cached nodes.
	 * 
	 * @return the list of cached nodes.
	 */
	public List<WrapperWSNNode> getNetworkcache() {
		return NetworkCache;

	}

	/**
	 * Gets the service descriptor for an endpoint.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param _requestIdentifier
	 *            the request identifier.
	 * @param addrOfInterest
	 *            the address of interest.
	 * @param endpoint
	 *            the endpoint
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the simple descriptor.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public ServiceDescriptor getServiceDescriptor(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final short endpoint, boolean Async) throws IOException, Exception, GatewayException {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					ServiceDescriptor _toRes = new ServiceDescriptor();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);
							_toRes = DataLayer.getServiceDescriptor(timeout, addrOfInterest, endpoint);
							_toRes.setAddress(addrOfInterest);
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, _toRes);

						} catch (GatewayException e) {
							_toRes.setAddress(addrOfInterest);
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, _toRes);
						} catch (Exception e) {
							_toRes.setAddress(addrOfInterest);
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, _toRes);
						}
					} else {
						_toRes.setAddress(addrOfInterest);
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, _toRes);
					}
				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				ServiceDescriptor _toRes;
				_toRes = DataLayer.getServiceDescriptor(timeout, addrOfInterest, endpoint);
				_toRes.setAddress(addrOfInterest);
				return SerializationUtils.clone(_toRes);
			} else
				throw new GatewayException("Gal is not in running state!");

		}
	}

	/**
	 * Gets a list of all bindings stored in a remote node, starting from a
	 * given index.
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @param _requestIdentifier
	 *            the request identifier
	 * @param aoi
	 *            the address of interest
	 * @param index
	 *            the index from where to start
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the binding list
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public BindingList getNodeBindingsSync(final long timeout, final int _requestIdentifier, final Address aoi, final short index, boolean Async) throws IOException, Exception, GatewayException {
		if (aoi.getNetworkAddress() == null && aoi.getIeeeAddress() != null)
			aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (aoi.getIeeeAddress() == null && aoi.getNetworkAddress() != null)
			aoi.setIeeeAddress(getIeeeAddress_FromShortAddress(aoi.getNetworkAddress()));

		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					BindingList _toRes = new BindingList();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							_toRes = DataLayer.getNodeBindings(timeout, aoi, index);
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, _toRes);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, _toRes);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, _toRes);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, _toRes);
					}
				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				BindingList _toRes;
				_toRes = DataLayer.getNodeBindings(timeout, aoi, index);
				return SerializationUtils.clone(_toRes);
			} else
				throw new GatewayException("Gal is not in running state!");

		}

	}

	/**
	 * Adds a binding.
	 * 
	 * @param timeout
	 * @param _requestIdentifier
	 * @param binding
	 *            the binding
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @see Binding
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status addBindingSync(final long timeout, final int _requestIdentifier, final Binding binding, final boolean Async) throws IOException, Exception, GatewayException {
		final Address aoi = new Address();
		aoi.setIeeeAddress(binding.getSourceIEEEAddress());
		aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));

		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							Status _s = DataLayer.addBinding(timeout, binding, aoi);
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, _s);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, _s);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, _s);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifybindingResult(_requestIdentifier, _s);

					}
				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING)

				return SerializationUtils.clone(DataLayer.addBinding(timeout, binding, aoi));
			else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	/**
	 * Removes a binding.
	 * 
	 * @param timeout
	 *            the desired binding
	 * @param _requestIdentifier
	 *            the request identifier
	 * @param binding
	 *            the binding
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @see Binding
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status removeBindingSync(final long timeout, final int _requestIdentifier, final Binding binding, final boolean Async) throws IOException, Exception, GatewayException {
		final Address aoi = new Address();
		aoi.setIeeeAddress(binding.getSourceIEEEAddress());
		aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = DataLayer.removeBinding(timeout, binding, aoi);
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, _s);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, _s);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, _s);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, _s);
					}
				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING)

				return SerializationUtils.clone(DataLayer.removeBinding(timeout, binding, aoi));
			else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	/**
	 * Frequency agility method.
	 * 
	 * @param timeout
	 *            the desired timeout
	 * @param _requestIdentifier
	 *            the request identifier
	 * @param scanChannel
	 *            the channel to scan
	 * @param scanDuration
	 *            the desired duration of the scan
	 * @param Async
	 *            whether the operation will be asynchronous ({@code true}) or
	 *            not ({@code false}).
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status frequencyAgilitySync(final long timeout, final int _requestIdentifier, final short scanChannel, final short scanDuration, final boolean Async) throws IOException, Exception, GatewayException {
		if (Async) {
			executor.execute(new Runnable() {
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _st = DataLayer.frequencyAgilitySync(timeout, scanChannel, scanDuration);
							get_gatewayEventManager().notifyFrequencyAgility(_st);
						} catch (GatewayException e) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage(e.getMessage());
							get_gatewayEventManager().notifyFrequencyAgility(_st);
						} catch (Exception e) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage(e.getMessage());
							get_gatewayEventManager().notifyFrequencyAgility(_st);
						}
					} else {
						Status _st = new Status();
						_st.setCode((short) GatewayConstants.GENERAL_ERROR);
						_st.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyFrequencyAgility(_st);

					}
				}
			});
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				Status _st = DataLayer.frequencyAgilitySync(timeout, scanChannel, scanDuration);
				return SerializationUtils.clone(_st);
			} else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	/**
	 * Tells is the address exists in the network cache.
	 * 
	 * @param shortAddress
	 *            the address to look for.
	 * @return -1 if the address does not exist in network cache or a positive
	 *         number indicating the index of the object on network cache
	 *         otherwise
	 */
	public WrapperWSNNode getFromNetworkCache(WrapperWSNNode nodeToSearch) {
		 synchronized (getNetworkcache()) {
		int index = getNetworkcache().indexOf(nodeToSearch);
		if (index > -1)
			return getNetworkcache().get(index);
		else
			return null;
		 }

	}

	/**
	 * Gets the Ieee Address from network cache.
	 * 
	 * @param shortAddress
	 *            the address of interest.
	 * @return null if the address does not exist in network cache or a positive
	 *         number indicating the index of the desired object
	 * @throws GatewayException
	 */
	public BigInteger getIeeeAddress_FromShortAddress(Integer shortAddress) throws Exception {
		LOG.debug("[getIeeeAddress_FromShortAddress] Start Search Node: {}", String.format("%04X", shortAddress));
		synchronized (getNetworkcache()) {
			for (WrapperWSNNode y : getNetworkcache()) {
				LOG.debug("[getIeeeAddress_FromShortAddress] Short Address: {}", ((y.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", y.get_node().getAddress().getNetworkAddress()) : "NULL") + " - IEEE Address:" + ((y.get_node().getAddress().getIeeeAddress() != null) ? String.format("%016X", y.get_node().getAddress().getIeeeAddress()) : "NULL") + " - - Discovery Completed:" + y.is_discoveryCompleted());

				if (y.is_discoveryCompleted() && y.get_node() != null && y.get_node().getAddress() != null && y.get_node().getAddress().getNetworkAddress() != null && y.get_node().getAddress().getIeeeAddress() != null && y.get_node().getAddress().getNetworkAddress().intValue() == shortAddress.intValue()) {
					LOG.debug("[getIeeeAddress_FromShortAddress] FOUND Node: {}", String.format("%04X", shortAddress));

					if (y.get_node().getAddress().getIeeeAddress() == null)
						throw new Exception("Iee Null on GAL: " + String.format("%04X", shortAddress));
					else
						return BigInteger.valueOf(y.get_node().getAddress().getIeeeAddress().longValue());
				}
			}
			throw new Exception("Short Address not found on GAL: " + String.format("%04X", shortAddress));
		}
	}

	/**
	 * Gets the Short Address from network cache.
	 * 
	 * @param IeeeAddress
	 *            the address of interest.
	 * @return null if the address does not exist in network cache or a positive
	 *         number indicating the index of the desired object
	 * @throws GatewayException
	 */
	public Integer getShortAddress_FromIeeeAddress(BigInteger IeeeAddress) throws Exception {
		LOG.debug("[getShortAddress_FromIeeeAddress] Start Search Node: {}", String.format("%016X", IeeeAddress));
		synchronized (getNetworkcache()) {
			for (WrapperWSNNode y : getNetworkcache()) {
				LOG.debug("[getShortAddress_FromIeeeAddress] Short Address: {}", ((y.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", y.get_node().getAddress().getNetworkAddress()) : "NULL") + " - IEEE Address:" + ((y.get_node().getAddress().getIeeeAddress() != null) ? String.format("%016X", y.get_node().getAddress().getIeeeAddress()) : "NULL") + " - - Discovery Completed:" + y.is_discoveryCompleted());
				if (y.is_discoveryCompleted() && (y.get_node() != null) && (y.get_node().getAddress() != null) && (y.get_node().getAddress().getIeeeAddress() != null) && (y.get_node().getAddress().getNetworkAddress() != null) && y.get_node().getAddress().getIeeeAddress().longValue() == IeeeAddress.longValue()) {
					LOG.debug("[getShortAddress_FromIeeeAddress] FOUND Node: {} ", String.format("%016X", IeeeAddress));

					if (y.get_node().getAddress().getNetworkAddress() == null)
						throw new Exception("Shoort Address null on GAL: " + String.format("%016X", IeeeAddress));
					else
						return new Integer(y.get_node().getAddress().getNetworkAddress());
				}
			}
			throw new Exception("Ieee Address not found on GAL: " + String.format("%016X", IeeeAddress));
		}
	}

	/**
	 * Tells is the address exists into Gateway Event Listener's list.
	 * 
	 * @param requestIdentifier
	 *            the request identifier to look for.
	 * @return -1 if the request identifier does not exist into Gateway Event
	 *         Listener's list or a positive number indicating its index onto
	 *         the list otherwise
	 */
	public short existIntolistGatewayEventListener(long requestIdentifier) {
		synchronized (getListGatewayEventListener()) {
			short __indexOnList = -1;
			for (GatewayDeviceEventEntry y : getListGatewayEventListener()) {
				__indexOnList++;
				if (y.getProxyIdentifier() == requestIdentifier)
					return __indexOnList;
			}
			return -1;
		}
	}

	private void resetGateway() throws Exception{
		if (DataLayer != null) {
			LOG.error("Starting reset...");
			/* Stop all timers */
			synchronized (getNetworkcache()) {
				for (Iterator<WrapperWSNNode> it= getNetworkcache().iterator();it.hasNext();) {
					WrapperWSNNode x=it.next();
					x.abortTimers();
				}
			}
			LOG.error("Stopped all timers");
			
			List<WrapperWSNNode> wsnWrappers=getNetworkcache();
			Iterator<WrapperWSNNode> wsnWrappersIterator=wsnWrappers.iterator();
			
			//remove all nodes from the cache and notify network manager
			while(wsnWrappersIterator.hasNext())
			{
				WrapperWSNNode nodeWrapper = wsnWrappersIterator.next();
				//Clear device keypair
				try{
					Status _st1 = getDataLayer().ClearDeviceKeyPairSet(getPropertiesManager().getCommandTimeoutMS(),
							nodeWrapper.get_node().getAddress());
				}catch(Exception e){
					LOG.error("Error ong Clearing device Keyset for device {} - Exception: {}",
							Utils.getAddressString(nodeWrapper.get_node().getAddress()),
							e);
				}
				
				//Clear neighbor table entries
				try{
					Status _st0 = getDataLayer().ClearNeighborTableEntry(getPropertiesManager().getCommandTimeoutMS(),
							nodeWrapper.get_node().getAddress());
				} catch (Exception e1) {
					LOG.error("Error on ClearNeighborTableEntry for node: {} - Exception: {}", 
							Utils.getAddressString(nodeWrapper.get_node().getAddress()), 
							e1); 
				}
				
				//notify the networkmanager of node removal
				Status s = new Status();
				s.setCode((short) GatewayConstants.SUCCESS);
				try{
					this.get_gatewayEventManager().nodeRemoved(s, nodeWrapper.get_node());
				}catch(Exception e){
					LOG.error("Error notifying node {} removal, Exception: {}",
							Utils.getAddressString(nodeWrapper.get_node().getAddress()),
							e);
				}
				
				
			}
			
			getNetworkcache().clear();
			
			/* Stop discovery and freshness */

			/* Destroy Gal Node */
			set_GalNode(null);

			setGatewayStatus(GatewayStatus.GW_READY_TO_START);

			LOG.error("Now Gateway have been set as ready to start and the GalNode have been set to null");
			
			if (DataLayer.getIKeyInstance().isConnected())
			{
				LOG.error("DataLayer instance was connected, disconnecting");
				DataLayer.getIKeyInstance().disconnect();
			}
			LOG.error("Destroying DataLayer");
			DataLayer.destroy();
			LOG.debug("Reset done!");
		}
	}

}
