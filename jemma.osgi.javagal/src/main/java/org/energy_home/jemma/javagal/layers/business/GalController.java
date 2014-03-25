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

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.implementations.ApsManager;
import org.energy_home.jemma.javagal.layers.business.implementations.Discovery_Freshness_ForcePing;
import org.energy_home.jemma.javagal.layers.business.implementations.GatewayEventManager;
import org.energy_home.jemma.javagal.layers.business.implementations.ZdoManager;
import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.CallbackEntry;
import org.energy_home.jemma.javagal.layers.object.GatewayDeviceEventEntry;
import org.energy_home.jemma.javagal.layers.object.GatewayStatus;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.MyThread;
import org.energy_home.jemma.javagal.layers.object.NeighborTableLis_Record;
import org.energy_home.jemma.javagal.layers.object.ParserLocker;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
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

/**
 * Actual JavaGal Controller. Only one instance of this object can exists at a
 * time. All clients can access this instance via their dedicated proxies (see
 * {@link org.energy_home.jemma.zgd.GalExtenderProxy}).
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */

public class GalController {
	final int startTimeFirstFreshness = 15;
	final int startTimeFirstForcePing = 16;

	private GatewayStatus _gatewayStatus = GatewayStatus.GW_READY_TO_START;
	private Long apsCallbackIdentifier = (long) 1;
	private List<WrapperWSNNode> NetworkCache = Collections.synchronizedList(new LinkedList<WrapperWSNNode>());
	private List<CallbackEntry> listCallback = Collections.synchronizedList(new LinkedList<CallbackEntry>());
	private List<GatewayDeviceEventEntry> listGatewayEventListener = Collections.synchronizedList(new LinkedList<GatewayDeviceEventEntry>());
	private final static Log logger = LogFactory.getLog(GalController.class);
	private ApsManager apsManager = null;

	private ZdoManager zdoManager = null;
	private GatewayEventManager _gatewayEventManager = null;
	private Boolean _Gal_in_Dyscovery_state = false;

	private ParserLocker _lockerStartDevice;
	private IDataLayer DataLayer = null;
	private Discovery_Freshness_ForcePing _discoveryManager = null;
	PropertiesManager PropertiesManager = null;

