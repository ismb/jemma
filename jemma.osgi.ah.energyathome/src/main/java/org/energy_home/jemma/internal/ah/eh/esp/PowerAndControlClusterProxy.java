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
package org.energy_home.jemma.internal.ah.eh.esp;

import java.util.Arrays;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.EnergyPhasesScheduleResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetOverallSchedulePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceExtendedResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.GetPowerProfilePriceResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo;
import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo.EnergyPhaseScheduleTime;
import org.energy_home.jemma.ah.ebrain.IPowerAndControlListener;
import org.energy_home.jemma.ah.ebrain.IPowerAndControlProxy;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileState;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileTimeConstraints;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.NotFoundException;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.m2m.ah.ApplianceLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//FIXME by Riccardo lots of String.format in the whole bundle just for log messages: any reason for this ? If not we should clean up
public class PowerAndControlClusterProxy extends ServiceClusterProxy implements PowerProfileClient, IPowerAndControlProxy {
	
	public class ApplianceControlProxy extends ServiceClusterProxy implements ApplianceControlClient {		
		ApplianceControlProxy() throws ApplianceException {}
		
		public void execSignalStateNotification(short ApplianceStatus, short RemoteEnableFlags, int ApplianceStatus2, IEndPointRequestContext context) throws ApplianceException,
				ServiceClusterException {
			String applianceId = getApplianceId(context);
			LOG.debug(String.format("ApplianceControlClient[%s].execSignalStateNotification(ApplianceStatus=%s, RemoteEnableFlags=%s, ApplianceStatus2=%s)", applianceId, ApplianceStatus, RemoteEnableFlags, ApplianceStatus2));
			listener.notifyApplianceState(applianceId, ApplianceStatus, RemoteEnableFlags, ApplianceStatus2);
		}
	}
	
	public class ApplianceStatisticsProxy extends ServiceClusterProxy implements ApplianceStatisticsClient {		
		
		ApplianceStatisticsProxy() throws ApplianceException {}
		
		public void execLogNotification(long timestamp, long logID, long logLength, byte[] logPayload, IEndPointRequestContext context) throws ApplianceException,
				ServiceClusterException {
			String applianceId = getApplianceId(context);
			ApplianceLog applianceLog = new ApplianceLog();
			applianceLog.setLogId(logID);
			applianceLog.setLogPayload(logPayload);
			LOG.debug(String.format("ApplianceStatisticsClient[%s].execLogNotification(Timestamp=%s, LogID=%s, LogLenght=%s, LogPayload=%s)", 					applianceId, timestamp, logID, logLength, Utils.asHex(logPayload)));	
			
			listener.notifyApplianceStatistics(applianceId, convertTimestampFromZigBee(timestamp), applianceLog);	
		}

		public void execStatisticsAvailable(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
			// TODO To be implemented
			String applianceId = getApplianceId(context);
			LOG.debug(String.format("ApplianceStatisticsClient[%s].execStatisticsAvailable()", applianceId));		
		}

	}
	
	private static final Logger LOG = LoggerFactory.getLogger( PowerAndControlClusterProxy.class );

	private static final long ZIGBEE_UTC_DELTA_SECONDS = 946684800;
	
	public static final int ISO4217_CURRENCY_CODE = 978; // Euro currency
	public static final short TRAILING_DIGIT_CENTS = 2; // trailing digits: cents of euros
	public static final short TRAILING_DIGIT_TENTHS = 3; // trailing digits: thousandths of euros
	
	// The trailing digit size is actually a choice of the application not the appliance, so it's under our control.
	public static short priceTrailingDigits = TRAILING_DIGIT_TENTHS;
	
	// zigbee rules to convert fixed decimals into floating decimals
	public static float interpretFormatting(short formatting) {
		int decimals = formatting & 0x7;
		if (decimals > 0) return (float)(1 / Math.pow(10, decimals));
		return 1;
	}
	
	// zigbee rules to convert floating decimal cost into a fixed decimal. 
	public static long convertPriceFromCost(float cost) {
		return Math.round(cost * Math.pow(10, priceTrailingDigits));
	}

