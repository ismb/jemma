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
package org.energy_home.jemma.javagal.layers.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Properties;

public class ManageMapPanId {
	String filename;

	public ManageMapPanId() {
		try {
			filename = System.getProperty("user.home") + File.separator + "mapPainId.properties";
			File f = new File(filename);
			if (!f.exists())
				f.createNewFile();
			printFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public synchronized Integer getPanid(BigInteger address) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			String _res = properties.getProperty(address.toString());

			if (_res != null)
				return Integer.parseInt(_res,16);
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;

		}
	}

	public synchronized void setPanid(BigInteger address, String panId) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			String _oldvalue = properties.getProperty(address.toString());
			if (_oldvalue == null)
			{
				properties.setProperty(address.toString(), panId);
				OutputStream out = new FileOutputStream(filename);
				properties.store(out, null);
				out.flush();
				out.close();
			}
			else if (!_oldvalue.toLowerCase().equals(panId.toLowerCase())) {
				properties.setProperty(address.toString(), panId);
				OutputStream out = new FileOutputStream(filename);
				properties.store(out, null);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	void printFile() {

		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			Enumeration<?> e = properties.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				System.out.println(key + " -- " + properties.getProperty(key));
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

}
