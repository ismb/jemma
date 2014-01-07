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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.GatewayStatus;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.MyThread;
import org.energy_home.jemma.javagal.layers.object.NeighborTableLis_Record;
import org.energy_home.jemma.javagal.layers.object.TypeFunction;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;

import sun.org.mozilla.javascript.internal.EcmaError;

import com.sun.org.apache.xml.internal.utils.BoolStack;
import com.sun.org.apache.xpath.internal.compiler.FunctionTable;

/**
 * Manages received APS messages for the discovery / Freshness / ForcePing
 * Algorithm.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Discovery_Freshness_ForcePing {
	GalController gal = null;
	int NUMBEROFATTEMPTSECONDS = 4;

	public Discovery_Freshness_ForcePing(GalController _gal) {
		gal = _gal;
	}

	private final static Log logger = LogFactory.getLog(Discovery_Freshness_ForcePing.class);
	private static List<DiscoveryMng> _Table = Collections.synchronizedList(new LinkedList<DiscoveryMng>());

	/**
	 * return -1 if not Exist; return > 0 is the index of the object
	 */
	private synchronized static short existIntoTable(int tranSeqNumber, Integer shortAddress) {
		/* Check if the request exists into the table DiscoveryMng */
		short __index = -1;
		for (DiscoveryMng x : _Table) {
			__index++;
			if (tranSeqNumber > -1) {
				if ((x.get_TranseqNumber() == tranSeqNumber) && (shortAddress.equals(x.get_Destination_NetworkAddress()))) {
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
	 * Create the Aps for the Lqi-Request command
	 * @param _transeqNumber --> The counter of the current ApsMessage
	 * @param startIndex --> The index of the Lqi table that will be read
	 * @param node --> The address of the destination node
	 */
	private APSMessage createApsMessaggeLqi_Req(byte _transeqNumber, int startIndex, Address node) {

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
		byte[] _commandLqiRequest = new byte[2];
		_commandLqiRequest[0] = _transeqNumber;/* TranseqNumber */
		_commandLqiRequest[1] = (byte) startIndex;/* Start Index */
		_LQIReq.setData(_commandLqiRequest);
		return _LQIReq;
	}

	
	/**
	 * Send the Lqi_Request for the selected address. Then manages the Lqi_Response
	 */
	public void startLqi(Address node, TypeFunction function, short startIndex) {

		String funcionName = null;
		byte TranseqNumber = (byte) gal.getTransequenceNumber();
		if (gal.getGatewayStatus() == GatewayStatus.GW_RUNNING) {

			if (function == TypeFunction.DISCOVERY)
				funcionName = "Discovery";
			else if (function == TypeFunction.FRESHNESS)
				funcionName = "Freshness";
			else if (function == TypeFunction.FORCEPING)
				funcionName = "ForcePing";

			WrapperWSNNode __currentNodeWrapper = null;
			int _indexParent = -1;
			_indexParent = gal.existIntoNetworkCache(node.getNetworkAddress());
			if (_indexParent != -1) {
				__currentNodeWrapper = gal.getNetworkcache().get(_indexParent);
			} else
				return;

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("\n\rStarting " + funcionName + " for node:" + node.getNetworkAddress() + " -- StartIndex:" + startIndex + "\n\r");
			}

			DiscoveryMng _newDsc = null;
			try {

				_newDsc = new DiscoveryMng();
				_newDsc.set_Destination_NetworkAddress(node.getNetworkAddress());
				_newDsc.set_TranseqNumber(TranseqNumber);
				synchronized (_Table) {
					_Table.add(_newDsc);
				}
				Status _stat = null;
				_stat = gal.getDataLayer().sendApsSync(IDataLayer.INTERNAL_TIMEOUT, createApsMessaggeLqi_Req(TranseqNumber, startIndex, node));

				/* Check no confirm received */
				if (_stat == null || _stat.getCode() != 0) {
					manageError(function, startIndex, __currentNodeWrapper, _indexParent, new Exception("LqiReq.Confirm not received!"));
				} else/* Confirm Received -- Waiting Response */
				{
					synchronized (_newDsc) {
						try {
							_newDsc.wait(IDataLayer.INTERNAL_TIMEOUT);
						} catch (InterruptedException e) {

						}
					}
					if (_newDsc.get_response() == null) {
						manageError(function, startIndex, __currentNodeWrapper, _indexParent, new Exception("LqiRsp not received!"));
					} else /* Response Received */
					{
						short _totalLqi = _newDsc.get_response()._NeighborTableEntries;
						short _indexLqi = _newDsc.get_response()._StartIndex;
						short _LqiListCount = _newDsc.get_response()._NeighborTableListCount;

						/*
						 * Start the discovery for any child and add the child
						 * to parent node
						 */

						AssociatedDevices _AssociatedDevices = new AssociatedDevices();
						if (_newDsc.get_response().NeighborTableList != null && _newDsc.get_response().NeighborTableList.size() > 0) {
							for (NeighborTableLis_Record x : _newDsc.get_response().NeighborTableList) {
								manageChildNode(node, function, funcionName, _AssociatedDevices, x);
							}
						}

						synchronized (__currentNodeWrapper) {
							__currentNodeWrapper.reset_numberOfAttempt();
							__currentNodeWrapper.get_node().getAssociatedDevices().clear();
							__currentNodeWrapper.get_node().getAssociatedDevices().add(_AssociatedDevices);

							if ((_indexLqi + _LqiListCount) < _totalLqi) {
								if (_LqiListCount == 0x00) {
									if (gal.getPropertiesManager().getDebugEnabled()) {
										logger.warn("patch that correct a 4noks bug - 07-12-2011");
									}
									return;
								} else {
									if (__currentNodeWrapper.get_Mgmt_LQI_rsp() != null && _newDsc.get_response().NeighborTableList != null) {

										if (__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.size() > 0) {
											if (startIndex == 0x00) {
												__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.clear();
											}
											__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.addAll(_newDsc.get_response().NeighborTableList);
										} else
											__currentNodeWrapper.set_Mgmt_LQI_rsp(_newDsc.get_response());
									} else
										__currentNodeWrapper.set_Mgmt_LQI_rsp(_newDsc.get_response());
								}
								__currentNodeWrapper.set_discoveryCompleted(true);
								List<Object> parameters = new ArrayList<Object>();
								short nextStartIndex = (short) (_indexLqi + _LqiListCount);
								parameters.add(nextStartIndex);
								parameters.add(node);
								parameters.add(function);

								Runnable thr = new MyThread(parameters) {
									@Override
									public void run() {
										List<Object> parameters = (List<Object>) (this.getParameter());
										Short _indexLqi = (Short) parameters.get(0);
										Address node = (Address) parameters.get(1);
										TypeFunction function = (TypeFunction) parameters.get(2);
										if (gal.getPropertiesManager().getDebugEnabled()) {

											logger.info("Executing Thread -- LqiReq Node:" + node.getNetworkAddress() + " StartIndex:" + _indexLqi);
										}
										startLqi(node, function, _indexLqi);
										return;
									}
								};
								Thread thr0 = new Thread(thr);
								thr0.setName("Node:" + node.getNetworkAddress() + " -- " + funcionName + " StartIndex:" + nextStartIndex);
								thr0.start();

							} else {
								if (__currentNodeWrapper.get_Mgmt_LQI_rsp() != null && __currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList != null) {
									if (__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.size() > 0) {
										if (startIndex == 0x00) {
											__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.clear();
										}
										__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.addAll(_newDsc.get_response().NeighborTableList);
									} else
										__currentNodeWrapper.set_Mgmt_LQI_rsp(_newDsc.get_response());
								} else
									__currentNodeWrapper.set_Mgmt_LQI_rsp(_newDsc.get_response());
								__currentNodeWrapper.set_discoveryCompleted(true);

								if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
									__currentNodeWrapper.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());

								if (gal.getPropertiesManager().getForcePingTimeout() > 0)
									__currentNodeWrapper.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());

								if (gal.getPropertiesManager().getDebugEnabled()) {
									logger.info("\n\r" + funcionName + " completed for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress());
								}

							}
						}

						if ((function == TypeFunction.FORCEPING) || (function == TypeFunction.DISCOVERY)) {
							Status _s = new Status();
							_s.setCode((short) 0x00);
							_s.setMessage("Successful - " + funcionName + " Algorithm");
							gal.get_gatewayEventManager().nodeDiscovered(_s, __currentNodeWrapper.get_node());
						}

					}
				}
			} catch (Exception e) {
				manageError(function, startIndex, __currentNodeWrapper, _indexParent, e);
			} finally {
				synchronized (_Table) {
					_Table.remove(_newDsc);
				}

			}
		}
	}

	/**
	 * For any Child into the Neighbor of he parent node, start the same Algorithm recursively
	 */
	private void manageChildNode(Address node, TypeFunction function, String funcionName, AssociatedDevices _AssociatedDevices, NeighborTableLis_Record x) throws Exception {
		if (x._Extended_Address == 0xFFFFFFFFFFFFFFFFL || x._Extended_Address == 0x0000000000000000L) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("Wrong IEEE found");
			}

		} else {
			short indexChildOnCache = -1;

			Address _addressChild = new Address();
			_addressChild.setNetworkAddress(x._Network_Address);
			BigInteger bi = BigInteger.valueOf(x._Extended_Address);
			_addressChild.setIeeeAddress(bi);
			WrapperWSNNode newNodeWrapperChild = new WrapperWSNNode(gal);
			WSNNode newNodeChild = new WSNNode();
			newNodeChild.setAddress(_addressChild);
			MACCapability _mac = new MACCapability();
			_mac.setReceiverOnWhenIdle((x._RxOnWhenIdle == 1) ? true : false);
			newNodeChild.setCapabilityInformation(_mac);
			newNodeChild.setParentAddress(node);
			newNodeChild.setStartIndex(x._Depth);
			/* Add child node to parent node */
			SonNode _SonNode = new SonNode();
			_SonNode.setShortAddr(_addressChild.getNetworkAddress());
			_AssociatedDevices.getSonNode().add(_SonNode);
			newNodeWrapperChild.set_node(newNodeChild);

			indexChildOnCache = gal.existIntoNetworkCache(newNodeWrapperChild.get_node().getAddress().getNetworkAddress());
			if (indexChildOnCache == -1)
				gal.getNetworkcache().add(newNodeWrapperChild);

			synchronized (newNodeWrapperChild) {

				if (indexChildOnCache == -1) {
					/*
					 * node child not exists
					 */

					if (!newNodeWrapperChild.isSleepy()) {

						/* Node Not SleepyEndDevice */
						/* Only Coordinator or Router */
						if (x._Device_Type == 0x00 /*
													 * LogicalType . COORDINATOR
													 */
								|| x._Device_Type == 0x01 /*
														 * LogicalType . Router
														 */) {
							newNodeWrapperChild.set_discoveryCompleted(false);

							if (function == TypeFunction.DISCOVERY) {
								if (gal.getPropertiesManager().getDebugEnabled()) {
									logger.info("\n\rScheduling " + funcionName + " for node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress());
								}
								/*
								 * Only Discovery Function execute recursion
								 */
								newNodeWrapperChild.setTimerDiscovery(0);

							}

							if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
								newNodeWrapperChild.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());

							if (gal.getPropertiesManager().getForcePingTimeout() > 0)
								newNodeWrapperChild.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());

						} else /*
								 * LogicalType EndDevice
								 */
						{
							newNodeWrapperChild.set_discoveryCompleted(true);
							Status _s = new Status();
							_s.setCode((short) 0x00);
							_s.setMessage("Successful - " + funcionName + " Algorithm");
							gal.get_gatewayEventManager().nodeDiscovered(_s, newNodeWrapperChild.get_node());
							if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
								newNodeWrapperChild.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());

							if (gal.getPropertiesManager().getForcePingTimeout() > 0)
								newNodeWrapperChild.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());

						}
					} else {
						/* If Sleepy EndDevice */
						newNodeWrapperChild.set_discoveryCompleted(true);
						Status _s = new Status();
						_s.setCode((short) 0x00);
						_s.setMessage("Successful - " + funcionName + " Algorithm");
						gal.get_gatewayEventManager().nodeDiscovered(_s, newNodeWrapperChild.get_node());
					}

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info(funcionName + ":Found new Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " from NeighborTableListCount of:" + node.getNetworkAddress() + "\n\r");
					}
				} else {

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Found an existing Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " into NeighborTableListCount of:" + node.getNetworkAddress() + "\n\r");
					}
				}
			}

		}
	}

	/**
	 * Manage the error on Lqi_Request or Lqi_response
	 */
	private synchronized void manageError(TypeFunction function, short startIndex, WrapperWSNNode __currentNodeWrapper, int _indexParent, Exception e) {
		synchronized (__currentNodeWrapper) {
			__currentNodeWrapper.set_numberOfAttempt();
		}
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.error("\n\rError on Lqi request for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + "\n\rError message: " + e.getMessage() + "\n\rNmberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt() + "\n\r");
		}

		if (__currentNodeWrapper.get_numberOfAttempt() >= gal.getPropertiesManager().getKeepAliveNumberOfAttempt()) {

			try {
				Status _st0 = gal.getDataLayer().ClearNeighborTableEntry(IDataLayer.INTERNAL_TIMEOUT, __currentNodeWrapper.get_node().getAddress());
			} catch (Exception e1) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error("\n\rError on ClearNeighborTableEntry for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + "\n\rError message: " + e.getMessage() + "\n\rNmberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt() + "\n\r");
				}
			}
			try {
				Status _st1 = gal.getDataLayer().ClearDeviceKeyPairSet(IDataLayer.INTERNAL_TIMEOUT, __currentNodeWrapper.get_node().getAddress());
			} catch (Exception e1) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error("\n\rError on ClearDeviceKeyPairSet for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + "\n\rError message: " + e.getMessage() + "\n\rNmberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt() + "\n\r");
				}
			}
			Status _s = new Status();
			_s.setCode((short) GatewayConstants.SUCCESS);
			try {
				gal.get_gatewayEventManager().nodeRemoved(_s, __currentNodeWrapper.get_node());
			} catch (Exception e1) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error("\n\rError on nodeRemoved callback for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + "\n\rError message: " + e.getMessage() + "\n\rNmberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt() + "\n\r");
				}
			}

			gal.getNetworkcache().get(_indexParent).abortTimers();
			gal.getNetworkcache().remove(_indexParent);

			return;

		} else {
			synchronized (__currentNodeWrapper) {
				if (function == TypeFunction.DISCOVERY)
					__currentNodeWrapper.setTimerDiscovery(NUMBEROFATTEMPTSECONDS);
				else if (function == TypeFunction.FRESHNESS)

					__currentNodeWrapper.setTimerFreshness(NUMBEROFATTEMPTSECONDS);

				else if (function == TypeFunction.FORCEPING)
					__currentNodeWrapper.setTimerForcePing(NUMBEROFATTEMPTSECONDS);
			}
		}
	}

	/**
	 * Class used to split the ApsMessage to Lqi_Response
	 */
	public static void Mgmt_LQI_Response(APSMessageEvent message) {
		Mgmt_LQI_rsp _res = new Mgmt_LQI_rsp(message.getData());
		short index = -1;
		synchronized (_Table) {
			index = existIntoTable(_res._TranseqNumber, message.getSourceAddress().getNetworkAddress());
			if (index == -1)
				return;
			else {
				DiscoveryMng i = _Table.get(index);
				synchronized (i) {
					i.set_response(_res);
					i.notify();
				}
			}
		}

	}

}
/**
 * Class used to manage the lock on the Lqi_Request
 */
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
