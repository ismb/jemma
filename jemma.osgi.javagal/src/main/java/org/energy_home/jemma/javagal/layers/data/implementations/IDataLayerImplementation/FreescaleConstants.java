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
package org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation;


public class FreescaleConstants {

	static final short ZTCGetModeRequest = (short) 0xA302;
	static final short ZTCGetModeConfirm = (short) 0xA402;

	static final short ZTCGetNumOfMsgsRequest = (short) 0xA324;
	static final short ZTCGetNumOfMsgsConfirm = (short) 0xA424;

	static final short ZTCCPUResetRequest = (short) 0xA308;

	static final short ZTCModeSelectRequest = (short) 0xA300;
	static final short ZTCModeSelectConfirm = (short) 0xA400;

	static final short ZTCWriteExtAddrRequest = (short) 0xA3DB;
	static final short ZTCWriteExtAddrConfirm = (short) 0xA4DB;

	static final short ZTCStopNwkExRequest = (short) 0xA3E8;
	static final short ZTCStopNwkExConfirm = (short) 0xA4E8;
	
	static final short ZTCStartNwkExRequest = (short) 0xA3E7;
	static final short ZTCStartNwkExConfirm = (short) 0xA4E7;

	static final short ZTCGetChannelRequest = (short) 0xA312;
	static final short ZTCGetChannelConfirm = (short) 0xA412;

	static final short ZDONetworkStateEvent = (short) 0xA0E6;

	static final short ZDPNwkAddrRequest = (short) 0xA200;

	static final short ZDPIeeeAddrRequest = (short) 0xA201;
	static final short ZDPIeeeAddrResponse = (short) 0xA081;

	static final short ZDPAPSMEProcessSecureFrameConfirm = (short) 0xA0CC;

	static final short ZDPMgmtLeaveRequest = (short) 0xA234;
	static final short ZDPMgmtLeaveResponse = (short) 0xA0B4;

	static final short ZDPMgmtLqiRequest = (short) 0xA231;
	static final short ZDPMgmtLqiResponse = (short) 0xA0B1;

	static final short ZDPNodeDescriptorRequest = (short) 0xA202;
	static final short ZDPNodeDescriptorResponse = (short) 0xA082;

	static final short ZDPPowerDescriptorRequest = (short) 0xA203;
	static final short ZDPPowerDescriptorResponse = (short) 0xA083;

	static final short ZDPSimpleDescriptorRequest = (short) 0xA204;
	static final short ZDPSimpleDescriptorResponse = (short) 0xA084;

	static final short ZDPActiveEpRequest = (short) 0xA205;
	static final short ZDPActiveEpResponse = (short) 0xA085;

	static final short ZDPMgmtBindRequest = (short) 0xA233;
	static final short ZDPMgmtBindResponse = (short) 0xA0A1;

	static final  short NLMENETWORKFORMATIONRequest = (short) 0x9635;
	
	static final short ZDPUnbindResponse = (short) 0xA0A2;
	
	
	static final short ZDPMgmt_BindResponse = (short) 0xA0B3;
	
	
	static final short APSMEBindRequest = (short) 0x9900;
	static final short APSMEUnbindRequest = (short) 0x9909;

	static final short APSMERemoveDeviceRequest = (short) 0x99F4;
	static final short APSMERemoveDeviceIndication = (short) 0x98D8;

	static final short APSMEGetRequest = (short) 0xA320;
	static final short APSMEGetConfirm = (short) 0xA420;
	
	static final short ZDPMgmt_Permit_JoinResponse = (short) 0xA0B6;
	
	
	
	
	static final short APSMESetRequest = (short) 0xA321;
	static final short APSMESetConfirm = (short) 0xA421;

	static final short ZDPBindRequest = (short) 0xA221;

	static final short ZDPUnbindRequest = (short) 0xA222;

	static final short ZDPNLMEPermitJoiningConfirm = (short) 0xA0F9;

	static final short ZTCReadExtAddrRequest = (short) 0xA3D2;
	static final short ZTCReadExtAddrConfirm = (short) 0xA4D2;

	
	static final short InterPANDataRequest = (short) 0xA500;
	static final short InterPANDataConfirm = (short) 0xA601;
	static final short InterPANDataIndication = (short) 0xA602;

	
	static final short APSDEDataRequest = (short) 0x9C00;
	static final short APSDEDataConfirm = (short) 0x9D00;
	static final short APSDEDataIndication = (short) 0x9D01;

	static final short NMLESETConfirm = (short) 0xA403;

	
	 
	 
	 
	 
	static final short BlackBoxWriteSAS = (short) 0x5001;
	static final short BlackBoxWriteSASConfirm = (short) 0x5004;

