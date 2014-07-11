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
package org.energy_home.jemma.ah.zigbee.zcl.lib;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.InvalidAttributeValueException;
import org.energy_home.jemma.ah.hac.MalformedMessageException;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.NotFoundException;
import org.energy_home.jemma.ah.hac.ReadOnlyAttributeException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.ZigBeeException;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.IZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.ZclException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.ZclWriteAttributeRecord;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclAbstractDataType;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO consider also the differences between general and cluster specific commands

public class ZclServiceCluster extends ServiceCluster implements IZclServiceCluster, ZigBeeDeviceListener {
	protected static final String BAD_DIRECTION_MESSAGE = "bad direction field";
	protected static final String BAD_RESPONSE_COMMAND_ID_MESSAGE = "bad command id in response";
	protected static final String INVOKE_ERROR_MESSAGE = "error in invoke";
	protected static final String POST_FAILED_MESSAGE = "error in post";
	protected static final String ERROR_PARSING_MESSAGE = "Error parsing ZigBee message";

	protected ZigBeeDevice device;

	protected int sequence = 30; // Zcl Frame sequence number
	private static final Logger LOG = LoggerFactory.getLogger( ZclServiceCluster.class );

	private boolean checkDirection = false;

	private IZclFrame deviceInvoke(short clusterId, IZclFrame zclFrame) throws ZigBeeException {
		int profileId = getProfileId();
		IZclFrame zclResponseFrame;
		if (profileId == -1)
			zclResponseFrame = device.invoke(clusterId, zclFrame);
		else
			zclResponseFrame = device.invoke((short) profileId, clusterId, zclFrame);
		return zclResponseFrame;
	}

	private boolean devicePost(short clusterId, IZclFrame zclFrame) {
		int profileId = getProfileId();
		if (profileId == -1)
			return device.post(clusterId, zclFrame);
		else
			return device.post((short) profileId, clusterId, zclFrame);
	}

	public ZclServiceCluster() throws ApplianceException {
		super();
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
		// Override in childs
		return null;
	}

	protected IZclAttributeDescriptor getAttributeDescriptor(int attrId) {
		// Override in childs
		return null;
	}

	protected Collection getAttributeDescriptors() {
		return null;
	}
	
	protected IZclAttributeDescriptor[] getPeerAttributeDescriptors() {
		return null;
	}

	protected Collection fillSupportedAttributes() {
		return null;
	}

	protected static Map fillAttributesMapsById(IZclAttributeDescriptor[] attributeDescriptors, Map attributesMapById) {
		if (attributesMapById == null) {
			attributesMapById = new HashMap();
			for (int i = 0; i < attributeDescriptors.length; i++) {
				attributesMapById.put(attributeDescriptors[i].zclGetId(), attributeDescriptors[i]);
			}
		}
		return attributesMapById;
	}

	protected static Map fillAttributesMapsByName(IZclAttributeDescriptor[] attributeDescriptors, Map attributesMapByName) {
		if (attributesMapByName == null) {
			attributesMapByName = new HashMap();
			for (int i = 0; i < attributeDescriptors.length; i++) {
				attributesMapByName.put(attributeDescriptors[i].getName(), attributeDescriptors[i]);
			}
		}
		return attributesMapByName;
	}

	/**
	 * Getter for the checkDirection field
	 * 
	 * @return true if the direction is checked on incoming Zcl messages, false
	 *         otherwise
	 */

	protected boolean getCheckDirection() {
		return this.checkDirection;
	}

	/**
	 * Permits to enable/disable the direction field check on incoming Zcl
	 * messages. The default behaviour is: not check the direction.
	 * 
	 * @param checkDirection
	 *            true enables check on the direction field of the incoming Zcl
	 *            messages. false disables this check. By default the direction
	 *            field check is disabled.
	 */

	protected void setCheckDirection(boolean checkDirection) {
		this.checkDirection = checkDirection;
	}

	protected IZclFrame readAttributes(short clusterId, int[] attrIds) {
		int sequence = 0;
		IZclFrame zclFrame = new ZclFrame((byte) 0x00, attrIds.length * 2);
		zclFrame.setSequence(sequence);

		zclFrame.appendUInt8(ZCL.ZclReadAttrs);

		for (int i = 0; i < attrIds.length; i++) {
			zclFrame.appendUInt16(attrIds[i]);
		}

		return zclFrame;
	}

	protected IServiceCluster getSinglePeerCluster(String name) throws ServiceClusterException {
		IServiceCluster[] serviceClusters = ((EndPoint) endPoint).getPeerServiceClusters(name);

		if (serviceClusters == null) {
			throw new ServiceClusterException("No appliances connected");
		} else if (serviceClusters.length > 1) {
			// FIXME: we can relax this by checking if the command don't need
			// any response.
			// throw new
			// ServiceClusterException("More than one appliance connected.");
		} else if (serviceClusters.length == 0) {
			throw new ServiceClusterException("Service Clusters List is empty!!!");
		}
		return serviceClusters[0];
	}

	protected IServiceCluster getSinglePeerClusterNoException(String name) throws ServiceClusterException {
		IServiceCluster[] serviceClusters = ((EndPoint) endPoint).getPeerServiceClusters(name);

		if (serviceClusters == null) {
			throw new ServiceClusterException("No appliances connected");
		} else if (serviceClusters.length > 1) {
			// FIXME: we can relax this by checking if the command don't need
			// any response.
			// throw new
			// ServiceClusterException("More than one appliance connected.");
		} else if (serviceClusters.length == 0) {
			return null;
		}
		return serviceClusters[0];
	}

