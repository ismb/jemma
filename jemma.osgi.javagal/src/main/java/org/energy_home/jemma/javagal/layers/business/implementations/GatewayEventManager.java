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
package org.energy_home.jemma.javagal.layers.business.implementations;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.object.GatewayDeviceEventEntry;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayEventListenerExtended;
import org.energy_home.jemma.zgd.IGatewayEventManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */

// FIXME a lot of redundancy in this class: to be considered for refactoring
public class GatewayEventManager implements IGatewayEventManager {
	// final int DISCOVERY_STOP = 0;
	ExecutorService executor = null;
	final static int DISCOVERY_ANNOUNCEMENTS = 2;
	final static int DISCOVERY_LEAVE = 4;
	final static int DISCOVERY_FRESHNESS = 16;

	private static final Logger LOG = LoggerFactory.getLogger(GatewayEventManager.class);

	/**
	 * The local {@link GalController} reference.
	 */
	private GalController gal = null;

	private GalController getGal() {
		return gal;
	}

	/**
	 * Creates a new instance with a Gal Controller reference.
	 * 
	 * @param _gal
	 *            a Gal controller reference.
	 */
	public GatewayEventManager(GalController _gal) {
		gal = _gal;
		LOG.debug("Creating Executor for GatewayEventManager with: {} threads", getGal().getPropertiesManager().getNumberOfThreadForAnyPool());

		executor = Executors.newFixedThreadPool(getGal().getPropertiesManager().getNumberOfThreadForAnyPool(), new ThreadFactory() {

			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-EventManager");
			}
		});