	static final short BlackBoxReadSAS = (short) 0x5002;

	static final short APSDeRegisterEndPointRequest = (short) 0xA30A;
	static final short APSDeRegisterEndPointConfirm = (short) 0xA40A;
	
	
	static final short APSRegisterEndPointRequest = (short) 0xA30B;
	static final short APSRegisterEndPointConfirm = (short) 0xA40B;
	
	static final short APSGetEndPointIdListRequest = (short) 0xA30E;
	static final short APSGetEndPointIdListConfirm = (short) 0xA40E; 
	
	

	static final short APSProcessSecureFrameReport = (short) 0x98CC;

	static final short MacPollNotifyIndication = (short) 0x8414;

	static final short MacBeaconNotifyIndication = (short) 0x8404;
	
	static final short NLMEJOINConfirm = (short) 0x9746;
	
	
	static final short NLMENETWORKDISCOVERYRequest = (short) 0x9634;
	
	
	static final short NLMEENERGYSCANconfirm = (short) 0x9755;

	static final short MacSetPIBAttributeConfirm = (short) 0x840D;
	static final short MacStartRequest = (short) 0x850A;
	static final short MacStartConfirm = (short) 0x840E;

	static final short NLMEENERGYSCANRequest = (short) 0x9654;
	
	static final short  MacScanRequest = (short) 0x8508;

	static final short MacScanConfirm = (short) 0x840B;
	
	
	///static final short MacScanConfirm = (short) 0x840B;
	
	
	
	static final short ZTCErrorevent = (short) 0xA4FE;
	static final short NLMENetworkDiscoveryConfirm = (short) 0x9741;
	static final short NLMENwkStatusIndication = (short) 0x9751;

	static final short NLMESetRequest = (short) 0xA323;

	static final short NLMESTARTROUTERConfirm = (short) 0x9744;

	
	static final short NWKProcessSecureFrameReport = (short) 0x9770;
	 
	static final short ZDPNwkProcessSecureFrameConfirm = (short) 0xA070;
	
	 
	static final short NLMENWKSTATUSIndication = (short) 0x9750;
	 
	 
	static final short NLMEGetRequest = (short) 0xA322;
	static final short NLMEGetConfirm = (short) 0xA422;

	static final short NLMEPermitJoiningRequest = (short) 0xA236;
	static final short NLMEPermitJoiningConfirm = (short) 0x9743;
	
	
	static final short NLMENWKUpdateReq = (short) 0xA238;
	static final short ZDPMgmt_Nwk_UpdateNotify = (short) 0xA2B8;
	
	static final short NLMEClearDeviceKeyPairSet = (short) 0xA33F;
	static final short APSClearDeviceKeyPairSetConfirm = (short) 0xA43F;
	
	
	
	
	static final short  NLMEClearNeighborTableEntry = (short) 0xA351;
	
	static final short  ZTCClearNeighborTableEntryConfirm = (short) 0xA451;
	
	
	
	
	
	static final short  MacGetPIBAttributeConfirm = (short) 0x8405;
	
	static final short  NLMENETWORKFORMATIONConfirm = (short) 0x9742;

	static final short  NLMESTARTROUTERRequest = (short) 0x9637;

	 
	
	
	class AddressMode {
		public static final byte Indirect = (byte) 0x00;
		public static final byte DirectGroup = (byte) 0x01;
		public static final byte Direct = (byte) 0x02;
		public static final byte DirectEx = (byte) 0x03;
	}

	class DeviceType {
		public static final byte Coordinator = (byte) 0xC0;
		public static final byte Router = (byte) 0x80;
		public static final byte EndDevice = (byte) 0x20;
		public static final byte RxOnWhenIdleEndDevice = (byte) 0x60;
	}
	
	class LogicalType {
		public static final byte Coordinator = (byte) 0x00;
		public static final byte Router = (byte) 0x01;
		public static final byte EndDevice = (byte) 0x02;
	}
	

	class StartupSet {
		public static final byte UseNVMSet = (byte) 0x00;
		public static final byte UseROMSet = (byte) 0x08;
		public static final byte UseWorkingSet = (byte) 0x10;
		public static final byte UseCommissioningClusterSet = (byte) 0x18;
	}

	class StartupControlMode {
		public static final byte Association = (byte) 0x00;
		public static final byte OrphanRejoin = (byte) 0x01;
		public static final byte NetworkRejoin = (byte) 0x02;
		public static final byte FindAndRejoin = (byte) 0x03;
		public static final byte SilentStart = (byte) 0x04;
	}
}
