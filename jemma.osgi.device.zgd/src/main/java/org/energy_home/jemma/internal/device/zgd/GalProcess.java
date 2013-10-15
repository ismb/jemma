/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
package org.energy_home.jemma.internal.device.zgd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.device.zgd.IGal;
import org.energy_home.jemma.device.zgd.IGalAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

/**
 * Spawn the gal process and control it
 */
class StreamGobbler extends Thread {
	InputStream is;
	String type;
	boolean show;

	private static final Log log = LogFactory.getLog(GalProcess.class);

	StreamGobbler(InputStream is, String type, boolean show) {
		this.is = is;
		this.type = type;
		this.show = show;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				if (show)
					log.debug(type + " >" + line);
		} catch (IOException ioe) {

		}
	}
}

public class GalProcess implements Runnable, IGal, IGalAdmin, BundleTrackerCustomizer {

	public static final String ZGD_WSNC_URL_PROP_NAME = "zgd.wsnc.uri";
	public static final String DEFAULT_ZGD_WSNC_URL_PROP = "http://163.162.180.176:8080";
	public static final String ZGD_DONGLE_URI_PROP_NAME = "zgd.dongle.uri";
	public static final String ZGD_DONGLE_TYPE_PROP_NAME = "zgd.dongle.type";
	public static final String ZGD_DONGLE_SPEED_PROP_NAME = "zgd.dongle.speed";
	public static final String ZGD_DEBUG_PROP_NAME = "zgd.log.debug";
	public static final String ZGD_SECURITY_PROP_NAME = "zgd.security";
	public static final String ZGD_SECURITY_WATCHDOG_TIME_NAME = "zgd.watchdog";
	public static final String ZGD_WSNC_ID_PROP_NAME = "zgd.wsnc.id";
	public static final String PROP_ZGD_PORT = "zgd.port";
	public static final String PROP_ZGD_CHANNEL = "zgd.channel";
	public static final String PROP_ZGD_AUTODISCOVERY = "zgd.autodiscovery";

	protected static final String FRAGMENT_BUNDLES_PREFIX = "org.energy_home.jemma.osgi.zgd";

	private static final Log log = LogFactory.getLog(GalProcess.class);

	Process p = null;
	private BundleContext bc;

	private boolean logDebug = true;

	// default values
	private static final String DEFAULT_ZGD_DONGLE_TYPE_PROP = "ezsp";
	private static final int DEFAULT_ZGD_DONGLE_SPEED_PROP = 115200;
	private static final boolean DEFAULT_ZGD_SECURITY = true;
	private static final boolean DEFAULT_ZGD_AUTODISCOVERY = true;

	private int zgdPort = 9000;
	private URI dongleUri = null;
	private String dongleType;
	private int dongleSpeed;

	private boolean security = true;
	private boolean autoExtract = true;

	String exeName = "gal";

	private int watchdogTime;
	private String zgdId = null;
	private int channel;
	private ServiceRegistration registration = null;

	boolean first = true;

	BundleTracker bundleTracker;
	private String os;
	private String arch;
	private Bundle fragmentBundle;
	private BundleTracker fragmentsTracker;

	Object registrationLock = new Object();
	private boolean autodiscovery = false;

	private volatile Thread processThread = null;
	private boolean useAbsolutePath = false;

	public GalProcess() {
		log.debug("constructor");
	}

