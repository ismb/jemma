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
package org.energy_home.jemma.osgi.zgd.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.device.zgd.IGal;
import org.energy_home.jemma.zgd.GatewayException;
import org.energy_home.jemma.zgd.GatewayFactory;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.GatewayProperties;
import org.energy_home.jemma.zgd.Trace;
import org.energy_home.jemma.zgd.impl.GatewayFactoryImpl;
import org.energy_home.jemma.zgd.jaxb.ObjectFactory;
import org.energy_home.jemma.zgd.jaxb.Version;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class GatewayActivator implements BundleActivator, Runnable {
	public static final String PROP_ZGD_BIND_DEVICE_NAME = "it.telecomitalia.zgd.bind-device";

	private ServiceRegistration registration;
	private GatewayFactory gatewayFactory = null;
	private GatewayInterface gatewayInterface;

	private BundleContext bc;

	private static final Log log = LogFactory.getLog(GatewayActivator.class);

	private volatile Thread pollingThread = null;

	boolean bindZgdDeviceService = true;

	private Object gal;

	private Object lock = new Object();

	private String zgdPort;

	private Map galProps = null;

	public void start(final BundleContext bc) throws Exception {
		synchronized (lock) {
			this.bc = bc;
			bindZgdDeviceService = getProperty(PROP_ZGD_BIND_DEVICE_NAME, true);
			if (this.gal != null || !bindZgdDeviceService)
				this.bindGal(galProps);
		}
	}

	public synchronized void stop(BundleContext bc) throws Exception {
		synchronized (lock) {
			if (!bindZgdDeviceService) {
				this.unbindGal();
			}
			if (log != null)
				log.debug("bundle stopped");
		}
	}

	protected void setGal(IGal gal, Map props) throws Exception {
		synchronized (lock) {
			if (bindZgdDeviceService) {
				this.gal = gal;
				this.galProps = props;
				if (this.bc != null)
					this.bindGal(props);
			}
		}
	}

	protected void unsetGal(IGal gal) throws Exception {
		synchronized (lock) {
			if (bindZgdDeviceService) {
				if (this.gal == gal) {
					this.galProps = null;
					this.gal = null;
					this.unbindGal();
				}
			}
		}
	}

	private void bindGal(Map props) {
		if (getProperty("jgal.debug", false)) {
			Trace.setTrace(new Trace() {
				protected void print0(String s) {
					log.debug(s);
				}

				protected void printf0(String s, Object... args) {
					log.debug(String.format(s, args));
				}
			});
		}

		if (props != null)
			this.zgdPort = (String) props.get("zgd.port");

		try {
			if (gatewayFactory == null) {
				GatewayProperties osgiProperties = new GatewayProperties() {
					public String getProperty(String key) {
						String value = GatewayActivator.this.getProperty(key);
						if (value == null)
							value = super.getProperty(key);
						return value;
					}
				};
				gatewayFactory = new GatewayFactoryImpl(osgiProperties);
			}
		} catch (Exception e) {
			log.error("", e);
		}

		try {
			gatewayInterface = gatewayFactory.createGatewayObject();
		} catch (Exception e) {
			log.error(e);
		}

		this.startPollingTask();
		if (log != null)
			log.debug("leaving bindGal()");
	}

	private void unbindGal() throws Exception {
		this.stopPollingTask();
		
		try {
			unregister(gatewayInterface);
		} catch (Throwable e) {
			log.error("error unregistering gatewayInterface", e);
		}
		
		gatewayInterface = null;
		
		if (gatewayFactory != null) {
			gatewayFactory.close();
			gatewayFactory = null;
		}

		if (log != null)
			log.debug("leaving unbindGal()");
	}

	protected String getProperty(String key) {
		if (key.equals(GatewayProperties.GATEWAY_ROOT_URI) && this.zgdPort != null && this.zgdPort.length() > 0) {
			return "http://127.0.0.1:" + this.zgdPort;
		}
		String value = bc.getProperty(key);
		if (value != null)
			log.debug(key + " = " + value);

		return value;
	}

	private void startPollingTask() {
		if (pollingThread != null) {
			log.fatal("error!!!! trying to start thread twice!!");
			return;
		}
		pollingThread = new Thread(this, "Zgd Monitoring Thread");
		pollingThread.start();
	}

	private void stopPollingTask() throws InterruptedException {
		// atomic because pollingThread is volatile
		Thread tmpThread = pollingThread;

		pollingThread = null;

		if (tmpThread != null) {
			tmpThread.interrupt();
			tmpThread.join();
		}
	}

	private boolean getProperty(String name, boolean value) {
		String prop = bc.getProperty(name);
		if (prop != null) {
			try {
				value = Boolean.valueOf(prop).booleanValue();
			} catch (Exception e) {
			}
		}
		return value;
	}

	public void run() {
		log.debug("started");
		if (pollingThread == null) {
			log.debug("stopped thread before started");
			return; // stopped before started.
		}

		Version version = null;
		while (!Thread.currentThread().isInterrupted()) {
			try {
				synchronized (lock) {
					if (gatewayInterface != null) {
						//AGGIUNTO DA GBO A SCOPO DI TEST CAUSA ECCEZIONE
						ObjectFactory factory = new ObjectFactory();
						version = factory.createVersion();
						//***************
						version = gatewayInterface.getVersion();
						register(version, gatewayInterface);
					} else {
						log.fatal("gatewayInterface is null. why?????");
						break;
					}
				}
			} catch (java.io.IOException e) {
				synchronized (lock) {
					log.debug("", e);
					unregister(gatewayInterface);
				}

			} catch (GatewayException e) {
				synchronized (lock) {
					unregister(gatewayInterface);
				}
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				log.fatal("generic exception on polling task. Exiting", e);
				break;
			}

			if (Thread.currentThread().isInterrupted()) {
				break;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				break;
			}
		}
		log.debug("leaving thread");
	}

	private void register(Version version, GatewayInterface gatewayInterface) {
		if (registration == null) {
			registration = bc.registerService(new String[] { GatewayInterface.class.getName() }, gatewayInterface, null);
			if (log != null)
				log.info("zgd version " + version.getManufacturerVersion()
						+ " up and running. zgd GatewayInterface registered successfully.");
		}
	}

	protected void unregister(GatewayInterface gatewayInterface) {
		if (registration != null) {
			log.debug("unregistering zgd service");
			registration.unregister();
			log.debug("unregistered zgd service");
			registration = null;
		}
	}
}