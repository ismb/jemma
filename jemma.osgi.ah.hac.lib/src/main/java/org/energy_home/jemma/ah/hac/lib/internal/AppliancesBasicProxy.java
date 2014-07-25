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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.hac.ApplianceException;
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
import org.energy_home.jemma.ah.hac.IServiceClustersListener;
import org.energy_home.jemma.ah.hac.ISubscriptionParameters;
import org.energy_home.jemma.ah.hac.NotAuthorized;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.ApplianceFactory;
import org.energy_home.jemma.ah.hac.lib.ServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceConfiguration;
import org.energy_home.jemma.ah.hac.lib.ext.ApplianceManager;
import org.energy_home.jemma.ah.hac.lib.ext.EndPointRequestContext;
import org.energy_home.jemma.ah.hac.lib.ext.ICoreApplication;
import org.energy_home.jemma.ah.hac.lib.ext.IHacService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.device.DriverLocator;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AppliancesBasicProxy extends Appliance implements IAppliancesBasicProxy, IServiceClustersListener {

	protected static final Logger LOG = LoggerFactory.getLogger(AppliancesProxy.class);

	protected static final long MINIMUM_VALID_TIME = 1356998400000l; // 01/01/2013
	
	protected static final int CHECK_SUBSCRIPTION_PERIOD_MULTIPLIER = 12;
	protected static final long CHECK_SUBSCRIPTION_PERIOD = 5000;
	
	protected static final long SUBSCRIPTION_MAX_DELAY_FACTOR = 1500;
	
	protected static final String START_IDENTIFY_EVENT_TOPIC = "ah/identify/START";
	
	protected static final String END_IDENTIFY_EVENT_TOPIC = "ah/identify/END";
	
	protected static final int INITIAL_APPLIANCE_NUMBER = 12;
	
	protected static final int INITIAL_APPLICATION_NUMBER = 3;
	
	// Driver mode is used when org.energy_home.jemma.osgi.ah.hac bundle id not available (no appliance configuration information is available)
	private static final String AH_HAC_DRIVER_MODE = "driver";
	private static final String AH_EXECUTION_MODE = System.getProperty("org.energy_home.jemma.ah.hac.mode");
	
	public static final String APPLIANCE_TYPE = "ah.app.proxy";
	public static final String END_POINT_TYPE = "ah.ep.zigbee.proxy";
	public static final String APPLICATION_END_POINT = "ah.app.application.proxy";
	public static final  String APPLIANCE_FRIENDLY_NAME = "ah.app.proxy";
	public static final  IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
			APPLIANCE_FRIENDLY_NAME);
	
	private static final String APPLICATION_SERVICE_NAME_PROPERTY_NAME = "ah.application.name";
	
	private static Dictionary initialConfig = new Hashtable(1);
			
	static {
		initialConfig.put(IAppliance.APPLIANCE_NAME_PROPERTY, APPLIANCE_FRIENDLY_NAME);
	};
	
	protected static boolean isHacDriverModeActive() {
		return AH_HAC_DRIVER_MODE.equals(AH_EXECUTION_MODE);
	}
		
	protected static boolean isNullOrEmpty(String s) {
		return ((s == null) || (s.length() == 0));
	}

	//********** ProxyEndPoint internal class
	
		
	boolean isApplianceEnabled(String appliancePid) {
		if (appliancePid.equals(APPLIANCE_TYPE))
			return true;
		ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		return (applianceProxy != null && applianceProxy.getStatus() == ManagedApplianceStatus.STATUS_ENABLED); 
	}
	
	//********** ApplicationTasks internal class
	
	private class ApplicationTasks implements IdentifyService {
		private Timer timer = new Timer("Appliances Proxy Timer", true);
		private boolean checkSubscriptionsActive = false;
		private TimerTask checkSubscriptionsTask;
		private long identifyStopTime = 0;
		private TimerTask identifyTask; 
		
		private int timeIntervalCounter = 0;
		private int fasterChecks = -1;

		long getValidCurrentTimeMillis() {
			long time = System.currentTimeMillis();
			if (time > MINIMUM_VALID_TIME)
				return time;
			return 0;
		}
		
		boolean isCheckRequired() {
			// After around 1 minutes, faster subscription check is disabled
			if (isFasterSubscriptionCheckEnabled()) {
				LOG.debug("Faster subscription check enabled");
				fasterChecks++;
				return true;
			}
			fasterChecks = -1;
			timeIntervalCounter++;
			if (timeIntervalCounter == Integer.MAX_VALUE)
				timeIntervalCounter = 0;
			return (timeIntervalCounter % CHECK_SUBSCRIPTION_PERIOD_MULTIPLIER == 0);					
		}
		
		boolean isFasterSubscriptionCheckEnabled() {
			return (fasterChecks >= 0 && fasterChecks <= 12);
		}
		
		void enableFasterSubscriptionCheck() {
			if (fasterChecks < 0) {
				fasterChecks = 0;
				timeIntervalCounter = 0;
			}
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
						LOG.warn(e.getMessage(), e);
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
				LOG.warn(e.getMessage(), e);
			}
		}
		
		public synchronized void stopSubscriptionsCheck() {
			if (checkSubscriptionsActive) {
				checkSubscriptionsTask.cancel();
				checkSubscriptionsActive = false;
			}	
		}
		
		public void checkSubscriptions() {
			if (!isCheckRequired()) {
				return;
			}
			long startTime = System.currentTimeMillis();
			LOG.debug("Starting subscriptions check");
			try {
				// Array is used to avoid concurrent modification exceptions
				String[] appliancePids = new String[applianceMap.size()];
				applianceMap.keySet().toArray(appliancePids);
				ManagedApplianceStatus applianceStatus;
				IManagedAppliance appliance;
				IServiceCluster[] serviceClusters;
				IServiceCluster serviceCluster;
				Map subscriptions;
				String attributeName;
				ISubscriptionParameters subscriptionParameters;
				ISubscriptionParameters returnedSubscriptionParameters;
				IAttributeValue attributeValue;
				long lastNotifiedTimestamp = 0;
				for (int i = 0; i < appliancePids.length; i++) {
					applianceStatus = (ManagedApplianceStatus) applianceMap.get(appliancePids[i]);
					if (applianceStatus == null)
						appliance = null;
					else
						appliance = applianceStatus.getAppliance();
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
											lastNotifiedTimestamp = (attributeValue != null) ? Math.max(attributeValue.getTimestamp(), applianceStatus.getLastSubscriptionRequestTime()) : 
													applianceStatus.getLastSubscriptionRequestTime();
											if (subscriptionParameters != null &&
													subscriptionParameters.getMaxReportingInterval() > 0 ) {
												if (System.currentTimeMillis() - lastNotifiedTimestamp >
													SUBSCRIPTION_MAX_DELAY_FACTOR * subscriptionParameters.getMaxReportingInterval()) {
														if (LOG.isDebugEnabled()) {
															LOG.debug("Subscription renewed for attribute " + attributeName + ", cluster " +serviceCluster.getName() + ","
																	+ " ep " + serviceCluster.getEndPoint().getId() + ", appliance " + appliancePids[i]);
														}
														returnedSubscriptionParameters = serviceCluster.setAttributeSubscription(attributeName, subscriptionParameters, confirmedRequestContext);
														if (returnedSubscriptionParameters == null) {
															// Retry subscription faster when fails (useful for sleeping end devices)
															enableFasterSubscriptionCheck();
														}
														if (returnedSubscriptionParameters != null || !isFasterSubscriptionCheckEnabled())
															// If the subscription succeeded or failed after a period of faster checks, last subscription request time for the appliance is initialized
															applianceStatus.setLastSubscriptionRequestTime(System.currentTimeMillis());
												}
											}
										} catch (Exception e) {
											LOG.warn("Error while subscribing attribute with invalid reporting time - " + attributeName, e);
										}							
									}
								}
							}
						}			
					}
				}
			} catch (Exception e) {
				LOG.warn("Error while checking subscriptions", e);
			}
			LOG.debug("Finished subscriptions check, elapsed millisecs " + (System.currentTimeMillis() - startTime));
		}	
	}
	
	ApplicationTasks applicationTasks = new ApplicationTasks();
	private AppliancesInitializationManager appliancesInitializationManager;
	
	protected BundleContext bc = null;
	protected ServiceRegistration hacServiceRegistration = null;
	protected ServiceRegistration hacDriverLocatorRegistration = null;
	
	protected EndPointProxy mainEndPoint;
	protected IEndPointRequestContext confirmedRequestContext;
	protected IEndPointRequestContext unconfirmedRequestContext;
	protected IEndPointRequestContext lastReadRequestContext;
	
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
	protected Map appliancesListenerListMap = new HashMap(INITIAL_APPLIANCE_NUMBER);

	protected Map applicationToProxyEndPointMap = new HashMap(INITIAL_APPLICATION_NUMBER);
	
	protected ManagedApplianceServiceTracker managedApplianceServiceTracker = null;
	protected boolean useManagedApplianceServiceTracker = true;

	//********** Internal miscellaneous methods
	
	private void postEvent(String topic, Map props) {
		synchronized (eventAdminSync) {
			if (this.eventAdmin != null) {
				try {
					this.eventAdmin.postEvent(new Event(topic, props));
				} catch (Exception e) {
					LOG.warn(e.getMessage(), e);
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
				if (!installing)
					listener.notifyApplianceAdded(mainEndPoint, appliance);	
				else if (listener instanceof ICoreApplication)
					((ICoreApplication)listener).notifyInstallingApplianceAdded(appliance);
				List proxyListeners = getProxyListeners(appliancePid);
				if (proxyListeners.remove(listener))
					LOG.debug("Existing listener " + listener +  " removed for appliance " + appliancePid);
				proxyListeners.add(listener);
			} catch (Exception e) {
				LOG.warn("Error while notifying appliance added to listener " + appliancePid, e);
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
				if (!installing)
					listener.notifyApplianceRemoved(appliance);
				else if (listener instanceof ICoreApplication)
					((ICoreApplication)listener).notifyInstallingApplianceRemoved(appliance);
			} catch (Exception e) {
				LOG.warn("Error while notifying appliance removed to listener " + appliance.getPid(), e);
			}
		}
	}
	
	private void notifyApplianceAdded(IAppliance appliance, boolean installing) {
		List proxyListeners = getProxyListeners(appliance.getPid());
		for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			try {
				if (!installing) 
					listener.notifyApplianceAdded(mainEndPoint, appliance);
				else if (listener instanceof ICoreApplication)
					((ICoreApplication)listener).notifyInstallingApplianceAdded(appliance);
			} catch (Exception e) {
				LOG.warn("Error while notifying new appliance to listener", e);
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
				LOG.warn("Error while notifying appliance removed to listener", e);
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
		super(APPLIANCE_TYPE, initialConfig);
		//setApplianceManager(null);
		mainEndPoint = new EndPointProxy(END_POINT_TYPE);
		addEndPoint(mainEndPoint);
		mainEndPoint.addServiceCluster(new BasicServerCluster());
		identifyServer = new IdentifyServerCluster(applicationTasks);
		mainEndPoint.addServiceCluster(identifyServer);
		mainEndPoint.addServiceCluster(new TimeServerCluster());
		mainEndPoint.registerServiceClustersListener(this);
		confirmedRequestContext = new EndPointRequestContext(mainEndPoint);
		unconfirmedRequestContext = new EndPointRequestContext(mainEndPoint, false, 0);
		lastReadRequestContext = new EndPointRequestContext(mainEndPoint, true, Long.MAX_VALUE);
		appliancesInitializationManager = new AppliancesInitializationManager(confirmedRequestContext);
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
		LOG.debug("Availability updated for appliance " + appliancePid);
		List proxyListeners = getProxyListeners(appliancePid);
		ManagedApplianceStatus applianceProxy = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		boolean installing = false;
		if (applianceProxy == null) {
			applianceProxy = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
			installing = (applianceProxy != null);
		}		
		if (applianceProxy == null)
			LOG.warn("Received availability update for not exhisting appliance " + appliancePid);
		else {
			IAppliance appliance = applianceProxy.getAppliance();
			if (appliance.isAvailable()) {
				appliancesInitializationManager.initAppliance(appliance, installing);
				applianceProxy.setLastSubscriptionRequestTime(System.currentTimeMillis());
			}
			for (Iterator iterator = proxyListeners.iterator(); iterator.hasNext();) {
				IApplicationService listener = (IApplicationService) iterator.next();
				try {
					if (applianceProxy.getStatus() == ManagedApplianceStatus.STATUS_ENABLED)
						listener.notifyApplianceAvailabilityUpdated(appliance);			
					else if (listener instanceof ICoreApplication)
						((ICoreApplication)listener).notifyInstallingApplianceAvailabilityUpdated(appliance);	
				} catch (Exception e) {
					LOG.warn("Error while notifying availability update", e);
				}
			}
		}
	}
	
	// Method use to receive configuration updates from registered appliances (IManagedAppliance services)
	public void notifyConfigurationUpdated(String appliancePid, int endPointId) {
		LOG.debug("Configuration updated for appliance " + appliancePid + " and end point " + endPointId);
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
				eventAdmin = null;
		}
	}
	
	public synchronized void addApplianceFactory (IApplianceFactory factory, Map props) {
		String type = factory.getDescriptor().getType();
		LOG.debug("Added appliancefactory " + factory.getDescriptor().getType());
		if (isHacDriverModeActive()) {
			factory.init();
		}
		applianceFactoryMap.put(type, factory);
	}
	
	public synchronized void removeApplianceFactory (IApplianceFactory factory) {
		String type = factory.getDescriptor().getType();
		LOG.debug("Removed appliancefactory " + factory.getDescriptor().getType());
		applianceFactoryMap.remove(type);
	}
	
	public synchronized void addManagedAppliance (IManagedAppliance appliance, Map props) {
		String appliancePid = appliance.getPid();
		LOG.debug("Added appliance " + appliancePid);
		if (isHacDriverModeActive()) {
			ApplianceManager applianceManager = ((ApplianceManager) appliance.getApplianceManager());
			applianceManager.setHacService(hacService);
		}
		IEndPoint[] endPoints = appliance.getEndPoints();
		Map savedProps = new HashMap(props);
		for (int i = 0; i < endPoints.length; i++) {
			((ApplianceManager)appliance.getApplianceManager()).setAppliancesProxy((AppliancesProxy)this);
		}
		String appStatus = (String) savedProps.get(ApplianceConfiguration.AH_STATUS_PROPERTY_NAME);
		boolean installing = false;
		if (appStatus != null && appStatus.equals("installing")) {
			installing = true;
		}
		if (appliance.isAvailable()) {
			appliancesInitializationManager.initAppliance(appliance, installing);
		}
		List proxyListeners = getProxyListeners(appliancePid);
		for (Iterator iterator = applicationToProxyEndPointMap.keySet().iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			if (proxyListeners.remove(listener))
				LOG.warn("Existing listener " + listener +  " removed for appliance " + appliancePid);
			// TODO: add here filter logic
			proxyListeners.add(listener);
		}
		applianceConfigurationMap.put(appliancePid, savedProps);
		if (installing) {
			LOG.debug("Appliance not yet installed " + appliancePid);
			ManagedApplianceStatus proxy = new ManagedApplianceStatus(appliance, ManagedApplianceStatus.STATUS_INSTALLING);
			if (appliance.isAvailable())
				proxy.setLastSubscriptionRequestTime(System.currentTimeMillis());
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
		if (appliance.isAvailable())
			applianceProxy.setLastSubscriptionRequestTime(System.currentTimeMillis());
		if (applianceMap.get(appliancePid) != null)
			LOG.warn("Appliance " + appliancePid + " already added");
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
		LOG.debug("Removed appliance " + appliancePid);	
		if (!isHacDriverModeActive())
			applianceConfigurationMap.remove(appliancePid);
		IEndPoint[] endPoints = appliance.getEndPoints();
		for (int i = 0; i < endPoints.length; i++) {
			((ApplianceManager)appliance.getApplianceManager()).setAppliancesProxy(null);
			IServiceCluster[] serviceClusterArray = endPoints[i].getServiceClusters();
			for (int j = 0; j < serviceClusterArray.length; j++) {
				try {
					serviceClusterArray[j].removeAllSubscriptions(confirmedRequestContext);
				} catch (Exception e) {
					LOG.warn("Error while removing all subscription from appliance " + appliancePid, e);
				} 
			}
		}
		if (installingApplianceMap.get(appliancePid) != null) {
			LOG.debug("Appliance not yet installed " + appliancePid);
			notifyApplianceRemoved(appliance, true);
			installingApplianceMap.remove(appliancePid);
		} else {
			notifyApplianceRemoved(appliance, false);
			applianceMap.remove(appliancePid);
		}
		List proxyListeners = getProxyListeners(appliancePid);
		for (Iterator iterator = applicationToProxyEndPointMap.keySet().iterator(); iterator.hasNext();) {
			IApplicationService listener = (IApplicationService) iterator.next();
			proxyListeners.remove(listener);
		}
	}
	
	public synchronized void addApplicationService(IApplicationService listener, Map props) {
		try {
			String endPointName = (String)props.get(APPLICATION_SERVICE_NAME_PROPERTY_NAME);
			if (endPointName == null)
				endPointName = APPLICATION_END_POINT;
			EndPointProxy appEndPoint = new EndPointProxy(endPointName);
			notifyAllAppliancesAdded(listener, false);
			if (listener instanceof ICoreApplication) {
				notifyAllAppliancesAdded(listener, true);
			}
			IServiceCluster[] serviceClusters = listener.getServiceClusters();
			if (serviceClusters != null) {
				for (int i = 0; i < serviceClusters.length; i++) {
					ServiceCluster serviceCluster = (ServiceCluster) serviceClusters[i];
					try {
						String clusterName = serviceCluster.getName();
						synchronized (mainEndPoint) {
							ServiceClusterProxy serviceClusterProxy = mainEndPoint.getServiceClusterProxy(clusterName);
							if (serviceClusterProxy == null) {
								Class clusterInterfaceClass = serviceCluster.getClusterInterfaceClass();
								serviceClusterProxy = new ServiceClusterProxy(this, clusterInterfaceClass, new IEndPointRequestContextCheck() {						
									public void checkRequestContext(IEndPointRequestContext context) throws ServiceClusterException {
										if (context != null) {
											String appliancePid = context.getPeerEndPoint().getAppliance().getPid();
											if (!isApplianceEnabled(appliancePid))
												throw new NotAuthorized("Invalid context: application not ready");
										}
									}
								});	
								mainEndPoint.addServiceClusterProxy(serviceClusterProxy);
							}
						}
						appEndPoint.addServiceCluster(serviceCluster);
					} catch (Exception e) {
						LOG.warn("Error while adding proxy service cluster" + serviceCluster.getName(), e);
					}
				}
			}
			addEndPoint(appEndPoint);
			applicationToProxyEndPointMap.put(listener, appEndPoint);		
		} catch (Exception e) {
			LOG.warn("Error while registering proxy application end point service clusters");
		}
	}	
	
	public synchronized void removeApplicationService(IApplicationService listener) {
		try {
			EndPointProxy appEndPoint = (EndPointProxy) applicationToProxyEndPointMap.get(listener);
			IServiceCluster[] serviceClusters = listener.getServiceClusters();
			if (serviceClusters != null) {
				for (int i = 0; i < serviceClusters.length; i++) {
					ServiceCluster serviceCluster = (ServiceCluster) serviceClusters[i];
					try {						
						appEndPoint.removeServiceCluster(serviceCluster);
						mainEndPoint.checkAndRemoveEmptyServiceClusterProxy(serviceCluster.getName()); 						
					} catch (Exception e) {
						LOG.warn("Error while removing proxy service cluster" + serviceCluster.getName(), e);
					}
				}
			}
			removeEndPoint(appEndPoint.getId());
			applicationToProxyEndPointMap.remove(listener);
			notifyAllAppliancesRemoved(listener, false);
			if (listener instanceof ICoreApplication) {
				notifyAllAppliancesRemoved(listener, true);
			}
		} catch (Exception e) {
			LOG.warn("Error while registering proxy application end point service clusters");
		}

	}	

	public synchronized void addAttributeValuesListener(IAttributeValuesListener listener) {
		synchronized (attributeValuesListenerList) {
			if (attributeValuesListenerList.contains(listener)) {
				LOG.warn("Dulicated listeners detected " + listener);
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
		ManagedApplianceStatus applianceStatus = (ManagedApplianceStatus) applianceMap.get(appliancePid);
		if (applianceStatus == null)
			applianceStatus = (ManagedApplianceStatus) installingApplianceMap.get(appliancePid);
		
		if (applianceStatus == null || applianceStatus.getStatus() != ManagedApplianceStatus.STATUS_ENABLED) {
			LOG.warn("notifyAttributeValue received from " +
					((applianceStatus == null) ? "an unknown" : "a not enabled ") + " appliance: " + 
						endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " " + clusterName + " " + 
						attributeName + " " + attributeValue.getTimestamp() + " " + attributeValue.getValue());
			return;
		}
		Integer endPointId = new Integer(endPointRequestContext.getPeerEndPoint().getId());
		LOG.debug("notifyAttributeValue: " + endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " " + endPointId +  " " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp()
				+ " " + attributeValue.getValue());		
		synchronized (attributeValuesListenerList) {
			for (Iterator iterator = attributeValuesListenerList.iterator(); iterator.hasNext();) {
				IAttributeValuesListener listener = (IAttributeValuesListener) iterator.next();
				try {
					listener.notifyAttributeValue(appliancePid, endPointId, clusterName, attributeName, attributeValue);				
				} catch (Exception e) {
					LOG.warn("Error while notifying attribute", e);
				}
			}
		}
	}

	public void notifyReadResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyReadResponse " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp() + " "
					+ attributeValue.getValue());
		}
	}

	public void notifyWriteResponse(String clusterName, String attributeName, IAttributeValue attributeValue,
			IEndPointRequestContext endPointRequestContext) throws ServiceClusterException, ApplianceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyWriteResponse " + clusterName + " " + attributeName + " " + attributeValue.getTimestamp()
					+ " " + attributeValue.getValue());
		}
	}

	public void notifyCommandResponse(String clusterName, String commandName, Object response,
			IEndPointRequestContext endPointRequestContext) throws ApplianceException {
		if (LOG.isDebugEnabled()) {
			LOG.debug(endPointRequestContext.getPeerEndPoint().getAppliance().getPid() + " - notifyCommandResponse " + clusterName + " " + commandName + " " + response);
		}
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

