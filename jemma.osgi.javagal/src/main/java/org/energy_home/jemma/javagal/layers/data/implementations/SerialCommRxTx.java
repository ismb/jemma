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

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;

/**
 * RxTx implementation of the {@link IConnector}.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SerialCommRxTx implements IConnector {
	private boolean connected = false;
	private SerialPort serialPort;
	private final static Log logger = LogFactory.getLog(SerialCommRxTx.class);
	CommPortIdentifier portIdentifier;
	InputStream in = null;
	private SerialReader serialReader = null;

	private IDataLayer DataLayer = null;
	private String commport = "";
	private int boudrate = 0;

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
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				logger.error("Error: Port is currently in use:" + portName);
				disconnect();
				return false;
			} else {

				serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
				if (serialPort instanceof SerialPort) {
					/*Freescale code*/
					serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
					serialPort.setRTS(true);
					serialPort.setDTR(true);
					serialPort.enableReceiveTimeout(1000);
					in = serialPort.getInputStream();
					serialReader = new SerialReader(in, this);
					try {
						serialPort.addEventListener(serialReader);
					} catch (TooManyListenersException e) {
						disconnect();
						throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
					}
					serialPort.notifyOnDataAvailable(true);
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.info("Connection on " + portName + " established");
					
					/*
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
					serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					in = serialPort.getInputStream();
					serialReader = new SerialReader(in, this);
					try {
						serialPort.addEventListener(serialReader);
					} catch (TooManyListenersException e) {
						disconnect();
						throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
					}
					serialPort.notifyOnDataAvailable(true);
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.info("Connection on " + portName + " established");
					*/
					return true;
				} else {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.error("Error on serial port connection:" + portName);
					disconnect();
					return false;
				}
			}

		} catch (NoSuchPortException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: NoSuchPortException " + portName);
			disconnect();
			return false;
		} catch (PortInUseException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: PortInUseException");
			disconnect();
			return false;
		} catch (UnsupportedCommOperationException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: UnsupportedCommOperationException");
			disconnect();
			return false;
		}

	}

	/**
	 * @inheritDoc
	 */
	public void write(ByteArrayObject buff) throws Exception {

		if (isConnected()) {
			synchronized (serialPort.getOutputStream()) {

				if (serialPort.getOutputStream() != null) {
					serialPort.getOutputStream().write(buff.getByteArray(), 0, buff.getByteCount(true));
					serialPort.getOutputStream().flush();
				} else
					throw new Exception("Error on serial write - out == null");

			}
		}

	}

	/**
	 * @inheritDoc
	 */
	@Override
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void disconnect() throws IOException {

		if (serialPort != null) {
			if (in != null) {
				in.close();
				in = null;
			}
			if (serialPort.getOutputStream() != null)
				serialPort.getOutputStream().close();

			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
			serialReader = null;
			portIdentifier = null;
		}
		synchronized (this) {
			connected = false;
		}
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Disconnected");
	}

	class SerialReader implements SerialPortEventListener {
		InputStream in;
		private byte[] buffer = new byte[1024];
		IConnector _caller = null;

		public SerialReader(InputStream in, IConnector _parent) {
			this.in = in;
			_caller = _parent;
		}

		@Override
		public void serialEvent(SerialPortEvent event) {
			try {
				switch (event.getEventType()) {
				case SerialPortEvent.BI:
				case SerialPortEvent.OE:
				case SerialPortEvent.FE:
				case SerialPortEvent.PE:
				case SerialPortEvent.CD:
				case SerialPortEvent.CTS:
				case SerialPortEvent.DSR:
				case SerialPortEvent.RI:
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
					break;
				case SerialPortEvent.DATA_AVAILABLE: {
					try {
						int pos = 0;
						int data = 0;
						while (in.available() > 0) {
							data = in.read();
							buffer[pos++] = (byte) data;
						}
						ByteArrayObject frame = new ByteArrayObject(buffer, pos);
						_caller.getDataLayer().notifyFrame(frame);
					} catch (Exception e) {
						if (DataLayer.getPropertiesManager().getDebugEnabled())
							logger.error("Error on data received:" + e.getMessage());
					}
				}
				}
			} catch (Exception e) {
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					logger.error("Error on read from serial data:" + e.getMessage());

			}

		}
	}

	/**
	 * @inheritDoc
	 */
	public void initialize() throws Exception {
		synchronized (this) {
			connected = true;
		}
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Starting inizialize procedure for: PortName=" + commport + "Speed=" + boudrate);
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}
		DataLayer.cpuReset();
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Waiting 2,5 seconds after command CPUReset...");
		Thread.sleep(2500);
		synchronized (this) {
			connected = true;
		}
		Status _status = DataLayer.SetModeSelectSync(DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (_status.getCode() != GatewayConstants.SUCCESS)
			throw new Exception("Errorn on SetMode:" + _status.getMessage());
		else {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.info("Connected: PortName=" + commport + "Speed=" + boudrate);
			synchronized (this) {
				connected = true;
			}
		}
	}

}