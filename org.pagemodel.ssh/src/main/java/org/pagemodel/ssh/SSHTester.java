/*
 * Copyright 2021 Matthew Stevenson <pagemodel.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pagemodel.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public class SSHTester<R> {
	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static int DEFAULT_SSH_CMD_TIMEOUT = 20;

	private final R returnObj;
	private final SSHTestContext testContext;
	private SSHSession sshSession;
	private boolean connected = false;
	private SSHAuthenticator authenticator;

	public SSHTester(R returnObj, SSHTestContext testContext) {
		this.returnObj = returnObj;
		this.testContext = testContext;
		this.authenticator = testContext.getSshAuthenticator();
	}

	public SSHCommandTester<R> runCommand(String command, Integer timeoutSec) {
		startSession();
		return new SSHCommandTester<>(command, timeoutSec, this, returnObj, testContext);
	}

	public SSHCommandTester<R> runCommand(String command) {
		return runCommand(command, DEFAULT_SSH_CMD_TIMEOUT);
	}

	public SSHCommandTester<R> sudoToRoot() {
		return sudoToRoot("");
	}

	public SSHCommandTester<R> sudoToRoot(String sudoOpts) {
		return runCommand("sudo-root:" + sudoOpts + ":" + getAuthenticator().getSudoPassword());
	}

	public SSHCommandTester<R> exitSudo() {
		return runCommand("exit-sudo");
	}

	protected SSHTester<R> connect() {
		return connect(testContext.getSshAuthenticator());
	}

	protected SSHTester<R> connect(SSHAuthenticator authenticator) {
		log.info("Connecting to SSH: Host [" + authenticator.getHost() + "], Username [" + authenticator.getUsername() + "], Password [" + authenticator.getPassword() + "]");
		this.authenticator = authenticator;
		startSession();
		assert (connected);
		return this;
	}

	protected R testConnectionFails() {
		return testConnectionFails(testContext.getSshAuthenticator());
	}

	protected R testConnectionFails(SSHAuthenticator authenticator) {
		log.info("Testing SSH connection fails: IP Address [" + authenticator.getHost() + "], Username [" + authenticator.getUsername() + "], Password [" + authenticator.getPassword() + "].");
		this.authenticator = authenticator;
		try {
			startSession();
		} catch (RuntimeException ex) {
			return returnObj;
		}
		throw new RuntimeException("Error: Connection expected to fail, but succeeded. IP Address [" + authenticator.getHost() + "], Username [" + authenticator.getUsername() + "], Password [" + authenticator.getPassword() + "].");
	}

	public R disconnect() {
		log.info("Disconnecting from SSH: IP Address [" + authenticator.getHost() + "]");
		if (!connected) {
			return returnObj;
		}
		try {
			sshSession.close();
		} catch (IOException ex) {
		}
		connected = false;
		sshSession = null;
		return returnObj;
	}

	private void startSession() {
		if (connected) {
			return;
		}
		if (sshSession == null) {
			sshSession = new SSHSession(authenticator);
		}
		try {
			sshSession.connect();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		connected = true;
	}

	protected R getReturnObj() {
		return returnObj;
	}

	protected SSHSession getSshSession() {
		return sshSession;
	}

	protected SSHAuthenticator getAuthenticator() {
		return authenticator;
	}

}
