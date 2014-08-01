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
package org.energy_home.jemma.javagal.rest.resources;

import java.util.Arrays;

import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;





/**
 * Resource file used to manage the API GET:URL menu.
 *
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class netDefaultIbLevelResource extends ServerResource {

	
	
	
	@Get
	public void represent() {
		Detail _det = new Detail();
		Info _info = new Info();
		Status _st = new Status();
		_st.setCode((short) GatewayConstants.SUCCESS);
		_info.setStatus(_st);
		_det.getValue().add("a1 - nwkSecurityMaterialSet (The two entries for the security material set, does not includes incoming counters.)");
		_det.getValue().add("c3 - apsChannelMask (Mask of channels to form/join)");
		_det.getValue().add("c4 - apsUseExtendedPANID (Extended PAN ID)");
		_det.getValue().add("c8 - apsUseInsecureJoin (Use secure or insecure join)");
		_det.getValue().add("80 - nwkPanId (The PAN Identifier for the PAN of which the device is amember.)");
		_det.getValue().add("9A - nwkExtendedPANID (The Extended PAN Identifier for the PAN of which the device is a member.)");
		_det.getValue().add("A0 - nwkSecurityLevel");
		_det.getValue().add("96 - nwkShortAddress");
		_det.getValue().add("DA - nwkDeviceType");
		_det.getValue().add("DB - nwkSoftwareVersion");
		_det.getValue().add("E6 - SASNwkKey");
		//_det.getValue().add("85 - MacKey");
		
		_info.setDetail(_det);
		getResponse().setEntity(Util.marshal(_info), MediaType.APPLICATION_XML);
		return;

	}
}