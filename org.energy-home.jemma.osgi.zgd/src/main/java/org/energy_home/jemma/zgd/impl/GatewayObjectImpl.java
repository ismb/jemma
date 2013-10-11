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
package org.energy_home.jemma.zgd.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.GatewayProperties;
import org.energy_home.jemma.zgd.ResourcePathURIs;
import org.energy_home.jemma.zgd.Trace;
import org.energy_home.jemma.zgd.jaxb.APSMessage;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Aliases;
import org.energy_home.jemma.zgd.jaxb.Binding;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.JoiningInfo;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.NodeServicesList;
import org.energy_home.jemma.zgd.jaxb.ObjectFactory;
import org.energy_home.jemma.zgd.jaxb.SimpleDescriptor;
import org.energy_home.jemma.zgd.jaxb.StartupAttributeInfo;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Version;
import org.energy_home.jemma.zgd.jaxb.WSNNodeList;
import org.restlet.VirtualHost;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;


class GatewayObjectImpl implements GatewayInterface, ResourcePathURIs, EventPathURIs {
	private JaxbConverter jaxbConverter;
	//private Client restClient;
	private ConcurrencyRestClient restClient;
	private ObjectFactory jaxbFactory;
	private RestletEventListener restlet;
	private String localHost;
	private String gatewayRootURI;
	private String networkRootURI;

	
	GatewayObjectImpl(GatewayFactoryImpl f) throws JAXBException, IOException {
		restlet = f.getRestlet();
		jaxbConverter = f.createConverter();
		//restClient = f.getClient();
		restClient = new ConcurrencyRestClient(f.getClient());
		jaxbFactory = new ObjectFactory();
		gatewayRootURI = f.getProperties().getProperty(GatewayProperties.GATEWAY_ROOT_URI);
		if (gatewayRootURI.endsWith("/")) gatewayRootURI = gatewayRootURI.substring(0, gatewayRootURI.length() -1);
		networkRootURI = gatewayRootURI + f.getProperties().getProperty(GatewayProperties.NETWORK_RESOURCES_URI);

		localHost = getLocalAddress(f.getProperties()) + ':' + f.getProperties().getProperty(GatewayProperties.LOCAL_PORT);
		Trace.println("local address: " + localHost);
	}


	
	public void setGatewayEventListener(GatewayEventListener listener) {
		restlet.setGatewayEventListener(listener);
	}
	
	
	public Version getVersion() throws IOException, JAXBException, GatewayException {
		// Get it using the HTTP client connector
		Response response = restClient.get(gatewayRootURI + VERSION);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getVersion();
	}


	
	public String getInfoBaseAttribute(short attrId) throws IOException, JAXBException, GatewayException {
		if (attrId < 0) throw new IllegalArgumentException("Negative number not allowed.");
		// there should be an enum to map attrID to meaningful names
		// also disambiguate here between GW infobase and NW infobase
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(INFOBASE);
		appendPaddedNumber(sb, attrId);
		
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		List<String> list = info.getDetail().getValue();
		if (list.isEmpty()) throw new NullPointerException("Returned no value.");
		return list.get(0);
	}
	