		if (executor instanceof ThreadPoolExecutor) {
			((ThreadPoolExecutor) executor).setKeepAliveTime(getGal().getPropertiesManager().getKeepAliveThread(), TimeUnit.MINUTES);
			((ThreadPoolExecutor) executor).allowCoreThreadTimeOut(true);

		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStartResult(final Status status) {
		executor.execute(new Runnable() {

			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					Status cstatus = null;
					synchronized (status) {
						cstatus = SerializationUtils.clone(status);
					}
					gel.getGatewayEventListener().gatewayStartResult(cstatus);
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStartResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						gel.getGatewayEventListener().gatewayStartResult(cstatus);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyServicesDiscovered(final int _requestIdentifier, final Status status, final NodeServices nodeServices) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						NodeServices cnodeServices = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (nodeServices) {
							cnodeServices = SerializationUtils.clone(nodeServices);
						}
						gel.getGatewayEventListener().servicesDiscovered(cstatus, cnodeServices);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStopResult(final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).gatewayStopResult(cstatus);
					}
				}

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStopResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier)
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
							Status cstatus = null;
							synchronized (status) {
								cstatus = SerializationUtils.clone(status);
							}
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).gatewayStopResult(cstatus);
						}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifypermitJoinResult(final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					Status cstatus = null;
					synchronized (status) {
						cstatus = SerializationUtils.clone(status);
					}
					gel.getGatewayEventListener().permitJoinResult(cstatus);
				}
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifypermitJoinResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						gel.getGatewayEventListener().permitJoinResult(cstatus);
					}
				}
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyResetResult(final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					Status cstatus = null;
					synchronized (status) {
						cstatus = SerializationUtils.clone(status);
					}
					gel.getGatewayEventListener().dongleResetResult(cstatus);
				}
			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyResetResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gel : getGal().getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						gel.getGatewayEventListener().dongleResetResult(cstatus);
					}
				}
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptor(final Status status, final NodeDescriptor node) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (!(gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)) {
						Status cstatus = null;
						NodeDescriptor cnode = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (node) {
							cnode = SerializationUtils.clone(node);
						}
						gl.getGatewayEventListener().nodeDescriptorRetrieved(cstatus, cnode);
					}
				}
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptor(final int _requestIdentifier, final Status status, final NodeDescriptor node) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier)
						if (!(gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)) {
							Status cstatus = null;
							NodeDescriptor cnode = null;
							synchronized (status) {
								cstatus = SerializationUtils.clone(status);
							}
							synchronized (node) {
								cnode = SerializationUtils.clone(node);
							}
							gl.getGatewayEventListener().nodeDescriptorRetrieved(cstatus, cnode);
						}
				}
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptorExtended(final Status status, final NodeDescriptor node, final Address address) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {

						Status cstatus = null;
						NodeDescriptor cnode = null;
						Address caddress = null;

						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (node) {
							cnode = SerializationUtils.clone(node);
						}

						synchronized (address) {
							caddress = SerializationUtils.clone(address);
						}
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).nodeDescriptorRetrievedExtended(cstatus, cnode, caddress);
					}
				}
			}

		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptorExtended(final int _requestIdentifier, final Status status, final NodeDescriptor node, final Address address) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier)
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
							Status cstatus = null;
							NodeDescriptor cnode = null;
							Address caddress = null;

							synchronized (status) {
								cstatus = SerializationUtils.clone(status);
							}
							synchronized (node) {
								cnode = SerializationUtils.clone(node);
							}

							synchronized (address) {
								caddress = SerializationUtils.clone(address);
							}

							((GatewayEventListenerExtended) gl.getGatewayEventListener()).nodeDescriptorRetrievedExtended(cstatus, cnode, caddress);
						}
				}
			}

		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void nodeDiscovered(final Status status, final WSNNode node) throws Exception {
		
		
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					{
						
						boolean _ReportOnExistingNodes = ((gl.getDiscoveryMask() & DISCOVERY_FRESHNESS) != 0);
						boolean _ReportAnnouncements = ((gl.getDiscoveryMask() & DISCOVERY_ANNOUNCEMENTS) != 0);
						if (_ReportOnExistingNodes || _ReportAnnouncements) {

							Status cstatus = null;
							WSNNode cnode = null;
							synchronized (status) {
								cstatus = SerializationUtils.clone(status);
							}
							synchronized (node) {
								cnode = SerializationUtils.clone(node);
							}
							gl.getGatewayEventListener().nodeDiscovered(cstatus, cnode);
						}
					}
				}
			}

		});
		

	}

	/**
	 * {@inheritDoc}
	 */
	public void nodeRemoved(final Status status, final WSNNode node) throws Exception {
		LOG.debug("\n\rNodeRemoved :" + String.format("%04X", node.getAddress().getNetworkAddress())  + "\n\r");

		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					boolean _ReportLeave = ((gl.getFreshnessMask() & DISCOVERY_LEAVE) != 0);
					if (_ReportLeave) {
						Status cstatus = null;
						WSNNode cnode = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (node) {
							cnode = SerializationUtils.clone(node);
						}
						gl.getGatewayEventListener().nodeRemoved(cstatus, cnode);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}

						gl.getGatewayEventListener().leaveResult(cstatus);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResult(final Status status) {
		executor.execute(new Runnable() {
			public void run() {

				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					Status cstatus = null;
					synchronized (status) {
						cstatus = SerializationUtils.clone(status);
					}
					gl.getGatewayEventListener().leaveResult(cstatus);
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResultExtended(final int _requestIdentifier, final Status status, final Address address) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier)
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {

							Status cstatus = null;
							Address caddress = null;
							synchronized (status) {
								cstatus = SerializationUtils.clone(status);
							}
							synchronized (address) {
								caddress = SerializationUtils.clone(address);
							}

							((GatewayEventListenerExtended) gl.getGatewayEventListener()).leaveResultExtended(cstatus, caddress);
						}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResultExtended(final Status status, final Address address) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
						Status cstatus = null;
						Address caddress = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (address) {
							caddress = SerializationUtils.clone(address);
						}
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).leaveResultExtended(cstatus, caddress);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyserviceDescriptorRetrieved(final int _requestIdentifier, final Status status, final ServiceDescriptor service) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						ServiceDescriptor cservice = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (service) {
							cservice = SerializationUtils.clone(service);
						}

						gl.getGatewayEventListener().serviceDescriptorRetrieved(cstatus, cservice);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifynodeBindingsRetrieved(final int _requestIdentifier, final Status status, final BindingList bindings) {

		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						BindingList cbindings = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						synchronized (bindings) {
							cbindings = SerializationUtils.clone(bindings);
						}

						gl.getGatewayEventListener().nodeBindingsRetrieved(cstatus, cbindings);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifybindingResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						gl.getGatewayEventListener().bindingResult(cstatus);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyUnbindingResult(final int _requestIdentifier, final Status status) {
		executor.execute(new Runnable() {
			public void run() {

				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						gl.getGatewayEventListener().unbindingResult(cstatus);
					}
				}

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyZDPEvent(final ZDPMessage message) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
						ZDPMessage cmessage = null;
						synchronized (message) {
							cmessage = SerializationUtils.clone(message);
						}

						((GatewayEventListenerExtended) gl.getGatewayEventListener()).notifyZDPCommand(cmessage);
					}
				}

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyZCLEvent(final ZCLMessage message) {
		executor.execute(new Runnable() {
			public void run() {
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
						ZCLMessage cmessage = null;
						synchronized (message) {
							cmessage = SerializationUtils.clone(message);
						}
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).notifyZCLCommand(cmessage);
					}
				}

			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyFrequencyAgility(final Status status) {
		executor.execute(new Runnable() {
			public void run() {
				LinkedList<GatewayDeviceEventEntry> copylist = null;
				for (GatewayDeviceEventEntry<?> gl : getGal().getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended) {
						Status cstatus = null;
						synchronized (status) {
							cstatus = SerializationUtils.clone(status);
						}
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).FrequencyAgilityResponse(cstatus);
					}
				}

			}
		});

	}

	public void notifyInterPANMessageEvent(InterPANMessageEvent message) {
		// TODO Auto-generated method stub

	}
}
