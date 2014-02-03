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

import com.sun.org.apache.xml.internal.utils.BoolStack;
import com.sun.org.apache.xpath.internal.compiler.FunctionTable;

/**
 * Manages received APS messages for the discovery / Freshness / ForcePing
 * Algorithm.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class Discovery_Freshness_ForcePing {
	GalController gal = null;
	int NUMBEROFATTEMPTSECONDS = 5;

	public Discovery_Freshness_ForcePing(GalController _gal) {
		gal = _gal;
	}

	private final static Log logger = LogFactory.getLog(Discovery_Freshness_ForcePing.class);

	/**
	 * Send the Lqi_Request for the selected address. Then manages the
	 * Lqi_Response
	 */
	public void startLqi(Address node, TypeFunction function, short startIndex) {
		Mgmt_LQI_rsp _Lqi = null;
		String funcionName = null;
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
				logger.info("\n\r****************Starting " + funcionName + " for node:" + node.getNetworkAddress() + " -- StartIndex:" + startIndex + "\n\r");
			}

			try {

				_Lqi = gal.getDataLayer().Mgmt_Lqi_Request(IDataLayer.INTERNAL_TIMEOUT, node, startIndex);

				/* Check no Response received */
				if (_Lqi == null  ) {
					manageError(function, startIndex, __currentNodeWrapper, _indexParent, new Exception("LqiReq.Response not received!"));
				} else/* Response Received */
				{
					short _totalLqi = _Lqi._NeighborTableEntries;
					short _indexLqi = _Lqi._StartIndex;
					short _LqiListCount = _Lqi._NeighborTableListCount;

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("\n\rReceived LQI_RSP (" + funcionName + ") for node:" + node.getNetworkAddress() + " -- StartIndex:" + _indexLqi + "\n\r");
					}
					
					/*
					 * Start the discovery for any child and add the child to
					 * parent node
					 */

					AssociatedDevices _AssociatedDevices = new AssociatedDevices();
					if (_Lqi.NeighborTableList != null && _Lqi.NeighborTableList.size() > 0) {
						for (NeighborTableLis_Record x : _Lqi.NeighborTableList) {
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
							} 
							else {
								if (__currentNodeWrapper.get_Mgmt_LQI_rsp() != null && _Lqi.NeighborTableList != null) {

									if (__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.size() > 0) {
										if (startIndex == 0x00) {
											__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.clear();
										}
										__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.addAll(_Lqi.NeighborTableList);
									} else
										__currentNodeWrapper.set_Mgmt_LQI_rsp(_Lqi);
								} else
									__currentNodeWrapper.set_Mgmt_LQI_rsp(_Lqi);
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
									__currentNodeWrapper.get_Mgmt_LQI_rsp().NeighborTableList.addAll(_Lqi.NeighborTableList);
								} else
									__currentNodeWrapper.set_Mgmt_LQI_rsp(_Lqi);
							} else
								__currentNodeWrapper.set_Mgmt_LQI_rsp(_Lqi);
							__currentNodeWrapper.set_discoveryCompleted(true);

							if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
								__currentNodeWrapper.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());

							if (gal.getPropertiesManager().getForcePingTimeout() > 0)

								__currentNodeWrapper.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());

							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.info("\n\r" + funcionName + " completed for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " Time:" + System.currentTimeMillis());
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
			} catch (Exception e) {
				manageError(function, startIndex, __currentNodeWrapper, _indexParent, e);
			}
		}
	}

	/**
	 * For any Child into the Neighbor of he parent node, start the same
	 * Algorithm recursively
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
			if (indexChildOnCache == -1) {
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
														 * LogicalType .
														 * COORDINATOR
														 */
									|| x._Device_Type == 0x01 /*
															 * LogicalType .
															 * Router
															 */) {
								newNodeWrapperChild.set_discoveryCompleted(false);

								if (function == TypeFunction.DISCOVERY) {
									if (gal.getPropertiesManager().getDebugEnabled()) {
										logger.info("\n\rScheduling " + funcionName + " for node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress());
									}
									/*
									 * Only Discovery Function execute recursion
									 */

									newNodeWrapperChild.setTimerDiscovery(NUMBEROFATTEMPTSECONDS);

								}
								if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
									newNodeWrapperChild.setTimerFreshness(NUMBEROFATTEMPTSECONDS);
								if (gal.getPropertiesManager().getForcePingTimeout() > 0)
									newNodeWrapperChild.setTimerForcePing(NUMBEROFATTEMPTSECONDS);

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
									newNodeWrapperChild.setTimerFreshness(NUMBEROFATTEMPTSECONDS);

								if (gal.getPropertiesManager().getForcePingTimeout() > 0)
									newNodeWrapperChild.setTimerForcePing(NUMBEROFATTEMPTSECONDS);

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
							logger.info(funcionName + ": Found new Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " from NeighborTableListCount of:" + node.getNetworkAddress() + "\n\r");
						}
					} else {

						if (gal.getPropertiesManager().getDebugEnabled()) {
							logger.info("Found an existing Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " into NeighborTableListCount of:" + node.getNetworkAddress() + "\n\r");
						}
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

			if (gal.getNetworkcache().size() > _indexParent) {
				if (gal.getNetworkcache().get(_indexParent) != null) {
					gal.getNetworkcache().get(_indexParent).abortTimers();
					gal.getNetworkcache().remove(_indexParent);
				}
			}

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

}