	public static long convertTimestampFromZigBee(long timestamp) {
		return (ZIGBEE_UTC_DELTA_SECONDS + timestamp) * 1000;
	}
	
	protected DeviceProxyList proxy;
	protected IPowerAndControlListener listener;
	protected ApplianceControlProxy appliaceControlProxy;
	protected ApplianceStatisticsProxy applianceStatisticsProxy;
	
	
	public PowerAndControlClusterProxy(DeviceProxyList proxy, IPowerAndControlListener listener) throws ApplianceException {
		this.proxy = proxy;
		this.listener = listener;
		appliaceControlProxy = new ApplianceControlProxy();
		applianceStatisticsProxy = new ApplianceStatisticsProxy();
	}
	
	PowerProfileClient asPowerProfileClient() {return this;}
	ApplianceControlClient asApplianceControlClient() {return appliaceControlProxy;}
	ApplianceStatisticsClient asApplianceStatisticsClient() {return applianceStatisticsProxy;}
	

	
	protected PowerProfileServer getRemotePowerProfileCluster(DeviceProxy deviceProxy) {
		// TODO: needs to be modified t manage multi end point devices
		return (PowerProfileServer) getServiceCluster(deviceProxy, PowerProfileServer.class.getName());
	}
	
	protected ApplianceControlServer getRemoteApplianceControlCluster(DeviceProxy deviceProxy) {
		// TODO: needs to be modified t manage multi end point devices
		return (ApplianceControlServer) getServiceCluster(deviceProxy, ApplianceControlServer.class.getName());
	}
	
	protected ApplianceStatisticsServer getRemoteApplianceStatisticCluster(DeviceProxy deviceProxy) {
		// TODO: needs to be modified t manage multi end point devices
		return (ApplianceStatisticsServer) getServiceCluster(deviceProxy, ApplianceStatisticsServer.class.getName());
	}
	
	public int getTariffTrailingDigits() {
		return priceTrailingDigits;
	}
	
	public void setTariffTrailingDigits(short trailing) {
		priceTrailingDigits = trailing;
	}

	/***********************************************************************************************
	 * Interface PowerProfileClient
	***********************************************************************************************/
	
	public GetPowerProfilePriceResponse execGetPowerProfilePrice(short powerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execGetPowerProfilePrice(PowerProfileID=%s)", applianceId, powerProfileID));				
		float cost = listener.calculatePowerProfilePrice(applianceId, powerProfileID, 0); //zero delay
		GetPowerProfilePriceResponse response =  convertGetPowerProfilePriceResponse(powerProfileID, cost);
		LOG.debug(String.format("PowerProfileClient[%s].execGetPowerProfilePrice() returned %s", applianceId, response));
		return response;
	}
	
	public GetPowerProfilePriceExtendedResponse execGetPowerProfilePriceExtended(short options, short powerProfileID, int powerProfileStartTime,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execGetPowerProfilePriceExtended(Options=%s, PowerProfileID=%s, PowerProfileStartTime=%s)", applianceId, options, powerProfileID, powerProfileStartTime));		
		float cost = listener.calculatePowerProfilePrice(applianceId, powerProfileID, powerProfileStartTime);
		GetPowerProfilePriceExtendedResponse response = convertGetPowerProfilePriceExtendedResponse(powerProfileID, cost);
		LOG.debug(String.format("PowerProfileClient[%s].execGetPowerProfilePriceExtended() returned %s", applianceId, response));
		return response;
	}

	public void execPowerProfileNotification(short totalProfileNum, short powerProfileID,
			PowerProfileTransferredPhase[] powerProfileTransferredPhases, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execPowerProfileNotification(TotalProfileNum=%s, PowerProfileID=%s, PowerProfileTransferredPhases=%s)",applianceId, totalProfileNum, powerProfileID, Arrays.toString(powerProfileTransferredPhases)));
		PowerProfileInfo ppi = convertPowerProfileInfo(totalProfileNum, powerProfileID, powerProfileTransferredPhases);
		listener.notifyPowerProfile(applianceId, ppi);
	}

