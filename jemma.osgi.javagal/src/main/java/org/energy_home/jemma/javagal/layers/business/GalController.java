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
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.implementations.ApsManager;
import org.energy_home.jemma.javagal.layers.business.implementations.Discovery_Freshness;
import org.energy_home.jemma.javagal.layers.business.implementations.GatewayEventManager;
import org.energy_home.jemma.javagal.layers.business.implementations.PartittionManager;
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
 * Actual JavaGal Controller. Only one instance of this object can exists at any time. All the clients can
 * access this instance via their dedicated proxies. See
 * {@link GalExtenderProxy}.
 * 
 */

/**
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GalController {
	private GatewayStatus _gatewayStatus = GatewayStatus.GW_READY_TO_START;
	private Long apsCallbackIdentifier = (long) 1;
	private short transequenceNumber = 1;
	private List<WrapperWSNNode> NetworkCache = Collections
			.synchronizedList(new LinkedList<WrapperWSNNode>());
	private List<CallbackEntry> listCallback = Collections
			.synchronizedList(new LinkedList<CallbackEntry>());
	private List<GatewayDeviceEventEntry> listGatewayEventListener = Collections
			.synchronizedList(new LinkedList<GatewayDeviceEventEntry>());
	private final static Log logger = LogFactory.getLog(GalController.class);
	private ApsManager apsManager = null;
	private PartittionManager partitionManager = null;

	private ZdoManager zdoManager = null;
	private GatewayEventManager _gatewayEventManager = null;
	private Boolean _Gal_in_Dyscovery_state = false;
	public Timer _timeoutGlobalDiscovery;
	private Boolean _Gal_in_Freshness_state = false;
	private ParserLocker _lockerStartDevice;
	private IDataLayer DataLayer = null;
	private Discovery_Freshness _discoveryManager = null;
	PropertiesManager PropertiesManager = null;

	private void inizializeGAL() throws Exception {
		/* Used for reset GAL */
		if (DataLayer != null) {
			if (getPropertiesManager().getDebugEnabled())
				logger.info("\n\rStarting reset...\n\r");
			/* Stop all timers */
			for (WrapperWSNNode x : getNetworkcache())
				x.abortTimers();
			getNetworkcache().clear();
			/* Stop discovery and freshness */
			set_Gal_in_Freshness_state(false);
			set_Gal_in_Dyscovery_state(false);
			/* Destroy Gal Node */
			set_GalNode(null);

			setGatewayStatus(GatewayStatus.GW_READY_TO_START);

			if (DataLayer.getIKeyInstance().isConnected())
				DataLayer.getIKeyInstance().disconnect();
			DataLayer = null;
			if (getPropertiesManager().getDebugEnabled())
				logger.info("\n\rReset done!\n\r");
		}
		/* End of reset section */
		if (PropertiesManager.getzgdDongleType().equalsIgnoreCase("freescale")) {
			DataLayer = new DataFreescale(this);
			try {

				DataLayer.getIKeyInstance().inizialize();
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

					logger.error("\n\rError on autostart!\n\r");
				}
			} else {
				short _EndPoint = 0;
				_EndPoint = DataLayer.configureEndPointSync(
						IDataLayer.INTERNAL_TIMEOUT,
						PropertiesManager.getSimpleDescriptorReadFromFile());
				if (_EndPoint == 0)
					throw new Exception("Error on configure endpoint");

			}
		}

		if (getPropertiesManager().getDebugEnabled())
			logger.info("\n\r***Gateway is ready now...***\n\r");

	}

	public GalController(PropertiesManager _properties) throws Exception {
		PropertiesManager = _properties;
		zdoManager = new ZdoManager(this);
		apsManager = new ApsManager(this);
		partitionManager = new PartittionManager(this);
		_gatewayEventManager = new GatewayEventManager(this);
		_timeoutGlobalDiscovery = null;
		_lockerStartDevice = new ParserLocker();
		_discoveryManager = new Discovery_Freshness(this);

		inizializeGAL();
	}

	public PropertiesManager getPropertiesManager() {
		return PropertiesManager;

	}

	public synchronized List<GatewayDeviceEventEntry> getListGatewayEventListener() {
		return listGatewayEventListener;
	}

	public synchronized List<CallbackEntry> getCallbacks() {
		return listCallback;
	}

	public synchronized Discovery_Freshness getDiscoveryManager() {
		return _discoveryManager;
	}

	public synchronized ApsManager getApsManager() {
		return apsManager;
	}

	public synchronized PartittionManager getPartitionManager() {
		return partitionManager;
	}

	public synchronized ZdoManager getZdoManager() {
		return zdoManager;
	}

	public synchronized IDataLayer getDataLayer() {
		return DataLayer;
	}

	public synchronized Boolean get_Gal_in_Dyscovery_state() {
		return _Gal_in_Dyscovery_state;
	}

	public synchronized void set_Gal_in_Dyscovery_state(Boolean GalonDyscovery) {
		_Gal_in_Dyscovery_state = GalonDyscovery;
	}

	public synchronized Boolean get_Gal_in_Freshness_state() {
		return _Gal_in_Freshness_state;
	}

	public synchronized void set_Gal_in_Freshness_state(Boolean GalonFreshness) {
		_Gal_in_Freshness_state = GalonFreshness;
	}

	public GatewayEventManager get_gatewayEventManager() {
		return _gatewayEventManager;
	}

	private WSNNode GalNode = null;

	public short configureEndpoint(long timeout, SimpleDescriptor desc)
			throws IOException, Exception, GatewayException {
		// TODO 30
		if ((desc.getApplicationInputCluster().size() + desc
				.getApplicationOutputCluster().size()) > 30) {
			throw new Exception("Simple Descriptor Out Of Memory");
		} else {
			short result = DataLayer.configureEndPointSync(timeout, desc);
			return result;
		}
	}

	public NodeServices getLocalServices() throws IOException, Exception,
			GatewayException {
		NodeServices result = DataLayer.getLocalServices();
		if (GalNode != null && GalNode.getAddress() != null)
			result.setAddress(GalNode.getAddress());
		for (WrapperWSNNode o : getNetworkcache()) {
			if (o.get_node().getAddress().getNetworkAddress() == get_GalNode()
					.getAddress().getNetworkAddress()) {
				o.set_nodeServices(result);
				result = o.get_nodeServices();
				break;
			}
		}
		return result;
	}

	public NodeServicesList readServicesCache() throws IOException, Exception,
			GatewayException {
		NodeServicesList list = new NodeServicesList();
		for (WrapperWSNNode o : getNetworkcache()) {
			if (o.get_nodeServices() != null)
				list.getNodeServices().add(o.get_nodeServices());
		}
		return list;
	}

	public void executeAutoStart() throws Exception {
		logger.info("Executing AutoStart procedure...");
		short _EndPoint = DataLayer.configureEndPointSync(
				IDataLayer.INTERNAL_TIMEOUT,
				PropertiesManager.getSimpleDescriptorReadFromFile());
		if (_EndPoint > 0x00) {
			logger.info("Configure EndPoint completed...");
			Status _statusStartGatewayDevice = DataLayer
					.startGatewayDeviceSync(IDataLayer.INTERNAL_TIMEOUT,
							PropertiesManager.getSturtupAttributeInfo());
			if (_statusStartGatewayDevice.getCode() == 0x00) {
				logger.info("StartGateway Device completed...");
				return;
			}

		}
	}

	public synchronized WSNNodeList readNodeCache() {
		WSNNodeList _list = new WSNNodeList();

		for (WrapperWSNNode x : getNetworkcache()) {
			if (x.is_discoveryCompleted())
				_list.getWSNNode().add(x.get_node());
		}

		return _list;
	}

	public synchronized Aliases listAddress() {
		Aliases _list = new Aliases();

		long counter = 0;
		for (WrapperWSNNode x : getNetworkcache()) {
			if (x.is_discoveryCompleted()) {
				if (x.get_node().getAddress().getIeeeAddress() == null)
					x.get_node()
							.getAddress()
							.setIeeeAddress(
									getIeeeAddress_FromNetworkCache(x
											.get_node().getAddress()
											.getNetworkAddress()));
				_list.getAlias().add(x.get_node().getAddress());
				counter++;
			}
		}
		_list.setNumberOfAlias(counter);
		return _list;
	}

	public LQIInformation getLQIInformation(Address aoi) throws IOException,
			Exception, GatewayException {
		LQIInformation _lqi = new LQIInformation();
		int _index = -1;
		if ((_index = existIntoNetworkCache(aoi.getNetworkAddress())) > -1) {
			WrapperWSNNode x = getNetworkcache().get(_index);
			if (x.is_discoveryCompleted()) {
				LQINode _lqinode = new LQINode();
				Mgmt_LQI_rsp _rsp = x.get_Mgmt_LQI_rsp();
				_lqinode.setNodeAddress(x.get_node().getAddress()
						.getIeeeAddress());
				if (_rsp.NeighborTableList != null) {
					for (NeighborTableLis_Record _n1 : _rsp.NeighborTableList) {

						Neighbor e = new Neighbor();
						e.setDepth((short) _n1._Depth);
						e.setDeviceTypeRxOnWhenIdleRelationship(_n1._RxOnWhenIdle);
						e.setExtendedPANId(BigInteger
								.valueOf(_n1._Extended_PAN_Id));
						e.setIeeeAddress(BigInteger
								.valueOf(_n1._Extended_Address));
						e.setLQI((short) _n1._LQI);
						e.setPermitJoining((short) _n1._Permitting_Joining);
						_lqinode.getNeighborList().add(e);
					}
				}

				_lqi.getLQINode().add(_lqinode);
			}

			return _lqi;

		} else
			throw new Exception("Address not found!");

	}

	public NodeDescriptor getNodeDescriptor(final long timeout,
			final int _requestIdentifier, final Address addrOfInterest,
			final boolean Async) throws IOException, Exception,
			GatewayException {

		if (Async) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {

							NodeDescriptor _node = DataLayer
									.getNodeDescriptorSync(timeout,
											addrOfInterest);

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							get_gatewayEventManager().notifyNodeDescriptor(
									_requestIdentifier, _s, _node);
							get_gatewayEventManager()
									.notifyNodeDescriptorExtended(
											_requestIdentifier, _s, _node,
											addrOfInterest);

						} catch (IOException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(
									_requestIdentifier, _s, null);
							get_gatewayEventManager()
									.notifyNodeDescriptorExtended(
											_requestIdentifier, _s, null,
											addrOfInterest);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(
									_requestIdentifier, _s, null);
							get_gatewayEventManager()
									.notifyNodeDescriptorExtended(
											_requestIdentifier, _s, null,
											addrOfInterest);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyNodeDescriptor(
									_requestIdentifier, _s, null);
							get_gatewayEventManager()
									.notifyNodeDescriptorExtended(
											_requestIdentifier, _s, null,
											addrOfInterest);

						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyNodeDescriptor(
								_requestIdentifier, _s, null);
						get_gatewayEventManager().notifyNodeDescriptorExtended(
								_requestIdentifier, _s, null, addrOfInterest);

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

	public short getChannelSync(long timeout) throws IOException, Exception,
			GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING)
			return DataLayer.getChannelSync(timeout);
		else
			throw new GatewayException("Gal is not in running state!");

	}

	

	public Status startGatewayDevice(final long timeout,
			final int _requestIdentifier, final StartupAttributeInfo sai,
			final boolean Async) throws IOException, Exception,
			GatewayException {
		// The network can start only from those two gateway status...
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START
							|| getGatewayStatus() == GatewayStatus.GW_STOPPED) {

						setGatewayStatus(GatewayStatus.GW_STARTING);
						try {
							Status _res = DataLayer.startGatewayDeviceSync(
									timeout, sai);
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
										logger.info("\n\rGateway Started now!\n\r");

								} else {
									setGatewayStatus(GatewayStatus.GW_READY_TO_START);
									if (PropertiesManager.getDebugEnabled())
										logger.error("\n\r*******Gateway NOT Started!\n\r");
									_res.setCode((short) GatewayConstants.GENERAL_ERROR);
									_res.setMessage("No Network Event Running received!");

								}
							}

							get_gatewayEventManager().notifyGatewayStartResult(
									_res);
						} catch (IOException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStartResult(
									_requestIdentifier, _s);

						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());

							get_gatewayEventManager().notifyGatewayStartResult(
									_requestIdentifier, _s);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());

							get_gatewayEventManager().notifyGatewayStartResult(
									_requestIdentifier, _s);

						}
					} else {
						// ...from all others, throw an exception
						String message = "Trying to start Gateway Device in "
								+ getGatewayStatus() + " state.";
						if (PropertiesManager.getDebugEnabled()) {
							logger.info(message);
						}
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStartResult(
								_requestIdentifier, _s);

					}
				}
			};
			thr.start();
			return null;
		} else {
			Status _status;
			if (getGatewayStatus() == GatewayStatus.GW_READY_TO_START
					|| getGatewayStatus() == GatewayStatus.GW_STOPPED) {
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
							logger.info("\n\rGateway Started now!\n\r");
					} else {
						setGatewayStatus(GatewayStatus.GW_READY_TO_START);
						if (PropertiesManager.getDebugEnabled())
							logger.error("\n\rGateway NOT Started!\n\r");
						_status.setCode((short) GatewayConstants.GENERAL_ERROR);
						_status.setMessage("No Network Event Running received!");
					}
				}

				get_gatewayEventManager().notifyGatewayStartResult(_status);
			} else {
				// ...from all others, throw an exception
				String message = "Trying to start Gateway Device in "
						+ getGatewayStatus() + " state.";
				if (PropertiesManager.getDebugEnabled()) {
					logger.info(message);
				}
				throw new GatewayException(message);
			}
			return _status;

		}

	}

	public Status startGatewayDevice(long timeout, int _requestIdentifier,
			boolean Async) throws IOException, Exception, GatewayException {
		StartupAttributeInfo sai = PropertiesManager.getSturtupAttributeInfo();
		return startGatewayDevice(timeout, _requestIdentifier, sai, Async);

	}

	public Status resetDongle(final long timeout, final int _requestIdentifier,
			final short mode, final boolean Async) throws IOException,
			Exception, GatewayException {
		if (mode == GatewayConstants.RESET_COMMISSIONING_ASSOCIATION) {
			PropertiesManager.setStartupSet((short) 0x18);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl(
					(short) 0x00);

		} else if (mode == GatewayConstants.RESET_USE_NVMEMORY) {
			PropertiesManager.setStartupSet((short) 0x00);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl(
					(short) 0x04);

		} else if (mode == GatewayConstants.RESET_COMMISSIONING_SILENTSTART) {
			PropertiesManager.setStartupSet((short) 0x18);
			PropertiesManager.getSturtupAttributeInfo().setStartupControl(
					(short) 0x04);
		}
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try {

						Status _s = new Status();
						_s.setCode((short) GatewayConstants.SUCCESS);
						_s.setMessage("Reset Done");
						inizializeGAL();
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
			inizializeGAL();
			get_gatewayEventManager().notifyResetResult(_s);
			return _s;
		}

	}

	public Status stopNetwork(final long timeout, final int _requestIdentifier,
			boolean Async) throws Exception, GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {

					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						setGatewayStatus(GatewayStatus.GW_STOPPING);

						Status _res = null;
						try {
							_res = DataLayer.stopNetworkSync(timeout);
							get_gatewayEventManager().notifyGatewayStopResult(
									_res);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStopResult(
									_requestIdentifier, _s);

						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyGatewayStopResult(
									_requestIdentifier, _s);

						}

					} else {

						String message = "Trying to stop Gateway Device in "
								+ getGatewayStatus() + " state.";
						if (PropertiesManager.getDebugEnabled()) {
							logger.info(message);
						}
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage(message);
						get_gatewayEventManager().notifyGatewayStopResult(
								_requestIdentifier, _s);

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
				String message = "Trying to stop Gateway Device in "
						+ getGatewayStatus() + " state.";
				throw new GatewayException(message);
			}
			return _status;

		}
	}

	public String APSME_GETSync(short attrId)
			throws Exception, GatewayException {
		return DataLayer.APSME_GETSync(IDataLayer.INTERNAL_TIMEOUT, attrId);
	}

	public void APSME_SETSync(short attrId, String value)
			throws Exception, GatewayException {
		DataLayer.APSME_SETSync(IDataLayer.INTERNAL_TIMEOUT, attrId, value);
	}

	public String NMLE_GetSync(short ilb) throws IOException, Exception,
	GatewayException {
return DataLayer.NMLE_GetSync(IDataLayer.INTERNAL_TIMEOUT, ilb);

}

