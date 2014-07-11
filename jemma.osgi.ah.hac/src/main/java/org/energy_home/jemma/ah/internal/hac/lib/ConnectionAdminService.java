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
package org.energy_home.jemma.ah.internal.hac.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.equinox.internal.util.timer.Timer;
import org.eclipse.equinox.internal.util.timer.TimerListener;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceFactory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceManager;
import org.energy_home.jemma.ah.hac.lib.ext.HacCommon;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.energy_home.jemma.ah.hac.lib.ext.PeerAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.PeerEndPoint;
import org.energy_home.jemma.ah.hac.lib.ext.PeerServiceClusterProxy;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConnectionAdminService implements TimerListener, IConnectionAdminService, ManagedServiceFactory {

	static final int saveTimerId = 1;

	public static final String WILDCARD_PID = "any";
	public static final String CORE_APP_PID = "ah.app.core";
	// !!! Previous value was "/"
	private final static String SCENARIOS_PATH = "";
	private final static String CONFIG_FILENAME = "cms-config.xml";

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionAdminService.class);
	
	private DocumentBuilderFactory factory;

	protected ArrayList positiveRules = new ArrayList();

	private Timer timer = null;
	private int saveTimeout = 1;
	
	private boolean useManagedApplianceServiceTracker = true;
	private CAManagedApplianceServiceTracker managedApplianceServiceTracker = null;
	
	private boolean patched = false; // true if an upgrade from 2.2.8 to 3.0.5 (hac.lib) has been detected.
	private boolean enableUpdatePatch = false;

//	/**
//	 * HashMap containing hashmap; each hashmap index is the appliance pid {
//	 * pid, { peerPid, peerAppliance } }
//	 */
//	private HashMap pid2PeerPidAppliances = new HashMap();

	/**
	 * Dictionary that permits to retrieve the IManagedAppliance service from
	 * its pid
	 */
	private Hashtable pid2appliance = new Hashtable();

