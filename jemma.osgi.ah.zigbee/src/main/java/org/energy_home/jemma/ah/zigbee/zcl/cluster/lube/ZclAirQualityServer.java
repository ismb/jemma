
package org.energy_home.jemma.ah.zigbee.zcl.cluster.lube;

import java.util.Collection;
import java.util.Map;

import org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityClient;
import org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.internal.zigbee.ZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.IZclFrame;
import org.energy_home.jemma.ah.zigbee.ZCL;
import org.energy_home.jemma.ah.zigbee.ZigBeeDevice;
import org.energy_home.jemma.ah.zigbee.ZigBeeDeviceListener;
import org.energy_home.jemma.ah.zigbee.zcl.IZclAttributeDescriptor;
import org.energy_home.jemma.ah.zigbee.zcl.ZclValidationException;
import org.energy_home.jemma.ah.zigbee.zcl.lib.ZclServiceCluster;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;

public class ZclAirQualityServer
    extends ZclServiceCluster
    implements AirQualityServer, ZigBeeDeviceListener
{

    public final static short CLUSTER_ID = 3071;
    static Map attributesMapByName = null;
    static Map attributesMapById = null;
    static ZclAttributeDescriptor[] attributeDescriptors = null;

    static {
        attributeDescriptors = new ZclAttributeDescriptor[ 1 ] ;
        attributeDescriptors[ 0 ] = new ZclAttributeDescriptor(0, ZclAirQualityServer.ATTR_AirQualityIndex_NAME, new ZclDataTypeUI8(), null, true, 1);
        attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
        attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
    }

    public ZclAirQualityServer()
        throws ApplianceException
    {
        super();
    }

    public boolean notifyZclFrame(short clusterId, IZclFrame zclFrame)
        throws Exception
    {
        boolean handled;
        handled = super.notifyZclFrame(clusterId, zclFrame);
        if (handled) {
            return handled;
        }
        int commandId = zclFrame.getCommandId();
        if (zclFrame.isClientToServer()) {
            throw new ZclValidationException("invalid direction field");
        }
        IZclFrame responseZclFrame = null;
        ZigBeeDevice device = getZigBeeDevice();
        int statusCode = ZCL.SUCCESS;
        org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityClient c = ((org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityClient) getSinglePeerCluster((AirQualityClient.class.getName())));
        switch (commandId) {
            case  2 :
                responseZclFrame = parseExternalAirQualityIndexNotification(c, zclFrame);
                break;
            default:
                return false;
        }
        if (responseZclFrame == null) {
            if (!zclFrame.isDefaultResponseDisabled()) {
                responseZclFrame = getDefaultResponse(zclFrame, statusCode);
            }
        } else {
            device.post(ZclAirQualityServer.CLUSTER_ID, responseZclFrame);
        }
        return true;
    }

    protected int getClusterId() {
        return CLUSTER_ID;
    }

    protected IZclAttributeDescriptor getAttributeDescriptor(String name) {
        return ((IZclAttributeDescriptor) attributesMapByName.get(name));
    }

    protected IZclAttributeDescriptor getAttributeDescriptor(int attrId) {
        return ((IZclAttributeDescriptor) attributesMapById.get(attrId));
    }

    protected Collection getAttributeDescriptors() {
        return (attributesMapByName.values());
    }

    protected IZclFrame parseExternalAirQualityIndexNotification(org.energy_home.jemma.ah.cluster.zigbee.lube.AirQualityClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        short Data = ZclDataTypeUI8 .zclParse(zclFrame);
        if (o == null) {
            return null;
        }
        o.execExternalAirQualityIndexNotification(Data, endPoint.getDefaultRequestContext());
        return null;
    }

    public short getAirQualityIndex(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Short objectResult = null;
            objectResult = ((Short) getValidCachedAttributeObject(0, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.shortValue();
            }
        }
        IZclFrame zclFrame = readAttribute(0, context);
        short v = ZclDataTypeUI8 .zclParse(zclFrame);
        setCachedAttributeObject(0, new Short(v));
        return v;
    }

}
