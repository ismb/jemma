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
package org.energy_home.jemma.ah.internal.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.energy_home.jemma.ah.configurator.IConfigurator;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.Category;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.Location;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

//FIXME:  We should consider refactoring this bundle/class name to something less generic
public class Configuratore implements FrameworkListener, IConfigurator {

	/**
	 * Dictionary that permits to retrieve the IManagedAppliance service from
	 * its pid
	 */
	private Hashtable pid2appliance = new Hashtable();

	/** DB for Locations */
	// private Locations locations = new Locations();

	static final int saveTimerId = 1;
	private DocumentBuilderFactory factory;
	private BundleContext bc;

	private Vector configurationsVector = new Vector();
	private Vector rules = new Vector();

	private final static String SCENARIOS_PATH = "/xml/scenarios/";

	private IHacService hacService;

	private UserAdmin ua;

	private IConnectionAdminService connAdmin;

	private Vector categories = new Vector();
	private Vector locations = new Vector();

	private static final Logger LOG = LoggerFactory.getLogger( Configuratore.class );

	protected synchronized void activate(ComponentContext ctxt) {
		this.bc = ctxt.getBundleContext();

		//TODO To check: unique location in the API bundle to centralize all properties and location codes, with documentation ?
		String defaultConfigProp = this.bc.getProperty("org.energy_home.jemma.ah.configuration.file");

		if (defaultConfigProp != null) {
			try {
				loadConfiguration(defaultConfigProp, false);
			} catch (Exception e) {
				LOG.error("exception on activate",e);
			}
		}

		this.bc.addFrameworkListener(this);
		LOG.info("Configuratore activated");
	}

	protected synchronized void deactivate(ComponentContext ctxt) {
		LOG.info("Configuratore deactivated");
	}

	public void modified(ComponentContext ctxt, Map props) {
		update(props);
	}

	protected synchronized void setDocumentBuilderFactory(DocumentBuilderFactory r) {
		factory = r;
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		factory.setCoalescing(false);
	}

	protected synchronized void unsetDocumentBuilderFactory(DocumentBuilderFactory r) {
		if (factory == r)
			factory = null;
	}

	protected synchronized void setHacService(IHacService s) {
		this.hacService = s;
	}

	protected synchronized void unsetHacService(IHacService s) {
		if (this.hacService == s)
			this.hacService = null;
	}

	protected synchronized void setConnectionAdminService(IConnectionAdminService s) {
		this.connAdmin = s;
	}

	protected synchronized void unsetConnectionAdminService(IConnectionAdminService s) {
		if (this.connAdmin == s)
			this.connAdmin = null;
	}

	/**
	 * Create the UserAdmin users and groups
	 * 
	 * @param ua
	 *            TODO
	 */

	protected Role createRole(UserAdmin ua, String name, int roleType) {
		Role role = ua.createRole(name, roleType);
		if (role == null) {
			role = ua.getRole(name);
		}
		return role;
	}

	public synchronized void setUserAdmin(UserAdmin ua) {
		this.ua = ua;
		this.installUsers();
	}

	public synchronized void unsetUserAdmin(UserAdmin ua) {
		if (this.ua == ua) {
			this.ua = null;
		}
	}

	private void setUserCredentials(User user, String password) {
		Object currentCredential = user.getProperties().get("org.energy_home.jemma.username");
		if (currentCredential == null) {
			user.getProperties().put("org.energy_home.jemma.username", user.getName().toLowerCase());
			user.getCredentials().put("org.energy_home.jemma.password", password);
		}
	}

