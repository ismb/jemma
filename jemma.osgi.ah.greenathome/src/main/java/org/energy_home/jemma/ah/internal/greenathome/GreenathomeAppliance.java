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
package org.energy_home.jemma.ah.internal.greenathome;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.eclipse.core.runtime.jobs.Job;
import org.energy_home.jemma.ah.cluster.ah.ConfigClient;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockClient;
import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer;
import org.energy_home.jemma.ah.cluster.zigbee.custom.SimpleMetering4NoksClient;
import org.energy_home.jemma.ah.cluster.zigbee.custom.SimpleMetering4NoksServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.SignalStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.general.BasicClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.IdentifyServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffClient;
import org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer;
import org.energy_home.jemma.ah.cluster.zigbee.hvac.ThermostatClient;
import org.energy_home.jemma.ah.cluster.zigbee.hvac.ThermostatServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.IlluminanceMeasurementClient;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.IlluminanceMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.OccupancySensingClient;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.OccupancySensingServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.RelativeHumidityMeasurementClient;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.RelativeHumidityMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.TemperatureMeasurementClient;
import org.energy_home.jemma.ah.cluster.zigbee.measurement.TemperatureMeasurementServer;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringClient;
import org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneClient;
import org.energy_home.jemma.ah.cluster.zigbee.security.IASZoneServer;
import org.energy_home.jemma.ah.cluster.zigbee.security.ZoneEnrollResponse;
import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer;
import org.energy_home.jemma.ah.configurator.IConfigurator;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.greenathome.GreenAtHomeApplianceService;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IEndPointTypes;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.IPeerAppliancesListener;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.PeerServiceClusterProxy;
import org.energy_home.jemma.ah.hap.client.AHContainerAddress;
import org.energy_home.jemma.ah.hap.client.AHContainers;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfigurator;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.hac.adapter.http.AhHttpAdapter;
import org.energy_home.jemma.hac.adapter.http.HttpImplementor;
import org.energy_home.jemma.m2m.ContentInstance;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//TODO: check merge, a lot's of changes in this class from 3.3.0

//FIXME consider refactoring/renaming
public class GreenathomeAppliance extends Appliance implements HttpImplementor, IServiceClustersListener, IPeerAppliancesListener, IManagedAppliance, GreenAtHomeApplianceService, IASZoneClient, ApplianceControlClient, IlluminanceMeasurementClient {

	private static final Logger LOG = LoggerFactory.getLogger(GreenathomeAppliance.class);

	private static final boolean logEnabled = false; // FIXME this variable
														// should be removed:
														// log has already some
														// behaviour

	protected static final String TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String FRIENDLY_NAME = "Green@Home";
	protected static final String END_POINT_TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String DEVICE_TYPE = null;

	private static final String SMARTINFO_APP_TYPE = "org.energy_home.jemma.ah.zigbee.metering";
	private static final String APPLIANCE_ID_SEPARATOR = "-";
	private Properties props;

	// Returns an array with two items: appliance pid and end point id
	public static String[] getDeviceIds(String applianceId) {
		String[] deviceIds = new String[2];
		if (applianceId.equals(AHContainerAddress.ALL_ID_FILTER)) {
			deviceIds[0] = AHContainerAddress.ALL_ID_FILTER;
			deviceIds[1] = AHContainerAddress.ALL_ID_FILTER;
		} else {
			StringTokenizer st = new StringTokenizer(applianceId, APPLIANCE_ID_SEPARATOR);
			int i = 0;
			while (st.hasMoreElements()) {
				deviceIds[i++] = (String) st.nextElement();
			}
			if (i == 1)
				deviceIds[1] = AHContainerAddress.DEFAULT_END_POINT_ID;
		}
		return deviceIds;
	}

	// Returns applianceId (= deviceId) from appliancePid and endPointId
	public static String getApplianceId(String appliancePid, int endPointId) {
		String result = appliancePid;
		if (endPointId != IEndPoint.DEFAULT_END_POINT_ID) {
			StringBuilder sb = new StringBuilder(appliancePid);
			sb.append(APPLIANCE_ID_SEPARATOR);
			sb.append(endPointId);
			result = sb.toString();
		}
		return result;
	}

	// Convert a dictionary to a map
	public static Map convertToMap(Dictionary source) {
		Map sink = new HashMap(source.size());
		for (Enumeration keys = source.keys(); keys.hasMoreElements();) {
			Object key = keys.nextElement();
			sink.put(key, source.get(key));
		}
		return sink;
	}

	private EndPoint greenathomeEndPoint = null;
	private AhHttpAdapter ahHttpAdapter;

	private String applicationWebAlias = "/";

	// protected Vector peerAppliances = new Vector();
	protected Hashtable demandFormattings = new Hashtable();
	protected Hashtable istantaneousDemands = new Hashtable();

	DecimalFormat OutputPower = new DecimalFormat("#0.000 kW");

	private ESPService espService;

	private boolean useReportingOnSimpleMetering = false;
	private boolean useReportingOnApplianceControlServer = true;

	private IEndPointRequestContext context = null;

	private static Hashtable initialConfig = new Hashtable();

	private static ApplianceDescriptor descriptor;

	protected Job readInfosJob;

	private IHacService hacService;

	// private HashMap installingAppliances = new HashMap();

	private IConnectionAdminService connectionAdminService;

	private boolean fakeMode = false;

	private IConfigurator configurator = null;

	private IM2MHapService hapService;
	private M2MDeviceConfigurator m2mDevice;

	// !!!Multieps: changed to true to read appliance status
	// private boolean readApplianceStatus = false;

	private Object lockPlatform = new Object();
	private Object lockEsp = new Object();
	private Object lockZbNwkMngr = new Object();
	private Object lockGatH = new Object();

	private HashMap onOffValues = new HashMap();
	private HashMap zoneStatusTable = new HashMap();
	private HashMap occupancySensing = new HashMap();
	private HashMap measuredValues = new HashMap();
	private HashMap iasZoneTypeValues = new HashMap();

	private ArrayList<Double> forecast = new ArrayList<Double>();
	private String forecast_debug = "";
	private static Thread PVThread;

	static {
		initialConfig.put(IAppliance.APPLIANCE_NAME_PROPERTY, FRIENDLY_NAME);
		descriptor = new ApplianceDescriptor(TYPE, DEVICE_TYPE, FRIENDLY_NAME);
	}

	public GreenathomeAppliance() throws ApplianceException {
		super("ah.app.Greenathome", initialConfig);

		// FIXME config aspects of this bundle to be checked.

		greenathomeEndPoint = (EndPoint) addEndPoint(END_POINT_TYPE);

		greenathomeEndPoint.registerCluster(ConfigClient.class.getName());
		greenathomeEndPoint.registerCluster(ApplianceControlClient.class.getName(), this);
		greenathomeEndPoint.registerCluster(OnOffClient.class.getName());
		// greenathomeEndPoint.registerCluster(OnOffServer.class.getName(),
		// this);
		greenathomeEndPoint.registerCluster(BasicClient.class.getName());
		greenathomeEndPoint.registerCluster(SimpleMeteringClient.class.getName());
		greenathomeEndPoint.registerCluster(SimpleMetering4NoksClient.class.getName());
		greenathomeEndPoint.registerCluster(ThermostatClient.class.getName());
		greenathomeEndPoint.registerCluster(RelativeHumidityMeasurementClient.class.getName());
		greenathomeEndPoint.registerCluster(IASZoneClient.class.getName(), this);
		greenathomeEndPoint.registerCluster(OccupancySensingClient.class.getName());
		greenathomeEndPoint.registerCluster(TemperatureMeasurementClient.class.getName());
		greenathomeEndPoint.registerCluster(IlluminanceMeasurementClient.class.getName());

		greenathomeEndPoint.registerCluster(LevelControlClient.class.getName());
		greenathomeEndPoint.registerCluster(ColorControlClient.class.getName());
		greenathomeEndPoint.registerCluster(ApplianceControlClient.class.getName());
		/* Added by Marco */
		greenathomeEndPoint.registerCluster(DoorLockClient.class.getName());
		greenathomeEndPoint.registerCluster(WindowCoveringClient.class.getName());
		/* End by Marco */

		this.greenathomeEndPoint.registerServiceClustersListener(this);
		greenathomeEndPoint.registerPeerAppliancesListener(this);

		context = greenathomeEndPoint.getDefaultRequestContext();
		context.setMaxAgeForAttributeValues(25000);//In order to read from the cache

		try {
			ahHttpAdapter = new AhHttpAdapter(this, this.applicationWebAlias);
		} catch (Throwable e) {
			LOG.error("Exception on GreenathomeAppliance Constructor", e);
		}

		// ///
		// eseguo la routine di forecast subito e ogni giorno in orario consono
		// PVThread = new Thread(new Runnable() {
		// public void run() {
		// getPVForecast();
		// }
		// });
		// PVThread.start();
		//
		// new Timer().scheduleAtFixedRate(new TimerTask() {
		// @Override
		// public void run() {
		// if(!PVThread.isAlive()){
		// PVThread = new Thread(new Runnable() {
		// public void run() {
		// getPVForecast();
		// }
		// });
		// PVThread.start();
		// }
		// }
		// }, getTomorrow10AM(), 24*60*60*1000); //ripeto alle 10 e ogni 24 ore

	}

	public IApplianceDescriptor getDescriptor() {
		return descriptor;
	}

	protected void attributeValueReceived(String localEndPointId, String peerAppliancePid, String peerEndPointId, String peerClusterName, IAttributeValue peerAttributeValue) {
	}

	protected void setHttpService(HttpService s) {
		synchronized (lockGatH) {
			ahHttpAdapter.setHttpService(s);
		}
	}

	protected void unsetHttpService(HttpService s) {
		synchronized (lockGatH) {
			ahHttpAdapter.unsetHttpService(s);
		}
	}

	protected void setHacService(IHacService s) {
		synchronized (lockGatH) {
			this.hacService = s;
		}
	}

	protected void unsetHacService(IHacService s) {
		synchronized (lockGatH) {
			if (this.hacService == s)
				this.hacService = null;
		}
	}

	protected void setESPService(ESPService s) {
		synchronized (lockEsp) {
			this.espService = s;
		}
	}

	protected void unsetESPService(ESPService s) {
		synchronized (lockEsp) {
			if (this.espService == s)
				this.espService = null;
		}
	}

	protected void setConfigurator(IConfigurator s) {
		synchronized (lockGatH) {
			this.configurator = s;
		}
	}

	protected void unsetConfigurator(IConfigurator s) {
		synchronized (lockGatH) {
			if (this.configurator == s)
				this.configurator = null;
		}
	}

	// protected void setManagedAppliance(IManagedAppliance s, Map map) {
	// synchronized (lockGatH) {
	// installingAppliances.put(s.getPid(), s);
	// }
	// }
	//
	// protected void unsetManagedAppliance(IManagedAppliance s) {
	// synchronized (lockGatH) {
	// IManagedAppliance appliance = (IManagedAppliance)
	// installingAppliances.get(s.getPid());
	// if (appliance != null) {
	// installingAppliances.remove(s.getPid());
	// appliance = null;
	// }
	// }
	// }

	public void setConnectionAdmin(IConnectionAdminService connectionAdminService) {
		synchronized (lockGatH) {
			this.connectionAdminService = connectionAdminService;
		}
	}

	public synchronized void unsetConnectionAdmin(IConnectionAdminService connectionAdminService) {
		synchronized (lockGatH) {
			if (this.connectionAdminService == connectionAdminService) {
				this.connectionAdminService = null;
			}
		}
	}

	public synchronized void setHapService(IM2MHapService hapService) {
		synchronized (lockPlatform) {
			this.hapService = hapService;
		}
	}

	public synchronized void unsetHapService(IM2MHapService hapService) {
		synchronized (lockPlatform) {
			if (this.hapService == hapService) {
				this.hapService = null;
			}
		}
	}

	public void setM2MDevice(M2MDeviceConfigurator m2mDevice) {
		synchronized (lockPlatform) {
			this.m2mDevice = m2mDevice;
		}
	}

	public void unsetM2MDevice(M2MDeviceConfigurator m2mDevice) {
		synchronized (lockPlatform) {
			if (this.m2mDevice == m2mDevice) {
				this.m2mDevice = null;
			}
		}
	}

