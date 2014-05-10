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

import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.ZCLMessage;
import org.energy_home.jemma.zgd.jaxb.ZDPMessage;

/**
 * Dispatches Gateway Device Events to registered listeners. This class is
 * called by the Data Layer to notify Device Events when they happens.
 * <p>
 * The {@link GalController} maintains a collection of registered event's
 * listeners. All possible events are those provided by
 * {@link GatewayEventListener}.
 * <p>
 * When an event happens, the Gal controller sends it to the relevant notifier
 * method, one of those present in this class.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface IGatewayEventManager {

	/**
	 * Called to notify Gateway Start Result's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyGatewayStartResult(Status status);

	/**
	 * Called to notify Gateway Start Result's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifyGatewayStartResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify Service Discovered's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param nodeServices
	 *            the discovered services
	 */
	public void notifyServicesDiscovered(int _requestIdentifier, Status status,
			NodeServices nodeServices);

	/**
	 * Called to notify Gateway Stop Result's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyGatewayStopResult(Status status);

	/**
	 * Called to notify Gateway Stop Result's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifyGatewayStopResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify Permit Join Result's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifypermitJoinResult(Status status);

	/**
	 * Called to notify Permit Join Result's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifypermitJoinResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify Reset Result's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyResetResult(Status status) throws Exception;

	/**
	 * Called to notify Reset Result's Event to the correct registered listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifyResetResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify Node Descriptor's Event to all relevant registered
	 * listeners.
	 * 
	 * @param _node
	 *            the Node Descriptor to notify.
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyNodeDescriptor(Status _status, NodeDescriptor _node);

	/**
	 * Called to notify Node Descriptor's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param _node
	 *            the Node Descriptor to notify.
	 */
	public void notifyNodeDescriptor(int _requestIdentifier, Status _status,
			NodeDescriptor _node);

	/**
	 * Called to notify Node Descriptor Extended's Event to all relevant
	 * registered listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 * @param _node
	 *            the Node Descriptor to notify.
	 * @param _addressOfInterest
	 *            the Address of Interest to notify.
	 */
	public void notifyNodeDescriptorExtended(Status _status,
			NodeDescriptor _node, Address _addressOfInterest);

	/**
	 * Called to notify Node Descriptor Extended's Event to the correct
	 * registered listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param _node
	 *            the Node Descriptor to notify.
	 * @param _addressOfInterest
	 *            the Address of Interest to notify.
	 */
	public void notifyNodeDescriptorExtended(int _requestIdentifier,
			Status _status, NodeDescriptor _node, Address _addressOfInterest);

	/**
	 * Called to notify WSNNode Discovered's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 * @param _node
	 *            the discovered WSNNode's descriptor to notify.
	 */
	public void nodeDiscovered(Status _status, WSNNode _node) throws Exception;

	/**
	 * Called to notify WSNNode Removed's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 * @param _node
	 *            the discovered WSNNode's descriptor to notify.
	 */
	public void nodeRemoved(Status _status, WSNNode _node) throws Exception;

	/**
	 * Called to notify Leave Result's Event to the correct registered listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifyleaveResult(int _requestIdentifier, Status _status);

	/**
	 * Called to notify Leave Result's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyleaveResult(Status _status);

	/**
	 * Called to notify Leave Result Extended's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param address
	 *            the leaving address to notify to the listener.
	 */
	public void notifyleaveResultExtended(int _requestIdentifier,
			Status _status, Address _address);

	/**
	 * Called to notify Leave Result Extended's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 * @param address
	 *            the leaving address to notify to the listeners.
	 */
	public void notifyleaveResultExtended(Status _status, Address _address);

	/**
	 * Called to notify Service Descriptor Retrieved's Event to the correct
	 * registered listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param service
	 *            the service descriptor to notify to the listener.
	 */
	public void notifyserviceDescriptorRetrieved(int _requestIdentifier,
			Status status, ServiceDescriptor service);

	/**
	 * Called to notify Node Bindings Retrieved's Event to the correct
	 * registered listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 * @param bindings
	 *            the node binding retrieved to notify to the listener.
	 */
	public void notifynodeBindingsRetrieved(int _requestIdentifier,
			Status status, BindingList bindings);

	/**
	 * Called to notify Node Binding Result's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifybindingResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify Unbinding Result's Event to the correct registered
	 * listener.
	 * 
	 * @param _requestIdentifier
	 *            identifies the target proxy to notify.
	 * @param status
	 *            the status to notify to the listener.
	 */
	public void notifyUnbindingResult(int _requestIdentifier, Status status);

	/**
	 * Called to notify the received ZDPCommand to all relevant registered
	 * listeners.
	 * 
	 * @param message
	 *            the ZDP message command to notify to the listeners.
	 */
	public void notifyZDPCommand(ZDPMessage message);

	
	/**
	 * Called to notify the received ZDPCommand to all relevant registered
	 * listeners.
	 * 
	 * @param message
	 *            the ZDP message command to notify to the listeners.
	 */
	public void notifyInterPANMessageEvent(InterPANMessageEvent message);
	
	
	/**
	 * Called to notify the received ZCLCommand to all relevant registered
	 * listeners.
	 * 
	 * @param message
	 *            the ZCL message command to notify to the listeners.
	 */
	public void notifyZCLCommand(ZCLMessage message);

	/**
	 * Called to notify Frequency Agility's Event to all relevant registered
	 * listeners.
	 * 
	 * @param status
	 *            the status to notify to the listeners.
	 */
	public void notifyFrequencyAgility(Status _status);

}