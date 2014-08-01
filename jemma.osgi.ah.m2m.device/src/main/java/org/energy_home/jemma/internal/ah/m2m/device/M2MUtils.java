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
package org.energy_home.jemma.internal.ah.m2m.device;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.apache.http.HttpResponse;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.utils.rest.RestClient;

//
class M2MUtils {
	
	//FIXME this pattern to be checked carefully and documented: it seems legit - but it's risky - Why am I passing logger to this function ?
	static void mapDeviceException(Logger log, Exception e, String msg) throws M2MServiceException {
		log.error("M2MServiceException: " + msg + " - " + e.getClass().getName());
		if (log.isDebugEnabled())
			log.error("Mapped Exception", e);
		if (e instanceof M2MServiceException)
			throw (M2MServiceException) e;
		else
			throw new M2MServiceException(msg);
	}

	static void checkHttpResponseStatus(HttpResponse response) throws M2MHttpStatusException {
		if (RestClient.isUnauthorized(response))
			throw new M2MUnauthorizedException("Unauthorized exception in http response, status "
					+ RestClient.getResponseStatus(response));
		else if (!RestClient.isOkOrCreatedStatus(response)) {
			throw new M2MHttpStatusException("Exception in http response, status " + RestClient.getResponseStatus(response));
		}
	}

	static String getAhConstant(String name) {
		try {
			Class ahConstantsClass = Class.forName("org.energy_home.jemma.ah.AHConstants");
			Field field = ahConstantsClass.getField(name);
			return (String) field.get(null);
		} catch (Exception e) {
			return null;
		}
	}

}