//	// HashMap of an HashMap; the first hashmap index is the appliance pid; the
//	// second hashmap index is the
//	// peer appliance and contains the linked endpointIds
//	// { pid, { peerAppliance, [ linkedEpsIds ] } }
//	private HashMap pid2PeerAppliancesLinkedEndPointIds = new HashMap();

	private ComponentContext ctxt;

	public ConnectionAdminService() {
	}

	public synchronized void activate(ComponentContext ctxt, Map props) {
		LOG.debug("activated");
		this.ctxt = ctxt;
		this.loadConfiguration();
		
		if (useManagedApplianceServiceTracker) {
			this.managedApplianceServiceTracker = new CAManagedApplianceServiceTracker(this.ctxt.getBundleContext(), this);
			this.managedApplianceServiceTracker.open();
		}		
	}

	public synchronized void deactivate(ComponentContext ctxt) {
		LOG.debug("deactivated");
		if (this.managedApplianceServiceTracker != null) {
			this.managedApplianceServiceTracker.close();
		}
		this.saveConfiguration();
	}

	public void modified(ComponentContext ctxt, Map props) {
		Object a = props.get("it.telecomitalia.ah.connadmin.rules");
		LOG.debug("modified");
	}

	public synchronized void setManagedAppliance(IManagedAppliance appliance, Map props) {
		String appStatus;
		if (LOG.isDebugEnabled()) {
			LOG.debug("set Appliance " + appliance.getPid());
		}
		// skip any appliance that is under installation
		if (((appStatus = (String) props.get("ah.status")) != null) && (appStatus.equals("installing"))) {
			return;
		}

		this.pid2appliance.put(appliance.getPid(), appliance);
		activateRules(appliance);
	}
	
	protected synchronized void updatedManagedAppliance(IManagedAppliance appliance, Map props) {
		// TODO optimize this
		this.unsetManagedAppliance(appliance);
		this.setManagedAppliance(appliance, props);
	}

	public synchronized void unsetManagedAppliance(IManagedAppliance appliance) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("unset Appliance " + appliance.getPid());
		}
		this.deactivateConnections(appliance);
		this.pid2appliance.remove(appliance.getPid());
	}

	public synchronized void setDocumentBuilderFactory(DocumentBuilderFactory r) {
		factory = r;
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		factory.setCoalescing(false);
	}

	public synchronized void unsetDocumentBuilderFactory(DocumentBuilderFactory r) {
		if (factory == r)
			factory = null;
	}

	public synchronized void setTimer(Timer timer) {
		this.timer = timer;
	}

	public synchronized void unsetTimer(Timer timer) {
		this.timer = null;
	}

	protected void activateRules(IManagedAppliance appliance) {
		// we have a new IManagedAppliance. We have to check if an already
		// configured connections exist that have this IManagedAppliance as an
		// edge

		if (LOG.isDebugEnabled()) {
			LOG.debug("new appliance with pid '" + appliance.getPid() + "' found, checks if there are connections pending for it");
		}

		for (Iterator it = this.pid2appliance.values().iterator(); it.hasNext();) {
			IManagedAppliance appliance2 = (IManagedAppliance) it.next();

			if (appliance != appliance2) {
				if (checkConnectivityOnDb(appliance.getPid(), appliance2.getPid()) ||
						appliance2.getPid().equals(CORE_APP_PID) || appliance2.getPid().equals(CORE_APP_PID)) {
					boolean res = activateConnection(appliance, appliance2);
					if (res) {
						LOG.debug(appliance.getPid() + " <--> " + appliance2.getPid());
					}					
				}
			}
		}

//		// notifies the peer appliances
//		HashMap peerAppliances = (HashMap) pid2PeerAppliancesLinkedEndPointIds.get(appliance.getPid());
//		PeerAppliance peerAppliance = null;
//		PeerAppliance linkedPeerAppliance = null;
//		IManagedAppliance linkedPeerManagedAppliance = null;
//
//		// TODO: check if this is really necessary (changing order of
//		// previous calls checkPendingConnecions and appliance.init seems
//		// not working)
//
//		if (peerAppliances != null) {
//			for (Iterator iterator = peerAppliances.keySet().iterator(); iterator.hasNext();) {
//				peerAppliance = (PeerAppliance) iterator.next();
//				linkedPeerAppliance = peerAppliance.getLinkedPeerAppliance();
//				if (linkedPeerAppliance != null) {
//					linkedPeerManagedAppliance = (IManagedAppliance) pid2appliance.get(linkedPeerAppliance.getPid());
//					if (linkedPeerManagedAppliance != null)
//						linkedPeerManagedAppliance.peerApplianceConnected(peerAppliance, (int[][]) peerAppliances
//								.get(peerAppliance));
//				}
//			}
//		}
	}

	public synchronized boolean deactivateBinds(String appliancePid) throws HacException {
		IManagedAppliance appliance = this.getAppliance(appliancePid);
		if (appliance == null) {
			throw new HacException("appliance doesn't exists");
		}
		this.deactivateConnections(appliance);
		return true;
	}

	private boolean deactivateConnection(String managedAppliancePid1, String managedAppliancePid2) {
		IManagedAppliance managedAppliance1 = (IManagedAppliance) pid2appliance.get(managedAppliancePid1);
		IManagedAppliance managedAppliance2 = (IManagedAppliance) pid2appliance.get(managedAppliancePid2);
		return deactivateConnection(managedAppliance1, managedAppliance2);
	}
	
	/**
	 * Deactivate any connection belonging to IManagedAppliance passed as
	 * parameter.
	 * 
	 * @param appliance
	 *            The IManagedAppliance
	 */
	protected void deactivateConnections(IManagedAppliance appliance) {

		String[] connectedAppliancesPids = ((Appliance)appliance).getPeerAppliancesPids();

		if (connectedAppliancesPids != null) {
			for (int i = 0; i < connectedAppliancesPids.length; i++) {
				if (this.deactivateConnection(appliance.getPid(), connectedAppliancesPids[i])) {
					LOG.debug(appliance.getPid() + " <  > " + connectedAppliancesPids[i]);
				}
			}
		}
	}

	protected IManagedAppliance getAppliance(String appliancePid) {
		return (IManagedAppliance) this.pid2appliance.get(appliancePid);
	}

	protected boolean checkConnectivityOnDb(String appliance1Pid, String appliance2Pid) {
		Dictionary connectionProps1 = new Hashtable();

		Dictionary connectionProps2 = new Hashtable();

		connectionProps1.put("pid1", appliance1Pid);
		connectionProps1.put("pid2", appliance2Pid);

		connectionProps2.put("pid2", appliance1Pid);
		connectionProps2.put("pid1", appliance2Pid);

		for (int i = 0; i < positiveRules.size(); i++) {
			Filter f = (Filter) this.positiveRules.get(i);
			if (f.match(connectionProps1) || f.match(connectionProps2)) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean deactivateBind(String appliance1Pid, String appliance2Pid) {
		IManagedAppliance appliance1 = this.getAppliance(appliance1Pid);
		IManagedAppliance appliance2 = this.getAppliance(appliance2Pid);
		return this.deactivateConnection(appliance1, appliance2);
	}

	protected boolean deactivateConnection(IManagedAppliance appliance1, IManagedAppliance appliance2) {
		if (appliance1 == null || appliance2 == null) {
			LOG.warn("Appliance connection failed because at least one managed appliance is null");
			return false;
		}
		
		IEndPoint[] endPoints1 = appliance1.getEndPoints();
		IEndPoint[] endPoints2 = appliance2.getEndPoints();
		
		ApplianceManager manager1 = (ApplianceManager)appliance1.getApplianceManager();
		ApplianceManager manager2 = (ApplianceManager)appliance2.getApplianceManager();
		notifyDisconnectedPeerAppliances(manager1, endPoints1, appliance2.getPid());
		notifyDisconnectedPeerAppliances(manager2, endPoints2, appliance1.getPid());
		removePeerAppliances(manager1, endPoints1, appliance2.getPid());
		removePeerAppliances(manager2, endPoints2, appliance1.getPid());
		return true;
	}

	protected void notifyDisconnectedPeerAppliances (ApplianceManager applianceManager, IEndPoint[] endPoints, String appliancePid) {
		for (int i = 0; i < endPoints.length; i++) {
			IAppliance peerAppliance = ((EndPoint)endPoints[i]).getPeerAppliance(appliancePid);
			if (peerAppliance instanceof PeerAppliance && 
					!((PeerAppliance)peerAppliance).containsOnlyCommonClientClusters()) {
				applianceManager.peerApplianceDisconnected(((EndPoint)endPoints[i]), peerAppliance);
				((PeerAppliance)peerAppliance).setPeerValid(false);	
			}
		}
	}
	
	protected void removePeerAppliances (ApplianceManager applianceManager, IEndPoint[] endPoints, String appliancePid) {
		for (int i = 0; i < endPoints.length; i++) {
			IAppliance peerAppliance = ((EndPoint)endPoints[i]).getPeerAppliance(appliancePid);
			if (peerAppliance != null)
				applianceManager.removePeerAppliance(((EndPoint)endPoints[i]), peerAppliance);
		}
	}
	
	public synchronized void deleteAllRules() {
		// deactivate all the connections and then delete the rules
		Collection appliances = this.pid2appliance.values();
		for (Iterator it = appliances.iterator(); it.hasNext();) {
			IManagedAppliance appliance = (IManagedAppliance) it.next();
			this.deactivateConnections(appliance);
		}

		this.positiveRules.clear();
		this.saveConfiguration();
	}

	public synchronized String[] getPeerAppliancesPids(String appliancePid) throws HacException {
		IManagedAppliance managedAppliance = (IManagedAppliance)pid2appliance.get(appliancePid);
		if (managedAppliance == null)
			throw new HacException("invalid appliance pid");
		return ((Appliance)managedAppliance).getPeerAppliancesPids();
	}

	public synchronized IAppliance[] getPeerAppliances(String appliancePid, int endPointId) throws HacException {
		IManagedAppliance managedAppliance = (IManagedAppliance) pid2appliance.get(appliancePid);
		if (managedAppliance == null)
			throw new HacException("invalid appliance pid");

		return ((EndPoint)(managedAppliance.getEndPoint(endPointId))).getPeerAppliances();
	}

	public synchronized IAppliance[] getPeerAppliances(String appliancePid, int endPointId, int propertyKey, String propertyValue)
			throws HacException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("called browsePeerAppliances from " + appliancePid);
		}
		IManagedAppliance managedAppliance = (IManagedAppliance) pid2appliance.get(appliancePid);
		IAppliance peerAppliance = null;
		IApplianceFactory peerApplianceFactory = null;

		if (managedAppliance == null)
			throw new HacException("Auhtorization error: invalid appliance pid");

		IAppliance[] peerAppliancesArray = ((EndPoint)(managedAppliance.getEndPoint(endPointId))).getPeerAppliances();

		if (peerAppliancesArray == null || peerAppliancesArray.length == 0)
			return null;
		ArrayList resultArrayList = new ArrayList();
		IAppliance[] resultArray = null;

		if (propertyKey == IAppliance.APPLIANCE_TYPE_PROPERTY_KEY) {
			if (propertyValue.compareTo("") == 0) {
				// return the list of devices as it is!
				return peerAppliancesArray;
			}
			for (int i = 0; i < peerAppliancesArray.length; i++) {
				peerAppliance = peerAppliancesArray[i];
				if ((peerAppliance.getDescriptor().getType() != null)
						&& (peerAppliance.getDescriptor().getType().compareTo(propertyValue) == 0)) {
					resultArrayList.add(peerAppliance);
				}
			}
		} else if (propertyKey == IAppliance.APPLIANCE_LOCATION_PID_PROPERTY_KEY) {
			// returns the list of devices at the specific location
			if (propertyValue.compareTo("") == 0) {
				return peerAppliancesArray;
			}

			// TODO: add explicit properties on IAppliance to have a better
			// performance
			
			for (int i = 0; i < peerAppliancesArray.length; i++) {
				peerAppliance = peerAppliancesArray[i];
				if (peerAppliance != null) {
					String locationPid = (String) peerAppliance.getConfiguration().get(
							IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
					if (locationPid.equals(propertyValue))
						resultArrayList.add(peerAppliance);
				}
			}
		} else if (propertyKey == IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY_KEY) {

			// returns the list of devices at the specific location
			if (propertyValue.compareTo("") == 0) {
				return peerAppliancesArray;
			}

			// TODO: add explicit properties on IAppliance to have a better
			// performance
			
			for (int i = 0; i < peerAppliancesArray.length; i++) {
				peerAppliance = peerAppliancesArray[i];
				if (peerAppliance != null) {
					if (peerApplianceFactory != null) {
						String locationPid = (String) peerAppliance.getConfiguration().get(
								IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);

						if (locationPid.equals(propertyValue))
							resultArrayList.add(peerAppliance);
					}
				}
			}
		}
		if (resultArrayList == null || resultArrayList.size() == 0)
			return null;
		else {
			resultArray = new IAppliance[resultArrayList.size()];
			return (IAppliance[]) resultArrayList.toArray(resultArray);
		}
	}

	public synchronized boolean createConnection(String appliance1Pid, String appliance2Pid) throws ApplianceException {

		// create a rule for this connection

		this.validateConnection(appliance1Pid, appliance2Pid);

		// create the filter for this new connection
		String filterString = "(&(pid1=" + appliance1Pid + ")" + "(pid2=" + appliance2Pid + "))";
		Filter filter = null;
		try {
			filter = this.ctxt.getBundleContext().createFilter(filterString);
		} catch (InvalidSyntaxException e) {
			LOG.debug("syntax error in filter " + filterString);
			throw new ApplianceException("Internal error");
		}

		this.positiveRules.add(filter);

		this.saveConfigurationDelayed();

		activateRules();

		addedRuleEvent(filter);
		return false;
	}

	private void validateConnection(String appliance1Pid, String appliance2Pid) throws ApplianceException {
		if (appliance1Pid.equals(appliance2Pid)) {
			LOG.warn("connecting an application to itself is not allowed");
			throw new ApplianceException("connection between an appliance and itself not allowed");
		}
	}

	private boolean activateRules() {

		for (Iterator it1 = this.pid2appliance.values().iterator(); it1.hasNext();) {
			IManagedAppliance appliance1 = (IManagedAppliance) it1.next();
			if (appliance1 == null) {
				continue;
			}

			for (Iterator it2 = this.pid2appliance.values().iterator(); it2.hasNext();) {
				IManagedAppliance appliance2 = (IManagedAppliance) it2.next();
				if (appliance2 == null) {
					continue;
				}

				if (appliance1 != appliance2) {
					if (checkConnectivityOnDb(appliance1.getPid(), appliance2.getPid())) {
						boolean res = activateConnection(appliance1, appliance2);
						if (res) {
							LOG.debug(appliance1.getPid() + " <--> " + appliance2.getPid());
						}
					}
				}
			}
		}

		return true;
	}

	private void addedRuleEvent(Filter filter) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("added rule " + filter.toString() + ". This rule will be applied each time a new appliance wil be detected.");
		}
	}

	private boolean activateConnection(String managedAppliancePid1, String managedAppliancePid2) {
		IManagedAppliance managedAppliance1 = (IManagedAppliance) pid2appliance.get(managedAppliancePid1);
		IManagedAppliance managedAppliance2 = (IManagedAppliance) pid2appliance.get(managedAppliancePid2);
		return activateConnection(managedAppliance1, managedAppliance2);
	}
	
	/**
	 * Activate a connection between two already running appliances
	 * 
	 * @param managedAppliance1
	 *            The first appliance
	 * @param managedAppliance2
	 *            The second appliance
	 * @return <code>true</code>, if successful.
	 */
	protected boolean activateConnection(IManagedAppliance managedAppliance1, IManagedAppliance managedAppliance2) {	
		if (managedAppliance1 == null || managedAppliance2 == null) {
			LOG.warn("Appliance connection failed because at least one managed appliance is null");
			return false;
		}
		
		String appliance1Pid = managedAppliance1.getPid();
		String appliance2Pid = managedAppliance2.getPid();
		IEndPoint[] endPoints = managedAppliance1.getEndPoints();
		if (endPoints == null || endPoints.length < 1 || endPoints == null || endPoints.length < 1) {
			LOG.warn("Appliance connection failed because first end point list is null or has less than 2 elements: " +  managedAppliance1.getPid() + " <> " +  managedAppliance2.getPid());
			return false;
		}		
		
		EndPoint[] endPoints1 = new EndPoint[endPoints.length];
		for (int i = 0; i < endPoints1.length; i++) {
			endPoints1[i] = (EndPoint) endPoints[i];
			if (endPoints1[i].getPeerAppliance(appliance2Pid) != null)
				// already connected
				return false;
		}
		endPoints = managedAppliance2.getEndPoints();
		if (endPoints == null || endPoints.length < 1 || endPoints == null || endPoints.length < 1) {
			LOG.warn("Appliance connection failed because first end point list is null or has less than 2 elements: " +  managedAppliance1.getPid() + " <> " +  managedAppliance2.getPid());
			return false;
		}		
		EndPoint[] endPoints2 = new EndPoint[endPoints.length];
		for (int i = 0; i < endPoints2.length; i++) {
			endPoints2[i] = (EndPoint) endPoints[i];
			if (endPoints2[i].getPeerAppliance(appliance1Pid) != null)
				// already connected
				return false;
		}
		ApplianceManager manager1 = (ApplianceManager)((Appliance)managedAppliance1).getApplianceManager();
		ApplianceManager manager2 = (ApplianceManager)((Appliance)managedAppliance2).getApplianceManager();
		
		PeerAppliance[] peerAppliances1 = addPeerAppliances(managedAppliance1, endPoints1, managedAppliance2, endPoints2);
		if (peerAppliances1 == null ) {
			removePeerAppliances(manager1, endPoints1, managedAppliance2.getPid());
			return false;
		}
		PeerAppliance[] peerAppliances2 = addPeerAppliances(managedAppliance2, endPoints2, managedAppliance1, endPoints1);
		if (peerAppliances2 == null) {
			removePeerAppliances(manager1, endPoints1, managedAppliance2.getPid());
			removePeerAppliances(manager2, endPoints2, managedAppliance1.getPid());
			return false;
		}
		StringBuilder sb = new StringBuilder("Appliance connection completed:\n");
		connectionToString(sb, managedAppliance1, endPoints1, peerAppliances1);
		connectionToString(sb, managedAppliance2, endPoints2, peerAppliances2);
		LOG.trace(sb.toString());
		notifyConnectedPeerAppliances(manager1, endPoints1, peerAppliances1);
		notifyConnectedPeerAppliances(manager2, endPoints2, peerAppliances2);
		return true;
	}

	protected void connectionToString(StringBuilder sb, IManagedAppliance managedAppliance, EndPoint[] endPoints, PeerAppliance[] peerAppliances) {
		for (int i = 0; i < peerAppliances.length; i++) {
			if (peerAppliances[i] == null)
				continue;
			sb.append(managedAppliance.getPid());
			sb.append(":");
			sb.append(i);
			sb.append("(");
			sb.append(endPoints[i].getType());
			sb.append(") -> ");
			sb.append(peerAppliances[i].getPid());
			sb.append("{");
			IEndPoint[] peerEndPoints = peerAppliances[i].getEndPoints();
			for (int j = 0; j < peerEndPoints.length; j++) {
				if (j > 0)
					sb.append(", ");
				sb.append(peerEndPoints[j].getId());
				sb.append("(");
				sb.append(peerEndPoints[j].getType());
				sb.append(")");
			}
			sb.append("}\n");
		}
	}
	
	protected void notifyConnectedPeerAppliances (ApplianceManager applianceManager, EndPoint[] endPoints, PeerAppliance[] peerAppliances) {
		for (int i = 0; i < endPoints.length; i++) {
			if (peerAppliances[i] != null && !peerAppliances[i].containsOnlyCommonClientClusters())
				applianceManager.peerApplianceConnected(endPoints[i], peerAppliances[i]);
		}
	}
	
	protected PeerAppliance[] addPeerAppliances(IManagedAppliance managedAppliance1, EndPoint[] endPoints1, IManagedAppliance managedAppliance2, EndPoint[] endPoints2) {	
		PeerAppliance peerAppliance = null;
		PeerEndPoint peerEndPoint = null;
		String clusterName = null;
		IServiceCluster serviceCluster = null;
		ServiceCluster peerServiceCluster = null;
		ArrayList serverMatchingClusterNames = null;
		boolean isPeerApplianceValid = false;
		boolean isPeerEndPointValid = false;
		PeerAppliance[] peerAppliances = new PeerAppliance[endPoints1.length];
		for (int i = 0; i < endPoints1.length; i++) {
			peerAppliance = new PeerAppliance((Appliance)managedAppliance2, endPoints1[i]);
			isPeerApplianceValid = false;
			for (int j = 0; j < endPoints2.length; j++) {
				peerEndPoint = new PeerEndPoint(endPoints2[j]);
				isPeerEndPointValid = false;
				serverMatchingClusterNames = this.getMatchingClusterNames(managedAppliance2, endPoints2[j], managedAppliance1, endPoints1[i]);
				if (serverMatchingClusterNames != null && serverMatchingClusterNames.size() > 0) {
					for (Iterator iterator = serverMatchingClusterNames.iterator(); iterator.hasNext();) {
						clusterName = (String) iterator.next();
						serviceCluster = endPoints2[j].getServiceCluster(clusterName);
						if (serviceCluster instanceof ServiceCluster)
							peerServiceCluster = (ServiceCluster) serviceCluster;
						else 
							peerServiceCluster = ((PeerServiceClusterProxy)Proxy.getInvocationHandler((Proxy)serviceCluster)).getServiceCluster();
						if (peerServiceCluster != null) {
							try {
								peerEndPoint.registerCluster(peerServiceCluster);
								isPeerEndPointValid = true;
							} catch (ApplianceException e) {
								if (LOG.isErrorEnabled()) {
									LOG.error("Exception while creating unidrectional connection: " +  managedAppliance1.getPid() + " -> " +  managedAppliance2.getPid(), e);
								}
								return null;
							}
						}
					}			
				}
				// Create peer cluster listeners for end point 1
				String[] clusterListenersNames = endPoints2[j].getAdditionalClusterNames();
				if (clusterListenersNames != null) {
					for (int k = 0; k < clusterListenersNames.length; k++) {
						if (endPoints1[i].getServiceCluster(HacCommon.getPeerClusterName(clusterListenersNames[k])) != null)
							try {
								peerEndPoint.registerClusterListener(clusterListenersNames[k]);
								isPeerEndPointValid = true;
							} catch (ApplianceException e) {
								if (LOG.isErrorEnabled()) {
									LOG.error("Exception while creating unidrectional connection: " +  managedAppliance1.getPid() + " -> " +  managedAppliance2.getPid(), e);
								}
								return null;	
							}
					}
				}
				if (isPeerEndPointValid) {
					try {
						peerAppliance.addPeerEndPoint(peerEndPoint);
						isPeerApplianceValid = true;
					} catch (ApplianceException e) {
						if (LOG.isErrorEnabled()) {
							LOG.error("Exception while creating unidrectional connection: " +  managedAppliance1.getPid() + " -> " +  managedAppliance2.getPid(), e);
						}
						return null;
					}
				}
			}
			ApplianceManager manager1 = (ApplianceManager)((Appliance) (managedAppliance1)).getApplianceManager();
			if (isPeerApplianceValid) {
				manager1.addPeerAppliance(endPoints1[i], peerAppliance);
				peerAppliances[i] = peerAppliance;
			} else
				peerAppliances[i] = null;
		}
		return peerAppliances;
	}
	
	/**
	 * Return an ArrayList with the name of appliance1's matching service
	 * clusters (services exposed to appliance1)
	 * 
	 * @param appliance1
	 * @param ep1
	 * @param appliance2
	 * @param ep2
	 * @return
	 */
	protected ArrayList getMatchingClusterNames(IManagedAppliance appliance1, IEndPoint ep1, IManagedAppliance appliance2,
			IEndPoint ep2) {
		String[] serverMatchingClusterTypes = null;
		String[] clientMatchingclusterTypes = null;
		ArrayList matchingClusterNamesList = null;

		// Invoke matching algorithm on appliance1 for its server side clusters
		serverMatchingClusterTypes = ((ApplianceManager)appliance1.getApplianceManager()).getMatchingClusterTypes(ep1.getId(), IServiceCluster.SERVER_SIDE, appliance2
				.getDescriptor(), ep2.getType(), ep2.getServiceClusterTypes(IServiceCluster.CLIENT_SIDE), ep2
				.getAdditionalClusterTypes(IServiceCluster.CLIENT_SIDE));

		// Invoke matching algorithm on appliance1 for its client side clusters
		clientMatchingclusterTypes = ((ApplianceManager)appliance1.getApplianceManager()).getMatchingClusterTypes(ep1.getId(), IServiceCluster.CLIENT_SIDE, appliance2
				.getDescriptor(), ep2.getType(), ep2.getServiceClusterTypes(IServiceCluster.SERVER_SIDE), ep2
				.getAdditionalClusterTypes(IServiceCluster.SERVER_SIDE));

		if ((serverMatchingClusterTypes != null && serverMatchingClusterTypes.length > 0)
				|| (clientMatchingclusterTypes != null && clientMatchingclusterTypes.length > 0)) {
			matchingClusterNamesList = new ArrayList();
			if (serverMatchingClusterTypes != null)
				for (int i = 0; i < serverMatchingClusterTypes.length; i++) {
					matchingClusterNamesList.add(serverMatchingClusterTypes[i] + HacCommon.CLUSTER_NAME_SERVER_POSTFIX);
				}
			if (clientMatchingclusterTypes != null)
				for (int i = 0; i < clientMatchingclusterTypes.length; i++) {
					matchingClusterNamesList.add(clientMatchingclusterTypes[i] + HacCommon.CLUSTER_NAME_CLIENT_POSTFIX);
				}
		}

		return matchingClusterNamesList;
	}

	protected synchronized void dumpConnections() {
		if (this.positiveRules.size() == 0) {
			LOG.trace("Rules: <none>");
			return;
		} else {
			LOG.trace("Rule number\tRule");
			LOG.trace("-----------\t--------------------");
		}

		for (int i = 0; i < this.positiveRules.size(); i++) {
			if (LOG.isTraceEnabled()) {
				LOG.trace(i + "\t\t" + this.positiveRules.get(i));
			}
		}
	}

	public synchronized boolean loadConfiguration() {
		File configFile;
		InputStream stream = null;

		if (LOG.isDebugEnabled()) {
			LOG.debug("try to load '" + CONFIG_FILENAME + "'");
		}
		
		String configFilename = SCENARIOS_PATH + CONFIG_FILENAME;
		try {
			if (getProperty("it.telecomitalia.ah.updatepatch", enableUpdatePatch)) {
				patched  = PatchUpdateBug.patchUpdateBugOnHacLib(this.ctxt.getBundleContext(), configFilename);
			}
			configFile = ctxt.getBundleContext().getDataFile(configFilename);
			if (LOG.isDebugEnabled()) {
				LOG.debug("storage area is " + configFile);
			}
			stream = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			LOG.debug("no previously saved configuration found");
			return false;
		}

		try {
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			DocumentBuilder parser = factory.newDocumentBuilder();
			Document doc = parser.parse(new InputSource(stream));

			// parses the configuration file and updates the current
			// configuration present in memory
			traverseConfigurationTree(doc);
		} catch (IOException e) {
			LOG.warn(e.getMessage(), e);
			return false;
		} catch (SAXException e) {
			LOG.warn(e.getMessage(), e);
			return false;
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
			return false;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("configuration '" + configFilename + "' loaded successfully");
		}
		return true;
	}

	protected Document createDoc() {
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage(), e);
			return null;
		}

		Document doc = docBuilder.newDocument();
		return doc;
	}

	public synchronized boolean saveConfiguration() {

		Document doc = createDoc();

		Element configurationEl = doc.createElement("configuration");
		doc.appendChild(configurationEl);

		// Element connectionsEl = doc.createElement("connections");
		Element rulesEl = doc.createElement("rules");

		// configurationEl.appendChild(connectionsEl);
		configurationEl.appendChild(rulesEl);

		for (int i = 0; i < this.positiveRules.size(); i++) {
			Filter f = (Filter) this.positiveRules.get(i);
			Element ruleEl = doc.createElement("rule");
			ruleEl.setAttribute("filter", f.toString());
			rulesEl.appendChild(ruleEl);
		}

		String xmlConfig = doc2xmlString(doc);
		if (LOG.isDebugEnabled())
			LOG.debug(xmlConfig);

		// save the configuration on the filesystem
		File configFile = this.ctxt.getBundleContext().getDataFile(SCENARIOS_PATH + CONFIG_FILENAME);
		if (LOG.isDebugEnabled())
			LOG.debug("saving configuration into " + configFile.getPath());

		if (!configFile.isFile()) {
			try {

				// check if the parent directory exists, otherwise create it and
				// all the parent dirs
				File scenariosDir = configFile.getParentFile();

				if (!scenariosDir.exists()) {
					scenariosDir.mkdirs();
				}

				// create the file
				if (!configFile.createNewFile()) {
					return false;
				}
			} catch (IOException e1) {
				LOG.warn("unable to create file " + configFile.getPath(), e1);
				return false;
			}
		}

		FileOutputStream fos;

		try {
			fos = new FileOutputStream(configFile);
			fos.write(xmlConfig.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			LOG.warn("unable to open file " + configFile + " for writing.", e);
			return false;
		} catch (IOException e) {
			LOG.warn("unable to write file " + configFile, e);
			return false;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("configuration '" + CONFIG_FILENAME + "' saved successfully");
		}
		return true;
	}

	protected String doc2xmlString(Document doc) {

		final String XML_VERSION = "1.0";
		final String XML_ENCODING = "UTF-8";

		StringWriter strWriter = null;

		XMLSerializer probeMsgSerializer = null;
		OutputFormat outFormat = null;
		String xmlStr = null;
		try {
			probeMsgSerializer = new XMLSerializer();
			strWriter = new StringWriter();
			outFormat = new OutputFormat();

			// Setup format settings
			outFormat.setEncoding(XML_ENCODING);
			outFormat.setVersion(XML_VERSION);
			outFormat.setIndenting(true);
			outFormat.setIndent(4);

			// Define a Writer
			probeMsgSerializer.setOutputCharStream(strWriter);

			// Apply the format settings
			probeMsgSerializer.setOutputFormat(outFormat);

			// Serialize XML Document
			probeMsgSerializer.serialize(doc);
			xmlStr = strWriter.toString();
			strWriter.close();

		} catch (IOException ioEx) {
			LOG.error("exception: " + ioEx.getMessage());
			return null;
		}
		return xmlStr;
	}

	Node lastNode;
	Hashtable props;
	Object prop;

	protected void traverseConfigurationTree(Node node) {
		lastNode = node;
		int nodeType = node.getNodeType();

		switch (nodeType) {
		case Node.DOCUMENT_NODE:
			traverseConfigurationTree(((Document) node).getDocumentElement());
			break;

		// print element with attributes
		case Node.ELEMENT_NODE:
			NamedNodeMap attrs = node.getAttributes();
			String tag = node.getNodeName();

			if (tag == "rule") {
				String filter = attrs.getNamedItem("filter").getNodeValue();

				try {
					this.internalAddBindRule(filter);
				} catch (InvalidSyntaxException e) {
					LOG.warn(e.getMessage(), e);
				}

			}

			NodeList children = node.getChildNodes();

			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++) {
					traverseConfigurationTree(children.item(i));
				}
			}
			break;

		case Node.TEXT_NODE:
			break;
		}
	}

	protected String getTextContent(Node node) {
		NodeList childs = node.getChildNodes();

		for (int i = 0; i < childs.getLength(); i++) {
			Node child = childs.item(i);
			int nodeType = child.getNodeType();
			switch (nodeType) {
			case Node.TEXT_NODE:
				return child.getNodeValue();
			}
		}

		return null;
	}

	protected void setTextContent(Document doc, Node node, String text) {
		Text textNode = doc.createTextNode(text);
		node.appendChild(textNode);
	}

	protected Timer getTimer() {
		return timer;
	}

	/**
	 * Save the current configuration after the specified delay. If this method
	 * is called again within the delay, the timer is rearmed.
	 * 
	 * @param delay
	 *            Delay in seconds
	 */

	protected void saveConfigurationDelayed() {
		timerCancel(saveTimerId);
		timerStart(saveTimerId, saveTimeout);
	}

	protected void timerCancel(int event) {
		Timer time = (Timer) getTimer();
		time.removeListener(this, event);
	}

	protected void timerStart(int event, int timePeriod) {
		Timer time = (Timer) getTimer();
		time.notifyAfter(this, timePeriod, event);
	}

	public void timer(int event) {
		switch (event) {
		case saveTimerId:
			saveConfiguration();
			break;
		}
	}

	public void deleted(String arg0) {
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void updated(String arg0, Dictionary arg1) throws ConfigurationException {
		// TODO Auto-generated method stub

	}

	public void deleteConnection(String appliance1Pid, String appliance2Pid) {
		// TODO Auto-generated method stub

	}

	public synchronized ArrayList getBindRules() {
		return this.positiveRules;
	}

	public synchronized boolean removeBindRule(String pid) throws HacException {
		// FIXME: should revert any connection that was activated thanks to the
		// bind rule that is being deleted.
		Filter filter = (Filter) this.positiveRules.remove(Integer.parseInt(pid));

		if (filter != null) {
			this.saveConfigurationDelayed();
			this.deactivateRule(filter);
			return true;
		} else {
			throw new HacException("rule pid not found");
		}
	}

	protected void deactivateRule(Filter filter) {
		// try to reapply the remaining rules and drop
		// the connection if they don't match anymore
		for (Iterator it = this.pid2appliance.values().iterator(); it.hasNext();) {
			IManagedAppliance appliance1 = (IManagedAppliance) it.next();
			if (appliance1 == null) {
				LOG.warn("null appliance");
				continue;
			}

			String[] connectedAppliancesPids = ((Appliance)appliance1).getPeerAppliancesPids();

			if (connectedAppliancesPids != null) {
				for (int i = 0; i < connectedAppliancesPids.length; i++) {
					boolean stillConnected = this.checkConnectivityOnDb(appliance1.getPid(), connectedAppliancesPids[i]);
					if (!stillConnected) {
						if (this.deactivateConnection(appliance1.getPid(), connectedAppliancesPids[i])) {
							LOG.debug(appliance1.getPid() + " <  > " + connectedAppliancesPids[i]);
						}
					}
				}
			}
		}
	}

	public synchronized void addBindRule(String rule) throws InvalidSyntaxException {

		Filter filter = this.ctxt.getBundleContext().createFilter(rule);

		this.positiveRules.add(filter);
		this.saveConfigurationDelayed();
		activateRules();
		addedRuleEvent(filter);
	}

	private void internalAddBindRule(String rule) throws InvalidSyntaxException {
		Filter filter = this.ctxt.getBundleContext().createFilter(rule);
		this.positiveRules.add(filter);
		activateRules();
		addedRuleEvent(filter);
	}
	
	boolean getProperty(String name, boolean defaultValue) {
		String value = System.getProperty(name);
		
		if (value != null) {
			if (value.equals("true")) {
				return true;
			}
			else if (value.equals("false")) {
				return false;
			}
		}
		return defaultValue;	
	}
}
