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

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author Matt Stevenson <matt@pagemodel.org>
 */
public interface SSHAuthenticator {

	public SSHClient connectAndAuthenticate() throws IOException;

	public String getHost();

	public String getUsername();

	public String getPassword();

	public String getSudoPassword();

	class PasswordAuthenticator implements SSHAuthenticator {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private String ipAddress;
		private String username;
		private String password;
		private String sudoPassword;

		public PasswordAuthenticator(String ipAddress, String username, String password, String sudoPassword) {
			this.ipAddress = ipAddress;
			this.username = username;
			this.password = password;
			this.sudoPassword = sudoPassword;
		}

		@Override
		public SSHClient connectAndAuthenticate() throws IOException {
			SSHClient ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(ipAddress);
			ssh.authPassword(username, password);
			return ssh;
		}

		@Override
		public String getHost() {
			return ipAddress;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public String getSudoPassword() {
			return sudoPassword;
		}

	}

	class KeyAuthenticator implements SSHAuthenticator {
		private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

		private String ipAddress;
		private String username;
		private String password;
		private String keyFilePath;
		private String keyFilePassword;
		private String sudoPassword;

		public KeyAuthenticator(String ipAddress, String username, String password, String keyFilePath, String keyFilePassword, String sudoPassword) {
			this.ipAddress = ipAddress;
			this.username = username;
			this.password = password;
			this.keyFilePath = keyFilePath;
			this.keyFilePassword = keyFilePassword;
			this.sudoPassword = sudoPassword;
		}

		@Override
		public SSHClient connectAndAuthenticate() throws IOException {
			SSHClient ssh = new SSHClient();
			ssh.addHostKeyVerifier(new PromiscuousVerifier());
			ssh.connect(ipAddress);
			KeyProvider kp;
			if (keyFilePassword == null) {
				kp = ssh.loadKeys(keyFilePath);
			} else {
				kp = ssh.loadKeys(keyFilePath, keyFilePassword);
			}
			ssh.authPublickey(username, kp);
			return ssh;
		}

		@Override
		public String getHost() {
			return ipAddress;
		}

		@Override
		public String getUsername() {
			return username;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public String getSudoPassword() {
			return sudoPassword;
		}
	}
}
