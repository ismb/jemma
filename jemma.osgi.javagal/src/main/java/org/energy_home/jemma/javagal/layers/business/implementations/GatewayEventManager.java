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

import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.object.GatewayDeviceEventEntry;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.GatewayEventListener;
import org.energy_home.jemma.zgd.GatewayEventListenerExtended;
import org.energy_home.jemma.zgd.IGatewayEventManager;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.BindingList;
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
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class GatewayEventManager implements IGatewayEventManager {
	// final int DISCOVERY_STOP = 0;
	final static int DISCOVERY_ANNOUNCEMENTS = 2;
	final static int DISCOVERY_LEAVE = 4;
	final static int DISCOVERY_FRESHNESS = 16;

	/**
	 * The local {@link GalController} reference.
	 */
	GalController gal = null;

	/**
	 * Creates a new instance with a Gal Controller reference.
	 * 
	 * @param _gal
	 *            a Gal controller reference.
	 */
	public GatewayEventManager(GalController _gal) {
		gal = _gal;
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStartResult(final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
					gel.getGatewayEventListener().gatewayStartResult(status);
				}

			}
		};
		thr.setName("Thread notifyGatewayStartResult(final Status status)");
		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStartResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier)
						gel.getGatewayEventListener().gatewayStartResult(status);
				}

			}
		};
		thr.setName("Thread notifyGatewayStartResult(final int _requestIdentifier, final Status status)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyServicesDiscovered(final int _requestIdentifier, final Status status, final NodeServices nodeServices) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
					if (gel.getProxyIdentifier() == _requestIdentifier)
						gel.getGatewayEventListener().servicesDiscovered(status, nodeServices);
				}

			}
		};
		thr.setName("Thread notifyServicesDiscovered(final int _requestIdentifier, final Status status, final NodeServices nodeServices)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStopResult(final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
					if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
						((GatewayEventListenerExtended) gl.getGatewayEventListener()).gatewayStopResult(status);
				}

			}
		};
		thr.setName("Thread notifyGatewayStopResult(final Status status)");

		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyGatewayStopResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
					if (gl.getProxyIdentifier() == _requestIdentifier)
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).gatewayStopResult(status);
				}

			}
		};
		thr.setName("Thread notifyGatewayStopResult(final int _requestIdentifier, final Status status)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifypermitJoinResult(final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
						gel.getGatewayEventListener().permitJoinResult(status);
					}
				}
		
		};
		thr.setName("Thread notifypermitJoinResult(final Status status)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifypermitJoinResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
						if (gel.getProxyIdentifier() == _requestIdentifier)
							gel.getGatewayEventListener().permitJoinResult(status);
					}
			}
		};
		thr.setName("Thread notifypermitJoinResult(final int _requestIdentifier, final Status status)");

		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyResetResult(final Status status) {
		/*
		 * Thread thr = new Thread() {
		 * 
		 * @Override public void run() {
		 */
			for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
				gel.getGatewayEventListener().dongleResetResult(status);
			}
		
		/*
		 * } }; thr.setName("Thread notifyResetResult(final Status status)");
		 * thr.start();
		 */
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyResetResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gel : gal.getListGatewayEventListener()) {
						if (gel.getProxyIdentifier() == _requestIdentifier)
							gel.getGatewayEventListener().dongleResetResult(status);
					}
				}
			
		};
		thr.setName("Thread notifyResetResult(final int _requestIdentifier, final Status status)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptor(final Status _status, final NodeDescriptor _node) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (!(gl.getGatewayEventListener() instanceof GatewayEventListenerExtended))
							gl.getGatewayEventListener().nodeDescriptorRetrieved(_status, _node);
					}
				}
			
		};
		thr.setName("Thread notifyNodeDescriptor(final Status _status, final NodeDescriptor _node)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptor(final int _requestIdentifier, final Status _status, final NodeDescriptor _node) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							if (!(gl.getGatewayEventListener() instanceof GatewayEventListenerExtended))
								gl.getGatewayEventListener().nodeDescriptorRetrieved(_status, _node);
					}
				}
			
		};
		thr.setName("Thread notifyNodeDescriptor(final int _requestIdentifier, final Status _status, final NodeDescriptor _node)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptorExtended(final Status _status, final NodeDescriptor _node, final Address _addressOfInterest) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).nodeDescriptorRetrievedExtended(_status, _node, _addressOfInterest);
					}
				}
			
		};
		thr.setName("Thread notifyNodeDescriptorExtended(final Status _status, final NodeDescriptor _node, final Address _addressOfInterest)");

		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyNodeDescriptorExtended(final int _requestIdentifier, final Status _status, final NodeDescriptor _node, final Address _addressOfInterest) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
								((GatewayEventListenerExtended) gl.getGatewayEventListener()).nodeDescriptorRetrievedExtended(_status, _node, _addressOfInterest);
					}
				}
			
		};
		thr.setName("Thread notifyNodeDescriptorExtended(final int _requestIdentifier, final Status _status, final NodeDescriptor _node, final Address _addressOfInterest)");
		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void nodeDiscovered(final Status _status, final WSNNode _node) throws Exception {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		//System.out.println("\n\rCalled--nodeDiscovered:" + _node.getAddress().getNetworkAddress() + " FROM:" + stackTraceElements[2].getMethodName() + "(Line Number:" + stackTraceElements[2].getLineNumber() + ")\n\r");

		try {
			Thread thr = new Thread() {
				@Override
				public void run() {
						for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
							{
								boolean _ReportOnExistingNodes = ((gl.getDiscoveryMask() & DISCOVERY_FRESHNESS) != 0);
								boolean _ReportAnnouncements = ((gl.getDiscoveryMask() & DISCOVERY_ANNOUNCEMENTS) != 0);
								if (_ReportOnExistingNodes || _ReportAnnouncements)
									gl.getGatewayEventListener().nodeDiscovered(_status, _node);
							}
						}
					}
				
			};
			thr.setName("Thread nodeDiscovered(final Status _status, final WSNNode _node)");

			thr.start();
		} catch (Exception e) {

			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void nodeRemoved(final Status _status, final WSNNode _node) throws Exception {
		try {
			Thread thr = new Thread() {
				@Override
				public void run() {
						for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
							boolean _ReportLeave = ((gl.getFreshnessMask() & DISCOVERY_LEAVE) != 0);
							if (_ReportLeave)
								gl.getGatewayEventListener().nodeRemoved(_status, _node);
						}
					
				}
			};
			thr.setName("Thread nodeRemoved(final Status _status, final WSNNode _node)");

			thr.start();

		} catch (Exception e) {

			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResult(final int _requestIdentifier, final Status _status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							gl.getGatewayEventListener().leaveResult(_status);
					}
				
			}
		};
		thr.setName("Thread notifyleaveResult(final int _requestIdentifier, final Status _status)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResult(final Status _status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						gl.getGatewayEventListener().leaveResult(_status);
					}
				
			}
		};
		thr.setName("Thread notifyleaveResult(final Status _status)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResultExtended(final int _requestIdentifier, final Status _status, final Address _address) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
								((GatewayEventListenerExtended) gl.getGatewayEventListener()).leaveResultExtended(_status, _address);
					}
				
			}
		};
		thr.setName("Thread notifyleaveResultExtended(final int _requestIdentifier, final Status _status, final Address _address)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyleaveResultExtended(final Status _status, final Address _address) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).leaveResultExtended(_status, _address);
					}
				
			}
		};
		thr.setName("Thread notifyleaveResultExtended(final Status _status, final Address _address)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyserviceDescriptorRetrieved(final int _requestIdentifier, final Status status, final ServiceDescriptor service) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							gl.getGatewayEventListener().serviceDescriptorRetrieved(status, service);
					}
				
			}
		};
		thr.setName("Thread notifyserviceDescriptorRetrieved(final int _requestIdentifier, final Status status, final ServiceDescriptor service)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifynodeBindingsRetrieved(final int _requestIdentifier, final Status status, final BindingList bindings) {

		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							gl.getGatewayEventListener().nodeBindingsRetrieved(status, bindings);
					}
				
			}
		};
		thr.setName("Thread notifynodeBindingsRetrieved(final int _requestIdentifier, final Status status, final BindingList bindings)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifybindingResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							gl.getGatewayEventListener().bindingResult(status);
					}
				
			}
		};
		thr.setName("Thread notifybindingResult(final int _requestIdentifier, final Status status)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyUnbindingResult(final int _requestIdentifier, final Status status) {
		Thread thr = new Thread() {
			@Override
			public void run() {

					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getProxyIdentifier() == _requestIdentifier)
							gl.getGatewayEventListener().unbindingResult(status);
					}
				
			}
		};
		thr.setName("Thread notifyUnbindingResult(final int _requestIdentifier, final Status status)");

		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyZDPCommand(final ZDPMessage message) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).notifyZDPCommand(message);
					}
				
			}
		};
		thr.setName("Thread notifyZDPCommand(final ZDPMessage message)");

		thr.start();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyZCLCommand(final ZCLMessage message) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).notifyZCLCommand(message);
					}
				
			}
		};
		thr.setName("Thread notifyZCLCommand(final ZCLMessage message)");
		thr.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyFrequencyAgility(final Status _status) {
		Thread thr = new Thread() {
			@Override
			public void run() {
					for (GatewayDeviceEventEntry gl : gal.getListGatewayEventListener()) {
						if (gl.getGatewayEventListener() instanceof GatewayEventListenerExtended)
							((GatewayEventListenerExtended) gl.getGatewayEventListener()).FrequencyAgilityResponse(_status);
					}
				
			}
		};
		thr.setName("Thread notifyZCLCommand(final ZCLMessage message)");
		thr.start();

	}
}
