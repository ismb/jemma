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
package org.energy_home.jemma.osgi.ah.felix.console.web;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.hac.lib.ext.TextConverter;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HacWebCommandProvider extends org.apache.felix.webconsole.AbstractWebConsolePlugin {
	private static final long serialVersionUID = -7727225969825874601L;

	private static final Log log = LogFactory.getLog(HacWebCommandProvider.class);
	
	private static final int RESULT_MAX_LINE_LENGHT = 100;
	
	private static final String PROXY_APPLIANCE_PID = "ah.app.proxy";
	
	private static final String GET_ATTRIBUTE_SUBSCRIPTION_METHOD = "getAttributeSubscription";
	private static final String SET_ATTRIBUTE_SUBSCRIPTION_METHOD = "setAttributeSubscription";
//	private static SimpleDateFormat dateFormat = new SimpleDateFormat();
//	static {
//		dateFormat.applyPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
//	}
	
	public static final String NAME = "AH";
	public static final String LABEL = "ah";
	
	private static String invokeClusterMethod(IAppliancesProxy proxy, String appliancePid, Integer endPointId, String clusterName, String methodName, String[] params) {
		try {
			Object[] objectParams  = TextConverter.getObjectParameters(Class.forName(clusterName), methodName, params, proxy.getRequestContext(true)); 
		    return TextConverter.getTextRepresentation(proxy.invokeClusterMethod(appliancePid, endPointId, clusterName, methodName, objectParams));
		} catch (Throwable t) {
			t.printStackTrace();
			return TextConverter.getTextRepresentation(new Exception(t));
		}
	}
	
	private IAppliancesProxy appliancesProxy;

	private INetworkManager zbNetworkManager;
	
	private boolean openZigBeeNetwork() {
		if (zbNetworkManager == null)
			return false;
		try {
			zbNetworkManager.openNetwork();
			return true;
		} catch (Exception e) {
			log.error("Error while opening zigbee network", e);
			return false;
		}
	}
	
	private boolean closeZigBeeNetwork() {
		if (zbNetworkManager == null)
			return false;
		try {
			zbNetworkManager.closeNetwork();
			return true;
		} catch (Exception e) {
			log.error("Error while closing zigbee network", e);
			return false;
		}
	}
	
	public String getLabel() {
		return LABEL;
	}

	public String getTitle() {
		return NAME;
	}
	
	public void setAppliancesProxy(IAppliancesProxy s) {
		this.appliancesProxy = s;
	}

	public void unsetAppliancesProxy(IAppliancesProxy s) {
		if (this.appliancesProxy == s) {
			this.appliancesProxy = null;
		}
	}
	
	public void addNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			log.error("addNetworkManager: received invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}

	public void removeNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null)
			log.error("removeNetworkManager: received invalid network type property");
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}
	
	
	protected void renderContent(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter pw = response.getWriter();
		if (appliancesProxy == null)
			return;

		String appRoot = req.getContextPath() + req.getServletPath();
		String pathInfo = req.getPathInfo();
		pathInfo = pathInfo.substring(LABEL.length() + 1);
		StringTokenizer st = null;
		String appliancePid;
		try {
			if (pathInfo.startsWith("/confirm")) {
				st = new StringTokenizer(pathInfo, "/");
				if (st.hasMoreTokens())
					st.nextToken();
				String confirmParam = null;
				if (st.hasMoreTokens())
					confirmParam = st.nextToken();
				appliancePid = null;
				if (st.hasMoreTokens())		
					appliancePid = st.nextToken();
				if (confirmParam.equals("delete"))
					pw.println("</br></br><b><font color=\"red\">Are you sure you want to delete appliance " + appliancePid + 
							"?&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/delete/" + appliancePid + "\">yes</a>&nbsp;&nbsp;&nbsp;" +
							"<a href=\"" + appRoot + "/" + LABEL + "\">no</a>");
			} else if (pathInfo == null || pathInfo.equals("") || pathInfo.equals("/") 
					|| pathInfo.startsWith("/install") || pathInfo.startsWith("/delete")) {
				List v = null;
				boolean installing = false;
				boolean deleteStatus = true;
				boolean closeStatus = true;
				boolean openStatus = true;
				boolean installStatus = true;
				if (pathInfo.startsWith("/delete")) {
					st = new StringTokenizer(pathInfo, "/");
					if (st.hasMoreTokens())
						st.nextToken();
					appliancePid = null;
					if (st.hasMoreTokens())		
						appliancePid = st.nextToken();
					deleteStatus = appliancesProxy.deleteAppliance(appliancePid);
				}
				if (pathInfo.startsWith("/install")) {
					installing = true;
					String installParam = null;
					st = new StringTokenizer(pathInfo, "/");
					if (st.hasMoreTokens())
						st.nextToken();
					if (st.hasMoreTokens())		
						installParam = st.nextToken();
					if (installParam != null) {
						if (installParam.equals("open"))
							openStatus = openZigBeeNetwork();
						else if (installParam.equals("close"))
							closeStatus = closeZigBeeNetwork();
						else
							installStatus = appliancesProxy.installAppliance(installParam);
					} 
					v = appliancesProxy.getInstallingAppliancePids();
					pw.println("<br/><b><u>INSTALLING APPLIANCES</u>&nbsp;&nbsp;&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "/install\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "\">Go to installed appliances</a>)</b></br>");
					pw.println("<br/><b>[<a href=\"" + appRoot + "/" + LABEL + "/install/open\">Open network</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install/close\">Close network</a>]</b>");
					if (!openStatus)
						pw.println("<b><font color=\"red\">Some problem occurred while opening network</font></b>");
					if (!closeStatus)
						pw.println("<b><font color=\"red\">Some problem occurred while closing network</font></b>");
					if (!installStatus)
						pw.println("<b><font color=\"red\">Some problem occurred while installing appliance " + installParam + "</font></b>");
					pw.println("</br>");
				} else {
					v = appliancesProxy.getAppliancePids();
					v.add(PROXY_APPLIANCE_PID);			
					pw.println("<br/><b><u>INSTALLED APPLIANCES</u>&nbsp;&nbsp;&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install\">Go to installing appliances</a>)</b></br>");
				}
				if (v != null) {
					appliancePid = null;
					IAppliance appliance = null;
					for (Iterator iterator = v.iterator(); iterator.hasNext();) {
						appliancePid = (String) iterator.next();
						if (installing)
							appliance = appliancesProxy.getInstallingAppliance(appliancePid);
						else 
							appliance = appliancesProxy.getAppliance(appliancePid);
						if (appliance != null) {
							IEndPoint[] endPoints = appliance.getEndPoints();
							if (endPoints != null && endPoints.length > 1) {
								pw.println("<hr>");
								if (appliance.isAvailable()) {
									pw.println((appliance.isDriver() ? "<b>DRIVER APPLIANCE: " : "<b>VIRTUAL APPLIANCE: ") + 
											"PID=" + appliance.getPid() + ", TYPE=" + appliance.getDescriptor().getType() + 
											((appliance.isSingleton()) ? "</b><br/><br/>" : 
											(appliancesProxy.getApplianceConfiguration(appliancePid, new Integer(0)) != null ? " (<a href=\"" + appRoot + "/" + LABEL + "/config/"+ appliance.getPid() + "\">configuration</a>" : "") + 
											 	(installing ? "&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install/"+ appliance.getPid() + "\">install</a>" : "") + 
										 			"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/confirm/delete/"+ appliance.getPid() + "\">delete</a>)</font></b><br/><br/>" ));						
								} else {
									pw.println((appliance.isDriver() ? "<b><font color=\"gray\">DRIVER APPLIANCE: " : "<b><font color=\"gray\">VIRTUAL APPLIANCE: ") + 
											"PID=" + appliance.getPid() + ", TYPE=" + appliance.getDescriptor().getType() + 
											((appliance.isSingleton()) ? "</b><br/><br/>" :  
											(appliancesProxy.getApplianceConfiguration(appliancePid, new Integer(0)) != null ? " (<a href=\"" + appRoot + "/" + LABEL + "/config/"+ appliance.getPid() + "\">configuration</a>" : "") + 
											 		(installing ? "&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install/"+ appliance.getPid() + "\">install</a>" :  "") +
											 			"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/confirm/delete/"+ appliance.getPid() + "\">delete</a>)</font></b><br/><br/>" ));
								}
								renderEndPoints(appRoot, endPoints, pw);
							}						
						}
					}
				}
			} else if (pathInfo.startsWith("/config")) {
				st = new StringTokenizer(pathInfo, "/");
				st.nextToken();
				appliancePid = st.nextToken();
				String[] params = req.getParameterValues("param");
				Map config = appliancesProxy.getApplianceConfiguration(appliancePid, new Integer(0));
				if (params != null && params.length == 3) {
					if (config != null) {
						config.put(IAppliance.APPLIANCE_NAME_PROPERTY, params[0]);
						config.put(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY, params[1]);
						config.put(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY, params[2]);
						appliancesProxy.updateApplianceConfiguration(appliancePid, new Integer(0), config);
					}		
				} 
				renderApplianceConfiguration(appRoot, appliancePid, (String)config.get(IAppliance.APPLIANCE_NAME_PROPERTY), 
						(String)config.get(IAppliance.APPLIANCE_CATEGORY_PID_PROPERTY), (String)config.get(IAppliance.APPLIANCE_LOCATION_PID_PROPERTY), pw);
			} else {
				st = new StringTokenizer(pathInfo, "/");
				int i = 0;
				appliancePid = null;
				int endPointId = 0;
				String clusterName = null;
				String methodName = null;
				while (st.hasMoreElements()) {
					switch (i) {
					case 0:
						appliancePid = st.nextToken();
						break;
					case 1:
						endPointId = Integer.parseInt(st.nextToken());
						break;
					case 2:
						clusterName = st.nextToken();
						break;
					case 3:
						methodName = st.nextToken();
						break;
					default:
						break;
					}
					i++;
				}
				String[] params = req.getParameterValues("param");
				renderClusterCommands(appRoot, appliancePid, endPointId, clusterName, methodName, params, pw);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void renderApplianceConfiguration(String appRoot, String appliancePid, String name, String categoryPid, String locationPid, PrintWriter pw) {
		IAppliance appliance = appliancesProxy.getAppliance(appliancePid);
		if (appliance == null)
			appliance = appliancesProxy.getInstallingAppliance(appliancePid);
		pw.println((appliance.isDriver() ? "<b><u><br/>DRIVER"  : "<b><u><br/>VIRTUAL") + " APPLIANCE CONFIGURATION: </u>" +
				" (<a href=\"" + appRoot + "/" + LABEL + "/config/" + appliancePid + "\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "\">Go to installed appliances</a>" +
				"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install\">Go to installing appliances</a>)</br>" +
				"<br/>PID=" + appliance.getPid() + "<br/>TYPE=" + appliance.getDescriptor().getType() + "</b><br/><hr/>");
		pw.println("<br/><form name=\"ApplianceConfig\"" + " action=\"" + appRoot + "/" + LABEL + "/config/"
				+ appliancePid + "\" method=\"get\">");
		pw.println("<table id=\"ApplianceConfig\" class=\"nicetable\"><tbody>");
		pw.println("<tr><td width=\"50%\"><b>Configuration </b></td><td><input type=\"submit\" value=\"update\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">Name: </td><td><input type=\"text\" name=\"param\" size=\"30\" value=\"" + name + "\"/></td></tr>");
		
		pw.println("</td></tr>");
		pw.println("<tr><td width=\"50%\">Category: </td><td><select name=\"param\">");
		ICategory[] categories = appliancesProxy.getCategories();
		String pid = null;
		for (int i = 0; i < categories.length; i++) {
			pid = categories[i].getPid();
			pw.println("<option value=\"" + pid + (pid.equals(categoryPid) ? "\" selected=\"selected\">" : "\" >") + categories[i].getName() + "</option>");
		}
		pw.println("</td></tr>");
		
		pw.println("<tr><td width=\"50%\">Location: </td><td><select name=\"param\">");
		ILocation[] locations = appliancesProxy.getLocations();
		pid = null;
		for (int i = 0; i < locations.length; i++) {
			pid = locations[i].getPid();
			pw.println("<option value=\"" + pid + (pid.equals(locationPid) ? "\" selected=\"selected\">" : "\" >") + locations[i].getName() + "</option>");
		}		
		pw.println("</tbody></table></form>");
	}
	
	private void renderGetSubscriptionCommand(String appRoot, String appliancePid, int endPointId, String clusterName, String methodName,
			String result, PrintWriter pw) {
		pw.println("<br/><form name=\"" + GET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\"" + " action=\"" + appRoot + "/" + LABEL + "/"
				+ appliancePid + "/" + endPointId + "/" + clusterName + "/" + GET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\" method=\"get\">");
		pw.println("<table id=\"" + GET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\" class=\"nicetable\"><tbody>");
		pw.println("<tr><td width=\"50%\"><b>" + GET_ATTRIBUTE_SUBSCRIPTION_METHOD + "</b></td><td><input type=\"submit\" value=\"invoke\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">AttributeName (String):</td><td><input type=\"text\" name=\"param\" size=\"100\"/></td></tr>");
		if (methodName != null && GET_ATTRIBUTE_SUBSCRIPTION_METHOD.equals(methodName)) {
			pw.println("<tr><td><b><font color=\"red\">Result (ISubscriptionParameters):</font></b></td><td><b><font color=\"red\">" + result + "</font></b></td></tr>");
		} else
			pw.println("<tr><td>Result (ISubscriptionParameters):</td><td>&nbsp;</td></tr>");
		pw.println("</tbody></table></form>");
	}
	
	private void renderSetSubscriptionCommand(String appRoot, String appliancePid, int endPointId, String clusterName, String methodName,
			String result, PrintWriter pw) {
		pw.println("<br/><form name=\"" + SET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\"" + " action=\"" + appRoot + "/" + LABEL + "/"
				+ appliancePid + "/" + endPointId + "/" + clusterName + "/" + SET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\" method=\"get\">");
		pw.println("<table id=\"" + SET_ATTRIBUTE_SUBSCRIPTION_METHOD + "\" class=\"nicetable\"><tbody>");
		pw.println("<tr><td width=\"50%\"><b>" + SET_ATTRIBUTE_SUBSCRIPTION_METHOD + "</b></td><td><input type=\"submit\" value=\"invoke\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">AttributeName (String):</td><td><input type=\"text\" name=\"param\" size=\"100\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">MinReportingInterval (Long):</td><td><input type=\"text\" name=\"param\" size=\"100\" value=\"10\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">ManReportingInterval (Long):</td><td><input type=\"text\" name=\"param\" size=\"100\" value=\"10\"/></td></tr>");
		pw.println("<tr><td width=\"50%\">ReportableChange (Double):</td><td><input type=\"text\" name=\"param\" size=\"100\" value=\"0\"/></td></tr>");
		if (methodName != null && SET_ATTRIBUTE_SUBSCRIPTION_METHOD.equals(methodName)) {
			pw.println("<tr><td><b><font color=\"red\">Result (ISubscriptionParameters):</font></b></td><td><b><font color=\"red\">" + result + "</font></b></td></tr>");
		} else
			pw.println("<tr><td>Result (ISubscriptionParameters):</td><td>&nbsp;</td></tr>");
		pw.println("</tbody></table></form>");
	}
	
	private static boolean isEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}
	
	private String formatResult (String result) {
		if (result.length() > RESULT_MAX_LINE_LENGHT) {
			StringBuilder sb = new StringBuilder("");
			int startIndex = 0;
			int endIndex = RESULT_MAX_LINE_LENGHT-1;
			while (startIndex < result.length()) {
				sb.append(result.substring(startIndex, endIndex));	
				startIndex = endIndex+1;
				endIndex = Math.min(endIndex+RESULT_MAX_LINE_LENGHT, result.length()-1);
				sb.append("<BR/>");
			}
			return sb.toString();
		} else {
			return result;			
		}
	}
	
	private void renderClusterCommands(String appRoot, String appliancePid, int endPointId, String clusterName, String methodName,
			String[] params, PrintWriter pw) throws SecurityException, InstantiationException, NoSuchFieldException, IllegalAccessException {
		IAppliance appliance = appliancesProxy.getAppliance(appliancePid);
		if (appliance == null)
			appliance = appliancesProxy.getInstallingAppliance(appliancePid);
		pw.println((appliance.isDriver() ? "<br><b><u>DRIVER APPLIANCE CLUSTER " : "<br><b><u>VIRTUAL APPLIANCE CLUSTER ") + 
				" </u>(<a <a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid + "/" + endPointId + "/" + clusterName + "\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "\">Go to installed appliances</a>" +
				"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install\">Go to installing appliances</a>)</br>" +
				"<br/>APPLIANCE PID=" + appliance.getPid() + "<br/>END POINT ID=" +  endPointId +  "<br/>CLUSTER NAME=" + clusterName + "</b><br/>");		
		String result = null;
		long timestamp = System.currentTimeMillis();
		try {
			ISubscriptionParameters sp = null;
			if (methodName != null && methodName.equals(GET_ATTRIBUTE_SUBSCRIPTION_METHOD)) {
				sp = appliancesProxy.getAttributeSubscription(appliancePid, endPointId, clusterName, params[0]);
				result = TextConverter.getTextRepresentation(sp);
			} else if (methodName != null && methodName.equals(SET_ATTRIBUTE_SUBSCRIPTION_METHOD)) {
				if (!isEmpty(params[1]) || !isEmpty(params[2]) || !isEmpty(params[3])) {
					long minReportingInterval = isEmpty(params[1]) ? 0 : Long.parseLong(params[1]);
					long maxReportingInterval = isEmpty(params[2]) ? 0 : Long.parseLong(params[2]);
					double reportableChange = isEmpty(params[3]) ? 0 : Double.parseDouble(params[3]);
					sp = new SubscriptionParameters(minReportingInterval, 
						maxReportingInterval, reportableChange);
				}
				result = TextConverter.getTextRepresentation(appliancesProxy.setAttributeSubscription(appliancePid, endPointId, clusterName, params[0], sp));			
			} else if (methodName != null) {
				result = invokeClusterMethod(appliancesProxy, appliancePid, new Integer(endPointId), clusterName, methodName, params);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = "ERROR: " + e.getMessage();
		}
		
		Method[] methods;

		try { 
			pw.print("<br/><br/><hr/><b><i>ATTRIBUTE SUBSCRIPTIONS:</i></b><hr/>");
			renderGetSubscriptionCommand(appRoot, appliancePid, endPointId, clusterName, methodName, result, pw);
			renderSetSubscriptionCommand(appRoot, appliancePid, endPointId, clusterName, methodName, result, pw);
			Map lastNotifiedAttributeValues = appliancesProxy.getLastNotifiedAttributeValues(appliancePid, endPointId, clusterName);
			if (lastNotifiedAttributeValues != null && lastNotifiedAttributeValues.size() > 0) {
				pw.println("<br/><table id=\"lastNotifiedAttributeValues\" class=\"nicetable\"><tbody>");
				pw.println("<tr><td width=\"50%\"><b>Last notified attribute values:</b></td><td>&nbsp;</td></tr>");
				for (Iterator iterator = lastNotifiedAttributeValues.entrySet().iterator(); iterator.hasNext();) {
					Entry entry = (Entry) iterator.next();
					pw.println("<tr><td width=\"30%\"><b><font color=\"red\">" + entry.getKey() + ":</font></b></td><td><b><font color=\"red\">" + TextConverter.getTextRepresentation(entry.getValue()) + "</font></b></td></tr>");				
				}
			}
			pw.println("</tbody></table>");		
			pw.print("<br/><br/><hr/><b><i>ATTRIBUTES AND COMMANDS:</i></b><hr/>");
			methods = Class.forName(clusterName).getMethods();
//			methods = new Method[specificMethods.length + 2];
//			methods[0] = IServiceCluster.class.getMethod(GET_ATTRIBUTE_SUBSCRIPTION_METHOD, new Class[] {String.class, IEndPointRequestContext.class});
//			methods[1] = IServiceCluster.class.getMethod(SET_ATTRIBUTE_SUBSCRIPTION_METHOD, new Class[] {String.class, ISubscriptionParameters.class, IEndPointRequestContext.class});
//			System.arraycopy(specificMethods, 0, methods, 2, specificMethods.length);
			Class[] parameters = null;
			for (int i = 0; i < methods.length; i++) {
				pw.println("<br/><form name=\"" + methods[i].getName() + i + "\"" + " action=\"" + appRoot + "/" + LABEL + "/"
						+ appliancePid + "/" + endPointId + "/" + clusterName + "/" + methods[i].getName() + "#" + methods[i].getName() + "\" method=\"get\">");
				pw.println("<table id=\"" + methods[i].getName() + i + "\" class=\"nicetable\"><tbody>");
				pw.println("<tr><td width=\"50%\"><b><a name=\"" + methods[i].getName() + "\">" + methods[i].getName() + "</a></b></td><td><input type=\"submit\" value=\"invoke\"/></td></tr>");
				parameters = methods[i].getParameterTypes();
				for (int j = 0; j < parameters.length - 1; j++) {
					if (parameters[j].isArray()) {
						pw.println("<tr><td width=\"50%\">Param" + (j + 1) + " (array[" + parameters[j].getComponentType().getName() + "]):</td><td><input type=\"text\" name=\"param\" size=\"100\"/></td></tr>");
					} else {
						pw.println("<tr><td width=\"50%\">Param" + (j + 1) + " (" + parameters[j].getName() + "):</td><td><input type=\"text\" name=\"param\" size=\"100\"/></td></tr>");
					}
				}
				pw.println("<input type=\"hidden\" name=\"ts\" value=\"" + timestamp + "\")");
				Class resultClass = methods[i].getReturnType();
				String resultClassStr;
				if (resultClass.isArray()) {
					resultClassStr = "array[" + resultClass.getComponentType().getName() + "]";
				} else {
					resultClassStr = resultClass.getName();
				}
				if (methodName != null && methods[i].getName().equals(methodName)) {
					pw.println("<tr><td><b><font color=\"red\">Result (" + resultClassStr +"):</font></b></td><td><b><font color=\"red\">" + formatResult(result) + "</font></b></td></tr>");
				} else
					pw.println("<tr><td>Result (" + resultClassStr +"):</td><td>&nbsp;</td>");
				pw.println("</tbody></table></form>");
				pw.println("<br/><br/>");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void renderClusterList(String appRoot, String appliancePid, String endPointId, IServiceCluster[] clusterArray, PrintWriter pw) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
		for (int i = 0; i < clusterArray.length; i++) {
			if (clusterArray[i].isAvailable())
				if (clusterArray[i].isEmpty())
					pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"#\">"+clusterArray[i].getName()+"</a>");
				else
					pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid 
						+ "/" + endPointId + "/" + clusterArray[i].getName() + "\">" + clusterArray[i].getName() + "</a>");
			else
				pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + clusterArray[i].getName());
			pw.println("<br/>");
		}
	}
	
	private void renderClusterList(String appRoot, String appliancePid, IEndPoint endPoint, String[] clusterNameArray, PrintWriter pw) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
		for (int i = 0; i < clusterNameArray.length; i++) {
			if (endPoint.isAvailable())
				pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"#\">"+clusterNameArray[i]+"</a>");
			else
				pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + clusterNameArray[i]);
			pw.println("<br/>");
		}
	}

	private void renderEndPoints(String appRoot, IEndPoint[] endPoints , PrintWriter pw) {
		for (int i = 0; i < endPoints.length; i++) {
			IEndPoint endPoint = endPoints[i];
			if (endPoint.isAvailable())
				pw.println("<b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + endPoint.getId() + ": " + 
						endPoint.getType() + "</b><br>"); 
			else
				pw.println("<b><font color=\"gray\">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + endPoint.getId() + ": " + 
						endPoint.getType() + "</font></b><br>");
			try {
				String appliancePid = endPoint.getAppliance().getPid();
				String endPointId = Integer.toString(endPoint.getId());
				IServiceCluster[] clusterArray = endPoint.getServiceClusters(IServiceCluster.SERVER_SIDE);
				if (clusterArray != null && clusterArray.length > 0) {
					pw.println("<br/>");				
					renderClusterList(appRoot, appliancePid, endPointId, clusterArray, pw);
				}			
				clusterArray = endPoint.getServiceClusters(IServiceCluster.CLIENT_SIDE);
				if (clusterArray != null && clusterArray.length > 0) {
					pw.println("<br/>");				
					renderClusterList(appRoot, appliancePid, endPointId, clusterArray, pw);
				}
				String[] clusterNames = endPoint.getAdditionalClusterNames();
				if (clusterNames != null && clusterNames.length > 0) {
					pw.println("<br/>");				
					renderClusterList(appRoot, appliancePid, endPoint, clusterNames, pw);
				}	
				pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<br/>");				
			} catch (InstanceNotFoundException e) {
				e.printStackTrace();
			} catch (IntrospectionException e) {
				e.printStackTrace();
			} catch (ReflectionException e) {
				e.printStackTrace();
			}
		}
	}

}
