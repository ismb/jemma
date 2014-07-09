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
package org.energy_home.jemma.javagal.launcher;

import org.energy_home.jemma.zgd.GalExtenderProxyFactory;
import org.energy_home.jemma.zgd.GatewayException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.layers.PropertiesManager;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.presentation.Activator;

/**
 * Starter class for the Javagal project.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class main {

	// public final static Log logger = LogFactory.getLog(main.class);
	private static final Logger LOG = LoggerFactory.getLogger(main.class);

	static String _help = "Usage:  Gal  [options...] -d <device url>\n" + "Options:\n" + "  -h               Show this information\n" + "  -c <filename>    Main configuration filename (default: \"config.properties\"\n" + "  -a               If present, create the network on startup (ignore config.ini setting)\n" + "  -m               If present, use the NVM setting (ignore config.ini setting)\n" + "  -d               Specify the device url\n" + "  -v               Be verbose.\n" + "  -V               Show the program version and quit.\n" + "\n";

	/**
	 * Static main class.
	 * 
	 * @param args
	 *            startup arguments.
	 */
	public static void main(String[] args) {
		LOG.debug("Starting Gal:Java!");
		PropertiesManager PropertiesManager = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
			case '-':
				switch (args[i].charAt(1)) {
				case 'h':
					LOG.debug(_help);
					break;
				case 'c':
					String _path = File.separator + "resources" + File.separator + args[++i].replace("\"", "");

					PropertiesManager = new PropertiesManager(Thread.currentThread().getContextClassLoader().getResource(_path));
					break;
				case 'a':
					PropertiesManager.setAutoStart(true);
					break;

				case 'm':
					PropertiesManager.getSturtupAttributeInfo().setStartupControl((short) 0x01);
					break;

				case 'd':
					i = i + 1;
					String _comport = args[i].substring(0, (args[i].indexOf("?")));
					String _dongleType = args[i].substring(args[i].indexOf("?"), args[i].indexOf("&"));
					_dongleType = _dongleType.substring(_dongleType.indexOf("=") + 1);
					String _donglespeed = args[i].substring(args[i].indexOf("&"));
					_donglespeed = _donglespeed.substring(_donglespeed.indexOf("=") + 1);
					PropertiesManager.props.setProperty("zgd.dongle.uri", _comport);
					PropertiesManager.props.setProperty("zgd.dongle.speed", _donglespeed);
					PropertiesManager.props.setProperty("zgd.dongle.type", _dongleType);
					break;
				case 'v':
					PropertiesManager.setDebugEnabled(true);
					break;
				case 'V':
					short vers = 0;
					try {
						vers = GalController.getVersion().getVersionIdentifier();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (GatewayException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					LOG.debug(Short.toString(vers));
					break;

				}
				break;
			}
		}
		try {
			new GalExtenderProxyFactory(PropertiesManager).createGatewayInterfaceObject();
		} catch (Exception e) {
			LOG.error("Error starting GAL: " + e.getMessage());
			return;
		}
		LOG.debug("Waiting Osgi connections...");

	}

}

class JavaGalLogger extends Formatter {

	int lastDotInClassName;
	String className;

	@Override
	public String format(LogRecord record) {
		className = record.getSourceClassName();
		lastDotInClassName = className.lastIndexOf('.');
		className = className.substring(lastDotInClassName + 1, className.length());
		return "\n" + className + " " + record.getSourceMethodName() + ": " + record.getMessage();
	}

}
