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
package org.energy_home.jemma.ah.upnp.energyathome;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPIcon;
import org.osgi.service.upnp.UPnPService;

public class HacUPnPDevice implements UPnPDevice {

	String udn;
	private static final String type = "schemas-upnp-org:device:Basic:1";
	private static final String friendlyName = "Energy@Home";
	private static final String man = "Telecom Italia S.p.A.";
	private static final String manURL = "http://www.telecomitalia.it";
	private static final String modelDesc = "Energy@Home Configuration Console";
	private static final String modelName = "Energy@Home";
	private static final String modelNumber = "1";
	private static final String modelURL = "http://www.telecomitalia.it/model.html";
	private static final String serialNumber = "1";
	private int upc = 776;

	protected Hashtable props = new Hashtable();
	protected UPnPService[] services = null;
	private Icon[] icons;

	public HacUPnPDevice() {

		udn = "uuid:BasicDevice-" + getHostname();

		props.put(UPnPDevice.UDN, new String(udn));
		props.put(UPnPDevice.TYPE, new String(type));
		props.put(UPnPDevice.FRIENDLY_NAME, new String(friendlyName));
		props.put(UPnPDevice.MANUFACTURER, new String(man));
		props.put(UPnPDevice.MANUFACTURER_URL, new String(manURL));
		props.put(UPnPDevice.MODEL_DESCRIPTION, new String(modelDesc));
		props.put(UPnPDevice.MODEL_NAME, new String(modelName));
		props.put(UPnPDevice.MODEL_NUMBER, new String(modelNumber));
		props.put(UPnPDevice.MODEL_URL, new String(modelURL));
		props.put(UPnPDevice.UPC, new String(Integer.toString(upc)));
		props.put(UPnPDevice.SERIAL_NUMBER, new String(serialNumber));
		props.put(org.osgi.service.device.Constants.DEVICE_CATEGORY, UPnPDevice.DEVICE_CATEGORY);
		props.put(UPnPDevice.UPNP_EXPORT, "YES");

		icons = new Icon[1];
		icons[0] = new Icon(16, 16, 256, "image/gif", "icon.gif");

		props.put("X_DLNADOC", new String("Test"));

		String presentationUrl = "http://" + getLocalIPAddress() + "/energyathome/index.html";
		props.put(UPnPDevice.PRESENTATION_URL, presentationUrl);

		services = new UPnPService[1];
		services[0] = new NullService();
	}

	public Dictionary getDescriptions(String locale) {
		return props;
	}

	public UPnPService getService(String serviceId) {
		return null;
	}

	public UPnPService[] getServices() {
		return services;
	}

	public UPnPIcon[] getIcons(String arg0) {
		return icons;
	}

	protected String getLocalIPAddress() {
		try {
			String hostname = InetAddress.getLocalHost().getHostAddress();
			return hostname;
		} catch (UnknownHostException e) {
		}
		return "";
	}

	protected String getHostname() {
		String comp = "";
		try {
			InetAddress inet = InetAddress.getLocalHost();
			comp = inet.getHostName();
		} catch (UnknownHostException uhe) {
		}

		return comp;
	}
}