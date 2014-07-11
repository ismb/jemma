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

import org.energy_home.jemma.ah.internal.zigbee.Hex;

public class ZclFrame implements IZclFrame {

	byte[] data;

	int pos = 0;

	public ZclFrame(int fcf, int payloadSize) {
		this((byte) fcf, payloadSize);
	}
	
	public ZclFrame(int fcf) {
		this((byte) fcf, 0);
	}
	
	public ZclFrame(byte fcf, int payloadSize) {

		if ((fcf & MANUFACTURER_SPECIFIC_MASK) > 0) {
			// 5 is the sum of Frame Control Field + Seq + Manufacturer ID +
			// Command Id
			data = new byte[5 + payloadSize];
			pos = 5;
		} else {
			// 3 is the sum of Frame Control Field + Seq + Command Id
			data = new byte[3 + payloadSize];
			pos = 3;
		}
		data[0] = fcf;
	}

	public ZclFrame(byte[] data) {
		this.data = data;
		if (data.length < 3)
			System.out.println("FIXME: check size, NOW IS INCORRECT!!!");
		// throw new ZclException("Frame too short");

		if (isManufacturerSpecific())
			pos = 5;
		else
			pos = 3;
	}

	public short getZclHeaderSize() {
		if (isManufacturerSpecific())
			return 5;
		else
			return 3;
	}

	public void disableDefaultResponse(boolean disableDefaultResponse) {
		if (disableDefaultResponse)
			data[0] = (byte) (data[0] | DISABLE_DEFAULT_RESPONSE_MASK);
		else
			data[0] = (byte) (data[0] & 0xEF);
	}

	public IZclFrame createResponseFrame(int payloadSize) {
		short size = getZclHeaderSize();
		byte[] responseData = new byte[size + payloadSize];
		System.arraycopy(this.data, 0, responseData, 0, size);
		IZclFrame zclResponseFrame = new ZclFrame(responseData);
		responseData[0] = (byte) (responseData[0] ^ DIRECTION_MASK);
		responseData[0] = (byte) (responseData[0] | DISABLE_DEFAULT_RESPONSE_MASK);
		return zclResponseFrame;
	}

	public byte[] getData() {
		return data;
	}

	public byte getFrameControlField() {
		return data[0];
	}

	public byte getSequenceNumber() {
		if (isManufacturerSpecific())
			return data[3];
		else
			return data[1];
	}

	public boolean isClientToServer() {
		return !isServerToClient();
	}

	public boolean isServerToClient() {
		return ((data[0] & DIRECTION_MASK) > 0);
	}

	public boolean isDefaultResponseDisabled() {
		return (data[0] & DISABLE_DEFAULT_RESPONSE_MASK) > 0;
	}

	public void setDirection(byte direction) {
		if (direction == SERVER_TO_CLIENT_DIRECTION)
			data[0] = (byte) ((data[0] & 0xff) | DIRECTION_MASK);
		else
			data[0] = (byte) (data[0] & ~DIRECTION_MASK);
	}

	public void setSequence(int sequence) {
		sequence = sequence % 0xff;
		if (isManufacturerSpecific())
			data[3] = (byte) sequence;
		else
			data[1] = (byte) sequence;
	}

	public int getCommandId() {
		if (isManufacturerSpecific())
			return data[4] & 0xff;
		else
			return data[2] & 0xff;
	}

	public void setCommandId(int commandId) {
		if (isManufacturerSpecific())
			data[4] = (byte) commandId;
		else
			data[2] = (byte) commandId;
	}

	public int getManufacturerCode() throws ZigBeeException {
		if (isManufacturerSpecific())
			return ((data[1] & 0xff) | ((data[2] & 0xff) << 8));
		else
			throw new ZigBeeException();
	}

	public boolean isManufacturerSpecific() {
		return (data[0] & MANUFACTURER_SPECIFIC_MASK) > 0;
	}

	public void appendULongWithSize(long l, int size) {
		for (int i = 0; i < size; i++) {
			data[pos++] = (byte) (l & 0xFF);
			l >>= 8;
		}
	}

	public long readULongWithSize(int size) {
		long l = 0;
		pos += size;
		for (int i = 0; i < size; i++) {
			l <<= 8;
			short s = (short) (data[--pos] & 0xFF);
			l = l | s;
		}
		pos += size;
		return l;
	}

	public void appendUInt8(int uint8) {
		data[pos++] = (byte) (uint8 & 0xff);
	}

	public void appendUInt16(int uint16) {
		data[pos++] = (byte) (uint16 & 0xFF);
		data[pos++] = (byte) (uint16 >> 8);
	}

	public void appendUInt24(int uint24) {
		data[pos++] = (byte) (uint24 & 0xFF);
		data[pos++] = (byte) (uint24 >> 8);
		data[pos++] = (byte) (uint24 >> 16);
	}

	public void appendUInt32(long uint32) {
		data[pos++] = (byte) (uint32 & 0xFF);
		data[pos++] = (byte) (uint32 >> 8);
		data[pos++] = (byte) (uint32 >> 16);
		data[pos++] = (byte) (uint32 >> 24);
	}

	public void appendBoolean(boolean b) {
		if (b)
			data[pos++] = 1;
		else
			data[pos++] = 0;
	}

