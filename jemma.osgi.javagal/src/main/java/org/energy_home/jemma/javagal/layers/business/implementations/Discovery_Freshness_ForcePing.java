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

import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.object.GatewayStatus;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.MyThread;
import org.energy_home.jemma.javagal.layers.object.NeighborTableLis_Record;
import org.energy_home.jemma.javagal.layers.object.TypeFunction;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.AssociatedDevices;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.SonNode;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

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
	int TimeForcePingErrorSeconds;
	int TimeFreshnessErrorSeconds;
	int TimeDiscoveryErrorSeconds;

	int TimeForcePingNewNodeSeconds;
	int TimeFreshnessNewNodeSeconds;
	int TimeDiscoveryNewNodeSeconds;

	public Discovery_Freshness_ForcePing(GalController _gal) {
		gal = _gal;
		TimeForcePingErrorSeconds = gal.getPropertiesManager().getTimeForcePingErrorSeconds();
		TimeFreshnessErrorSeconds = gal.getPropertiesManager().getTimeFreshnessErrorSeconds();
		TimeDiscoveryErrorSeconds = gal.getPropertiesManager().getTimeDiscoveryErrorSeconds();
		TimeForcePingNewNodeSeconds = gal.getPropertiesManager().getTimeForcePingNewNodeSeconds();
		TimeFreshnessNewNodeSeconds = gal.getPropertiesManager().getTimeFreshnessNewNodeSeconds();
		TimeDiscoveryNewNodeSeconds = gal.getPropertiesManager().getTimeDiscoveryNewNodeSeconds();
	}

	private final static Log logger = LogFactory.getLog(Discovery_Freshness_ForcePing.class);

	/**
	 * Send the Lqi_Request for the selected address. Then manages the
	 * Lqi_Response
	 */
	public void startLqi(Address node, TypeFunction function, short startIndex) {
		Mgmt_LQI_rsp _Lqi = null;
		String functionName = null;
		if (gal.getGatewayStatus() == GatewayStatus.GW_RUNNING) {

			if (function == TypeFunction.DISCOVERY)
				functionName = "Discovery";
			else if (function == TypeFunction.FRESHNESS)
				functionName = "Freshness";
			else if (function == TypeFunction.FORCEPING)
				functionName = "ForcePing";

			WrapperWSNNode __currentNodeWrapper = null;
			int _indexParent = -1;
			_indexParent = gal.existIntoNetworkCache(node.getNetworkAddress());
			if (_indexParent != -1) {
				__currentNodeWrapper = gal.getNetworkcache().get(_indexParent);
			} else
				return;

			if (function == TypeFunction.FORCEPING) {
				if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
					__currentNodeWrapper.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
				if (gal.getPropertiesManager().getDebugEnabled()) {
					//System.out.println("Postponing  timer Freshness by ForcePing for node:" + node.getNetworkAddress());
					logger.info("Postponing  timer Freshness by ForcePing for node:" + node.getNetworkAddress());
				}

			}
			

			try {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					System.out.println("\n\r"+ new Date(System.currentTimeMillis()).toLocaleString() + "Sending LQI_REQ (" + functionName + ") for node:" + String.format("%04x",node.getNetworkAddress()) + " -- StartIndex:" + startIndex + "\n\r");

					logger.info("\n\rSending LQI_REQ (" + functionName + ") for node:" + node.getNetworkAddress() + " -- StartIndex:" + startIndex + "\n\r");
				}
				_Lqi = gal.getDataLayer().Mgmt_Lqi_Request(gal.getPropertiesManager().getCommandTimeoutMS(), node, startIndex);

				/* Check no Response received */
				if (_Lqi == null) {
					manageError(function, startIndex, __currentNodeWrapper, _indexParent, new Exception("LqiReq.Response not received!"));
				} else/* Response Received */
				{
					short _totalLqi = _Lqi._NeighborTableEntries;
					short _indexLqi = _Lqi._StartIndex;
					short _LqiListCount = _Lqi._NeighborTableListCount;

					if (gal.getPropertiesManager().getDebugEnabled()) {
						//System.out.println("\n\rReceived LQI_RSP (" + functionName + ") for node:" + node.getNetworkAddress() + " -- StartIndex:" + _indexLqi + "\n\r");
						logger.info("Received LQI_RSP (" + functionName + ") for node:" + node.getNetworkAddress() + " -- StartIndex:" + _indexLqi);
					}

					AssociatedDevices _AssociatedDevices = new AssociatedDevices();
					if (_Lqi.NeighborTableList != null && _Lqi.NeighborTableList.size() > 0) {
						for (NeighborTableLis_Record x : _Lqi.NeighborTableList) {
							manageChildNode(node, function, functionName, _AssociatedDevices, x);
						}
					}

					synchronized (__currentNodeWrapper) {
						__currentNodeWrapper.reset_numberOfAttempt();
						__currentNodeWrapper.set_discoveryCompleted(true);
						__currentNodeWrapper.get_node().getAssociatedDevices().clear();
						__currentNodeWrapper.get_node().getAssociatedDevices().add(_AssociatedDevices);

						if ((_indexLqi + _LqiListCount) < _totalLqi) {
							if (_LqiListCount == 0x00) {
								if (gal.getPropertiesManager().getDebugEnabled()) {
									logger.warn("patch that correct a 4-noks bug - 07-12-2011");
								}
								return;
							} else {
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

										//System.out.println("Executing Thread -- LqiReq Node:" + node.getNetworkAddress() + " StartIndex:" + _indexLqi);
										logger.info("Executing Thread -- LqiReq Node:" + node.getNetworkAddress() + " StartIndex:" + _indexLqi);
									}
									startLqi(node, function, _indexLqi);
									return;
								}
							};
							Thread thr0 = new Thread(thr);
							thr0.setName("Node:" + node.getNetworkAddress() + " -- " + functionName + " StartIndex:" + nextStartIndex);
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

							if (function == TypeFunction.FRESHNESS)
								if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
									__currentNodeWrapper.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
							if (function == TypeFunction.FORCEPING)
								if (gal.getPropertiesManager().getForcePingTimeout() > 0)
									__currentNodeWrapper.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());

							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.info(functionName + " completed for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress());
							}

						}
					}

					if ((function == TypeFunction.FORCEPING) || (function == TypeFunction.DISCOVERY)) {
						if ((System.currentTimeMillis() - __currentNodeWrapper.getLastDiscovered()) > gal.getPropertiesManager().getForcePingTimeout()) {
							__currentNodeWrapper.setLastDiscovered(System.currentTimeMillis());
							Status _s = new Status();
							_s.setCode((short) 0x00);
							_s.setMessage("Successful - " + functionName + " Algorithm");

							if (gal.getPropertiesManager().getDebugEnabled())
								logger.info("Starting nodeDiscovered from function: " + functionName);

							gal.get_gatewayEventManager().nodeDiscovered(_s, __currentNodeWrapper.get_node());
						}
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
				/*
				 * node child not exists
				 */

				gal.getNetworkcache().add(newNodeWrapperChild);

				synchronized (newNodeWrapperChild) {

					if (!newNodeWrapperChild.isSleepy()) {

						newNodeWrapperChild.set_discoveryCompleted(false);

						if (function == TypeFunction.DISCOVERY) {
							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.info("Scheduling Discovery for node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress());
							}
							newNodeWrapperChild.setTimerDiscovery(TimeDiscoveryNewNodeSeconds);
							if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
								newNodeWrapperChild.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
							if (gal.getPropertiesManager().getForcePingTimeout() > 0)
								newNodeWrapperChild.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());
						}

						else if (function == TypeFunction.FRESHNESS || function == TypeFunction.FORCEPING) {
							if (gal.getPropertiesManager().getKeepAliveThreshold() > 0)
								newNodeWrapperChild.setTimerFreshness(TimeFreshnessNewNodeSeconds);
							if (gal.getPropertiesManager().getForcePingTimeout() > 0)
								newNodeWrapperChild.setTimerForcePing(TimeForcePingNewNodeSeconds);
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
						System.out.println(funcionName + ": Found new Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " from NeighborTableListCount of:" + node.getNetworkAddress());

						logger.info(funcionName + ": Found new Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " from NeighborTableListCount of:" + node.getNetworkAddress());
					}

				}
			} else {

				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Found an existing Node:" + newNodeWrapperChild.get_node().getAddress().getNetworkAddress() + " into NeighborTableListCount of:" + node.getNetworkAddress());
				}
			}

		}
	}

	/**
	 * Manage the error on Lqi_Request or Lqi_response
	 */
	private synchronized void manageError(TypeFunction function, short startIndex, WrapperWSNNode __currentNodeWrapper, int _indexParent, Exception e) {
		/* Check if the node exist o cache or is already deleted */
		int indexOnCache = gal.existIntoNetworkCache(__currentNodeWrapper.get_node().getAddress().getNetworkAddress());
		if (indexOnCache > -1) {
			synchronized (__currentNodeWrapper) {
				__currentNodeWrapper.set_numberOfAttempt();
			}
			if (gal.getPropertiesManager().getDebugEnabled()) {
				System.out.println("Error on Lqi( " + function + " ) request for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
				logger.error("Error on Lqi( " + function + " ) request for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
			}

			if (__currentNodeWrapper.get_numberOfAttempt() >= gal.getPropertiesManager().getKeepAliveNumberOfAttempt()) {
				try {
					Status _st1 = gal.getDataLayer().ClearDeviceKeyPairSet(gal.getPropertiesManager().getCommandTimeoutMS(), __currentNodeWrapper.get_node().getAddress());
					if (_st1.getCode() == GatewayConstants.SUCCESS) {
						try {
							Status _st0 = gal.getDataLayer().ClearNeighborTableEntry(gal.getPropertiesManager().getCommandTimeoutMS(), __currentNodeWrapper.get_node().getAddress());
						} catch (Exception e1) {
							if (gal.getPropertiesManager().getDebugEnabled()) {
								System.out.println("Error on ClearNeighborTableEntry for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
								logger.error("Error on ClearNeighborTableEntry for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
							}
						}
					}

				} catch (Exception e1) {
					if (gal.getPropertiesManager().getDebugEnabled()) {
						System.out.println("Error on ClearDeviceKeyPairSet for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
						logger.error("Error on ClearDeviceKeyPairSet for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + " - NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
					}
				}

				if (gal.getNetworkcache().size() > _indexParent) {
					if (gal.getNetworkcache().get(_indexParent) != null) {
						gal.getNetworkcache().get(_indexParent).abortTimers();
						if (gal.getPropertiesManager().getDebugEnabled()) {
							System.out.println("Removed node: " + gal.getNetworkcache().get(_indexParent).get_node().getAddress().getNetworkAddress());
							logger.error("Removed node: " + gal.getNetworkcache().get(_indexParent).get_node().getAddress().getNetworkAddress());
						}
						gal.getNetworkcache().remove(_indexParent);

					}
				}

				Status _s = new Status();
				_s.setCode((short) GatewayConstants.SUCCESS);
				try {
					gal.get_gatewayEventManager().nodeRemoved(_s, __currentNodeWrapper.get_node());
				} catch (Exception e1) {
					if (gal.getPropertiesManager().getDebugEnabled()) {
						System.out.println("Error on nodeRemoved callback for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + "NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
						logger.error("Error on nodeRemoved callback for node: " + __currentNodeWrapper.get_node().getAddress().getNetworkAddress() + " - Error message: " + e.getMessage() + "NumberOfAttempt:" + __currentNodeWrapper.get_numberOfAttempt());
					}
				}

				return;

			} else {
				synchronized (__currentNodeWrapper) {
					if (function == TypeFunction.DISCOVERY)
						__currentNodeWrapper.setTimerDiscovery(TimeDiscoveryErrorSeconds);
					else if (function == TypeFunction.FRESHNESS)
						__currentNodeWrapper.setTimerFreshness(TimeFreshnessErrorSeconds);
					else if (function == TypeFunction.FORCEPING)
						__currentNodeWrapper.setTimerForcePing(TimeForcePingErrorSeconds);
				}
			}
		}
	}

}
