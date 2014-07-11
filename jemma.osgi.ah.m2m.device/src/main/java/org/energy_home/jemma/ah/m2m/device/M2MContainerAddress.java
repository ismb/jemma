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
package org.energy_home.jemma.ah.m2m.device;


import java.security.InvalidParameterException;
import java.util.StringTokenizer;

import org.energy_home.jemma.m2m.M2MConstants;

public class M2MContainerAddress {	
	public static final String ALL_ID_FILTER = "ALL";
	public static final String POSITIVE_ID_FILTER = "POS";
	
	private static final String INVALID_CONTAINER_ID_MSG = "Invalid container id";
	
	private static final String ENDS_WITH_FILTER_STRING = ".";
	
	private static final String[] RESERVED_WORDS = { "CS", "CIS" };
	private static final String[] FILTER_WORDS = { ALL_ID_FILTER, POSITIVE_ID_FILTER };

	private static final boolean isNullOrEmpty(String s) {
		return (s == null || (s.length() == 0));
	}
	
	private static boolean matchFilterId(String containerPart, String filterPart) {
		if (containerPart == null || filterPart == null)
			return false;
		if (filterPart.equals(ALL_ID_FILTER) ||
				filterPart.endsWith(ENDS_WITH_FILTER_STRING) && containerPart.startsWith(filterPart))
			return true;
		if (filterPart.equals(POSITIVE_ID_FILTER)) {
			try {
				Integer intId = Integer.parseInt(containerPart);
				if (intId > 0)
					return true;
			} catch (Exception e) {
			}
		}
		
		return false;
	}
	
	
	public static boolean isFilterWord(String id) {
		if (id == null)
			return false;
		for (int j = 0; j < FILTER_WORDS.length; j++) {
			if (id.equals(FILTER_WORDS[j]))
				return true;
		}
		return false;
	}
	
	public static boolean isFilterId(String id) {
		if (id == null)
			return false;
		for (int j = 0; j < FILTER_WORDS.length; j++) {
			if (id.equals(FILTER_WORDS[j]) || id.endsWith(ENDS_WITH_FILTER_STRING))
				return true;
		}
		return false;
	}
	
	public static boolean match(M2MContainerAddress containerId, M2MContainerAddress filterId) {
		// FilterId is not necessary a filter, but container id cannot be a
		// filter
		if (containerId.isFilterAddress())
			throw new InvalidParameterException("First parameter cannot be a container id filter");
		String[] containerIds = containerId.getContainerIdParts();
		String[] filterIds = filterId.getContainerIdParts();
		if (containerIds.length != filterIds.length)
			return false;
		boolean isFilterId;
		for (int i = 0; i < filterIds.length; i++) {
			isFilterId = isFilterId(filterIds[i]);
			if ((!isFilterId && !containerIds[i].equals(filterIds[i])) ||
					(isFilterId && !matchFilterId(containerIds[i], filterIds[i])))
				return false;
		}
		return true;
	}

	
	protected boolean isLocal = false;
	protected boolean isProxy = false;
	protected boolean isFilter = false;

	protected String url = null;
	protected String cisUrl = null;
	
	protected String sclId = null;
	protected String[] parts = null;

