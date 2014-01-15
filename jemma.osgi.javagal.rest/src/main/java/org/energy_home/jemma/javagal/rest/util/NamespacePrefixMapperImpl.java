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

import java.util.HashMap;
import java.util.Map;

import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;



/**
 * Implementation for {@code NamespacePrefixMapper}.
 * 
 * @author
 *   "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class NamespacePrefixMapperImpl extends NamespacePrefixMapper {
	private static final String[] EMPTY_STRING = new String[0];

	private Map prefixToUri = null;
	private Map uriToPrefix = null;

	private void init() {
		prefixToUri = new HashMap();
		prefixToUri.put("tns", "http://www.zigbee.org/GWGRESTSchema");
		prefixToUri.put("gal", "http://www.zigbee.org/GWGSchema");
		
		uriToPrefix = new HashMap();
		for (Object prefix : prefixToUri.keySet()) {
			uriToPrefix.put(prefixToUri.get(prefix), prefix);
		}
	}

	@Override
	public String getPreferredPrefix(String namespaceUri, String suggestion,
			boolean requirePrefix) {
		if (uriToPrefix == null)
			init();

		if (uriToPrefix.containsKey(namespaceUri)) {
			return (String) uriToPrefix.get(namespaceUri);
		}

		return suggestion;
	}

	@Override
	public String[] getContextualNamespaceDecls() {
		return EMPTY_STRING;
	}

	@Override
	public String[] getPreDeclaredNamespaceUris() {
		// TODO Auto-generated method stub
		return EMPTY_STRING;

	}

	@Override
	public String[] getPreDeclaredNamespaceUris2() {
		return new String[] { "", (String) prefixToUri.get("") };

	}
}
