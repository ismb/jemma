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
package org.energy_home.jemma.javagal.layers.data.interfaces;


import java.io.IOException;

import jssc.SerialPortException;

import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;

/**
 * Interface for all serial implementation of the FlexGrid key (jssc and RxTx).
 * @author 
 *         "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 
 */
public interface IConnector {
	/**
	 * Gets the actual DataLayer implementation object.
	 * 
	 * @return the actual DataLayer object.
	 */
	public IDataLayer getDataLayer();

	/**
	 * Initializes the dongle.
	 * 
	 * @throws Exception
	 *             if an error occurs in dongle initialization.
	 */
	public void initialize() throws Exception;

	/**
	 * Writes data on the dongle.
	 * 
	 * @param buff
	 *            the data to write.
	 * @throws Exception
	 *             if errors occurs in write process.
	 */
	public void write(ByteArrayObject buff) throws Exception;

	/**
	 * Tells if the dongle is connected or not.
	 * 
	 * @return true if dongle is connected or false if not.
	 */
	public boolean isConnected();

	/**
	 * Disconnects the dongle.
	 * 
	 * @throws IOException
	 *             if an error occurs in disconnection phase.
	 * @throws SerialPortException 
	 */
	public void disconnect() throws IOException, SerialPortException;
}
