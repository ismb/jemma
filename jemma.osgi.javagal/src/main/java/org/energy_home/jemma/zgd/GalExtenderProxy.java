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

import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.CallbackIdentifierList;
import org.energy_home.jemma.zgd.jaxb.Filter;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;

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
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GalExtenderProxy implements IGalExtender {
	private final static Log logger = LogFactory.getLog(GalExtenderProxyFactory.class);
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

	@Override
	public short getChannelSync(long timeout) throws IOException, Exception, GatewayException {
		return gal.getChannelSync(timeout);

	}

	@Override
	public void setGatewayEventListener(GatewayEventListener listener) {
		gal.setGatewayEventListener(listener, this.getProxyIdentifier());
	}

	@Override
	public Version getVersion() throws IOException, Exception, GatewayException {
		return GalController.getVersion();
	}

	@Override
	public String getInfoBaseAttribute(short attrId) throws Exception, Exception, GatewayException {
		switch (attrId) {
		case 0xA1:
		case 0x80:
			return gal.NMLE_GetSync(attrId);

		case 0xC3:
		case 0xC4:
		case 0xC8:
			return gal.APSME_GETSync(attrId);

		default:
			throw new Exception("Unsupported Attribute");
		}
	}

	@Override
	public long createCallback(Callback callback, APSMessageListener listener) throws IOException, Exception, GatewayException {
		return gal.createCallback(this.getProxyIdentifier(), callback, listener);
	}

	@Override
	public long createAPSCallback(short endpoint, APSMessageListener listener) throws IOException, Exception, GatewayException {
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Create ApsCallBack(short endpoint, APSMessageListener listener)...");
		}
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

	@Override
	public long createAPSCallback(APSMessageListener listener) throws IOException, Exception, GatewayException {
		if (gal.getPropertiesManager().getDebugEnabled()) {
			logger.info("Create ApsCallBack(APSMessageListener listener)...");
		}
		Callback _newCallBack = new Callback();
		Filter _newFilter = new Filter();
		Filter.LevelSpecification ls1 = new Filter.LevelSpecification();
		ls1.getLevel().add(Level.APS_LEVEL);
		_newFilter.setLevelSpecification(ls1);
		_newFilter.setLevelSpecification(_newFilter.getLevelSpecification());
		_newCallBack.setFilter(_newFilter);
		return gal.createCallback(this.getProxyIdentifier(), _newCallBack, listener);
	}

	@Override
	public List<Long> listCallbacks() throws IOException, Exception, GatewayException {
		return gal.listCallbacks(this.getProxyIdentifier()).getCallbackIdentifier();
	}

	@Override
	public CallbackIdentifierList getlistCallbacks() throws IOException, Exception, GatewayException {
		return gal.listCallbacks(this.getProxyIdentifier());
	}

	@Override
	public void deleteCallback(long callId) throws IOException, Exception, GatewayException {
		gal.deleteCallback(callId);
	}

	@Override
	public Aliases listAddresses() throws IOException, Exception, GatewayException {
		return gal.listAddress();
	}

	@Override
	public void configureStartupAttributeSet(StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		gal.getPropertiesManager().SetStartupAttributeInfo(sai);

	}

	@Override
	public StartupAttributeInfo readStartupAttributeSet(short index) throws IOException, Exception, GatewayException {
		if (index == 0)
			return gal.getPropertiesManager().getSturtupAttributeInfo();
		else
			throw new Exception("The index is not correct. Only index 0 is a possible request!");
	}

	@Override
	public Status stopNetworkSync(long timeout) throws Exception, GatewayException {
		return gal.stopNetwork(timeout, this.getProxyIdentifier(), false);
	}

	@Override
	public void stopNetwork(long timeout) throws Exception, GatewayException {
		gal.stopNetwork(timeout, this.getProxyIdentifier(), true);
	}

	@Override
	public void startGatewayDevice(long timeout) throws IOException, Exception, GatewayException {

		gal.startGatewayDevice(timeout, this.getProxyIdentifier(), true);

	}
	
	

	@Override
	public void startGatewayDevice(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException {

		gal.startGatewayDevice(timeout, this.getProxyIdentifier(), sai, true);
	}

	@Override
	public Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException {
		if (sai == null)
			return gal.startGatewayDevice(timeout, this.getProxyIdentifier(), false);
		else
			return gal.startGatewayDevice(timeout, this.getProxyIdentifier(), sai, false);
	}

	@Override
	public WSNNodeList readNodeCache() throws IOException, Exception, GatewayException {
		return gal.readNodeCache();
	}

	@Override
	public void startNodeDiscovery(long timeout, int discoveryMask) throws IOException, Exception, GatewayException {
		gal.startNodeDiscovery(timeout, this.getProxyIdentifier(), discoveryMask);

	}

	@Override
	public void subscribeNodeRemoval(long timeout, int freshnessMask) throws IOException, Exception, GatewayException {

		if (freshnessMask != 16 && freshnessMask != 4 && freshnessMask != 20 && freshnessMask != 0)
			throw new GatewayException("NodeRemoval mask not valid");
		else
			gal.subscribeNodeRemoval(timeout, this.getProxyIdentifier(), freshnessMask);

	}

	@Override
	public NodeServices getLocalServices() throws IOException, Exception, GatewayException {
		return gal.getLocalServices();
	}

	@Override
	public NodeServicesList readServicesCache() throws IOException, Exception, GatewayException {
		return gal.readServicesCache();
	}

	@Override
	public void getServiceDescriptor(long timeout, Address aoi, short endpoint) throws IOException, Exception, GatewayException {
		gal.getServiceDescriptor(timeout, this.getProxyIdentifier(), aoi, endpoint, true);

	}

	@Override
	public ServiceDescriptor getServiceDescriptorSync(long timeout, Address aoi, short endpoint) throws IOException, Exception, GatewayException {
		return gal.getServiceDescriptor(timeout, this.getProxyIdentifier(), aoi, endpoint, false);

	}

	@Override
	public short configureEndpoint(long timeout, SimpleDescriptor desc) throws IOException, Exception, GatewayException {
		return gal.configureEndpoint(timeout, desc);
	
	
	
	}

	@Override
	public void clearEndpoint(short endpoint) throws IOException, Exception, GatewayException {
		gal.clearEndpoint(endpoint);

	}

	@Override
	public void leaveAll() throws IOException, Exception, GatewayException {
		long _timeout = GatewayConstants.INFINITE_TIMEOUT;
		int mask = 0;
		Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		gal.leave(_timeout, this.getProxyIdentifier(), _add, mask, true);

		
	}

	@Override
	public Status leaveAllSync() throws IOException, Exception, GatewayException {
		long _timeout = GatewayConstants.INFINITE_TIMEOUT;
		int mask = 0;
		Address _add = new Address();
		_add.setNetworkAddress(0xFFFC);
		return gal.leave(_timeout, this.getProxyIdentifier(), _add, mask, false);

	}

	@Override
	public void leave(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		int mask = 0;
		gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, true);

	}

	@Override
	public void leave(long timeout, Address aoi, int mask) throws IOException, Exception, GatewayException {
		gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, true);

	}

	@Override
	public Status leaveSync(long timeout, Address aoi, int mask) throws IOException, Exception, GatewayException {
		return gal.leave(timeout, this.getProxyIdentifier(), aoi, mask, false);

	}

	@Override
	public void addBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		gal.addBindingSync(timeout, this.getProxyIdentifier(), binding, true);

	}

	@Override
	public Status addBindingSync(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		return gal.addBindingSync(timeout, this.getProxyIdentifier(), binding, false);

	}

	@Override
	public void removeBinding(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		gal.removeBindingSync(timeout, this.getProxyIdentifier(), binding, true);

	}

	@Override
	public Status removeBindingSync(long timeout, Binding binding) throws IOException, Exception, GatewayException {
		return gal.removeBindingSync(timeout, this.getProxyIdentifier(), binding, false);
	}

	@Override
	public BindingList getNodeBindingsSync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		return gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, (short) 0, false);
	}

	@Override
	public void getNodeBindings(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, (short) 0, true);
	}

	@Override
	public void getNodeBindings(long timeout, Address aoi, short index) throws IOException, Exception, GatewayException {
		gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, index, true);

	}

	@Override
	public BindingList getNodeBindingsSync(long timeout, Address aoi, short index) throws IOException, Exception, GatewayException {
		return gal.getNodeBindingsSync(timeout, this.getProxyIdentifier(), aoi, index, false);

	}

	@Override
	public void permitJoinAll(long timeout, short duration) throws IOException, Exception, GatewayException {
		gal.permitJoinAll(timeout, this.getProxyIdentifier(), duration, true);
	}

	@Override
	public Status permitJoinAllSync(long timeout, short duration) throws IOException, Exception, GatewayException {
		return gal.permitJoinAll(timeout, this.getProxyIdentifier(), duration, false);
	}
	
	@Override
	public void permitJoin(long timeout, Address addrOfInterest, short duration) throws IOException, Exception, GatewayException {
		gal.permitJoin(timeout, this.getProxyIdentifier(), addrOfInterest, duration, true);
	}

	@Override
	public Status permitJoinSync(long timeout, Address aoi, short duration) throws IOException, Exception, GatewayException {
		return gal.permitJoin(timeout, this.getProxyIdentifier(), aoi, duration, false);
	}

	@Override
	public void sendAPSMessage(APSMessage message) throws IOException, Exception, GatewayException {
		gal.sendAPSMessage(gal.getPropertiesManager().getCommandTimeoutMS(), this.getProxyIdentifier(), message);
	}

	@Override
	public void sendAPSMessage(long timeout, APSMessage message) throws IOException, Exception, GatewayException {
		gal.sendAPSMessage(timeout, this.getProxyIdentifier(), message);
	}

	@Override
	public void resetDongle(long timeout, short mode) throws IOException, Exception, GatewayException {
		gal.resetDongle(timeout, this.getProxyIdentifier(), mode, true);
	}

	@Override
	public Status resetDongleSync(long timeout, short mode) throws IOException, Exception, GatewayException {
		return gal.resetDongle(timeout, this.getProxyIdentifier(), mode, false);
	}

	@Override
	public int getProxyIdentifier() {
		return proxyIdentifier;
	}

	@Override
	public NodeDescriptor getNodeDescriptorSync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		return gal.getNodeDescriptor(timeout, this.getProxyIdentifier(), aoi, false);
	}

	@Override
	public void getNodeDescriptor(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		gal.getNodeDescriptor(timeout, this.getProxyIdentifier(), aoi, true);

	}

	@Override
	public void setInfoBaseAttribute(short attrId, String value) throws IOException, Exception, GatewayException {

		switch (attrId) {
		case 0xA1:
		case 0x80:
			gal.NMLE_SetSync(attrId, value);

		case 0xC3:
		case 0xC4:
		case 0xC8:
			gal.APSME_SETSync(attrId, value);

		default:
			throw new Exception("Unsupported Attribute");
		}

	}

	@Override
	public NodeServices startServiceDiscoverySync(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		return gal.startServiceDiscovery(timeout, this.getProxyIdentifier(), aoi, false);
	}

	@Override
	public void startServiceDiscovery(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		gal.startServiceDiscovery(timeout, this.getProxyIdentifier(), aoi, true);

	}

	@Override
	public void frequencyAgility(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException {
		gal.frequencyAgilitySync(timeout, this.getProxyIdentifier(), scanChannel, scanDuration, true);
	}

	@Override
	public Status frequencyAgilitySync(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException {
		return gal.frequencyAgilitySync(timeout, this.getProxyIdentifier(), scanChannel, scanDuration, false);
	}

	@Override
	public LQIInformation getLQIInformation(Address aoi) throws IOException, Exception, GatewayException {
		return gal.getLQIInformation(aoi);
	}

	@Override
	public LQIInformation getLQIInformation() throws IOException, Exception, GatewayException {
		return gal.getAllLQIInformations();
	}

	@Override
	public void sendZCLCommand(long timeout, ZCLCommand command) throws IOException, Exception, GatewayException {
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

	@Override
	public void sendZDPCommand(long timeout, ZDPCommand command) throws IOException, Exception, GatewayException {
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

	@Override
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

}
