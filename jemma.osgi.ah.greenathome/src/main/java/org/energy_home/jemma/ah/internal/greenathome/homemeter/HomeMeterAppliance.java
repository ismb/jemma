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
package org.energy_home.jemma.ah.internal.greenathome.homemeter;


import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.energy_home.jemma.ah.cluster.ah.ConfigClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.BasicClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IPeerAppliancesListener;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Implements the Overload Control Application
 * 
 * @author 00918161
 * 
 */

public class HomeMeterAppliance extends Appliance implements IPeerAppliancesListener {
	
	private static final Logger LOG = LoggerFactory.getLogger( HomeMeterAppliance.class );

	protected static final String TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String FRIENDLY_NAME = "Homemeter";
	protected static final String END_POINT_TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String DEVICE_TYPE = null;
	protected static final String DEVICE_END_POINT_TYPE = null;

	private static Dictionary initialConfig;

	private static ApplianceDescriptor descriptor;

	private boolean isActive = false;

	protected Vector peerAppliances = new Vector();
	protected Hashtable lastPowerValues = new Hashtable();
	
	protected long lastEnergyTimestamp = -1;
	protected long lastStartTimestamp = -1;
	protected long lastEndTimestamp = -1;
	protected long lastIntervals = -1;
	protected double lastEnergy = 0;
	protected int lastConnectedWires = -1;
	
	private EndPoint greenathomeEndPoint = null;
	
	protected double totalPower = 0;
	
	static {
		initialConfig.put(IAppliance.APPLIANCE_NAME_PROPERTY, FRIENDLY_NAME);
		descriptor = new ApplianceDescriptor(TYPE, DEVICE_TYPE, FRIENDLY_NAME);
	}

	public HomeMeterAppliance() throws ApplianceException {
		super("Appliance.HomeMeter", initialConfig);
		
		EndPoint basicEndPoint = (EndPoint) getEndPoint(0);
		basicEndPoint.registerCluster(ConfigClient.class.getName());

		greenathomeEndPoint = (EndPoint) this.addEndPoint(END_POINT_TYPE);

		//greenathomeEndPoint.registerClusterListener(BasicInfoClient.class.getName());
		//greenathomeEndPoint.addServiceCluster(new GreenathomeIdentifyServer(greenathomeEndPoint));
		greenathomeEndPoint.registerCluster(BasicClient.class.getName());
		greenathomeEndPoint.registerCluster(SimpleMeteringClient.class.getName());
		greenathomeEndPoint.registerCluster(ApplianceControlClient.class.getName());
		greenathomeEndPoint.registerCluster(OnOffClient.class.getName());
		
		greenathomeEndPoint.registerPeerAppliancesListener(this);
	}

	protected void attributeValueReceived(String localEndPointId, String peerAppliancePid, String peerEndPointId,
			String peerClusterName, IAttributeValue peerAttributeValue) {
		Long value = (Long) peerAttributeValue.getValue();
		
		//lastPowerValues.put(wire, new Float(peerAttributeValue.getValue()));
//		if (attrName.equals("12.Power")) {
//			lastPowerValues.put(wire, new Float(value.floatValue()));
//			updateTotalPower();
//		}
	}


//	public String getValue() {
//		ZBAttributeValue attr = getAttribute("TotalPower");
//		if (attr != null) {
//			String s;
//
//			boolean complete = false;
//			if (complete) {
//				s = (attr.floatValue()) + " W, " + lastPowerValues.size() + " device";
//				s += " device";
//				if (lastPowerValues.size() > 1) {
//					s += "s";
//				}
//			} else {
//				// short
//				s = (attr.floatValue()) + " W (" + lastPowerValues.size() + ")";
//			}
//
//			return s;
//		} else {
//			return "n/a W";
//		}
//	}

	public boolean isStateChangable() {
		return false;
	}

	protected void updateTotalPower() {
		Collection values = lastPowerValues.values();
		Iterator i = values.iterator();
		double t = 0.0;
		while (i.hasNext()) {
			double f = ((Float) i.next()).floatValue();
			t += f;
		}

		totalPower = t;
	}


	public Vector calculateEnergy(long start, long end, long intervals) {
		Vector v = calculateEnergyDistrib(start, end, intervals);
		return null;
	}
	
	protected IServiceCluster getMatchingCluster(IAppliance peerAppliance, String clusterName) {
		IServiceCluster serviceCluster = null;
		IEndPoint[] endPoints1 = peerAppliance.getEndPoints();
		for (int i = 0; i < endPoints1.length; i++) {
			serviceCluster = endPoints1[i].getServiceCluster(clusterName);
			if (serviceCluster != null)
				break;
		}

		return serviceCluster;
	}


