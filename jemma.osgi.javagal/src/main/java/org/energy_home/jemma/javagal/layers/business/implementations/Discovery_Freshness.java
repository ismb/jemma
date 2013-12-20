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
package org.energy_home.jemma.javagal.layers.business.implementations;

import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.AssociatedDevices;
import org.energy_home.jemma.zgd.jaxb.LogicalType;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.SonNode;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.TxOptions;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

import java.io.IOException;
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

import com.sun.org.apache.xml.internal.utils.BoolStack;

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

	public void StartDiscovery(final Address node) {
		DiscoveryMng _newDsc = null;
		WrapperWSNNode __currentNodeWrapper = null;
		int _indexParent = -1;
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("\n\rStarting Discovery for node:"
					+ node.getNetworkAddress() + "\n\r");
		}
		try {

			synchronized (gal.getNetworkcache()) {
				_indexParent = gal.existIntoNetworkCache(node
						.getNetworkAddress());
				if (_indexParent != -1) {
					__currentNodeWrapper = gal.getNetworkcache().get(
							_indexParent);
					synchronized (__currentNodeWrapper) {
						__currentNodeWrapper.set_onDiscovery(true);
					}
				} else
					return;
			}

			byte[] _commandLqiRequest = new byte[2];
			_commandLqiRequest[0] = (byte) gal.getTransequenceNumber();/* TranseqNumber */
			_commandLqiRequest[1] = 0x00;/* Start Index */
			_newDsc = new DiscoveryMng();
			_newDsc.set_Destination_NetworkAddress(node.getNetworkAddress());
			_newDsc.set_TranseqNumber(_commandLqiRequest[0]);

			synchronized (_DiscoveryTable) {
				_DiscoveryTable.add(_newDsc);
			}

			APSMessage _LQIReq = new APSMessage();
			_LQIReq.setClusterID(0x0031)/* Mngm LQI Req */;
			_LQIReq.setProfileID(0x0000);
			_LQIReq.setDestinationAddressMode((long) 0x02);// Short
			Address _add = new Address();
			_add.setNetworkAddress(node.getNetworkAddress());
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
							+ __currentNodeWrapper.get_node().getAddress()
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
								+ __currentNodeWrapper.get_node().getAddress()
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
							newNodeChild.setParentAddress(node);
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
								if (indexOnCache == -1) {
									/*
									 * node child not exists
									 */

									if (newNodeWrapperChild.get_node()
											.getCapabilityInformation()
											.isReceiverOnWhenIdle()) {
										if (x._Device_Type == 0x00 /*
																	 * LogicalType.
																	 * COORDINATOR
																	 */
												|| x._Device_Type == 0x01 /*
																		 * LogicalType
																		 * .
																		 * Router
																		 */) {
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
														.setTimerDiscovery(0,
																false);

											}
										} else /*
												 * LogicalType != COOORDINATOR
												 * && Router
												 */

										{
											newNodeWrapperChild
													.set_discoveryCompleted(true);
											
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
												.set_onDiscovery(false);
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
												+ node.getNetworkAddress()
												+ "\n\r");
									}
								} else {

									if (gal.getPropertiesManager()
											.getDebugEnabled()) {
										logger.info("Found an existing Node:"
												+ newNodeWrapperChild
														.get_node()
														.getAddress()
														.getNetworkAddress()
												+ " into NeighborTableListCount of:"
												+ node.getNetworkAddress()
												+ "\n\r");
									}
								}
							}
						}
					}
					synchronized (__currentNodeWrapper) {
						/*Start of the freshness and ForcePing for all nodes NOT Sleepy and NOT Gal*/
						if (__currentNodeWrapper.get_node().getAddress()
								.getNetworkAddress() != gal.get_GalNode()
								.get_node().getAddress().getNetworkAddress()) {
							__currentNodeWrapper.setTimerFreshness(gal
									.getPropertiesManager()
									.getKeepAliveThreshold());

						}
						/*StartForcePing for GAL*/
						else
						{
							//TODO MARCO
							/*
							__currentNodeWrapper.setTimerForcePing(gal
									.getPropertiesManager()
									.getForcePingTimeout());
							*/
						}
						__currentNodeWrapper.set_discoveryCompleted(true);
						__currentNodeWrapper.get_node().getAssociatedDevices()
								.clear();
						__currentNodeWrapper.get_node().getAssociatedDevices()
								.add(_AssociatedDevices);
						__currentNodeWrapper.set_Mgmt_LQI_rsp(_newDsc
								.get_response());
						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.info("\n\rDiscovery completed for node: "
									+ __currentNodeWrapper.get_node()
											.getAddress().getNetworkAddress());
						}
						Status _st = new Status();
						_st.setCode((short) GatewayConstants.SUCCESS);
						try {
							gal.get_gatewayEventManager().nodeDiscovered(_st,
									__currentNodeWrapper.get_node());
						} catch (Exception e) {
							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.error("\n\rError on nodeDiscovered: "
										+ e.getMessage() + "\n\r");
							}
						}
					}
				}
			}

		} catch (Exception e) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("\n\rError on discovery request for node: "
						+ __currentNodeWrapper.get_node().getAddress()
								.getNetworkAddress() + "\n\rError message: "
						+ e.getMessage());
			}

		} finally {
			synchronized (__currentNodeWrapper) {
				__currentNodeWrapper.set_onDiscovery(false);
			}
			synchronized (_DiscoveryTable) {
				_DiscoveryTable.remove(_newDsc);
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
