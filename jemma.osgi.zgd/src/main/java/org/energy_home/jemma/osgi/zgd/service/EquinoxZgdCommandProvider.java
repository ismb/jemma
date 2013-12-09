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
package org.energy_home.jemma.osgi.zgd.service;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.energy_home.jemma.zgd.GatewayInterface;
import org.energy_home.jemma.zgd.client.GatewayConsoleClient;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.energy_home.jemma.zgd.jaxb.Version;

public class EquinoxZgdCommandProvider implements CommandProvider {

	private GatewayInterface gateway;
	private GatewayConsoleClient gatewayCC = null;

	public synchronized void setGatewayInterface(GatewayInterface r) {
		gateway = r;
		this.gatewayCC = new GatewayConsoleClient(gateway);
	}

	public synchronized void unsetGatewayInterface(GatewayInterface r) {
		if (r == gateway)
			gateway = null;
	}

	public synchronized void _version(CommandInterpreter ci) {
		if (gateway != null) {
			try {
				getVersion();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void _v(CommandInterpreter ci) {
		try {
			this.gatewayCC.getVersion();
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _o(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.configureEndpoint(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _r(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.clearEndpoint(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _b(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.createCallback(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _c(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.createAPSCallback(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _d(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.deleteCallback(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _l(CommandInterpreter ci) {
		try {
			this.gatewayCC.listCallbacks();
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _a(CommandInterpreter ci) {
		try {
			this.gatewayCC.listAddresses();
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _g(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.getInfoBaseAttribute(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _s(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.setInfoBaseAttribute(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _t(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.startGatewayDevice(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _n(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.startNodeDiscovery(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _u(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.subscribeNodeRemoval(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public synchronized void _j(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.permitJoin(scanner);
		} catch (Exception e) {
			ci.println("error: " + e.getMessage());
		}
	}

	public synchronized void _e(CommandInterpreter ci) {
		Scanner scanner = ci2scanner(ci);
		try {
			this.gatewayCC.permitJoin(scanner);
		} catch (Exception e) {
			ci.println(e.getMessage());
		}
	}

	public Scanner ci2scanner(CommandInterpreter ci) {
		String arg;
		String total = "";
		while ((arg = ci.nextArgument()) != null) {
			total += arg + " ";
		}
		
		Scanner scanner = new Scanner(total);
		return scanner;
	}

	// case 'i':
	// startServiceDiscovery(scanner);
	// continue;
	// case 'p':
	// getServiceDescriptor(scanner);
	// continue;
	// case 'z':
	// getNodeDescriptor(scanner);
	// continue;
	// case 'm':
	// sendAPSMessage(scanner);
	// continue;
	//
	// case 'f':
	// startDefaultSequence();
	// continue;
	//
	//
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }

	public void getVersion() throws IOException, JAXBException {
		Version v;
		try {
			v = gateway.getVersion();
			System.out.printf("Version Identifier: %x\n", v.getVersionIdentifier());
			System.out.printf("Feature Set Identifier: %x\n", v.getFeatureSetIdentifier());
			System.out.printf("RPC protocol: %s\n", v.getRPCProtocol().get(0));
			System.out.printf("Manufacturer Version: %s\n", v.getManufacturerVersion());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			v = new Version();
			v.setFeatureSetIdentifier((short) 1);
			v.setManufacturerVersion("manuf-1");
			v.setVersionIdentifier((short) 2);
			Info info = new Info();
			Status s = new Status();
			s.setCode((short) 0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			det.setVersion(v);
			// Representation r = converter.toRepresentation(info);
		}
	}

	public String getHelp() {
		String help = "";
		help += "---Testing jGal---\n";
		help += "\tType the identifying letter of a command + <parameters> and press enter\n";
		help += "\t[V]version\n";
		help += "\tC[O]nfigure Endpoint <endpoint>\n";
		help += "\tClea[r] Endpoint <endpoint>\n";
		help += "\tCreate (generic) Call[B]ack <0|1|2|3>\n";
		help += "\t[C]reate APS Endpoint Callback <endpoint> (-1 == all EPs)\n";
		help += "\t[D]elete Callback <callback-identifier>\n";
		help += "\t[L]ist All Callbacks\n" + "\tList All [A]ddresses\n";
		help += "\t[G]et InfoBase Attribute <attrId>\n";
		help += "\t[S]et InfoBase Attribute <attrId> <value>\n";
		help += "\tS[T]art Gateway Device <attributeset-index>\n";
		help += "\tStart [N]ode Discovery\n";
		help += "\tStart Serv[I]ce Discovery <address-of-interest>\n";
		help += "\tGet Service Descri[P]tor <address-of-interest> <endpoint>\n" + "\tSend APS [M]essage\n";
		help += "\tL[E]ave <address-of-interest>\n";
		help += "\tPermit [J]oin <address-of-interest> <duration-seconds>\n";

		return help;
	}

	public void _zgd(CommandInterpreter ci) {
		if (!checkGalInterfaceService(ci))
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

	private boolean checkGalInterfaceService(CommandInterpreter ci) {
		if (this.gateway == null) {
			ci.print("ZigBee Gateway not running");
			return false;
		}

		return true;
	}
}
