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

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceStatisticsServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.EnergyPhasesScheduleStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogQueueResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.LogResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfile;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileTransferredPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.cluster.zigbee.eh.SignalStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.WriteAttributeRecord;
import org.energy_home.jemma.ah.ebrain.EnergyPhaseInfo.EnergyPhaseScheduleTime;
import org.energy_home.jemma.ah.ebrain.IPowerAndControlListener;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileState;
import org.energy_home.jemma.ah.ebrain.PowerProfileInfo.PowerProfileTimeConstraints;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.UnsupportedClusterOperationException;
import org.energy_home.jemma.m2m.ah.ApplianceLog;


public class PowerAndControlClusterProxySimpleTest extends PowerAndControlClusterProxy implements PowerProfileClient {
	private static final String APPLIANCE_PID = "ah.app.1";
	
	private static IPowerAndControlListener listener = new IPowerAndControlListener() {
		
		public void notifyApplianceState(String applianceId, short applianceStatus, short remoteEnableFlags, int applianceStatus2) {
			// TODO Auto-generated method stub
			
		}
		
		public void notifyPowerProfileScheduleConstraints(String applianceId, PowerProfileTimeConstraints profileConstraints) {
			// TODO Auto-generated method stub
			
		}
		
		public void notifyPowerProfile(String applianceId, PowerProfileInfo powerProfile) {
			// TODO Auto-generated method stub
			
		}
		
		public void notifyEnergyPhasesScheduleTime(String applianceId, short powerProfileID, EnergyPhaseScheduleTime[] scheduledPhases) {
			// TODO Auto-generated method stub
			
		}
		
		public void notifyAllPowerProfilesState(String applianceId, PowerProfileState[] powerProfilesState) {
			// TODO Auto-generated method stub
			
		}
		
		public float calculatePowerProfilePrice(String applianceId, short powerProfileID, int delay) {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public float calculateOverallSchedulePrice() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public EnergyPhaseScheduleTime[] calculateEnergyPhasesSchedule(String applianceId, short powerProfileID) {
			EnergyPhaseScheduleTime epst1 = new EnergyPhaseScheduleTime((short) 1, 10);
			EnergyPhaseScheduleTime epst2 = new EnergyPhaseScheduleTime((short) 2, 20);
			return new EnergyPhaseScheduleTime[] {epst1, epst2};
			
		}
		
		public void notifyApplianceStatistics(String applianceId, long timestamp, ApplianceLog applianceLog) {
			// TODO Auto-generated method stub
			
		}
	};

	protected IEndPointRequestContext getRequestContext (boolean isConfirmed) {
		return null;
	}
	
	protected String getApplianceId(IEndPointRequestContext context) {
		return APPLIANCE_PID;
	}
	
	protected PowerProfileServer getRemotePowerProfileCluster(String applianceId) {
		return new PowerProfileServer() {
			
			private PowerProfileTransferredPhase powerProfileTransferredPhase1 = new PowerProfileTransferredPhase((short) 1, (short) 2, 30, 40, 50, 60);
			private PowerProfileTransferredPhase powerProfileTransferredPhase2 = new PowerProfileTransferredPhase((short) 7, (short) 8, 90, 100, 110, 120);
			private PowerProfileTransferredPhase[] powerProfileTransferredPhases = new PowerProfileTransferredPhase[] {powerProfileTransferredPhase1, powerProfileTransferredPhase2};
			private PowerProfileResponse powerProfileResponse = new PowerProfileResponse((short) 1, (short) 2, powerProfileTransferredPhases);
			private PowerProfile powerProfile1 = new PowerProfile((short) 1, (short) 2, true, (short) 3);
			private PowerProfile powerProfile2 = new PowerProfile((short) 4, (short) 5, true, (short) 6);
			private PowerProfile[] powerProfiles = new PowerProfile[] {powerProfile1, powerProfile2};
			private PowerProfileStateResponse powerProfileStateResponse = new PowerProfileStateResponse(powerProfiles);
			
			
			public void setScheduleMode(short ScheduleMode, IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
			
			public short getTotalProfileNum(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 1;
			}
			
			public short getScheduleMode(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public boolean getMultipleScheduling(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}
			
			public boolean getEnergyRemote(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return true;
			}
			
			public short getEnergyFormatting(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public PowerProfileStateResponse execPowerProfileStateRequest(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				return powerProfileStateResponse;
			}
			
			public PowerProfileScheduleConstraintsResponse execPowerProfileScheduleConstraintsRequest(short PowerProfileID,
					IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public PowerProfileResponse execPowerProfileRequest(short PowerProfileID, IEndPointRequestContext context)
					throws ApplianceException, ServiceClusterException {
				return powerProfileResponse;
			}
			
			public EnergyPhasesScheduleStateResponse execEnergyPhasesScheduleStateRequest(short PowerProfileID,
					IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void execEnergyPhasesScheduleNotification(short PowerProfileID, ScheduledPhase[] ScheduledPhases,
					IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	protected ApplianceControlServer getRemoteApplianceControlCluster(String applianceId) {
		return new ApplianceControlServer() {
			
			public int getStartTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public int getRemainingTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public int getFinishTime(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public void execWriteFunctions(WriteAttributeRecord[] WriteAttributeRecords, IEndPointRequestContext context)
					throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
			
			public SignalStateResponse execSignalState(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public void execOverloadWarning(short WarningEvent, IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
			
			public void execOverloadPauseResume(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
			
			public void execOverloadPause(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				
			}
			
			public void execCommandExecution(short CommandId, IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				
			}

			public short getCycleTarget0(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}

			public short getCycleTarget1(IEndPointRequestContext context)
					throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getTemperatureTarget0(IEndPointRequestContext context)
					throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getTemperatureTarget1(IEndPointRequestContext context)
					throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}

			public short getSpin(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}

			public boolean getEcoMode(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean getNormalMode(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean getHolidayMode(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean getIceParty(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean getSuperCoolMode(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}

			public boolean getSuperFreezeMode(IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return false;
			}		
		};
	}
	
	protected ApplianceStatisticsServer getRemoteApplianceStatisticCluster(String applianceId) {
		return new ApplianceStatisticsServer() {
			
			public short getLogQueueMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public long getLogMaxSize(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			public LogResponse execLogRequest(long LogID, IEndPointRequestContext context) throws ApplianceException,
					ServiceClusterException {
				// TODO Auto-generated method stub
				return null;
			}
			
			public LogQueueResponse execLogQueueRequest(IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
				// TODO Auto-generated method stub
				return null;
			}
		};
	}
	
	
	public PowerAndControlClusterProxySimpleTest() throws ApplianceException {
		super(null, listener);
	}

	public static void main(String[] args) {
		try {
			PowerAndControlClusterProxySimpleTest test = new PowerAndControlClusterProxySimpleTest();
			test.appliaceControlProxy.execSignalStateNotification((short)1, (short)2, 3, null);
			test.applianceStatisticsProxy.execLogNotification(System.currentTimeMillis(), 1, 5, new byte[] {0x00,  0x01, 0x02, 0x03, 0x04}, null);
			test.applianceStatisticsProxy.execStatisticsAvailable(null);
			test.execGetPowerProfilePrice((short)1, null);
			test.execGetPowerProfilePriceExtended((short) 0, (short) 1, 2, null);
			PowerProfileTransferredPhase pptp1 = new PowerProfileTransferredPhase((short) 1, (short) 2, 3, 4, 5, 6); 
			PowerProfileTransferredPhase pptp2 = new PowerProfileTransferredPhase((short) 7, (short) 8, 9, 10, 11, 12); 
			test.execPowerProfileNotification((short) 1, (short) 1, new PowerProfileTransferredPhase[] {pptp1, pptp2}, null);
			test.execEnergyPhasesScheduleRequest((short) 1, null);
			ScheduledPhase sp1 = new ScheduledPhase((short) 1, 10);
			ScheduledPhase sp2 = new ScheduledPhase((short) 2, 20);
			test.execEnergyPhasesScheduleStateNotification((short) 1, new ScheduledPhase[] {sp1, sp2}, null);
			try {
				test.execGetOverallSchedulePrice(null);
			} catch (UnsupportedClusterOperationException e) {
				System.out.println("Unsupported operation ok");
			} 
			test.execPowerProfileScheduleConstraintsNotification((short) 0, 10, 100, null);
			PowerProfile pp1 = new PowerProfile((short) 1, (short) 2, true, (short) 3);
			PowerProfile pp2 = new PowerProfile((short) 4, (short) 5, false, (short) 6);
			test.execPowerProfilesStateNotification(new PowerProfile[] {pp1, pp2}, null);
			test.getTotalProfileNum(APPLIANCE_PID);
			test.getEnergyFormatting(APPLIANCE_PID);
			EnergyPhaseScheduleTime epst1 = new EnergyPhaseScheduleTime((short)1, 10);
			EnergyPhaseScheduleTime epst2 = new EnergyPhaseScheduleTime((short)2, 20);
			test.notifyProposedEnergyPhasesSchedule(APPLIANCE_PID, (short) 1, new EnergyPhaseScheduleTime[] {epst1, epst2});
			test.retrievePowerProfile(APPLIANCE_PID, (short)1);
//			test.retrieveProfileScheduleConstraints(APPLIANCE_PID, (short)1);
//			test.retrieveEnergyPhasesScheduleTime(APPLIANCE_PID, (short) 1);
			test.retrieveAllPowerProfilesState(APPLIANCE_PID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
	}

}
