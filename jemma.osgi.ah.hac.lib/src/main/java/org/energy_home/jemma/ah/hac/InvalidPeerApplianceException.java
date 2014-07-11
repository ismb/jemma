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
package org.energy_home.jemma.ah.hac;

/**
 * Situations related to an interaction with an appliance that is not accessible
 * (e.g. the appliance connection used for the interaction has been removed)
 * 
 */
public class InvalidPeerApplianceException extends ApplianceValidationException {
	private static final long serialVersionUID = -4776260979659656336L;

	/**
	 * Creates an {@link InvalidPeerApplianceException} 
	 */
	public InvalidPeerApplianceException() {
		super("InvalidPeerApplianceException");
	}
	
	/**
	 * Creates an {@link InvalidPeerApplianceException} on the specified error
	 * description
	 * 
	 * @param errorDescription
	 *            describes the type of error
	 */
	public InvalidPeerApplianceException(String errorDescription) {
		super(errorDescription);
	}
}