	public void setInfoBaseAttribute(short attrId, String value) throws IOException, JAXBException, GatewayException {
		if (attrId < 0) throw new IllegalArgumentException("Negative number not allowed.");
		// there should be an enum to map attrID to meaningful names
		// also disambiguate here between GW infobase and NW infobase
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(INFOBASE);
		appendPaddedNumber(sb, attrId);
		
		Trace.println(sb.toString());
		
		Representation rep = jaxbConverter.toRepresentation(jaxbFactory.createValue(value));
		Response response = restClient.put(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}

	/*
	 * DecodeSpecification Element: If the DecodeZDPBit is set and the ZigBee
	 * frame is a valid APS frame containing source and destination endpoints
	 * that are both zero then it shall be decoded as a NotifyZDPEvent. If the
	 * DecodeZCLBit is set and the ZigBee frame is a valid APS frame and the APS
	 * payload length is greater than or equal to the minimum size of the ZCL
	 * Header (3 octets) then it shall be decoded as a NotifyZCLEvent. If the
	 * DecodeAPSBit is set and the ZigBee frame is a valid APS frame then it
	 * shall be decoded as a NotifyAPSEvent.
	 */
	public long createCallback(Callback callback, APSMessageListener listener) throws IOException, JAXBException, GatewayException {
		callback.getAction().setForwardingSpecification(localHost + APS_NOTIFY_EVENT);
		Representation rep = jaxbConverter.toRepresentation(callback);

		// Handle it using an HTTP client connector
		Response response = restClient.post(networkRootURI + CALLBACKS, rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		Long cid = info.getDetail().getCallbackIdentifier();
		//if (cid == null) throw new NullPointerException("Returned no Callback Identifier.");
		restlet.addAPSMEssageListener(cid, listener);
		
		return info.getDetail().getCallbackIdentifier();
	}
		
	
	public long createAPSCallback(APSMessageListener listener) throws IOException, JAXBException, GatewayException {
		return createAPSCallback((short)-1, listener);
	}
	
	
	public long createAPSCallback(short endpoint, APSMessageListener listener) throws IOException, JAXBException, GatewayException {
		if (endpoint < -1) throw new IllegalArgumentException("Negative number not allowed.");
/*		Filter filter = new Filter();
		filter.setLevelSpecification(new Filter.LevelSpecification());
		filter.getLevelSpecification().getLevel().add(Level.APS_LEVEL);
		Filter.AddressSpecification address = new Filter.AddressSpecification();
		address.setAPSDestinationEndpoint((short)endpoint);
		filter.getAddressSpecification().add(address);
		
		Action action = new Action();
		action.setDecodeSpecification(new Action.DecodeSpecification());
		action.getDecodeSpecification().getDecodeLevel().add(DecodeLevel.DECODE_APS);
		action.setForwardingSpecification(localHost + APS_NOTIFY_EVENT);
		
		Callback callback = new Callback();
		callback.setFilter(filter);
		callback.setAction(action);
		
		Representation rep = jaxbConverter.toRepresentation(callback);
		if (true) return createCallback(callback, listener);*/
		
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		if (endpoint == -1) {
			sb.append(LOCALNODE_ALLSERVICES_WSNCONNECTION);
		} else {
    		sb.append(LOCALNODE_SERVICES).append('/');
    		appendPaddedNumber(sb, endpoint);
    		sb.append(WSNCONNECTION);
		}
		sb.append('?').append(URILISTENER_PARAM);
		sb.append(localHost).append(APS_NOTIFY_EVENT);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.post(sb.toString(), new StringRepresentation(" "));
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		Long cid = info.getDetail().getCallbackIdentifier();
		//if (cid == null) throw new NullPointerException("Returned no Callback Identifier.");
		restlet.addAPSMEssageListener(cid, listener);
		return cid;
	}

	
	
	public List<Long> listCallbacks() throws IOException, JAXBException, GatewayException {
		// Prepare the request
		Response response = restClient.get(networkRootURI + CALLBACKS);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getCallbacks().getCallbackIdentifier();
	}
	
	
	public void deleteCallback(long cid) throws IOException, JAXBException, GatewayException {
		if (cid < 0) throw new IllegalArgumentException("Negative number not allowed.");
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(CALLBACKS).append('/');
		appendPaddedNumber(sb, cid);

		Trace.println(sb.toString());

		restlet.removeAPSMesssageListener(cid);
		Response response = restClient.delete(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	
	public Aliases listAddresses() throws IOException, JAXBException, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(ALIASES);
		
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getAliases();
	}
	
	
	public void configureStartupAttributeSet(StartupAttributeInfo sai) throws IOException, JAXBException, GatewayException {
		Representation rep = jaxbConverter.toRepresentation(sai);
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(gatewayRootURI);
		sb.append(STARTUP).append("?start=false");
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	
	public StartupAttributeInfo readStartupAttributeSet(short index) throws IOException, JAXBException, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(gatewayRootURI);
		sb.append(STARTUP).append('?');
		sb.append(INDEX_PARAM);
		appendPaddedNumber(sb, index);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getStartupAttributeInfo();
	}

	
	public void startGatewayDevice(long timeout) throws IOException, JAXBException, GatewayException {
		StartupAttributeInfo sai = new StartupAttributeInfo();
		sai.setStartupAttributeSetIndex((short)0);
		startGatewayDevice(timeout, sai);
	}

	
	public void startGatewayDevice(long timeout, StartupAttributeInfo sai) throws IOException, JAXBException, GatewayException {
		Representation rep = jaxbConverter.toRepresentation(sai);
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(gatewayRootURI);
		sb.append(STARTUP);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(STARTUP_RESPONSE);
		sb.append("&start=true");
		
		Trace.println(sb.toString());

		// Handle it using an HTTP client connector
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}

	
	public void startNodeDiscovery(long timeout, int discoveryMask) throws IOException, JAXBException, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(NODE_DISCOVERED);

		if ((discoveryMask & GatewayConstants.DISCOVERY_INQUIRY) > 0) {
			sb.append('&').append(DISCOVERY_INQUIRY);
		}
		if ((discoveryMask & GatewayConstants.DISCOVERY_ANNOUNCEMENTS) > 0) {
			sb.append('&').append(DISCOVERY_ANNOUNCEMENTS);
		}
		/*
		if ((discoveryMask & GatewayConstants.DISCOVERY_LEAVE) > 0) {
			sb.append('&').append(DISCOVERY_LEAVE);
		}
		*/
		if ((discoveryMask & GatewayConstants.DISCOVERY_LQI) > 0) {
			sb.append('&').append(DISCOVERY_LQI);
		}
		
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	public void subscribeNodeRemoval(long timeout, int discoveryMask) throws IOException, Exception, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(NODE_REMOVED);

		if ((discoveryMask & GatewayConstants.DISCOVERY_LEAVE) > 0) {
			sb.append('&').append(DISCOVERY_LEAVE);
		}
		if ((discoveryMask & GatewayConstants.DISCOVERY_FRESHNESS) > 0) {
			sb.append('&').append(DISCOVERY_FRESHNESS);
		}
		
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	public NodeServices getLocalServices() throws IOException, JAXBException, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI).append(LOCALNODE_SERVICES);
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getNodeServices();
	}

	public void startServiceDiscovery(long timeout, Address aoi) throws IOException, JAXBException, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		Integer shortAddress = aoi.getNetworkAddress();
		if (shortAddress!= null && shortAddress == GatewayConstants.BROADCAST_ADDRESS) {
			sb.append(ALLWSNNODES_SERVICES);
		} else {
			sb.append(WSNNODES).append('/');
			if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
			else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
			else appendPaddedNumber(sb, shortAddress);
			sb.append(SERVICES);
		}
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(SERVICES_DISCOVERED);

		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	
	
	public void getServiceDescriptor(long timeout, Address aoi, short endpoint) throws IOException, JAXBException, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('/');
		if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
		else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
		else appendPaddedNumber(sb, aoi.getNetworkAddress());
		sb.append(SERVICES).append('/');
		appendPaddedNumber(sb, endpoint);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(SERVICE_DESCRIPTOR);

		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	public void getNodeDescriptor(long timeout, Address aoi) throws IOException, Exception, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('/');
		if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
		else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
		else appendPaddedNumber(sb, aoi.getNetworkAddress());
		sb.append(NODEDESCRIPTOR);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(NODE_DESCRIPTOR);

		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}

	
	
