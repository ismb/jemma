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
package org.energy_home.jemma.ah.hac.lib.internal;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.ApplianceValidationException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplianceFactory;
import org.energy_home.jemma.ah.hac.IAppliancesBasicProxy;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IAttributeValuesListener;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IManagedAppliance;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.IServiceClusterListener;
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.ApplianceFactory;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceManager;
import org.energy_home.jemma.ah.hac.lib.ext.EndPointRequestContext;
import org.energy_home.jemma.ah.hac.lib.ext.ICoreApplication;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.energy_home.jemma.ah.hac.lib.ext.ServiceClusterProxyHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.device.DriverLocator;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public abstract class AppliancesBasicProxy extends Appliance implements IAppliancesBasicProxy, IServiceClustersListener {

	protected static final Log log = LogFactory.getLog(AppliancesProxy.class);

	protected static final long MINIMUM_VALID_TIME = 1356998400000l; // 01/01/2013
	
	protected static final long CHECK_SUBSCRIPTION_PERIOD = 60000;
	
	protected static final long SUBSCRIPTION_MAX_DELAY_FACTOR = 1500;
	
	protected static final String START_IDENTIFY_EVENT_TOPIC = "ah/identify/START";
	
	protected static final String END_IDENTIFY_EVENT_TOPIC = "ah/identify/END";
	
	protected static final int INITIAL_APPLIANCE_NUMBER = 12;
	
	// Driver mode is used when jemma.osgi.ah.hac bundle id not available (no appliance configuration information is available)
	private static final String AH_HAC_DRIVER_MODE = "driver";
	private static final String AH_EXECUTION_MODE = System.getProperty("it.telecomitalia.ah.hac.mode");
	
	public static final String APPLIANCE_TYPE = "ah.app.proxy";
	public static final String END_POINT_TYPE = "ah.ep.zigbee.proxy";
	public static final  String APPLIANCE_FRIENDLY_NAME = "ah.app.proxy";
	public static final  IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
			APPLIANCE_FRIENDLY_NAME);
	
	protected static boolean isHacDriverModeActive() {
		return AH_HAC_DRIVER_MODE.equals(AH_EXECUTION_MODE);
	}
		
	protected static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}

	//********** ProxyEndPoint internal class
	
	public class ProxyEndPoint extends EndPoint {

		ProxyEndPoint(String type) throws ApplianceException {
			super(type);
		}	
		
		boolean isApplianceEnabled(String appliancePid) {
			if (appliancePid.equals(APPLIANCE_TYPE))
				return true;
			ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
			return (applianceProxy != null && applianceProxy.getStatus() == ManagedApplianceStatus.STATUS_ENABLED); 
		}
		
		public IServiceCluster addProxyServiceCluster(ServiceCluster serviceCluster) throws ApplianceValidationException {
			if (serviceCluster == null)
				return null;
			Class clusterIf = serviceCluster.getClusterInterfaceClass();
			try {
				ServiceClusterProxyHandler serviceClusterHandler = new ServiceClusterProxyHandler(
						serviceCluster.getClusterInterfaceImpl(), clusterIf, new IEndPointRequestContextCheck() {						
							public void checkRequestContext(IEndPointRequestContext context) throws ServiceClusterException {
								if (context != null) {
									String appliancePid = context.getPeerEndPoint().getAppliance().getPid();
									if (!isApplianceEnabled(appliancePid))
										throw new NotAuthorized("Invalid context appliance not enabled");
								}
							}
						});
				ServiceCluster proxyCluster = serviceClusterHandler.getServiceCluster();
				Class[] registeredInterfaces;
				if (serviceCluster instanceof IServiceClusterListener)
					registeredInterfaces = new Class[] {IServiceCluster.class, clusterIf, IServiceClusterListener.class };
				else
					registeredInterfaces = new Class[] {IServiceCluster.class, clusterIf};
				return addServiceCluster(proxyCluster, (IServiceCluster)Proxy.newProxyInstance(
						clusterIf.getClassLoader(), registeredInterfaces , serviceClusterHandler));
			} catch (Exception e) {
				throw new ApplianceValidationException("End point cluster proxy instantiation error " + clusterIf.getClass().getName());
			}
		}
		
		public void removeProxyServiceCluster(ServiceCluster serviceCluster) {
			removeServiceCluster(serviceCluster);
		}
		
		public ServiceCluster addServiceCluster(ServiceCluster serviceCluster) {
			if (mainEndPoint.getServiceCluster(serviceCluster.getName()) != null) {
				// TODO: last registered service is used (control access or connection management needs to be added)
				log.warn("addExportedServiceCluster: already registered service cluster " + serviceCluster.getName());
				removeServiceCluster(serviceCluster);
			}
			try {
				return super.addServiceCluster(serviceCluster);
			} catch (ApplianceException e) {
				log.error("Error while adding service cluster " + serviceCluster.getName(), e);
				return null;
			}
		}
		
		public void removeServiceCluster(ServiceCluster serviceCluster) {
			if (serviceCluster == null)
				return;
			String clusterType = serviceCluster.getType();
			String clusterName = serviceCluster.getName();
			try {		
				if (serviceCluster.getSide() == IServiceCluster.SERVER_SIDE) {
					serverServiceClusters.remove(clusterType);
				} else {
					clientServiceClusters.remove(clusterType);
				}
			} catch (Exception e) {
				log.error("Error while removing cluster " + clusterName, e);
			}
		}
			

		
	}
	
	//********** ApplicationTasks internal class
	
	private class ApplicationTasks implements IdentifyService {
		private Timer timer = new Timer("Appliances Proxy Timer", true);
		private boolean checkSubscriptionsActive = false;
		private TimerTask checkSubscriptionsTask;
		private long identifyStopTime = 0;
		private TimerTask identifyTask; 

		long getValidCurrentTimeMillis() {
			long time = System.currentTimeMillis();
			if (time > MINIMUM_VALID_TIME)
				return time;
			return 0;
		}
		
		public boolean isIdentifyActive() {
			return (identifyStopTime > 0);
		}

		public long getIdentifyDelay() {
			if (identifyStopTime > 0)
				return identifyStopTime - System.currentTimeMillis();
			else
				return 0;
		}
		
		public synchronized void setIdentifyDelay(long delay) {
			if (identifyStopTime > 0)
				identifyTask.cancel();
			if (delay > 0) {
				identifyStopTime = getValidCurrentTimeMillis();
				if (identifyStopTime > 0) {
					identifyStopTime +=  delay;
					identifyTask = new TimerTask() {
						public void run() {
							stopIdentify();
						}
					};
					try {
						timer.schedule(identifyTask, delay);
						AppliancesBasicProxy.this.postEvent(START_IDENTIFY_EVENT_TOPIC, null);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			} else {
				stopIdentify();
			}
		}		
			
		public synchronized void stopIdentify() {
			if (identifyStopTime > 0) {
				AppliancesBasicProxy.this.postEvent(END_IDENTIFY_EVENT_TOPIC, null);
				identifyStopTime = 0;
				identifyTask.cancel();
			}
		}

		public boolean isSubscriptionsCheckActive() {
			return checkSubscriptionsActive;
		}

		public synchronized void startSubscriptionsCheck() {
			checkSubscriptionsActive = true;
			checkSubscriptionsTask = new TimerTask() {
				public void run() {
					checkSubscriptions();	
				}
			};
			try {
				timer.schedule(checkSubscriptionsTask, CHECK_SUBSCRIPTION_PERIOD, CHECK_SUBSCRIPTION_PERIOD);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		public synchronized void stopSubscriptionsCheck() {
			if (checkSubscriptionsActive) {
				checkSubscriptionsTask.cancel();
				checkSubscriptionsActive = false;
			}	
		}
		
		public void checkSubscriptions() {
			long startTime = System.currentTimeMillis();
			log.info("Starting subscriptions check");
			try {
				// Array is used to avoid concurrent modification exceptions
				String[] appliancePids = new String[applianceMap.size()];
				applianceMap.keySet().toArray(appliancePids);
				Appliance appliance;
				IServiceCluster[] serviceClusters;
				IServiceCluster serviceCluster;
				Map subscriptions;
				String attributeName;
				ISubscriptionParameters subscriptionParameters;
				IAttributeValue attributeValue;
				for (int i = 0; i < appliancePids.length; i++) {
					appliance = (Appliance) getAppliance(appliancePids[i]);
					if (appliance != null && appliance.isAvailable()) {
						IEndPoint[] endPoints = appliance.getEndPoints();
						for (int j = 0; j < endPoints.length; j++) {
							serviceClusters = endPoints[j].getServiceClusters();
							for (int k = 0; k < serviceClusters.length; k++) {
								serviceCluster = serviceClusters[k];
								subscriptions = serviceCluster.getAllSubscriptions(null);
								if (subscriptions != null) {
									for (Iterator iterator = subscriptions.entrySet().iterator(); iterator.hasNext();) {
										Entry entry = (Entry) iterator.next();
										attributeName  = (String) entry.getKey();
										subscriptionParameters = (ISubscriptionParameters) entry.getValue();
										try {
											attributeValue = serviceCluster.getLastNotifiedAttributeValue(attributeName, null);
											if (attributeValue != null && subscriptionParameters != null &&
													subscriptionParameters.getMaxReportingInterval() > 0 ) {
												if (System.currentTimeMillis() - attributeValue.getTimestamp() >
													SUBSCRIPTION_MAX_DELAY_FACTOR * subscriptionParameters.getMaxReportingInterval()) {
														log.warn("Subscription renewed for attribute " + attributeName + " of appliance " + appliancePids[i]);
														serviceCluster.setAttributeSubscription(attributeName, subscriptionParameters, confirmedRequestContext);
												}
											}
										} catch (Exception e) {
											log.error("Error while subscribing attribute with invalid reporting time - " + attributeName, e);
										}							
									}
								}
							}
						}			
					}
				}
			} catch (Exception e) {
				log.error("Error while checking subscriptions", e);
			}
			log.info("Finished subscriptions check, elapsed millisecs " + (System.currentTimeMillis() - startTime));
		}	
	}
	
	ApplicationTasks applicationTasks = new ApplicationTasks();
	
	protected BundleContext bc = null;
	protected ServiceRegistration hacServiceRegistration = null;
	protected ServiceRegistration hacDriverLocatorRegistration = null;
	
	protected ProxyEndPoint mainEndPoint;
	protected IEndPointRequestContext confirmedRequestContext;
	protected IEndPointRequestContext unconfirmedRequestContext;
	protected IdentifyServerCluster identifyServer;
	
	protected Object hacServiceSync = new Object();
	protected IHacService hacService = null;
	protected Object eventAdminSync = new Object();
	protected EventAdmin eventAdmin;
	
	protected Map applianceFactoryMap = new HashMap(INITIAL_APPLIANCE_NUMBER);
	protected Map applianceMap = new HashMap(INITIAL_APPLIANCE_NUMBER);
	protected Map applianceConfigurationMap = new HashMap(INITIAL_APPLIANCE_NUMBER);
	protected Map installingApplianceMap = new HashMap(INITIAL_APPLIANCE_NUMBER);

	protected List attributeValuesListenerList = new ArrayList();
	protected List<IApplicationService> appliancesListenerList = new ArrayList<IApplicationService>();
	protected Map appliancesListenerListMap = new HashMap(INITIAL_APPLIANCE_NUMBER);

	protected ManagedApplianceServiceTracker managedApplianceServiceTracker = null;
	protected boolean useManagedApplianceServiceTracker = true;

	//********** Internal miscellaneous methods
	
	private void postEvent(String topic, Map props) {
		synchronized (eventAdminSync) {
			if (this.eventAdmin != null) {
				try {
					this.eventAdmin.postEvent(new Event(topic, props));
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}
	
	private void notifyAllAppliancesAdded(IApplicationService listener, boolean installing) {
		Map applianceMap = installing ? installingApplianceMap : this.applianceMap;
		for (Iterator iterator = applianceMap.values().iterator(); iterator.hasNext();) {
			IAppliance appliance = ((ManagedApplianceStatus) iterator.next()).getAppliance();
			String appliancePid = appliance.getPid();
			try {
				listener.notifyApplianceAdded(mainEndPoint, appliance);	
				List proxyListeners = getProxyListeners(appliancePid);
				if (proxyListeners.remove(listener))
					log.error("Existing listener " + listener +  " removed for appliance " + appliancePid);
				proxyListeners.add(listener);
			} catch (Exception e) {
				log.error("Error while notifying appliance added to listener " + appliancePid, e);
			}
		}
	}
	
	private void notifyAllAppliancesRemoved(IApplicationService listener, boolean installing) {
		Map applianceMap = installing ? installingApplianceMap : this.applianceMap;
		for (Iterator iterator = applianceMap.values().iterator(); iterator.hasNext();) {
			IAppliance appliance = ((ManagedApplianceStatus) iterator.next()).getAppliance();
			String appliancePid = appliance.getPid();
			try {	
				List proxyListeners = getProxyListeners(appliancePid);
				proxyListeners.remove(listener);
				listener.notifyApplianceRemoved(appliance);
			} catch (Exception e) {
				log.error("Error while notifying appliance removed to listener " + appliance.getPid(), e);
			}
		}
	}
	
	private void notifyApplianceAdded(IAppliance appliance, boolean installing) {
		List proxyListeners = getProxyListeners(appliance.getPid());
		for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			try {
				if (!installing || listener instanceof ICoreApplication)
					listener.notifyApplianceAdded(mainEndPoint, appliance);
			} catch (Exception e) {
				log.error("Error while notifying new appliance to listener", e);
			}
		}
	}

	private void notifyApplianceRemoved(IAppliance appliance, boolean installing) {
		List proxyListeners = getProxyListeners(appliance.getPid());
		for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			try {
				if (!installing /* || (installing && !listener.filterInstallingAppliances())*/)
					listener.notifyApplianceRemoved(appliance);
			} catch (Exception e) {
				log.error("Error while notifying appliance removed to listener", e);
			}
		}
	}

	protected synchronized List getProxyListeners(String appliancePid) {
		if (appliancesListenerListMap.get(appliancePid) == null)
			 appliancesListenerListMap.put(appliancePid, new ArrayList());
		return (List) appliancesListenerListMap.get(appliancePid);
	}
	
	protected String[] getApplianceFactoryTypes() {
		String[] result = new String[applianceFactoryMap.size()];
		applianceFactoryMap.keySet().toArray(result);
		return result;
	}
	
	protected ApplianceFactory getApplianceFactory(String appliancePid) {
		ManagedApplianceStatus proxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		if (proxy == null)
			proxy = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
		if (proxy == null)
			return null;
		String type = proxy.getAppliance().getDescriptor().getType();
		return (ApplianceFactory) applianceFactoryMap.get(type);
	}
	
	//********** Appliance related methods
	
	public AppliancesBasicProxy() throws ApplianceException {
		super(APPLIANCE_TYPE, null);
		//setApplianceManager(null);

		mainEndPoint = new ProxyEndPoint(END_POINT_TYPE);
		this.addEndPoint(mainEndPoint);
		mainEndPoint.addServiceCluster(new BasicServerCluster());
		identifyServer = new IdentifyServerCluster(applicationTasks);
		mainEndPoint.addServiceCluster(identifyServer);
		mainEndPoint.addServiceCluster(new TimeServerCluster());
		mainEndPoint.registerServiceClustersListener(this);
		this.confirmedRequestContext = new EndPointRequestContext(mainEndPoint);
		this.unconfirmedRequestContext = new EndPointRequestContext(mainEndPoint, false, 0);
	}
	
	public void start(ComponentContext ctxt) {
		super.start();
		applicationTasks.startSubscriptionsCheck();
		bc = ctxt.getBundleContext();
		if (useManagedApplianceServiceTracker) {
			this.managedApplianceServiceTracker = new ManagedApplianceServiceTracker(bc, this);
			this.managedApplianceServiceTracker.open();
		}			

		if (isHacDriverModeActive()) {
			Hashtable props = new Hashtable(1);
			props.put("osgi.command.scope", "hac");
			hacServiceRegistration = bc.registerService(new String[] { IHacService.class.getName(), CommandProvider.class.getName()}, new SimpleHacService((AppliancesProxy)this), props);
			hacDriverLocatorRegistration = bc.registerService(new String[] { DriverLocator.class.getName()}, new SimpleHacDriverLocator(), null);
		}
	}
	
	public void stop() {
		if (isHacDriverModeActive()) {
			if (hacServiceRegistration != null) {
				hacServiceRegistration.unregister();
				hacServiceRegistration = null;
			}
			if (hacDriverLocatorRegistration != null) {
				hacDriverLocatorRegistration.unregister();
				hacDriverLocatorRegistration = null;
			}
		}
		
		if (this.managedApplianceServiceTracker != null) {
			this.managedApplianceServiceTracker.close();
		}	
		super.stop();
	
		applicationTasks.stopSubscriptionsCheck();
		applicationTasks.stopIdentify();
	}
	
	public IApplianceDescriptor getDescriptor() {
		return APPLIANCE_DESCRIPTOR;
	}
	
	// Method use to receive availability updates from registered appliances (IManagedAppliance services)
	public synchronized void notifyAvailabilityUpdated(String appliancePid) {
		log.info("Availability updated for appliance " + appliancePid);
		List proxyListeners = getProxyListeners(appliancePid);
		ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		if (applianceProxy == null)
			applianceProxy = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
		if (applianceProxy == null)
			log.error("Received availability update for not exhisting appliance " + appliancePid);
		else
			for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
				IApplicationService listener = (IApplicationService) iterator.next();
				try {
					if (applianceProxy.getStatus() == ManagedApplianceStatus.STATUS_ENABLED)
						listener.notifyApplianceAvailabilityUpdated(applianceProxy.getAppliance());			
					else if (listener instanceof ICoreApplication)
						((ICoreApplication)listener).notifyInstallingApplianceAvailabilityUpdated(applianceProxy.getAppliance());	
				} catch (Exception e) {
					log.error("Error while notifying availability update", e);
				}
			}
	}
	
	// Method use to receive configuration updates from registered appliances (IManagedAppliance services)
	public void notifyConfigurationUpdated(String appliancePid, int endPointId) {
		log.info("Configuration updated for appliance " + appliancePid + " and end point " + endPointId);
		if (!isHacDriverModeActive()) {
// Configuration updated ignored because hac service unregister and register IManagedAppliance service
//			for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
//				IAppliancesBasicListener listener = (IAppliancesBasicListener) iterator.next();
//				try {
//					listener.notifyEndPointConfigurationUpdated(appliancePid, new Integer(endPointId));			
//				} catch (Exception e) {
//					log.error("Error while notifying configuration update", e);
//				}
//			}
		} 
			
	}
	
	//********** Methods used by declarative services
	
	public void setHacService(IHacService s) {
		synchronized (hacServiceSync) {
			hacService = s;
		}
	}
	
	public void unsetHacService(IHacService s) {
		synchronized (hacServiceSync) {
			if (s == hacService)
				hacService = null;
		}
	}
	
	public void setEventAdmin(EventAdmin s) {
		synchronized (eventAdminSync) {
			eventAdmin = s;
		}
	}

	public void unsetEventAdmin(EventAdmin s) {
		synchronized (eventAdminSync) {
			if (s == eventAdmin)
				eventAdmin = s;
		}
	}
	
	public synchronized void addApplianceFactory (IApplianceFactory factory, Map props) {
		String type = factory.getDescriptor().getType();
		log.info("Added appliancefactory " + factory.getDescriptor().getType());
		if (isHacDriverModeActive()) {
			factory.init();
		}
		applianceFactoryMap.put(type, factory);
	}
	
	public synchronized void removeApplianceFactory (IApplianceFactory factory) {
		String type = factory.getDescriptor().getType();
		log.info("Removed appliancefactory " + factory.getDescriptor().getType());
		applianceFactoryMap.remove(type);
	}
	
	public synchronized void addManagedAppliance (IManagedAppliance appliance, Map props) {
		String appliancePid = appliance.getPid();
		log.info("Added appliance " + appliancePid);
		if (isHacDriverModeActive()) {
			ApplianceManager applianceManager = ((ApplianceManager) appliance.getApplianceManager());
			applianceManager.setHacService(hacService);
		}
		IEndPoint[] endPoints = appliance.getEndPoints();
		Map savedProps = new HashMap(props);
		for (int i = 0; i < endPoints.length; i++) {
			((ApplianceManager)appliance.getApplianceManager()).setAppliancesProxy((EndPoint)endPoints[i], (AppliancesProxy)this);
		}
		List proxyListeners = getProxyListeners(appliancePid);
		for (Iterator iterator = appliancesListenerList.iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			if (proxyListeners.remove(listener))
				log.error("Existing listener " + listener +  " removed for appliance " + appliancePid);
			// TODO: add here filter logic
			proxyListeners.add(listener);
		}
		applianceConfigurationMap.put(appliancePid, savedProps);
		String appStatus = (String) savedProps.get("ah.status");
		if (appStatus != null && appStatus.equals("installing")) {
			log.info("Appliance not yet installed " + appliancePid);
			ManagedApplianceStatus proxy = new ManagedApplianceStatus(appliance, ManagedApplianceStatus.STATUS_INSTALLING);
			installingApplianceMap.put(appliancePid, proxy);
			notifyApplianceAdded(appliance, true);
			return;
		}  
		ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
		if (applianceProxy != null) {
			installingApplianceMap.remove(appliancePid);
			applianceProxy.setStatus(ManagedApplianceStatus.STATUS_ENABLING);
		} else {
			applianceProxy = new ManagedApplianceStatus(appliance, ManagedApplianceStatus.STATUS_ENABLING);
		}
		if (applianceMap.get(appliancePid) != null)
			log.error("Appliance " + appliancePid + " already added");
		applianceMap.put(appliancePid, applianceProxy);

		notifyApplianceAdded(appliance, false);
		applianceProxy.setStatus(ManagedApplianceStatus.STATUS_ENABLED);
	}
	
	public synchronized void updatedManagedAppliance (IManagedAppliance appliance, Map props) {
		this.removeManagedAppliance(appliance);
		this.addManagedAppliance(appliance, props);
	}
	
	public synchronized void removeManagedAppliance (IManagedAppliance appliance) {
		String appliancePid = appliance.getPid();
		log.info("Removed appliance " + appliancePid);	
		if (!isHacDriverModeActive())
			applianceConfigurationMap.remove(appliancePid);
		IEndPoint[] endPoints = appliance.getEndPoints();
		for (int i = 0; i < endPoints.length; i++) {
			((ApplianceManager)appliance.getApplianceManager()).setAppliancesProxy((EndPoint)endPoints[i], null);
			IServiceCluster[] serviceClusterArray = endPoints[i].getServiceClusters();
			for (int j = 0; j < serviceClusterArray.length; j++) {
				try {
					serviceClusterArray[j].removeAllSubscriptions(confirmedRequestContext);
				} catch (Exception e) {
					log.error("Error while removing all subscription from appliance " + appliancePid, e);
				} 
			}
		}
		if (installingApplianceMap.get(appliancePid) != null) {
			log.info("Appliance not yet installed " + appliancePid);
			notifyApplianceRemoved(appliance, true);
			installingApplianceMap.remove(appliancePid);
		} else {
			notifyApplianceRemoved(appliance, false);
			applianceMap.remove(appliancePid);
		}
		List proxyListeners = getProxyListeners(appliancePid);
		for (Iterator iterator = appliancesListenerList.iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			proxyListeners.remove(listener);
		}
	}
	
	public synchronized void addApplicationService(IApplicationService listener) {
		appliancesListenerList.add(listener);
		notifyAllAppliancesAdded(listener, false);
		if (listener instanceof ICoreApplication) {
			notifyAllAppliancesAdded(listener, true);
		}
		IServiceCluster[] serviceClusters = listener.getServiceClusters();
		if (serviceClusters != null) {
			for (int i = 0; i < serviceClusters.length; i++) {
				ServiceCluster serviceCluster = (ServiceCluster) serviceClusters[i];
				try {
					mainEndPoint.addProxyServiceCluster(serviceCluster);
				} catch (ApplianceValidationException e) {
					log.error("Error while adding proxy service cluster" + serviceCluster.getName(), e);
				}
			}
		}
	}	
	
	public synchronized void removeApplicationService(IApplicationService listener) {
		IServiceCluster[] serviceClusters = listener.getServiceClusters();
		if (serviceClusters != null) {
			for (int i = 0; i < serviceClusters.length; i++) {
				ServiceCluster serviceCluster = (ServiceCluster) serviceClusters[i];
				mainEndPoint.removeProxyServiceCluster(serviceCluster);
			}
		}
		appliancesListenerList.remove(listener);
		notifyAllAppliancesRemoved(listener, false);
		if (listener instanceof ICoreApplication) {
			notifyAllAppliancesRemoved(listener, true);
		}

	}	

	public synchronized void addAttributeValuesListener(IAttributeValuesListener listener) {
		synchronized (attributeValuesListenerList) {
			if (attributeValuesListenerList.contains(listener)) {
				log.warn("Dulicated listeners detected " + listener);
				return;
			}
			attributeValuesListenerList.add(listener);
		}
	}

	public void removeAttributeValuesListener(IAttributeValuesListener listener) {
		synchronized (attributeValuesListenerList) {
			attributeValuesListenerList.remove(listener);
		}
	}	
		
	//********** IServiceClusterListeners interface
	
	public void notifyAttributeValue(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		String appliancePid = endPointRequestContext.getPeerEndPoint().getAppliance().getPid();
		ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		if (applianceProxy == null || applianceProxy.getStatus() != ManagedApplianceStatus.STATUS_ENABLED) {
			log.warn("notifyAttributeValue received from an " +
					((applianceProxy == null) ? "unknown" : "not enabled ") + " appliance: " + 
						endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " " + clusterName + " " + 
						attributeName + " " + attributeValue.getTimestamp() + " " + attributeValue.getValue());
			return;
		}
		Integer endPointId = new Integer(endPointRequestContext.getPeerEndPoint().getId());
		log.debug("notifyAttributeValue: " + endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp()
				+ " " + attributeValue.getValue());		
		synchronized (attributeValuesListenerList) {
			for (Iterator iterator = attributeValuesListenerList.iterator(); iterator.hasNext();) {
				IAttributeValuesListener listener = (IAttributeValuesListener) iterator.next();
				try {
					listener.notifyAttributeValue(appliancePid, endPointId, clusterName, attributeName, attributeValue);				
				} catch (Exception e) {
					log.error("Error while notifying attribute", e);
				}
			}
		}
	}

	public void notifyReadResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		log.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyReadResponse " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp() + " "
				+ attributeValue.getValue());
	}

	public void notifyWriteResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		log.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyWriteResponse " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp()
				+ " " + attributeValue.getValue());
	}

	public void notifyCommandResponse(String clusterName, String commandName, Object response,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException {
		log.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyCommandResponse " + clusterName + " " + commandName + " " + response);
	}
	 
	//********** IAppliancesProxy service interface

	public IEndPointRequestContext getRequestContext(boolean isConfirmationRequired) {
		if (isConfirmationRequired)
			return confirmedRequestContext;
		return unconfirmedRequestContext;	
	}

	public synchronized List getAppliances() {
		List list = new ArrayList(applianceMap.size());
		for (Iterator iterator = applianceMap.values().iterator(); iterator.hasNext();) {
			ManagedApplianceStatus proxy = ((ManagedApplianceStatus) iterator.next());
			list.add(proxy.getAppliance());
		}
		return list;
	}

	public synchronized List getAppliancePids() {
		List list = new ArrayList(applianceMap.size());
		for (Iterator iterator = applianceMap.values().iterator(); iterator.hasNext();) {
			ManagedApplianceStatus proxy = ((ManagedApplianceStatus) iterator.next());
			list.add(proxy.getAppliance().getPid());
		}
		return list;
	}
	
	public IAppliance getAppliance(String appliancePid) {
		// This appliance is used to export services and can be retrieved only by pid 
		// (not included in appliance list) 
		if (appliancePid.equals(APPLIANCE_TYPE))
			return this;
		ManagedApplianceStatus proxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		if (proxy != null)
			return proxy.getAppliance();
		return null;
	}
	
	
	
}

