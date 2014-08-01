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

import org.energy_home.jemma.javagal.layers.data.implementations.Utils.DataManipulation;

/**
 * * Class used to populate the Lqi NeighborTableLis_Record of the Lqi_Response
 * received This class is shared for the LqiRequest procedure
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class NeighborTableLis_Record {
	public long _Extended_PAN_Id;
	public long _Extended_Address;
	public int _Network_Address;
	public byte _Device_Type_RxOnWhenIdle_Relationship;
	public byte _Device_Type;
	public byte _RxOnWhenIdle;
	public byte _Relationship;
	public byte _Permitting_Joining;
	public long _Depth;
	public long _LQI;

	public NeighborTableLis_Record(byte[] _data) {
		/* 650040000086DE30 A30020000086DE30 9576 15 00 01 8A */
		if (_data.length >= 21) {
			_Extended_PAN_Id = DataManipulation.toLong(_data[7], _data[6], _data[5], _data[4], _data[3], _data[2], _data[1], _data[0]);
			_Extended_Address = DataManipulation.toLong(_data[15], _data[14], _data[13], _data[12], _data[11], _data[10], _data[9], _data[8]);
			_Network_Address = DataManipulation.toIntFromShort(_data[17], _data[16]);
			byte _DRRR = _data[18];
			_Device_Type_RxOnWhenIdle_Relationship = _DRRR;
			_Device_Type = (byte) (_DRRR & 0x03);/* 0 and 1 bit */
			_RxOnWhenIdle = (byte) ((_DRRR & 0x0C) >> 0x02);/* 2 and 3 bit */
			_Relationship = (byte) ((_DRRR & 0x70) >> 0x04);/* 4 and 5 and 6 bit */
			byte _PR = _data[19];
			_Permitting_Joining = (byte) (_PR & 0x03);/* 0 and 1 bit */
			_Depth = (_data[20] & 0xFF);
			_LQI = (_data[21] & 0xFF);
		}
	}
}
