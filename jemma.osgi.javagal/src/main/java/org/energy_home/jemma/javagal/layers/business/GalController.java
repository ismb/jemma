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

import org.apache.commons.lang3.SerializationUtils;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.implementations.*;
import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.*;
import org.energy_home.jemma.zgd.*;
import org.energy_home.jemma.zgd.jaxb.*;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * JavaGal Controller. Only one instance of this object can exists at a
 * time. All clients can access this instance via their dedicated proxies (see
 * {@link org.energy_home.jemma.zgd.GalExtenderProxy}).
 * 
 * @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */

public class GalController {
	private GatewayStatus _gatewayStatus = GatewayStatus.GW_READY_TO_START;
	private Long CallbackIdentifier = (long) 1;
	private List<WrapperWSNNode> NetworkCache = Collections.synchronizedList(new LinkedList<WrapperWSNNode>());
	private final List<CallbackEntry> listCallback = Collections.synchronizedList(new LinkedList<CallbackEntry>());
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

	private final ParserLocker _lockerStartDevice;
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
	 * Initialize the DataLayer class, with the relative RS-232 conection Used,
	 * also for the Rest Api
	 */
	private synchronized void initializeGAL() throws Exception {

		if (getPropertiesManager().getDebugEnabled())
			LOG.info("Gal Version: " + getVersion().getManufacturerVersion());

		/* Used for reset GAL */
		if (DataLayer != null) {
			if (getPropertiesManager().getDebugEnabled())
				LOG.info("Starting reset...");
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
			DataLayer.destroy();
			if (getPropertiesManager().getDebugEnabled())
				LOG.info("Reset done!");
		}
		/* End of reset section */
		if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
			DataLayer = new DataFreescale(this);
			DataLayer.initialize();
			try {

				DataLayer.getIKeyInstance().initialize();
			} catch (Exception e) {
				DataLayer.getIKeyInstance().disconnect();
				throw e;
			}
		} else
			try {
				// FIXME why trow and catch directly in the same place ?
				throw new Exception("No Platform found!");
			} catch (Exception e) {
				LOG.error("Caught No Platform found", e);
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
				short _EndPoint;
				_EndPoint = DataLayer.configureEndPointSync(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
				if (_EndPoint == 0)
					throw new Exception("Error on configure endpoint");

			}
		}

