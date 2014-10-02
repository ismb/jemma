package org.energy_home.jemma.javagal.rest.util;

import org.energy_home.jemma.zgd.jaxb.*;
import org.restlet.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by fabio on 01/10/14.
 */
public class ThreadPoolManager {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadPoolManager.class);
    private static ThreadPoolManager instance;
    private int cores;
    private ExecutorService executor;
    private ThreadPoolManager() {
        try{
            cores = Runtime.getRuntime().availableProcessors() + 1;
        } catch(Exception e) {
            LOG.error("Unable to get cores number. Error: ", e.getMessage());
            cores = 4;
        }
        executor = Executors.newFixedThreadPool(cores);
    }
    public static ThreadPoolManager getInstance(){
        return(instance == null) ? new ThreadPoolManager() : instance;
    }
    public void gatewayStartResult(Status status, Context context, String descriptor) {
        Runnable worker = new WorkerThread(context, status, descriptor);
        executor.execute(worker);
    }
    public void nodeDiscovered(Status status, Context context, WSNNode wsnNode, String descriptor) {
        Runnable worker = new WorkerThread(context, status, wsnNode, descriptor);
        executor.execute(worker);
    }
    public void servicesDiscovered(Status status, Context context, NodeServices services, String descriptor) {
        Runnable worker = new WorkerThread(context, status, services, descriptor);
        executor.execute(worker);
    }
    public void serviceDescriptorRetrieved(Status status, Context context, ServiceDescriptor serviceDescriptor, String descriptor) {
        Runnable worker = new WorkerThread(context, status, serviceDescriptor, descriptor);
        executor.execute(worker);
    }
    public void nodeBindingsRetrieved(Status status, Context context, BindingList list, String descriptor) {
        Runnable worker = new WorkerThread(context, status, list,  descriptor);
        executor.execute(worker);
    }
    public void nodeDescriptorRetrievedExtended(Status status, Context context, Address address, NodeDescriptor nodeDescriptor, String descriptor) {
        Runnable worker = new WorkerThread(context, status, nodeDescriptor, address, descriptor);
        executor.execute(worker);
    }
    public void nodeDescriptorRetrieved(Status status, Context context, NodeDescriptor node, String descriptor) {
        Runnable worker = new WorkerThread(context, status, node, descriptor);
        executor.execute(worker);
    }
    public void leaveResultExtended(Status status, Context context, Address address, String descriptor) {
        Runnable worker = new WorkerThread(context, status, address, descriptor);
        executor.execute(worker);

    }
    public void notifyZDPCommand(Context context, ZDPMessage zdpMessage, String descriptor) {
        Runnable worker = new WorkerThread(context, zdpMessage, descriptor);
        executor.execute(worker);
    }
    public void notifyZCLCommand(Context context, ZCLMessage zclMessage, String descriptor) {
        Runnable worker = new WorkerThread(context, zclMessage,descriptor);
        executor.execute(worker);
    }
    public void notifyAPSMessage(Context context, APSMessageEvent apsMessageEvent,long calBackIdentifier, String descriptor) {
        Runnable worker = new WorkerThread(context, apsMessageEvent, calBackIdentifier, descriptor);
        executor.execute(worker);
    }
    public void notifyInterPANMessage(Context context, InterPANMessageEvent interPANMessageEvent, long calBackIdentifier, String descriptor) {
        Runnable worker = new WorkerThread(context, interPANMessageEvent, calBackIdentifier, descriptor);
        executor.execute(worker);
    }
}
