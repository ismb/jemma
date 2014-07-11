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
package org.energy_home.jemma.ah.upnp.energyathome;

import org.osgi.service.upnp.UPnPIcon;
import java.io.InputStream;
import java.io.IOException;

public final class Icon implements UPnPIcon {

	private int height;

	private int width;

	private int depth;

	private String type;


	/**
	 * Constructs a new <code>Icon</code> object. The image content is not
	 * downloaded on construction of the object but on demand (it's downloaded
	 * only ones).
	 * 
	 * @param int h the height of the icon
	 * @param int w the width of the icon
	 * @param int d the depth of the icon
	 * @param String
	 *            type the MIME type
	 * @param String
	 *            name the resource name
	 */
	public Icon(int h, int w, int d, String type, String resName) {
		height = h;
		width = w;
		depth = d;
		this.type = type;
	}

	/**
	 * Gets the height of the icon.
	 * 
	 * @return a <code>byte</code> representation of the Height of the icon.
	 */
	public int getHeight() {
		return height;
	}

	public int getSize() {
		return -1;
	}

	/**
	 * Gets the width of the icon.
	 * 
	 * @return a <code>byte</code> representation of the Width of the icon.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the Color Depth of the icon.
	 * 
	 * @return a <code>byte</code> representation of the Color Depth of the icon
	 */
	public int getDepth() {
		return depth;
	}

	/**
	 * Gets type of the icon . MIME Type see RFC 2387
	 * 
	 * @return a <code>String</code> representation of the Type of the icon.
	 */
	public String getMimeType() {
		return type;
	}

	public InputStream getInputStream() throws IOException {
		InputStream aa = this.getClass().getResourceAsStream("icon");
		return aa;
	}
}
