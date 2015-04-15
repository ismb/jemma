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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.energy_home.jemma.javagal.layers.data.implementations.IDataLayerImplementation.DataFreescale;
import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RxTx implementation of the {@link IConnector}.
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SerialPortConnectorRxTx implements IConnector {
	private Boolean connected = Boolean.FALSE;

	private Boolean ignoreMessage = Boolean.FALSE;
	private SerialPort serialPort;
	private static final Logger LOG = LoggerFactory.getLogger(SerialPortConnectorRxTx.class);

	CommPortIdentifier portIdentifier;
	InputStream in = null;
	OutputStream ou = null;

	private IDataLayer DataLayer = null;
	private String commport = "";
	private int boudrate = 0;
	private SerialReader serialReader = null;

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
	public SerialPortConnectorRxTx(String _portName, int _boudRate, IDataLayer _DataLayer) throws Exception {
		DataLayer = _DataLayer;
		commport = _portName;
		boudrate = _boudRate;
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
			System.setProperty("gnu.io.rxtx.SerialPorts", portName);
			portIdentifier = null;
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
					serialPort.enableReceiveTimeout(2000);
					serialPort.notifyOnDataAvailable(true);

					try {
						serialReader = new SerialReader(this);
						serialPort.addEventListener(serialReader);
						LOG.debug("Added SerialPort event listener");
					} catch (TooManyListenersException e) {
						disconnect();
						throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
					}

					in = serialPort.getInputStream();
					ou = serialPort.getOutputStream();
					
					LOG.info("Connection on " + portName + " established");

					setConnected(true);

					return true;
				} else {
					LOG.error("Error on serial port connection:" + portName);
					disconnect();
					return false;
				}
			}

		} catch (NoSuchPortException e) {
			disconnect();
			LOG.error("the connection could not be made: NoSuchPortException " + portName);
			return false;
		} catch (PortInUseException e) {
			disconnect();
			LOG.error("the connection could not be made: PortInUseException");
			return false;
		} catch (UnsupportedCommOperationException e) {
			disconnect();
			LOG.error("the connection could not be made: UnsupportedCommOperationException");
			return false;
		}

	}

	private void setConnected(boolean value) {
		connected = value;

	}

	/**
	 * @inheritDoc
	 */
	public void write(ByteArrayObject buff) throws Exception {
		if (isConnected()) {
			if (ou != null) {
				try {
					LOG.debug(">>> Sending: " + buff.ToHexString());
					synchronized(ou)
					{
						ou.write(buff.getArray(), 0, buff.getCount(true));
						if(DataLayer.getPropertiesManager().getzgdDump())
							{
							String directory = DataLayer.getPropertiesManager().getDirDump();
							String fileName = System.currentTimeMillis() + "-w.bin";
							dumpToFile(directory + File.separator + fileName, buff.getArray());
							}
					}
					// ou.flush();// TODO FLUSH PROBLEM INTO THE FLEX-GATEWAY

				} catch (Exception e) {

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
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @inheritDoc
	 */
	public void disconnect() throws IOException {
		System.setProperty("gnu.io.rxtx.SerialPorts", "");
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
		LOG.info("RS232 - Disconnected");
	}

	class SerialReader implements SerialPortEventListener {

		IConnector _caller = null;

		public SerialReader(IConnector _parent) {

			_caller = _parent;
		}

		public void serialEvent(SerialPortEvent event) {
			try {
				synchronized(in)
				{
					if ((event.getEventType() == SerialPortEvent.DATA_AVAILABLE) && !getIgnoreMessage()) {
						int numberOfBytes = in.available();
						if (numberOfBytes > 0) {
							byte[] bufferOriginal = new byte[numberOfBytes];
							in.read(bufferOriginal);
							ByteArrayObject frame = new ByteArrayObject(bufferOriginal, numberOfBytes);
							_caller.getDataLayer().notifyFrame(frame);
							if(DataLayer.getPropertiesManager().getzgdDump())
								{
								String directory = DataLayer.getPropertiesManager().getDirDump();
								String fileName = System.currentTimeMillis() + "-r.bin";
								dumpToFile(directory + File.separator + fileName, bufferOriginal);
								}
						}
					}
				}

			} catch (Exception e) {

				LOG.error("Error on read from serial data:" + e.getMessage());

			}
		}
	}

	private void setIgnoreMessage(boolean value) {
		ignoreMessage = value;

	}

	private boolean getIgnoreMessage() {
		return ignoreMessage;

	}

	/**
	 * @inheritDoc
	 */
	public void initialize() throws Exception {
		LOG.info("Starting inizialize procedure for: PortName=" + commport + " -- Speed=" + boudrate + " -- DefaultTimeout:" + DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}

		setIgnoreMessage(true);
		DataLayer.cpuReset();
		LOG.info("Waiting 3,5 seconds after command CPUReset...");
		Thread.sleep(3500);
		disconnect();
		LOG.info("Clear buffer after CPUReset...");
		DataLayer.clearBuffer();
		setIgnoreMessage(false);
		LOG.debug("Re-Starting inizialize procedure after CPUReset for: PortName=" + commport + " -- Speed=" + boudrate + " -- DefaultTimeout:" + DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}
		Status _status = DataLayer.SetModeSelectSync(DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (_status.getCode() != GatewayConstants.SUCCESS)
			throw new Exception("Errorn on SetMode:" + _status.getMessage());
		else {
			LOG.info("Connected: PortName=" + commport + "Speed=" + boudrate);

		}
	}
	
	/**
	 * Dump byte array on file
	 * @param filepath name of file
	 * @param buffer the buffer to dump
	 * @throws IOException if file doesn't exist or some problem occurs during opening
	 */
	public void dumpToFile(String filepath, byte[] buffer) 
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream(filepath);
		fos.write(buffer);
		fos.close();
	}	

}