		if (getPropertiesManager().getDebugEnabled())
			LOG.info("***Gateway is ready now... Current GAL Status: " + getGatewayStatus().toString() + "***");

	}

	/**
	 * recovery of the GAL,
	 */
	public synchronized void recoveryGAL() throws Exception {
		MyRunnable thr = new MyRunnable(this) {
			@Override
			public void run() {
				String filenamelog = System.getProperty("user.home") + File.separator + "GalLog.log";
				BufferedWriter bufferFileWriter = null;

				try {
					LOG.error("\n\r********GAL node is not responding...Starting recovery procedue. Wait...");
					LOG.error("\n\r********STARTING RECOVERY...");

					/* Gal is not Responding */
					File f = new File(filenamelog);
					if (!f.exists())
						try {
							f.createNewFile();
						} catch (IOException e2) {
							LOG.error("\n\rError creating file log: " + filenamelog);
						}

					FileWriter fileWriter = new FileWriter(f, true);
					bufferFileWriter = new BufferedWriter(fileWriter);
					fileWriter.append("\n\r").append(new Date(System.currentTimeMillis()).toString()).append("STARTING RECOVERY");

					/* Used for reset GAL */
					if (DataLayer != null) {
						if (getPropertiesManager().getDebugEnabled())
							LOG.info("Starting reset...");
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
						DataLayer.destroy();
						if (getPropertiesManager().getDebugEnabled())
							LOG.info("Reset done!");
					}
					/* End of reset section */
					if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
						DataLayer = new DataFreescale((GalController) this.getParameter());
						DataLayer.initialize();
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
							LOG.error("Caught No Platform found", e);
						}

					if (DataLayer.getIKeyInstance().isConnected()) {
						short _EndPoint;
						if (lastEndPoint == null) {
							_EndPoint = configureEndpoint(PropertiesManager.getCommandTimeoutMS(), PropertiesManager.getSimpleDescriptorReadFromFile());
							if (_EndPoint == 0)
								throw new Exception("Error on configure endpoint");
						} else {
							_EndPoint = configureEndpoint(PropertiesManager.getCommandTimeoutMS(), lastEndPoint);
							if (_EndPoint == 0)
								throw new Exception("Error on configure endpoint");

						}
						Status st;
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
					}
					LOG.error("********RECOVERY DONE!");

					fileWriter.append("\n\r").append(new Date(System.currentTimeMillis()).toString()).append("RECOVERY DONE!");
					return;
				} catch (Exception e1) {
					LOG.error("Error resetting GAL");
				} finally {
					try {
                        assert bufferFileWriter != null;
                        bufferFileWriter.close();
					} catch (IOException e) {
						LOG.error("Error closing file: " + filenamelog);
					}
				}

			}
		};
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
	@Deprecated
	public synchronized ApsMessageManager getApsManager() {
		return apsManager;
	}

	/**
	 * Gets the Message manager APS/INTERPAN.
	 * 
	 * @return the message manager.
	 */
	public synchronized MessageManager getMessageManager() {
		return messageManager;
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
	public short configureEndpoint(long timeout, SimpleDescriptor desc) throws  Exception {

		if ((desc.getApplicationInputCluster().size() + desc.getApplicationOutputCluster().size()) > 30) {
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
	public NodeServices getLocalServices() throws  Exception {
		NodeServices result = DataLayer.getLocalServices();
		if (GalNode != null && GalNode.get_node().getAddress() != null) {
			result.setAddress(GalNode.get_node().getAddress());
			List<WrapperWSNNode> _list = getNetworkcache();
			for (WrapperWSNNode o : _list) {

				if (o.get_node() != null && o.get_node().getAddress() != null && o.get_node().getAddress().getNetworkAddress().equals(get_GalNode().get_node().getAddress().getNetworkAddress())) {
					o.set_nodeServices(result);
					result = o.get_nodeServices();

					break;
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
	public NodeServicesList readServicesCache() throws Exception {
		NodeServicesList list = new NodeServicesList();
		List<WrapperWSNNode> _list = getNetworkcache();
		for (WrapperWSNNode o : _list) {
			if (o.get_nodeServices() != null)
				list.getNodeServices().add(o.get_nodeServices());
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
		}

		return SerializationUtils.clone(_list);
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
				try {
					if (x.get_node().getAddress().getNetworkAddress() == null && x.get_node().getAddress().getIeeeAddress() != null)
						x.get_node().getAddress().setNetworkAddress(getShortAddress_FromIeeeAddress(x.get_node().getAddress().getIeeeAddress()));
					if (x.get_node().getAddress().getIeeeAddress() == null && x.get_node().getAddress().getNetworkAddress() != null)
						x.get_node().getAddress().setIeeeAddress(getIeeeAddress_FromShortAddress(x.get_node().getAddress().getNetworkAddress()));
					_list.getAlias().add(x.get_node().getAddress());
					counter++;
				} catch (Exception e) {
					LOG.error(e.getMessage());

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
	public LQIInformation getLQIInformation(Address aoi) throws  Exception {

		LQIInformation _lqi = new LQIInformation();
		WrapperWSNNode x = null;
		synchronized (this) {
			int _index;
			_index = existIntoNetworkCache(aoi);
			if (_index > -1)
				x = getNetworkcache().get(_index);

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
	public LQIInformation getAllLQIInformations() throws  Exception {
		LQIInformation _lqi = new LQIInformation();
		synchronized (this) {
			List<WrapperWSNNode> _list = getNetworkcache();

			for (WrapperWSNNode x : _list) {
				if (PropertiesManager.getDebugEnabled())
					LOG.info("Node:" + x.get_node().getAddress().getNetworkAddress() + " - DiscoveryCompleted:" + x.is_discoveryCompleted());
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
	public NodeDescriptor getNodeDescriptor(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final boolean Async) throws  Exception {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					NodeDescriptor nodeDescriptor = new NodeDescriptor();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							nodeDescriptor = DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);
							int _index;
							if ((_index = existIntoNetworkCache(addrOfInterest)) > -1)
								getNetworkcache().get(_index).setNodeDescriptor(nodeDescriptor);
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						} catch (IOException e) {

							Status _s = errorManager(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);
						} catch (GatewayException e) {
							Status _s = errorManager(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						} catch (Exception e) {
							Status _s = errorManager(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
							get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

						}
					} else {
						Status _s = errorManager("Gal is not in running state!");
						get_gatewayEventManager().notifyNodeDescriptor(_requestIdentifier, _s, nodeDescriptor);
						get_gatewayEventManager().notifyNodeDescriptorExtended(_requestIdentifier, _s, nodeDescriptor, addrOfInterest);

					}

				}
			};
			thr.start();
			return null;

		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				NodeDescriptor nodeDescriptor = DataLayer.getNodeDescriptorSync(timeout, addrOfInterest);
				int _index;
				if ((_index = existIntoNetworkCache(addrOfInterest)) > -1)
					getNetworkcache().get(_index).setNodeDescriptor(nodeDescriptor);
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
	public short getChannelSync(long timeout) throws  Exception {
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

	public Status startGatewayDevice(final long timeout, final int _requestIdentifier, final StartupAttributeInfo sai, final boolean Async) throws  Exception {
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

									} catch (InterruptedException ignored) {

									}
								}
								if (_lockerStartDevice.getId() > 0) {
									lastSai = sai;
									if (PropertiesManager.getDebugEnabled())
										LOG.info("Gateway Started now!");

								} else {
									setGatewayStatus(GatewayStatus.GW_READY_TO_START);

									LOG.error("*******Gateway NOT Started!");
									_res.setCode((short) GatewayConstants.GENERAL_ERROR);
									_res.setMessage("No Network Event Running received!");

								}
							}

							get_gatewayEventManager().notifyGatewayStartResult(_res);
						} catch (IOException e) {
							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, errorManager(e.getMessage()));

						} catch (GatewayException e) {
							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, errorManager(e.getMessage()));

						} catch (Exception e) {
							get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, errorManager(e.getMessage()));

						}
					} else {
						// ...from all others, throw an exception
						String message = "Trying to start Gateway Device in " + getGatewayStatus() + " state.";
						if (PropertiesManager.getDebugEnabled()) {
							LOG.info(message);
						}

						get_gatewayEventManager().notifyGatewayStartResult(_requestIdentifier, errorManager(message));

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
						lastSai = sai;
						if (PropertiesManager.getDebugEnabled())
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
				if (PropertiesManager.getDebugEnabled()) {
					LOG.info(message);
				}
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
	public Status startGatewayDevice(long timeout, int _requestIdentifier, boolean Async) throws  Exception {
		StartupAttributeInfo sai = PropertiesManager.getSturtupAttributeInfo();
		return startGatewayDevice(timeout, _requestIdentifier, sai, Async);

	}

	/**
	 * Resets the GAl with the ability to set whether to delete the
	 * NonVolatileMemory to the next reboot
	 * 
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
	public Status resetDongle(final short mode, final boolean Async) throws  Exception {
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

						get_gatewayEventManager().notifyResetResult(errorManager(e.getMessage()));
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
	public Status stopNetwork(final long timeout, final int _requestIdentifier, boolean Async) throws Exception {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						setGatewayStatus(GatewayStatus.GW_STOPPING);

						Status _res;
						try {
							_res = DataLayer.stopNetworkSync(timeout);
							get_gatewayEventManager().notifyGatewayStopResult(_res);
						} catch (GatewayException e) {
							get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, errorManager(e.getMessage()));

						} catch (Exception e) {
							get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, errorManager(e.getMessage()));

						}

					} else {

						String message = "Trying to stop Gateway Device in " + getGatewayStatus() + " state.";
						if (PropertiesManager.getDebugEnabled()) {
							LOG.info(message);
						}

						get_gatewayEventManager().notifyGatewayStopResult(_requestIdentifier, errorManager(message));

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
			return SerializationUtils.clone(_status);

		}
	}

	public String APSME_GETSync(short attrId) throws Exception {
		return DataLayer.APSME_GETSync(PropertiesManager.getCommandTimeoutMS(), attrId);
	}

	public String MacGetPIBAttributeSync(short attrId) throws Exception {
		return DataLayer.MacGetPIBAttributeSync(PropertiesManager.getCommandTimeoutMS(), attrId);
	}

	public void APSME_SETSync(short attrId, String value) throws Exception {
		DataLayer.APSME_SETSync(PropertiesManager.getCommandTimeoutMS(), attrId, value);
	}

	public String NMLE_GetSync(short ilb, short iEntry) throws  Exception {
		String _value = DataLayer.NMLE_GetSync(PropertiesManager.getCommandTimeoutMS(), ilb, iEntry);
		/* Refresh value of the PanId */
		if (ilb == 80)
			setNetworkPanID(_value);

		return _value;

	}

	public void NMLE_SetSync(short attrId, String value) throws Exception {
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
	public long createCallback(int proxyIdentifier, Callback callback, APSMessageListener listener) throws  Exception {
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
	public long createCallback(int proxyIdentifier, Callback callback, MessageListener listener) throws  Exception {
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
	public void deleteCallback(long id) throws  Exception {
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
	public NodeServices startServiceDiscovery(final long timeout, final int _requestIdentifier, final Address aoi, boolean Async) throws  Exception {
		if (aoi.getNetworkAddress() == null && aoi.getIeeeAddress() != null)
			aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (aoi.getIeeeAddress() == null && aoi.getNetworkAddress() != null)
			aoi.setIeeeAddress(getIeeeAddress_FromShortAddress(aoi.getNetworkAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					NodeServices _newNodeService = new NodeServices();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						List<Short> _s;
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
							int _index;
							if ((_index = existIntoNetworkCache(aoi)) == -1) {
								getNetworkcache().get(_index).set_nodeServices(_newNodeService);
							}
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, _ok, _newNodeService);
						} catch (IOException e) {
							_newNodeService.setAddress(aoi);
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, errorManager((e.getMessage())), _newNodeService);
						} catch (GatewayException e) {
							_newNodeService.setAddress(aoi);
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, errorManager(e.getMessage()), _newNodeService);
						} catch (Exception e) {
							_newNodeService.setAddress(aoi);
							get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, errorManager(e.getMessage()), _newNodeService);
						}
					} else {
						get_gatewayEventManager().notifyServicesDiscovered(_requestIdentifier, errorManager(("Gal is not in running state")), _newNodeService);

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

				for (Short x : _result) {
					ActiveEndpoints _n = new ActiveEndpoints();
					_n.setEndPoint(x);
					_newNodeService.getActiveEndpoints().add(_n);
				}
				int _index;
				if ((_index = existIntoNetworkCache(aoi)) > -1) {
					getNetworkcache().get(_index).set_nodeServices(_newNodeService);
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
	public CallbackIdentifierList listCallbacks(int requestIdentifier) throws  Exception {
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
		for (int i = 0; i < getListGatewayEventListener().size(); i++) {
			if (getListGatewayEventListener().get(i).getProxyIdentifier() == proxyIdentifier) {
				if (listener == null) {
					getListGatewayEventListener().remove(i);
					if (getPropertiesManager().getDebugEnabled())
						LOG.info("Removing Listener for: " + proxyIdentifier);
					return;
				} else {
					_listenerFound = true;
					break;
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
	 * @param message
	 *            the message to send.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void sendAPSMessage(long timeout, APSMessage message) throws  Exception {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
			if (message.getDestinationAddress().getNetworkAddress() == null && message.getDestinationAddress().getIeeeAddress() != null)
				message.getDestinationAddress().setNetworkAddress(getShortAddress_FromIeeeAddress(message.getDestinationAddress().getIeeeAddress()));
			if (message.getDestinationAddress().getIeeeAddress() == null && message.getDestinationAddress().getNetworkAddress() != null)
				message.getDestinationAddress().setIeeeAddress(getIeeeAddress_FromShortAddress(message.getDestinationAddress().getNetworkAddress()));
			DataLayer.sendApsSync(timeout, message);
		} else
			throw new GatewayException("Gal is not in running state!");
	}

	/**
	 * Sends an InterPAN message.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param message
	 *            the message to send.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void sendInterPANMessage(long timeout, InterPANMessage message) throws  Exception {
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
	public Status leave(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final int mask, final boolean Async) throws  Exception {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					Status _s;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						if (!addrOfInterest.getNetworkAddress().equals(GalNode.get_node().getAddress().getNetworkAddress())) {

							try {

								leavePhilips(timeout, _requestIdentifier, addrOfInterest);

								_s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
								if (_s.getCode() == GatewayConstants.SUCCESS) {

									/* get the node from cache */
									int index = existIntoNetworkCache(addrOfInterest);
									if (index > -1) {
										WrapperWSNNode _wrapper = getNetworkcache().get(index);
										_wrapper.abortTimers();
										get_gatewayEventManager().nodeRemoved(_s, _wrapper.get_node());
										getNetworkcache().remove(index);

									}
								}

								get_gatewayEventManager().notifyleaveResult(_s);
								get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);

							} catch (IOException e) {
								Status _s1 = errorManager(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							} catch (GatewayException e) {
								Status _s1 = errorManager(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							} catch (Exception e) {
								Status _s1 = errorManager(e.getMessage());
								get_gatewayEventManager().notifyleaveResult(_requestIdentifier, _s1);
								get_gatewayEventManager().notifyleaveResultExtended(_requestIdentifier, _s1, addrOfInterest);

							}
						} else {
							Status _s1 = errorManager("Is not possible Leave the GAL!");
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
					leavePhilips(timeout, _requestIdentifier, addrOfInterest);

					Status _s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
					if (_s.getCode() == GatewayConstants.SUCCESS) {

						/* get the node from cache */
						int index = existIntoNetworkCache(addrOfInterest);
						if (index > -1) {
							WrapperWSNNode _wrapper = getNetworkcache().get(index);
							_wrapper.abortTimers();
							get_gatewayEventManager().nodeRemoved(_s, _wrapper.get_node());
							getNetworkcache().remove(index);

						}
					}

					get_gatewayEventManager().notifyleaveResult(_s);
					get_gatewayEventManager().notifyleaveResultExtended(_s, addrOfInterest);
					return SerializationUtils.clone(_s);
				} else
					throw new GatewayException("Is not possible Leave the GAL!");
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	private void leavePhilips(final long timeout, final int _requestIdentifier, final Address addrOfInterest) throws  Exception {
		WrapperWSNNode node = null;
		/* Check if the device is the Philips light */
		int _index = -1;
		if ((_index = existIntoNetworkCache(addrOfInterest)) > -1) {
			node = getNetworkcache().get(_index);
		}

		if (node != null) {

			NodeDescriptor nodeDescriptor;
			if (node.getNodeDescriptor() == null)
				nodeDescriptor = getNodeDescriptor(timeout, _requestIdentifier, addrOfInterest, false);

			else
				nodeDescriptor = node.getNodeDescriptor();

			/* Philips Device Led */
			if (nodeDescriptor.getManufacturerCode() == 4107) {

				if (PropertiesManager.getDebugEnabled())
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
				scanReqCommand.setDestPANID(getManageMapPanId().getPanid(node.get_node().getAddress().getIeeeAddress()));
				scanReqCommand.setProfileID(49246);
				scanReqCommand.setClusterID(4096);
				scanReqCommand.setASDULength(9);
				scanReqCommand.setASDU(new byte[] { 0x11, 0x01, 0x00, (byte) 0xCA, (byte) 0xFE, (byte) 0xCA, (byte) 0xFE, 0x02, 0x33 });
				sendInterPANMessage(timeout, scanReqCommand);

				Thread.sleep(1000);

				/* ScanRequest */
				InterPANMessage resetCommand = new InterPANMessage();
				resetCommand.setSrcAddressMode(3);
				resetCommand.setSrcAddress(GalNode.get_node().getAddress());
				resetCommand.setSrcPANID(Integer.parseInt(getNetworkPanID(), 16));
				resetCommand.setDstAddressMode(2);
				resetCommand.setDestinationAddress(broadcast);
				resetCommand.setDestPANID(getManageMapPanId().getPanid(node.get_node().getAddress().getIeeeAddress()));
				resetCommand.setProfileID(49246);
				resetCommand.setClusterID(4096);
				resetCommand.setASDULength(7);
				resetCommand.setASDU(new byte[] { 0x11, 0x03, 0x07, (byte) 0xCA, (byte) 0xFE, (byte) 0xCA, (byte) 0xFE });
				sendInterPANMessage(timeout, resetCommand);

				if (PropertiesManager.getDebugEnabled())
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
	public Status permitJoin(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final short duration, final boolean Async) throws Exception {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {

					Status _s;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							_s = DataLayer.permitJoinSync(timeout, addrOfInterest, duration, (byte) 0x00);
							get_gatewayEventManager().notifypermitJoinResult(_s);
						} catch (IOException e) {
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
						} catch (GatewayException e) {
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
						} catch (Exception e) {
							get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
						}
					} else {
						get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager("Gal is not in running state!"));
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
					return SerializationUtils.clone(_s);
				} catch (IOException e) {
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
					throw e;
				} catch (GatewayException e) {
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
					throw e;
				} catch (Exception e) {
					get_gatewayEventManager().notifypermitJoinResult(_requestIdentifier, errorManager(e.getMessage()));
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
	public Status permitJoinAll(final long timeout, final short duration, final boolean Async) throws  Exception {
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
							get_gatewayEventManager().notifypermitJoinResult(errorManager(e.getMessage()));
						} catch (Exception e) {

							get_gatewayEventManager().notifypermitJoinResult(errorManager(e.getMessage()));
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
		int _index;
		if (PropertiesManager.getDebugEnabled())
			LOG.info("Called startNodeDiscovery Mask: " + discoveryMask + " - Timeout:" + timeout);
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
				if (getPropertiesManager().getDebugEnabled())
					LOG.info("Adding node from start Discovery: " + GalNode.get_node().getAddress().getNetworkAddress());

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
					LOG.info("Global Discovery Started(" + __timeout + " seconds)!");
				}
			} else if ((discoveryMask == GatewayConstants.DISCOVERY_STOP) || (timeout == 1)) {

				if (PropertiesManager.getDebugEnabled())
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
	 * @param requestIdentifier
	 *            the request identifier
	 * @param discoveryFreshness_mask
	 *            the freshness mask
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public void subscribeNodeRemoval(int requestIdentifier, int discoveryFreshness_mask) throws GatewayException {
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
	public static Version getVersion() throws  Exception {
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
					WrapperWSNNode galNodeWrapper = new WrapperWSNNode(((GalController) this.getParameter()));
					WSNNode galNode = new WSNNode();
					Address _add = new Address();
					_add.setNetworkAddress(Integer.parseInt(_NetworkAdd, 16));
					_add.setIeeeAddress(_IeeeAdd);
					galNode.setAddress(_add);
					galNodeWrapper.set_node(galNode);

					/* Read the NodeDescriptor of the GAL */
					NodeDescriptor _NodeDescriptor = null;
					while (_NodeDescriptor == null) {
						try {
							_NodeDescriptor = DataLayer.getNodeDescriptorSync(PropertiesManager.getCommandTimeoutMS(), _add);
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
								int _index;

								/* If the Node Not Exists */
								if ((_index = existIntoNetworkCache(_add)) == -1) {
									if (getPropertiesManager().getDebugEnabled())
										LOG.info("Adding node from SetStatus: " + galNodeWrapper.get_node().getAddress().getNetworkAddress());
									getNetworkcache().add(galNodeWrapper);
								}
								/* The GAl node is already present into the DB */
								else {
									getNetworkcache().get(_index).abortTimers();
									getNetworkcache().get(_index).set_node(galNodeWrapper.get_node());
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
					while (_permitjoin == null || ((_permitjoin.getCode() != GatewayConstants.SUCCESS))) {
						try {
							_permitjoin = DataLayer.permitJoinSync(PropertiesManager.getCommandTimeoutMS(), _add, (short) 0x00, (byte) 0x01);
							if (_permitjoin.getCode() != GatewayConstants.SUCCESS)
                                get_gatewayEventManager().notifyGatewayStartResult(errorManager("Error on permitJoin(0) for the GAL node on startup!"));
						} catch (Exception e) {

							LOG.error("Error retrieving the Gal Node Descriptor!");

						}

					}

					if (!galNodeWrapper.isSleepy()) {
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

					synchronized (_lockerStartDevice) {
						_lockerStartDevice.setId(1);
						_lockerStartDevice.notify();
					}
					_gatewayStatus = gatewayStatus;

					Status _s = new Status();
					_s.setCode((short) 0x00);
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
			if (PropertiesManager.getDebugEnabled()) {
				LOG.info("Stopping Discovery and Freshness procedures...");
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
	public Status clearEndpoint(short endpoint) throws  Exception {
		Status _s = DataLayer.clearEndpointSync(endpoint);
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
	public ServiceDescriptor getServiceDescriptor(final long timeout, final int _requestIdentifier, final Address addrOfInterest, final short endpoint, boolean Async) throws  Exception {
		if (addrOfInterest.getNetworkAddress() == null && addrOfInterest.getIeeeAddress() != null)
			addrOfInterest.setNetworkAddress(getShortAddress_FromIeeeAddress(addrOfInterest.getIeeeAddress()));
		if (addrOfInterest.getIeeeAddress() == null && addrOfInterest.getNetworkAddress() != null)
			addrOfInterest.setIeeeAddress(getIeeeAddress_FromShortAddress(addrOfInterest.getNetworkAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
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
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, errorManager(e.getMessage()), _toRes);
						} catch (Exception e) {
							_toRes.setAddress(addrOfInterest);
							get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, errorManager(e.getMessage()), _toRes);
						}
					} else {
						_toRes.setAddress(addrOfInterest);
						get_gatewayEventManager().notifyserviceDescriptorRetrieved(_requestIdentifier, errorManager("Gal is not in running state!"), _toRes);
					}
				}
			};
			thr.start();
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
	public BindingList getNodeBindingsSync(final long timeout, final int _requestIdentifier, final Address aoi, final short index, boolean Async) throws  Exception {
		if (aoi.getNetworkAddress() == null && aoi.getIeeeAddress() != null)
			aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (aoi.getIeeeAddress() == null && aoi.getNetworkAddress() != null)
			aoi.setIeeeAddress(getIeeeAddress_FromShortAddress(aoi.getNetworkAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					BindingList _toRes = new BindingList();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							_toRes = DataLayer.getNodeBindings(timeout, aoi, index);
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, _s, _toRes);
						} catch (GatewayException e) {
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, errorManager(e.getMessage()), _toRes);
						} catch (Exception e) {
							get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, errorManager(e.getMessage()), _toRes);
						}
					} else {
						get_gatewayEventManager().notifynodeBindingsRetrieved(_requestIdentifier, errorManager("Gal is not in running state!"), _toRes);
					}
				}
			};
			thr.start();
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
	public Status addBindingSync(final long timeout, final int _requestIdentifier, final Binding binding, final boolean Async) throws Exception {
		final Address aoi = new Address();
		aoi.setIeeeAddress(binding.getSourceIEEEAddress());
		aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							Status _s = DataLayer.addBinding(timeout, binding, aoi);
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, _s);
						} catch (GatewayException e) {
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, errorManager(e.getMessage()));
						} catch (Exception e) {
							get_gatewayEventManager().notifybindingResult(_requestIdentifier, errorManager(e.getMessage()));
						}
					} else {
						get_gatewayEventManager().notifybindingResult(_requestIdentifier, errorManager("Gal is not in running state!"));

					}
				}
			};
			thr.start();
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
	public Status removeBindingSync(final long timeout, final int _requestIdentifier, final Binding binding, final boolean Async) throws  Exception {
		final Address aoi = new Address();
		aoi.setIeeeAddress(binding.getSourceIEEEAddress());
		aoi.setNetworkAddress(getShortAddress_FromIeeeAddress(aoi.getIeeeAddress()));
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = DataLayer.removeBinding(timeout, binding, aoi);
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, _s);
						} catch (GatewayException e) {
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, errorManager(e.getMessage()));
						} catch (Exception e) {
							get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, errorManager(e.getMessage()));
						}
					} else {
						get_gatewayEventManager().notifyUnbindingResult(_requestIdentifier, errorManager("Gal is not in running state!"));
					}
				}
			};
			thr.start();
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
	public Status frequencyAgilitySync(final long timeout, final short scanChannel, final short scanDuration, final boolean Async) throws  Exception {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _st = DataLayer.frequencyAgilitySync(timeout, scanChannel, scanDuration);
							get_gatewayEventManager().notifyFrequencyAgility(_st);
						} catch (GatewayException e) {
							get_gatewayEventManager().notifyFrequencyAgility(errorManager(e.getMessage()));
						} catch (Exception e) {
							get_gatewayEventManager().notifyFrequencyAgility(errorManager(e.getMessage()));
						}
					} else {
						get_gatewayEventManager().notifyFrequencyAgility(errorManager("Gal is not in running state!"));

					}
				}
			};
			thr.start();
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
	 * @param address
	 *            the address to look for.
	 * @return -1 if the address does not exist in network cache or a positive
	 *         number indicating the index of the object on network cache
	 *         otherwise
	 */
	public synchronized short existIntoNetworkCache(Address address) {
		short __indexOnCache = -1;
		short _indexToReturn = -1;
		String AddressStr = "";
		List<WrapperWSNNode> _list = getNetworkcache();
		if (getPropertiesManager().getDebugEnabled())
			LOG.debug("[ExistIntoNetworkCache] Start Search Node ShortAddress: " + ((address.getNetworkAddress() != null) ? String.format("%04X", address.getNetworkAddress()) : "NULL") + " -- IeeeAdd: " + ((address.getIeeeAddress() != null) ? String.format("%04X", address.getIeeeAddress()) : "NULL"));
		for (WrapperWSNNode y : _list) {
			if (getPropertiesManager().getDebugEnabled())
				LOG.debug("[ExistIntoNetworkCache] Short Address:" + ((y.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", y.get_node().getAddress().getNetworkAddress()) : "NULL") + " - IEEE Address:" + ((y.get_node().getAddress().getIeeeAddress() != null) ? String.format("%016X", y.get_node().getAddress().getIeeeAddress()) : "NULL") + " - - Discovery Completed:" + y.is_discoveryCompleted());
			__indexOnCache++;
			if (y.get_node() != null && y.get_node().getAddress() != null && y.get_node().getAddress().getNetworkAddress() != null && address.getNetworkAddress() != null && y.get_node().getAddress().getNetworkAddress().intValue() == address.getNetworkAddress().intValue()) {
				AddressStr = String.format("%04X", y.get_node().getAddress().getNetworkAddress());
				_indexToReturn = __indexOnCache;
			} else if (y.get_node() != null && y.get_node().getAddress() != null && y.get_node().getAddress().getIeeeAddress() != null && address.getIeeeAddress() != null && y.get_node().getAddress().getIeeeAddress().longValue() == address.getIeeeAddress().longValue()) {
				AddressStr = String.format("%16X", y.get_node().getAddress().getIeeeAddress());

				_indexToReturn = __indexOnCache;
			}
		}

		if (_indexToReturn > -1) {
			if (getPropertiesManager().getDebugEnabled())
				LOG.debug("Found node:" + AddressStr);
			return _indexToReturn;
		} else {
			if (address.getNetworkAddress() != null)
				if (getPropertiesManager().getDebugEnabled())
					LOG.debug("Not Found node:" + String.format("%04X", address.getNetworkAddress()));
				else if (address.getIeeeAddress() != null)
					if (getPropertiesManager().getDebugEnabled())
						LOG.debug("Not Found node:" + String.format("%16X", address.getIeeeAddress()));
			return -1;
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
	public synchronized BigInteger getIeeeAddress_FromShortAddress(Integer shortAddress) throws Exception {
		List<WrapperWSNNode> _list = getNetworkcache();
		if (getPropertiesManager().getDebugEnabled())
			LOG.debug("[getIeeeAddress_FromShortAddress] Start Search Node: " + String.format("%04X", shortAddress));
		for (WrapperWSNNode y : _list) {
			if (getPropertiesManager().getDebugEnabled())
				LOG.debug("[getIeeeAddress_FromShortAddress] Short Address:" + ((y.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", y.get_node().getAddress().getNetworkAddress()) : "NULL") + " - IEEE Address:" + ((y.get_node().getAddress().getIeeeAddress() != null) ? String.format("%016X", y.get_node().getAddress().getIeeeAddress()) : "NULL") + " - - Discovery Completed:" + y.is_discoveryCompleted());

			if (y.is_discoveryCompleted() && y.get_node() != null && y.get_node().getAddress() != null && y.get_node().getAddress().getNetworkAddress() != null && y.get_node().getAddress().getIeeeAddress() != null && y.get_node().getAddress().getNetworkAddress().intValue() == shortAddress.intValue()) {
				if (getPropertiesManager().getDebugEnabled())
					LOG.debug("[getIeeeAddress_FromShortAddress] FOUND Node: " + String.format("%04X", shortAddress));

				return y.get_node().getAddress().getIeeeAddress();
			}
		}
		throw new Exception("Short Address not found on GAL: " + String.format("%04X", shortAddress));

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
	public synchronized Integer getShortAddress_FromIeeeAddress(BigInteger IeeeAddress) throws Exception {
		List<WrapperWSNNode> _list = getNetworkcache();
		if (getPropertiesManager().getDebugEnabled())
			LOG.debug("[getShortAddress_FromIeeeAddress] Start Search Node: " + String.format("%016X", IeeeAddress));
		for (WrapperWSNNode y : _list) {
			if (getPropertiesManager().getDebugEnabled())
				LOG.debug("[getShortAddress_FromIeeeAddress] Short Address:" + ((y.get_node().getAddress().getNetworkAddress() != null) ? String.format("%04X", y.get_node().getAddress().getNetworkAddress()) : "NULL") + " - IEEE Address:" + ((y.get_node().getAddress().getIeeeAddress() != null) ? String.format("%016X", y.get_node().getAddress().getIeeeAddress()) : "NULL") + " - - Discovery Completed:" + y.is_discoveryCompleted());
			if (y.is_discoveryCompleted() && (y.get_node() != null) && (y.get_node().getAddress() != null) && (y.get_node().getAddress().getIeeeAddress() != null) && (y.get_node().getAddress().getNetworkAddress() != null) && y.get_node().getAddress().getIeeeAddress().longValue() == IeeeAddress.longValue()) {
				if (getPropertiesManager().getDebugEnabled())
					LOG.debug("[getShortAddress_FromIeeeAddress] FOUND Node: " + String.format("%016X", IeeeAddress));

				return y.get_node().getAddress().getNetworkAddress();
			}
		}
		throw new Exception("Ieee Address not found on GAL: " + String.format("%016X", IeeeAddress));
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
    private Status errorManager(String message) {
        Status st = new Status();
        st.setCode((short)GatewayConstants.GENERAL_ERROR);
        st.setMessage(message);
        return st;

    }
}