	protected void installUsers() {
		Group administratorsGroup = (Group) this.createRole(ua, "Administrators", Role.GROUP);
		Group residentsGroup = (Group) this.createRole(ua, "Residents", Role.GROUP);
		Group membersGroup = (Group) this.createRole(ua, "Members", Role.GROUP);
		Group serviceGroup = (Group) this.createRole(ua, "Service", Role.GROUP);

		// Actions groups
		Group felixWebConsoleAccess = (Group) this.createRole(ua, "FelixWebConsoleAccess", Role.GROUP);
		Group homeEnergyPortalView = (Group) this.createRole(ua, "HomeEnergyPortalView", Role.GROUP);
		Group remoteAccess = (Group) this.createRole(ua, "RemoteAccess", Role.GROUP);
		Group softwareUpgradeAccess = (Group) this.createRole(ua, "SoftwareUpgradeAccess", Role.GROUP);
		Group homeEnergyPortalConfiguration = (Group) this.createRole(ua, "HomeEnergyPortalConfiguration", Role.GROUP);

		// Create users
		User installUser = (User) this.createRole(ua, "Install", Role.USER);
		User homeUser = (User) this.createRole(ua, "Home", Role.USER);
		User cedacUser = (User) this.createRole(ua, "Cedac", Role.USER);
		User adminUser = (User) this.createRole(ua, "Admin", Role.USER);
		User indesitUser = (User) this.createRole(ua, "Indesit", Role.USER);
		User electroluxUser = (User) this.createRole(ua, "Electrolux", Role.USER);
		User enelUser = (User) this.createRole(ua, "Enel", Role.USER);
		User telecomitaliaUser = (User) this.createRole(ua, "Telecomitalia", Role.USER);

		this.setUserCredentials(installUser, "Install");
		this.setUserCredentials(homeUser, "Home");
		this.setUserCredentials(cedacUser, "Cedac");
		this.setUserCredentials(adminUser, "Admin");
		this.setUserCredentials(indesitUser, "Indesit");
		this.setUserCredentials(electroluxUser, "Electrolux");
		this.setUserCredentials(telecomitaliaUser, "Telecomitalia");

		// Fills the Administrators group
		administratorsGroup.addMember(adminUser);

		residentsGroup.addMember(homeUser);

		membersGroup.addMember(indesitUser);
		membersGroup.addMember(electroluxUser);
		membersGroup.addMember(enelUser);
		membersGroup.addMember(telecomitaliaUser);

		felixWebConsoleAccess.addMember(administratorsGroup);
		homeEnergyPortalView.addMember(membersGroup);
		remoteAccess.addMember(administratorsGroup);
		softwareUpgradeAccess.addMember(administratorsGroup);
		serviceGroup.addMember(cedacUser);

		homeEnergyPortalConfiguration.addMember(serviceGroup);
		homeEnergyPortalConfiguration.addMember(administratorsGroup);
	}

	protected void createAppliance(String appliancePid, Dictionary props) throws ApplianceException {
		try {
			this.hacService.createAppliance(appliancePid, props);
		} catch (HacException e) {
			throw new ApplianceException(e.getMessage());
		}
	}

	public void update(Map props) {
		LOG.debug("Configuratore received configuration");
	}

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
	//FIXME general comment by Riccerdo: I'm not sure if it covers 100% of what we do here - but probably a lot of this behaviour is already standardized in the ConfigAdmin service. We should consider refactoring to use that.
	public boolean loadConfiguration(String configName, boolean storageArea) {
		File configFile;
		InputStream stream = null;

		if (configName == null) {
			configName = "defaultconfig";
			storageArea = true;
		}

		LOG.debug("trying to load configuration '" + configName + "'");

		try {
			if (storageArea) {
				String configFilename = SCENARIOS_PATH + configName + ".xml";
				configFile = bc.getDataFile(configFilename);
				LOG.debug("storage area is " + configFile);
				stream = new FileInputStream(configFile);
			} else {
				File f = new File(configName);
				if (f.isAbsolute()) {
					stream = new FileInputStream(configName);
				} else {
					String configFilename = SCENARIOS_PATH + configName + ".xml";
					//URL url = bc.getBundle().getEntry(configFilename);
					URL url = this.getClass().getResource(configFilename);
					if (url == null) {
						LOG.error("unable to open file " + configFilename);
						return false;
					}
					stream = url.openStream();
				}
			}
		} catch (FileNotFoundException e) {
			LOG.error("unable to open file " + configName,e);
			return false;
		} catch (IOException e) {
			LOG.error("unable to open file " + configName,e);
			return false;
		}

		try {
			importConfiguration(stream);
		} catch (Exception e) {
			LOG.error("Exception on importConfiguration", e);
			return false;
		}

		LOG.debug("configuration '" + configName + "' loaded successfully");
		return true;
	}

	public void updated(Dictionary props) throws ConfigurationException {
		LOG.debug("received props");
		//FIXME Note by Riccardo: why is this empty ?????
	}

