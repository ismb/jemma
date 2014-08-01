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
package org.energy_home.jemma.ah.internal.zigbee;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.energy_home.jemma.ah.cluster.zigbee.general.PartitionServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.ReadHandshakeParamResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.TransferPartitionedFrameCommand;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.eh.ZclReadHandshakeParamResponse;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPartitionClient;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclPartitionServer;
import org.energy_home.jemma.ah.zigbee.zcl.cluster.general.ZclTransferPartitionedFrameCommand;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeClusterID;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PartitionedFrame {
	private boolean _ack = false;
	private int _from;
	private int _length;
	private byte[] _data;

	PartitionedFrame(int from, int length) {
		_from = from;
		_length = length;
		_data = null;
	}

	public PartitionedFrame(int from, byte[] data) {
		_from = from;
		_data = data;
		_length = data.length;
	}

	public boolean isACKd() {
		return (_ack);
	}

	public void setACKd(boolean acked) {
		_ack = acked;
	}

	public int getFrom() {
		return (_from);
	}

	public int getLength() {
		return (_length);
	}

	public byte[] getData() {
		return (_data);
	}
}

public class ZclPartitionServerImpl extends ZclPartitionClient implements Runnable, PartitionServer {
	public ZclPartitionServerImpl() throws ApplianceException {
		super();
		this.c = (PartitionServer) this;
	}

	// Partition Server Cluster Attributes default values
	public int MaximumIncomingTransferSize = 0x0500;
	public int MaximumOutgoingTransferSize = 0x0500;
	public short PartitionedFrameSize = 0x50;
	public int LargeFrameSize = 0x0500;
	public short NumberOfACKFrame = 0x04;
	public int NACKTimeout = 0x4000; // dependent
	public short InterframeDelay = 0x0032;
	public short NumberOfSendRetries = 0x03;
	public int SenderTimeout = 0xffff; // dependent
	public int ReceiverTimeout = 0xffff; // dependent

	private static final int RECEIVER_TIMEOUT = 0x03;
	private static final int NACK_TIMEOUT = 0x04;

	private Hashtable states = new Hashtable();
	private Hashtable statesBySequenceNumber = new Hashtable();

	private class State {
		public State(int clusterId, short sequenceNumber) {
			this.clusterId = clusterId;
			this.calculateDependentAttributes();
		}

		static final int IDLE = 1;
		static final int RECEIVING = 2;

		static final int apsAckWaitDuration = 1600;

		private int clusterId;
		public int currentState = IDLE;
		public int firstPartitionedFrameInSet = 0;
		public int remainingPartitionedFramesToReceive = 0;
		public short remainingPartitionedFramesToReceiveInSet = 0;
		public PartitionedFrame[] partitionedFrames;
		public int sequenceNumber = 0;

		private PartitionTimerTask _timeoutTimerReceiverTask = null;
		private PartitionTimerTask _timeoutTimerNACKTask = null;
		private Timer _receiverTimerThread = new Timer();

		// Attributes
		public int MaximumIncomingTransferSize = ZclPartitionServerImpl.this.MaximumIncomingTransferSize;
		public int MaximumOutgoingTransferSize = ZclPartitionServerImpl.this.MaximumOutgoingTransferSize;
		public short PartitionedFrameSize = ZclPartitionServerImpl.this.PartitionedFrameSize;
		public int LargeFrameSize = ZclPartitionServerImpl.this.LargeFrameSize;
		public short NumberOfACKFrame = ZclPartitionServerImpl.this.NumberOfACKFrame;
		public short InterframeDelay = ZclPartitionServerImpl.this.InterframeDelay;
		public short NumberOfSendRetries = ZclPartitionServerImpl.this.NumberOfSendRetries;

		// The following attributes depends on aps constants and other Partition
		// Cluster attributes
		public int SenderTimeout = ZclPartitionServerImpl.this.SenderTimeout;
		public int NACKTimeout = ZclPartitionServerImpl.this.NACKTimeout;
		public int ReceiverTimeout = ZclPartitionServerImpl.this.ReceiverTimeout;

		public void calculateDependentAttributes() {
			SenderTimeout = 2 * apsAckWaitDuration + InterframeDelay * NumberOfACKFrame;
			NACKTimeout = apsAckWaitDuration + InterframeDelay * NumberOfACKFrame;
			ReceiverTimeout = apsAckWaitDuration + InterframeDelay + NumberOfSendRetries * NACKTimeout;
		}
	}

