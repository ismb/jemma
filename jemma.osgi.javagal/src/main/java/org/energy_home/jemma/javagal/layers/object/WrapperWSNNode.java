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

import java.util.Timer;
import java.util.TimerTask;
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
	private Timer _timerDiscovery;
	private Timer _timerFreshness;
	private Timer _timerForcePing;
	private Short _numberOfAttempt = 0;
	private Boolean _discoveryCompleted;
	private NodeServices _nodeServices;
	private NodeDescriptor _nodeDescriptor;
	private Mgmt_LQI_rsp _Mgmt_LQI_rsp;
	private Long lastDiscovered;

	public Timer getTimerDiscovery() {
		synchronized (_timerDiscovery) {
			return _timerDiscovery;
		}
	}

	public Timer getTimerFreshness() {
		synchronized (_timerFreshness) {
			return _timerFreshness;
		}
	}

	public Timer getTimerForcePing() {
		synchronized (_timerForcePing) {
			return _timerForcePing;
		}
	}

	public NodeDescriptor getNodeDescriptor() {
		synchronized (_nodeDescriptor) {
			return _nodeDescriptor;
		}
	}

	public void setNodeDescriptor(NodeDescriptor nodeDescriptor) {
		synchronized (_nodeDescriptor) {
			_nodeDescriptor = nodeDescriptor;
		}
	}

	public long getLastDiscovered() {
		synchronized (lastDiscovered) {
			return lastDiscovered;
		}
	}

	public void setLastDiscovered(long _lastDiscovered) {
		synchronized (lastDiscovered) {
			lastDiscovered = _lastDiscovered;
		}
	}

	private GalController gal = null;

	public WrapperWSNNode(GalController _gal) {
		gal = _gal;
		_numberOfAttempt = 0;
		lastDiscovered = 0L;
		_node = new WSNNode();
		_timerDiscovery = new Timer();
		_timerFreshness = new Timer();
		_timerForcePing = new Timer();
		_discoveryCompleted = false;
		_nodeServices = new NodeServices();
		_nodeDescriptor = new NodeDescriptor();
		_Mgmt_LQI_rsp = new Mgmt_LQI_rsp(null);
		
		

	}

	/**
	 * Check if the Node is a sleepy device
	 */
	public boolean isSleepy() {
		synchronized (_node) {
			if ((_node != null) && (_node.getCapabilityInformation() != null)) {
				if (_node.getCapabilityInformation().isReceiverOnWhenIdle())
					return false;
				else
					return true;
			} else
				return true;
		}

	}

	/**
	 * return the WsnNode from the wrapper
	 */
	public WSNNode get_node() {
		synchronized (_node) {
			return _node;
		}
	}

	/**
	 * Set the WsnNode into the Wrapper
	 */
	public synchronized void set_node(WSNNode node) {
		
		
			_node = node;
		
	}

	/**
	 * Set the Discovery Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public void setTimerDiscovery(int seconds) {
		synchronized (_timerDiscovery) {
			if (_timerDiscovery != null) {
				_timerDiscovery.cancel();
				_timerDiscovery.purge();
			}
			if (seconds >= 0) {
				String name = "";
				synchronized (this._node) {

					name = "Node: " + String.format("%04X", this._node.getAddress().getNetworkAddress()) + " -- TimerDiscovery(Seconds:" + seconds + "-ID:" + ++_timerID + ")";
				}
				_timerDiscovery = new Timer(name);
				_timerDiscovery.schedule(new RemindTaskDiscovery(name), seconds * 1000);

			}
		}

	}

	/**
	 * Set the Freshness Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public void setTimerFreshness(int seconds) {
		synchronized (_timerFreshness) {
			if (_timerFreshness != null) {
				_timerFreshness.cancel();
				_timerFreshness.purge();
			}

			if (seconds >= 0) {

				String name = "";
				synchronized (this._node) {
					name = "Node: " + String.format("%04X", this._node.getAddress().getNetworkAddress()) + " -- TimerFreshness(Seconds:" + seconds + "-ID:" + ++_timerID + ")";

				}

				_timerFreshness = new Timer(name);
				_timerFreshness.schedule(new RemindTaskFreshness(name), seconds * 1000);
			}
		}

	}

	/**
	 * Set the ForcePing Timer
	 * 
	 * @param int second --> Schedule the timer for the number of seconds passed
	 *        how parameter
	 */
	public void setTimerForcePing(int seconds) {
		synchronized (_timerForcePing) {
			if (_timerForcePing != null) {
				_timerForcePing.cancel();
				_timerForcePing.purge();
			}

			if (seconds >= 0) {
				String name = "";

				synchronized (this._node) {
					name = "Node: " + String.format("%04X", this._node.getAddress().getNetworkAddress()) + " -- TimerForcePing(Seconds:" + seconds + "-ID:" + ++_timerID + ")";

				}
				_timerForcePing = new Timer(name);
				_timerForcePing.schedule(new RemindTaskForcePing(name), seconds * 1000);

			}
		}
	}

	/**
	 * return the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public short get_numberOfAttempt() {
		synchronized (_numberOfAttempt) {
			return _numberOfAttempt;
		}
	}

	/**
	 * Increase the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public void set_numberOfAttempt() {
		synchronized (_numberOfAttempt) {
			_numberOfAttempt = (short) (this._numberOfAttempt + 1);
		}
	}

	/**
	 * Cancel all timers
	 */
	public void abortTimers() {
		synchronized (_timerDiscovery) {
			if (_timerDiscovery != null) {
				_timerDiscovery.cancel();
				_timerDiscovery.purge();

				_timerDiscovery = null;

			}
		}

		synchronized (_timerFreshness) {
			if (_timerFreshness != null) {
				_timerFreshness.cancel();
				_timerFreshness.purge();
				_timerFreshness = null;

			}
		}
		synchronized (_timerForcePing) {
			if (_timerForcePing != null) {
				_timerForcePing.cancel();
				_timerForcePing.purge();
				_timerForcePing = null;

			}
		}

	}

	/**
	 * reset the number of fail of the (Discovery, Freshness, ForcePing)
	 * procedures
	 */
	public void reset_numberOfAttempt() {
		synchronized (_numberOfAttempt) {
			_numberOfAttempt = 0;
		}
	}

	/**
	 * Return the status of the discovery. The discovery is complited when the
	 * node has sent the response of the LqiRequest
	 */
	public boolean is_discoveryCompleted() {
		synchronized (_discoveryCompleted) {
			return _discoveryCompleted;
		}
	}

	/**
	 * Set the status of the discovery. The discovery is complited when the node
	 * has sent the response of the LqiRequest
	 */
	public void set_discoveryCompleted(boolean discoveryCompleted) {
		synchronized (_discoveryCompleted) {
			_discoveryCompleted = _discoveryCompleted;
		}
	}

	/**
	 * return the Lqi Response Class of the node.
	 */
	public Mgmt_LQI_rsp get_Mgmt_LQI_rsp() {
		synchronized (_Mgmt_LQI_rsp) {
			return _Mgmt_LQI_rsp;
		}
	}

	/**
	 * Set the Lqi Response Class of the node.
	 */
	public synchronized void set_Mgmt_LQI_rsp(Mgmt_LQI_rsp _Mgmt_LQI_rsp) {
		 	_Mgmt_LQI_rsp = _Mgmt_LQI_rsp;
		
	}

	/**
	 * return the list of the EndPoints of the node.
	 */
	public NodeServices get_nodeServices() {
		synchronized (_nodeServices) {
			return _nodeServices;
		}
	}

	/**
	 * Set the list of the EndPoints of the node. Is called when is present a
	 * response of the startnodeServices
	 */
	public synchronized void set_nodeServices(NodeServices _nodeServices) {
			_nodeServices = _nodeServices;
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
			getTimerDiscovery().cancel();
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

			getTimerFreshness().cancel();

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
			getTimerForcePing().cancel();
			gal.getDiscoveryManager().startLqi(WrapperWSNNode.this.get_node().getAddress(), TypeFunction.FORCEPING, (short) 0x00);

		}
	}
}
