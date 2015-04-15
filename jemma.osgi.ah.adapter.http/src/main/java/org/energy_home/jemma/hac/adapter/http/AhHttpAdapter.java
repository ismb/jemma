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
package org.energy_home.jemma.hac.adapter.http;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.hac.adapter.http.contexts.UserAdminBasicHttpContext;
import org.energy_home.jemma.hac.adapter.http.impl.HacHttpContext;
import org.energy_home.jemma.hac.adapter.http.impl.SecureBasicHttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.useradmin.UserAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;

public class AhHttpAdapter implements EventHandler, HttpServletBinder, HttpImplementor {

	private static final long serialVersionUID = 1L;

	int counter = 0;

	private HttpService httpService = null;

	private String jsonServletUrl = "/post-json";
	private String jsonRpcUrl = "/jsonrpc";

	// have to be set to "UserAdminBasicHttpContext" to enable security
	private String contextType = "UserAdminBasicHttpContext";

	private BlockingQueue queue = new LinkedBlockingQueue();

	private HttpContext httpContext = null;

	private JsonRPC jsonRpcServlet;

	private UserAdmin userAdmin;

	private static final Logger LOG = LoggerFactory.getLogger( AhHttpAdapter.class );

	private CustomJsonServlet customJSONServlet;

	private HttpImplementor implementor = null;

	public AhHttpAdapter(HttpImplementor implementor) {
		this.implementor = implementor;
	}
	
	public AhHttpAdapter(HttpImplementor implementor, String alias) {
		this.implementor = implementor;
		String separator = "";
		if (!alias.endsWith("/")) {
			separator = "/";
		}
		this.jsonServletUrl = alias + separator + "post-json";
		this.jsonRpcUrl = alias + separator + "jsonrpc";
	}
	
	public void bind(HttpImplementor implementor) {
		this.implementor = implementor;
	}

	protected void activate(ComponentContext ctxt) {
		String[] topics = new String[] { "org/telecomitalia/HacEvent/*", };

		Dictionary d = new Hashtable();
		d.put(EventConstants.EVENT_TOPIC, topics);
		// d.put(EventConstants.EVENT_FILTER, "(bundle.symbolicName=com.acme.*)"
		// );
		ctxt.getBundleContext().registerService(EventHandler.class.getName(), this, d);
		Bundle bundle = null;

		bundle = ctxt.getBundleContext().getBundle();

		if (contextType.equals("SecureBasicHttpContext")) {
			SecureBasicHttpContext c = (SecureBasicHttpContext) httpContext;

			URL resourceBase = bundle.getEntry("");
			URL configFile = bundle.getEntry("jaas_config.txt");

			c.setResourceBase(resourceBase);
			c.setConfigFile(configFile);
			c.setRealm("Prova!!");
		} else if (contextType.equals("UserAdminBasicHttpContext")) {
			UserAdminBasicHttpContext c = (UserAdminBasicHttpContext) httpContext;
			c.setUserAdmin(userAdmin);
		}

		LOG.info("AhHttpAdapter activated");
	}

	protected void deactivate(ComponentContext ctxt) {
		LOG.info("AhHttpAdapter deactivated");
	}

	public synchronized void setUserAdmin(UserAdmin s) {
		userAdmin = s;
		if ((contextType.equals("UserAdminBasicHttpContext") && httpContext != null)) {
			UserAdminBasicHttpContext c = (UserAdminBasicHttpContext) httpContext;
			c.setUserAdmin(userAdmin);
		}
	}

	public synchronized void setHttpService(HttpService s) {

		httpService = s;

		if (contextType.equals("SecureBasicHttpContext")) {
			httpContext = new SecureBasicHttpContext(null, null, null);
		} else if (contextType.equals("UserAdminBasicHttpContext")) {
			httpContext = new UserAdminBasicHttpContext(null, null, null);
		} else {
			httpContext = new HacHttpContext(s.createDefaultHttpContext());
		}

		try {
			jsonRpcServlet = new JsonRPC(this, jsonRpcUrl);
			customJSONServlet = new CustomJsonServlet(this, jsonServletUrl);

			httpService.registerServlet(jsonServletUrl, customJSONServlet, null, httpContext);
			httpService.registerServlet(jsonRpcUrl, jsonRpcServlet, null, httpContext);

		} catch (ServletException e) {
			LOG.error("Servlet Exception, unable to register servlets",e);
		} catch (NamespaceException e) {
			LOG.error("NameSpace Exception, unable to register servlets",e);
		}
		LOG.debug("registered http resources");
	}

