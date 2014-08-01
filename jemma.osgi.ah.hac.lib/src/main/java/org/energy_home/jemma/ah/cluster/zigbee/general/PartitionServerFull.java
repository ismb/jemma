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
package org.energy_home.jemma.ah.cluster.zigbee.general;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface PartitionServerFull {

	final static String ATTR_MaximumIncomingTransferSize_NAME = "MaximumIncomingTransferSize";
	final static String ATTR_MaximumOutgoingTransferSize_NAME = "MaximumOutgoingTransferSize";
	final static String ATTR_PartitionedFrameSize_NAME = "PartitionedFrameSize";
	final static String ATTR_LargeFrameSize_NAME = "LargeFrameSize";
	final static String ATTR_NumberOfACKFrame_NAME = "NumberOfACKFrame";
	final static String ATTR_NACKTimeout_NAME = "NACKTimeout";
	final static String ATTR_InterframeDelay_NAME = "InterframeDelay";
	final static String ATTR_NumberOfSendRetries_NAME = "NumberOfSendRetries";
	final static String ATTR_SenderTimeout_NAME = "SenderTimeout";
	final static String ATTR_ReceiverTimeout_NAME = "ReceiverTimeout";
	final static String CMD_TransferPartitionedFrame_NAME = "TransferPartitionedFrame";
	final static String CMD_ReadHandshakeParam_NAME = "ReadHandshakeParam";
	final static String CMD_WriteHandshakeParam_NAME = "WriteHandshakeParam";

	public int getMaximumIncomingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getMaximumOutgoingTransferSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getPartitionedFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setPartitionedFrameSize(short PartitionedFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getLargeFrameSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setLargeFrameSize(int LargeFrameSize, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getNumberOfACKFrame(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setNumberOfACKFrame(short NumberOfACKFrame, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public int getNACKTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getInterframeDelay(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void setInterframeDelay(short InterframeDelay, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public short getNumberOfSendRetries(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getSenderTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getReceiverTimeout(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execTransferPartitionedFrame(short FragmentationOptions, int PartitionIndicator, byte[] PartitionedFrame,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public ReadHandshakeParamResponse execReadHandshakeParam(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException;

	public void execWriteHandshakeParam(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
