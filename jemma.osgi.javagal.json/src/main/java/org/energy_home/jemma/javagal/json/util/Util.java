package org.energy_home.jemma.javagal.json.util; /**
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

import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Info;
import org.energy_home.jemma.zgd.jaxb.Status;

public class Util {

    /**
     * Conventional internal timeout value.
     */
    public final static long INTERNAL_TIMEOUT;
	private final static Long unsigned8MaxValue;
    private final static Long unsigned32MaxValue;

    static {
        unsigned8MaxValue = Long.decode("0xff");
        unsigned32MaxValue = Long.decode("0xffffffff");
        INTERNAL_TIMEOUT = 5000;
    }

	/**
	 * Tells if the value contained in a candidate Long is actually an unsigned
	 * 8 bits value or not (1 byte).
	 * 
	 * @param candidate
	 *            the candidate Long.
	 * @return true if the value is actually an unsigned 8 bits, false
	 *         otherwise.
	 */
	synchronized public static boolean isUnsigned8(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned8MaxValue));
	}

    /**
	 * Tells if the value contained in a candidate Long is actually an unsigned
	 * 32 bits value or not (4 bytes).
	 * 
	 * @param candidate
	 *            the candidate Long.
	 * @return true if the value is actually an unsigned 32 bits, false
	 *         otherwise.
	 */
	synchronized public static boolean isUnsigned32(Long candidate) {
		return ((candidate >= 0) && (candidate <= unsigned32MaxValue));
	}
    synchronized  public static Info setError(String error) {
        Info info = new Info();
        Status status = new Status();
        status.setCode((short) GatewayConstants.GENERAL_ERROR);
        status.setMessage(error);
        info.setStatus(status);
        info.setDetail(new Info.Detail());
        return info;
    }
    synchronized  public static Info setSuccess(Info.Detail detail){
        Info info = new Info();
        Status st = new Status();
        st.setCode((short) GatewayConstants.SUCCESS);
        info.setStatus(st);

        info.setDetail(detail);
        return info;
    }
}
