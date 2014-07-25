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
 * Represents an end point of an A@H appliance
 * <p>
 * An end point groups a set of homogeneous functionalities that are exposed to
 * other appliances through service clusters
 * 
 * @see IAppliance
 * @see IServiceCluster
 */
public interface IEndPoint {
	/**
	 * The identifier associated to the end point implemented by all A@H
	 * appliances and used to expose common functionalities managed by the
	 * framework (e.g. a configuration service cluster that can be used to
	 * access to peer appliances' name, location and category information). The
	 * value is {@value} .
	 */
	public static final int COMMON_END_POINT_ID = 0;
	/**
	 * The identifier associated to the end point implemented by all A@H
	 * appliances to interact with the framework and to expose specific
	 * functionalities to other appliances. The value is {@value} .
	 */
	public static final int DEFAULT_END_POINT_ID = 1;
	/**
	 * The type associated to the end point implemented by all A@H appliances
	 * and used to expose common functionalities managed by the framework (see
	 * {@link #COMMON_END_POINT_ID}). The value is {@value} .
	 */
	public static final String COMMON_END_POINT_TYPE = "ah.ep.common";

	/**
	 * Returns the appliance object that exposes this end point
	 * 
	 * @return The {@link IAppliance}
	 */
	public IAppliance getAppliance();

	/**
	 * Returns the identifier associated to this end point
	 * 
	 * @return A non negative {@code int} identifier for this end point
	 * 
	 */
	public int getId();

	/**
	 * Returns the type associated to this end point
	 * 
	 * @return The end point type
	 */
	public String getType();

	/**
	 * Checks the availability of all the services ({@link IServiceCluster}) 
	 * implemented by this end point
	 * 
	 * @return {@code true} if all cluster services are currently accessible, {@code false} otherwise
	 */
	public boolean isAvailable();

	/**
	 * 
	 * Returns the list of all client or server (typically client) service
	 * cluster types exposed by this end point interface without implementing
	 * any attributes/commands.
	 * 
	 * @param clusterSide
	 *            Specifies the side ({@link IServiceCluster#CLIENT_SIDE} or
	 *            {@link IServiceCluster#SERVER_SIDE}) of the requested cluster
	 *            types
	 * 
	 * @return An array with all the requested client or server cluster types;
	 *         an empty array is returned when no cluster types are found for
	 *         the specified side
	 * 
	 */
	public String[] getAdditionalClusterTypes(int clusterSide);

	/**
	 * Returns the list of all client and server (typically client) service
	 * cluster names exposed by this end point interface without implementing
	 * any attributes/commands.
	 * 
	 * @return An array with all the requested client and server cluster names;
	 *         an empty array is returned when no cluster names are found
	 */
	public String[] getAdditionalClusterNames();

	/**
	 * Returns the list of all implemented client and server service cluster,
	 * whose attributes/commands are exposed by this end point interface.
	 * 
	 * @return An array with all the implemented client and server cluster 
	 *         {@link IServiceCluster} interfaces; an empty array is returned 
	 *         when no implemented cluster types are found
	 */
	public IServiceCluster[] getServiceClusters();	
	
	/**
	 * Returns the list of all implemented client or server service cluster,
	 * whose attributes/commands are exposed by this end point interface.
	 * 
	 * @param clusterSide
	 *            Specifies the side ({@link IServiceCluster#CLIENT_SIDE} or
	 *            {@link IServiceCluster#SERVER_SIDE}) of the requested cluster
	 *            types.
	 * 
	 * @return An array with all the implemented client or server cluster 
	 *         {@link IServiceCluster} interfaces; an empty array is returned 
	 *         when no implemented cluster types are found for the specified side
	 */
	public IServiceCluster[] getServiceClusters(int clusterSide);	
	
	/**
	 * Returns the list of all implemented client or server service cluster
	 * types, whose attributes/commands are exposed by this end point interface.
	 * 
	 * @param clusterSide
	 *            Specifies the side ({@link IServiceCluster#CLIENT_SIDE} or
	 *            {@link IServiceCluster#SERVER_SIDE}) of the requested cluster
	 *            types.
	 * 
	 * @return An array with all the requested client or server cluster types;
	 *         an empty array is returned when no implemented cluster types are
	 *         found for the specified side
	 */
	public String[] getServiceClusterTypes(int clusterSide);

	/**
	 * Returns the list of all implemented client and server service cluster
	 * names, whose attributes/commands are exposed by this end point interface.
	 * 
	 * @return An array with all the requested client and server cluster names;
	 *         an empty array is returned when no implemented clusters names are
	 *         found
	 */
	public String[] getServiceClusterNames();

	/**
	 * Return the service cluster interface associated to an implemented cluster
	 * <p>
	 * This interface can be casted to the corresponding specific interface,
	 * e.g. the {@code IServiceCluster} object returned by the invocation of the
	 * method {@code
	 * getServiceCluster(org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer.
	 * class.getName())} can be casted to the class {@code
	 * org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer}
	 * 
	 * @param clusterName
	 *            The name of the requested cluster
	 * @return The {@link IServiceCluster} interface associated to the requested
	 *         cluster, {@code null} in case the requested service cluster is
	 *         not found
	 */
	public IServiceCluster getServiceCluster(String clusterName);
	
//	/**
//	 * Returns the service clusters listener interface registered for this end
//	 * point (used to receive asynchronous notification from all service
//	 * clusters exposed by peer appliances' end points connected to this end
//	 * point).
//	 * 
//	 * @return The service cluster listener interface, {@code null} in case no
//	 *         listener has been registered
//	 * 
//	 * @see IServiceClustersListener
//	 */
//	public IServiceClustersListener getServiceClustersListener();

}