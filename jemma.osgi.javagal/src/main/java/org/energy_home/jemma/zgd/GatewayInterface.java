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
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;
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
import java.util.List;

/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface GatewayInterface {
	/**
	 * Registration callback to receive notifications about events
	 * 
	 * @param listener
	 *            to receive notifications
	 */
	void setGatewayEventListener(GatewayEventListener listener);

	/**
	 * return the current channel
	 * 
	 * @param timeout
	 *            to receive response
	 */
	 short getChannelSync(long timeout) throws Exception,
			GatewayException;

	


	

	/**
	 * Retrieves the version and the main informations of the GAL. It can be
	 * used as a way to tell if and when the GAL is running as it does not
	 * affect the status of the GAL and it is a very light command
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Version getVersion() throws IOException, Exception, GatewayException;

	/**
	 * Retrieves a particular attribute of the database InfoBaseAttribute
	 * defined in ZigBee Alliance
	 * 
	 * @param attrId
	 *            the ID of the attribute to retrieve
	 * @return
	 * @throws Exception
	 * @throws Exception
	 * @throws GatewayException
	 */
	String getInfoBaseAttribute(short attrId) throws Exception,
			GatewayException;

	/**
	 * Set a particular attribute of the database InfoBaseAttribute defined in
	 * ZigBee Alliance
	 * 
	 * @param attrId
	 *            the ID of the attribute to retrieve
	 * @param Value
	 *            to set into the attribute
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void setInfoBaseAttribute(short attrId, String value) throws IOException,
			Exception, GatewayException;

	/**
	 * Allows the creation of a callback to receive APS/ZDP/ZCL messages using a
	 * class of filters
	 * 
	 * @param callback
	 * @param listener
	 *            to receive notifications
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	long createCallback(Callback callback, APSMessageListener listener)
			throws IOException, Exception, GatewayException;
	
	
	
	/**
	 * Allows the creation of a callback to receive InterPAN messages using a
	 * class of filters
	 * 
	 * @param callback
	 * @param listener
	 *            to receive notifications
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	long createCallback(Callback callback, MessageListener listener)
			throws IOException, Exception, GatewayException;
	

	/**
	 * Allows the creation of a callback to receive APS messages and specifying
	 * the endPoint on which to listen. In fact represents a more fast version,
	 * compared to the previous function createCallback that acts on all the
	 * endpoints
	 * 
	 * @param endpoint
	 *            on which to listen
	 * @param listener
	 *            to receive notifications
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	long createAPSCallback(short endpoint, APSMessageListener listener)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows the creation of a callback to receive APS messages. In fact
	 * represents a more fast version, compared to the previous function
	 * createCallback that acts on all the endpoints
	 * 
	 * @param listener
	 *            to receive notifications
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	long createAPSCallback(APSMessageListener listener) throws IOException,
			Exception, GatewayException;

	/**
	 * Returns list of all callbacks to which you have previously registered
	 * 
	 * @return List<Long>
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	List<Long> listCallbacks() throws IOException, Exception, GatewayException;

	/**
	 * Returns list of all callbacks to which you have previously registered
	 * 
	 * @return CallbackIdentifierList
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */

	CallbackIdentifierList getlistCallbacks() throws IOException, Exception,
			GatewayException;

	/**
	 * Allows to remove a callback
	 * 
	 * @param callId
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void deleteCallback(long callId) throws IOException, Exception,
			GatewayException;

	/**
	 * Returns the list of associated nodes in the network, and for each node
	 * gives the short and the IEEE Address
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Aliases listAddresses() throws IOException, Exception, GatewayException;

	/**
	 * Allows to configure a set of parameters throught the StartupAttributeInfo
	 * class before to launch the ZigBee network
	 * @param sai the StartupAttributeInfo
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void configureStartupAttributeSet(StartupAttributeInfo sai)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows to read a set of parameters throught the StartupAttributeInfo
	 * class
	 * @param index
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	StartupAttributeInfo readStartupAttributeSet(short index)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows to start/create a ZigBee network using the StartupAttributeInfo
	 * class as parameter previously configured
	 * 
	 * @param timeout
	 * @param sai
	 *            the StartupAttributeInfo
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void startGatewayDevice(long timeout, StartupAttributeInfo sai)
			throws IOException, Exception, GatewayException;

	Status stopNetworkSync(long timeout) throws Exception, GatewayException;

	void stopNetwork(long timeout) throws Exception, GatewayException;

	/**
	 * Allows to start/create a ZigBee network using the StartupAttributeInfo
	 * class as parameter previously configured
	 * 
	 * @param timeout
	 * @param sai
	 *            the StartupAttributeInfo
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows to start/create a ZigBee network using a set of default values
	 * inside the GAL
	 * 
	 * @param timeout
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	void startGatewayDevice(long timeout) throws IOException, Exception,
			GatewayException;

	
	

	/**
	 * Returns the list of active nodes and connected to the ZigBee network from
	 * the cache of the GAL
	 * 
	 * @return WSNNodeList the list of active nodes
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	WSNNodeList readNodeCache() throws IOException, Exception, GatewayException;

	/**
	 * Activation of the discovery procedures of the nodes in a ZigBee network.
	 * Each node will produce a notification by the announcement
	 * 
	 * @param timeout
	 * @param discoveryMask
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */

	void startNodeDiscovery(long timeout, int discoveryMask)
			throws IOException, Exception, GatewayException;

	void subscribeNodeRemoval(long timeout, int freshnessMask)
			throws IOException, Exception, GatewayException;

	/**
	 * Retrieves the local services (the endpoints) on which the GAL is running
	 * and listening
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	NodeServices getLocalServices() throws IOException, Exception,
			GatewayException;

	/**
	 * Returns the list of active endpoints from the cache of the GAL
	 * 
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	NodeServicesList readServicesCache() throws IOException, Exception,
			GatewayException;

	/**
	 * Activation of the discovery procedures of the services (the endpoints) of
	 * a node connected to the ZigBee network
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void startServiceDiscovery(long timeout, Address addrOfInterest)
			throws IOException, Exception, GatewayException;

	/**
	 * Activation of the discovery procedures of the services of a node
	 * connected to the ZigBee network
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	NodeServices startServiceDiscoverySync(long timeout,
			Address addrOfInterest) throws IOException, Exception,
			GatewayException;

	
	/**
	 * Retrieves the informations about the ServiceDescriptor of a specific
	 * endpoint of a ZigBee node
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @param endpoint
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void getServiceDescriptor(long timeout, Address addrOfInterest,
			short endpoint) throws IOException, Exception, GatewayException;

	/**
	 * Retrieves the informations about the ServiceDescriptor of a specific
	 * endpoint of a ZigBee node
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @param endpoint
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	ServiceDescriptor getServiceDescriptorSync(long timeout,
			Address addrOfInterest, short endpoint) throws IOException,
			Exception, GatewayException;

	/**
	 * Retrieves the informations about the NodeDescriptor of a ZigBee node
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void getNodeDescriptor(long timeout, Address addrOfInterest)
			throws IOException, Exception, GatewayException;

	/**
	 * Retrieves the informations about the NodeDescriptor of a ZigBee node
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	NodeDescriptor getNodeDescriptorSync(long timeout, Address addrOfInterest)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows the creation of an endpoint to which is associated a
	 * SimpleDescriptor
	 * 
	 * @param timeout
	 * @param desc
	 *            the SimpleDescriptor
	 * @return
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	short configureEndpoint(long timeout, SimpleDescriptor desc)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows to remove a SimpleDescriptor or an endpoint
	 * 
	 * @param endpoint
	 *            the endpoint to remove
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void clearEndpoint(short endpoint) throws IOException, Exception,
			GatewayException;

	/**
	 * It�s a command to generate the disassociation of all the nodes from the
	 * network ZigBee
	 * 
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void leaveAll() throws IOException, Exception, GatewayException;

	/**
	 * It�s a command to generate the disassociation of all the nodes from the
	 * network ZigBee
	 * 
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status leaveAllSync() throws IOException, Exception, GatewayException;

	/**
	 * It�s a command to generate the disassociation of a node from the network
	 * ZigBee
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	void leave(long timeout, Address addrOfInterest) throws IOException,
			Exception, GatewayException;

	
	/**
	 * It�s a command to generate the disassociation of a node from the network
	 * ZigBee. Mask equals to 0x00 close the network, 0xff leaves the network
	 * open, and any other value leaves the network open for that number of
	 * seconds.
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @param mask
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status leaveSync(long timeout, Address addrOfInterest, int mask)
			throws IOException, Exception, GatewayException;

	/*
	 * It�s a command to generate the disassociation of a node from the network
	 * ZigBee. Mask equals to 0x00 close the network, 0xff leaves the network
	 * open, and any other value leaves the network open for that number of
	 * seconds.
	 * 
	 * @param timeout
	 * 
	 * @param addrOfInterest
	 * 
	 * @param mask
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 * 
	 * @throws GatewayException
	 */
	void leave(long timeout, Address addrOfInterest, int mask)
			throws IOException, Exception, GatewayException;

	/**
	 * This command allows to create a binding of a remote node to a prefefined
	 * destination address node
	 * 
	 * @param timeout
	 * @param binding
	 * @throws IOException
	 * @throws GatewayException
	 */
	void addBinding(long timeout, Binding binding) throws IOException,
			Exception, GatewayException;

	/**
	 * This command allows to create a binding of a remote node to a prefefined
	 * destination address node
	 * 
	 * @param timeout
	 * @param binding
	 * @throws IOException
	 * @throws GatewayException
	 */

	Status addBindingSync(long timeout, Binding binding)
			throws IOException, Exception, GatewayException;

	/**
	 * This command removes a previously created binding of a remote node
	 * 
	 * @param timeout
	 * @param binding
	 * @throws IOException
	 * @throws GatewayException
	 */
	void removeBinding(long timeout, Binding binding) throws IOException,
			Exception, GatewayException;

	/**
	 * This command removes a previously created binding of a remote node
	 * 
	 * @param timeout
	 * @param binding
	 * @throws IOException
	 * @throws GatewayException
	 */
	Status removeBindingSync(long timeout, Binding binding) throws IOException,
			Exception, GatewayException;

	/**
	 * This command request a list of all the bindings stored in a remote node,
	 * starting from index zero
	 * 
	 * @param timeout
	 * @param aoi
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void getNodeBindings(long timeout, Address aoi) throws IOException,
			Exception, GatewayException;

	/**
	 * This command request a list of all the bindings stored in a remote node,
	 * starting from index zero
	 * 
	 * @param timeout
	 * @param aoi
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	BindingList getNodeBindingsSync(long timeout, Address aoi)
			throws IOException, Exception, GatewayException;

	/**
	 * This command request a list of all the bindings stored in a remote node
	 * 
	 * @param timeout
	 * @param aoi
	 * @param index
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void getNodeBindings(long timeout, Address aoi, short index)
			throws IOException, Exception, GatewayException;

	/**
	 * This command request a list of all the bindings stored in a remote node
	 * 
	 * @param timeout
	 * @param aoi
	 * @param index
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */

	BindingList getNodeBindingsSync(long timeout, Address aoi,
			short index) throws IOException, Exception, GatewayException;

	/**
	 * Allows the opening of the ZigBee network to all nodes, and for a
	 * specified duration, to be able to associate new nodes
	 * 
	 * @param timeout
	 * @param duration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void permitJoinAll(long timeout, short duration) throws IOException,
			Exception, GatewayException;
	
	/**
	 * Allows the opening of the ZigBee network for all nodes, and for a
	 * specified duration, to be able to associate new nodes
	 * 
	 * @param timeout
	 
	 * @param duration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status permitJoinAllSync(long timeout, short duration)
			throws IOException, Exception, GatewayException;


	/**
	 * Allows the opening of the ZigBee network to a single node, and for a
	 * specified duration, to be able to associate new nodes
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @param duration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void permitJoin(long timeout, Address addrOfInterest, short duration)
			throws IOException, Exception, GatewayException;

	/**
	 * Allows the opening of the ZigBee network to a single node, and for a
	 * specified duration, to be able to associate new nodes
	 * 
	 * @param timeout
	 * @param addrOfInterest
	 * @param duration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status permitJoinSync(long timeout, Address addrOfInterest, short duration)
			throws IOException, Exception, GatewayException;

	/**
	 * Sends an APS message to a node in blocking mode
	 * 
	 * @param message
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	@Deprecated
	void sendAPSMessage(APSMessage message) throws IOException, Exception,
			GatewayException;

	/**
	 * Sends an APS message to a node in an asynchronous mode
	 * 
	 * @param timeout
	 * @param message
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void sendAPSMessage(long timeout, APSMessage message) throws IOException,
			Exception, GatewayException;
	
	
	/**
	 * Sends an InterPAN message to a node in an asynchronous mode
	 * 
	 * @param timeout
	 * @param message
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void sendInterPANMessage(long timeout, InterPANMessage message) throws IOException,
			Exception, GatewayException;
	

	/**
	 * Resets the GAl with the ability to set whether to delete the
	 * NonVolatileMemory to the next reboot
	 * 
	 * @param timeout
	 * @param mode
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void resetDongle(long timeout, short mode) throws IOException, Exception,
			GatewayException;

	/**
	 * Resets the GAl with the ability to set whether to delete the
	 * NonVolatileMemory to the next reboot
	 * 
	 * @param timeout
	 * @param mode
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status resetDongleSync(long timeout, short mode) throws IOException,
			Exception, GatewayException;

	/**
	 * Frequency Agility
	 * 
	 * @param timeout
	 * @param aoi
	 * @param scanChannel
	 * @param scanDuration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	Status frequencyAgilitySync(long timeout, short scanChannel,
			short scanDuration) throws IOException, Exception, GatewayException;

	/**
	 * Frequency Agility
	 * 
	 * @param timeout
	 * @param aoi
	 * @param scanChannel
	 * @param scanDuration
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void frequencyAgility(long timeout, short scanChannel, short scanDuration)
			throws IOException, Exception, GatewayException;

	/**
	 * Returns the list of neighbor of related nodes of the network
	 * 
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	LQIInformation getLQIInformation(Address aoi) throws IOException,
			Exception, GatewayException;

	
	/**
	 * Returns the list of neighbor of all nodes of the network
	 * 
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	 LQIInformation getLQIInformation() throws IOException,
			Exception, GatewayException;

	
	
	/**
	 * Sends a ZCL Command to a node in an asynchronous mode
	 * 
	 * @param timeout
	 * @param ZCLCommand
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void sendZCLCommand(long timeout, ZCLCommand command) throws IOException,
			Exception, GatewayException;

	/**
	 * Sends a ZDP Command to a node in an asynchronous mode
	 * 
	 * @param timeout
	 * @param ZDPCommand
	 * @throws IOException
	 * @throws Exception
	 * @throws GatewayException
	 */
	void sendZDPCommand(long timeout, ZDPCommand command) throws IOException,
			Exception, GatewayException;

	
}
