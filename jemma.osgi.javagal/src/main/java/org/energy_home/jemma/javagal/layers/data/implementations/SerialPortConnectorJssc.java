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
import org.energy_home.jemma.javagal.layers.object.ShortArrayObject;
import org.energy_home.jemma.zgd.GatewayConstants;
import org.energy_home.jemma.zgd.jaxb.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jssc implementation of the {@link IConnector}.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
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
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.info("Added SerialPort event listener");
				} catch (SerialPortException e) {
					disconnect();
					throw new Exception("Error Too Many Listeners Exception on  serial port:" + e.getMessage());
				}

				if (DataLayer.getPropertiesManager().getDebugEnabled())
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

	private synchronized void setConnected(boolean value) {
		connected = value;

	}

	/**
	 * @inheritDoc
	 */
	public void write(ShortArrayObject buff) throws Exception {
		if (isConnected()) {

			try {
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					LOG.info(">>> Sending: " + buff.ToHexString());
				serialPort.writeBytes(buff.getByteArrayRealSize());
			} catch (Exception e) {

				if (DataLayer.getPropertiesManager().getDebugEnabled())
					LOG.error("Error writing Rs232:" + buff.ToHexString() + " -- Error:" + e.getMessage());
				throw e;

			}

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
	 * @throws SerialPortException
	 * @inheritDoc
	 */
	@Override
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
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("RS232 - Disconnected");
	}

	class SerialReader implements SerialPortEventListener {

		IConnector _caller = null;

		public SerialReader(IConnector _parent) {

			_caller = _parent;
		}

		public synchronized void serialEvent(SerialPortEvent event) {
			try {
				try {
					int numberOfBytes = event.getEventValue();
					if (numberOfBytes > 0) {
						byte[] bufferOr = serialPort.readBytes(numberOfBytes);

						if (!ignoreMessage) {
							short[] buffer = new short[bufferOr.length];

							for (int i = 0; i < buffer.length; i++) {
								buffer[i] = bufferOr[i];
							}
							ShortArrayObject frame = new ShortArrayObject(buffer, numberOfBytes);
							_caller.getDataLayer().notifyFrame(frame);
						}
					}
				} catch (Exception e) {
					if (DataLayer.getPropertiesManager().getDebugEnabled())
						LOG.error("Error on data received:" + e.getMessage());
				}

			} catch (Exception e) {
				if (DataLayer.getPropertiesManager().getDebugEnabled())
					LOG.error("Error on read from serial data:" + e.getMessage());

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
		if (DataLayer.getPropertiesManager().getDebugEnabled())
			LOG.info("Clear buffer after CPUReset...");
		DataLayer.clearBuffer();
		setIgnoreMessage(false);
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