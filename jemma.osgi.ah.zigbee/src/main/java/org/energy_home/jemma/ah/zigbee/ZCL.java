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
package org.energy_home.jemma.ah.zigbee;

/**
 * Contains constants related to the Zigbee Cluster Library like: profile ids,
 * commands ids, attributes ids
 */
public interface ZCL {

	/* ZCL command frames */
	public static final int ZclReadAttrs = 0;
	public static final int ZclReadAttrsRsp = 1;

	public static final int ZclWriteAttrs = 2;
	public static final int ZclWriteAttrsUndivided = 3;
	public static final int ZclWriteAttrsRsp = 4;
	public static final int ZclWriteAttrsNoRsp = 5;

	public static final int ZclConfigRep = 6;
	public static final int ZclConfigRepRsp = 7;

	public static final int ZclRepConf = 8;
	public static final int ZclRepConfRsp = 9;

	public static final int ZclReportAttrs = 0x0A;

	public static final int ZclDefaultRsp = 0x0B;

	public static final int ZclDiscoverAttrs = 0x0c;
	public static final int ZclDiscoverAttrsRsp = 0x0d;
	public static final int ZclReadAttrsStructured = 0x0e;
	public static final int ZclWriteAttrsStructured = 0x0f;
	public static final int ZclWriteAttrsStructuredRsp = 0x10;
	

	public static final short SUCCESS = 0x00;
	public static final short FAILURE = 0x01;
	public static final short NOT_AUTHORIZED = 0x7e;
	public static final short RESERVED_FIELD_NOT_ZERO = 0x7f;
	public static final short MALFORMED_COMMAND = 0x80;
	public static final short UNSUP_CLUSTER_COMMAND = 0x81;
	public static final short UNSUP_GENERAL_COMMAND = 0x82;
	public static final short UNSUP_MANUF_CLUSTER_COMMAND = 0x83;
	public static final short UNSUP_MANUF_GENERAL_COMMAND = 0x84;
	public static final short INVALID_FIELD = 0x85;
	public static final short UNSUPPORTED_ATTRIBUTE = 0x86;
	public static final short INVALID_VALUE = 0x87;
	public static final short READ_ONLY = 0x88;
	public static final short INSUFFICIENT_SPACE = 0x89;
	public static final short DUPLICATE_EXISTS = 0x8a;
	public static final short NOT_FOUND = 0x8b;
	public static final short UNREPORTABLE_ATTRIBUTE = 0x8c;
	public static final short INVALID_DATA_TYPE = 0x8d;
	public static final short INVALID_SELECTOR = 0x8e;
	public static final short WRITE_ONLY = 0x8f;
	public static final short INCONSISTENT_STARTUP_STATE = 0x90;
	public static final short DEFINED_OUT_OF_BAND = 0x91;
	public static final short HARDWARE_FAILURE = 0xc0;
	public static final short SOFTWARE_FAILURE = 0xc1;
	public static final short CALIBRATION_ERROR = 0xc3;
}
