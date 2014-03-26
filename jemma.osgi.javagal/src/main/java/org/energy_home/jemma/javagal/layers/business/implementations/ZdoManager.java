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

import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.MACCapability;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.WSNNode;
import org.energy_home.jemma.zgd.jaxb.ZDPMessage;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;
import org.energy_home.jemma.javagal.layers.object.WrapperWSNNode;

/**
 * Manages received ZDO messages. When an ZDO indication is received it is
 * passed to this class' {@code ZDOMessageIndication} method.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 */
public class ZdoManager /* implements APSMessageListener */{
	private static Log logger = LogFactory.getLog(ZdoManager.class);

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
		if (message.getClusterID() == 0x8031) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("**************************Extracted APS With a MGMT_LQI_Response");
			
			}
			
			
			
		 	
			
		}
		/* MGMT_LQI_Request */
		else if (message.getClusterID() == 0x0031) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("**************************Extracted APS With a MGMT_LQI_Request");
			}
		}
		/* Node_Desc_req */
		else if (message.getClusterID() == 0x0002) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("**************************Extracted APS With a Node_Desc_req");
			}
		}
		/* Node_Desc_rsp */
		else if (message.getClusterID() == 0x8002) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("**************************Extracted APS With a Node_Desc_rsp");
			}
		}
		/* Leave_rsp */
		else if (message.getClusterID() == 0x8034) {
			if (gal.getPropertiesManager().getDebugEnabled()) {
				logger.info("**************************Extracted APS With a Leave_rsp");
			}
			WSNNode _nodeRemoved = new WSNNode();
			Address _add = message.getSourceAddress();
			_nodeRemoved.setAddress(_add);
			byte _status = message.getData()[0];
			if (_status == 0x00) {
				int _index = -1;
				if ((_index = gal.existIntoNetworkCache(_add.getNetworkAddress())) != -1) {
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
		/* ZDP Device_announcement */
		else if (message.getClusterID() == 0x0013) {

			WrapperWSNNode _Node = new WrapperWSNNode(gal);
			WSNNode n = new WSNNode();
			Address _add = new Address();
			_add.setNetworkAddress(DataManipulation.toIntFromShort(message.getData()[2], message.getData()[1]));
			byte[] _IEEE = new byte[8];
			_IEEE[0] = message.getData()[10];
			_IEEE[1] = message.getData()[9];
			_IEEE[2] = message.getData()[8];
			_IEEE[3] = message.getData()[7];
			_IEEE[4] = message.getData()[6];
			_IEEE[5] = message.getData()[5];
			_IEEE[6] = message.getData()[4];
			_IEEE[7] = message.getData()[3];
			_add.setIeeeAddress(new BigInteger(_IEEE));
			n.setAddress(_add);
			byte _Capability = message.getData()[11];
			byte _AlternatePANCoordinator = (byte) (_Capability & 0x01);/* bit0 */
			byte _PowerSource = (byte) ((_Capability & 0x04) >> 2);/* bit2 */
			byte _ReceiverOnWhenIdle = (byte) ((_Capability & 0x08) >> 3);/* bit3 */
			byte _SecurityCapability = (byte) ((_Capability & 0x40) >> 6);/* bit6 */
			byte _AllocateAddress = (byte) ((_Capability & 0x80) >> 7);/* bit7 */
			MACCapability _mac = new MACCapability();
			_mac.setAllocateAddress((_AllocateAddress == 1 ? true : false));
			_mac.setAlternatePanCoordinator((_AlternatePANCoordinator == 1 ? true : false));
			_mac.setMainsPowered((_PowerSource == 1 ? true : false));
			_mac.setReceiverOnWhenIdle((_ReceiverOnWhenIdle == 1 ? true : false));
			_mac.setSecuritySupported((_SecurityCapability == 1 ? true : false));
			n.setCapabilityInformation(_mac);
			_Node.set_node(n);
			_Node.set_discoveryCompleted(true);
			_Node.reset_numberOfAttempt();
			int _index = -1;
			synchronized (gal) {
				if ((_index = gal.existIntoNetworkCache(_Node.get_node().getAddress().getNetworkAddress())) == -1) {
					/* id not exist */
					gal.getNetworkcache().add(_Node);
					if (!_Node.isSleepy()) {
						if (gal.getPropertiesManager().getKeepAliveThreshold() > 0) {
							_Node.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
						}
						if (gal.getPropertiesManager().getForcePingTimeout() > 0) {
							_Node.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());
						}
					}
				} else/* if exist */{
					gal.getNetworkcache().get(_index).abortTimers();
					gal.getNetworkcache().remove(_index);
					gal.getNetworkcache().add(_Node);
					if (!_Node.isSleepy()) {
						if (gal.getPropertiesManager().getKeepAliveThreshold() > 0) {
							_Node.setTimerFreshness(gal.getPropertiesManager().getKeepAliveThreshold());
						}
						if (gal.getPropertiesManager().getForcePingTimeout() > 0) {
							_Node.setTimerForcePing(gal.getPropertiesManager().getForcePingTimeout());
						}
					}
				}
			}
			Status _s = new Status();
			_s.setCode((short) 0x00);
			try {
				gal.get_gatewayEventManager().nodeDiscovered(_s, _Node.get_node());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (gal.getPropertiesManager().getDebugEnabled()) {
				{
					logger.info("Received ZDP Device_announcement: " + _Node.get_node().getAddress().getNetworkAddress());

				}
			}

		}

		gal.getApsManager().APSMessageIndication(message);
		// TODO ZDPMessage
		ZDPMessage _zdpM = new ZDPMessage();
		_zdpM.setClusterID(message.getClusterID());
		_zdpM.setCommand(message.getData());
		_zdpM.setLinkQuality(message.getLinkQuality());
		_zdpM.setRxTime(message.getRxTime());
		_zdpM.setSourceAddress(message.getSourceAddress());
		_zdpM.setSourceAddressMode(message.getSourceAddressMode());
		gal.get_gatewayEventManager().notifyZDPCommand(_zdpM);
	}
}
