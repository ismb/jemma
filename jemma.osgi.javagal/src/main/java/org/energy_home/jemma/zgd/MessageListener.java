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
package org.energy_home.jemma.zgd;

import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;

/**
 * Message listener interface. Objects interested in message's
 * notification must implement this interface.
 *
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public interface MessageListener {
	
	/**
	 * Called to notify registered Aps message listeners.
	 * 
	 * @param message
	 *            the message to notify.
	 */
	void notifyAPSMessage(APSMessageEvent message);
	
	/**
	 * Called to notify registered InterPAN message listeners.
	 * 
	 * @param message
	 *            the message to notify.
	 */
	void notifyInterPANMessage(InterPANMessageEvent message);
}