	public M2MContainerAddress(String contentInstancesOrContainerUrl) throws IllegalArgumentException {
		if (contentInstancesOrContainerUrl.endsWith(M2MConstants.URL_SLASH))
			contentInstancesOrContainerUrl = contentInstancesOrContainerUrl.substring(0, contentInstancesOrContainerUrl.length()-1);
		String strUri = contentInstancesOrContainerUrl;
		int index = -1;
		if ((index = strUri.indexOf(M2MConstants.URL_HAG_SCL_BASE)) >= 0) {
			strUri = strUri.substring(index+M2MConstants.URL_HAG_SCL_BASE.length());
			isLocal = true;
//			strUri = strUri.substring(index+M2MConstants.URL_HAG_SCL_BASE.length());
//			index = strUri.indexOf(M2MConstants.URL_CONTAINERS);
//			if (index < 0)
//				throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
//			if (index > 0) {
//				isProxy = true;
//				sclId = strUri.substring(1, index);
//			} else {
//				isLocal = true;				
//			}
//			strUri = strUri.substring(index+M2MConstants.URL_CONTAINERS.length());
		} 
		if ((index = strUri.indexOf(M2MConstants.URL_SCL_BASE)) >= 0) {
			strUri = strUri.substring(index+M2MConstants.URL_SCL_BASE.length());
		}
		if (strUri.startsWith(M2MConstants.URL_SCLS)) {
			strUri = strUri.substring(M2MConstants.URL_SCLS.length());
			index = strUri.indexOf(M2MConstants.URL_CONTAINERS);
			if (index <= 1)
				throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
			sclId = strUri.substring(1, index);
			strUri = strUri.substring(index);
		} 
		if (!strUri.startsWith(M2MConstants.URL_CONTAINERS)) {
			throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
		}
		strUri = strUri.substring(M2MConstants.URL_CONTAINERS.length());
//		}
		index = strUri.indexOf(M2MConstants.URL_CONTENT_INSTANCES);
		if (index >= 0) {
			if (index <= 1)
				throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
			url = contentInstancesOrContainerUrl.substring(0, contentInstancesOrContainerUrl.length()-M2MConstants.URL_CONTENT_INSTANCES.length()); 
			cisUrl = contentInstancesOrContainerUrl;
			strUri = strUri.substring(0, index);
		} else {
			url = contentInstancesOrContainerUrl;
			cisUrl = contentInstancesOrContainerUrl + M2MConstants.URL_CONTENT_INSTANCES;
		}	
		StringTokenizer st = new StringTokenizer(strUri, M2MConstants.URL_SLASH);
		if (st.countTokens() < 1) {
			parts = new String[1];
			parts[0] = strUri;
		} else {
			parts = new String[st.countTokens()];
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < parts.length; i++) {
				parts[i] = st.nextToken();
				if (parts[i] == null || parts[i].equals(""))
					throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
				if (i != 0)
					sb.append(M2MConstants.URL_SLASH);
				sb.append(parts[i]);
				if (isFilterId(parts[i]))
					isFilter = true;
			}
		}		
		if (parts[parts.length-1].endsWith(ENDS_WITH_FILTER_STRING))
			isFilter = true;
	}

	public M2MContainerAddress(String parts[]) throws IllegalArgumentException {
		this(null, parts, false, false);
	}
	
	public M2MContainerAddress(String parts[], boolean isLocal) throws IllegalArgumentException {
		this(null, parts, isLocal, false);
	}
	
	public M2MContainerAddress(String sclId, String[] parts) throws IllegalArgumentException {
		this(sclId, parts, false, false);
	}
	
	public M2MContainerAddress(String sclId, String[] parts, boolean isProxy) throws IllegalArgumentException {
		this(sclId, parts, false, isProxy);
	}
	
	public M2MContainerAddress(String remoteSclId, String[] containerId, boolean isLocal, boolean isProxy) throws IllegalArgumentException {
		if ((isProxy && isLocal) || (isProxy && remoteSclId == null) || (isLocal && remoteSclId == null) ||
				containerId == null || containerId.length == 0)
			throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
		// TODO: add here a check on illegal chars (e.g. space, newline, ...)
		this.sclId = remoteSclId;
		this.isProxy = isProxy;
		this.isLocal = isLocal;
		StringBuilder sb = new StringBuilder();
		if (!isProxy && !isLocal) {
			sb.append(M2MConstants.URL_SCL_BASE);
		} else {
			sb.append(M2MConstants.URL_HAG_SCL_BASE);
		} 
		if (remoteSclId != null) {
			sb.append(M2MConstants.URL_SCLS);
			sb.append(M2MConstants.URL_SLASH);
			sb.append(remoteSclId);
		}
		sb.append(M2MConstants.URL_CONTAINERS);
		this.parts = new String[containerId.length];
		for (int i = 0; i < containerId.length; i++) {
			for (int j = 0; j < RESERVED_WORDS.length; j++) {
				if (containerId[i] == ALL_ID_FILTER)
					isFilter = true;
				if (isNullOrEmpty(containerId[i]) || containerId[i].equals(RESERVED_WORDS[j]))
					throw new IllegalArgumentException(INVALID_CONTAINER_ID_MSG);
				else
					this.parts[i] = containerId[i];
			}
			sb.append(M2MConstants.URL_SLASH);
			sb.append(this.parts[i]);
		}
		if (parts[parts.length-1].endsWith(ENDS_WITH_FILTER_STRING))
			isFilter = true;
		url = sb.toString();
		sb.append(M2MConstants.URL_CONTENT_INSTANCES);
		cisUrl = sb.toString();
	}

	public String getUrl() {
		return url;
	}
	
	public String getContentInstancesUrl() {
		return cisUrl;
	}

	public boolean isLocalAddress() {
		return isLocal;
	}	
	
	public boolean isProxyAddress() {
		return isProxy;
	}
	
	public boolean isFilterAddress() {
		return isFilter;
	}
	
	public String getSclId() {
		return sclId;
	}
	
	public String[] getContainerIdParts() {
		return parts;
	}

	public String toString() {
		return url;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (obj.getClass() != this.getClass()))
			return false;

		M2MContainerAddress id = (M2MContainerAddress) obj;
		if (url == id.url)
			return true;
		else if (url != null)
			return url.equals(id.url);
		return false;
	}

	public int hashCode() {
		int hashCode = 11;
		hashCode = hashCode * 23 + ((url != null) ? url.hashCode() : 0);
		return hashCode;
	}
	
