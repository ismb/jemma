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
package org.energy_home.jemma.zgd;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.presentation.Activator;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
import org.energy_home.jemma.zgd.jaxb.Filter;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;
import org.energy_home.jemma.zgd.jaxb.Level;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.NodeServicesList;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Version;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.energy_home.jemma.zgd.jaxb.ZCLCommand;
import org.energy_home.jemma.zgd.jaxb.ZDPCommand;

/**
 * Proxy object for {@link GalController} object. The proxy pattern enables the
 * management of multiple concurrent clients. Each client instantiates a proxy
 * with its unique identifier that distinguishes it between other proxy's
 * objects.
 * <p>
 * The mechanism used by the Gal controller is simple: every time a client
 * requests to the Gal controller the execution of one of its methods, the proxy
 * adds its identifier to that request. After a while, when the response will
 * become available to the Gal controller, it uses that identifier to find the
 * right proxy destination (the client) to dispatch the response to it.
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 */
public class GalExtenderProxy implements IGalExtender {
	private static final Logger LOG = LoggerFactory.getLogger(GalExtenderProxyFactory.class);
	/**
	 * The identification number for this proxy instance.
	 */
	private final int proxyIdentifier;
	/**
	 * The local {@link GalController} reference.
	 */
	private final GalController gal;

