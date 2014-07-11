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
package org.energy_home.jemma.ah.hac;


import org.osgi.service.cm.ManagedServiceFactory;

/**
 * This interface is exposed by an appliance factory associated with a specific
 * appliance class; an appliance factory must be provided by all {@code driver
 * appliances}, while a factory class is not always needed for a {@code logical
 * appliance}.
 * <p>
 * For each provided appliance factory associated with a specific appliance
 * class, an object is registered with the framework under the {@code
 * IApplianceFactory} interface.
 * <p>
 * For each created appliance, the factory is responsible to register the
 * appliance object with the framework under the {@code IManagedAppliance}
 * interface. In case of a singleton {@code logical appliance}, a factory is not
 * needed and the appliance can directly register the {@code IManagedAppliance}
 * service with the framework.
 * <p>
 * In case of a {@code driver appliance} the associated factory also implements
 * the {@link org.osgi.service.device.Driver} interface: the first time that a
 * Device Service ({@link org.osgi.service.device.Device}, {@link IHacDevice})
 * is detected and attached to the driver (through the process specified in the
 * OSGi Device Access Specification), the factory is responsible to create an
 * appliance and associate it with the attached device. If the device service is
 * unregistered (this occurs when the home gateway detects that the physical
 * device is disconnected from the HAN), the corresponding {@code
 * IManagedAppliance} end points status is updated for (
 * {@link IEndPoint#isAvailable()}). The driver is responsible to re-associate
 * the OSGi device service with the previously registered {@code
 * IManagedAppliance} interface each time the same physical device is
 * re-connected to the HAN and discovered by the home gateway.
 * 
 * @see {@link IManagedAppliance}, {@link IAppliance}
 */
public interface IApplianceFactory extends ManagedServiceFactory {
	/**
	 * This method is called by the A@H framework when the {@code
	 * IApplianceFactory} OSGi service is registered. After this invocation, the
	 * appliance factory must be ready to create, update and delete appliance
	 * instances.
	 */
	public void init();
	
	/**
	 * Returns a set of properties associated to a specific appliance class;
	 * this method returns the same descriptor as those returned by the method
	 * {@link IApplianceFactory#getDescriptor()} for all appliance instances
	 * created by this factory
	 * 
	 * @return The {@link IApplianceDescriptor} associated to the appliance
	 *         class managed by this factory
	 */
	public IApplianceDescriptor getDescriptor();
}