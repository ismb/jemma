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
package org.energy_home.jemma.javagal.layers.data.interfaces;

import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.GalController;

/**
 * Data layer interface to be implemented by every vendor (Freescale, Ember and
 * so on...).
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface IDataLayer extends IFrameCallback {
	final Long INTERNAL_TIMEOUT = 5000L;

	public GalController getGalController();
	/* Internal section */

	public void addToReceivedDataQueue(int size, short[] buff);

	public void processMessages() throws Exception;

	public PropertiesManager getPropertiesManager();

	/* Serial comm section */
	public IConnector getIKeyInstance();

	/* Zigbee section */

	public Status APSME_SETSync(long timeout, short _AttID, String _value)
			throws Exception;

	public BigInteger readExtAddress(long timeout) throws GatewayException,
			Exception;

	public List<Short> startServiceDiscoverySync(long timeout, Address aoi)
			throws Exception;

	public String APSME_GETSync(long timeout, short _AttID) throws Exception;

	public String NMLE_GetSync(long timeout, short _AttID) throws Exception;

	public Status NMLE_SETSync(long timeout, short _AttID, String _value)
			throws Exception;

	
	public Status sendApsSync(long timeout, APSMessage message)
			throws Exception;

	public NodeDescriptor getNodeDescriptorSync(long timeout,
			Address addrOfInterest) throws IOException, Exception,
			GatewayException;

	public short getChannelSync(long timeout) throws IOException, Exception,
			GatewayException;

	public Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai)
			throws IOException, Exception, GatewayException;

	public Status stopNetworkSync(long timeout) throws Exception,
			GatewayException;

	public short configureEndPointSync(long timeout, SimpleDescriptor desc)
			throws IOException, Exception, GatewayException;

	public Status clearEndpointSync(short endpoint) throws IOException,
			Exception, GatewayException;

	public Status permitJoinSync(long timeout, Address addrOfInterest,
			short duration, byte TCSignificance) throws IOException, Exception,
			GatewayException;

	public Status permitJoinAllSync(long timeout, Address addrOfInterest,
			short duration, byte TCSignificance) throws IOException, Exception;

	public Status leaveSync(long timeout, Address addrOfInterest, int mask)
			throws Exception;

	
	public NodeServices getLocalServices() throws IOException, Exception,
			GatewayException;

	public ServiceDescriptor getServiceDescriptor(long timeout,
			Address addrOfInterest, short endpoint) throws IOException,
			Exception, GatewayException;

	public BindingList getNodeBindings(long timeout, Address addrOfInterest,
			short index) throws IOException, Exception, GatewayException;

	public Status addBinding(long timeout, Binding binding) throws IOException,
			Exception, GatewayException;

	public Status removeBinding(long timeout, Binding binding)
			throws IOException, Exception, GatewayException;

	public Status frequencyAgilitySync(long timeout, short scanChannel,
			short scanDuration) throws IOException, Exception, GatewayException;

	public void cpuReset() throws Exception;

	public Status SetModeSelectSync(long timeout) throws IOException,
			Exception, GatewayException;
	
	public Status ClearDeviceKeyPairSet(long timeout, Address addrOfInterest) throws IOException,
	Exception, GatewayException;
	
	
	public Status ClearNeighborTableEntry(long timeout, Address addrOfInterest) throws IOException,
	Exception, GatewayException;
	

	
}
