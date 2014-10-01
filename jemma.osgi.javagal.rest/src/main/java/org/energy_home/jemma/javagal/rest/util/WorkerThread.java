package org.energy_home.jemma.javagal.rest.util;

import org.energy_home.jemma.zgd.jaxb.*;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fabio on 01/10/14.
 */
public class WorkerThread implements Runnable {

    private Status status;
    private WSNNode wsnNode;
    private NodeServices nodeServices;
    private NodeDescriptor nodeDescriptor;
    private ServiceDescriptor serviceDescriptor;
    private BindingList bindingList;
    private Address addressOfInteres;
    private ZDPMessage zdpMessage;
    private InterPANMessageEvent interPANMessageEvent;
    private ZCLMessage zclMessage;
    private Context context;
    private String descriptor;
    private long calbackIdentifier;
    private APSMessageEvent apsMessageEvent;
    private static final Logger LOG = LoggerFactory.getLogger(WorkerThread.class);
    public WorkerThread(Context context, Status status, String descriptor) {
        this.status = status;
        this.context = context;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, WSNNode wsnNode, String descriptor) {
        this.context = context;
        this.status = status;
        this.wsnNode = wsnNode;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, NodeServices nodeServices, String descriptor){
        this.context = context;
        this.status = status;
        this.nodeServices = nodeServices;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, ServiceDescriptor serviceDescriptor, String descriptor){
        this.context = context;
        this.status = status;
        this.serviceDescriptor = serviceDescriptor;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, BindingList bindingList, String descriptor){
        this.context = context;
        this.status = status;
        this.bindingList = bindingList;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, NodeDescriptor nodeDescriptor, Address addressOfInteres, String descriptor){
        this.context = context;
        this.status = status;
        this.nodeDescriptor = nodeDescriptor;
        this.addressOfInteres = addressOfInteres;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, Status status, NodeDescriptor nodeDescriptor, String descriptor){
        this.context = context;
        this.status = status;
        this.nodeDescriptor = nodeDescriptor;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context contex, Status status, Address addressOfInteres, String descriptor) {
        this.context = contex;
        this.status = status;
        this.addressOfInteres = addressOfInteres;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, ZCLMessage zclMessage, String descriptor) {
        this.context = context;
        this.zclMessage = zclMessage;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, ZDPMessage zdpMessage, String descriptor) {
        this.context = context;
        this.zdpMessage = zdpMessage;
        this.descriptor = descriptor;
    }
    public WorkerThread(Context context, APSMessageEvent apsMessageEvent, long calbackIdentifier, String descriptor) {
        this.context = context;
        this.apsMessageEvent = apsMessageEvent;
        this.descriptor = descriptor;
        this.calbackIdentifier = calbackIdentifier;
    }
    public WorkerThread(Context context, InterPANMessageEvent interPANMessageEvent, long calbackIdentifier, String descriptor) {
        this.context = context;
        this.interPANMessageEvent = interPANMessageEvent;
        this.descriptor = descriptor;
        this.calbackIdentifier = calbackIdentifier;
    }
    @Override
    public void run() {
        try {
            ClientResource source = new ClientResource(context, descriptor);
            Info info = new Info();
            Info.Detail detail = new Info.Detail();
            if (status != null)
                info.setStatus(status);
            if (wsnNode != null)
                detail.setWSNNode(wsnNode);
            if (nodeServices != null)
                detail.setNodeServices(nodeServices);
            if (serviceDescriptor != null)
                detail.setServiceDescriptor(serviceDescriptor);
            if (bindingList != null)
                detail.setBindings(bindingList);
            if (nodeDescriptor != null)
                detail.setNodeDescriptor(nodeDescriptor);
            if (addressOfInteres != null) {
                WSNNode n = new WSNNode();
                n.setAddress(addressOfInteres);
                detail.setWSNNode(n);
            }
            if (zdpMessage != null)
                detail.setZDPMessage(zdpMessage);
            if (zclMessage != null)
                detail.setZCLMessage(zclMessage);
            if(apsMessageEvent != null) {
                detail.setAPSMessageEvent(apsMessageEvent);
                info.setDetail(detail);
                info.setEventCallbackIdentifier(calbackIdentifier);
            }
            if(interPANMessageEvent != null) {
                detail.setInterPANMessageEvent(interPANMessageEvent);
                info.setDetail(detail);
                info.setEventCallbackIdentifier(calbackIdentifier);
            }
            info.setDetail(detail);
            String xml = Util.marshal(info);
            source.post(xml, MediaType.TEXT_XML);
            source.release();
        } catch(Exception e) {
           LOG.error("Error ???", e);
        }

    }
}

