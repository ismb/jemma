/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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

import org.energy_home.jemma.ah.cluster.ah.ConfigClient;
import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.cluster.zigbee.custom.SimpleMetering4NoksClient;
import org.energy_home.jemma.ah.cluster.zigbee.custom.SimpleMetering4NoksServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.general.BasicClient;
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
import org.energy_home.jemma.ah.configurator.IConfigurator;
import org.energy_home.jemma.ah.eh.esp.ESPConfigParameters;
import org.energy_home.jemma.ah.eh.esp.ESPService;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.IPeerAppliancesListener;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hap.client.IM2MHapService;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfigurator;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.jobs.Job;
import org.energy_home.jemma.ah.greenathome.GreenAtHomeApplianceService;
import org.osgi.service.http.HttpService;
import org.energy_home.jemma.hac.adapter.http.AhHttpAdapter;
import org.energy_home.jemma.hac.adapter.http.HttpImplementor;

public class GreenathomeAppliance extends Appliance implements HttpImplementor, IServiceClustersListener, IPeerAppliancesListener,
		IManagedAppliance, GreenAtHomeApplianceService, IASZoneClient, ApplianceControlClient, IlluminanceMeasurementClient {

	private static final Log log = LogFactory.getLog(GreenathomeAppliance.class);

	private static final boolean logEnabled = false;

	protected static final String TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String FRIENDLY_NAME = "Green@Home";
	protected static final String END_POINT_TYPE = "org.energy_home.jemma.ah.appliance.greeenathome";
	protected static final String DEVICE_TYPE = null;

	private static final String SMARTINFO_APP_TYPE = "org.energy_home.jemma.ah.zigbee.metering";

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
	private IEndPointRequestContext maxAgeContext;

	private static Hashtable initialConfig = new Hashtable();

	private static ApplianceDescriptor descriptor;

	protected Job readInfosJob;

	protected IEndPointRequestContext ConfirmationNotRequiredRequestContext;
	protected IEndPointRequestContext onOffCommandContext;
	protected IEndPointRequestContext infiniteMaxAge;

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

	static {
		initialConfig.put(IAppliance.APPLIANCE_NAME_PROPERTY, FRIENDLY_NAME);
		descriptor = new ApplianceDescriptor(TYPE, DEVICE_TYPE, FRIENDLY_NAME);
	}

	public GreenathomeAppliance() throws ApplianceException {
		super("ah.app.Greenathome", initialConfig);

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

		this.greenathomeEndPoint.registerServiceClustersListener(this);
		greenathomeEndPoint.registerPeerAppliancesListener(this);

		context = greenathomeEndPoint.getDefaultRequestContext();
		maxAgeContext = greenathomeEndPoint.getRequestContext(true, 120000);
		ConfirmationNotRequiredRequestContext = greenathomeEndPoint.getRequestContext(false, 0);
		onOffCommandContext = greenathomeEndPoint.getRequestContext(true, 20000);
		maxAgeContext = greenathomeEndPoint.getRequestContext(true, 20000000);

		try {
			ahHttpAdapter = new AhHttpAdapter(this, this.applicationWebAlias);
		} catch (Throwable e) {
			log.error(e);
		}
	}

	public IApplianceDescriptor getDescriptor() {
		return descriptor;
	}

	protected void attributeValueReceived(String localEndPointId, String peerAppliancePid, String peerEndPointId,
			String peerClusterName, IAttributeValue peerAttributeValue) {
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
			onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
					OnOffServer.class.getName());
			if (onOffServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) onOffServer).getEndPoint().isAvailable() ? 2 : 0;
				boolean onOff = false;

				try {
					onOff = onOffServer.getOnOff(onOffCommandContext);
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
			ThermostatServer thermostatServer = (ThermostatServer) greenathomeEndPoint.getPeerServiceCluster(
					peerAppliance.getPid(), ThermostatServer.class.getName());

			if (thermostatServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;
			}
			RelativeHumidityMeasurementServer humidityServer = (RelativeHumidityMeasurementServer) greenathomeEndPoint
					.getPeerServiceCluster(peerAppliance.getPid(), RelativeHumidityMeasurementServer.class.getName());
			if (humidityServer != null) {
				isStateChangable = true;
				availability = ((IServiceCluster) humidityServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			// handle Smart Info and Smart Plugs
			SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(
					peerAppliance.getPid(), SimpleMeteringServer.class.getName());

			if (onOffServer == null && simpleMeteringServer != null) {
				availability = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable() ? 2 : 0;
			}

			ConfigServer configServer = (ConfigServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
					ConfigServer.class.getName());

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
					float localTemperature = (float) (thermostatServer.getLocalTemperature(maxAgeContext) / 100.0);
					value = localTemperature + "^C";
					if (humidityServer != null) {
						float humididy = (float) (humidityServer.getMeasuredValue(maxAgeContext) / 100.0);
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
			configServer = (ConfigServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
					ConfigServer.class.getName());

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
			configServer = (ConfigServer) this.greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
					ConfigServer.class.getName());

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
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * removeDevice(org.energy_home.jemma.ah.hac.IAppliance)
	 */
	public synchronized void removeDevice(String appliancePid) throws ApplianceException {
		synchronized (lockGatH) {
			if (this.hacService != null) {
				this.hacService.removeAppliance(appliancePid);
			} else
				throw new ApplianceException("Unable to remove the appliance.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * getInfosNew()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * setDeviceState(org.energy_home.jemma.ah.hac.IAppliance, int)
	 */
	public boolean setDeviceState(IAppliance peerAppliance, int state) {

		OnOffServer onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
				OnOffServer.class.getName());
		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(
				peerAppliance.getPid(), ApplianceControlServer.class.getName());

		if (onOffServer != null) {
			if (state == On) {
				try {
					// 4Noks smart plugs require to disable default response to
					// work!!!
					onOffServer.execOn(ConfirmationNotRequiredRequestContext);
				} catch (Exception e) {
					if (logEnabled)
						log.debug("setDeviceState returned exception '" + e.getMessage());
					return false;
				}
			} else if (state == Off) {
				try {
					// 4Noks smart plugs require to disable default response to
					// work!!!
					onOffServer.execOff(ConfirmationNotRequiredRequestContext);
				} catch (Exception e) {
					log.debug("setDeviceState returned exception '" + e.getMessage());
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
				e.printStackTrace();
				log.debug("execCommandExecution exception " + e.getMessage());
				return false;
			}
		} else
			return false;

		return true;
	}

	public int getDeviceState(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			OnOffServer onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
					OnOffServer.class.getName());

			ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(
					peerAppliance.getPid(), ApplianceControlServer.class.getName());

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
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * getObjectByPid(java.lang.String)
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
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * notifyPeerApplianceConnected(java.lang.String)
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
						SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, SimpleMeteringServer.class.getName());

						ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, ApplianceControlServer.class.getName());

						OccupancySensingServer occupancySensingServer = (OccupancySensingServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, OccupancySensingServer.class.getName());

						if (simpleMeteringServer != null) {
							boolean avail = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable();
							if (avail) {
								Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
								if (demandFormatting == null) {
									short df = simpleMeteringServer.getDemandFormatting(context);
									demandFormatting = new Short(df);
									if (logEnabled)
										log.debug("read demand formatting for appliance " + peerAppliance.getPid() + " with value "
												+ df);
									demandFormattings.put(peerAppliance.getPid(), new Short(df));
								}

								if (useReportingOnSimpleMetering) {
									((IServiceCluster) simpleMeteringServer).setAttributeSubscription(
											SimpleMeteringServer.ATTR_IstantaneousDemand_NAME,
											new SubscriptionParameters(2, 10, 5), null);
								}
							}
						} else {
							log.debug("SimpleMetering Server Cluster missing on appliance " + peerAppliancePid);
						}

						// if (applianceControlServer != null) {
						// if (useReportingOnApplianceControlServer) {
						// ((IServiceCluster)
						// applianceControlServer).setAttributeSubscription(
						// ApplianceControlServer.ATTR_CycleTarget0_NAME, new
						// SubscriptionParameters(2, 50, 1), null);
						// ((IServiceCluster)
						// applianceControlServer).setAttributeSubscription(
						// ApplianceControlServer.ATTR_TemperatureTarget0_NAME,
						// new SubscriptionParameters(2, 50, 1),
						// null);
						// ((IServiceCluster)
						// applianceControlServer).setAttributeSubscription(
						// ApplianceControlServer.ATTR_Spin_NAME, new
						// SubscriptionParameters(2, 50, 1), null);
						// }
						// } else {
						// log.debug("ApplianceControl Server Cluster missing on appliance "
						// + peerAppliancePid);
						// }

						SimpleMetering4NoksServer simpleMetering4NoksServer = (SimpleMetering4NoksServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, SimpleMetering4NoksServer.class.getName());

						if (simpleMetering4NoksServer != null) {
							if (useReportingOnSimpleMetering) {
								((IServiceCluster) simpleMetering4NoksServer).setAttributeSubscription(
										SimpleMetering4NoksServer.ATTR_Power_NAME, new SubscriptionParameters(5, 20, 5), null);

								((IServiceCluster) simpleMetering4NoksServer).setAttributeSubscription(
										SimpleMetering4NoksServer.ATTR_Energy_NAME, new SubscriptionParameters(5, 120, 1), null);
							}

						} else {
							log.debug("SimpleMetering Server Cluster missing on appliance " + peerAppliancePid);
						}

						if (occupancySensingServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) occupancySensingServer).setAttributeSubscription(
										OccupancySensingServer.ATTR_Occupancy_NAME, new SubscriptionParameters(2, 50, 1), null);
							}
						} else {
							log.debug("OccupancySensing Server Cluster missing on appliance " + peerAppliancePid);
						}

						TemperatureMeasurementServer temperatureMeasurementServer = (TemperatureMeasurementServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, TemperatureMeasurementServer.class.getName());

						if (temperatureMeasurementServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) temperatureMeasurementServer).setAttributeSubscription(
										TemperatureMeasurementServer.ATTR_MeasuredValue_NAME, new SubscriptionParameters(2, 50, 1),
										null);
							}
						} else {
							log.debug("Temperature Measurement Server Cluster missing on appliance " + peerAppliancePid);
						}

						IlluminanceMeasurementServer illuminanceMeasurementServer = (IlluminanceMeasurementServer) greenathomeEndPoint
								.getPeerServiceCluster(peerAppliancePid, IlluminanceMeasurementServer.class.getName());
						if (illuminanceMeasurementServer != null) {
							if (useReportingOnApplianceControlServer) {
								((IServiceCluster) illuminanceMeasurementServer).setAttributeSubscription(
										IlluminanceMeasurementServer.ATTR_MeasuredValue_NAME, new SubscriptionParameters(2, 50, 1),
										null);
							}
						} else {
							log.debug("Illuminance Measurement Server Cluster missing on appliance " + peerAppliancePid);
						}
					} catch (ServiceClusterException e) {
						log.error("", e);
					} catch (ApplianceException e) {
						log.error("", e);
					}
				} else {
					log.debug("appliance " + peerAppliancePid + " in a not valid state");
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
	 * org.energy_home.jemma.ah.internal.greenathome.GreenAtHomeApplianceService#
	 * readPower(org.energy_home.jemma.ah.hac.IAppliance)
	 */
	public double readPower(IAppliance peerAppliance) throws Exception {
		synchronized (lockEsp) {
			if (espService != null) {
				return espService.getInstantaneousPowerFloatValue(peerAppliance.getPid());
			}
		}

		if (!peerAppliance.getDescriptor().getType().equals(SMARTINFO_APP_TYPE)) {
			// this is not the SmartInfo
			SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) this.greenathomeEndPoint.getPeerServiceCluster(
					peerAppliance.getPid(), SimpleMeteringServer.class.getName());

			if (simpleMeteringServer != null) {
				boolean avail = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable();
				if (avail) {
					try {
						Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
						if (demandFormatting == null) {

							short df = simpleMeteringServer.getDemandFormatting(context);
							demandFormatting = new Short(df);
							if (logEnabled)
								log.debug("read demand formatting with value " + df);
							demandFormattings.put(peerAppliance.getPid(), new Short(df));
						}

						int istantaneousDemand = simpleMeteringServer.getIstantaneousDemand(maxAgeContext);
						double power = decodeFormatting(istantaneousDemand, demandFormatting.shortValue());
						return power;
					} catch (Exception e) {
						log.error("Error while calling while trying to invoke getIstantaneousDemand command", e);
					}
				}
			}

			SimpleMetering4NoksServer simpleMetering4NoksServer = (SimpleMetering4NoksServer) this.greenathomeEndPoint
					.getPeerServiceCluster(peerAppliance.getPid(), SimpleMetering4NoksServer.class.getName());

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

	public void notifyAttributeValue(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		synchronized (lockGatH) {
			if (logEnabled)
				log.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());

			if (attributeName.equals(SimpleMeteringServer.ATTR_IstantaneousDemand_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();

				Short demandFormatting = (Short) demandFormattings.get(peerAppliance.getPid());
				if (demandFormatting == null) {
					log.fatal("demand formatting not available for appliance " + peerAppliance.getPid());
					return;
				}

				double power = decodeFormatting(((Number) attributeValue.getValue()).longValue(), demandFormatting.shortValue());
				this.istantaneousDemands.put(peerAppliance.getPid(), new Double(power));
				log.debug("calculated on appliance " + peerAppliance.getPid() + " power " + power);
			} else if (attributeName.equals(SimpleMetering4NoksServer.ATTR_Power_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();

				double power = ((Number) attributeValue.getValue()).longValue();
				this.istantaneousDemands.put(peerAppliance.getPid(), new Double(power));
			} else if (attributeName.equals(OccupancySensingServer.ATTR_Occupancy_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();

				if (logEnabled)
					log.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());

				this.occupancySensing.put(peerAppliance.getPid(), attributeValue);
			} else if (attributeName.equals(TemperatureMeasurementServer.ATTR_MeasuredValue_NAME)) {
				IAppliance peerAppliance = endPointRequestContext.getPeerEndPoint().getAppliance();
				if (logEnabled)
					log.debug("arrived attribute " + attributeName + " with value " + attributeValue.getValue().toString());

				this.measuredValues.put(peerAppliance.getPid(), attributeValue);
			}
		}
	}

	public void notifyCommandResponse(String clusterName, String commandName, Object response,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException {
		// TODO Auto-generated method stub
	}

	public void notifyReadResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		// TODO Auto-generated method stub
	}

	public void notifyWriteResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
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
							config.put(key, c.get(key));
						}

						inquredDevices.add(config);
					} catch (Exception e) {
						log.fatal("Unable to get Inquired Appliance " + appliancePids[i], e);
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
				this.hacService.installAppliance(appliancePid, props);
			} catch (HacException e) {
				log.error(e);
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
				log.error(e);
				throw new ApplianceException(e.getMessage());
			}
		}
	}

	public List getAttributeData(String appliancePid, String attributeName, long startTime, long endTime, int resolution,
			boolean fitResolution, int processType) throws Exception {
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

	public Map getAttributeData(String attributeName, long startTime, long endTime, int resolution, boolean fitResolution,
			int processType) throws Exception {

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
			return this.getApplianceConfiguration(appliance);
		}
	}

	private boolean doReadOnOff() {
		return (count % 4) == 0;
	}

	private int count = 0;

	public Hashtable getApplianceConfiguration(IAppliance peerAppliance) throws ApplianceException, ServiceClusterException {
		int availability = 0;
		int state = 0;
		int status = 0;
		boolean isStateChangable = false;

		String locationPid = null;
		String categoryPid = null;

		Hashtable props = new Hashtable();

		AttributeValueExtended attributeValue = null;

		props.put(IAppliance.APPLIANCE_TYPE_PROPERTY, peerAppliance.getDescriptor().getType());
		props.put(IAppliance.APPLIANCE_PID, peerAppliance.getPid());

		OnOffServer onOffServer = null;
		onOffServer = (OnOffServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(), OnOffServer.class.getName());
		if (onOffServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) onOffServer).getEndPoint().isAvailable() ? 2 : 0;
			boolean onOff = false;

			if (availability == 2) {
				Boolean onOffValue = (Boolean) this.onOffValues.get(peerAppliance.getPid());
				if (doReadOnOff() || (onOffValue == null)) {
					try {
						onOff = onOffServer.getOnOff(onOffCommandContext);
						this.onOffValues.put(peerAppliance.getPid(), new Boolean(onOff));

					} catch (Exception e) {
						// availability = 0;
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

		ApplianceControlServer applianceControlServer = (ApplianceControlServer) greenathomeEndPoint.getPeerServiceCluster(
				peerAppliance.getPid(), ApplianceControlServer.class.getName());

		/*
		 * if (applianceControlServer != null) { availability =
		 * ((IServiceCluster)
		 * applianceControlServer).getEndPoint().isAvailable() ? 2 : 0; if
		 * (readApplianceStatus) { isStateChangable = true;
		 * 
		 * int applianceStatus = 0;
		 * 
		 * try { applianceStatus =
		 * applianceControlServer.getApplianceStatus(null);
		 * 
		 * if (logEnabled) log.debug("applianceStatus is " + applianceStatus);
		 * 
		 * if (applianceStatus < 0x03) { state = Off; } else { state = On; } }
		 * catch (Exception e) { state = Unknown; } } else { state = Unknown; }
		 * }
		 */

		IASZoneServer iasZoneServer = (IASZoneServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
				IASZoneServer.class.getName());

		if (iasZoneServer != null) {
			isStateChangable = false;
			availability = ((IServiceCluster) iasZoneServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			Integer zoneStatus = (Integer) this.zoneStatusTable.get(peerAppliance.getPid());
			int iasZoneType = 0;
			if (zoneStatus != null) {

				Integer iasZoneTypeValue = (Integer) this.iasZoneTypeValues.get(peerAppliance.getPid());
				if (iasZoneTypeValue == null) {
					iasZoneType = iasZoneServer.getZoneType(onOffCommandContext);
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

			}
		}

		IlluminanceMeasurementServer illuminanceMeasurementServer = (IlluminanceMeasurementServer) greenathomeEndPoint
				.getPeerServiceCluster(peerAppliance.getPid(), IlluminanceMeasurementServer.class.getName());

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

		OccupancySensingServer occupancySensingServer = (OccupancySensingServer) greenathomeEndPoint.getPeerServiceCluster(
				peerAppliance.getPid(), OccupancySensingServer.class.getName());

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

		TemperatureMeasurementServer temperatureMeasurementServer = (TemperatureMeasurementServer) greenathomeEndPoint
				.getPeerServiceCluster(peerAppliance.getPid(), TemperatureMeasurementServer.class.getName());

		if (temperatureMeasurementServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) temperatureMeasurementServer).getEndPoint().isAvailable() ? 2 : 0;
			state = Unknown;

			IAttributeValue measuredValue = (IAttributeValue) this.measuredValues.get(peerAppliance.getPid());
			if (measuredValue != null) {
				double value = ((double) ((Integer) measuredValue.getValue()).intValue()) / 100;
				attributeValue = new AttributeValueExtended("Temperature", new AttributeValue(
						new DecimalFormat("#.##").format(value) + " �C"));

				if (attributeValue != null)
					props.put("device_value", attributeValue);
			}
		}

		ThermostatServer thermostatServer = (ThermostatServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
				ThermostatServer.class.getName());
		if (thermostatServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;
			int applianceStatus = 0;
		}
		RelativeHumidityMeasurementServer humidityServer = (RelativeHumidityMeasurementServer) greenathomeEndPoint
				.getPeerServiceCluster(peerAppliance.getPid(), RelativeHumidityMeasurementServer.class.getName());
		if (humidityServer != null) {
			isStateChangable = true;
			availability = ((IServiceCluster) humidityServer).getEndPoint().isAvailable() ? 2 : 0;

			int applianceStatus = 0;
		}

		// handle Smart Info and Smart Plugs
		SimpleMeteringServer simpleMeteringServer = (SimpleMeteringServer) greenathomeEndPoint.getPeerServiceCluster(
				peerAppliance.getPid(), SimpleMeteringServer.class.getName());

		if (onOffServer == null && simpleMeteringServer != null) {
			availability = ((IServiceCluster) simpleMeteringServer).getEndPoint().isAvailable() ? 2 : 0;
		}

		ConfigServer configServer = (ConfigServer) greenathomeEndPoint.getPeerServiceCluster(peerAppliance.getPid(),
				ConfigServer.class.getName());

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
		} else
			return null;

		props.put("device_state_avail", new Boolean(isStateChangable));
		props.put("device_state", new Integer(state));
		props.put("availability", new Integer(availability));
		props.put("device_status", new Integer(status));

		if (thermostatServer != null) {
			availability = ((IServiceCluster) thermostatServer).getEndPoint().isAvailable() ? 2 : 0;
			if (availability == 2) {
				float localTemperature = (float) (thermostatServer.getLocalTemperature(maxAgeContext) / 100.0);

				
				String value = localTemperature + " &degC";
				attributeValue = new AttributeValueExtended("LocalTemperature", new AttributeValue(value));
				if (humidityServer != null) {
					float humididy = (float) (humidityServer.getMeasuredValue(maxAgeContext) / 100.0);
					value += " " + humididy + "%";
				}
			}
		} else if (simpleMeteringServer != null) {
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
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(
							istantaneousDemand.doubleValue()));
				} else if (this.fakeMode) {
					attributeValue = new AttributeValueExtended("IstantaneousDemands", new AttributeValue(123.1));
				}
			}
		}

		if (attributeValue != null)
			props.put("device_value", attributeValue);
		return props;
	}

	public ArrayList getAppliancesConfigurations() throws ApplianceException, ServiceClusterException {
		synchronized (lockGatH) {
			ArrayList infos = new ArrayList();

			IAppliance[] peerAppliances = greenathomeEndPoint.getPeerAppliances();

			for (int i = 0; i < peerAppliances.length; i++) {

				IAppliance peerAppliance = peerAppliances[i];

				Hashtable props;
				try {
					props = this.getApplianceConfiguration(peerAppliance);
					// skip ESP and core appliance
					if (peerAppliance.getDescriptor().getType().equals("ah.app.EnergyServicePortal")
							|| peerAppliance.getPid().equals("ah.app.core")) {
						continue;
					}
				} catch (ApplianceException e) {
					continue;
				} catch (ServiceClusterException e) {
					continue;
				}

				if (props == null)
					continue;

				infos.add(props);
			}

			count++;
			return infos;
		}
	}

	public void updateAppliance(Dictionary props) throws ApplianceException {

		log.debug("updateAppliance");
		String appliancePid = (String) props.get("appliance.pid");
		if (appliancePid == null)
			throw new ApplianceException("appliance.pid is null");
		synchronized (lockGatH) {
			if (hacService != null) {
				try {
					this.hacService.updateAppliance(appliancePid, props);
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

	public void execSignalStateNotification(short ApplianceStatus, short RemoteEnableFlags, int ApplianceStatus2,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		log.debug("appliance Status");

		IEndPoint aa = context.getPeerEndPoint();
		aa.getAppliance();

		boolean isStateChangable = true;
		// int availability = ((IServiceCluster)
		// applianceControlServer).getEndPoint().isAvailable() ? 2 : 0;

		int applianceStatus = 0;
		int state;

		try {
			applianceStatus = ApplianceStatus;

			if (logEnabled)
				log.debug("applianceStatus is " + applianceStatus);

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

	public ZoneEnrollResponse execZoneEnrollRequest(int ZoneType, int ManufacturerCode, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		ZoneEnrollResponse zoneEnrollResponse = new ZoneEnrollResponse();
		return zoneEnrollResponse;
	}

	public void execZoneStatusChangeNotification(int ZoneStatus, short ExtendedStatus, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		log.debug(context.getPeerEndPoint().getAppliance().getPid() + ": ZoneStatus=" + ZoneStatus + ", ExtendedStatus="
				+ ExtendedStatus);
		IAppliance peerAppliance = context.getPeerEndPoint().getAppliance();
		Integer zoneValue = new Integer(ZoneStatus);
		this.zoneStatusTable.put(peerAppliance.getPid(), zoneValue);
	}
}
