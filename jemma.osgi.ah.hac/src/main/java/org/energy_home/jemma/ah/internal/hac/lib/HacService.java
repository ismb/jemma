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
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.equinox.internal.util.timer.Timer;
import org.eclipse.equinox.internal.util.timer.TimerListener;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceFactory;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceFactory;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceManager;
import org.energy_home.jemma.ah.hac.lib.ext.Category;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.hac.lib.ext.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
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

public class HacService implements TimerListener, FrameworkListener, IHacService {
	
	private static final Logger LOG = LoggerFactory.getLogger(HacService.class);

	private static String replaceIvalidPidChars(String appliancePid) {
		char[] chars = appliancePid.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i]) && !Character.isLetter(chars[i]))
				chars[i] = '-';
		}
		return new String(chars);
	}

	private boolean permitAlternateDefaultConfiguration = false;

	/**
	 * Dictionary that permits to retrieve the IManagedAppliance service from
	 * its pid
	 */
	private Hashtable pid2appliance = new Hashtable();

	/** dictionary of currently available IManagedAppliance services */
	private Vector appliances = new Vector();

	protected Vector installingAppliances = new Vector();

	/** dictionary the appliances pids */
	private Vector appliancePids = new Vector();

	/** DB for Locations */
	// private Locations locations = new Locations();

	/** DB for Categories */
	private Categories categories = new Categories();

	static final int saveTimerId = 1;

	// private PreferencesService prefsService = null;

	// private HacDriverLocator hacDriverLocator = null;
	private Timer timer = null;

	private Vector applianceFactories = new Vector();

	private ConfigurationAdmin configAdmin;
	private Hashtable type2applianceFactory = new Hashtable();
	private DocumentBuilderFactory factory;
	private BundleContext bc;

	private boolean saveConfigurationToCurrent = true;
	private static final String servicePid = "org.telecomitalia.hac";
	//TODO: check merge, path was empty in 3.3.0
	//private final static String SCENARIOS_PATH = "xml/scenarios/";
	private final static String SCENARIOS_PATH = "";

	private String defaultConfig = "defaultconfig";
	private String configurationFilename = "hac-config";

	private int saveTimeout = 1;

	Hashtable factorypids2configuration = new Hashtable();
	private Object lockHacService = new Object();

	private int newAppliancePid = 0;

	private Hashtable pid2configurations = new Hashtable();

	private LocationsService locationsDb;
	private ServiceRegistration locationsServiceReg;
	private boolean useManagedApplianceServiceTracker = true;
	private ManagedApplianceServiceTracker managedApplianceServiceTracker = null;
