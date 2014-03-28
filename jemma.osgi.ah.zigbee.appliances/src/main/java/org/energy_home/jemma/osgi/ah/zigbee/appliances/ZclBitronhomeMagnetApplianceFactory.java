package org.energy_home.jemma.osgi.ah.zigbee.appliances;

import java.util.Dictionary;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.DriverApplianceFactory;
import org.osgi.service.device.Driver;

public class ZclBitronhomeMagnetApplianceFactory extends DriverApplianceFactory implements Driver{

	public static final String APPLIANCE_TYPE = "org.energy_home.jemma.ah.zigbee.bitronhome.magnet";
	public static final String APPLIANCE_FRIENDLY_NAME = "bitronhome magnet sensor";
	public static final String DEVICE_TYPE = "ZigBee";

	public static final IApplianceDescriptor APPLIANCE_DESCRIPTOR = new ApplianceDescriptor(APPLIANCE_TYPE, null,
			APPLIANCE_FRIENDLY_NAME);

	public IApplianceDescriptor getDescriptor() {
		return APPLIANCE_DESCRIPTOR;
	}

	public Appliance getInstance(String pid, Dictionary config) throws ApplianceException {
		return new ZclBitronhomeMagnetAppliance(pid, config);
	}

	public String deviceMatchFilterString() {
		//String result = "(&(DEVICE_CATEGORY=ZigBee)(zigbee.device.eps.number=1)(zigbee.device.profile.id=260)(zigbee.device.device.id=6)(zigbee.device.manufacturer.id=0))";
		String result="";
		return result; 
		
	}
}
