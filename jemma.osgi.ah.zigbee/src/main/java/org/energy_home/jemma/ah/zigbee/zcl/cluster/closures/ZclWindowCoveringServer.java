
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

import java.util.Collection;
import java.util.Map;
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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer;

public class ZclWindowCoveringServer
    extends ZclServiceCluster
    implements ZigBeeDeviceListener, WindowCoveringServer
{

    public final static short CLUSTER_ID = 258;
    static Map attributesMapByName = null;
    static Map attributesMapById = null;
    static ZclAttributeDescriptor[] attributeDescriptors = null;

    static {
        attributeDescriptors = new ZclAttributeDescriptor[ 20 ] ;
        attributeDescriptors[ 0 ] = new ZclAttributeDescriptor(0, ZclWindowCoveringServer.ATTR_WindowCoveringType_NAME, new ZclDataTypeEnum8(), null, true, 1);
        attributeDescriptors[ 1 ] = new ZclAttributeDescriptor(1, ZclWindowCoveringServer.ATTR_PhysicalClosedLimitLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 2 ] = new ZclAttributeDescriptor(2, ZclWindowCoveringServer.ATTR_PhysicalClosedLimitTilt_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 3 ] = new ZclAttributeDescriptor(3, ZclWindowCoveringServer.ATTR_CurrentPositionLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 4 ] = new ZclAttributeDescriptor(4, ZclWindowCoveringServer.ATTR_CurrentPositionTilt_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 5 ] = new ZclAttributeDescriptor(5, ZclWindowCoveringServer.ATTR_NumberofActuationsLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 6 ] = new ZclAttributeDescriptor(6, ZclWindowCoveringServer.ATTR_NumberofActuationsTilt_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 7 ] = new ZclAttributeDescriptor(7, ZclWindowCoveringServer.ATTR_ConfigStatus_NAME, new ZclDataTypeBitmap8(), null, true, 1);
        attributeDescriptors[ 8 ] = new ZclAttributeDescriptor(8, ZclWindowCoveringServer.ATTR_CurrentPositionLiftPercentage_NAME, new ZclDataTypeUI8(), null, true, 1);
        attributeDescriptors[ 9 ] = new ZclAttributeDescriptor(9, ZclWindowCoveringServer.ATTR_CurrentPositionTiltPercentage_NAME, new ZclDataTypeUI8(), null, true, 1);
        attributeDescriptors[ 10 ] = new ZclAttributeDescriptor(16, ZclWindowCoveringServer.ATTR_InstalledOpenLimit_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 11 ] = new ZclAttributeDescriptor(17, ZclWindowCoveringServer.ATTR_InstalledClosedLimit_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 12 ] = new ZclAttributeDescriptor(18, ZclWindowCoveringServer.ATTR_InstalledOpenLimitTilt_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 13 ] = new ZclAttributeDescriptor(19, ZclWindowCoveringServer.ATTR_InstalledClosedLimitTilt_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 14 ] = new ZclAttributeDescriptor(20, ZclWindowCoveringServer.ATTR_VelocityLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 15 ] = new ZclAttributeDescriptor(21, ZclWindowCoveringServer.ATTR_AccelerationTimeLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 16 ] = new ZclAttributeDescriptor(22, ZclWindowCoveringServer.ATTR_DecelerationTimeLift_NAME, new ZclDataTypeUI16(), null, true, 1);
        attributeDescriptors[ 17 ] = new ZclAttributeDescriptor(23, ZclWindowCoveringServer.ATTR_Mode_NAME, new ZclDataTypeBitmap8(), null, true, 1);
        attributeDescriptors[ 18 ] = new ZclAttributeDescriptor(24, ZclWindowCoveringServer.ATTR_IntermediateSetpointsLift_NAME, new ZclDataTypeString(16), null, true, 1);
        attributeDescriptors[ 19 ] = new ZclAttributeDescriptor(25, ZclWindowCoveringServer.ATTR_IntermediateSetpointsTilt_NAME, new ZclDataTypeString(16), null, true, 1);
        attributesMapByName = fillAttributesMapsByName(attributeDescriptors, attributesMapByName);
        attributesMapById = fillAttributesMapsById(attributeDescriptors, attributesMapById);
    }

    public ZclWindowCoveringServer()
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
        org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient c = ((org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient) getSinglePeerCluster((WindowCoveringClient.class.getName())));
        switch (commandId) {
            case  0 :
                responseZclFrame = parseUpOpen(c, zclFrame);
                break;
            case  1 :
                responseZclFrame = parseDownClose(c, zclFrame);
                break;
            case  2 :
                responseZclFrame = parseStop(c, zclFrame);
                break;
            case  4 :
                responseZclFrame = parseGoToLiftValue(c, zclFrame);
                break;
            case  5 :
                responseZclFrame = parseGoToLiftPercentage(c, zclFrame);
                break;
            case  7 :
                responseZclFrame = parseGoToTiltValue(c, zclFrame);
                break;
            case  8 :
                responseZclFrame = parseGoToTiltPercentage(c, zclFrame);
                break;
            default:
                return false;
        }
        if (responseZclFrame == null) {
            if (!zclFrame.isDefaultResponseDisabled()) {
                responseZclFrame = getDefaultResponse(zclFrame, statusCode);
            }
        } else {
            device.post(ZclWindowCoveringServer.CLUSTER_ID, responseZclFrame);
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

    protected IZclFrame parseUpOpen(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        if (o == null) {
            return null;
        }
        o.execUpOpen(endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseDownClose(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        if (o == null) {
            return null;
        }
        o.execDownClose(endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseStop(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        if (o == null) {
            return null;
        }
        o.execStop(endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseGoToLiftValue(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        int LiftValue = ZclDataTypeUI16 .zclParse(zclFrame);
        if (o == null) {
            return null;
        }
        o.execGoToLiftValue(LiftValue, endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseGoToLiftPercentage(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        short PercentageLiftValue = ZclDataTypeUI8 .zclParse(zclFrame);
        if (o == null) {
            return null;
        }
        o.execGoToLiftPercentage(PercentageLiftValue, endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseGoToTiltValue(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        int TiltValue = ZclDataTypeUI16 .zclParse(zclFrame);
        if (o == null) {
            return null;
        }
        o.execGoToTiltValue(TiltValue, endPoint.getDefaultRequestContext());
        return null;
    }

    protected IZclFrame parseGoToTiltPercentage(org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient o, IZclFrame zclFrame)
        throws ApplianceException, ServiceClusterException
    {
        short PercentageTiltValue = ZclDataTypeUI8 .zclParse(zclFrame);
        if (o == null) {
            return null;
        }
        o.execGoToTiltPercentage(PercentageTiltValue, endPoint.getDefaultRequestContext());
        return null;
    }

    public short getWindowCoveringType(IEndPointRequestContext context)
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
        short v = ZclDataTypeEnum8 .zclParse(zclFrame);
        setCachedAttributeObject(0, new Short(v));
        return v;
    }

    public int getPhysicalClosedLimitLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(1, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(1, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(1, new Integer(v));
        return v;
    }

    public int getPhysicalClosedLimitTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(2, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(2, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(2, new Integer(v));
        return v;
    }

    public int getCurrentPositionLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(3, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(3, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(3, new Integer(v));
        return v;
    }

    public int getCurrentPositionTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(4, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(4, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(4, new Integer(v));
        return v;
    }

    public int getNumberofActuationsLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(5, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(5, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(5, new Integer(v));
        return v;
    }

    public int getNumberofActuationsTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(6, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(6, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(6, new Integer(v));
        return v;
    }

    public short getConfigStatus(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Short objectResult = null;
            objectResult = ((Short) getValidCachedAttributeObject(7, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.shortValue();
            }
        }
        IZclFrame zclFrame = readAttribute(7, context);
        short v = ZclDataTypeBitmap8 .zclParse(zclFrame);
        setCachedAttributeObject(7, new Short(v));
        return v;
    }

    public short getCurrentPositionLiftPercentage(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Short objectResult = null;
            objectResult = ((Short) getValidCachedAttributeObject(8, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.shortValue();
            }
        }
        IZclFrame zclFrame = readAttribute(8, context);
        short v = ZclDataTypeUI8 .zclParse(zclFrame);
        setCachedAttributeObject(8, new Short(v));
        return v;
    }

    public short getCurrentPositionTiltPercentage(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Short objectResult = null;
            objectResult = ((Short) getValidCachedAttributeObject(9, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.shortValue();
            }
        }
        IZclFrame zclFrame = readAttribute(9, context);
        short v = ZclDataTypeUI8 .zclParse(zclFrame);
        setCachedAttributeObject(9, new Short(v));
        return v;
    }

    public int getInstalledOpenLimit(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(16, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(16, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(16, new Integer(v));
        return v;
    }

    public int getInstalledClosedLimit(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(17, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(17, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(17, new Integer(v));
        return v;
    }

    public int getInstalledOpenLimitTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(18, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(18, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(18, new Integer(v));
        return v;
    }

    public int getInstalledClosedLimitTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(19, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(19, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(19, new Integer(v));
        return v;
    }

    public int getVelocityLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(20, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(20, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(20, new Integer(v));
        return v;
    }

    public int getAccelerationTimeLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(21, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(21, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(21, new Integer(v));
        return v;
    }

    public int getDecelerationTimeLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Integer objectResult = null;
            objectResult = ((Integer) getValidCachedAttributeObject(22, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.intValue();
            }
        }
        IZclFrame zclFrame = readAttribute(22, context);
        int v = ZclDataTypeUI16 .zclParse(zclFrame);
        setCachedAttributeObject(22, new Integer(v));
        return v;
    }

    public short getMode(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            Short objectResult = null;
            objectResult = ((Short) getValidCachedAttributeObject(23, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult.shortValue();
            }
        }
        IZclFrame zclFrame = readAttribute(23, context);
        short v = ZclDataTypeBitmap8 .zclParse(zclFrame);
        setCachedAttributeObject(23, new Short(v));
        return v;
    }

    public String getIntermediateSetpointsLift(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(24, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(24, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(24, v);
        return v;
    }

    public String getIntermediateSetpointsTilt(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        if (context!= null) {
            String objectResult = null;
            objectResult = ((String) getValidCachedAttributeObject(25, context.getMaxAgeForAttributeValues()));
            if (objectResult!= null) {
                return objectResult;
            }
        }
        IZclFrame zclFrame = readAttribute(25, context);
        String v = ZclDataTypeString.zclParse(zclFrame);
        setCachedAttributeObject(25, v);
        return v;
    }

}
