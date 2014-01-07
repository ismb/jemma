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

import org.energy_home.jemma.zgd.jaxb.NodeServices;
import org.energy_home.jemma.zgd.jaxb.WSNNode;

import java.util.Timer;
import java.util.TimerTask;

import org.energy_home.jemma.javagal.layers.business.GalController;

/**
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class WrapperWSNNode {

	private WSNNode _node;
	private Timer _timerDiscovery;
	private Timer _timerFreshness;
	private Timer _timerForcePing;
	private short _numberOfAttempt;
	private boolean _discoveryCompleted;
	private NodeServices _nodeServices;
	private Mgmt_LQI_rsp _Mgmt_LQI_rsp;

	private GalController gal = null;

	public WrapperWSNNode(GalController _gal) {
		gal = _gal;
		this._node = null;
		this._timerDiscovery = null;
		this._timerFreshness = null;
		this._numberOfAttempt = 0;

	}

	public boolean isSleepy() {
		if ((_node != null) && (_node.getCapabilityInformation() != null)) {
			if (_node.getCapabilityInformation().isReceiverOnWhenIdle())
				return false;
			else
				return true;
		} else
			return true;

	}

	public synchronized WSNNode get_node() {
		return _node;
	}

	public synchronized void set_node(WSNNode _node) {
		this._node = _node;
	}

	public synchronized void setTimerDiscovery(int seconds) {

		if (_timerDiscovery != null) {
			_timerDiscovery.cancel();
			_timerDiscovery.purge();

		}
		if (seconds >= 0) {
			_timerDiscovery = new Timer("Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerDiscovery");
			_timerDiscovery.schedule(new RemindTaskDiscovery(), seconds * 1000);
		}

	}

	public synchronized void setTimerFreshness(int seconds) {

		if (_timerFreshness != null) {
			_timerFreshness.cancel();
			_timerFreshness.purge();

		}
		if (seconds >= 0) {
			_timerFreshness = new Timer("Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerFreshness");
			_timerFreshness.schedule(new RemindTaskFreshness(), seconds * 1000);
		}

	}

	public synchronized void setTimerForcePing(int seconds) {

		if (_timerForcePing != null) {
			_timerForcePing.cancel();
			_timerForcePing.purge();
		}
		if (seconds >= 0) {
			_timerForcePing = new Timer("Node: " + this._node.getAddress().getNetworkAddress() + " -- TimerForcePing");
			_timerForcePing.schedule(new RemindTaskForcePing(), seconds * 1000);
		}

	}

	public synchronized short get_numberOfAttempt() {
		return _numberOfAttempt;
	}

	public synchronized void set_numberOfAttempt() {
		this._numberOfAttempt = (short) (this._numberOfAttempt + 1);
	}

	public synchronized void abortTimers() {

		if (_timerDiscovery != null) {
			_timerDiscovery.cancel();
			_timerDiscovery = null;
			
		}
		if (_timerFreshness != null) {
			_timerFreshness.cancel();
			_timerFreshness = null;
			
		}

		if (_timerForcePing != null) {
			_timerForcePing.cancel();
			_timerForcePing = null;
		
		}

	}

	public synchronized void reset_numberOfAttempt() {
		this._numberOfAttempt = 0;
	}

	public synchronized boolean is_discoveryCompleted() {
		return _discoveryCompleted;
	}

	public synchronized void set_discoveryCompleted(boolean _discoveryCompleted) {
		this._discoveryCompleted = _discoveryCompleted;
	}

	public synchronized Mgmt_LQI_rsp get_Mgmt_LQI_rsp() {
		return _Mgmt_LQI_rsp;
	}

	public synchronized void set_Mgmt_LQI_rsp(Mgmt_LQI_rsp _Mgmt_LQI_rsp) {
		this._Mgmt_LQI_rsp = _Mgmt_LQI_rsp;
	}

	public synchronized NodeServices get_nodeServices() {
		return _nodeServices;
	}

	public synchronized void set_nodeServices(NodeServices _nodeServices) {
		this._nodeServices = _nodeServices;
	}

	class RemindTaskDiscovery extends TimerTask {
		@Override
		public void run() {
			_timerDiscovery.cancel();

			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.DISCOVERY, (short) 0x00);

		}
	}

	class RemindTaskFreshness extends TimerTask {
		@Override
		public void run() {
			_timerFreshness.cancel();
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FRESHNESS, (short) 0x00);
		}
	}

	class RemindTaskForcePing extends TimerTask {
		@Override
		public void run() {
			_timerForcePing.cancel();
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FORCEPING, (short) 0x00);
		}
	}
}
