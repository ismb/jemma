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
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;
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
	private Boolean connected = false;
	private SerialPort serialPort;
	private final static Log logger = LogFactory.getLog(SerialCommRxTx.class);
	CommPortIdentifier portIdentifier;
	InputStream in = null;
	OutputStream ou = null;
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
			System.setProperty("gnu.io.SerialPorts", portName);
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				logger.error("Error: Port is currently in use:" + portName);
				disconnect();
				return false;
			} else {

				serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
				if (serialPort instanceof SerialPort) {
					/* Freescale code */
					serialPort.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
					serialPort.setRTS(true);
					serialPort.setDTR(true);
					serialPort.enableReceiveTimeout(1000);
					in = serialPort.getInputStream();
					ou = serialPort.getOutputStream();
					serialReader = new SerialReader(this);
					serialPort.notifyOnDataAvailable(true);
					try {
						serialPort.addEventListener(serialReader);
						if (DataLayer.getPropertiesManager().getDebugEnabled())
							logger.info("Added SerialPort event listener");
					} catch (TooManyListenersException e) {
						disconnect();
						throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
					}

					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.info("Connection on " + portName + " established");

					synchronized (connected) {
						connected = true;
					}

					return true;
				} else {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.error("Error on serial port connection:" + portName);
					disconnect();
					return false;
				}
			}

		} catch (NoSuchPortException e) {
			disconnect();
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: NoSuchPortException " + portName + " - \n\rList of ports:");
			CommPortIdentifier cpi = null;
			Enumeration ls = CommPortIdentifier.getPortIdentifiers();
			while (ls.hasMoreElements()) {
				try {
					cpi = (CommPortIdentifier) ls.nextElement();
				} catch (NoSuchElementException n) {

				}
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					logger.error("Port: " + cpi.getName());
			}
			e.printStackTrace();
			return false;
		} catch (PortInUseException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: PortInUseException");
			disconnect();
			e.printStackTrace();
			return false;
		} catch (UnsupportedCommOperationException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("the connection could not be made: UnsupportedCommOperationException");
			disconnect();
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * @inheritDoc
	 */
	public void write(ByteArrayObject buff) throws Exception {
		if (isConnected()) {
			if (ou != null) {
				try {
					ou.write(buff.getByteArray(), 0, buff.getByteCount(true));
					ou.flush();

				} catch (Exception e) {

					e.printStackTrace();
					throw e;

				}
			} else
				throw new Exception("Error on serial write - out == null");

		}
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
		synchronized (connected) {
			connected = false;
		}
		if (serialPort != null) {
			if (in != null) {
				in.close();
				in = null;
			}
			if (ou != null) {
				ou.flush();
				ou.close();
			}

			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
			serialReader = null;
			portIdentifier = null;
		}

		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Disconnected");
	}

	class SerialReader implements SerialPortEventListener {

		private byte[] buffer = new byte[2048];
		IConnector _caller = null;

		public SerialReader(IConnector _parent) {

			_caller = _parent;
		}

		@Override
		public synchronized void serialEvent(SerialPortEvent event) {
			try {
				if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
					try {
						int pos = 0;
						Integer data = 0;
						while (in.available() > 0) {
							try {
								data = in.read();
								buffer[pos] = data.byteValue();
								pos = pos + 1;
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						ByteArrayObject frame = new ByteArrayObject(buffer, pos);
						_caller.getDataLayer().notifyFrame(frame);

					} catch (Exception e) {
						if (DataLayer.getPropertiesManager().getDebugEnabled())
							logger.error("Error on data received:" + e.getMessage());
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

		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Starting inizialize procedure for: PortName=" + commport + " -- Speed=" + boudrate + " -- DefaultTimeout:" + DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}

		DataLayer.cpuReset();

		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Waiting 5 seconds after command CPUReset...");
		Thread.sleep(5000);

		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("Clear buffer after CPUReset...");

		DataLayer.clearBuffer();

		Status _status = DataLayer.SetModeSelectSync(DataLayer.getPropertiesManager().getCommandTimeoutMS());
		if (_status.getCode() != GatewayConstants.SUCCESS)
			throw new Exception("Errorn on SetMode:" + _status.getMessage());
		else {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.info("Connected: PortName=" + commport + "Speed=" + boudrate);

		}
	}

}