	public WSNNodeList readNodeCache() throws IOException, JAXBException, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('&').append(MODE_CACHE);

		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getWSNNodes();
	}
	
	
	public NodeServicesList readServicesCache() throws IOException, Exception, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(ALLWSNNODES_SERVICES).append('&').append(MODE_CACHE);
		
		Trace.println(sb.toString());
		
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getNodeServicesList();
	}
	
	/*
	//
	public void sendZDPCommand(long timeout, ZDPCommand command) throws IOException, JAXBException, GatewayException {
		Representation rep = jaxbConverter.toRepresentation(command);
		
		// Prepare the request.
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('/');
		Address aoi = command.getDestination();
		if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
		else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
		else appendPaddedNumber(sb, aoi.getNetworkAddress());
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(ZDP_NOTIFY_EVENT);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		//return info.getDetail().getZDPMessage();
	}
	*/
	
	public short configureEndpoint(long timeout, SimpleDescriptor desc) throws IOException, JAXBException, GatewayException {
		if (desc == null) throw new IllegalArgumentException("SimpleDescriptor cannot be null.");
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(LOCALNODE_SERVICES).append('?');
		sb.append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Representation rep = jaxbConverter.toRepresentation(desc);
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		return info.getDetail().getEndpoint();
	}
	
	
	public void clearEndpoint(short endpoint) throws IOException, JAXBException, GatewayException {
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(LOCALNODE_SERVICES).append('/');
		appendPaddedNumber(sb, endpoint);

		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.delete(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	
	public void addBinding(long timeout, Binding binding) throws IOException, JAXBException, GatewayException {
		/*
		Binding b = new Binding();
		b.setClusterID(1234);
		b.setSourceEndpoint((short)12);
		b.setSourceIEEEAddress(BigInteger.valueOf(1234567890));
		Device dest = new Device();
		dest.setAddress(BigInteger.valueOf(987654321));
		dest.setEndpoint((short)14);
		b.getDeviceDestination().add(dest);
		*/
		internalBinding(timeout, binding, false);
	}
	
	public void removeBinding(long timeout, Binding binding) throws IOException, JAXBException, GatewayException {
		internalBinding(timeout, binding, true);
	}

	// best effort?!
	private void internalBinding(long timeout, Binding binding, boolean isDelete) throws IOException, JAXBException, GatewayException {
		if (binding == null) throw new IllegalArgumentException("Binding cannot be null.");
		if (binding.getDeviceDestination() == null || binding.getDeviceDestination().size() != 1)
			throw new IllegalArgumentException("DeviceDestination must contain exaclty one element.");
		
		// sanity check remove any group if present
		binding.getGroupDestination().clear();
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('/');
		appendPaddedNumber(sb, binding.getSourceIEEEAddress());
		sb.append(isDelete ? UNBINDINGS : BINDINGS);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(isDelete ? NODE_UNBINDING_RESPONSE : NODE_BINDING_RESPONSE);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Representation rep = jaxbConverter.toRepresentation(binding);
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}

	public void getNodeBindings(long timeout, Address aoi) throws IOException, JAXBException, GatewayException {
		getNodeBindings(timeout, aoi, (short)0);
	}
	public void getNodeBindings(long timeout, Address aoi, short index) throws IOException, JAXBException, GatewayException {
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(WSNNODES).append('/');
		if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
		else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
		else appendPaddedNumber(sb, aoi.getNetworkAddress());
		sb.append(BINDINGS).append('?');
		sb.append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		if (index > 0) {
			sb.append('&').append(INDEX_PARAM);
			appendPaddedNumber(sb, index);
		}
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(NODE_BINDING_LIST_RESPONSE);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}

	
	public void leaveAll() throws IOException, JAXBException, GatewayException {
		Address address = new Address();
		address.setNetworkAddress(GatewayConstants.ROUTER_BROADCAST_ADDRESS);
		leave(GatewayConstants.INFINITE_TIMEOUT, address, 0);
	}
	
	public void leave(long timeout, Address aoi) throws IOException, JAXBException, GatewayException {
		leave(timeout, aoi, 0);
	}
	
	public void leave(long timeout, Address aoi, int mask) throws IOException, JAXBException, GatewayException {

		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		Integer shortAddress = aoi.getNetworkAddress();
		if (shortAddress != null && shortAddress.intValue() >= GatewayConstants.ROUTER_BROADCAST_ADDRESS) {
			sb.append(ALLWSNNODES);
		} else {
			sb.append(WSNNODES).append('/');
			if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
			else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
			else appendPaddedNumber(sb, shortAddress);
		}
		
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(LEAVE_RESPONSE);
		if ((mask & GatewayConstants.LEAVE_REMOVE_CHILDERN) > 0) sb.append('&').append(REMOVE_CHILDREN);
		if ((mask & GatewayConstants.LEAVE_REJOIN) > 0) sb.append('&').append(REJOIN);

		Trace.println(sb.toString());
		
		Response response = restClient.delete(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	
	
	public void permitJoinAll(long timeout, short duration) throws IOException, JAXBException, GatewayException {
		Address address = new Address();
		address.setNetworkAddress(GatewayConstants.ROUTER_BROADCAST_ADDRESS);
		permitJoin(timeout, address, duration);
	}
	
	public void permitJoin(long timeout, Address aoi, short duration) throws IOException, JAXBException, GatewayException {
		
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		StringBuilder sb = new StringBuilder(networkRootURI);
		Integer shortAddress = aoi.getNetworkAddress();
		if (shortAddress != null && shortAddress.intValue() >= GatewayConstants.ROUTER_BROADCAST_ADDRESS) {
			sb.append(ALLPERMIT_JOIN);
		} else {
			sb.append(WSNNODES).append('/');
			if (aoi.getIeeeAddress() != null) appendPaddedNumber(sb, aoi.getIeeeAddress());
			else if (aoi.getAliasAddress() != null) sb.append(aoi.getAliasAddress());
			else appendPaddedNumber(sb, shortAddress);
			sb.append(PERMIT_JOIN);
		}
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		sb.append('&').append(URILISTENER_PARAM);
		sb.append(localHost).append(PERMITJOIN_RESPONSE);

		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		JoiningInfo join = new JoiningInfo();
		join.setTCSignificance(false);
		join.setPermitDuration(duration);
		Representation rep = jaxbConverter.toRepresentation(join);
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	public void sendAPSMessage(APSMessage message) throws IOException, JAXBException, GatewayException {
		sendAPSMessage(0, message);
	}
	
	public void sendAPSMessage(long timeout, APSMessage message) throws IOException, JAXBException, GatewayException {
		if (message == null) throw new IllegalArgumentException("APSMessage cannot be null.");
		if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
		
		// Prepare the request
		short ep = message.getSourceEndpoint();
		StringBuilder sb = new StringBuilder(networkRootURI);
		sb.append(LOCALNODE_SERVICES).append('/');
		appendPaddedNumber(sb, ep);
		sb.append(SEND_APSMESSAGE);
		sb.append('?').append(TIMEOUT_PARAM);
		appendPaddedNumber(sb, timeout);
		
		Trace.println(sb.toString());
		
		// Handle it using an HTTP client connector
		Representation rep = jaxbConverter.toRepresentation(message);
		Response response = restClient.post(sb.toString(), rep);
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
		info.getDetail().getAPSMessageResult();
	}
	
	public void resetDongle(long timeout, short mode) throws IOException, Exception, GatewayException {

		if (mode < 0 || mode > 2) throw new IllegalArgumentException("Unsupported reset mode.");
    	if (timeout == 0) timeout = GatewayConstants.INFINITE_TIMEOUT;
    	
    	// Prepare the request
    	StringBuilder sb = new StringBuilder(gatewayRootURI);
    	sb.append(RESET);
    	sb.append('?').append(TIMEOUT_PARAM);
    	appendPaddedNumber(sb, timeout);
    	sb.append('&').append(URILISTENER_PARAM);
    	sb.append(localHost).append(RESET_RESPONSE);
    	sb.append('&').append(RESET_START_MODE);
    	appendPaddedNumber(sb, mode);
   	
    	Trace.println(sb.toString());

		Response response = restClient.get(sb.toString());
		Info info = jaxbConverter.getInfo(response);
		checkStatus(info.getStatus());
	}
	
	private void checkStatus(Status s) throws GatewayException {
		if (s.getCode() == GatewayConstants.SUCCESS) return;
		StringBuilder sb = new StringBuilder();
		sb.append(s.getCode());
		if (s.getMessage() != null) {
			sb.append(" - ").append(s.getMessage());
		}
		throw new GatewayException(sb.toString());
	}
	
	private void checkStatus(Info info) throws GatewayException {
		Status s = info.getStatus();
		if (info.getNWKStatus() == null && s.getCode() == GatewayConstants.SUCCESS) return;
		StringBuilder sb = new StringBuilder();
		if (s.getCode() != GatewayConstants.SUCCESS) {
    		sb.append(s.getCode());
    		if (s.getMessage() != null) {
    			sb.append(" - ").append(s.getMessage());
    		}
		} else {
			sb.append(info.getNWKStatus());
			sb.append(" - Zegbee Network Error.");
		}
		throw new GatewayException(sb.toString());
	}
	

	private String getLocalAddress(GatewayProperties p) {
		BufferedReader buffer = null;
		try {
			if (p.getProperty(GatewayProperties.USE_PUBLIC_ADDRESS_RESOLUTION).equalsIgnoreCase("true")) {
				URL url = new URL(p.getProperty(GatewayProperties.PUBLIC_ADDRESS_RESOLUTION));
				buffer = new BufferedReader(new InputStreamReader(url.openStream()));
			    return buffer.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) try { buffer.close(); } catch (IOException e) {}
		}

		String ip = p.getProperty(GatewayProperties.LOCAL_ADDRESS);
		if (ip != null && !ip.equals("")) return ip;

		return VirtualHost.getLocalHostAddress();
	}
	
	
	private void appendPaddedNumber(StringBuilder sb, short num) {
		appendPaddedNumber(sb, Integer.toHexString(num), 2);
	}
	
	private void appendPaddedNumber(StringBuilder sb, int num) {
		appendPaddedNumber(sb, Integer.toHexString(num), 4);
	}
	
	private void appendPaddedNumber(StringBuilder sb, long num) {
		appendPaddedNumber(sb, Long.toHexString(num), 8);
	}
	
	private void appendPaddedNumber(StringBuilder sb, BigInteger bi) {
		appendPaddedNumber(sb, bi.toString(16), 16);
	}
	
	private void appendPaddedNumber(StringBuilder sb, String num, int padLen) {
		for (int i = padLen - num.length(); --i >= 0; sb.append('0'));
		sb.append(num);
	}
}