//	public static void print(M2MContainerAddress containerId) {
//		System.out.println("-----------------------------------");
//		System.out.println("Url:    " + containerId.getUrl());
//		System.out.println("CisUrl: " + containerId.getContentInstancesUrl());
//		System.out.println("isLocal: " + containerId.isLocalAddress());
//		System.out.println("isProxy: " + containerId.isProxyAddress());
//		System.out.println("isFilter: " + containerId.isFilterAddress());
//		System.out.println("SclId: " + containerId.getSclId());
//		String[] parts = containerId.getContainerIdParts();
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < parts.length; i++) {
//			sb.append(parts[i]);
//			if (i < parts.length-1)
//				sb.append(",");
//		}
//		System.out.println("Parts: " + sb.toString());
//		System.out.println("-----------------------------------");
//	}
//	
//	public static final void main(String[] args) {
//		// Remote scl containers
//		M2MContainerAddress containerId = new M2MContainerAddress("/HAP/SC/SB/SCLS/hag-0001/CS/ah.app.1/1/ah.app.test/");
//		print(containerId);
//		containerId = new M2MContainerAddress("/HAP/SC/SB/SCLS/hag-0001/CS/ah.app.1/1/ah.app.test/CIS/");
//		print(containerId);
//		containerId = new M2MContainerAddress("hag-0001", new String[] {"ah.app.1", "1", "ah.app.test"});
//		print(containerId);
//		
//		M2MContainerAddress filterContainerId = new M2MContainerAddress("/HAP/SC/SB/SCLS/hag-0001/CS/ALL/1/ah.app.test");
//		print(filterContainerId);
//		filterContainerId = new M2MContainerAddress("/HAP/SC/SB/SCLS/hag-0001/CS/ALL/1/ah.app.test/CIS");
//		print(filterContainerId);
//		filterContainerId = new M2MContainerAddress("hag-0001", new String[] {"ALL", "1", "ah.app.test"});
//		print(filterContainerId);
//		
//		if (M2MContainerAddress.match(containerId, filterContainerId))
//			System.out.println("Match ok");
//		
//		// Remote containers
//		containerId = new M2MContainerAddress("/HAP/SC/SB/CS/ah.app.test");
//		print(containerId);	
//		containerId = new M2MContainerAddress("/HAP/SC/SB/CS/ah.app.test/CIS");
//		print(containerId);	
//		containerId = new M2MContainerAddress(new String[] {"ah.app.test"});
//		print(containerId);	
//		
//		filterContainerId = new M2MContainerAddress("/HAP/SC/SB/CS/ALL");
//		print(filterContainerId);	
//		filterContainerId = new M2MContainerAddress("/HAP/SC/SB/CS/ALL/CIS");
//		print(filterContainerId);	
//		filterContainerId = new M2MContainerAddress(new String[] {"ALL"});
//		print(filterContainerId);	
//		
//		if (M2MContainerAddress.match(containerId, filterContainerId))
//			System.out.println("Match ok");
//		
//		// Local containers
//		containerId = new M2MContainerAddress("/HAG/SC/SB/CS/ah.app.1/1/ah.app.test");
//		print(containerId);	
//		containerId = new M2MContainerAddress("/HAG/SC/SB/CS/ah.app.1/1/ah.app.test/CIS");
//		print(containerId);	
//		containerId = new M2MContainerAddress(new String[] {"ah.app.1", "1", "ah.app.test"}, true);
//		print(containerId);
//		
//		filterContainerId = new M2MContainerAddress("/HAG/SC/SB/CS/ALL/1/ah.app.test");
//		print(filterContainerId);	
//		filterContainerId = new M2MContainerAddress("/HAG/SC/SB/CS/ALL/1/ah.app.test/CIS");
//		print(filterContainerId);	
//		filterContainerId = new M2MContainerAddress(new String[] {"ALL", "1", "ah.app.test"}, true);
//		print(filterContainerId);
//		
//		if (M2MContainerAddress.match(containerId, filterContainerId))
//			System.out.println("Match ok");
//		 
//		// Remote proxy containers
//		containerId = new M2MContainerAddress("/HAG/SC/SB/hag-0002/CS/ah.app.1/1/ah.app.test");
//		print(containerId);	
//		containerId = new M2MContainerAddress("/HAG/SC/SB/hag-0002/CS/ah.app.1/1/ah.app.test/CIS");
//		print(containerId);	
//		containerId = new M2MContainerAddress("hag-0002", new String[] {"ah.app.1", "1", "ah.app.test"}, true);
//		print(containerId);
//		
//		filterContainerId = new M2MContainerAddress("/HAG/SC/SB/hag-0002/CS/ALL/1/ah.app.test");		
//		print(filterContainerId);
//		filterContainerId = new M2MContainerAddress("/HAG/SC/SB/hag-0002/CS/ALL/1/ah.app.test");		
//		print(filterContainerId);
//		filterContainerId = new M2MContainerAddress("hag-0002", new String[] {"ALL", "1", "ah.app.test"}, true);
//		print(filterContainerId);
//		
//		if (M2MContainerAddress.match(containerId, filterContainerId))
//			System.out.println("Match ok");
//	}

}