public void NMLE_SetSync(short attrId, String value) throws Exception,
	GatewayException {
DataLayer.NMLE_SETSync(IDataLayer.INTERNAL_TIMEOUT, attrId, value);
}

	public long createCallback(int proxyIdentifier, Callback callback,
			APSMessageListener listener) throws IOException, Exception,
			GatewayException {
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

	public void deleteCallback(long id) throws IOException, Exception,
			GatewayException {
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
			throw new GatewayException("Callback with id " + id
					+ " not present");

	}

	public NodeServices startServiceDiscovery(final long timeout,
			final int _requestIdentifier, final Address aoi, boolean Async)
			throws IOException, Exception, GatewayException {

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						List<Short> _s = null;
						try {
							_s = DataLayer.startServiceDiscoverySync(timeout,
									aoi);
							Status _ok = new Status();
							_ok.setCode((short) 0x00);
							if (aoi.getIeeeAddress() == null)
								aoi.setIeeeAddress(getIeeeAddress_FromNetworkCache(aoi
										.getNetworkAddress()));

							NodeServices _newNodeService = new NodeServices();
							_newNodeService.setAddress(aoi);
							if (_newNodeService.getAddress().getIeeeAddress() == null)
								_newNodeService
										.getAddress()
										.setIeeeAddress(
												getIeeeAddress_FromNetworkCache(_newNodeService
														.getAddress()
														.getNetworkAddress()));

							for (Short x : _s) {
								ActiveEndpoints _n = new ActiveEndpoints();
								_n.setEndPoint(x);
								_newNodeService.getActiveEndpoints().add(_n);
							}
							int _index = -1;
							if ((_index = existIntoNetworkCache(aoi
									.getNetworkAddress())) == -1) {
								getNetworkcache().get(_index).set_nodeServices(
										_newNodeService);
							}
							get_gatewayEventManager().notifyServicesDiscovered(
									_requestIdentifier, _ok, _newNodeService);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(
									_requestIdentifier, _s1, null);
						} catch (GatewayException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(
									_requestIdentifier, _s1, null);
						} catch (Exception e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyServicesDiscovered(
									_requestIdentifier, _s1, null);
						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state");
						get_gatewayEventManager().notifyServicesDiscovered(
								_requestIdentifier, _s1, null);

					}
				}

			};

			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				List<Short> _result = DataLayer.startServiceDiscoverySync(
						timeout, aoi);

				NodeServices _newNodeService = new NodeServices();
				_newNodeService.setAddress(aoi);

				if (_newNodeService.getAddress().getIeeeAddress() == null)
					_newNodeService.getAddress().setIeeeAddress(
							getIeeeAddress_FromNetworkCache(_newNodeService
									.getAddress().getNetworkAddress()));

				for (Short x : _result) {
					ActiveEndpoints _n = new ActiveEndpoints();
					_n.setEndPoint(x);
					_newNodeService.getActiveEndpoints().add(_n);
				}
				int _index = -1;
				if ((_index = existIntoNetworkCache(aoi.getNetworkAddress())) > -1) {
					getNetworkcache().get(_index).set_nodeServices(
							_newNodeService);
				}

				return _newNodeService;
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	public CallbackIdentifierList listCallbacks(int requestIdentifier)
			throws IOException, Exception, GatewayException {
		CallbackIdentifierList toReturn = new CallbackIdentifierList();
		for (CallbackEntry ce : listCallback) {
			if (ce.getProxyIdentifier() == requestIdentifier)
				toReturn.getCallbackIdentifier().add(
						ce.getApsCallbackIdentifier());
		}
		return toReturn;
	}

	public void setGatewayEventListener(GatewayEventListener listener,
			int proxyIdentifier) {
		boolean _listenerFound = false;
		synchronized (getListGatewayEventListener()) {
			for (int i = 0; i < getListGatewayEventListener().size(); i++) {
				if (getListGatewayEventListener().get(i).getProxyIdentifier() == proxyIdentifier) {
					_listenerFound = true;
					break;
				}

			}
		}
		if (!_listenerFound) {
			GatewayDeviceEventEntry<GatewayEventListener> gdee = new GatewayDeviceEventEntry<GatewayEventListener>();
			gdee.setGatewayEventListener(listener);
			gdee.setProxyIdentifier(proxyIdentifier);
			synchronized (getListGatewayEventListener()) {
				getListGatewayEventListener().add(gdee);
			}
		}
	}

	public void sendAPSMessage(long timeout, long _requestIdentifier,
			APSMessage message) throws IOException, Exception, GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

			if (message.getDestinationAddress().getIeeeAddress() != null
					&& message.getDestinationAddress().getNetworkAddress() == null)
				message.getDestinationAddress().setNetworkAddress(
						getShortAddress_FromNetworkCache(message
								.getDestinationAddress().getIeeeAddress()));
			DataLayer.sendApsSync(timeout, message);
		} else
			throw new GatewayException("Gal is not in running state!");
	}

	public void sendAPSWithPartitioning(long timeout, long _requestIdentifier,
			APSMessage message) throws IOException, Exception, GatewayException {
		if (getGatewayStatus() == GatewayStatus.GW_RUNNING)
			getPartitionManager().SendApsWithPartitioning(message);
		else
			throw new GatewayException("Gal is not in running state!");
	}

	public Status leave(final long timeout, final int _requestIdentifier,
			final Address addrOfInterest, final int mask, final boolean Async)
			throws IOException, Exception, GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					Status _s = null;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							_s = DataLayer.leaveSync(timeout, addrOfInterest,
									mask);
							if (_s.getCode() == GatewayConstants.SUCCESS) {
								if (addrOfInterest.getNetworkAddress() == null)
									addrOfInterest
											.setNetworkAddress(getShortAddress_FromNetworkCache(addrOfInterest
													.getIeeeAddress()));

								int _index = -1;
								synchronized (getNetworkcache()) {
									if ((_index = existIntoNetworkCache(addrOfInterest
											.getNetworkAddress())) != -1) {
										getNetworkcache().remove(_index);
									}
								}
							}
							get_gatewayEventManager().notifyleaveResult(_s);
							get_gatewayEventManager()
									.notifyleaveResultExtended(_s,
											addrOfInterest);

						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyleaveResult(
									_requestIdentifier, _s1);
							get_gatewayEventManager()
									.notifyleaveResultExtended(
											_requestIdentifier, _s1,
											addrOfInterest);

						} catch (GatewayException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyleaveResult(
									_requestIdentifier, _s1);
							get_gatewayEventManager()
									.notifyleaveResultExtended(
											_requestIdentifier, _s1,
											addrOfInterest);

						} catch (Exception e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifyleaveResult(
									_requestIdentifier, _s1);
							get_gatewayEventManager()
									.notifyleaveResultExtended(
											_requestIdentifier, _s1,
											addrOfInterest);

						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyleaveResult(
								_requestIdentifier, _s1);
						get_gatewayEventManager().notifyleaveResultExtended(
								_requestIdentifier, _s1, addrOfInterest);

					}

				}

			};

			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				Status _s = DataLayer.leaveSync(timeout, addrOfInterest, mask);
				if (_s.getCode() == GatewayConstants.SUCCESS) {
					if (addrOfInterest.getNetworkAddress() == null)
						addrOfInterest
								.setNetworkAddress(getShortAddress_FromNetworkCache(addrOfInterest
										.getIeeeAddress()));

					int _index = -1;
					synchronized (getNetworkcache()) {
						if ((_index = existIntoNetworkCache(addrOfInterest
								.getNetworkAddress())) != -1) {
							getNetworkcache().remove(_index);
						}
					}
				}
				get_gatewayEventManager().notifyleaveResult(_s);
				get_gatewayEventManager().notifyleaveResultExtended(_s,
						addrOfInterest);
				return _s;
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	public Status permitJoin(final long timeout, final int _requestIdentifier,
			final Address addrOfInterest, final short duration,
			final boolean Async) throws IOException, GatewayException,
			Exception {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {

					Status _s = new Status();
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							_s = DataLayer.permitJoinSync(timeout,
									addrOfInterest, duration, (byte) 0x00);
							get_gatewayEventManager()
									.notifypermitJoinResult(_s);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(
									_requestIdentifier, _s1);
						} catch (GatewayException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(
									_requestIdentifier, _s1);
						} catch (Exception e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(
									_requestIdentifier, _s1);
						}
					} else {
						Status _s1 = new Status();
						_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s1.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifypermitJoinResult(
								_requestIdentifier, _s1);
					}

				}

			};

			thr.start();
			return null;
		} else {
			Status _s;
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

				try {
					_s = DataLayer.permitJoinSync(timeout, addrOfInterest,
							duration, (byte) 0x00);
					get_gatewayEventManager().notifypermitJoinResult(_s);
					return _s;
				} catch (IOException e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(
							_requestIdentifier, _s1);
					throw e;
				} catch (GatewayException e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(
							_requestIdentifier, _s1);
					throw e;
				} catch (Exception e) {
					Status _s1 = new Status();
					_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
					_s1.setMessage(e.getMessage());
					get_gatewayEventManager().notifypermitJoinResult(
							_requestIdentifier, _s1);
					throw e;
				}
			} else
				throw new GatewayException("Gal is not in running state!");
		}

	}

	public Status permitJoinAll(final long timeout,
			final int _requestIdentifier, final short duration,
			final boolean Async) throws IOException, Exception {
		final Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					Status _s;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							_s = DataLayer.permitJoinAllSync(timeout, _add,
									duration, (byte) 0x00);
							get_gatewayEventManager()
									.notifypermitJoinResult(_s);
						} catch (IOException e) {
							Status _s1 = new Status();
							_s1.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s1.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(
									_s1);
						} catch (Exception e) {
							Status _s2 = new Status();
							_s2.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s2.setMessage(e.getMessage());
							get_gatewayEventManager().notifypermitJoinResult(
									_s2);
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

				Status _s = DataLayer.permitJoinAllSync(timeout, _add,
						duration, (byte) 0x00);
				get_gatewayEventManager().notifypermitJoinResult(_s);
				return _s;
			} else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	public void startNodeDiscovery(long timeout, int requestIdentifier,
			int discoveryMask) throws GatewayException {
		int _index = -1;

		synchronized (get_gatewayEventManager()) {
			if ((_index = existIntolistGatewayEventListener(requestIdentifier)) != -1) {

				listGatewayEventListener.get(_index).setDiscoveryMask(
						discoveryMask);
				if (!get_Gal_in_Dyscovery_state()) {

					if (discoveryMask != 0 && timeout > 1) {
						long __timeout = 0;

						if (timeout == 0)
							timeout = GatewayConstants.INFINITE_TIMEOUT;

						__timeout = timeout / 1000
								+ ((timeout % 1000 > 0) ? 1 : 0);

						set_Gal_in_Dyscovery_state(true);
						_timeoutGlobalDiscovery = new Timer(
								"TimerGlobalDiscovery for: " + __timeout
										+ " seconds.");
						_timeoutGlobalDiscovery.schedule(
								new stopTaskDiscovery(), timeout);
						for (WrapperWSNNode x : getNetworkcache()) {
							x.setTimerDiscovery(0, false);
						}
						if (PropertiesManager.getDebugEnabled()) {
							logger.info("\n\rGlobal Discovery Started("
									+ __timeout + " seconds)!\n\r");
						}
					}

				} else {
					if (discoveryMask == 0) {
						if (_timeoutGlobalDiscovery != null) {
							_timeoutGlobalDiscovery.cancel();

						}
						set_Gal_in_Dyscovery_state(false);
						if (PropertiesManager.getDebugEnabled()) {
							logger.info("\n\rGlobal Discovery Stopped!\n\r");
						}
					} else {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("\n\rError on discovery: Gal is already on discovery, but your listener is saved!\n\r");
						}
						throw new GatewayException(
								"Gal is already on discovery, but your listener is saved!");
					}
				}
			} else {
				if (PropertiesManager.getDebugEnabled()) {
					logger.error("\n\rError on discovery: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!\n\r");
				}
				throw new GatewayException(
						"No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
			}
		}

	}

	public void subscribeNodeRemoval(long timeout, int requestIdentifier,
			int discoveryFreshness_mask) throws GatewayException {
		int _index = -1;
		synchronized (getListGatewayEventListener()) {
			if ((_index = existIntolistGatewayEventListener(requestIdentifier)) != -1) {

				listGatewayEventListener.get(_index).setFreshnessMask(
						discoveryFreshness_mask);

				if (!get_Gal_in_Freshness_state()) {

					if (discoveryFreshness_mask > 0 && timeout > 1) {
						set_Gal_in_Freshness_state(true);
						for (WrapperWSNNode x : getNetworkcache()) {
							x.setTimerFreshness(0);
						}

						if (PropertiesManager.getDebugEnabled()) {
							logger.info("\n\rGlobal Freshness Started!\n\r");
						}
					}

				} else {
					if (discoveryFreshness_mask == 0) {

						set_Gal_in_Freshness_state(false);

						if (PropertiesManager.getDebugEnabled()) {
							logger.info("\n\rGlobal Freshness Stopped!\n\r");
						}
					} else {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("\n\rError on discovery: Gal is already on freshness, but your listener is saved!\n\r");
						}
						throw new GatewayException(
								"Gal is already on freshness, but your listener is saved!");
					}
				}

			} else {
				if (PropertiesManager.getDebugEnabled()) {
					logger.error("\n\rError on discovery: No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!\n\r");
				}
				throw new GatewayException(
						"No GatewayEventListener found for the User. Set the GatewayEventListener before call this API!");
			}
		}

	}

	public short getTransequenceNumber() {
		if (transequenceNumber < 0xFF)
			transequenceNumber++;
		else
			transequenceNumber = 0x01;
		return transequenceNumber;
	}

	public synchronized GatewayStatus getGatewayStatus() {
		return _gatewayStatus;
	}

	public static Version getVersion() throws IOException, Exception,
			GatewayException {
		Version v = new Version();
		v.setVersionIdentifier((short) 0x01);
		v.setFeatureSetIdentifier((short) 0x00);
		String version = GalController.class.getPackage()
				.getImplementationVersion();
		if (version != null) {
			v.setManufacturerVersion(GalController.class.getPackage()
					.getImplementationVersion());
			v.setVersionIdentifier(Short.parseShort(GalController.class
					.getPackage().getImplementationVersion()));
		}
		v.getRPCProtocol().add(RPCProtocol.REST);
		return v;
	}

	public synchronized void setGatewayStatus(GatewayStatus gatewayStatus) {
		_gatewayStatus = gatewayStatus;
		if (gatewayStatus == GatewayStatus.GW_RUNNING) {
			/* Get The Network Address of the GAL */
			Runnable thr = new MyThread(this) {

				@Override
				public void run() {
					String _NetworkAdd = null;
					BigInteger _IeeeAdd = null;
					try {
						_NetworkAdd = DataLayer.NMLE_GetSync(
								IDataLayer.INTERNAL_TIMEOUT, (short) 0x96);
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal Network Address!");
						}

					}
					try {
						_IeeeAdd = DataLayer
								.readExtAddress(IDataLayer.INTERNAL_TIMEOUT);
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal IEEE Address!");
						}

					}

					WrapperWSNNode galNodeWrapper = new WrapperWSNNode(
							((GalController) this.getParameter()));
					WSNNode galNode = new WSNNode();
					Address _add = new Address();
					_add.setNetworkAddress(Integer.parseInt(_NetworkAdd, 16));
					_add.setIeeeAddress(_IeeeAdd);
					galNode.setAddress(_add);
					galNodeWrapper.set_node(galNode);
					set_GalNode(galNode);
					try {

						Status _permitjoin = DataLayer.permitJoinSync(
								IDataLayer.INTERNAL_TIMEOUT, _add,
								(short) 0x00, (byte) 0x01);

						if (_permitjoin.getCode() != GatewayConstants.SUCCESS) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage("Error on permitJoin(0) for the GAL node on startup!");
							get_gatewayEventManager().notifyGatewayStartResult(
									_st);
						}
						NodeDescriptor _NodeDescriptor = DataLayer
								.getNodeDescriptorSync(
										IDataLayer.INTERNAL_TIMEOUT, _add);
						if (_NodeDescriptor != null) {
							if (galNodeWrapper.get_node()
									.getCapabilityInformation() == null)
								galNodeWrapper.get_node()
										.setCapabilityInformation(
												new MACCapability());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setReceiverOnWhenIdle(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isReceiverOnWhenIdle());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setAllocateAddress(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isAllocateAddress());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setAlternatePanCoordinator(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isAlternatePanCoordinator());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setDeviceIsFFD(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isDeviceIsFFD());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setMainsPowered(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isMainsPowered());
							galNodeWrapper
									.get_node()
									.getCapabilityInformation()
									.setSecuritySupported(
											_NodeDescriptor
													.getMACCapabilityFlag()
													.isSecuritySupported());
						}
					} catch (Exception e) {
						if (PropertiesManager.getDebugEnabled()) {
							logger.error("Error retrieving the Gal Node Descriptor!");
						}

					}

					int _index = -1;
					synchronized (getNetworkcache()) {
						if ((_index = existIntoNetworkCache(_add
								.getNetworkAddress())) == -1) {

							if (galNodeWrapper.get_node()
									.getCapabilityInformation()
									.isReceiverOnWhenIdle()) {
								if (PropertiesManager.getKeepAliveThreshold() > 0) {
									galNodeWrapper.set_discoveryCompleted(true);
									galNodeWrapper.reset_numberOfAttempt();
									galNodeWrapper.setTimerDiscovery(-1, false);
								}
								if (PropertiesManager.getForcePingTimeout() > 0
										&& get_Gal_in_Freshness_state()) {
									galNodeWrapper
											.setTimerFreshness(PropertiesManager
													.getForcePingTimeout());
								}
							} else {
								/* Sleepy end device */
								galNodeWrapper.set_discoveryCompleted(true);
								galNodeWrapper.reset_numberOfAttempt();
								galNodeWrapper.setTimerDiscovery(-1, false);

							}

							getNetworkcache().add(galNodeWrapper);

						} else {
							galNodeWrapper.set_discoveryCompleted(true);
							galNodeWrapper.reset_numberOfAttempt();
							galNodeWrapper.setTimerDiscovery(-1, false);
							getNetworkcache().get(_index).set_node(
									galNodeWrapper.get_node());
						}
					}

					synchronized (_lockerStartDevice) {
						_lockerStartDevice.setId(1);
						_lockerStartDevice.notify();
					}
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
			synchronized (getNetworkcache()) {
				getNetworkcache().clear();
			}
		}

	}

	public long getApsCallbackIdentifier() {
		synchronized (this) {
			if (apsCallbackIdentifier == Long.MAX_VALUE) {
				apsCallbackIdentifier = (long) 1;
			}
			return apsCallbackIdentifier++;
		}
	}

	public synchronized WSNNode get_GalNode() {
		return GalNode;
	}

	public Status clearEndpoint(short endpoint) throws IOException, Exception,
			GatewayException {
		Status _s = DataLayer.clearEndpointSync(endpoint);
		return _s;
	}

	public synchronized void set_GalNode(WSNNode _GalNode) {
		GalNode = _GalNode;
	}

	public synchronized List<WrapperWSNNode> getNetworkcache() {
		return NetworkCache;
	}

	public ServiceDescriptor getServiceDescriptor(final long timeout,
			final int _requestIdentifier, final Address addrOfInterest,
			final short endpoint, boolean Async) throws IOException, Exception,
			GatewayException {
		if (addrOfInterest.getNetworkAddress() == null)
			addrOfInterest
					.setNetworkAddress(getShortAddress_FromNetworkCache(addrOfInterest
							.getIeeeAddress()));

		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					ServiceDescriptor _toRes;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {

							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							_toRes = DataLayer.getServiceDescriptor(timeout,
									addrOfInterest, endpoint);
							_toRes.getAddress().setIeeeAddress(
									getIeeeAddress_FromNetworkCache(_toRes
											.getAddress().getNetworkAddress()));
							get_gatewayEventManager()
									.notifyserviceDescriptorRetrieved(
											_requestIdentifier, _s, _toRes);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager()
									.notifyserviceDescriptorRetrieved(
											_requestIdentifier, _s, null);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager()
									.notifyserviceDescriptorRetrieved(
											_requestIdentifier, _s, null);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager()
								.notifyserviceDescriptorRetrieved(
										_requestIdentifier, _s, null);
					}
				}
			};
			thr.start();
			return null;
		} else {
			if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
				ServiceDescriptor _toRes;
				_toRes = DataLayer.getServiceDescriptor(timeout,
						addrOfInterest, endpoint);
				if (_toRes.getAddress().getIeeeAddress() == null)
					_toRes.getAddress().setIeeeAddress(
							getIeeeAddress_FromNetworkCache(_toRes.getAddress()
									.getNetworkAddress()));
				return _toRes;
			} else
				throw new GatewayException("Gal is not in running state!");

		}
	}

	public BindingList getNodeBindingsSync(final long timeout,
			final int _requestIdentifier, final Address aoi, final short index,
			boolean Async) throws IOException, Exception, GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					BindingList _toRes;
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.SUCCESS);

							_toRes = DataLayer.getNodeBindings(timeout, aoi,
									index);
							get_gatewayEventManager()
									.notifynodeBindingsRetrieved(
											_requestIdentifier, _s, _toRes);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager()
									.notifynodeBindingsRetrieved(
											_requestIdentifier, _s, null);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager()
									.notifynodeBindingsRetrieved(
											_requestIdentifier, _s, null);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifynodeBindingsRetrieved(
								_requestIdentifier, _s, null);
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

	public Status addBindingSync(final long timeout,
			final int _requestIdentifier, final Binding binding,
			final boolean Async) throws IOException, Exception,
			GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {

						try {
							Status _s = DataLayer.addBinding(timeout, binding);
							get_gatewayEventManager().notifybindingResult(
									_requestIdentifier, _s);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifybindingResult(
									_requestIdentifier, _s);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifybindingResult(
									_requestIdentifier, _s);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifybindingResult(
								_requestIdentifier, _s);

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

	public Status removeBindingSync(final long timeout,
			final int _requestIdentifier, final Binding binding,
			final boolean Async) throws IOException, Exception,
			GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _s = DataLayer.removeBinding(timeout,
									binding);
							get_gatewayEventManager().notifyUnbindingResult(
									_requestIdentifier, _s);
						} catch (GatewayException e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyUnbindingResult(
									_requestIdentifier, _s);
						} catch (Exception e) {
							Status _s = new Status();
							_s.setCode((short) GatewayConstants.GENERAL_ERROR);
							_s.setMessage(e.getMessage());
							get_gatewayEventManager().notifyUnbindingResult(
									_requestIdentifier, _s);
						}
					} else {
						Status _s = new Status();
						_s.setCode((short) GatewayConstants.GENERAL_ERROR);
						_s.setMessage("Gal is not in running state!");
						get_gatewayEventManager().notifyUnbindingResult(
								_requestIdentifier, _s);
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

	public Status frequencyAgilitySync(final long timeout,
			final int _requestIdentifier, final short scanChannel,
			final short scanDuration, final boolean Async) throws IOException,
			Exception, GatewayException {
		if (Async) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					if (getGatewayStatus() == GatewayStatus.GW_RUNNING) {
						try {
							Status _st = DataLayer.frequencyAgilitySync(
									timeout, scanChannel, scanDuration);
							get_gatewayEventManager().notifyFrequencyAgility(
									_st);
						} catch (GatewayException e) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage(e.getMessage());
							get_gatewayEventManager().notifyFrequencyAgility(
									_st);
						} catch (Exception e) {
							Status _st = new Status();
							_st.setCode((short) GatewayConstants.GENERAL_ERROR);
							_st.setMessage(e.getMessage());
							get_gatewayEventManager().notifyFrequencyAgility(
									_st);
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
				Status _st = DataLayer.frequencyAgilitySync(timeout,
						scanChannel, scanDuration);
				return _st;
			} else
				throw new GatewayException("Gal is not in running state!");
		}
	}

	/**
	 * return -1 if not Exist; return >0 is the index of the object
	 */
	public synchronized short existIntoNetworkCache(Integer shortAddress) {
		short __indexOnCache = -1;
		for (WrapperWSNNode y : getNetworkcache()) {
			__indexOnCache++;
			if (y.get_node().getAddress().getNetworkAddress()
					.equals(shortAddress))
				return __indexOnCache;

		}
		return -1;
	}

	/**
	 * return null if not Exist; return >0 is the index of the object
	 */
	public synchronized BigInteger getIeeeAddress_FromNetworkCache(
			Integer shortAddress) {

		for (WrapperWSNNode y : getNetworkcache()) {
			if (y.get_node().getAddress().getNetworkAddress()
					.equals(shortAddress))
				return y.get_node().getAddress().getIeeeAddress();
		}
		return null;
	}

	/**
	 * return null if not Exist; return >0 is the index of the object
	 */
	public synchronized Integer getShortAddress_FromNetworkCache(
			BigInteger IeeeAddress) {

		for (WrapperWSNNode y : getNetworkcache()) {
			if (y.get_node().getAddress().getIeeeAddress().equals(IeeeAddress))
				return y.get_node().getAddress().getNetworkAddress();
		}
		return null;
	}

	/**
	 * return -1 if not Exist; return >0 is the index of the object
	 */
	public synchronized short existIntolistGatewayEventListener(
			long requestIdentifier) {
		short __indexOnList = -1;
		for (GatewayDeviceEventEntry y : listGatewayEventListener) {
			__indexOnList++;
			if (y.getProxyIdentifier() == requestIdentifier)
				return __indexOnList;

		}
		return -1;
	}

	class stopTaskDiscovery extends TimerTask {
		@Override
		public void run() {
			_timeoutGlobalDiscovery.cancel();
			set_Gal_in_Dyscovery_state(false);
			if (PropertiesManager.getDebugEnabled())
				logger.info("Discovery completed for timeout period elapsed!");
		}
	}

}
