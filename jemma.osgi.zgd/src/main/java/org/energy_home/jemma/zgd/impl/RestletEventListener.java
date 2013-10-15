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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import org.energy_home.jemma.zgd.APSMessageListener;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.Trace;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.restlet.Application;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;


class RestletEventListener extends Application implements GatewayEventListener, EventPathURIs {
	private Representation okResponse;
	
	private JaxbConverter jaxbConverter;
	private GatewayEventListener eventListener;
	private Map<Long, APSMessageListener> callbacks;
	//private ExecutorService executor;
	private ThreadPoolExecutor executor;
	
	RestletEventListener(JaxbConverter converter) throws JAXBException, IOException {
		jaxbConverter = converter;
		callbacks = new HashMap<Long, APSMessageListener>();		
		
		executor = new ThreadPoolExecutor(4, 48, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new RejectedExecutionHandler() {

					public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
						// TODO Auto-generated method stub
						Trace.println("\nrejected GAL request because of unavailable resources\n");
						Trace.println(((DispatchTask)r).eventPath);
					}
				});
		
		eventListener = this;
		Status s = new Status();
		s.setCode((short)0);
		Info info = new Info();
		info.setStatus(s);
		okResponse = converter.toRepresentation(info);
	}

	@Override
    public void handle(Request request, Response response) {
		Trace.println("\nreceived event from gateway");
		Trace.println(request.getResourceRef().toString());
		Trace.println(request.getResourceRef().getPath(false));
		
		// read request Info:
		try {
			Info info = jaxbConverter.getInfo(request.getEntity());
			executor.execute(new DispatchTask(info, request.getResourceRef().getPath(false)));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// send response
		Status s = new Status();
		s.setCode((short)0);
		Info info = new Info();
		info.setStatus(s);
		try {
			response.setEntity(jaxbConverter.toRepresentation(info));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void stop() throws Exception {
		executor.shutdownNow();
		super.stop();
	}

	void setGatewayEventListener(GatewayEventListener l) {
		eventListener = l;
	}
	
	GatewayEventListener getGatewayEventListener() {
		return eventListener;
	}
	
	void addAPSMEssageListener(Long cid, APSMessageListener l) {
		callbacks.put(cid, l);
	}
	
	void removeAPSMesssageListener(long cid) {
		callbacks.remove(cid);
	}


	public void gatewayStartResult(Status s) {
		System.out.println("Empty implementation of gatewayStartResult");
	}

	public void nodeDiscovered(Status status, WSNNode node) {
		System.out.println("Empty implementation of nodeDiscovered");
	}
	
	public void nodeRemoved(Status status, WSNNode node) {
		System.out.println("Empty implementation of nodeRemoved");
	}

	public void servicesDiscovered(Status status, NodeServices services) {
		System.out.println("Empty implementation of servicesDiscovered");
	}
	
	public void serviceDescriptorRetrieved(Status status, ServiceDescriptor service) {
		System.out.println("Empty implementation of serviceDescriptor");
	}
	
	public void nodeDescriptorRetrieved(Status status, NodeDescriptor node) {
		System.out.println("Empty implementation of nodeDescriptor");
	}
	
	public void leaveResult(Status s) {
		System.out.println("Empty implementation of leaveResult");
	}

	public void permitJoinResult(Status s) {
		System.out.println("Empty implementation of permitJoinResult");
	}
	
	public void dongleResetResult(Status s) {
		System.out.println("Empty implementation of gatewayResetResult");
	}
	
	public void bindingResult(Status status) {
		System.out.println("Empty implementation of bindingResult");
	}
	
	public void unbindingResult(Status status) {
		System.out.println("Empty implementation of unbindingResult");
	}
	
	public void nodeBindingsRetrieved(Status status, BindingList bindings) {
		System.out.println("Empty implementation of nodeBindingsRetrieved");
	}
	
	private class DispatchTask implements Runnable {
		private Info info;
		private String eventPath;
		
		DispatchTask(Info i, String p) {
			info = i;
			eventPath = p;
		}
		
		public void run() {
			try {
				Info.Detail detail = info.getDetail();
				Status status = info.getStatus();
				
				if (eventPath.equals(APS_NOTIFY_EVENT)) {
					APSMessageEvent message = detail.getAPSMessageEvent();
					Long cid = info.getEventCallbackIdentifier();
					APSMessageListener l = callbacks.get(cid);
					if (l == null && !callbacks.values().isEmpty()) l = callbacks.values().iterator().next(); // take the 1st as default
					if (l != null) l.notifyAPSMessage(message);
					else Trace.print("callback Id does not match a registered listener " + cid);
					
				} else if (eventPath.equals(STARTUP_RESPONSE)) {
					eventListener.gatewayStartResult(status);
				
				} else if (eventPath.equals(RESET_RESPONSE)) {
					eventListener.dongleResetResult(status);
					
				} else if (eventPath.equals(LEAVE_RESPONSE)) {
					//eventListener.leaveResult(status);
					
				} else if (eventPath.equals(PERMITJOIN_RESPONSE)) {
					//eventListener.permitJoinResult(status);
				
				} else if (eventPath.equals(NODE_DISCOVERED)) {
					WSNNode node = detail == null ? null : detail.getWSNNode();
					eventListener.nodeDiscovered(status, node);
					
				} else if (eventPath.equals(NODE_REMOVED)) {
					WSNNode node = detail == null ? null : detail.getWSNNode();
					eventListener.nodeRemoved(status, node);
				
				} else if (eventPath.equals(SERVICES_DISCOVERED)) {
					NodeServices services = detail == null ? null : detail.getNodeServices();
					eventListener.servicesDiscovered(status, services);
					
				} else if (eventPath.equals(SERVICE_DESCRIPTOR)) {
					ServiceDescriptor service = detail == null ? null : detail.getServiceDescriptor();
					eventListener.serviceDescriptorRetrieved(status, service);
					
				} else if (eventPath.equals(NODE_DESCRIPTOR)) {
					NodeDescriptor node = detail == null ? null : detail.getNodeDescriptor();
					eventListener.nodeDescriptorRetrieved(status, node);
					
				} else if (eventPath.equals(NODE_BINDING_RESPONSE)) {
					eventListener.bindingResult(status);
					
				} else if (eventPath.equals(NODE_UNBINDING_RESPONSE)) {
					eventListener.unbindingResult(status);	
					
				} else if (eventPath.equals(NODE_BINDING_LIST_RESPONSE)) {
					BindingList bindings = detail == null ? null : detail.getBindings();
					eventListener.nodeBindingsRetrieved(status, bindings);	
										
				} else {
					Trace.println("Unknown event resource path");
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
