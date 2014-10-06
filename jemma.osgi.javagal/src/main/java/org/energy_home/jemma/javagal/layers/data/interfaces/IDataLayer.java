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
package org.energy_home.jemma.javagal.layers.data.interfaces;

import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
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
import org.energy_home.jemma.javagal.layers.object.Mgmt_LQI_rsp;

/**
 * Data layer interface to be implemented by every vendor (Freescale, Ember and
 * so on...).
 * 
 * @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 
 */
public interface IDataLayer extends IFrameCallback {

	public void initialize();

	/**
	 * Gets the properties manager.
	 * 
	 * @return the properties manager.
	 */
	public PropertiesManager getPropertiesManager();

	/* Serial comm section */
	/**
	 * Gets the actual Data Layer implementation.
	 * 
	 * @return the DataLayer
	 * @see IConnector
	 */
	public IConnector getIKeyInstance();

	/* Zigbee section */
	/**
	 * Sets APSME synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param _AttID
	 *            attribute id.
	 * @param _value
	 *            value to set.
	 * @return the resulting status from ZGD.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Status APSME_SETSync(long timeout, short _AttID, String _value) throws Exception;

	/**
	 * Reads extended Address of the gal.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @return the resulting status from ZGD.
	 * @throws GatewayException
	 *             if an error occurs in the ZGD.
	 * @throws Exception
	 *             if a not ZGD error occurs.
	 */
	public BigInteger readExtAddressGal(long timeout) throws GatewayException, Exception;

	/**
	 * Reads extended Address.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param shortAddress
	 *            the related shortAddress value of the node.
	 * @return the resulting status from ZGD.
	 * @throws GatewayException
	 *             if an error occurs in the ZGD.
	 * @throws Exception
	 *             if a not ZGD error occurs.
	 */
	public BigInteger readExtAddress(long timeout, Integer shortAddress) throws GatewayException, Exception;

	/**
	 * Starts a service discovery function. The StartServiceDiscovery procedure
	 * is invoked by an IPHA to perform a service discovery on the network to
	 * know which services could be offered by the devices in the ZigBee net.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param aoi
	 *            Address of interest.
	 * @return a list of services found.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public List<Short> startServiceDiscoverySync(long timeout, Address aoi) throws Exception;

	/**
	 * Gets APSME synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param _AttID
	 *            attribute id.
	 * @return the APSME.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String APSME_GETSync(long timeout, short _AttID) throws Exception;

	
	
	
	
	/**
	 * Gets MACGetPibAttributes synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param _AttID
	 *            attribute id.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String MacGetPIBAttributeSync(long timeout, short _AttID) throws Exception;

	
	/**
	 * Gets NLME synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param _AttID
	 *            attribute id.
	 * @return the NLME.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public String NMLE_GetSync(long timeout, short _AttID, short iEntry) throws Exception;

	public Status NMLE_SETSync(long timeout, short _AttID, String _value) throws Exception;

	/**
	 * Sends an APS message synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param message
	 *            the message to send.
	 * @return the resulting status from ZGD.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Status sendApsSync(long timeout, APSMessage message) throws Exception;

	/**
	 * Sends an InterPAN message synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param message
	 *            the message to send.
	 * @return the resulting status from ZGD.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public Status sendInterPANMessaSync(long timeout, InterPANMessage message) throws Exception;

	/**
	 * Gets the Node Descriptor for an address of interest.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param addrOfInterest
	 *            the address of interest for which the Node Descriptor is
	 *            required.
	 * @return the Node Descriptor.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeDescriptor getNodeDescriptorSync(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException;

	/**
	 * Gets the channel synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @return the channel.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public short getChannelSync(long timeout) throws IOException, Exception, GatewayException;

	/**
	 * Starts the Gateway Device synchronously. You can control the start
	 * behavior with the {@link StartupAttributeInfo} object.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param sai
	 *            the {@code StartupAttributeInfo} to influence start behavior.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status startGatewayDeviceSync(long timeout, StartupAttributeInfo sai) throws IOException, Exception, GatewayException;

	/**
	 * Stops the network synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @return the resulting status from ZGD.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status stopNetworkSync(long timeout) throws Exception, GatewayException;

	/**
	 * Configures an end point synchronously. Configuration values are carried
	 * by a {@link SimpleDescriptor} object.
	 * <p>
	 * The ConfigureEndpoint procedure is invoked by an IPHA in order to include
	 * an endpoint in the list of active endpoints and to set a simple
	 * descriptor for this endpoint in the ZDO of the ZGD. Consequently, if a
	 * ZigBee node issues a request to discover the active endpoints on the ZGD,
	 * the ZGD will indicate that the list of active endpoints includes the
	 * endpoint supplied in parameter of this procedure. If a ZigBee node issues
	 * a request to discover the simple descriptor supported by this endpoint on
	 * the ZGD, the ZGD will respond with the simple descriptor supplied in
	 * parameter of this procedure. An IPHA normally call such procedure when it
	 * implements a ZigBee application object on behalf of the ZigBee node of
	 * the ZGD so that from a node in the ZigBee network, the ZGD behaves
	 * exactly as if the application object was implemented on this device.
	 * 
	 * 
	 * @param timeout
	 *            the desired timeout value.
	 * @param desc
	 *            the SimpleDesctriptor containing configuration values
	 * @return the endpoint marked as active with the simple descriptor
	 *         supplied.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public short configureEndPointSync(long timeout, SimpleDescriptor desc) throws IOException, Exception, GatewayException;

	/**
	 * Clears an end point synchronously. The ClearEndpoint procedure is invoked
	 * by an IPHA in order to exclude an endpoint in the list of active
	 * endpoints and to remove any simple descriptor for this endpoint in the
	 * ZDO of the ZGD. Consequently, if a ZigBee node issues a request to
	 * discover the active endpoints on the ZGD, the ZGD will not indicate the
	 * endpoint supplied in parameter of this procedure in the list of active
	 * endpoints. If a ZigBee node issues a request to discover the simple
	 * descriptor supported by this endpoint on the ZGD, the ZGD will not report
	 * any simple descriptor supported. An IPHA normally call such procedure
	 * when a previous configuration of the ZDO indicated some services
	 * supported by this endpoint for instance due to a prior call to the
	 * ConfigureEndpoint procedure.
	 * 
	 * @param endpoint
	 *            the end point to clear.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status clearEndpointSync(long timeout,short endpoint) throws IOException, Exception, GatewayException;

	/**
	 * Starts a PermitJoin procedure synchronously. The PermitJoin procedure is
	 * invoked by an IPHA to issue a Mgmt_Permit_Joining_req.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address of interest for the PermitJoin procedure.
	 * @param duration
	 *            the value expressed in seconds to allow nodes to join the
	 *            network.
	 * @param TCSignificance
	 *            the Trust Center significance.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status permitJoinSync(long timeout, Address addrOfInterest, short duration, byte TCSignificance) throws IOException, Exception, GatewayException;

	/**
	 * Starts a PermitJoinAll procedure synchronously.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address of interest for the PermitJoin procedure.
	 * @param duration
	 *            the value expressed in seconds to allow nodes to join the
	 *            network.
	 * @param TCSignificance
	 *            the Trust Center significance.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 */
	public Status permitJoinAllSync(long timeout, Address addrOfInterest, short duration, byte TCSignificance) throws IOException, Exception;