	/**
	 * Creates a new instance with the desired proxy identifier and a reference
	 * the Gal Controller.
	 * 
	 * @param proxyIdentifier
	 *            the desired proxy identifier
	 * @param _gal
	 *            a reference to the Gal controller
	 */
	public GalExtenderProxy(int proxyIdentifier, GalController _gal) {
		// The gal controller is created just one time.
		this.gal = _gal;
		this.proxyIdentifier = proxyIdentifier;
	}

	
	public short getChannelSync(long timeout) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.getChannelSync(timeout);

	}

	
	public void setGatewayEventListener(GatewayEventListener listener) {
		gal.setGatewayEventListener(listener, this.getProxyIdentifier());
	}

	
	public Version getVersion() throws IOException, Exception, GatewayException {
		return GalController.getVersion();
	}

	
	public String getInfoBaseAttribute(short attrId) throws Exception, Exception, GatewayException {
		String res = null;
		switch (attrId) {
		case 0xA0:// nwkSecurityLevel
		case 0x80:// Short PanId
		case 0x9A:// Extended Pan ID
		case 0x96:// NetWorkAddress
		case 0xDA:// DeviceType
		case 0xDB:// nwkSoftwareVersion
		case 0xE6:// nwkSoftwareVersion
			res = gal.NMLE_GetSync(attrId, (short) 0x00);
			break;
		case 0xA1:// nwkTransportKey
			res = gal.NMLE_GetSync(attrId, (short) 0x01);
			break;
		/*
		 * case 0x85: //MacKey res = gal.MacGetPIBAttributeSync(attrId);
		 */
		case 0xC3:
		case 0xC4:
		case 0xC8:
			res = gal.APSME_GETSync(attrId);
			break;
		default:
			throw new Exception("Unsupported Attribute");
		}
		return res;
	}

	
	@Deprecated
	public long createCallback(Callback callback, APSMessageListener listener) throws IOException, Exception, GatewayException {
		return gal.createCallback(this.getProxyIdentifier(), callback, listener);
	}

	@Deprecated
	
	public long createAPSCallback(short endpoint, APSMessageListener listener) throws IOException, Exception, GatewayException {
		LOG.debug("Create ApsCallBack(short endpoint, APSMessageListener listener)...");

		Callback _newCallBack = new Callback();
		Filter _newFilter = new Filter();
		Filter.LevelSpecification ls1 = new Filter.LevelSpecification();
		ls1.getLevel().add(Level.APS_LEVEL);
		_newFilter.setLevelSpecification(ls1);
		_newFilter.setLevelSpecification(_newFilter.getLevelSpecification());
		Filter.AddressSpecification _addressSpec = new Filter.AddressSpecification();
		_addressSpec.setAPSDestinationEndpoint(endpoint);
		_newFilter.getAddressSpecification().add(_addressSpec);
		_newCallBack.setFilter(_newFilter);
		return gal.createCallback(this.getProxyIdentifier(), _newCallBack, listener);
	}

	@Deprecated
	
	public long createAPSCallback(APSMessageListener listener) throws IOException, Exception, GatewayException {
		LOG.debug("Create ApsCallBack(APSMessageListener listener)...");

		Callback _newCallBack = new Callback();
		Filter _newFilter = new Filter();
		Filter.LevelSpecification ls1 = new Filter.LevelSpecification();
		ls1.getLevel().add(Level.APS_LEVEL);
		_newFilter.setLevelSpecification(ls1);
		_newFilter.setLevelSpecification(_newFilter.getLevelSpecification());
		_newCallBack.setFilter(_newFilter);
		return gal.createCallback(this.getProxyIdentifier(), _newCallBack, listener);
	}

	
	public List<Long> listCallbacks() throws IOException, Exception, GatewayException {
		return gal.listCallbacks(this.getProxyIdentifier()).getCallbackIdentifier();
	}

	
	public CallbackIdentifierList getlistCallbacks() throws IOException, Exception, GatewayException {
		return gal.listCallbacks(this.getProxyIdentifier());
	}

	
	public void deleteCallback(long callId) throws IOException, Exception, GatewayException {
		gal.deleteCallback(callId);
	}

	
	public Aliases listAddresses() throws IOException, Exception, GatewayException {
		return gal.listAddress();
	}

	
	public void configureStartupAttributeSet(StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		gal.getPropertiesManager().SetStartupAttributeInfo(sai);

	}

	
	public StartupAttributeInfo readStartupAttributeSet(short index) throws IOException, Exception, GatewayException {
		if (index == 0)
			return gal.getPropertiesManager().getSturtupAttributeInfo();
		else
			throw new Exception("The index is not correct. Only index 0 is a possible request!");
	}

	
	public Status stopNetworkSync(long timeout) throws Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.stopNetwork(timeout, this.getProxyIdentifier(), false);
	}

	
	public void stopNetwork(long timeout) throws Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.stopNetwork(timeout, this.getProxyIdentifier(), true);
	}

	
	public void startGatewayDevice(long timeout) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.startGatewayDevice(timeout, this.getProxyIdentifier(), true);

	}

	
	public void startGatewayDevice(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.startGatewayDevice(timeout, this.getProxyIdentifier(), sai, true);
	}

	
	public Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		if (sai == null)
			return gal.startGatewayDevice(timeout, this.getProxyIdentifier(), false);
		else
			return gal.startGatewayDevice(timeout, this.getProxyIdentifier(), sai, false);
	}

	
	public WSNNodeList readNodeCache() throws IOException, Exception, GatewayException {
		return gal.readNodeCache();
	}

	
	public void startNodeDiscovery(long timeout, int discoveryMask) throws IOException, Exception, GatewayException {
		gal.startNodeDiscovery(timeout, this.getProxyIdentifier(), discoveryMask);

	}

	
	public void subscribeNodeRemoval(long timeout, int freshnessMask) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		if (freshnessMask != 16 && freshnessMask != 4 && freshnessMask != 20 && freshnessMask != 0)
			throw new GatewayException("NodeRemoval mask not valid");
		else
			gal.subscribeNodeRemoval(timeout, this.getProxyIdentifier(), freshnessMask);

	}

	
	public NodeServices getLocalServices() throws IOException, Exception, GatewayException {
		return gal.getLocalServices();
	}

	
	public NodeServicesList readServicesCache() throws IOException, Exception, GatewayException {
		return gal.readServicesCache();
	}

	
	public void getServiceDescriptor(long timeout, Address aoi, short endpoint) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.getServiceDescriptor(timeout, this.getProxyIdentifier(), aoi, endpoint, true);

	}

	
	public ServiceDescriptor getServiceDescriptorSync(long timeout, Address aoi, short endpoint) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.getServiceDescriptor(timeout, this.getProxyIdentifier(), aoi, endpoint, false);

	}

	
	public short configureEndpoint(long timeout, SimpleDescriptor desc) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.configureEndpoint(timeout, desc);

	}

	
	public void clearEndpoint(short endpoint) throws IOException, Exception, GatewayException {
		gal.clearEndpoint(endpoint);

	}

	
	public void leaveAll() throws IOException, Exception, GatewayException {
		long _timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		int mask = 0;
		Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		gal.leave(_timeout, this.getProxyIdentifier(), _add, mask, true);

	}

	
	public Status leaveAllSync() throws IOException, Exception, GatewayException {
		long _timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		int mask = 0;
		Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		return gal.leave(_timeout, this.getProxyIdentifier(), _add, mask, false);

	}

	
	public void leave(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		int mask = 0;
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, true);

	}

	
	public void leave(long timeout, Address aoi, int mask) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, true);

	}

	
	public Status leaveSync(long timeout, Address aoi, int mask) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, false);

	}

	
	public void addBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.addBindingSync(timeout, this.getProxyIdentifier(), binding, true);

	}

	
	public Status addBindingSync(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.addBindingSync(timeout, this.getProxyIdentifier(), binding, false);

	}

	
	public void removeBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.removeBindingSync(timeout, this.getProxyIdentifier(), binding, true);

	}

	
	public Status removeBindingSync(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.removeBindingSync(timeout, this.getProxyIdentifier(), binding, false);
	}

	
	public BindingList getNodeBindingsSync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, (short) 0, false);
	}

	
	public void getNodeBindings(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, (short) 0, true);
	}

	
	public void getNodeBindings(long timeout, Address aoi, short index) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, index, true);

	}

	
	public BindingList getNodeBindingsSync(long timeout, Address aoi, short index) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, index, false);

	}

	
	public void permitJoinAll(long timeout, short duration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.permitJoinAll(timeout, this.getProxyIdentifier(), duration, true);
	}

	
	public Status permitJoinAllSync(long timeout, short duration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.permitJoinAll(timeout, this.getProxyIdentifier(), duration, false);
	}

	
	public void permitJoin(long timeout, Address addrOfInterest, short duration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.permitJoin(timeout, this.getProxyIdentifier(), addrOfInterest, duration, true);
	}

	
	public Status permitJoinSync(long timeout, Address aoi, short duration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.permitJoin(timeout, this.getProxyIdentifier(), aoi, duration, false);
	}

	
	public void sendAPSMessage(APSMessage message) throws IOException, Exception, GatewayException {
		gal.sendAPSMessage(gal.getPropertiesManager().getCommandTimeoutMS(), this.getProxyIdentifier(), message);
	}

	
	public void sendInterPANMessage(long timeout, InterPANMessage message) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.sendInterPANMessage(timeout, this.getProxyIdentifier(), message);
	}

	
	public void sendAPSMessage(long timeout, APSMessage message) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.sendAPSMessage(timeout, this.getProxyIdentifier(), message);
	}

	
	public void resetDongle(long timeout, short mode) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.resetDongle(timeout, this.getProxyIdentifier(), mode, true);
	}

	
	public Status resetDongleSync(long timeout, short mode) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.resetDongle(timeout, this.getProxyIdentifier(), mode, false);
	}

	
	public int getProxyIdentifier() {
		return proxyIdentifier;
	}

	
	public NodeDescriptor getNodeDescriptorSync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.getNodeDescriptor(timeout, this.getProxyIdentifier(), aoi, false);
	}

	
	public void getNodeDescriptor(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.getNodeDescriptor(timeout, this.getProxyIdentifier(), aoi, true);

	}

	
	public void setInfoBaseAttribute(short attrId, String value) throws IOException, Exception, GatewayException {

		switch (attrId) {
		case 0xA1:
		case 0x80:
			gal.NMLE_SetSync(attrId, value);
			break;
		case 0xC3:
		case 0xC4:
		case 0xC8:
			gal.APSME_SETSync(attrId, value);
			break;
		default:
			throw new Exception("Unsupported Attribute");
		}

	}

	
	public NodeServices startServiceDiscoverySync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.startServiceDiscovery(timeout, this.getProxyIdentifier(), aoi, false);
	}

	
	public void startServiceDiscovery(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.startServiceDiscovery(timeout, this.getProxyIdentifier(), aoi, true);

	}

	
	public void frequencyAgility(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		gal.frequencyAgilitySync(timeout, this.getProxyIdentifier(), scanChannel, scanDuration, true);
	}

	
	public Status frequencyAgilitySync(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		return gal.frequencyAgilitySync(timeout, this.getProxyIdentifier(), scanChannel, scanDuration, false);
	}

	
	public LQIInformation getLQIInformation(Address aoi) throws IOException, Exception, GatewayException {

		return gal.getLQIInformation(aoi);
	}

	
	public LQIInformation getLQIInformation() throws IOException, Exception, GatewayException {
		return gal.getAllLQIInformations();
	}

	
	public void sendZCLCommand(long timeout, ZCLCommand command) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		APSMessage _message = new APSMessage();
		_message.setClusterID(command.getClusterID());
		_message.setProfileID(command.getProfileID());
		_message.setDestinationAddress(command.getDestinationAddress());
		_message.setDestinationAddressMode(command.getDestinationAddressMode());
		_message.setDestinationEndpoint(command.getDestinationEndpoint());
		_message.setRadius(command.getRadius());
		_message.setSourceEndpoint(command.getSourceEndpoint());
		_message.setTxOptions(command.getTxOptions());
		_message.setData(org.energy_home.jemma.javagal.layers.business.Utils.mergeBytesVect(command.getZCLHeader(), command.getZCLPayload()));
		gal.sendAPSMessage(timeout, this.getProxyIdentifier(), _message);

	}

	
	public void sendZDPCommand(long timeout, ZDPCommand command) throws IOException, Exception, GatewayException {
		if (timeout == 0)
			timeout = gal.getPropertiesManager().getCommandTimeoutMS();
		APSMessage _message = new APSMessage();
		_message.setClusterID(command.getClusterID());
		_message.setProfileID(0x0000);
		_message.setDestinationAddress(command.getDestination());
		_message.setDestinationAddressMode(command.getDestinationAddrMode());
		_message.setDestinationEndpoint((short) 0x00);
		_message.setRadius(command.getRadius());
		_message.setSourceEndpoint((short) 0x00);
		_message.setTxOptions(command.getTxOptions());
		_message.setData(command.getCommand());
		gal.sendAPSMessage(timeout, this.getProxyIdentifier(), _message);
	}

	
	public void deleteProxy() throws Exception {

		/**
		 * Deletion of GatewayEventListener and Callbacks related to the
		 * GatewayInterface Proxy ID
		 **/
		for (int i = 0; i < gal.getListGatewayEventListener().size(); i++) {
			if (gal.getListGatewayEventListener().get(i).getProxyIdentifier() == getProxyIdentifier()) {
				gal.getListGatewayEventListener().remove(i);
			}
		}

		for (int i = 0; i < gal.getCallbacks().size(); i++) {
			if (gal.getCallbacks().get(i).getProxyIdentifier() == getProxyIdentifier()) {
				gal.getCallbacks().remove(i);
			}

		}

	}

	
	public long createCallback(Callback callback, MessageListener listener) throws IOException, Exception, GatewayException {
		return gal.createCallback(this.getProxyIdentifier(), callback, listener);

	}
	
	public void recoveryGal() throws Exception
	{
		gal.recoveryGAL();
	}
}
