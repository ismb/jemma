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
package org.energy_home.jemma.internal.ah.hap.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.energy_home.jemma.m2m.ContentInstance;

public class HapContentInstancesQueue {
	private static final int MAX_QUEUE_SIZE = 100;

	private AHM2MContainerAddress containerId;
	private ArrayList<ContentInstance> contentInstancesList;
	private ContentInstance lastCreatedContentInstance;
	private boolean filterEqualContent = false;

	private static boolean compareContentInstances(ContentInstance ci1, ContentInstance ci2) {
		if (ci1 == null)
			return ci2 == null;
		if (ci2 == null)
			return false;
		Object content1 = ci1.getContent();
		Object content2 = ci2.getContent();
		// FIXME: currently work only for primitive data; it needs the equal
		// method to be implemented
		// in all ah jaxb generated classes (add an xml common object class?)
		if (content1 == null && content2 == null)
			return true;
		if ((content1 == null && content2 != null) ||
				(content1 != null && content2 == null))
			return false;
		if (content1.getClass().equals(content2.getClass()))
			return content1.equals(content2);
		// Following lines of code have been added to manage automatic int conversion done for locationPid 
		// and categoryPid attributes by jaxb parser 
		else if (content1 instanceof String)
			return content1.equals(content2.toString());
		else if (content2 instanceof String)
			return content2.equals(content1.toString());
		else 
			return false;
	}

	private void deleteInitialDuplicatedContentInstances(ContentInstance contentInstance) {
		if (filterEqualContent) {
			ContentInstance requestInstance = null;
			for (Iterator<ContentInstance> iterator = contentInstancesList.iterator(); iterator.hasNext();) {
				requestInstance = iterator.next();
				if (compareContentInstances(requestInstance, contentInstance))
					iterator.remove();
				else
					break;
			}
		}
	}

	public HapContentInstancesQueue(AHM2MContainerAddress containerId) {
		this(containerId, false);
	}

	public HapContentInstancesQueue(AHM2MContainerAddress containerId, boolean filterEqualContent) {
		this.containerId = containerId;
		this.filterEqualContent = filterEqualContent;
		contentInstancesList = new ArrayList<ContentInstance>(0);
	}

	public AHM2MContainerAddress getContainerId() {
		return containerId;
	}

	public int getQueueSize() {
		return contentInstancesList.size();
	}

	public synchronized void addContentInstance(ContentInstance contentInstance) {
		if (filterEqualContent && contentInstancesList.size() == 0
				&& compareContentInstances(lastCreatedContentInstance, contentInstance))
			return;
		if (filterEqualContent && contentInstancesList.size() > 0) {
			deleteInitialDuplicatedContentInstances(contentInstance);
		}
		contentInstancesList.add(contentInstance);
		while (contentInstancesList.size() >= MAX_QUEUE_SIZE)
			contentInstancesList.remove(0);
	}

	public synchronized ContentInstance getFirstContentInstance() {
		if (contentInstancesList.size() == 0)
			return null;
		return contentInstancesList.get(0);
	}

	public synchronized ContentInstance removeFirstContentInstance() {
		if (contentInstancesList.size() == 0)
			return null;
		return contentInstancesList.remove(0);
	}

	public synchronized ContentInstance getLastCreatedContentInstance() {
		return lastCreatedContentInstance;
	}

	public synchronized void setLastCreatedContentInstance(ContentInstance contentInstance) {
		lastCreatedContentInstance = contentInstance;
		if (contentInstance != null && filterEqualContent)
			deleteInitialDuplicatedContentInstances(lastCreatedContentInstance);
	}

}