	private class PartitionTimerTask extends TimerTask {
		private State state;
		private int eventType;

		public PartitionTimerTask(int eventType, State state) {
			this.state = state;
			this.eventType = eventType;
		}

		public void run() {
			try {
				handleEvent(eventType, state.clusterId, state);
			} catch (Exception e) {
				LOG.error("Exception returned by handleEvent", e);
			}
		}
	}

	private Thread thread;

	public ZclPartitionServerImpl(ZigBeeDevice device) throws ApplianceException {
		super();

		// FIXME: a che serve il thread?
		thread = new Thread();
		thread.setName("ZclReceiverPartition");
		thread.start();
		this.device = device;
	}

	public void run() {
	}

	/**
	 * Concatenate the received frames and return back a ZclFrame that will be
	 * notified to the upper cluster.
	 * 
	 * @param partitionedFrames
	 * @return
	 */
	private IZclFrame createAssembledZclFrame(State state) {
		// Determina la lunghezza del buffer che ospitera' l'intero frame.
		// Nota che in partitionedFrame c'e' sempre almeno un frame per cui
		// partitionedFrame.length > 0.

		PartitionedFrame[] pFrame = state.partitionedFrames;

		int frameLength = ((pFrame.length - 1) * state.PartitionedFrameSize)
				+ pFrame[state.partitionedFrames.length - 1].getLength();

		byte[] frame = new byte[frameLength];

		// Assembla tutte le partizioni in un solo grande frame
		for (int i = 0; i < pFrame.length; i++) {
			System.arraycopy(pFrame[i].getData(), 0, frame, (i * state.PartitionedFrameSize), pFrame[i].getLength());
		}

		// FIXME: build correct frame!!!!!
		IZclFrame zclFrame = new ZclFrame(frame);
		zclFrame.setSequence(state.sequenceNumber);
		return zclFrame;
	}

	protected void execMultipleNACK(State state, short ACKOptions, int FirstFrameID, int[] ackList) throws ApplianceException,
			ServiceClusterException {
		boolean NACKIDLength16 = false;
		int size = 0;

		size += ZclDataTypeBitmap8.zclSize(ACKOptions);
		if ((ACKOptions & 0x01) > 0) {
			NACKIDLength16 = true;
		}

		if (NACKIDLength16) {
			size += ZclDataTypeUI16.zclSize(FirstFrameID);
			size += 2 * ackList.length;
		} else {
			size += ZclDataTypeUI8.zclSize((short) FirstFrameID);
			size += ackList.length;
		}

		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(0);
		zclFrame.setSequence(state.sequenceNumber);

		ZclDataTypeBitmap8.zclSerialize(zclFrame, ACKOptions);

		if (NACKIDLength16) {
			ZclDataTypeUI16.zclSerialize(zclFrame, FirstFrameID);
			for (int i = 0; i < ackList.length; i++)
				ZclDataTypeUI16.zclSerialize(zclFrame, ackList[i]);
		} else {
			ZclDataTypeUI8.zclSerialize(zclFrame, (short) FirstFrameID);
			for (int i = 0; i < ackList.length; i++)
				ZclDataTypeUI8.zclSerialize(zclFrame, (short) ackList[i]);
		}

		ZigBeeDevice device = getZigBeeDevice();
		if (device == null)
			throw new ApplianceException("Not attached");

		zclFrame.setDirection(IZclFrame.SERVER_TO_CLIENT_DIRECTION);

		boolean res = false;
		try {
			res = device.post((short) getClusterId(), zclFrame);
		} catch (Exception e) {
			LOG.error("Exception on execMultipleNACK",e);
		}
		if (!res) {
			throw new ApplianceException(POST_FAILED_MESSAGE);
		}
	}