	/**
	 * Starts a Leave procedure synchronously. The Leave procedure is invoked by
	 * an IPHA to issue a Mgmt_Leave_req command.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address of interest for the Leave procedure.
	 * @param mask
	 *            the mask.
	 * @return the resulting status from ZGD.
	 * @throws Exception
	 *             if a general error occurs.
	 */
	public Status leaveSync(long timeout, Address addrOfInterest, int mask) throws Exception;

	/**
	 * Gets a NodeServices object containing all available local services.
	 * 
	 * @return the available local services.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public NodeServices getLocalServices(long timout) throws IOException, Exception, GatewayException;

	/**
	 * Starts the GetServiceDescriptor procedure. The GetServiceDescriptor
	 * procedure is invoked by an IPHA to retrieve the Service Descriptor
	 * related to a specific node (identified by its address) and active
	 * endpoint.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address of interest.
	 * @param endpoint
	 *            the endpoint of interest.
	 * @return the retrieved ServiceDescriptor object.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public ServiceDescriptor getServiceDescriptor(long timeout, Address addrOfInterest, short endpoint) throws IOException, Exception, GatewayException;

	/**
	 * Starts the GetBindingList. The GetBindingList procedure is invoked by an
	 * IPHA in order to retrieve the list of ZigBee device bindings which are
	 * set on the ZGD.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            address of a device which is a destination for the binding.
	 * @param index
	 *            endpoint on the destination device which is a destination for
	 *            the binding.
	 * 
	 * @return the retrieved BindingList.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public BindingList getNodeBindings(long timeout, Address addrOfInterest, short index) throws IOException, Exception, GatewayException;

	/**
	 * Adds a {@link Binding}.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param binding
	 *            the binding to add.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status addBinding(long timeout, Binding binding, Address aoi) throws IOException, Exception, GatewayException;

	/**
	 * Removes a {@link Binding}.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param binding
	 *            the binding to remove.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status removeBinding(long timeout, Binding binding, Address aoi) throws IOException, Exception, GatewayException;

	public Status frequencyAgilitySync(long timeout, short scanChannel, short scanDuration) throws IOException, Exception, GatewayException;

	/**
	 * Tries to reset dongle's hardware.
	 * 
	 * @throws Exception
	 *             if an error occurs.
	 */
	public void cpuReset() throws Exception;

	public Status SetModeSelectSync(long timeout) throws IOException, Exception, GatewayException;

	public Status ClearDeviceKeyPairSet(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException;

	/**
	 * Clears the neighbor table entry.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address on which the neighbor table is to be cleared.
	 * @return the resulting status from ZGD.
	 * @throws IOException
	 *             if an Input Output error occurs.
	 * @throws Exception
	 *             if a general error occurs.
	 * @throws GatewayException
	 *             if a ZGD error occurs.
	 */
	public Status ClearNeighborTableEntry(long timeout, Address addrOfInterest) throws IOException, Exception, GatewayException;

	/**
	 * Starts a ZDP-Mgmt_Lqi.Request procedure synchronously. The Lqi_req
	 * procedure is invoked by an IPHA to issue a ZDP-Mgmt_Lqi.Request.
	 * 
	 * @param timeout
	 *            the desired timeout.
	 * @param addrOfInterest
	 *            the address of interest for the PermitJoin procedure.
	 * @param startindex
	 *            the value of the table index.
	 */
	public Mgmt_LQI_rsp Mgmt_Lqi_Request(long timeout, Address addrOfInterest, short startIndex) throws IOException, Exception, GatewayException;

	void clearBuffer();

	void destroy();

	boolean getDestroy();

}
