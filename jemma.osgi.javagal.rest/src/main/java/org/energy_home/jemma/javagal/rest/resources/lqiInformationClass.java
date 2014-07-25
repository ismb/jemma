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

import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.LQIInformation;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;

import java.math.BigInteger;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.Resources;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;


/**
 * Resource file used to manage the API GET:getLQIInformation(address).
 * 
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class lqiInformationClass extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void processGet() {
		String addrString = (String) getRequest().getAttributes().get("addr");
	
		Address address = new Address();
		if (addrString.length() > 4) {
			BigInteger addressBigInteger = BigInteger.valueOf(Long.parseLong(
					addrString, 16));
			address.setIeeeAddress(addressBigInteger);
		} else {
			Integer addressInteger = Integer.parseInt(addrString, 16);
			address.setNetworkAddress(addressInteger);
		}
		try {
			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			LQIInformation lqi = proxyGalInterface.getLQIInformation(address);
			
			
			Detail _det = new Detail();
			_det.getLQIInformation().add(lqi);
			Info _info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.SUCCESS);
			_info.setStatus(_st);
			
			_info.setDetail(_det);
			
			getResponse().setEntity(Util.marshal(_info),
					MediaType.APPLICATION_XML);
			return;
			
			

		} catch (Exception e) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(e.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;
		}
	}

	/**
	 * Gets the RestManager.
	 * 
	 * @return the RestManager.
	 */
	private RestManager getRestManager() {
		return ((GalManagerRestApplication) getApplication()).getRestManager();
	}
}