	public String doc2xmlString(Document doc) {

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
			LOG.error("exception on doc2xmlString ", ioEx);
			return null;
		}
		return xmlStr;
	}

	Node lastNode;
	Hashtable props;
	Object prop;

	private boolean loadLocations = true;
	private boolean loadAppliances = true;
	private boolean loadCategories = true;

	private void traverseConfigurationTree(Node node) {
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
				
				locations.add(new Location(pid, name, icon));

			}
			if ((tag == "category") && (loadCategories)) {
				String name = attrs.getNamedItem("name").getNodeValue();
				String icon = attrs.getNamedItem("icon").getNodeValue();
				String pid = attrs.getNamedItem("pid").getNodeValue();

				categories.add(new Category(pid, name, icon));

			} else if (tag == "rule") {
				try {
					String rule = attrs.getNamedItem("filter").getNodeValue();
					rules.add(rule);
				} catch (Exception e) {
					LOG.error("Excpetion on trasversecConfigurationTree",e);
				}
			} else if (tag == "connect") {
				try {
					String pid1 = attrs.getNamedItem("pid1").getNodeValue();
					String pid2 = attrs.getNamedItem("pid2").getNodeValue();
					this.connAdmin.createConnection(pid1, pid2);
				} catch (Exception e) {
					LOG.error("Excpetion on trasversecConfigurationTree",e);
				}
			} else if ((tag == "appliance") && (loadAppliances)) {
				props = new Hashtable();
			} else if ((tag == "configuration")) {
				props = new Hashtable();
			} else if ((tag == "property") && (lastNode != null) && (props != null)) {
				// log.debug("last node is " + lastNode.getNodeName());
				String name = attrs.getNamedItem("name").getNodeValue();
				Object propValue = traversePropertyNode(lastNode);
				if (propValue == null) {
					LOG.error("null property " + name);
				} else {
					props.put(name, propValue);
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
						String appliancePid = (String) props.get(Constants.SERVICE_PID);
						if (appliancePid == null) {
							// for backward compatibility
							appliancePid = (String) props.get(IAppliance.APPLIANCE_PID);
						}

						String type = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);

						if ((appliancePid != null) && (type != null)) {
							String locationPid = (String) props.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
							String categoryPid = (String) props.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);
							createAppliance(appliancePid, props);
							// }
						} else {
							LOG.error("during reading configuration: unable to retrieve driver pid");
						}
					} catch (Exception e) {
						LOG.error("Excpetion on trasversecConfigurationTree",e);
					}
				} else if (tag.equals("configuration")) {
					// we traversed all appliances children, its time to
					// instantiate the
					// driver
					try {
						configurationsVector.add(props);
					} catch (Exception e) {
						LOG.error("Excpetion on trasversecConfigurationTree",e);
					}
				}
			}
			break;

		case Node.TEXT_NODE:
			break;
		}
	}

	//Note by Riccardo: Is this some old/unused implementation ? It seems so: commenting out - stage for future removal
	
	/*
	public void traverseConfigurationTreeOld(Node node) {
		lastNode = node;
		int nodeType = node.getNodeType();

		switch (nodeType) {
		case Node.DOCUMENT_NODE:
			traverseConfigurationTreeOld(((Document) node).getDocumentElement());
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
					this.hacService.addLocation(new Location(pid, name, icon));
				} catch (Throwable e) {
					// this is a duplicate location, skip it by putting a log
					// message
					log.warn("error while adding location found reading configuration file");
				}
			}
			if ((tag == "category") && (loadCategories)) {
				String name = attrs.getNamedItem("name").getNodeValue();
				String icon = attrs.getNamedItem("icon").getNodeValue();
				String pid = attrs.getNamedItem("pid").getNodeValue();
				
				categories.add(new Category(pid, name, icon));
			} else if (tag == "rule") {
				try {
					String rule = attrs.getNamedItem("filter").getNodeValue();
					rules.add(rule);
					// this.connAdmin.addBindRule(rule);
				} catch (Exception e) {
					log.error(e);
				}
			} else if (tag == "connect") {
				try {
					String pid1 = attrs.getNamedItem("pid1").getNodeValue();
					String pid2 = attrs.getNamedItem("pid2").getNodeValue();
					//this.connAdmin.createConnection(pid1, pid2);
				} catch (Exception e) {
					log.error(e);
				}
			} else if ((tag == "appliance") && (loadAppliances)) {
				props = new Hashtable();
			} else if ((tag == "property") && (lastNode != null) && (props != null)) {
				// log.debug("last node is " + lastNode.getNodeName());
				String name = attrs.getNamedItem("name").getNodeValue();
				Object propValue = traversePropertyNode(lastNode);
				if (propValue == null) {
					log.error("null property " + name);
				} else {
					props.put(name, propValue);
				}
			}
			NodeList children = node.getChildNodes();

			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++) {
					traverseConfigurationTreeOld(children.item(i));
				}
				if (tag.equals("appliance")) {
					// we traversed all appliances children, its time to
					// instantiate the
					// driver
					try {
						String appliancePid = (String) props.get(Constants.SERVICE_PID);
						if (appliancePid == null) {
							// for backward compatibility
							appliancePid = (String) props.get(IAppliance.APPLIANCE_PID);
						}

						String type = (String) props.get(IAppliance.APPLIANCE_TYPE_PROPERTY);

						if ((appliancePid != null) && (type != null)) {
							String locationPid = (String) props.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY);
							String categoryPid = (String) props.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY);
							createAppliance(appliancePid, props);
							// }
						} else {
							log.error("during reading configuration: unable to retrieve driver pid");
						}
					} catch (Exception e) {
						log.debug(e.getMessage());
					}
				}
			}
			break;

		case Node.TEXT_NODE:
			break;
		}
	}

	*/
	
	boolean isArray = false;
	String arrayType = null;

	private Object getInstance(String type, String value) {
		if ((type == null) || ((type != null) && (type.equals("String")))) {
			return value;
		} else if (type.equals("Integer")) {
			return Integer.valueOf(value);
		} else if (type.equals("Boolean")) {
			return Boolean.valueOf(value);
		} else if (type.equals("Double")) {
			return Double.valueOf(value);
		} else if (type.equals("Short")) {
			return Short.valueOf(value);
		} else if (type.equals("Long")) {
			return Long.valueOf(value);
		} else if (type.equals("Float")) {
			return Float.valueOf(value);
		} else if (type.equals("Character")) {
			return Character.valueOf(value.charAt(0));
		} else if (type.equals("int")) {
			return Integer.parseInt(value);
		} else if (type.equals("short")) {
			return Short.parseShort(value);
		} else if (type.equals("boolean")) {
			return Boolean.parseBoolean(value);
		} else if (type.equals("double")) {
			return Double.parseDouble(value);
		} else if (type.equals("long")) {
			return Long.parseLong(value);
		} else if (type.equals("float")) {
			return Float.parseFloat(value);
		} else if (type.equals("char")) {
			return value.charAt(0);
		}

		return value;
	}

	private Object[] getArrayInstance(String type, int size) {
		if ((type == null) || ((type != null) && (type.equals("String")))) {
			return new String[size];
		} else if (type.equals("Integer")) {
			return new Integer[size];
		} else if (type.equals("Boolean")) {
			return new Boolean[size];
		} else if (type.equals("Double")) {
			return new Double[size];
		} else if (type.equals("Short")) {
			return new Short[size];
		} else if (type.equals("Long")) {
			return new Long[size];
		} else if (type.equals("Float")) {
			return new Float[size];
		} else if (type.equals("Character")) {
			return new Character[size];
		}
		return new String[size];
	}

	private Object getBaseTypeArrayInstance(String type, int size) {
		if (type.equals("int")) {
			return new int[size];
		} else if (type.equals("boolean")) {
			return new boolean[size];
		} else if (type.equals("double")) {
			return new double[size];
		} else if (type.equals("short")) {
			return new short[size];
		} else if (type.equals("long")) {
			return new long[size];
		} else if (type.equals("float")) {
			return new float[size];
		} else if (type.equals("char")) {
			return new char[size];
		} else
			return null;

	}

	private Object fillBaseTypeArrayInstance(String type, Vector container) {
		Object o = getBaseTypeArrayInstance(type, container.size());
		for (int i = 0; i < container.size(); i++) {
			// int[] a = new int[5];
			if (type.equals("int")) {
				int[] o1 = (int[]) o;
				o1[i] = (Integer) container.get(i);
				return (Object) o1;
			} else if (type.equals("boolean")) {
				boolean[] o1 = (boolean[]) o;
				o1[i] = (Boolean) container.get(i);
				return (Object) o1;
			} else if (type.equals("double")) {
				double[] o1 = (double[]) o;
				o1[i] = (Double) container.get(i);
				return (Object) o1;
			} else if (type.equals("short")) {
				short[] o1 = (short[]) o;
				o1[i] = (Short) container.get(i);
				return (Object) o1;
			} else if (type.equals("long")) {
				long[] o1 = (long[]) o;
				o1[i] = (Long) container.get(i);
				return (Object) o1;
			} else if (type.equals("float")) {
				float[] o1 = (float[]) o;
				o1[i] = (Float) container.get(i);
				return (Object) o1;
			} else if (type.equals("char")) {
				char[] o1 = (char[]) o;
				o1[i] = (Character) container.get(i);
				return (Object) o1;
			}
		}
		return null;
	}

	private Object traverseItemNode(Node node) {
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
			if (tag.equals("item")) {
				return this.getInstance(arrayType, getTextContent(node));
			}
		}
		return null;
	}

	public Object traversePropertyNode(Node node) {
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

			if (tag.equals("property")) {
				if (type.endsWith("[]")) {
					isArray = true;
					arrayType = type.substring(0, type.length() - 2);
				} else {
					isArray = false;
					arrayType = null;
				}

				if (type.equals("Vector")) {
					NodeList children = node.getChildNodes();
					int len = children.getLength();
					Vector container = new Vector();
					for (int i = 0; i < len; i++) {
						Object res = traverseItemNode(children.item(i));

						if (res != null) {
							container.add(res);
							LOG.trace("added to vector/array " + res.toString());
						}
					}

					return container;

				} else if (isArray) {
					NodeList children = node.getChildNodes();
					int len = children.getLength();
					Vector container = new Vector();
					for (int i = 0; i < len; i++) {
						Object res = traverseItemNode(children.item(i));
						if (res != null) {
							container.add(res);
							LOG.trace("added to vector/array " + res.toString());
						}
					}

					if (isBaseType(this.arrayType)) {
						return fillBaseTypeArrayInstance(arrayType, container);

					} else {
						return container.toArray(getArrayInstance(arrayType, 0));
					}
				} else {
					return this.getInstance(type, getTextContent(node));
				}
			}
		}

		return null;
	}

	private boolean isBaseType(String type) {
		if (type.equals("int")) {
			return true;
		} else if (type.equals("boolean")) {
			return true;
		} else if (type.equals("double")) {
			return true;
		} else if (type.equals("short")) {
			return true;
		} else if (type.equals("long")) {
			return true;
		} else if (type.equals("float")) {
			return true;
		} else if (type.equals("char")) {
			return true;
		} else

			return false;
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

	public void frameworkEvent(FrameworkEvent fe) {
		if (fe.getType() == FrameworkEvent.STARTED) {
		}
		//FIXME why is this function here ? Just to log Framework event messages ?
		LOG.debug("FrameworkEvent:"+fe.toString() + " type " + fe.getType());
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

	public Vector listConfigurations(boolean builtIn) {
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

		if (level == 0) {
			try {

				File configFilesDirectory = bc.getDataFile(SCENARIOS_PATH);
				LOG.debug("deleting directory " + configFilesDirectory.getPath());
				if (configFilesDirectory.isDirectory()) {
					return deleteDirectory(configFilesDirectory);
				}
			} catch (Exception e) {
				LOG.error("during reset exception contains '" + e.getMessage() + "'",e);
				return false;
			}
			this.hacService.clean();

		} else if (level == 1) {
			try {
				int time = 4;
				LOG.info("shutdown in " + (time * 60) + " seconds");
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
				LOG.error("exception during shutdown " + e.getMessage(),e);
			}
		} else if (level == 2) {
			this.hacService.clean();
		}

		// reset successful
		return true;
	}

	static protected boolean deleteDirectory(File path) {
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
			LOG.error("Exception on createDoc",e);
			return null;
		}

		Document doc = docBuilder.newDocument();
		return doc;
	}

	/****** IAppliancesManager exposed services ******/

	public synchronized void updateConfiguration(String pid, Dictionary props) throws HacException {
		IManagedAppliance managedAppliance = (IManagedAppliance) pid2appliance.get(pid);
		if (managedAppliance == null)
			throw new HacException("Auhtorization error: invalid appliance pid");

		// FIXME: agganciare al configuration admin Service
		// saveConfigurationDelayed();
	}

	public void loadConfiguration(String filename) {
		this.loadConfiguration(filename, false);
	}

	public void importConfiguration(InputStream inputStream) throws Exception {
		// Ottiene un riferimento al servizio Configuration Admin
		ServiceReference sr = bc.getServiceReference(ConfigurationAdmin.class.getName());
		ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bc.getService(sr);

		configurationsVector.clear();
		rules.clear();
		categories.clear();

		// Esegue il parsing del contenuto XML dentro inputStream
		factory.setNamespaceAware(true);
		factory.setValidating(false);

		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(new InputSource(inputStream));

		// Mette nella variabile configurationVector le configurazioni lette dal
		// documento XML...
		traverseConfigurationTree(doc);

		// Itera su ogni configurazione contenuta nel vettore
		// configurationsVector

		// clean the current A@H configuration
		this.connAdmin.deleteAllRules();
		
		ICategory[] oldCategories = hacService.getCategories();
		if (oldCategories != null) {
			for (int i = 0; i < oldCategories.length; i++) {
				ICategory category = oldCategories[i];
				hacService.removeCategory(category.getPid());
			}
		}
		
		for (Iterator iterator = categories.iterator(); iterator.hasNext();) {
			ICategory category = (ICategory) iterator.next();
			hacService.addCategory(category);
		}

				
		Configuration[] configurations = configurationAdmin.listConfigurations("(" + ConfigurationAdmin.SERVICE_FACTORYPID + "=" + "org.energy_home.jemma.osgi.ah.hac.locations" + ")" );
		if (configurations != null) {
			for (int i = 0; i < configurations.length; i++) {
				configurations[i].delete();
			}
		}
		
		for (Iterator iterator = locations.iterator(); iterator.hasNext();) {
			Location location = (Location) iterator.next();
			hacService.addLocation(location);
		}

		for (Iterator iterator = configurationsVector.iterator(); iterator.hasNext();) {
			Configuration c;

			// Ottiene le proprieta' dalla configurazione corrente
			Hashtable props = (Hashtable) iterator.next();

			// Se serviceFactoryPid esiste, allora la configurazione e' di tipo
			// Factory...
			String servicePid = (String) props.get(Constants.SERVICE_PID);
			String serviceFactoryPid = (String) props.get(ConfigurationAdmin.SERVICE_FACTORYPID);

			if (serviceFactoryPid != null) {
				String appliancePid = (String) props.get("appliance.pid");
				if (appliancePid != null) {
					// this is an appliance. We need to handle appliances in a
					// special way.
					Configuration[] applianceConfigs = configurationAdmin
							.listConfigurations("(appliance.pid=" + appliancePid + ")");

					if ((applianceConfigs != null) && (applianceConfigs.length > 1)) {
						LOG.warn("appliance.pid " + appliancePid + " has been found more than once in the configuation admin");
					}

					if (applianceConfigs == null) {
						c = configurationAdmin.createFactoryConfiguration(serviceFactoryPid, null);
					} else {
						c = applianceConfigs[0];
					}
				} else {
					c = configurationAdmin.createFactoryConfiguration(serviceFactoryPid, null);
				}
				c.update(props);
			} else if (servicePid != null) {
				// singleton configuration
				c = configurationAdmin.getConfiguration(servicePid, null);
				c.update(props);
			}
		}
		
		for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
			String rule = (String) iterator.next();
			connAdmin.addBindRule(rule);
		}		
	}

	private void test() throws Exception {
		ServiceReference sr = bc.getServiceReference(ConfigurationAdmin.class.getName());
		ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bc.getService(sr);

		// Array
		Hashtable testArrayConf = new Hashtable();
		testArrayConf.put("int", new int[] { 1, 2 });
		testArrayConf.put("String", new String[] { "1", "2" });
		testArrayConf.put("Integer", new Integer[] { new Integer(1), new Integer(2) });
		testArrayConf.put("Boolean", new Boolean[] { new Boolean(true), new Boolean(false) });
		testArrayConf.put("Double", new Double[] { new Double(1), new Double(2) });
		testArrayConf.put("Short", new Short[] { new Short((short) 1), new Short((short) 2) });
		testArrayConf.put("Long", new Long[] { new Long(1), new Long(2) });
		testArrayConf.put("Float", new Float[] { new Float(1), new Float(2) });
		testArrayConf.put("Character", new Character[] { new Character('a'), new Character('b') });

		Configuration c = configurationAdmin.getConfiguration("testArray");
		c.update(testArrayConf);
	}

	public static String xmlContentEscape(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;

			case '\000':
			case '\001':
			case '\002':
			case '\003':
			case '\004':
			case '\005':
			case '\006':
			case '\007':
			case '\010':
			case '\013':
			case '\014':
			case '\016':
			case '\017':
			case '\020':
			case '\021':
			case '\022':
			case '\023':
			case '\024':
			case '\025':
			case '\026':
			case '\027':
			case '\030':
			case '\031':
			case '\032':
			case '\033':
			case '\034':
			case '\035':
			case '\036':
			case '\037':
				// do nothing, these are disallowed characters
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public void exportConfiguration(OutputStream os) throws Exception {
		// Ottiene un riferimento al servizio Configuration Admin
		ServiceReference sr = bc.getServiceReference(ConfigurationAdmin.class.getName());
		ConfigurationAdmin configurationAdmin = (ConfigurationAdmin) bc.getService(sr);

		// test();

		// Ottiene un array contenente tutte le configurazioni salvate nel
		// Configuration Admin
		Configuration[] configs = configurationAdmin.listConfigurations(null);

		// Stampa nello stream di output il file XML
		PrintWriter pw = new PrintWriter(os);
		pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		pw.println("<configurations>");

		// Export delle categories
		if (hacService != null) {
			ICategory[] categories = hacService.getCategories();
			if (categories != null) {
				pw.println("<categories>");
				for (int i = 0; i < categories.length; i++) {
					ICategory c = categories[i];
					pw.println("<category icon=\"" + c.getIconName() + "\" name = \"" + c.getName() + "\" pid = \"" + c.getPid()
							+ "\"/>");
				}
				pw.println("</categories>");
			}
		}

		// Export delle rules
		if (connAdmin != null) {
			ArrayList rules = connAdmin.getBindRules();
			if (rules != null) {
				pw.println("<rules>");
				for (int i = 0; i < rules.size(); i++) {
					Filter f = (Filter) rules.get(i);
					pw.println("<rule filter =\"" + this.xmlContentEscape(f.toString()) + "\"/>");
				}
				pw.println("</rules>");
			}
		}

		// Export delle configurazioni
		if (configs != null && configs.length > 0) {
			Set factories = new HashSet();
			SortedMap sm = new TreeMap();
			for (int i = 0; i < configs.length; i++) {
				sm.put(configs[i].getPid(), configs[i]);
				String fpid = configs[i].getFactoryPid();
				if (null != fpid) {
					factories.add(fpid);
				}
			}

			for (Iterator mi = sm.values().iterator(); mi.hasNext();) {
				Configuration config = (Configuration) mi.next();
				pw.println("<configuration>");

				// Emette una ad una le proprieta' della configurazione
				Dictionary props = config.getProperties();
				if (props != null) {
					SortedSet keys = new TreeSet();
					for (Enumeration ke = props.keys(); ke.hasMoreElements();)
						keys.add(ke.nextElement());
					for (Iterator ki = keys.iterator(); ki.hasNext();) {
						String key = (String) ki.next();

						pw.print("<property type=\"" + props.get(key).getClass().getSimpleName() + "\" name=\"" + key + "\">");

						if (props.get(key).getClass().isArray() == true) {
							pw.println();
							Object value = props.get(key);
							int len = Array.getLength(value);
							for (int i = 0; i < len; i++) {
								Object element = Array.get(value, i);
								pw.print("<item>" + element.toString() + "</item>");
							}
						} else
							pw.print(props.get(key));

						pw.println("</property>");
					}
				}
				pw.println("</configuration>");
			}
		}
		pw.println("</configurations>");
		pw.flush();
	}
}
