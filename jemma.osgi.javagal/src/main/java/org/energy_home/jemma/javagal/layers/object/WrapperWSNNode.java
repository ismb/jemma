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
package org.energy_home.jemma.javagal.layers.object;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.swing.text.StyledEditorKit.BoldAction;

import org.apache.commons.lang3.SerializationUtils;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to encapsulate any ZigBee Node. This class manage the Timers for
 * the Algorithms Discovery, Freshness and ForcePing
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 */
public class WrapperWSNNode {
	int _timerID = 0;
	private WSNNode _node;
	ScheduledThreadPoolExecutor freshnessTPool;
	ScheduledThreadPoolExecutor discoveryTPool;
	ScheduledThreadPoolExecutor forcePingTPool;

	ScheduledFuture<Void> freshnessJob = null;
	ScheduledFuture<Void> forcePingJob = null;
	ScheduledFuture<Void> discoveryJob = null;
	private short _numberOfAttempt;
	private boolean dead;

	private boolean _discoveryCompleted;
	private boolean _executingForcePing;

	private static final Logger LOG = LoggerFactory.getLogger(WrapperWSNNode.class);
	
	public synchronized boolean is_executingForcePing() {
		return _executingForcePing;
	}

	public synchronized void set_executingForcePing(boolean _executingForcePing) {
		this._executingForcePing = _executingForcePing;
	}

	public synchronized boolean is_executingFreshness() {
		return _executingFreshness;
	}

	public synchronized void set_executingFreshness(boolean _executingFreshness) {
		this._executingFreshness = _executingFreshness;
	}

	public synchronized boolean is_executingDiscovery() {
		return _executingDiscovery;
	}

	public synchronized void set_executingDiscovery(boolean _executingDiscovery) {
		this._executingDiscovery = _executingDiscovery;
	}

	private boolean _executingFreshness;
	private boolean _executingDiscovery;

	private NodeServices _nodeServices;
	private NodeDescriptor _nodeDescriptor;
	private Mgmt_LQI_rsp _Mgmt_LQI_rsp;

	private GalController gal = null;

