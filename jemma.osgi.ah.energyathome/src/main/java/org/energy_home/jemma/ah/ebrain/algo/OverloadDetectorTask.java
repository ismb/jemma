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
package org.energy_home.jemma.ah.ebrain.algo;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import org.energy_home.jemma.ah.ebrain.old.SmartAppliance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OverloadDetectorTask extends TimerTask {
	public static final int DETECTION_INTERVAL = 5 * 1000; // 10 seconds
	public static final int SAME_EVENT_NOTIFICATION_DELAY = 20 * 1000; // 60 seconds
	
	public static final byte OVERLOAD_OVER_EVENT = 1;
	public static final byte OVERLOAD_UNDER_EVENT = 2;
	public static final byte SAFE_THRESHOLD_EVENT = 3;
	

    public static final float POWER_USAGE_WEIGHT = 1;
    public static final float ELAPSED_TIME_SWITCH_WEIGHT = .1f;
    public static final float SUSPENSION_PRIORITY_WEIGHT = 10000;
    public static final float REACTIVATION_PRIORITY_WEIGHT = SUSPENSION_PRIORITY_WEIGHT / 100;
	public static final int ELAPSED_TIME_WINDOW = 60 * 1000;
	
	private static final Logger LOG = LoggerFactory.getLogger( OverloadDetectorTask.class );
	
	private Timer timer = new Timer(true);
	private OverloadDetectorListener listener;
	private List<SmartAppliance> appliances = new CopyOnWriteArrayList<SmartAppliance>();
	private long lastNotifiedTimeTime;
	private long currentTime;
	private float totalPowerUsage = 0;
	private byte lastNotifiedEvent = 0;
	private float upperPowerThreshold, lowerPowerThreshold;
	
	public OverloadDetectorTask(OverloadDetectorListener l) {
		listener = l;
		setUpperPowerThreshold(3300.0f);
		timer.scheduleAtFixedRate(this, DETECTION_INTERVAL / 10, DETECTION_INTERVAL);
	}
	
	public void addAppliance(SmartAppliance a) {
		appliances.add(a);
	}
	
	void removeAppliance(SmartAppliance a) {
		appliances.remove(a);
	}
	
	void setTotalPowerUsage(float p) {
		totalPowerUsage = p;
	}
	
	void setUpperPowerThreshold(float up) {
		upperPowerThreshold = up;
		lowerPowerThreshold = up * .75f;
	}
	
	public void close() {
		timer.cancel();
	}
	
	
	
	public void run() {
		try {
			float power = computeTotalPowerUsage();
			LOG.debug("TotalPowerUsage: " + power);
			currentTime = System.currentTimeMillis();

			// check suspendable appliance and report the 1st appliance to switch based on priority
			if (power > upperPowerThreshold) { 
				LOG.debug("upperPowerThreshold exceeded by: " + (power - upperPowerThreshold));
				if (canNotifyEvent(OVERLOAD_OVER_EVENT)) {
					checkOverload();
				}
			// check resumable appliance and report the 1st appliance to switch based on priority
			} else if (power < lowerPowerThreshold) {
				LOG.debug("lowerPowerThreshold exceeded by: " + (lowerPowerThreshold - power));
				if (canNotifyEvent(OVERLOAD_UNDER_EVENT)) {
					checkUnderload();
				}
			} else { // here should be in the hysteresis window
				if (canNotifyEvent(SAFE_THRESHOLD_EVENT)) {
					lastNotifiedEvent = SAFE_THRESHOLD_EVENT;
					lastNotifiedTimeTime = currentTime;
					LOG.debug("notify overload re-entered.");
					listener.notifySafeLoad();
				}
			}
		} catch (Exception e) {
			LOG.error("Exception on run", e);
		}

	}
	
	private boolean canNotifyEvent(byte event) {
		return lastNotifiedEvent != event || currentTime - lastNotifiedTimeTime > SAME_EVENT_NOTIFICATION_DELAY;
	}
	
	private void checkUnderload() {
		SmartAppliance candidate = null;
		float weighedCandidate = 0;
		for (SmartAppliance a : appliances) {
			if (a.getState() == SmartAppliance.STATE_SUSPENDED) {
				float weighedPriority = weighedActivationPriority(a);
				LOG.debug("weighed Activation Priority " + a.getApplianceId() + " = " + weighedPriority);
				
				if (weighedPriority > weighedCandidate) {
					weighedCandidate = weighedPriority;
					candidate = a;
				}
			}
		}
		if (candidate != null) {
			candidate.setState(SmartAppliance.STATE_PENDING_ACTIVATION);
			lastNotifiedEvent = OVERLOAD_UNDER_EVENT;
			lastNotifiedTimeTime = currentTime;
			LOG.debug("notify underload for appliance: " + candidate.getApplianceId());
			listener.notifyUnderload(candidate);
		}
	}
	
	
	private void checkOverload() {
		SmartAppliance candidate = null;
		float weighedCandidate = 0;
		for (SmartAppliance a : appliances) {
			if (a.getState() == SmartAppliance.STATE_ACTIVE) {
				float weighedPriority = weighedSustensionPriority(a);
				LOG.debug("weighed Suspension Priority " + a.getApplianceId() + " = " + weighedPriority);
				
				if (weighedPriority > weighedCandidate) {
					weighedCandidate = weighedPriority;
					candidate = a;
				}
			}
		}
		if (candidate != null) {
			candidate.setState(SmartAppliance.STATE_PENDING_SUSPENSION);
			lastNotifiedEvent = OVERLOAD_OVER_EVENT;
			lastNotifiedTimeTime = currentTime;
			LOG.debug("notify overload for appliance: " + candidate.getApplianceId());
			listener.notifyOverload(candidate);
		}
	}
	
	private float weighedSustensionPriority(SmartAppliance a) {
		// weighed function = w1*time + w2*power + w3*priority
		float weighedPower = POWER_USAGE_WEIGHT * a.getIstantaneousPower();
		float weighedTime = ELAPSED_TIME_SWITCH_WEIGHT * Math.min(currentTime - a.getLastStateChange(), ELAPSED_TIME_WINDOW);
		float weighedPriority = SUSPENSION_PRIORITY_WEIGHT / (1 + a.getPriority());
		LOG.debug(String.format("weighed suspension: Pow[%.2f] Time[%.2f] Pry[%.2f]", weighedPower, weighedTime, weighedPriority));
		return weighedPower + weighedTime + weighedPriority;
	}
	
	private float weighedActivationPriority(SmartAppliance a) {
		// weighed function = w1*time + w2*power + w3*priority
		float weighedPower = POWER_USAGE_WEIGHT * (upperPowerThreshold - a.getIstantaneousPower()); // a lower consumption is preferred
		float weighedTime = ELAPSED_TIME_SWITCH_WEIGHT * Math.min(currentTime - a.getLastStateChange(), ELAPSED_TIME_WINDOW);
		float weighedPriority = REACTIVATION_PRIORITY_WEIGHT * a.getPriority();
		LOG.debug(String.format("weighed reactivation: Pow[%.2f] Time[%.2f] Pry[%.2f]", weighedPower, weighedTime, weighedPriority));
		return weighedPower + weighedTime + weighedPriority;		
	}
	
	

	public float computeTotalPowerUsage() {
		float p = totalPowerUsage;
		if (p == 0) {
			for (int i = appliances.size(); --i >= 0; p += appliances.get(i).getIstantaneousPower());
		}
		return p;
	}
}
