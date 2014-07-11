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

public class ZclSerializer {

	private int pos = 0;
	public int profileId;
	public int clusterId;
	public byte data[];
	private int seq;

	public ZclSerializer() {
		data = new byte[300];
		pos = 0;
	}

	public byte readByte() {
		return data[pos++];
	}

	public long readUInt8() {
		return data[pos++] & 0xFF;
	}

	public int readShort() {
		int ret = (int) ((data[pos + 1] & 0xFF) << 8 | (data[pos] & 0xFF));
		pos += 2;
		return ret;
	}

	public int readInt32() {
		// FIXME: order is correct?
		long res = 0;
		short val;
		for (int i = 3; i >= 0; i--) {
			val = (short) (data[pos + i] & 0xFF);
			res = (res << 8) | val;
		}
		pos += 4;
		return (int) res;
	}

	public void writeByte(byte v) {
		data[pos++] = v;
	}

	public void writeShort(int i) {
		data[pos++] = (byte) (i & 0xFF);
		data[pos++] = (byte) (i >> 8);
	}

	public int readInt() {
		// FIXME: order is correct?
		long res = 0;
		short val;
		for (int i = 3; i >= 0; i--) {
			val = (short) (data[pos + i] & 0xFF);
			res = (res << 8) | val;
		}
		pos += 4;
		return (int) res;
	}

	public void resetData() {
		pos = 0;
	}

	/**
	 * Returns the payload
	 */
	public byte[] getPayload() {
		byte[] newarray = new byte[pos];
		System.arraycopy(data, 0, newarray, 0, newarray.length);
		return newarray;
	}
	
	public int getPosition() {
		return pos;
	}

	public int getDataLeftLength() {
		return (data.length - pos);
	}

	/**
	 * Returns the payload
	 */
	public byte[] getDataLeft() {
		byte[] newarray = new byte[data.length - pos];
		try {
			System.arraycopy(data, pos, newarray, 0, newarray.length);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newarray;
	}

	public void setPayload(byte[] data, int pos) {
		this.data = data;
		this.pos = pos;
	}

	public void writeUInt32(long i32) {
		data[pos++] = (byte) (i32 & 0xFF);
		data[pos++] = (byte) (i32 >> 8);
		data[pos++] = (byte) (i32 >> 16);
		data[pos++] = (byte) (i32 >> 24);
	}

	public int readInt24() {
		// FIXME: order is correct?
		long res = 0;
		short val;
		for (int i = 2; i >= 0; i--) {
			val = (short) (data[pos + i] & 0xFF);
			res = (res << 8) | val;
		}
		pos += 4;
		return (int) res;
	}

	public int readInt16() {
		// FIXME: order is correct?
		long res = 0;
		short val;
		for (int i = 1; i >= 0; i--) {
			val = (short) (data[pos + i] & 0xFF);
			res = (res << 8) | val;
		}
		pos += 4;
		return (int) res;
	}

	public void writeInt16(int i16) {
		data[pos++] = (byte) (i16 & 0xFF);
		data[pos++] = (byte) (i16 >> 8);
	}

	public void writeInt24(byte i24) {
		data[pos++] = (byte) (i24 & 0xFF);
		data[pos++] = (byte) (i24 >> 8);
		data[pos++] = (byte) (i24 >> 16);
	}

	public void writeBytes(byte[] data2) {
		for (int i = 0; i < data2.length; i++) {
			data[pos++] = data2[i];
		}
	}

	public void setSeq(int seq) {
		this.seq = seq;
		writeByte((byte) seq);
	}

	public String readString() {
		long len = readUInt8();
		if (len == 0xff) {
			return null;
		}

		String s = "";

		// FIXME: optimize
		for (int i = 0; i < len; i++) {
			s = s + data[pos++];
		}

		return s;
	}

	public byte[] readData() {
		long len = readUInt8();
		if (len == 0xff) {
			return null;
		}

		byte[] d = new byte[(int) len];

		String s = "";

		// FIXME: optimize
		for (int i = 0; i < len; i++) {
			d[i] = data[pos++];
		}

		return d;
	}

	public int getSeq() {
		// System.out.println("Seq is " + seq);
		return seq;
	}

	public short getFrameControlField() {
		if (pos > 0) {
			return (short) (data[0] & 0xFF);
		}
		return -1;
	}

	public void undo(int i) {
		if (pos > i) {
			pos -= i;
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

	public void append_uint8(byte uint8) {
		data[pos++] = uint8;
	}

	public void append_uint16(short uint16) {
		data[pos++] = (byte) (uint16 & 0xFF);
		data[pos++] = (byte) (uint16 >> 8);
	}
	
	public void append_uint24(int uint24) {
		data[pos++] = (byte) (uint24 & 0xFF);
		data[pos++] = (byte) (uint24 >> 8);
		data[pos++] = (byte) (uint24 >> 16);
	}	
	public void append_uint32(int uint32) {
		data[pos++] = (byte) (uint32 & 0xFF);
		data[pos++] = (byte) (uint32 >> 8);
		data[pos++] = (byte) (uint32 >> 16);
		data[pos++] = (byte) (uint32 >> 24);
	}	
	
	public void append_ulong(long l, int size) {
		for (int i = 0; i < size; i++) {
			data[pos++] = (byte) (l & 0xFF);
			l >>= 8;
		}
	}	
	
	public void append_octets (byte[] octets) {
		for (int i = 0; i < octets.length; i++) {
			data[pos++] = octets[i];
		}
	}
}
