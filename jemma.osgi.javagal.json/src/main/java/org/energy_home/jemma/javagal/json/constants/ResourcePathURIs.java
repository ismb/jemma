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
package org.energy_home.jemma.javagal.json.constants;
public interface ResourcePathURIs {
	public static final String VERSION = "/version";
	public static final String INFOBASE = "/ib";
    public static final String RESET = "/reset";
	public static final String STARTUP = "/startup";

    public static final String SERVICES = "/services";

	public static final String LOCALNODE = "/localnode";

	public static final String LOCALNODE_SERVICES = LOCALNODE + SERVICES;
    public static final String WSNNODES = "/wsnnodes";
	public static final String CHANNEL = "/channel";

	public static final String ALLWSNNODES = "/allwsnnodes";
    public static final String NODEDESCRIPTORSERVICELIST = "/nodedescriptorservicelist";
	public static final String ALLPERMIT_JOIN = "/allwsnnodes/permitjoin";

    public static final String LQIINFORMATION = "/lqi";

    public final static String FREQUENCY_AGILITY = "/frequencyagility";

	public final static String URI_FREQUENCY_AGILITY = LOCALNODE
			+ FREQUENCY_AGILITY;

}