	public EnergyPhasesScheduleResponse execEnergyPhasesScheduleRequest(short powerProfileID, IEndPointRequestContext context)
			throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execEnergyPhasesScheduleRequest(powerProfileID=%s)", applianceId, powerProfileID));
		EnergyPhaseScheduleTime[] epst = listener.calculateEnergyPhasesSchedule(applianceId, powerProfileID);
		EnergyPhasesScheduleResponse response = convertEnergyPhasesScheduleResponse(powerProfileID, epst);
		LOG.debug(String.format("PowerProfileClient[%s].execEnergyPhasesScheduleRequest() returned EnergyPhaseScheduleTimes=%s", applianceId, response));
		return response;
	}
	
	public void execEnergyPhasesScheduleStateNotification(short powerProfileID, ScheduledPhase[] scheduledPhases,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execEnergyPhasesScheduleStateNotification(powerProfileID=%s, scheduledPhases=%s)", 				applianceId, powerProfileID, Arrays.toString(scheduledPhases)));		
		EnergyPhaseScheduleTime[] epst = convertEnergyPhaseScheduleTime(scheduledPhases);
		listener.notifyEnergyPhasesScheduleTime(applianceId, powerProfileID, epst);
	}

	public GetOverallSchedulePriceResponse execGetOverallSchedulePrice(IEndPointRequestContext context) throws ApplianceException,
			ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execGetOverallSchedulePrice()", applianceId));
		// TODO To be implemented
		throw new NotFoundException();
	}

	public void execPowerProfileScheduleConstraintsNotification(short powerProfileID, int startAfter, int stopBefore,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execPowerProfileScheduleConstraintsNotification: PowerProfileID=%s, StartAfter=%s, StopBefore=%s",		applianceId, powerProfileID, startAfter, stopBefore));
		PowerProfileTimeConstraints pptc = convertPowerProfileTimeConstraints(powerProfileID, startAfter, stopBefore);
		listener.notifyPowerProfileScheduleConstraints(applianceId, pptc);
	}

	public void execPowerProfilesStateNotification(PowerProfile[] powerProfiles,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		// TODO To be implemented (not yet tested with a device)
		String applianceId = getApplianceId(context);
		LOG.debug(String.format("PowerProfileClient[%s].execPowerProfilesStateNotification(PowerProfiles=%s)", applianceId, Arrays.toString(powerProfiles)));
		PowerProfileState[] pps = convertPowerProfileState(powerProfiles);
		listener.notifyAllPowerProfilesState(applianceId, pps);
	}
	
	
	
	/***********************************************************************************************
	 * Interface IPowerProfileProxy
	***********************************************************************************************/
	

	
	public short getTotalProfileNum(String applianceId) {
		short num = -1;
		try {
			LOG.debug(String.format("PowerProfileServer[%s].getTotalProfileNum()", applianceId));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			PowerProfileServer pps = getRemotePowerProfileCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, false);
			num = pps.getTotalProfileNum(context);
			LOG.debug(String.format("PowerProfileServer[%s].getTotalProfileNum() returned %s", applianceId, num));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return num;
	}

	
	public boolean isMultiplePhasesSchedulingSupported(String applianceId) {
		// TODO Auto-generated method stub
		return true;
	}

	
	public float getEnergyFormatting(String applianceId) {
		float decimalFormatting = -1;
		try {
			LOG.debug(String.format("PowerProfileServer[%s].getEnergyFormatting()", applianceId));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			PowerProfileServer pps = getRemotePowerProfileCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, false);
			short zigbeeFormating = pps.getEnergyFormatting(context);
			LOG.debug(String.format("PowerProfileServer[%s].getEnergyFormatting() returned %s", applianceId, zigbeeFormating));
			decimalFormatting = interpretFormatting(zigbeeFormating);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return decimalFormatting;
	}

	
	public boolean isEnergyRemoteSupported(String applianceId) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public short getScheduleMode(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void setScheduleMode(String applianceId, short ScheduleMode) {
		// TODO Auto-generated method stub
		
	}

	
	public void notifyProposedEnergyPhasesSchedule(String applianceId, short powerProfileID, EnergyPhaseScheduleTime[] epst) {
		ScheduledPhase[] sp = convertScheduledPhases(epst);
		try {
			LOG.debug(String.format("PowerProfileServer[%s].execEnergyPhasesScheduleNotification(powerProfileID=%s, SchedulePhase=%s)", 					applianceId, powerProfileID, Arrays.toString(sp)));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			PowerProfileServer pps = getRemotePowerProfileCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, false);
			pps.execEnergyPhasesScheduleNotification(powerProfileID, sp, context);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	
	public PowerProfileInfo retrievePowerProfile(String applianceId, short powerProfileID) {
		PowerProfileResponse ppr = null;
		try {
			LOG.debug(String.format("PowerProfileServer[%s].execPowerProfileRequest(PowerProfileID=%s)", applianceId, powerProfileID));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			PowerProfileServer pps = getRemotePowerProfileCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, false);
			ppr = pps.execPowerProfileRequest(powerProfileID, context);
			LOG.debug(String.format("PowerProfileServer[%s].execPowerProfileRequest returned PowerProfileResponse=%s", applianceId, ppr));
			return convertPowerProfileInfo(ppr);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
//	public PowerProfileTimeConstraints retrieveProfileScheduleConstraints(String applianceId, short powerProfileID) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	
//	public EnergyPhaseScheduleTime[] retrieveEnergyPhasesScheduleTime(String applianceId, short powerProfileID) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	
	public PowerProfileState[] retrieveAllPowerProfilesState(String applianceId) {
		PowerProfileStateResponse ppsr = null;
		try {
			LOG.debug(String.format("PowerProfileServer[%s].execPowerProfileStateRequest()", applianceId));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			PowerProfileServer pps = getRemotePowerProfileCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, false);
			ppsr = pps.execPowerProfileStateRequest(context);
			LOG.debug(String.format("PowerProfileServer[%s].execPowerProfileStateRequest() returned PowerProfileStateResponse=%s", applianceId, ppsr));
			return convertPowerProfileState(ppsr);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/***********************************************************************************************
	 * Interface IApplianceControlProxy
	***********************************************************************************************/

	
	public int getStartTime(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getFinishTime(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public int getRemainingTime(String applianceId) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public void executeCommand(String applianceId, short command) {
		try {
			LOG.debug(String.format("ApplianceControlServer[%s].execCommandExecution(%s)", applianceId, command));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			ApplianceControlServer acs = getRemoteApplianceControlCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, true);
			acs.execCommandExecution(command, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void overloadPause(String applianceId) {
		try {
			LOG.debug(String.format("ApplianceControlServer[%s].execOverloadPause()", applianceId));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			ApplianceControlServer acs = getRemoteApplianceControlCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, true);
			acs.execOverloadPause(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void overloadResume(String applianceId) {
		try {
			LOG.debug(String.format("ApplianceControlServer[%s].execOverloadPauseResume()", applianceId));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			ApplianceControlServer acs = getRemoteApplianceControlCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, true);
			acs.execOverloadPauseResume(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void notifyOverloadWarning(String applianceId, short event) {
		try {
			LOG.debug(String.format("ApplianceControlServer[%s].execOverloadWarning(%s)", applianceId, event));
			DeviceProxy deviceProxy = proxy.getDeviceProxy(applianceId);
			ApplianceControlServer acs = getRemoteApplianceControlCluster(deviceProxy);
			IEndPointRequestContext context = getApplicationRequestContext(deviceProxy, true);
			acs.execOverloadWarning(event, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/***********************************************************************************************
	 * Converter Methods for to and for ZegBee framework and Energy Brain types
	***********************************************************************************************/

	public PowerProfileInfo convertPowerProfileInfo(PowerProfileResponse ppr) {
		return convertPowerProfileInfo(ppr.TotalProfileNum, ppr.PowerProfileID, ppr.PowerProfileTransferredPhases);
	}
	public PowerProfileInfo convertPowerProfileInfo(short totalProfileNum, short powerProfileID, PowerProfileTransferredPhase[] powerProfileTransferredPhases) {
		PowerProfileInfo ppi = new PowerProfileInfo(powerProfileID);
		ppi.setTotalProfileNum(totalProfileNum);
		EnergyPhaseInfo[] epi = new EnergyPhaseInfo[powerProfileTransferredPhases.length];
		for (int i = epi.length; --i >= 0;) {
			PowerProfileTransferredPhase pptp = powerProfileTransferredPhases[i];
			epi[i] = new EnergyPhaseInfo(pptp.EnergyPhaseID);
			epi[i].setExpectedDuration(pptp.ExpectedDuration);
			epi[i].setMacroPhaseID(pptp.MacroPhaseID);
			epi[i].setMaxActivationDelay(pptp.MaxActivationDelay);
			epi[i].setPeakPower(pptp.PeakPower);
			epi[i].setTotalEnergy(pptp.Energy);
		}
		ppi.setEnergyPhases(epi);
		return ppi;
	}
	
	public PowerProfileState[] convertPowerProfileState(PowerProfileStateResponse ppsr) {
		return convertPowerProfileState(ppsr.PowerProfiles);
	}
	public PowerProfileState[] convertPowerProfileState(PowerProfile[] pp) {
		PowerProfileState[] pps = new PowerProfileState[pp.length];
		for (int i = pp.length; --i >= 0;) {
			pps[i] = new PowerProfileState(pp[i].PowerProfileID);
			pps[i].setEnergyPhaseID(pp[i].EnergyPhaseID);
			pps[i].setPowerProfileRemoteControllable(pp[i].PowerProfileRemoteControl);
			pps[i].setState(pp[i].PowerProfileState);
		}
		return pps;
	}
	
	public EnergyPhaseScheduleTime[] convertEnergyPhaseScheduleTime(ScheduledPhase[] sp) {
		EnergyPhaseScheduleTime[] epst = new EnergyPhaseScheduleTime[sp.length];
		for (int i = sp.length; --i >= 0;) {
			epst[i] = new EnergyPhaseScheduleTime(sp[i].EnergyPhaseID, sp[i].ScheduledTime);
		}
		return epst;
	}
	
	public ScheduledPhase[] convertScheduledPhases(EnergyPhaseScheduleTime[] epst) {
		ScheduledPhase[] sp = new ScheduledPhase[epst.length];
		for (int i = sp.length; --i >= 0;) {
			sp[i] = new ScheduledPhase(epst[i].getEnergyPhaseID(), epst[i].getScheduledDelay());
		}
		return sp;
	}
	
	public GetPowerProfilePriceResponse convertGetPowerProfilePriceResponse(short profileId, float cost) {
		GetPowerProfilePriceResponse gpppr = new GetPowerProfilePriceResponse();
		gpppr.Currency = ISO4217_CURRENCY_CODE;
		gpppr.PowerProfileID = profileId;
		gpppr.PriceTrailingDigit = priceTrailingDigits;
		gpppr.Price = convertPriceFromCost(cost);
		return gpppr;
	}
	
	public GetPowerProfilePriceExtendedResponse convertGetPowerProfilePriceExtendedResponse(short profileId, float cost) {
		GetPowerProfilePriceExtendedResponse gpppr = new GetPowerProfilePriceExtendedResponse();
		gpppr.Currency = ISO4217_CURRENCY_CODE;
		gpppr.PowerProfileID = profileId;
		gpppr.PriceTrailingDigit = priceTrailingDigits;
		gpppr.Price = convertPriceFromCost(cost);
		return gpppr;
	}
	
	public EnergyPhasesScheduleResponse convertEnergyPhasesScheduleResponse(short profileID, EnergyPhaseScheduleTime[] epst) {
		EnergyPhasesScheduleResponse epsr = new EnergyPhasesScheduleResponse();
		epsr.PowerProfileID = profileID;
		ScheduledPhase[] sp = new ScheduledPhase[epst.length];
		for (int i = sp.length; --i >= 0;) {
			sp[i] = new ScheduledPhase(epst[i].getEnergyPhaseID(), epst[i].getScheduledDelay());
		}
		epsr.ScheduledPhases = sp;
		return epsr;
	}
	
	public PowerProfileTimeConstraints convertPowerProfileTimeConstraints(short powerProfileID, int startAfter, int stopBefore) {
		return new PowerProfileTimeConstraints(powerProfileID, startAfter, stopBefore);
	}

	public void notifyAttributeValue(String applianceId, String attributeName, IAttributeValue attributeValue) {
		// Attribute notification not used	
	}
}