	public void appendOctets(byte[] octets) {
		// TODO: optimize
		if (octets == null) {
			appendUInt8((short) 0xFF); // invalid value
			return;
		}
		
		appendUInt8((short) octets.length);
		for (int i = 0; i < octets.length; i++) {
			data[pos++] = octets[i];
		}
	}

	public void appendString(String s, int size) throws ZigBeeException {
		// FIXME: optimize
		if (s == null) {
			appendUInt8((short) 0xFF); // invalid value
			return;
		}
		
		int len = s.length();
		
		// FIXME: check if this check is correct!!!
		if (len > size) {
			throw new ZigBeeException();
		}
		
		appendUInt8((short) s.length());
		byte[] c = s.getBytes();
		for (int i = 0; i < len; i++) {
			data[pos++] = c[i];
		}
	}

	public void appendInt8(short int8) {
		appendUInt8(int8);
	}

	public void appendInt16(int int16) {
		appendUInt16(int16);
	}

	public void appendInt24(int int24) {
		appendUInt24(int24);
	}

	public void appendInt32(long int32) {
		// TODO Auto-generated method stub
		appendUInt32(int32);
	}

	public short parseUInt8() {
		return (short) this.readULongWithSize(1);
	}

	public int parseUInt16() {
		return (int) this.readULongWithSize(2);
	}

	public int parseUInt24() {
		return (int) this.readULongWithSize(3);
	}

	public long parseUInt32() {
		return (int) this.readULongWithSize(4);
	}

	public boolean parseBoolean() {
		return (parseUInt8() > 0);
	}

	public byte[] parseOctets() {
		long len = parseUInt8();
		if (len == 0xff) {
			return null;
		}

		byte[] d = new byte[(int) len];
		// TODO: optimize it
		for (int i = 0; i < len; i++) {
			d[i] = data[pos++];
		}
		return d;
	}
	
	public String parseString() {
		int len = parseUInt8();
		if (len == 0xff) {
			return null;
		}

		String s = new String(data, pos, len);
		pos += len;
		return s;
	}

	public void appendString(String value) {
		// FIXME: optimize
		if (value == null) {
			appendUInt8((short) 0xFF); // invalid value
			return;
		}
		
		int len = value.length();
		
		appendUInt8((short) value.length());
		byte[] c = value.getBytes();
		for (int i = 0; i < len; i++) {
			data[pos++] = c[i];
		}
	}

	public void appendUTCTime(long epoch) {
		appendUInt32(epoch);
	}

	public long parseUTCTime() {
		return parseUInt32();
	}

	public void appendUInt48(long value) {
		appendULongWithSize(value, 6);
	}

	public long parseUInt48() {
		return readULongWithSize(6);
	}

	public String toString() {
		if (data == null)
			return "null";

		if (data.length == 0)
			return "empty message";

		if (this.isManufacturerSpecific()) {
			if (data.length < 5)
				return "wrong message, must be at least 5 bytes long";
			else if (data.length < 3)
				return "wrong message, must be at least 5 bytes long";
		}

		String out = "";

		// OK, print the message

		byte frameType = getFrameType();
		if (frameType == 0x00)
			out += "P";
		else
			out += "C";

		if (this.isManufacturerSpecific())
			out += "M";
		else
			out += "_";

		if (this.isServerToClient())
			out += "S";
		else
			out += "C";

		if (!isDefaultResponseDisabled())
			out += "R";
		else
			out += "_";

		int i;
		if (isManufacturerSpecific())
			i = 5;
		else
			i = 3;

		out += " 0x" + Hex.toHexString(getSequenceNumber() & 0xff, 1) + " [seq] ";

		out += " 0x" + Hex.toHexString(getCommandId(), 1) + " [cmdId] ";

		out += Hex.byteToHex(data, i);

		return out;
	}

	public byte getFrameType() {
		return (byte) (data[0] & FRAME_TYPE_MASK);
	}

	public short getPayloadSize() {
		return (short) (data.length - this.getZclHeaderSize());
	}

	public void shrink() {
		if (pos != data.length) {
			byte[] d = new byte[pos];
			System.arraycopy(data, 0, d, 0, pos);
			data = d;
		}
	}

	public void setFrameType(byte frameType) {
		data[0] = (byte) ((data[0] & ~FRAME_TYPE_MASK) | (frameType & FRAME_TYPE_MASK));
	}
	
	public void appendArray(byte[] array) {
		System.arraycopy(array, 0, this.data, pos, array.length);
	}
	
	public void appendArray(byte[] array, boolean swap) {
		if (swap) {
			int j = pos + array.length - 1;
			for (int i = 0; i < array.length; i++) {
				data[j--] = array[i];
			}
		}
		else
			System.arraycopy(array, 0, this.data, pos, array.length);
	}

	public byte getDirection() {
		return (byte) ((data[0] & DIRECTION_MASK) >> 3);
	}

	public byte[] parseArray(int len) {
		byte[] array = new byte[len];
		System.arraycopy(this.data, pos, array,0 , len);
		pos += len;
		return array;
	}
	
	public byte[] parseArray(int len, boolean swap) {
		if (swap) {
			byte[] array = new byte[len];
			pos += len;
			int j = pos;
			for (int i = 0; i < array.length; i++) {
				array[i] = data[j--];
			}
			return array;
		}
		else 
			return parseArray(len);
	}
}
