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
package org.energy_home.jemma.internal.utils.thread;

import java.util.concurrent.ScheduledFuture;

import org.energy_home.jemma.utils.thread.ExecutorService;

public class ExecutorObject implements ExecutorService {
	private ExecutorManager executorManager;
	private String user;
	
	public ExecutorObject(String user) {
		this.user = user;
		this.executorManager = ExecutorManager.getInstance();
		executorManager.addUser(user);
	}
	
	public void release() {
		executorManager.removeUser(user);
	}
	
	public void addNearRealTimeOrderedTask(Runnable runnable) {
		executorManager.addNearRealTimeOrderedTask(user, runnable);	
	}

    public ScheduledFuture<?> scheduleTask(Runnable runnable, long delay, long period) {
		return executorManager.scheduleTask(user, runnable, delay, period);
	}
	
}
