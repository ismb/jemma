package org.energy_home.jemma.ah.cluster.zigbee.closures;

import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;

public interface WindowCoveringServer {
	final static String ATTR_WindowCoveringType_NAME = "WindowCoveringType";
	final static String ATTR_PhysicalClosedLimitLift_NAME = "PhysicalClosedLimitLift";
	final static String ATTR_PhysicalClosedLimitTilt_NAME = "PhysicalClosedLimitTilt";
	final static String ATTR_CurrentPositionLift_NAME = "CurrentPositionLift";
	final static String ATTR_CurrentPositionTilt_NAME = "CurrentPositionTilt";
	final static String ATTR_NumberofActuationsLift_NAME = "NumberofActuationsLift";
	final static String ATTR_NumberofActuationsTilt_NAME = "NumberofActuationsTilt";
	final static String ATTR_ConfigStatus_NAME = "ConfigStatus";
	final static String ATTR_CurrentPositionLiftPercentage_NAME = "CurrentPositionLiftPercentage";
	final static String ATTR_CurrentPositionTiltPercentage_NAME = "CurrentPositionTiltPercentage";
	final static String ATTR_InstalledOpenLimit_NAME = "InstalledOpenLimit";
	final static String ATTR_InstalledClosedLimit_NAME = "InstalledClosedLimit";
	final static String ATTR_InstalledOpenLimitTilt_NAME = "InstalledOpenLimitTilt";
	final static String ATTR_InstalledClosedLimitTilt_NAME = "InstalledClosedLimitTilt";
	final static String ATTR_VelocityLift_NAME = "VelocityLift";
	final static String ATTR_AccelerationTimeLift_NAME = "AccelerationTimeLift";
	final static String ATTR_DecelerationTimeLift_NAME = "DecelerationTimeLift";
	final static String ATTR_Mode_NAME = "Mode";
	final static String ATTR_IntermediateSetpointsLift_NAME = "IntermediateSetpointsLift";
	final static String ATTR_IntermediateSetpointsTilt_NAME = "IntermediateSetpointsTilt";

	public short getWindowCoveringType(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPhysicalClosedLimitLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getPhysicalClosedLimitTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentPositionLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getCurrentPositionTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofActuationsLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getNumberofActuationsTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getConfigStatus(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCurrentPositionLiftPercentage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getCurrentPositionTiltPercentage(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getInstalledOpenLimit(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getInstalledClosedLimit(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getInstalledOpenLimitTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getInstalledClosedLimitTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getVelocityLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getAccelerationTimeLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public int getDecelerationTimeLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public short getMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getIntermediateSetpointsLift(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public String getIntermediateSetpointsTilt(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execUpOpen(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execDownClose(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execStop(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execGoToLiftValue(int LiftValue, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execGoToLiftPercentage(short PercentageLiftValue, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execGoToTiltValue(int TiltValue, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

	public void execGoToTiltPercentage(short PercentageTiltValue, IEndPointRequestContext context) throws ApplianceException, ServiceClusterException;

}
