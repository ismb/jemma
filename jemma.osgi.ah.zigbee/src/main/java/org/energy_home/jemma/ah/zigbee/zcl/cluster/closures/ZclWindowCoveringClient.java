
package org.energy_home.jemma.ah.zigbee.zcl.cluster.closures;

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
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeBitmap8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeEnum8;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeString;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI16;
import org.energy_home.jemma.ah.zigbee.zcl.lib.types.ZclDataTypeUI8;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringClient;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer;

public class ZclWindowCoveringClient
    extends ZclServiceCluster
    implements ZigBeeDeviceListener, WindowCoveringClient
{

    public final static short CLUSTER_ID = 258;

    public ZclWindowCoveringClient()
        throws ApplianceException
    {
        super();
    }

    protected int getClusterId() {
        return CLUSTER_ID;
    }

    protected IZclAttributeDescriptor[] getPeerClusterAttributeDescriptors() {
        return ZclWindowCoveringServer.attributeDescriptors;
    }

    public void execUpOpen(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        ZclFrame zclFrame = new ZclFrame(1);
        zclFrame.setCommandId(0);
        issueExec(zclFrame, 11, context);
    }

    public void execDownClose(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        ZclFrame zclFrame = new ZclFrame(1);
        zclFrame.setCommandId(1);
        issueExec(zclFrame, 11, context);
    }

    public void execStop(IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        ZclFrame zclFrame = new ZclFrame(1);
        zclFrame.setCommandId(2);
        issueExec(zclFrame, 11, context);
    }

    public void execGoToLiftValue(int LiftValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(LiftValue);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(4);
        ZclDataTypeUI16 .zclSerialize(zclFrame, LiftValue);
        issueExec(zclFrame, 11, context);
    }

    public void execGoToLiftPercentage(short PercentageLiftValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(PercentageLiftValue);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(5);
        ZclDataTypeUI8 .zclSerialize(zclFrame, PercentageLiftValue);
        issueExec(zclFrame, 11, context);
    }

    public void execGoToTiltValue(int TiltValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeUI16 .zclSize(TiltValue);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(7);
        ZclDataTypeUI16 .zclSerialize(zclFrame, TiltValue);
        issueExec(zclFrame, 11, context);
    }

    public void execGoToTiltPercentage(short PercentageTiltValue, IEndPointRequestContext context)
        throws ApplianceException, ServiceClusterException
    {
        int size = 0;
        size += ZclDataTypeUI8 .zclSize(PercentageTiltValue);
        ZclFrame zclFrame = new ZclFrame(1, size);
        zclFrame.setCommandId(8);
        ZclDataTypeUI8 .zclSerialize(zclFrame, PercentageTiltValue);
        issueExec(zclFrame, 11, context);
    }

    protected int readAttributeResponseGetSize(int attrId)
        throws ServiceClusterException, ZclValidationException
    {
        switch (attrId) {
            case  0 :
                return ZclDataTypeEnum8 .zclSize((short)0);
            case  1 :
                return ZclDataTypeUI16 .zclSize(0);
            case  2 :
                return ZclDataTypeUI16 .zclSize(0);
            case  3 :
                return ZclDataTypeUI16 .zclSize(0);
            case  4 :
                return ZclDataTypeUI16 .zclSize(0);
            case  5 :
                return ZclDataTypeUI16 .zclSize(0);
            case  6 :
                return ZclDataTypeUI16 .zclSize(0);
            case  7 :
                return ZclDataTypeBitmap8 .zclSize((short)0);
            case  8 :
                return ZclDataTypeUI8 .zclSize((short)0);
            case  9 :
                return ZclDataTypeUI8 .zclSize((short)0);
            case  16 :
                return ZclDataTypeUI16 .zclSize(0);
            case  17 :
                return ZclDataTypeUI16 .zclSize(0);
            case  18 :
                return ZclDataTypeUI16 .zclSize(0);
            case  19 :
                return ZclDataTypeUI16 .zclSize(0);
            case  20 :
                return ZclDataTypeUI16 .zclSize(0);
            case  21 :
                return ZclDataTypeUI16 .zclSize(0);
            case  22 :
                return ZclDataTypeUI16 .zclSize(0);
            case  23 :
                return ZclDataTypeBitmap8 .zclSize((short)0);
                //case  24 :
                //return ZclDataTypeString.zclSize();
                //case  25 :
                //return ZclDataTypeString.zclSize((short)0);
            default:
                throw new UnsupportedClusterAttributeException();
        }
    }

    protected boolean fillAttributeRecord(IZclFrame zclResponseFrame, int attrId)
        throws ApplianceException, ServiceClusterException
    {
        WindowCoveringServer c = ((WindowCoveringServer) getSinglePeerCluster((WindowCoveringServer.class.getName())));
        switch (attrId) {
            case  0 :
            {
                short v;
                v = c.getWindowCoveringType(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeEnum8 .ZCL_DATA_TYPE);
                ZclDataTypeEnum8 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  1 :
            {
                int v;
                v = c.getPhysicalClosedLimitLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  2 :
            {
                int v;
                v = c.getPhysicalClosedLimitTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  3 :
            {
                int v;
                v = c.getCurrentPositionLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  4 :
            {
                int v;
                v = c.getCurrentPositionTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  5 :
            {
                int v;
                v = c.getNumberofActuationsLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  6 :
            {
                int v;
                v = c.getNumberofActuationsTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  7 :
            {
                short v;
                v = c.getConfigStatus(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeBitmap8 .ZCL_DATA_TYPE);
                ZclDataTypeBitmap8 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  8 :
            {
                short v;
                v = c.getCurrentPositionLiftPercentage(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI8 .ZCL_DATA_TYPE);
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  9 :
            {
                short v;
                v = c.getCurrentPositionTiltPercentage(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI8 .ZCL_DATA_TYPE);
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  16 :
            {
                int v;
                v = c.getInstalledOpenLimit(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  17 :
            {
                int v;
                v = c.getInstalledClosedLimit(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  18 :
            {
                int v;
                v = c.getInstalledOpenLimitTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  19 :
            {
                int v;
                v = c.getInstalledClosedLimitTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  20 :
            {
                int v;
                v = c.getVelocityLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  21 :
            {
                int v;
                v = c.getAccelerationTimeLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  22 :
            {
                int v;
                v = c.getDecelerationTimeLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeUI16 .ZCL_DATA_TYPE);
                ZclDataTypeUI16 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  23 :
            {
                short v;
                v = c.getMode(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeBitmap8 .ZCL_DATA_TYPE);
                ZclDataTypeBitmap8 .zclSerialize(zclResponseFrame, v);
                break;
            }
            case  24 :
            {
                String v;
                v = c.getIntermediateSetpointsLift(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeString.ZCL_DATA_TYPE);
                ZclDataTypeString.zclSerialize(zclResponseFrame, v);
                break;
            }
            case  25 :
            {
                String v;
                v = c.getIntermediateSetpointsTilt(endPoint.getDefaultRequestContext());
                ZclDataTypeUI8 .zclSerialize(zclResponseFrame, ZCL.SUCCESS);
                zclResponseFrame.appendUInt8(ZclDataTypeString.ZCL_DATA_TYPE);
                ZclDataTypeString.zclSerialize(zclResponseFrame, v);
                break;
            }
            default:
                return false;
        }
        return true;
    }

    protected short writeAttribute(IZclFrame zclFrame, int attrId, short dataType)
        throws Exception
    {
        switch (attrId) {
            case  0 :
            case  1 :
            case  2 :
            case  3 :
            case  4 :
            case  5 :
            case  6 :
            case  7 :
            case  8 :
            case  9 :
            case  16 :
            case  17 :
            case  18 :
            case  19 :
            case  20 :
            case  21 :
            case  22 :
            case  23 :
            case  24 :
            case  25 :
                return ZCL.READ_ONLY;
            default:
                return ZCL.UNSUPPORTED_ATTRIBUTE;
        }
    }

}