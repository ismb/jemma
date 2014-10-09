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

import java.util.ArrayList;
import java.util.List;

import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to populate the Lqi Response received
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 * 
 * 
 */
public class Mgmt_LQI_rsp {
	public short _Status;
	public short _NeighborTableEntries;
	public short _StartIndex;
	public short _NeighborTableListCount;
	public List<NeighborTableLis_Record> NeighborTableList;
	private static final Logger LOG = LoggerFactory.getLogger(Mgmt_LQI_rsp.class);

	public Mgmt_LQI_rsp(byte[] data) {
		/* 02 00 01 00 01 */
		_Status = data[1];
		if (_Status == 0) {
			_NeighborTableEntries = (short) (data[2] & 0xFF);
			_StartIndex = (short) (data[3] & 0xFF);
			_NeighborTableListCount = (short) (data[4] & 0xFF);
			if (_NeighborTableListCount > 0) {
				NeighborTableList = new ArrayList<NeighborTableLis_Record>();
				byte[] _newData = org.energy_home.jemma.javagal.layers.business.Utils.copyOfRange(data, 5, data.length);
				for (int i = 0; i < _NeighborTableListCount; i++) {
					byte[] _newData_i = org.energy_home.jemma.javagal.layers.business.Utils.copyOfRange(_newData, (22 * i), 22 * (i + 1));
					NeighborTableList.add(new NeighborTableLis_Record(_newData_i));
				}
			} else {
				NeighborTableList = new ArrayList<NeighborTableLis_Record>();
			}
		} else {
			LOG.debug("\n\rReceived a not Success status from the LQI Req Status value:" + _Status + "\n\r");
		}

	}
}