	private void sendMultipleNACK(State state) {
		int FirstFrameID = state.firstPartitionedFrameInSet;
		PartitionedFrame[] partitionedFrames = state.partitionedFrames;

		// Crea la lista dei NACKids in funzione dei partitioned frame non
		// ricevuti
		List nACKIdsList = new LinkedList();

		if (partitionedFrames != null) {
			for (int pfIdx = FirstFrameID; (pfIdx < (FirstFrameID + state.NumberOfACKFrame)) && (pfIdx < partitionedFrames.length); pfIdx++) {
				// Se non e' stato ricevuto allora la relativa posizione
				// nell'array e' null
				if (partitionedFrames[pfIdx] == null)
					nACKIdsList.add(new Integer(pfIdx));
			}
		}

		// TODO: che succede se partitionedFrames e' null?

		// TODO generate the array instead of the List
		int[] nackList = new int[nACKIdsList.size()];
		for (int i = 0; i < nACKIdsList.size(); i++) {
			nackList[i] = ((Integer) nACKIdsList.get(i)).intValue();
		}

		// Aggiunge le ACKOptions con b0=1 (due byte per FirstFrameID e i
		// NACKIds)
		short ACKOptions = 0x00;

		try {
			LOG.debug("Sending MultipleACK for set = " + FirstFrameID);
			execMultipleNACK(state, ACKOptions, FirstFrameID, nackList);
			LOG.debug("MultipleACK sent");
		} catch (Exception e) {
			LOG.error("Exception returned by execMultipleACK", e);
		}
	}

	private boolean handleEvent(int eventType, int clusterId, Object data) throws ZclException {
		State state;

		synchronized (lock) {
			switch (eventType) {

			case NACK_TIMEOUT:
				LOG.warn("NACK Timeout");
				state = (State) data;
				sendMultipleNACK(state);
				break;

			case RECEIVER_TIMEOUT:
				LOG.warn("Receiver Timeout");
				state = (State) data;
				this.deleteState(state);
				break;

			default:
				break;
			}
		}

		return false;
	}

	private static final Logger LOG = LoggerFactory.getLogger( ZclPartitionServer.class );

	private Object lock = new Object();
	private Map peerAttributeDescriptorsMap;

