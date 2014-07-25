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
package org.energy_home.jemma.ah.io;

public class Color {
	static Color black = new Color(0, 0, 0);
	static Color BLACK = black;
	static Color blue = new Color(0, 0, 255);
	static Color BLUE = blue;
	static Color cyan;
	static Color CYAN = cyan;
	static Color darkGray;
	static Color DARK_GRAY = darkGray;
	static Color gray;
	static Color GRAY = gray;
	static Color green = new Color(0, 255, 0);
	static Color GREEN = green;
	static Color lightGray;
	static Color LIGHT_GRAY = lightGray;
	static Color magenta;
	static Color MAGENTA = magenta;
	static Color orange;
	static Color ORANGE = orange;
	static Color pink;
	static Color PINK = pink;
	static Color red = new Color(255, 0, 0);
	static Color RED = red;
	static Color white = new Color(255, 255, 255);
	static Color WHITE = white;
	static Color yellow;
	static Color YELLOW = yellow;

	private int color = 0;

	public Color(int r, int g, int b) {
		this.color = r << 16 | g << 8 | b;
	}

	public int getRed() {
		return (color >> 16) & 0xFF;
	}

	public int getGreen() {
		return (color >> 8) & 0xFF;
	}

	public int getBlue() {
		return color & 0xFF;
	}
}
