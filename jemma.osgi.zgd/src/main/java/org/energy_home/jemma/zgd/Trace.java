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
package org.energy_home.jemma.zgd;

public class Trace {
	protected static Trace instance;

	public static void println(String s) {
		if (isTrace()) instance.print0(s + '\n');
	}
	
	public static void print(String s) {
		if (isTrace()) instance.print0(s);
	}

	public static void printf(String s, Object... args) {
		if (isTrace()) instance.printf0(s, args);
	}
	
	public static void setTrace(Trace t) {
		instance = t;
	}
	
	public static boolean isTrace() {
		return instance != null;
	}
	
	protected void print0(String s) {
		System.out.print(s);
	}
	
	protected void printf0(String s, Object... args) {
		System.out.printf(s, args);
	}
}