	/**
	 * Given a commandId and a size, it creates a ZclFrame object of the correct
	 * size, direction. The direction is set according to the cluster type. If
	 * it is a client cluster, the direction is set C to S. The method sets also
	 * the sequence number accordingly
	 * 
	 * @param commandId
	 *            The commandId of the newly created frame
	 * @param size
	 *            The total size.
	 * @return The newly created ZclFrame.
	 */

	protected IZclFrame createOutgoingZclFrame(int commandId, int size) {
		IZclFrame zclFrame = new ZclFrame((byte) 0x00, size);

		if (this.getSide() == IZclServiceCluster.CLIENT_SIDE)
			zclFrame.setDirection(IZclFrame.SERVER_TO_CLIENT_DIRECTION);
		else
			zclFrame.setDirection(IZclFrame.CLIENT_TO_SERVER_DIRECTION);

		zclFrame.setSequence(sequence++);
		zclFrame.setCommandId(commandId);
		return zclFrame;
	}

	protected ISubscriptionParameters[] configureReportings(int clusterId, String[] attrNames,
			ISubscriptionParameters[] parameters, IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException, ZclValidationException {

		boolean sync = true;

		ZigBeeDevice device = getZigBeeDevice();
		if (device == null)
			throw new ApplianceException("Not attached");

		// The size is set to 255 since we don't know in advance the actual
		// packet size
		IZclFrame zclFrame = this.createOutgoingZclFrame(ZCL.ZclConfigRep, 255);

		for (int i = 0; i < attrNames.length; i++) {
			String attrName = attrNames[i];
			IZclAttributeDescriptor zclAttributeDescriptor = getAttributeDescriptor(attrName);
			if (!zclAttributeDescriptor.isReportable())
				throw new ServiceClusterException("Attribute '" + attrName + "' is not reportable");

			ISubscriptionParameters parameter = parameters[i];

			int direction = 0x00;
			int timeout = 0;

			zclFrame.appendUInt8(direction);
			zclFrame.appendUInt16(zclAttributeDescriptor.zclGetId());

			ZclAbstractDataType zclDataType = zclAttributeDescriptor.zclGetDataType();

			if (direction == 0x00) {
				zclFrame.appendUInt8(zclDataType.zclGetDataType());
				if (parameter != null) {
					zclFrame.appendUInt16((int) parameter.getMinReportingInterval());
					zclFrame.appendUInt16((int) parameter.getMaxReportingInterval());
				} else {
					// disable attribute reporting on this attribute
					zclFrame.appendUInt16(0x0000);
					zclFrame.appendUInt16(0xffff); // disable!
				}

			} else if (direction == 0x01) {
				zclFrame.appendUInt16(timeout);
			}

			if (zclDataType.isAnalog()) {
				// FIXME there are some issue here...
				if (parameter != null) {
					zclDataType.zclObjectSerialize(zclFrame, new Integer((int) parameter.getReportableChange()));
				} else {
					zclDataType.zclObjectSerialize(zclFrame, new Integer(1));
				}
			}

			zclFrame.shrink();

			if ((context != null) && (!context.isConfirmationRequired())) {
				zclFrame.disableDefaultResponse(true);
				sync = false;
			}

			if (sync) {
				IZclFrame zclResponseFrame = deviceInvoke((short) clusterId, zclFrame);
				if (zclResponseFrame == null)
					throw new ApplianceException("Timeout");

				// TODO: check if the response frame is correct
				if (zclResponseFrame.getCommandId() == ZCL.ZclConfigRepRsp) {
					short status = ZclDataTypeUI8.zclParse(zclResponseFrame);
					if (status != ZCL.SUCCESS)
						this.raiseServiceClusterException(status);
				} else if (zclResponseFrame.getCommandId() == ZCL.ZclDefaultRsp) {
					short status = ZclDataTypeUI8.zclParse(zclResponseFrame);
					if (status != ZCL.SUCCESS) {
						this.raiseServiceClusterException(status);
					}
				} else
					throw new ServiceClusterException("Response command doesn't match the request command");

			} else {
				devicePost((short) clusterId, zclFrame);
				return null;
			}
		}

		return parameters;
	}

	protected static short translateToZclStatusCode(Throwable e) {
		if (e instanceof ZclException) {
			return (short) ((ZclException) e).getStatusCode();
		} else if (e instanceof ServiceClusterException) {
			if (e instanceof UnsupportedClusterOperationException) {
				return ZCL.UNSUP_CLUSTER_COMMAND;
			} else if (e instanceof UnsupportedClusterAttributeException) {
				return ZCL.UNSUPPORTED_ATTRIBUTE;
			} else if (e instanceof ReadOnlyAttributeException) {
				return ZCL.READ_ONLY;
			} else if (e instanceof NotAuthorized) {
				return ZCL.NOT_AUTHORIZED;
			} else if (e instanceof MalformedMessageException) {
				return ZCL.MALFORMED_COMMAND;
			} else if (e instanceof NotFoundException) {
				return ZCL.NOT_FOUND;
			} else if (e instanceof ZclValidationException) {
				return ZCL.MALFORMED_COMMAND;
			} else
				return ZCL.FAILURE;
		} else if (e instanceof ApplianceException) {
			return ZCL.FAILURE;
		}
		return ZCL.NOT_AUTHORIZED;
	}

	/**
	 * Given a Zcl status code, this method convert it in the corresponding
	 * specific ServiceClusterException that represents this status code. No
	 * specific exception is found a generic ServiceClusterException exception
	 * is raised, where the status code is set to the Zcl status code.
	 * 
	 * @param status
	 * @throws ServiceClusterException
	 */

	protected void raiseServiceClusterException(short status) throws ServiceClusterException {
		// TODO: cover all the Zcl status codes

		switch (status) {
		case ZCL.UNSUPPORTED_ATTRIBUTE:
			throw new UnsupportedClusterAttributeException("Zcl unsupported attribute: " + status);

		case ZCL.UNREPORTABLE_ATTRIBUTE:
			throw new UnsupportedClusterOperationException("Zcl unreportable attribute: " + status);

		case ZCL.INVALID_VALUE:
			throw new InvalidAttributeValueException();

		case ZCL.READ_ONLY:
			throw new ReadOnlyAttributeException();

		case ZCL.NOT_AUTHORIZED:
			throw new NotAuthorized();

		case ZCL.MALFORMED_COMMAND:
			throw new MalformedMessageException();

		default:
		}

		throw new ServiceClusterException("Got Zcl Status Code: " + status);
	}

	protected ZigBeeDevice getZigBeeDevice() throws ApplianceException {
		return device;
	}

	// FIXME: qui passare anche l'attr Type!!!!
	// in questo modo e' possible fare il check se la risposta e' del tipo
	// corretto!!!!

	protected IZclFrame readAttribute(short clusterId, int attrId, boolean sync) throws Exception {

		ZigBeeDevice device = getZigBeeDevice();

		if (device == null)
			throw new ApplianceException("Not attached");

		IZclFrame zclFrame = this.createOutgoingZclFrame(ZCL.ZclReadAttrs, 2);
		zclFrame.appendUInt16(attrId);

		if (sync) {
			IZclFrame zclResponseFrame = deviceInvoke(clusterId, zclFrame);

			if (zclResponseFrame == null)
				throw new ApplianceException("Timeout");

			// TODO: check if the response frame is correct
			if (zclResponseFrame.getCommandId() != ZCL.ZclReadAttrsRsp)
				throw new ApplianceException("Response command doesn't match the request command");

			int responseAttrId = ZclDataTypeUI16.zclParse(zclResponseFrame);
			if (responseAttrId != attrId)
				throw new ApplianceException("Response attrId doesn't match requeted one");

			short status = ZclDataTypeUI8.zclParse(zclResponseFrame);
			if (status != ZCL.SUCCESS)
				this.raiseServiceClusterException(status);

			short responseDataType = ZclDataTypeUI8.zclParse(zclResponseFrame);

			short dataType = responseDataType;
			if (responseDataType != dataType)
				throw new ZclException("response data type doesn't match request data type", ZCL.INVALID_DATA_TYPE);

			return zclResponseFrame;
		} else {
			devicePost(clusterId, zclFrame);
			return null;
		}
	}

	protected IZclFrame readAttribute(int attrId, IEndPointRequestContext context) throws ServiceClusterException,
			ApplianceException {

		boolean sync = true;

		ZigBeeDevice device = getZigBeeDevice();

		if (device == null)
			throw new ApplianceException("Not attached");

		IZclFrame zclFrame = this.createOutgoingZclFrame(ZCL.ZclReadAttrs, 2);
		zclFrame.appendUInt16(attrId);

		if ((context != null) && (!context.isConfirmationRequired())) {
			zclFrame.disableDefaultResponse(true);
			// sync = false;
		}

		if (sync) {
			IZclFrame zclResponseFrame = deviceInvoke((short) getClusterId(), zclFrame);
			if (zclResponseFrame == null)
				throw new ApplianceException("Timeout");

			// TODO: check if the response frame is correct
			int responseCommandId = zclResponseFrame.getCommandId();

			if (responseCommandId == ZCL.ZclReadAttrsRsp) {

				try {
					int responseAttrId = ZclDataTypeUI16.zclParse(zclResponseFrame);
					if (responseAttrId != attrId)
						throw new ApplianceException("Response attrId doesn't match requeted one");

					short status = ZclDataTypeUI8.zclParse(zclResponseFrame);
					if (status != ZCL.SUCCESS)
						throw new ApplianceException("Error: zigbee status " + status);

					short responseDataType = ZclDataTypeUI8.zclParse(zclResponseFrame);

					short dataType = responseDataType;
					if (responseDataType != dataType)
						this.raiseServiceClusterException(ZCL.INVALID_DATA_TYPE);

					if (this.checkDirection && (zclFrame.getDirection() == zclResponseFrame.getDirection())) {
						// this is an error, since the direction of the outgoing
						// frame cannot be identical to dir of the received
						// frame.
						LOG.warn(BAD_DIRECTION_MESSAGE);
						throw new ServiceClusterException("bad direction field in incoming packet");
					}
				} catch (ZclValidationException e) {
					this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
				}
			} else if (responseCommandId == ZCL.ZclDefaultRsp) {

				short commandId = 0;
				try {
					commandId = ZclDataTypeUI8.zclParse(zclResponseFrame);
				} catch (ZclValidationException e1) {
					this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
				}

				if (commandId != ZCL.ZclReadAttrs)
					throw new ServiceClusterException("Expected default response for ReadAttributes, received " + commandId);

				short status = 0;
				try {
					status = ZclDataTypeUI8.zclParse(zclResponseFrame);
				} catch (ZclValidationException e) {
					this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
				}

				if (status != ZCL.SUCCESS) {
					this.raiseServiceClusterException(status);
				}

			} else {
				throw new ApplianceException("Response command doesn't match the request command");
			}
			return zclResponseFrame;
		} else {
			devicePost((short) getClusterId(), zclFrame);
			return null;
		}
	}

	protected IZclFrame getDefaultResponse(IZclFrame zclFrame, int statusCode) {
		IZclFrame responseZclFrame = zclFrame.createResponseFrame(2);
		responseZclFrame.setCommandId(ZCL.ZclDefaultRsp);
		responseZclFrame.appendUInt8(zclFrame.getCommandId());
		responseZclFrame.setFrameType(IZclFrame.GENERAL_COMMAND);
		responseZclFrame.appendUInt8(statusCode);
		return responseZclFrame;
	}

	public void zclAttach(ZigBeeDevice device) {
		device.setListener((short) this.getClusterId(), this.getSide(), this);
		this.device = device;
	}

	public void zclDetach(ZigBeeDevice device) {
		device.removeListener((short) this.getClusterId(), this.getSide(), this);
		this.device = null;
	}

	public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame) throws Exception {

		this.checkIncomingFrameDirection(zclFrame);

		if (zclFrame.getFrameType() == IZclFrame.GENERAL_COMMAND) {
			handleGeneralCommands(clusterId, zclFrame);
			return true;
		}

		// FIXME: what if we receive an other side response command?

		// Added to manage automatic announcement in case a subscription is already active
		IEndPoint ep = this.getEndPoint();
		if (ep != null && ep.isAvailable())
			((ZclAppliance) this.getEndPoint().getAppliance()).notifyEvent(ZigBeeDeviceListener.ANNOUNCEMENT);

		return false;
	}

