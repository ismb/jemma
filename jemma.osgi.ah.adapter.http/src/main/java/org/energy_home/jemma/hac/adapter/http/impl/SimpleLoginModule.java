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
package org.energy_home.jemma.hac.adapter.http.impl;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class SimpleLoginModule implements LoginModule {

	private CallbackHandler handler;
	private Subject subject;
	private Principal user;

	public boolean abort() throws LoginException {
		return true;
	}

	public boolean commit() throws LoginException {
		subject.getPrincipals().add(user);
		return true;
	}

	private Principal createUser(final Callback[] callbacks) {
		return new Principal() {
			public boolean equals(Object obj) {
				if (!(obj instanceof Principal))
					return false;
				return getName().equals(((Principal) obj).getName());
			}

			public String getName() {
				return ((NameCallback) callbacks[0]).getName();
			}

			public int hashCode() {
				return getName().hashCode();
			}

			public String toString() {
				return getName().toString();
			}
		};
	}

	public void initialize(Subject subject, final CallbackHandler handler,
			Map arg2, Map arg3) {
		this.handler = handler;
		this.subject = subject;
	}

	public boolean login() throws LoginException {
		final Callback[] callbacks = { new NameCallback("Username"),
				new PasswordCallback("Password", false) };

		try {
			handler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException(e.getMessage());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.getMessage());
		}
		String name = ((NameCallback) callbacks[0]).getName();
		String password = new String(((PasswordCallback) callbacks[1])
				.getPassword());
		if ("user".equals(name) && "password".equals(password)) {
			user = createUser(callbacks);
			return true;
		} else
			throw new LoginException("Login failed");
	}

	public boolean logout() throws LoginException {
		return true;
	}
}