//	private CoreAppliance coreAppliance = null;
	private boolean patched = false; // true if an upgrade from 2.2.8 to 3.0.5 (hac.lib) has been detected.
	private boolean enableUpdatePatch = false;

	public IApplianceFactory getFactoryFromManagedAppliance(IManagedAppliance appliance) {
		if (appliance == null)
			return null;
		return (IApplianceFactory) type2applianceFactory.get(appliance.getDescriptor().getType());
	}

	protected void activate(ComponentContext ctxt) {
		synchronized (lockHacService) {
			this.bc = ctxt.getBundleContext();

			if (permitAlternateDefaultConfiguration) {
				String defaultConfigProp = this.bc.getProperty("org.telecomitalia.hac.configuration");

				if (defaultConfigProp != null) {
					defaultConfig = defaultConfigProp;
				}
			}

			this.createLocationService();

			loadCurrentConfig();
			this.bc.addFrameworkListener(this);
			if (useManagedApplianceServiceTracker) {
				this.managedApplianceServiceTracker = new ManagedApplianceServiceTracker(bc, this);
				this.managedApplianceServiceTracker.open();
			}
// Core appliance no more used (now exported service clusters have been implemented by AppliancesProxy)
//			try {
//				coreAppliance = new CoreAppliance();
//				coreAppliance.start(bc);
//			} catch (ApplianceException e) {
//				log.error("Error while creating core appliance", e);
//			}
		}
	}

	private void createLocationService() {
		locationsDb = new LocationsService();

		locationsDb.setConfigurationAdmin(configAdmin);

		Dictionary props = new Hashtable();
		props.put(Constants.SERVICE_PID, "org.energy_home.jemma.osgi.ah.hac.locations");

		locationsServiceReg = bc.registerService(ManagedServiceFactory.class.getName(), locationsDb, props);
	}

	private void disposeLocationService() {
		if (locationsServiceReg != null) {
			locationsDb.unsetConfigurationAdmin(configAdmin);
			locationsServiceReg.unregister();
			locationsDb = null;
		}
	}

	protected void deactivate(ComponentContext ctxt) {
		synchronized (lockHacService) {
			disposeLocationService();

			Led.setLed(0);
			this.bc.removeFrameworkListener(this);
			LOG.debug("deactivated");
//			coreAppliance.stop();
			if (this.managedApplianceServiceTracker != null)
				this.managedApplianceServiceTracker.close();
		}
	}

	public void modified(ComponentContext ctxt, Map props) {
		synchronized (lockHacService) {
			update(props);
		}
	}

	public void setDocumentBuilderFactory(DocumentBuilderFactory r) {
		synchronized (lockHacService) {
			factory = r;
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			factory.setCoalescing(false);
		}
	}

	public void unsetDocumentBuilderFactory(DocumentBuilderFactory r) {
		synchronized (lockHacService) {
			if (factory == r)
				factory = null;
		}
	}

	/**
	 * Called by DS when a new IManagedAppliance service is detected in OSGi
	 * 
	 * @param appliance
	 *            The IManagedAppliance service object
	 * @throws ApplianceException
	 */

	public void setManagedAppliance(IManagedAppliance appliance, Map appProps) throws ApplianceException {
		synchronized (lockHacService) {

			/*
			 * Internally to the hac applications are identified by their pid
			 * that should be unique and persistent.
			 * 
			 * This helps to maintain a coherence when the HAC or an Appliance
			 * is restarted.
			 */

			String appliancePid = appliance.getPid();

			if (appliancePid == null) {
				LOG.warn("the managed appliance doesn't have an associated pid, discarding it!");
				return;
			}

			if (!pid2appliance.contains(appliancePid)) {
				pid2appliance.put(appliancePid, appliance);
			} else {
				LOG.warn("discarding appliance because it has a duplicated appliance.pid");
				return;
			}

			String appStatus = (String) appProps.get("ah.status");
			if (appStatus != null && (appStatus.equals("installing"))) {
				LOG.debug("New appliance to install detected: " + appliancePid);
				installingAppliances.add(appliance);
			} else {
				appliances.add(appliance);
			}

			((ApplianceManager) appliance.getApplianceManager()).setHacService(this);

			String applianceName = null;

			Configuration c;
			try {
				c = getApplianceCAConfiguration(appliancePid);
			} catch (Exception e) {
				LOG.warn(e.getMessage(), e);
				return;
			}

			applianceName = (String) appProps.get(IAppliance.APPLIANCE_NAME_PROPERTY);
			// If no name is assigned to the appliance, a unique name is
			// created and assigned
			if (applianceName == null) {
				if (c == null) {
					String factoryPid = (String) appProps.get(IAppliance.APPLIANCE_TYPE_PROPERTY);

					if (factoryPid == null) {
						if (!appliance.isSingleton())
							LOG.debug("the appliance doesn't have the ah.app.type property set and is not a singleton");
						return;
					}
					
					IApplianceFactory applianceFactory = this.getApplianceFactory(factoryPid);

					if (applianceFactory == null) {
						LOG.debug("no factory for type " + factoryPid);
						return;
					}
					try {
						Configuration[] configurations = this.getApplianceCAConfigurations(appliancePid);
						if (configurations == null) {
							c = this.configAdmin.createFactoryConfiguration(factoryPid, null);
							LOG.debug("created configuration for appliance.pid " + appliancePid);
						}
					} catch (Exception e) {
						LOG.warn(e.getMessage(), e);
						return;
					}
				}

				Dictionary props = new Hashtable();
				for (Iterator iterator = appProps.keySet().iterator(); iterator.hasNext();) {
					Object type = (Object) iterator.next();
					props.put(type, appProps.get(type));
				}
				if (applianceName == null) {
					String namePrefix = appliance.getDescriptor().getFriendlyName();
					applianceName = createUniqueName(namePrefix);
					props.put(IAppliance.APPLIANCE_NAME_PROPERTY, applianceName);
				}

				try {
					c.update(props);
				} catch (IOException e) {
					LOG.debug(e.getMessage());
				}
			}
		}
	}

	protected void unsetManagedAppliance(IManagedAppliance appliance) {
		synchronized (lockHacService) {
			String appliancePid = appliance.getPid();

			if (appliancePid == null) {
				return;
			}

			appliancePids.remove(appliancePid);
			pid2appliance.remove(appliancePid);
			appliances.remove(appliance);
			installingAppliances.remove(appliance);
		}
	}

	protected void updatedManagedAppliance(IManagedAppliance appliance, final Map props) {
		LOG.debug("called updated method");
		// this method is called when the service properties are updated
		synchronized (lockHacService) {
			String appliancePid = appliance.getPid();

			if (appliancePid == null) {
				LOG.warn("the managed appliance doesn't have an associated pid, discarding it!");
				return;
			}

			Object a = pid2appliance.get(appliancePid);
			if (a == null) {
				LOG.debug("updated unknown appliance " + appliancePid);
				return;
			}

			String appStatus = (String) props.get("ah.status");
			if (appStatus != null && (appStatus.equals("installing"))) {
				// !!!Multieps: a concurrent update can occur (device access and
				// configuration admin can concurrenlty attach and update the
				// application)
				LOG.debug("appliance with installing state detected. Why? The appliance pid is " + appliancePid);
				return;
			} else {
				// !!!Multieps: a concurrent update can occur (device access and
				// configuration admin can concurrently attach and update the
				// application)
				if (installingAppliances.contains(appliance))
					installingAppliances.remove(appliance);
				if (!appliances.contains(appliance)) {
					appliances.add(appliance);
				}
			}
		}
	}

	private Configuration getApplianceCAConfiguration(String appliancePid) throws Exception {
		Configuration[] configurations;
		configurations = this.configAdmin.listConfigurations("(appliance.pid=" + appliancePid + ")");

		if (configurations == null) {
			return null;
		}
		return configurations[0];
	}

	private Configuration[] getApplianceCAConfigurations(String appliancePid) throws Exception {
		return this.configAdmin.listConfigurations("(appliance.pid=" + appliancePid + ")");
	}

	public void setConfigurationAdmin(ConfigurationAdmin configAdmin) {
		synchronized (lockHacService) {
			this.configAdmin = configAdmin;
		}
	}

	public void unsetConfigurationAdmin(ConfigurationAdmin configAdmin) {
		synchronized (lockHacService) {
			if (this.configAdmin == configAdmin) {
				this.configAdmin = null;
			}
		}
	}

	public void setApplianceFactory(IApplianceFactory s, Map props) throws HacException {
		synchronized (lockHacService) {
			String type = (String) props.get("service.pid");
			if ((type == null) || (type != null) && (type.length() == 0)) {
				LOG.debug("the appliance factory doesn't have the service.pid set");
				throw new HacException("type missing in appliance factory " + s.getName());
			}

			if (type2applianceFactory.get(type) != null) {
				LOG.debug("two different appliance factories serve the same type '" + s.getDescriptor().getType() + "'");
				return;
			}

			applianceFactories.add(s);
			type2applianceFactory.put(type, s);
			s.init();
		}
	}

	public void unsetApplianceFactory(IApplianceFactory s) {
		synchronized (lockHacService) {
			String type = s.getDescriptor().getType();

			applianceFactories.remove(s);
			type2applianceFactory.remove(type);

			LOG.debug("removed IApplianceType " + type);
		}
	}

	public void setTimer(Timer timer) {
		synchronized (lockHacService) {
			this.timer = timer;
		}
	}

	public void unsetTimer(Timer timer) {
		synchronized (lockHacService) {
			if (this.timer == timer)
				this.timer = null;
		}
	}

	protected synchronized Dictionary getAvailableConfigurations(String type) {
		Dictionary configurations = (Dictionary) factorypids2configuration.get(type);
		return configurations;
	}

	/**
	 * Creates a factory configuration object if it doesn't exist, yet
	 * 
	 * @param type
	 * @param pid
	 * @param props
	 */

	protected void createConfiguration(String factoryPid, String pid, Dictionary props) {

		if (pid == null)
			pid = generatePid();

		Configuration c = null;

		LOG.debug("adding configuration for appliance " + pid);

		try {
			Configuration[] configurations = getApplianceCAConfigurations(pid);
			if (configurations == null) {
				c = this.configAdmin.createFactoryConfiguration(factoryPid, null);

				// remove old property service.pid
				props.remove(Constants.SERVICE_PID);
				props.put("appliance.pid", pid);
				LOG.debug("created configuration for appliance.pid " + pid);
				c.update(props);
			}
		} catch (Exception e) {
			LOG.warn(e.getMessage(), e);
		}
	}

	/**
	 * Create an appliance given its type (i.e. factory type) and properties.
	 * The props dictionary may be filled with the properties that have to be
	 * assigned to the new appliance.
	 * 
	 * @param factoryPid
	 *            The type of the new appliance
	 * @param props
	 *            The properties of the new appliance
	 * @return the pid of the newly created appliance.
	 */

	protected String createApplianceByFactory(String factoryPid, Dictionary props) throws HacException {
		synchronized (lockHacService) {
			ApplianceFactory applianceFactoryService = this.getApplianceFactory(factoryPid);
			if (applianceFactoryService == null)
				throw new HacException("unable to find an appliance factory for type '" + factoryPid + "'");

			// check if the property dictionary contains the ah.app.name
			// property
			String friendlyName = applianceFactoryService.getDescriptor().getFriendlyName();

			String pid;

			if (friendlyName != null) {
				pid = generateUniquePid(friendlyName);
			} else {
				pid = this.generatePid();
			}

			if ((props.get(IAppliance.APPLIANCE_NAME_PROPERTY) == null) && (friendlyName != null)) {
				String name = createUniqueName(friendlyName);
				props.put(IAppliance.APPLIANCE_NAME_PROPERTY, name);
			}

			createConfiguration(factoryPid, pid, props);
			return pid;
		}
	}

	protected void deleteConfiguration(String type, String pid) {
		LOG.debug("delete configuration for device " + pid);
		Dictionary configurations = getAvailableConfigurations(type);
		if (configurations != null) {
			configurations.remove(pid);
		}
	}

	/**
	 * Retrieves the last saved configuration or the default one if not
	 * available.
	 */

	private void loadCurrentConfig() {
		boolean res = loadConfiguration(configurationFilename, true);
		if (!res) {
			/* switch to factory default */
			if ((defaultConfig != null) && !defaultConfig.equals("")) {
				LOG.debug("no saved configuration found, try to load '" + defaultConfig + "'");
				if (loadConfiguration(defaultConfig, false)) {
					// log.debug("configuration '" + defaultConfig +
					// "' loaded successfully");
				} else {
					LOG.debug("no saved configuration found and unable to read configuration '" + defaultConfig + "'");
				}
			} else {
				LOG.debug("no saved configuration found and no configuration specified. Skip loading configuration");
			}
		}
	}

	/**
	 * Stores the current hac configuration
	 */

	protected void storeCurrentConfiguration() {
		takeConfigurationSnapshot(configurationFilename);
	}

	protected void timerStart(int event, int timePeriod) {
		Timer time = (Timer) getTimer();
		time.notifyAfter(this, timePeriod, event);
	}

	protected Timer getTimer() {
		return timer;
	}

	protected void timerCancel(int event) {
		Timer time = (Timer) getTimer();
		time.removeListener(this, event);
	}

	private String generatePid() {
		return Integer.toString(newAppliancePid++);
	}


	
	private String generateUniquePid(String prefix) {
		int count = 1;
		String generatedPid;

		while (true) {
			// Multieps: modified pid format with prefix used in hap service to
			// identify an appliance pid
			generatedPid = "ah.app." + replaceIvalidPidChars(prefix) + Integer.toString(count);
			Vector app = browseAppliances(IAppliance.APPLIANCE_PID_PROPERTY_KEY, generatedPid);

			if (app.size() == 0) {
				break;
			}
			count++;
		}
		return generatedPid;
	}

	public String createUniqueName(String rootName) {
		int count = 1;
		String proposedName;
		Vector applNames;

		while (true) {
			proposedName = rootName + " " + Integer.toString(count);
			applNames = browseAppliances(IAppliance.APPLIANCE_NAME_PROPERTY_KEY, proposedName);

			if (applNames.size() == 0) {
				// found a not used name
				break;
			}
			count++;
		}
		return proposedName;
	}

	/*
	 * HacService interface-related methods
	 */

	protected boolean add(IManagedAppliance managedAppliance) {
		if (appliances.indexOf(managedAppliance) != -1) {
			return true;
		}

		Iterator it = appliances.iterator();

		String name = managedAppliance.getDescriptor().getType();
		boolean found = false;

		while (it.hasNext()) {
			IManagedAppliance d = (IManagedAppliance) it.next();
			if (name == d.getDescriptor().getType()) {
				found = true;
				break;
			}
		}

		if (found) {
			LOG.debug("device names must be unique");
			return false;
		}

		appliances.add(managedAppliance);
		// objects.put(new Integer(managedAppliance.hashCode()),
		// managedAppliance);

		pid2appliance.put(managedAppliance.getPid(), managedAppliance);
		return true;
	}

	public Vector getAppliances() {
		synchronized (lockHacService) {

			Vector list = new Vector();
			for (Iterator iterator = appliances.iterator(); iterator.hasNext();) {
				IAppliance object = (IAppliance) iterator.next();
				list.add(object.getPid());
			}
			return list;
		}
	}

	public Vector browseAppliances(int key_type, String key_value) {
		synchronized (lockHacService) {
			Vector result = new Vector();

			LOG.debug("called browseDevices");
			IManagedAppliance d = null;

			if (key_type == IAppliance.APPLIANCE_TYPE_PROPERTY_KEY) {
				if (key_value.compareTo("") == 0) {
					// return the list of devices as it is!
					return appliances;
				}

				Iterator it = appliances.iterator();
				while (it.hasNext()) {
					d = (IManagedAppliance) it.next();
					if ((d.getDescriptor().getType() != null) && (d.getDescriptor().getType().compareTo(key_value) == 0)) {
						result.add(d);
					}
				}
			} else if (key_type == IAppliance.APPLIANCE_LOCATION_PID_PROPERTY_KEY) {
				// returns the list of devices at the specific location
				if (key_value.compareTo("") == 0) {
					return appliances;
				}

				Iterator it = appliances.iterator();
				while (it.hasNext()) {
					d = (IManagedAppliance) it.next();
					if (d != null) {
						String locationPid = (String) d.getConfiguration().get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
						if (locationPid != null && locationPid.equals(key_value))
							result.add(d);
					}
				}
			} else if (key_type == IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY_KEY) {

				// returns the list of devices at the specific location
				if (key_value.compareTo("") == 0) {
					return appliances;
				}

				Iterator it = appliances.iterator();
				while (it.hasNext()) {
					d = (IManagedAppliance) it.next();
					if (d != null) {
						String categoryPid = (String) d.getConfiguration().get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);
						if (categoryPid != null && categoryPid.equals(key_value))
							result.add(d);
					}
				}
			} else if (key_type == IAppliance.APPLIANCE_NAME_PROPERTY_KEY) {

				// returns the list of devices at the specific location
				if (key_value.compareTo("") == 0) {
					return appliances;
				}

				Iterator it = appliances.iterator();
				while (it.hasNext()) {
					d = (IManagedAppliance) it.next();
					if (d != null) {
						Dictionary c = d.getConfiguration();
						String name = (String) c.get(IAppliance.APPLIANCE_NAME_PROPERTY);
						if (name != null && name.equals(key_value))
							result.add(d);
					}
				}
			} else if (key_type == IAppliance.APPLIANCE_PID_PROPERTY_KEY) {
				IAppliance appliance = (IAppliance) this.pid2appliance.get(key_value);
				if (appliance != null)
					result.add(appliance);
			}
			return result;
		}
	}

	public ILocation[] getLocations() {
		synchronized (lockHacService) {
			return this.locationsDb.getLocations();
		}
	}

	public ICategory[] getCategories() {
		synchronized (lockHacService) {
			return categories.getCategories();
		}
	}

	public Location addLocation(Location location) throws HacException {
		synchronized (lockHacService) {
			return this.locationsDb.add(location);
		}
	}

	public void addCategory(ICategory category) throws HacException {
		synchronized (lockHacService) {
			categories.add(category);
			this.configUpdated();
		}
	}

	protected void configUpdated() {
		this.saveConfigurationDelayed();
	}

	public boolean removeAppliance(String appliancePid) {
		if (appliancePid.equals(IAppliancesProxy.PROXY_APPLIANCE_PID))
			throw new IllegalArgumentException("Appliances proxy appliance cannot be deleted!");
		synchronized (lockHacService) {
			IManagedAppliance appliance = (IManagedAppliance) this.pid2appliance.get(appliancePid);
			this.removeDevice(appliance);

			if (appliance != null) {
				try {
					Configuration configuration = this.getApplianceCAConfiguration(appliancePid);
					if (configuration != null) {
						configuration.delete();
					}
					return true;
				} catch (Exception e) {
					LOG.warn(e.getMessage(), e);
				}

			}
			return false;
		}
	}

	public boolean removeDevice(IManagedAppliance device) {
		synchronized (lockHacService) {
			if (appliances.contains(device)) {
				appliances.remove(device);
				return true;
			}
			return false;
		}
	}

	/**
	 * Hac drivers can be connected each other if they are compatible each
	 * other. In order to be compatible they should have in common at least one
	 * cluster id.
	 * 
	 * 
	 * @return
	 */

	public Vector getConnectableDevices() {
		synchronized (lockHacService) {
			return null;
		}
	}

	public void timer(int event) {
		switch (event) {
		case saveTimerId:
			saveConfiguration();
			break;
		}

	}

	/**
	 * 
	 * @param props
	 */

	private void update(Map props) {
		LOG.debug("received configuration");
		// boolean enableAutoInstall = getProperty(props,
		// PROP_ENABLE_AUTOINSTALL, DEFAULT_ENABLE_AUTOINSTALL);
		// enableAutoInstall
	}

	public void clean() {
		synchronized (lockHacService) {
			try {
				Configuration[] configurations = configAdmin.listConfigurations("(appliance.pid=*)");
				if (configurations != null)
					for (int i = 0; i < configurations.length; i++) {
						configurations[i].delete();
					}
			} catch (IOException e) {
				LOG.warn("exception deleting configurations", e);
			} catch (Exception e) {
				LOG.warn("exception deleting configurations", e);
			}

			categories.clear();
			locationsDb.clear();

			this.saveConfiguration();
		}
	}

	protected boolean saveConfiguration() {
		return saveConfiguration(configurationFilename);
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
		if (saveConfigurationToCurrent) {
			timerStart(saveTimerId, saveTimeout);
		}
	}

	protected boolean saveConfiguration(String configName) {
		timerCancel(saveTimerId);
		return takeConfigurationSnapshot(configName);
	}

	protected ApplianceFactory getApplianceFactory(String type) {
		return (ApplianceFactory) type2applianceFactory.get(type);
	}

	/**
	 * Takes a snapshot of the current configuration and name it 'configName'
	 */

	protected boolean takeConfigurationSnapshot(String configName) {

		Document doc = createDoc();

		Element configurationEl = doc.createElement("configuration");
		doc.appendChild(configurationEl);

		Element categoriesEl = doc.createElement("categories");
		configurationEl.appendChild(categoriesEl);

		Iterator it = categories.iterator();

		Category category = null;
		while (it.hasNext()) {
			category = (Category) it.next();
			Element categoryEl = doc.createElement("category");
			categoriesEl.appendChild(categoryEl);
			categoryEl.setAttribute("pid", category.getPid());
			categoryEl.setAttribute("name", category.getName());
			categoryEl.setAttribute("icon", category.getIconName());
		}

		String xmlConfig = doc2xmlString(doc);
		if (LOG.isDebugEnabled())
			LOG.debug(xmlConfig);

		// save the configuration on the filesystem
		File configFile = bc.getDataFile(SCENARIOS_PATH + configName + ".xml");
		if (LOG.isDebugEnabled()) {
			LOG.debug("saving configuration into " + configFile.getPath());
		}
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

		LOG.debug("configuration '" + configName + "' saved successfully");
		return true;
	}

	// private void prop2dom(Document doc, Element father, String name, Object
	// value) {
	// Element el = null;
	//
	// if (name != null) {
	// el = doc.createElement("property");
	// el.setAttribute("name", name);
	// } else {
	// el = doc.createElement("item");
	// }
	//
	// father.appendChild(el);
	//
	// String text;
	// if (value instanceof String) {
	// text = value.toString();
	// setTextContent(doc, el, text);
	// } else if (value instanceof AttributeValue) {
	// text = value.toString();
	// setTextContent(doc, el, text);
	// } else if (value instanceof Vector) {
	// Vector v = (Vector) value;
	// for (int i = 0; i < v.size(); i++) {
	// Object item = v.get(i);
	// prop2dom(doc, el, null, item);
	// }
	// } else {
	// log.warn("property '" + value.getClass().getName() +
	// "'is of an unsupported class");
	// return;
	// }
	//
	// el.setAttribute("type", value.getClass().getName());
	// }

	/**
	 * Load HAC configuration. It is useful to describe the algorithm used to
	 * instantiate the virtual appliances trough virtual appliances factory
	 * services Each <va></va> section contains the properties of the virtual
	 * appliance that the section represents. The load procedure stores all
	 * these properties and tries to match them in an already installed bundles
	 * or to download them on demand.
	 * 
	 * The procedure for doing that is identical to the Device Attachment
	 * Algorithm described in the "Device Access Specification v1.1" The
	 * HacService implements the DeviceLocator interface but doesn't register
	 * it.
	 * 
	 * 
	 * 1. If a DRIVER_ID property is present, the algorithm tries to locate an
	 * already registered IApplianceType service exposing the same DERVICE_ID
	 * property. If such an IApplianceType service is not found, an attempt to
	 * download the maching driver bundle. If the
	 * 
	 * 
	 * @param configName
	 *            Filename. The filename must not include the .xml extension
	 * 
	 * @param storageArea
	 *            If this parameter is true the configuration file is got from
	 *            the r/w data area reserved to the bundle
	 * 
	 * @return true if the configuration has been read and applied correctly. In
	 *         case of errors returns false
	 */

	protected boolean loadConfiguration(String configName, boolean storageArea) {
		synchronized (lockHacService) {
			File configFile;
			InputStream stream = null;

			if (configName == null) {
				configName = "defaultconfig";
				storageArea = true;
			}

			LOG.debug("try to load '" + configName + "'");
			
			try {
				if (storageArea) {
					String configFilename = SCENARIOS_PATH + configName + ".xml";
					if (getProperty("org.energy_home.jemma.ah.updatepatch", enableUpdatePatch)) {
						patched  = PatchUpdateBug.patchUpdateBugOnHacLib(bc, configFilename);
					}
					configFile = bc.getDataFile(configFilename);
					LOG.debug("storage area is " + configFile);
					stream = new FileInputStream(configFile);					
				} else {
					File f = new File(configName);
					if (f.isAbsolute()) {
						stream = new FileInputStream(configName);
					} else {
						String configFilename = SCENARIOS_PATH + configName + ".xml";
						URL url = bc.getBundle().getEntry(configFilename);
						if (url == null) {
							LOG.debug("unable to open file " + configFilename);
							return false;
						}
						stream = url.openStream();
					}
				}
			} catch (FileNotFoundException e) {
				LOG.warn("no saved configuration '" + configName + "'", e);
				return false;
			} catch (IOException e) {
				LOG.warn("unable to open file " + configName, e);
				return false;
			}

			categories.clear();

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

			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					LOG.warn(e.getMessage(), e);
					return false;
				}
			}
			
			if (patched && (getProperty("it.telecomitalia.ah.updatepatch", enableUpdatePatch))) {
				PatchUpdateBug.moveFactoryConfigurations(configAdmin, LocationsService.FACTORY_PID);
			}

			LOG.debug("loaded successfully the previously saved configuration");
			return true;
		}
	}

	public void updated(Dictionary props) throws ConfigurationException {
		LOG.debug("received props");
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
			LOG.error("exception: " + ioEx);
			return null;
		}
		return xmlStr;
	}

	Node lastNode;
	Hashtable properties;
	Object prop;

	private boolean loadLocations = false;
	private boolean loadAppliances = false;

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

			if ((tag == "location") && (loadLocations)) {
				String name = attrs.getNamedItem("name").getNodeValue();
				String icon = attrs.getNamedItem("icon").getNodeValue();
				String pid = attrs.getNamedItem("pid").getNodeValue();

				try {
					this.locationsDb.add(new Location(pid, name, icon));
				} catch (HacException e) {
					// this is a duplicate location, skip it by putting a log
					// message
					LOG.warn("error while adding location found reading configuration file", e);
				}
			}
			if (tag == "category") {
				String name = attrs.getNamedItem("name").getNodeValue();
				String icon = attrs.getNamedItem("icon").getNodeValue();
				String pid = attrs.getNamedItem("pid").getNodeValue();

				try {
					categories.add(new Category(pid, name, icon));
				} catch (HacException e) {
					// this is a duplicate location, skip it by putting a log
					// message
					LOG.warn("error while adding location found reading configuration file", e);
				}
			} else if ((tag == "appliance") && (loadAppliances)) {
				/*
				 * String name = attrs.getNamedItem("name").getNodeValue();
				 * if (log.isDebugEnabled()) log.debug("reading va " + name);
				 */
				properties = new Hashtable();
			} else if ((tag == "property") && (lastNode != null) && (properties != null)) {
				// log.debug("last node is " + lastNode.getNodeName());
				String name = attrs.getNamedItem("name").getNodeValue();

				// Node item = attrs.getNamedItem("type");

				// String type = null;
				//
				// if (item != null) {
				// type = item.getNodeValue();
				// }

				Object propValue = traversePropertyNode(lastNode);
				if (propValue == null) {
					LOG.debug("null property " + name);
				} else {
					properties.put(name, propValue);
				}
			}
			NodeList children = node.getChildNodes();

			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++) {
					traverseConfigurationTree(children.item(i));
				}
				if (tag.equals("appliance")) {
					// we traversed all appliances children, its time to
					// instantiate the
					// driver
					try {
						String appliancePid = (String) properties.get(Constants.SERVICE_PID);
						if (appliancePid == null) {
							// for backward compatibility
							appliancePid = (String) properties.get(IAppliance.APPLIANCE_PID);
						}

						String type = (String) properties.get(IAppliance.APPLIANCE_TYPE_PROPERTY);

						if ((appliancePid != null) && (type != null)) {
							String locationPid = (String) properties.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
							String categoryPid = (String) properties.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);
							if ((locationPid != null) && (this.locationsDb.getByPid(locationPid) == null)) {
								LOG.debug("WARNING: device " + servicePid + " specifies an unknown location pid");
							} else if ((categoryPid != null) && (this.categories.getCategoryByPid(categoryPid) == null)) {
								LOG.debug("WARNING: device " + servicePid + " specifies an unknown category pid");
							} else {
								createConfiguration(type, appliancePid, properties);
							}
						} else {
							LOG.debug("during reading configuration: unable to retrieve driver pid");
						}
					} catch (Exception e) {
						LOG.warn(e.getMessage(), e);
					}
				}
			}
			break;

		case Node.TEXT_NODE:
			break;
		}
	}

	private Object traversePropertyNode(Node node) {
		int nodeType = node.getNodeType();

		switch (nodeType) {
		// print element with attributes
		case Node.ELEMENT_NODE:
			NamedNodeMap attrs = node.getAttributes();
			String tag = node.getNodeName();

			Node item = attrs.getNamedItem("type");
			String type = null;
			if (item != null) {
				type = item.getNodeValue();
			}

			if (tag.equals("item") || tag.equals("property")) {
				if (type == null) {
					return getTextContent(node);
				} else if (type.equals("java.lang.String")) {
					return getTextContent(node);
				} else if (type.equals("java.util.Vector")) {
					NodeList children = node.getChildNodes();
					int len = children.getLength();
					Vector container = new Vector();
					for (int i = 0; i < len; i++) {
						Object res = traversePropertyNode(children.item(i));

						if (res != null) {
							container.add(res);
							LOG.debug("added to vector " + res.toString());
						}
					}

					return container;
				}
			}
		}

		return null;
	}

	private String getTextContent(Node node) {
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

	private void setTextContent(Document doc, Node node, String text) {
		Text textNode = doc.createTextNode(text);
		node.appendChild(textNode);
	}

	public void frameworkEvent(FrameworkEvent fe) {
		if (fe.getType() == FrameworkEvent.STARTED) {
			// loadCurrentConfig();
			// applyConfigurations();
			Led.setLed(1);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(fe.toString() + " type " + fe.getType());
		}
	}

	public Location getLocation(String locationPid) {
		synchronized (lockHacService) {
			return this.locationsDb.getByPid(locationPid);
		}
	}

	public Category getCategory(String categoryPid) {
		synchronized (lockHacService) {
			return this.categories.getCategoryByPid(categoryPid);
		}
	}

	public void updateLocation(Location location) {
		// TODO: not yet implemented
	}

	// public boolean updateConfiguration() {
	// synchronized (lockHacService) {
	// Iterator it = appliances.iterator();
	//
	// String data = "[ ";
	//
	// while (it.hasNext()) {
	// IManagedAppliance appliance = (IManagedAppliance) it.next();
	// IApplianceFactory applianceFactory =
	// getFactoryFromManagedAppliance(appliance);
	// String info;
	//
	// try {
	//
	// // convert info to json
	// info = "{";
	//
	// if (appliance.getAttachedDevice().getPid() != null) {
	// info += "\"ieee_address\": \"" + appliance.getAttachedDevice().getPid() +
	// "\", ";
	// } else {
	// continue;
	// }
	//
	// info += "\"name\": \"" + appliance.getDescriptor().getType() + "\"";
	//
	// Location location = null;
	// String locationPid = (String)
	// appliance.getConfig().get(IApplianceManager.APPLIANCE_LOCATION_PID_PROPERTY);
	// if (locationPid != null) {
	// location = this.locationsDb.getByPid(locationPid);
	// }
	//
	// if (location != null) {
	// info += ", \"location\": \"" + location.getName() + "\"";
	// }
	// Category category = null;
	// String categoryPid = (String)
	// appliance.getConfig().get(IApplianceManager.APPLIANCE_CATEGORY_PID_PROPERTY);
	// if (categoryPid != null) {
	// category = this.categories.getCategoryByPid(categoryPid);
	// info += ", \"category\": \"" + category.getName() + "\"";
	// }
	//
	// info += "}";
	//
	// data += info;
	// } catch (Exception e) {
	// continue;
	// }
	// }
	// data += " ]";
	//
	// boolean result = postConfiguration(data);
	// if (result) {
	// log.info("configuration successfully sent to WSNC");
	// } else {
	// log.error("sending configuration to WSNC");
	// }
	//
	// return result;
	// }
	// }

	public boolean postConfiguration(String data) {
		synchronized (lockHacService) {
			// url of the energy at home application
			String applUrl = bc.getProperty("org.energy_home.jemma.energyathome.url");
			if (applUrl == null) {
				applUrl = "http://163.162.180.229:8282/energyathome";
			}

			String wsncId = bc.getProperty("org.telecomitalia.gal.wsnc.id");
			if (wsncId == null) {
				return false;
			}

			HttpClient client = new HttpClient();

			PostMethod method = new PostMethod(applUrl + "/store?n=" + wsncId);

			InputStream inputStream = null;
			method.setRequestEntity(new StringRequestEntity(data));

			// Execute the method.
			int statusCode = 0;

			try {
				statusCode = client.executeMethod(method);
			} catch (HttpException e) {
				LOG.warn(e.getMessage(), e);
				return false;
			} catch (IOException e) {
				LOG.warn(e.getMessage(), e);
				return false;
			}

			if (statusCode != HttpStatus.SC_OK) {
				LOG.warn("method failed: " + method.getStatusLine());
				return false;
			}

			String responseBody = null;

			try {
				responseBody = method.getResponseBodyAsString();
			} catch (IOException e1) {
				LOG.warn(e1.getMessage(), e1);
				return false;
			}

			if (LOG.isDebugEnabled())
				LOG.debug(new String(responseBody));

			return true;
		}
	}

	private static String removeExtension(String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path upto the filename.
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
	}

	private Vector listConfigurations(boolean builtIn) {
		Vector configurations = new Vector();

		if (builtIn) {
			// gets demos present in the RO area
			Enumeration paths = bc.getBundle().getEntryPaths(SCENARIOS_PATH);
			while (paths.hasMoreElements()) {
				String path = (String) paths.nextElement();
				if (path.endsWith(".xml")) {
					// xml file
					File f = new File(path);

					String name = removeExtension(f.getName());
					configurations.add(name);
				}
			}
		}

		if (!builtIn) {
			// read in storage area (i.e. user scenarios)
			File scenariosDir = bc.getDataFile(SCENARIOS_PATH);

			String[] files = scenariosDir.list();
			for (int i = 0; i < files.length; i++) {
				if (files[i].endsWith(".xml")) {
					String name = removeExtension(files[i]);
					configurations.add(name);
				}
			}
		}
		return configurations;
	}

	public synchronized boolean reset(int level) {
		synchronized (lockHacService) {

			if (level == 0) {
				try {
					this.clean();

					File configFilesDirectory = bc.getDataFile(SCENARIOS_PATH);
					if (LOG.isDebugEnabled()) {
						LOG.debug("deleting directory " + configFilesDirectory.getPath());
					}
					boolean deleted = false;
					if (configFilesDirectory.isDirectory()) {
						deleted = deleteDirectory(configFilesDirectory);
					}
				} catch (Exception e) {
					LOG.warn("during reset exception contains '" + e.getMessage() + "'", e);
					return false;
				}
			} else if (level == 1) {
				try {
					int time = 4;
					LOG.debug("shutdown in " + (time * 60) + " seconds");
					String osName = System.getProperty("os.name");
					String shutdownCommand = null;
					if (osName.equals("Linux")) {
						shutdownCommand = "/sbin/shutdown now";
					}

					if (shutdownCommand != null) {
						Runtime.getRuntime().exec(shutdownCommand);
						return true;
					} else {
						return false;
					}
				} catch (IOException e) {
					LOG.warn("exception during shutdown " + e.getMessage(), e);
				}
			} else if (level == 2) {
				this.clean();
			}

			// reset successful
			return true;
		}
	}

	protected static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	protected Document createDoc() {
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			LOG.error(e.getMessage());
			return null;
		}

		Document doc = docBuilder.newDocument();
		return doc;
	}

	/****** IAppliancesManager exposed services ******/

	// public void updateConfiguration(String pid, Dictionary props) throws
	// HacException {
	// synchronized (lockHacService) {
	// IManagedAppliance managedAppliance = (IManagedAppliance)
	// pid2appliance.get(pid);
	// if (managedAppliance == null)
	// throw new HacException("Auhtorization error: invalid appliance pid");
	//
	// // FIXME: agganciare al configuration admin Service
	// // saveConfigurationDelayed();
	// }
	// }
	//
	// public boolean setConfiguration(String configName) {
	// return false;
	// }

	protected Configuration createFactoryConfiguration(String factoryPid) {
		FactoryConfigurationImpl configuration = new FactoryConfigurationImpl(factoryPid);
		this.factorypids2configuration.put(factoryPid, configuration);
		return configuration;
	}

	protected Configuration getConfiguration(String pid) {
		Configuration configuration = (Configuration) this.pid2configurations.get(pid);
		if (configuration == null) {
			// configuration = new ConfigurationImpl(pid);
		}
		return configuration;
	}

	protected Vector getFactories() {
		synchronized (lockHacService) {
			return this.applianceFactories;
		}
	}

	protected IManagedAppliance getAppliance(String appliancePid) {
		synchronized (lockHacService) {
			IManagedAppliance appliance = (IManagedAppliance) this.pid2appliance.get(appliancePid);
			return appliance;
		}
	}

	public String[] getInquiredAppliances() {
		synchronized (lockHacService) {
			String[] appliancePids = new String[this.installingAppliances.size()];
			for (int i = 0; i < this.installingAppliances.size(); i++) {
				appliancePids[i] = ((IManagedAppliance) this.installingAppliances.get(i)).getPid();
			}
			return appliancePids;
		}
	}

	public void enableAppliance(String appliancePid) throws HacException {
		this.installAppliance(appliancePid);
	}

	public void installAppliance(String appliancePid, Dictionary props) throws HacException {
		synchronized (lockHacService) {
			IManagedAppliance appliance = (IManagedAppliance) this.pid2appliance.get(appliancePid);
			if (appliance == null) {
				throw new HacException("an appliance can be installed only if has been already created");
			}

			if (!this.installingAppliances.contains(appliance)) {
				throw new HacException("an appliance can be installed only if has been already created");
			}

			String factoryPid = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);
			IApplianceFactory applianceFactory = this.getApplianceFactory(factoryPid);

			if (applianceFactory == null) {
				throw new HacException("unable to find a factory");
			}
			try {
				Configuration c = this.getApplianceCAConfiguration(appliancePid);
				if (c == null) {
					c = this.configAdmin.createFactoryConfiguration(factoryPid, null);

					// FIXME: la seguente riga deve essere scommentata?
					// overwrite
					// any property service.pid
					// props.remove(Constants.SERVICE_PID);
					props.put("appliance.pid", appliancePid);
					LOG.debug("created configuration for appliance.pid " + appliancePid);
				}

				// remove the ah.status properties to force appliance
				// installation
				props.remove("ah.status");
				c.update(props);

				this.installingAppliances.remove(appliance);
			} catch (Exception e) {
				LOG.debug(e.getMessage());
				throw new HacException("unable to install appliance");
			}
		}
	}
	
	public void installAppliance(String appliancePid) throws HacException {
		synchronized (lockHacService) {
			IManagedAppliance appliance = (IManagedAppliance) this.pid2appliance.get(appliancePid);
			if (appliance == null) {
				throw new HacException("an appliance can be installed only if has been already created");
			}

			if (!this.installingAppliances.contains(appliance)) {
				throw new HacException("an appliance can be installed only if has been already created");
			}

			try {
				Configuration c = this.getApplianceCAConfiguration(appliancePid);
				if (c == null) {
					throw new HacException("an appliance can be installed only if has been already created");
				}
				Dictionary props = c.getProperties();
				
				// remove the ah.status properties to force appliance
				// installation
				props.remove("ah.status");
				c.update(props);

				this.installingAppliances.remove(appliance);
			} catch (Exception e) {
				LOG.debug(e.getMessage());
				throw new HacException("unable to install appliance");
			}
		}
	}

	public void updateAppliance(String appliancePid, Dictionary props) throws HacException {
		synchronized (lockHacService) {
			IManagedAppliance managedAppliance = (IManagedAppliance) pid2appliance.get(appliancePid);
			if (managedAppliance == null)
				throw new HacException("unable to update appliance because it doesn't exist" + appliancePid);

			Configuration c;
			try {
				c = this.getApplianceCAConfiguration(appliancePid);
				if (c == null) {
					throw new HacException("unable to update appliance because it doesn't exist" + appliancePid);
				}
				this.checkAndUpdateProperties(managedAppliance, c, props);

				c.update(props);
			} catch (Exception e) {
				LOG.debug(e.getMessage());
				throw new HacException(e.getMessage());
			}
		}
	}

	public void createAppliance(String appliancePid, Dictionary props) throws HacException {
		if (appliancePid.equals(IAppliancesProxy.PROXY_APPLIANCE_PID))
			throw new IllegalArgumentException("Appliances proxy appliance cannot be created!");
		synchronized (lockHacService) {
			IManagedAppliance managedAppliance = (IManagedAppliance) pid2appliance.get(appliancePid);
			if (managedAppliance != null)
				throw new HacException("appliance " + appliancePid + " already exists");

			Configuration c;
			try {
				c = this.getApplianceCAConfiguration(appliancePid);
				if (c != null) {
					throw new HacException("appliance " + appliancePid + " already exists");
				}

				String factoryPid = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);
				if (factoryPid != null) {
					c = this.configAdmin.createFactoryConfiguration(factoryPid, null);
					props.put("appliance.pid", appliancePid);
					LOG.debug("created factory configuration for appliance.pid " + appliancePid);
				} else {
					c = this.configAdmin.getConfiguration(appliancePid);
					props.put("appliance.pid", appliancePid);
					LOG.debug("created factory configuration for appliance.pid " + appliancePid);
				}

				c.update(props);
			} catch (Exception e) {
				LOG.debug(e.getMessage());
				throw new HacException(e.getMessage());
			}
		}
	}
	//TODO: check merge, method below missing in 3.3.0
	private void manageMultiEndPointConfiguration(Dictionary props, Dictionary oldProps, String applianceProperty, String endPointsProperty) {
		String value = (String) props.get(applianceProperty);
		String[] values = (String[]) props.get(endPointsProperty);
		if (values == null) {
			values = (String[])  oldProps.get(endPointsProperty);
		}	
		if (value == null && values != null && values.length > 0) {
			// If no appliance property is present and end point 0 property is present, the appliance property is created/aligned  
			props.put(applianceProperty, values[0]);
		}	
		if (value != null && values != null && values.length > 0 && !value.equals(values[0])) {
			// If appliance property is present and end point properties are already present or updated,
			// all end point corresponding properties are reset to appliance property			
			for (int i = 0; i < values.length; i++) {
				values[i] = (String)value;				
			}
			props.put(endPointsProperty, values);
		}
	}
	
	/**
	 * Checks and adds or updates some properties contained in
	 * the configuration. The props dictionary is changed.
	 * 
	 * @param c
	 *            The Configuration Admin configuration.
	 * @param props
	 *            The new property set
	 * @param name
	 *            The name of the property of c that must not be overridden.
	 * @throws HacException
	 */

	private void checkAndUpdateProperties(IManagedAppliance managedAppliance, Configuration c, Dictionary props) throws HacException {
		// don't override the appliance type property. Fatal error if
		// this property is not set for the appliance
		Dictionary oldProps = c.getProperties();
		String applianceType = (String) oldProps.get(IAppliance.APPLIANCE_TYPE_PROPERTY);
		if (applianceType == null) {
			// FIXME: nella configurazione NON compare mai la ah.app.type
			// property!!!!! Perche?
			LOG.warn(IAppliance.APPLIANCE_TYPE_PROPERTY + " property not found in record");
		}
		
		// Restore some key properties: it seems it does not associate to new service registration properties 
		// that are not included in last change to configuration (it also avoid to have some properties
		// can contains invalid values)
		props.put(IAppliance.APPLIANCE_TYPE_PROPERTY, managedAppliance.getDescriptor().getType());
		props.put(IAppliance.APPLIANCE_PID, managedAppliance.getPid());
		props.put(IAppliance.APPLIANCE_EPS_IDS_PROPERTY, managedAppliance.getEndPointIds());
		props.put(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY, managedAppliance.getEndPointTypes());		
		props.put(IAppliance.APPLIANCE_EPS_IDS_PROPERTY, managedAppliance.getEndPointIds());
		props.put(IAppliance.APPLIANCE_EPS_TYPES_PROPERTY, managedAppliance.getEndPointTypes());		
		Dictionary customConfig = managedAppliance.getCustomConfiguration();
		if (customConfig != null) {				
			for (Enumeration e = customConfig.keys(); e.hasMoreElements();) {
				String key = (String)e.nextElement();
				// Custom properties that are invalid are filtered
				if (key.startsWith(IAppliance.APPLIANCE_CUSTOM_PROPERTIES_PREXIF));
				props.put(key, customConfig.get(key));
			}
		}	
		//TODO: check merge, 5 lines below were missing in 3.3.0
		// For compatibility with old applications (i.e. green@home), appliance common property is always managed
		manageMultiEndPointConfiguration(props, oldProps, IAppliance.APPLIANCE_NAME_PROPERTY, IAppliance.END_POINT_NAMES_PROPERTY);
		manageMultiEndPointConfiguration(props, oldProps, IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, IAppliance.END_POINT_CATEGORY_PIDS_PROPERTY);
		manageMultiEndPointConfiguration(props, oldProps, IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, IAppliance.END_POINT_LOCATION_PIDS_PROPERTY);
		manageMultiEndPointConfiguration(props, oldProps, IAppliance.APPLIANCE_ICON_PROPERTY, IAppliance.END_POINT_LOCATION_PIDS_PROPERTY);
		
	}

	private Map networkManagers = new HashMap(1);

	public void addNetworkManager(INetworkManager manager, Map properties) {
		synchronized (networkManagers) {
			String key = (String) properties.get("network.type");
			if (key == null)
				LOG.debug("addNetworkManager: received invalid network type property");
			else {
				LOG.debug("Adding network manager for " + key);
				networkManagers.put(key, manager);
			}
		}
	}

	public void removeNetworkManager(INetworkManager manager, Map properties) {
		synchronized (networkManagers) {
			String key = (String) properties.get("network.type");
			if (key == null)
				LOG.debug("removeNetworkManager: received invalid network type property");
			else {
				LOG.debug("Removing network manager for " + key);
				networkManagers.remove(key);
			}
		}
	}

	public Dictionary getManagedConfiguration(String appliancePid) {
		Configuration c = null;
		try {
			c = getApplianceCAConfiguration(appliancePid);
		} catch (Exception e) {
			LOG.error("getManagedConfiguration(" + appliancePid + ") error", e);
		}
		if (c != null)
			return c.getProperties();
		return null;
	}

	public boolean isNetworkOpen(String networkType) throws HacException {
		if (networkType == null)
			throw new HacException("isNetworkOpen: network type cannot be null");
		INetworkManager nm = (INetworkManager) networkManagers.get(networkType);
		if (nm == null)
			throw new HacException("isNetworkOpen: network type " + networkType + " not available");
		try {
			return nm.isNetworkOpen();
		} catch (Exception e) {
			String msg = "isNetworkOpen: error while opening network " + networkType;
			LOG.debug(msg, e);
			throw new HacException(msg);
		}
	}
	
	public void openNetwork(String networkType) throws HacException {
		if (networkType == null)
			throw new HacException("openNetwork: network type cannot be null");
		INetworkManager nm = (INetworkManager) networkManagers.get(networkType);
		if (nm == null)
			throw new HacException("openNetwork: network type " + networkType + " not available");
		try {
			nm.openNetwork();
		} catch (Exception e) {
			String msg = "openNetwork: error while opening network " + networkType;
			LOG.debug(msg, e);
			throw new HacException(msg);
		}
	}

	public void openNetwork(String networkType, int duration) throws HacException {
		if (networkType == null)
			throw new HacException("openNetwork: network type cannot be null");
		INetworkManager nm = (INetworkManager) networkManagers.get(networkType);
		if (nm == null)
			throw new HacException("openNetwork: network type " + networkType + " not available");
		try {
			nm.openNetwork(duration);
		} catch (Exception e) {
			String msg = "openNetwork: error while opening network " + networkType;
			LOG.debug(msg, e);
			throw new HacException(msg);
		}
	}

	public void closeNetwork(String networkType) throws HacException {
		if (networkType == null)
			throw new HacException("closeNetwork: network type cannot be null");
		INetworkManager nm = (INetworkManager) networkManagers.get(networkType);
		if (nm == null)
			throw new HacException("closeNetwork: network type " + networkType + " not available");
		try {
			nm.closeNetwork();
		} catch (Exception e) {
			String msg = "closeNetwork: error while opening network " + networkType;
			LOG.debug(msg, e);
			throw new HacException(msg);
		}
	}

	public void removeCategory(String categoryPid) throws HacException {
		synchronized (lockHacService) {
			categories.remove(categoryPid);
		}
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