	public AttributeValue getAttribute(String peerAppliancePid, String name) throws Exception {
		synchronized (lockGatH) {
			IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(peerAppliancePid);

			if (name.equals("12.Power")) {
				if (!useReportingOnSimpleMetering) {
					double power = this.readPower(peerAppliance);
					if (power == ESPService.INVALID_INSTANTANEOUS_POWER_VALUE) {
						return null;
					}
					return new AttributeValue(power / 1000.0);
				} else {
					Double istantaneousDemand = (Double) istantaneousDemands.get(peerAppliance.getPid());
					if (istantaneousDemand != null) {
						return new AttributeValue(istantaneousDemand);
					}
				}
			} else {
				// ApplianceControlServer applianceControlServer =
				// (ApplianceControlServer)
				// greenathomeEndPoint.getPeerServiceCluster(
				// peerAppliancePid, ApplianceControlServer.class.getName());
				// if (applianceControlServer != null) {
				// if (name.equals("2561.Spin")) {
				// int spin = applianceControlServer.getSpin(maxAgeContext);
				// return new AttributeValue(spin);
				// } else if (name.equals("2561.CycleTarget0")) {
				// int cycleTarget0 =
				// applianceControlServer.getCycleTarget0(maxAgeContext);
				// return new AttributeValue(cycleTarget0);
				// } else if (name.equals("2561.CycleDuration")) {
				// return new AttributeValue(0);
				// } else if (name.equals("2561.TemperatureTarget0")) {
				// int temperatureTarget0 =
				// applianceControlServer.getTemperatureTarget0(maxAgeContext);
				// return new AttributeValue(temperatureTarget0);
				// } else if (name.equals("CycleType")) {
				// return new AttributeValue(0);
				// } else {
				// throw new ApplianceException("Attribute '" + name +
				// "' not found");
				// }
				// } else {
				// throw new
				// ApplianceException("Unable to retrieve ApplianceControlServer cluster");
				// }
			}
		}

		return null;
	}

	public AttributeValue getAttribute(String name) throws Exception {
		AttributeValue value = null;

		if (name.equals("TotalPower")) {
			synchronized (lockEsp) {
				if (espService != null) {
					float totalPower = espService.getTotalInstantaneousPowerFloatValue();
					value = new AttributeValue(new Integer((int) totalPower));
				} else {
					throw new IllegalStateException("ESP Service not bound");
				}
			}
		} else if (name.equals("ProducedPower")) {
			synchronized (lockEsp) {
				if (espService != null) {
					float totalPower = espService.getInstantaneousProducedPowerFloatValue();
					value = new AttributeValue(new Integer((int) totalPower));
				} else {
					throw new IllegalStateException("ESP Service not bound");
				}
			}
		} else if (name.equals("SoldPower")) {
			synchronized (lockEsp) {
				if (espService != null) {
					float totalPower = espService.getInstantaneousSoldPowerFloatValue();
					value = new AttributeValue(new Integer((int) totalPower));
				} else {
					throw new IllegalStateException("ESP Service not bound");
				}
			}
		} else if (name.equals("PowerLimit") || (name.equals("InstantaneousPowerLimit"))) {
			synchronized (lockEsp) {
				if (espService == null) {
					throw new IllegalStateException("ESP service not bound");
				}

				ESPConfigParameters config = this.espService.getCurrentConfiguration();
				if (config == null) {
					config = new ESPConfigParameters();
				}

				float limit = config.getContractualPowerThreshold();
				value = new AttributeValue(new Float(limit));
			}
		} else if (name.equals("PeakProducedPower")) {
			synchronized (lockEsp) {
				if (espService == null) {
					throw new IllegalStateException("ESP service not bound");
				}

				ESPConfigParameters config = this.espService.getCurrentConfiguration();
				if (config == null) {
					config = new ESPConfigParameters();
				}

				float peakProducedPower = config.getPeakProducedPower();
				value = new AttributeValue(new Float(peakProducedPower));
			}
		}
		return value;
	}

	public void setAttribute(String name, Object value) throws Exception {
		if (name.equals("PowerLimit") || (name.equals("InstantaneousPowerLimit"))) {
			synchronized (lockEsp) {
				if (espService == null) {
					throw new IllegalStateException("ESP service not bound");
				}

				float limit;
				if (value instanceof Number) {
					limit = ((Number) value).floatValue();
				} else if (value instanceof String) {
					limit = Float.parseFloat((String) value);
				} else {
					throw new ApplianceException("wrong value passed in setAttribute");
				}

				ESPConfigParameters config = espService.getCurrentConfiguration();
				if (config != null)
					config.setContractualPowerThreshold(limit);
				else
					config = new ESPConfigParameters(limit, ESPConfigParameters.DEFAULT_PEAK_PRODUCED_POWER);
				this.espService.setConfiguration(config);
			}
		} else if (name.equals("PeakProducedPower")) {
			synchronized (lockEsp) {
				if (espService == null) {
					throw new IllegalStateException("ESP service not bound");
				}

				float peakProducedPower;
				if (value instanceof Number) {
					peakProducedPower = ((Number) value).floatValue();
				} else if (value instanceof String) {
					peakProducedPower = Float.parseFloat((String) value);
				} else {
					throw new ApplianceException("wrong value passed in setAttribute");
				}

				ESPConfigParameters config = espService.getCurrentConfiguration();
				if (config != null)
					config.setPeakProducedPower(peakProducedPower);
				else
					config = new ESPConfigParameters(ESPConfigParameters.DEFAULT_CONTRACTUAL_POWER_THRESHOLD, peakProducedPower);
				this.espService.setConfiguration(config);
			}
		}
	}

	public IAppliance[] getDevices() {
		return greenathomeEndPoint.getPeerAppliances();
	}

	public synchronized Vector getInfos() {

		Vector infos = new Vector();

		IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();

		for (int i = 0; i < peerAppliances.length; i++) {

			IAppliance peerAppliance = peerAppliances[i];

			Hashtable props;
			try {
				props = this.getInfo(peerAppliance);
			} catch (ApplianceException e) {
				continue;
			} catch (ServiceClusterException e) {
				continue;
			}

			if (props == null)
				continue;

			infos.add(props);
		}

		return infos;
	}

	public Hashtable getInfo(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {

		int availability = 0;
		int state = 0;
		int status = 0;
		boolean isStateChangable = false;

		String locationPid = null;
		String categoryPid = null;

		Hashtable props = new Hashtable();

		props.put("type", peerAppliance.getDescriptor().getType());
		props.put("pid", peerAppliance.getPid());
		props.put("id", peerAppliance.getPid()); // Pid or id?

		synchronized (lockGatH) {

			OnOffServer onOffServer = null;
			onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName());
			if (onOffServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) onOffServer).getEndPoint().isAvailable() ? 2 : 0;
				boolean onOff = false;

				try {
					onOff = onOffServer.getOnOff(context);
					if (onOff)
						state = On;
					else
						state = Off;

				} catch (Exception e) {
					// availability = 0;
					state = Unknown;
				}
			}

			/*
			 * if (readApplianceStatus) { ApplianceControlServer
			 * applianceControlServer = (ApplianceControlServer)
			 * greenathomeEndPoint.getPeerServiceCluster(
			 * peerAppliance.getPid(), ApplianceControlServer.class.getName());
			 * if (applianceControlServer != null) { isStateChangable = true;
			 * availability = ((IServiceCluster)
			 * applianceControlServer).getEndPoint().isAvailable() ? 2 : 0;
			 * 
			 * int applianceStatus = 0;
			 * 
			 * try { applianceStatus =
			 * applianceControlServer.getApplianceStatus(null);
			 * 
			 * if (logEnabled) log.debug("applianceStatus is " +
			 * applianceStatus);
			 * 
			 * if (applianceStatus < 0x03) { state = Off; } else { state = On; }
			 * } catch (Exception e) { state = Unknown; // availability = 0; } }
			 * }
			 */
			ThermostatServer thermostatServer = (ThermostatServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ThermostatServer.class.getName());

