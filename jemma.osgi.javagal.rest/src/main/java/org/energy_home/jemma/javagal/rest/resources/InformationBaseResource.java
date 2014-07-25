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
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;

import org.energy_home.jemma.javagal.rest.GalManagerRestApplication;
import org.energy_home.jemma.javagal.rest.RestManager;
import org.energy_home.jemma.javagal.rest.util.ResourcePathURIs;
import org.energy_home.jemma.javagal.rest.util.Util;
import org.restlet.data.MediaType;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Resource file used to manage the API GET:getInfoBaseAttribute. PUT:setInfoBaseAttribute
 *  
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 *
 */
public class InformationBaseResource extends ServerResource {

	private GatewayInterface proxyGalInterface;

	@Get
	public void informationBaseGetmethod() {

		String attrString = (String) getRequest().getAttributes().get("attr");
		Long attrId = -1l;

		if (attrString == null) {

			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory attr parameter's missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;

		} else {
			try {
				attrId = Long.decode("0x" + attrString);
			} catch (NumberFormatException nfe) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage(nfe.getMessage());
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}
			if (!Util.isUnsigned8(attrId)) {

				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory id parameter's value invalid (Unsigned8). You provided: "
						+ attrId);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}
		}

		try {

			short shortAttrId = attrId.shortValue();
			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			String attributeString = proxyGalInterface.getInfoBaseAttribute(shortAttrId);
			
			
			
			
			
			Detail _det = new Detail();
			Info _info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.SUCCESS);
			_info.setStatus(_st);
			_det.getValue().add(attributeString);
			_info.setDetail(_det);
			getResponse().setEntity(Util.marshal(_info),
					MediaType.APPLICATION_XML);
			return;
			
		} catch (NullPointerException npe) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(npe.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;
		} catch (Exception e1) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage(e1.getMessage());
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;
		}
	}

	@Put
	public void setMethod(String body) {

		String attrString = (String) getRequest().getAttributes().get("attr");
		Long attrId = -1l;

		if (attrString == null) {
			Info info = new Info();
			Status _st = new Status();
			_st.setCode((short) GatewayConstants.GENERAL_ERROR);
			_st.setMessage("Error: mandatory attr parameter's missing.");
			info.setStatus(_st);
			Info.Detail detail = new Info.Detail();
			info.setDetail(detail);
			getResponse().setEntity(Util.marshal(info), MediaType.APPLICATION_XML);
			return ;

		} else {
			try {
				attrId = Long.decode("0x" + attrString);
			} catch (NumberFormatException nfe) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory callback id parameter incorrect (Unsigned8). You provided: "
						+ attrString);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}
			if (!Util.isUnsigned8(attrId)) {
				Info info = new Info();
				Status _st = new Status();
				_st.setCode((short) GatewayConstants.GENERAL_ERROR);
				_st.setMessage("Error: mandatory id parameter's value invalid (Unsigned8). You provided: "
						+ attrId);
				info.setStatus(_st);
				Info.Detail detail = new Info.Detail();
				info.setDetail(detail);
				getResponse().setEntity(Util.marshal(info),
						MediaType.APPLICATION_XML);
				return ;

			}
		}

		// TODO control if the http body is a simple value or the value is in an
		// Info object. The specification speak about a Value object but this
		// object is not defined.
		String value = body;

		try {
			short shortAttrId = attrId.shortValue();
			proxyGalInterface = getRestManager().getClientObjectKey(-1,
					getClientInfo().getAddress()).getGatewayInterface();
			
			
			proxyGalInterface.setInfoBaseAttribute(shortAttrId, value);
			
			Detail _det = new Detail();
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