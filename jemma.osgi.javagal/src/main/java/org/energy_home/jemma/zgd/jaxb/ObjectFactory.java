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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.9-03/31/2009 04:14 PM(snajper)-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.03 at 05:23:14 PM CEST 
//


package org.energy_home.jemma.zgd.jaxb;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.energy_home.jemma.zgd.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ResetInfo_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "ResetInfo");
    private final static QName _RouteDiscoveryInfo_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "RouteDiscoveryInfo");
    private final static QName _UserDescriptor_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "UserDescriptor");
    private final static QName _NetworkConfiguration_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "NetworkConfiguration");
    private final static QName _Group_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "Group");
    private final static QName _ZCLMessage_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "ZCLMessage");
    private final static QName _APSMessage_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "APSMessage");
    private final static QName _NWKMessage_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "NWKMessage");
    private final static QName _ZDPCommand_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "ZDPCommand");
    private final static QName _Value_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "Value");
    private final static QName _Callback_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "Callback");
    private final static QName _Info_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "Info");
    private final static QName _StartupAttributeInfo_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "StartupAttributeInfo");
    private final static QName _NodeDescriptor_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "NodeDescriptor");
    private final static QName _Alias_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "Alias");
    private final static QName _JoinConfiguration_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "JoinConfiguration");
    private final static QName _SimpleDescriptor_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "SimpleDescriptor");
    private final static QName _PowerDescriptor_QNAME = new QName("http://www.zigbee.org/GWGRESTSchema", "PowerDescriptor");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.energy_home.jemma.zgd.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link JoiningInfo }
     * 
     */
    public JoiningInfo createJoiningInfo() {
        return new JoiningInfo();
    }

    /**
     * Create an instance of {@link NodeServicesList }
     * 
     */
    public NodeServicesList createNodeServicesList() {
        return new NodeServicesList();
    }

    /**
     * Create an instance of {@link RouteDiscoveryInfo }
     * 
     */
    public RouteDiscoveryInfo createRouteDiscoveryInfo() {
        return new RouteDiscoveryInfo();
    }

    /**
     * Create an instance of {@link NetworkConfiguration }
     * 
     */
    public NetworkConfiguration createNetworkConfiguration() {
        return new NetworkConfiguration();
    }

    /**
     * Create an instance of {@link APSMessageResult }
     * 
     */
    public APSMessageResult createAPSMessageResult() {
        return new APSMessageResult();
    }

    /**
     * Create an instance of {@link NodeDescriptor }
     * 
     */
    public NodeDescriptor createNodeDescriptor() {
        return new NodeDescriptor();
    }

    /**
     * Create an instance of {@link Filter.AddressSpecification }
     * 
     */
    public Filter.AddressSpecification createFilterAddressSpecification() {
        return new Filter.AddressSpecification();
    }

    /**
     * Create an instance of {@link GroupList }
     * 
     */
    public GroupList createGroupList() {
        return new GroupList();
    }

    /**
     * Create an instance of {@link WSNNode }
     * 
     */
    public WSNNode createWSNNode() {
        return new WSNNode();
    }

    /**
     * Create an instance of {@link BindingList }
     * 
     */
    public BindingList createBindingList() {
        return new BindingList();
    }

    /**
     * Create an instance of {@link ResetInfo }
     * 
     */
    public ResetInfo createResetInfo() {
        return new ResetInfo();
    }

    /**
     * Create an instance of {@link Device }
     * 
     */
    public Device createDevice() {
        return new Device();
    }

    /**
     * Create an instance of {@link NetworkDescriptorList }
     * 
     */
    public NetworkDescriptorList createNetworkDescriptorList() {
        return new NetworkDescriptorList();
    }

    /**
     * Create an instance of {@link InterPANMessage }
     * 
     */
    public InterPANMessage createInterPANMessage() {
        return new InterPANMessage();
    }

    /**
     * Create an instance of {@link UserDescriptor }
     * 
     */
    public UserDescriptor createUserDescriptor() {
        return new UserDescriptor();
    }

    /**
     * Create an instance of {@link Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link DescriptorCapability }
     * 
     */
    public DescriptorCapability createDescriptorCapability() {
        return new DescriptorCapability();
    }

    /**
     * Create an instance of {@link Neighbor }
     * 
     */
    public Neighbor createNeighbor() {
        return new Neighbor();
    }

    /**
     * Create an instance of {@link MACCapability }
     * 
     */
    public MACCapability createMACCapability() {
        return new MACCapability();
    }

    /**
     * Create an instance of {@link Callback }
     * 
     */
    public Callback createCallback() {
        return new Callback();
    }

    /**
     * Create an instance of {@link ZCLMessage }
     * 
     */
    public ZCLMessage createZCLMessage() {
        return new ZCLMessage();
    }

    /**
     * Create an instance of {@link Binding }
     * 
     */
    public Binding createBinding() {
        return new Binding();
    }

    /**
     * Create an instance of {@link StartupAttributeInfo }
     * 
     */
    public StartupAttributeInfo createStartupAttributeInfo() {
        return new StartupAttributeInfo();
    }

    /**
     * Create an instance of {@link NWKMessageEvent }
     * 
     */
    public NWKMessageEvent createNWKMessageEvent() {
        return new NWKMessageEvent();
    }

    /**
     * Create an instance of {@link PowerDescriptor }
     * 
     */
    public PowerDescriptor createPowerDescriptor() {
        return new PowerDescriptor();
    }

    /**
     * Create an instance of {@link CallbackIdentifierList }
     * 
     */
    public CallbackIdentifierList createCallbackIdentifierList() {
        return new CallbackIdentifierList();
    }

    /**
     * Create an instance of {@link SonNode }
     * 
     */
    public SonNode createSonNode() {
        return new SonNode();
    }

    /**
     * Create an instance of {@link InterPANMessageResult }
     * 
     */
    public InterPANMessageResult createInterPANMessageResult() {
        return new InterPANMessageResult();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

    /**
     * Create an instance of {@link Filter }
     * 
     */
    public Filter createFilter() {
        return new Filter();
    }

    /**
     * Create an instance of {@link ServerMask }
     * 
     */
    public ServerMask createServerMask() {
        return new ServerMask();
    }

    /**
     * Create an instance of {@link Action }
     * 
     */
    public Action createAction() {
        return new Action();
    }

    /**
     * Create an instance of {@link LanguageAndCharacterSet }
     * 
     */
    public LanguageAndCharacterSet createLanguageAndCharacterSet() {
        return new LanguageAndCharacterSet();
    }

    /**
     * Create an instance of {@link Aliases }
     * 
     */
    public Aliases createAliases() {
        return new Aliases();
    }

    /**
     * Create an instance of {@link MgmtLqiRequest }
     * 
     */
    public MgmtLqiRequest createMgmtLqiRequest() {
        return new MgmtLqiRequest();
    }

    /**
     * Create an instance of {@link EnergyScanResult }
     * 
     */
    public EnergyScanResult createEnergyScanResult() {
        return new EnergyScanResult();
    }

    /**
     * Create an instance of {@link PowerSources }
     * 
     */
    public PowerSources createPowerSources() {
        return new PowerSources();
    }

    /**
     * Create an instance of {@link MgmtLqiResponse }
     * 
     */
    public MgmtLqiResponse createMgmtLqiResponse() {
        return new MgmtLqiResponse();
    }

    /**
     * Create an instance of {@link InterPANMessageEvent }
     * 
     */
    public InterPANMessageEvent createInterPANMessageEvent() {
        return new InterPANMessageEvent();
    }

    /**
     * Create an instance of {@link Buffer }
     * 
     */
    public Buffer createBuffer() {
        return new Buffer();
    }

    /**
     * Create an instance of {@link Info.Detail }
     * 
     */
    public Info.Detail createInfoDetail() {
        return new Info.Detail();
    }

    /**
     * Create an instance of {@link NetworkStatusCode }
     * 
     */
    public NetworkStatusCode createNetworkStatusCode() {
        return new NetworkStatusCode();
    }

    /**
     * Create an instance of {@link Version }
     * 
     */
    public Version createVersion() {
        return new Version();
    }

    /**
     * Create an instance of {@link Filter.LevelSpecification }
     * 
     */
    public Filter.LevelSpecification createFilterLevelSpecification() {
        return new Filter.LevelSpecification();
    }

    /**
     * Create an instance of {@link ServiceDescriptor }
     * 
     */
    public ServiceDescriptor createServiceDescriptor() {
        return new ServiceDescriptor();
    }

    /**
     * Create an instance of {@link NeighborList }
     * 
     */
    public NeighborList createNeighborList() {
        return new NeighborList();
    }

    /**
     * Create an instance of {@link ZCLCommandResult }
     * 
     */
    public ZCLCommandResult createZCLCommandResult() {
        return new ZCLCommandResult();
    }

    /**
     * Create an instance of {@link NodeServices.ActiveEndpoints }
     * 
     */
    public NodeServices.ActiveEndpoints createNodeServicesActiveEndpoints() {
        return new NodeServices.ActiveEndpoints();
    }

    /**
     * Create an instance of {@link ZCLCommand }
     * 
     */
    public ZCLCommand createZCLCommand() {
        return new ZCLCommand();
    }

    /**
     * Create an instance of {@link LQINode }
     * 
     */
    public LQINode createLQINode() {
        return new LQINode();
    }

    /**
     * Create an instance of {@link LQIInformation }
     * 
     */
    public LQIInformation createLQIInformation() {
        return new LQIInformation();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link WSNNodeList }
     * 
     */
    public WSNNodeList createWSNNodeList() {
        return new WSNNodeList();
    }

    /**
     * Create an instance of {@link Status }
     * 
     */
    public Status createStatus() {
        return new Status();
    }

    /**
     * Create an instance of {@link NWKMessage }
     * 
     */
    public NWKMessage createNWKMessage() {
        return new NWKMessage();
    }

    /**
     * Create an instance of {@link Info }
     * 
     */
    public Info createInfo() {
        return new Info();
    }

    /**
     * Create an instance of {@link ZDPMessage }
     * 
     */
    public ZDPMessage createZDPMessage() {
        return new ZDPMessage();
    }

    /**
     * Create an instance of {@link NWKMessageResult }
     * 
     */
    public NWKMessageResult createNWKMessageResult() {
        return new NWKMessageResult();
    }

    /**
     * Create an instance of {@link Action.DecodeSpecification }
     * 
     */
    public Action.DecodeSpecification createActionDecodeSpecification() {
        return new Action.DecodeSpecification();
    }

    /**
     * Create an instance of {@link APSMessageEvent }
     * 
     */
    public APSMessageEvent createAPSMessageEvent() {
        return new APSMessageEvent();
    }

    /**
     * Create an instance of {@link NodeServices }
     * 
     */
    public NodeServices createNodeServices() {
        return new NodeServices();
    }

    /**
     * Create an instance of {@link JoinConfiguration }
     * 
     */
    public JoinConfiguration createJoinConfiguration() {
        return new JoinConfiguration();
    }

    /**
     * Create an instance of {@link APSMessage }
     * 
     */
    public APSMessage createAPSMessage() {
        return new APSMessage();
    }

    /**
     * Create an instance of {@link MACMessage }
     * 
     */
    public MACMessage createMACMessage() {
        return new MACMessage();
    }

    /**
     * Create an instance of {@link ZDPCommand }
     * 
     */
    public ZDPCommand createZDPCommand() {
        return new ZDPCommand();
    }

    /**
     * Create an instance of {@link EnergyScanResult.ScannedChannel }
     * 
     */
    public EnergyScanResult.ScannedChannel createEnergyScanResultScannedChannel() {
        return new EnergyScanResult.ScannedChannel();
    }

    /**
     * Create an instance of {@link NetworkDescriptor }
     * 
     */
    public NetworkDescriptor createNetworkDescriptor() {
        return new NetworkDescriptor();
    }

    /**
     * Create an instance of {@link AssociatedDevices }
     * 
     */
    public AssociatedDevices createAssociatedDevices() {
        return new AssociatedDevices();
    }

    /**
     * Create an instance of {@link SimpleDescriptor }
     * 
     */
    public SimpleDescriptor createSimpleDescriptor() {
        return new SimpleDescriptor();
    }

    /**
     * Create an instance of {@link PolledMessage }
     * 
     */
    public PolledMessage createPolledMessage() {
        return new PolledMessage();
    }

    /**
     * Create an instance of {@link Filter.MessageSpecification }
     * 
     */
    public Filter.MessageSpecification createFilterMessageSpecification() {
        return new Filter.MessageSpecification();
    }

    /**
     * Create an instance of {@link TxOptions }
     * 
     */
    public TxOptions createTxOptions() {
        return new TxOptions();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ResetInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "ResetInfo")
    public JAXBElement<ResetInfo> createResetInfo(ResetInfo value) {
        return new JAXBElement<ResetInfo>(_ResetInfo_QNAME, ResetInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RouteDiscoveryInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "RouteDiscoveryInfo")
    public JAXBElement<RouteDiscoveryInfo> createRouteDiscoveryInfo(RouteDiscoveryInfo value) {
        return new JAXBElement<RouteDiscoveryInfo>(_RouteDiscoveryInfo_QNAME, RouteDiscoveryInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UserDescriptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "UserDescriptor")
    public JAXBElement<UserDescriptor> createUserDescriptor(UserDescriptor value) {
        return new JAXBElement<UserDescriptor>(_UserDescriptor_QNAME, UserDescriptor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NetworkConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "NetworkConfiguration")
    public JAXBElement<NetworkConfiguration> createNetworkConfiguration(NetworkConfiguration value) {
        return new JAXBElement<NetworkConfiguration>(_NetworkConfiguration_QNAME, NetworkConfiguration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Group }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "Group")
    public JAXBElement<Group> createGroup(Group value) {
        return new JAXBElement<Group>(_Group_QNAME, Group.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ZCLMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "ZCLMessage")
    public JAXBElement<ZCLMessage> createZCLMessage(ZCLMessage value) {
        return new JAXBElement<ZCLMessage>(_ZCLMessage_QNAME, ZCLMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link APSMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "APSMessage")
    public JAXBElement<APSMessage> createAPSMessage(APSMessage value) {
        return new JAXBElement<APSMessage>(_APSMessage_QNAME, APSMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NWKMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "NWKMessage")
    public JAXBElement<NWKMessage> createNWKMessage(NWKMessage value) {
        return new JAXBElement<NWKMessage>(_NWKMessage_QNAME, NWKMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ZDPCommand }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "ZDPCommand")
    public JAXBElement<ZDPCommand> createZDPCommand(ZDPCommand value) {
        return new JAXBElement<ZDPCommand>(_ZDPCommand_QNAME, ZDPCommand.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "Value")
    public JAXBElement<String> createValue(String value) {
        return new JAXBElement<String>(_Value_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Callback }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "Callback")
    public JAXBElement<Callback> createCallback(Callback value) {
        return new JAXBElement<Callback>(_Callback_QNAME, Callback.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Info }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "Info")
    public JAXBElement<Info> createInfo(Info value) {
        return new JAXBElement<Info>(_Info_QNAME, Info.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StartupAttributeInfo }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "StartupAttributeInfo")
    public JAXBElement<StartupAttributeInfo> createStartupAttributeInfo(StartupAttributeInfo value) {
        return new JAXBElement<StartupAttributeInfo>(_StartupAttributeInfo_QNAME, StartupAttributeInfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NodeDescriptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "NodeDescriptor")
    public JAXBElement<NodeDescriptor> createNodeDescriptor(NodeDescriptor value) {
        return new JAXBElement<NodeDescriptor>(_NodeDescriptor_QNAME, NodeDescriptor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Address }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "Alias")
    public JAXBElement<Address> createAlias(Address value) {
        return new JAXBElement<Address>(_Alias_QNAME, Address.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link JoinConfiguration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "JoinConfiguration")
    public JAXBElement<JoinConfiguration> createJoinConfiguration(JoinConfiguration value) {
        return new JAXBElement<JoinConfiguration>(_JoinConfiguration_QNAME, JoinConfiguration.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleDescriptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "SimpleDescriptor")
    public JAXBElement<SimpleDescriptor> createSimpleDescriptor(SimpleDescriptor value) {
        return new JAXBElement<SimpleDescriptor>(_SimpleDescriptor_QNAME, SimpleDescriptor.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PowerDescriptor }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.zigbee.org/GWGRESTSchema", name = "PowerDescriptor")
    public JAXBElement<PowerDescriptor> createPowerDescriptor(PowerDescriptor value) {
        return new JAXBElement<PowerDescriptor>(_PowerDescriptor_QNAME, PowerDescriptor.class, null, value);
    }

}
