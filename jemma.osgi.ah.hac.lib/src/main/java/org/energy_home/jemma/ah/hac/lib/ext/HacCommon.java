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
package org.energy_home.jemma.ah.hac.lib.ext;

import org.energy_home.jemma.ah.hac.IServiceCluster;

public class HacCommon {

	public static final String CLUSTER_ATTRIBUTE_SELECTOR_PREFIX = "/";

	/**
	 * Prefix associated to all methods of an {@link IServiceInterface} used to
	 * read an attribute of a service cluster. The value is {@value}
	 */
	public static final String CLUSTER_ATTRIBUTE_GETTER_PREFIX = "get";

	/**
	 * TODO: used for initial implementation of map attributes (do not use yet)
	 */
//	public static final String CLUSTER_ATTRIBUTE_SELECT_PREFIX = "select";

	/**
	 * Prefix associated to all methods of an {@link IServiceInterface} used to
	 * write an attribute of a service cluster. The value is {@value}
	 */
	public static final String CLUSTER_ATTRIBUTE_SETTER_PREFIX = "set";

	/**
	 * TODO: used for initial implementation of map attributes (do not use yet)
	 */
//	public static final String CLUSTER_ATTRIBUTE_PUT_PREFIX = "put";

	/**
	 * Prefix associated to all methods of an {@link IServiceInterface} used to
	 * model the execution of a service cluster command. The value is {@value}
	 */
	public static final String CLUSTER_COMMAND_PREFIX = "exec";

	/**
	 * Postfix used for all java interfaces class names that implements the
	 * "client" side of a service cluster The value is {@value}
	 */
	public static final String CLUSTER_NAME_CLIENT_POSTFIX = "Client";

	/**
	 * Postfix used for all java interface class names that implements the
	 * "server" side of a service cluster. The value is {@value}
	 */
	public static final String CLUSTER_NAME_SERVER_POSTFIX = "Server";

	public static String getClusterName(int clusterSide, String clusterType) {
		switch (clusterSide) {
		case IServiceCluster.CLIENT_SIDE:
			return clusterType + CLUSTER_NAME_CLIENT_POSTFIX;
		case IServiceCluster.SERVER_SIDE:
			return clusterType + CLUSTER_NAME_SERVER_POSTFIX;
		default:
			return null;
		}
	}

	public static String getPeerClusterName(String clusterName) {
		if (clusterName.endsWith(CLUSTER_NAME_CLIENT_POSTFIX))
			return clusterName.substring(0, clusterName.length() - CLUSTER_NAME_CLIENT_POSTFIX.length())
					+ CLUSTER_NAME_SERVER_POSTFIX;
		else if (clusterName.endsWith(CLUSTER_NAME_SERVER_POSTFIX))
			return clusterName.substring(0, clusterName.length() - CLUSTER_NAME_SERVER_POSTFIX.length())
					+ CLUSTER_NAME_CLIENT_POSTFIX;
		else
			return null;
	}

	public static int getClusterSide(String clusterName) {
		if (clusterName.endsWith(CLUSTER_NAME_CLIENT_POSTFIX))
			return IServiceCluster.CLIENT_SIDE;
		return IServiceCluster.SERVER_SIDE;
	}

	public static String getClusterType(String clusterName) {
		int index = clusterName.indexOf(CLUSTER_NAME_CLIENT_POSTFIX);
		if (index == -1)
			index = clusterName.indexOf(CLUSTER_NAME_SERVER_POSTFIX);
		if (index == -1)
			return null;
		else
			return clusterName.substring(0, index);

	}
}