	protected synchronized void activate(BundleContext bc, Map props) {
			log.debug("activate");
			this.bc = bc;

			if (first) {
				logDebug = getProperty(ZGD_DEBUG_PROP_NAME, false);
				dongleSpeed = getProperty(ZGD_DONGLE_SPEED_PROP_NAME, DEFAULT_ZGD_DONGLE_SPEED_PROP);
				dongleType = getProperty(ZGD_DONGLE_TYPE_PROP_NAME, DEFAULT_ZGD_DONGLE_TYPE_PROP);
				security = getProperty(ZGD_SECURITY_PROP_NAME, DEFAULT_ZGD_SECURITY);
				channel = getProperty(PROP_ZGD_CHANNEL, 0);
				autodiscovery = getProperty(PROP_ZGD_AUTODISCOVERY, DEFAULT_ZGD_AUTODISCOVERY);
				this.zgdId = bc.getProperty(ZGD_WSNC_ID_PROP_NAME);
				try {
					dongleUri = new URI(bc.getProperty(ZGD_DONGLE_URI_PROP_NAME));
			} catch (URISyntaxException e) {
					log.debug("invalid dongle url received from configuration: " + props.get(ZGD_DONGLE_URI_PROP_NAME));
			} catch (NullPointerException e) {
				}

				zgdPort = getProperty(PROP_ZGD_PORT, 9000);
				watchdogTime = getProperty(ZGD_SECURITY_WATCHDOG_TIME_NAME, 10000);
				first = false;
			}

			logDebug = getProperty(props, ZGD_DEBUG_PROP_NAME, logDebug);
			dongleSpeed = getProperty(props, ZGD_DONGLE_SPEED_PROP_NAME, dongleSpeed);
			dongleType = getProperty(props, ZGD_DONGLE_TYPE_PROP_NAME, dongleType);
			security = getProperty(props, ZGD_SECURITY_PROP_NAME, security);
			channel = getProperty(props, PROP_ZGD_CHANNEL, channel);
			zgdId = getProperty(props, ZGD_WSNC_ID_PROP_NAME, zgdId);
			autodiscovery = getProperty(props, PROP_ZGD_AUTODISCOVERY, autodiscovery);

			try {
				dongleUri = getProperty(props, ZGD_DONGLE_URI_PROP_NAME, dongleUri);
		} catch (URISyntaxException e) {
				log.debug("invalid dongle url received from configuration admin: " + props.get(ZGD_DONGLE_URI_PROP_NAME));
		} catch (NullPointerException e) {
			}
			zgdPort = this.getProperty(props, PROP_ZGD_PORT, zgdPort);
			watchdogTime = getProperty(props, ZGD_SECURITY_WATCHDOG_TIME_NAME, watchdogTime);

			if (logDebug)
				dumpProperties(props);

			fragmentsTracker = new BundleTracker(bc, Bundle.RESOLVED | Bundle.INSTALLED, this);
			fragmentsTracker.open();
		}

	protected synchronized void deactivate(final BundleContext bc) throws Exception {
			log.debug("deactivate");
		synchronized (registrationLock) {
			this.unregister();
		}
			this.stopProcess();
			fragmentsTracker.close();
		}

	private void startProcess() {
		processThread = new Thread(this, "Zgd Process and Monitoring Thread");
		processThread.start();
	}

	private void stopProcess() {
		// atomic because pollingThread is volatile
		Thread tmpThread = processThread;

		processThread = null;

		if (tmpThread != null) {
			tmpThread.interrupt();
			try {
				tmpThread.join();
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

		if (log != null)
			log.debug("stopped process");
	}

	protected void modified(final Map props) {
		log.debug("modified");
	}

	private boolean generateGalConfigFile(String path) {
		InputStream is = null;

		String ps = File.separator;

		// config.ini location in the jar file
		String configFilenameEntry = ps + "resources" + ps + "config.ini";

		try {

			URL url = bc.getBundle().getEntry(configFilenameEntry);
			if (url == null) {
				if (log != null)
					log.error("unable to open file " + configFilenameEntry);
				return false;
			}

			is = url.openStream();

			File outputFile = new File(path + ps + "config.ini");

			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("gwStatusChanged_DefaultURIListener")) {
					// replace
					// bw.write("gwStatusChanged_DefaultURIListener = " +
					// parent.getWsncUrl() + "/gal/startup");
				} else if (line.startsWith("SecurityLevel")) {
					if (security)
						bw.write("SecurityLevel = 5");
					else
						bw.write("SecurityLevel = 0");
				} else if (line.startsWith("serverPorts")) {
					bw.write("serverPorts = \"" + zgdPort + "\"");
				} else if (line.startsWith("ChannelMask")) {
					bw.write("ChannelMask = " + channel);
				} else if (line.startsWith("debugEnabled")) {
					if (logDebug)
						bw.write("debugEnabled = 1");
					else
						bw.write("debugEnabled = 0");
				} else if (line.startsWith("autostart")) {
					bw.write("autostart = 0");
				} else if (line.startsWith("autoDiscoveryUnknownNodes")) {
					if (autodiscovery)
						bw.write("autoDiscoveryUnknownNodes = 1");
					else
						bw.write("autoDiscoveryUnknownNodes = 0");
				} else {
					bw.write(line);
				}
				bw.newLine();
			}

			br.close(); // Close to unlock.
			bw.close(); // Close to unlock and flush to disk.

			is.close();

			if (logDebug && log != null)
				log.info("gal config.ini file generated successfully");

		} catch (FileNotFoundException ex) {
			if (log != null)
				log.error(ex.getMessage() + " in the specified directory.");

			return false;
		} catch (IOException e) {
			if (log != null)
				log.error(e.getMessage());

			return false;
		}

		return true;
	}

