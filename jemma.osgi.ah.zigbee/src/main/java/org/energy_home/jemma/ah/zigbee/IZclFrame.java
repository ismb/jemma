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
package org.energy_home.jemma.ah.zigbee;

public interface IZclFrame {

	public static final byte FRAME_TYPE_MASK = 0x03;
	public static final byte MANUFACTURER_SPECIFIC_MASK = 0x04;
	public static final byte DIRECTION_MASK = 0x08;
	public static final byte DISABLE_DEFAULT_RESPONSE_MASK = 0x10;

	public static final byte SERVER_TO_CLIENT_DIRECTION = 0x01;
	public static final byte CLIENT_TO_SERVER_DIRECTION = 0x00;

	public static final byte GENERAL_COMMAND = 0x00;
	public static final byte CLUSTER_COMMAND = 0x01;

	public void disableDefaultResponse(boolean disableDefaultResponse);

	public byte getFrameControlField();

	public byte getSequenceNumber();

	public int getManufacturerCode() throws ZigBeeException;

	public byte[] getData();

	public boolean isDefaultResponseDisabled();

	public boolean isManufacturerSpecific();

	public boolean isClientToServer();

	public boolean isServerToClient();

	public byte getDirection();

	public void setDirection(byte direction);

	public void setSequence(int sequence);

	public void setFrameType(byte generalCommand);

	public IZclFrame createResponseFrame(int payloadSize);

	public void appendUInt8(int uint8);

	public void appendUInt16(int uint16);

	public void appendUInt24(int uint24);

	public void appendUInt32(long uint32);

	public void appendInt8(short uint8);

	public void appendInt16(int uint16);

	public void appendInt24(int uint24);

	public void appendInt32(long uint32);

	public void appendBoolean(boolean value);

	public short parseUInt8();

	public int parseUInt16();

	public int parseUInt24();

	public long parseUInt32();

	public boolean parseBoolean();

	public byte[] parseOctets();

	public String parseString();

	public byte[] parseArray(int len);

	public int getCommandId();

	public void appendOctets(byte[] value);

	public void appendString(String value);
	
	public void appendArray(byte[] array);

	public long parseUTCTime();

	public void appendUTCTime(long value);

	public long parseUInt48();

	public void appendUInt48(long value);

	public void setCommandId(int commandId);

	public String toString();

	public byte getFrameType();

	public short getPayloadSize();

	public void shrink();
}
