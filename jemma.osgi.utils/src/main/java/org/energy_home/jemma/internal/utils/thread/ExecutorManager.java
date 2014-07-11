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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExecutorManager {
	private static final Log log = LogFactory.getLog(ExecutorManager.class);
	
	private static final long NEAR_REAL_TIME_DELAY = 100;
//  TODO: check for execution interruption (currenlty not used): it seems working only if a thread sleep is added to the task
//	private static final Long MAX_EXECUTION_DURATION = new Long(5000);		
	private static final long SHUTDOWN_TIMEOUT = 10000;
	
	private static final ExecutorManager instance = new ExecutorManager();
	
	public static ExecutorManager getInstance() {
		return instance;
	}
	
	private class UserTasks implements Runnable {
		private List<Runnable> runnableList;
		private Future<?> lastExecutionFuture;
		private Long maxExecutionTime;
		private List<ScheduledUserTask> scheduledList;
		
		UserTasks(Long maxExecutionTime) {
			this.runnableList = new LinkedList<Runnable>();
			this.scheduledList = new LinkedList<ScheduledUserTask>();
			this.maxExecutionTime = maxExecutionTime;
		}
		
		void addTask(Runnable task) {
			synchronized (runnableList) {
				runnableList.add(task);
			}
		}
		
		void waitForTasksCompletion() {
			try {
				synchronized (runnableList) {
					try {
						if (lastExecutionFuture != null)						
							lastExecutionFuture.get();				
					} catch (Exception e) {
						log.error("cancelAllTasks error while waiting for current task execution" , e);
					}
					while (runnableList.size() > 0)
						run();
				}				
			} catch (Exception e) {
				log.error("cancelAllTasks error", e);
			}
		}
		
		void addScheduledTask(ScheduledUserTask task) {
			synchronized (scheduledList) {
				scheduledList.add(task);
			}
		}
		
		void cancelAllScheduledTasks() {
			try {
				synchronized (scheduledList) {
					ScheduledUserTask scheduledTask;
					for (Iterator<ScheduledUserTask> iterator = scheduledList.iterator(); iterator.hasNext();) {
						scheduledTask = (ScheduledUserTask) iterator.next();
						scheduledTask.scheduledFuture.cancel(true);
						iterator.remove();
					}
				}				
			} catch (Exception e) {
				log.error("cancelAllScheduledTasks error", e);
			}
		}

		public void run() {
			synchronized (scheduledList) {
				try {
					// Remove completed scheduled tasks
					if (scheduledList.size() > 0) {
						ScheduledUserTask task;
						for (Iterator<ScheduledUserTask> iterator = scheduledList.iterator(); iterator.hasNext();) {
							task = (ScheduledUserTask) iterator.next();
							if (task.scheduledFuture.isDone())
								iterator.remove();					
						}		
					}					
				} catch (Exception e) {
					log.error("UserTasks run: error while removing scheduled completed tasks", e);
				}
			}			
			synchronized (runnableList) {
				if (runnableList.size() > 0) {
					try {
						if (lastExecutionFuture != null)
							lastExecutionFuture.get();	
					} catch (Exception e) {
						log.error("UserTasks run: error while waiting previous task execution", e);
					}
					final Runnable runnable = runnableList.remove(0);
					numberOfOrderedTasks--;
					try {
						if (maxExecutionTime == null) {
							runnable.run();
						} else {
							final Future<?> future = scheduler.submit(runnable);
							canceller.schedule(new Runnable() {
								public void run() {
									if (!future.isDone()) {
										try {
											log.error("UserTasks: timeout exceeded, cancelling execution: " + runnable);
											future.cancel(true);											
										} catch (Exception e) {
											log.error("UserTasks: error while cancelling for timeout: " + runnable);
										}
									}
								}
							}, maxExecutionTime, TimeUnit.MILLISECONDS);	
							lastExecutionFuture = future;
						}
					} catch (Exception e) {
						log.error("UserTasks run error " + runnable, e);
					}
				}
			}
		}
	}
	
	private class ScheduledUserTask implements Runnable {
		private Runnable runnable;
		private ScheduledFuture<?> scheduledFuture;
		private Long maxExecutionTime;

		ScheduledUserTask(Runnable runnable, long delay, Long period, Long maxExecutionTime) {
			this.runnable = runnable;
			this.maxExecutionTime = maxExecutionTime;
			if (period == null)
				this.scheduledFuture = scheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
			else 
				this.scheduledFuture = scheduler.scheduleWithFixedDelay(this, delay, period.longValue(), TimeUnit.MILLISECONDS);
		}

		ScheduledFuture<?> getScheduledFuture() {
			return scheduledFuture;
		}	

		public void run() {
			try {	
				if (maxExecutionTime == null) {
					runnable.run();
				} else {
					final Future<?> future = scheduler.submit(runnable);
					canceller.schedule(new Runnable() {
						public void run() {
							if (!future.isDone()) {
								try {
									log.error("ScheduledUserTask: timeout exceeded, cancelling execution: " + runnable);
									future.cancel(true);	
								} catch (Exception e) {
									log.error("ScheduledUserTask: error while cancelling for timeout: " + runnable);
								}
							}
						}
					}, maxExecutionTime, TimeUnit.MILLISECONDS);
				}
			} catch (Exception e) {
				log.error("ScheduledUserTask run error " + runnable, e);
			} 
		}		
	}	
		
	private ScheduledExecutorService scheduler;
	private ScheduledExecutorService canceller;
	private Map<String, UserTasks> nearRealTimeOrderedTasksMap;
	private int numberOfOrderedTasks = 0;
	private Runnable nearRealTimeOrderedTask;
	private ScheduledFuture<?> nearRealTimeMainTaskFuture;
	
	private class SchedulerThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			return new Thread(r, "Executor Scheduler Thread");
		}
	}
	private class CancellerThreadFactory implements ThreadFactory {
		public Thread newThread(Runnable r) {
			return new Thread(r, "Executor Canceller Thread");
		}
	}	
	
	public ExecutorManager() {
		log.info("Calling constructor...");
		nearRealTimeOrderedTasksMap = new HashMap<String, UserTasks>();
	}
	
	private void start() {
		log.info("ExecutorManager starting...");
		numberOfOrderedTasks = 0;
		scheduler = Executors.newScheduledThreadPool(1, new SchedulerThreadFactory());
		canceller = Executors.newScheduledThreadPool(1, new CancellerThreadFactory());
		nearRealTimeOrderedTask = new Runnable() {
			public void run() {
				try {
					UserTasks[] tasksArray = null;
					synchronized (nearRealTimeOrderedTasksMap) {
						if (nearRealTimeOrderedTasksMap.size() > 0) {
							tasksArray = new UserTasks[nearRealTimeOrderedTasksMap.size()];
							nearRealTimeOrderedTasksMap.values().toArray(tasksArray);
						}
					}
					if (tasksArray != null) {
						for (int i = 0; i < tasksArray.length; i++) {
							tasksArray[i].run();
						}	
					}
					synchronized (nearRealTimeOrderedTasksMap) {
						if (numberOfOrderedTasks == 0)
							nearRealTimeMainTaskFuture.cancel(true);
					}
				} catch (Exception e) {
					log.error("nearRealTimeTask run error", e);
				}
			}
		};
	}
	
	private void stop() {
		log.info("ExecutorManager stopping...");
		scheduler.shutdown();
		try {
			scheduler.awaitTermination(SHUTDOWN_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			log.error("scheduler await termination error", e);
		}
		canceller.shutdown();
		try {
			canceller.awaitTermination(0, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("canceller await termination error", e);
		}
		nearRealTimeOrderedTasksMap.clear();
	}	
	
	private UserTasks getUserTasks(String user) {
		synchronized (nearRealTimeOrderedTasksMap) {
			return nearRealTimeOrderedTasksMap.get(user);
		}
	}
	
	public void addUser(String user) {
		log.info("Adding user " + user);
		synchronized (nearRealTimeOrderedTasksMap) {
			if (nearRealTimeOrderedTasksMap.size() == 0)
				start();
			nearRealTimeOrderedTasksMap.put(user, new UserTasks(null));
		}
	}
	
	public void removeUser(String user) {
		log.info("Removing user " + user);
		UserTasks tasks;
		synchronized (nearRealTimeOrderedTasksMap) {
			tasks = nearRealTimeOrderedTasksMap.remove(user);
		}
		if (tasks != null) {
			tasks.cancelAllScheduledTasks();
			tasks.waitForTasksCompletion();
		}
		synchronized (nearRealTimeOrderedTasksMap) {
			if (nearRealTimeOrderedTasksMap.size() == 0) {
				stop();
				return;
			}
		}
	}
	
	public void addNearRealTimeOrderedTask(String user, Runnable runnable) {
		synchronized (nearRealTimeOrderedTasksMap) {
			UserTasks tasks = nearRealTimeOrderedTasksMap.get(user);
			if (tasks == null)
				return;
			tasks.addTask(runnable);
			if (numberOfOrderedTasks == 0)
				nearRealTimeMainTaskFuture = scheduler.scheduleWithFixedDelay(nearRealTimeOrderedTask, NEAR_REAL_TIME_DELAY, NEAR_REAL_TIME_DELAY, TimeUnit.MILLISECONDS);
			numberOfOrderedTasks++;
		}
	}
	
	public ScheduledFuture<?> scheduleTask(String user, Runnable runnable, long delay) {
		synchronized (nearRealTimeOrderedTasksMap) {
			UserTasks tasks = getUserTasks(user);
			if (tasks == null)
				return null;
			ScheduledUserTask scheduledTask = new ScheduledUserTask(runnable, delay, null, null);
			tasks.addScheduledTask(scheduledTask);
			return scheduledTask.getScheduledFuture();
		}
	}
	
	public ScheduledFuture<?> scheduleTask(String user, Runnable runnable, long delay, long period) {
		synchronized (nearRealTimeOrderedTasksMap) {
			UserTasks tasks = getUserTasks(user);
			if (tasks == null)
				return null;
			ScheduledUserTask scheduledTask = new ScheduledUserTask(runnable, delay, new Long(period), null);
			tasks.addScheduledTask(scheduledTask);
			return scheduledTask.getScheduledFuture();
		}	
	}
}
