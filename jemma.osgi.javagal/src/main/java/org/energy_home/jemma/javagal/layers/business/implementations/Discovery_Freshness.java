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
package org.energy_home.jemma.javagal.layers.business.implementations;

import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.AssociatedDevices;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.SonNode;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.TxOptions;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.NeighborTableLis_Record;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;


/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class Discovery_Freshness {
	GalController gal = null;

	public Discovery_Freshness(GalController _gal) {
		gal = _gal;
	}

	private final static Log logger = LogFactory
			.getLog(Discovery_Freshness.class);
	private static List<DiscoveryMng> _DiscoveryTable = Collections
			.synchronizedList(new LinkedList<DiscoveryMng>());

	/**
	 * return -1 if not Exist; return > 0 is the index of the object
	 */
	private synchronized static short existIntoDiscoveryTable(
			int tranSeqNumber, Integer shortAddress) {
		/* Check if the request exists into the table DiscoveryMng */
		short __index = -1;
		for (DiscoveryMng x : _DiscoveryTable) {
			__index++;
			if (tranSeqNumber > -1) {
				if ((x.get_TranseqNumber() == tranSeqNumber)
						&& (shortAddress.equals(x
								.get_Destination_NetworkAddress()))) {
					return __index;

				}
			} else {
				if (shortAddress.equals(x.get_Destination_NetworkAddress()))
					return __index;
			}
		}
		return -1;
	}

	/**
	 * return -1 if not Exist; return > 0 is the index of the object
	 */

	public void StartDiscovery(final Address _nodeSource,
			final Address _nodeDestination) {
		DiscoveryMng _newDsc = null;
		WrapperWSNNode _parent = null;
		int _indexParent = -1;
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("\n\rStarting Discovery for node:"
					+ _nodeDestination.getNetworkAddress() + "\n\r");
		}
		try {

			synchronized (gal.getNetworkcache()) {
				_indexParent = gal.existIntoNetworkCache(_nodeDestination
						.getNetworkAddress());
				if (_indexParent != -1) {
					_parent = gal.getNetworkcache().get(_indexParent);
					synchronized (_parent) {
						_parent.set_onDiscovery(true);
					}
				} else
					return;
			}

			byte[] _commandLqiRequest = new byte[2];
			_commandLqiRequest[0] = (byte) gal.getTransequenceNumber();/* TranseqNumber */
			_commandLqiRequest[1] = 0x00;/* Start Index */
			_newDsc = new DiscoveryMng();
			_newDsc.set_Destination_NetworkAddress(_nodeDestination
					.getNetworkAddress());
			_newDsc.set_TranseqNumber(_commandLqiRequest[0]);

			synchronized (_DiscoveryTable) {
				_DiscoveryTable.add(_newDsc);
			}

			APSMessage _LQIReq = new APSMessage();
			_LQIReq.setClusterID(0x0031)/* Mngm LQI Req */;
			_LQIReq.setProfileID(0x0000);
			_LQIReq.setDestinationAddressMode((long) 0x02);// Short
			Address _add = new Address();
			_add.setNetworkAddress(_nodeDestination.getNetworkAddress());
			_LQIReq.setDestinationAddress(_add);
			_LQIReq.setDestinationEndpoint((short) 0x00);
			_LQIReq.setSourceEndpoint((short) 0x00);
			_LQIReq.setRadius((short) 0x0A);
			TxOptions _op = new TxOptions();
			_op.setAcknowledged(false);
			_op.setPermitFragmentation(false);
			_op.setSecurityEnabled(false);
			_op.setUseNetworkKey(false);
			_LQIReq.setTxOptions(_op);
			_LQIReq.setData(_commandLqiRequest);
			Status _stat = null;
			_stat = gal.getDataLayer().sendApsSync(IDataLayer.INTERNAL_TIMEOUT,
					_LQIReq);
			/* Check no confirm received */
			if (_stat == null || _stat.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error(

					"\n\rNo Discovery request confirm received or confirm status not success for node: "
							+ _parent.get_node().getAddress()
									.getNetworkAddress() + "\n\r");
				}

			} else/* Confirm Received -- Waiting Response */
			{
				synchronized (_newDsc) {
					try {
						_newDsc.wait(IDataLayer.INTERNAL_TIMEOUT);
					} catch (InterruptedException e) {

					}
				}
				if (_newDsc.get_response() == null) {
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.error("\n\rDiscovery response null for node: "
								+ _parent.get_node().getAddress()
										.getNetworkAddress() + "\n\r");
					}

				} else /* Response Received */
				{

					/*
					 * Start the discovery for any child and add the child to
					 * parent node
					 */

					AssociatedDevices _AssociatedDevices = new AssociatedDevices();
					if (_newDsc.get_response().NeighborTableList != null
							&& _newDsc.get_response().NeighborTableList.size() > 0) {
						for (NeighborTableLis_Record x : _newDsc.get_response().NeighborTableList) {
							short indexOnCache = -1;
							/* Only Coordinator or Router */
							Address _addressChild = new Address();
							_addressChild.setNetworkAddress(x._Network_Address);
							BigInteger bi = BigInteger
									.valueOf(x._Extended_Address);
							_addressChild.setIeeeAddress(bi);
							WrapperWSNNode newNodeWrapperChild = new WrapperWSNNode(
									gal);
							WSNNode newNodeChild = new WSNNode();
							newNodeChild.setAddress(_addressChild);
							MACCapability _mac = new MACCapability();
							_mac.setReceiverOnWhenIdle((x._RxOnWhenIdle == 1) ? true
									: false);
							newNodeChild.setCapabilityInformation(_mac);
							newNodeChild.setParentAddress(_nodeSource);
							newNodeChild.setStartIndex(x._Depth);
							/* Add child node to parent node */
							SonNode _SonNode = new SonNode();
							_SonNode.setShortAddr(_addressChild
									.getNetworkAddress());
							_AssociatedDevices.getSonNode().add(_SonNode);
							newNodeWrapperChild.set_node(newNodeChild);
							synchronized (gal.getNetworkcache()) {
								indexOnCache = gal
										.existIntoNetworkCache(newNodeWrapperChild
												.get_node().getAddress()
												.getNetworkAddress());
								/*
								 * if not exists, add the new node into the
								 * cache and send a notification
								 */
								if (indexOnCache == -1) {
									if (newNodeWrapperChild.get_node()
											.getCapabilityInformation()
											.isReceiverOnWhenIdle()) {
										if (x._Device_Type == 0x00
												|| x._Device_Type == 0x01) {
											newNodeWrapperChild
													.set_discoveryCompleted(false);
											if (gal.getPropertiesManager()
													.getKeepAliveThreshold() > 0
													&& gal.get_Gal_in_Dyscovery_state()) {
												if (gal.getPropertiesManager()
														.getDebugEnabled()) {
													logger.info(

													"\n\rScheduling Discovery for node:"
															+ newNodeWrapperChild
																	.get_node()
																	.getAddress()
																	.getNetworkAddress());
												}
												newNodeWrapperChild
														.reset_numberOfAttempt();
												newNodeWrapperChild
														.setTimerDiscovery(0,
																false);
												newNodeWrapperChild
														.setTimerFreshness(0);

											}
										} else {
											newNodeWrapperChild
													.set_discoveryCompleted(true);
											newNodeWrapperChild
													.reset_numberOfAttempt();
											newNodeWrapperChild
													.setTimerDiscovery(-1,
															false);
											Status _s = new Status();
											_s.setCode((short) 0x00);
											_s.setMessage("Successful - Discovery Algorithm");
											gal.get_gatewayEventManager()
													.nodeDiscovered(
															_s,
															newNodeWrapperChild
																	.get_node());

										}
									} else {
										/* If Sleepy EndDevice */
										newNodeWrapperChild
												.set_discoveryCompleted(true);
										newNodeWrapperChild
												.reset_numberOfAttempt();
										newNodeWrapperChild.setTimerDiscovery(
												-1, false);
										Status _s = new Status();
										_s.setCode((short) 0x00);
										_s.setMessage("Successful - Discovery Algorithm");
										gal.get_gatewayEventManager()
												.nodeDiscovered(
														_s,
														newNodeWrapperChild
																.get_node());
									}

									gal.getNetworkcache().add(
											newNodeWrapperChild);
									if (gal.getPropertiesManager()
											.getDebugEnabled()) {
										logger.info("Discovery:Found new Node:"
												+ newNodeWrapperChild
														.get_node()
														.getAddress()
														.getNetworkAddress()
												+ " from NeighborTableListCount of:"
												+ _nodeSource
														.getNetworkAddress()
												+ "\n\r");
									}
								} else {
									gal.getNetworkcache()
											.get(indexOnCache)
											.set_node(
													newNodeWrapperChild
															.get_node());
									if (gal.getPropertiesManager()
											.getDebugEnabled()) {
										logger.info("Found an existing Node:"
												+ newNodeWrapperChild
														.get_node()
														.getAddress()
														.getNetworkAddress()
												+ " into NeighborTableListCount of:"
												+ _nodeSource
														.getNetworkAddress()
												+ "\n\r");
									}
								}
							}
						}
					}
					synchronized (_parent) {
						_parent.reset_numberOfAttempt();
						_parent.set_discoveryCompleted(true);
						_parent.get_node().getAssociatedDevices().clear();
						_parent.get_node().getAssociatedDevices()
								.add(_AssociatedDevices);
						_parent.set_Mgmt_LQI_rsp(_newDsc.get_response());
						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.info("\n\rDiscovery completed for node: "
									+ _parent.get_node().getAddress()
											.getNetworkAddress());
						}
					}
				}
			}

		} catch (Exception e) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("\n\rError on discovery request for node: "
						+ _parent.get_node().getAddress().getNetworkAddress()
						+ "\n\rError message: " + e.getMessage());
			}

		} finally {
			synchronized (gal.getNetworkcache()) {
				_indexParent = gal.existIntoNetworkCache(_nodeDestination
						.getNetworkAddress());
				if (_indexParent != -1) {
					_parent = gal.getNetworkcache().get(_indexParent);
					synchronized (_parent) {
						_parent.set_onDiscovery(false);
						_parent.setTimerDiscovery(gal.getPropertiesManager()
								.getForcePingTimeout(), false);
					}
				}
			}
			synchronized (_DiscoveryTable) {
				_DiscoveryTable.remove(_newDsc);
			}
		}
	}

	public void StartFreshness(final Address _nodeSource,
			final Address _nodeDestination) {
		DiscoveryMng _newDsc = null;
		WrapperWSNNode _parent = null;
		int _indexParent = -1;
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("\n\rStarting Freshness for node:"
					+ _nodeDestination.getNetworkAddress() + "\n\r");
		}
		try {

			synchronized (gal.getNetworkcache()) {
				_indexParent = gal.existIntoNetworkCache(_nodeDestination
						.getNetworkAddress());
				if (_indexParent != -1)
					_parent = gal.getNetworkcache().get(_indexParent);
				else
					return;
			}

			byte[] _commandLqiRequest = new byte[2];
			_commandLqiRequest[0] = (byte) gal.getTransequenceNumber();/* TranseqNumber */
			_commandLqiRequest[1] = 0x00;/* Start Index */
			_newDsc = new DiscoveryMng();
			_newDsc.set_Destination_NetworkAddress(_nodeDestination
					.getNetworkAddress());
			_newDsc.set_TranseqNumber(_commandLqiRequest[0]);

			synchronized (_DiscoveryTable) {
				_DiscoveryTable.add(_newDsc);
			}

			APSMessage _LQIReq = new APSMessage();
			_LQIReq.setClusterID(0x0031)/* Mngm LQI Req */;
			_LQIReq.setProfileID(0x0000);
			_LQIReq.setDestinationAddressMode((long) 0x02);// Short
			Address _add = new Address();
			_add.setNetworkAddress(_nodeDestination.getNetworkAddress());
			_LQIReq.setDestinationAddress(_add);
			_LQIReq.setDestinationEndpoint((short) 0x00);
			_LQIReq.setSourceEndpoint((short) 0x00);
			_LQIReq.setRadius((short) 0x0A);
			TxOptions _op = new TxOptions();
			_op.setAcknowledged(false);
			_op.setPermitFragmentation(false);
			_op.setSecurityEnabled(false);
			_op.setUseNetworkKey(false);
			_LQIReq.setTxOptions(_op);
			_LQIReq.setData(_commandLqiRequest);

			Status _stat = null;
			_stat = gal.getDataLayer().sendApsSync(IDataLayer.INTERNAL_TIMEOUT,
					_LQIReq);

			/* Check no confirm received */
			if (_stat == null || _stat.getCode() != 0) {
				synchronized (_parent) {
					_parent.set_numberOfAttempt();

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.error(

						"\n\r*********Error:Timeout on (Freshness)LQI Request Confirm! Address:"
								+ _nodeDestination.getNetworkAddress()
								+ " - NumberOfAttempt:"
								+ _parent.get_numberOfAttempt());
					}
					if (_parent.get_numberOfAttempt() >= gal
							.getPropertiesManager()
							.getKeepAliveNumberOfAttempt()) {
						_parent.abortTimers();
						if (_parent.is_discoveryCompleted()) {
							Status _st = new Status();
							_st.setCode((short) 0x00);
							_st.setMessage("Node removed");
							gal.get_gatewayEventManager().nodeRemoved(_st,
									_parent.get_node());
						}
						synchronized (gal.getNetworkcache()) {
							gal.getNetworkcache().remove(_indexParent);

						}
					} else {
						if (gal.getPropertiesManager().getKeepAliveThreshold() > 0
								&& gal.get_Gal_in_Freshness_state()) {
							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.info("\n\rScheduling Freshness for node:"
										+ _parent.get_node().getAddress()
												.getNetworkAddress());
							}
							_parent.setTimerFreshness(gal
									.getPropertiesManager()
									.getForcePingTimeout());
						}
					}
				}

			} else/* Confirm Received -- Waiting Response */
			{
				synchronized (_newDsc) {
					try {
						_newDsc.wait(IDataLayer.INTERNAL_TIMEOUT);
					} catch (InterruptedException e) {

					}
				}
				if (_newDsc.get_response() == null) {
					synchronized (_parent) {
						_parent.set_numberOfAttempt();
						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.error(

							"\n\r*********Error:Timeout on (Freshness)LQI Response! Address:"
									+ _nodeDestination.getNetworkAddress()
									+ " - NumberOfAttempt:"
									+ _parent.get_numberOfAttempt());
						}
						if (_parent.get_numberOfAttempt() >= gal
								.getPropertiesManager()
								.getKeepAliveNumberOfAttempt()) {
							_parent.abortTimers();
							if (_parent.is_discoveryCompleted()) {
								Status _st = new Status();
								_st.setCode((short) 0x00);
								_st.setMessage("Node removed");
								gal.get_gatewayEventManager().nodeRemoved(_st,
										_parent.get_node());
							}
							synchronized (gal.getNetworkcache()) {
								gal.getNetworkcache().remove(_indexParent);

							}
						} else {
							if (gal.getPropertiesManager()
									.getKeepAliveThreshold() > 0
									&& gal.get_Gal_in_Freshness_state()) {
								if (gal.getPropertiesManager()
										.getDebugEnabled()) {
									logger.info(

									"\n\rScheduling Freshness for node:"
											+ _parent.get_node().getAddress()
													.getNetworkAddress());
								}
								_parent.setTimerFreshness(gal
										.getPropertiesManager()
										.getForcePingTimeout());
							}
						}
					}

				} else /* Response Received */
				{
					AssociatedDevices _AssociatedDevices = new AssociatedDevices();
					if (_newDsc.get_response().NeighborTableList != null
							&& _newDsc.get_response().NeighborTableList.size() > 0)
						for (NeighborTableLis_Record x : _newDsc.get_response().NeighborTableList) {
							Address _addressChild = new Address();
							_addressChild.setNetworkAddress(x._Network_Address);
							WrapperWSNNode newNodeWrapperChild = new WrapperWSNNode(
									gal);
							WSNNode newNodeChild = new WSNNode();
							newNodeChild.setAddress(_addressChild);
							MACCapability _mac = new MACCapability();
							_mac.setReceiverOnWhenIdle((x._RxOnWhenIdle == 1) ? true
									: false);
							newNodeChild.setCapabilityInformation(_mac);
							newNodeChild.setParentAddress(_nodeSource);
							newNodeChild.setStartIndex(x._Depth);
							/* Add child node to parent node */
							SonNode _SonNode = new SonNode();
							_SonNode.setShortAddr(_addressChild
									.getNetworkAddress());
							_AssociatedDevices.getSonNode().add(_SonNode);
							newNodeWrapperChild.set_node(newNodeChild);
						}
					synchronized (gal.getNetworkcache()) {
						_indexParent = gal
								.existIntoNetworkCache(_nodeDestination
										.getNetworkAddress());
						if (_indexParent != -1) {
							_parent = gal.getNetworkcache().get(_indexParent);
							synchronized (_parent) {
								_parent.get_node().getAssociatedDevices()
										.clear();
								_parent.get_node().getAssociatedDevices()
										.add(_AssociatedDevices);
								if (_parent.get_node().getAddress()
										.getIeeeAddress() == null)
									_parent.get_node()
											.getAddress()
											.setIeeeAddress(
													gal.getIeeeAddress_FromNetworkCache(_parent
															.get_node()
															.getAddress()
															.getNetworkAddress()));

							}
							Status _st = new Status();
							_st.setCode((short) 0x00);
							_st.setMessage("Freshness");
							gal.get_gatewayEventManager()
							.nodeDiscovered(_st, _parent.get_node());
						} else
							return;
					}

				}
			}

		} catch (Exception e) {
			synchronized (_parent) {
				_parent.set_numberOfAttempt();

				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error(

					"\n\r*********Error on (Freshness)LQI Request! Address:"
							+ _nodeDestination.getNetworkAddress()
							+ " - NumberOfAttempt:"
							+ _parent.get_numberOfAttempt());
				}
				if (_parent.get_numberOfAttempt() >= gal.getPropertiesManager()
						.getKeepAliveNumberOfAttempt()) {
					_parent.abortTimers();
					if (_parent.is_discoveryCompleted()) {
						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.info(

							"\n\rRemoving node Address:"
									+ _nodeDestination.getNetworkAddress()
									+ "\n\r");
						}
						Status _st = new Status();
						_st.setCode((short) 0x00);
						_st.setMessage("Node removed");
						try {
							gal.get_gatewayEventManager().nodeRemoved(_st,
									_parent.get_node());
						} catch (Exception e1) {
							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.error("\n\rError calling GatewayEventManager.nodeRemoved\n\r");
							}
						}
					}
					synchronized (gal.getNetworkcache()) {
						gal.getNetworkcache().remove(_indexParent);
					}
				} else {
					if (gal.getPropertiesManager().getKeepAliveThreshold() > 0
							&& gal.get_Gal_in_Freshness_state()) {
						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.info("\n\rScheduling Freshness for node:"
									+ _parent.get_node().getAddress()
											.getNetworkAddress());
						}
						_parent.setTimerFreshness(gal.getPropertiesManager()
								.getForcePingTimeout());
					}
				}
			}

		} finally {
			synchronized (_DiscoveryTable) {
				_DiscoveryTable.remove(_newDsc);
			}
			synchronized (gal.getNetworkcache()) {
				_indexParent = gal.existIntoNetworkCache(_nodeDestination
						.getNetworkAddress());
				if (_indexParent != -1) {
					_parent = gal.getNetworkcache().get(_indexParent);
					if (gal.getPropertiesManager().getForcePingTimeout() > 0) {
						_parent.setTimerFreshness(gal.getPropertiesManager()
								.getForcePingTimeout());
					}
				}
			}
		}
	}

	public static void Mgmt_LQI_Response(APSMessageEvent message) {
		Mgmt_LQI_rsp _res = new Mgmt_LQI_rsp(message.getData());
		short index = -1;
		synchronized (_DiscoveryTable) {
			index = existIntoDiscoveryTable(_res._TranseqNumber, message
					.getSourceAddress().getNetworkAddress());
			if (index == -1)
				return;
			else {
				DiscoveryMng i = _DiscoveryTable.get(index);
				synchronized (i) {
					i.set_response(_res);
					i.notify();
				}
			}
		}

	}

}