			if (thermostatServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			/* Added by Marco */
			DoorLockServer doorLockServer = (DoorLockServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), DoorLockServer.class.getName());
			if (doorLockServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) doorLockServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());
			if (windowCoveringServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) windowCoveringServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			/*
			 * WindowCoveringClient windowCoveringClient =
			 * (WindowCoveringClient)
			 * greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
			 * WindowCoveringClient.class.getName()); if (windowCoveringClient
			 * != null) { isStateChangable = true; availability =
			 * ((IServiceCluster)
			 * windowCoveringClient).getEndPoint().isAvailable() ? 2 : 0; }
			 */
			/* End by Marco */

			RelativeHumidityMeasurementServer humidityServer = (RelativeHumidityMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), RelativeHumidityMeasurementServer.class.getName());
			if (humidityServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) humidityServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			// handle Smart Info and Smart Plugs
			SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMeteringServer.class.getName());

			if (onOffServer == null && simpleMeteringServer != null) {
				availability = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			ConfigServer configServer = (ConfigServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ConfigServer.class.getName());

			if (configServer != null) {
				locationPid = configServer.getLocationPid(null);
				if (locationPid != null) {
					ILocation location = this.getLocation(locationPid);
					if (location != null)
						props.put("location", location);
				}

				categoryPid = configServer.getCategoryPid(null);
				if (categoryPid != null) {
					ICategory category = this.getCategory(categoryPid);
					if (category != null)
						props.put("category", category);
				}

				try {
					props.put("name", configServer.getName(null));
				} catch (Exception e) {
					props.put("name", peerAppliance.getPid());
				}

				try {
					props.put("icon", configServer.getIconName(null));
				} catch (Exception e) {
					props.put("icon", "lampadina.png");
				}
			} else
				return null;

			props.put("device_state_avail", new Boolean(isStateChangable));
			props.put("device_state", new Integer(state));
			props.put("availability", new Integer(availability));
			props.put("device_status", new Integer(status));

			String value = "";

			if (!peerAppliance.getDescriptor().getType().equals(SMARTINFO_APP_TYPE)) {
				if (thermostatServer != null) {
					float localTemperature = (float) (thermostatServer.getLocalTemperature(context) / 100.0);
					value = localTemperature + "^C";
					if (humidityServer != null) {
						float humididy = (float) (humidityServer.getMeasuredValue(context) / 100.0);
						value += " " + humididy + "%";

					}
				} else {
					if (!useReportingOnSimpleMetering) {
						try {
							double power = this.readPower(peerAppliance);
							value = OutputPower.format(power / 1000.0);
						} catch (Exception e) {
							value = "na";
						}
					} else {
						Double istantaneousDemand = (Double) istantaneousDemands.get(peerAppliance.getPid());
						if (istantaneousDemand != null) {
							value = OutputPower.format(istantaneousDemand.doubleValue() / 1000.0);
						}
					}
				}
			} else if (windowCoveringServer != null) {
				Integer currentLiftPosition = windowCoveringServer.getCurrentPositionLift(context);
				value = currentLiftPosition.toString();
			} else if (doorLockServer != null) {
				Short currentLockState = (Short) doorLockServer.getLockState(context);
				value = currentLockState.toString();
			}

			props.put("device_value", value);

			// if (location != null) {
			// props.put("location", location);
			// }
			//
			// if (category != null) {
			// props.put("category", category);
			// }

		}
		return props;
	}

	public ICategory[] getCategories(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			ICategory[] categories = this.getCategories();
			return categories;
		}
	}

	public ICategory[] getCategories(String appliancePid) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			ICategory[] categories = this.getCategories();
			return categories;
		}
	}

	public ICategory[] getCategories() throws ApplianceValidationException {
		synchronized (lockGatH) {
			ICategory[] categories = super.getCategories();
			if (categories == null) {
				return new ICategory[0];
			}
			return categories;
		}
	}

	public ILocation[] getLocations() throws ApplianceValidationException {
		synchronized (lockGatH) {
			ILocation[] locations = super.getLocations();
			if (locations == null) {
				return new ILocation[0];
			}
			return locations;
		}
	}

	public boolean setCategory(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			return false;
		}
	}

	public void setCategory(IAppliance peerAppliance, String category) throws ApplianceException, ServiceClusterException {

		synchronized (lockGatH) {

			ConfigServer configServer;
			configServer = (ConfigServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ConfigServer.class.getName());

			if (configServer != null) {
				String categoryPid = getCategoryPid(category);
				if (categoryPid == null) {
					throw new ApplianceException("Category pid not found");
				}

				// FIXME:
				// configServer.setCategoryPid(categoryPid, null);
			}
		}
	}

	public void setLocation(IAppliance peerAppliance, String locationName) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			ConfigServer configServer;
			configServer = (ConfigServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ConfigServer.class.getName());

			if (configServer != null) {
				String locationPid = getLocationPid(locationName);
				if (locationPid == null) {
					throw new ApplianceException("location pid not found");
				}
				// FIXME: write the new value on the HAC SErvice
				// configServer.setLocationPid(locationPid, null);
			}
		}
	}

	protected String getCategoryPid(String name) throws ApplianceException, ServiceClusterException {
		ICategory[] categories = this.getCategories();
		for (int i = 0; i < categories.length; i++) {
			ICategory category = categories[i];
			if (category.getName().equals(name)) {
				return category.getPid();
			}
		}
		return null;
	}

	protected String getLocationPid(String name) throws ApplianceException, ServiceClusterException {
		ILocation[] locations = this.getLocations();
		for (int i = 0; i < locations.length; i++) {
			ILocation location = locations[i];
			if (location.getName().equals(name)) {
				return location.getPid();
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # removeDevice(org.energy_home.jemma.ah.hac.IAppliance)
	 */
	public synchronized void removeDevice(String appliancePid) throws ApplianceException {
		synchronized (lockGatH) {
			if (this.hacService != null) {
				// !!! Energy@home webui compatibility
				String[] ids = getDeviceIds(appliancePid);
				// appliancePid = ids[0];
				IAppliance appliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
				this.hacService.removeAppliance(appliancePid);
			} else
				throw new ApplianceException("Unable to remove the appliance.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # getInfosNew()
	 */
	public synchronized Dictionary getInfosNew() {
		Vector activeDevices = new Vector();
		Vector notActiveDevices = new Vector();
		Vector disconnectedDevices = new Vector();

		IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();

		for (int i = 0; i < peerAppliances.length; i++) {

			IAppliance ac = peerAppliances[i];

			int availability = 0;
			int state = 0;
			int status = 0;
			boolean isStateChangable = true;

			String pid = ac.getPid();

			String locationName = null;
			ICategory category = null;

			Hashtable props = new Hashtable();

			// props.put("name", ac.getType());
			// props.put("icon", ac.getIcon());

			String classname = this.getClass().getName();

			int lastdot = classname.lastIndexOf(".");
			if (lastdot != -1) {
				classname = classname.substring(lastdot + 1);
			}

			props.put("type", classname);

			props.put("id", pid);
			props.put("device_state_avail", new Boolean(isStateChangable));
			props.put("device_state", new Integer(state));
			props.put("availability", new Integer(availability));
			props.put("device_status", new Integer(status));

			if (locationName != null) {

			}

			if (category != null) {
				props.put("category", category);
			}

			// list only the devices that provides the power attribute

			String attribute = null; // PATCH
			if (attribute != null) {

				// double power = attribute.floatValue();
				double power = 0;
				if (availability == 2) {

					props.put("value", new Double(power));

					if (state == 1) {
						// Connected -> On

						// add into active list
						activeDevices.add(props);

					} else if (state == 0) {
						// Connected -> Off
						activeDevices.add(props);
					} else {
						// Connected -> NOT in On or in Off state
						notActiveDevices.add(props);
					}
				} else {
					// device disconnected
					if (isStateChangable) {
						disconnectedDevices.add(props);
					}
				}

				// sort the tables according the value field
			}

			Dictionary resultTable = new Hashtable();
			resultTable.put("activeDevices", activeDevices);
			resultTable.put("notActiveDevices", notActiveDevices);
			resultTable.put("disconnectedDevices", disconnectedDevices);

			return resultTable;
		}
		return null;

	}

	static final int Off = 0;
	static final int On = 1;
	static final int Unknown = 4;

	// DoorLock State
	static final int DoorLockCloseUnLock = 0;
	static final int DoorLockClose = 1;
	static final int DoorLockOpen = 2;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # setDeviceState(org.energy_home.jemma.ah.hac.IAppliance, int)
	 */
	public boolean setDeviceState(IAppliance peerAppliance, int state) {

		OnOffServer onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName());
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());
		DoorLockServer doorLockServer = (DoorLockServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), DoorLockServer.class.getName());

		if (onOffServer != null) {
			if (state == On) {
				try {
					// 4Noks smart plugs require to disable default response to
					// work!!!
					onOffServer.execOn(context);
				} catch (Exception e) {
					if (logEnabled)
						LOG.debug("setDeviceState returned exception '" + e.getMessage(), e);
					return false;
				}
			} else if (state == Off) {
				try {
					// 4Noks smart plugs require to disable default response to
					// work!!!
					onOffServer.execOff(context);
				} catch (Exception e) {
					LOG.debug("setDeviceState returned exception '" + e.getMessage(), e);
					return false;
				}
			}
		} else if (applianceControlServer != null) {
			try {
				short commandId;

				if (state == On) {
					commandId = 0x01;
				} else if (state == Off) {
					commandId = 0x02;
				} else
					return false;

				applianceControlServer.execCommandExecution(commandId, null);
			} catch (Exception e) {
				LOG.error("execCommandExecution exception " + e.getMessage(), e);
				return false;
			}
		} else if (doorLockServer != null) {
			try {
				if (state == DoorLockCloseUnLock) {
					doorLockServer.execUnlockDoor("0000", context);
				} else if (state == DoorLockClose) {
					doorLockServer.execLockDoor("0001", context);
				} else if (state == DoorLockOpen) {
					doorLockServer.execUnlockDoor("0002", context);
				} else
					return false;
			} catch (Exception e) {
				LOG.error("execCommandExecution exception " + e.getMessage(), e);
				return false;
			}
		} else
			return false;

		return true;
	}

	// WindowCovering State
	static final int CurrentPositionLiftPercentage = 0;
	static final int InfoInstalledOpenLimit = 1;
	static final int InfoInstalledClosedLimit = 2;
	static final int InfoInstalledOpenLimitTilt = 3;
	static final int InfoInstalledClosedLimitTilt = 4;
	static final int Stopped = 5;
	static final int UpOpen = 6;
	static final int DownClose = 7;
	static final int OpenPercentage = 8;
	static final int UnknownWC = 9;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # setDeviceState(org.energy_home.jemma.ah.hac.IAppliance, int)
	 */
	public boolean setDeviceState(IAppliance peerAppliance, int state, short value) {
		synchronized (lockGatH) {

			WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());

			if (windowCoveringServer != null) {
				LOG.info("================> setDeviceState WindowCovering Server Istanziato");
				LOG.info("================> setDeviceState WindowCovering Server " + peerAppliance.getPid() + " state = " + state);
				try {
					if (state == Stopped) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.execStop");
						windowCoveringServer.execStop(context);
					} else if (state == UpOpen) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.execUpOpen");
						windowCoveringServer.execUpOpen(context);
					} else if (state == DownClose) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.execDownClose");
						windowCoveringServer.execDownClose(context);
					} else if (state == OpenPercentage) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.execGoToLiftPercentage");
						windowCoveringServer.execGoToLiftPercentage(value, context);
					} else
						return false;
				} catch (Exception e) {
					LOG.error("execCommandExecution exception " + e.getMessage(), e);
					return false;
				}
			} else
				return false;

			return true;
		}
	}

	public int getDeviceState(IAppliance peerAppliance, int state) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			LOG.info("================> getDeviceState WindowCovering Server " + peerAppliance.getPid() + " state = " + state);

			WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());

			if (windowCoveringServer != null) {
				try {
					if (state == CurrentPositionLiftPercentage) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.getCurrentPositionLiftPercentage");
						return windowCoveringServer.getCurrentPositionLiftPercentage(context);
					} else if (state == InfoInstalledOpenLimit) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.getInstalledOpenLimit");
						return windowCoveringServer.getInstalledOpenLimit(context);
					} else if (state == InfoInstalledClosedLimit) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.getInstalledClosedLimit");
						return windowCoveringServer.getInstalledClosedLimit(context);
					} else if (state == InfoInstalledOpenLimitTilt) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.getInstalledOpenLimitTilt");
						return windowCoveringServer.getInstalledOpenLimitTilt(context);
					} else if (state == InfoInstalledClosedLimit) {
						LOG.info("================> getDeviceState WindowCovering Server windowCoveringServer.getInstalledClosedLimitTilt");
						return windowCoveringServer.getInstalledClosedLimitTilt(context);
					} else
						return UnknownWC;
				} catch (Exception e) {
					LOG.error("execCommandExecution exception " + e.getMessage(), e);
					return UnknownWC;
				}
			} else
				return UnknownWC;
		}
	}

	public int getDeviceState(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			OnOffServer onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName());
			ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

			if (onOffServer != null) {
				boolean onOff = onOffServer.getOnOff(context);
				if (onOff)
					return On;
				else
					return Off;

			} else if (applianceControlServer != null) {

			} else
				return Unknown;

			return Unknown;
		}
	}

	public boolean reset(int value) throws Exception {
		synchronized (lockGatH) {
			this.hacService.clean();
			if (connectionAdminService != null)
				this.connectionAdminService.deleteAllRules();
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # getObjectByPid(java.lang.String)
	 */
	public Object getObjectByPid(String pid) {
		if (pid.equals("HacApplication.HomeMeter")) {
			return this;
		} else if (pid.equals("HacApplication.OverloadControl")) {
			return this;
		} else if (pid.equals("homeauto")) {
			return this;
		} else {
			Object targetObject = greenathomeEndPoint.getPeerAppliance(pid);
			return targetObject;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # notifyPeerApplianceConnected(java.lang.String)
	 */
	public void notifyPeerApplianceConnected(String peerAppliancePid) {
		initEndPoint(peerAppliancePid);
	}

	public void notifyPeerApplianceDisconnected(String peerAppliancePid) {
	}

	public void notifyPeerApplianceUpdated(String peerAppliancePid) {
		initEndPoint(peerAppliancePid);
	}

	private void initEndPoint(String peerAppliancePid) {
		synchronized (lockGatH) {
			IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(peerAppliancePid);
			IEndPoint[] peerEndPoints = null;

			if (peerAppliance != null && !peerAppliance.getDescriptor().getType().equals(SMARTINFO_APP_TYPE)) {
				// this is not the SmartInfo

				peerEndPoints = this.greenathomeEndPoint.getPeerEndPoints(peerAppliancePid);
				if (peerAppliance.isAvailable() && peerEndPoints != null && peerEndPoints.length >= 1) {
					try {
						SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, SimpleMeteringServer.class.getName());

						ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, ApplianceControlServer.class.getName());

						OccupancySensingServer occupancySensingServer = (OccupancySensingServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, OccupancySensingServer.class.getName());

						if (simpleMeteringServer != null) {
							boolean avail = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable();
							if (avail) {
								Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
								if (demandFormatting == null) {
									short df = simpleMeteringServer.getDemandFormatting(context);
									demandFormatting = new Short(df);
									if (logEnabled) {
										LOG.debug("read demand formatting for appliance " + peerAppliance.getPid() + " with value " + df);
									}
									demandFormattings.put(peerAppliance.getPid(), new Short(df));
								}

								if (useReportingOnSimpleMetering) {
									((IServiceCluster) simpleMeteringServer).setAttributeSubscription(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
								}
							}
						} else {
							LOG.debug("SimpleMetering Server Cluster missing on appliance " + peerAppliancePid);
						}

						SimpleMetering4NoksServer simpleMetering4NoksServer = (SimpleMetering4NoksServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, SimpleMetering4NoksServer.class.getName());

						if (simpleMetering4NoksServer != null) {
							if (useReportingOnSimpleMetering) {
								((IServiceCluster) simpleMetering4NoksServer).setAttributeSubscription(SimpleMetering4NoksServer.ATTR_Power_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);

								((IServiceCluster) simpleMetering4NoksServer).setAttributeSubscription(SimpleMetering4NoksServer.ATTR_Energy_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
							}

						} else {
							LOG.debug("SimpleMetering Server Cluster missing on appliance " + peerAppliancePid);
						}

						if (occupancySensingServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) occupancySensingServer).setAttributeSubscription(OccupancySensingServer.ATTR_Occupancy_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
							}
						} else {
							LOG.debug("OccupancySensing Server Cluster missing on appliance " + peerAppliancePid);
						}

						TemperatureMeasurementServer temperatureMeasurementServer = (TemperatureMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, TemperatureMeasurementServer.class.getName());

						if (temperatureMeasurementServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) temperatureMeasurementServer).setAttributeSubscription(TemperatureMeasurementServer.ATTR_MeasuredValue_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
							}
						} else {
							LOG.debug("Temperature Measurement Server Cluster missing on appliance " + peerAppliancePid);
						}

						IlluminanceMeasurementServer illuminanceMeasurementServer = (IlluminanceMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliancePid, IlluminanceMeasurementServer.class.getName());
						if (illuminanceMeasurementServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) illuminanceMeasurementServer).setAttributeSubscription(IlluminanceMeasurementServer.ATTR_MeasuredValue_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
							}
						} else {
							LOG.debug("Illuminance Measurement Server Cluster missing on appliance " + peerAppliancePid);
						}

						/* Added by Marco */
						DoorLockServer doorLockServer = (DoorLockServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), DoorLockServer.class.getName());
						if (doorLockServer != null) {
							((IServiceCluster) doorLockServer).setAttributeSubscription(DoorLockServer.ATTR_LockState_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);

						} else {
							LOG.debug("Door Lock Server Cluster missing on appliance " + peerAppliancePid);
						}

						WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());
						if (windowCoveringServer != null) {
							((IServiceCluster) windowCoveringServer).setAttributeSubscription(WindowCoveringServer.ATTR_CurrentPositionLiftPercentage_NAME, ISubscriptionParameters.DEFAULT_SUBSCRIPTION_PARAMETERS, null);
						} else {
							LOG.debug("Window Covering Server Cluster missing on appliance " + peerAppliancePid);
						}

						/* End Marco */

					} catch (ServiceClusterException e) {
						LOG.error("Exception on initEndPoint", e);
					} catch (ApplianceException e) {
						LOG.error("Exception on initEndPoint", e);
					}
				} else {
					LOG.debug("appliance " + peerAppliancePid + " in a not valid state");
				}
			}
		}
	}

	private double decodeFormatting(long value, short formatting) {
		double v = (double) value;
		int decimals = formatting & 0x07;
		v = v / Math.pow(10, decimals) * 1000;
		return v;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService
	 * # readPower(org.energy_home.jemma.ah.hac.IAppliance)
	 */
	public double readPower(IAppliance peerAppliance) throws Exception {
		synchronized (lockEsp) {
			if (espService != null) {
				return espService.getInstantaneousPowerFloatValue(peerAppliance.getPid());
			}
		}

		if (!peerAppliance.getDescriptor().getType().equals(SMARTINFO_APP_TYPE)) {
			// this is not the SmartInfo
			SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMeteringServer.class.getName());

			if (simpleMeteringServer != null) {
				boolean avail = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable();
				if (avail) {
					try {
						Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
						if (demandFormatting == null) {

							short df = simpleMeteringServer.getDemandFormatting(context);
							demandFormatting = new Short(df);
							if (logEnabled) {
								LOG.debug("read demand formatting with value " + df);
							}
							demandFormattings.put(peerAppliance.getPid(), new Short(df));
						}

						int istantaneousDemand = simpleMeteringServer.getIstantaneousDemand(context);
						double power = decodeFormatting(istantaneousDemand, demandFormatting.shortValue());
						return power;
					} catch (Exception e) {
						LOG.error("Error while calling while trying to invoke getIstantaneousDemand command", e);
					}
				}
			}

			SimpleMetering4NoksServer simpleMetering4NoksServer = (SimpleMetering4NoksServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMetering4NoksServer.class.getName());

			if (simpleMetering4NoksServer != null) {
				try {
					long power = simpleMetering4NoksServer.getPower(context);
					return power;
				} catch (ApplianceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceClusterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return 0;
	}

	public void notifyAttributeValue(String clusterName, String attributeName, IAttributeValue attributeValue, IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		synchronized (lockGatH) {
			if (logEnabled) {
				LOG.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());
			}

			if (attributeName.equals(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();
				Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
				if (demandFormatting == null) {
					LOG.error("demand formatting not available for appliance " + peerAppliance.getPid());
					return;
				}
				double power = decodeFormatting(((Number) attributeValue.getValue()).longValue(), demandFormatting.shortValue());
				this.istantaneousDemands.put(peerAppliance.getPid(), new Double(power));
				LOG.debug("calculated on appliance " + peerAppliance.getPid() + " power " + power);
			} else if (attributeName.equals(SimpleMetering4NoksServer.ATTR_Power_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();
				double power = ((Number) attributeValue.getValue()).longValue();
				this.istantaneousDemands.put(peerAppliance.getPid(), new Double(power));
			} else if (attributeName.equals(OccupancySensingServer.ATTR_Occupancy_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();

				if (logEnabled) {
					LOG.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());
				}

				this.occupancySensing.put(peerAppliance.getPid(), attributeValue);
			} else if (attributeName.equals(TemperatureMeasurementServer.ATTR_MeasuredValue_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();
				if (logEnabled) {
					LOG.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());
				}

				this.measuredValues.put(peerAppliance.getPid(), attributeValue);
			}
		}
	}

	public void notifyCommandResponse(String clusterName, String commandName, Object response, IEndPointRequestContext endPointRequestContext) throws ApplianceException {
		// TODO Auto-generated method stub
	}

	public void notifyReadResponse(String clusterName, String attributeName, IAttributeValue attributeValue, IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		// TODO Auto-generated method stub
	}

	public void notifyWriteResponse(String clusterName, String attributeName, IAttributeValue attributeValue, IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		// TODO Auto-generated method stub

	}

	public void stopInquiry() throws Exception {

		if (fakeMode)
			return;

		synchronized (lockZbNwkMngr) {
			if (hacService != null)
				this.hacService.closeNetwork("ZigBee");
			else
				throw new IllegalStateException("hap service not bound");
		}
	}

	int fakeCounter = 0;

	protected Dictionary getFakeAppliance() {
		Dictionary device = new Hashtable();
		device.put(IAppliance.APPLIANCE_TYPE_PROPERTY, "com.indesit.ah.app.whitegood");
		device.put(IAppliance.APPLIANCE_NAME_PROPERTY, "Pippo");
		device.put(IAppliance.APPLIANCE_ICON_PROPERTY, "lavatrice.png");
		device.put(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, "1");
		device.put(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, "1");
		device.put(IAppliance.APPLIANCE_PID, "ah.app.Fake." + Integer.toString(fakeCounter++));

		device.put("zigbee.device.profile.id", new Integer(49724));
		device.put("zigbee.device.device.id", new Integer(2560));
		return device;
	}

	public ArrayList getInquiredDevices() {
		synchronized (lockGatH) {
			String[] appliancePids = null;
			appliancePids = this.hacService.getInquiredAppliances();

			ArrayList inquredDevices = new ArrayList();

			if (fakeMode) {
				inquredDevices.add(this.getFakeAppliance());
			} else {
				for (int i = 0; i < appliancePids.length; i++) {
					try {
						Dictionary c = this.hacService.getManagedConfiguration(appliancePids[i]);
						// those information that can cause marshalling problems
						// in JSON RPC.
						Hashtable config = new Hashtable();
						Enumeration keys = c.keys();
						while (keys.hasMoreElements()) {
							Object key = keys.nextElement();
							Object value = c.get(key);
							if (key.equals(IAppliance.APPLIANCE_TYPE_PROPERTY)) {
								// !!! Energy@home webui compatibility
								String[] epsTypes = (String[]) config.get(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY);
								value = encodeGenericApplianceType((String) value, epsTypes[1]);
								config.put(IAppliance.APPLIANCE_TYPE_PROPERTY, value);
							} else {
								config.put(key, value);
							}
						}

						inquredDevices.add(config);
					} catch (Exception e) {
						LOG.error("Unable to get Inquired Appliance " + appliancePids[i], e);
					}
				}
			}
			return inquredDevices;
		}
	}

	public void startInquiry(short duration) throws Exception {

		if (fakeMode)
			return;

		synchronized (lockZbNwkMngr) {
			if (hacService != null)
				this.hacService.openNetwork("ZigBee", duration);
			else
				throw new IllegalStateException("hap service not bound");
		}
	}

	public Float getForecast(String appliancePid, String attributeName, long timestamp, int resolution) throws Exception {

		synchronized (lockEsp) {
			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}
			if (attributeName.equals("ah.eh.esp.Energy")) {
				return espService.getEnergyConsumptionForecast(appliancePid, resolution);
			} else if (attributeName.equals("ah.eh.esp.EnergyCost")) {
				return espService.getEnergyCostForecast(appliancePid, resolution);
			}
		}
		throw new ApplianceException("unknown attribute");
	}

	public List getWeekDayAverage(String appliancePid, String attributeName, int weekday) throws Exception {
		synchronized (lockEsp) {
			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}
			if (attributeName.equals("ah.eh.esp.Energy")) {

				return espService.getWeekDayEnergyConsumpionAverage(appliancePid, weekday);
			} else if (attributeName.equals("ah.eh.esp.EnergyCost")) {
				return espService.getWeekDayEnergyCostAverage(appliancePid, weekday);
			}
		}

		throw new ApplianceException("unknown attribute");
	}

	public void installAppliance(Dictionary props) throws ApplianceException {

		// TODO probabilmente e' necessario separeare la installAppliance in
		// set Properties dell'appliance (con accesso ai cluster)

		synchronized (lockGatH) {
			String appliancePid = (String) props.get(IAppliance.APPLIANCE_PID);
			if (appliancePid == null)
				throw new ApplianceException("appliancePid not set");

			try {
				// !!! Energy@home webui compatibility
				Dictionary c = this.hacService.getManagedConfiguration(appliancePid);
				props.put(IAppliance.APPLIANCE_TYPE_PROPERTY, c.get(IAppliance.APPLIANCE_TYPE_PROPERTY));
				this.hacService.installAppliance(appliancePid, props);
			} catch (HacException e) {
				LOG.error("Hackexception on installAppliance", e);
				throw new ApplianceException(e.getMessage());
			}
		}
	}

	public void enableAppliance(String appliancePid) throws ApplianceException {
		synchronized (lockGatH) {
			if (appliancePid == null)
				throw new ApplianceException("appliancePid not set");

			try {
				this.hacService.enableAppliance(appliancePid);
			} catch (HacException e) {
				LOG.error("enableAppliance Exception", e);
				throw new ApplianceException(e.getMessage());
			}
		}
	}

	public List getAttributeData(String appliancePid, String attributeName, long startTime, long endTime, int resolution, boolean fitResolution, int processType) throws Exception {
		synchronized (lockEsp) {
			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}
			if (attributeName.equals("ah.eh.esp.Energy")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported for this attribute");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}
				return espService.getEnergyConsumption(appliancePid, startTime, endTime, resolution);
			} else if (attributeName.equals("ah.eh.esp.EnergyCost")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}

				return espService.getEnergyCost(appliancePid, startTime, endTime, resolution);
			} else if (attributeName.equals("ah.eh.esp.ProducedEnergy")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}

				return espService.getProducedEnergy(startTime, endTime, resolution);
			} else if (attributeName.equals("ah.eh.esp.SoldEnergy")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}

				return espService.getSoldEnergy(startTime, endTime, resolution);
			}

			throw new ApplianceException("unknown attribute");
		}
	}

	public Map getAttributeData(String attributeName, long startTime, long endTime, int resolution, boolean fitResolution, int processType) throws Exception {

		synchronized (lockEsp) {

			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}

			if (attributeName.equals("ah.eh.esp.Energy")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported for this attribute");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}
				return espService.getEnergyConsumption(startTime, endTime, resolution);
			} else if (attributeName.equals("ah.eh.esp.EnergyCost")) {
				if (!fitResolution) {
					throw new ApplianceException("only fitResolution=true is supported");
				}
				if (processType != DELTA) {
					throw new ApplianceException("only processType=DELTA is supported for this attribute");
				}
				return espService.getEnergyCost(startTime, endTime, resolution);
			}
		}

		throw new ApplianceException("unknown attribute");
	}

	public void initialProvisioning() {
	}

	public void loadConfiguration(String filename) throws Exception {
		synchronized (lockGatH) {
			if (this.configurator != null) {
				this.configurator.loadConfiguration(filename);
			} else
				throw new IllegalStateException("configurator not present");
		}
	}

	public Hashtable getApplianceConfiguration(String appliancePid) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			IAppliance appliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
			return this.getApplianceConfiguration(appliance, 1);
		}
	}

	private int count = 0;

	private String encodeGenericApplianceType(String appType, String endPointType) {
		String result = appType;
		if (appType.equals("org.energy_home.jemma.ah.zigbee.generic")) {
			if (endPointType.equals(IEndPointTypes.ZIGBEE_METERING_DEVICE)) {
				result = "org.energy_home.jemma.ah.zigbee.metering";
			} else if (endPointType.equals(IEndPointTypes.ZIGBEE_WHITE_GOODS)) {
				result = "com.indesit.ah.app.whitegood";
			} else {
				result = appType + APPLIANCE_ID_SEPARATOR + endPointType;
			}
		}
		return result;
	}

	public Hashtable getApplianceConfiguration(IAppliance peerAppliance, int endPointId) throws ApplianceException, ServiceClusterException {
		int availability = 0;
		int state = 0;
		boolean isStateChangable = false;
		String locationPid = null;

		String categoryPid = null;

		Hashtable props = new Hashtable();

		AttributeValueExtended attributeValue = null;

		String appliancePid = getApplianceId(peerAppliance.getPid(), endPointId);

		String appType = peerAppliance.getDescriptor().getType();
		String endPointType = peerAppliance.getEndPoint(endPointId).getType();
		// !!! Energy@home webui compatibility
		if (endPointType == IEndPointTypes.ZIGBEE_ON_OFF_SWITCH_DEVICE)
			return null;

		appType = encodeGenericApplianceType(appType, endPointType);

		props.put(IAppliance.APPLIANCE_TYPE_PROPERTY, appType);
		props.put(IAppliance.APPLIANCE_PID, appliancePid);

		OnOffServer onOffServer = null;
		onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName(), endPointId);
		if (onOffServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) onOffServer).getEndPoint().isAvailable() ? 2 : 0;
			boolean onOff = false;

			if (availability == 2) {
				Boolean onOffValue = (Boolean) this.onOffValues.get(peerAppliance.getPid());
				if (onOffValue == null) {
					try {
						onOff = onOffServer.getOnOff(context);
						this.onOffValues.put(peerAppliance.getPid(), new Boolean(onOff));

					} catch (Exception e) {

						state = Unknown;
					}
				} else {
					onOff = onOffValue.booleanValue();
				}

				if (onOff)
					state = On;
				else
					state = Off;
			} else {
				this.onOffValues.remove(peerAppliance.getPid());
				state = Unknown;
			}
		}

		IASZoneServer iasZoneServer = (IASZoneServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), IASZoneServer.class.getName(), endPointId);

		if (iasZoneServer != null) {
			isStateChangable = false;
			availability = ((IServiceCluster) iasZoneServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			Integer zoneStatus = (Integer) this.zoneStatusTable.get(peerAppliance.getPid());
			int iasZoneType = 0;

			if (zoneStatus != null) {

				Integer iasZoneTypeValue = (Integer) this.iasZoneTypeValues.get(peerAppliance.getPid());
				if (iasZoneTypeValue == null) {
					iasZoneType = iasZoneServer.getZoneType(context);
					this.iasZoneTypeValues.put(peerAppliance.getPid(), new Integer(iasZoneType));
				} else {
					iasZoneType = iasZoneTypeValue.intValue();
				}

				String value = "";
				try {

					switch (iasZoneType) {
					case 0x0015:
						if ((zoneStatus.intValue() & 0x01) > 0)
							value = "Aperto";
						else
							value = "Chiuso";
						break;

					case 0x002a:
						if ((zoneStatus.intValue() & 0x01) > 0)
							value = "Overflow";
						else
							value = "Normal";
						break;
					}

					if ((zoneStatus.intValue() & 0x08) > 0)
						value += "(Low Battery)";
					else
						value += "";

					attributeValue = new AttributeValueExtended("ZoneStatus", new AttributeValue(value));
					if (attributeValue != null)
						props.put("device_value", attributeValue);

				} catch (Exception e) {
					value = "nd";
				}

			} else if (hapService != null) {
				// Code added because IASZone cluster is now used by HAP Proxy
				// and cannot be
				// simultaneously used by Green@Home appliance through
				// connections

				String value = "";
				ContentInstance ci = null;
				try {
					AHContainerAddress containerAddress = hapService.getHagContainerAddress(appliancePid, endPointId, AHContainers.attrId_ah_cluster_iascontact_open);
					ci = hapService.getCachedLatestContentInstance(containerAddress);
				} catch (Exception e) {
					LOG.error("Error while reading isopen container", e);
				}

				if (ci == null) {
					value = "nd";
				} else {
					Object content = ci.getContent();
					if (content != null) {
						if (((Boolean) content).booleanValue())
							value = "Aperto";
						else
							value = "Chiuso";
					}
					attributeValue = new AttributeValueExtended("ZoneStatus", new AttributeValue(value));
					if (attributeValue != null)
						props.put("device_value", attributeValue);
				}
			}
		}

		IlluminanceMeasurementServer illuminanceMeasurementServer = (IlluminanceMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), IlluminanceMeasurementServer.class.getName(), endPointId);

		if (illuminanceMeasurementServer != null) {
			isStateChangable = false;
			availability = ((IServiceCluster) illuminanceMeasurementServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			IAttributeValue measuredValue = (IAttributeValue) this.measuredValues.get(peerAppliance.getPid());
			if (measuredValue != null) {
				int illuminance = ((Integer) measuredValue.getValue()).intValue();
				String value = "";
				if (illuminance == 0x0000) {
					value = "Too Low";
				} else {
					value = new DecimalFormat("#.##").format(illuminance) + "";
				}

				attributeValue = new AttributeValueExtended("Illuminance", new AttributeValue(value));
				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}
		}

		OccupancySensingServer occupancySensingServer = (OccupancySensingServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OccupancySensingServer.class.getName(), endPointId);

		if (occupancySensingServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) occupancySensingServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			IAttributeValue occupancyValue = (IAttributeValue) this.occupancySensing.get(peerAppliance.getPid());
			if (occupancyValue != null) {
				Short value = (Short) occupancyValue.getValue();
				if ((value.intValue() & 0x01) > 0)
					attributeValue = new AttributeValueExtended("Occupancy", new AttributeValue("Occupato"));
				else
					attributeValue = new AttributeValueExtended("Occupancy", new AttributeValue("Libero"));

				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}
		}

		TemperatureMeasurementServer temperatureMeasurementServer = (TemperatureMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), TemperatureMeasurementServer.class.getName(), endPointId);

		if (temperatureMeasurementServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) temperatureMeasurementServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			IAttributeValue measuredValue = (IAttributeValue) this.measuredValues.get(peerAppliance.getPid());
			if (measuredValue != null) {
				double value = ((double) ((Integer) measuredValue.getValue()).intValue()) / 100;
				attributeValue = new AttributeValueExtended("Temperature", new AttributeValue(new DecimalFormat("#.##").format(value) + " ^C"));

				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}
		}

		ThermostatServer thermostatServer = (ThermostatServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ThermostatServer.class.getName(), endPointId);
		if (thermostatServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;

			if (availability == 2) {
				float localTemperature = (float) (thermostatServer.getLocalTemperature(context) / 100.0);
				String value = localTemperature + " &degC";
				attributeValue = new AttributeValueExtended("LocalTemperature", new AttributeValue(value));
				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}

		}

		RelativeHumidityMeasurementServer humidityServer = (RelativeHumidityMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), RelativeHumidityMeasurementServer.class.getName());
		if (humidityServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) humidityServer).getEndPoint().isAvailable() ? 2 : 0;
			if (availability == 2) {
				String value = "";
				float humididy = (float) (humidityServer.getMeasuredValue(context) / 100.0);
				value += " " + humididy + "%";
				attributeValue = new AttributeValueExtended("LocalHumidity", new AttributeValue(value));

				System.out.println("Humidity:" + value);
				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}

		}

		/* Added by Marco */
		DoorLockServer doorLockServer = (DoorLockServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), DoorLockServer.class.getName());

		if (doorLockServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) doorLockServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;
			if (availability == 2) {
				Short currentLock = doorLockServer.getLockState(context);
				attributeValue = new AttributeValueExtended("LockState", new AttributeValue(currentLock));
				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}
		}

		WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());
		if (windowCoveringServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) windowCoveringServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;
			if (availability == 2) {
				Short currentLift = windowCoveringServer.getCurrentPositionLiftPercentage(context);
				attributeValue = new AttributeValueExtended("CurrentPositionLiftPercentage", new AttributeValue(currentLift));
				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}

		}

		SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMeteringServer.class.getName(), endPointId);
		if (simpleMeteringServer != null) {
			if (onOffServer == null) {
				availability = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			if (!useReportingOnSimpleMetering) {
				double power;
				try {
					power = this.readPower(peerAppliance);
					if (power != ESPService.INVALID_INSTANTANEOUS_POWER_VALUE)
						attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(power));

				} catch (Exception e) {
					power = 0;
				}

			} else {
				Double istantaneousDemand = (Double) istantaneousDemands.get(peerAppliance.getPid());

				if (istantaneousDemand != null) {
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(istantaneousDemand.doubleValue()));
				} else if (this.fakeMode) {
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(123.1));
				}
			}
			if (attributeValue != null)
				props.put("device_value", attributeValue);
		}

		ConfigServer configServer = (ConfigServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ConfigServer.class.getName(), endPointId);
		if (configServer != null) {
			locationPid = configServer.getLocationPid(null);

			if (locationPid == null) {
				locationPid = "0";
			}
			props.put(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, locationPid);

			categoryPid = configServer.getCategoryPid(null);
			if (categoryPid == null) {
				categoryPid = "0";
			}
			props.put(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, categoryPid);

			try {
				props.put(IAppliance.APPLIANCE_NAME_PROPERTY, configServer.getName(null));
			} catch (Exception e) {
				props.put(IAppliance.APPLIANCE_NAME_PROPERTY, peerAppliance.getPid());
			}

			try {
				props.put(IAppliance.APPLIANCE_ICON_PROPERTY, configServer.getIconName(null));
			} catch (Exception e) {
				props.put(IAppliance.APPLIANCE_ICON_PROPERTY, "plug.png");
			}
		}

		props.put("device_state_avail", new Boolean(isStateChangable));
		props.put("device_state", new Integer(state));
		props.put("availability", new Integer(availability));

		System.out.println("Props:" + props.toString());
		return props;
	}

	private void loadPropFile() throws IOException {

		// String _path = "noserver.properties";
		String _path = System.getProperty("user.home") + File.separator + "noserver.properties";

		this.props = new Properties();

		InputStream stream = new FileInputStream(_path);
		this.props.load(stream);
		stream.close();
	}

	private void storePropFile() throws IOException {

		// String _path = "noserver.properties";
		String _path = System.getProperty("user.home") + File.separator + "noserver.properties";
		OutputStream out = null;

		File f = new File(_path);
		if (!f.exists())
			f.createNewFile();
		out = new FileOutputStream(f);
		this.props.store(out, null);
		out.flush();
		if (out != null)
			out.close();
	}

	/*
	 * private void storePropFile() throws IOException{
	 * 
	 * String _path = "noserver.properties"; FileOutputStream out = null;
	 * 
	 * // Howto get a bundle context //
	 * http://tux2323.blogspot.it/2011/10/osgi-how
	 * -to-get-bundle-context-in-java.html BundleContext bc =
	 * BundleReference.class
	 * .cast(GreenathomeAppliance.class.getClassLoader()).getBundle
	 * ().getBundleContext(); URL _url = bc.getBundle().getResource(_path);
	 * 
	 * System.out.println("---------------------------------> " +
	 * _url.getPath());
	 * 
	 * try {
	 * 
	 * File configFile = new File(_url.getPath()); if (configFile.canWrite()){
	 * //PrintWriter out = new PrintWriter(configFile); out = new
	 * FileOutputStream(_url.getPath()); this.props.store(out, null);
	 * out.flush(); } else { throw new Exception("File don't writable!"); } }
	 * catch(FileNotFoundException fnfe){ fnfe.printStackTrace(); }
	 * catch(IOException ioe){ ioe.printStackTrace(); } catch (Exception e) {
	 * e.printStackTrace(); } finally { if (out != null) try { out.close(); }
	 * catch (IOException e) { e.printStackTrace(); }
	 * 
	 * } }
	 */

	public ArrayList getAppliancesConfigurations() throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			ArrayList infos = new ArrayList();

			IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();

			for (int i = 0; i < peerAppliances.length; i++) {

				IAppliance peerAppliance = peerAppliances[i];

				Hashtable props = null;
				IEndPoint[] endPoints = peerAppliance.getEndPoints();

				for (int j = 1; j < endPoints.length; j++) {
					try {
						// skip ESP and core appliance
						if (peerAppliance.getDescriptor().getType().equals("ah.app.EnergyServicePortal") || peerAppliance.getPid().equals("ah.app.core"))
							continue;
						props = this.getApplianceConfiguration(peerAppliance, j);
						if (props != null)
							infos.add(props);
					} catch (ApplianceException e) {
						continue;
					} catch (ServiceClusterException e) {
						continue;
					}

				}
			}
			count++;
			return infos;
		}
	}

	public void updateAppliance(Dictionary props) throws ApplianceException {

		LOG.debug("updateAppliance");
		String appliancePid = (String) props.get("appliance.pid");
		if (appliancePid == null)
			throw new ApplianceException("appliance.pid is null");
		synchronized (lockGatH) {
			if (hacService != null) {
				try {
					// !!! Energy@home webui compatibility
					String[] ids = getDeviceIds(appliancePid);
					// appliancePid = ids[0];
					Integer endPointId = new Integer(ids[1]);
					IAppliance appliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
					props.put(IAppliance.APPLIANCE_TYPE_PROPERTY, appliance.getDescriptor().getType());
					if (appliance.getEndPointIds().length > 2) {
						ApplianceConfiguration applianceConfig = new ApplianceConfiguration(appliance.getEndPointIds(), convertToMap(this.hacService.getManagedConfiguration(appliancePid)));
						applianceConfig.updateName(endPointId, (String) props.get(IAppliance.APPLIANCE_NAME_PROPERTY));
						applianceConfig.updateCategoryPid(endPointId, (String) props.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY));
						applianceConfig.updateLocationPid(endPointId, (String) props.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY));
						applianceConfig.updateIconName(endPointId, (String) props.get(IAppliance.APPLIANCE_ICON_PROPERTY));
						this.hacService.updateAppliance(appliancePid, new Hashtable(applianceConfig.getConfigurationMap()));
					} else {
						this.hacService.updateAppliance(appliancePid, props);
					}
				} catch (HacException e) {
					throw new ApplianceException(e.getMessage());
				}
			} else
				throw new IllegalStateException("hap service not bound");

		}
	}

	public long getHapLastUploadTime() {
		synchronized (lockPlatform) {
			if (hapService != null)
				return hapService.getLastSuccessfulBatchRequestTimestamp();
			else
				throw new IllegalStateException("hap service not bound");
		}
	}

	public boolean isHapClientConnected() {
		synchronized (lockPlatform) {
			if (hapService != null)
				return hapService.isConnected();
			else
				return false;
		}
	}

	public void setHapConnectionId(String m2mDeviceId) {
		synchronized (lockPlatform) {
			if (m2mDevice != null) {
				M2MDeviceConfig m2mDeviceConfig;
				try {
					m2mDeviceConfig = this.m2mDevice.getConfiguration();
					m2mDeviceConfig.setDeviceId(m2mDeviceId);
					m2mDevice.setConfiguration(m2mDeviceConfig);
				} catch (M2MServiceException e) {
					throw new IllegalStateException(e.getMessage());
				}
			} else {
				throw new IllegalStateException("m2m device not bound");
			}
		}
	}

	public String getHapConnectionId() {
		synchronized (lockPlatform) {
			if (m2mDevice != null) {
				M2MDeviceConfig m2mDeviceConfig;
				m2mDeviceConfig = this.m2mDevice.getConfiguration();
				String a = m2mDeviceConfig.getDeviceId();
				return a;
			} else {
				throw new IllegalStateException("m2m device not bound");
			}
		}
	}

	public void execSignalStateNotification(short ApplianceStatus, short RemoteEnableFlags, int ApplianceStatus2, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		LOG.debug("appliance Status");

		IEndPoint aa = context.getPeerEndPoint();
		aa.getAppliance();

		boolean isStateChangable = true;
		// int availability = ((IServiceCluster)
		// applianceControlServer).getEndPoint().isAvailable() ? 2 : 0;

		int applianceStatus = 0;
		int state;

		try {
			applianceStatus = ApplianceStatus;

			if (logEnabled) {
				LOG.debug("applianceStatus is " + applianceStatus);
			}

			if (applianceStatus < 0x03) {
				state = Off;
			} else {
				state = On;
			}
		} catch (Exception e) {
			state = Unknown;
			// availability = 0;
		}
	}

	public void sendGuiLog(String msg) throws Exception {
		synchronized (lockEsp) {
			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}

			this.espService.sendGuiLog(msg);
		}
	}

	public long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	public Long getInitialConfigurationTime() {
		synchronized (lockEsp) {
			if (espService == null) {
				throw new IllegalStateException("ESP service not bound");
			}

			return this.espService.getInitialConfigurationTime();
		}
	}

	public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		ZoneEnrollResponse zoneEnrollResponse = new ZoneEnrollResponse();
		return zoneEnrollResponse;
	}

	// HA 1.1
	public void execZoneStatusChangeNotification(int ZoneStatus, short ExtendedStatus, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		LOG.debug(context.getPeerEndPoint().getAppliance().getPid() + ": ZoneStatus=" + ZoneStatus + ", ExtendedStatus=" + ExtendedStatus);
		IAppliance peerAppliance = context.getPeerEndPoint().getAppliance();
		Integer zoneValue = new Integer(ZoneStatus);
		this.zoneStatusTable.put(peerAppliance.getPid(), zoneValue);
	}

	// HA 1.2
	public void execZoneStatusChangeNotification(int ZoneStatus, short ExtendedStatus, short ZoneID, int Delay, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		LOG.debug(context.getPeerEndPoint().getAppliance().getPid() + ": ZoneStatus=" + ZoneStatus + ", ExtendedStatus=" + ExtendedStatus);
		IAppliance peerAppliance = context.getPeerEndPoint().getAppliance();
		Integer zoneValue = new Integer(ZoneStatus);
		this.zoneStatusTable.put(peerAppliance.getPid(), zoneValue);
	}

	/*
	 * METODI INTECS
	 */

	public Hashtable getDeviceClusters(String appliancePid) {

		Hashtable res = new Hashtable();

		res.put("pid", appliancePid);

		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		if (peerAppliance == null)
			return null;

		IEndPoint eps[] = peerAppliance.getEndPoints();

		for (int i = 0; i < eps.length; i++) {
			String cls[] = eps[i].getServiceClusterNames();
			for (int j = 0; j < cls.length; j++) {

				res.put("cluster" + (j + i), cls[j]);

			}
		}
		return res;

	}

	public short levelControlGetCurrentValue(String appliancePid) {
		// synchronized (lockGatH) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		if (peerAppliance == null)
			return -1;
		// }
		LevelControlServer levelControlServer = (LevelControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), LevelControlServer.class.getName());

		if (levelControlServer != null) {
			try {

				short level = levelControlServer.getCurrentLevel(context);
				return level;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("levelControlGetCurrentValue returned exception '" + e.getMessage());
				}

				return -2;
			}
		}

		return -1;
	}

	public boolean levelControlExecMoveToLevelWithOnOff(String appliancePid, short Level, int TransitionTime) {
		// TODO Auto-generated method stub

		// synchronized (lockGatH) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		if (peerAppliance == null)
			return false;
		// }
		LevelControlServer levelControlServer = (LevelControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), LevelControlServer.class.getName());

		if (levelControlServer != null) {
			try {

				levelControlServer.execMoveToLevelWithOnOff(Level, TransitionTime, context);
				return true;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("levelControlExecMoveToLevelWithOnOff returned exception '" + e.getMessage());
				}
				return false;

			}
		}

		return false;

	}

	public boolean levelControlExecMoveToLevel(String appliancePid, short Level, int TransitionTime) {
		// TODO Auto-generated method stub

		// synchronized (lockGatH) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		if (peerAppliance == null)
			return false;
		// }
		LevelControlServer levelControlServer = (LevelControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), LevelControlServer.class.getName());

		if (levelControlServer != null) {
			try {

				levelControlServer.execMoveToLevel(Level, TransitionTime, context);
				return true;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("levelControlExecMoveToLevel returned exception '" + e.getMessage());
				}

				return false;
			}
		}

		return false;

	}

	public boolean levelControlExecStopWithOnOff(String appliancePid) {
		// TODO Auto-generated method stub
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		// }
		LevelControlServer levelControlServer = (LevelControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), LevelControlServer.class.getName());

		if (levelControlServer != null) {
			try {

				levelControlServer.execStopWithOnOff(context);
				return true;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("levelControlExecStopWithOnOff returned exception '" + e.getMessage());
				}

				return false;
			}
		}
		return false;
	}

	public boolean colorControlMoveToColorHSL(String appliancePid, short hue, short saturation, short level, int transitionTime) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		ColorControlServer colorControlServer = (ColorControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ColorControlServer.class.getName());

		if (colorControlServer != null) {
			try {

				colorControlServer.execMoveToHueAndSaturation(hue, saturation, transitionTime, context);
				boolean ret = levelControlExecMoveToLevelWithOnOff(appliancePid, level, transitionTime);

				return ret;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("colorControlMoveToColorHSL returned exception '" + e.getMessage());
				}

				return false;
			}
		}

		return false;
	}

	public boolean colorControlMoveToColorHS(String appliancePid, short hue, short saturation, int transitionTime) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		ColorControlServer colorControlServer = (ColorControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ColorControlServer.class.getName());

		if (colorControlServer != null) {
			try {

				colorControlServer.execMoveToHueAndSaturation(hue, saturation, transitionTime, context);
				// boolean
				// ret=levelControlExecMoveToLevelWithOnOff(appliancePid, level,
				// transitionTime);

				return true;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("colorControlMoveToColorHSL returned exception '" + e.getMessage());
				}

				return false;
			}
		}

		return false;
	}

	public Hashtable colorControlGetColorHSL(String appliancePid) {

		Hashtable props = new Hashtable();

		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		ColorControlServer colorControlServer = (ColorControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ColorControlServer.class.getName());

		if (colorControlServer != null) {
			try {

				short hue = colorControlServer.getCurrentHue(context);
				short sat = colorControlServer.getCurrentSaturation(context);
				short level = levelControlGetCurrentValue(appliancePid);

				props.put("hue", hue);
				props.put("saturation", sat);
				props.put("level", level);
				props.put("pid", appliancePid);
			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("colorControlGetColorHSL returned exception '" + e.getMessage());
				}
				props.put("Errore", e.getMessage());

			}
		}

		return props;

	}

	public Hashtable colorControlGetColorHS(String appliancePid) {

		Hashtable props = new Hashtable();

		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		ColorControlServer colorControlServer = (ColorControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ColorControlServer.class.getName());

		if (colorControlServer != null) {
			try {

				short hue = colorControlServer.getCurrentHue(context);
				short sat = colorControlServer.getCurrentSaturation(context);
				// short level=levelControlGetCurrentValue(appliancePid);

				props.put("hue", hue);
				props.put("saturation", sat);
				// props.put("level", level);
				props.put("pid", appliancePid);
			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("colorControlGetColorHSL returned exception '" + e.getMessage());
				}
				props.put("Errore", e.getMessage());

			}
		}

		return props;

	}

	public boolean colorControlMoveToColorXYL(String appliancePid, int X, int Y, short level, int transitionTime) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		ColorControlServer colorControlServer = (ColorControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ColorControlServer.class.getName());

		if (colorControlServer != null) {
			try {
				colorControlServer.execMoveToColor(X, Y, transitionTime, context);
				boolean ret = levelControlExecMoveToLevelWithOnOff(appliancePid, level, transitionTime);

				return ret;

			} catch (Exception e) {
				if (logEnabled) {
					LOG.debug("colorControlMoveToColorXYL returned exception '" + e.getMessage());
				}

				return false;

			}
		}

		return false;
	}

	public ArrayList getCategoriesWithPid() throws ApplianceValidationException {
		ArrayList vec = new ArrayList();
		ICategory[] categories = getCategories();
		for (int i = 0; i < categories.length; i++) {
			ICategory category = categories[i];
			Hashtable props = new Hashtable();
			props.put("name", category.getName());
			props.put("icon", category.getIconName());
			props.put("pid", category.getPid());
			vec.add(props);
		}

		return vec;
	}

	// added for demo support
	public synchronized Vector getInfosDemo() {

		Vector infos = new Vector();

		IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();

		for (int i = 0; i < peerAppliances.length; i++) {

			IAppliance peerAppliance = peerAppliances[i];

			Hashtable props;
			try {
				props = this.getInfoNew(peerAppliance);
			} catch (ApplianceException e) {
				continue;
			} catch (ServiceClusterException e) {
				continue;
			}

			if (props == null)
				continue;

			infos.add(props);
		}

		return infos;
	}

	public Hashtable getInfoNew(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {

		int availability = 0;
		int state = 0;
		int status = 0;
		boolean isStateChangable = false;

		AttributeValueExtended attributeValue = null;

		String locationPid = null;
		String categoryPid = null;
		String value = "";

		Hashtable props = new Hashtable();

		props.put("type", peerAppliance.getDescriptor().getType());
		props.put("pid", peerAppliance.getPid());
		props.put("id", peerAppliance.getPid()); // Pid or id?

		// synchronized (lockGatH) {

		OnOffServer onOffServer = null;
		onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName());
		if (onOffServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) onOffServer).getEndPoint().isAvailable() ? 2 : 0;
			boolean onOff = false;
			if (availability == 2) {
				try {
					onOff = onOffServer.getOnOff(context);
					if (onOff)
						state = On;
					else
						state = Off;

				} catch (Exception e) {
					value = e.getMessage();
					state = Unknown;
				}
			}
			else {
				state = Unknown;
			}
		}

		

		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null && onOffServer == null) {
			isStateChangable = true;

			availability = ((IServiceCluster) applianceControlServer).getEndPoint().isAvailable() ? 2 : 0;

			int applianceStatus = 0;

			try {
				applianceStatus = applianceControlServer.execSignalState(context).ApplianceStatus;

				if (logEnabled) {
					LOG.debug("applianceStatus is " + applianceStatus);
				}

				if (applianceStatus < 0x03) {
					state = Off;
				} else {
					state = On;
				}
			} catch (Exception e) {
				state = Unknown; // availability = 0; } }
			}

		}
		ThermostatServer thermostatServer = (ThermostatServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ThermostatServer.class.getName());

		if (thermostatServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;
		}
		RelativeHumidityMeasurementServer humidityServer = (RelativeHumidityMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), RelativeHumidityMeasurementServer.class.getName());
		if (humidityServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) humidityServer).getEndPoint().isAvailable() ? 2 : 0;
		}

		// handle Smart Info and Smart Plugs
		SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMeteringServer.class.getName());
		if (onOffServer == null && simpleMeteringServer != null) {
			availability = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable() ? 2 : 0;
		}
		if (!useReportingOnSimpleMetering) {
			double power;
			try {
				power = this.readPower(peerAppliance);
				if (power != ESPService.INVALID_INSTANTANEOUS_POWER_VALUE)
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(power));

			} catch (Exception e) {
				power = 0;
			}

		} else {
			Double istantaneousDemand = (Double) istantaneousDemands.get(peerAppliance.getPid());

			if (istantaneousDemand != null) {
				attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(istantaneousDemand.doubleValue()));
			} else if (this.fakeMode) {
				attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(123.1));
			}
		}

		/* Added by Marco */
		DoorLockServer doorLockServer = (DoorLockServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), DoorLockServer.class.getName());
		if (doorLockServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) doorLockServer).getEndPoint().isAvailable() ? 2 : 0;
		}

		WindowCoveringServer windowCoveringServer = (WindowCoveringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), WindowCoveringServer.class.getName());
		if (windowCoveringServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) windowCoveringServer).getEndPoint().isAvailable() ? 2 : 0;
		}

		/* End by Marco */

		/*
		 * Added by Marco DoorLockServer doorLockServer = (DoorLockServer)
		 * greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
		 * DoorLockServer.class.getName()); if (doorLockServer != null) {
		 * isStateChangable = true; availability = ((IServiceCluster)
		 * doorLockServer).getEndPoint().isAvailable() ? 2 : 0; }
		 * 
		 * WindowCoveringServer windowCoveringServer = (WindowCoveringServer)
		 * greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
		 * WindowCoveringServer.class.getName()); if (windowCoveringServer !=
		 * null) { isStateChangable = true; availability = ((IServiceCluster)
		 * windowCoveringServer).getEndPoint().isAvailable() ? 2 : 0; }
		 * 
		 * End by Marco
		 */

		ConfigServer configServer = (ConfigServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ConfigServer.class.getName());

		if (configServer != null) {
			locationPid = configServer.getLocationPid(null);
			if (locationPid != null) {
				ILocation location = this.getLocation(locationPid);
				if (location != null)
					props.put("location", location);
			}
			props.put("location_pid", locationPid);

			categoryPid = configServer.getCategoryPid(null);
			if (categoryPid != null) {
				ICategory category = this.getCategory(categoryPid);
				if (category != null)
					props.put("category", category);
			}

			try {
				props.put("name", configServer.getName(null));
			} catch (Exception e) {
				props.put("name", peerAppliance.getPid());
			}

			try {
				props.put("icon", configServer.getIconName(null));
			} catch (Exception e) {
				props.put("icon", "lampadina.png");
			}
		} else
			return null;

		props.put("device_state_avail", new Boolean(isStateChangable));
		props.put("device_state", new Integer(state));
		props.put("availability", new Integer(availability));
		props.put("device_status", new Integer(status));

		// if
		// (!peerAppliance.getDescriptor().getType().equals(SMARTINFO_APP_TYPE))
		// {
		// if (thermostatServer != null) {
		// float localTemperature = (float)
		// (thermostatServer.getLocalTemperature(getterContext) / 100.0);
		// value = localTemperature + "^C";
		// if (humidityServer != null) {
		// float humididy = (float)
		// (humidityServer.getMeasuredValue(getterContext) / 100.0);
		// value += " " + humididy + "%";
		// }
		//
		// } else {
		// if (!useReportingOnSimpleMetering) {
		// try {
		// double power = this.readPower(peerAppliance);
		// value = OutputPower.format(power / 1000.0);
		// } catch (Exception e) {
		// value = "na";
		// }
		// } else {
		// Double istantaneousDemand = (Double)
		// istantaneousDemands.get(peerAppliance.getPid());
		// if (istantaneousDemand != null) {
		// value = OutputPower.format(istantaneousDemand.doubleValue() /
		// 1000.0);
		// }
		// }
		// }
		// }

		if (thermostatServer != null) {

			float localTemperature = (float) (thermostatServer.getLocalTemperature(context) / 100.0);
			props.put("temperature", localTemperature);
			if (humidityServer != null) {
				float humidity = (float) (humidityServer.getMeasuredValue(context) / 100.0);
				props.put("humidity", humidity);
			}
		}

		TemperatureMeasurementServer temperatureMeasurementServer = (TemperatureMeasurementServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), TemperatureMeasurementServer.class.getName());

		if (temperatureMeasurementServer != null) {
			int measuredValue = temperatureMeasurementServer.getMeasuredValue(context);
			double localTemperature = ((double) measuredValue) / 100;
			props.put("temperature", localTemperature);
		}

		if (simpleMeteringServer != null) {

			double realInstantaneousValue;

			try {
				realInstantaneousValue = this.readPower(peerAppliance);
				if (realInstantaneousValue != ESPService.INVALID_INSTANTANEOUS_POWER_VALUE)
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(attributeValue));

			} catch (Exception e) {
				realInstantaneousValue = 0.0;
			}

			LOG.debug("Calculated realInstantaneousDemand: " + realInstantaneousValue);
			props.put("device_value_2", realInstantaneousValue);

		}
		/* Added By Marco */
		else if (windowCoveringServer != null) {
			Integer currentLiftPosition = windowCoveringServer.getCurrentPositionLift(context);
			value = currentLiftPosition.toString();
		} else if (doorLockServer != null) {
			Short currentLockState = (Short) doorLockServer.getLockState(context);
			value = currentLockState.toString();
		}
		/* End By Marco */
		else {
			props.put("device_value_2", "na");
		}

		props.put("device_value", value);

		// }
		return props;
	}

	public boolean setDeviceState(String appliancePid, int state) {
		// TODO Auto-generated method stub
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		return setDeviceState(peerAppliance, state);

	}

	public boolean setDeviceState(String appliancePid, int state, short value) {
		// TODO Auto-generated method stub
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		return setDeviceState(peerAppliance, state, value);

	}

	public int getDeviceState(String appliancePid) throws ApplianceException, ServiceClusterException {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		return getDeviceState(peerAppliance);
	}

	public int getDeviceState(String appliancePid, int state) throws ApplianceException, ServiceClusterException {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		return getDeviceState(peerAppliance, state);
	}

	public Hashtable testFunction(String appliancePid, String p1, int p2, int p3) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);
		Hashtable res = new Hashtable();
		res.put("pid", appliancePid);

		res.put("class", ColorControlClient.class.getName());

		SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), SimpleMeteringServer.class.getName());

		try {

			res.put("currentSummation", simpleMeteringServer.getCurrentSummationReceived(context));
			res.put("currentSummationDelivered", simpleMeteringServer.getCurrentSummationDelivered(context));
			res.put("istantaneousDemand", simpleMeteringServer.getIstantaneousDemand(context));
			res.put("DemandFormatting", simpleMeteringServer.getDemandFormatting(context));
			res.put("divisor", simpleMeteringServer.getDivisor(context));
			res.put("type", simpleMeteringServer.getMeteringDeviceType(context));
			res.put("mult", simpleMeteringServer.getMultiplier(context));
			res.put("powerfactor", simpleMeteringServer.getPowerFactor(context));
			res.put("sumFormatting", simpleMeteringServer.getSummationFormatting(context));
			res.put("unit", simpleMeteringServer.getUnitOfMeasure(context));

		} catch (ApplianceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceClusterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return res;

	}

	public int applianceControlGetStartTime(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		int ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getStartTime(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public int applianceControlGetFinishTime(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		int ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getFinishTime(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public int applianceControlGetRemainingTime(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		int ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getRemainingTime(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public Hashtable applianceControlExecSignalState(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		Hashtable res = null;
		SignalStateResponse sg = null;

		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {

			try {

				sg = applianceControlServer.execSignalState(context);
				res = new Hashtable();
				res.put("ApplianceStatus", sg.ApplianceStatus);
				res.put("ApplianceStatus2", sg.ApplianceStatus2);
				res.put("RemoteEnableFlags", sg.RemoteEnableFlags);

			} catch (ApplianceException e) {
				res = null;
			} catch (ServiceClusterException e) {
				res = null;
			}

		}
		return res;
	}

	public short applianceControlGetCycleTarget0(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		short ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getCycleTarget0(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public short applianceControlGetCycleTarget1(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		short ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getCycleTarget1(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public int applianceControlGetTemperatureTarget0(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		int ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {
			try {

				ret = applianceControlServer.getTemperatureTarget0(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}
		}
		return ret;
	}

	public int applianceControlGetTemperatureTarget1(String appliancePid) {
		IAppliance peerAppliance = greenathomeEndPoint.getPeerAppliance(appliancePid);

		int ret = -1;
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (applianceControlServer != null) {

			try {

				ret = applianceControlServer.getTemperatureTarget1(context);

			} catch (ApplianceException e) {

			} catch (ServiceClusterException e) {

			}

		}
		return ret;
	}

	// START DEMO MILANO

	// protected class _MyDevice{
	// //Hashtable che contiene i valori da passare alla webui
	// public String appliancePid="";
	// public Hashtable info=null;
	// public IAppliance peerAppliance=null;
	//
	//
	// }
	//
	// protected Vector _myDevices = new Vector();
	// protected boolean _isUopdatingDeviceList=false;
	// protected boolean _isUpdateSchedulerActive=false;
	// private final ScheduledExecutorService scheduler =
	// Executors.newScheduledThreadPool(1);
	// private ScheduledFuture<?> updateHandle;
	// private final int UPDATE_TIME=20;
	//
	// //Aggiungere metodo deactivate per stoppare lo scheduler!
	//
	// public Vector getDeviceList(){
	//
	// if(updateHandle==null && !_isUpdateSchedulerActive)
	// {
	// startDeviceListUpdateScheduler();
	// }
	//
	// Vector list = new Vector();
	// for (int i = 0; i < _myDevices.size(); i++) {
	// _MyDevice el= (_MyDevice) _myDevices.elementAt(i);
	// list.add(el.info);
	// }
	// return list;
	// }
	//
	// public void updateDeviceList(){
	//
	//
	// if(_isUopdatingDeviceList)
	// return;
	//
	// Runnable r=new Runnable() {
	// Vector updated = new Vector();
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try{
	// _isUopdatingDeviceList=true;
	//
	// IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();
	//
	// for (int i = 0; i < peerAppliances.length; i++) {
	//
	// IAppliance peerAppliance = peerAppliances[i];
	//
	// Hashtable props;
	// try {
	// props = getInfoNew(peerAppliance);
	// } catch (ApplianceException e) {
	// continue;
	// } catch (ServiceClusterException e) {
	// continue;
	// }
	//
	// if (props == null)
	// continue;
	//
	// _MyDevice el=new _MyDevice();
	// el.appliancePid=(String) props.get("pid");
	// el.info=props;
	// el.peerAppliance=peerAppliance;
	// updated.add(el);
	// }
	//
	// _myDevices=updated;
	// _isUopdatingDeviceList=false;
	// }catch(Exception e){
	// _isUopdatingDeviceList=false;
	// }finally{
	// _isUopdatingDeviceList=false;
	// }
	// }
	// };
	// Thread th=new Thread(r);
	// th.start();
	// }
	//
	// public void startDeviceListUpdateScheduler(){
	//
	// final Runnable updater = new Runnable() {
	// public void run() {
	// updateDeviceList();
	// }
	// };
	// updateHandle = scheduler.scheduleAtFixedRate(updater, UPDATE_TIME,
	// UPDATE_TIME, TimeUnit.SECONDS);
	// _isUpdateSchedulerActive=true;
	//
	// }
	//
	// public void stopDeviceListUpdateScheduler(){
	// try{
	// updateHandle.cancel(true);
	// updateHandle=null;
	// _isUpdateSchedulerActive=false;
	// scheduler.shutdownNow();
	// }catch(Exception e){
	//
	// }
	// }

	// END DEMO MILANO

	public List getDailyPVForecast() {
		// controllo se l'istanza \E8 ancora in esecuzione, altrimenti ne faccio
		// partire una nuova
		// faccio partire l'acquisizione dati per avere i dati aggiornati alla
		// prossima chiamata
		if (PVThread == null) {
			PVThread = new Thread(new Runnable() {
				public void run() {
					getPVForecast();
				}
			});
			PVThread.start();
		} else {
			if (!PVThread.isAlive()) {
				PVThread = new Thread(new Runnable() {
					public void run() {
						getPVForecast();
					}
				});
				PVThread.start();
			}
		}
		forecast_debug += "--momento:" + forecast.size();
		if (!forecast.isEmpty())
			return forecast;
		else { // se non ho valori ritorno valori fake mentre sta acquisendo
				// valori reali
			return new ArrayList<Double>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.349, 0.349, 0.447, 0.761, 0.761, 0.911, 0.688, 0.688, 0.607, 0.327, 0.052, 0.0, 0.0, 0.0, 0.0));
		}
	}

	public String getDailyPVForecastDebug() {
		return forecast_debug;
	}

	private void getPVForecast() {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			Calendar cal = new GregorianCalendar();
			if (cal.get(Calendar.HOUR_OF_DAY) < 11)
				cal.add(Calendar.DAY_OF_MONTH, -1);
			String date = format.format(cal.getTime());
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();
			// Send SOAP Message to SOAP Server
			String url = "http://ws.i-em.eu/v4/iem.asmx";
			SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(date), url);
			// Process the SOAP Response
			double[] val = getValuesFromSOAPResponse(soapResponse);
			if (val != null) {
				if (val.length > 0)
					forecast = new ArrayList<Double>();
				for (int i = 0; i < val.length; i++) {
					if (Double.isNaN(val[i]))
						val[i] = 0;
					forecast.add(val[i]);
				}
			}
			forecast_debug += "---fsize: " + forecast.size();
			soapConnection.close();
		} catch (Exception e) {
			forecast_debug += "---EXCEPT: " + e.getMessage();
		}
	}

	private static SOAPMessage createSOAPRequest(String date) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String serverURI = "http://ws.i-em.eu/v4/";

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration("example", serverURI);

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("Get72hPlantForecast", "example");
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("plantID", "example");
		soapBodyElem1.addTextNode("telecom_02");
		SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("quantityID", "example");
		soapBodyElem2.addTextNode("frc_pac");
		SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("timestamp", "example");
		soapBodyElem3.addTextNode(date);
		SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("langID", "example");
		soapBodyElem4.addTextNode("en");

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", serverURI + "Get72hPlantForecast");

		soapMessage.saveChanges();

		return soapMessage;
	}

	private double[] getValuesFromSOAPResponse(SOAPMessage soapResponse) throws Exception {
		NodeList list = soapResponse.getSOAPPart().getElementsByTagName("values");
		if (list != null) {
			forecast_debug += "---found values";
			list = list.item(0).getChildNodes();
			double[] frc_values = new double[24];
			for (int i = 0; i < 24; i++) {
				Node node = list.item(i);
				Node value = node.getLastChild();
				frc_values[i] = Double.parseDouble(value.getTextContent());
				// System.out.println("node name: "+value.getNodeName()+" value: "+value.getTextContent());
			}
			return frc_values;
		} else {
			forecast_debug += "---not found values";
			return null;
		}
	}

	/*
	 * Ottengo la data di domani in orario in cui \E8 arrivato il nuovo forecast
	 */
	private static Date getTomorrow10AM() {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.HOUR_OF_DAY, 10);
		cal.set(Calendar.MINUTE, 40);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date date = cal.getTime();
		return date;
	}

	public List<String> getPropConfiguration(String lblProps) throws IOException {

		List<String> items = null;

		try {
			if (this.props == null) {
				loadPropFile();
			}
			return Arrays.asList(this.props.getProperty(lblProps).split("\\s*,\\s*"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}

	public List<String> getPropStoricoConfiguration(String device, String tipoDato, String periodo) throws IOException {
		/* periodo = GIORNO : 1, SETTIMANA : 2, MESE : 3, ANNO : 4 */
		/*
		 * tipoDato = CONSUMO : "ah.eh.esp.Energy", COSTO :
		 * "ah.eh.esp.EnergyCost", PRODUZIONE : "ah.eh.esp.ProducedEnergy",
		 */
		/* device = SMARTINFO : 0 || null, ALTRI : 1..infinito */

		// List<String> items = null;
		String lblProps = null;

		try {
			if (this.props == null) {
				loadPropFile();
			}

			if ((device == null) || (device.equals("0"))) {
				// SmartInfo
				lblProps = "SI";
			} else {
				// Device
				lblProps = "DEV";
			}

			if (tipoDato.equals("ah.eh.esp.Energy")) {
				lblProps += "Energy";
			} else if (tipoDato.equals("ah.eh.esp.EnergyCost")) {
				lblProps += "Cost";
			} else {
				lblProps += "Production";
			}

			if (periodo.equals("1")) {
				lblProps += "DAY";
			} else if (periodo.equals("2")) {
				lblProps += "WEEK";
			} else if (periodo.equals("3")) {
				lblProps += "MONTH";
			} else {
				lblProps += "YEAR";
			}

			return Arrays.asList(this.props.getProperty(lblProps).split("\\s*,\\s*"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public Hashtable getPropConfigurationHM(String lblProps) throws IOException {

		Hashtable props = new Hashtable();
		String[] lbl = null;
		String[] stringToParseInt = null;
		List<Integer> series = new ArrayList<Integer>();
		int idx, idxC;

		try {
			if (this.props == null) {
				loadPropFile();
			}
			if ((lblProps.equals("SuddivisioneConsumi"))) {

				idx = Integer.parseInt(this.props.getProperty("SuddivisioneConsumi"));
				for (idxC = 0; idxC < idx; idxC++) {
					lbl = this.props.getProperty("SuddivisioneConsumi" + idxC + "_el").split("\\s*,\\s*");
					stringToParseInt = this.props.getProperty("SuddivisioneConsumi" + idxC + "_val").split("\\s*,\\s*");
					series = new ArrayList<Integer>();
					for (String s : stringToParseInt) {
						Integer tmpValue = Integer.parseInt(s);
						series.add(tmpValue.intValue());
					}
					props.put(lbl[0], series);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return props;
	}

	public Hashtable getAllPropConfiguration() throws IOException {

		Hashtable props = new Hashtable();
		String[] lbls = { "ActualDate", "EnergiaProdottaGiornalieroSimul", "EnergiaConsumataGiornalieroSimul", "ConsumoOdiernoSimul", "ConsumoMedio", "ProdottaMedio", "PercIAC2", "PercIAC", "ConsumoMedioSettimanale", "ProdottaMedioSettimanale", "ConsumoAttuale", "ProduzioneAttuale", "ConsumoPrevisto", "Forecast", "SuddivisioneConsumi", "SIEnergyDAY", "SIEnergyWEEK", "SIEnergyMONTH", "SIEnergyYEAR", "SICostDAY", "SICostWEEK", "SICostMONTH", "SICostYEAR", "SIProductionDAY", "SIProductionWEEK", "SIProductionMONTH", "SIProductionYEAR", "DEVEnergyDAY", "DEVEnergyWEEK",
				"DEVEnergyMONTH", "DEVEnergyYEAR", "DEVCostDAY", "DEVCostWEEK", "DEVCostMONTH", "DEVCostYEAR" };
		int idx, idxC;

		try {
			if (this.props == null) {
				loadPropFile();
			}

			for (String lbl : lbls) {
				if ((lbl.equals("SuddivisioneConsumi"))) {
					props.put("SuddivisioneConsumi", getPropConfigurationHM("SuddivisioneConsumi"));
				} else {
					props.put(lbl, getPropConfiguration(lbl));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return props;
	}

	public Boolean setAllPropConfiguration(String jsonVar) throws JSONException, IOException {

		Hashtable props = new Hashtable();
		String[] lbls = { "ActualDate", "EnergiaProdottaGiornalieroSimul", "EnergiaConsumataGiornalieroSimul", "ConsumoOdiernoSimul", "ConsumoMedio", "ProdottaMedio", "PercIAC2", "PercIAC", "ConsumoMedioSettimanale", "ProdottaMedioSettimanale", "ConsumoAttuale", "ProduzioneAttuale", "ConsumoPrevisto", "Forecast", "SuddivisioneConsumi", "SIEnergyDAY", "SIEnergyWEEK", "SIEnergyMONTH", "SIEnergyYEAR", "SICostDAY", "SICostWEEK", "SICostMONTH", "SICostYEAR", "SIProductionDAY", "SIProductionWEEK", "SIProductionMONTH", "SIProductionYEAR", "DEVEnergyDAY", "DEVEnergyWEEK",
				"DEVEnergyMONTH", "DEVEnergyYEAR", "DEVCostDAY", "DEVCostWEEK", "DEVCostMONTH", "DEVCostYEAR" };
		int idx, idxC;

		try {
			JSONObject obj = new JSONObject(jsonVar);

			for (String lbl : lbls) {
				if ((lbl.equals("SuddivisioneConsumi"))) {
					// props.put("SuddivisioneConsumi",
					// getPropConfigurationHM("SuddivisioneConsumi"));
				} else {
					String JSONProp = obj.getString(lbl);
					this.props.setProperty(lbl, JSONProp);
				}
			}

			storePropFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;

	}
}
