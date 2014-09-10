package org.energy_home.jemma.ah.zigbee.zcl.cluster.lube;

import org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityClient;
import org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterAttributeException;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZclFrame;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclAirQualityClient extends ZclServiceCluster implements AirQualityClient, ZigBeeDeviceListener {

	public final static short CLUSTER_ID = 3071;

	public ZclAirQualityClient() throws ApplianceException {
		super();
	}

	protected int getClusterId() {
		return CLUSTER_ID;
	}

	protected IZclAttributeDescriptor[] getPeerClusterAttributeDescriptors() {
		return ZclAirQualityServer.attributeDescriptors;
	}

	public void execExternalAirQualityIndexNotification(short Data, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		int size = 0;
		size += ZclDataTypeUI8.zclSize(Data);
		ZclFrame zclFrame = new ZclFrame(1, size);
		zclFrame.setCommandId(2);
		ZclDataTypeUI8.zclSerialize(zclFrame, Data);
		issueExec(zclFrame, 11, context);
	}

	protected int readAttributeResponseGetSize(int attrId) throws ServiceClusterException, ZclValidationException {
		switch (attrId) {
		case 0:
			return ZclDataTypeUI8.zclSize((short) 0);
		default:
			throw new UnsupportedClusterAttributeException();
		}
	}

	protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId) throws ApplianceException, ServiceClusterException {
		AirQualityServer c = ((AirQualityServer) getSinglePeerCluster((AirQualityServer.class.getName())));
		switch (attrId) {
		case 0: {
			short v;
			v = c.getAirQualityIndex(endPoint.getDefaultRequestContext());
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, ZCL.SUCCESS);
			zclResponseFrame.appendUInt8(ZclDataTypeUI8.ZCL_DATA_TYPE);
			ZclDataTypeUI8.zclSerialize(zclResponseFrame, v);
			break;
		}
		default:
			return false;
		}
		return true;
	}

	protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType) throws Exception {
		switch (attrId) {
		case 0:
			return ZCL.READ_ONLY;
		default:
			return ZCL.UNSUPPORTED_ATTRIBUTE;
		}
	}

}
