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
package org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.business.Utils;
import org.energy_home.jemma.javagal.layers.data.implementations.SerialCommRxTx;
import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;
import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;
import org.energy_home.jemma.javagal.layers.object.GatewayDeviceEventEntry;
import org.energy_home.jemma.javagal.layers.object.GatewayStatus;
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;
import org.energy_home.jemma.javagal.layers.object.MyThread;
import org.energy_home.jemma.javagal.layers.object.ParserLocker;
import org.energy_home.jemma.javagal.layers.object.TypeMessage;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.DescriptorCapability;
import org.energy_home.jemma.zgd.jaxb.Device;
import org.energy_home.jemma.zgd.jaxb.EnergyScanResult;
import org.energy_home.jemma.zgd.jaxb.EnergyScanResult.ScannedChannel;
import org.energy_home.jemma.zgd.jaxb.LogicalType;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;
import org.energy_home.jemma.zgd.jaxb.SecurityStatus;
import org.energy_home.jemma.zgd.jaxb.ServerMask;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.TxOptions;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.ZCLMessage;

/**
 * Freescale implementation of {@link IDataLayer}.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class DataFreescale implements IDataLayer {
	GalController gal = null;
	private IConnector _key = null;
	private final static Log logger = LogFactory.getLog(DataFreescale.class);
	private LinkedBlockingQueue<ByteArrayObject> listOfCommandToSend = new LinkedBlockingQueue<ByteArrayObject>();
	private final List<ParserLocker> listLocker;
	/**
	 * Default timeout's value.
	 */
	public Long INTERNAL_TIMEOUT;

	public final static short MAX_TO_SEND_BYTE_ARRAY = 2048;

	public final List<Short> receivedDataQueue = Collections.synchronizedList(new LinkedList<Short>());

	/**
	 * Creates a new instance with a reference to the Gal Controller.
	 * 
	 * @param _gal
	 *            a reference to the Gal Controller.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public DataFreescale(GalController _gal) throws Exception {
		gal = _gal;
		listLocker = Collections.synchronizedList(new LinkedList<ParserLocker>());
		_key = new SerialCommRxTx(gal.getPropertiesManager().getzgdDongleUri(), gal.getPropertiesManager().getzgdDongleSpeed(), this);
		INTERNAL_TIMEOUT = gal.getPropertiesManager().getCommandTimeoutMS();
		Thread thr = new Thread() {
			@Override
			public void run() {
				ByteArrayObject _currentCommand;
				while (true) {
					try {
						_currentCommand = listOfCommandToSend.poll();
						if (_currentCommand != null){
							if (gal.getPropertiesManager().getDebugEnabled()) {
								logger.info(">>> Sending: " + _currentCommand.ToHexString());
								System.out.println(">>> Sending: " + _currentCommand.ToHexString());
							}
								_key.write(_currentCommand);
						}
						
						
					
					Thread.sleep(30);
					}
					 catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		};
		thr.start();

	}

	private List<short[]> messages = Collections.synchronizedList(new LinkedList<short[]>());

	/**
	 * Gets the list of current available messages.
	 * 
	 * @return the list of current available message.
	 */
	public List<short[]> getMessages() {
		return messages;
	}

	private short[] createMessageFromRowData() {
		boolean error = false;
		synchronized (this) {
			if (receivedDataQueue.size() < (DataManipulation.START_PAYLOAD_INDEX + 1)) {
				return null;
			}

			Integer _HexLegth = receivedDataQueue.get(3).intValue();
			int payloadLenght = Integer.parseInt(Integer.toHexString(_HexLegth), 16);

			if (receivedDataQueue.size() < (DataManipulation.START_PAYLOAD_INDEX + payloadLenght + 1)) {
				return null;
			}

			int messageCfc = receivedDataQueue.get(DataManipulation.START_PAYLOAD_INDEX + payloadLenght);

			csc.resetLastCalculated();
			int currCscControl = 0x00;
			csc.getCumulativeXor(receivedDataQueue.get(1));
			csc.getCumulativeXor(receivedDataQueue.get(2));
			csc.getCumulativeXor(receivedDataQueue.get(3));
			for (int i = 0; i < payloadLenght; i++) {
				currCscControl = csc.getCumulativeXor(receivedDataQueue.get(DataManipulation.START_PAYLOAD_INDEX + i));
			}

			if (currCscControl != messageCfc) {
				logger.error("Error CscControl: " + Integer.toHexString(currCscControl) + " != " + Integer.toHexString(messageCfc));

				error = true;
			}

			int messageLenght = payloadLenght + DataManipulation.START_PAYLOAD_INDEX - 1;
			short[] toReturn = new short[messageLenght];

			receivedDataQueue.remove(0);

			toReturn[0] = receivedDataQueue.remove(0);
			toReturn[1] = receivedDataQueue.remove(0);
			toReturn[2] = receivedDataQueue.remove(0);

			for (int i = 0; i < payloadLenght; i++)
				toReturn[i + 3] = receivedDataQueue.remove(0);
			receivedDataQueue.remove(0);
			if (error)
				return null;
			else
				return toReturn;
		}
	}

	public void addToReceivedDataQueue(int size, short[] buff) {

		synchronized (this) {
			for (int i = 0; i < size; i++)
				receivedDataQueue.add(buff[i]);

			while (!receivedDataQueue.isEmpty()) {
				int first_element = receivedDataQueue.get(0);

				if (first_element != DataManipulation.SEQUENCE_START) {
					receivedDataQueue.remove(0);
					continue;
				}

				short[] tempArray = createMessageFromRowData();
				if (tempArray != null) {
					messages.add(tempArray);

				} else
					break;

			}
		}
	}

	public void processMessages() throws Exception {
		// Look on received messages
		int _size = 0;
		synchronized (this) {
			_size = messages.size();
		}

		while (_size > 0) {
			short[] message = null;
			synchronized (this) {
				message = messages.remove(0);
			}

			ByteBuffer bb = ByteBuffer.allocate(2);
			bb.order(ByteOrder.BIG_ENDIAN);
			bb.put((byte) message[0]);
			bb.put((byte) message[1]);
			short _command = bb.getShort(0);

			/* APSDE-DATA.Indication */
			if (_command == FreescaleConstants.APSDEDataIndication) {
				final APSMessageEvent messageEvent = new APSMessageEvent();
				short destAddressMode = message[3];
				messageEvent.setDestinationAddressMode((long) destAddressMode);
				BigInteger _ieee = null;
				Address address = new Address();
				switch (destAddressMode) {
				case 0x00:
					// Reserved (No source address supplied)
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Message Discarded: found reserved 0x00 as Destination Address Mode ");
					}// Error found, we don't proceed and discard the
						// message
					return;
				case 0x01:
					// Value16bitgroupfordstAddr (DstEndpoint not
					// present)
					// No destination end point (so FF broadcast),
					// present
					// short
					// address on 2 bytes
					address.setNetworkAddress(DataManipulation.toIntFromShort((byte) message[5], (byte) message[4]));

					_ieee = gal.getIeeeAddress_FromNetworkCache(address.getNetworkAddress());
					if (_ieee != null)
						address.setIeeeAddress(_ieee);

					messageEvent.setDestinationAddress(address);
					messageEvent.setDestinationEndpoint((short) 0xff);

					break;
				case 0x02:
					// Value16bitAddrandDstEndpoint (16 bit address
					// supplied)
					address.setNetworkAddress(DataManipulation.toIntFromShort((byte) message[5], (byte) message[4]));
					_ieee = gal.getIeeeAddress_FromNetworkCache(address.getNetworkAddress());
					if (_ieee != null)
						address.setIeeeAddress(_ieee);

					messageEvent.setDestinationAddress(address);
					messageEvent.setDestinationEndpoint(message[6]);
					break;
				default:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.error("Message Discarded: not valid Destination Address Mode");
					}
					// Error found, we don't proceed and discard the
					// message
					return;
				}
				messageEvent.setSourceAddressMode((long) message[7]);
				address = new Address();
				address.setNetworkAddress(DataManipulation.toIntFromShort((byte) message[9], (byte) message[8]));
				_ieee = gal.getIeeeAddress_FromNetworkCache(address.getNetworkAddress());
				if (_ieee != null)
					address.setIeeeAddress(_ieee);
				messageEvent.setSourceAddress(address);
				messageEvent.setSourceEndpoint(message[10]);
				messageEvent.setProfileID(DataManipulation.toIntFromShort((byte) message[12], (byte) message[11]));
				messageEvent.setClusterID(DataManipulation.toIntFromShort((byte) message[14], (byte) message[13]));

				if (gal.getGatewayStatus() == GatewayStatus.GW_RUNNING && gal.get_GalNode() != null) {
					/* Update The Node Data */

					int _indexOnCache = -1;
					_indexOnCache = gal.existIntoNetworkCache(address.getNetworkAddress());
					if (_indexOnCache != -1) {
						/* The node is already into the DB */
						if (gal.getPropertiesManager().getKeepAliveThreshold() > 0) {
							if (!gal.getNetworkcache().get(_indexOnCache).isSleepy()) {
								gal.getNetworkcache().get(_indexOnCache).reset_numberOfAttempt();
								gal.getNetworkcache().get(_indexOnCache).setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
								if (gal.getPropertiesManager().getDebugEnabled()) {
									//System.out.println("\n\rPostponing  timer Freshness by Aps.Indication for node:" + gal.getNetworkcache().get(_indexOnCache).get_node().getAddress().getNetworkAddress() + "\n\r");
									logger.info("Postponing  timer Freshness by Aps.Indication for node:" + gal.getNetworkcache().get(_indexOnCache).get_node().getAddress().getNetworkAddress());
								}
							}

						}
					} else {
						// 0x8034 is a LeaveAnnouncement, 0x0013 is a
						// DeviceAnnouncement, 0x8001 is a IEEE_Addr_Rsp
						if ((gal.getPropertiesManager().getAutoDiscoveryUnknownNodes() > 0) && (!(messageEvent.getProfileID() == 0x0000 && (messageEvent.getClusterID() == 0x0013 || messageEvent.getClusterID() == 0x8034 || messageEvent.getClusterID() == 0x8001)))) {

							if (address.getNetworkAddress() != gal.get_GalNode().get_node().getAddress().getNetworkAddress()) {
								Runnable thr = new MyThread(address) {
									@Override
									public void run() {
										Address _address = (Address) this.getParameter();
										int _indexOnCache = -1;
										_indexOnCache = gal.existIntoNetworkCache(_address.getNetworkAddress());
										if (_indexOnCache == -1) {

											if (gal.getPropertiesManager().getDebugEnabled()) {
												//System.out.println("\n\rAutoDiscoveryUnknownNodes procedure of Node:" + messageEvent.getSourceAddress().getNetworkAddress() + "\n\r");

												logger.info("AutoDiscoveryUnknownNodes procedure of Node:" + messageEvent.getSourceAddress().getNetworkAddress());
											}
											try {

												
												// Insert the node into cache,
												// but with the
												// discovery_completed flag a
												// false
												BigInteger ieee = null;
												WrapperWSNNode o = new WrapperWSNNode(gal);
												WSNNode _newNode = new WSNNode();
												o.set_discoveryCompleted(false);
												_newNode.setAddress(_address);
												o.set_node(_newNode);
												gal.getNetworkcache().add(o);

												Thread.sleep(500);
												/*
												 * Reading the IEEEAddress of
												 * the new node
												 */

												if (gal.getPropertiesManager().getDebugEnabled())
													logger.info("Sending IeeeReq to:" + _address.getNetworkAddress());
												System.out.println("Sending IeeeReq to:" + _address.getNetworkAddress());
												ieee = readExtAddress(INTERNAL_TIMEOUT, _address.getNetworkAddress().shortValue());
												_address.setIeeeAddress(ieee);
												if (gal.getPropertiesManager().getDebugEnabled()) {
													logger.info("Readed Ieee of the new node:" + _address.getNetworkAddress() + " Ieee: " + ieee.toString());
													System.out.println("Readed Ieee of the new node:" + _address.getNetworkAddress() + " Ieee: " + ieee.toString());

												}
												if (gal.getPropertiesManager().getDebugEnabled())
													logger.info("Sending NodeDescriptorReq to:" + _address.getNetworkAddress());
												NodeDescriptor _ndesc = getNodeDescriptorSync(INTERNAL_TIMEOUT, _address);
												_newNode.setCapabilityInformation(_ndesc.getMACCapabilityFlag());

												if (gal.getPropertiesManager().getDebugEnabled()) {
													logger.info("Readed NodeDescriptor of the new node:" + _address.getNetworkAddress());
													System.out.println("Readed NodeDescriptor of the new node:" + _address.getNetworkAddress());

												}

												o.reset_numberOfAttempt();
												o.set_discoveryCompleted(true);
												if (!o.isSleepy()) {

													if (gal.getPropertiesManager().getKeepAliveThreshold() > 0) {
														o.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
													}
													if (gal.getPropertiesManager().getForcePingTimeout() > 0) {
														o.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());
													}
												}

												_indexOnCache = gal.existIntoNetworkCache(_newNode.getAddress().getNetworkAddress());
												if (_indexOnCache > -1) {
													gal.getNetworkcache().remove(_indexOnCache);

												}

												// Updating the node
												// informations
												gal.getNetworkcache().add(o);
												o.set_discoveryCompleted(true);
												Status _st = new Status();
												_st.setCode((short) GatewayConstants.SUCCESS);
												gal.get_gatewayEventManager().nodeDiscovered(_st, _newNode);

											} catch (GatewayException e) {
												logger.error("Error on getAutoDiscoveryUnknownNodes for node:" + _address.getNetworkAddress() + " Error:" + e.getMessage());

												System.out.println("Error on getAutoDiscoveryUnknownNodes for node:" + _address.getNetworkAddress() + " Error:" + e.getMessage());

												_indexOnCache = gal.existIntoNetworkCache(_address.getNetworkAddress());
												if (_indexOnCache > -1) {
													gal.getNetworkcache().get(_indexOnCache).abortTimers();
													gal.getNetworkcache().remove(_indexOnCache);
												}
												
											} catch (Exception e) {
												logger.error("Error on getAutoDiscoveryUnknownNodes for node:" + _address.getNetworkAddress() + " Error:" + e.getMessage());
												System.out.println("Error on getAutoDiscoveryUnknownNodes for node:" + _address.getNetworkAddress() + " Error:" + e.getMessage());
												_indexOnCache = gal.existIntoNetworkCache(_address.getNetworkAddress());
												if (_indexOnCache > -1) {
													gal.getNetworkcache().get(_indexOnCache).abortTimers();
													gal.getNetworkcache().remove(_indexOnCache);

												}

											}
										}
									}
								};

								Thread _thr0 = new Thread(thr);
								_thr0.setName("Thread getAutoDiscoveryUnknownNodes:" + address.getNetworkAddress());
								_thr0.start();
							}
						}
					}

				}
				int lastAsdu = 16 + message[15] - 1;

				messageEvent.setData(DataManipulation.subByteArray(message, 16, lastAsdu));
				messageEvent.setAPSStatus(message[lastAsdu + 1]);
				// ASK Jump WasBroadcast
				// Security Status
				switch (message[lastAsdu + 3]) {
				case 0x00:
					messageEvent.setSecurityStatus(SecurityStatus.UNSECURED);
					break;
				case 0x01:
					messageEvent.setSecurityStatus(SecurityStatus.SECURED_NWK_KEY);
					break;
				case 0x02:
					messageEvent.setSecurityStatus(SecurityStatus.SECURED_LINK_KEY);
					break;
				// ASK 0x03 not present on telecomitalia object
				default:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Message Discarded: not valid Security Status");
					}
					// Error found, we don't proceed and discard the
					// message
					return;
				}
				messageEvent.setLinkQuality(message[lastAsdu + 4]);
				messageEvent.setRxTime((long) DataManipulation.toIntFromShort((byte) message[(lastAsdu + 8)], (byte) message[(lastAsdu + 5)]));
				// ASK: jumped iMsgType, pNext, iDataSize, pData,
				// iBufferNumber
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APSDE-DATA.Indication", message);
				if ((messageEvent.getDestinationAddressMode() == GatewayConstants.ADDRESS_MODE_SHORT) && (messageEvent.getDestinationAddress().getIeeeAddress() == null))
					messageEvent.getDestinationAddress().setIeeeAddress(gal.getIeeeAddress_FromNetworkCache(messageEvent.getDestinationAddress().getNetworkAddress()));

				if ((messageEvent.getDestinationAddressMode() == GatewayConstants.EXTENDED_ADDRESS_MODE) && (messageEvent.getDestinationAddress().getNetworkAddress() == null))
					messageEvent.getDestinationAddress().setNetworkAddress(gal.getShortAddress_FromNetworkCache(messageEvent.getDestinationAddress().getIeeeAddress()));

				if ((messageEvent.getSourceAddressMode() == GatewayConstants.ADDRESS_MODE_SHORT) && (messageEvent.getDestinationAddress().getIeeeAddress() == null))
					messageEvent.getSourceAddress().setIeeeAddress(gal.getIeeeAddress_FromNetworkCache(messageEvent.getSourceAddress().getNetworkAddress()));

				if ((messageEvent.getSourceAddressMode() == GatewayConstants.EXTENDED_ADDRESS_MODE) && (messageEvent.getDestinationAddress().getNetworkAddress() == null))
					messageEvent.getSourceAddress().setNetworkAddress(gal.getShortAddress_FromNetworkCache(messageEvent.getSourceAddress().getIeeeAddress()));

				if (messageEvent.getProfileID().equals(0)) {/*
															 * ZDO Command
															 */
					if (messageEvent.getClusterID() == 0x8031) {
						String __key = "";
						__key = String.format("%04X", messageEvent.getSourceAddress().getNetworkAddress());
						synchronized (listLocker) {
							for (ParserLocker pl : listLocker) {
								if ((pl.getType() == TypeMessage.LQI_REQ) && __key.equalsIgnoreCase(pl.get_Key())) {
									synchronized (pl) {
										pl.getStatus().setCode((short) messageEvent.getAPSStatus());
										Mgmt_LQI_rsp _res = new Mgmt_LQI_rsp(messageEvent.getData());
										pl.set_objectOfResponse(_res);
										pl.notify();
									}
									break;
								}
							}
						}
					}
					// profileid == 0
					gal.getZdoManager().ZDOMessageIndication(messageEvent);
				} else {
					// profileid > 0
					ZCLMessage _zm = new ZCLMessage();
					_zm.setAPSStatus(messageEvent.getAPSStatus());
					_zm.setClusterID(messageEvent.getClusterID());
					_zm.setDestinationEndpoint(messageEvent.getDestinationEndpoint());
					_zm.setProfileID(messageEvent.getProfileID());
					_zm.setRxTime(messageEvent.getRxTime());
					_zm.setSourceAddress(messageEvent.getSourceAddress());
					_zm.setSourceAddressMode(messageEvent.getSourceAddressMode());
					_zm.setSourceEndpoint(messageEvent.getSourceEndpoint());

					byte[] data = messageEvent.getData();
					// ZCL Header
					// Frame control 8bit
					// Manufacturer code 0/16bits
					// Transaction sequence number 8bit
					// Command identifier 8 bit
					ByteArrayObject _header = new ByteArrayObject();
					ByteArrayObject _payload = new ByteArrayObject();
					if ((data[0] & 0x04) == 1)/* Check manufacturer code */
					{
						_header.addByte(data[0]);// Frame control
						_header.addByte(data[1]);// Manufacturer Code(1/2)
						_header.addByte(data[2]);// Manufacturer Code(2/2)
						_header.addByte(data[3]);// Transaction sequence number
						_header.addByte(data[4]);// Command Identifier
						for (int i = 5; i < data.length; i++)
							_payload.addByte(data[i]);
					} else {
						_header.addByte(data[0]);// Frame control
						_header.addByte(data[1]);// Transaction sequence number
						_header.addByte(data[2]);// Command Identifier
						for (int i = 3; i < data.length; i++)
							_payload.addByte(data[i]);
					}

					_zm.setZCLHeader(_header.getRealByteArray());
					_zm.setZCLPayload(_payload.getRealByteArray());
					gal.get_gatewayEventManager().notifyZCLCommand(_zm);
					gal.getApsManager().APSMessageIndication(messageEvent);

				}
			}

			/* APSDE-DATA.Confirm */
			else if (_command == FreescaleConstants.APSDEDataConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APSDE-DATA.Confirm", message);

				/* DestAddress + DestEndPoint + SourceEndPoint */
				long destAddress = DataManipulation.toLong((byte) message[11], (byte) message[10], (byte) message[9], (byte) message[8], (byte) message[7], (byte) message[6], (byte) message[5], (byte) message[4]);
				byte destEndPoint = (byte) message[12];
				byte sourceEndPoint = (byte) message[13];
				String Key = String.format("%016X", destAddress) + String.format("%02X", destEndPoint) + String.format("%02X", sourceEndPoint);

				// Found APSDE-DATA.Confirm. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						/* DestAddress + DestEndPoint + SourceEndPoint */
						/*
						 * if (gal.getPropertiesManager().getDebugEnabled())
						 * logger.info("APSDE-DATA.Confirm KEY SEND: " +
						 * pl.get_Key() + " -- KEY Received: " + Key);
						 */
						if ((pl.getType() == TypeMessage.APS) && pl.get_Key().equalsIgnoreCase(Key)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[14]);
								pl.notify();
							}
							break;
						}
					}
				}

			}

			/* ZTC-Error.event */
			else if (_command == FreescaleConstants.ZTCErrorevent) {

				byte len = (byte) message[2];
				String MessageStatus = "";
				if (len > 0) {
					int status = message[3];
					switch (status) {
					case 0x00:
						MessageStatus = "0x00: gSuccess_c (Should not be seen in this event.)";
						break;
					case 0xF4:
						MessageStatus = "0xF4: gZtcOutOfMessages_c (ZTC tried to allocate a message, but the allocation failed.)";
						break;
					case 0xF5:
						MessageStatus = "0xF5: gZtcEndPointTableIsFull_c (Self explanatory.)";
						break;
					case 0xF6:
						MessageStatus = "0xF6: gZtcEndPointNotFound_c (Self explanatory.)";
						break;
					case 0xF7:
						MessageStatus = "0xF7: gZtcUnknownOpcodeGroup_c (ZTC does not recognize the opcode group, and there is no application hook.)";
						break;
					case 0xF8:
						MessageStatus = "0xF8: gZtcOpcodeGroupIsDisabled_c (ZTC support for an opcode group is turned off by a compile option.)";
						break;
					case 0xF9:
						MessageStatus = "0xF9: gZtcDebugPrintFailed_c (An attempt to print a debug message ran out of buffer space.)";
						break;
					case 0xFA:
						MessageStatus = "0xFA: gZtcReadOnly_c (Attempt to set read-only data.)";
						break;
					case 0xFB:
						MessageStatus = "0xFB: gZtcUnknownIBIdentifier_c (Self explanatory.)";
						break;
					case 0xFC:
						MessageStatus = "0xFC: gZtcRequestIsDisabled_c (ZTC support for an opcode is turned off by a compile option.)";
						break;
					case 0xFD:
						MessageStatus = "0xFD: gZtcUnknownOpcode_c (Self expanatory.)";
						break;
					case 0xFE:
						MessageStatus = "0xFE: gZtcTooBig_c (A data item to be set or retrieved is too big for the buffer available to hold it.)";
						break;
					case 0xFF:
						MessageStatus = "0xFF: gZtcError_c (Non-specific, catchall error code.)";
						break;
					default:
						break;
					}

				}
				String logMessage = "Extracted ZTC-ERROR.Event Status:" + MessageStatus;
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix(logMessage, message);
			}

			/* ZDP-Mgmt_Nwk_Update.Notify */

			else if (_command == FreescaleConstants.ZDPMgmt_Nwk_UpdateNotify) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Mgmt_Nwk_Update.Notify", message);

				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.NWK_UPDATE) {

							EnergyScanResult _result = new EnergyScanResult();

							int _address = DataManipulation.toIntFromShort((byte) message[4], (byte) message[3]);

							short _status = message[5];
							if (_status == GatewayConstants.SUCCESS) {
								byte[] _scannedChannel = new byte[4];
								_scannedChannel[0] = (byte) message[9];
								_scannedChannel[1] = (byte) message[8];
								_scannedChannel[2] = (byte) message[7];
								_scannedChannel[3] = (byte) message[6];

								int _totalTrasmission = DataManipulation.toIntFromShort((byte) message[11], (byte) message[10]);

								int _trasmissionFailure = DataManipulation.toIntFromShort((byte) message[13], (byte) message[12]);

								short _scannedChannelListCount = message[14];
								for (int i = 0; i < _scannedChannelListCount; i++) {
									ScannedChannel _sc = new ScannedChannel();
									// _sc.setChannel(value)
									_sc.setEnergy(message[15 + i]);

									_result.getScannedChannel().add(_sc);
								}
								synchronized (pl) {
									pl.getStatus().setCode(message[7]);
									pl.set_objectOfResponse(_result);
									pl.notify();
								}
								break;

							}

						}
					}
				}

			}

			/* ZDP-SimpleDescriptor.Response */
			else if (_command == FreescaleConstants.ZDPSimpleDescriptorResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-ExtendedSimpleDescriptor.Response", message);
				/* Address + EndPoint */
				Address _add = new Address();

				_add.setNetworkAddress(DataManipulation.toIntFromShort((byte) message[5], (byte) message[4]));
				byte EndPoint = (byte) message[7];
				String Key = String.format("%04X", _add.getNetworkAddress()) + String.format("%02X", EndPoint);
				// Found ZDP-SimpleDescriptor.Response. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						/* Address + EndPoint */
						if ((pl.getType() == TypeMessage.GET_SIMPLE_DESCRIPTOR) && pl.get_Key().equalsIgnoreCase(Key)) {

							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								if (pl.getStatus().getCode() == GatewayConstants.SUCCESS) {

									SimpleDescriptor _sp = new SimpleDescriptor();
									_sp.setApplicationProfileIdentifier(DataManipulation.toIntFromShort((byte) message[9], (byte) message[8]));
									_sp.setApplicationDeviceIdentifier(DataManipulation.toIntFromShort((byte) message[11], (byte) message[10]));
									_sp.setApplicationDeviceVersion(message[12]);
									int _index = 14;
									short _numInpCluster = message[13];
									for (int i = 0; i < _numInpCluster; i++) {
										_sp.getApplicationInputCluster().add(DataManipulation.toIntFromShort((byte) message[_index + 1], (byte) message[_index]));
										_index = _index + 2;
									}

									short _numOutCluster = message[_index++];

									for (int i = 0; i < _numOutCluster; i++) {
										_sp.getApplicationOutputCluster().add(DataManipulation.toIntFromShort((byte) message[_index + 1], (byte) message[_index]));
										_index = _index + 2;
									}
									ServiceDescriptor _toRes = new ServiceDescriptor();
									_toRes.setAddress(_add);
									_toRes.setEndPoint(EndPoint);
									_toRes.setSimpleDescriptor(_sp);
									pl.set_objectOfResponse(_toRes);
								}
								pl.notify();
							}
							break;
						}
					}
				}

			}

			/* APS-GetEndPointIdList.Confirm */
			else if (_command == FreescaleConstants.APSGetEndPointIdListConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APS-GetEndPointIdList.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.GET_END_POINT_LIST)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								NodeServices _res = new NodeServices();
								if (pl.getStatus().getCode() == GatewayConstants.SUCCESS) {
									short length = message[4];
									for (int i = 0; i < length; i++) {
										ActiveEndpoints _ep = new ActiveEndpoints();
										_ep.setEndPoint(message[5 + i]);
										_res.getActiveEndpoints().add(_ep);
									}
								}
								pl.set_objectOfResponse(_res);
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* ZDP-BIND.Response */
			else if (_command == FreescaleConstants.ZDPMgmtBindResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-BIND.Response", message);

				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.ADD_BINDING)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								switch (pl.getStatus().getCode()) {
								case GatewayConstants.SUCCESS:

									break;

								case 0x84:
									pl.getStatus().setMessage("NOT_SUPPORTED (NOT SUPPORTED)");
									break;

								case 0x8C:
									pl.getStatus().setMessage("TABLE_FULL (TABLE FULL)");
									break;
								case 0x8D:
									pl.getStatus().setMessage("NOT_AUTHORIZED (NOT AUTHORIZED)");
									break;
								}
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* ZDP-UNBIND.Response */
			else if (_command == FreescaleConstants.ZDPUnbindResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-UNBIND.Response", message);

				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.REMOVE_BINDING)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								switch (pl.getStatus().getCode()) {
								case GatewayConstants.SUCCESS:

									break;

								case 0x84:
									pl.getStatus().setMessage("NOT_SUPPORTED (NOT SUPPORTED)");
									break;
								case 0x88:
									pl.getStatus().setMessage("No_Entry (No Entry)");
									break;
								case 0x8D:
									pl.getStatus().setMessage("NOT_AUTHORIZED (NOT AUTHORIZED");
									break;
								}
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* ZDP-Mgmt_Bind.Response */
			else if (_command == FreescaleConstants.ZDPMgmt_BindResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Mgmt_Bind.Response", message);

				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.GET_BINDINGS)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								BindingList _res = new BindingList();

								if (pl.getStatus().getCode() == GatewayConstants.SUCCESS) {
									short length = message[6];
									int _index = 6;
									for (int i = 0; i < length; i++) {
										Binding _b = new Binding();
										long src_longAddress = DataManipulation.toLong((byte) message[_index + 8], (byte) message[_index + 7], (byte) message[_index + 6], (byte) message[_index + 5], (byte) message[_index + 4], (byte) message[_index + 3], (byte) message[_index + 2], (byte) message[_index + 1]);
										short _srcEP = message[_index + 9];

										int _cluster = DataManipulation.toIntFromShort((byte) message[_index + 11], (byte) message[_index + 10]);

										short _DestinationMode = message[_index + 12];
										Device _dev = new Device();

										if (_DestinationMode == 0x03) {

											long dst_longAddress = DataManipulation.toLong((byte) message[_index + 20], (byte) message[_index + 19], (byte) message[_index + 18], (byte) message[_index + 17], (byte) message[_index + 16], (byte) message[_index + 15], (byte) message[_index + 14], (byte) message[_index + 13]);

											short _dstEP = message[_index + 21];
											_dev.setAddress(BigInteger.valueOf(dst_longAddress));
											_dev.setEndpoint(_dstEP);
											_index = _index + 21;
										} else if (_DestinationMode == 0x01) {

											int _groupId = DataManipulation.toIntFromShort((byte) message[_index + 14], (byte) message[_index + 13]);
											_dev.setAddress(BigInteger.valueOf(_groupId));
											_index = _index + 10;
										}
										_b.setClusterID(_cluster);
										_b.setSourceEndpoint(_srcEP);
										_b.setSourceIEEEAddress(BigInteger.valueOf(src_longAddress));

										_b.getDeviceDestination().add(_dev);
										_res.getBinding().add(_b);

									}
								}
								pl.set_objectOfResponse(_res);
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* APS-DeregisterEndPoint.Confirm */
			else if (_command == FreescaleConstants.APSDeRegisterEndPointConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APS-DeregisterEndPoint.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.DEREGISTER_END_POINT)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}

			}

			/* APS-ZDP-Mgmt_Lqi.Response */
			else if (_command == FreescaleConstants.ZDPMgmtLqiResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Mgmt_Lqi.Response... waiting the related Indication ZDO", message);

			}
			/* ZTC-ReadExtAddr.Confirm */
			else if (_command == FreescaleConstants.ZTCReadExtAddrConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZTC-ReadExtAddr.Confirm", message);
				long longAddress = DataManipulation.toLong((byte) message[11], (byte) message[10], (byte) message[9], (byte) message[8], (byte) message[7], (byte) message[6], (byte) message[5], (byte) message[4]);
				BigInteger _bi = BigInteger.valueOf(longAddress);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.READ_EXT_ADDRESS)) {
							synchronized (pl) {
								pl.set_objectOfResponse(_bi);
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}

			}

			/* ZDP-IEEE_addr.response */
			else if (_command == FreescaleConstants.ZDPIeeeAddrResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-IEEE_addr.response", message);
				long longAddress = DataManipulation.toLong((byte) message[11], (byte) message[10], (byte) message[9], (byte) message[8], (byte) message[7], (byte) message[6], (byte) message[5], (byte) message[4]);
				BigInteger _bi = BigInteger.valueOf(longAddress);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if ((pl.getType() == TypeMessage.READ_IEEE_ADDRESS)) {
							synchronized (pl) {
								pl.set_objectOfResponse(_bi);
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}

			}
			/* ZDP-Mgmt_Leave.Response */
			else if (_command == FreescaleConstants.ZDPMgmtLeaveResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Mgmt_Leave.Response", message);

			}
			/* ZDP-Active_EP_rsp.response */
			else if (_command == FreescaleConstants.ZDPActiveEpResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Active_EP_rsp.response", message);
				short Status = message[3];
				Address _add = new Address();

				_add.setNetworkAddress(DataManipulation.toIntFromShort((byte) message[5], (byte) message[4]));
				String Key = String.format("%04X", _add.getNetworkAddress());
				List<Short> _toRes = null;

				NodeServices _node = new NodeServices();
				_node.setAddress(_add);

				switch (Status) {
				case 0x00:
					_toRes = new ArrayList<Short>();
					int _EPCount = message[6];

					for (int i = 0; i < _EPCount; i++) {
						_toRes.add(message[7 + i]);
						ActiveEndpoints _aep = new ActiveEndpoints();
						_aep.setEndPoint(message[7 + i]);
						_node.getActiveEndpoints().add(_aep);

					}
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("ZDP-Active_EP_rsp.response status:00 - Success");
					}
					break;
				case 0x80:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("ZDP-Active_EP_rsp.response status:80 - Inv_RequestType");
					}
					break;
				case 0x89:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("ZDP-Active_EP_rsp.response status:89 - No_Descriptor");
					}
					break;
				case 0x81:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("ZDP-Active_EP_rsp.response status:81 - Device_Not_found");
					}
					break;
				}
				// Found ZDP-Active_EP_rsp.response. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						/* DestAddress */
						if ((pl.getType() == TypeMessage.ACTIVE_EP) && pl.get_Key().equalsIgnoreCase(Key)) {
							synchronized (pl) {
								pl.set_objectOfResponse(_toRes);
								pl.getStatus().setCode(Status);
								pl.notify();

							}
							break;
						}
					}
				}

			}

			/* ZDP-StopNwkEx.Confirm */
			else if (_command == FreescaleConstants.ZTCStopNwkExConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-StopNwkEx.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.STOP_NETWORK) {
							if (message[3] == 0x00) {
								gal.get_gatewayEventManager().notifyGatewayStopResult(makeStatusObject("The stop command has been processed byt ZDO with success.", (short) 0x00));
								synchronized (gal) {
									gal.setGatewayStatus(GatewayStatus.GW_STOPPING);
								}
							}
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}

			}
			/* NLME-GET.Confirm */
			else if (_command == FreescaleConstants.NLMEGetConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-GET.Confirm", message);
				String _Key = String.format("%02X", message[4]);
				// Found APSDE-DATA.Confirm. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.NMLE_GET && pl.get_Key().equalsIgnoreCase(_Key)) {
							short _Length = (short) DataManipulation.toIntFromShort((byte) message[9], (byte) message[8]);
							byte[] _res = DataManipulation.subByteArray(message, 10, _Length + 9);
							if (_Length >= 2)
								_res = DataManipulation.reverseBytes(_res);
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.set_objectOfResponse(DataManipulation.convertBytesToString(_res));
								pl.notify();
							}
							break;
						}
					}
				}

			}
			/* APSME_GET.Confirm */
			else if (_command == FreescaleConstants.APSMEGetConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APSME_GET.Confirm", message);
				String _Key = String.format("%02X", message[4]);
				// Found APSME_GET-DATA.Confirm. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.APSME_GET && pl.get_Key().equalsIgnoreCase(_Key)) {
							short _Length = (short) DataManipulation.toIntFromShort((byte) message[9], (byte) message[8]);
							byte[] _res = DataManipulation.subByteArray(message, 10, _Length + 9);
							if (_Length >= 2)
								_res = DataManipulation.reverseBytes(_res);
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.set_objectOfResponse(DataManipulation.convertBytesToString(_res));
								pl.notify();
							}
							break;
						}
					}
				}

			}
			// ZDP-StartNwkEx.Confirm
			else if (_command == FreescaleConstants.ZTCStartNwkExConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-StartNwkEx.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.START_NETWORK) {
							if (message[3] == 0x00) {
								gal.setGatewayStatus(GatewayStatus.GW_STARTED);
							}
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* APS-RegisterEndPoint.Confirm */
			else if (_command == FreescaleConstants.APSRegisterEndPointConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APS-RegisterEndPoint.Confirm", message);
				// Found APS-RegisterEndPoint.Confirm. Remove the lock
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.CONFIGURE_END_POINT) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}
			}
			/* ZTC-ModeSelect.Confirm */
			else if (_command == FreescaleConstants.ZTCModeSelectConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZTC-ModeSelect.Confirm", message);
				short status = message[3];
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.MODE_SELECT) {
							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.notify();
							}
							break;
						}
					}
				}
			}
			/* MacGetPIBAttribute.Confirm */
			else if (_command == FreescaleConstants.MacGetPIBAttributeConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacGetPIBAttribute.Confirm", message);
			}
			/* MacBeaconNotify.Indication */
			else if (_command == FreescaleConstants.MacBeaconNotifyIndication) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacBeaconNotify.Indication", message);
			}
			/* MacBeaconStart.Indication */
			else if (_command == FreescaleConstants.MacPollNotifyIndication) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacBeaconStart.Indication", message);
			}
			/* NLME-NETWORK-FORMATION.Confirmn */
			else if (_command == FreescaleConstants.NLMENETWORKFORMATIONConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-NETWORK-FORMATION.Confirm", message);
			}
			/* NLME-START-ROUTER.Request */
			else if (_command == FreescaleConstants.NLMESTARTROUTERRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-NETWORK-FORMATION.Confirm", message);
			}
			/* MacStart.Request */
			else if (_command == FreescaleConstants.MacStartRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacStart.Request", message);
			}
			/* MacStart.Confirm */
			else if (_command == FreescaleConstants.MacStartConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacStart.Confirm", message);
			}
			/* NLME-START-ROUTER.Confirm */
			else if (_command == FreescaleConstants.NLMESTARTROUTERConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-START-ROUTER.Confirm", message);
			}
			/* NWK-ProcessSecureFrame.Report */
			else if (_command == FreescaleConstants.NWKProcessSecureFrameReport) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NWK-ProcessSecureFrame.Report", message);
			}
			/* ZDP-Nwk-ProcessSecureFrame.Confirm */
			else if (_command == FreescaleConstants.ZDPNwkProcessSecureFrameConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Nwk-ProcessSecureFrame.Confirm", message);
			}

			/* BlackBox.WriteSAS.Confirm */
			else if (_command == FreescaleConstants.BlackBoxWriteSASConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted BlackBox.WriteSAS.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.WRITE_SAS) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.notify();
							}
							break;
						}
					}
				}
			}
			/* ZTC-GetChannel.Confirm */
			else if (_command == FreescaleConstants.ZTCGetChannelConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZTC-GetChannel.Confirm", message);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.CHANNEL_REQUEST) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);
								pl.set_objectOfResponse(message[4]);
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* ZDP-NodeDescriptor.Response */
			else if (_command == FreescaleConstants.ZDPNodeDescriptorResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-NodeDescriptor.Response", message);
				int _NWKAddressOfInterest = DataManipulation.toIntFromShort((byte) message[5], (byte) message[4]);
				Address _addressOfInterst = new Address();
				_addressOfInterst.setNetworkAddress(_NWKAddressOfInterest);
				NodeDescriptor _node = new NodeDescriptor();

				/* First Byte */
				byte _first = (byte) message[6];
				byte _Logical_byte = (byte) (_first & 0x07);/* Bits 0,1,2 */
				byte _ComplexDescriptorAvalilable = (byte) ((_first & 0x08) >> 3);/* Bit3 */
				byte _UserDescriptorAvalilable = (byte) ((_first & 0x0A) >> 4);/* Bit4 */
				switch (_Logical_byte) {
				case FreescaleConstants.LogicalType.Coordinator:
					_node.setLogicalType(LogicalType.COORDINATOR);
					break;
				case FreescaleConstants.LogicalType.Router:
					_node.setLogicalType(LogicalType.ROUTER);
					break;
				case FreescaleConstants.LogicalType.EndDevice:
					_node.setLogicalType(LogicalType.END_DEVICE);
					break;
				default:
					throw new Exception("LogicalType is not valid value");
				}
				_node.setComplexDescriptorAvailable((_ComplexDescriptorAvalilable == 1 ? true : false));
				_node.setUserDescriptorAvailable((_UserDescriptorAvalilable == 1 ? true : false));

				/* Second Byte */
				byte _second = (byte) message[7];
				/* Aps flags bits 0,1,2 */
				byte _FrequencyBand = (byte) ((_second & 0xF8) >> 0x03);/*
																		 * bits
																		 * 3
																		 * ,4,5,
																		 * 6,7
																		 */
				switch (_FrequencyBand) {
				case 0x01:
					_node.setFrequencyBand("868MHz");
					break;
				case 0x04:
					_node.setFrequencyBand("900MHz");
					break;
				case 0x08:
					_node.setFrequencyBand("2400MHz");
					break;
				default:
					_node.setFrequencyBand("Reserved");
					break;
				}

				/* MACcapabilityFlags_BYTE Byte */
				byte _MACcapabilityFlags_BYTE = (byte) message[8];
				MACCapability _maccapability = new MACCapability();
				byte _AlternatePanCoordinator = (byte) (_MACcapabilityFlags_BYTE & 0x01);/* Bit0 */
				byte _DeviceIsFFD = (byte) ((_MACcapabilityFlags_BYTE & 0x02) >> 1);/* Bit1 */
				byte _MainsPowered = (byte) ((_MACcapabilityFlags_BYTE & 0x04) >> 2);/* Bit2 */
				byte _ReceiverOnWhenIdle = (byte) ((_MACcapabilityFlags_BYTE & 0x08) >> 3);/* Bit3 */
				// bit 4-5 reserved
				byte _SecuritySupported = (byte) ((_MACcapabilityFlags_BYTE & 0x40) >> 6);/* Bit6 */
				byte _AllocateAddress = (byte) ((_MACcapabilityFlags_BYTE & 0x80) >> 7);/* Bit7 */
				_maccapability.setAlternatePanCoordinator((_AlternatePanCoordinator == 1 ? true : false));
				_maccapability.setDeviceIsFFD((_DeviceIsFFD == 1 ? true : false));
				_maccapability.setMainsPowered((_MainsPowered == 1 ? true : false));
				_maccapability.setReceiverOnWhenIdle((_ReceiverOnWhenIdle == 1 ? true : false));
				_maccapability.setSecuritySupported((_SecuritySupported == 1 ? true : false));
				_maccapability.setAllocateAddress((_AllocateAddress == 1 ? true : false));
				_node.setMACCapabilityFlag(_maccapability);

				/* ManufacturerCode_BYTES */
				int _ManufacturerCode_BYTES = DataManipulation.toIntFromShort((byte) message[10], (byte) message[9]);
				_node.setManufacturerCode(_ManufacturerCode_BYTES);

				/* MaximumBufferSize_BYTE */
				short _MaximumBufferSize_BYTE = message[11];
				_node.setMaximumBufferSize(_MaximumBufferSize_BYTE);

				/* MaximumTransferSize_BYTES */
				int _MaximumTransferSize_BYTES = DataManipulation.toIntFromShort((byte) message[13], (byte) message[12]);
				_node.setMaximumIncomingTransferSize(_MaximumTransferSize_BYTES);

				/* ServerMask_BYTES */
				int _ServerMask_BYTES = DataManipulation.toIntFromShort((byte) message[15], (byte) message[14]);
				ServerMask _serverMask = new ServerMask();
				byte _PrimaryTrustCenter = (byte) (_ServerMask_BYTES & 0x01);/* Bit0 */
				byte _BackupTrustCenter = (byte) ((_ServerMask_BYTES & 0x02) >> 1);/* Bit1 */
				byte _PrimaryBindingTableCache = (byte) ((_ServerMask_BYTES & 0x04) >> 2);/* Bit2 */
				byte _BackupBindingTableCache = (byte) ((_ServerMask_BYTES & 0x08) >> 3);/* Bit3 */
				byte _PrimaryDiscoveryCache = (byte) ((_ServerMask_BYTES & 0x10) >> 4);/* Bit4 */
				byte _BackupDiscoveryCache = (byte) ((_ServerMask_BYTES & 0x20) >> 5);/* Bit5 */
				_serverMask.setPrimaryTrustCenter((_PrimaryTrustCenter == 1 ? true : false));
				_serverMask.setBackupTrustCenter((_BackupTrustCenter == 1 ? true : false));
				_serverMask.setPrimaryBindingTableCache((_PrimaryBindingTableCache == 1 ? true : false));
				_serverMask.setBackupBindingTableCache((_BackupBindingTableCache == 1 ? true : false));
				_serverMask.setPrimaryDiscoveryCache((_PrimaryDiscoveryCache == 1 ? true : false));
				_serverMask.setBackupDiscoveryCache((_BackupDiscoveryCache == 1 ? true : false));
				_node.setServerMask(_serverMask);

				/* MaximumOutTransferSize_BYTES */
				int _MaximumOutTransferSize_BYTES = DataManipulation.toIntFromShort((byte) message[17], (byte) message[16]);
				_node.setMaximumOutgoingTransferSize(_MaximumOutTransferSize_BYTES);

				/* CapabilityField_BYTES */
				byte _CapabilityField_BYTES = (byte) message[18];
				DescriptorCapability _DescriptorCapability = new DescriptorCapability();
				byte _ExtendedActiveEndpointListAvailable = (byte) (_CapabilityField_BYTES & 0x01);/* Bit0 */
				byte _ExtendedSimpleDescriptorListAvailable = (byte) ((_CapabilityField_BYTES & 0x02) >> 1);/* Bit1 */
				_DescriptorCapability.setExtendedActiveEndpointListAvailable((_ExtendedActiveEndpointListAvailable == 1 ? true : false));
				_DescriptorCapability.setExtendedSimpleDescriptorListAvailable((_ExtendedSimpleDescriptorListAvailable == 1 ? true : false));
				_node.setDescriptorCapabilityField(_DescriptorCapability);
				String _key = String.format("%04X", _NWKAddressOfInterest);
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.NODE_DESCRIPTOR && pl.get_Key().equalsIgnoreCase(_key)) {
							synchronized (pl) {
								pl.getStatus().setCode(message[3]);/* Status */
								pl.set_objectOfResponse(_node);
								pl.notify();
							}
							break;
						}
					}
				}
			}
			/* NMLE-SET.Confirm */
			else if (_command == FreescaleConstants.NMLESETConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NMLE-SET.Confirm", message);
				short status = message[3];
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.NMLE_SET) {

							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.notify();
							}
							break;
						}
					}
				}
			}
			/* APSME-SET.Confirm */
			else if (_command == FreescaleConstants.APSMESetConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted APSME-SET.Confirm", message);
				short status = message[3];
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.APSME_SET) {

							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.notify();
							}
							break;
						}
					}
				}
			}

			/* ZDP-Mgmt_Permit_Join.response */
			else if (_command == FreescaleConstants.ZDPMgmt_Permit_JoinResponse) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted ZDP-Mgmt_Permit_Join.response", message);
				short status = message[3];
				String mess = "";

				switch (status) {
				case 0x00:

					break;
				case 0x80:
					mess = "InvRequestType";
					break;
				case 0x84:
					mess = "Not Supported";
					break;
				case 0x87:
					mess = "Table Full";
					break;
				case 0x8D:
					mess = "NOT AUTHORIZED";
					break;
				case 0xC5:
					mess = "Already present in the network";
					break;

				}
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.PERMIT_JOIN) {
							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.getStatus().setMessage(mess);
								pl.notify();
							}
							break;
						}
					}

				}
			}

			/* APS-ClearDeviceKeyPairSet.Confirm */
			else if (_command == FreescaleConstants.APSClearDeviceKeyPairSetConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("APS-ClearDeviceKeyPairSet.Confirm", message);
				short status = message[3];
				String mess = "";
				switch (status) {
				case 0x00:

					break;
				}
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.CLEAR_DEVICE_KEY_PAIR_SET) {
							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.getStatus().setMessage(mess);
								pl.notify();
							}
							break;
						}
					}

				}

			}

			/* ZTC-ClearNeighborTableEntry.Confirm */
			else if (_command == FreescaleConstants.ZTCClearNeighborTableEntryConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("ZTC-ClearNeighborTableEntry.Confirm", message);
				short status = message[3];
				String mess = "";
				switch (status) {
				case 0x00:

					break;
				}
				synchronized (listLocker) {
					for (ParserLocker pl : listLocker) {
						if (pl.getType() == TypeMessage.CLEAR_NEIGHBOR_TABLE_ENTRY) {
							synchronized (pl) {
								pl.getStatus().setCode(status);
								pl.getStatus().setMessage(mess);
								pl.notify();
							}
							break;
						}
					}

				}

			}

			/* NLME-JOIN.Confirm */
			else if (_command == FreescaleConstants.NLMEJOINConfirm) {
				short _status = message[8];
				switch (_status) {
				case 0x00:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: SUCCESS (Joined the network)");
					}
					break;
				case 0xC2:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: INVALID_REQUEST (Not Valid Request)");
					}
					break;
				case 0xC3:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: NOT_PERMITTED (Not allowed to join the network)");
					}
					break;
				case 0xCA:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: NO_NETWORKS (Network not found)");
					}
					break;
				case 0x01:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: PAN_AT_CAPACITY (PAN at capacity)");
					}
					break;
				case 0x02:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: PAN_ACCESS_DENIED (PAN access denied)");
					}
					break;
				case 0xE1:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: CHANNEL_ACCESS_FAILURE (Transmission failed due to activity on the channel)");
					}
					break;
				case 0xE4:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: FAILED_SECURITY_CHECK (The received frame failed security check)");
					}
					break;
				case 0xE8:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: INVALID_PARAMETER (A parameter in the primitive is out of the valid range)");
					}
					break;
				case 0xE9:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: NO_ACK (Acknowledgement was not received)");
					}
					break;
				case 0xEB:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: NO_DATA (No response data was available following a request)");
					}
					break;
				case 0xF3:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: UNAVAILABLE_KEY (The appropriate key is not available in the ACL)");
					}
					break;
				case 0xEA:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted NLME-JOIN.Confirm: NO_BEACON (No Networks)");
					}
					break;
				default:
					throw new Exception("Extracted NLME-JOIN.Confirm: Invalid Status - " + _status);
				}
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("NLME-JOIN.Confirm", message);
			}

			/* ZDO-NetworkState.Event */
			else if (_command == FreescaleConstants.ZDONetworkStateEvent) {
				short _status = message[3];
				String mess;
				switch (_status) {
				case 0x00:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceInitialized (Device Initialized)");
					}
					break;
				case 0x01:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceinNetworkDiscoveryState (Device in Network Discovery State)");
					}
					break;
				case 0x02:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceJoinNetworkstate (Device Join Network state)");
					}
					break;
				case 0x03:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceinCoordinatorstartingstate (Device in Coordinator starting state)");
					}
					gal.setGatewayStatus(GatewayStatus.GW_STARTING);
					break;
				case 0x04:
					mess = "ZDO-NetworkState.Event: DeviceinRouterRunningstate (Device in Router Running state)";
					gal.get_gatewayEventManager().notifyGatewayStartResult(makeStatusObject(mess, _status));
					gal.setGatewayStatus(GatewayStatus.GW_RUNNING);
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info(mess);
					}
					break;
				case 0x05:
					mess = "ZDO-NetworkState.Event: DeviceinEndDeviceRunningstate (Device in End Device Running state)";
					gal.get_gatewayEventManager().notifyGatewayStartResult(makeStatusObject(mess, _status));
					gal.setGatewayStatus(GatewayStatus.GW_RUNNING);

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info(mess);
					}
					break;
				case 0x09:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: Deviceinleavenetworkstate (Device in leave network state)");
					}

					gal.setGatewayStatus(GatewayStatus.GW_STOPPING);
					break;
				case 0x0A:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: Deviceinauthenticationstate (Device in authentication state)");
					}
					break;
				case 0x0B:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: Deviceinstoppedstate (Device in stopped state)");
					}
					gal.setGatewayStatus(GatewayStatus.GW_STOPPED);

					break;
				case 0x0C:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceinOrphanjoinstate (Device in Orphan join state)");
					}
					break;
				case 0x10:
					mess = "ZDO-NetworkState.Event: DeviceinCoordinatorRunningstate (Device is Coordinator Running state)";

					gal.get_gatewayEventManager().notifyGatewayStartResult(makeStatusObject(mess, _status));
					gal.setGatewayStatus(GatewayStatus.GW_RUNNING);
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info(mess);
					}
					break;
				case 0x11:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceinKeytransferstate (Device in Key transfer state)");
					}
					break;
				case 0x12:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: Deviceinauthenticationstate (Device in authentication state)");
					}
					break;
				case 0x13:
					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Extracted ZDO-NetworkState.Event: DeviceOfftheNetwork (Device Off the Network)");
					}
					break;
				default:
					throw new Exception("ZDO-NetworkState.Event: Invalid Status - " + _status);
				}
			}
			/* MacSetPIBAttribute.Confirm */
			else if (_command == FreescaleConstants.MacSetPIBAttributeConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacSetPIBAttribute.Confirm", message);
			}
			/* NLME-ENERGY-SCAN.Request */
			else if (_command == FreescaleConstants.NLMEENERGYSCANRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-ENERGY-SCAN.Request", message);
			}
			/* MacScan.Request */
			else if (_command == FreescaleConstants.MacScanRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacScan.Request", message);
			}
			/* MacScan.Confirm */
			else if (_command == FreescaleConstants.MacScanConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacScan.Confirm", message);
			}
			/* NLME-ENERGY-SCAN.confirm */
			else if (_command == FreescaleConstants.NLMEENERGYSCANconfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-ENERGY-SCAN.confirm", message);
			}
			/* NLME-NETWORK-DISCOVERY.Request */
			else if (_command == FreescaleConstants.NLMENETWORKDISCOVERYRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-NETWORK-DISCOVERY.Request", message);
			}
			/* MacScan.Request */
			else if (_command == FreescaleConstants.MacScanRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted MacScan.Request", message);
			}
			/* NLME-NETWORK-DISCOVERY.Confirm */
			else if (_command == FreescaleConstants.NLMENetworkDiscoveryConfirm) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-NETWORK-DISCOVERY.Confirm", message);
			}
			/* NLME-NETWORK-FORMATION.Request */
			else if (_command == FreescaleConstants.NLMENETWORKFORMATIONRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-NETWORK-FORMATION.Request", message);
			}
			/* NLME-SET.Request */
			else if (_command == FreescaleConstants.NLMESetRequest) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted NLME-SET.Request", message);
			}
			/* NLME-NWK-STATUS.Indication */
			else if (_command == FreescaleConstants.NLMENwkStatusIndication) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("NLME-NWK-STATUS.Indication", message);
			}
			/* NLME-ROUTE-DISCOVERY.confirm */
			else if (_command == FreescaleConstants.NLMENWKSTATUSIndication) {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("NLME-ROUTE-DISCOVERY.confirm", message);
			} else {
				if (gal.getPropertiesManager().getDebugEnabled())
					DataManipulation.logArrayHexRadix("Extracted a Message not in Message Management", message);
			}

			synchronized (this) {
				_size = messages.size();
			}
		}

	}

	private Status makeStatusObject(String message, short code) {
		Status toReturn = new Status();
		toReturn.setMessage(message);
		toReturn.setCode(code);
		return toReturn;
	}

	private final ChecksumControl csc = new ChecksumControl();

	public synchronized void addToSendDataQueue(final ByteArrayObject toAdd) throws Exception {
		listOfCommandToSend.put(toAdd);
	}

	public ByteArrayObject Set_SequenceStart_And_FSC(ByteArrayObject x, short commandCode) {
		byte size = (byte) x.getByteCount(false);
		byte opgroup = (byte) ((commandCode >> 8) & 0xff);
		byte opcode = (byte) (commandCode & 0xff);
		x.addOPGroup(opgroup);
		x.addOPCode(opcode);
		x.addLength(size);
		byte FSC = 0;
		for (Byte b : x.getByteArray())
			FSC ^= b;
		x.addStartSequance((byte) 0x02);
		x.addByte(FSC);
		return x;
	}

	@Override
	public Status APSME_SETSync(long timeout, short _AttID, String _value) throws GatewayException, Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) _AttID);/* _AttId */
		_res.addByte((byte) 0x00);
		_res.addByte((byte) 0x00);
		_res.addByte((byte) 0x00);
		for (byte x : DataManipulation.hexStringToByteArray(_value))
			_res.addByte(x);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSMESetRequest);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("APSME_SET command:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.APSME_SET);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}

			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}

			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}

		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in APSME SET");
			}
			throw new GatewayException("Timeout expired in APSME SET");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on APSME_SET.request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			}
			return status;
		}

	}

	@Override
	public String APSME_GETSync(long timeout, short _AttID) throws Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) _AttID);/* iId */
		_res.addByte((byte) 0x00);/* iIndex */
		_res.addByte((byte) 0x00);/* iEntries */
		_res.addByte((byte) 0x00);/* iEntrySize */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSMEGetRequest);/*
																					 * StartSequence
																					 * +
																					 * Control
																					 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("APSME_GET.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();

		lock.setType(TypeMessage.APSME_GET);
		lock.set_Key(String.format("%02X", _AttID));
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}

		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in APSME-GET.Request");
			}
			throw new GatewayException("Timeout expired in APSME-GET.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on APSME-GET.Request. Status code: " + status.getCode() + " Status Message: " + status.getMessage());
			} else
				return (String) lock.get_objectOfResponse();
		}

	}

	@Override
	public String NMLE_GetSync(long timeout, short _AttID) throws Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) _AttID);/* iId */
		_res.addByte((byte) 0x00);/* iIndex */
		_res.addByte((byte) 0x00);/* iEntries */
		_res.addByte((byte) 0x00);/* iEntrySize */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMEGetRequest);/*
																				 * StartSequence
																				 * +
																				 * Control
																				 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("NLME-GET.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.NMLE_GET);
		lock.set_Key(String.format("%02X", _AttID));
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}

		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in NLME-GET.Request");
			}
			throw new GatewayException("Timeout expired in NLME-GET.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on  NLME-GET.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());

			} else
				return (String) lock.get_objectOfResponse();
		}

	}

	@Override
	public Status stopNetworkSync(long timeout) throws Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) 0x01);/*
								 * Stop Mode AnnounceStop (Stops after
								 * announcing it is leaving the network.)
								 */
		_res.addByte((byte) 0x00);/*
								 * Reset binding/group tables, node type, PAN ID
								 * etc to ROM state.
								 */
		_res.addByte((byte) 0x00);/* Restart after stopping. */
		_res.addByte((byte) 0xFF);/* Writes NVM upon stop. */

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCStopNwkExRequest);/*
																						 * StartSequence
																						 * +
																						 * Control
																						 */

		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZDP-StopNwkEx.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.STOP_NETWORK);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}

		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-StopNwkEx.Request");
			}

			throw new GatewayException("Timeout expired in ZDP-StopNwkEx.Request");
		} else {
			if (status.getCode() != 0) {

				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on  ZDP-StopNwkEx.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			}
			return status;
		}

	}

	@Override
	public Status sendApsSync(long timeout, APSMessage message) throws Exception {
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Data_FreeScale.send_aps");
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.APS);
		/* DestAddress + DestEndPoint + SourceEndPoint */
		BigInteger _DSTAdd = null;
		if ((message.getDestinationAddressMode() == GatewayConstants.EXTENDED_ADDRESS_MODE))
			_DSTAdd = message.getDestinationAddress().getIeeeAddress();
		else if ((message.getDestinationAddressMode() == GatewayConstants.ADDRESS_MODE_SHORT))
			_DSTAdd = BigInteger.valueOf(message.getDestinationAddress().getNetworkAddress());
		else if (((message.getDestinationAddressMode() == GatewayConstants.ADDRESS_MODE_ALIAS)))
			throw new Exception("The DestinationAddressMode == ADDRESS_MODE_ALIAS is not implemented!!");
		String _key = String.format("%016X", _DSTAdd) + String.format("%02X", message.getDestinationEndpoint()) + String.format("%02X", message.getSourceEndpoint());
		lock.set_Key(_key);

		if (gal.getPropertiesManager().getDebugEnabled()) {
			System.out.println("Sending Aps message to: " + String.format("%016X", _DSTAdd));
		}

		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(makeByteArrayFromApsMessage(message));
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in send aps message");
			}
			throw new GatewayException("Timeout expired in send aps message. No Confirm Received.");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on  APSDE-DATA.Request.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());

			}
			return status;
		}
	}

	@Override
	public short configureEndPointSync(long timeout, SimpleDescriptor desc) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte(desc.getEndPoint().byteValue());/* End Point */
		_res.addBytesShort(Short.reverseBytes(desc.getApplicationProfileIdentifier().shortValue()), 2);
		_res.addBytesShort(Short.reverseBytes(desc.getApplicationDeviceIdentifier().shortValue()), 2);
		_res.addByte(desc.getApplicationDeviceVersion().byteValue());/* DeviceVersion */
		_res.addByte((byte) desc.getApplicationInputCluster().size());/* ClusterInputSize */
		if (desc.getApplicationInputCluster().size() > 0) {
			for (Integer x : desc.getApplicationInputCluster())
				_res.addBytesShort(Short.reverseBytes(x.shortValue()), 2);
		}
		_res.addByte((byte) desc.getApplicationOutputCluster().size());/* ClusterOutputSize */
		if (desc.getApplicationOutputCluster().size() > 0) {
			for (Integer x : desc.getApplicationOutputCluster())
				_res.addBytesShort(Short.reverseBytes(x.shortValue()), 2);
		}
		_res.addByte((byte) 0x01);/* Maximum Window Size */

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSRegisterEndPointRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
		/* APS-RegisterEndPoint.Request */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Configure EndPoint command:" + _res.ToHexString());
		}
		short _endPoint = 0;

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.CONFIGURE_END_POINT);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in Configure End Point");
			}
			throw new GatewayException("Timeout expired in Configure End Point");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on  APS-RegisterEndPoint.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else
				_endPoint = desc.getEndPoint();
		}
		return _endPoint;
	}

	public ByteArrayObject makeByteArrayFromApsMessage(APSMessage apsMessage) throws Exception {
		byte[] data = apsMessage.getData();

		ByteArrayObject _res = new ByteArrayObject();
		byte dam = apsMessage.getDestinationAddressMode().byteValue();
		_res.addByte(dam);
		Address address = apsMessage.getDestinationAddress();
		byte[] _reversed = null;
		switch (dam) {
		// TODO Control those address modes!
		case GatewayConstants.ADDRESS_MODE_SHORT:
			byte[] networkAddress = DataManipulation.toByteVect(address.getNetworkAddress(), 8);
			_reversed = DataManipulation.reverseBytes(networkAddress);
			for (byte b : _reversed)
				_res.addByte(b);
			break;
		case GatewayConstants.EXTENDED_ADDRESS_MODE:
			byte[] ieeeAddress = DataManipulation.toByteVect(address.getIeeeAddress(), 8);
			_reversed = DataManipulation.reverseBytes(ieeeAddress);
			for (byte b : _reversed)
				_res.addByte(b);
			break;
		case GatewayConstants.ADDRESS_MODE_ALIAS:
			// TODO
			throw new UnsupportedOperationException("Address Mode Alias");
		default:
			throw new Exception("Address Mode undefined!");

		}

		_res.addByte((byte) apsMessage.getDestinationEndpoint());

		_res.addBytesShort(Short.reverseBytes(apsMessage.getProfileID().shortValue()), 2);

		_res.addBytesShort(Short.reverseBytes((short) apsMessage.getClusterID()), 2);

		_res.addByte((byte) apsMessage.getSourceEndpoint());

		if (data.length > 0x64) {
			throw new Exception("ASDU length must 0x64 or less in length");
		} else {
			_res.addByte((byte) data.length);

		}

		for (Byte b : data)
			_res.addByte(b);

		TxOptions txo = apsMessage.getTxOptions();
		int bitmap = 0x00;
		if (txo.isSecurityEnabled()) {
			bitmap |= 0x01;
		}
		if (txo.isUseNetworkKey()) {
			bitmap |= 0x02;
		}
		if (txo.isAcknowledged()) {
			bitmap |= 0x04;
		}
		if (txo.isPermitFragmentation()) {
			bitmap |= 0x08;
		}
		_res.addByte((byte) bitmap);

		_res.addByte((byte) apsMessage.getRadius());

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSDEDataRequest);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Write APS on: " + System.currentTimeMillis() + " Message:" + _res.ToHexString());
		}
		return _res;
	}

	@Override
	public Status SetModeSelectSync(long timeout) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) 0x01);/* UART Tx Blocking */
		_res.addByte((byte) 0x02);/* MCPS */
		_res.addByte((byte) 0x02);/* MLME */
		_res.addByte((byte) 0x02);/* ASP */
		_res.addByte((byte) 0x02);/* NLDE */
		_res.addByte((byte) 0x02);/* NLME */
		_res.addByte((byte) 0x02);/* APSDE */
		_res.addByte((byte) 0x02);/* AFDE */
		_res.addByte((byte) 0x02);/* APSME */
		_res.addByte((byte) 0x02);/* ZDP */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCModeSelectRequest);/*
																						 * StartSequence
																						 * +
																						 * Control
																						 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Reset command:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.MODE_SELECT);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}

			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			throw new GatewayException("Timeout expired in SetModeReq");
		} else if (status.getCode() != 0) {
			throw new GatewayException("Error on APSME-RESET.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
		}
		return status;
	}

	@Override
	public Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		Status _statWriteSas = WriteSasSync(timeout, sai);
		if (_statWriteSas.getCode() == 0) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("Starting Network...");
			}
			LogicalType devType = gal.getPropertiesManager().getSturtupAttributeInfo().getDeviceType();
			ByteArrayObject _res = new ByteArrayObject();

			if (devType == LogicalType.CURRENT) {
				throw new Exception("LogicalType not Valid!");
			} else if (devType == LogicalType.COORDINATOR) {

				_res.addByte(FreescaleConstants.DeviceType.Coordinator);/* Coordinator */
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("DeviceType == COORDINATOR");
				}
			} else if (devType == LogicalType.END_DEVICE) {
				_res.addByte(FreescaleConstants.DeviceType.EndDevice);/*
																	 * End
																	 * Device
																	 */
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("DeviceType == ENDDEVICE");
				}
			} else if (devType == LogicalType.ROUTER) {
				_res.addByte(FreescaleConstants.DeviceType.Router);/* Router */
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("DeviceType == ROUTER");
				}
			}
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("StartupSet value read from PropertiesManager:" + gal.getPropertiesManager().getStartupSet());
				logger.info("StartupControlMode value read from PropertiesManager:" + gal.getPropertiesManager().getSturtupAttributeInfo().getStartupControl().byteValue());
			}
			_res.addByte((byte) gal.getPropertiesManager().getStartupSet());
			_res.addByte(gal.getPropertiesManager().getSturtupAttributeInfo().getStartupControl().byteValue());

			_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCStartNwkExRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("Start Network command:" + _res.ToHexString());
			}
			ParserLocker lock = new ParserLocker();
			lock.setType(TypeMessage.START_NETWORK);
			Status status = null;
			try {
				synchronized (listLocker) {
					listLocker.add(lock);
				}
				addToSendDataQueue(_res);
				synchronized (lock) {
					try {
						lock.wait(timeout);
					} catch (InterruptedException e) {

					}
				}
				status = lock.getStatus();
				synchronized (listLocker) {
					if (listLocker.contains(lock))
						listLocker.remove(lock);
				}
			} catch (Exception e) {
				synchronized (listLocker) {
					if (listLocker.contains(lock))
						listLocker.remove(lock);
				}
			}
			if (status.getCode() == ParserLocker.INVALID_ID) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.error("Timeout expired in startGatewayDevice");
				}

				throw new GatewayException("Timeout expired in ZDP-StartNwkEx.Request");
			} else {
				if (status.getCode() != 0) {

					if (gal.getPropertiesManager().getDebugEnabled()) {
						logger.info("Returned Status: " + status);
					}
					throw new GatewayException("Error on ZDP-StartNwkEx.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
				}
			}

			return status;
		} else
			return _statWriteSas;
	}

	private Status WriteSasSync(long timeout, StartupAttributeInfo sai) throws InterruptedException, Exception {
		// TODO CHECK
		if (sai.getChannelMask() == null)
			sai = gal.getPropertiesManager().getSturtupAttributeInfo();

		LogicalType devType = sai.getDeviceType();

		ByteArrayObject res = new ByteArrayObject();
		res.addBytesShort(Short.reverseBytes(sai.getShortAddress().shortValue()), 2);

		/* Extended PanID */
		byte[] ExtendedPaniId = DataManipulation.toByteVect(sai.getExtendedPANId(), 8);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Extended PanID:" + DataManipulation.convertBytesToString(ExtendedPaniId));
		}

		for (byte b : DataManipulation.reverseBytes(ExtendedPaniId))
			res.addByte(b);

		/* Extended APS Use Extended PAN Id */
		byte[] APSUseExtendedPANId = DataManipulation.toByteVect(BigInteger.ZERO, 8);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("APS Use Extended PAN Id:" + DataManipulation.convertBytesToString(APSUseExtendedPANId));
		}
		for (byte b : DataManipulation.reverseBytes(APSUseExtendedPANId))
			res.addByte(b);
		res.addBytesShort(Short.reverseBytes(sai.getPANId().shortValue()), 2);
		byte[] _channel = Utils.buildChannelMask(sai.getChannelMask().shortValue());

		if (gal.getPropertiesManager().getDebugEnabled())
			logger.info("Channel readed from PropertiesManager:" + sai.getChannelMask());

		if (gal.getPropertiesManager().getDebugEnabled())
			DataManipulation.logArrayHexRadix("Channel after conversion", _channel);

		for (byte x : DataManipulation.reverseBytes(_channel))
			res.addByte(x);

		res.addByte(sai.getProtocolVersion().byteValue());
		res.addByte(sai.getStackProfile().byteValue());
		res.addByte(sai.getStartupControl().byteValue());

		/* TrustCenterAddress */
		byte[] TrustCenterAddress = DataManipulation.toByteVect(sai.getTrustCenterAddress(), 8);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("TrustCenterAddress:" + DataManipulation.convertBytesToString(TrustCenterAddress));
		}
		for (byte b : DataManipulation.reverseBytes(TrustCenterAddress))
			res.addByte(b);

		/* TrustCenterMasterKey */
		byte[] TrustCenterMasterKey = (devType == LogicalType.COORDINATOR) ? sai.getTrustCenterMasterKey() : DataManipulation.toByteVect(BigInteger.ZERO, 16);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("TrustCenterMasterKey:" + DataManipulation.convertBytesToString(TrustCenterMasterKey));
		}
		for (byte b : DataManipulation.reverseBytes(TrustCenterMasterKey))
			res.addByte(b);

		/* NetworKey */
		byte[] NetworKey = (devType == LogicalType.COORDINATOR) ? sai.getNetworkKey() : DataManipulation.toByteVect(BigInteger.ZERO, 16);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("NetworKey:" + DataManipulation.convertBytesToString(NetworKey));
		}
		for (byte b : DataManipulation.reverseBytes(NetworKey))
			res.addByte(b);

		res.addByte((sai.isUseInsecureJoin()) ? ((byte) 0x01) : ((byte) 0x00));

		/* PreconfiguredLinkKey */
		byte[] PreconfiguredLinkKey = sai.getPreconfiguredLinkKey();
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("PreconfiguredLinkKey:" + DataManipulation.convertBytesToString(PreconfiguredLinkKey));
		}
		for (byte b : PreconfiguredLinkKey)
			res.addByte(b);

		res.addByte(sai.getNetworkKeySeqNum().byteValue());
		res.addByte((byte) 0x01);

		res.addBytesShort(Short.reverseBytes(sai.getNetworkManagerAddress().shortValue()), 2);
		res.addByte(sai.getScanAttempts().byteValue());

		res.addBytesShort(sai.getTimeBetweenScans().shortValue(), 2);

		res.addBytesShort(Short.reverseBytes(sai.getRejoinInterval().shortValue()), 2);

		res.addBytesShort(Short.reverseBytes(sai.getMaxRejoinInterval().shortValue()), 2);

		res.addBytesShort(Short.reverseBytes(sai.getIndirectPollRate().shortValue()), 2);

		res.addByte(sai.getParentRetryThreshold().byteValue());

		res.addByte((sai.isConcentratorFlag()) ? ((byte) 0x01) : ((byte) 0x00));

		res.addByte(sai.getConcentratorRadius().byteValue());

		res.addByte(sai.getConcentratorDiscoveryTime().byteValue());

		res = Set_SequenceStart_And_FSC(res, FreescaleConstants.BlackBoxWriteSAS);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("WriteSas Command:" + res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.WRITE_SAS);
		Status status = null;
		try {

			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in write sas");
			}
			throw new GatewayException("Timeout expired in write sas");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on BlackBox.WriteSAS. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			}
			return status;
		}
	}

	@Override
	public Status permitJoinSync(long timeout, Address addrOfInterest, short duration, byte TCSignificance) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/*
																									 * Short
																									 * Network
																									 * Address
																									 */
		_res.addByte((byte) duration);/* Duration */
		_res.addByte(TCSignificance);/* TCSignificant */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMEPermitJoiningRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Permit Join command:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.PERMIT_JOIN);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID)
			throw new GatewayException("Timeout expired in Permit Join");
		else {
			if (status.getCode() != 0)
				throw new GatewayException("Error on ZDP-Mgmt_Permit_Join.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());

		}
		return status;
	}

	@Override
	public Status permitJoinAllSync(long timeout, Address addrOfInterest, short duration, byte TCSignificance) throws IOException, Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/*
																									 * Short
																									 * Network
																									 * Address
																									 */
		_res.addByte((byte) duration);/* Duration */
		_res.addByte(TCSignificance);/* TCSignificant */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMEPermitJoiningRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Permit Join command:" + _res.ToHexString());
		}

		addToSendDataQueue(_res);
		Status status = new Status();
		status.setCode((short) GatewayConstants.SUCCESS);

		return status;
	}

	@Override
	public short getChannelSync(long timeout) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCGetChannelRequest);// StartSequence
		// +
		// Control
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZTC-GetChannel.Request:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.CHANNEL_REQUEST);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZTC-GetChannel.Request");
			}
			throw new GatewayException("Timeout expired in ZTC-GetChannel.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZTC-GetChannel.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else
				return ((Short) lock.get_objectOfResponse()).shortValue();
		}
	}

	@Override
	public BigInteger readExtAddressGal(long timeout) throws GatewayException, Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCReadExtAddrRequest);// StartSequence
		// +
		// Control
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZTC-ReadExtAddr.Request:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.READ_EXT_ADDRESS);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZTC-ReadExtAddr.Request");
			}
			throw new GatewayException("Timeout expired in ZTC-ReadExtAddr.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZTC-ReadExtAddr.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else
				return ((BigInteger) lock.get_objectOfResponse());
		}
	}

	public BigInteger readExtAddress(long timeout, short shortAddress) throws GatewayException, Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(shortAddress), 2);/*
																 * Short Network
																 * Address
																 */
		_res.addBytesShort(Short.reverseBytes(shortAddress), 2);/*
																 * Short Network
																 * Address
																 */
		_res.addByte((byte) 0x01);/* Request Type */
		_res.addByte((byte) 0x00);/* StartIndex */

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPIeeeAddrRequest);// StartSequence
		// +
		// Control
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZDP-IEEE_addr.Request.Request:" + _res.ToHexString());
			System.out.println("ZDP-IEEE_addr.Request.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.READ_IEEE_ADDRESS);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-IEEE_addr.Request");
			}
			throw new GatewayException("Timeout expired in ZDP-IEEE_addr.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-IEEE_addr.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else
				return ((BigInteger) lock.get_objectOfResponse());
		}
	}

	@Override
	public NodeDescriptor getNodeDescriptorSync(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException {

		ByteArrayObject _res = new ByteArrayObject();

		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/*
																									 * Short
																									 * Network
																									 * Address
																									 */
		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/*
																									 * Short
																									 * Network
																									 * Address
																									 */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPNodeDescriptorRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZDP-NodeDescriptor.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.NODE_DESCRIPTOR);
		String __Key = String.format("%04X", addrOfInterest.getNetworkAddress());
		lock.set_Key(__Key);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}

		if (status.getCode() == ParserLocker.INVALID_ID) {

			throw new GatewayException("Timeout expired in ZDP-NodeDescriptor.Request:" + addrOfInterest.getNetworkAddress());

		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-NodeDescriptor.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());

			} else
				return (NodeDescriptor) lock.get_objectOfResponse();
		}
	}

	@Override
	public List<Short> startServiceDiscoverySync(long timeout, Address aoi) throws Exception {
		if (gal.getPropertiesManager().getDebugEnabled())
			logger.info("startServiceDiscoverySync Timeout:" + timeout);
		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(aoi.getNetworkAddress().shortValue()), 2);/*
																						 * Short
																						 * Network
																						 * Address
																						 */
		_res.addBytesShort(Short.reverseBytes(aoi.getNetworkAddress().shortValue()), 2);/*
																						 * Short
																						 * Network
																						 * Address
																						 */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPActiveEpRequest);/*
																					 * StartSequence
																					 * +
																					 * Control
																					 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("ZDP-Active_EP_req.Request:" + _res.ToHexString());
		}

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.ACTIVE_EP);
		lock.set_Key(String.format("%04X", aoi.getNetworkAddress()));
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-Active_EP_req.Request");
			}

			throw new GatewayException("Timeout expired in ZDP-Active_EP_req.Request");
		} else {
			if (status.getCode() != 0) {
				throw new GatewayException("Error on ZDP-Active_EP_req.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());

			} else {
				return (List<Short>) lock.get_objectOfResponse();
			}
		}

	}

	@Override
	public Status leaveSync(long timeout, Address addrOfInterest, int mask) throws Exception {
		ByteArrayObject _res = new ByteArrayObject();

		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/*
																									 * Short
																									 * Network
																									 * Address
																									 */
		byte[] deviceAddress = DataManipulation.toByteVect(gal.get_GalNode().get_node().getAddress().getNetworkAddress(), 8);
		byte[] _reversed = DataManipulation.reverseBytes(deviceAddress);
		for (byte b : _reversed)
			_res.addByte(b);
		byte options = 0;
		options = (byte) (options & GatewayConstants.LEAVE_REJOIN);
		options = (byte) (options & GatewayConstants.LEAVE_REMOVE_CHILDERN);
		_res.addByte(options);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPMgmtLeaveRequest);/*
																						 * StartSequence
																						 * +
																						 * Control
																						 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Leave command:" + _res.ToHexString());
		}

		addToSendDataQueue(_res);
		if (addrOfInterest.getIeeeAddress() == null) {
			BigInteger _add = gal.getIeeeAddress_FromNetworkCache(addrOfInterest.getNetworkAddress());
			if (_add != null)
				addrOfInterest.setIeeeAddress(_add);
		}
		if (addrOfInterest.getIeeeAddress() != null) {
			Status _st = ClearDeviceKeyPairSet(timeout, addrOfInterest);
			if (_st.getCode() == GatewayConstants.SUCCESS)
				ClearNeighborTableEntry(timeout, addrOfInterest);
		}

		Status status = new Status();
		status.setCode((short) GatewayConstants.SUCCESS);

		return status;
	}

	@Override
	public Status clearEndpointSync(short endpoint) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) endpoint);/* EndPoint */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSDeRegisterEndPointRequest);/*
																								 * StartSequence
																								 * +
																								 * Control
																								 */

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.DEREGISTER_END_POINT);
		Status status = null;

		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(INTERNAL_TIMEOUT);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in Deregister End Point");
			}
			throw new GatewayException("Timeout expired in Deregister End Point");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on  APS-DeregisterEndPoint.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else
				return status;
		}
	}

	@Override
	public NodeServices getLocalServices() throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.APSGetEndPointIdListRequest);/*
																								 * StartSequence
																								 * +
																								 * Control
																								 */

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.GET_END_POINT_LIST);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("APS-GetEndPointIdList.Request command:" + _res.ToHexString());
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(INTERNAL_TIMEOUT);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in GetEndPointIdList");
			}
			throw new GatewayException("Timeout expired in GetEndPointIdList");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on APS-GetEndPointIdList.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				NodeServices _tores = (NodeServices) lock.get_objectOfResponse();

				return _tores;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer#
	 * getServiceDescriptor(long, it.telecomitalia.zgd.jaxb.Address, short)
	 */
	@Override
	public ServiceDescriptor getServiceDescriptor(long timeout, Address addrOfInterest, short endpoint) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();

		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/* ShortNetworkAddress */
		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/* ShortNetworkAddress */
		_res.addByte((byte) endpoint);/* EndPoint */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPSimpleDescriptorRequest);/*
																							 * StartSequence
																							 * +
																							 * Control
																							 */
		String Key = String.format("%04X", addrOfInterest.getNetworkAddress()) + String.format("%02X", endpoint);

		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.GET_SIMPLE_DESCRIPTOR);
		lock.set_Key(Key);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-SimpleDescriptor.Request");
			}
			throw new GatewayException("Timeout expired in ZDP-SimpleDescriptor.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-SimpleDescriptor.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				ServiceDescriptor _tores = (ServiceDescriptor) lock.get_objectOfResponse();
				return _tores;
			}
		}
	}

	@Override
	public void cpuReset() throws Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZTCCPUResetRequest);/*
																					 * StartSequence
																					 * +
																					 * Control
																					 */
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("CPUResetCommnad command:" + _res.ToHexString());
		}
		addToSendDataQueue(_res);

	}

	@Override
	public BindingList getNodeBindings(long timeout, Address addrOfInterest, short index) throws IOException, Exception, GatewayException {

		ByteArrayObject _res = new ByteArrayObject();
		if (addrOfInterest.getNetworkAddress() == null)
			addrOfInterest.setNetworkAddress(gal.getShortAddress_FromNetworkCache(addrOfInterest.getIeeeAddress()));

		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);/* ShortNetworkAddress */
		_res.addByte((byte) index);/* startIndex */
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPMgmtBindRequest);/*
																					 * StartSequence
																					 * +
																					 * Control
																					 */
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.GET_BINDINGS);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-Mgmt_Bind.Request");
			}
			throw new GatewayException("Timeout expired in ZDP-Mgmt_Bind.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-Mgmt_Bind.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				BindingList _tores = (BindingList) lock.get_objectOfResponse();
				return _tores;
			}
		}
	}

	@Override
	public Status addBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		byte[] _reversed;

		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(gal.getShortAddress_FromNetworkCache(binding.getSourceIEEEAddress()).shortValue()), 2);

		byte[] ieeeAddress = DataManipulation.toByteVect(binding.getSourceIEEEAddress(), 8);
		_reversed = DataManipulation.reverseBytes(ieeeAddress);
		for (byte b : _reversed)
			/* Source IEEEAddress */
			_res.addByte(b);

		_res.addByte((byte) binding.getSourceEndpoint());/* Source EndPoint */

		Integer _clusterID = binding.getClusterID();
		_res.addBytesShort(Short.reverseBytes(_clusterID.shortValue()), 2);/* ClusterID */

		if (binding.getDeviceDestination().size() > 0 && binding.getGroupDestination().size() > 0)
			throw new GatewayException("The Address mode can only be one between Group or Device!");
		else if (binding.getDeviceDestination().size() == 1) {
			_res.addByte((byte) 0x03);/*
									 * Destination AddressMode IeeeAddress +
									 * EndPoint
									 */

			byte[] _DestinationieeeAddress = DataManipulation.toByteVect(binding.getDeviceDestination().get(0).getAddress(), 8);
			_reversed = DataManipulation.reverseBytes(_DestinationieeeAddress);
			for (byte b : _reversed)
				/* Destination IEEEAddress */
				_res.addByte(b);
			_res.addByte((byte) binding.getDeviceDestination().get(0).getEndpoint());/*
																					 * Destination
																					 * EndPoint
																					 */

		} else if (binding.getGroupDestination().size() == 1) {
			_res.addByte((byte) 0x01);/* Destination AddressMode Group */

			byte[] _DestinationGroupAddress = DataManipulation.toByteVect(binding.getGroupDestination().get(0).longValue(), 8);
			_reversed = DataManipulation.reverseBytes(_DestinationGroupAddress);
			for (byte b : _reversed)
				/* Destination Group */
				_res.addByte(b);

		} else {
			throw new GatewayException("The Address mode can only be one Group or one Device!");

		}

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPBindRequest);/*
																				 * StartSequence
																				 * +
																				 * Control
																				 */
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.ADD_BINDING);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-BIND.Response");
			}
			throw new GatewayException("Timeout expired in ZDP-BIND.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-BIND.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				return lock.getStatus();

			}
		}
	}

	@Override
	public Status removeBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		byte[] _reversed;

		ByteArrayObject _res = new ByteArrayObject();

		_res.addBytesShort(Short.reverseBytes(gal.getShortAddress_FromNetworkCache(binding.getSourceIEEEAddress()).shortValue()), 2);

		byte[] ieeeAddress = DataManipulation.toByteVect(binding.getSourceIEEEAddress(), 8);
		_reversed = DataManipulation.reverseBytes(ieeeAddress);
		for (byte b : _reversed)
			/* Source IEEEAddress */
			_res.addByte(b);

		_res.addByte((byte) binding.getSourceEndpoint());/* Source EndPoint */

		Integer _clusterID = binding.getClusterID();
		_res.addBytesShort(Short.reverseBytes(_clusterID.shortValue()), 2);/* ClusterID */

		if (binding.getDeviceDestination().size() > 0 && binding.getGroupDestination().size() > 0)
			throw new GatewayException("The Address mode can only be one between Group or Device!");
		else if (binding.getDeviceDestination().size() == 1) {
			_res.addByte((byte) 0x03);/*
									 * Destination AddressMode IeeeAddress +
									 * EndPoint
									 */

			byte[] _DestinationieeeAddress = DataManipulation.toByteVect(binding.getDeviceDestination().get(0).getAddress(), 8);
			_reversed = DataManipulation.reverseBytes(_DestinationieeeAddress);
			for (byte b : _reversed)
				/* Destination IEEEAddress */
				_res.addByte(b);
			_res.addByte((byte) binding.getDeviceDestination().get(0).getEndpoint());/*
																					 * Destination
																					 * EndPoint
																					 */

		} else if (binding.getGroupDestination().size() == 1) {
			_res.addByte((byte) 0x01);/* Destination AddressMode Group */

			byte[] _DestinationGroupAddress = DataManipulation.toByteVect(binding.getGroupDestination().get(0).longValue(), 8);
			_reversed = DataManipulation.reverseBytes(_DestinationGroupAddress);
			for (byte b : _reversed)
				/* Destination Group */
				_res.addByte(b);

		} else {
			throw new GatewayException("The Address mode can only be one Group or one Device!");

		}

		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPUnbindRequest);/*
																					 * StartSequence
																					 * +
																					 * Control
																					 */
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.REMOVE_BINDING);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-UNBIND.Response");
			}
			throw new GatewayException("Timeout expired in ZDP-UNBIND.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-UNBIND.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				return lock.getStatus();

			}
		}
	}

	@Override
	public Status frequencyAgilitySync(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException {
		ByteArrayObject _bodyCommand = new ByteArrayObject();
		_bodyCommand.addByte((byte) 0xFD);
		_bodyCommand.addByte((byte) 0xFF);
		byte[] _channel = Utils.buildChannelMask(scanChannel);
		for (byte x : DataManipulation.reverseBytes(_channel))
			_bodyCommand.addByte(x);
		_bodyCommand.addByte((byte) scanDuration);
		_bodyCommand.addByte((byte) 0x00);// Add parameter nwkupdate
		// _bodyCommand.addByte((byte) 0x01);// Add parameter nwkupdate
		_bodyCommand = Set_SequenceStart_And_FSC(_bodyCommand, FreescaleConstants.NLMENWKUpdateReq);/*
																									 * StartSequence
																									 * +
																									 * Control
																									 */

		addToSendDataQueue(_bodyCommand);
		Status _st = new Status();
		_st.setCode((short) GatewayConstants.SUCCESS);

		return _st;
	}

	@Override
	public IConnector getIKeyInstance() {
		return _key;
	}

	@Override
	public PropertiesManager getPropertiesManager() {
		return gal.getPropertiesManager();
	}

	@Override
	public void notifyFrame(final ByteArrayObject frame) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				try {

					byte[] msg = frame.getByteArray();
					int size = frame.getByteCount(true);

					short[] messageShort = new short[size];
					for (int i = 0; i < size; i++)
						messageShort[i] = (short) (msg[i] & 0xff);

					if (gal.getPropertiesManager().getDebugEnabled())
						DataManipulation.logArrayHexRadixDataReceived("<<< Received data: ", messageShort);
					addToReceivedDataQueue(size, messageShort);
					try {
						processMessages();
					} catch (Exception e) {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.error("Error on notifyFrame:" + e.getMessage());
					}
				} catch (Exception e) {
					if (gal.getPropertiesManager().getDebugEnabled())
						logger.error("Error on notifyFrame: " + e.getMessage());
				}
			}
		};
		thr.setName("Thread notifyFrame");
		thr.start();
	}

	@Override
	public Status ClearDeviceKeyPairSet(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		byte[] ieeeAddress = DataManipulation.toByteVect(addrOfInterest.getIeeeAddress(), 8);
		byte[] _reversed = DataManipulation.reverseBytes(ieeeAddress);
		for (byte b : _reversed)
			_res.addByte(b);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMEClearDeviceKeyPairSet);
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.CLEAR_DEVICE_KEY_PAIR_SET);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("APS-ClearDeviceKeyPairSet.Request command:" + _res.ToHexString());
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(INTERNAL_TIMEOUT);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ClearDeviceKeyPairSet");
			}
			throw new GatewayException("Timeout expired in ClearDeviceKeyPairSet");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on APS-ClearDeviceKeyPairSet.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				return status;
			}
		}

	}

	@Override
	public Status ClearNeighborTableEntry(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) 0xFF);
		_res.addByte((byte) 0xFF);

		byte[] ieeeAddress = DataManipulation.toByteVect(addrOfInterest.getIeeeAddress(), 8);
		byte[] _reversed = DataManipulation.reverseBytes(ieeeAddress);
		for (byte b : _reversed)
			_res.addByte(b);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMEClearNeighborTableEntry);
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.CLEAR_NEIGHBOR_TABLE_ENTRY);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("ZTC-ClearNeighborTableEntry.Request command:" + _res.ToHexString());
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(INTERNAL_TIMEOUT);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}

		}
		if (status.getCode() == ParserLocker.INVALID_ID) {

			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZTC-ClearNeighborTableEntry.Request");
			}
			throw new GatewayException("Timeout expired in ZTC-ClearNeighborTableEntry.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZTC-ClearNeighborTableEntry.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			} else {
				return status;
			}
		}
	}

	public GalController getGalController() {
		return gal;
	}

	@Override
	public Status NMLE_SETSync(long timeout, short _AttID, String _value) throws Exception {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addByte((byte) _AttID);/* _AttId */
		_res.addByte((byte) 0x00);
		_res.addByte((byte) 0x00);
		_res.addByte((byte) 0x00);
		for (byte x : DataManipulation.hexStringToByteArray(_value))
			_res.addByte(x);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.NLMESetRequest);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("NMLE_SET command:" + _res.ToHexString());
		}
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.NMLE_SET);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}

			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}

			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {

			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in NMLE SET");
			}
			throw new GatewayException("Timeout expired in NMLE SET");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on NMLE_SET.request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			}
			return status;
		}
	}

	@Override
	public Mgmt_LQI_rsp Mgmt_Lqi_Request(long timeout, Address addrOfInterest, short startIndex) throws IOException, Exception, GatewayException {
		ByteArrayObject _res = new ByteArrayObject();
		_res.addBytesShort(Short.reverseBytes(addrOfInterest.getNetworkAddress().shortValue()), 2);
		_res.addByte((byte) startIndex);
		_res = Set_SequenceStart_And_FSC(_res, FreescaleConstants.ZDPMgmtLqiRequest);
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Mgmt_Lqi_Request command:" + _res.ToHexString());
		}
		String __Key = String.format("%04X", addrOfInterest.getNetworkAddress());
		ParserLocker lock = new ParserLocker();
		lock.setType(TypeMessage.LQI_REQ);
		lock.set_Key(__Key);
		Status status = null;
		try {
			synchronized (listLocker) {
				listLocker.add(lock);
			}
			addToSendDataQueue(_res);
			synchronized (lock) {
				try {
					lock.wait(timeout);
				} catch (InterruptedException e) {

				}
			}
			status = lock.getStatus();
			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		} catch (Exception e) {

			synchronized (listLocker) {
				if (listLocker.contains(lock))
					listLocker.remove(lock);
			}
		}
		if (status.getCode() == ParserLocker.INVALID_ID) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.error("Timeout expired in ZDP-Mgmt_Lqi.Request");
			}
			throw new GatewayException("Timeout expired in ZDP-Mgmt_Lqi.Request");
		} else {
			if (status.getCode() != 0) {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					logger.info("Returned Status: " + status.getCode());
				}
				throw new GatewayException("Error on ZDP-Mgmt_Lqi.Request. Status code:" + status.getCode() + " Status Message: " + status.getMessage());
			}
			return (Mgmt_LQI_rsp) lock.get_objectOfResponse();
		}
	}
}

class ChecksumControl {
	int lastCalculated = 0x00;

	public int getCumulativeXor(byte b) {
		return lastCalculated ^= (0xFF & b);
	}

	public int getCumulativeXor(int i) {
		return lastCalculated ^= i;
	}

	public void resetLastCalculated() {
		lastCalculated = 0x00;
	}

	public int getLastCalulated() {
		return lastCalculated;
	}
}