class DiscoveryMng {
	private byte _TranseqNumber;
	private Mgmt_LQI_rsp _response;
	private int _Destination_NetworkAddress;

	public DiscoveryMng() {
		_TranseqNumber = -1;
		_Destination_NetworkAddress = -1;
		_response = null;
	}

	public short get_TranseqNumber() {
		return _TranseqNumber;
	}

	public void set_TranseqNumber(byte _TranseqNumber) {
		this._TranseqNumber = _TranseqNumber;
	}

	public int get_Destination_NetworkAddress() {
		return _Destination_NetworkAddress;
	}

	public void set_Destination_NetworkAddress(int _Destination_NetworkAddress) {
		this._Destination_NetworkAddress = _Destination_NetworkAddress;
	}

	public Mgmt_LQI_rsp get_response() {
		return _response;
	}

	public void set_response(Mgmt_LQI_rsp _response) {
		this._response = _response;
	}

}

class Freshness_Mng {
	private boolean _completed;
	private WSNNode _node;
	private int _retryCounter;

	public Freshness_Mng() {
		_completed = false;
		_retryCounter = 0;

	}

	public boolean is_completed() {
		return _completed;
	}

	public void set_completed(boolean _completed) {
		this._completed = _completed;
	}

	public WSNNode get_node() {
		return _node;
	}

	public void set_node(WSNNode _node) {
		this._node = _node;
	}

	public int get_retryCounter() {
		return _retryCounter;
	}

	public void set_retryCounter(int _retryCounter) {
		this._retryCounter = _retryCounter;
	}
}
