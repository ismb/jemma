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
package org.energy_home.jemma.ah.io.flexgateway;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlexGatewayButtons implements Runnable {

	static final String flexGatewayButtonsDevice = "/dev/input/event0";
	private File buttonFile;
	private static final Logger LOG = LoggerFactory.getLogger( FlexGatewayButtons.class );
	private Thread t;

	private FileInputStream fis = null;
	FileChannel channel = null;
	private EventAdmin eventAdmin;

	protected synchronized void activate() {
		t = new Thread(this);
		t.start();
	}

	protected synchronized void deactivate() {
		LOG.debug("deactivate");
		t.interrupt();
		try {
			t.join();
		} catch (InterruptedException e) {
			LOG.error("interrupted exception on join",e);
		}
	}

	protected void setEventAdmin(EventAdmin s) {
		this.eventAdmin = s;
	}

	protected void unsetEventAdmin(EventAdmin s) {
		if (eventAdmin == s)
			this.eventAdmin = null;
	}

	public void run() {
		long tv_sec;
		long tv_usec;
		short type;
		short code;
		int value;
		while (true) {
			buttonFile = new File(flexGatewayButtonsDevice);
			ByteBuffer buffer = ByteBuffer.allocate(100);
			buffer.order(ByteOrder.nativeOrder());
			try {
				fis = new FileInputStream(buttonFile);
				channel = fis.getChannel();
				while (true) {
					buffer.clear();
					int size = channel.read(buffer);
					buffer.rewind();

					while (size > 0) {
						tv_sec = buffer.getInt();
						tv_usec = buffer.getInt();
						long tv = tv_sec * 1000000 + tv_usec;
						type = buffer.getShort();
						code = buffer.getShort();
						value = buffer.getInt();
						size -= 16;

						if (type == 0 && code == 0 && value == 0)
							continue;

						// Code 3 -> front button
						// Code 2 -> back button
						// Value > 0 -> button pressed
						// Value > 0 -> button released

						// send event
						LOG.debug("Button: ms " + tv + " type " + (type & 0xffff) + " code " + (code & 0xffff) + " value "
								+ (value & 0xffffffff));

						Hashtable props = new Hashtable();
						props.put("timestamp", new Long(tv));
						if (value > 0)
							this.postEvent("button/" + code + "/UP", props);
						else
							this.postEvent("button/" + code + "/DOWN", props);
					}
				}
			} catch (ClosedByInterruptException e) {
				break;
			} catch (IOException e) {
				// FIXME Why these Breaks in catch block ? What is the effect on this ?this seems risky / to be reviewed
				LOG.error("Exception on Run", e);
				break;
			} finally {
				try {
					if (channel != null)
						channel.close();
				} catch (IOException e) {
					LOG.error("exception", e);
					break;
				}
			}
		}
		LOG.debug("exiting");
	}

	private void postEvent(String topic, Map props) {
		if (this.eventAdmin != null) {
			try {
				this.eventAdmin.postEvent(new Event(topic, props));
			} catch (Exception e) {
				LOG.error("Exception on postEvent",e);
			}
		}
	}
}
