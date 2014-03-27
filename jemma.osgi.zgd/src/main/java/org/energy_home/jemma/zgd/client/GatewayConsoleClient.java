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
package org.energy_home.jemma.zgd.client;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import org.energy_home.jemma.zgd.*;
import org.energy_home.jemma.zgd.impl.GatewayFactoryImpl;
import org.energy_home.jemma.zgd.impl.JaxbConverter;
import org.energy_home.jemma.zgd.jaxb.*;
import org.energy_home.jemma.zgd.jaxb.Info.Detail;
import org.energy_home.jemma.zgd.jaxb.NodeServices.ActiveEndpoints;
import org.restlet.representation.Representation;

public class GatewayConsoleClient implements APSMessageListener, GatewayEventListener {
	GatewayFactory factory;
	GatewayInterface gateway;
	JaxbConverter converter;
	Scanner scanner;
	short localEndpoint = 1;
	short lastEndpoint = -1;
	Address lastNodeAddress;
	BigInteger MINUS_ONE = BigInteger.valueOf(-1);
	
	public static void main(String args[]) {
		Trace.setTrace(new Trace());
		GatewayConsoleClient client = new GatewayConsoleClient();
		client.loopCommands();
	}

	public GatewayConsoleClient() {
		GatewayProperties prop = new GatewayProperties();
		//prop.setProperty(GatewayProperties.GATEWAY_ROOT_URI, "http://130.192.86.164:9000/");
		prop.setProperty(GatewayProperties.GATEWAY_ROOT_URI, "http://192.168.1.64:9000");
		//prop.setProperty(GatewayProperties.USE_PUBLIC_ADDRESS_RESOLUTION, "true");
		prop.setProperty(GatewayProperties.CONNECTION_TIMEOUT, "100");
		prop.setProperty(GatewayProperties.ENABLE_RESTLET_CONSOLE, "true");
		try {
			factory = GatewayFactory.getInstance(prop);
			converter = ((GatewayFactoryImpl)factory).createConverter();
			gateway = factory.createGatewayObject();
			
			Info i = new Info();
			Detail d = new Detail();
			i.setDetail(d);
            BindingList bl = new BindingList();
            Binding b =  new Binding();
            bl.getBinding().add(b);
            d.setBindings(bl);
            b.setClusterID(123);
            b.setSourceEndpoint((short)2);
            b.setSourceIEEEAddress(new BigInteger("1234567890"));
            Device dev = new Device();
            dev.setAddress(new BigInteger("9876543210"));
            dev.setEndpoint((short)13);
            b.getDeviceDestination().add(dev);
			
            Representation r = converter.toRepresentation(i);
			
			NodeServices ns = new NodeServices();
			NodeServices.ActiveEndpoints aep1 = new ActiveEndpoints();
			aep1.setEndPoint((short)1);
			SimpleDescriptor sp1 = new SimpleDescriptor();
			sp1.setApplicationDeviceIdentifier(12);
			//aep1.setSimpleDescriptor(sp1);
			
			NodeServices.ActiveEndpoints aep2 = new ActiveEndpoints();
			aep2.setEndPoint((short)4);
			SimpleDescriptor sp2 = new SimpleDescriptor();
			sp2.setApplicationDeviceIdentifier(44);
			//aep2.setSimpleDescriptor(sp2);
			ns.getActiveEndpoints().add(aep1);
			ns.getActiveEndpoints().add(aep2);
			r = converter.toRepresentation(ns);
			
			ServiceDescriptor sd = new ServiceDescriptor();
			sd.setEndPoint((short)6);
			Address a = new Address();
			a.setNetworkAddress(32);
			sd.setAddress(a);
			SimpleDescriptor simp = new SimpleDescriptor();
			simp.setEndPoint((short)6);
			simp.setApplicationDeviceIdentifier(12);
			simp.setApplicationDeviceVersion((short)3);
			sd.setSimpleDescriptor(simp);
			r = converter.toRepresentation(sd);
			
			//startDefaultSequence();
			//lastNodeAddress = new Address();
			//lastNodeAddress.setIeeeAddress(BigInteger.valueOf(1234567890L));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		gateway.setGatewayEventListener(this);

		scanner = new Scanner(System.in);
		showCommands();
	}
	
	public GatewayConsoleClient(GatewayInterface gateway) {
		this.gateway = gateway;
	}

	APSMessage buildAPSMessage(BigInteger aoi, short ep) {
		APSMessage sentMsg = new APSMessage();
		Address a = new Address();
		a.setIeeeAddress(aoi);
		//a.setNetworkAddress(aoi);
		sentMsg.setDestinationAddress(a);
		sentMsg.setDestinationAddressMode((long)GatewayConstants.EXTENDED_ADDRESS_MODE);
		//sentMsg.setDestinationAddressMode((long)GatewayStatusCodes.SHORT_ADDRESS_MODE);
		sentMsg.setDestinationEndpoint((short)ep);
		sentMsg.setSourceEndpoint((short)localEndpoint);
		sentMsg.setClusterID(0x900);
		sentMsg.setProfileID(0x107);
		byte[] data = new byte[] {0x10, 1, 0, 0, 0};
		sentMsg.setData(data);

		TxOptions tx = new TxOptions();
		tx.setAcknowledged(false);
		tx.setPermitFragmentation(false);
		tx.setSecurityEnabled(false);
		tx.setUseNetworkKey(false);
		sentMsg.setTxOptions(tx);
		sentMsg.setRadius((short)10);
		return sentMsg;
	}

	Callback buildCallback(int version) {
		Filter filter = new Filter();
		filter.setLevelSpecification(new Filter.LevelSpecification());
		filter.getLevelSpecification().getLevel().add(Level.APS_LEVEL);

		if ((version & 1) > 0) {
			Filter.MessageSpecification msg = new Filter.MessageSpecification();
			filter.getMessageSpecification().add(msg);
			// check this out...
			// msg.setAPSClusterIdentifier(0);
			msg.setAPSClusterGroup("ZDP");
		}

		if ((version & 2) > 0) {
			Filter.AddressSpecification as = new Filter.AddressSpecification();
			filter.getAddressSpecification().add(as);
			// ...and check this out
			as.setNWKSourceAddress(new Address());
			as.getNWKSourceAddress().setNetworkAddress(1);
		}

		Action action = new Action();
		action.setDecodeSpecification(new Action.DecodeSpecification());
		action.getDecodeSpecification().getDecodeLevel().add(DecodeLevel.DECODE_APS);
		Callback callback = new Callback();
		callback.setFilter(filter);
		callback.setAction(action);
		return callback;
	}

	void showCommands() {
		System.out.println("_______________________________________________________________________");
		System.out.println("Type the identifying letter of a command + <parameters> and press enter");
		System.out.println("- Get [V]ersion");
		System.out.println("- C[O]nfigure Endpoint <endpoint>");
		System.out.println("- Clea[R] Endpoint <endpoint>");
		System.out.println("- Create Generic Call[B]ack <0|1|2|3>");
		System.out.println("- [C]reate APS Endpoint Callback <endpoint> (-1 == all endpoints)");
		System.out.println("- [D]elete Callback <callback-identifier>");
		System.out.println("- [L]ist All Callbacks");
		System.out.println("- List All [A]ddresses");
		System.out.println("- [G]et InfoBase Attribute <attrId>");
		//System.out.println("- [S]et InfoBase Attribute <attrId> <value>");
		System.out.println("- S[T]art Gateway Device <attributeset-index>");
		System.out.println("- Start [N]ode Discovery <on|off>");
		System.out.println("- S[U]bscribe Node Removal <on|off>");
		System.out.println("- Start Serv[I]ce Discovery <address-of-interest> (-1 == last used IEEE)");
		System.out.println("- Get Service Descri[P]tor <address-of-interest>  (-1 == last used IEEE) <endpoint>");
		System.out.println("- Get Node Descriptor[Z] <address-of-interest>  (-1 == last used IEEE)");
		System.out.println("- Send APS [M]essage <address-of-interest>  (-1 == last used IEEE) <endpoint>");
		System.out.println("- L[E]ave <address-of-interest> (-1 == last used IEEE, 1 == all nodes)");
		System.out.println("- Permit [J]oin <address-of-interest> (-1 == last used IEEE, 1 == all nodes) <duration-seconds>");
		System.out.println("- Start De[F]ault Startup Sequence <local-endpoint>");
		System.out.println("- Show this [H]elp");
		System.out.println("- E[X]it");
	}

	void loopCommands() {
		while (true) {
			try {
				scanner.useDelimiter("\\p{javaWhitespace}+").useLocale(Locale.getDefault()).useRadix(10);
				if (scanner.hasNext()) {
					switch (scanner.next().toLowerCase().charAt(0)) {
					case 'v':
						getVersion();
						continue;
					case 'o':
						configureEndpoint(scanner);
						continue;
					case 'r':
						clearEndpoint(scanner);
						continue;
					case 'b':
						createCallback(scanner);
						continue;
					case 'c':
						createAPSCallback(scanner);
						continue;
					case 'd':
						deleteCallback(scanner);
						continue;
					case 'l':
						listCallbacks();
						continue;
					case 'a':
						listAddresses();
						continue;
					case 'g':
						getInfoBaseAttribute(scanner);
						continue;
					case 's':
						setInfoBaseAttribute(scanner);
						continue;
					case 't':
						startGatewayDevice(scanner);
						continue;
					case 'n':
						startNodeDiscovery(scanner);
						continue;
					case 'u':
						subscribeNodeRemoval(scanner);
						continue;
					case 'i':
						startServiceDiscovery(scanner);
						continue;
					case 'p':
						getServiceDescriptor(scanner);
						continue;
					case 'z':
						getNodeDescriptor(scanner);
						continue;
					case 'm':
						sendAPSMessage(scanner);
						continue;
					case 'e':
						leave(scanner);
						continue;
					case 'j':
						permitJoin(scanner);
						continue;
					case 'f':
						startDefaultSequence();
						continue;
					case 'x':
						factory.close();
						System.exit(0);
					case 'h':
						showCommands();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void permitJoin(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger();
		short duration = scanner.nextShort();
		if (aoi.equals(BigInteger.ONE)) gateway.permitJoinAll(0, duration);
		else {
    		Address a = lastNodeAddress;
    		if (!aoi.equals(MINUS_ONE)) {
    			a = new Address();
    			a.setIeeeAddress(aoi);
    		}
    		gateway.permitJoin(0, a, duration);
		}
	}

	public void leave(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger();
		if (aoi.equals(BigInteger.ONE)) gateway.leaveAll();
		else {
			Address a = lastNodeAddress;
    		if (!aoi.equals(MINUS_ONE)) {
    			a = new Address();
    			a.setIeeeAddress(aoi);
    		}
    		gateway.leave(0, a);
		}
	}

	public void clearEndpoint(Scanner scanner) throws IOException, Exception, GatewayException {
		gateway.clearEndpoint(scanner.nextShort());
	}

	public void configureEndpoint(Scanner scanner) throws IOException, Exception {
		SimpleDescriptor desc = new SimpleDescriptor();
		desc.setEndPoint(scanner.nextShort());
		desc.setApplicationDeviceIdentifier(2);
		desc.setApplicationDeviceVersion((short)0);
		desc.setApplicationProfileIdentifier(0x0104);
		List<Integer> c = desc.getApplicationInputCluster();
		c.add(0);
		c.add(3);
		c.add(4);
		c.add(5);
		c.add(6);
		try {
			localEndpoint = gateway.configureEndpoint(0, desc);
			System.out.printf("reply EndPoint: %x\n", localEndpoint);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			det.setEndpoint(desc.getEndPoint());
			Representation r = converter.toRepresentation(info);
		}
	}

	public void configureStartupAttributeSet(StartupAttributeInfo sai) throws IOException, JAXBException {
	}

	public void createCallback(Scanner scanner) throws IOException, JAXBException {
		try {
			long cid = gateway.createCallback(buildCallback(scanner.nextInt()), this);
			System.out.printf("reply Callback Identifier: %d\n", cid);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail dt = new Info.Detail();
			info.setDetail(dt);
			dt.setCallbackIdentifier(1234567890L);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void createAPSCallback(Scanner scanner) throws IOException, JAXBException {
		try {
			long cid = gateway.createAPSCallback(scanner.nextShort(), this);
			System.out.printf("reply Callback Identifier: %d\n", cid);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail dt = new Info.Detail();
			info.setDetail(dt);
			dt.setCallbackIdentifier(1234567890L);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void deleteCallback(Scanner scanner) throws IOException, Exception, GatewayException {
		gateway.deleteCallback(scanner.nextLong());
	}

	public void getInfoBaseAttribute(Scanner scanner) throws IOException, JAXBException {
		try {
			gateway.getInfoBaseAttribute(scanner.nextShort());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			List<String> list = det.getValue();
			list.add("021345");
			Representation r = converter.toRepresentation(info);
		}
	}

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
			v.setFeatureSetIdentifier((short)1);
			v.setManufacturerVersion("manuf-1");
			v.setVersionIdentifier((short)2);
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			det.setVersion(v);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void listAddresses() throws IOException, JAXBException {
		try {
			List<Address> al = gateway.listAddresses().getAlias();
			for (Address a : al) {
				String alias = a.getAliasAddress();
				if (alias != null)
					System.out.printf("Alias Address: %s\n", alias);
				BigInteger bi = a.getIeeeAddress();
				if (bi != null)
					System.out.printf("Extended Address: %x\n", bi);
				System.out.printf("Short Address: %x\n", a.getNetworkAddress());
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			Aliases aliases = new Aliases();
			List<Address> al = aliases.getAlias();
			Address a1 = new Address();
			a1.setAliasAddress("PippoAlias01");
			al.add(a1);
			Address a2 = new Address();
			a2.setIeeeAddress(BigInteger.valueOf(122394883904L));
			a2.setNetworkAddress(232);
			a2.setAliasAddress("PippoAlias02");
			al.add(a2);
			Address a3 = new Address();
			a3.setNetworkAddress(1200);
			al.add(a3);
			aliases.setNumberOfAlias(3L);
			det.setAliases(aliases);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void listCallbacks() throws IOException, JAXBException {
		try {
			List<Long> list = gateway.listCallbacks();
			for (long l : list) {
				System.out.printf("Callback Identifier: %d (%x)\n", l, l);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Info.Detail det = new Info.Detail();
			info.setDetail(det);
			CallbackIdentifierList cil = new CallbackIdentifierList();
			info.getDetail().setCallbacks(cil);
			List<Long> ls = cil.getCallbackIdentifier();
			ls.add(100001L);
			ls.add(100002L);
			ls.add(100003L);
			ls.add(100004L);
			ls.add(100005L);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void readStartupAttributeSet(short index) throws IOException, JAXBException {
	}

	public void sendAPSMessage(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger(16);
		if (aoi.equals(MINUS_ONE)) aoi = lastNodeAddress.getIeeeAddress();
		short ep = scanner.nextShort();
		if (ep < 0) ep = lastEndpoint;
		APSMessage m = buildAPSMessage(aoi, ep);
		System.out.println("Sending APS Message");
		long mode = m.getDestinationAddressMode();
		switch ((int)mode) {
		case (int)GatewayConstants.EXTENDED_ADDRESS_MODE:
			System.out.printf("Destination IEEE Address: %d (%x)\n", m.getDestinationAddress().getIeeeAddress(), m.getDestinationAddress().getIeeeAddress());
			break;
		case (int)GatewayConstants.SHORT_ADDRESS_MODE:
			System.out.printf("Destination Network Address: %d (%x)\n", m.getDestinationAddress().getNetworkAddress(), m.getDestinationAddress().getNetworkAddress());
			break;
		case (int)GatewayConstants.ALIAS_ADDRESS_MODE:
			System.out.printf("Destination Network Address: %s\n", m.getDestinationAddress().getAliasAddress());
			break;
		default:
			System.out.printf("Unknowun Destination Address Mode!");
		}
		System.out.printf("Destination Endpoint: %x\n", m.getDestinationEndpoint());
		System.out.printf("Cluster ID: %x\n", m.getClusterID());
		System.out.printf("Profile ID: %x\n", m.getProfileID());
		System.out.printf("Source Endpoint: %x\n", m.getSourceEndpoint());
		System.out.printf("Radius: %d\n", m.getRadius());
		System.out.printf("TxOptions Acknowledged: %b\n", m.getTxOptions().isAcknowledged());
		System.out.printf("TxOptions Permit Fragmentation: %b\n", m.getTxOptions().isPermitFragmentation());
		System.out.printf("TxOptions Security Enabled: %b\n", m.getTxOptions().isSecurityEnabled());
		System.out.printf("TxOptions Use Network Key: %b\n", m.getTxOptions().isUseNetworkKey());
		byte[] data = m.getData();
		System.out.print("Data Packet: ");
		for (byte b : data) System.out.printf("%02x ", b);
		System.out.println();
		gateway.sendAPSMessage(m);
	}

	public void setInfoBaseAttribute(Scanner scanner) throws IOException, Exception {
		try {
			//gateway.setInfoBaseAttribute(scanner.nextInt(), scanner.next());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void startGatewayDevice(Scanner scanner) throws IOException, JAXBException {
		short index = scanner.nextShort();
		StartupAttributeInfo sai = new StartupAttributeInfo();
		sai.setStartupAttributeSetIndex(index);
		//sai.setStartupControl((short)1);
		sai.setDeviceType(LogicalType.COORDINATOR);
		//sai.setDeviceType(LogicalType.END_DEVICE);
		try {
			gateway.startGatewayDevice(0, sai);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A correct XML example follows");
			Info info = new Info();
			Status s = new Status();
			s.setCode((short)0);
			info.setStatus(s);
			byte[] id = BigInteger.valueOf(95867028590L).toByteArray();
			info.setRequestIdentifier(id);
			Representation r = converter.toRepresentation(info);
		}
	}

	public void startNodeDiscovery(Scanner scanner) throws IOException, Exception, GatewayException {
		int mask = GatewayConstants.DISCOVERY_LQI;
		if (scanner.next().equalsIgnoreCase("off")) mask = GatewayConstants.DISCOVERY_STOP;
		gateway.startNodeDiscovery(0, mask);
	}
	
	public void subscribeNodeRemoval(Scanner scanner) throws IOException, Exception, GatewayException {
		int mask = GatewayConstants.DISCOVERY_FRESHNESS | GatewayConstants.DISCOVERY_LEAVE;
		if (scanner.next().equalsIgnoreCase("off")) mask = GatewayConstants.DISCOVERY_STOP;
		gateway.subscribeNodeRemoval(0, mask);
	}

	public void startServiceDiscovery(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger(16);
		Address a = lastNodeAddress;
		if (!aoi.equals(MINUS_ONE)) {
			a = new Address();
			a.setIeeeAddress(aoi);
		}
		gateway.startServiceDiscovery(0, a);
	}

	public void getServiceDescriptor(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger(16);
		Address a = lastNodeAddress;
		if (!aoi.equals(MINUS_ONE)) {
			a = new Address();
			a.setIeeeAddress(aoi);
		}
		short ep = scanner.nextShort();
		if (ep < 0) ep = lastEndpoint;
		gateway.getServiceDescriptor(0, a, ep);
	}
	
	public void getNodeDescriptor(Scanner scanner) throws IOException, Exception, GatewayException {
		BigInteger aoi = scanner.nextBigInteger(16);
		Address a = lastNodeAddress;
		if (!aoi.equals(MINUS_ONE)) {
			a = new Address();
			a.setIeeeAddress(aoi);
		}
		gateway.getNodeDescriptor(0, a);
	}

	public void notifyAPSMessage(APSMessageEvent message) {
		System.out.println("Received APS message from GAL");
		System.out.printf("APS Status: %d (%x)\n", message.getAPSStatus(), message.getAPSStatus());
		System.out.printf("Cluster ID: %d (%x)\n", message.getClusterID(), message.getClusterID());
		System.out.printf("Destination Network Address: %d (%x)\n", message.getDestinationAddress().getNetworkAddress(), message.getDestinationAddress().getNetworkAddress());
		System.out.printf("Destination Endpoint: %d (%x)\n", message.getDestinationEndpoint(), message.getDestinationEndpoint());
		System.out.printf("Profile ID: %d (%x)\n", message.getProfileID(), message.getProfileID());
		System.out.printf("Security Status: %s\n", message.getSecurityStatus().value());
		System.out.printf("Source Network Address: %d (%x)\n", message.getSourceAddress().getNetworkAddress(), message.getSourceAddress().getNetworkAddress());
		System.out.printf("Source IEEE Address: %d (%x)\n", message.getSourceAddress().getIeeeAddress(), message.getSourceAddress().getIeeeAddress());
		System.out.printf("Source Endpoint: %d (%x)\n", message.getSourceEndpoint(), message.getSourceEndpoint());
		byte[] data = message.getData();
		for (int i = 0; i < data.length; ++i) {
			System.out.printf("%02x ", data[i]);
		}
	}

	public void gatewayStartResult(Status s) {
		System.out.println("Gateway Started notification from GAL");
	}


	public void leaveResult(Status s) {
		System.out.println("Leave notification from GAL");
	}


	public void permitJoinResult(Status s) {
		System.out.println("Permit Join notification from GAL");
	}

	public void nodeDiscovered(Status s, WSNNode node) {
		System.out.println("Node Discoverd notification from GAL");
		if (s.getCode() != GatewayConstants.SUCCESS) {
			System.out.println("Error " + s.getCode() + " - " + s.getMessage());
			return;
		}
		Address a = node.getAddress();
		System.out.printf("Alias Address: %s\n", a.getAliasAddress());
		System.out.printf("Extended Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
		System.out.printf("Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());
		this.lastNodeAddress = a;
		
		a = node.getParentAddress();
		if (a != null) {
			System.out.printf("Parent Node Full Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
			System.out.printf("Parent Node Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());
		}
		System.out.printf("Start Index: %d\n", node.getStartIndex());
		List<AssociatedDevices> list = node.getAssociatedDevices();
		for (AssociatedDevices d : list) {
			System.out.printf("Total number: %d", d.getTotalNumber());
			List<SonNode> sons = d.getSonNode();
			for (SonNode i : sons) {
				System.out.printf("Son Node Short Address: %d\n", i.getShortAddr());
			}
		}
	}
	
	public void nodeRemoved(Status s, WSNNode node) {
		System.out.println("Node Removed notification from GAL");
		if (s.getCode() != GatewayConstants.SUCCESS) {
			System.out.println("Error " + s.getCode() + " - " + s.getMessage());
			return;
		}
		Address a = node.getAddress();
		System.out.printf("Alias Address: %s\n", a.getAliasAddress());
		System.out.printf("Extended Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
		System.out.printf("Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());
		this.lastNodeAddress = a;
		
		a = node.getParentAddress();
		if (a != null) {
			System.out.printf("Parent Node Full Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
			System.out.printf("Parent Node Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());
		}
	}

	public void servicesDiscovered(Status s, NodeServices services) {
		System.out.println("Services Discoverd notification from GAL");
		if (s.getCode() != GatewayConstants.SUCCESS) {
			System.out.println("Error " + s.getCode() + " - " + s.getMessage());
			return;
		}
		List<ActiveEndpoints> list = services.getActiveEndpoints();
		Address a = services.getAddress();
		String alias = a.getAliasAddress();
		System.out.printf("Alias Address: %s\n", alias);
		System.out.printf("IEEE Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
		System.out.printf("Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());
		for (ActiveEndpoints aep : list) {
			System.out.printf("End Point: %d (%x)\n", aep.getEndPoint(), aep.getEndPoint());
		}
		
		try {
			// Retrieve the simple descriptor of the 1st endpoint
			//gateway.getServiceDescriptor(0, a, list.get(0).getEndPoint());
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void serviceDescriptorRetrieved(Status s, ServiceDescriptor service) {
		System.out.println("Service Descriptor notification from GAL");
		if (s.getCode() != GatewayConstants.SUCCESS) {
			System.out.println("Error " + s.getCode() + " - " + s.getMessage());
			return;
		}
		Address a = service.getAddress();
		String alias = a.getAliasAddress();
		System.out.printf("Alias Address: %s\n", alias);
		System.out.printf("IEEE Address: %d (%x)\n", a.getIeeeAddress(), a.getIeeeAddress());
		System.out.printf("Short Address: %d (%x)\n", a.getNetworkAddress(), a.getNetworkAddress());

		// should contain exactly 1 element in the list
		this.lastEndpoint = service.getEndPoint();
		System.out.printf("End Point: %d (%x)\n", lastEndpoint, lastEndpoint);
		SimpleDescriptor sd = service.getSimpleDescriptor();
		System.out.printf("Application Device Identifier: (%x)\n", sd.getApplicationDeviceIdentifier());
		System.out.printf("Application Device Version: (%x)\n", sd.getApplicationDeviceVersion());
		System.out.printf("Application Profile Identifier: (%x)\n", sd.getApplicationProfileIdentifier());
		System.out.printf("Input Clusters: ");
		List<Integer> clus = sd.getApplicationInputCluster();
		for (int c : clus) {
			System.out.printf("%02x ", c);
		}
		System.out.printf("\nOutput Clusters: ");
		clus = sd.getApplicationOutputCluster();
		for (int c : clus) {
			System.out.printf("%02x ", c);
		}
		System.out.println();
	}
	

	public void nodeDescriptorRetrieved(Status s, NodeDescriptor node) {
		System.out.println("Node Descriptor notification from GAL");
		if (s.getCode() != GatewayConstants.SUCCESS) {
			System.out.println("Error " + s.getCode() + " - " + s.getMessage());
			return;
		}
	}
	
	void startDefaultSequence() {
		try {
			// configure endpoint
			SimpleDescriptor desc = new SimpleDescriptor();
			desc.setEndPoint(localEndpoint);
			localEndpoint = gateway.configureEndpoint(0, desc);
			
			// create APS callback
			long cid = gateway.createAPSCallback(localEndpoint, this);
			
			// start discovery announcement;
			gateway.startNodeDiscovery(0, GatewayConstants.DISCOVERY_LQI);
			
			// start gateway
			gateway.startGatewayDevice(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dongleResetResult(Status status) {
	}

	public void bindingResult(Status status) {
	}

	public void unbindingResult(Status status) {
	}

	public void nodeBindingsRetrieved(Status status, BindingList bindings) {
	}
}
