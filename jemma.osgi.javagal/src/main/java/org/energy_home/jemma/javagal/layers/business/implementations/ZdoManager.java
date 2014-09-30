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
import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;
import org.energy_home.jemma.zgd.jaxb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Manages received ZDO messages. When an ZDO indication is received it is
 * passed to this class' {@code ZDOMessageIndication} method.
 * 
* @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
  */
public class ZdoManager /* implements APSMessageListener */{
	private static final Logger LOG = LoggerFactory.getLogger(ZdoManager.class);

	/**
	 * The local {@link GalController} reference.
	 */
	GalController gal = null;

	/**
	 * Creates a new instance with a Gal controller reference.
	 * 
	 * @param _gal
	 *            a Gal controller reference.
	 */
	public ZdoManager(GalController _gal) {
		gal = _gal;
	}

	/**
	 * Processes the ZDO indication message trying to dispatch it to the right
	 * destinations. This method takes a ZDO message, looks on it to understand
	 * which ZDO command is contained on it, executes it and at last notifies it
	 * to both the registered APS callbacks' listeners and to the ZDO command's
	 * listeners.
	 * 
	 * @param message
	 *            the ZDO message to process.
	 */
	public void ZDOMessageIndication(APSMessageEvent message) {

		/* MGMT_LQI_Response */
		if (message.getClusterID() == 0x8031 && (gal.getPropertiesManager().getDebugEnabled())) {
            LOG.debug("Extracted APS With a MGMT_LQI_Response");

        }
		/* MGMT_LQI_Request */
		else if (message.getClusterID() == 0x0031 && (gal.getPropertiesManager().getDebugEnabled())) {
            LOG.debug("Extracted APS With a MGMT_LQI_Request");
        }
		/* Node_Desc_req */
		else if (message.getClusterID() == 0x0002 && (gal.getPropertiesManager().getDebugEnabled())) {
            LOG.debug("Extracted APS With a Node_Desc_req");
        }
		/* Node_Desc_rsp */
		else if (message.getClusterID() == 0x8002 && (gal.getPropertiesManager().getDebugEnabled())) {
            LOG.debug("Extracted APS With a Node_Desc_rsp");
        }
		/* Leave_rsp */
		else if (message.getClusterID() == 0x8034) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				LOG.debug("Extracted APS With a Leave_rsp");
			}
			WSNNode _nodeRemoved = new WSNNode();
			Address _add = message.getSourceAddress();
			_nodeRemoved.setAddress(_add);
			byte _status = message.getData()[0];
			if (_status == 0x00) {
				int _index;
				synchronized (gal.getNetworkcache()) {
					if ((_index = gal.existIntoNetworkCache(_add)) != -1) {
						gal.getNetworkcache().remove(_index);
						Status _s = new Status();
						_s.setCode((short) 0x00);
						_s.setMessage("Successful - Device Removed by Leave Response");
						try {
							gal.get_gatewayEventManager().nodeRemoved(_s, _nodeRemoved);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		/* ZDP Device_announcement */
		else if (message.getClusterID() == 0x0013) {

			WrapperWSNNode _Node = new WrapperWSNNode(gal);
			WSNNode n = new WSNNode();
			Address _add = new Address();
			_add.setNetworkAddress(DataManipulation.toIntFromShort(message.getData()[2], message.getData()[1]));
			byte[] _IEEE = new byte[8];
            for(int i = 0; i < _IEEE.length; i++)
                _IEEE[i] = message.getData()[10 - i];
			_add.setIeeeAddress(new BigInteger(_IEEE));
			n.setAddress(_add);
			byte _Capability = message.getData()[11];
			byte _AlternatePANCoordinator = (byte) (_Capability & 0x01);/* bit0 */
			byte _PowerSource = (byte) ((_Capability & 0x04) >> 2);/* bit2 */
			byte _ReceiverOnWhenIdle = (byte) ((_Capability & 0x08) >> 3);/* bit3 */
			byte _SecurityCapability = (byte) ((_Capability & 0x40) >> 6);/* bit6 */
			byte _AllocateAddress = (byte) ((_Capability & 0x80) >> 7);/* bit7 */
			MACCapability _mac = new MACCapability();
			_mac.setAllocateAddress((_AllocateAddress == 1));
			_mac.setAlternatePanCoordinator((_AlternatePANCoordinator == 1));
			_mac.setMainsPowered((_PowerSource == 1));
			_mac.setReceiverOnWhenIdle((_ReceiverOnWhenIdle == 1));
			_mac.setSecuritySupported((_SecurityCapability == 1));
			n.setCapabilityInformation(_mac);
			_Node.set_node(n);
			_Node.set_discoveryCompleted(true);
			_Node.reset_numberOfAttempt();
            synchronized (gal.getNetworkcache()) {
				if (gal.existIntoNetworkCache(_Node.get_node().getAddress()) == -1) {
					/* id not exist */
					if (gal.getPropertiesManager().getDebugEnabled())
						LOG.info("Adding node from Node Announcement: " + _Node.get_node().getAddress().getNetworkAddress());

					gal.getNetworkcache().add(_Node);
					if (!_Node.isSleepy()) {
						if (gal.getPropertiesManager().getKeepAliveThreshold() > 0) {
							_Node.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
						}
						if (gal.getPropertiesManager().getForcePingTimeout() > 0) {
							_Node.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());
						}
					}
					/* Saving the Panid in order to leave the Philips light */
					gal.getManageMapPanId().setPanid(_Node.get_node().getAddress().getIeeeAddress(), gal.getNetworkPanID());
					/**/

					Status _s = new Status();
					_s.setCode((short) 0x00);
					try {
						gal.get_gatewayEventManager().nodeDiscovered(_s, _Node.get_node());
					} catch (Exception e) {

						LOG.error("Error on Received ZDP Device_announcement: " + _Node.get_node().getAddress().getNetworkAddress() + "--" + e.getMessage());

					}
					if (gal.getPropertiesManager().getDebugEnabled()) {
						LOG.debug("Received ZDP Device_announcement: " + _Node.get_node().getAddress().getNetworkAddress());
					}
				}
			}

		}

		gal.getApsManager().APSMessageIndication(message);
		gal.getMessageManager().APSMessageIndication(message);

		// TODO ZDPMessage
		ZDPMessage _zdpM = new ZDPMessage();
		_zdpM.setClusterID(message.getClusterID());
		_zdpM.setCommand(message.getData());
		_zdpM.setLinkQuality(message.getLinkQuality());
		_zdpM.setRxTime(message.getRxTime());
		_zdpM.setSourceAddress(message.getSourceAddress());
		_zdpM.setSourceAddressMode(message.getSourceAddressMode());
		gal.get_gatewayEventManager().notifyZDPEvent(_zdpM);
	}
}
