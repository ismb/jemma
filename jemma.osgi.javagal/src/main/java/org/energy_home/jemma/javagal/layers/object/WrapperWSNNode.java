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

import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.zgd.jaxb.NodeDescriptor;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

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
	private boolean _discoveryCompleted;
	private NodeServices _nodeServices;
	private NodeDescriptor _nodeDescriptor;
	private Mgmt_LQI_rsp _Mgmt_LQI_rsp;
	private long lastDiscovered;
	private GalController gal = null;

	public WrapperWSNNode(GalController _gal, final String networkAdd) {
		gal = _gal;
		this._numberOfAttempt = 0;
		this.lastDiscovered = 0;
		freshnessTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-Freshness[" + networkAdd + "]");
			}
		});
		discoveryTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-Discovery[" + networkAdd + "]");
			}
		});
		forcePingTPool = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {

				return new Thread(r, "THPool-ForcePing[" + networkAdd + "]");
			}
		});

	}

	public synchronized NodeDescriptor getNodeDescriptor() {
		return _nodeDescriptor;
	}

	public synchronized void setNodeDescriptor(NodeDescriptor nodeDescriptor) {
		this._nodeDescriptor = nodeDescriptor;
	}

	public synchronized long getLastDiscovered() {
		return lastDiscovered;
	}

	public synchronized void setLastDiscovered(long lastDiscovered) {
		this.lastDiscovered = lastDiscovered;
	}

	/**
	 * return the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public synchronized short get_numberOfAttempt() {
		return _numberOfAttempt;
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

		if (discoveryJob != null) {
			discoveryJob.cancel(true);
			discoveryJob = null;
		}

		if (freshnessJob != null) {
			freshnessJob.cancel(true);
			freshnessJob = null;
		}

		if (forcePingJob != null) {
			forcePingJob.cancel(true);
			forcePingJob = null;
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
	 * Check if the Node is a sleepy device
	 */
	public synchronized boolean isSleepy() {
		if ((_node != null) && (_node.getCapabilityInformation() != null)) {
			if (_node.getCapabilityInformation().isReceiverOnWhenIdle())
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
			discoveryJob.cancel(true);
		}
		if (seconds >= 0) {
			try {
				discoveryJob = discoveryTPool.schedule(new DiscoveryJob(), seconds, TimeUnit.SECONDS);
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();

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
			freshnessJob.cancel(true);
		}

		if (seconds >= 0) {
			try {
				freshnessJob = freshnessTPool.schedule(new FreshnessJob(), seconds, TimeUnit.SECONDS);
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();

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
			forcePingJob.cancel(true);
		}

		if (seconds >= 0) {
			try {
				forcePingJob = forcePingTPool.schedule(new ForcePingJob(), seconds, TimeUnit.SECONDS);
			} catch (Exception e) {
				System.out.print(e.getMessage());
				e.printStackTrace();

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