	protected double leggiEnergia(long start, long end, long intervals) {

		double totalEnergy = -1;

		if (start >= end) {
			return totalEnergy;
		}
		
		
		Vector infos = new Vector();
		
		for (Iterator iterator = peerAppliances.iterator(); iterator.hasNext();) {
			IAppliance peerAppliance = (IAppliance) iterator.next();
			
			SimpleMeteringServer simpleMeteringServer = null;
			simpleMeteringServer = (SimpleMeteringServer) getMatchingCluster(peerAppliance, SimpleMeteringServer.class.getName());

			if (simpleMeteringServer != null) {
				
		
			
//			AttributeData energyData = null;
//
//			try {
//				energyData = simpleMeteringServer.getAttributeData("SummationDelivered");
//			} catch (Exception e) {
//				log.debug("exception returned by getAttributeData on appliance " + simpleMeteringServer.getApplianceEndPointId());
//				continue;
//			}

//			if (energyData != null) {
//				WsncValue startValue = energyData.getNearValue(start, WsncRestAdapter.After);
//				WsncValue endValue = energyData.getNearValue(end, WsncRestAdapter.Before);
//
//				if ((startValue == null) || (endValue == null)) {
//					continue;
//				}
//
//				if (startValue.date > endValue.date) {
//					continue;
//				}
//
//				totalEnergy += endValue.value - startValue.value;
//			} else {
//				log.debug("unable to retrieve AttributeData 12.Energy from the WSNC for device " + simpleMeteringServer.getApplianceEndPointId() + ": device probably not attached!");
//			}
				
			}
			
		}		
		return totalEnergy;
	}

	/**
	 * Returns the energy consumed in the passed period
	 * 
	 * @param start
	 * @param end
	 * @param intervals
	 * @return the energy or -1 if were not possible to calculate it!
	 */

	public double getEnergy(long start, long end, long intervals) {

		double totalEnergy = 0;
//		long oneHour = 1000 * 60 * 5;
//		Calendar now = Calendar.getInstance();
//		long t = now.getTimeInMillis() / 1000;
//
//		List wires = getWires("Monitoring");
//		int connectedWires = wires.size();
//
//		// cache values
//		boolean cache = false;
//
//		if ((lastEnergyTimestamp >= 0) && (cache)) {
//			if ((lastStartTimestamp == start) && (lastEndTimestamp == end) && (lastIntervals == intervals) && (connectedWires == lastConnectedWires)) {
//				if ((t - lastEnergyTimestamp) >= oneHour) {
//					lastEnergyTimestamp = t;
//					totalEnergy = leggiEnergia(start, end, intervals);
//					if (totalEnergy >= 0) {
//						lastEnergyTimestamp = t;
//					}
//
//					return totalEnergy;
//				} else {
//					return lastEnergy;
//				}
//			}
//		}
//
//		totalEnergy = leggiEnergia(start, end, intervals);
//
//		if (totalEnergy >= 0) {
//			lastStartTimestamp = start;
//			lastEndTimestamp = end;
//			lastIntervals = intervals;
//			lastEnergyTimestamp = t;
//			lastConnectedWires = connectedWires;
//		}

		return totalEnergy;
	}

	// http://localhost/post-json?objectid=HacApplication.HomeMeter&method=calculate_energy_distrib&param0=1&param1=2

	public Vector calculateEnergyDistrib(long start, long end, long intervals) {
		
		Vector table = new Vector();
		
//		if (start >= end) {
//			return null;
//		}
//
//		/*
//		 * 
//		 * Data format [ {label: "Zona TV", data: [[1, 90.0]]}, {[label:
//		 * "Boiler", data: [[1, null]]}, {[label: "Lampadina", data: [1, 20.0]]}
//		 * ]
//		 */
//
//		boolean fake = false;
//		
//
//		List wires = this.getWires("Monitoring");
//
//		Iterator it = wires.iterator();
//
//		boolean dataFound = false;
//
//		while (it.hasNext()) {
//			VirtualAppliance wire = (VirtualAppliance) it.next();
//			try {
//				AttributeData energyData = wire.getAttributeData("12.Energy");
//				if (energyData != null) {
//					WsncValue startValue = energyData.getNearValue(start, WsncRestAdapter.After);
//					WsncValue endValue = energyData.getNearValue(end, WsncRestAdapter.Before);
//
//					if ((startValue == null) || (endValue == null)) {
//						log.debug("startValue or endValue or both are not available for VA " + wire.getType());
//						continue;
//					}
//
//					if (startValue.date > endValue.date) {
//						continue;
//					}
//
//					Hashtable row = new Hashtable();
//					Vector dataSeries = new Vector();
//					Vector data = new Vector();
//
//					dataSeries.add(data);
//
//					data.add(new Integer(1));
//					data.add(new Double(endValue.value - startValue.value));
//
//					row.put("label", wire.getType());
//					row.put("data", dataSeries);
//
//					dataFound = true;
//
//					table.add(row);
//				} else {
//					log.debug("unable to retrieve AttributeData 12.Energy from the WSNC for device " + wire.getType() + ": device probably not attached!");
//				}
//			} catch (Exception e) {
//				log.debug("exception reading energy for VA " + wire.getType());
//			}
//		}
//
//		if (dataFound) {
//			return table;
//		} else {
//			return null;
//		}
		
		return null;
	}

	public void attributeChanged(String name) {
		// TODO Auto-generated method stub
	}

	public void notifyPeerApplianceConnected(String peerAppliancePid) {
	}

	public void notifyPeerApplianceDisconnected(String peerAppliancePid) {
		updateTotalPower();
	}
	
	public void notifyPeerApplianceUpdated(String peerAppliancePid) {
		updateTotalPower();
	}

	public void startup() {
		LOG.debug("Home Meter started");
	}

	public void shutdown() {
		LOG.debug("Home Meter stopped");
	}
}