	protected int getClusterId() {
		return -1;
	}

	protected int getProfileId() {
		return -1;
	}

	public ISubscriptionParameters setAttributeSubscription(String attributeName, ISubscriptionParameters parameters,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException, ServiceClusterException {
		// FIXME: currently sleeping end device is supposed to later receive the
		// subscription (no error managed)
		// TODO: add configure reporting read operation
		ISubscriptionParameters result = null;
		parameters = super.initAttributeSubscription(attributeName, parameters, endPointRequestContext);
		ISubscriptionParameters[] sps = null;
		try {
			sps = configureReportings(getClusterId(), new String[] { attributeName }, new ISubscriptionParameters[] { parameters },
					endPointRequestContext);
			result = sps[0];
		} catch (Exception e) {
			LOG.warn("Error while subscribing attribute " + attributeName + " for driver appliance "
					+ this.getEndPoint().getAppliance().getPid() + ". Maybe this is a sleeping end device!",e);
			sps = new ISubscriptionParameters[] { parameters };
		}
		updateAllSubscriptionMap(attributeName, sps[0], endPointRequestContext);
		// A null value is returned if configure reportings command fails 
		return result;
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		throw new UnsupportedClusterOperationException();
	}

	/**
	 * This method must be redefined by the children classes. It should read
	 * from the peer appliances the requested attribute and, if found it should
	 * marshal this attribute in the passed frame object.
	 * 
	 * @param zclResponseFrame
	 * @param attrId
	 * @return true if the attribute has been handled, false, otherwise
	 * @throws ZclValidationException
	 * 
	 *             TODO: allineare documentazione
	 */

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ServiceClusterException,
			ApplianceException {
		return false;
	}

	/**
	 * The writeAttribute method have to be overriden by the cluster stub
	 * implementation if the cluster has at least one writable attribute. It
	 * must return 0 (see ZCL.SUCCESS) if the write operation has been
	 * successful. Otherwise it could return the ZCL status code. The method
	 * could also raise a ServiceClusterException. There is one specific
	 * exception for each ZCL status code. If the raised exception is
	 * UnsupportedClusterAttributeException or ReadOnlyAttribute, than the
	 * ZclServiceCluster supposes that the writeAttribute implementation has
	 * already removed the attribute value from the ZCL frame.
	 * 
	 * @param zclFrame
	 * @param attrId
	 * @param dataType
	 * @return
	 * @throws Exception
	 */

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {
		return ZCL.UNSUPPORTED_ATTRIBUTE;
	}

	/**
	 * Handles incoming General Command Frames
	 * 
	 * @param clusterId
	 *            The Cluster identifier of the received general command
	 * @param zclFrame
	 *            The received zclFrame
	 * @throws ServiceClusterException
	 *             This exeption is raised in case of error
	 */
	protected void handleGeneralCommands(short clusterId, IZclFrame zclFrame) throws ZclException {
		int commandId = zclFrame.getCommandId();

		if (zclFrame.isManufacturerSpecific()) {
			throw new ZclException(ZCL.UNSUP_MANUF_GENERAL_COMMAND);
		}

		switch (commandId) {
		case ZCL.ZclReadAttrs: {
			// Handles an incoming Read Attributes ZCL Command
			int attributesIdsNumber = (zclFrame.getPayloadSize() / 2);

			int[] attrIds = new int[attributesIdsNumber];

			int size = 0;
			for (int i = 0; i < attributesIdsNumber; i++) {
				try {
					attrIds[i] = ZclDataTypeUI16.zclParse(zclFrame);
				} catch (ZclValidationException e) {

				}
				try {
					// initially supposes that all the attributes are returned
					size += readAttributeResponseGetSize(attrIds[i]);
				} catch (ServiceClusterException e) {
					continue;
				}
			}

			IZclFrame zclResponseFrame = zclFrame.createResponseFrame(size + 4 * attributesIdsNumber);
			zclResponseFrame.setCommandId(ZCL.ZclReadAttrsRsp);

			for (int i = 0; i < attributesIdsNumber; i++) {
				try {
					ZclDataTypeUI16.zclSerialize(zclResponseFrame, attrIds[i]);
				} catch (Exception e1) {
					// FIXME: what I have to do here?
					LOG.error("Exception iterating through attributeIDSNumbers",e1);
					return;
				}

				boolean handled = true;
				try {
					handled = fillAttributeRecord(zclResponseFrame, attrIds[i]);
					if (!handled)
						ZclDataTypeUI8.zclSerialize(zclResponseFrame, (byte) ZCL.UNSUPPORTED_ATTRIBUTE);

				} catch (Throwable e) {
					int statusCode = translateToZclStatusCode(e);
					try {
						ZclDataTypeUI8.zclSerialize(zclResponseFrame, (byte) statusCode);
					} catch (ZclValidationException e1) {
						LOG.error("Opps... unable to serialize ZclReadAttrs Response ", e1);
						return;
					}
				}
			}

			try {
				ZigBeeDevice device = getZigBeeDevice();
				zclResponseFrame.shrink();
				devicePost(clusterId, zclResponseFrame);
			} catch (ApplianceException e) {
				LOG.error("exception", e);
			}
			break;
		}

		case ZCL.ZclReportAttrs: {
			// Handles an incoming Report Attributes ZCL Command
			short statusCode = ZCL.SUCCESS;
			while (true) {

				try {
					int attrId = ZclDataTypeUI16.zclParse(zclFrame);
					short attrDataType = ZclDataTypeUI8.zclParse(zclFrame);
					IZclAttributeDescriptor zclAttributeDescriptor = getAttributeDescriptor(attrId);
					if (zclAttributeDescriptor == null)
						continue;

					ZclAbstractDataType zclDataTypeObject = zclAttributeDescriptor.zclGetDataType();
					if (zclDataTypeObject.zclGetDataType() == attrDataType) {
						Object objectValue = zclDataTypeObject.zclParseToObject(zclFrame);
						try {
							AttributeValue attrValue = new AttributeValue(objectValue, System.currentTimeMillis());
							setCachedAttributeValue(attrId, attrValue);
							notifyAttributeValue(zclAttributeDescriptor.getName(), attrValue);
						} catch (Exception e) {
							continue;
						}
					} else {
						statusCode = ZCL.INVALID_DATA_TYPE;
					}
				} catch (Throwable e) {
					// probably an array out of band.
					break;
				}
			}

			// generates the default response, if required
			if (!zclFrame.isDefaultResponseDisabled()) {
				statusCode = ZCL.SUCCESS;
				IZclFrame zclResponseFrame = this.getDefaultResponse(zclFrame, statusCode);
				// try {
				// device = getZigBeeDevice();
				// } catch (ApplianceException e) {
				// log.fatal("getZigBeeDevice() returned exception. Unable to send default response",
				// e);
				// }

				devicePost(clusterId, zclResponseFrame);
			}
			break;
		}

		case ZCL.ZclWriteAttrs: {
			// unfortunately it is not possible to say the number of Write
			// Attribute Records that the command contains.

			IZclFrame zclResponseFrame = zclFrame.createResponseFrame(255);
			zclResponseFrame.setCommandId(ZCL.ZclWriteAttrsRsp);

			int failures = 0;

			while (true) {
				short statusCode = ZCL.SUCCESS;
				int attrId;
				try {
					attrId = ZclDataTypeUI16.zclParse(zclFrame);
				} catch (ArrayIndexOutOfBoundsException e) {
					// end of array
					break;
				} catch (ZclValidationException e) {
					break;
				}

				short dataType;
				try {
					dataType = ZclDataTypeUI8.zclParse(zclFrame);
				} catch (ZclValidationException e) {
					// Error the attribute, return an error for this attribute
					statusCode = ZCL.INVALID_FIELD;
					break;
				}

				boolean skipped = false;
				try {
					statusCode = this.writeAttribute(zclFrame, attrId, dataType);
				} catch (Throwable e) {
					failures += 1;
					statusCode = translateToZclStatusCode(e);
					if (statusCode == ZCL.READ_ONLY || statusCode == ZCL.UNSUPPORTED_ATTRIBUTE) {
						// flag to remember that the attribute value has to be
						// removed from the incoming frame.
						skipped = true;
					}
				}

				if (statusCode == ZCL.SUCCESS) {
					continue;
				} else {

					try {
						if (!skipped) {
							// skip the attribute
							ZclWriteAttributeRecord.zclSkip(zclFrame, dataType);
						}
					} catch (Exception e) {
						throw new ZclException(ZCL.MALFORMED_COMMAND);
					}
				}

				failures += 1;
				try {
					ZclDataTypeUI8.zclSerialize(zclResponseFrame, statusCode);
					ZclDataTypeUI16.zclSerialize(zclResponseFrame, attrId);
				} catch (ZclValidationException e) {
					break;
				}
			}

			if (failures == 0) {
				try {
					ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
				} catch (ZclValidationException e) {
					throw new ZclException(ZCL.FAILURE);
				}
			}

			try {
				ZigBeeDevice device = getZigBeeDevice();
				zclResponseFrame.shrink();
				devicePost(clusterId, zclResponseFrame);
			} catch (ApplianceException e) {
				LOG.error("exception", e);
			}

			break;
		}

		case ZCL.ZclDiscoverAttrs: {
			int startAttributeIdentifier;
			short maxAttributeIdentifiers;

			IZclFrame zclResponseFrame = zclFrame.createResponseFrame(255);
			zclResponseFrame.setCommandId(ZCL.ZclDiscoverAttrsRsp);

			ServiceCluster peerCluster = null;
			try {
				String clazz = getName();
				if (clazz.endsWith("Server")) {
					clazz = getName().substring(0, clazz.length() - 6);
					peerCluster = (ServiceCluster) getSinglePeerCluster(clazz + "Client");
				} else {
					clazz = getName().substring(0, clazz.length() - 6);
					peerCluster = (ServiceCluster) getSinglePeerCluster(clazz + "Server");
				}

			} catch (ServiceClusterException e) {
				LOG.error("Exception ", e);
			}

			if (peerCluster == null) {
				// No peer
				throw new ZclException(ZCL.FAILURE);
			}
			String[] supportedAttributes = null;
			try {
				supportedAttributes = peerCluster.getSupportedAttributeNames(null);
			} catch (ApplianceException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ServiceClusterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				startAttributeIdentifier = ZclDataTypeUI16.zclParse(zclFrame);
				maxAttributeIdentifiers = ZclDataTypeUI8.zclParse(zclFrame);
			} catch (ZclValidationException e) {
				throw new ZclException(ZCL.MALFORMED_COMMAND);
			}

			// FIXME: Optimize it
			IZclAttributeDescriptor[] peerAttributeDescriptors = this.getPeerAttributeDescriptors();
			HashMap peerAttributeDescriptorsByIdMap = new HashMap();

			List l = new ArrayList();

			if (peerAttributeDescriptors != null) {
				for (int i = 0; i < peerAttributeDescriptors.length; i++) {
					String attributeName = peerAttributeDescriptors[i].getName();
					if (contains(supportedAttributes, attributeName) || (supportedAttributes == null)) {

						int id = peerAttributeDescriptors[i].zclGetId();
						if (id >= startAttributeIdentifier) {
							l.add(id);
							peerAttributeDescriptorsByIdMap.put(new Integer(id), peerAttributeDescriptors[i]);
						}
					}
				}
			}

			// sets discovery complete field
			try {

				// Sets the Discovery Complete value
				if (l.size() > maxAttributeIdentifiers) {
					ZclDataTypeUI8.zclSerialize(zclResponseFrame, (short) 0);
				} else {
					ZclDataTypeUI8.zclSerialize(zclResponseFrame, (short) 1);
				}
			} catch (ZclValidationException e) {
				LOG.error("Unable to marshall the discovery attr response",e);
				throw new ZclException(ZCL.FAILURE);
			}

			Collections.sort(l);
			// Unfortunately the spec must know if there are remaining
			// attributes to discover ...
			int i;

			int attributeCount = 1;
			for (i = 0; i < l.size(); i++) {
				int attrId = ((Integer) l.get(i)).intValue();
				if (attrId >= startAttributeIdentifier) {
					if (attributeCount <= maxAttributeIdentifiers) {
						// Is the attribute supported?
						ZclAttributeDescriptor attributeDescriptor = (ZclAttributeDescriptor) peerAttributeDescriptorsByIdMap
								.get(attrId);
						if (attributeDescriptor != null) {
							try {
								ZclDataTypeUI16.zclSerialize(zclResponseFrame, attributeDescriptor.zclGetId());
								ZclDataTypeUI8
										.zclSerialize(zclResponseFrame, attributeDescriptor.zclGetDataType().zclGetDataType());
							} catch (ZclValidationException e) {
								LOG.error("Unable to marshall the discovery attr response",e);
								throw new ZclException(ZCL.FAILURE);
							}
						}
						attributeCount++;
					} else {
						break;
					}
				}
			}

			try {
				ZigBeeDevice device = getZigBeeDevice();
				zclResponseFrame.shrink();
				devicePost(clusterId, zclResponseFrame);
			} catch (ApplianceException e) {
				LOG.error("ApplianceException on handleGeneralCommand", e);
			}
			break;
		}

		case ZCL.ZclConfigRep:
			break;

		case ZCL.ZclRepConf:
			break;

		case ZCL.ZclReadAttrsRsp:
		case ZCL.ZclWriteAttrsRsp:
		case ZCL.ZclWriteAttrsNoRsp:
		case ZCL.ZclConfigRepRsp:
		case ZCL.ZclRepConfRsp:
		case ZCL.ZclDefaultRsp:
			break;

		case ZCL.ZclWriteAttrsUndivided:
		case ZCL.ZclReadAttrsStructured:
		case ZCL.ZclWriteAttrsStructured:
		case ZCL.ZclWriteAttrsStructuredRsp:
			// FIXME: probably here we should send back a different error for
			// the Rsp commands, like failure ...
			LOG.error("Unsupported incoming general command: " + commandId);
			throw new ZclException(ZCL.UNSUP_GENERAL_COMMAND);

		default:
			throw new ZclException(ZCL.UNSUP_GENERAL_COMMAND);
		}
	}

	protected void issueSet(short clusterId, IZclFrame zclFrame, int attrId, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		boolean sync = true;
		IZclFrame zclResponseFrame;

		ZigBeeDevice device = getZigBeeDevice();
		if (device == null) {
			throw new ApplianceException("Not attached");
		}

		zclFrame.setSequence(sequence++);
		zclFrame.setCommandId(ZCL.ZclWriteAttrs);

		if (sync) {
			zclResponseFrame = deviceInvoke(clusterId, zclFrame);

			this.checkResponseFrameDirection(zclResponseFrame);

			// FIXME: handle the ZclWriteAttrsRsp or the default Response
			if (zclResponseFrame.getCommandId() != ZCL.ZclWriteAttrsRsp) {
				throw new ApplianceException(BAD_RESPONSE_COMMAND_ID_MESSAGE + " " + zclResponseFrame.getCommandId() + "'");
			}

			short status = ZCL.SUCCESS;

			try {
				status = ZclDataTypeUI8.zclParse(zclResponseFrame);
			} catch (ZclValidationException e) {
				this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
			}

			if (status != ZCL.SUCCESS) {
				int responseAttrId = 0;
				try {
					responseAttrId = ZclDataTypeUI16.zclParse(zclResponseFrame);
				} catch (ZclValidationException e) {
					this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
				}

				if (responseAttrId != attrId)
					throw new ApplianceException("Response attrId doesn't match requeted one");
				else
					this.raiseServiceClusterException(ZCL.MALFORMED_COMMAND);
			} else {
				// success, we need to check that no more bytes follows
				// TODO: performs this check!!
			}
			return;
		} else {
			boolean res = devicePost(clusterId, zclFrame);
			if (!res) {
				throw new ApplianceException(POST_FAILED_MESSAGE);
			}
		}
	}

	protected IZclFrame issueExec(IZclFrame zclFrame, int expectedResponseId, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		boolean sync = true;

		IZclFrame zclResponseFrame = null;

		ZigBeeDevice device = getZigBeeDevice();
		if (device == null)
			throw new ApplianceException("Not attached");

		zclFrame.setSequence(sequence++);

		if ((context != null) && (!context.isConfirmationRequired())) {
			zclFrame.disableDefaultResponse(true);
			if (expectedResponseId == ZCL.ZclDefaultRsp) {
				sync = false;
			}
		}

		if (sync) {
			try {
				zclResponseFrame = deviceInvoke((short) getClusterId(), zclFrame);
			} catch (ZigBeeException _x) {
				throw new ApplianceException((INVOKE_ERROR_MESSAGE));
			}

			this.checkResponseFrameDirection(zclResponseFrame);
			this.handleResponseFrame(zclFrame, zclResponseFrame, expectedResponseId);

		} else {
			boolean res = devicePost((short) getClusterId(), zclFrame);
			if (!res) {
				throw new ApplianceException(POST_FAILED_MESSAGE);
			}
		}
		return zclResponseFrame;
	}

	/**
	 * @deprecated Use the other issueExec
	 * @param clusterId
	 * @param zclFrame
	 * @param expectedResponseId
	 * @param context
	 * @return
	 * @throws ApplianceException
	 */
	protected IZclFrame issueExec(short clusterId, IZclFrame zclFrame, int expectedResponseId, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		boolean sync = true;

		IZclFrame zclResponseFrame = null;

		ZigBeeDevice device = getZigBeeDevice();
		if (device == null)
			throw new ApplianceException("Not attached");

		zclFrame.setSequence(sequence++);

		if ((context != null) && (!context.isConfirmationRequired())) {
			zclFrame.disableDefaultResponse(true);
			sync = false;
		}

		if (sync) {
			try {
				zclResponseFrame = deviceInvoke(clusterId, zclFrame);
			} catch (ZigBeeException _x) {
				throw new ApplianceException((INVOKE_ERROR_MESSAGE));
			}

			this.checkResponseFrameDirection(zclResponseFrame);

			if (zclResponseFrame.getCommandId() != expectedResponseId) {
				throw new ApplianceException(BAD_RESPONSE_COMMAND_ID_MESSAGE + ": '" + zclResponseFrame.getCommandId()
						+ "', expected " + expectedResponseId);
			}
		} else {
			boolean res = devicePost(clusterId, zclFrame);
			if (!res) {
				throw new ApplianceException(POST_FAILED_MESSAGE);
			}
		}
		return zclResponseFrame;
	}

	/**
	 * Check if the direction of the incoming Zcl Frame is compatible with the
	 * type of cluster that the current instance of service cluster represents:
	 * client or server
	 * 
	 * @param incomingZclFrame
	 *            The incoming IZclFrame
	 * @throws ServiceClusterException
	 *             In case of error it throws a ServiceCluster Exception
	 */
	private void checkIncomingFrameDirection(IZclFrame incomingZclFrame) throws ZclException {
		if (checkDirection) {
			if (incomingZclFrame.isClientToServer() && (this.getSide() != IServiceCluster.CLIENT_SIDE))
				throw new ZclException(BAD_DIRECTION_MESSAGE + " Expected C to S", ZCL.MALFORMED_COMMAND);

			if (!incomingZclFrame.isClientToServer() && (this.getSide() != IServiceCluster.SERVER_SIDE))
				throw new ZclException(BAD_DIRECTION_MESSAGE + " Expected S to C", ZCL.MALFORMED_COMMAND);
		}
	}

	/**
	 * Check if the direction of a response Zcl Frame is compatible with the
	 * type of cluster that the current instance of service cluster represents:
	 * client or server
	 * 
	 * @param responseZclFrame
	 *            The response IZclFrame
	 * @throws ServiceClusterException
	 *             In case of error it throws a ZclException exception
	 */
	private void checkResponseFrameDirection(IZclFrame responseZclFrame) throws ServiceClusterException {
		if (checkDirection) {
			if (responseZclFrame.isClientToServer() && (this.getSide() != IServiceCluster.SERVER_SIDE))
				throw new MalformedMessageException(BAD_DIRECTION_MESSAGE + " Expected C to S");

			if (!responseZclFrame.isClientToServer() && (this.getSide() != IServiceCluster.CLIENT_SIDE))
				throw new MalformedMessageException(BAD_DIRECTION_MESSAGE + " Expected S to C");

		}
	}

	public void notifyEvent(int event) {
		// TODO Auto-generated method stub
	}

	/**
	 * Method to access the Cache of the current cluster
	 * 
	 * @param clusterId
	 * @param attrId
	 * @param context
	 * @return
	 */

	protected IAttributeValue checkCache(short clusterId, int attrId, IEndPointRequestContext context) {

		if ((context != null) && (context.getMaxAgeForAttributeValues() > 0)) {
			IAttributeValue attributeValue = (IAttributeValue) this.cachedAttributeValues.get(new Integer(attrId));
			if (attributeValue != null
					&& attributeValue.getTimestamp() + context.getMaxAgeForAttributeValues() >= System.currentTimeMillis()) {
				return attributeValue;
			}
			return null;
		} else
			return null;

	}

	protected void updateCachedValue(int attrId, Boolean v) {
		IAttributeValue av = new AttributeValue(v);
		this.cachedAttributeValues.put(new Integer(attrId), av);
	}

	private HashMap cachedAttributeValues = new HashMap();

	protected void setCachedAttributeObject(int attributeId, Object attributeObjectValue) {
		this.cachedAttributeValues.put(new Integer(attributeId), new AttributeValue(attributeObjectValue));
	}

	protected void setCachedAttributeValue(int attributeId, AttributeValue attributeValue) {
		this.cachedAttributeValues.put(new Integer(attributeId), attributeValue);
	}

	protected Object getValidCachedAttributeObject(int attributeId, long maxAge) {
		AttributeValue attributeValue = (AttributeValue) this.cachedAttributeValues.get(new Integer(attributeId));
		if (attributeValue != null && attributeValue.getTimestamp() + maxAge >= System.currentTimeMillis())
			return attributeValue.getValue();
		return null;
	}

	/**
	 * Raise an exception if the response frame doesn't contain the expected
	 * command
	 * 
	 * @param zclFrame
	 *            The outgoing Zcl Frame.
	 * @param zclResponseFrame
	 *            The response Zcl Frame
	 * @param expectedResponseId
	 *            The expected response command Id
	 * @throws ServiceClusterException
	 */

	private void handleResponseFrame(IZclFrame zclFrame, IZclFrame zclResponseFrame, int expectedResponseId)
			throws ServiceClusterException {

		int responseCommandId = zclResponseFrame.getCommandId();

		if (responseCommandId != expectedResponseId) {
			if (responseCommandId == ZCL.ZclDefaultRsp) {
				raiseDefaultResponse(zclResponseFrame, zclFrame.getCommandId(), true);
			} else {
				// This is a completed different message (wrong sequence
				// number?)
				throw new ServiceClusterException("Completely unexpected response: expecting " + expectedResponseId
						+ " or defaultResponse");
			}
		}

		if (expectedResponseId == ZCL.ZclDefaultRsp) {
			raiseDefaultResponse(zclResponseFrame, zclFrame.getCommandId(), false);
		}
	}

	/**
	 * 
	 * @param zclFrame
	 *            The passed zclFrame must contain a default response command
	 * @param sentCommandId
	 *            The commandId it must be contained in the DefaultResponse
	 * @param expectError
	 *            true if the defaultResponse is expected to contain an error.
	 *            If it doesn't contain and error, it an error if it doesn't
	 *            contain an error.
	 * @throws ServiceClusterException
	 */

	private void raiseDefaultResponse(IZclFrame zclFrame, int sentCommandId, boolean expectError) throws ServiceClusterException {
		int commandId;
		try {
			commandId = ZclDataTypeUI8.zclParse(zclFrame);
		} catch (ZclValidationException e) {
			throw new ServiceClusterException("Sent cmdId " + sentCommandId + ", received a wrong message");
		}

		if (commandId != sentCommandId)
			throw new ServiceClusterException("Sent cmdId " + commandId + ", received default response with wrong cmdId: "
					+ zclFrame.getCommandId());

		short status = ZclDataTypeUI8.zclParse(zclFrame);

		if (status != ZCL.SUCCESS) {
			this.raiseServiceClusterException(status);
		} else if (expectError) {
			// the status is ZCL.SUCCESS but we expect that the DefaultResponse
			// contain an error.
			throw new ServiceClusterException("Unexpected Success status code in DefaultResponse");
		}
	}

	protected boolean contains(String[] array, String s) {
		if (array == null)
			return false;
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(s))
				return true;
		}
		return false;
	}
}
