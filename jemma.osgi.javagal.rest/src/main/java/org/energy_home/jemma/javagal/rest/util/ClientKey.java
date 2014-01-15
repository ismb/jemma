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
package org.energy_home.jemma.javagal.rest.util;
/**
 * Client identification key parameters.
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class ClientKey {
	private String address;

	private int port = -1;

	/**
	 * Gets the client's address.
	 * 
	 * @return the client's address.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the client's address.
	 * 
	 * @param address
	 *            the client's address to set.
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Gets the client's port.
	 * 
	 * @return the client's port.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the client's port.
	 * 
	 * @param port
	 *            the client's port to set.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode())
				+ ((port == -1) ? 0 : port);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientKey other = (ClientKey) obj;
			if (other.getPort() == this.getPort()
					&& other.getAddress().equals(this.getAddress()))
				return true;



		return false;
	}

}
