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
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class WrapperWSNNode {
	private boolean _onDiscovery;
	private WSNNode _node;
	private Timer _timerDiscovery;
	private Timer _timerFreshness;
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
		this._onDiscovery = false;

	}

	public synchronized WSNNode get_node() {
		return _node;
	}

	public synchronized void set_node(WSNNode _node) {
		this._node = _node;
	}

	public synchronized void setTimerDiscovery(int seconds,
			boolean forceDiscovery) {

		if (_timerDiscovery != null) {
			_timerDiscovery.cancel();

		}
		if (seconds >= 0
				&& (((!forceDiscovery) && gal.get_Gal_in_Dyscovery_state()) || (forceDiscovery))) {
			_timerDiscovery = new Timer("TimerDiscovery-Node: "
					+ this._node.getAddress().getNetworkAddress());
			_timerDiscovery.schedule(new RemindTaskDiscovery(), seconds * 1000);
		}

	}

	public synchronized void setTimerFreshness(int seconds) {

		if (_timerFreshness != null) {
			_timerFreshness.cancel();

		}
		if (seconds >= 0 && gal.get_Gal_in_Freshness_state()) {
			_timerFreshness = new Timer("TimerFreshness-Node: "
					+ this._node.getAddress().getNetworkAddress());
			_timerFreshness.schedule(new RemindTaskFreshness(), seconds * 1000);
		}

	}

	public synchronized boolean isSetTimerFreshness() {
		if (_timerFreshness == null)
			return false;
		else
			return true;

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

	}

	public synchronized void reset_numberOfAttempt() {
		this._numberOfAttempt = 0;
	}

	public synchronized boolean is_onDiscovery() {
		return _onDiscovery;
	}

	public synchronized void set_onDiscovery(boolean _onDiscovery) {
		this._onDiscovery = _onDiscovery;
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
			if (!WrapperWSNNode.this.is_onDiscovery())
				gal.getDiscoveryManager().StartDiscovery(
						WrapperWSNNode.this.get_node().getAddress());
		}
	}

	class RemindTaskFreshness extends TimerTask {
		@Override
		public void run() {
			_timerFreshness.cancel();
			gal.getDiscoveryManager().StartDiscovery(
					WrapperWSNNode.this.get_node().getAddress());
		}
	}
}
