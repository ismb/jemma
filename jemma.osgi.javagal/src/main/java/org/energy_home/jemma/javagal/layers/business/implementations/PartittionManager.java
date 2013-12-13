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
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.TxOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;
import org.energy_home.jemma.javagal.layers.object.ParserLocker;
import org.energy_home.jemma.javagal.layers.object._ZCLMessage;


/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class PartittionManager {
	private final static Log logger = LogFactory
			.getLog(PartittionManager.class);


	private GalController gal = null;
	private Map<String, _partitionInstance> _mapInstance;

	public PartittionManager(GalController _gal) {
		gal = _gal;
		_mapInstance = new HashMap<String, PartittionManager._partitionInstance>();
	}

	public void ListenerPartitionMessagesManager(APSMessageEvent m) {
		_ZCLMessage _message = new _ZCLMessage(m.getData());
		String __KEY = m.getDestinationAddress().getNetworkAddress().toString()
				+ m.getDestinationEndpoint() + m.getSourceEndpoint()
				+ _message.TransequenceNumber;
		if (_mapInstance.containsKey(__KEY)) {
			_partitionInstance _current = _mapInstance.get(__KEY);
			_current.PartitionMessages.add(m);
			Object _locker = _current.getLocker();
			synchronized (_locker) {
				_locker.notify();
			}
		} else {
			_partitionInstance _current = new _partitionInstance(__KEY);
			_mapInstance.put(__KEY, _current);
			_current.PartitionMessages.add(m);
			Object _locker = _current.getLocker();
			synchronized (_locker) {
				_locker.notify();
			}

		}
	}

	public void SendApsWithPartitioning(final APSMessage m) {

		Thread thr = new Thread() {
			@Override
			public void run() {
				_ZCLMessage _message = new _ZCLMessage(m.getData());
				String __KEY = m.getDestinationAddress().getNetworkAddress()
						.toString()

						+ m.getDestinationEndpoint()
						+ m.getSourceEndpoint()
						+ _message.TransequenceNumber;
				_partitionInstance _instance = new _partitionInstance(__KEY);
				_mapInstance.put(__KEY, _instance);
				_instance.StartSendMessage(m, (short) 0x04, (short) 0x03);
			}
		};
		thr.setName("Thread Partition");
		thr.start();

	}

	class _partitionInstance {
		private Object _locker = new Object();
		private List<APSMessageEvent> PartitionMessages;
		private List<partitionedFrame_Class> PartitionedFrames;
		private String _identifier;
		private ParserLocker lock_DefaultResponse_Of_WriteHandshake_Received = new ParserLocker();
		private ParserLocker lock_MultipleACK = new ParserLocker();
		private int ClusterPartionedID;
		Boolean _MultipleAckVectorEmpty = null;
		HashSet<Integer> _MultipleAckVector;
		private final short apsAckWaitDuration = 3600;

		/* Attributes */
		private short MaximumIncomingTransferSize;

		private short MaximumOutgoingTransferSize;

		private int PartitionedFrameSize;

		private short LargeFrameSize;

		private int NumberOfACKFrame;

		private short InterframeDelay;

		private short NACKTimeout;

		private short getNACKTimeout() {
			return (short) (apsAckWaitDuration + InterframeDelay
					* NumberOfACKFrame);
		}

		private void setNACKTimeout(short value) {
			NACKTimeout = value;
		}

		private int NumberOfSendRetries;

		private short SenderTimeout;

		private short getSenderTimeout() {

			return (short) (2 * apsAckWaitDuration + InterframeDelay
					* NumberOfACKFrame);
		}

		private void setSenderTimeout(short value) {
			SenderTimeout = value;
		}

		private short ReceiverTimeout;

		private short getReceiverTimeout() {

			return (short) (apsAckWaitDuration + InterframeDelay + NumberOfSendRetries
					* NACKTimeout);
		}

		private void setReceiverTimeout(short value) {
			SenderTimeout = value;
		}

		/* End of attributes */

		public _partitionInstance(String key) {
			_identifier = key;
			/* Initializing the attributes value */
			MaximumIncomingTransferSize = 0x0500;
			MaximumOutgoingTransferSize = 0x0500;
			PartitionedFrameSize = 0;
			LargeFrameSize = 0x0500;
			NumberOfACKFrame = 0;
			InterframeDelay = 0xFF; /* 255 ms */
			NumberOfSendRetries = 0x03;

			PartitionMessages = Collections
					.synchronizedList(new LinkedList<APSMessageEvent>());
			PartitionedFrames = Collections
					.synchronizedList(new LinkedList<partitionedFrame_Class>());
			_MultipleAckVector = new HashSet<Integer>();
			_reader.start();
		}

		public Object getLocker() {
			return _locker;
		}

		Thread _reader = new Thread() {
			@Override
			public void run() {
				while (true) {
					synchronized (_locker) {
						try {
							_locker.wait();
						} catch (InterruptedException e) {

						}
						APSMessageEvent _current;
						synchronized (PartitionMessages) {
							_current = PartitionMessages.get(0);
							PartitionMessages.remove(0);
						}
						try {
							_computeMessage(_current);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		};

		private void _computeMessage(APSMessageEvent _currentMessage)
				throws Exception {

			_ZCLMessage Message = new _ZCLMessage(_currentMessage.getData());
			if (!Message.Server_to_Client) {
				switch (Message.CommandID) {
				case 0x00:/* TransferPartitionedFrame */
					if (Message._commandSpecifcForCluster) {

						synchronized (PartitionedFrames) {
							PartitionedFrames.add(new partitionedFrame_Class(
									_currentMessage.getData()));
							// TODO Signal su locker
						}
					}
					break;
				case 0x01:/* ReadHandShakeParam */
				{
					if (Message._commandSpecifcForCluster) {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Received Partitioned message, command ReadHandShakeParam: "
									+ DataManipulation
											.convertBytesToString(_currentMessage
													.getData()));

						if (Message.Payload.length >= 2) {
							ClusterPartionedID = DataManipulation
									.toIntFromShort(Message.Payload[1],
											Message.Payload[0]);

						} else
							throw new Exception(
									"Payload too short on ReadHandShakeParam ClusterID:"
											+ DataManipulation
													.convertBytesToString(Message.Payload));

						ByteArrayObject _ReadHandShakeParamResponse = new ByteArrayObject();
						_ReadHandShakeParamResponse.addBytesShort(
								Short.reverseBytes((short) ClusterPartionedID),
								2);

						for (int i = 2; i <= Message.Payload.length; i++) {
							int _AttributeID = -1;
							if (i + 2 <= Message.Payload.length) {
								_AttributeID = DataManipulation.toIntFromShort(
										Message.Payload[i + 1],
										Message.Payload[i]);
								i = i + 2;
							} else
								throw new Exception(
										"Payload too short on ReadHandShakeParam AttributeID:"
												+ DataManipulation
														.convertBytesToString(Message.Payload));
							byte[] _ReadHandShakeParam = ReadHandShakeParam((short) _AttributeID);
							for (byte x : _ReadHandShakeParam)
								_ReadHandShakeParamResponse.addByte(x);
						}
						Send_Message(
								_ReadHandShakeParamResponse.getRealByteArray(),
								_currentMessage);
					}
				}
					break;
				case 0x02:/* WriteHandShakeParam */
				{
					if (Message._commandSpecifcForCluster) {

						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Received Partitioned message, command WriteHandShakeParam: "
									+ DataManipulation
											.convertBytesToString(_currentMessage
													.getData()));

						if (Message.Payload.length >= 2) {
							ClusterPartionedID = DataManipulation
									.toIntFromShort(Message.Payload[1],
											Message.Payload[0]);

						} else
							throw new Exception(
									"Payload too short on WriteHandShakeParam ClusterID:"
											+ DataManipulation
													.convertBytesToString(Message.Payload));

						ByteArrayObject _ReadHandShakeParamResponse = new ByteArrayObject();
						_ReadHandShakeParamResponse.addBytesShort(
								Short.reverseBytes((short) ClusterPartionedID),
								2);

						for (int i = 2; i <= Message.Payload.length; i++) {
							int _AttributeID = -1;
							int _AttributeDataType = -1;
							ByteArrayObject _Data = new ByteArrayObject();
							if (i + 1 <= Message.Payload.length) {
								_AttributeID = DataManipulation.toIntFromShort(
										Message.Payload[i + 1],
										Message.Payload[i]);
								i = i + 2;
							} else
								throw new Exception(
										"Payload too short on WriteHandShakeParam AttributeID:"
												+ DataManipulation
														.convertBytesToString(Message.Payload));

							if (i <= Message.Payload.length) {
								_AttributeDataType = Message.Payload[i];
								i = i + 1;
							} else
								throw new Exception(
										"Payload too short on WriteHandShakeParam AttributeID:"
												+ DataManipulation
														.convertBytesToString(Message.Payload));

							if (_AttributeDataType == 21)/* Unsigned 16bit */
							{
								if (i + 2 <= Message.Payload.length) {
									_Data.addByte(Message.Payload[i + 1]);
									_Data.addByte(Message.Payload[i]);
									i = i + 2;
								} else
									throw new Exception(
											"Payload too short on WriteHandShakeParam Data Unsigned 16bit:"
													+ DataManipulation
															.convertBytesToString(Message.Payload));
							} else if (_AttributeDataType == 20)/* Unsigned 8bit */
							{
								if (i <= Message.Payload.length) {
									_Data.addByte(Message.Payload[i]);
									i = i + 1;

								} else
									throw new Exception(
											"Payload too short on WriteHandShakeParam Data Unsigned 8bit:"
													+ DataManipulation
															.convertBytesToString(Message.Payload));

							}

							SetHandShakeParam((short) _AttributeID,
									_Data.getRealByteArray());
						}
					}
				}
					break;
				case 0x0B:/* Default_Response */
				{
					if (Message._commandAccrossEntireProfile) {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Received Default Response command: "
									+ DataManipulation
											.convertBytesToString(_currentMessage
													.getData()));

						int _CommandID = -1;
						int _status = -1;

						if (Message.Payload.length >= 2) {
							_CommandID = Message.Payload[0];
							_status = Message.Payload[1];
						} else
							throw new Exception(
									"Payload too short on Default_Response:"
											+ Message.Payload);

						if (_CommandID == 2 && _status == 0)
							synchronized (lock_DefaultResponse_Of_WriteHandshake_Received) {
								lock_DefaultResponse_Of_WriteHandshake_Received
										.notify();
							}

					}
				}
					break;
				}
			} else {
				switch (Message.CommandID) {
				case 0x0B:/* Default_Response */
				{
					if (Message._commandAccrossEntireProfile) {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Received Default Response command: "
									+ DataManipulation
											.convertBytesToString(_currentMessage
													.getData()));

						int _CommandID = -1;
						int _status = -1;

						if (Message.Payload.length >= 2) {
							_CommandID = Message.Payload[0];
							_status = Message.Payload[1];
						} else
							throw new Exception(
									"Payload too short on Default_Response:"
											+ Message.Payload);

						if (_CommandID == 2 && _status == 0)
							synchronized (lock_DefaultResponse_Of_WriteHandshake_Received) {
								lock_DefaultResponse_Of_WriteHandshake_Received
										.notify();
							}

					}
				}
					break;
				case 0x00:/* MultipleACK */
				{
					if (Message._commandSpecifcForCluster) {
						int _index = 0;
						int ACKOption = -1;
						if (Message.Payload.length >= 1) {
							ACKOption = Message.Payload[0];
							_index = 1;
						} else
							throw new Exception(
									"Payload too short on MultipleACK ACKOption:"
											+ DataManipulation
													.convertBytesToString(Message.Payload));

						int FirstFrameID = -1;

						if (ACKOption == 1) {
							if (Message.Payload.length >= 3) {
								FirstFrameID = DataManipulation.toIntFromShort(
										Message.Payload[2], Message.Payload[1]);
								_index = _index + 2;

							} else
								throw new Exception(
										"Payload too short on MultipleACK FirstFrameID 2bytes:"
												+ DataManipulation
														.convertBytesToString(Message.Payload));

						} else {
							if (Message.Payload.length >= 2) {
								FirstFrameID = Message.Payload[1];
								_index = _index + 1;
							} else
								throw new Exception(
										"Payload too short on MultipleACK FirstFrameID 1byte:"
												+ DataManipulation
														.convertBytesToString(Message.Payload));
						}

						int ID = -1;
						if (Message.Payload.length > _index) {
							while (_index > Message.Payload.length) {

								if (ID == -1) {
									if (ACKOption == 0) {
										if (_index + 1 > Message.Payload.length) {
											ID = Message.Payload[_index];
											_index = _index + 1;
										} else
											throw new Exception(
													"Payload too short on MultipleACK ID 1byte:"
															+ DataManipulation
																	.convertBytesToString(Message.Payload));
									} else {

										if (_index + 2 > Message.Payload.length) {
											ID = DataManipulation
													.toIntFromShort(
															Message.Payload[_index + 1],
															Message.Payload[_index]);
											_index = _index + 2;
										} else
											throw new Exception(
													"Payload too short on MultipleACK ID 2byte:"
															+ DataManipulation
																	.convertBytesToString(Message.Payload));
									}
								} else {
									if (ID + 1 < 256) {

										if (_index + 1 > Message.Payload.length) {
											ID = Message.Payload[_index];
											_index = _index + 1;
										} else
											throw new Exception(
													"Payload too short on MultipleACK ID 1byte:"
															+ DataManipulation
																	.convertBytesToString(Message.Payload));

									} else {

										if (_index + 2 > Message.Payload.length) {
											ID = DataManipulation
													.toIntFromShort(
															Message.Payload[_index + 1],
															Message.Payload[_index]);
											_index = _index + 2;
										} else
											throw new Exception(
													"Payload too short on MultipleACK ID 2byte:"
															+ DataManipulation
																	.convertBytesToString(Message.Payload));
									}

								}
								synchronized (_MultipleAckVector) {
									if (!_MultipleAckVector.contains(ID))
										_MultipleAckVector.add(ID);
								}

							}
							synchronized (_MultipleAckVectorEmpty) {
								_MultipleAckVectorEmpty = false;
							}
						} else {
							synchronized (_MultipleAckVectorEmpty) {
								_MultipleAckVectorEmpty = true;
							}
						}

						if (gal.getPropertiesManager().getDebugEnabled()) {
							if (ID == -1)
								logger.info("Received Partitioned message, command MultipleACK: "
										+ DataManipulation
												.convertBytesToString(_currentMessage
														.getData()));
							else
								logger.info("Received Partitioned message, command MultipleACK WITH NACKIDs: "
										+ DataManipulation
												.convertBytesToString(_currentMessage
														.getData()));
							synchronized (lock_MultipleACK) {

								lock_MultipleACK.notify();
							}
						}

					}
				}
					break;
				case 0x01:/* ReadHandShakeParamResponse */
				{
					int _index = 0;
					if (Message._commandSpecifcForCluster) {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Received Partitioned message, command ReadHandShakeParamResponse: "
									+ DataManipulation
											.convertBytesToString(_currentMessage
													.getData()));

						if (Message.Payload.length >= 2) {
							ClusterPartionedID = DataManipulation
									.toIntFromShort(
											Message.Payload[_index + 1],
											Message.Payload[_index]);
							_index = _index + 2;
						} else
							throw new Exception(
									"Payload too short on ReadHandShakeParamResponse ClusterPartionedID:"
											+ DataManipulation
													.convertBytesToString(_currentMessage
															.getData()));

						while (_index < Message.Payload.length) {

							int _AttributeID = -1;
							if (_index + 2 > Message.Payload.length) {
								_AttributeID = DataManipulation.toIntFromShort(
										Message.Payload[_index + 1],
										Message.Payload[_index]);
								_index = _index + 2;
							} else
								throw new Exception(
										"Payload too short on ReadHandShakeParamResponse AttributeID:"
												+ DataManipulation
														.convertBytesToString(_currentMessage
																.getData()));
							int _Status = -1;
							if (_index + 1 > Message.Payload.length) {
								_Status = Message.Payload[_index];
								_index = _index + 1;

							} else
								throw new Exception(
										"Payload too short on ReadHandShakeParamResponse Status:"
												+ DataManipulation
														.convertBytesToString(_currentMessage
																.getData()));

							if (_Status == 0) {
								int _AttributeDataType = -1;
								if (_index + 1 > Message.Payload.length) {
									_AttributeDataType = Message.Payload[_index];
									_index = _index + 1;
								} else
									throw new Exception(
											"Payload too short on ReadHandShakeParamResponse AttributeDataType:"
													+ DataManipulation
															.convertBytesToString(_currentMessage
																	.getData()));

								ByteArrayObject _data = new ByteArrayObject();
								if (_AttributeDataType == 21)/* Unsigned 16bit */
								{

									if (_index + 2 > Message.Payload.length) {
										_data.addByte(Message.Payload[_index + 1]);
										_data.addByte(Message.Payload[_index]);
										_index = _index + 2;
									} else
										throw new Exception(
												"Payload too short on ReadHandShakeParamResponse Data Unsigned 16bit:"
														+ DataManipulation
																.convertBytesToString(_currentMessage
																		.getData()));

								} else if (_AttributeDataType == 20)/*
																	 * Unsigned
																	 * 8bit
																	 */
								{
									if (_index + 1 > Message.Payload.length) {
										_data.addByte(Message.Payload[_index]);
										_index = _index + 1;
									} else
										throw new Exception(
												"Payload too short on ReadHandShakeParamResponse Data Unsigned 8bit:"
														+ DataManipulation
																.convertBytesToString(_currentMessage
																		.getData()));

								}
								SetHandShakeParam((short) _AttributeID,
										_data.getRealByteArray());
							}
						}
					}
				}
					break;
				case 0x02:/* Error */
					if (gal.getPropertiesManager().getDebugEnabled())
						logger.info("ERROR : DirectionBIT!");
					break;
				}
			}

			if (!Message.Disable_DefaultResponse) {
				/* Send the default Message */
				ByteArrayObject payload_DefaultResponse = new ByteArrayObject();
				if (!Message.Server_to_Client)
					payload_DefaultResponse.addByte((byte) 0x10);// 0x10 stands
																	// for all
																	// zero
																	// expect
																	// for
																	// Disable
																	// Default
																	// Response
				else
					payload_DefaultResponse.addByte((byte) 0x18); // 0x18 means
																	// Direction
																	// and
																	// Disable
																	// Default
																	// Response
																	// set to 1
				payload_DefaultResponse.addByte(Message.TransequenceNumber);
				payload_DefaultResponse.addByte((byte) 0x0B); // 0x0b is the
																// command of
																// "Default response"
																// (from ZCL)
				payload_DefaultResponse.addByte(Message.CommandID);
				payload_DefaultResponse.addByte((byte) 0x00);// SUCCESS
				if (!Send_Message(payload_DefaultResponse.getRealByteArray(),
						_currentMessage))
					throw new Exception("Error sending the default response!");
			}

		}

		public byte[] ReadHandShakeParam(short _AttributeID) {

			ByteArrayObject _payload = new ByteArrayObject();
			switch (_AttributeID) {
			case 0x00:
				_payload.addBytesShort(
						Short.reverseBytes(MaximumIncomingTransferSize), 2);
				break;
			case 0x01:
				_payload.addBytesShort(
						Short.reverseBytes(MaximumOutgoingTransferSize), 2);
				break;
			case 0x02:
				_payload.addByte((byte) PartitionedFrameSize);
				break;
			case 0x03:
				_payload.addBytesShort(Short.reverseBytes(LargeFrameSize), 2);
				break;
			case 0x04:
				_payload.addByte((byte) NumberOfACKFrame);
				break;
			case 0x05:
				_payload.addBytesShort(Short.reverseBytes(NACKTimeout), 2);
				break;
			case 0x06:
				_payload.addByte((byte) InterframeDelay);
				break;
			case 0x07:
				_payload.addByte((byte) NumberOfSendRetries);
				break;
			case 0x08:
				_payload.addBytesShort(Short.reverseBytes(SenderTimeout), 2);
				break;
			case 0x09:
				_payload.addBytesShort(Short.reverseBytes(ReceiverTimeout), 2);
				break;
			}
			return _payload.getRealByteArray();
		}

		public void SetHandShakeParam(short _AttributeID, byte[] _data) {

			switch (_AttributeID) {
			case 0x00:
				MaximumIncomingTransferSize = (short) DataManipulation
						.toIntFromShort(_data[1], _data[0]);
				break;
			case 0x01:
				MaximumOutgoingTransferSize = (short) DataManipulation
						.toIntFromShort(_data[1], _data[0]);
				break;
			case 0x02:
				PartitionedFrameSize = _data[0];
				break;
			case 0x03:
				LargeFrameSize = (short) DataManipulation.toIntFromShort(
						_data[1], _data[0]);
				break;
			case 0x04:
				NumberOfACKFrame = _data[0];
				break;
			case 0x05:
				NACKTimeout = (short) DataManipulation.toIntFromShort(_data[1],
						_data[0]);
				break;
			case 0x06:
				InterframeDelay = _data[0];
				break;
			case 0x07:
				NumberOfSendRetries = _data[0];
				break;
			case 0x08:
				SenderTimeout = (short) DataManipulation.toIntFromShort(
						_data[1], _data[0]);
				break;
			case 0x09:
				ReceiverTimeout = (short) DataManipulation.toIntFromShort(
						_data[1], _data[0]);
				break;
			}

		}

		private List<byte[]> _SplitMessage(_ZCLMessage ZCLMessage,
				short partitionedFrameSize) {
			ByteArrayObject _Payload = new ByteArrayObject();
			_Payload.addByte(ZCLMessage.FrameControl);
			_Payload.addByte(ZCLMessage.TransequenceNumber);
			_Payload.addByte(ZCLMessage.CommandID);
			for (byte x : ZCLMessage.Payload)
				_Payload.addByte(x);
			List<byte[]> _Result = new ArrayList<byte[]>();
			boolean _end = false;
			int index = 0;
			int _total = _Payload.getByteCount(false);
			while (!_end) {
				byte[] _current;
				if ((_total - index) > 0) {
					if ((_total - index) >= partitionedFrameSize) {
						_current = _Payload.getPartialRealByteArray(index,
								(int) partitionedFrameSize);
						index = index + (int) partitionedFrameSize;

					} else {
						_current = _Payload.getPartialRealByteArray(index,
								(_total - index));
						index = index + (_Payload.getByteCount(true) - index);

						_end = true;
					}
					_Result.add(_current);
				} else
					_end = true;
			}
			return _Result;

		}

		public boolean Send_Message(byte[] _Payload,
				APSMessageEvent _MessageReceived) {

			APSMessage m = new APSMessage();
			m.setClusterID(0x0016);/* Partition Cluster */
			m.setProfileID(0x0104);
			m.setRadius((short) 0x0A);
			TxOptions _txt = new TxOptions();
			_txt.setAcknowledged(false);
			_txt.setPermitFragmentation(false);
			_txt.setSecurityEnabled(true);
			_txt.setUseNetworkKey(true);
			m.setTxOptions(_txt);
			m.setSourceEndpoint(_MessageReceived.getDestinationEndpoint());
			m.setDestinationEndpoint(_MessageReceived.getSourceEndpoint());
			m.setDestinationAddress(_MessageReceived.getSourceAddress());
			m.setDestinationAddressMode(_MessageReceived.getSourceAddressMode());
			m.setData(_Payload);
			Status _stat = null;
			try {
				_stat = gal.getDataLayer().sendApsSync(
						IDataLayer.INTERNAL_TIMEOUT, m);
			} catch (Exception e) {
				return false;
			}
			/* Check no confirm received */
			if (_stat == null || _stat.getCode() != 0)
				return false;
			else
				return true;
		}

		private boolean Send_Message(byte _SourceEndPoint,
				byte _DestinationEndPoint, Address _destinationAddress,
				byte[] _payload) {
			APSMessage m = new APSMessage();

			m.setClusterID(0x0016);/* Partition Cluster */
			m.setProfileID(0x0104);
			m.setRadius((short) 0x0A);
			TxOptions _txt = new TxOptions();
			_txt.setAcknowledged(false);
			_txt.setPermitFragmentation(false);
			_txt.setSecurityEnabled(true);
			_txt.setUseNetworkKey(true);
			m.setTxOptions(_txt);
			m.setSourceEndpoint(_SourceEndPoint);
			m.setDestinationEndpoint(_DestinationEndPoint);

			m.setDestinationAddress(_destinationAddress);
			m.setData(_payload);

			if (gal.getPropertiesManager().getDebugEnabled())
				logger.info("Sending Message DATA: "
						+ DataManipulation.convertBytesToString(m.getData()));
			Status _stat = null;
			try {
				_stat = gal.getDataLayer().sendApsSync(
						IDataLayer.INTERNAL_TIMEOUT, m);
			} catch (Exception e) {
				return false;
			}
			/* Check no confirm received */
			if (_stat == null || _stat.getCode() != 0)
				return false;
			else
				return true;

		}

		public boolean StartSendMessage(APSMessage _Originalmessage,
				short _NumberOfACKFrame, short _PartitionedFrameSize) {

			int _counterOfSending = 1;

			NumberOfACKFrame = _NumberOfACKFrame;
			PartitionedFrameSize = _PartitionedFrameSize;
			_ZCLMessage Message = new _ZCLMessage(_Originalmessage.getData());
			byte ___FrameControl = Message.FrameControl;
			// CREO I MESSAGGI DA INVIARE List<string> _Payloads =
			List<byte[]> _Payloads = _SplitMessage(Message,
					(short) PartitionedFrameSize);
			while (_counterOfSending < NumberOfSendRetries) {
				if (gal.getPropertiesManager().getDebugEnabled())
					logger.info("Attempt partition procedure["
							+ _counterOfSending + "/" + NumberOfSendRetries
							+ "]");

				NumberOfACKFrame = _NumberOfACKFrame;
				PartitionedFrameSize = _PartitionedFrameSize;

				// WriteHandShakeParam
				ByteArrayObject _WriteHandshakeParam = new ByteArrayObject();
				_WriteHandshakeParam.addBytesShort(Short
						.reverseBytes((short) _Originalmessage.getClusterID()),
						2); // Sender
				// TimeOut
				_WriteHandshakeParam.addBytesShort(
						Short.reverseBytes((short) 0x0008), 2);
				_WriteHandshakeParam.addByte((byte) 0x21);
				_WriteHandshakeParam.addBytesShort(
						Short.reverseBytes(SenderTimeout), 2);

				// NumberOfACKFrame
				_WriteHandshakeParam.addBytesShort(
						Short.reverseBytes((short) 0x0004), 2);
				_WriteHandshakeParam.addByte((byte) 0x20);
				_WriteHandshakeParam.addByte((byte) NumberOfACKFrame);

				// PartitionedFrameSize
				_WriteHandshakeParam.addBytesShort(
						Short.reverseBytes((short) 0x0002), 2);
				_WriteHandshakeParam.addByte((byte) 0x20);
				_WriteHandshakeParam.addByte((byte) PartitionedFrameSize);

				// InterFrameDelay
				_WriteHandshakeParam.addBytesShort(
						Short.reverseBytes((short) 0x0006), 2);
				_WriteHandshakeParam.addByte((byte) 0x20);
				_WriteHandshakeParam.addByte((byte) InterframeDelay);

				if (gal.getPropertiesManager().getDebugEnabled())
					logger.info("Sending WriteHandshakeParam -- FrameControl: "
							+ Message.FrameControl
							+ " - TransequenceNumber: "
							+ Message.TransequenceNumber
							+ " - CommandID: 02 - PartitionedFrame: "
							+ DataManipulation
									.convertBytesToString(_WriteHandshakeParam
											.getRealByteArray()));

				byte __FrameControl = Message.FrameControl;
				if (Message.Server_to_Client)
					__FrameControl = Message.InvertDirectionBitOfFrameControl();

				ByteArrayObject payloadToSend = new ByteArrayObject();
				payloadToSend.addByte(Message.SetDisableDefaultResponse(true,
						__FrameControl));
				payloadToSend.addByte((byte) 0x02);
				for (byte x : _WriteHandshakeParam.getRealByteArray())
					payloadToSend.addByte(x);

				if (!Send_Message((byte) _Originalmessage.getSourceEndpoint(),
						(byte) _Originalmessage.getDestinationEndpoint(),
						_Originalmessage.getDestinationAddress(),
						payloadToSend.getRealByteArray()))
					return false;
				try {
					Thread.sleep(InterframeDelay);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (gal.getPropertiesManager().getDebugEnabled())
					logger.info("Waiting default response of the WriteHandeshake command...");

				synchronized (lock_DefaultResponse_Of_WriteHandshake_Received) {
					try {
						if (gal.getPropertiesManager().getDebugEnabled())
							logger.info("Waiting default response of the WriteHandeshake command...");
						lock_DefaultResponse_Of_WriteHandshake_Received
								.wait(SenderTimeout);
					} catch (InterruptedException e) {
						if (_counterOfSending < NumberOfSendRetries) {
							_counterOfSending++;
							continue;
						} else
							return false;

					}

				}

				if (gal.getPropertiesManager().getDebugEnabled())
					logger.info("Default response of the WriteHandeshake command received!");

				int _currentNumberOfACK = 0;
				for (int i = 0; i <= _Payloads.size() - 1; i++) {
					if (gal.getPropertiesManager().getDebugEnabled())
						logger.info("Sending Partition Message[" + i + "/"
								+ (_Payloads.size() - 1) + "]");

					ByteArrayObject _payload = new ByteArrayObject();
					_payload.addByte(Message.SetDisableDefaultResponse(false,
							___FrameControl));
					_payload.addByte(Message.TransequenceNumber);
					_payload.addByte((byte) 0x00);

					if (i == 0) {
						_payload.addByte((byte) 0x01);
						_payload.addByte((byte) _Payloads.size());
						for (byte x : _Payloads.get(i))
							_payload.addByte(x);
					} else {
						_payload.addByte((byte) 0x00);
						_payload.addByte((byte) i);
						for (byte x : _Payloads.get(i))
							_payload.addByte(x);

					}

					if (Message.Server_to_Client)
						___FrameControl = Message
								.InvertDirectionBitOfFrameControl();

					if (gal.getPropertiesManager().getDebugEnabled())
						logger.info("Sending TransferPartitionedFrame -- FrameControl:"
								+ ___FrameControl
								+ " - TransequenceNumber:"
								+ Message.TransequenceNumber
								+ " - CommandID:00  - PartitionedFrame:"
								+ DataManipulation
										.convertBytesToString(_Payloads.get(i)));

					if (!Send_Message(
							(byte) _Originalmessage.getSourceEndpoint(),

							(byte) _Originalmessage.getDestinationEndpoint(),
							_Originalmessage.getDestinationAddress(),
							_payload.getRealByteArray()))
						return false;

					try {
						Thread.sleep(InterframeDelay);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if ((_currentNumberOfACK++ == (NumberOfACKFrame - 1))/*
																		 * Last
																		 * frame
																		 * of
																		 * the
																		 * current
																		 * block
																		 */
							|| (i == _Payloads.size() - 1))/*
															 * Last frame of
															 * total count
															 */
					{
						while (true) {

							synchronized (lock_MultipleACK) {
								try {
									if (gal.getPropertiesManager()
											.getDebugEnabled())
										logger.info("Waiting MultipleACK Command...");

									lock_MultipleACK.wait(SenderTimeout);
								} catch (InterruptedException e) {
									if (gal.getPropertiesManager()
											.getDebugEnabled())
										logger.info("SenderTimeout Expired!");

									if (_counterOfSending < NumberOfSendRetries) {
										_counterOfSending++;
										try {
											Thread.sleep(InterframeDelay);
										} catch (InterruptedException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
										continue;
									} else
										return false;

								}

							}
							synchronized (_MultipleAckVectorEmpty) {

								_MultipleAckVectorEmpty = null;
							}
							byte ____FrameControl = Message.FrameControl;

							if (_MultipleAckVector.size() > 0) {

								while (_MultipleAckVector.size() > 0) {
									int _NACKID;
									synchronized (_MultipleAckVector) {
										_NACKID = (Integer) _MultipleAckVector
												.toArray()[0];
										_MultipleAckVector.remove(_NACKID);
									}

									ByteArrayObject _payloadNack = new ByteArrayObject();

									_payloadNack.addByte(Message
											.SetDisableDefaultResponse(false,
													____FrameControl));

									_payloadNack
											.addByte(Message.TransequenceNumber);
									_payloadNack.addByte((byte) 0x00);

									if (_NACKID == 0) {
										_payload.addByte((byte) 0x01);
										_payload.addByte((byte) _Payloads
												.size());
										for (byte x : _Payloads.get(_NACKID))
											_payload.addByte(x);
									} else {
										_payload.addByte((byte) 0x00);
										_payload.addByte((byte) _NACKID);
										for (byte x : _Payloads.get(i))
											_payload.addByte(x);

									}

									if (gal.getPropertiesManager()
											.getDebugEnabled()) {
										logger.info("Sending TransferPartitionedFrame[MultipleACK]  -- FrameControl:"
												+ Message.FrameControl
												+ " - TransequenceNumber:"
												+ Message.TransequenceNumber
												+ " - CommandID:00 - PartitionedFrame:"
												+ DataManipulation
														.convertBytesToString(_payloadNack
																.getRealByteArray()));

										logger.info("Re-Sending Partition Message["
												+ _NACKID
												+ "/"
												+ (_Payloads.size() - 1) + "]");

									}

									if (Message.Server_to_Client)
										____FrameControl = Message
												.InvertDirectionBitOfFrameControl();

									if (!Send_Message(
											(byte) _Originalmessage
													.getSourceEndpoint(),

											(byte) _Originalmessage
													.getDestinationEndpoint(),
											_Originalmessage
													.getDestinationAddress(),
											_payloadNack.getRealByteArray()))
										return false;
									try {
										Thread.sleep(InterframeDelay);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
								continue;
							}
							_currentNumberOfACK = 0;

						}
					}
				}

			}
			return true;
		}

		private class partitionedFrame_Class {

			byte fragmentation;
			boolean firstBlock;
			int PartitionIndicator = 0;
			byte[] partitionedFrame;

			public partitionedFrame_Class(byte[] _Data) throws Exception {

				_ZCLMessage m = new _ZCLMessage(_Data);
				if (m.Payload.length >= 2) {
					fragmentation = m.Payload[0];
				} else
					throw new Exception("Payload too short on Fragmentation:");
				firstBlock = ((fragmentation & 0x01) == 1);// Check //
															// FirstBlock
				int _length = -1;
				if ((fragmentation & 0x02) == 0)// Check Indicator Length
				{
					PartitionIndicator = m.Payload[1];
					_length = m.Payload[2];
					partitionedFrame = new byte[_length];
					int x = 0;
					for (int i = 3; i < (_length + 3); i++)
						partitionedFrame[x++] = m.Payload[i];
				} else {

					PartitionIndicator = DataManipulation.toIntFromShort(
							m.Payload[2], m.Payload[1]);
					_length = m.Payload[3];
					partitionedFrame = new byte[_length];
					int x = 0;
					for (int i = 4; i < (_length + 4); i++)
						partitionedFrame[x++] = m.Payload[i];
				}
			}
		}

	}
}
