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
package org.energy_home.jemma.javagal.layers.data.implementations;

import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ShortArrayObject;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RxTx implementation of the {@link IConnector}.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SerialCommRxTx implements IConnector, Runnable {
	private Boolean connected = Boolean.FALSE;

	private Boolean ignoreMessage = Boolean.FALSE;
	private SerialPort serialPort;
	private static final Logger LOG = LoggerFactory.getLogger(SerialCommRxTx.class);

	CommPortIdentifier portIdentifier;
	InputStream in = null;
	OutputStream ou = null;

	private IDataLayer DataLayer = null;
	private String commport = "";
	private int boudrate = 0;
	Thread serialReader;

	/**
	 * Creates a new instance.
	 * 
	 * @param _portName
	 *            the port name.
	 * @param _boudRate
	 *            the baud rate.
	 * @param _DataLayer
	 *            an actual implementation of the {@code IDataLayer} to use in
	 *            this connection.
	 * @throws Exception
	 *             if an error occurs.
	 */
	public SerialCommRxTx(String _portName, int _boudRate, IDataLayer _DataLayer) throws Exception {
		DataLayer = _DataLayer;
		commport = _portName;
		boudrate = _boudRate;
		new Thread(this, "SerialReader").start();
	}

	/**
	 * Gets the actual DataLayer implementation used by this connection.
	 */
	public IDataLayer getDataLayer() {
		return DataLayer;
	}

	/**
	 * @inheritDoc
	 */
	private boolean connect(String portName, int speed) throws Exception {

		try {
			portIdentifier = null;
			System.setProperty("gnu.io.rxtx.SerialPorts", portName);
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				LOG.error("Error: Port is currently in use:" + portName + " by: " + portIdentifier.getCurrentOwner());
				disconnect();
				return false;

			} else {
				serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
				if (serialPort instanceof SerialPort) {
					serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

					// serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN
					// | SerialPort.FLOWCONTROL_RTSCTS_OUT);
					// serialPort.setDTR(true);
					// serialPort.setRTS(true);
					serialPort.enableReceiveTimeout(2000);
					// serialPort.notifyOnDataAvailable(true);
					in = serialPort.getInputStream();
					ou = serialPort.getOutputStream();
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.info("Connection on " + portName + " established");

					setConnected(true);
					serialReader = new Thread(this, "SerialReader");
					serialReader.start();

					return true;
				} else {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.error("Error on serial port connection:" + portName);
					disconnect();
					return false;
				}
			}

		} catch (NoSuchPortException e) {
			disconnect();
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				LOG.error("the connection could not be made: NoSuchPortException " + portName);
			// e.printStackTrace();
			return false;
		} catch (PortInUseException e) {
			disconnect();
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				LOG.error("the connection could not be made: PortInUseException");

			// e.printStackTrace();
			return false;
		} catch (UnsupportedCommOperationException e) {
			disconnect();
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				LOG.error("the connection could not be made: UnsupportedCommOperationException");
			// e.printStackTrace();
			return false;
		}

	}

	private synchronized void setConnected(boolean value) {
		connected = value;

	}

	/**
	 * @inheritDoc
	 */
	public void write(ShortArrayObject buff) throws Exception {
		if (isConnected()) {
			if (ou != null) {
				try {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.info(">>> Sending: " + buff.ToHexString());
					ou.write(buff.getByteArray(), 0, buff.getCount(true));
					// ou.flush();// TODO FLUSH PROBLEM INTO THE FLEX-GATEWAY

				} catch (Exception e) {

					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.error("Error writing Rs232:" + buff.ToHexString() + " -- Error:" + e.getMessage());
					throw e;

				}

			} else
				throw new Exception("Error on serial write - out == null");

		} else
			throw new Exception("Error on serial write - not connected");
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public synchronized boolean isConnected() {
		return connected;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void disconnect() throws IOException {
		System.setProperty("gnu.io.rxtx.SerialPorts", "");
		// serialReader.interrupt();
		setConnected(false);

		serialReader = null;

		if (serialPort != null) {
			if (in != null) {
				in.close();
				in = null;
			}
			if (ou != null) {
				ou.close();
				ou = null;
			}
			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;

			portIdentifier = null;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {

			}
		}
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("RS232 - Disconnected");
	}

	@Override
	public void run() {
		short[] buffer = new short[5000];
		while (isConnected()) {
			try {
				if (in != null) {
					int pos = 0;
					Integer data = 0;
					while ((in.available()) > 0) {
						data = in.read();
						buffer[pos] = data.shortValue();
						pos = pos + 1;
					}

					if (!getIgnoreMessage() && pos > 0) {
						ShortArrayObject frame = new ShortArrayObject(buffer, pos);
						getDataLayer().notifyFrame(frame);
					}

				}
			} catch (Exception e) {
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					LOG.error("Error reading rs232 data:" + e.getMessage());

			}

		}

	}

	private synchronized void setIgnoreMessage(boolean value) {
		ignoreMessage = value;

	}

	private synchronized boolean getIgnoreMessage() {
		return ignoreMessage;

	}

	/**
	 * @inheritDoc
	 */
	public void initialize() throws Exception {
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("Starting inizialize procedure for: PortName=" + commport + " -- Speed=" + boudrate + " -- DefaultTimeout:" + DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}

		setIgnoreMessage(true);
		DataLayer.cpuReset();
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("Waiting 3,5 seconds after command CPUReset...");
		Thread.sleep(3500);

		disconnect();
		setIgnoreMessage(false);
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("Clear buffer after CPUReset...");
		DataLayer.clearBuffer();
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("Re-Starting inizialize procedure after CPUReset for: PortName=" + commport + " -- Speed=" + boudrate + " -- DefaultTimeout:" + DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}
		Status _status = DataLayer.SetModeSelectSync(DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (_status.getCode() != GatewayConstants.SUCCESS)
			throw new Exception("Errorn on SetMode:" + _status.getMessage());
		else {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				LOG.info("Connected: PortName=" + commport + "Speed=" + boudrate);

		}
	}

}