	protected IZclFrame parseTransferPartitionedFrame(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException, ZclException {
		synchronized (lock) {
			State rcvrState = this.getState(zclFrame.getSequenceNumber());

			if (rcvrState == null) {
				throw new ZclException("Unexpected PartitionedFrame received", ZCL.FAILURE);
			}

			TransferPartitionedFrameCommand zclTPFCommand;
			try {
				zclTPFCommand = ZclTransferPartitionedFrameCommand.zclParse(zclFrame);
			} catch (Exception e) {
				LOG.error("Error parsing TransferPartitionedFrame", e);
				return null;
			}

			LOG.debug("PARTITION CLUSTER FRAME RECEIVED");
			LOG.debug("PartitionIndicator: " + zclTPFCommand.PartitionIndicator);

			// log.debug("ClusterID: " + rcvrState.clusterId +
			// ", FragmentationOptions: " + zclTPFCommand.FragmentationOptions
			// + " PartitionIndicator: " + zclTPFCommand.PartitionIndicator);

			if (zclTPFCommand.isFirstBlock()) {
				LOG.debug("isFirstBlock");

				// we are waiting for the first frame && this is the first
				// frame!
				// let start!

				if (rcvrState.currentState == State.IDLE) {
					rcvrState.currentState = State.RECEIVING;
					// Nel primo frame il campo PartitionIndicator indica il
					// numero totale di frame che dovranno
					// essere ricevuti (compreso il primo frame che ha
					// firstBlock = true).
					rcvrState.partitionedFrames = new PartitionedFrame[zclTPFCommand.PartitionIndicator];
					// saves the total number of frames expected
					rcvrState.remainingPartitionedFramesToReceive = zclTPFCommand.PartitionIndicator;
					rcvrState.remainingPartitionedFramesToReceiveInSet = rcvrState.NumberOfACKFrame;
					startReceiverNACKTimer(rcvrState);
				}

				// Forza il primo frame ad avere PartitionIndicator = 0
				// (anziche' il numero di frame totali)
				zclTPFCommand.PartitionIndicator = 0;

				LOG.debug("firstPartitionedFrameInSet: " + rcvrState.firstPartitionedFrameInSet);
				// log.debug("remainingPartitionedFramesToReceiveInSet = " +
				// rcvrState.remainingPartitionedFramesToReceiveInSet);
				// log.debug("remainingPartitionedFramesToReceive = " +
				// rcvrState.remainingPartitionedFramesToReceive);

			} else if (rcvrState.currentState != State.RECEIVING) {
				LOG.debug("Frame discarded because state != RECEIVING");
				return null;
			}

			// checks if the frame have to be discarded

			if ((zclTPFCommand.PartitionIndicator >= rcvrState.partitionedFrames.length)
					|| (zclTPFCommand.PartitionIndicator > (rcvrState.firstPartitionedFrameInSet + rcvrState.NumberOfACKFrame))) {
				LOG.debug("Frame discarded because out of set");
				return null;
			}

			if (rcvrState.partitionedFrames == null) {
				LOG.debug("Frame discarded because partitionedFrames is null");
				return null;
			}

			if (rcvrState.partitionedFrames[zclTPFCommand.PartitionIndicator] != null) {
				LOG.debug("Frame discarded because already received");
				return null;
			}

			// Inserisce il partitioned frame nell'array di quelli ricevuti
			rcvrState.partitionedFrames[zclTPFCommand.PartitionIndicator] = new PartitionedFrame(0, zclTPFCommand.PartitionedFrame);

			if ((rcvrState.remainingPartitionedFramesToReceiveInSet % rcvrState.NumberOfACKFrame) == 0) {
				startReceiverNACKTimer(rcvrState);
			}

			// Decrementa il numero di partitioned frame che ci si aspetta di
			// ricevere
			rcvrState.remainingPartitionedFramesToReceiveInSet--;
			rcvrState.remainingPartitionedFramesToReceive--;

			LOG.debug("Frame accepted");
			LOG.debug("Now remainingPartitionedFramesToReceiveInSet = " + rcvrState.remainingPartitionedFramesToReceiveInSet);
			LOG.debug("Now remainingPartitionedFramesToReceive = " + rcvrState.remainingPartitionedFramesToReceive);

			if ((rcvrState.remainingPartitionedFramesToReceiveInSet == 0) || (rcvrState.remainingPartitionedFramesToReceive == 0)) {
				// TODO: ottimizzare. Qui si manda sempre un MultipleNACK
				// vuoto!!!
				stopReceiverNACKTimer(rcvrState);
				sendMultipleNACK(rcvrState);
				rcvrState.firstPartitionedFrameInSet += rcvrState.NumberOfACKFrame;
				rcvrState.remainingPartitionedFramesToReceiveInSet = rcvrState.NumberOfACKFrame;

				if (rcvrState.remainingPartitionedFramesToReceive == 0) {
					LOG.debug("ALL THE FRAMES WERE RECEIVED");
					stopReceiverTimeoutTimer(rcvrState);

					IZclFrame assembledZclFrame = createAssembledZclFrame(rcvrState);
					LOG.debug("THE MESSAGE IS: " + assembledZclFrame.toString());
					this.device.injectZclFrame((short) rcvrState.clusterId, assembledZclFrame);

					// Soluzione bug
					deleteState(rcvrState);

					return null;
				} else {
					startReceiverNACKTimer(rcvrState);
				}
			}

			// Riavvia il ReceiverTimeout
			restartReceiverTimeoutTimer(rcvrState);
			return null;
		}
	}

	private long calculateTxRxHash(int clusterId, IZclFrame zclFrame) {
		long hash = clusterId;
		hash = hash << 8;
		return zclFrame.getSequenceNumber() & 0xFF | hash;
	}

	private State getState(int clusterId, IZclFrame zclFrame) {
		long hash = calculateTxRxHash(clusterId, zclFrame);
		return (State) this.states.get(new Long(hash));
	}

	private State getState(byte sequenceNumber) {
		return (State) this.statesBySequenceNumber.get(new Integer(sequenceNumber & 0xFF));
	}

	private State createState(int clusterId, IZclFrame zclFrame) {
		long hash = calculateTxRxHash(clusterId, zclFrame);
		Long h = new Long(hash);
		// FIXME: probably rcvState must be initialized with the current value of the server attributes
		
		State state = new State(clusterId, zclFrame.getSequenceNumber());
		this.states.put(h, state);
		state.sequenceNumber = zclFrame.getSequenceNumber() & 0xFF;
		this.statesBySequenceNumber.put(new Integer(state.sequenceNumber), state);
		return state;
	}

	private void deleteState(State state) {
		this.statesBySequenceNumber.remove(state.sequenceNumber);
	}

	protected IZclFrame parseReadHandshakeParam(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {
		ReadHandshakeParamResponse r = new ReadHandshakeParamResponse();

		int size = ZclReadHandshakeParamResponse.zclSize(r);
		IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size);
		zclResponseFrame.setCommandId(1);
		ZclReadHandshakeParamResponse.zclSerialize(zclResponseFrame, r);
		return zclResponseFrame;
	}

	protected IZclFrame parseWriteHandshakeParam(PartitionServer o, IZclFrame zclFrame) throws ApplianceException,
			ServiceClusterException {

		if (this.peerAttributeDescriptorsMap == null) {
			this.peerAttributeDescriptorsMap = new HashMap();
			IZclAttributeDescriptor[] attributeDescriptors = getPeerClusterAttributeDescriptors();
			if (attributeDescriptors != null) {
				for (int i = 0; i < attributeDescriptors.length; i++) {
					peerAttributeDescriptorsMap.put(attributeDescriptors[i].zclGetId(), attributeDescriptors[i]);
				}
			}
		}

		int ClusterID = ZclDataTypeClusterID.zclParse(zclFrame);

		State rcvState = getState(ClusterID, zclFrame);
		if (rcvState == null) {
			rcvState = createState(ClusterID, zclFrame);
		}

		LOG.debug("WRITE HANDASHAKE PARAMETERS RECEIVED");
		while (true) {
			int attrId;
			try {
				attrId = ZclDataTypeUI16.zclParse(zclFrame);
			} catch (Throwable e) {
				LOG.debug("No more attributes",e); //FIXME this seems not critical - why catching this with an exception ?
				break;
			}

			try {
				IZclAttributeDescriptor descriptor = this.getPeerAttributeDescriptor(attrId);
				short zclDataType = ZclDataTypeUI8.zclParse(zclFrame);
				if (descriptor != null && descriptor.zclGetDataType().zclGetDataType() == zclDataType) {
					switch (attrId) {
					case 0x0000:
						rcvState.MaximumIncomingTransferSize = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.debug("MaximumIncomingTransferSize = " + rcvState.MaximumIncomingTransferSize);
						break;

					case 0x0001:
						rcvState.MaximumOutgoingTransferSize = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.debug("MaximumOutgoingTransferSize = " + rcvState.MaximumOutgoingTransferSize);
						break;

					case 0x0002:
						rcvState.PartitionedFrameSize = ZclDataTypeUI8.zclParse(zclFrame);
						LOG.debug("PartitionedFrameSize = " + rcvState.PartitionedFrameSize);
						break;

					case 0x0003:
						rcvState.LargeFrameSize = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.debug("LargeFrameSize = " + rcvState.LargeFrameSize);
						break;

					case 0x0004:
						rcvState.NumberOfACKFrame = ZclDataTypeUI8.zclParse(zclFrame);
						LOG.debug("NumberOfACKFrame = " + rcvState.NumberOfACKFrame);
						break;

					case 0x0005:
						rcvState.NACKTimeout = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.warn("WriteHanshakeParameter requested to change the dependent attribute NACKTimeout to "
								+ rcvState.NACKTimeout);
						break;

					case 0x0006:
						rcvState.InterframeDelay = ZclDataTypeUI8.zclParse(zclFrame);
						LOG.warn("WriteHanshakeParameter requested to change the dependent attribute InterframeDelay to "
								+ rcvState.InterframeDelay);
						break;

					case 0x0007:
						rcvState.NumberOfSendRetries = ZclDataTypeUI8.zclParse(zclFrame);
						LOG.warn("WriteHanshakeParameter requested to change the dependent attribute NumberOfSendRetries to "
								+ rcvState.NumberOfSendRetries);
						break;

					case 0x0008:
						rcvState.SenderTimeout = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.warn("WriteHanshakeParameter requested to change the dependent attribute SenderTimeout to "
								+ rcvState.SenderTimeout);
						break;

					case 0x0009:
						rcvState.ReceiverTimeout = ZclDataTypeUI16.zclParse(zclFrame);
						LOG.warn("WriteHanshakeParameter requested to change the dependent attribute ReceiverTimeout to "
								+ rcvState.ReceiverTimeout);
						break;

					default:
						LOG.error("Unknown attribute ID " + attrId);
						break;
					}
				}
			} catch (ZclValidationException e) {
				continue;
			} catch (Throwable e) {
				LOG.error("Error parsing WriteHanshakeParameters", e);
				return null;
			}
		}

		LOG.error("ReceiverTimeout: " + rcvState.ReceiverTimeout + ", NACKTimeout: " + rcvState.NACKTimeout);

		// recalculate dependent attributes because they might have changed.
		rcvState.calculateDependentAttributes();
		return null;
	}

	private IZclAttributeDescriptor getPeerAttributeDescriptor(int attrId) {
		return (IZclAttributeDescriptor) this.peerAttributeDescriptorsMap.get(attrId);
	}

	private void startReceiverTimeoutTimer(State state) {
		LOG.debug("Starting Receiver Timeout Timer of " + state.ReceiverTimeout + " ms");
		stopReceiverTimeoutTimer(state);
		if (state._timeoutTimerReceiverTask == null) {
			state._timeoutTimerReceiverTask = new PartitionTimerTask(RECEIVER_TIMEOUT, state);
		}
		state._receiverTimerThread.schedule(state._timeoutTimerReceiverTask, state.ReceiverTimeout);
	}

	private void stopReceiverTimeoutTimer(State state) {
		LOG.debug("Stopping Receiver Timeout Timer");
		synchronized (state) {
			if (state._timeoutTimerReceiverTask != null) {
				state._timeoutTimerReceiverTask.cancel();
				state._receiverTimerThread.purge();
				state._timeoutTimerReceiverTask = null;
			}
		}
	}

	private void startReceiverNACKTimer(State state) {
		LOG.debug("Starting Receiver NACK Timer of " + state.NACKTimeout + " ms");
		stopReceiverNACKTimer(state);
		if (state._timeoutTimerNACKTask == null) {
			state._timeoutTimerNACKTask = new PartitionTimerTask(NACK_TIMEOUT, state);
		}
		state._receiverTimerThread.schedule(state._timeoutTimerNACKTask, state.NACKTimeout);
	}

	private void stopReceiverNACKTimer(State state) {
		synchronized (state) {
			LOG.debug("Stopping Receiver NACK Timer");
			if (state._timeoutTimerNACKTask != null) {
				state._timeoutTimerNACKTask.cancel();
				state._receiverTimerThread.purge();
				state._timeoutTimerNACKTask = null;
			}
		}
	}

	private void restartReceiverTimeoutTimer(State state) {
		stopReceiverTimeoutTimer(state);
		startReceiverTimeoutTimer(state);
	}

	public boolean handleCluster(short clusterId, short commandId) {
		return false;
	}

	public boolean enablePartitioning(short clusterId, short commandId) {
		// TODO: implement it!!!
		return true;
	}

	public boolean disablePartitioning(short clusterId, short commandId) {
		// TODO implement it!!
		return true;
	}

	public boolean isPartitioningEnabled(short clusterId, short commandId) {
		// TODO: implement it!!
		return true;
	}

	protected ZigBeeDevice getZigBeeDevice() throws ApplianceException {
		return device;
	}

	public int getMaximumIncomingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.MaximumIncomingTransferSize;
	}

