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

import org.energy_home.jemma.javagal.rest.util.ClientResources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.energy_home.jemma.zgd.GatewayEventListenerExtended;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.BindingList;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.InterPANMessage;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.ServiceDescriptor;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.ZCLMessage;
import org.energy_home.jemma.zgd.jaxb.ZDPMessage;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private String interPANCommandDestination;
	private String frequencyAgilityResultDestination;
	private final Context context;
	private static final Logger LOG = LoggerFactory.getLogger( RestClientManagerAndListener.class );
	private PropertiesManager _PropertiesManager;
	private ClientResources clientResource;

	public RestClientManagerAndListener(PropertiesManager ___PropertiesManager,
			ClientResources _clientResorce) {
		_PropertiesManager = ___PropertiesManager;
		this.clientResource = _clientResorce;
		this.context =  new Context();
		context.getParameters().add("socketTimeout", ((Integer)(_PropertiesManager.getHttpOptTimeout()*1000)).toString());
	}

	synchronized public void gatewayStartResult(final Status status) {

		if ((startGatewayDestination != null)
				&& !startGatewayDestination.equals("")) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + startGatewayDestination);
						ClientResource resource = new ClientResource(context,
								startGatewayDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled:" + _xml);
						resource.post(_xml, MediaType.TEXT_XML);
						
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on gatewayStartResult", e);
	
						clientResource.addToCounterException();
					}
				}

			};
			thr.start();
		}

	}

	public void nodeDiscovered(final Status status, final WSNNode node) {

		if ((nodeDiscoveredDestination != null)
				&& !nodeDiscoveredDestination.equals("")) {
			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeDiscoveredDestination);
						
						ClientResource resource = new ClientResource(context,
								nodeDiscoveredDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setWSNNode(node);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void nodeRemoved(final Status status, final WSNNode node) {

		if ((nodeRemovedDestination != null)
				&& !nodeRemovedDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeRemovedDestination);
						
						ClientResource resource = new ClientResource(context,
								nodeRemovedDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setWSNNode(node);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void servicesDiscovered(final Status status,
			final NodeServices services) {

		if ((nodeServicesDestination != null)
				&& !nodeServicesDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeServicesDestination);
						
						ClientResource resource = new ClientResource(context,
								nodeServicesDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setNodeServices(services);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void serviceDescriptorRetrieved(final Status status,
			final ServiceDescriptor service) {

		if ((serviceDescriptorDestination != null)
				&& !serviceDescriptorDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + serviceDescriptorDestination);
						
						ClientResource resource = new ClientResource(context,
								serviceDescriptorDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setServiceDescriptor(service);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void dongleResetResult(final Status status) {

		if ((resetDestination != null) && !resetDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + resetDestination);
						
						ClientResource resource = new ClientResource(context,
								resetDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void bindingResult(final Status status) {

		if ((bindingDestination != null) && !bindingDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + bindingDestination);
						ClientResource resource = new ClientResource(context,
								bindingDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}

	public void unbindingResult(final Status status) {

		if ((unbindingDestination != null) && !unbindingDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + unbindingDestination);
						
						ClientResource resource = new ClientResource(context,
								unbindingDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void nodeBindingsRetrieved(final Status status,
			final BindingList bindings) {

		if ((nodeBindingDestination != null)
				&& !nodeBindingDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeBindingDestination);
						
						ClientResource resource = new ClientResource(context,
								nodeBindingDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setBindings(bindings);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void leaveResult(final Status status) {

		if ((leaveResultDestination != null)
				&& !leaveResultDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + leaveResultDestination);
						
						ClientResource resource = new ClientResource(context,
								leaveResultDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}

	public void permitJoinResult(final Status status) {

		if ((permitJoinDestination != null)
				&& !permitJoinDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + permitJoinDestination);
						
						ClientResource resource = new ClientResource(context,
								permitJoinDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}

	public void nodeDescriptorRetrievedExtended(final Status status,
			final NodeDescriptor node, final Address addressOfInteres) {

		if ((nodeDescriptorDestination != null)
				&& !nodeDescriptorDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeDescriptorDestination);
						
						ClientResource resource = new ClientResource(context,
								nodeDescriptorDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setNodeDescriptor(node);
						WSNNode n = new WSNNode();
						n.setAddress(addressOfInteres);
						detail.setWSNNode(n);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	@Deprecated
	public void nodeDescriptorRetrieved(final Status status,
			final NodeDescriptor node) {

		if ((nodeDescriptorDestination != null)
				&& !nodeDescriptorDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + nodeDescriptorDestination);
					
						ClientResource resource = new ClientResource(context,
								nodeDescriptorDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						detail.setNodeDescriptor(node);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void gatewayStopResult(final Status status) {

		if ((gatewayStopDestination != null)
				&& !gatewayStopDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + gatewayStopDestination);
					
						ClientResource resource = new ClientResource(context,
								gatewayStopDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void leaveResultExtended(final Status status,
			final Address addressOfInteres) {

		if ((leaveResultDestination != null)
				&& !leaveResultDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + leaveResultDestination);
	
						ClientResource resource = new ClientResource(context,
								leaveResultDestination);
						Info info = new Info();
						info.setStatus(status);
						Info.Detail detail = new Info.Detail();
						WSNNode node = new WSNNode();
						node.setAddress(addressOfInteres);
						detail.setWSNNode(node);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}

	public void notifyZDPCommand(final ZDPMessage message) {

		if ((zdpCommandDestination != null)
				&& !zdpCommandDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + zdpCommandDestination);
	
						ClientResource resource = new ClientResource(context,
								zdpCommandDestination);
						Info.Detail detail = new Info.Detail();
						detail.setZDPMessage(message);
						String _xml = Util.marshal(detail);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}
	
	
	
	
	public void notifyInterPANCommand(final InterPANMessageEvent message) {

		if ((interPANCommandDestination  != null)
				&& !interPANCommandDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.info("Connecting to:"  + interPANCommandDestination);
	
						ClientResource resource = new ClientResource(context,
								interPANCommandDestination);
						Info.Detail detail = new Info.Detail();
						detail.setInterPANMessageEvent(message);
						String _xml = Util.marshal(detail);
						if (_PropertiesManager.getDebugEnabled())
							LOG.info(_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error(e.getMessage(),e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();

		}

	}
	

	public void notifyZCLCommand(final ZCLMessage message) {

		if ((zclCommandDestination != null)
				&& !zclCommandDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + zclCommandDestination);
	
						ClientResource resource = new ClientResource(context,
								zclCommandDestination);
						Info.Detail detail = new Info.Detail();
						detail.setZCLMessage(message);
						String _xml = Util.marshal(detail);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public void FrequencyAgilityResponse(final Status _st) {

		if ((frequencyAgilityResultDestination != null)
				&& !frequencyAgilityResultDestination.equals("")) {

			Thread thr = new Thread() {
				@Override
				public void run() {
					try {
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Connecting to:"  + frequencyAgilityResultDestination);
						ClientResource resource = new ClientResource(context,
								frequencyAgilityResultDestination);
						Info info = new Info();
						info.setStatus(_st);
						Info.Detail detail = new Info.Detail();
						// detail.setEnergyScanResult(_result);
						info.setDetail(detail);
						String _xml = Util.marshal(info);
						if (_PropertiesManager.getDebugEnabled())
							LOG.debug("Marshaled: "+_xml);
						resource.post(_xml, MediaType.TEXT_XML);
						resource.release();
						resource = null;
						clientResource.resetCounter();
					} catch (Exception e) {
						if (_PropertiesManager.getDebugEnabled())
							LOG.error("Exception on ??", e);
	
						clientResource.addToCounterException();
					}
				}
			};
			thr.start();
		}

	}

	public String getGatewayStopDestination() {
		return gatewayStopDestination;
	}

	public void setGatewayStopDestination(String gatewayStopDestination) {
		this.gatewayStopDestination = gatewayStopDestination;
	}

	public String getLeaveResultDestination() {
		return leaveResultDestination;
	}

	public void setLeaveResultDestination(String leaveResultDestination) {
		this.leaveResultDestination = leaveResultDestination;
	}

	public String getNodeDescriptorDestination() {
		return nodeDescriptorDestination;
	}

	public void setNodeDescriptorDestination(String nodeDescriptorDestination) {
		this.nodeDescriptorDestination = nodeDescriptorDestination;
	}

	public String getNodeDiscoveredDestination() {
		return nodeDiscoveredDestination;
	}

	public void setNodeDiscoveredDestination(String nodeDiscoveredDestination) {
		this.nodeDiscoveredDestination = nodeDiscoveredDestination;
	}

	public String getNodeRemovedDestination() {
		return nodeRemovedDestination;
	}

	public void setNodeRemovedDestination(String nodeRemovedDestination) {
		this.nodeRemovedDestination = nodeRemovedDestination;
	}

	public String getNodeServicesDestination() {
		return nodeServicesDestination;
	}

	public void setNodeServicesDestination(String nodeServicesDestination) {
		this.nodeServicesDestination = nodeServicesDestination;
	}

	public String getPermitJoinDestination() {
		return permitJoinDestination;
	}

	public void setPermitJoinDestination(String permitJoinDestination) {
		this.permitJoinDestination = permitJoinDestination;
	}

	public String getResetDestination() {
		return resetDestination;
	}

	public void setResetDestination(String resetDestination) {
		this.resetDestination = resetDestination;
	}

	public String getServiceDescriptorDestination() {
		return serviceDescriptorDestination;
	}

	public void setServiceDescriptorDestination(
			String serviceDescriptorDestination) {
		this.serviceDescriptorDestination = serviceDescriptorDestination;
	}

	public String getStartGatewayDestination() {
		return startGatewayDestination;
	}

	public void setStartGatewayDestination(String startGatewayDestination) {
		this.startGatewayDestination = startGatewayDestination;
	}

	public String getZclCommandDestination() {
		return zclCommandDestination;
	}

	public void setZclCommandDestination(String zclCommandDestination) {
		this.zclCommandDestination = zclCommandDestination;
	}

	public String getFrequencyAgilityResultDestination() {
		return frequencyAgilityResultDestination;
	}

	public void setFrequencyAgilityResultDestination(
			String _frequencyAgilityResultDestination) {
		this.frequencyAgilityResultDestination = _frequencyAgilityResultDestination;
	}

	public String getZdpCommandDestination() {
		return zdpCommandDestination;
	}
	
	
	public String getinterPANCommandDestination() {
		return interPANCommandDestination;
	}

	public void setZdpCommandDestination(String zdpCommandDestination) {
		this.zdpCommandDestination = zdpCommandDestination;
	}
	
	public void setInterPANCommandDestination(String interPANCommandDestination) {
		this.interPANCommandDestination = interPANCommandDestination;
	}

	public String getBindingDestination() {
		return bindingDestination;
	}

	public void setBindingDestination(String bindingDestination) {
		this.bindingDestination = bindingDestination;
	}

	public String getUnbindingDestination() {
		return unbindingDestination;
	}

	public void setUnbindingDestination(String unbindingDestination) {
		this.unbindingDestination = unbindingDestination;
	}

	public String getNodeBindingDestination() {
		return nodeBindingDestination;
	}

	public void setNodeBindingDestination(String nodeBindingDestination) {
		this.nodeBindingDestination = nodeBindingDestination;
	}

}