	/**
	 * Initialize the DataLayer class, with the relative RS-232 conection Used,
	 * also for the Rest Api
	 */
	private void initializeGAL() throws Exception {
		/* Used for reset GAL */
		if (DataLayer != null) {
			if (getPropertiesManager().getDebugEnabled())
				logger.info("Starting reset...");
			/* Stop all timers */
			for (WrapperWSNNode x : getNetworkcache())
				x.abortTimers();
			getNetworkcache().clear();
			/* Stop discovery and freshness */

			/* Destroy Gal Node */
			set_GalNode(null);

			setGatewayStatus(GatewayStatus.GW_READY_TO_START);

			if (DataLayer.getIKeyInstance().isConnected())
				DataLayer.getIKeyInstance().disconnect();
			DataLayer = null;
			if (getPropertiesManager().getDebugEnabled())
				logger.info("Reset done!");
		}
		/* End of reset section */
		if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
			DataLayer = new DataFreescale(this);
			try {

				DataLayer.getIKeyInstance().initialize();
			} catch (Exception e) {
				DataLayer.getIKeyInstance().disconnect();
				throw e;
			}
		} else
			try {
				throw new Exception("No Platform found!");
			} catch (Exception e) {
				if (getPropertiesManager().getDebugEnabled())
					logger.error(e.getMessage());
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

					logger.error("Error on autostart!");
				}
			} else {
				short _EndPoint = 0;
				_EndPoint = DataLayer.configureEndPointSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
				if (_EndPoint == 0)
					throw new Exception("Error on configure endpoint");

			}
		}

		if (getPropertiesManager().getDebugEnabled())
			logger.info("***Gateway is ready now... Current GAL Status: " + getGatewayStatus().toString() + "***");

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
		apsManager = new ApsManager(this);
		_gatewayEventManager = new GatewayEventManager(this);

		_lockerStartDevice = new ParserLocker();
		_discoveryManager = new Discovery_Freshness_ForcePing(this);

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

	public synchronized List<GatewayDeviceEventEntry> getListGatewayEventListener() {
		return listGatewayEventListener;
	}

	/**
	 * Gets the list of registered callbacks. The callbacks are registered in a
	 * {@code CallbackEntry} acting as a convenient container.
	 * 
	 * @return the list of registered callbacks.
	 * @see CallbackEntry
	 */
	public synchronized List<CallbackEntry> getCallbacks() {
		return listCallback;
	}

	/**
	 * Gets a discovery manager.
	 * 
	 * @return the discovery manager.
	 */
	public synchronized Discovery_Freshness_ForcePing getDiscoveryManager() {
		return _discoveryManager;
	}

	/**
	 * Gets the Aps manager.
	 * 
	 * @return the Aps manager.
	 */
	public synchronized ApsManager getApsManager() {
		return apsManager;
	}

	/**
	 * Gets the Zdo manager.
	 * 
	 * @return the Zdo manager.
	 */
	public synchronized ZdoManager getZdoManager() {
		return zdoManager;
	}

	/**
	 * Gets the actual data layer implementation.
	 * 
	 * @return the actual data layer implementation.
	 */
	public synchronized IDataLayer getDataLayer() {
		return DataLayer;
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
		// TODO 30
		if ((desc.getApplicationInputCluster().size() + desc.getApplicationOutputCluster().size()) > 30) {
			throw new Exception("Simple Descriptor Out Of Memory");
		} else {
			short result = DataLayer.configureEndPointSync(timeout, desc);
			return result;
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
		NodeServices result = DataLayer.getLocalServices();
		if (GalNode != null && GalNode.get_node().getAddress() != null)
			result.setAddress(GalNode.get_node().getAddress());
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode o : _list) {
			if (o.get_node().getAddress().getNetworkAddress() == get_GalNode().get_node().getAddress().getNetworkAddress()) {
				o.set_nodeServices(result);
				result = o.get_nodeServices();
				break;
			}
		}
		return result;
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
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode o : _list) {
			if (o.get_nodeServices() != null)
				list.getNodeServices().add(o.get_nodeServices());
		}
		return list;
	}

	/**
	 * Autostart's execution.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void executeAutoStart() throws Exception {
		logger.info("Executing AutoStart procedure...");
		short _EndPoint = DataLayer.configureEndPointSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
		if (_EndPoint > 0x00) {
			logger.info("Configure EndPoint completed...");
			Status _statusStartGatewayDevice = DataLayer.startGatewayDeviceSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSturtupAttributeInfo());
			if (_statusStartGatewayDevice.getCode() == 0x00) {
				logger.info("StartGateway Device completed...");
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
	public synchronized WSNNodeList readNodeCache() {
		WSNNodeList _list = new WSNNodeList();
		List<WrapperWSNNode> _list0 = getNetworkcache();

		for (WrapperWSNNode x : _list0) {
			if (x.is_discoveryCompleted())
				_list.getWSNNode().add(x.get_node());
			if (PropertiesManager.getDebugEnabled())
				System.out.println(x.get_node().getAddress().getNetworkAddress() + "-" + x.is_discoveryCompleted());
		}

		return _list;
	}

	/**
	 * Returns the list of associated nodes in the network, and for each node
	 * gives the short and the IEEE Address
	 * 
	 * @return the list of associated nodes in the network.
	 */
	public synchronized Aliases listAddress() {
		Aliases _list = new Aliases();

		long counter = 0;
		List<WrapperWSNNode> _list1 = getNetworkcache();
		for (WrapperWSNNode x : _list1) {
			if (x.is_discoveryCompleted()) {
				if (x.get_node().getAddress().getIeeeAddress() == null)
					x.get_node().getAddress().setIeeeAddress(getIeeeAddress_FromNetworkCache(x.get_node().getAddress().getNetworkAddress()));
				_list.getAlias().add(x.get_node().getAddress());
				counter++;
			}
		}
		_list.setNumberOfAlias(counter);
		return _list;
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
		int _index = -1;
		if ((_index = existIntoNetworkCache(aoi.getNetworkAddress())) > -1) {
			WrapperWSNNode x = getNetworkcache().get(_index);
			if (x.is_discoveryCompleted()) {
				LQINode _lqinode = new LQINode();
				Mgmt_LQI_rsp _rsp = x.get_Mgmt_LQI_rsp();
				_lqinode.setNodeAddress(x.get_node().getAddress().getIeeeAddress());

				if (_rsp != null && _rsp.NeighborTableList != null) {
					for (NeighborTableLis_Record _n1 : _rsp.NeighborTableList) {
						Neighbor e = new Neighbor();
						e.setDepth((short) _n1._Depth);
						e.setDeviceTypeRxOnWhenIdleRelationship(_n1._RxOnWhenIdle);
						Integer _shortAddress = getShortAddress_FromNetworkCache(BigInteger.valueOf(_n1._Extended_Address));
						if (_shortAddress != null)
							e.setShortAddress(_shortAddress);
						else
							continue;
						e.setIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
						e.setLQI((short) _n1._LQI);
						e.setExtendedPANId(BigInteger.valueOf(_n1._Extended_PAN_Id));
						e.setPermitJoining((short) _n1._Permitting_Joining);
						_lqinode.getNeighborList().getNeighbor().add(e);
					}
				}

				_lqi.getLQINode().add(_lqinode);
			}
			return _lqi;

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
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode x : _list) {

			logger.info("Node:" + x.get_node().getAddress().getNetworkAddress() + "DiscoveryCompleted:" + x.is_discoveryCompleted());
			if (x.is_discoveryCompleted()) {
				LQINode _lqinode = new LQINode();
				Mgmt_LQI_rsp _rsp = x.get_Mgmt_LQI_rsp();
				_lqinode.setNodeAddress(x.get_node().getAddress().getIeeeAddress());

				if (_rsp != null && _rsp.NeighborTableList != null) {

					NeighborList _list0 = new NeighborList();
					for (NeighborTableLis_Record _n1 : _rsp.NeighborTableList) {
						Neighbor e = new Neighbor();
						e.setDepth((short) _n1._Depth);
						e.setDeviceTypeRxOnWhenIdleRelationship(_n1._Device_Type_RxOnWhenIdle_Relationship);
						Integer _shortAddress = getShortAddress_FromNetworkCache(BigInteger.valueOf(_n1._Extended_Address));
						if (_shortAddress != null)
							e.setShortAddress(_shortAddress);
						else {

							if (PropertiesManager.getDebugEnabled()) {
								logger.error("Not found node with the IEEE:" + _n1._Extended_Address);
							}

							continue;
						}
						e.setIeeeAddress(BigInteger.valueOf(_n1._Extended_Address));
						e.setExtendedPANId(BigInteger.valueOf(_n1._Extended_PAN_Id));
						e.setPermitJoining((short) _n1._Permitting_Joining);
						e.setLQI((short) _n1._LQI);
						_list0.getNeighbor().add(e);
						_lqinode.setNeighborList(_list0);
					}
				}
				_lqi.getLQINode().add(_lqinode);
			}
		}
		return _lqi;
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

		if (Async) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {

							NodeDescriptor _node = DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, _node);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, _node, addrOfInterest);

						} catch (IOException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, null);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, null, addrOfInterest);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, null);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, null, addrOfInterest);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, null);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, null, addrOfInterest);

						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, null);
						get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, null, addrOfInterest);

					}

				}
			};
			thr.start();
			return null;

		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING)

				return DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);
			else
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
			Thread thr = new Thread() {
				@Override
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START || getGatewayStatus() == GatewayStatus.GW_STOPPED) {

						setGatewayStatus(GatewayStatus.GW_STARTING);
						try {
							Status _res = DataLayer.startGatewayDeviceSync(timeout, sai);
							if (_res.getCode() == GatewayConstants.SUCCESS) {

								synchronized (_lockerStartDevice) {
									try {
										_lockerStartDevice.setId(0);
										_lockerStartDevice.wait(timeout);

									} catch (InterruptedException e) {

									}
								}
								if (_lockerStartDevice.getId() > 0) {
									if (PropertiesManager.getDebugEnabled())
										logger.info("Gateway Started now!");

								} else {
									setGatewayStatus(GatewayStatus.GW_READY_TO_START);
									if (PropertiesManager.getDebugEnabled())
										logger.error("*******Gateway NOT Started!");
									_res.setCode((short) GatewayConstants.GENERAL_ERROR);
									_res.setMessage("No Network Event Running received!");

								}
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
						if (PropertiesManager.getDebugEnabled()) {
							logger.info(message);
						}
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, _s);

					}
				}
			};
			thr.start();
			return null;
		} else {
			Status _status;
			if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START || getGatewayStatus() == GatewayStatus.GW_STOPPED) {
				setGatewayStatus(GatewayStatus.GW_STARTING);

				_status = DataLayer.startGatewayDeviceSync(timeout, sai);

				if (_status.getCode() == GatewayConstants.SUCCESS) {

					synchronized (_lockerStartDevice) {
						try {
							_lockerStartDevice.setId(0);
							_lockerStartDevice.wait(timeout);

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (_lockerStartDevice.getId() > 0) {
						if (PropertiesManager.getDebugEnabled())
							logger.info("Gateway Started now!");
					} else {
						setGatewayStatus(GatewayStatus.GW_READY_TO_START);
						if (PropertiesManager.getDebugEnabled())
							logger.error("Gateway NOT Started!");
						_status.setCode((short) GatewayConstants.GENERAL_ERROR);
						_status.setMessage("No Network Event Running received!");
					}
				}

				get_gatewayEventManager().notifyGatewayStartResult(_status);
			} else {
				// ...from all others, throw an exception
				String message = "Trying to start Gateway Device in " + getGatewayStatus() + " state.";
				if (PropertiesManager.getDebugEnabled()) {
					logger.info(message);
				}
				throw new GatewayException(message);
			}
			return _status;

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
			Thread thr = new Thread() {
				@Override
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
			};
			thr.setName("Gateway reset Thread");
			thr.start();
			return null;
		} else {

			Status _s = new Status();
			_s.setCode((short) GatewayConstants.SUCCESS);
			_s.setMessage("Reset Done");
			initializeGAL();
			get_gatewayEventManager().notifyResetResult(_s);
			return _s;
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
			Thread thr = new Thread() {
				@Override
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
						if (PropertiesManager.getDebugEnabled()) {
							logger.info(message);
						}
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, _s);

					}
				}
			};
			thr.start();
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
			return _status;

		}
	}

	public String APSME_GETSync(short attrId) throws Exception, GatewayException {
		return DataLayer.APSME_GETSync(PropertiesManager.getCommandTimeoutMS(), attrId);
	}

	public void APSME_SETSync(short attrId, String value) throws Exception, GatewayException {
		DataLayer.APSME_SETSync(PropertiesManager.getCommandTimeoutMS(), attrId, value);
	}

	public String NMLE_GetSync(short ilb) throws IOException, Exception, GatewayException {
		return DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), ilb);

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
	public long createCallback(int proxyIdentifier, Callback callback, APSMessageListener listener) throws IOException, Exception, GatewayException {
		CallbackEntry callbackEntry = new CallbackEntry();
		callbackEntry.setCallback(callback);
		callbackEntry.setDestination(listener);
		callbackEntry.setProxyIdentifier(proxyIdentifier);
		long id = getApsCallbackIdentifier();
		callbackEntry.setApsCallbackIdentifier(id);
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
			if (x.getApsCallbackIdentifier().equals(id)) {
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

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						List<Short> _s = null;
						try {
							_s = DataLayer.startServiceDiscoverySync(timeout, aoi);
							Status _ok = new Status();
							_ok.setCode((short) 0x00);
							if (aoi.getIeeeAddress() == null)
								aoi.setIeeeAddress(getIeeeAddress_FromNetworkCache(aoi.getNetworkAddress()));

							NodeServices _newNodeService = new NodeServices();
							_newNodeService.setAddress(aoi);
							if (_newNodeService.getAddress().getIeeeAddress() == null)
								_newNodeService.getAddress().setIeeeAddress(getIeeeAddress_FromNetworkCache(_newNodeService.getAddress().getNetworkAddress()));

							for (Short x : _s) {
								ActiveEndpoints _n = new ActiveEndpoints();
								_n.setEndPoint(x);
								_newNodeService.getActiveEndpoints().add(_n);
							}
							int _index = -1;
							if ((_index = existIntoNetworkCache(aoi.getNetworkAddress())) == -1) {
								getNetworkcache().get(_index).set_nodeServices(_newNodeService);
							}
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _ok, _newNodeService);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, null);
						} catch (GatewayException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, null);
						} catch (Exception e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, null);
						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state");
						get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _s1, null);

					}
				}

			};

			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				List<Short> _result = DataLayer.startServiceDiscoverySync(timeout, aoi);

				NodeServices _newNodeService = new NodeServices();
				_newNodeService.setAddress(aoi);

				if (_newNodeService.getAddress().getIeeeAddress() == null)
					_newNodeService.getAddress().setIeeeAddress(getIeeeAddress_FromNetworkCache(_newNodeService.getAddress().getNetworkAddress()));

				for (Short x : _result) {
					ActiveEndpoints _n = new ActiveEndpoints();
					_n.setEndPoint(x);
					_newNodeService.getActiveEndpoints().add(_n);
				}
				int _index = -1;
				if ((_index = existIntoNetworkCache(aoi.getNetworkAddress())) > -1) {
					getNetworkcache().get(_index).set_nodeServices(_newNodeService);
				}

				return _newNodeService;
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
				toReturn.getCallbackIdentifier().add(ce.getApsCallbackIdentifier());
		}
		return toReturn;
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
		for (int i = 0; i < getListGatewayEventListener().size(); i++) {
			if (getListGatewayEventListener().get(i).getProxyIdentifier() == proxyIdentifier) {
				_listenerFound = true;
				break;
			}

		}

		if (!_listenerFound) {
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

			if (message.getDestinationAddress().getIeeeAddress() != null && message.getDestinationAddress().getNetworkAddress() == null)
				message.getDestinationAddress().setNetworkAddress(getShortAddress_FromNetworkCache(message.getDestinationAddress().getIeeeAddress()));
			DataLayer.sendApsSync(timeout, message);
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
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					Status _s = null;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						if (!addrOfInterest.getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {
							try {
								_s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
								if (_s.getCode() == GatewayConstants.SUCCESS) {
									/* Broadcast address */
									if ((addrOfInterest.getNetworkAddress() == 0xFFFF) || (addrOfInterest.getNetworkAddress() == 0xFFFC)) {
										/* Clear the Network Cache */
										List<WrapperWSNNode> _list = getNetworkcache();
										for (WrapperWSNNode x : _list) {
											x.abortTimers();
											if (!x.get_node().getAddress().getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress()))
												get_gatewayEventManager().nodeRemoved(_s, x.get_node());
										}
										getNetworkcache().clear();
										getNetworkcache().add(GalNode);
										getNetworkcache().get(0).setTimerForcePing(getPropertiesManager().getForcePingTimeout());
										getNetworkcache().get(0).setTimerFreshness(getPropertiesManager().getKeepAliveThreshold());

									} else /* Single address */
									{
										/* get the node from cache */
										int index = existIntoNetworkCache(addrOfInterest.getNetworkAddress());
										if (index > -1) {
											WrapperWSNNode _wrapper = getNetworkcache().get(index);
											_wrapper.abortTimers();
											get_gatewayEventManager().nodeRemoved(_s, _wrapper.get_node());
											getNetworkcache().remove(index);

										}
									}
								}

								get_gatewayEventManager().notifyleaveResult(_s);
								get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);

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

			};

			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				if (!addrOfInterest.getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {

					Status _s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
					if (_s.getCode() == GatewayConstants.SUCCESS) {
						/* Broadcast Address */
						if ((addrOfInterest.getNetworkAddress() == 0xFFFF) || (addrOfInterest.getNetworkAddress() == 0xFFFC)) {
							/* Clear the Network Cache */
							List<WrapperWSNNode> _list = getNetworkcache();
							for (WrapperWSNNode x : _list) {
								x.abortTimers();
								if (!x.get_node().getAddress().getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress()))
									get_gatewayEventManager().nodeRemoved(_s, x.get_node());
							}
							getNetworkcache().clear();
							getNetworkcache().add(GalNode);
							getNetworkcache().get(0).setTimerForcePing(getPropertiesManager().getForcePingTimeout());
							getNetworkcache().get(0).setTimerFreshness(getPropertiesManager().getKeepAliveThreshold());

						} else /* Broadcast Address */
						{
							/* get the node from cache */
							int index = existIntoNetworkCache(addrOfInterest.getNetworkAddress());
							if (index > -1) {
								WrapperWSNNode _wrapper = getNetworkcache().get(index);
								_wrapper.abortTimers();
								get_gatewayEventManager().nodeRemoved(_s, _wrapper.get_node());
								getNetworkcache().remove(index);

							}
						}
					}

					get_gatewayEventManager().notifyleaveResult(_s);
					get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);
					return _s;
				} else
					throw new GatewayException("Is not possible Leave the GAL!");
			} else
				throw new GatewayException("Gal is not in running state!");
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
			Thread thr = new Thread() {
				@Override
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

			};

			thr.start();
			return null;
		} else {
			Status _s;
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				try {
					_s = DataLayer.permitJoinSync(timeout, addrOfInterest, duration, (byte) 0x00);
					get_gatewayEventManager().notifypermitJoinResult(_s);
					return _s;
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
			Thread thr = new Thread() {
				@Override
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
			};

			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				Status _s = DataLayer.permitJoinAllSync(timeout, _add, duration, (byte) 0x00);
				get_gatewayEventManager().notifypermitJoinResult(_s);
				return _s;
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
		if (PropertiesManager.getDebugEnabled())
			logger.info("Called startNodeDiscovery Mask: " + discoveryMask + " - Timeout:" + timeout);
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
				List<WrapperWSNNode> _list = getNetworkcache();
				for (WrapperWSNNode x : _list) {
					x.abortTimers();
				}
				getNetworkcache().clear();
				getNetworkcache().add(GalNode);
				getNetworkcache().get(0).set_discoveryCompleted(false);
				getNetworkcache().get(0).setTimerForcePing(getPropertiesManager().getForcePingTimeout());
				getNetworkcache().get(0).setTimerFreshness(getPropertiesManager().getKeepAliveThreshold());
				long __timeout = 0;
				if (timeout == 0)
					timeout = GatewayConstants.INFINITE_TIMEOUT;
				__timeout = timeout / 1000 + ((timeout % 1000 > 0) ? 1 : 0);
				/* Only one element (GALNode) */
				synchronized (GalNode) {
					getNetworkcache().get(0).setTimerDiscovery(0);
				}
				if (PropertiesManager.getDebugEnabled()) {
					logger.info("Global Discovery Started(" + __timeout + " seconds)!");
				}
			} else if ((discoveryMask == GatewayConstants.DISCOVERY_STOP) || (timeout == 1)) {

				if (PropertiesManager.getDebugEnabled())
					logger.info("Global Discovery Stopped!");

			}
		} else {
			if (PropertiesManager.getDebugEnabled())
				logger.error("Error on startNodeDiscovery: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
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
			if (PropertiesManager.getDebugEnabled()) {
				logger.error("Error on subscribeNodeRemoval: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
			}
			throw new GatewayException("No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
		}

	}

	/**
	 * Gets the Gateway Status.
	 * 
	 * @return the gateway status
	 * @see GatewayStatus
	 */
	public synchronized GatewayStatus getGatewayStatus() {
		return _gatewayStatus;
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
		v.setVersionIdentifier((short) 0x01);
		v.setFeatureSetIdentifier((short) 0x00);
		String version = GalController.class.getPackage().getImplementationVersion();
		if (version != null) {
			v.setManufacturerVersion(GalController.class.getPackage().getImplementationVersion());
			v.setVersionIdentifier(Short.parseShort(GalController.class.getPackage().getImplementationVersion()));
		}
		v.getRPCProtocol().add(RPCProtocol.REST);
		return v;
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
			Runnable thr = new MyThread(this) {

				@Override
				public void run() {
					String _NetworkAdd = null;
					BigInteger _IeeeAdd = null;
					/* Read the ShortAddress of the GAL */
					try {
						_NetworkAdd = DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), (short) 0x96);
						System.out.println("Readed Network Addres of Gal: " + _NetworkAdd);
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal Network Address!");
						}
						return;
					}
					/* Read the IEEEAddress of the GAL */
					try {
						_IeeeAdd = DataLayer.readExtAddressGal(PropertiesManager.getCommandTimeoutMS());
						System.out.println("Readed IEEE Addres of Gal: " + _IeeeAdd);

					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal IEEE Address!");
						}
						return;
					}

					WrapperWSNNode galNodeWrapper = new WrapperWSNNode(((GalController) this.getParameter()));
					WSNNode galNode = new WSNNode();
					Address _add = new Address();
					_add.setNetworkAddress(Integer.parseInt(_NetworkAdd, 16));
					_add.setIeeeAddress(_IeeeAdd);
					galNode.setAddress(_add);
					galNodeWrapper.set_node(galNode);

					/* Read the NodeDescriptor of the GAL */
					try {
						NodeDescriptor _NodeDescriptor = DataLayer.getNodeDescriptorSync(PropertiesManager.getCommandTimeoutMS(), _add);
						if (_NodeDescriptor != null) {
							if (galNodeWrapper.get_node().getCapabilityInformation() == null)
								galNodeWrapper.get_node().setCapabilityInformation(new MACCapability());
							galNodeWrapper.get_node().getCapabilityInformation().setReceiverOnWhenIdle(_NodeDescriptor.getMACCapabilityFlag().isReceiverOnWhenIdle());
							galNodeWrapper.get_node().getCapabilityInformation().setAllocateAddress(_NodeDescriptor.getMACCapabilityFlag().isAllocateAddress());
							galNodeWrapper.get_node().getCapabilityInformation().setAlternatePanCoordinator(_NodeDescriptor.getMACCapabilityFlag().isAlternatePanCoordinator());
							galNodeWrapper.get_node().getCapabilityInformation().setDeviceIsFFD(_NodeDescriptor.getMACCapabilityFlag().isDeviceIsFFD());
							galNodeWrapper.get_node().getCapabilityInformation().setMainsPowered(_NodeDescriptor.getMACCapabilityFlag().isMainsPowered());
							galNodeWrapper.get_node().getCapabilityInformation().setSecuritySupported(_NodeDescriptor.getMACCapabilityFlag().isSecuritySupported());
							galNodeWrapper.reset_numberOfAttempt();
							galNodeWrapper.set_discoveryCompleted(true);
							int _index = -1;

							/* If the Node Not Exists */
							if ((_index = existIntoNetworkCache(_add.getNetworkAddress())) == -1) {
								getNetworkcache().add(galNodeWrapper);
							}
							/* The GAl node is already present into the DB */
							else {
								getNetworkcache().get(_index).abortTimers();
								getNetworkcache().get(_index).set_node(galNodeWrapper.get_node());
							}

							System.out.println("Readed Node Descriptor of Gal.");

						} else {
							System.out.println("ERROR on Read Node Descriptor of Gal.");
							if (PropertiesManager.getDebugEnabled()) {
								logger.error("ERROR on Read Node Descriptor of Gal.!");
							}
						}

					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal Node Descriptor!");
						}
						return;
					}

					/* Executing the command(PermitJoin==0) to close network */
					try {
						Status _permitjoin = DataLayer.permitJoinSync(PropertiesManager.getCommandTimeoutMS(), _add, (short) 0x00, (byte) 0x01);
						if (_permitjoin.getCode() != GatewayConstants.SUCCESS) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage("Error on permitJoin(0) for the GAL node on startup!");

							get_gatewayEventManager().notifyGatewayStartResult(_st);
							System.out.println("Permit join close GAL executed.");
							System.out.println("Sent NetworkStart event");

						}
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal Node Descriptor!");
						}
						return;
					}
					if (!galNodeWrapper.isSleepy()) {
						/* If the Node is NOT a sleepyEndDevice */

						if (PropertiesManager.getKeepAliveThreshold() > 0) {
							/* Execute the Freshness */
							galNodeWrapper.setTimerFreshness(startTimeFirstFreshness);
						}
						if (PropertiesManager.getForcePingTimeout() > 0) {
							/* Execute the ForcePing */
							galNodeWrapper.setTimerForcePing(startTimeFirstForcePing);
						}

					}

					set_GalNode(galNodeWrapper);
					/* Notify Gal Node */
					Status _s = new Status();
					_s.setCode((short) 0x00);
					try {
						get_gatewayEventManager().nodeDiscovered(_s, galNodeWrapper.get_node());
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error calling nodeDiscovered for the GAL node!");
						}
					}

					synchronized (_lockerStartDevice) {
						_lockerStartDevice.setId(1);
						_lockerStartDevice.notify();
					}
					_gatewayStatus = gatewayStatus;
				}
			};

			new Thread(thr).start();

		} else if (gatewayStatus == GatewayStatus.GW_STOPPED) {
			/* Stop Discovery */
			if (PropertiesManager.getDebugEnabled()) {
				logger.info("Stopping Discovery and Freshness procedures...");
			}
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
	public long getApsCallbackIdentifier() {
		synchronized (this) {
			if (apsCallbackIdentifier == Long.MAX_VALUE) {
				apsCallbackIdentifier = (long) 1;
			}
			return apsCallbackIdentifier++;
		}
	}

	/**
	 * Gets the Gal node.
	 * 
	 * @return the gal node.
	 * @see WrapperWSNNode
	 */
	public synchronized WrapperWSNNode get_GalNode() {
		return GalNode;
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
		Status _s = DataLayer.clearEndpointSync(endpoint);
		return _s;
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
	public synchronized List<WrapperWSNNode> getNetworkcache() {
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
		if (addrOfInterest.getNetworkAddress() == null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromNetworkCache(addrOfInterest.getIeeeAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					ServiceDescriptor _toRes;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							_toRes = DataLayer.getServiceDescriptor(timeout, addrOfInterest, endpoint);
							_toRes.getAddress().setIeeeAddress(getIeeeAddress_FromNetworkCache(_toRes.getAddress().getNetworkAddress()));
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, _toRes);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, null);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, null);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, _s, null);
					}
				}
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				ServiceDescriptor _toRes;
				_toRes = DataLayer.getServiceDescriptor(timeout, addrOfInterest, endpoint);
				if (_toRes.getAddress().getIeeeAddress() == null)
					_toRes.getAddress().setIeeeAddress(getIeeeAddress_FromNetworkCache(_toRes.getAddress().getNetworkAddress()));
				return _toRes;
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
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					BindingList _toRes;
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
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, null);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, null);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, null);
					}
				}
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				BindingList _toRes;
				_toRes = DataLayer.getNodeBindings(timeout, aoi, index);
				return _toRes;
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
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							Status _s = DataLayer.addBinding(timeout, binding);
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
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING)

				return DataLayer.addBinding(timeout, binding);
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
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = DataLayer.removeBinding(timeout, binding);
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
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING)

				return DataLayer.removeBinding(timeout, binding);
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
			Thread thr = new Thread() {
				@Override
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
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				Status _st = DataLayer.frequencyAgilitySync(timeout, scanChannel, scanDuration);
				return _st;
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
	public synchronized short existIntoNetworkCache(Integer shortAddress) {
		short __indexOnCache = -1;
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode y : _list) {
			__indexOnCache++;
			if (y.get_node().getAddress().getNetworkAddress().equals(shortAddress))
				return __indexOnCache;

		}
		return -1;
	}

	/**
	 * Gets the Ieee Address from network cache.
	 * 
	 * @param shortAddress
	 *            the address of interest.
	 * @return null if the address does not exist in network cache or a positive
	 *         number indicating the index of the desired object
	 */
	public synchronized BigInteger getIeeeAddress_FromNetworkCache(Integer shortAddress) {
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode y : _list) {
			if (y.get_node().getAddress().getNetworkAddress().equals(shortAddress))
				return y.get_node().getAddress().getIeeeAddress();
		}
		return null;
	}

	/**
	 * Gets the Short Address from network cache.
	 * 
	 * @param IeeeAddress
	 *            the address of interest.
	 * @return null if the address does not exist in network cache or a positive
	 *         number indicating the index of the desired object
	 */
	public synchronized Integer getShortAddress_FromNetworkCache(BigInteger IeeeAddress) {
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode y : _list) {
			if (y.get_node().getAddress().getIeeeAddress().equals(IeeeAddress))
				return y.get_node().getAddress().getNetworkAddress();
		}
		return null;
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
	public synchronized short existIntolistGatewayEventListener(long requestIdentifier) {
		short __indexOnList = -1;
		for (GatewayDeviceEventEntry y : listGatewayEventListener) {
			__indexOnList++;
			if (y.getProxyIdentifier() == requestIdentifier)
				return __indexOnList;

		}
		return -1;
	}

}
