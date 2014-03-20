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

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

/**
 * Class used to encapsulate any ZigBee Node. This class manage the Timers for
 * the Algorithms Discovery, Freshness and ForcePing
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class WrapperWSNNode {
	int _timerID = 0;
	private WSNNode _node;
	private Timer _timerDiscovery;
	private Timer _timerFreshness;
	private Timer _timerForcePing;
	private short _numberOfAttempt;
	private boolean _discoveryCompleted;
	private NodeServices _nodeServices;
	private Mgmt_LQI_rsp _Mgmt_LQI_rsp;
	private long lastDiscovered;
	public long getLastDiscovered() {
		return lastDiscovered;
	}

	public void setLastDiscovered(long lastDiscovered) {
		this.lastDiscovered = lastDiscovered;
	}

	private GalController gal = null;

	public WrapperWSNNode(GalController _gal) {
		gal = _gal;
		this._node = null;
		this._timerDiscovery = null;
		this._timerFreshness = null;
		this._numberOfAttempt = 0;
		this.lastDiscovered = 0;

	}

	/**
	 * Check if the Node is a sleepy device
	 */
	public boolean isSleepy() {
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

		if (_timerDiscovery != null) {
			_timerDiscovery.cancel();
			_timerDiscovery.purge();
		}
		if (seconds >= 0) {
			String name = "Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerDiscovery(Seconds:" + seconds + "-ID:" + ++_timerID + ")";
			_timerDiscovery = new Timer(name);
			_timerDiscovery.schedule(new RemindTaskDiscovery(name), seconds * 1000);

		}

	}

	/**
	 * Set the Freshness Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public synchronized void setTimerFreshness(int seconds) {

		if (_timerFreshness != null) {
			_timerFreshness.cancel();
			_timerFreshness.purge();
			//System.out.println("Stopping timer Freshness ("+_timerFreshness.hashCode()+")");
		}

		if (seconds >= 0) {

			String name = "Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerFreshness(Seconds:" + seconds + "-ID:" + ++_timerID + ")";
			_timerFreshness = new Timer(name);
			_timerFreshness.schedule(new RemindTaskFreshness(name), seconds * 1000);
			//System.out.println("Creation of new Timer  Freshness "+_timerFreshness.hashCode()+"("+seconds+" seconds)");
		}

	}

	/**
	 * Set the ForcePing Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public synchronized void setTimerForcePing(int seconds) {
		if (_timerForcePing != null) {
			_timerForcePing.cancel();
			_timerForcePing.purge();
		}

		if (seconds >= 0) {
			String name = "Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerForcePing(Seconds:" + seconds + "-ID:" + ++_timerID + ")";
			_timerForcePing = new Timer(name);
			_timerForcePing.schedule(new RemindTaskForcePing(name), seconds * 1000);

		}

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

		if (_timerDiscovery != null) {
			_timerDiscovery.cancel();
			_timerDiscovery.purge();
			
			_timerDiscovery = null;

		}
		if (_timerFreshness != null) {
			_timerFreshness.cancel();
			_timerFreshness.purge();
			_timerFreshness = null;

		}

		if (_timerForcePing != null) {
			_timerForcePing.cancel();
			_timerForcePing.purge();
			_timerForcePing = null;

		}
		
		//System.out.println("\n\rAbort all timers of node:" + this.get_node().getAddress().getNetworkAddress() + "\n\r");
		

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
	 * Procedure execute when the Discovery Tiler elapsed
	 */
	class RemindTaskDiscovery extends TimerTask {
		String _name;

		RemindTaskDiscovery(String name) {
			_name = name;
		}

		@Override
		public void run() {
			//System.out.println("\n\rTimer Elapsed:" + _name+ "\n\r");
			_timerDiscovery.cancel();
			
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.DISCOVERY, (short) 0x00);

		}
	}

	/**
	 * Procedure execute when the Freshness Tiler elapsed
	 */
	class RemindTaskFreshness extends TimerTask {
		String _name;

		RemindTaskFreshness(String name) {
			_name = name;
		}

		@Override
		public void run() {
			//System.out.println("\n\rTimer Elapsed:" + _name+"("+_timerFreshness.hashCode()+") "+new Date());
			_timerFreshness.cancel();
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FRESHNESS, (short) 0x00);
			
			
		}
	}

	/**
	 * Procedure execute when the ForcePing Tiler elapsed
	 */
	class RemindTaskForcePing extends TimerTask {
		String _name;

		RemindTaskForcePing(String name) {
			_name = name;
		}

		@Override
		public void run() {
			//System.out.println("\n\rTimer Elapsed:" + _name+ "\n\r");
			_timerForcePing.cancel();
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FORCEPING, (short) 0x00);

		}
	}
}
