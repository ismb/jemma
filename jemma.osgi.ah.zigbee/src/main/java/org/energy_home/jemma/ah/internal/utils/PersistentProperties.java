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
package org.energy_home.jemma.ah.internal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

public class PersistentProperties extends Properties {	
	private URL url;
	private Properties properties = null;
	
	public void PersistentProperties1(URL url) throws Exception {
		this.url = url;
		this.properties  = new Properties();
		this.loadProperties();
	}
	
	private synchronized void loadProperties() throws Exception {
		File f;
		f = new File(url.getFile());
		if (f.exists()) {
			this.properties.load(new FileInputStream(f));
		}
		else {
			f.createNewFile();
		}
	}

	public void putBoolean(String name, boolean value) {
		this.properties.setProperty(name, Boolean.toString(value));
		try {
			this.sync();
		} catch (Exception e) {
			System.out.println("unable to write preferences");
			e.printStackTrace();
		}		
	}
	
	public void putString(String name, String value) {
		this.properties.setProperty(name, value);
		try {
			this.sync();
		} catch (Exception e) {
			System.out.println("unable to write preferences");
			e.printStackTrace();
		}
	}
	
	public boolean getBoolean(String name, boolean value) {
		String v = this.properties.getProperty(name);
		if (v == null) {
			return value;
		}
		
		return Boolean.parseBoolean(v);
	}
	
	public String getString(String name, String value) {
		String v = this.properties.getProperty(name);
		if (v == null) {
			return value;
		}
		
		return v;
	}
	
	public synchronized void saveProperties() throws Exception {
		this.saveProperties();
	}
	
	protected  void _saveProperties() throws Exception {
		File f;
		f = new File(url.getFile());
		FileOutputStream fos = new FileOutputStream(f);
		properties.store(fos, null);
		fos.close();
	}
	
	public synchronized void sync() throws Exception {
		this._saveProperties();
	}
}