	public int getMaximumOutgoingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.MaximumOutgoingTransferSize;
	}

	public short getPartitionedFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.PartitionedFrameSize;
	}

	public void setPartitionedFrameSize(short PartitionedFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		this.PartitionedFrameSize = PartitionedFrameSize;
	}

	public int getLargeFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.LargeFrameSize;
	}

	public void setLargeFrameSize(int LargeFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		this.LargeFrameSize = LargeFrameSize;
	}

	public short getNumberOfACKFrame(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.NumberOfACKFrame;
	}

	public void setNumberOfACKFrame(short NumberOfACKFrame, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		this.NumberOfACKFrame = NumberOfACKFrame;
	}

	public int getNACKTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.NACKTimeout;
	}

	public short getInterframeDelay(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.InterframeDelay;
	}

	public void setInterframeDelay(short InterframeDelay, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		this.InterframeDelay = InterframeDelay;
	}

	public short getNumberOfSendRetries(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.NumberOfSendRetries;
	}

	public int getSenderTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.SenderTimeout;
	}

	public int getReceiverTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		return this.ReceiverTimeout;
	}

	public void execTransferPartitionedFrame(short FragmentationOptions, int PartitionIndicator, short FrameType,
			byte[] PartitionedFrame, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		// NOT USED, we prefer to override the parsePartitioningFrame method
	}

	public ReadHandshakeParamResponse execReadHandshakeParam(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		// NOT USED, we prefer to override the parseReadHandshakeParam method
		return null;
	}

	public void execWriteHandshakeParam(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		// NOT USED, we prefer to override the parseWriteHandshakeParam method
	}
}