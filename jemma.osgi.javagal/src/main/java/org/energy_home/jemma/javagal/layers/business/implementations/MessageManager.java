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
package org.energy_home.jemma.javagal.layers.business.implementations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.energy_home.jemma.javagal.layers.business.GalController;
import org.energy_home.jemma.javagal.layers.object.CallbackEntry;
import org.energy_home.jemma.javagal.layers.presentation.Activator;
import org.energy_home.jemma.zgd.MessageListener;
import org.energy_home.jemma.zgd.jaxb.APSMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Address;
import org.energy_home.jemma.zgd.jaxb.Callback;
import org.energy_home.jemma.zgd.jaxb.Filter;
import org.energy_home.jemma.zgd.jaxb.Filter.AddressSpecification;
import org.energy_home.jemma.zgd.jaxb.Filter.MessageSpecification;
import org.energy_home.jemma.zgd.jaxb.InterPANMessageEvent;
import org.energy_home.jemma.zgd.jaxb.Level;

/**
 * Manages received APS messages. When an APS indication is received it is
 * passed to this class' {@code APSMessageIndication} method.
 * 
 * @author 
 *         "Ing. Marco Nieddu <marco.nieddu@consoft.it> or <marco.niedducv@gmail.com> from Consoft Sistemi S.P.A.<http://www.consoft.it>, financed by EIT ICT Labs activity SecSES - Secure Energy Systems (activity id 13030)"
 * 
 */
public class MessageManager {

	private static final Logger LOG = LoggerFactory.getLogger( MessageManager.class );

	/**
	 * The local {@link GalController} reference.
	 */
	GalController gal = null;

	/**
	 * Creates a new instance with a Gal controller reference.
	 * 
	 * @param _gal
	 *            a Gal controller reference.
	 */
	public MessageManager(GalController _gal) {
		gal = _gal;

	}

