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

/**
 * Class used to split an ApsMessage section Data into a Zcl Message.
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class _ZCLMessage {

	public byte FrameControl;

	public boolean _commandAccrossEntireProfile;
	public boolean _commandSpecifcForCluster;
	public byte[] Payload;

	public byte TransequenceNumber;

	public byte CommandID;

	public boolean Server_to_Client;

	public boolean Disable_DefaultResponse;

	/**
	 * Procedure that invert the direction bit of the Framecontrol Byte of a Zcl Header.
	 */
	public byte InvertDirectionBitOfFrameControl() {

		if (!Server_to_Client)
			return ((byte) (FrameControl | 3));
		else
			return ((byte) (FrameControl & ~3));

	}

	/**
	 * Procedure that disable the bit of the DefaultResponse into the Framecontrol Byte of a Zcl Header.
	 */
	public byte SetBitDefaultResponse(boolean sendDefaltResponse) {
		if (!sendDefaltResponse)
			return ((byte) (FrameControl | 4));
		else
			return ((byte) (FrameControl & ~4));
	}

	/**
	 * Procedure that disable the bit of the DefaultResponse into the Framecontrol Byte of a Zcl Header.
	 * @param sendDefaltResponse --> Value of the bit DefaultResponse
	 * @param _FrameControl --> The FrameControl Byte
	 */
	public byte SetBitDefaultResponse(boolean sendDefaltResponse,
			byte _FrameControl) {

		if (!sendDefaltResponse)
			return ((byte) (_FrameControl | 4));
		else
			return ((byte) (_FrameControl & ~4));

	}

	/**
	 * Constructor that populate the class starting from a byte array
	 * @param _Data --> The array on bytes that represent the Zcl Message
	 * 
	 */
	public _ZCLMessage(byte[] _Data) {

		FrameControl = _Data[0];
		boolean _bit0 = false;
		boolean _bit1 = false;

		if ((FrameControl & 0x01) == 1) {
			_bit0 = true;
		}
		if ((FrameControl & 0x02) == 1) {
			_bit1 = true;
		}

		if (!_bit0 && !_bit1)
			_commandAccrossEntireProfile = true;
		else if (_bit0 && !_bit1)
			_commandSpecifcForCluster = true;

		// Check Manufacurer code
		if ((FrameControl & 0x04) == 1) {
			TransequenceNumber = _Data[3];
			CommandID = _Data[4];
			int x = 0;
			Payload = new byte[_Data.length - 5];
			for (int i = 5; i < _Data.length; i++)
				Payload[x++] = _Data[i];
		} else {
			TransequenceNumber = _Data[1];
			CommandID = _Data[2];
			int x = 0;
			Payload = new byte[_Data.length - 3];
			for (int i = 3; i < _Data.length; i++)
				Payload[x++] = _Data[i];

		}
		Server_to_Client = ((FrameControl & 0x08) == 1) ? true : false;// Direction
																		// Bit
		Disable_DefaultResponse = ((FrameControl & 0x10) == 1) ? true : false;// Disable
																				// Disable_DefaultResponse
																				// Bit

	}
}