	public WrapperWSNNode(GalController _gal, final String networkAdd) {
		gal = _gal;
		this._numberOfAttempt = 0;

		this.dead = false;
		freshnessTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-Freshness[" + networkAdd + "]");
			}
		});
		discoveryTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-Discovery[" + networkAdd + "]");
			}
		});
		forcePingTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-ForcePing[" + networkAdd + "]");
			}
		});

	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof WrapperWSNNode) {
			WrapperWSNNode node = (WrapperWSNNode) o;
			if (node.get_node() != null && node.get_node().getAddress() != null && node.get_node().getAddress().getIeeeAddress() != null && this.get_node() != null && this.get_node().getAddress() != null && this.get_node().getAddress().getIeeeAddress() != null) {
				if (node.get_node().getAddress().getIeeeAddress().longValue() == this.get_node().getAddress().getIeeeAddress().longValue())
					return true;
				else
					return false;
			} else if (node.get_node() != null && node.get_node().getAddress() != null && node.get_node().getAddress().getNetworkAddress() != null && this.get_node() != null && this.get_node().getAddress() != null && this.get_node().getAddress().getNetworkAddress() != null) {
				if (node.get_node().getAddress().getNetworkAddress().intValue() == this.get_node().getAddress().getNetworkAddress().intValue())
					return true;
				else
					return false;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public synchronized NodeDescriptor getNodeDescriptor() {
		return _nodeDescriptor;
	}

	public synchronized void setNodeDescriptor(NodeDescriptor nodeDescriptor) {
		this._nodeDescriptor = nodeDescriptor;
	}

	/**
	 * return the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public synchronized short get_numberOfAttempt() {
		return _numberOfAttempt;
	}

	public synchronized boolean isDead() {
		return dead;
	}

	/**
	 * Increase the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public synchronized void set_numberOfAttempt() {
		this._numberOfAttempt = (short) (this._numberOfAttempt + 1);
	}

	/**
	 * Cancel all timers
	 */
	public synchronized void abortTimers() {
		this.dead = true;
		if (discoveryJob != null) {
			discoveryJob.cancel(false);
			discoveryJob = null;
		}

		if (freshnessJob != null) {
			freshnessJob.cancel(false);
			freshnessJob = null;
		}

		if (forcePingJob != null) {
			forcePingJob.cancel(false);
			forcePingJob = null;
		}
		if (freshnessTPool != null) {
			freshnessTPool.shutdown();
			freshnessTPool = null;
		}
		if (discoveryTPool != null) {
			discoveryTPool.shutdown();
			discoveryTPool = null;
		}
		if (forcePingTPool != null) {
			forcePingTPool.shutdown();
			forcePingTPool = null;
		}
	}

	/**
	 * reset the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public synchronized void reset_numberOfAttempt() {
		this._numberOfAttempt = 0;
	}

	/**
	 * Return the status of the discovery. The discovery is complited when the
	 * node has sent the response of the LqiRequest
	 */
	public synchronized boolean is_discoveryCompleted() {
		return _discoveryCompleted;
	}

	/**
	 * Set the status of the discovery. The discovery is complited when the node
	 * has sent the response of the LqiRequest
	 */
	public synchronized void set_discoveryCompleted(boolean _discoveryCompleted) {
		this._discoveryCompleted = _discoveryCompleted;
	}

	/**
	 * return the Lqi Response Class of the node.
	 */
	public synchronized Mgmt_LQI_rsp get_Mgmt_LQI_rsp() {
		return _Mgmt_LQI_rsp;
	}

	/**
	 * Set the Lqi Response Class of the node.
	 */
	public synchronized void set_Mgmt_LQI_rsp(Mgmt_LQI_rsp _Mgmt_LQI_rsp) {
		this._Mgmt_LQI_rsp = _Mgmt_LQI_rsp;
	}

	/**
	 * return the list of the EndPoints of the node.
	 */
	public synchronized NodeServices get_nodeServices() {
		return _nodeServices;
	}

	/**
	 * Set the list of the EndPoints of the node. Is called when is present a
	 * response of the startnodeServices
	 */
	public synchronized void set_nodeServices(NodeServices _nodeServices) {
		this._nodeServices = _nodeServices;
	}

	/**
	 * Check if the Node is a sleepy end device or end device
	 */
	public synchronized boolean isSleepyOrEndDevice() {
		if ((_node != null) && (_node.getCapabilityInformation() != null)) {
			if (_node.getCapabilityInformation().isDeviceIsFFD())
				return false;
			else
				return true;
		} else
			return true;

	}

	/**
	 * return the WsnNode from the wrapper
	 */
	public synchronized WSNNode get_node() {
		return _node;
	}

	/**
	 * Set the WsnNode into the Wrapper
	 */
	public synchronized void set_node(WSNNode _node) {
		this._node = _node;
	}

	/**
	 * Set the Discovery Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public synchronized void setTimerDiscovery(int seconds) {
		if (discoveryJob != null) {
			discoveryJob.cancel(false);
		}
		if (!is_executingDiscovery()) {
			if (!isDead()) {
				if (seconds >= 0) {
					try {
						discoveryJob = discoveryTPool.schedule(new DiscoveryJob(), seconds, TimeUnit.SECONDS);
					} catch (Exception e) {
						LOG.error("Error scheduling thread: {}",e.getMessage());

					}
				}
			}
		}

	}

	/**
	 * Set the Freshness Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public synchronized void setTimerFreshness(int seconds) {
		if (freshnessJob != null) {
			freshnessJob.cancel(false);
		}

		if (!is_executingFreshness()) {
			if (!isDead()) {
				if (seconds >= 0) {
					try {
						freshnessJob = freshnessTPool.schedule(new FreshnessJob(), seconds, TimeUnit.SECONDS);
					} catch (Exception e) {
						LOG.error("Error scheduling thread: {}",e.getMessage());
					}
				}
			}
		}

	}

	/**
	 * Set the ForcePing Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public synchronized void setTimerForcePing(int seconds) {

		if (forcePingJob != null) {
			forcePingJob.cancel(false);
		}

		if (!is_executingForcePing()) {
			if (!isDead()) {
				if (seconds >= 0) {
					try {
						forcePingJob = forcePingTPool.schedule(new ForcePingJob(), seconds, TimeUnit.SECONDS);
					} catch (Exception e) {
						LOG.error("Error scheduling thread: {}",e.getMessage());
					}
				}
			}
		}
	}

	private class FreshnessJob implements Callable<Void> {

		public FreshnessJob() {
			super();
		}

		public Void call() {
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FRESHNESS, (short) 0x00);
			return null;
		}
	}

	private class ForcePingJob implements Callable<Void> {

		public ForcePingJob() {
			super();

		}

		public Void call() {
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FORCEPING, (short) 0x00);

			return null;
		}
	}

	private class DiscoveryJob implements Callable<Void> {

		public DiscoveryJob() {
			super();

		}

		public Void call() {

			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.DISCOVERY, (short) 0x00);

			return null;
		}
	}

}
