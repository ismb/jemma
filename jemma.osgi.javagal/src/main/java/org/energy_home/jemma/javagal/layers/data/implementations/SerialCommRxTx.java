/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2010 Telecom Italia (http://www.telecomitalia.it)
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
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;

import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;

/**
 * RxTx implementation of the {@link IConnector}.
 */
/**
 * @author "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
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

	public SerialCommRxTx(String _portName, int _boudRate, IDataLayer _DataLayer)
			throws Exception {
		DataLayer = _DataLayer;
		commport = _portName;
		boudrate = _boudRate;
	}

	public IDataLayer getDataLayer() {
		return DataLayer;
	}

	public void inizialize() throws Exception {
		synchronized (this) {
			connected = true;
		}
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("\n\rStarting inizialize procedure for: \n\rPortName="
					+ commport + "\n\rSpeed=" + boudrate + "\n\r");
		if (!connect(commport, boudrate)) {
			throw new Exception("Unable to connect to serial port!");
		}
		DataLayer.cpuReset();
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			logger.info("\n\rWaiting 2,5 seconds after command CPUReset...\n\r");
		Thread.sleep(2500);
		synchronized (this) {
			connected = true;
		}
		Status _status = DataLayer
				.SetModeSelectSync(IDataLayer.INTERNAL_TIMEOUT);
		if (_status.getCode() != GatewayConstants.SUCCESS)
			throw new Exception("Errorn on SetMode:" + _status.getMessage());
		else {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.info("\n\rConnected: \n\rPortName=" + commport
						+ "\n\rSpeed=" + boudrate + "\n\r");
			synchronized (this) {
				connected = true;
			}
		}
	}

	private boolean connect(String portName, int speed) throws Exception {

		try {
			System.setProperty("gnu.io.rxtx.SerialPorts", portName);
			portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
			if (portIdentifier.isCurrentlyOwned()) {
				logger.error("\n\rError: Port is currently in use:" + portName
						+ "\n\r");
				disconnect();
				return false;
			} else {

				serialPort = (SerialPort) portIdentifier.open(this.getClass()
						.getName(), 2000);
				if (serialPort instanceof SerialPort) {
					serialPort.setSerialPortParams(speed,
							SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
					in = serialPort.getInputStream();
					serialReader = new SerialReader(in, this);
					serialPort.notifyOnDataAvailable(true);
					try {
						serialPort.addEventListener(serialReader);
					} catch (TooManyListenersException e) {
						disconnect();
						throw new Exception(
								"Error Too Many Listeners Exception on  serial port:"
										+ e.getMessage());
					}
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.info("\n\rConnection on " + portName
								+ " established\n\r");
					return true;
				} else {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						logger.error("\n\rError on serial port connection:"
								+ portName + "\n\r");
					disconnect();
					return false;
				}
			}

		} catch (NoSuchPortException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("\n\rthe connection could not be made: NoSuchPortException "
						+ portName + "\n\r");
			disconnect();
			return false;
		} catch (PortInUseException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("\n\rthe connection could not be made: PortInUseException\n\r");
			disconnect();
			return false;
		} catch (UnsupportedCommOperationException e) {
			if (DataLayer.getPropertiesManager().getDebugEnabled())
				logger.error("\n\rthe connection could not be made: UnsupportedCommOperationException\n\r");
			disconnect();
			return false;
		}

	}

	public void write(ByteArrayObject buff) throws Exception {

		if (isConnected()) {
			synchronized (serialPort.getOutputStream()) {

				if (serialPort.getOutputStream() != null) {
					serialPort.getOutputStream().write(buff.getByteArray(), 0,
							buff.getByteCount(true));
					serialPort.getOutputStream().flush();
				} else
					throw new Exception("Error on serial write - out == null");

			}
		}

	}

	@Override
	public boolean isConnected() {
		return connected;
	}

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
			logger.info("\n\rDisconnected\n\r");
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
							logger.error("\n\rError on data received:"
									+ e.getMessage() + "\n\r");
					}

				}
				}
			} catch (Exception e) {
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					logger.error("Error on read from serial data:"
							+ e.getMessage());

			}

		}
	}

}
