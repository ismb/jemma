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
package org.energy_home.jemma.ah.internal.zigbee;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.ah.hac.lib.ext.INetworkManager;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;

public class ZigBeeCommandProvider implements CommandProvider {

	INetworkManager zbMngr = null;

	protected void setNetworkManager(INetworkManager s) {
		synchronized (this) {
			this.zbMngr = s;
		}
	}

	protected void unsetNetworkManager(INetworkManager s) {
		synchronized (this) {
			if (this.zbMngr == s) {
				this.zbMngr = null;
			}
		}
	}

	public void _permitjoin(CommandInterpreter ci) {
		if (!this.checkZbMngrService(ci))
			return;

		String durationStr = ci.nextArgument();

		short duration = 0;

		if (durationStr != null) {
			duration = Short.parseShort(durationStr);
		}

		try {
			((ZigBeeManagerImpl) this.zbMngr).permitJoin(duration);
		} catch (Exception e) {
			System.out.println("Error in permt join: " + e.getMessage());
		}
	}

	public void _config(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			try {
				Dictionary config = ((ZigBeeManagerImpl) this.zbMngr).getConfiguration();
				ci.printDictionary(config, "Configuration");
			} catch (Exception e) {
				System.out.println("Error in permt join: " + e.getMessage());
			}
		}
	}

	public void _disablenvm(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			try {
				((ZigBeeManagerImpl) this.zbMngr).disableNVM();
			} catch (Exception e) {
				ci.println(this.getHelp());
			}
		}
	}

	public void _enablenvm(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;
			
			try {
				((ZigBeeManagerImpl) this.zbMngr).enableNVM();
			} catch (Exception e) {
				ci.println(this.getHelp());
			}
		}
	}

	public void _getusenvm(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			try {
				ci.println("usenvm is currently " + ((ZigBeeManagerImpl) this.zbMngr).getNVMStatus());
			} catch (Exception e) {
				ci.println(this.getHelp());
			}
		}
	}

	public void _lsdevs(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			try {
				Collection nodes = ((ZigBeeManagerImpl) this.zbMngr).getNodes();
				for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
					Vector devices = (Vector) iterator.next();
					for (int i = 0; i < devices.size(); i++) {
						ZigBeeDevice zbDevice = (ZigBeeDevice) devices.get(i);
						ci.println(this.printDevice(zbDevice));
					}
				}
			} catch (Exception e) {
				System.out.println("Error in permt join: " + e.getMessage());
			}
		}
	}
	
	
	public void _bind_remove(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			String nodeIeeeAddress;
			short nodeEp;
			short clusterId;

			try {
				nodeIeeeAddress = ci.nextArgument();
				nodeEp = Short.parseShort(ci.nextArgument());
				clusterId = Short.parseShort(ci.nextArgument());
			} catch (Exception e) {
				ci.println("wrong command arguments. See help");
				return;
			}

			try {
				((ZigBeeManagerImpl) this.zbMngr).removeToBinding(nodeIeeeAddress, nodeEp, clusterId);
			} catch (Exception e) {
				ci.println("exception: " + e.getMessage());
				return;
			}
		}
	}

	public void _bind_add(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			String nodeIeeeAddress;
			short nodeEp;
			short clusterId;

			try {
				nodeIeeeAddress = ci.nextArgument();
				nodeEp = Short.parseShort(ci.nextArgument());
				clusterId = (short) Integer.parseInt(ci.nextArgument());
			} catch (Exception e) {
				ci.println("wrong command arguments. See help");
				return;
			}

			try {
				((ZigBeeManagerImpl) this.zbMngr).addToBinding(nodeIeeeAddress, nodeEp, clusterId);
			} catch (Exception e) {
				ci.println("exception: " + e.getMessage());
				return;
			}
		}
	}

	private String printDevice(ZigBeeDevice zbDevice) {
		String out = "";

		out += "ieee address = " + zbDevice.getIeeeAddress() + ", ep = " + zbDevice.getServiceDescriptor().getEndPoint();
		out += ", profileid = " + zbDevice.getServiceDescriptor().getSimpleDescriptor().getApplicationProfileIdentifier();
		out += ", deviceid = " + zbDevice.getServiceDescriptor().getSimpleDescriptor().getApplicationDeviceIdentifier();
		return out;
	}

	public void _status(CommandInterpreter ci) {
		synchronized (this) {
			if (!this.checkZbMngrService(ci))
				return;

			try {
				boolean isGalRunning = ((ZigBeeManagerImpl) this.zbMngr).isGalRunning();
				ci.print("" + isGalRunning);

			} catch (Exception e) {
				System.out.println("Error in permt join: " + e.getMessage());
			}
		}
	}
	
	public void _bind(CommandInterpreter ci) {
		if (!checkZbMngrService(ci))
			return;

		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_bind_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			return;
		} catch (NoSuchMethodException e) {
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			return;
		}
	}

	public String getHelp() {
		String help = "---ZigBee---\n";
		help += "\tzb permitjoin [<timeout>] - open the ZigBee network. <timeout> (ms) permits to specify how long. Infinite othewise.\n";
		help += "\t\tIf <timeout> is zero, the join is cosed\n";
		help += "\tzb enablenvm - enable the usage of NVM next time the gal is restarted\n";
		help += "\tzb disablenvm - disable the usage of NVM next time the gal is restarted\n";
		help += "\tzb getusenvm - returns true if the NVM-usage is enabled, false otherwise\n";
		help += "\tzb status - true if zgd is running, false otherwise\n";
		help += "\tzb config - print the current service configuration (configadmin)\n";
		help += "\tzb lsdevs - list currently discovered devices\n";
		help += "\tzb bind add <node IEEE addr> <ep> <clusterId>  - add a bind from the local node with the remote device cluster\n";
		help += "\tzb bind remove <node IEEE addr> [<ep> <clusterId>]  - remove binds the local node with the remote device cluster\n";
		//help += "\tzb bind list - list binds\n";
		return help;
	}

	public void _zb(CommandInterpreter ci) {
		if (!checkZbMngrService(ci))
			return;

		String command = ci.nextArgument();
		Method method = null;

		try {
			method = this.getClass().getMethod("_" + command, new Class[] { CommandInterpreter.class });
		} catch (SecurityException e) {
			return;
		} catch (NoSuchMethodException e) {
			return;
		}

		try {
			method.invoke(this, new Object[] { ci });
		} catch (Exception e) {
			return;
		}
	}

	private boolean checkZbMngrService(CommandInterpreter ci) {
		if (this.zbMngr == null) {
			ci.print("ZigBee Network Manager Service not running");
			return false;
		}

		return true;
	}
}
