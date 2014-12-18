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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import org.energy_home.jemma.javagal.layers.data.interfaces.IConnector;
import org.energy_home.jemma.javagal.layers.data.interfaces.IDataLayer;
import org.energy_home.jemma.javagal.layers.object.ByteArrayObject;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jssc implementation of the {@link IConnector}.
 * 
 * @author "Ing. Marco Nieddu <a href="mailto:marco.nieddu@consoft.it
 *         ">marco.nieddu@consoft.it</a> or <a href="marco.niedducv@gmail.com
 *         ">marco.niedducv@gmail.com</a> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class SerialPortConnectorJssc implements IConnector {
	private Boolean connected = Boolean.FALSE;

	private Boolean ignoreMessage = Boolean.FALSE;
	private SerialPort serialPort;
	private static final Logger LOG = LoggerFactory.getLogger(SerialPortConnectorJssc.class);

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
	public SerialPortConnectorJssc(String _portName, int _boudRate, IDataLayer _DataLayer) throws Exception {
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
			serialPort = new SerialPort(portName);
			if (!serialPort.openPort()) {
				LOG.error("Error: Port is currently in use:" + portName);
				disconnect();
				return false;
			} else {

				serialPort.setParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

				try {
					serialReader = new SerialReader(this);
					serialPort.addEventListener(serialReader);
					LOG.debug("Added SerialPort event listener");
				} catch (SerialPortException e) {
					disconnect();
					throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
				}

				LOG.info("Connection on " + portName + " established");

				setConnected(true);

				return true;

			}

		} catch (SerialPortException e) {
			disconnect();
			LOG.error("the connection could not be made: " + portName);
			return false;
		}
	}

	private void setConnected(boolean value) {
		connected = value;

	}

	/**
	 * @inheritDoc
	 */
	public synchronized void write(ByteArrayObject buff) throws Exception {
		if (isConnected()) {

			try {
				LOG.debug(">>> Sending: {}", buff.ToHexString());
				serialPort.writeBytes(buff.getArrayRealSize());
			} catch (Exception e) {

				LOG.debug("Error writing Rs232: {} -- Error: {}",buff.ToHexString() , e);
				throw e;

			}

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
	 * @throws SerialPortException
	 * @inheritDoc
	 */
	public void disconnect() throws SerialPortException {
		setConnected(false);
		serialReader = null;

		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.closePort();
			serialPort = null;

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

				if (event.isRXCHAR() && !getIgnoreMessage()) {
					int numberOfBytes = event.getEventValue();
					if (numberOfBytes > 0) {
						byte[] bufferOriginal = serialPort.readBytes(numberOfBytes);
						ByteArrayObject frame = new ByteArrayObject(bufferOriginal, numberOfBytes);
						_caller.getDataLayer().notifyFrame(frame);
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
		LOG.info("Starting inizialize procedure for: PortName={} -- Speed={}", commport , boudrate);
		LOG.info("DefaultTimeout: {}", DataLayer.getPropertiesManager().getCommandTimeoutMS());
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
		LOG.debug("Starting inizialize procedure for: PortName={} -- Speed={}", commport , boudrate);
		LOG.debug("DefaultTimeout: {}", DataLayer.getPropertiesManager().getCommandTimeoutMS());

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

}