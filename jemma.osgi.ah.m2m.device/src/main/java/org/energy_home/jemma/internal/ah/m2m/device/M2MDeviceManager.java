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
package org.energy_home.jemma.internal.ah.m2m.device;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfig;
import org.energy_home.jemma.ah.m2m.device.M2MDeviceConfigurator;
import org.energy_home.jemma.ah.m2m.device.M2MServiceException;
import org.energy_home.jemma.ah.m2m.device.lib.M2MDeviceListener;
import org.energy_home.jemma.m2m.M2MConstants;
import org.energy_home.jemma.m2m.connection.ConnectionParameters;
import org.energy_home.jemma.m2m.connection.DeviceConnectionParameters;
import org.energy_home.jemma.utils.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class M2MDeviceManager implements M2MDeviceConfigurator {
	private static final Logger LOG = LoggerFactory.getLogger( M2MDeviceManager.class );
	
	private static M2MDeviceManager instance;
	private static int referenceCounter = 0;

	public synchronized static M2MDeviceManager get() {
		if(instance==null)
		{
			instance= new M2MDeviceManager();
		}
		return instance;
	}

	// Following properties objects are initialized in the constructor (these
	// properties are persistent
	// between different startup and shutdown invocation)
	private M2MDeviceConfigObject deviceConfig;
	private DeviceConnectionParameters deviceConnectionParams;
	private M2MDeviceStatus deviceStatus;
	private ArrayList<M2MDeviceListener> listeners;
	private M2MNetworkSclManager networkSclManager;

	// Following properties are set to null by calling shutdown method (release
	// method called by
	// run method before terminating the thread)
	private HttpEntityXmlConverter jaxbConverterFactory;
	private RestClient restClient;
	private URI networkConnectionUri;
	private URI refreshNetworkConnectionUri;

	// Following properties are tied to connection/disconnection
	private ConnectionParameters connectionParams;

	// Following properties are instantiated only when needed
	private TimerTask connectTask;
	private TimerTask keepAliveTask;
	private Timer timer;

	private class M2MDeviceStatus {
		private boolean valid = false;
		private boolean connected = false;
		private int activeRequestsNumber = 0;
		private boolean connectTaskScheduled = false;
		private boolean keepAliveTaskScheduled = false;

		void incrementActiveRequests() {
			activeRequestsNumber++;
		}

		void decrementActiveRequests() {
			activeRequestsNumber--;
		}

		int getActiveRequests() {
			return activeRequestsNumber;
		}

		void setValid(boolean valid) {
			this.valid = valid;
		}

		boolean isValid() {
			return valid;
		}

		void setConnected(boolean connected) {
			this.connected = connected;
		}

		boolean isConnected() {
			return connected;
		}

		void setKeepAliveTaskScheduled(boolean keepAliveTaskScheduled) {
			this.keepAliveTaskScheduled = keepAliveTaskScheduled;
		}

		boolean isKeepAliveTaskScheduled() {
			return keepAliveTaskScheduled;
		}

		void setConnectTaskScheduled(boolean connectTaskScheduled) {
			this.connectTaskScheduled = connectTaskScheduled;
		}

		boolean isConnectTaskScheduled() {
			return connectTaskScheduled;
		}
	}

	private void connect() throws M2MServiceException {
		synchronized (deviceStatus) {
			HttpResponse response = null;
			try {
				if (deviceStatus.isConnected())
					// The following method can be called when keep alive method
					// fails with unauthorized exception
					disconnect();
				long now = System.currentTimeMillis();
				deviceConnectionParams.setTime(now);
				deviceConnectionParams.setTimeOffset(TimeZone.getDefault().getOffset(now));
				response = restClient.post(networkConnectionUri, jaxbConverterFactory.getEntity(deviceConnectionParams));
				M2MUtils.checkHttpResponseStatus(response);
				connectionParams = (ConnectionParameters) jaxbConverterFactory.getObject(response.getEntity());
				deviceConnectionParams.setRestarted(false);
				// SclId is tied to device id
				//deviceConfig.setNetworkSclBaseId(connectionParams.getId());
				if (connectionParams.getToken() != null)
					deviceConfig.setNetworkSclBaseToken(connectionParams.getToken());
				deviceConfig.setNetworkSclBaseUri(connectionParams.getNwkSclBaseId());
				networkSclManager.startup();
				refreshNetworkConnectionUri = new URI(networkConnectionUri.toString() + M2MConstants.URL_SLASH
						+ connectionParams.getId());
				deviceStatus.setConnected(true);
				notifyConnectionStatusToListeners();
			} catch (Exception e) {
				deviceStatus.setConnected(false);
				M2MUtils.mapDeviceException(LOG, e, "Error while setting up connection");
			} finally {
				restClient.consume(response);
			}
		}
	}

	private void disconnect() {
		synchronized (deviceStatus) {
			try {
				waitForAllNetworkSclRequestsCompletion();
				networkSclManager.shutdown();
				connectionParams = null;
				deviceStatus.setConnected(false);
				notifyConnectionStatusToListeners();
			} catch (Exception e) {
				LOG.error("Exception on disconnect", e);
			}
		}
	}
	
	private void notifyStartedStatusToListeners() {
		synchronized (deviceStatus) {
			if (listeners.size() > 0) {
				// Array copy is needed because inside this cycle a new listener
				// can be added
				// or removed inside the networkSclConnected method
				M2MDeviceListener[] listenersArray = new M2MDeviceListener[listeners.size()];
				listeners.toArray(listenersArray);		
				if (deviceStatus.isValid())
					for (int i = 0; i < listenersArray.length; i++) {
						listenersArray[i].deviceStarted();
					}
				else
					for (int i = 0; i < listenersArray.length; i++) {
						listenersArray[i].deviceStopped();
					}
			}
		}
	}
	
	private void notifyConfigUpdated() {
		synchronized (deviceStatus) {
			if (listeners.size() > 0) {
				// Array copy is needed because inside this cycle a new listener
				// can be added
				// or removed inside the networkSclConnected method
				M2MDeviceListener[] listenersArray = new M2MDeviceListener[listeners.size()];
				listeners.toArray(listenersArray);		
				for (int i = 0; i < listenersArray.length; i++) {
					listenersArray[i].deviceConfigUpdated();
				}
			}
		}
	}
	
	private void notifyConnectionStatusToListeners() {
		synchronized (deviceStatus) {
			if (listeners.size() > 0) {
				// Array copy is needed because inside this cycle a new listener
				// can be added
				// or removed inside the networkSclConnected method
				M2MDeviceListener[] listenersArray = new M2MDeviceListener[listeners.size()];
				listeners.toArray(listenersArray);		
				if (deviceStatus.isConnected())
					for (int i = 0; i < listenersArray.length; i++) {
						listenersArray[i].networkSclConnected();
					}
				else
					for (int i = 0; i < listenersArray.length; i++) {
						listenersArray[i].networkSclDisconnected();
					}
			}
		}
	}

	private boolean sendKeepAlive() throws M2MServiceException {
		synchronized (deviceStatus) {
			HttpResponse response = null;
			long keepAliveTimeout = connectionParams.getKeepAliveTimeout();
			try {
				long now = System.currentTimeMillis();
				deviceConnectionParams.setTime(now);
				deviceConnectionParams.setTimeOffset(TimeZone.getDefault().getOffset(now));
				response = restClient.put(refreshNetworkConnectionUri, jaxbConverterFactory.getEntity(deviceConnectionParams));
				M2MUtils.checkHttpResponseStatus(response);
				ConnectionParameters lastConnectionParams = (ConnectionParameters) jaxbConverterFactory.getObject(response
						.getEntity());
				if (keepAliveTimeout != lastConnectionParams.getKeepAliveTimeout()) {
					connectionParams.setKeepAliveTimeout(lastConnectionParams.getKeepAliveTimeout());
					return true;
				}
				return false;
			} catch (Exception e) {
				M2MUtils.mapDeviceException(LOG, e, "Error while sending connection keepalive");
				return false;
			} finally {
				restClient.consume(response);
			}
		}
	}

	private void scheduleConnectTask(long delay, long period) {
		synchronized (deviceStatus) {
			if (!deviceStatus.isConnectTaskScheduled()) {
				if (timer == null)
					timer = new Timer("M2MDevice Connection Timer", true);
				connectTask = new TimerTask() {
					public void run() {
						connectTask();
					}
				};
				try {
					if (period > 0)
						timer.schedule(connectTask, delay, period);
					else
						timer.schedule(connectTask, delay);
				} catch (Exception e) {
					LOG.error("Exception on scheduleConnectTask", e);
				}
				deviceStatus.setConnectTaskScheduled(true);
				LOG.debug("Connection setup task scheduled: delay=" + delay + ", period=" + period);
			}
		}
	}

	private void cancelConnectTask() {
		synchronized (deviceStatus) {
			if (deviceStatus.isConnectTaskScheduled()) {
				connectTask.cancel();
				connectTask = null;
				deviceStatus.setConnectTaskScheduled(false);
			}
		}
	}

	private void scheduleKeepAliveTask(long delay, long period) {
		synchronized (deviceStatus) {
			if (!deviceStatus.isKeepAliveTaskScheduled()) {
				if (timer == null)
					timer = new Timer("M2MDevice Keepalive Timer", true);
				keepAliveTask = new TimerTask() {
					public void run() {
						keepAliveTask();
					}
				};
				try {
					timer.schedule(keepAliveTask, delay, period);
				} catch (Exception e) {
					LOG.error("Exception on scheduleKeepAliveTask", e);
				}
				deviceStatus.setKeepAliveTaskScheduled(true);
				LOG.debug("Periodic keep alive task scheduled: delay=" + delay + ", period=" + period);
			}
		}
	}

	private void cancelKeepAliveTask() {
		synchronized (deviceStatus) {
			if (deviceStatus.isKeepAliveTaskScheduled()) {
				keepAliveTask.cancel();
				keepAliveTask = null;
				deviceStatus.setKeepAliveTaskScheduled(false);
			}
		}
	}

	private void cancelTimer() {
		synchronized (deviceStatus) {
			if (timer != null) {
				cancelKeepAliveTask();
				cancelConnectTask();
				timer.cancel();
				timer = null;
			}

		}
	}

	private void connectTask(boolean isStartup) {
		try {
			LOG.debug("M2M Connection setup task: isValid=" + deviceStatus.isValid() + ", isConnected=" + deviceStatus.isConnected());
			connect();
			cancelConnectTask();
			long keepAliveTimeout = connectionParams.getKeepAliveTimeout();
			LOG.debug("M2M Device connection has been established");
			if (keepAliveTimeout > 0) {
				LOG.debug("Periodic keep alive started " + keepAliveTimeout);
				restClient.setCredential(refreshNetworkConnectionUri.getHost(), refreshNetworkConnectionUri.getPort(),
						connectionParams.getId(), connectionParams.getToken());
				scheduleKeepAliveTask(keepAliveTimeout, keepAliveTimeout);
			} else {
				LOG.debug("No keep alive requested " + keepAliveTimeout);
				cancelTimer();
			}
		} catch (Exception e) {
			LOG.error("M2M Device connection setup task failed with exception", e);
			cancelKeepAliveTask();
			long retryTimeout = deviceConfig.getConnectionRetryTimeout();
			scheduleConnectTask(retryTimeout, retryTimeout);
		}
	}

	private void connectTask() {
		synchronized (deviceStatus) {
			if (deviceStatus.isConnectTaskScheduled())
				connectTask(false);
		}
	}

	private void keepAliveTask() {
		synchronized (deviceStatus) {
			if (deviceStatus.isKeepAliveTaskScheduled()) {
				try {
					LOG.debug("M2M Connection keep alive task: isValid=" + deviceStatus.isValid() + ", isConnected="
							+ deviceStatus.isConnected());
					boolean timeoutChanged = sendKeepAlive();
					LOG.debug("M2M Device connection keepalive sent");
					if (timeoutChanged) {
						cancelKeepAliveTask();
						LOG.debug("Timeout changed, cancelled scheduled keep alive task");
						long timeout = connectionParams.getKeepAliveTimeout();
						if (timeout > 0) {
							LOG.debug("New periodic keep alive task scheduled  " + timeout);
							scheduleKeepAliveTask(timeout, timeout);
						} else {
							cancelTimer();
						}
					}
				} catch (Exception e) {
					LOG.error("M2M Device connection keep alive failed with exception", e);
					// All error are currently managed by restarting the connection; a more restrictive test could be performed
					// e.g. (e instanceof M2MUnauthorizedException)
					cancelKeepAliveTask();
					restClient.setCredential(networkConnectionUri.getHost(), networkConnectionUri.getPort(),
							deviceConfig.getConnectionId(), deviceConfig.getConnectionToken());
					long retryTimeout = deviceConfig.getConnectionRetryTimeout();
					scheduleConnectTask(retryTimeout, retryTimeout);
				}
			}
		}
	}

	private void startup() throws M2MServiceException {
		synchronized (deviceStatus) {
			LOG.debug("M2M Device startup requested: " + deviceConfig.getProperties());
			
			if (!deviceConfig.isLocalOnly()) {		
				if (!deviceConfig.isValid())
					throw new M2MConfigException("Startup method failed: incomplete configuration");
	
				if (deviceStatus.isValid()) {
					LOG.debug("M2M Device already started");
					return;
				}
				jaxbConverterFactory = HttpEntityXmlConverter.getConnectionConverter();
				try {
					
					restClient = RestClient.get();
				
					networkConnectionUri = new URI(deviceConfig.getConnectionBaseUri());
				} catch (URISyntaxException e) {
					M2MUtils.mapDeviceException(LOG, e, "Invalid base uri configuration");
				}
				restClient.setCredential(networkConnectionUri.getHost(), networkConnectionUri.getPort(),
						deviceConfig.getConnectionId(), deviceConfig.getConnectionToken());
			}
			deviceStatus.setValid(true);
			// First connection is scheduled so that OSGi framework startup is
			// not blocked by timeout on network connection
			notifyStartedStatusToListeners();
			if (!deviceConfig.isLocalOnly()) {
				scheduleConnectTask(100, deviceConfig.getConnectionRetryTimeout());
				}
			else {
				LOG.debug("M2M Device local only configuration");
			}
			
			LOG.debug("M2M Device startup completed");
		}
	}

	private void shutdown() {
		synchronized (deviceStatus) {
			LOG.debug("M2M Device shutdown requested");

			cancelTimer();
			if (deviceStatus.isConnected())
				disconnect();
			if (restClient != null)
				restClient.release();

			jaxbConverterFactory = null;
			restClient = null;
			networkConnectionUri = null;
			refreshNetworkConnectionUri = null;
			deviceStatus.setValid(false);
			notifyStartedStatusToListeners();
			LOG.debug("M2M Device shutdown completed");
		}
	}

	void waitForAllNetworkSclRequestsCompletion() {
		synchronized (deviceStatus) {
			while (deviceStatus.getActiveRequests() > 0)
				try {
					deviceStatus.wait();
				} catch (InterruptedException e) {
					LOG.error("InterruptedException on waitForAllNetworkSclRequestsCompletion", e);
				}
		}
	}

	void signalNetworkSclRequest(boolean checkConnectedStatus) throws M2MServiceException {
		synchronized (deviceStatus) {
			if (!deviceStatus.isValid() || (checkConnectedStatus && !deviceStatus.isConnected()))
				throw new M2MServiceException("M2M device status check failed");
			deviceStatus.incrementActiveRequests();
		}
	}

	void signalNetworkSclRequestCompletion(boolean newConnectionRequired) {
		synchronized (deviceStatus) {
			deviceStatus.decrementActiveRequests();
			if (newConnectionRequired && deviceStatus.isValid() && !deviceStatus.isConnectTaskScheduled()) {
				cancelKeepAliveTask();
				restClient.setCredential(networkConnectionUri.getHost(), networkConnectionUri.getPort(),
						deviceConfig.getConnectionId(), deviceConfig.getConnectionToken());
				long retryTimeout = deviceConfig.getConnectionRetryTimeout();
				scheduleConnectTask(retryTimeout, retryTimeout);
			}
			deviceStatus.notifyAll();
		}
	}

	private M2MDeviceManager() {
		deviceConfig = new M2MDeviceConfigObject();
		deviceConnectionParams = new DeviceConnectionParameters();
		if (M2MConstants.CLIENT_VERSION != null)
			deviceConnectionParams.setVersion(M2MConstants.CLIENT_VERSION);
		deviceConnectionParams.setRestarted(true);
		deviceStatus = new M2MDeviceStatus();
		networkSclManager = new M2MNetworkSclManager(this);
		listeners = new ArrayList<M2MDeviceListener>();
	}

	public M2MNetworkSclManager getNetworkSclManager() {
		return this.networkSclManager;
	}

	public void addListener(M2MDeviceListener listener) {
		synchronized (deviceStatus) {
			if (listener != null) {
				listeners.remove(listener);
				listeners.add(listener);
				if (deviceStatus.isValid())
					listener.deviceStarted();
				if (deviceStatus.isConnected())
					listener.networkSclConnected();
			}
		}
	}

	public void removeListener(M2MDeviceListener listener) {
		synchronized (deviceStatus) {
			if (listener != null) {
				listeners.remove(listener);
			}
		}
	}

	public void addReference() {
		boolean startupRequired = false;
		synchronized (this) {
			referenceCounter++;
			if (referenceCounter == 1)
				startupRequired = true;
		}
		if (startupRequired)
			try {
				startup();
			} catch (M2MConfigException e) {
				LOG.warn("No valid configuration has been found");
			} catch (Exception e) {
				LOG.error("An error occcured while starting M2MDevice", e);
			}
		LOG.debug("Added reference " + referenceCounter);
	}

	public void removeReference() {
		boolean shutdownRequired = false;
		synchronized (this) {
			referenceCounter--;
			if (referenceCounter == 0)
				shutdownRequired = true;
		}
		if (shutdownRequired)
			shutdown();
		LOG.debug("Removed reference " + referenceCounter);
	}

	// M2MDevice interface implementation
	public boolean isStarted() {
		return deviceStatus.isValid();
	}

	public boolean isConnected() {
		return deviceStatus.isConnected();
	}

	public M2MDeviceConfig getConfiguration() {
		return deviceConfig;
	}

	public void setConfiguration(M2MDeviceConfig config) throws M2MServiceException {
		synchronized (deviceStatus) {
			if (deviceStatus.isValid())
				shutdown();
			deviceConfig = M2MDeviceConfigObject.updateConfigProperties((M2MDeviceConfigObject) config);
			networkSclManager.setDeviceConfig(deviceConfig);	
			notifyConfigUpdated();		
			if (config != null)
				startup();
		}
	}

	public ConnectionParameters getCurrentConnectionParameters() {
		return connectionParams;
	}
}