	/**
	 * Processes the APS indication message trying to dispatch it to the right
	 * destination. The {@link GalController} maintains a collection of
	 * registered callbacks' listeners. This method verifies if a match exists
	 * on that collection for the callback's filter, i.e. looks if one or more
	 * destination(s) for that APS message is present. If it exists, sends the
	 * APS message to all found destinations.
	 * 
	 * @param message
	 *            the indication APSMessageEvent to process.
	 */
	public void APSMessageIndication(final APSMessageEvent message) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					LOG.debug("Aps Message Indication in process...");
				}

				for (CallbackEntry ce : gal.getCallbacks()) {

					Callback callback = ce.getCallback();
					Filter filter = callback.getFilter();
					if (filter.getLevelSpecification().getLevel().get(0).equals(Level.APS_LEVEL)) {
						if (filter.getMessageSpecification().size() > 0) {
							boolean messageSpecificationFound = false;
							for (MessageSpecification ms : filter.getMessageSpecification()) {

								if (ms.getAPSClusterIdentifier() == null) {
									messageSpecificationFound = true;
									// If match we can stop the search loop.
									break;

								} else if (ms.getAPSClusterIdentifier() == message.getClusterID()) {
									messageSpecificationFound = true;
									// If match we can stop the search loop.
									break;
								}
							}
							if (!messageSpecificationFound) {
								// If no Messaging Specification was found,
								// then this callback doesn't match and we
								// can jump to check the next one.
								continue;
							}
						}

						// Address Specification check. If there are at
						// least one address specification in the filter,
						// then we proceed to find a match, else if no
						// address specification is present we assume that
						// the check pass.
						if (filter.getAddressSpecification().size() > 0) {
							boolean addressingSpecificationFound = false;
							for (AddressSpecification as : filter.getAddressSpecification()) {
								// Source Address (Address Specification)
								Address assa = as.getNWKSourceAddress();
								int asnsa = 0xFFFF;
								// If null, then we assume that all address
								// match for this filter, and so we leave
								// the initial value of 0xFFFF.
								if (assa != null) {
									asnsa = assa.getNetworkAddress();
								}
								short assep = -1;

								if (as.getAPSSourceEndpoint() != null)
									assep = as.getAPSSourceEndpoint();
								// Pass if the callback has a broadcast
								// Source Address
								if (asnsa != 0xFFFF) {
									// Source Address
									long msam = message.getSourceAddressMode().longValue();
									Address msa = message.getSourceAddress();
									if (msam == 0x01) {
										// Network address, NO source end
										// point
										int msna0x01 = msa.getNetworkAddress();
										// Pass if the message has a
										// broadcast Source Address
										if (msna0x01 != 0xFFFF) {
											// Don't pass if they differs,
											// so we go ahead on the next
											// iteration in the for cycle
											if (asnsa != msna0x01) {
												continue;
											}
										}
									} else if (msam == 0x02) {
										// Network address, AND source end
										// point present.
										int msna0x02 = msa.getNetworkAddress();
										short msep = message.getSourceEndpoint();
										// Pass if the message has a
										// broadcast Source Address.
										if (msna0x02 != 0xFFFF) {
											// Don't pass if they differs,
											// so we go ahead on the
											// next iteration in for cycle.
											if (asnsa != msna0x02) {
												// Don't pass if they
												// differs, so we go ahead
												// on the next iteration in
												// the for cycle.
												continue;
											} else if (msep != 0xFF) {
												if (msep != assep) {
													// Don't pass if they
													// differs, so we go
													// ahead on the next
													// iteration in the for
													// cycle.
													continue;
												}
											}
										}
									} else if (msam == 0x03) {
										LOG.warn("AIA"); //FIXME is this something expected or not ? maybe a better log message would also help ...

										// ASK No ieee address defined in
										// the AddressSpecification
										// object. We do nothing since we
										// can't compare the values.
									}
								}

								// If reached this point, then a matching
								// Source Address is found for the current
								// AddressSpecification. So we can proceed
								// to check the Destination End Point.

								// Destination End Point (Address
								// Specification)
								if (as.getAPSDestinationEndpoint() == null) {
									addressingSpecificationFound = true;
									break;
								} else {
									short asdep = as.getAPSDestinationEndpoint();
									// Pass if the callback has a broadcast
									// Destination End Point
									if (asdep != 0xFF) {
										long dam = message.getDestinationAddressMode();
										// 0x00 and 0x01 Destination End
										// Point
										// not present
										if (dam > 0x01) {
											short mdep = message.getDestinationEndpoint();
											// Pass if the message has a
											// broadcast Destination End
											// Point
											if (mdep != 0xFF) {
												// Don't pass if they
												// differs,
												// so we go ahead on the
												// next
												// iteration in the for
												// cycle
												if (asdep != mdep) {
													continue;
												}
											}
										}
									}
								}
								// If reached this point, then a matching
								// Destination End Point is also found for
								// the current AddressSpecification. This
								// means that a matching Addressing
								// Specification is found. We can stop here
								// the loop since one match it's enough.
								addressingSpecificationFound = true;
								break;
							}

							if (!addressingSpecificationFound) {
								// If no Addressing Specification was found,
								// then this callback doesn't match and we
								// can jump to check the next one.
								continue;
							}
						}

						// If this point is reached, then a matching
						// callback is found. Notify the message to its
						// destination.

						MessageListener napml = ce.getGenericDestination();
						if (napml != null)
							napml.notifyAPSMessage(message);

						// Add it to the list of already notified
						// destinations.

					}
				}

			}
		};
		thr.setName("Thread APSMessageIndication(final APSMessageEvent message)");
		thr.start();
	}

	/**
	 * Processes the InterPAN indication message trying to dispatch it to the
	 * right destination. The {@link GalController} maintains a collection of
	 * registered callbacks' listeners. This method verifies if a match exists
	 * on that collection for the callback's filter, i.e. looks if one or more
	 * destination(s) for that APS message is present. If it exists, sends the
	 * APS message to all found destinations.
	 * 
	 * @param message
	 *            the indication APSMessageEvent to process.
	 */
	public void InterPANMessageIndication(final InterPANMessageEvent message) {
		Thread thr = new Thread() {
			@Override
			public void run() {
				if (gal.getPropertiesManager().getDebugEnabled()) {
					LOG.debug("Aps Message Indication in process...");
				}

				for (CallbackEntry ce : gal.getCallbacks()) {

					Callback callback = ce.getCallback();
					Filter filter = callback.getFilter();
					if (filter.getLevelSpecification().getLevel().get(0).equals(Level.INTRP_LEVEL)) {
						if (filter.getMessageSpecification().size() > 0) {
							boolean messageSpecificationFound = false;
							for (MessageSpecification ms : filter.getMessageSpecification()) {

								if (ms.getAPSClusterIdentifier() == null) {
									messageSpecificationFound = true;
									// If match we can stop the search loop.
									break;

								} else if (ms.getAPSClusterIdentifier() == message.getClusterID()) {
									messageSpecificationFound = true;
									// If match we can stop the search loop.
									break;
								}
							}
							if (!messageSpecificationFound) {
								// If no Messaging Specification was found,
								// then this callback doesn't match and we
								// can jump to check the next one.
								continue;
							}
						}

						// Address Specification check. If there are at
						// least one address specification in the filter,
						// then we proceed to find a match, else if no
						// address specification is present we assume that
						// the check pass.
						if (filter.getAddressSpecification().size() > 0) {
							boolean addressingSpecificationFound = false;
							for (AddressSpecification as : filter.getAddressSpecification()) {
								// Source Address (Address Specification)
								Address assa = as.getNWKSourceAddress();
								int asnsa = 0xFFFF;
								// If null, then we assume that all address
								// match for this filter, and so we leave
								// the initial value of 0xFFFF.
								if (assa != null) {
									asnsa = assa.getNetworkAddress();
								}
								short assep = -1;

								if (as.getAPSSourceEndpoint() != null)
									assep = as.getAPSSourceEndpoint();
								// Pass if the callback has a broadcast
								// Source Address
								if (asnsa != 0xFFFF) {
									// Source Address
									long msam = message.getSrcAddressMode();
									Address msa = message.getSrcAddress();
									if (msam == 0x01) {
										// Network address, NO source end
										// point
										int msna0x01 = msa.getNetworkAddress();
										// Pass if the message has a
										// broadcast Source Address
										if (msna0x01 != 0xFFFF) {
											// Don't pass if they differs,
											// so we go ahead on the next
											// iteration in the for cycle
											if (asnsa != msna0x01) {
												continue;
											}
										}
									} else if (msam == 0x02) {
										// Network address, AND source end
										// point present.
										int msna0x02 = msa.getNetworkAddress();
										// Pass if the message has a
										// broadcast Source Address.
										if (msna0x02 != 0xFFFF) {
											// Don't pass if they differs,
											// so we go ahead on the
											// next iteration in for cycle.
											if (asnsa != msna0x02) {
												// Don't pass if they
												// differs, so we go ahead
												// on the next iteration in
												// the for cycle.
												continue;
											}
										}
									}
								}
							}

							// If reached this point, then a matching
							// Source Address is found for the current
							// AddressSpecification. So we can proceed
							// to check the Destination End Point.

							// If reached this point, then a matching
							// Destination End Point is also found for
							// the current AddressSpecification. This
							// means that a matching Addressing
							// Specification is found. We can stop here
							// the loop since one match it's enough.
							addressingSpecificationFound = true;
							break;
						}

					}

					// If this point is reached, then a matching
					// callback is found. Notify the message to its
					// destination.

					MessageListener napml = ce.getGenericDestination();
					if (napml != null)
						napml.notifyInterPANMessage(message);

					// Add it to the list of already notified
					// destinations.

				}
			}

		};
		thr.setName("Thread InterPANMessageIndication(final InterPANMessageEvent message)");
		thr.start();
	}
}