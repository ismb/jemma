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
package org.energy_home.jemma.javagal.rest;

import org.energy_home.jemma.javagal.rest.util.ThreadPoolManager;
import org.energy_home.jemma.zgd.GatewayEventListenerExtended;
import org.energy_home.jemma.zgd.jaxb.*;
import org.restlet.Context;
/**
 * Implementation of {@code GatewayEventListenerExtended} for the Rest server.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class RestClientManagerAndListener implements
		GatewayEventListenerExtended {
	private String bindingDestination;
	private String gatewayStopDestination;
	private String leaveResultDestination;
	private String nodeBindingDestination;
	private String nodeDescriptorDestination;
	private String nodeDiscoveredDestination;
	private String nodeRemovedDestination;
	private String nodeServicesDestination;
	private String permitJoinDestination;
	private String resetDestination;
	private String serviceDescriptorDestination;
	private String startGatewayDestination;
	private String unbindingDestination;
	private String zclCommandDestination;
	private String zdpCommandDestination;
    private String frequencyAgilityResultDestination;
	private final Context context;
    private PropertiesManager _PropertiesManager;
    private ThreadPoolManager threadPoolManager;
	public RestClientManagerAndListener(PropertiesManager ___PropertiesManager) {
		_PropertiesManager = ___PropertiesManager;
        this.context =  new Context();
		context.getParameters().add("socketTimeout", ((Integer)(_PropertiesManager.getHttpOptTimeout()*1000)).toString());
        threadPoolManager = ThreadPoolManager.getInstance();
	}

	synchronized public void gatewayStartResult(final Status status) {

		if ((startGatewayDestination != null) && !startGatewayDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, startGatewayDestination);
	}

	public void nodeDiscovered(final Status status, final WSNNode node) {

		if ((nodeDiscoveredDestination != null) && !nodeDiscoveredDestination.equals(""))
            threadPoolManager.nodeDiscovered(status, context, node, nodeDiscoveredDestination);

	}

	public void nodeRemoved(final Status status, final WSNNode node) {

		if ((nodeRemovedDestination != null) && !nodeRemovedDestination.equals(""))
		         //It's used nodeDiscovered beacuse nodeDiscovered & nodeRemoved has the same method
            threadPoolManager.nodeDiscovered(status, context, node, nodeRemovedDestination);

	}

	public void servicesDiscovered(final Status status, final NodeServices services) {

		if ((nodeServicesDestination != null) && !nodeServicesDestination.equals(""))
            threadPoolManager.servicesDiscovered(status, context, services, nodeServicesDestination);

	}

	public void serviceDescriptorRetrieved(final Status status, final ServiceDescriptor service) {

		if ((serviceDescriptorDestination != null) && !serviceDescriptorDestination.equals(""))
            threadPoolManager.serviceDescriptorRetrieved(status, context, service, serviceDescriptorDestination);

	}

	public void dongleResetResult(final Status status) {

		if ((resetDestination != null) && !resetDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, resetDestination);
	}

	public void bindingResult(final Status status) {

		if ((bindingDestination != null) && !bindingDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, bindingDestination);

	}

	public void unbindingResult(final Status status) {

		if ((unbindingDestination != null) && !unbindingDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, unbindingDestination);

	}

	public void nodeBindingsRetrieved(final Status status, final BindingList bindings) {

		if ((nodeBindingDestination != null) && !nodeBindingDestination.equals(""))
			threadPoolManager.nodeBindingsRetrieved(status, context, bindings, nodeBindingDestination);
	}

	public void leaveResult(final Status status) {

		if ((leaveResultDestination != null) && !leaveResultDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, leaveResultDestination);

	}

	public void permitJoinResult(final Status status) {

		if ((permitJoinDestination != null) && !permitJoinDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, permitJoinDestination);

	}

	public void nodeDescriptorRetrievedExtended(final Status status,
			final NodeDescriptor node, final Address addressOfInteres) {

		if ((nodeDescriptorDestination != null)
				&& !nodeDescriptorDestination.equals(""))
            threadPoolManager.nodeDescriptorRetrievedExtended(status, context, addressOfInteres, node, nodeDescriptorDestination);

	}

	@Deprecated
	public void nodeDescriptorRetrieved(final Status status, final NodeDescriptor node) {
		if ((nodeDescriptorDestination != null) && !nodeDescriptorDestination.equals(""))
            threadPoolManager.nodeDescriptorRetrieved(status, context, node, nodeDescriptorDestination);

	}

	public void gatewayStopResult(final Status status) {

		if ((gatewayStopDestination != null)
				&& !gatewayStopDestination.equals(""))
            threadPoolManager.gatewayStartResult(status, context, gatewayStopDestination);

	}

	public void leaveResultExtended(final Status status,
			final Address addressOfInteres) {

		if ((leaveResultDestination != null)
				&& !leaveResultDestination.equals(""))
            threadPoolManager.leaveResultExtended(status, context, addressOfInteres, leaveResultDestination);

	}

	public void notifyZDPCommand(final ZDPMessage message) {

		if ((zdpCommandDestination != null)
				&& !zdpCommandDestination.equals(""))
            threadPoolManager.notifyZDPCommand(context, message, zdpCommandDestination);

	}


    public void notifyZCLCommand(final ZCLMessage message) {

		if ((zclCommandDestination != null) && !zclCommandDestination.equals(""))
            threadPoolManager.notifyZCLCommand(context, message, zclCommandDestination);

	}

	public void FrequencyAgilityResponse(final Status _st) {

		if ((frequencyAgilityResultDestination != null) && !frequencyAgilityResultDestination.equals(""))
            threadPoolManager.gatewayStartResult(_st, context, frequencyAgilityResultDestination);
	}

    public void setGatewayStopDestination(String gatewayStopDestination) {
		this.gatewayStopDestination = gatewayStopDestination;
	}

    public void setLeaveResultDestination(String leaveResultDestination) {
		this.leaveResultDestination = leaveResultDestination;
	}

    public void setNodeDescriptorDestination(String nodeDescriptorDestination) {
		this.nodeDescriptorDestination = nodeDescriptorDestination;
	}

    public void setNodeDiscoveredDestination(String nodeDiscoveredDestination) {
		this.nodeDiscoveredDestination = nodeDiscoveredDestination;
	}

    public void setNodeRemovedDestination(String nodeRemovedDestination) {
		this.nodeRemovedDestination = nodeRemovedDestination;
	}

    public void setNodeServicesDestination(String nodeServicesDestination) {
		this.nodeServicesDestination = nodeServicesDestination;
	}

    public void setPermitJoinDestination(String permitJoinDestination) {
		this.permitJoinDestination = permitJoinDestination;
	}

    public void setResetDestination(String resetDestination) {
		this.resetDestination = resetDestination;
	}

    public void setServiceDescriptorDestination(
			String serviceDescriptorDestination) {
		this.serviceDescriptorDestination = serviceDescriptorDestination;
	}

    public void setStartGatewayDestination(String startGatewayDestination) {
		this.startGatewayDestination = startGatewayDestination;
	}

    public void setZclCommandDestination(String zclCommandDestination) {
		this.zclCommandDestination = zclCommandDestination;
	}

    public void setFrequencyAgilityResultDestination(
			String _frequencyAgilityResultDestination) {
		this.frequencyAgilityResultDestination = _frequencyAgilityResultDestination;
	}


    public void setZdpCommandDestination(String zdpCommandDestination) {
		this.zdpCommandDestination = zdpCommandDestination;
	}
	
	public void setInterPANCommandDestination(String interPANCommandDestination) {
        String interPANCommandDestination1 = interPANCommandDestination;
	}

    public void setBindingDestination(String bindingDestination) {
		this.bindingDestination = bindingDestination;
	}

    public void setUnbindingDestination(String unbindingDestination) {
		this.unbindingDestination = unbindingDestination;
	}

    public void setNodeBindingDestination(String nodeBindingDestination) {
		this.nodeBindingDestination = nodeBindingDestination;
	}

}
