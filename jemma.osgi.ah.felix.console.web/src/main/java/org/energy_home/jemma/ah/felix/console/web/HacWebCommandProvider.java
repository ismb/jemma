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
package org.energy_home.jemma.ah.felix.console.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

import org.energy_home.jemma.ah.cluster.ah.ConfigServer;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.ICategory;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.ILocation;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.SubscriptionParameters;
import org.energy_home.jemma.ah.hac.lib.ext.IApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.hac.lib.ext.TextConverter;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HacWebCommandProvider extends org.apache.felix.webconsole.AbstractWebConsolePlugin {
	private static final long serialVersionUID = -7727225969825874601L;

	private static final Logger LOG = LoggerFactory.getLogger( HacWebCommandProvider.class );
	
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
			LOG.error("Error while opening zigbee network", e);
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
			LOG.error("Error while closing zigbee network", e);
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
		if (key == null) {
			LOG.warn("addNetworkManager: received invalid network type property");
		}
		else if (key.equals("ZigBee")){
			zbNetworkManager = s;
		}
	}

	public void removeNetworkManager(INetworkManager s, Map properties) {
		String key = (String) properties.get("network.type");
		if (key == null) {
			LOG.warn("removeNetworkManager: received invalid network type property");
		}
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
					pw.println("</br><hr/>");
				} else {
					v = appliancesProxy.getAppliancePids();
					v.add(PROXY_APPLIANCE_PID);			
					pw.println("<br/><b><u>INSTALLED APPLIANCES</u>&nbsp;&nbsp;&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install\">Go to installing appliances</a>)</b></br><hr/>");
				}
				List driverApplianceList = new ArrayList();
				List virtualApplianceList = new ArrayList();
				List singletonApplianceList = new ArrayList();
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
							if (appliance.isSingleton())
								singletonApplianceList.add(appliance);
							else if (appliance.isDriver())
								driverApplianceList.add(appliance);
							else
								virtualApplianceList.add(appliance);
						}
					}
					for (Iterator iterator = driverApplianceList.iterator(); iterator.hasNext();) {
						appliance = (IAppliance) iterator.next();
						renderAppliance(appRoot, installing, false, appliance, pw);
					}
					pw.println("<hr/>");
					for (Iterator iterator = virtualApplianceList.iterator(); iterator.hasNext();) {
						appliance = (IAppliance) iterator.next();
						renderAppliance(appRoot, installing, false, appliance, pw);
					}
					pw.println("<hr/>");
					for (Iterator iterator = singletonApplianceList.iterator(); iterator.hasNext();) {
						appliance = (IAppliance) iterator.next();
						renderAppliance(appRoot, installing, false, appliance, pw);
					}
					pw.println("<br/>");
					String gitVersion=getGitBuildNumber();
					LOG.debug("GIT VERSION: {}",gitVersion);
					if(gitVersion!=null)
					{
						pw.println("Git version: <a href=\"https://github.com/ismb/jemma/commit/"+gitVersion+"\">"+gitVersion+"</a>");
					}else{
						pw.println("Git version: UNKNOWN");
					}
				}
			} else if (pathInfo.startsWith("/config")) {
				st = new StringTokenizer(pathInfo, "/");
				st.nextToken();
				appliancePid = st.nextToken();
				Integer endPointId = null;
				if (st.hasMoreElements()) {
					endPointId = new Integer(st.nextToken());
				}
				String[] params = req.getParameterValues("param");
				IApplianceConfiguration config = appliancesProxy.getApplianceConfiguration(appliancePid);
				if (config != null && params != null && params.length >= 1) {
					config.updateName(endPointId, params[0]);
					config.updateCategoryPid(endPointId, params[1]);
					config.updateLocationPid(endPointId, params[2]);
					appliancesProxy.updateApplianceConfiguration(config);
				} 
				renderApplianceConfiguration(appRoot, appliancesProxy.getApplianceConfiguration(appliancePid), endPointId, pw);
			} else {
				st = new StringTokenizer(pathInfo, "/");
				int i = 0;
				appliancePid = null;
				int endPointId = -1;
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
				IAppliance appliance = appliancesProxy.getAppliance(appliancePid);
				if (appliance == null)
					appliance = appliancesProxy.getInstallingAppliance(appliancePid);
				if (appliance == null) {
					pw.println("<br><br>Please wait some seconds and try to reload this web page or click on the AH tab...");
					return;
				}
				IApplianceConfiguration config = appliancesProxy.getApplianceConfiguration(appliancePid);
				if (endPointId < 0) { 
					renderAppliance(appRoot, false, true, appliance, pw);					
				} else {
					renderClusterCommands(appRoot, config, appliance.getEndPoint(endPointId), clusterName, methodName, params, pw);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getGitBuildNumber() {
		Bundle[] allBundles=FrameworkUtil.getBundle(this.getClass()).getBundleContext().getBundles();
		//find the jemma.osgi.ah.bundle and get property Implementation-Version value
		for(int i=0;i<allBundles.length;i++)
		{
			if(allBundles[i].getSymbolicName().equals("jemma.osgi.ah.app"))
			{
				return allBundles[i].getHeaders().get("Implementation-Version");
			}
		}
		return null;
	}
	
	private void renderAppliance(String appRoot, boolean installing, boolean details, IAppliance appliance, PrintWriter pw) {
		String appliancePid = appliance.getPid();
		IApplianceConfiguration config = appliancesProxy.getApplianceConfiguration(appliancePid);
		IEndPoint[] endPoints = appliance.getEndPoints();
		if (endPoints != null && endPoints.length > 1) {
			String name = null; 
			ICategory category = null;
			ILocation location = null;
			if (config != null) {
				name = config.getName(null);
				category = appliancesProxy.getCategory(config.getCategoryPid(null));
				location = appliancesProxy.getLocation(config.getLocationPid(null));
			}
			boolean isAvailable = appliance.isAvailable();
			if (isAvailable) {
				pw.println("<b>");
			} else {
				pw.println("<b><font color=\"gray\">");
			}
			if (!details) {
				pw.println((appliance.isDriver() ? "DRIVER APPLIANCE" : "VIRTUAL APPLIANCE"));
				pw.println("&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "/"+ appliancePid + "\">Details</a>" +
				 	(installing ? "&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install/"+ appliance.getPid() + "\">Install</a>" : "") + 
			 			(appliance.isSingleton() ? ")" : ("&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/confirm/delete/"+ appliance.getPid() + "\">Delete</a>)")));
			} else {
				pw.print("<br/><u>APPLIANCE DETAILS</u>" + 
						"&nbsp;&nbsp;(<a <a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid + "\">Reload page</a>&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + 
						"\">Go to installed appliances</a>" + "&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/install\">Go to installing appliances</a>)</br><hr/>");
				pw.println((appliance.isDriver() ? "DRIVER APPLIANCE" : "VIRTUAL APPLIANCE"));		
				if (!appliance.isSingleton()) {
					pw.println("&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "/config/"+ appliancePid + "\">Configuration</a>)");
				}
			}
			pw.println("<br/>PID: " + appliance.getPid() + "<br/>TYPE: " + appliance.getDescriptor().getType() + 
					((name != null)? "<br/>Name: " + name : "") + 
					((category != null)? "<br/>Category: " + category.getName() : "") + 
					((location != null)? "<br/>Location: " + location.getName() : "") + 
					"</b><br/><br/>");		
			if (isAvailable) {
				pw.println("</b>");
			} else {
				pw.println("</b></font>");
			}
			if (details)
				renderEndPoints(appRoot, config, endPoints, pw);
			pw.println("<hr>");
		}
	}
	
	private void renderApplianceConfiguration(String appRoot, IApplianceConfiguration config, Integer endPointId, PrintWriter pw) {
		if (config == null)
			return;
		String appliancePid = config.getAppliancePid();
		IAppliance appliance = appliancesProxy.getAppliance(appliancePid);
		if (appliance == null)
			appliance = appliancesProxy.getInstallingAppliance(appliancePid);

		String name = config.getName(null);
		ICategory category = appliancesProxy.getCategory(config.getCategoryPid(null));
		ILocation location = appliancesProxy.getLocation(config.getLocationPid(null));
		pw.println((appliance.isDriver() ? "<b><u><br/>DRIVER"  : "<b><u><br/>VIRTUAL") + " APPLIANCE</u>" +
				" (<a href=\"" + appRoot + "/" + LABEL + "/config/" + appliancePid + (endPointId != null ? ("/" + endPointId) : "") + "\">Reload page</a>" +
				"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid + "\">Go to appliance details</a>)</br>" +
				"<br/>PID: " + appliance.getPid() + "<br/>TYPE: " + appliance.getDescriptor().getType() + 
					((name != null)? "<br/>Name: " + name : "") + 
					((category != null)? "<br/>Category: " + category.getName() : "") + 
					((location != null)? "<br/>Location: " + location.getName() : "") + 
					"</b><br/><hr/>");
		
		pw.println("<br/><form name=\"ApplianceConfig\"" + " action=\"" + appRoot + "/" + LABEL + "/config/"
				+ appliancePid + (endPointId != null ? ("/"+endPointId) : "") + "\" method=\"get\">");
		pw.println("<table id=\"ApplianceConfig\" class=\"nicetable\"><tbody>");
		pw.println("<tr><td width=\"50%\">");
		if (endPointId != null) {
			IEndPoint endPoint = appliance.getEndPoint(endPointId.intValue());
			pw.println("<b>End point configuration (ID: " + endPointId + ", TYPE: " + endPoint.getType() + ")</b><br/>");
		} else {
			pw.println("<b>Appliance configuration</b><br/>");
		}
		pw.println("</b></td><td><input type=\"submit\" value=\"update\"/></td></tr>");
		
		name = config.getName(endPointId);
		String categoryPid = config.getCategoryPid(endPointId);
		String locationPid = config.getLocationPid(endPointId);
		pw.println("<tr><td width=\"50%\">Name: </td><td><input type=\"text\" name=\"param\" size=\"30\" value=\"" + name + "\"/></td></tr>");
		
		pw.println("</td></tr>");
		String pid = null;
		ICategory[] categories = appliancesProxy.getCategories();
		if (categories != null && categories.length > 0) {
			pw.println("<tr><td width=\"50%\">Category: </td><td><select name=\"param\">");	
			for (int i = 0; i < categories.length; i++) {
				pid = categories[i].getPid();
				pw.println("<option value=\"" + pid + (pid.equals(categoryPid) ? "\" selected=\"selected\">" : "\" >") + categories[i].getName() + "</option>");
			}
			pw.println("</td></tr>");
		}

		ILocation[] locations = appliancesProxy.getLocations();
		if (locations != null && locations.length > 0) {
			pw.println("<tr><td width=\"50%\">Location: </td><td><select name=\"param\">");
			pid = null;
			for (int i = 0; i < locations.length; i++) {
				pid = locations[i].getPid();
				pw.println("<option value=\"" + pid + (pid.equals(locationPid) ? "\" selected=\"selected\">" : "\" >") + locations[i].getName() + "</option>");
			}	
			pw.println("</td></tr>");
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
	
	private void renderClusterCommands(String appRoot, IApplianceConfiguration config, IEndPoint endPoint, String clusterName, String methodName,
			String[] params, PrintWriter pw) throws SecurityException, InstantiationException, NoSuchFieldException, IllegalAccessException {
		IAppliance appliance = endPoint.getAppliance();
		String appliancePid = appliance.getPid();
		Integer endPointId = new Integer(endPoint.getId());
		String endPointType = endPoint.getType();
		String name = null;
		ICategory category = null;
		ILocation location = null;
		if (config != null) {
			name = config.getName(null);
			category = appliancesProxy.getCategory(config.getCategoryPid(null));
			location = appliancesProxy.getLocation(config.getLocationPid(null));			
		}
		pw.println((appliance.isDriver() ? "<br><b><u>DRIVER APPLIANCE CLUSTER</u>" : "<br><b>VIRTUAL APPLIANCE CLUSTER</u>") +
				"&nbsp;&nbsp;(<a <a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid + "/" + endPointId + "/" + clusterName + "\">Reload page</a>" +
				"&nbsp;&nbsp;&nbsp;<a href=\"" + appRoot + "/" + LABEL + "/" + appliancePid + "\">Go to appliance details</a>)</br><hr/>");

		pw.println("<br>APPLIANCE<br/>&nbsp;&nbsp;&nbsp;PID: " + appliance.getPid() + 
				"<br/>&nbsp;&nbsp;&nbsp;TYPE: " + appliance.getDescriptor().getType() + 
				((name != null)? "<br/>&nbsp;&nbsp;&nbsp;Name: " + name : "") + 
				((category != null)? "<br/>&nbsp;&nbsp;&nbsp;Category: " + category.getName() : "") + 
				((location != null)? "<br/>&nbsp;&nbsp;&nbsp;Location: " + location.getName() : "") +
				"<br/><br/>");
				
				
		if (config != null) {
			name = config.getName(endPointId);
			category = appliancesProxy.getCategory(config.getCategoryPid(endPointId));
			location = appliancesProxy.getLocation(config.getLocationPid(endPointId));			
		}		
				
		pw.println("<br/>END POINT" +
				"<br/>&nbsp;&nbsp;&nbsp;ID: " +  endPointId +  
				"<br/>&nbsp;&nbsp;&nbsp;TYPE: " +  endPointType +  
				((name != null)? "<br/>&nbsp;&nbsp;&nbsp;Name: " + name : "") + 
				((category != null)? "<br/>&nbsp;&nbsp;&nbsp;Category: " + category.getName() : "") + 
				((location != null)? "<br/>&nbsp;&nbsp;&nbsp;Location: " + location.getName() : "") + 
				"<br/>&nbsp;&nbsp;&nbsp;Cluster: " + clusterName + "</b><br/>");

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

	private void renderClusterList(String appRoot, String appliancePid, Integer endPointId, IServiceCluster[] clusterArray, PrintWriter pw) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
		for (int i = 0; i < clusterArray.length; i++) {
			if (clusterArray[i].getName().equals(ConfigServer.class.getName()))
				continue;
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

	private void renderEndPoints(String appRoot, IApplianceConfiguration config, IEndPoint[] endPoints , PrintWriter pw) {
		for (int i = 0; i < endPoints.length; i++) {
			IEndPoint endPoint = endPoints[i];
			IAppliance appliance = endPoint.getAppliance();
			String appliancePid = appliance.getPid();
			Integer endPointId = new Integer(endPoint.getId());
			boolean isAvailable = appliance.isAvailable();
			if (isAvailable)
				pw.println("<b>");
			else
				pw.println("<b><font color=\"gray\">");	
			String name = null;
			ICategory category  = null;
			ILocation location = null;
			if (config != null) {
				name = config.getName(endPointId);
				category = appliancesProxy.getCategory(config.getCategoryPid(endPointId));
				location = appliancesProxy.getLocation(config.getLocationPid(endPointId));
			}
			pw.println("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ENDPOINT");
			if (!appliance.isSingleton())
				pw.println("&nbsp;(<a href=\"" + appRoot + "/" + LABEL + "/config/"+ appliancePid + "/" + endPointId + "\">Configuration</a>)");
			pw.println("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;ID: " + endPoint.getId() + 
						"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TYPE: " + endPoint.getType() + 
						((name != null)? "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Name: " + name : "") + 
						((category != null)? "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Category: " + category.getName() : "") + 
						((location != null)? "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Location: " + location.getName() : ""));
			if (isAvailable)
				pw.println("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + (endPointId > 0 ? "Clusters:" : "") + "</b><br/>");
			else
				pw.println("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + (endPointId > 0 ? "Clusters:" : "") + "</b></font><br/>");	
			try {
				IServiceCluster[] clusterArray = endPoint.getServiceClusters(IServiceCluster.SERVER_SIDE);
				if (clusterArray != null && clusterArray.length > 0) {				
					renderClusterList(appRoot, appliancePid, endPointId, clusterArray, pw);
				}			
				clusterArray = endPoint.getServiceClusters(IServiceCluster.CLIENT_SIDE);
				if (clusterArray != null && clusterArray.length > 0) {			
					renderClusterList(appRoot, appliancePid, endPointId, clusterArray, pw);
				}
				String[] clusterNames = endPoint.getAdditionalClusterNames();
				if (clusterNames != null && clusterNames.length > 0) {			
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