	public synchronized void unsetHttpService(HttpService s) {
		if (httpService == s) {
			try {
				httpService.unregister(jsonServletUrl);
			} catch (Exception e) {
			}

			try {
				httpService.unregister(jsonRpcUrl);
			} catch (Exception e) {
			}

			httpService = null;
		}

		LOG.debug("unregistered http resources");
	}

	public Object invokeMethod(Object targetObject, String methodName, ArrayList paramValues) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		int params = paramValues.size();

		Method[] methods = targetObject.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().compareTo(methodName) == 0) {
				Class[] paramTypes = method.getParameterTypes();
				if (paramTypes.length == params) {

					// HTTP GET params number matches method param number
					Object[] arglist = new Object[params];

					boolean signatureMach = true;
					for (int j = 0; j < params; j++) {
						Class paramT = paramTypes[j];
						String typename = paramTypes[j].getName();

						/*
						 * currently we support the following param types:
						 * 
						 * Enums, int, short, String
						 */

						String value = (String) paramValues.get(j).toString();
						if (typename.compareTo("int") == 0) {
							arglist[j] = new Integer(value);
						} else if (typename.compareTo("java.lang.String") == 0) {
							arglist[j] = value;
						} else if (typename.compareTo("short") == 0) {
							arglist[j] = new Short(value);
						} else if (typename.compareTo("boolean") == 0) {
							arglist[j] = new Boolean(value);
						} else if (typename.compareTo("long") == 0) {
							arglist[j] = new Long(value);
						} else if (typename.compareTo("[B") == 0) {
							// convert value (that is an hex string) into a byte
							// array
							byte[] v = hexStringToByteArray(value);
							arglist[j] = v;
						} else if (typename.equals("java.util.Vector")) {
							// checks if the passed value represents a JSON
							// array
							if ((value.length() >= 2) && (value.charAt(0) == '[')) {
								JSONArray outer;
								try {
									outer = new JSONArray(value);
								} catch (JSONException e) {
									signatureMach = false;
									break;
								}

								// convert the json array into a Vector
								Vector v = new Vector();

								for (int k = 0; k < outer.length(); k++) {
									try {
										v.add(outer.get(k));
									} catch (JSONException e) {
										LOG.error("Error creating JSON Object",e);
									}
								}

								if (v instanceof Vector) {
									arglist[j] = v;
								}
							}
						} else if (paramT.isInterface()) {
							Object o = getObjectByPid(value);
							if (o != null) {
								arglist[j] = o;
							} else {
								signatureMach = false;
								break;
							}
						} else {
							LOG.warn("unsupported type '" + typename + "'in target signature");
							signatureMach = false;
							break;

						}
					}

					if (signatureMach) {
						Object result = null;
						result = method.invoke(targetObject, arglist);
						return this.resultToJSON(result);
					} else {
						LOG.error("signature not found for method " + methodName);
					}
				}
			}
		}

		throw new RuntimeException("Could not find the method " + methodName + " parameters or name doesn't match");
	}

	public Object getObjectByPid(String pid) {
		return implementor.getObjectByPid(pid);
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	private String resultToJSON(Object o) {
		String out = "";

		if (o == null) {
			out = "null"; // legacy with void returning methods in python
		} else if ((o instanceof List) || (o instanceof Vector)) {
			out += "[ ";
			Iterator it = ((List) o).iterator();
			boolean first = true;

			while (it.hasNext()) {
				if (!first) {
					out += ", ";
				} else {
					first = false;
				}
				Object e = it.next();

				out += this.resultToJSON(e);
			}
			out += " ]";
		} else if (o instanceof Enumeration) {
			out += "[ ";
			boolean first = true;

			Enumeration e = (Enumeration) o;

			while (e.hasMoreElements()) {
				if (!first) {
					out += ", ";
				} else {
					first = false;
				}

				out += this.resultToJSON(e);
			}
			out += " ]";
		} else if (o instanceof Hashtable) {
			Hashtable ht = (Hashtable) o;
			Enumeration keys = ht.keys();
			boolean first = true;
			out = "{ ";
			while (keys.hasMoreElements()) {
				if (first) {
					first = false;
				} else {
					out += ", ";
				}

				Object key = keys.nextElement();
				Object value = ht.get(key);

				if (value instanceof String) {
					out += "\"" + key.toString() + "\"" + ": " + "\"" + ht.get(key).toString() + "\"";
				} else {
					out += "\"" + key.toString() + "\": " + this.resultToJSON(value);
				}
			}

			out += " }";
		} else if (o instanceof String) {
			// log.debug("traduco " + o);
			out = "\"" + o.toString() + "\"";
		} else if (o instanceof Integer) {
			out = o.toString();
		} else if (o instanceof Double) {
			out = o.toString();
		} else if (o instanceof ICategory) {
			ICategory category = (ICategory) o;
			out = "{ \"name\": \"" + category.getName() + "\", \"icon\": \"" + category.getIconName() + "\" }";
		} else if (o instanceof ILocation) {
			ILocation location = (ILocation) o;
			// log.debug("traduco " + location.getName());
			out = "{ " + "\"name\": \"" + _(location.getName()) + "\", " + "\"icon\": \"" + location.getIconName() + "\", "
					+ "\"pid\": \"" + location.getPid() + "\"" + "}";
		} else if (o instanceof IAppliance) {
			out += "\"" + ((IAppliance) o).getPid() + "\"";
		} else if (o instanceof IServiceCluster) {
			out += "\"" + ((IServiceCluster) o).getEndPoint().getAppliance().getPid() + "\"";
		} else if (o instanceof IAttributeValue) {
			IAttributeValue v = (IAttributeValue) o;
			Object value = v.getValue();
			if (value instanceof String)
				out += "{ \"type\": \"string\", \"value\": " + "\"" + value.toString() + "\" }";
			else
				out += "{ \"type\": \"double\", \"value\": " + value.toString() + " }";
		} else if (o instanceof byte[]) {
			out += "\"" + byteToHex((byte[]) o) + "\"";
		} else if (o.getClass().isArray()) {
			Object[] o1 = (Object[]) o;

			out += "[ ";
			boolean first = true;

			for (int i = 0; i < o1.length; i++) {
				if (!first) {
					out += ", ";
				} else {
					first = false;
				}
				out += this.resultToJSON(o1[i]);
			}
			out += " ]";
		} else {
			out = "\"" + o.toString() + "\"";
		}
		return out;
	}

	/**
	 * Translates the passed string
	 * 
	 * @param name
	 * @return
	 */

	public static String _(String key) {
		return key;
	}

	public void handleEvent(Event event) {
		//TODO Hardcoded event topic: we should cross-check across the project where these are stored. Is it worth centralizing event topics somewhere ? e.g. in the API ?
		if (event.getTopic() == "org/telecomitalia/HacEvent/NEW_APPLIANCE") {
			try {
				queue.put(event);
			} catch (InterruptedException e) {
				LOG.error("Interrupted put in BlockingQueue",e);
			}
		} else {
			LOG.trace("received event: " + event.getTopic());
		}
	}

	/**
	 * method to convert a byte to a hex string.
	 * 
	 * @param data
	 *            the byte to convert
	 * @return String the converted byte
	 */
	private static String byteToHex(byte[] data) {
		if (data == null) {
			return "";
		}

		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			buf.append(toHexChar((data[i] >>> 4) & 0x0F));
			buf.append(toHexChar(data[i] & 0x0F));
		}
		return buf.toString();
	}

	/**
	 * Convenience method to convert an int to a hex char.
	 * 
	 * @param i
	 *            the int to convert
	 * @return char the converted char
	 */

	public static char toHexChar(int i) {
		if ((0 <= i) && (i <= 9)) {
			return (char) ('0' + i);
		} else {
			return (char) ('a' + (i - 10));
		}
	}

	protected int lastUpdate = 0;
	protected Vector lastDevicesList = null;

	public Object getImplementor() {
		return implementor;
	}
}