	public void run() {
		log.debug("started processThread");
		if (processThread == null) {
			log.debug("stopped process thread before started");
			return; // stopped before started.
		}

		String path = null;
		try {
			path = retrieveGalPath();
		} catch (URISyntaxException e) {
			log.error(e);
		}

		if (path == null) {
			log.fatal("no destination path for gal. Errors extracting gal?");
			return;
		}

		log.info("gal dir exported to " + path);

		if (log != null)
			log.debug("osName = '" + getOS() + "', osArch = '" + getArch() + "'");

		if (getOS().equals("win32") && getArch().equals("x86")) {
			useAbsolutePath = true;
			if (dongleUri == null) {
				try {
					dongleUri = new URI("zigbee:///dev/ttyS43");
				} catch (URISyntaxException e) {
					log.fatal("error in default URI");
				}
			}
			exeName += ".exe";
		} else if (getOS().equals("linux") && (getArch().equals("arm") || getArch().equals("x86") || getArch().equals("mips"))) {
			if (dongleUri == null) {
				try {
					dongleUri = new URI("zigbee:///dev/ttyUSB0");
				} catch (URISyntaxException e) {
					log.fatal("error in default URI");
				}
			}

			try {
				Runtime.getRuntime().exec(new String[] { "chmod", "755", path + File.separator + exeName }).waitFor();
				log.debug("chmod di " + path + File.separator + exeName);
			} catch (Throwable e) {
				// ignore
			}

		} else {
			log.error("gal binaries not available for current platform: " + getOS() + ", " + getArch());
			return;
		}

		String url = dongleUri.toString();

		url = addParameter(url, "dongle", dongleType);
		url = addParameter(url, "speed", dongleSpeed + "");

		ArrayList cmd = new ArrayList();

		if (useAbsolutePath) {
			cmd.add(path + File.separator + exeName);
		}
		else {
			cmd.add("." + File.separator + exeName);
		}

		if ((this.zgdId != null) && (zgdId.length() > 0)) {
			cmd.add("-n");
			cmd.add(this.zgdId);
		}
		cmd.add("-c");
		if (useAbsolutePath) {
			cmd.add(path + File.separator + "config.ini");
		}
		else
			cmd.add("config.ini");

		cmd.add(url);

		try {
			generateGalConfigFile(path);
		} catch (Exception e) {
			if (log != null)
				log.error("Exception generating config.ini file '" + e.getMessage() + "'");
		}

		String[] cmdArray = new String[cmd.size()];
		cmd.toArray(cmdArray);

		while (!Thread.currentThread().isInterrupted()) {
			String cmdline = "";
			for (int i = 0; i < cmdArray.length; i++) {
				cmdline += cmdArray[i] + " ";
			}

			log.debug("exec gal with command line: " + cmdline);

			// pace time to avoid to too close gal startups
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				break;
			}

			String[] envp = null;

			if (!useAbsolutePath)
				envp = new String[] { "LD_LIBRARY_PATH=." };

			try {
				if (useAbsolutePath)
					p = Runtime.getRuntime().exec(cmdArray);
				else
					p = Runtime.getRuntime().exec(cmdArray, envp, new File(path + File.separator));
			} catch (IOException e) {
				log.debug("Exception", e);
				continue;
			}

			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), "ERROR", logDebug);
			StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), "OUTPUT", logDebug);

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			synchronized (registrationLock) {
				this.register();
			}

			int exitVal;

			try {
				exitVal = p.waitFor();
			} catch (InterruptedException e) {
				break;
			}

			p = null;
			errorGobbler = null;
			outputGobbler = null;

			if (log != null) {
				switch (exitVal) {
				case 0:
					log.debug("gal exited successfully");
					break;

				case 1:
					log.error("exit code: adapter not found. The adapter uri was '" + this.dongleUri + "'");
					break;

				default:
					log.error("gal exited with exit code = " + exitVal);
				}
			}

			synchronized (registrationLock) {
				unregister();
			}

			// force restart after 'watchdogTime'
			if (watchdogTime > 1) {
				try {
					Thread.sleep(watchdogTime);
				} catch (InterruptedException e) {
					break;
				}
			} else {
				break;
			}
		}

			this.unregister();
			this.kill();
		}

	private void register() {
		if (registration == null)
			registration = bc.registerService(new String[] { IGal.class.getName() }, this, getCurrentConfig());
	}

	private void unregister() {
		if (registration != null) {
			log.debug("unregistering gal");
			registration.unregister();
			registration = null;
		}
	}

	private Dictionary getCurrentConfig() {
		Dictionary props = new Hashtable();
		props.put(PROP_ZGD_PORT, new String(this.zgdPort + ""));
		return props;
	}

	protected static String addParameter(String URL, String name, String value) {
		int qpos = URL.indexOf('?');
		int hpos = URL.indexOf('#');
		char sep = qpos == -1 ? '?' : '&';
		String seg = sep + encodeUrl(name) + '=' + encodeUrl(value);
		return hpos == -1 ? URL + seg : URL.substring(0, hpos) + seg + URL.substring(hpos);
	}

	private static String encodeUrl(String name) {
		return name;
	}

	private String retrieveGalPath() throws URISyntaxException {
		String path;

		if (autoExtract) {
			if (!getOS().equals("win32") && !getOS().equals("linux")) {
				return null;
			}

			if (!getArch().equals("x86") && !getArch().equals("arm")) {
				return null;
			}

			// ATTTENTION: DO NOT USE File.separator here!!! Works only with "/"
			path = "resources/zgd/" + getOS() + "/" + getArch();

			// locates the executable into the bundle
			return this.extractGalBinaries(path);
		} else {
			if (getOS().equals("win32") && getArch().equals("x86")) {
				path = getNativeStorageLocation("cygcurl-4.dll");
				if (dongleUri == null) {
					dongleUri = new URI("zigbee:///dev/com8");
				}
				exeName += ".exe";
			} else if (getOS().equals("linux") && (getArch().equals("arm") || getArch().equals("x86"))) {
				path = getNativeStorageLocation("gal");
				if (dongleUri == null) {
					dongleUri = new URI("zigbee:///dev/ttyUSB0");
				}
				try {
					Runtime.getRuntime().exec("chmod +x " + path + exeName);
				} catch (IOException e) {
					return null;
				}

			} else {
				log.error("gal binaries not available for current platform: " + getOS() + ", " + getArch());
				return null;
			}
		}

		return path;
	}

	/**
	 * We try to load a dll that causes an UnsatisfiedLinkError exception. This
	 * exception contains in the error message the location of the native
	 * storage area. We exctract the path and return back to the caller. FIXME:
	 * probably this trick is valid only on the Sun's jdk.
	 * 
	 * @param filename
	 * @return
	 */

	private String getNativeStorageLocation(String filename) {
		String path = "";

		if (logDebug && (log != null))
			log.debug("getNativeStorageLocation = " + filename);

		try {
			System.loadLibrary(filename);
		} catch (UnsatisfiedLinkError e) {
			int index_start = 0;
			int index_end = 0;

			if (e.getMessage().indexOf("cannot dynamically load executable") > 0) {
				index_end = e.getMessage().indexOf("(", index_start + 2);
				if (index_end == -1) {
					// try with :
					index_end = e.getMessage().indexOf(":", index_start + 2);
				}
			} else {
				index_end = e.getMessage().indexOf(":", index_start + 2) + 1;
			}

			path = e.getMessage().substring(index_start, index_end - filename.length() - 1);
		} catch (Exception e) {
			log.error(e);
			return path;
		}
		return path + File.separator;
	}

	/**
	 * This function copy the whole content of the passed path (that refers to a
	 * folder that is in the bundle) in the storage area reserved to the bundle,
	 * by the OSGi framework. The original path is reproduced. If, for instance,
	 * bundlePath is /gal/windows, all the files in <code>/gal/windows</code>
	 * are copied under <code>bundle data path/gal/windows</code>
	 * 
	 * 
	 * @param bundleResourcePath
	 *            path to a folder packaged into the bundle jar
	 */

	private String alignFiles(String bundleResourcePath) {
		byte[] buffer = new byte[20480];
		log.debug("bundleResourcePath is " + bundleResourcePath);

		Enumeration entries = null;

		Dictionary headers = bc.getBundle(0).getHeaders();
		String vendor = ((String) headers.get("Bundle-Vendor"));

		boolean isEquinox = false;
		if ((vendor != null) && vendor.startsWith("Eclipse"))
			isEquinox = true;

		if (isEquinox) {
			entries = bc.getBundle().findEntries(bundleResourcePath, "*", false);
		} else {
			// On Felix it seems that the fragment resources are not MERGED with
			// the host bundle so we access them by using fragmentBundle
			if (fragmentBundle != null) {
				entries = fragmentBundle.findEntries(bundleResourcePath, "*", false);
			} else {
				log.fatal("fragmentBundle is unexpectly null!!");
				return null;
			}
		}

		File root = bc.getDataFile(bundleResourcePath);

		if (entries == null) {
			log.fatal("no entries in bundle resource path '" + bundleResourcePath + "'");
			return null;
		}

		while (entries.hasMoreElements()) {

			URL entry = (URL) entries.nextElement();
			File entryFile = new File(entry.getPath());
			String path = entryFile.getPath();

			// NOTE: Apache Felix requires that the path specified in
			// getDataFile is relative. Equinox accepts both relative or
			// absolute paths.

			// TODO: what about Prosyst?
			File f = bc.getDataFile("." + File.separator + path);

			if (path.endsWith(".svn")) {
				continue;
			}

			f.getParentFile().mkdirs();

			boolean update = needsRefresh(entryFile, f);
			if (!update) {
				log.debug("file " + f.getName() + " already up-to-date");
				continue;
			}

			try {
				log.debug("extracting file = " + entry.toString());
				FileOutputStream fos = new FileOutputStream(f);
				InputStream is = entry.openStream();

				for (int read = is.read(buffer, 0, buffer.length); read > 0; read = is.read(buffer, 0, buffer.length)) {
					fos.write(buffer, 0, read);
				}

				fos.flush();
				fos.close();
			} catch (IOException e) {
				log.error("error extracting file " + path + " " + e.getMessage()
						+ ". Probably the file is used by another process.");
				return null;
			}
		}

		// TODO: remove those elements from the data area that are not into the
		// bundle anymore

		return root.getAbsolutePath();
	}

	private boolean needsRefresh(File origin, File destination) {
		if (fragmentBundle == null) {
			return false;
		}
		if (!destination.exists()) {
			return true;
		}
		if (fragmentBundle.getLastModified() >= destination.lastModified())
			return true;

		return false;
	}

	protected void removeGalBinaries() {
		File root = bc.getDataFile("/");
		deleteDirectory(root);
		log.debug("removed gal binaries");
	}

	static private boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	protected String extractGalBinaries(String bundlePath) {
		return alignFiles(bundlePath);
	}

	protected void kill() {
		if (p != null) {
			log.debug("killing gal");
			p.destroy();
		} else {
			log.debug("gal is not currently running");
		}
	}

	protected int getProperty(String name, int value) {
		String prop = bc.getProperty(name);
		if (prop != null) {
			try {
				value = Integer.valueOf(prop).intValue();
			} catch (Exception e) {
			}
		}
		return value;
	}

	protected String getProperty(String name, String value) {
		String prop = bc.getProperty(name);
		if (prop != null) {
			value = prop;
		}
		return value;
	}

	protected boolean getProperty(String name, boolean value) {
		String prop = bc.getProperty(name);
		if (prop != null) {
			try {
				value = Boolean.valueOf(prop).booleanValue();
			} catch (Exception e) {
			}
		}
		return value;
	}

	protected int getProperty(Map props, String name, int value) {
		Number prop = null;
		try {
			prop = (Number) props.get(name);
		} catch (Exception e) {
			log.error("bad property type for property " + name + " expecting Integer");
			return value;
		}
		if (prop != null) {
			return prop.intValue();
		}
		return value;
	}

	private URI getProperty(Map props, String name, URI url) throws URISyntaxException {
		String prop = (String) props.get(name);
		if (prop != null) {
			if (prop.length() > 0)
				url = new URI(prop);
		}
		return url;
	}

	protected String getProperty(Map props, String name, String value) {
		String prop = (String) props.get(name);
		if (prop != null) {
			value = prop;
		}
		return value;
	}

	protected boolean getProperty(Map props, String name, boolean value) {
		Boolean prop = null;
		try {
			prop = (Boolean) props.get(name);
		} catch (Exception e) {
			log.error("bad property type for property " + name + " expecting Boolean");
			return value;
		}

		if (prop != null) {
			return prop.booleanValue();
		}
		return value;
	}

	protected void dumpProperties(Map map) {
		if (map.size() == 0) {
			log.debug("Map: <empty>");
			return;
		} else {
			log.debug("Key\t\t\tValue");
			log.debug("-----------\t--------------------");
		}

		Set keys = map.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Object object = (Object) iterator.next();
			log.debug(object.toString() + "\t" + map.get(object));
		}
	}

	private String getOS() {
		if (os != null)
			return os;
		String osName = System.getProperties().getProperty("os.name"); //$NON-NLS-1$
		if (osName.regionMatches(true, 0, Constants.OS_WIN32, 0, 3))
			return Constants.OS_WIN32;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_SUNOS))
			return Constants.OS_SOLARIS;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_LINUX))
			return Constants.OS_LINUX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_AIX))
			return Constants.OS_AIX;
		if (osName.equalsIgnoreCase(Constants.INTERNAL_OS_HPUX))
			return Constants.OS_HPUX;
		if (osName.regionMatches(true, 0, Constants.INTERNAL_OS_MACOSX, 0, Constants.INTERNAL_OS_MACOSX.length()))
			return Constants.OS_MACOSX;
		return Constants.OS_UNKNOWN;
	}

	private String getArch() {
		if (arch != null)
			return arch;
		String name = System.getProperties().getProperty("os.arch");
		if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_I386))
			return Constants.INTERNAL_ARCH_X86;
		else if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_AMD64))
			return Constants.INTERNAL_ARCH_X86_64;
		else if (name.equalsIgnoreCase(Constants.INTERNAL_ARCH_ARM))
			return Constants.INTERNAL_ARCH_ARM;

		return name;
	}

	/**
	 * Special attention should be taken to handle fragment life cycle. Here is
	 * what I have discovered:
	 * 
	 * 'refresh <host bundle> -> removedBundle[bundleEvent=null] ->
	 * addingBundle[bundleEvent=null]
	 * 
	 * 'refresh <fragment id>' -> removedBundle[bundleEvent=null] ->
	 * addingBundle[bundleEvent=null]
	 * 
	 * In the following cases DO NOT kill or start application but refresh the
	 * host bundle: 'install <fragment>' -> addingBundle[bundleEvent=INSTALLED]
	 * 'uninstall <fragment>' -> modifiedBundle[bundleEvent=UNRESOLVED] ->
	 * removedBundle[bundleEvent=UNINSTALLED] 'update <fragment>' ->
	 * modifiedBundle[bundleEvent=UNRESOLVED] ->
	 * modifiedBundle[bundleEvent=UPDATED]
	 * 
	 * <normal startup (bundle starting before or after fragment)> ->
	 * addingBundle[bundleEvent=null]
	 * 
	 * So here I start the process on: addingBundle[bundleEvent=null]
	 * 
	 * I stop the process on: removedBundle[bundleEvent=null]
	 */

	public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
		synchronized (registrationLock) {
			String expectedFragmentName = FRAGMENT_BUNDLES_PREFIX + "." + PlatformsUtil.getOS() + "." + PlatformsUtil.getArch();
			if (bundle.getSymbolicName().startsWith(expectedFragmentName)) {

				if (bundleEvent != null) {
					log.debug("addingBundle " + expectedFragmentName + " bundle event type " + bundleEvent.getType());
					if (bundleEvent.getType() == BundleEvent.RESOLVED) {
						this.fragmentBundle = bundle;
						if (System.getProperty("org.osgi.framework.vendor").toLowerCase().equals("prosyst")) {
							startProcess();
						}
						log.debug("Resolved Fragment " + bundle.getSymbolicName() + " refresh host to apply modifications");
					}
					else if (bundleEvent.getType() == BundleEvent.INSTALLED) {
						this.fragmentBundle = bundle;
						log.debug("Installed Fragment " + bundle.getSymbolicName() + " refresh host to apply modifications");
					}
				}
				else {
					log.debug("addingBundle(): detected fragment " + expectedFragmentName + " with bundleEvent = null");
					this.fragmentBundle = bundle;
					log.debug("called start()");
					this.startProcess();
				}
				return bundle;
			}
			return null;
		}
	}

	public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object arg2) {
		synchronized (registrationLock) {
			String expectedFragmentName = FRAGMENT_BUNDLES_PREFIX + "." + PlatformsUtil.getOS() + "." + PlatformsUtil.getArch();
			if (bundle.getSymbolicName().startsWith(expectedFragmentName)) {
				log.debug("modifiedBundle " + expectedFragmentName + " event type " + bundleEvent.getType());
				if (bundleEvent.getType() == BundleEvent.RESOLVED) {
					log.debug("Resolved Fragment " + bundle.getSymbolicName());
					this.fragmentBundle = bundle;
					this.startProcess();
				}
				else if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
					log.debug("Unresolved Fragment " + bundle.getSymbolicName() + " refresh host to apply modifications");
				}
				else if (bundleEvent.getType() == BundleEvent.UPDATED) {
					log.debug("Updated Fragment " + bundle.getSymbolicName() + " refresh host to apply modifications");
				}
			}
		}
	}

	public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object arg2) {
		synchronized (registrationLock) {
			String expectedFragmentName = FRAGMENT_BUNDLES_PREFIX + "." + PlatformsUtil.getOS() + "." + PlatformsUtil.getArch();
			if (bundle.getSymbolicName().startsWith(expectedFragmentName)) {
				if (bundleEvent != null) {
					log.debug("removedBundle " + expectedFragmentName + ", event type " + bundleEvent.getType());
					if (bundleEvent.getType() == BundleEvent.UNINSTALLED) {
						log.debug("Uninstalled Fragment " + bundle.getSymbolicName() + " refresh host to apply modifications");
						this.fragmentBundle = null;
						// ---this.stopProcess();
						// ---this.removeGalBinaries();
					}
				}
				else {
					log.debug("removedBundle " + expectedFragmentName + ", event type: null");
				}
			}
		}
	}
}
